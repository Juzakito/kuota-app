package com.kuota.backend

import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DemoStorePaymentFlowTest {

    private lateinit var directory: Path
    private lateinit var accounting: FileAccountingStore
    private lateinit var store: DemoStore

    @BeforeTest
    fun setUp() {
        directory = Files.createTempDirectory("kuota-store-flow")
        accounting = newAccounting()
        store = newStore()
    }

    @AfterTest
    fun tearDown() {
        accounting.close()
    }

    @Test
    fun `demo purchase and payment write a balanced ledger`() = runBlocking {
        val (quote, purchase) = newPurchase()
        assertEquals(30_000L, quote.buyerDownPaymentCents)
        assertEquals(5_000L, quote.buyerFeeCents)
        assertEquals(60_000L, quote.supplierImmediatePayoutCents)
        assertEquals(4_000L, quote.supplierFeeCents)
        assertEquals(75_000L, quote.installments.sumOf { it.amountCents })

        val purchaseLedger = accounting.transactions().single()
        assertEquals("PURCHASE_RECOGNITION", purchaseLedger.kind)
        assertEquals(purchase.purchaseId, purchaseLedger.sourceId)

        val installment = store.getInstallments(purchase.purchaseId)!!.first()
        assertEquals(18_750L, installment.amountCents)
        val intent = createIntent(installment.installmentId)
        assertEquals("PENDING", intent.status)
        assertEquals("PAYMENT_PENDING", store.getInstallments(purchase.purchaseId)!!.first().status)

        val paid = applyEvent(intent.paymentId, "SUCCEED")
        assertEquals("SUCCEEDED", paid.status)
        assertEquals("MATCHED", paid.reconciliationStatus)
        assertEquals("DEMO_PAID", store.getInstallments(purchase.purchaseId)!!.first().status)

        val ledger = accounting.transactions()
        assertEquals(2, ledger.size)
        assertEquals("INSTALLMENT_PAYMENT", ledger.first().kind)
        assertEquals("DEMO", ledger.first().sourceType)

        val balances = balances()
        assertEquals(18_750L, balances[LedgerAccount.CASH])
        assertEquals(86_250L, balances[LedgerAccount.BUYER_RECEIVABLE])
        assertEquals(-60_000L, balances[LedgerAccount.SUPPLIER_PAYABLE_IMMEDIATE])
        assertEquals(-36_000L, balances[LedgerAccount.SUPPLIER_PAYABLE_DEFERRED])
        assertEquals(-5_000L, balances[LedgerAccount.REVENUE_BUYER_FEE])
        assertEquals(-4_000L, balances[LedgerAccount.REVENUE_SUPPLIER_FEE])
        assertEquals(0L, balances.values.sum())
    }

    @Test
    fun `a refund reverses cash and the buyer receivable`() = runBlocking {
        val (_, purchase) = newPurchase()
        val installment = store.getInstallments(purchase.purchaseId)!!.first()
        val intent = createIntent(installment.installmentId)
        applyEvent(intent.paymentId, "SUCCEED")

        val refunded = applyEvent(intent.paymentId, "REFUND")
        assertEquals("REFUNDED", refunded.status)
        assertEquals("MATCHED", refunded.reconciliationStatus)
        assertEquals("DEMO_REFUNDED", store.getInstallments(purchase.purchaseId)!!.first().status)
        assertEquals("INSTALLMENT_REFUND", accounting.transactions().first().kind)

        val balances = balances()
        assertEquals(0L, balances[LedgerAccount.CASH])
        assertEquals(105_000L, balances[LedgerAccount.BUYER_RECEIVABLE])
        assertEquals(0L, balances.values.sum())
    }

    @Test
    fun `an amount mismatch stays in review until manual reconciliation`() = runBlocking {
        val (_, purchase) = newPurchase()
        val installment = store.getInstallments(purchase.purchaseId)!!.first()
        val intent = createIntent(installment.installmentId)

        val review = applyEvent(intent.paymentId, "SUCCEED_AMOUNT_MISMATCH")
        assertEquals("SUCCEEDED", review.status)
        assertEquals("AMOUNT_MISMATCH", review.reconciliationStatus)
        assertEquals(18_751L, review.providerAmountCents)
        assertEquals("DEMO_PAYMENT_REVIEW", store.getInstallments(purchase.purchaseId)!!.first().status)
        assertEquals(1, store.getReconciliationQueue().size)
        assertEquals(1, accounting.transactions().size)

        val reconciled = applyEvent(intent.paymentId, "RECONCILE_MATCH")
        assertEquals("SUCCEEDED", reconciled.status)
        assertEquals("MANUALLY_RESOLVED", reconciled.reconciliationStatus)
        assertEquals("DEMO_PAID", store.getInstallments(purchase.purchaseId)!!.first().status)
        assertTrue(store.getReconciliationQueue().isEmpty())
        assertEquals("INSTALLMENT_RECONCILIATION", accounting.transactions().first().kind)

        val balances = balances()
        assertEquals(18_751L, balances[LedgerAccount.CASH])
        assertEquals(86_250L, balances[LedgerAccount.BUYER_RECEIVABLE])
        assertEquals(-1L, balances[LedgerAccount.PAYMENT_VARIANCE])
        assertEquals(0L, balances.values.sum())
    }

    @Test
    fun `idempotency replays records and rejects conflicting reuse`() = runBlocking {
        val quote = store.createQuote(quoteRequest())
        val request = purchaseRequest(quote)
        val first = store.createPurchase("purchase-key", request) as PurchaseCreateResult.Success
        val replayed = store.createPurchase("purchase-key", request) as PurchaseCreateResult.Success
        assertTrue(replayed.replayed)
        assertEquals(first.purchase.purchaseId, replayed.purchase.purchaseId)
        assertEquals(1, accounting.transactions().size)

        val otherQuote = store.createQuote(quoteRequest())
        val conflict = store.createPurchase("purchase-key", purchaseRequest(otherQuote)) as PurchaseCreateResult.Rejected
        assertEquals("IDEMPOTENCY_KEY_REUSED", conflict.code)

        val duplicateQuote = store.createPurchase("another-key", request) as PurchaseCreateResult.Rejected
        assertEquals("QUOTE_ALREADY_USED", duplicateQuote.code)

        val installment = store.getInstallments(first.purchase.purchaseId)!!.first()
        val intent = createIntent(installment.installmentId, "intent-key")
        val replayedIntent = store.createPaymentIntent(
            "intent-key",
            installment.installmentId,
            CreatePaymentIntentRequest(DEMO_PAYMENT_METHOD)
        ) as PaymentCreateResult.Success
        assertTrue(replayedIntent.replayed)
        assertEquals(intent.paymentId, replayedIntent.payment.paymentId)

        val secondInstallment = store.getInstallments(first.purchase.purchaseId)!![1]
        val intentConflict = store.createPaymentIntent(
            "intent-key",
            secondInstallment.installmentId,
            CreatePaymentIntentRequest(DEMO_PAYMENT_METHOD)
        ) as PaymentCreateResult.Rejected
        assertEquals("IDEMPOTENCY_KEY_REUSED", intentConflict.code)

        val event = applyEvent(intent.paymentId, "PROCESSING", key = "event-key")
        val replayEvent = store.applyDemoPaymentEvent(
            "event-key",
            intent.paymentId,
            DemoPaymentEventRequest("PROCESSING")
        ) as PaymentEventResult.Success
        assertEquals(event.status, replayEvent.payment.status)
        val eventConflict = store.applyDemoPaymentEvent(
            "event-key",
            intent.paymentId,
            DemoPaymentEventRequest("SUCCEED")
        ) as PaymentEventResult.Rejected
        assertEquals("IDEMPOTENCY_KEY_REUSED", eventConflict.code)
    }

    @Test
    fun `store and ledger survive a restart without duplicating recognition`() {
        val quote = store.createQuote(quoteRequest())
        val request = purchaseRequest(quote)
        store.createPurchase("restart-key", request)
        assertEquals(1, accounting.transactions().size)

        accounting.close()
        accounting = newAccounting()
        store = newStore()

        assertEquals(1, store.getPurchaseHistory().size)
        assertEquals(1, accounting.transactions().size)
        val replayed = store.createPurchase("restart-key", request) as PurchaseCreateResult.Success
        assertTrue(replayed.replayed)
        assertEquals(1, accounting.transactions().size)
    }

    @Test
    fun `production store blocks demo events and settles real provider payments`() = runBlocking {
        val productionAccounting = FileAccountingStore(
            directory.resolve("production-ledger.ndjson"),
            directory.resolve("production-webhooks.ndjson")
        )
        try {
            val productionStore = DemoStore(
                directory.resolve("production-store.json"),
                accounting = productionAccounting,
                environment = LEDGER_ENV_PRODUCTION
            )
            val quote = productionStore.createQuote(quoteRequest())
            val purchase = productionStore.createPurchase("production-key", purchaseRequest(quote))
                as PurchaseCreateResult.Success
            val installment = productionStore.getInstallments(purchase.purchase.purchaseId)!!.first()
            val intent = productionStore.createPaymentIntent(
                "production-intent",
                installment.installmentId,
                CreatePaymentIntentRequest(DEMO_PAYMENT_METHOD)
            ) as PaymentCreateResult.Success

            val rejected = productionStore.applyDemoPaymentEvent(
                "production-event",
                intent.payment.paymentId,
                DemoPaymentEventRequest("SUCCEED")
            ) as PaymentEventResult.Rejected
            assertEquals("DEMO_EVENTS_DISABLED", rejected.code)

            val settlement = productionStore.settle(
                approvedSettlement(intent.payment.paymentId, "mp-payment-1", installment.amountCents)
            )
            assertIs<SettlementResult.Applied>(settlement)
            assertEquals("SUCCEEDED", settlement.paymentStatus)
            assertEquals("MATCHED", settlement.reconciliationStatus)
            assertEquals("PAID", productionStore.getInstallments(purchase.purchase.purchaseId)!!.first().status)
            assertEquals(2, productionAccounting.transactions().size)
        } finally {
            productionAccounting.close()
        }
    }

    @Test
    fun `a second provider payment id is flagged without double booking`() = runBlocking {
        val (_, purchase) = newPurchase()
        val installment = store.getInstallments(purchase.purchaseId)!!.first()
        val intent = createIntent(installment.installmentId)

        assertIs<SettlementResult.Applied>(
            store.settle(approvedSettlement(intent.paymentId, "psp-payment-1", installment.amountCents))
        )
        val duplicate = store.settle(
            approvedSettlement(intent.paymentId, "psp-payment-2", installment.amountCents)
        )
        assertIs<SettlementResult.Review>(duplicate)
        assertEquals("DUPLICATE_PROVIDER_PAYMENT", duplicate.reasonCode)
        assertEquals(2, accounting.transactions().size)
        assertEquals("DEMO_PAID", store.getInstallments(purchase.purchaseId)!!.first().status)
    }

    @Test
    fun `hmac webhook settles a demo payment end to end`() = runBlocking {
        val (_, purchase) = newPurchase()
        val installment = store.getInstallments(purchase.purchaseId)!!.first()
        val intent = createIntent(installment.installmentId)
        val processor = PaymentWebhookProcessor(store, accounting, LEDGER_ENV_DEMO, null)
        val body = """
            {
              "event_id": "evt-e2e-1",
              "provider": "demo",
              "payment_id": "${intent.paymentId}",
              "provider_payment_id": "demo-provider-e2e",
              "status": "APPROVED",
              "amount_cents": ${installment.amountCents},
              "currency": "PEN",
              "occurred_at": "2026-10-20T12:00:00Z"
            }
        """.trimIndent()

        val first = processor.processHmac(body)
        assertEquals(200, first.httpStatus)
        assertEquals("APPLIED", first.body["code"])
        assertEquals("DEMO_PAID", store.getInstallments(purchase.purchaseId)!!.first().status)
        assertEquals(2, accounting.transactions().size)

        val duplicate = processor.processHmac(body)
        assertEquals("DUPLICATE", duplicate.body["code"])
        assertEquals(2, accounting.transactions().size)
    }

    @Test
    fun `purchase and payment reads are scoped to their owner`() = runBlocking {
        val quote = store.createQuote(quoteRequest())
        val purchase = (store.createPurchase("owner-key", purchaseRequest(quote), ownerUserId = "user-a")
            as PurchaseCreateResult.Success).purchase
        val installment = store.getInstallments(purchase.purchaseId, "user-a")!!.first()

        assertNotNull(store.getPurchase(purchase.purchaseId, "user-a"))
        assertNull(store.getPurchase(purchase.purchaseId, "user-b"))
        assertNull(store.getInstallments(purchase.purchaseId, "user-b"))
        assertEquals(listOf(purchase.purchaseId), store.getPurchaseHistory("user-a").map { it.purchaseId })
        assertTrue(store.getPurchaseHistory("user-b").isEmpty())

        val intent = store.createPaymentIntent(
            "owner-intent",
            installment.installmentId,
            CreatePaymentIntentRequest(DEMO_PAYMENT_METHOD),
            "user-a"
        ) as PaymentCreateResult.Success
        assertNotNull(store.getPayment(intent.payment.paymentId, "user-a"))
        assertNull(store.getPayment(intent.payment.paymentId, "user-b"))

        val crossOwnerIntent = store.createPaymentIntent(
            "owner-intent-b",
            installment.installmentId,
            CreatePaymentIntentRequest(DEMO_PAYMENT_METHOD),
            "user-b"
        ) as PaymentCreateResult.Rejected
        assertEquals("INSTALLMENT_NOT_FOUND", crossOwnerIntent.code)

        val crossOwnerEvent = store.applyDemoPaymentEvent(
            "owner-event-b",
            intent.payment.paymentId,
            DemoPaymentEventRequest("SUCCEED"),
            "user-b"
        ) as PaymentEventResult.Rejected
        assertEquals("PAYMENT_NOT_FOUND", crossOwnerEvent.code)
        assertEquals("PAYMENT_PENDING", store.getInstallments(purchase.purchaseId, "user-a")!!.first().status)
    }

    @Test
    fun `an idempotency key cannot be replayed by another owner`() {
        val quote = store.createQuote(quoteRequest())
        val request = purchaseRequest(quote)
        assertIs<PurchaseCreateResult.Success>(store.createPurchase("shared-key", request, "user-a"))

        val replay = store.createPurchase("shared-key", request, "user-b")
        assertIs<PurchaseCreateResult.Rejected>(replay)
        assertEquals("IDEMPOTENCY_KEY_REUSED", replay.code)
        assertTrue(store.getPurchaseHistory("user-b").isEmpty())
    }

    @Test
    fun `purchase ownership survives a restart`() {
        val quote = store.createQuote(quoteRequest())
        val purchase = (store.createPurchase("restart-owner", purchaseRequest(quote), "user-a")
            as PurchaseCreateResult.Success).purchase

        accounting.close()
        accounting = newAccounting()
        store = newStore()

        assertNotNull(store.getPurchase(purchase.purchaseId, "user-a"))
        assertNull(store.getPurchase(purchase.purchaseId, "user-b"))
        assertEquals(1, store.getPurchaseHistory("user-a").size)
    }

    @Test
    fun `unowned purchases are not visible to authenticated owners`() {
        val (_, purchase) = newPurchase()

        assertNotNull(store.getPurchase(purchase.purchaseId))
        assertNull(store.getPurchase(purchase.purchaseId, "user-a"))
        assertTrue(store.getPurchaseHistory("user-a").isEmpty())
    }

    private fun newAccounting(): FileAccountingStore = FileAccountingStore(
        directory.resolve("ledger.ndjson"),
        directory.resolve("webhooks.ndjson")
    )

    private fun newStore(): DemoStore = DemoStore(
        file = directory.resolve("store.json"),
        accounting = accounting
    )

    private fun quoteRequest(): QuoteRequest = QuoteRequest(
        businessId = "demo-buyer-001",
        supplierQrPayload = "KUOTA-DEMO-SUPPLIER-LIMA",
        amountCents = 100_000,
        termDays = 30,
        eligibilityDecisionId = "demo-decision"
    )

    private fun purchaseRequest(quote: QuoteResponse): CreatePurchaseRequest = CreatePurchaseRequest(
        quoteId = quote.quoteId,
        quoteVersion = quote.quoteVersion,
        acceptedTermsVersion = DEMO_NOT_ACCEPTED_VERSION,
        acceptedPrivacyNoticeVersion = DEMO_NOT_ACCEPTED_VERSION
    )

    private fun newPurchase(): Pair<QuoteResponse, PurchaseResponse> {
        val quote = store.createQuote(quoteRequest())
        val purchase = store.createPurchase("purchase-1", purchaseRequest(quote)) as PurchaseCreateResult.Success
        return quote to purchase.purchase
    }

    private suspend fun createIntent(installmentId: String, key: String = "intent-${UUID.randomUUID()}"): PaymentRecord {
        val result = store.createPaymentIntent(key, installmentId, CreatePaymentIntentRequest(DEMO_PAYMENT_METHOD))
        return (result as PaymentCreateResult.Success).payment
    }

    private fun applyEvent(
        paymentId: String,
        eventType: String,
        key: String = "event-${UUID.randomUUID()}"
    ): PaymentRecord {
        val result = store.applyDemoPaymentEvent(key, paymentId, DemoPaymentEventRequest(eventType))
        return (result as PaymentEventResult.Success).payment
    }

    private fun approvedSettlement(
        paymentId: String,
        providerPaymentId: String,
        amountCents: Long
    ): ProviderSettlement = ProviderSettlement(
        internalPaymentId = paymentId,
        provider = "mercadopago",
        providerPaymentId = providerPaymentId,
        status = ProviderPaymentStatus.APPROVED,
        amountCents = amountCents,
        currency = "PEN",
        occurredAt = "2026-10-20T12:00:00Z",
        reasonCode = "MERCADO_PAGO_APPROVED"
    )

    private fun balances(): Map<LedgerAccount, Long> =
        accounting.balances(LEDGER_ENV_DEMO).associate { it.account to it.netDebitCents }
}

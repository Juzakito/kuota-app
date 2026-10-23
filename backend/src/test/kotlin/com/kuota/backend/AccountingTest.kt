package com.kuota.backend

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AccountingTest {

    private val occurredAt = "2026-10-20T12:00:00Z"

    @Test
    fun `purchase recognition is balanced and applies both commissions`() {
        val transaction = purchaseTransaction()

        assertEquals(105_000L, debits(transaction))
        assertEquals(105_000L, credits(transaction))
        assertEquals(105_000L, posting(transaction, LedgerAccount.BUYER_RECEIVABLE, PostingDirection.DEBIT))
        assertEquals(60_000L, posting(transaction, LedgerAccount.SUPPLIER_PAYABLE_IMMEDIATE, PostingDirection.CREDIT))
        assertEquals(36_000L, posting(transaction, LedgerAccount.SUPPLIER_PAYABLE_DEFERRED, PostingDirection.CREDIT))
        assertEquals(5_000L, posting(transaction, LedgerAccount.REVENUE_BUYER_FEE, PostingDirection.CREDIT))
        assertEquals(4_000L, posting(transaction, LedgerAccount.REVENUE_SUPPLIER_FEE, PostingDirection.CREDIT))
    }

    @Test
    fun `purchase rejects arithmetic that does not match the commissions`() {
        assertFailsWith<IllegalArgumentException> {
            LedgerTransactions.purchase(
                environment = LEDGER_ENV_DEMO,
                purchaseId = "purchase-bad-buyer",
                occurredAt = occurredAt,
                purchaseAmountCents = 100_000,
                buyerDownPaymentCents = 30_000,
                buyerInstallmentsTotalCents = 75_001,
                buyerFeeCents = 5_000,
                supplierImmediateCents = 60_000,
                supplierDeferredNetCents = 36_000,
                supplierFeeCents = 4_000,
                supplierId = "supplier-1"
            )
        }

        assertFailsWith<IllegalArgumentException> {
            LedgerTransactions.purchase(
                environment = LEDGER_ENV_DEMO,
                purchaseId = "purchase-bad-supplier",
                occurredAt = occurredAt,
                purchaseAmountCents = 100_000,
                buyerDownPaymentCents = 30_000,
                buyerInstallmentsTotalCents = 75_000,
                buyerFeeCents = 5_000,
                supplierImmediateCents = 60_000,
                supplierDeferredNetCents = 35_000,
                supplierFeeCents = 4_000,
                supplierId = "supplier-1"
            )
        }
    }

    @Test
    fun `payment refund and reconciliation transactions stay balanced`() {
        val paid = LedgerTransactions.installmentPaid(
            environment = LEDGER_ENV_DEMO,
            internalPaymentId = "payment-1",
            provider = "demo",
            providerPaymentId = "demo-provider-1",
            purchaseId = "purchase-1",
            installmentId = "installment-1",
            amountCents = 18_750,
            occurredAt = occurredAt
        )
        assertEquals(18_750L, debits(paid))
        assertEquals(18_750L, credits(paid))

        val refunded = LedgerTransactions.installmentRefunded(
            environment = LEDGER_ENV_DEMO,
            internalPaymentId = "payment-1",
            provider = "demo",
            providerPaymentId = "demo-provider-1",
            purchaseId = "purchase-1",
            installmentId = "installment-1",
            amountCents = 18_750,
            occurredAt = occurredAt,
            reason = "CHARGEBACK"
        )
        assertEquals("INSTALLMENT_CHARGEBACK", refunded.kind)
        assertEquals(18_750L, debits(refunded))
        assertEquals(18_750L, credits(refunded))

        val overpaid = LedgerTransactions.installmentReconciled(
            environment = LEDGER_ENV_DEMO,
            internalPaymentId = "payment-2",
            provider = "demo",
            providerPaymentId = "demo-provider-2",
            purchaseId = "purchase-1",
            installmentId = "installment-2",
            expectedAmountCents = 18_750,
            providerAmountCents = 18_751,
            occurredAt = occurredAt
        )
        assertEquals(18_751L, debits(overpaid))
        assertEquals(18_751L, credits(overpaid))
        assertEquals(1L, posting(overpaid, LedgerAccount.PAYMENT_VARIANCE, PostingDirection.CREDIT))

        val underpaid = LedgerTransactions.installmentReconciled(
            environment = LEDGER_ENV_DEMO,
            internalPaymentId = "payment-3",
            provider = "demo",
            providerPaymentId = "demo-provider-3",
            purchaseId = "purchase-1",
            installmentId = "installment-3",
            expectedAmountCents = 18_750,
            providerAmountCents = 18_700,
            occurredAt = occurredAt
        )
        assertEquals(18_750L, debits(underpaid))
        assertEquals(18_750L, credits(underpaid))
        assertEquals(50L, posting(underpaid, LedgerAccount.PAYMENT_VARIANCE, PostingDirection.DEBIT))
    }

    @Test
    fun `unbalanced or invalid transactions are rejected`() {
        val unbalanced = purchaseTransaction().copy(
            postings = listOf(
                LedgerPosting(LedgerAccount.CASH, PostingDirection.DEBIT, 100),
                LedgerPosting(LedgerAccount.BUYER_RECEIVABLE, PostingDirection.CREDIT, 99)
            )
        )
        assertFailsWith<IllegalArgumentException> { unbalanced.requireValid() }

        val negative = purchaseTransaction().copy(
            postings = listOf(
                LedgerPosting(LedgerAccount.CASH, PostingDirection.DEBIT, -100),
                LedgerPosting(LedgerAccount.BUYER_RECEIVABLE, PostingDirection.CREDIT, -100)
            )
        )
        assertFailsWith<IllegalArgumentException> { negative.requireValid() }

        val wrongEnvironment = purchaseTransaction().copy(environment = "STAGING")
        assertFailsWith<IllegalArgumentException> { wrongEnvironment.requireValid() }
    }

    @Test
    fun `file ledger persists and deduplicates append only entries`() {
        val directory = Files.createTempDirectory("kuota-ledger-test")
        val ledger = directory.resolve("ledger.ndjson")
        val webhooks = directory.resolve("webhooks.ndjson")

        FileAccountingStore(ledger, webhooks).use { store ->
            val first = purchaseTransaction()
            val recorded = store.append(first)
            assertIs<LedgerAppendResult.Recorded>(recorded)

            val duplicate = store.append(first.copy(transactionId = "another-id"))
            assertIs<LedgerAppendResult.Duplicate>(duplicate)
            assertEquals(first.transactionId, duplicate.transactionId)

            assertFailsWith<IllegalStateException> {
                store.append(
                    first.copy(
                        transactionId = "conflict-id",
                        postings = listOf(
                            LedgerPosting(LedgerAccount.CASH, PostingDirection.DEBIT, 1),
                            LedgerPosting(LedgerAccount.BUYER_RECEIVABLE, PostingDirection.CREDIT, 1)
                        )
                    )
                )
            }

            store.append(
                LedgerTransactions.installmentPaid(
                    environment = LEDGER_ENV_DEMO,
                    internalPaymentId = "payment-1",
                    provider = "demo",
                    providerPaymentId = "provider-1",
                    purchaseId = "purchase-1",
                    installmentId = "installment-1",
                    amountCents = 18_750,
                    occurredAt = occurredAt
                )
            )
            assertEquals(2, store.transactions().size)
            assertEquals(2, Files.readAllLines(ledger).size)
        }

        FileAccountingStore(ledger, webhooks).use { reopened ->
            assertEquals(2, reopened.transactions().size)
            val balances = reopened.balances(LEDGER_ENV_DEMO).associate { it.account to it.netDebitCents }
            assertEquals(18_750L, balances[LedgerAccount.CASH])
            assertEquals(86_250L, balances[LedgerAccount.BUYER_RECEIVABLE])
            assertEquals(0, balances.values.sum())
        }

        val corrupt = Files.createTempDirectory("kuota-ledger-corrupt").resolve("ledger.ndjson")
        Files.writeString(corrupt, "{not-json}\n")
        assertFailsWith<IllegalStateException> {
            FileAccountingStore(corrupt, corrupt.resolveSibling("webhooks.ndjson"))
        }
    }

    @Test
    fun `webhook events are claimed once and failed events can be retried`() {
        val directory = Files.createTempDirectory("kuota-webhook-test")
        val ledger = directory.resolve("ledger.ndjson")
        val webhooks = directory.resolve("webhooks.ndjson")

        FileAccountingStore(ledger, webhooks).use { store ->
            assertIs<WebhookClaim.Claimed>(
                store.beginWebhook("mercadopago", "event-1", LEDGER_ENV_DEMO, "hash-1")
            )
            assertIs<WebhookClaim.InProgress>(
                store.beginWebhook("mercadopago", "event-1", LEDGER_ENV_DEMO, "hash-1")
            )

            store.finishWebhook("mercadopago", "event-1", WEBHOOK_FAILED, "LOOKUP_FAILED")
            assertIs<WebhookClaim.Claimed>(
                store.beginWebhook("mercadopago", "event-1", LEDGER_ENV_DEMO, "hash-1")
            )

            store.finishWebhook("mercadopago", "event-1", WEBHOOK_COMPLETED, "APPLIED")
            val duplicate = store.beginWebhook("mercadopago", "event-1", LEDGER_ENV_DEMO, "hash-1")
            assertIs<WebhookClaim.Duplicate>(duplicate)
            assertEquals(WEBHOOK_COMPLETED, duplicate.status)
        }

        FileAccountingStore(ledger, webhooks).use { reopened ->
            assertIs<WebhookClaim.Duplicate>(
                reopened.beginWebhook("mercadopago", "event-1", LEDGER_ENV_DEMO, "hash-1")
            )
            assertTrue(reopened.transactions().isEmpty())
        }
    }

    private fun purchaseTransaction(): LedgerTransaction = LedgerTransactions.purchase(
        environment = LEDGER_ENV_DEMO,
        purchaseId = "purchase-1",
        occurredAt = occurredAt,
        purchaseAmountCents = 100_000,
        buyerDownPaymentCents = 30_000,
        buyerInstallmentsTotalCents = 75_000,
        buyerFeeCents = 5_000,
        supplierImmediateCents = 60_000,
        supplierDeferredNetCents = 36_000,
        supplierFeeCents = 4_000,
        supplierId = "supplier-1"
    )

    private fun debits(transaction: LedgerTransaction): Long =
        transaction.postings.filter { it.direction == PostingDirection.DEBIT }.sumOf { it.amountCents }

    private fun credits(transaction: LedgerTransaction): Long =
        transaction.postings.filter { it.direction == PostingDirection.CREDIT }.sumOf { it.amountCents }

    private fun posting(
        transaction: LedgerTransaction,
        account: LedgerAccount,
        direction: PostingDirection
    ): Long = transaction.postings.single { it.account == account && it.direction == direction }.amountCents
}

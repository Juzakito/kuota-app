package com.kouta.app.data.remote

import android.content.Context
import com.kouta.app.core.util.PeruBusinessCalendar
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.File
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Base64
import java.util.UUID

internal class LocalDemoApiException(
    val httpCode: Int,
    val code: String,
    override val message: String,
    val retryable: Boolean = false
) : RuntimeException(message)

@Serializable
private data class LocalDemoQrPayload(
    val transactionId: String,
    val supplierId: String,
    val supplierName: String,
    val amountCents: Long,
    val expiresAtEpochMillis: Long,
    val signature: String = "local-demo-sig"
)

@Serializable
private data class LocalDemoState(
    val decisions: MutableMap<String, EligibilityCheckDto> = mutableMapOf(),
    val quotes: MutableMap<String, QuoteDto> = mutableMapOf(),
    val purchases: MutableList<PurchaseHistoryItemDto> = mutableListOf(),
    val claims: MutableList<ClaimReceiptDto> = mutableListOf(),
    val inbox: MutableList<InboxMessageDto> = mutableListOf()
)

/**
 * Motor demo local embebido para builds DEBUG cuando el teléfono físico no puede alcanzar el servidor local de la PC
 * (por ejemplo, fuera de la red Wi-Fi, datos móviles o bloqueo de firewall).
 * Replica exactamente los contratos y reglas de negocio del backend Ktor según PRD v5.2.
 */
internal class LocalDemoKuotaApi private constructor(context: Context) : KuotaApi {
    private val lock = Any()
    private val file = File(context.applicationContext.filesDir, "kuota_local_demo_server.json")
    private val limaZone = ZoneId.of("America/Lima")
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    private var state: LocalDemoState = loadState()

    private fun loadState(): LocalDemoState {
        if (!file.exists()) return LocalDemoState()
        return runCatching {
            json.decodeFromString(LocalDemoState.serializer(), file.readText())
        }.getOrElse { LocalDemoState() }
    }

    private fun saveState() {
        runCatching {
            file.writeText(json.encodeToString(LocalDemoState.serializer(), state))
        }
    }

    private fun percentageOf(amountCents: Long, basisPoints: Int): Long =
        (amountCents * basisPoints.toLong() + 5_000L) / 10_000L

    override suspend fun health(): Map<String, String> = mapOf(
        "status" to "ok",
        "service" to "kuota-local-embedded-demo",
        "environment" to "DEMO_ONLY"
    )

    override suspend fun requestOtp(body: RequestOtpBody): RequestOtpDto {
        val digits = body.phone.filter(Char::isDigit).takeLast(9)
        if (digits.length != 9) {
            throw LocalDemoApiException(400, "PHONE_INVALID", "El número celular debe tener 9 dígitos.")
        }
        return RequestOtpDto(
            otpCodeDebug = "123456",
            message = "Modo demo local activo: usa el código 123456.",
            expiresInSeconds = 300
        )
    }

    override suspend fun verifyOtp(body: VerifyOtpBody): VerifyOtpDto {
        val digits = body.phone.filter(Char::isDigit).takeLast(9)
        if (digits.length != 9) {
            throw LocalDemoApiException(400, "PHONE_INVALID", "El número celular debe tener 9 dígitos.")
        }
        if (body.otpCode.trim() != "123456") {
            throw LocalDemoApiException(401, "OTP_INVALID", "Código incorrecto. En modo demo usa 123456.")
        }
        return VerifyOtpDto(accessToken = "demo-local-jwt-$digits-${UUID.randomUUID()}")
    }

    override suspend fun submitKycDni(body: SubmitKycDniBody): SubmitKycDniDto {
        val dniDigits = body.dni.filter(Char::isDigit)
        if (dniDigits.length != 8) {
            throw LocalDemoApiException(400, "DNI_INVALID", "El DNI debe tener exactamente 8 dígitos numéricos.")
        }
        if (body.fullName.isBlank()) {
            throw LocalDemoApiException(400, "NAME_REQUIRED", "El nombre completo del titular es obligatorio.")
        }
        return SubmitKycDniDto(
            requestId = "KYC-DEMO-${System.currentTimeMillis().toString().takeLast(6)}",
            kycStatus = "PENDING_REVIEW",
            dni = dniDigits,
            fullName = body.fullName.trim(),
            message = "Solicitud KYC registrada correctamente. En espera de cotejo con padrón oficial.",
            submittedAt = java.time.Instant.now().toString()
        )
    }

    override suspend fun registerDevice(body: DeviceRegistrationBody): DeviceRegistrationDto =
        DeviceRegistrationDto(pushMode = "PULL", quietHours = "20:00–07:00")

    override suspend fun unregisterDevice(token: String): Response<Unit> =
        Response.success(Unit)

    override suspend fun inbox(token: String): InboxDto = synchronized(lock) {
        InboxDto(messages = state.inbox.toList())
    }

    override suspend fun ackInbox(token: String, body: InboxAckBody): Response<Unit> = synchronized(lock) {
        val ids = body.ids.toSet()
        state.inbox.removeAll { it.id in ids }
        saveState()
        Response.success(Unit)
    }

    override suspend fun testPush(): TestPushDto = synchronized(lock) {
        state.inbox.add(
            0,
            InboxMessageDto(
                id = "msg-${UUID.randomUUID().toString().take(8)}",
                createdAt = Instant.now().toString(),
                data = mapOf(
                    "type" to "INSTALLMENT_REMINDER",
                    "title" to "Recordatorio de cuota demo",
                    "body" to "Tienes una cuota programada en tu plan de inventario demo.",
                    "deep_link" to "kuota://compras"
                )
            )
        )
        saveState()
        TestPushDto(pushMode = "PULL", devices = 1, delivered = 1)
    }

    override suspend fun submitClaim(idempotencyKey: String, body: ClaimSubmissionBody): ClaimReceiptDto = synchronized(lock) {
        val existing = state.claims.firstOrNull { it.clientClaimId == body.clientClaimId }
        if (existing != null) return@synchronized existing

        val nextNumber = state.claims.size + 1
        val dueDate = PeruBusinessCalendar.addBusinessDays(LocalDate.now(limaZone), 15).toString()
        val receipt = ClaimReceiptDto(
            claimNumber = "LR-2026-%06d".format(nextNumber),
            clientClaimId = body.clientClaimId,
            status = "RECIBIDO",
            responseDueDate = dueDate,
            channels = listOf(
                ClaimChannelDto(channel = "ARCHIVO_LOCAL", status = "ENVIADO"),
                ClaimChannelDto(channel = "COPIA_CONSUMIDOR", status = "ENVIADO")
            ),
            responseText = null
        )
        state.claims.add(0, receipt)
        saveState()
        receipt
    }

    override suspend fun listClaims(): ClaimListDto = synchronized(lock) {
        ClaimListDto(claims = state.claims.toList())
    }

    override suspend fun syncDeclaredSales(body: DeclaredSalesSyncBody): DeclaredSalesSyncDto =
        DeclaredSalesSyncDto(
            accepted = body.sales.size,
            duplicates = 0,
            rejected = emptyList(),
            serverTime = Instant.now().toString()
        )

    override suspend fun generateDemoQr(amountPen: Double): GenerateDemoQrDto {
        val safeAmountPen = amountPen.takeIf { it.isFinite() && it in 1.0..1_000_000.0 } ?: 1_000.0
        val amountCents = Math.round(safeAmountPen * 100.0)
        val payload = LocalDemoQrPayload(
            transactionId = "tx-demo-${System.currentTimeMillis()}",
            supplierId = "sup-lima-001",
            supplierName = "Distribuidora Mayorista Lima S.A.C.",
            amountCents = amountCents,
            expiresAtEpochMillis = System.currentTimeMillis() + 15 * 60 * 1000L
        )
        val rawJson = json.encodeToString(LocalDemoQrPayload.serializer(), payload)
        val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(rawJson.toByteArray(StandardCharsets.UTF_8))
        return GenerateDemoQrDto(
            signedQrPayload = encoded,
            amountCents = amountCents,
            supplierName = payload.supplierName,
            expiresInMinutes = 15
        )
    }

    private fun decodeQrPayload(qrPayload: String): VerifiedSupplierQrDto {
        val trimmed = qrPayload.trim()
        if (trimmed.equals("KUOTA-DEMO-SUPPLIER-LIMA", ignoreCase = true)) {
            return VerifiedSupplierQrDto(
                transactionId = "tx-demo-legacy-1000",
                supplierId = "sup-lima-001",
                supplierName = "Distribuidora Mayorista Lima S.A.C.",
                amountCents = 100_000L,
                expiresAtEpochMillis = System.currentTimeMillis() + 15 * 60 * 1000L,
                demoOnly = true
            )
        }
        val decodedBytes = runCatching {
            Base64.getUrlDecoder().decode(trimmed)
        }.recoverCatching {
            Base64.getDecoder().decode(trimmed)
        }.getOrElse {
            throw LocalDemoApiException(422, "QR_INVALID_OR_EXPIRED", "El código QR no tiene un formato Base64 válido.")
        }
        val decodedString = String(decodedBytes, StandardCharsets.UTF_8)
        val parsed = runCatching {
            json.decodeFromString(LocalDemoQrPayload.serializer(), decodedString)
        }.getOrElse {
            throw LocalDemoApiException(422, "QR_INVALID_OR_EXPIRED", "El QR del proveedor no es válido o expiró.")
        }
        if (parsed.amountCents <= 0L || parsed.expiresAtEpochMillis < System.currentTimeMillis()) {
            throw LocalDemoApiException(422, "QR_INVALID_OR_EXPIRED", "El QR del proveedor expiró. Genera uno nuevo.")
        }
        return VerifiedSupplierQrDto(
            transactionId = parsed.transactionId,
            supplierId = parsed.supplierId,
            supplierName = parsed.supplierName,
            amountCents = parsed.amountCents,
            expiresAtEpochMillis = parsed.expiresAtEpochMillis,
            demoOnly = true
        )
    }

    override suspend fun verifySupplierQr(body: VerifySupplierQrBody): VerifiedSupplierQrDto =
        decodeQrPayload(body.qrPayload)

    override suspend fun confirmSupplierSale(body: ConfirmSupplierSaleBody): SupplierConfirmationDto {
        val verified = decodeQrPayload(body.qrPayload)
        val immediate = percentageOf(verified.amountCents, 6_000)
        val deferred = verified.amountCents - immediate
        val fee = percentageOf(verified.amountCents, 400)
        return SupplierConfirmationDto(
            confirmationId = "demo-confirm-${UUID.randomUUID().toString().take(8)}",
            transactionId = verified.transactionId,
            status = "DEMO_SUPPLIER_CONFIRMED",
            supplierName = verified.supplierName,
            amountCents = verified.amountCents,
            immediatePayoutCents = immediate,
            deferredPayoutCents = deferred,
            supplierFeeCents = fee,
            confirmedAt = Instant.now().toString(),
            demoOnly = true
        )
    }

    override suspend fun checkEligibility(body: EligibilityCheckBody): EligibilityCheckDto = synchronized(lock) {
        val actualPaid = state.purchases.sumOf { p ->
            p.installments.count { it.status in setOf("DEMO_PAID", "PAID") }
        }
        val effectivePaid = (body.simulatedPaidInstallments ?: actualPaid).coerceAtLeast(0)
        val (downPaymentBps, riskScore, riskTier) = when {
            effectivePaid >= 3 -> Triple(0, 860, "TRUSTED_0")
            effectivePaid == 2 -> Triple(1_000, 790, "PROGRESS_10")
            effectivePaid == 1 -> Triple(2_000, 730, "PROGRESS_20")
            else -> Triple(3_000, 680, "ENTRY_30")
        }
        val dto = EligibilityCheckDto(
            decisionId = "demo-elig-${UUID.randomUUID().toString().take(8)}",
            status = "DEMO_APPROVED",
            approvedAmountCents = body.amountCents,
            downPaymentBasisPoints = downPaymentBps,
            policyVersion = "DEMO_POLICY_2026_V2",
            riskScore = riskScore,
            riskTier = riskTier,
            completedInstallmentsCount = effectivePaid,
            expiresAt = LocalDate.now(limaZone).plusDays(1).toString()
        )
        state.decisions[dto.decisionId] = dto
        saveState()
        dto
    }

    override suspend fun requestQuote(body: QuoteRequestBody): QuoteDto = synchronized(lock) {
        val verified = decodeQrPayload(body.supplierQrPayload)
        if (verified.amountCents != body.amountCents) {
            throw LocalDemoApiException(422, "QR_AMOUNT_MISMATCH", "El monto solicitado no coincide con el QR firmado.")
        }
        val decision = state.decisions[body.eligibilityDecisionId]
        val downPaymentBps = (decision?.downPaymentBasisPoints ?: 3_000).coerceIn(0, 3_000)
        val policyVersion = decision?.policyVersion ?: "DEMO_POLICY_2026_V2"

        val amountCents = body.amountCents
        val downPayment = percentageOf(amountCents, downPaymentBps)
        val buyerFee = percentageOf(amountCents, 500)
        val financedPrincipal = amountCents - downPayment
        val buyerTotalAfterDownPayment = financedPrincipal + buyerFee
        val supplierImmediate = percentageOf(amountCents, 6_000)
        val supplierDeferred = amountCents - supplierImmediate
        val koutaInitialContribution = (supplierImmediate - downPayment).coerceAtLeast(0L)
        val supplierFee = percentageOf(amountCents, 400)

        val baseInstallment = buyerTotalAfterDownPayment / 4L
        val remainder = buyerTotalAfterDownPayment - (baseInstallment * 4L)
        val anchorDate = LocalDate.now(limaZone)
        val quoteId = "demo-quote-${UUID.randomUUID().toString().take(8)}"

        val installments = (1..4).map { sequence ->
            QuoteInstallmentDto(
                installmentId = "$quoteId-inst-$sequence",
                sequenceNumber = sequence,
                amountCents = if (sequence == 4) baseInstallment + remainder else baseInstallment,
                dueAt = anchorDate.plusWeeks(sequence.toLong()).toString(),
                status = "PROGRAMMED"
            )
        }

        val quote = QuoteDto(
            quoteId = quoteId,
            quoteVersion = "DEMO_QUOTE_V1",
            policyVersion = policyVersion,
            downPaymentBasisPoints = downPaymentBps,
            expiresAt = anchorDate.plusDays(1).toString(),
            supplier = QuoteSupplierDto(
                supplierId = verified.supplierId,
                displayName = verified.supplierName
            ),
            purchaseAmountCents = amountCents,
            buyerDownPaymentCents = downPayment,
            buyerFeeCents = buyerFee,
            buyerTotalAfterDownPaymentCents = buyerTotalAfterDownPayment,
            supplierImmediatePayoutCents = supplierImmediate,
            supplierDeferredPayoutCents = supplierDeferred,
            koutaInitialContributionCents = koutaInitialContribution,
            supplierFeeCents = supplierFee,
            installments = installments
        )
        state.quotes[quoteId] = quote
        saveState()
        quote
    }

    override suspend fun createPurchase(idempotencyKey: String, body: CreatePurchaseBody): PurchaseDto = synchronized(lock) {
        val quote = state.quotes[body.quoteId]
            ?: throw LocalDemoApiException(404, "QUOTE_NOT_FOUND", "La cotización expiró. Vuelve a verificar el QR.")
        val now = Instant.now().toString()
        val purchaseId = "demo-purchase-${UUID.randomUUID().toString().take(8)}"
        val historyItem = PurchaseHistoryItemDto(
            purchaseId = purchaseId,
            status = "DEMO_CREATED",
            updatedAt = now,
            supplierName = quote.supplier.displayName,
            purchaseAmountCents = quote.purchaseAmountCents,
            buyerDownPaymentCents = quote.buyerDownPaymentCents,
            buyerTotalAfterDownPaymentCents = quote.buyerTotalAfterDownPaymentCents,
            supplierImmediatePayoutCents = quote.supplierImmediatePayoutCents,
            supplierDeferredPayoutCents = quote.supplierDeferredPayoutCents,
            koutaInitialContributionCents = quote.koutaInitialContributionCents,
            supplierFeeCents = quote.supplierFeeCents,
            installments = quote.installments,
            payments = emptyList()
        )
        state.purchases.add(0, historyItem)
        saveState()
        PurchaseDto(
            purchaseId = purchaseId,
            status = "DEMO_CREATED",
            updatedAt = now,
            statusReasonCode = "DEMO_ONLY_NO_DISBURSEMENT"
        )
    }

    override suspend fun listPurchases(): List<PurchaseHistoryItemDto> = synchronized(lock) {
        state.purchases.toList()
    }

    override suspend fun createPaymentIntent(
        installmentId: String,
        idempotencyKey: String,
        body: CreatePaymentIntentBody
    ): PaymentIntentDto = synchronized(lock) {
        val purchaseIndex = state.purchases.indexOfFirst { p -> p.installments.any { it.installmentId == installmentId } }
        if (purchaseIndex < 0) {
            throw LocalDemoApiException(404, "INSTALLMENT_NOT_FOUND", "No se encontró la cuota seleccionada.")
        }
        val purchase = state.purchases[purchaseIndex]
        val installment = purchase.installments.first { it.installmentId == installmentId }

        val activeExisting = purchase.payments.lastOrNull {
            it.installmentId == installmentId && it.status in setOf("PENDING", "PROCESSING", "ACTION_REQUIRED")
        }
        if (activeExisting != null) {
            throw LocalDemoApiException(
                409,
                "PAYMENT_ALREADY_IN_PROGRESS",
                "Ya existe un intento de pago en curso para esta cuota."
            )
        }

        val now = Instant.now().toString()
        val paymentId = "demo-pay-${UUID.randomUUID().toString().take(8)}"
        val newPayment = PaymentIntentDto(
            paymentId = paymentId,
            installmentId = installmentId,
            amountCents = installment.amountCents,
            status = "PENDING",
            nextActionUrl = null,
            updatedAt = now,
            statusReasonCode = "DEMO_INTENT_CREATED",
            providerReference = null,
            providerAmountCents = null,
            reconciliationStatus = "NOT_RECONCILED",
            demoOnly = true,
            events = listOf(
                PaymentEventDto(
                    eventType = "CREATED",
                    statusBefore = "NONE",
                    statusAfter = "PENDING",
                    occurredAt = now,
                    reasonCode = "DEMO_INTENT_CREATED"
                )
            )
        )
        val updatedInstallments = purchase.installments.map {
            if (it.installmentId == installmentId) it.copy(status = "PAYMENT_PENDING") else it
        }
        state.purchases[purchaseIndex] = purchase.copy(
            updatedAt = now,
            installments = updatedInstallments,
            payments = purchase.payments + newPayment
        )
        saveState()
        newPayment
    }

    override suspend fun getPayment(paymentId: String): PaymentIntentDto = synchronized(lock) {
        state.purchases.asSequence()
            .flatMap { it.payments.asSequence() }
            .firstOrNull { it.paymentId == paymentId }
            ?: throw LocalDemoApiException(404, "PAYMENT_NOT_FOUND", "No se encontró el intento de pago.")
    }

    override suspend fun applyDemoPaymentEvent(
        paymentId: String,
        idempotencyKey: String,
        body: DemoPaymentEventBody
    ): PaymentIntentDto = synchronized(lock) {
        val purchaseIndex = state.purchases.indexOfFirst { p -> p.payments.any { it.paymentId == paymentId } }
        if (purchaseIndex < 0) {
            throw LocalDemoApiException(404, "PAYMENT_NOT_FOUND", "No se encontró el pago seleccionado.")
        }
        val purchase = state.purchases[purchaseIndex]
        val payment = purchase.payments.first { it.paymentId == paymentId }
        val now = Instant.now().toString()
        val ref = payment.providerReference ?: "PSP-DEMO-${paymentId.takeLast(6).uppercase()}"

        val (newPayStatus, newReconStatus, newInstStatus, reason) = when (body.eventType.uppercase()) {
            "SUCCEED" -> Quad("SUCCEEDED", "MATCHED", "DEMO_PAID", "DEMO_SETTLED_IN_LEDGER")
            "FAIL" -> Quad("FAILED", "NOT_RECONCILED", "PROGRAMMED", "DEMO_PROVIDER_REJECTED")
            "SUCCEED_AMOUNT_MISMATCH" -> Quad("SUCCEEDED", "AMOUNT_MISMATCH", "DEMO_PAYMENT_REVIEW", "DEMO_VARIANCE_DETECTED")
            "REFUND" -> Quad("REFUNDED", "MATCHED", "DEMO_REFUNDED", "DEMO_COMPENSATING_ENTRY")
            "REVERSE" -> Quad("REVERSED", "MATCHED", "DEMO_REVERSED", "DEMO_CHARGEBACK_REVERSAL")
            "RECONCILE_MATCH" -> Quad("SUCCEEDED", "MANUALLY_RESOLVED", "DEMO_PAID", "DEMO_MANUAL_LEDGER_RECONCILED")
            else -> throw LocalDemoApiException(400, "EVENT_INVALID", "Evento demo no soportado: ${body.eventType}")
        }

        val updatedPayment = payment.copy(
            status = newPayStatus,
            reconciliationStatus = newReconStatus,
            providerReference = ref,
            providerAmountCents = if (newReconStatus == "AMOUNT_MISMATCH") (payment.amountCents - 100L).coerceAtLeast(1L) else payment.amountCents,
            statusReasonCode = reason,
            updatedAt = now,
            events = payment.events + PaymentEventDto(
                eventType = body.eventType.uppercase(),
                statusBefore = payment.status,
                statusAfter = newPayStatus,
                occurredAt = now,
                reasonCode = reason,
                providerReference = ref,
                providerAmountCents = payment.amountCents
            )
        )
        val updatedInstallments = purchase.installments.map {
            if (it.installmentId == payment.installmentId) it.copy(status = newInstStatus) else it
        }
        val updatedPayments = purchase.payments.map {
            if (it.paymentId == paymentId) updatedPayment else it
        }
        state.purchases[purchaseIndex] = purchase.copy(
            updatedAt = now,
            installments = updatedInstallments,
            payments = updatedPayments
        )
        saveState()
        updatedPayment
    }

    private data class Quad(
        val payStatus: String,
        val reconStatus: String,
        val instStatus: String,
        val reason: String
    )

    companion object {
        @Volatile
        private var instance: LocalDemoKuotaApi? = null

        fun getInstance(context: Context): LocalDemoKuotaApi =
            instance ?: synchronized(this) {
                instance ?: LocalDemoKuotaApi(context.applicationContext).also { instance = it }
            }
    }
}

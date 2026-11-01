package com.kouta.app.data.remote

import android.content.Context
import com.kouta.app.BuildConfig
import com.kouta.app.core.network.AuthInterceptor
import com.kouta.app.data.local.AppSettings
import com.kouta.app.data.local.TokenManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.IOException
import java.util.concurrent.TimeUnit

@Serializable
data class RequestOtpBody(val phone: String, val email: String? = null)

@Serializable
data class RequestOtpDto(
    @SerialName("otp_code_debug") val otpCodeDebug: String? = null,
    val message: String = "",
    @SerialName("expires_in_seconds") val expiresInSeconds: Int = 300
)

@Serializable
data class VerifyOtpBody(val phone: String, @SerialName("otp_code") val otpCode: String)

@Serializable
data class VerifyOtpDto(@SerialName("access_token") val accessToken: String)

@Serializable
data class NotificationPreferencesDto(
    @SerialName("installment_reminders") val installmentReminders: Boolean,
    @SerialName("support_updates") val supportUpdates: Boolean,
    val news: Boolean
)

@Serializable
data class DeviceRegistrationBody(
    val token: String,
    val platform: String = "android",
    @SerialName("app_version") val appVersion: String = BuildConfig.VERSION_NAME,
    val preferences: NotificationPreferencesDto
)

@Serializable
data class DeviceRegistrationDto(
    @SerialName("push_mode") val pushMode: String = "DRY_RUN",
    @SerialName("quiet_hours") val quietHours: String? = null
)

@Serializable
data class TestPushDto(
    @SerialName("push_mode") val pushMode: String,
    val devices: Int,
    val delivered: Int
)

@Serializable
data class ClaimSubmissionBody(
    @SerialName("client_claim_id") val clientClaimId: String,
    val type: String,
    @SerialName("help_topic") val helpTopic: String? = null,
    @SerialName("consumer_name") val consumerName: String,
    @SerialName("document_type") val documentType: String,
    @SerialName("document_number") val documentNumber: String,
    val address: String,
    val phone: String,
    val email: String,
    @SerialName("good_type") val goodType: String,
    @SerialName("good_description") val goodDescription: String,
    @SerialName("amount_claimed_cents") val amountClaimedCents: Long? = null,
    @SerialName("operation_reference") val operationReference: String? = null,
    val detail: String,
    @SerialName("consumer_request") val consumerRequest: String,
    @SerialName("created_at_device") val createdAtDevice: String? = null
)

@Serializable
data class ClaimChannelDto(val channel: String, val status: String)

@Serializable
data class ClaimReceiptDto(
    @SerialName("claim_number") val claimNumber: String,
    @SerialName("client_claim_id") val clientClaimId: String,
    val status: String,
    @SerialName("response_due_date") val responseDueDate: String,
    val channels: List<ClaimChannelDto> = emptyList(),
    @SerialName("response_text") val responseText: String? = null
)

@Serializable
data class ClaimListDto(val claims: List<ClaimReceiptDto> = emptyList())

@Serializable
data class InboxMessageDto(
    val id: String,
    @SerialName("created_at") val createdAt: String = "",
    val data: Map<String, String> = emptyMap()
)

@Serializable
data class InboxDto(val messages: List<InboxMessageDto> = emptyList())

@Serializable
data class InboxAckBody(val ids: List<String>)

@Serializable
data class ApiErrorDto(
    val code: String = "",
    val message: String = "",
    @SerialName("request_id") val requestId: String = "",
    val retryable: Boolean = false
)

@Serializable
data class VerifySupplierQrBody(@SerialName("qr_payload") val qrPayload: String)

@Serializable
data class VerifiedSupplierQrDto(
    @SerialName("transaction_id") val transactionId: String,
    @SerialName("supplier_id") val supplierId: String,
    @SerialName("supplier_name") val supplierName: String,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("expires_at_epoch_millis") val expiresAtEpochMillis: Long,
    @SerialName("demo_only") val demoOnly: Boolean = true
)

@Serializable
data class GenerateDemoQrDto(
    @SerialName("signed_qr_payload") val signedQrPayload: String,
    @SerialName("amount_cents") val amountCents: Long = 0,
    @SerialName("supplier_name") val supplierName: String = "",
    @SerialName("expires_in_minutes") val expiresInMinutes: Int = 15
)

@Serializable
data class ConfirmSupplierSaleBody(@SerialName("qr_payload") val qrPayload: String)

@Serializable
data class SupplierConfirmationDto(
    @SerialName("confirmation_id") val confirmationId: String,
    @SerialName("transaction_id") val transactionId: String,
    val status: String,
    @SerialName("supplier_name") val supplierName: String,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("immediate_payout_cents") val immediatePayoutCents: Long,
    @SerialName("deferred_payout_cents") val deferredPayoutCents: Long,
    @SerialName("supplier_fee_cents") val supplierFeeCents: Long,
    @SerialName("confirmed_at") val confirmedAt: String,
    @SerialName("demo_only") val demoOnly: Boolean = true
)

@Serializable
data class EligibilityCheckBody(
    @SerialName("business_id") val businessId: String,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("term_days") val termDays: Int,
    @SerialName("consent_version") val consentVersion: String,
    @SerialName("simulated_paid_installments") val simulatedPaidInstallments: Int? = null
)

@Serializable
data class EligibilityCheckDto(
    @SerialName("decision_id") val decisionId: String,
    val status: String,
    @SerialName("approved_amount_cents") val approvedAmountCents: Long,
    @SerialName("down_payment_basis_points") val downPaymentBasisPoints: Int,
    @SerialName("policy_version") val policyVersion: String = "DEMO_POLICY_2026_V2",
    @SerialName("risk_score") val riskScore: Int = 620,
    @SerialName("risk_tier") val riskTier: String = "ENTRY_30",
    @SerialName("completed_installments_count") val completedInstallmentsCount: Int = 0,
    @SerialName("expires_at") val expiresAt: String
)

@Serializable
data class SubmitKycDniBody(
    val dni: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("consent_given") val consentGiven: Boolean = true,
    @SerialName("has_document_photo") val hasDocumentPhoto: Boolean = false
)

@Serializable
data class SubmitKycDniDto(
    @SerialName("request_id") val requestId: String = "",
    @SerialName("kyc_status") val kycStatus: String = "PENDING_REVIEW",
    val dni: String = "",
    @SerialName("full_name") val fullName: String = "",
    val message: String = "",
    @SerialName("submitted_at") val submittedAt: String = ""
)

@Serializable
data class CreatePaymentIntentBody(
    @SerialName("payment_method_id") val paymentMethodId: String = "DEMO_MANUAL"
)

@Serializable
data class DemoPaymentEventBody(
    @SerialName("event_type") val eventType: String
)

@Serializable
data class PaymentEventDto(
    @SerialName("event_type") val eventType: String = "",
    @SerialName("status_before") val statusBefore: String = "",
    @SerialName("status_after") val statusAfter: String = "",
    @SerialName("occurred_at") val occurredAt: String = "",
    @SerialName("reason_code") val reasonCode: String = "",
    @SerialName("provider_reference") val providerReference: String? = null,
    @SerialName("provider_amount_cents") val providerAmountCents: Long? = null
)

@Serializable
data class PaymentIntentDto(
    @SerialName("payment_id") val paymentId: String,
    @SerialName("installment_id") val installmentId: String,
    @SerialName("amount_cents") val amountCents: Long = 0,
    val status: String = "",
    @SerialName("next_action_url") val nextActionUrl: String? = null,
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("status_reason_code") val statusReasonCode: String? = null,
    @SerialName("provider_reference") val providerReference: String? = null,
    @SerialName("provider_amount_cents") val providerAmountCents: Long? = null,
    @SerialName("reconciliation_status") val reconciliationStatus: String = "NOT_RECONCILED",
    @SerialName("demo_only") val demoOnly: Boolean = true,
    val events: List<PaymentEventDto> = emptyList()
)

@Serializable
data class QuoteRequestBody(
    @SerialName("business_id") val businessId: String,
    @SerialName("supplier_qr_payload") val supplierQrPayload: String,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("term_days") val termDays: Int,
    @SerialName("eligibility_decision_id") val eligibilityDecisionId: String
)

@Serializable
data class QuoteSupplierDto(
    @SerialName("supplier_id") val supplierId: String,
    @SerialName("display_name") val displayName: String
)

@Serializable
data class QuoteInstallmentDto(
    @SerialName("installment_id") val installmentId: String,
    @SerialName("sequence_number") val sequenceNumber: Int,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("due_at") val dueAt: String,
    val status: String
)

@Serializable
data class CreatePurchaseBody(
    @SerialName("quote_id") val quoteId: String,
    @SerialName("quote_version") val quoteVersion: String,
    @SerialName("accepted_terms_version") val acceptedTermsVersion: String,
    @SerialName("accepted_privacy_notice_version") val acceptedPrivacyNoticeVersion: String
)

@Serializable
data class PurchaseDto(
    @SerialName("purchase_id") val purchaseId: String,
    val status: String,
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("status_reason_code") val statusReasonCode: String = ""
)

@Serializable
data class PurchaseHistoryItemDto(
    @SerialName("purchase_id") val purchaseId: String,
    val status: String,
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("supplier_name") val supplierName: String = "",
    @SerialName("purchase_amount_cents") val purchaseAmountCents: Long = 0,
    @SerialName("buyer_down_payment_cents") val buyerDownPaymentCents: Long = 0,
    @SerialName("buyer_total_after_down_payment_cents") val buyerTotalAfterDownPaymentCents: Long = 0,
    @SerialName("supplier_immediate_payout_cents") val supplierImmediatePayoutCents: Long = 0,
    @SerialName("supplier_deferred_payout_cents") val supplierDeferredPayoutCents: Long = 0,
    @SerialName("kouta_initial_contribution_cents") val koutaInitialContributionCents: Long = 0,
    @SerialName("supplier_fee_cents") val supplierFeeCents: Long = 0,
    val installments: List<QuoteInstallmentDto> = emptyList(),
    val payments: List<PaymentIntentDto> = emptyList()
)

@Serializable
data class QuoteDto(
    @SerialName("quote_id") val quoteId: String,
    @SerialName("quote_version") val quoteVersion: String,
    @SerialName("policy_version") val policyVersion: String = "DEMO_POLICY_2026_V2",
    @SerialName("down_payment_basis_points") val downPaymentBasisPoints: Int = 3_000,
    @SerialName("expires_at") val expiresAt: String,
    val supplier: QuoteSupplierDto,
    @SerialName("purchase_amount_cents") val purchaseAmountCents: Long,
    @SerialName("buyer_down_payment_cents") val buyerDownPaymentCents: Long,
    @SerialName("buyer_fee_cents") val buyerFeeCents: Long,
    @SerialName("buyer_total_after_down_payment_cents") val buyerTotalAfterDownPaymentCents: Long,
    @SerialName("supplier_immediate_payout_cents") val supplierImmediatePayoutCents: Long,
    @SerialName("supplier_deferred_payout_cents") val supplierDeferredPayoutCents: Long,
    @SerialName("kouta_initial_contribution_cents") val koutaInitialContributionCents: Long,
    @SerialName("supplier_fee_cents") val supplierFeeCents: Long,
    val installments: List<QuoteInstallmentDto> = emptyList()
)

/** Venta declarada por el usuario: respaldo, no verificación de cobro. */
@Serializable
data class DeclaredSaleDto(
    @SerialName("client_sale_id") val clientSaleId: String,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("occurred_at") val occurredAt: String,
    val source: String,
    val revision: Int,
    val deleted: Boolean = false
)

@Serializable
data class DeclaredSalesSyncBody(val sales: List<DeclaredSaleDto>)

@Serializable
data class DeclaredSaleRejectionDto(
    @SerialName("client_sale_id") val clientSaleId: String,
    val code: String = "",
    val message: String = ""
)

@Serializable
data class DeclaredSalesSyncDto(
    val accepted: Int = 0,
    val duplicates: Int = 0,
    val rejected: List<DeclaredSaleRejectionDto> = emptyList(),
    @SerialName("server_time") val serverTime: String = ""
)

interface KuotaApi {
    @GET("health")
    suspend fun health(): Map<String, String>

    @POST("v1/auth/request-otp")
    suspend fun requestOtp(@Body body: RequestOtpBody): RequestOtpDto

    @POST("v1/auth/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpBody): VerifyOtpDto

    @POST("v1/kyc/dni")
    suspend fun submitKycDni(@Body body: SubmitKycDniBody): SubmitKycDniDto

    @POST("v1/devices")
    suspend fun registerDevice(@Body body: DeviceRegistrationBody): DeviceRegistrationDto

    @DELETE("v1/devices/{token}")
    suspend fun unregisterDevice(@Path("token") token: String): Response<Unit>

    @GET("v1/devices/{token}/inbox")
    suspend fun inbox(@Path("token") token: String): InboxDto

    @POST("v1/devices/{token}/inbox/ack")
    suspend fun ackInbox(@Path("token") token: String, @Body body: InboxAckBody): Response<Unit>

    @POST("v1/devices/test-push")
    suspend fun testPush(): TestPushDto

    @POST("v1/support/claims")
    suspend fun submitClaim(@Header("Idempotency-Key") idempotencyKey: String, @Body body: ClaimSubmissionBody): ClaimReceiptDto

    @GET("v1/support/claims")
    suspend fun listClaims(): ClaimListDto

    @POST("v1/sales/declared/sync")
    suspend fun syncDeclaredSales(@Body body: DeclaredSalesSyncBody): DeclaredSalesSyncDto

    @POST("v1/supplier/qr/verify")
    suspend fun verifySupplierQr(@Body body: VerifySupplierQrBody): VerifiedSupplierQrDto

    @FormUrlEncoded
    @POST("v1/supplier/generate-qr")
    suspend fun generateDemoQr(@Field("amountPen") amountPen: Double): GenerateDemoQrDto

    @POST("v1/supplier/confirm-sale")
    suspend fun confirmSupplierSale(@Body body: ConfirmSupplierSaleBody): SupplierConfirmationDto

    @POST("v1/eligibility/checks")
    suspend fun checkEligibility(@Body body: EligibilityCheckBody): EligibilityCheckDto

    @POST("v1/quotes")
    suspend fun requestQuote(@Body body: QuoteRequestBody): QuoteDto

    @POST("v1/purchases")
    suspend fun createPurchase(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: CreatePurchaseBody
    ): PurchaseDto

    @GET("v1/purchases")
    suspend fun listPurchases(): List<PurchaseHistoryItemDto>

    @POST("v1/installments/{installmentId}/payment-intents")
    suspend fun createPaymentIntent(
        @Path("installmentId") installmentId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: CreatePaymentIntentBody
    ): PaymentIntentDto

    @GET("v1/payments/{paymentId}")
    suspend fun getPayment(@Path("paymentId") paymentId: String): PaymentIntentDto

    @POST("v1/payments/{paymentId}/demo-events")
    suspend fun applyDemoPaymentEvent(
        @Path("paymentId") paymentId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: DemoPaymentEventBody
    ): PaymentIntentDto
}

sealed interface ApiOutcome<out T> {
    data class Ok<T>(val value: T) : ApiOutcome<T>
    /** Sesión ausente, vencida o inválida: el token local ya fue borrado. */
    data object SessionExpired : ApiOutcome<Nothing>
    data class Rejected(
        val httpCode: Int,
        val code: String,
        val message: String,
        val requestId: String = "",
        val retryable: Boolean = false
    ) : ApiOutcome<Nothing>
    data class Unreachable(val serverUrl: String) : ApiOutcome<Nothing>
}

object KuotaApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    @Volatile private var cached: Pair<String, KuotaApi>? = null
    @Volatile private var lastUnreachableUrl: String? = null
    @Volatile private var lastUnreachableEpochMs: Long = 0L
    private const val UNREACHABLE_BACKOFF_MS = 30_000L

    fun isValidServerUrl(url: String): Boolean {
        val parsed = url.trim().toHttpUrlOrNull() ?: return false
        return parsed.scheme == "https" || BuildConfig.DEBUG
    }

    fun hasSession(context: Context): Boolean = !TokenManager(context).getToken().isNullOrBlank()

    fun api(context: Context): KuotaApi {
        val baseUrl = AppSettings(context).serverUrl
        cached?.takeIf { it.first == baseUrl }?.let { return it.second }
        val client = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(TokenManager(context.applicationContext)))
            .build()
        val api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(KuotaApi::class.java)
        cached = baseUrl to api
        return api
    }

    // AGENTS.md Regla 1: sin mocks ni servidores locales. Si no hay backend alcanzable,
    // la UI debe mostrar su estado de error/offline; nunca se simulan respuestas.
    suspend fun <T> call(context: Context, block: suspend KuotaApi.() -> T): ApiOutcome<T> {
        val settings = AppSettings(context)
        val currentUrl = settings.serverUrl
        if (!isValidServerUrl(currentUrl)) return ApiOutcome.Unreachable(currentUrl)

        val now = System.currentTimeMillis()
        if (lastUnreachableUrl == currentUrl &&
            (now - lastUnreachableEpochMs) < UNREACHABLE_BACKOFF_MS
        ) {
            return ApiOutcome.Unreachable(currentUrl)
        }

        return try {
            val result = api(context).block()
            lastUnreachableEpochMs = 0L
            ApiOutcome.Ok(result)
        } catch (error: HttpException) {
            if (error.code() == 401) {
                TokenManager(context).clearToken()
                settings.clearSession()
                ApiOutcome.SessionExpired
            } else {
                val body = runCatching { error.response()?.errorBody()?.string() }.getOrNull()
                val parsed = body?.let { runCatching { json.decodeFromString(ApiErrorDto.serializer(), it) }.getOrNull() }
                ApiOutcome.Rejected(
                    httpCode = error.code(),
                    code = parsed?.code.orEmpty(),
                    message = parsed?.message ?: "El servidor rechazó la solicitud.",
                    requestId = parsed?.requestId.orEmpty(),
                    retryable = parsed?.retryable == true
                )
            }
        } catch (_: IOException) {
            lastUnreachableUrl = currentUrl
            lastUnreachableEpochMs = System.currentTimeMillis()
            ApiOutcome.Unreachable(currentUrl)
        } catch (_: kotlinx.serialization.SerializationException) {
            ApiOutcome.Rejected(0, "BAD_RESPONSE", "Respuesta inesperada del servidor.", retryable = true)
        }
    }
}

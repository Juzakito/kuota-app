package com.kuota.backend

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

private const val DEMO_BUSINESS_ID = "demo-buyer-001"
private const val DEMO_SUPPLIER_QR = "KUOTA-DEMO-SUPPLIER-LIMA"
private const val DEMO_OTP_CODE = "123456"
private const val DEMO_SUPPLIER_ID = "demo-supplier-lima"
private const val DEMO_SUPPLIER_NAME = "Distribuidora Lima S.A.C."
private val allowedTerms = setOf(15, 30, 60, 90)

private val logger = LoggerFactory.getLogger("com.kuota.backend.Application")

fun main() {
    val config = BackendConfigLoader.fromEnvironment()
    JwtConfig.initialize(config.jwtSecret)
    DynamicQrSigner.initialize(config.qrSigningSecret)
    val storePath = config.demoStorePath
    val dataDirectory = config.dataDirectory
    val accounting = createAccountingStore(config)
    val paymentProvider = createPaymentProvider(config)
    val store = DemoStore(
        file = storePath,
        paymentProvider = paymentProvider,
        accounting = accounting,
        environment = config.environment.name
    )
    val webhookProcessor = PaymentWebhookProcessor(
        repository = store,
        accounting = accounting,
        environment = config.environment.name,
        mercadoPagoLookup = paymentProvider as? MercadoPagoPaymentLookup
    )

    val forwarders = configuredClaimForwarders(dataDirectory)
    val claims = ClaimStore(dataDirectory.resolve("kuota-claims.json")) { forwarders.map { it.channel } }
    val forwarding = ClaimForwardingService(claims, forwarders).also { it.start() }
    val devices = DeviceStore(dataDirectory.resolve("kuota-devices.json"))
    val push = PushService(devices, configuredPushSender())
    val reminders = ReminderScheduler(devices, push) { store.dueInstallments() }.also { it.start() }
    val declaredSales = DeclaredSalesStore(dataDirectory.resolve("kuota-declared-sales.json"))
    val services = KuotaServices(claims, forwarding, devices, push, reminders, declaredSales)

    logger.info(
        "Kuota backend environment={} payment_provider={} ledger={}",
        config.environment,
        paymentProvider.id,
        accounting.backendName
    )
    embeddedServer(Netty, host = config.host, port = config.port) {
        monitor.subscribe(ApplicationStopped) {
            runCatching { accounting.close() }
            runCatching { paymentProvider.close() }
        }
        kuotaModule(store, services, config, accounting, paymentProvider, webhookProcessor)
    }.start(wait = true)
}

private fun createAccountingStore(config: BackendConfig): AccountingStore =
    config.database?.let { PostgresAccountingStore(it) }
        ?: FileAccountingStore(
            ledgerFile = config.dataDirectory.resolve("kuota-ledger.ndjson"),
            webhookFile = config.dataDirectory.resolve("kuota-webhook-events.ndjson")
        )

internal class KuotaServices(
    val claims: ClaimStore,
    val forwarding: ClaimForwardingService,
    val devices: DeviceStore,
    val push: PushService,
    val reminders: ReminderScheduler,
    val declaredSales: DeclaredSalesStore
)

private const val MAX_WEBHOOK_BODY_CHARS = 64_000

private fun ApplicationCall.isDeclaredBodyTooLarge(): Boolean =
    request.headers[io.ktor.http.HttpHeaders.ContentLength]?.toLongOrNull()?.let { it > MAX_WEBHOOK_BODY_CHARS } == true

private fun ApplicationCall.clientAddress(): String =
    request.origin.remoteHost.ifBlank { "local" }

private suspend fun ApplicationCall.enforceRateLimit(
    limiter: InMemoryRateLimiter,
    key: String,
    limit: Int,
    windowSeconds: Long
): Boolean {
    val decision = limiter.check(key, limit, windowSeconds)
    if (decision.allowed) return true
    response.headers.append(HttpHeaders.RetryAfter, decision.retryAfterSeconds.toString())
    respond(
        HttpStatusCode.TooManyRequests,
        apiError(
            "RATE_LIMITED",
            "Demasiadas solicitudes. Espera ${decision.retryAfterSeconds} segundos e inténtalo de nuevo.",
            retryable = true
        )
    )
    return false
}

private suspend fun ApplicationCall.requireLedgerAdmin(config: BackendConfig): Boolean {
    if (config.adminToken == null) {
        respond(HttpStatusCode.ServiceUnavailable, apiError("LEDGER_ADMIN_DISABLED", "Set KUOTA_ADMIN_TOKEN to enable ledger inspection."))
        return false
    }
    val candidate = request.headers["X-Kuota-Admin-Token"]
        ?: request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()
    if (!config.isAdminTokenValid(candidate)) {
        respond(HttpStatusCode.Unauthorized, apiError("ADMIN_TOKEN_INVALID", "A valid admin token is required."))
        return false
    }
    return true
}

@Serializable
private data class LedgerTransactionsResponse(
    @SerialName("ledger_backend") val ledgerBackend: String,
    val environment: String,
    val transactions: List<LedgerTransaction>
)

@Serializable
private data class LedgerBalancesResponse(
    @SerialName("ledger_backend") val ledgerBackend: String,
    val environment: String,
    val balances: List<AccountBalance>,
    val balanced: Boolean
)

@Serializable
internal data class VerifySupplierQrRequest(
    @SerialName("qr_payload") val qrPayload: String
)

@Serializable
internal data class VerifiedSupplierQrResponse(
    @SerialName("transaction_id") val transactionId: String,
    @SerialName("supplier_id") val supplierId: String,
    @SerialName("supplier_name") val supplierName: String,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("expires_at_epoch_millis") val expiresAtEpochMillis: Long,
    @SerialName("demo_only") val demoOnly: Boolean
)

@Serializable
internal data class GenerateSupplierQrResponse(
    @SerialName("signed_qr_payload") val signedQrPayload: String,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("supplier_name") val supplierName: String,
    @SerialName("expires_in_minutes") val expiresInMinutes: Int
)

@Serializable
internal data class ConfirmSupplierSaleRequest(
    @SerialName("qr_payload") val qrPayload: String
)

@Serializable
internal data class SupplierConfirmationResponse(
    @SerialName("confirmation_id") val confirmationId: String,
    @SerialName("transaction_id") val transactionId: String,
    val status: String,
    @SerialName("supplier_name") val supplierName: String,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("immediate_payout_cents") val immediatePayoutCents: Long,
    @SerialName("deferred_payout_cents") val deferredPayoutCents: Long,
    @SerialName("supplier_fee_cents") val supplierFeeCents: Long,
    @SerialName("confirmed_at") val confirmedAt: String,
    @SerialName("demo_only") val demoOnly: Boolean
)

internal fun Application.kuotaModule(
    store: DemoStore,
    services: KuotaServices,
    config: BackendConfig,
    accounting: AccountingStore,
    paymentProvider: PaymentProviderAdapter,
    webhookProcessor: PaymentWebhookProcessor
) {
    install(RequestIdPlugin)
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
        })
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            if (cause is CancellationException) throw cause
            logger.error("Unhandled request failure request_id={}", call.requestId(), cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                call.apiError(
                    "INTERNAL_ERROR",
                    "No pudimos completar la solicitud. Intenta de nuevo o comparte la referencia con soporte.",
                    retryable = true
                )
            )
        }
    }
    val rateLimiter = InMemoryRateLimiter()

    routing {
        get("/") {
            val serviceDescription = if (config.isProduction) {
                "El backend está configurado con un procesador de pagos real. La operación requiere validación legal, comercial y de cumplimiento antes de recibir dinero."
            } else {
                "El backend local está activo en modo demostración. No procesa pagos reales ni crea deudas reales."
            }
            call.respondText(
                """
                <!doctype html>
                <html lang="es">
                  <head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Kuota API</title></head>
                  <body style="font-family:system-ui,sans-serif;max-width:42rem;margin:4rem auto;padding:0 1rem;color:#18212f">
                    <h1>Kuota API</h1>
                    <p>$serviceDescription</p>
                    <p>Estado: <a href="/health">comprobar API</a> | Portal B2B: <a href="/supplier-portal">Portal Proveedores</a></p>
                  </body>
                </html>
                """.trimIndent(),
                ContentType.Text.Html
            )
        }

        get("/supplier-portal") {
            val requestedAmountPen = call.request.queryParameters["amountPen"]
                ?.toDoubleOrNull()
                ?.takeIf { it.isFinite() && it in 1.0..1_000_000.0 }
                ?: 1_000.0
            val requestedAmountCents = Math.round(requestedAmountPen * 100.0)
            val sampleSignedQr = DynamicQrSigner.generateSignedQrPayload(
                transactionId = "tx-demo-${requestedAmountCents}",
                supplierId = DEMO_SUPPLIER_ID,
                supplierName = DEMO_SUPPLIER_NAME,
                amountCents = requestedAmountCents,
                validityMinutes = 30
            )
            val settlements = store.getSupplierSettlements()
            val settlementRowsHtml = if (settlements.isEmpty()) {
                """
                <tr>
                    <td><code>tx-demo-1000</code></td>
                    <td>MYPE Kouta Perú S.A.C. (ejemplo)</td>
                    <td>S/ 1,000.00</td>
                    <td><strong>S/ 600.00</strong></td>
                    <td>S/ 400.00 (neto S/ 360.00)</td>
                    <td>S/ 40.00</td>
                    <td>0 / 4 cuotas</td>
                    <td><span class="badge">DEMO_EJEMPLO_REFERENCIAL</span></td>
                </tr>
                """.trimIndent()
            } else {
                settlements.joinToString("\n") { item ->
                    """
                    <tr>
                        <td><code>${item.purchaseId}</code></td>
                        <td>${item.supplierName}</td>
                        <td>${formatPenFromCents(item.purchaseAmountCents)}</td>
                        <td><strong>${formatPenFromCents(item.immediatePayoutCents)}</strong></td>
                        <td>${formatPenFromCents(item.deferredGrossCents)} (neto ${formatPenFromCents(item.deferredNetCents)})</td>
                        <td>${formatPenFromCents(item.supplierFeeCents)}</td>
                        <td>${item.paidInstallmentsCount} / ${item.totalInstallmentsCount} cuotas</td>
                        <td><span class="badge">${item.settlementStatus}</span></td>
                    </tr>
                    """.trimIndent()
                }
            }

            call.respondText(
                """
                <!doctype html>
                <html lang="es">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Kouta Perú — Portal B2B de Proveedores</title>
                    <style>
                        body { font-family: system-ui, -apple-system, sans-serif; background: #f7f8fa; color: #0f172a; margin: 0; padding: 2rem; }
                        .card { background: #ffffff; border-radius: 1rem; padding: 1.5rem; box-shadow: 0 2px 8px rgba(15,23,42,0.06); margin-bottom: 1.5rem; max-width: 60rem; border: 1px solid #e2e8f0; }
                        h1 { color: #101e38; margin-top: 0; }
                        h2 { color: #101e38; margin-top: 0; }
                        .btn { background: #101e38; color: #ffffff; border: none; border-radius: 0.5rem; padding: 0.65rem 1.15rem; font-weight: bold; cursor: pointer; }
                        .btn:hover { background: #1d3258; }
                        .badge { background: #dff5eb; color: #118056; padding: 0.25rem 0.6rem; border-radius: 0.5rem; font-size: 0.82rem; font-weight: bold; }
                        table { width: 100%; border-collapse: collapse; margin-top: 1rem; font-size: 0.92rem; }
                        th, td { text-align: left; padding: 0.75rem; border-bottom: 1px solid #e2e8f0; }
                        th { background: #f1f4f9; color: #101e38; }
                        .qr-box { background: #f1f4f9; border: 2px dashed #101e38; padding: 1rem; border-radius: 0.75rem; font-family: monospace; word-break: break-all; margin-top: 1rem; }
                        .form-row { display: flex; gap: 0.75rem; align-items: center; margin-top: 0.75rem; flex-wrap: wrap; }
                        input[type="number"] { padding: 0.6rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 0.5rem; font-size: 0.95rem; width: 11rem; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <h1>Kouta Perú — Portal B2B de Proveedores (Demo)</h1>
                        <p><strong>Proveedor Afiliado:</strong> $DEMO_SUPPLIER_NAME (RUC demo: 20987654321)</p>
                        <p>Esquema de liquidación B2B: <strong>60% al inicio</strong> (30% comprador + 30% Kouta, o hasta 60% Kouta según historial aprobado) y <strong>40% diferido</strong> menos <strong>4% de comisión proveedor</strong>.</p>
                        <span class="badge">DEMO_SIN_DESEMBOLSO_REAL</span>
                    </div>

                    <div class="card">
                        <h2>Generador de QR Dinámico Firmado (HMAC-SHA256)</h2>
                        <p>Genera un payload QR firmado criptográficamente con vigencia de 30 minutos para simular el cobro de mercadería:</p>
                        <form method="get" action="/supplier-portal" class="form-row">
                            <label for="amountPen"><strong>Monto de venta (S/):</strong></label>
                            <input id="amountPen" name="amountPen" type="number" step="0.01" min="1" max="1000000" value="${"%.2f".format(java.util.Locale.US, requestedAmountPen)}" />
                            <button type="submit" class="btn">Firmar nuevo QR</button>
                        </form>
                        <div class="qr-box">
                            <strong>Payload QR Dinámico (${formatPenFromCents(requestedAmountCents)}):</strong><br>
                            <code>$sampleSignedQr</code>
                        </div>
                    </div>

                    <div class="card">
                        <h2>Liquidaciones y Ventas MYPE Registradas (${settlements.size} operaciones en DemoStore)</h2>
                        <table>
                            <thead>
                                <tr>
                                    <th>ID Operación</th>
                                    <th>Proveedor</th>
                                    <th>Total Compra</th>
                                    <th>Abono Inmediato (60%)</th>
                                    <th>Abono Diferido (40%)</th>
                                    <th>Comisión (4%)</th>
                                    <th>Cobranza Cuotas</th>
                                    <th>Estado Liquidación</th>
                                </tr>
                            </thead>
                            <tbody>
                                $settlementRowsHtml
                            </tbody>
                        </table>
                    </div>
                </body>
                </html>
                """.trimIndent(),
                ContentType.Text.Html
            )
        }

        get("/health") {
            call.respond(
                mapOf(
                    "status" to "ok",
                    "environment" to config.environment.name,
                    "demo_only" to (!config.isProduction).toString(),
                    "payment_provider" to paymentProvider.id,
                    "ledger" to accounting.backendName,
                    "mercadopago_webhook" to if (config.mercadoPago != null) "CONFIGURED" else "NOT_CONFIGURED",
                    "hmac_webhook" to if (config.webhookHmacSecret != null) "CONFIGURED" else "NOT_CONFIGURED"
                )
            )
        }

        route("/v1") {
            post("/webhooks/mercadopago") {
                if (call.isDeclaredBodyTooLarge()) {
                    call.respond(HttpStatusCode.PayloadTooLarge, call.apiError("WEBHOOK_TOO_LARGE", "The webhook payload is too large."))
                    return@post
                }
                val rawBody = call.receiveText()
                if (rawBody.length > MAX_WEBHOOK_BODY_CHARS) {
                    call.respond(HttpStatusCode.PayloadTooLarge, call.apiError("WEBHOOK_TOO_LARGE", "The webhook payload is too large."))
                    return@post
                }
                val webhookSecret = config.mercadoPago?.webhookSecret
                if (webhookSecret == null) {
                    call.respond(HttpStatusCode.ServiceUnavailable, call.apiError("MERCADO_PAGO_NOT_CONFIGURED", "Mercado Pago webhooks are not configured."))
                    return@post
                }
                val dataId = call.request.queryParameters["data.id"]
                val valid = MercadoPagoSignatureVerifier(webhookSecret).verify(
                    signatureHeader = call.request.headers["x-signature"],
                    requestId = call.request.headers["x-request-id"],
                    dataId = dataId
                )
                if (!valid) {
                    call.respond(HttpStatusCode.Unauthorized, call.apiError("WEBHOOK_SIGNATURE_INVALID", "The webhook signature is missing or invalid."))
                    return@post
                }
                val outcome = webhookProcessor.processMercadoPago(rawBody, dataId)
                call.respond(HttpStatusCode.fromValue(outcome.httpStatus), outcome.body)
            }

            post("/webhooks/hmac") {
                if (call.isDeclaredBodyTooLarge()) {
                    call.respond(HttpStatusCode.PayloadTooLarge, call.apiError("WEBHOOK_TOO_LARGE", "The webhook payload is too large."))
                    return@post
                }
                val rawBody = call.receiveText()
                if (rawBody.length > MAX_WEBHOOK_BODY_CHARS) {
                    call.respond(HttpStatusCode.PayloadTooLarge, call.apiError("WEBHOOK_TOO_LARGE", "The webhook payload is too large."))
                    return@post
                }
                val webhookSecret = config.webhookHmacSecret
                if (webhookSecret == null) {
                    call.respond(HttpStatusCode.ServiceUnavailable, call.apiError("HMAC_WEBHOOK_NOT_CONFIGURED", "Configure KUOTA_WEBHOOK_SECRET to receive HMAC webhooks."))
                    return@post
                }
                val valid = HmacWebhookVerifier.verify(
                    secret = webhookSecret,
                    timestampHeader = call.request.headers["X-Kuota-Timestamp"],
                    signatureHeader = call.request.headers["X-Kuota-Signature"],
                    body = rawBody.toByteArray(StandardCharsets.UTF_8)
                )
                if (!valid) {
                    call.respond(HttpStatusCode.Unauthorized, call.apiError("WEBHOOK_SIGNATURE_INVALID", "The webhook HMAC signature is missing, expired, or invalid."))
                    return@post
                }
                val outcome = webhookProcessor.processHmac(rawBody)
                call.respond(HttpStatusCode.fromValue(outcome.httpStatus), outcome.body)
            }

            get("/admin/ledger/transactions") {
                if (!call.requireLedgerAdmin(config)) return@get
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                if (limit !in 1..500) {
                    call.respond(HttpStatusCode.BadRequest, call.apiError("LEDGER_LIMIT_INVALID", "limit must be between 1 and 500."))
                    return@get
                }
                val environment = call.request.queryParameters["environment"]?.uppercase() ?: config.environment.name
                if (environment !in setOf(LEDGER_ENV_DEMO, LEDGER_ENV_PRODUCTION)) {
                    call.respond(HttpStatusCode.BadRequest, call.apiError("LEDGER_ENVIRONMENT_INVALID", "environment must be DEMO or PRODUCTION."))
                    return@get
                }
                val transactions = accounting.transactions(limit).filter { it.environment == environment }
                call.respond(
                    LedgerTransactionsResponse(
                        ledgerBackend = accounting.backendName,
                        environment = environment,
                        transactions = transactions
                    )
                )
            }

            get("/admin/ledger/balances") {
                if (!call.requireLedgerAdmin(config)) return@get
                val environment = call.request.queryParameters["environment"]?.uppercase() ?: config.environment.name
                if (environment !in setOf(LEDGER_ENV_DEMO, LEDGER_ENV_PRODUCTION)) {
                    call.respond(HttpStatusCode.BadRequest, call.apiError("LEDGER_ENVIRONMENT_INVALID", "environment must be DEMO or PRODUCTION."))
                    return@get
                }
                val balances = accounting.balances(environment)
                call.respond(
                    LedgerBalancesResponse(
                        ledgerBackend = accounting.backendName,
                        environment = environment,
                        balances = balances,
                        balanced = balances.sumOf { it.netDebitCents } == 0L
                    )
                )
            }
            supportRoutes(services.claims, services.forwarding) { record ->
                val label = if (record.submission.type == "QUEJA") "queja" else "reclamo"
                services.push.notifyUser(
                    record.userId,
                    PushMessage(
                        type = "SUPPORT_UPDATE",
                        title = if (record.status == "RESPONDIDO") "Respondimos tu $label" else "Tu $label está en atención",
                        body = "Hoja ${record.claimNumber}. Revisa el estado en Ayuda y reclamos.",
                        deepLink = "kuota://soporte",
                        extras = mapOf("claim_number" to record.claimNumber, "claim_status" to record.status)
                    )
                ) { it.supportUpdates }
            }
            declaredSalesRoutes(services.declaredSales)
            pushRoutes(services.devices, services.push, services.reminders)

            post("/supplier/qr/verify") {
                if (!call.enforceRateLimit(
                        rateLimiter,
                        "supplier-qr-verify:${call.clientAddress()}",
                        limit = 30,
                        windowSeconds = 60
                    )
                ) return@post
                val request = call.receive<VerifySupplierQrRequest>()
                if (request.qrPayload.length > QR_MAX_PAYLOAD_CHARS) {
                    call.respond(HttpStatusCode.UnprocessableEntity, call.apiError("QR_PAYLOAD_TOO_LARGE", "El código QR enviado es demasiado grande."))
                    return@post
                }
                val verified = DynamicQrSigner.verifyQrPayload(request.qrPayload)
                if (verified == null) {
                    call.respond(
                        HttpStatusCode.UnprocessableEntity,
                        call.apiError("QR_INVALID_OR_EXPIRED", "El QR del proveedor no es válido o expiró. Pide al proveedor que genere uno nuevo.")
                    )
                    return@post
                }
                call.response.headers.append(HttpHeaders.CacheControl, "no-store")
                call.respond(
                    VerifiedSupplierQrResponse(
                        transactionId = verified.transactionId,
                        supplierId = verified.supplierId,
                        supplierName = verified.supplierName,
                        amountCents = verified.amountCents,
                        expiresAtEpochMillis = verified.expiresAtEpochMillis,
                        demoOnly = !config.isProduction
                    )
                )
            }

            post("/supplier/generate-qr") {
                if (config.isProduction) {
                    call.respond(HttpStatusCode.Forbidden, call.apiError("SUPPLIER_DEMO_ONLY", "La generación de QR demo no está habilitada en producción."))
                    return@post
                }
                if (!call.enforceRateLimit(
                        rateLimiter,
                        "supplier-qr-generate:${call.clientAddress()}",
                        limit = 10,
                        windowSeconds = 60
                    )
                ) return@post

                val params = call.receiveParameters()
                val amountPen = params["amountPen"]?.toDoubleOrNull() ?: 1_000.0
                if (!amountPen.isFinite() || amountPen <= 0.0 || amountPen > 1_000_000.0) {
                    call.respond(HttpStatusCode.UnprocessableEntity, call.apiError("QR_AMOUNT_INVALID", "El monto debe ser mayor que cero y menor o igual a S/ 1,000,000."))
                    return@post
                }
                val amountCents = Math.round(amountPen * 100.0)
                if (amountCents <= 0) {
                    call.respond(HttpStatusCode.UnprocessableEntity, call.apiError("QR_AMOUNT_INVALID", "El monto no se puede representar en céntimos."))
                    return@post
                }

                val signedQr = DynamicQrSigner.generateSignedQrPayload(
                    transactionId = "tx-gen-${UUID.randomUUID().toString().take(8)}",
                    supplierId = DEMO_SUPPLIER_ID,
                    supplierName = DEMO_SUPPLIER_NAME,
                    amountCents = amountCents,
                    validityMinutes = 15
                )

                call.respond(
                    GenerateSupplierQrResponse(
                        signedQrPayload = signedQr,
                        amountCents = amountCents,
                        supplierName = DEMO_SUPPLIER_NAME,
                        expiresInMinutes = 15
                    )
                )
            }

            post("/supplier/confirm-sale") {
                if (config.isProduction) {
                    call.respond(HttpStatusCode.Forbidden, call.apiError("SUPPLIER_DEMO_ONLY", "La confirmación demo del proveedor no está habilitada en producción."))
                    return@post
                }
                if (!call.enforceRateLimit(
                        rateLimiter,
                        "supplier-confirm:${call.clientAddress()}",
                        limit = 20,
                        windowSeconds = 60
                    )
                ) return@post

                val request = call.receive<ConfirmSupplierSaleRequest>()
                val verified = DynamicQrSigner.verifyQrPayload(request.qrPayload)
                if (verified == null) {
                    call.respond(
                        HttpStatusCode.UnprocessableEntity,
                        call.apiError("QR_INVALID_OR_EXPIRED", "El QR del proveedor no es válido o expiró. No se puede simular la confirmación.")
                    )
                    return@post
                }
                val immediate = percentageOf(verified.amountCents, 6_000)
                val deferred = verified.amountCents - immediate
                val fee = percentageOf(verified.amountCents, 400)
                call.respond(
                    SupplierConfirmationResponse(
                        confirmationId = "demo-confirm-${UUID.randomUUID()}",
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
                )
            }

            post("/auth/request-otp") {
                if (!call.enforceRateLimit(
                        rateLimiter,
                        "otp-request:ip:${call.clientAddress()}",
                        limit = 10,
                        windowSeconds = 600
                    )
                ) return@post
                if (config.isProduction) {
                    call.respond(
                        HttpStatusCode.ServiceUnavailable,
                        call.apiError("OTP_PROVIDER_NOT_CONFIGURED", "El envío real de códigos no está configurado en este entorno.")
                    )
                    return@post
                }
                val request = call.receive<RequestOtpRequest>()
                val digitsPhone = request.phone.filter(Char::isDigit).takeLast(9)
                if (digitsPhone.length != 9) {
                    call.respond(HttpStatusCode.BadRequest, call.apiError("PHONE_INVALID", "El número celular debe tener 9 dígitos."))
                    return@post
                }
                if (!call.enforceRateLimit(
                        rateLimiter,
                        "otp-request:phone:$digitsPhone",
                        limit = 3,
                        windowSeconds = 600
                    )
                ) return@post
                call.respond(
                    RequestOtpResponse(
                        otpCodeDebug = DEMO_OTP_CODE,
                        message = "Modo demo: no se envió un SMS. Usa el código mostrado en la app."
                    )
                )
            }

            post("/auth/verify-otp") {
                if (!call.enforceRateLimit(
                        rateLimiter,
                        "otp-verify:ip:${call.clientAddress()}",
                        limit = 20,
                        windowSeconds = 600
                    )
                ) return@post
                if (config.isProduction) {
                    call.respond(
                        HttpStatusCode.ServiceUnavailable,
                        call.apiError("OTP_PROVIDER_NOT_CONFIGURED", "La verificación real de códigos no está configurada en este entorno.")
                    )
                    return@post
                }
                val request = call.receive<VerifyOtpRequest>()
                val digitsPhone = request.phone.filter(Char::isDigit).takeLast(9)
                if (digitsPhone.length != 9) {
                    call.respond(HttpStatusCode.BadRequest, call.apiError("PHONE_INVALID", "El número celular es inválido."))
                    return@post
                }
                if (!call.enforceRateLimit(
                        rateLimiter,
                        "otp-verify:phone:$digitsPhone",
                        limit = 5,
                        windowSeconds = 600
                    )
                ) return@post
                if (request.otpCode.trim() != DEMO_OTP_CODE) {
                    call.respond(HttpStatusCode.Unauthorized, call.apiError("OTP_INVALID", "Código OTP incorrecto o expirado."))
                    return@post
                }

                val userId = "user-" + digitsPhone
                val jwtToken = JwtConfig.makeToken(userId, digitsPhone)

                call.respond(
                    VerifyOtpResponse(
                        accessToken = jwtToken,
                        user = UserProfileDto(
                            userId = userId,
                            phone = "+51 $digitsPhone",
                            email = "representante@kuota.pe",
                            kycStatus = "VERIFIED_MOCK",
                            createdAt = LocalDate.now(ZoneId.of("America/Lima")).toString()
                        )
                    )
                )
            }

            get("/me") {
                val claims = call.requireUser() ?: return@get

                call.respond(
                    UserProfileDto(
                        userId = claims.userId,
                        phone = claims.phone,
                        email = "representante@kuota.pe",
                        kycStatus = "VERIFIED_MOCK",
                        createdAt = LocalDate.now(ZoneId.of("America/Lima")).toString()
                    )
                )
            }

            post("/kyc/dni") {
                val request = call.receive<SubmitKycDniRequest>()
                val dniDigits = request.dni.filter(Char::isDigit)
                if (dniDigits.length != 8) {
                    call.respond(HttpStatusCode.BadRequest, call.apiError("DNI_INVALID", "El DNI debe tener exactamente 8 dígitos numéricos."))
                    return@post
                }
                if (request.fullName.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, call.apiError("NAME_REQUIRED", "El nombre completo del titular es obligatorio."))
                    return@post
                }

                call.respond(
                    SubmitKycDniResponse(
                        kycStatus = "VERIFIED_MOCK",
                        dni = dniDigits,
                        fullName = request.fullName.trim(),
                        message = "DNI $dniDigits verificado con éxito para ${request.fullName.trim()}."
                    )
                )
            }

            post("/identity/sessions") {
                call.respond(
                    HttpStatusCode.NotImplemented,
                    call.apiError("IDENTITY_PROVIDER_NOT_CONFIGURED", "Identity verification is not available in this demo.")
                )
            }

            post("/eligibility/checks") {
                if (!call.enforceRateLimit(
                        rateLimiter,
                        "eligibility:ip:${call.clientAddress()}",
                        limit = 30,
                        windowSeconds = 60
                    )
                ) return@post
                val request = call.receive<EligibilityRequest>()
                if (request.businessId != DEMO_BUSINESS_ID || request.amountCents <= 0 || request.termDays !in allowedTerms) {
                    call.respond(
                        HttpStatusCode.UnprocessableEntity,
                        call.apiError("DEMO_REQUEST_INVALID", "Only the documented demo business and supported terms can be simulated.")
                    )
                    return@post
                }
                if (request.consentVersion.isBlank()) {
                    call.respond(HttpStatusCode.UnprocessableEntity, call.apiError("CONSENT_VERSION_REQUIRED", "A consent version is required."))
                    return@post
                }

                val optionalUser = call.optionalUser()
                val decision = store.evaluateDemoEligibility(
                    ownerUserId = optionalUser?.userId,
                    simulatedPaidInstallments = request.simulatedPaidInstallments
                )
                call.respond(
                    EligibilityResponse(
                        decisionId = decision.decisionId,
                        status = "DEMO_APPROVED",
                        approvedAmountCents = request.amountCents,
                        downPaymentBasisPoints = decision.downPaymentBasisPoints,
                        policyVersion = decision.policyVersion,
                        riskScore = decision.riskScore,
                        riskTier = decision.riskTier,
                        completedInstallmentsCount = decision.completedInstallmentsCount,
                        expiresAt = LocalDate.now(ZoneId.of("America/Lima")).plusDays(1).toString()
                    )
                )
            }

            get("/supplier/settlements") {
                call.respond(store.getSupplierSettlements())
            }

            post("/quotes") {
                if (!call.enforceRateLimit(
                        rateLimiter,
                        "quotes:ip:${call.clientAddress()}",
                        limit = 30,
                        windowSeconds = 60
                    )
                ) return@post
                val request = call.receive<QuoteRequest>()
                if (request.businessId != DEMO_BUSINESS_ID ||
                    request.amountCents <= 0 || request.termDays !in allowedTerms ||
                    !store.hasDemoDecision(request.eligibilityDecisionId)
                ) {
                    call.respond(HttpStatusCode.UnprocessableEntity, call.apiError("DEMO_QUOTE_INVALID", "The demo buyer, amount, term, or eligibility decision is invalid."))
                    return@post
                }

                val supplier = if (request.supplierQrPayload == DEMO_SUPPLIER_QR && !config.isProduction) {
                    SupplierSummary(DEMO_SUPPLIER_ID, DEMO_SUPPLIER_NAME)
                } else {
                    val verified = DynamicQrSigner.verifyQrPayload(request.supplierQrPayload)
                    if (verified == null) {
                        call.respond(
                            HttpStatusCode.UnprocessableEntity,
                            call.apiError("QR_INVALID_OR_EXPIRED", "El QR del proveedor no es válido o expiró. Pide uno nuevo antes de cotizar.")
                        )
                        return@post
                    }
                    if (verified.amountCents != request.amountCents) {
                        call.respond(
                            HttpStatusCode.UnprocessableEntity,
                            call.apiError("QR_AMOUNT_MISMATCH", "El monto solicitado no coincide con el monto firmado en el QR del proveedor.")
                        )
                        return@post
                    }
                    SupplierSummary(verified.supplierId, verified.supplierName)
                }

                val quote = try {
                    store.createQuote(request, supplier)
                } catch (_: ArithmeticException) {
                    call.respond(HttpStatusCode.UnprocessableEntity, call.apiError("DEMO_AMOUNT_OUT_OF_RANGE", "The amount cannot be represented safely in integer céntimos."))
                    return@post
                }
                call.respond(quote)
            }

            post("/purchases") {
                val user = call.requireUser() ?: return@post
                if (!call.enforceRateLimit(
                        rateLimiter,
                        "purchases:user:${user.userId}",
                        limit = 30,
                        windowSeconds = 60
                    )
                ) return@post
                val idempotencyKey = call.request.headers["Idempotency-Key"].orEmpty()
                if (idempotencyKey.isBlank() || idempotencyKey.length > 128) {
                    call.respond(HttpStatusCode.BadRequest, call.apiError("IDEMPOTENCY_KEY_REQUIRED", "A valid idempotency key is required."))
                    return@post
                }
                val request = call.receive<CreatePurchaseRequest>()
                when (val result = store.createPurchase(idempotencyKey, request, user.userId)) {
                    is PurchaseCreateResult.Success -> {
                        call.respond(if (result.replayed) HttpStatusCode.OK else HttpStatusCode.Created, result.purchase)
                    }
                    is PurchaseCreateResult.Rejected -> {
                        val status = when (result.code) {
                            "IDEMPOTENCY_KEY_REUSED", "QUOTE_ALREADY_USED" -> HttpStatusCode.Conflict
                            else -> HttpStatusCode.UnprocessableEntity
                        }
                        call.respond(status, call.apiError(result.code, result.message))
                    }
                }
            }

            get("/purchases") {
                val user = call.requireUser() ?: return@get
                call.respond(store.getPurchaseHistory(user.userId))
            }

            get("/purchases/{purchaseId}") {
                val user = call.requireUser() ?: return@get
                val purchaseId = call.parameters["purchaseId"].orEmpty()
                val purchase = store.getPurchase(purchaseId, user.userId)
                if (purchase == null) {
                    call.respond(HttpStatusCode.NotFound, call.apiError("PURCHASE_NOT_FOUND", "The demo purchase was not found."))
                    return@get
                }
                call.respond(purchase)
            }

            get("/purchases/{purchaseId}/installments") {
                val user = call.requireUser() ?: return@get
                val purchaseId = call.parameters["purchaseId"].orEmpty()
                val installments = store.getInstallments(purchaseId, user.userId)
                if (installments == null) {
                    call.respond(HttpStatusCode.NotFound, call.apiError("PURCHASE_NOT_FOUND", "The demo purchase was not found."))
                    return@get
                }
                call.respond(InstallmentsResponse(purchaseId, installments))
            }

            post("/installments/{installmentId}/payment-intents") {
                val user = call.requireUser() ?: return@post
                if (!call.enforceRateLimit(
                        rateLimiter,
                        "payment-intents:user:${user.userId}",
                        limit = 20,
                        windowSeconds = 60
                    )
                ) return@post
                val key = call.request.headers["Idempotency-Key"].orEmpty()
                if (key.isBlank() || key.length > 128) {
                    call.respond(HttpStatusCode.BadRequest, call.apiError("IDEMPOTENCY_KEY_REQUIRED", "A valid idempotency key is required."))
                    return@post
                }
                val installmentId = call.parameters["installmentId"].orEmpty()
                val request = call.receive<CreatePaymentIntentRequest>()
                val demoOnly = !config.isProduction
                when (val result = store.createPaymentIntent(key, installmentId, request, user.userId)) {
                    is PaymentCreateResult.Success -> call.respond(
                        if (result.replayed) HttpStatusCode.OK else HttpStatusCode.Created,
                        result.payment.toResponse(demoOnly)
                    )
                    is PaymentCreateResult.Rejected -> {
                        val status = when (result.code) {
                            "IDEMPOTENCY_KEY_REUSED", "PAYMENT_ALREADY_PENDING", "INSTALLMENT_ALREADY_PAID", "PAYMENT_RECONCILIATION_REQUIRED" -> HttpStatusCode.Conflict
                            "PAYMENT_PROVIDER_UNAVAILABLE", "PAYMENT_PROVIDER_ERROR" -> HttpStatusCode.BadGateway
                            "INSTALLMENT_NOT_FOUND" -> HttpStatusCode.NotFound
                            else -> HttpStatusCode.UnprocessableEntity
                        }
                        call.respond(status, call.apiError(result.code, result.message, retryable = status.value >= 500))
                    }
                }
            }

            get("/payments/{paymentId}") {
                val user = call.requireUser() ?: return@get
                val payment = store.getPayment(call.parameters["paymentId"].orEmpty(), user.userId)
                if (payment == null) {
                    call.respond(HttpStatusCode.NotFound, call.apiError("PAYMENT_NOT_FOUND", "The payment was not found."))
                    return@get
                }
                call.respond(payment.toResponse(!config.isProduction))
            }

            post("/payments/{paymentId}/demo-events") {
                if (paymentProvider.isReal || config.isProduction) {
                    call.respond(HttpStatusCode.Forbidden, call.apiError("DEMO_EVENTS_DISABLED", "Demo payment events are disabled when a real payment provider is configured."))
                    return@post
                }
                val user = call.requireUser() ?: return@post
                if (!call.enforceRateLimit(
                        rateLimiter,
                        "demo-events:user:${user.userId}",
                        limit = 30,
                        windowSeconds = 60
                    )
                ) return@post
                val key = call.request.headers["Idempotency-Key"].orEmpty()
                if (key.isBlank() || key.length > 128) {
                    call.respond(HttpStatusCode.BadRequest, call.apiError("IDEMPOTENCY_KEY_REQUIRED", "A valid idempotency key is required."))
                    return@post
                }
                val paymentId = call.parameters["paymentId"].orEmpty()
                val request = call.receive<DemoPaymentEventRequest>()
                when (val result = store.applyDemoPaymentEvent(key, paymentId, request, user.userId)) {
                    is PaymentEventResult.Success -> call.respond(HttpStatusCode.OK, result.payment.toResponse(!config.isProduction))
                    is PaymentEventResult.Rejected -> call.respond(
                        if (result.code in setOf("IDEMPOTENCY_KEY_REUSED", "PAYMENT_TRANSITION_INVALID")) HttpStatusCode.Conflict else HttpStatusCode.NotFound,
                        call.apiError(result.code, result.message)
                    )
                }
            }

            get("/demo/reconciliation") {
                if (!call.requireLedgerAdmin(config)) return@get
                call.respond(store.getReconciliationQueue())
            }
        }
    }
}

internal class DemoStore(
    private val file: Path,
    private val paymentProvider: PaymentProviderAdapter = DemoPaymentProviderAdapter,
    private val accounting: AccountingStore,
    private val environment: String = LEDGER_ENV_DEMO
) : PaymentSettlementRepository {
    private val lock = Any()
    private val json = Json { ignoreUnknownKeys = true }
    private val demoDecisions = linkedMapOf<String, EligibilityDecisionRecord>()
    private val quotes = linkedMapOf<String, QuoteResponse>()
    private val purchases = linkedMapOf<String, PurchaseRecord>()
    private val idempotencyKeys = linkedMapOf<String, String>()
    private val idempotencyFingerprints = linkedMapOf<String, String>()
    private val paymentIntents = linkedMapOf<String, PaymentRecord>()
    private val paymentIdempotencyKeys = linkedMapOf<String, String>()
    private val paymentIdempotencyFingerprints = linkedMapOf<String, String>()
    private val paymentEventIdempotencyFingerprints = linkedMapOf<String, String>()

    init {
        file.parent?.let { java.nio.file.Files.createDirectories(it) }
        if (java.nio.file.Files.exists(file)) {
            runCatching { json.decodeFromString<PersistedDemoStore>(java.nio.file.Files.readString(file)) }
                .getOrNull()
                ?.let { persisted ->
                    persisted.decisions.forEach { demoDecisions[it.decisionId] = it }
                    persisted.quotes.forEach { quotes[it.quoteId] = it }
                    persisted.purchases.forEach { purchases[it.purchase.purchaseId] = it }
                    idempotencyKeys.putAll(persisted.idempotencyKeys)
                    idempotencyFingerprints.putAll(persisted.idempotencyFingerprints)
                    persisted.paymentIntents.forEach { paymentIntents[it.paymentId] = it }
                    paymentIdempotencyKeys.putAll(persisted.paymentIdempotencyKeys)
                    paymentIdempotencyFingerprints.putAll(persisted.paymentIdempotencyFingerprints)
                    paymentEventIdempotencyFingerprints.putAll(persisted.paymentEventIdempotencyFingerprints)
                }
        }
    }

    fun recordDemoDecision(
        id: String,
        downPaymentBasisPoints: Int = 3_000,
        policyVersion: String = DEMO_ELIGIBILITY_POLICY_VERSION,
        riskScore: Int = 620,
        riskTier: String = "ENTRY_30",
        completedInstallmentsCount: Int = 0
    ): EligibilityDecisionRecord = synchronized(lock) {
        val record = EligibilityDecisionRecord(
            decisionId = id,
            policyVersion = policyVersion,
            riskScore = riskScore,
            riskTier = riskTier,
            downPaymentBasisPoints = downPaymentBasisPoints.coerceIn(0, 3_000),
            completedInstallmentsCount = completedInstallmentsCount
        )
        demoDecisions[id] = record
        record
    }

    fun evaluateDemoEligibility(
        ownerUserId: String? = null,
        simulatedPaidInstallments: Int? = null
    ): EligibilityDecisionRecord = synchronized(lock) {
        val actualPaidInstallments = if (ownerUserId != null) {
            purchases.values
                .filter { it.ownerUserId == ownerUserId }
                .sumOf { record -> record.installments.count { it.status in setOf("DEMO_PAID", "PAID") } }
        } else {
            0
        }
        val completedInstallments = if (environment == LEDGER_ENV_DEMO && simulatedPaidInstallments != null) {
            simulatedPaidInstallments.coerceIn(0, 100)
        } else {
            actualPaidInstallments
        }
        // PRD §18.21 y §21.2: risk_score y down_payment_basis_points son atributos separados gobernados por política backend versionada.
        val (riskScore, riskTier, downPaymentBps) = when {
            completedInstallments >= 3 -> Triple(860, "TRUSTED_0", 0)
            completedInstallments == 2 -> Triple(790, "PROGRESS_10", 1_000)
            completedInstallments == 1 -> Triple(710, "PROGRESS_20", 2_000)
            else -> Triple(620, "ENTRY_30", 3_000)
        }
        val decisionId = "demo-decision-${UUID.randomUUID()}"
        recordDemoDecision(
            id = decisionId,
            downPaymentBasisPoints = downPaymentBps,
            policyVersion = DEMO_ELIGIBILITY_POLICY_VERSION,
            riskScore = riskScore,
            riskTier = riskTier,
            completedInstallmentsCount = completedInstallments
        )
    }

    fun hasDemoDecision(id: String): Boolean = synchronized(lock) { id in demoDecisions }

    fun getSupplierSettlements(): List<SupplierSettlementItem> = synchronized(lock) {
        purchases.values.mapNotNull { record ->
            val quote = quotes[record.quoteId] ?: return@mapNotNull null
            val paidCount = record.installments.count { it.status in setOf("DEMO_PAID", "PAID") }
            val totalCount = record.installments.size
            val settlementStatus = when {
                totalCount > 0 && paidCount >= totalCount -> "DEMO_SETTLEMENT_COMPLETED"
                paidCount > 0 -> "DEMO_SETTLEMENT_IN_PROGRESS"
                else -> "DEMO_IMMEDIATE_60_SCHEDULED"
            }
            SupplierSettlementItem(
                purchaseId = record.purchase.purchaseId,
                supplierId = quote.supplier.supplierId,
                supplierName = quote.supplier.displayName,
                purchaseAmountCents = quote.purchaseAmountCents,
                immediatePayoutCents = quote.supplierImmediatePayoutCents,
                deferredGrossCents = quote.supplierDeferredPayoutCents,
                supplierFeeCents = quote.supplierFeeCents,
                deferredNetCents = (quote.supplierDeferredPayoutCents - quote.supplierFeeCents).coerceAtLeast(0L),
                paidInstallmentsCount = paidCount,
                totalInstallmentsCount = totalCount,
                settlementStatus = settlementStatus,
                updatedAt = record.purchase.updatedAt,
                demoOnly = environment == LEDGER_ENV_DEMO
            )
        }.asReversed()
    }

    fun dueInstallments(): List<DueInstallment> = synchronized(lock) {
        purchases.values.flatMap { record ->
            record.installments.map {
                DueInstallment(record.purchase.purchaseId, it.installmentId, it.sequenceNumber, it.amountCents, it.dueAt, it.status)
            }
        }
    }

    fun createQuote(
        request: QuoteRequest,
        supplier: SupplierSummary = SupplierSummary(DEMO_SUPPLIER_ID, DEMO_SUPPLIER_NAME)
    ): QuoteResponse = synchronized(lock) {
        val purchaseAmount = request.amountCents
        val decision = demoDecisions[request.eligibilityDecisionId]
        val downPaymentBps = (decision?.downPaymentBasisPoints ?: 3_000).coerceIn(0, 3_000)
        val policyVersion = decision?.policyVersion ?: DEMO_ELIGIBILITY_POLICY_VERSION
        val downPayment = percentageOf(purchaseAmount, downPaymentBps)
        val buyerFee = percentageOf(purchaseAmount, 500)
        val buyerTotal = Math.addExact(purchaseAmount - downPayment, buyerFee)
        val supplierImmediate = percentageOf(purchaseAmount, 6_000)
        val supplierDeferred = purchaseAmount - supplierImmediate
        val koutaContribution = (supplierImmediate - downPayment).coerceAtLeast(0L)
        val supplierFee = percentageOf(purchaseAmount, 400)
        val count = when (request.termDays) {
            15 -> 2
            30 -> 4
            60 -> 8
            90 -> 12
            else -> error("Validated term became invalid")
        }
        val baseInstallment = buyerTotal / count
        val remainder = buyerTotal % count
        val firstDue = LocalDate.now(ZoneId.of("America/Lima")).plusWeeks(1)
        val quoteId = "demo-quote-${UUID.randomUUID()}"
        val quote = QuoteResponse(
            quoteId = quoteId,
            quoteVersion = "demo-v1",
            policyVersion = policyVersion,
            downPaymentBasisPoints = downPaymentBps,
            expiresAt = LocalDate.now(ZoneId.of("America/Lima")).plusDays(1).toString(),
            supplier = supplier,
            purchaseAmountCents = purchaseAmount,
            buyerDownPaymentCents = downPayment,
            buyerFeeCents = buyerFee,
            buyerTotalAfterDownPaymentCents = buyerTotal,
            supplierImmediatePayoutCents = supplierImmediate,
            supplierDeferredPayoutCents = supplierDeferred,
            koutaInitialContributionCents = koutaContribution,
            supplierFeeCents = supplierFee,
            installments = List(count) { index ->
                InstallmentDto(
                    installmentId = "demo-installment-${UUID.randomUUID()}",
                    sequenceNumber = index + 1,
                    amountCents = baseInstallment + if (index == count - 1) remainder else 0,
                    dueAt = firstDue.plusWeeks(index.toLong()).toString(),
                    status = "PROGRAMMED"
                )
            }
        )
        quotes[quoteId] = quote
        persist()
        quote
    }

    fun createPurchase(
        key: String,
        request: CreatePurchaseRequest,
        ownerUserId: String? = null
    ): PurchaseCreateResult = synchronized(lock) {
        val fingerprint = json.encodeToString(request)
        idempotencyKeys[key]?.let { existingId ->
            val existingFingerprint = idempotencyFingerprints[key]
            val existingRecord = purchases[existingId]
            if (ownerUserId != null && existingRecord?.ownerUserId != ownerUserId) {
                return PurchaseCreateResult.Rejected("IDEMPOTENCY_KEY_REUSED", "This idempotency key was already used with a different request.")
            }
            return if (existingFingerprint == fingerprint) {
                existingRecord?.let { record ->
                    quotes[record.quoteId]?.let { quote -> recordPurchaseLedger(record.purchase, quote) }
                    PurchaseCreateResult.Success(record.purchase, replayed = true)
                } ?: PurchaseCreateResult.Rejected("IDEMPOTENCY_RECORD_MISSING", "The demo idempotency record is incomplete.")
            } else {
                PurchaseCreateResult.Rejected("IDEMPOTENCY_KEY_REUSED", "This idempotency key was already used with a different request.")
            }
        }
        val quote = quotes[request.quoteId]
            ?: return PurchaseCreateResult.Rejected("QUOTE_NOT_CONFIRMABLE", "The demo quote was not found.")
        if (quote.quoteVersion != request.quoteVersion ||
            request.acceptedTermsVersion != DEMO_NOT_ACCEPTED_VERSION ||
            request.acceptedPrivacyNoticeVersion != DEMO_NOT_ACCEPTED_VERSION ||
            runCatching { LocalDate.parse(quote.expiresAt).isBefore(LocalDate.now(ZoneId.of("America/Lima"))) }.getOrDefault(false)
        ) return PurchaseCreateResult.Rejected("QUOTE_NOT_CONFIRMABLE", "The demo quote is expired or its versions do not match the demo contract.")
        if (purchases.values.any { it.quoteId == request.quoteId }) {
            return PurchaseCreateResult.Rejected("QUOTE_ALREADY_USED", "A demo quote can create only one demo purchase.")
        }

        val purchase = PurchaseResponse(
            purchaseId = "demo-purchase-${UUID.randomUUID()}",
            status = "DEMO_CREATED",
            updatedAt = LocalDate.now(ZoneId.of("America/Lima")).toString(),
            statusReasonCode = "NO_REAL_PURCHASE_OR_DEBT"
        )
        purchases[purchase.purchaseId] = PurchaseRecord(purchase, quote.quoteId, quote.installments, ownerUserId)
        idempotencyKeys[key] = purchase.purchaseId
        idempotencyFingerprints[key] = fingerprint
        persist()
        recordPurchaseLedger(purchase, quote)
        PurchaseCreateResult.Success(purchase, replayed = false)
    }

    fun getPurchase(id: String, ownerUserId: String? = null): PurchaseResponse? = synchronized(lock) {
        val record = purchases[id] ?: return@synchronized null
        if (ownerUserId != null && record.ownerUserId != ownerUserId) return@synchronized null
        record.purchase
    }

    fun getPurchaseHistory(ownerUserId: String? = null): List<PurchaseHistoryItem> = synchronized(lock) {
        purchases.values
            .filter { ownerUserId == null || it.ownerUserId == ownerUserId }
            .mapNotNull { record ->
                val quote = quotes[record.quoteId] ?: return@mapNotNull null
                PurchaseHistoryItem(
                    purchaseId = record.purchase.purchaseId,
                    status = record.purchase.status,
                    updatedAt = record.purchase.updatedAt,
                    supplierName = quote.supplier.displayName,
                    purchaseAmountCents = quote.purchaseAmountCents,
                    buyerDownPaymentCents = quote.buyerDownPaymentCents,
                    buyerTotalAfterDownPaymentCents = quote.buyerTotalAfterDownPaymentCents,
                    supplierImmediatePayoutCents = quote.supplierImmediatePayoutCents,
                    supplierDeferredPayoutCents = quote.supplierDeferredPayoutCents,
                    koutaInitialContributionCents = quote.koutaInitialContributionCents,
                    supplierFeeCents = quote.supplierFeeCents,
                    installments = record.installments,
                    payments = paymentIntents.values
                        .filter { it.purchaseId == record.purchase.purchaseId }
                        .map { it.toResponse(environment == LEDGER_ENV_DEMO) }
                )
            }.asReversed()
    }

    fun getInstallments(id: String, ownerUserId: String? = null): List<InstallmentDto>? = synchronized(lock) {
        val record = purchases[id] ?: return@synchronized null
        if (ownerUserId != null && record.ownerUserId != ownerUserId) return@synchronized null
        record.installments
    }

    suspend fun createPaymentIntent(
        key: String,
        installmentId: String,
        request: CreatePaymentIntentRequest,
        ownerUserId: String? = null
    ): PaymentCreateResult {
        val prepared = synchronized(lock) {
            val fingerprint = json.encodeToString(PaymentIntentFingerprint(installmentId, request))
            paymentIdempotencyKeys[key]?.let { existingId ->
                if (paymentIdempotencyFingerprints[key] != fingerprint) {
                    return@synchronized PaymentIntentPreparation.Rejected("IDEMPOTENCY_KEY_REUSED", "This idempotency key was already used with different payment details.")
                }
                val existing = paymentIntents[existingId]
                return@synchronized when {
                    existing == null ->
                        PaymentIntentPreparation.Rejected("PAYMENT_RECORD_MISSING", "The payment record is incomplete.")
                    ownerUserId != null && purchases[existing.purchaseId]?.ownerUserId != ownerUserId ->
                        PaymentIntentPreparation.Rejected("IDEMPOTENCY_KEY_REUSED", "This idempotency key was already used with different payment details.")
                    else -> PaymentIntentPreparation.Replayed(existing)
                }
            }
            if (request.paymentMethodId != paymentProvider.paymentMethodId) {
                return@synchronized PaymentIntentPreparation.Rejected(
                    "PAYMENT_METHOD_NOT_CONFIGURED",
                    if (paymentProvider.isReal) {
                        "The configured provider requires payment_method_id=${paymentProvider.paymentMethodId}."
                    } else {
                        "Only the documented demo payment method is available."
                    }
                )
            }
            val purchaseEntry = purchases.entries.firstOrNull { (_, record) ->
                (ownerUserId == null || record.ownerUserId == ownerUserId) &&
                    record.installments.any { it.installmentId == installmentId }
            } ?: return@synchronized PaymentIntentPreparation.Rejected("INSTALLMENT_NOT_FOUND", "The installment was not found.")
            val purchaseId = purchaseEntry.key
            val installment = purchaseEntry.value.installments.first { it.installmentId == installmentId }
            if (installment.status in setOf("DEMO_PAID", "DEMO_PAYMENT_REVIEW", "PAID", "PAYMENT_REVIEW", "REFUNDED", "REVERSED", "DEMO_REFUNDED", "DEMO_REVERSED")) {
                return@synchronized PaymentIntentPreparation.Rejected("INSTALLMENT_ALREADY_PAID", "A payment exists for this installment and must be reviewed before another attempt.")
            }
            val existingForInstallment = paymentIntents.values.filter { it.installmentId == installmentId }
            if (existingForInstallment.any { it.status in setOf("PENDING", "PROCESSING", "ACTION_REQUIRED") }) {
                return@synchronized PaymentIntentPreparation.Rejected("PAYMENT_ALREADY_PENDING", "This installment already has an active payment attempt.")
            }
            if (existingForInstallment.any { it.status in setOf("REVERSED", "REFUNDED") && it.reconciliationStatus == "UNDER_REVIEW" }) {
                return@synchronized PaymentIntentPreparation.Rejected("PAYMENT_RECONCILIATION_REQUIRED", "Resolve the payment reversal or refund before retrying this installment.")
            }
            val quote = quotes[purchaseEntry.value.quoteId]
                ?: return@synchronized PaymentIntentPreparation.Rejected("PURCHASE_QUOTE_MISSING", "The purchase quote is not available.")

            val now = demoTimestamp()
            val payment = PaymentRecord(
                paymentId = paymentProvider.newPaymentId(),
                purchaseId = purchaseId,
                installmentId = installmentId,
                amountCents = installment.amountCents,
                status = "PENDING",
                reconciliationStatus = "NOT_RECONCILED",
                updatedAt = now,
                statusReasonCode = "PAYMENT_INTENT_CREATED",
                events = listOf(
                    PaymentEventRecord(
                        "INTENT_CREATED",
                        "NONE",
                        "PENDING",
                        now,
                        if (paymentProvider.isReal) "PSP_CHECKOUT_PENDING" else "DEMO_ONLY"
                    )
                )
            )
            paymentIntents[payment.paymentId] = payment
            paymentIdempotencyKeys[key] = payment.paymentId
            paymentIdempotencyFingerprints[key] = fingerprint
            updateInstallmentStatus(purchaseId, installmentId, "PAYMENT_PENDING")
            persist()
            PaymentIntentPreparation.Ready(
                payment = payment,
                description = "Cuota ${installment.sequenceNumber} - ${quote.supplier.displayName}"
            )
        }

        return when (prepared) {
            is PaymentIntentPreparation.Replayed -> PaymentCreateResult.Success(prepared.payment, replayed = true)
            is PaymentIntentPreparation.Rejected -> PaymentCreateResult.Rejected(prepared.code, prepared.message)
            is PaymentIntentPreparation.Ready -> {
                val checkout = try {
                    paymentProvider.createCheckout(
                        PaymentCheckoutRequest(
                            internalPaymentId = prepared.payment.paymentId,
                            purchaseId = prepared.payment.purchaseId,
                            installmentId = prepared.payment.installmentId,
                            amountCents = prepared.payment.amountCents,
                            description = prepared.description
                        )
                    )
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    val code = if (error is PaymentProviderException) error.code else "PAYMENT_PROVIDER_ERROR"
                    synchronized(lock) {
                        val current = paymentIntents[prepared.payment.paymentId]
                        if (current != null) {
                            val now = demoTimestamp()
                            paymentIntents[current.paymentId] = current.copy(
                                status = "FAILED",
                                statusReasonCode = code,
                                updatedAt = now,
                                events = current.events + PaymentEventRecord("CHECKOUT_FAILED", current.status, "FAILED", now, code)
                            )
                        }
                        updateInstallmentStatus(prepared.payment.purchaseId, prepared.payment.installmentId, "PROGRAMMED")
                        persist()
                    }
                    return PaymentCreateResult.Rejected("PAYMENT_PROVIDER_UNAVAILABLE", "No se pudo iniciar el pago con la pasarela configurada.")
                }
                synchronized(lock) {
                    val current = paymentIntents[prepared.payment.paymentId]
                        ?: return@synchronized PaymentCreateResult.Rejected("PAYMENT_RECORD_MISSING", "The payment record is incomplete.")
                    val now = demoTimestamp()
                    val updated = current.copy(
                        status = checkout.status,
                        statusReasonCode = checkout.reasonCode,
                        providerPreferenceId = checkout.checkoutReference,
                        nextActionUrl = checkout.nextActionUrl,
                        updatedAt = now,
                        events = current.events + PaymentEventRecord(
                            eventType = "CHECKOUT_CREATED",
                            statusBefore = current.status,
                            statusAfter = checkout.status,
                            occurredAt = now,
                            reasonCode = checkout.reasonCode
                        )
                    )
                    paymentIntents[updated.paymentId] = updated
                    updateInstallmentStatus(updated.purchaseId, updated.installmentId, "PAYMENT_PENDING")
                    persist()
                    PaymentCreateResult.Success(updated, replayed = false)
                }
            }
        }
    }

    fun getPayment(id: String, ownerUserId: String? = null): PaymentRecord? = synchronized(lock) {
        val payment = paymentIntents[id] ?: return@synchronized null
        if (ownerUserId != null && purchases[payment.purchaseId]?.ownerUserId != ownerUserId) return@synchronized null
        payment
    }

    fun applyDemoPaymentEvent(
        key: String,
        paymentId: String,
        request: DemoPaymentEventRequest,
        ownerUserId: String? = null
    ): PaymentEventResult = synchronized(lock) {
        if (paymentProvider.isReal || environment == LEDGER_ENV_PRODUCTION) {
            return@synchronized PaymentEventResult.Rejected("DEMO_EVENTS_DISABLED", "Demo payment events are disabled in this environment.")
        }
        val fingerprint = json.encodeToString(PaymentEventFingerprint(paymentId, request))
        paymentEventIdempotencyFingerprints[key]?.let { existingFingerprint ->
            if (existingFingerprint == fingerprint) {
                val existing = paymentIntents[paymentId]
                return when {
                    existing == null -> PaymentEventResult.Rejected("PAYMENT_NOT_FOUND", "The demo payment was not found.")
                    ownerUserId != null && purchases[existing.purchaseId]?.ownerUserId != ownerUserId ->
                        PaymentEventResult.Rejected("PAYMENT_NOT_FOUND", "The demo payment was not found.")
                    else -> PaymentEventResult.Success(existing)
                }
            }
            return PaymentEventResult.Rejected("IDEMPOTENCY_KEY_REUSED", "This idempotency key was already used for a different demo event.")
        }
        val payment = paymentIntents[paymentId]
            ?.takeIf { ownerUserId == null || purchases[it.purchaseId]?.ownerUserId == ownerUserId }
            ?: return PaymentEventResult.Rejected("PAYMENT_NOT_FOUND", "The demo payment was not found.")
        val eventType = request.eventType.uppercase()
        val transition = paymentTransition(payment, eventType)
            ?: return PaymentEventResult.Rejected("PAYMENT_TRANSITION_INVALID", "The event is not valid for the current demo payment state.")
        val now = demoTimestamp()
        recordDemoLedgerTransition(payment, transition, now)
        val updated = payment.copy(
            status = transition.status,
            reconciliationStatus = transition.reconciliationStatus,
            providerReference = transition.providerReference ?: payment.providerReference,
            providerAmountCents = transition.providerAmountCents ?: payment.providerAmountCents,
            updatedAt = now,
            statusReasonCode = transition.reasonCode,
            events = payment.events + PaymentEventRecord(
                eventType = eventType,
                statusBefore = payment.status,
                statusAfter = transition.status,
                occurredAt = now,
                reasonCode = transition.reasonCode,
                providerReference = transition.providerReference,
                providerAmountCents = transition.providerAmountCents
            )
        )
        paymentIntents[paymentId] = updated
        paymentEventIdempotencyFingerprints[key] = fingerprint
        val installmentStatus = when (transition.status) {
            "PROCESSING", "PENDING" -> "PAYMENT_PENDING"
            "SUCCEEDED" -> if (transition.reconciliationStatus in setOf("MATCHED", "MANUALLY_RESOLVED")) "DEMO_PAID" else "DEMO_PAYMENT_REVIEW"
            "FAILED" -> "PROGRAMMED"
            "REVERSED" -> "DEMO_REVERSED"
            "REFUNDED" -> "DEMO_REFUNDED"
            else -> "PROGRAMMED"
        }
        updateInstallmentStatus(payment.purchaseId, payment.installmentId, installmentStatus)
        persist()
        PaymentEventResult.Success(updated)
    }

    fun getReconciliationQueue(): List<PaymentIntentResponse> = synchronized(lock) {
        paymentIntents.values
            .filter { it.reconciliationStatus in setOf("AMOUNT_MISMATCH", "REFERENCE_MISMATCH", "UNDER_REVIEW") }
            .map { it.toResponse(environment == LEDGER_ENV_DEMO) }
    }

    override fun settle(update: ProviderSettlement): SettlementResult = synchronized(lock) {
        val payment = paymentIntents[update.internalPaymentId] ?: return SettlementResult.NotFound

        if (update.status == ProviderPaymentStatus.APPROVED &&
            payment.status == "SUCCEEDED" &&
            payment.reconciliationStatus == "MATCHED" &&
            payment.providerReference != null &&
            payment.providerReference != update.providerPaymentId
        ) {
            return SettlementResult.Review("DUPLICATE_PROVIDER_PAYMENT")
        }

        val transitionAllowed = when (payment.status) {
            "REFUNDED", "REVERSED" -> false
            "SUCCEEDED" -> update.status == ProviderPaymentStatus.REFUNDED || update.status == ProviderPaymentStatus.REVERSED
            else -> true
        }
        if (!transitionAllowed) return SettlementResult.Duplicate(payment.status)

        val resultingStatus = targetStatus(update.status)
        if (payment.providerReference == update.providerPaymentId && payment.status == resultingStatus) {
            return SettlementResult.Duplicate(payment.status)
        }

        var reconciliation = payment.reconciliationStatus
        var reasonCode = update.reasonCode
        var providerAmount = payment.providerAmountCents

        val installmentStatus = when (update.status) {
            ProviderPaymentStatus.APPROVED -> {
                val amountMatches = update.amountCents != null && update.amountCents == payment.amountCents
                val currencyMatches = update.currency?.equals(CURRENCY_PEN, ignoreCase = true) == true
                if (amountMatches && currencyMatches) {
                    accounting.append(
                        LedgerTransactions.installmentPaid(
                            environment = environment,
                            internalPaymentId = payment.paymentId,
                            provider = update.provider,
                            providerPaymentId = update.providerPaymentId,
                            purchaseId = payment.purchaseId,
                            installmentId = payment.installmentId,
                            amountCents = payment.amountCents,
                            occurredAt = update.occurredAt
                        )
                    )
                    reconciliation = "MATCHED"
                    providerAmount = update.amountCents
                    paidStatus()
                } else {
                    reconciliation = "AMOUNT_MISMATCH"
                    reasonCode = if (!currencyMatches) "PROVIDER_CURRENCY_MISMATCH" else "PROVIDER_AMOUNT_MISMATCH"
                    providerAmount = update.amountCents
                    reviewStatus()
                }
            }
            ProviderPaymentStatus.PENDING, ProviderPaymentStatus.PROCESSING -> "PAYMENT_PENDING"
            ProviderPaymentStatus.REJECTED -> {
                reconciliation = "NOT_RECONCILED"
                "PROGRAMMED"
            }
            ProviderPaymentStatus.REFUNDED, ProviderPaymentStatus.REVERSED -> {
                if (payment.status == "SUCCEEDED" && payment.reconciliationStatus == "MATCHED") {
                    val amountToReverse = payment.providerAmountCents ?: payment.amountCents
                    accounting.append(
                        LedgerTransactions.installmentRefunded(
                            environment = environment,
                            internalPaymentId = payment.paymentId,
                            provider = update.provider,
                            providerPaymentId = payment.providerReference ?: update.providerPaymentId,
                            purchaseId = payment.purchaseId,
                            installmentId = payment.installmentId,
                            amountCents = amountToReverse,
                            occurredAt = update.occurredAt,
                            reason = if (update.status == ProviderPaymentStatus.REVERSED) "CHARGEBACK" else "REFUND"
                        )
                    )
                    reconciliation = "MATCHED"
                } else {
                    reconciliation = "UNDER_REVIEW"
                }
                refundedStatus(reversed = update.status == ProviderPaymentStatus.REVERSED)
            }
        }

        val now = demoTimestamp()
        val updated = payment.copy(
            status = resultingStatus,
            reconciliationStatus = reconciliation,
            providerReference = update.providerPaymentId,
            providerAmountCents = providerAmount,
            updatedAt = now,
            statusReasonCode = reasonCode,
            events = payment.events + PaymentEventRecord(
                eventType = "PROVIDER_${update.status.name}",
                statusBefore = payment.status,
                statusAfter = resultingStatus,
                occurredAt = now,
                reasonCode = reasonCode,
                providerReference = update.providerPaymentId,
                providerAmountCents = update.amountCents
            )
        )
        paymentIntents[payment.paymentId] = updated
        updateInstallmentStatus(payment.purchaseId, payment.installmentId, installmentStatus)
        persist()
        SettlementResult.Applied(updated.status, updated.reconciliationStatus)
    }

    private fun recordPurchaseLedger(purchase: PurchaseResponse, quote: QuoteResponse) {
        accounting.append(
            LedgerTransactions.purchase(
                environment = environment,
                purchaseId = purchase.purchaseId,
                occurredAt = demoTimestamp(),
                purchaseAmountCents = quote.purchaseAmountCents,
                buyerDownPaymentCents = quote.buyerDownPaymentCents,
                buyerInstallmentsTotalCents = quote.buyerTotalAfterDownPaymentCents,
                buyerFeeCents = quote.buyerFeeCents,
                supplierImmediateCents = quote.supplierImmediatePayoutCents,
                supplierDeferredNetCents = quote.supplierDeferredPayoutCents - quote.supplierFeeCents,
                supplierFeeCents = quote.supplierFeeCents,
                supplierId = quote.supplier.supplierId
            )
        )
    }

    private fun recordDemoLedgerTransition(payment: PaymentRecord, transition: PaymentTransition, occurredAt: String) {
        when {
            transition.status == "SUCCEEDED" && transition.reconciliationStatus == "MATCHED" -> {
                accounting.append(
                    LedgerTransactions.installmentPaid(
                        environment = environment,
                        internalPaymentId = payment.paymentId,
                        provider = "demo",
                        providerPaymentId = payment.paymentId,
                        purchaseId = payment.purchaseId,
                        installmentId = payment.installmentId,
                        amountCents = payment.amountCents,
                        occurredAt = occurredAt
                    )
                )
            }
            transition.status == "SUCCEEDED" && transition.reconciliationStatus == "MANUALLY_RESOLVED" -> {
                accounting.append(
                    LedgerTransactions.installmentReconciled(
                        environment = environment,
                        internalPaymentId = payment.paymentId,
                        provider = "demo",
                        providerPaymentId = payment.paymentId,
                        purchaseId = payment.purchaseId,
                        installmentId = payment.installmentId,
                        expectedAmountCents = payment.amountCents,
                        providerAmountCents = payment.providerAmountCents ?: payment.amountCents,
                        occurredAt = occurredAt
                    )
                )
            }
            transition.status in setOf("REFUNDED", "REVERSED") &&
                payment.status == "SUCCEEDED" &&
                payment.reconciliationStatus == "MATCHED" -> {
                accounting.append(
                    LedgerTransactions.installmentRefunded(
                        environment = environment,
                        internalPaymentId = payment.paymentId,
                        provider = "demo",
                        providerPaymentId = payment.paymentId,
                        purchaseId = payment.purchaseId,
                        installmentId = payment.installmentId,
                        amountCents = payment.providerAmountCents ?: payment.amountCents,
                        occurredAt = occurredAt,
                        reason = if (transition.status == "REVERSED") "CHARGEBACK" else "REFUND"
                    )
                )
            }
        }
    }

    private fun targetStatus(status: ProviderPaymentStatus): String = when (status) {
        ProviderPaymentStatus.APPROVED -> "SUCCEEDED"
        ProviderPaymentStatus.PENDING, ProviderPaymentStatus.PROCESSING -> "PROCESSING"
        ProviderPaymentStatus.REJECTED -> "FAILED"
        ProviderPaymentStatus.REFUNDED -> "REFUNDED"
        ProviderPaymentStatus.REVERSED -> "REVERSED"
    }

    private fun paidStatus(): String = if (environment == LEDGER_ENV_DEMO) "DEMO_PAID" else "PAID"

    private fun reviewStatus(): String = if (environment == LEDGER_ENV_DEMO) "DEMO_PAYMENT_REVIEW" else "PAYMENT_REVIEW"

    private fun refundedStatus(reversed: Boolean): String = when {
        environment == LEDGER_ENV_DEMO && reversed -> "DEMO_REVERSED"
        environment == LEDGER_ENV_DEMO -> "DEMO_REFUNDED"
        reversed -> "REVERSED"
        else -> "REFUNDED"
    }

    private fun updateInstallmentStatus(purchaseId: String, installmentId: String, status: String) {
        val record = purchases[purchaseId] ?: return
        purchases[purchaseId] = record.copy(
            installments = record.installments.map { if (it.installmentId == installmentId) it.copy(status = status) else it }
        )
    }

    private fun persist() {
        val snapshot = PersistedDemoStore(
            decisions = demoDecisions.values.toList(),
            quotes = quotes.values.toList(),
            purchases = purchases.values.toList(),
            idempotencyKeys = idempotencyKeys.toMap(),
            idempotencyFingerprints = idempotencyFingerprints.toMap(),
            paymentIntents = paymentIntents.values.toList(),
            paymentIdempotencyKeys = paymentIdempotencyKeys.toMap(),
            paymentIdempotencyFingerprints = paymentIdempotencyFingerprints.toMap(),
            paymentEventIdempotencyFingerprints = paymentEventIdempotencyFingerprints.toMap()
        )
        val temporary = file.resolveSibling("${file.fileName}.tmp")
        java.nio.file.Files.writeString(temporary, json.encodeToString(snapshot))
        runCatching {
            java.nio.file.Files.move(
                temporary,
                file,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE
            )
        }.getOrElse {
            java.nio.file.Files.move(temporary, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        }
    }
}

private fun percentageOf(amountCents: Long, basisPoints: Int): Long =
    BigInteger.valueOf(amountCents)
        .multiply(BigInteger.valueOf(basisPoints.toLong()))
        .divide(BigInteger.valueOf(10_000L))
        .longValueExact()

private fun formatPenFromCents(amountCents: Long): String =
    "S/ %,.2f".format(java.util.Locale.US, amountCents / 100.0)

internal const val DEMO_ELIGIBILITY_POLICY_VERSION = "DEMO_POLICY_2026_V2"

@Serializable
internal data class EligibilityDecisionRecord(
    @SerialName("decision_id") val decisionId: String,
    @SerialName("policy_version") val policyVersion: String = DEMO_ELIGIBILITY_POLICY_VERSION,
    @SerialName("risk_score") val riskScore: Int = 620,
    @SerialName("risk_tier") val riskTier: String = "ENTRY_30",
    @SerialName("down_payment_basis_points") val downPaymentBasisPoints: Int = 3_000,
    @SerialName("completed_installments_count") val completedInstallmentsCount: Int = 0
)

@Serializable
internal data class SupplierSettlementItem(
    @SerialName("purchase_id") val purchaseId: String,
    @SerialName("supplier_id") val supplierId: String,
    @SerialName("supplier_name") val supplierName: String,
    @SerialName("purchase_amount_cents") val purchaseAmountCents: Long,
    @SerialName("immediate_payout_cents") val immediatePayoutCents: Long,
    @SerialName("deferred_gross_cents") val deferredGrossCents: Long,
    @SerialName("supplier_fee_cents") val supplierFeeCents: Long,
    @SerialName("deferred_net_cents") val deferredNetCents: Long,
    @SerialName("paid_installments_count") val paidInstallmentsCount: Int,
    @SerialName("total_installments_count") val totalInstallmentsCount: Int,
    @SerialName("settlement_status") val settlementStatus: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("demo_only") val demoOnly: Boolean = true
)

@Serializable
private data class PersistedDemoStore(
    val decisions: List<EligibilityDecisionRecord> = emptyList(),
    val quotes: List<QuoteResponse> = emptyList(),
    val purchases: List<PurchaseRecord> = emptyList(),
    val idempotencyKeys: Map<String, String> = emptyMap(),
    val idempotencyFingerprints: Map<String, String> = emptyMap(),
    val paymentIntents: List<PaymentRecord> = emptyList(),
    val paymentIdempotencyKeys: Map<String, String> = emptyMap(),
    val paymentIdempotencyFingerprints: Map<String, String> = emptyMap(),
    val paymentEventIdempotencyFingerprints: Map<String, String> = emptyMap()
)

internal sealed interface PurchaseCreateResult {
    data class Success(val purchase: PurchaseResponse, val replayed: Boolean) : PurchaseCreateResult
    data class Rejected(val code: String, val message: String) : PurchaseCreateResult
}

@Serializable
private data class PurchaseRecord(
    val purchase: PurchaseResponse,
    val quoteId: String = "",
    val installments: List<InstallmentDto> = emptyList(),
    val ownerUserId: String? = null
)

internal sealed interface PaymentCreateResult {
    data class Success(val payment: PaymentRecord, val replayed: Boolean) : PaymentCreateResult
    data class Rejected(val code: String, val message: String) : PaymentCreateResult
}

internal sealed interface PaymentEventResult {
    data class Success(val payment: PaymentRecord) : PaymentEventResult
    data class Rejected(val code: String, val message: String) : PaymentEventResult
}

private sealed interface PaymentIntentPreparation {
    data class Ready(val payment: PaymentRecord, val description: String) : PaymentIntentPreparation
    data class Replayed(val payment: PaymentRecord) : PaymentIntentPreparation
    data class Rejected(val code: String, val message: String) : PaymentIntentPreparation
}

@Serializable
internal data class PaymentRecord(
    @SerialName("payment_id") val paymentId: String,
    @SerialName("purchase_id") val purchaseId: String,
    @SerialName("installment_id") val installmentId: String,
    @SerialName("amount_cents") val amountCents: Long,
    val status: String,
    @SerialName("reconciliation_status") val reconciliationStatus: String,
    @SerialName("provider_reference") val providerReference: String? = null,
    @SerialName("provider_amount_cents") val providerAmountCents: Long? = null,
    @SerialName("provider_preference_id") val providerPreferenceId: String? = null,
    @SerialName("next_action_url") val nextActionUrl: String? = null,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("status_reason_code") val statusReasonCode: String,
    val events: List<PaymentEventRecord> = emptyList()
) {
    fun toResponse(demoOnly: Boolean = true) = PaymentIntentResponse(
        paymentId = paymentId,
        installmentId = installmentId,
        amountCents = amountCents,
        status = status,
        nextActionUrl = nextActionUrl,
        updatedAt = updatedAt,
        statusReasonCode = statusReasonCode,
        providerReference = providerReference,
        providerAmountCents = providerAmountCents,
        reconciliationStatus = reconciliationStatus,
        demoOnly = demoOnly,
        events = events
    )
}

@Serializable
internal data class PaymentEventRecord(
    @SerialName("event_type") val eventType: String,
    @SerialName("status_before") val statusBefore: String,
    @SerialName("status_after") val statusAfter: String,
    @SerialName("occurred_at") val occurredAt: String,
    @SerialName("reason_code") val reasonCode: String,
    @SerialName("provider_reference") val providerReference: String? = null,
    @SerialName("provider_amount_cents") val providerAmountCents: Long? = null
)

private data class PaymentTransition(
    val status: String,
    val reconciliationStatus: String,
    val reasonCode: String,
    val providerReference: String? = null,
    val providerAmountCents: Long? = null
)

private fun paymentTransition(payment: PaymentRecord, eventType: String): PaymentTransition? {
    val providerReference = "demo-provider-${payment.paymentId.substringAfterLast('-')}"
    return when (eventType) {
        "PROCESSING" -> if (payment.status == "PENDING") PaymentTransition("PROCESSING", payment.reconciliationStatus, "DEMO_PROCESSING") else null
        "SUCCEED" -> if (payment.status in setOf("PENDING", "PROCESSING", "ACTION_REQUIRED")) {
            PaymentTransition("SUCCEEDED", "MATCHED", "DEMO_SETTLED_AND_MATCHED", providerReference, payment.amountCents)
        } else null
        "SUCCEED_AMOUNT_MISMATCH" -> if (payment.status in setOf("PENDING", "PROCESSING", "ACTION_REQUIRED")) {
            val mismatchedAmount = if (payment.amountCents < Long.MAX_VALUE) payment.amountCents + 1L else payment.amountCents - 1L
            PaymentTransition("SUCCEEDED", "AMOUNT_MISMATCH", "DEMO_PROVIDER_AMOUNT_MISMATCH", providerReference, mismatchedAmount)
        } else null
        "SUCCEED_REFERENCE_MISMATCH" -> if (payment.status in setOf("PENDING", "PROCESSING", "ACTION_REQUIRED")) {
            PaymentTransition("SUCCEEDED", "REFERENCE_MISMATCH", "DEMO_PROVIDER_REFERENCE_MISMATCH", "$providerReference-wrong", payment.amountCents)
        } else null
        "FAIL" -> if (payment.status in setOf("PENDING", "PROCESSING", "ACTION_REQUIRED")) {
            PaymentTransition("FAILED", "NOT_RECONCILED", "DEMO_PAYMENT_FAILED")
        } else null
        "REVERSE" -> if (payment.status == "SUCCEEDED") {
            PaymentTransition("REVERSED", "UNDER_REVIEW", "DEMO_PAYMENT_REVERSED", payment.providerReference, payment.providerAmountCents)
        } else null
        "REFUND" -> if (payment.status == "SUCCEEDED") {
            PaymentTransition("REFUNDED", if (payment.reconciliationStatus == "MATCHED") "MATCHED" else "UNDER_REVIEW", "DEMO_PAYMENT_REFUNDED", payment.providerReference, payment.providerAmountCents)
        } else null
        "RECONCILE_MATCH" -> if (payment.status in setOf("SUCCEEDED", "REVERSED", "REFUNDED") && payment.reconciliationStatus !in setOf("MATCHED", "MANUALLY_RESOLVED")) {
            PaymentTransition(payment.status, "MANUALLY_RESOLVED", "DEMO_MANUALLY_RECONCILED")
        } else null
        else -> null
    }
}

private fun demoTimestamp(): String = java.time.Instant.now().toString()

@Serializable
private data class PaymentIntentFingerprint(
    val installmentId: String,
    val request: CreatePaymentIntentRequest
)

@Serializable
private data class PaymentEventFingerprint(
    val paymentId: String,
    val request: DemoPaymentEventRequest
)

@Serializable
internal data class PurchaseHistoryItem(
    @SerialName("purchase_id") val purchaseId: String,
    val status: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("supplier_name") val supplierName: String,
    @SerialName("purchase_amount_cents") val purchaseAmountCents: Long,
    @SerialName("buyer_down_payment_cents") val buyerDownPaymentCents: Long,
    @SerialName("buyer_total_after_down_payment_cents") val buyerTotalAfterDownPaymentCents: Long,
    @SerialName("supplier_immediate_payout_cents") val supplierImmediatePayoutCents: Long,
    @SerialName("supplier_deferred_payout_cents") val supplierDeferredPayoutCents: Long,
    @SerialName("kouta_initial_contribution_cents") val koutaInitialContributionCents: Long,
    @SerialName("supplier_fee_cents") val supplierFeeCents: Long,
    val installments: List<InstallmentDto>,
    val payments: List<PaymentIntentResponse> = emptyList()
)

@Serializable
internal data class EligibilityRequest(
    @SerialName("business_id") val businessId: String,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("term_days") val termDays: Int,
    @SerialName("consent_version") val consentVersion: String,
    @SerialName("simulated_paid_installments") val simulatedPaidInstallments: Int? = null
)

@Serializable
internal data class EligibilityResponse(
    @SerialName("decision_id") val decisionId: String,
    val status: String,
    @SerialName("approved_amount_cents") val approvedAmountCents: Long,
    @SerialName("down_payment_basis_points") val downPaymentBasisPoints: Int,
    @SerialName("policy_version") val policyVersion: String = DEMO_ELIGIBILITY_POLICY_VERSION,
    @SerialName("risk_score") val riskScore: Int = 620,
    @SerialName("risk_tier") val riskTier: String = "ENTRY_30",
    @SerialName("completed_installments_count") val completedInstallmentsCount: Int = 0,
    @SerialName("expires_at") val expiresAt: String
)

@Serializable
internal data class QuoteRequest(
    @SerialName("business_id") val businessId: String,
    @SerialName("supplier_qr_payload") val supplierQrPayload: String,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("term_days") val termDays: Int,
    @SerialName("eligibility_decision_id") val eligibilityDecisionId: String
)

@Serializable
internal data class QuoteResponse(
    @SerialName("quote_id") val quoteId: String,
    @SerialName("quote_version") val quoteVersion: String,
    @SerialName("policy_version") val policyVersion: String = DEMO_ELIGIBILITY_POLICY_VERSION,
    @SerialName("down_payment_basis_points") val downPaymentBasisPoints: Int = 3_000,
    @SerialName("expires_at") val expiresAt: String,
    val supplier: SupplierSummary,
    @SerialName("purchase_amount_cents") val purchaseAmountCents: Long,
    @SerialName("buyer_down_payment_cents") val buyerDownPaymentCents: Long,
    @SerialName("buyer_fee_cents") val buyerFeeCents: Long,
    @SerialName("buyer_total_after_down_payment_cents") val buyerTotalAfterDownPaymentCents: Long,
    @SerialName("supplier_immediate_payout_cents") val supplierImmediatePayoutCents: Long,
    @SerialName("supplier_deferred_payout_cents") val supplierDeferredPayoutCents: Long,
    @SerialName("kouta_initial_contribution_cents") val koutaInitialContributionCents: Long,
    @SerialName("supplier_fee_cents") val supplierFeeCents: Long,
    val installments: List<InstallmentDto>
)

@Serializable
internal data class SupplierSummary(
    @SerialName("supplier_id") val supplierId: String,
    @SerialName("display_name") val displayName: String
)

@Serializable
internal data class InstallmentDto(
    @SerialName("installment_id") val installmentId: String,
    @SerialName("sequence_number") val sequenceNumber: Int,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("due_at") val dueAt: String,
    val status: String
)

@Serializable
internal data class CreatePurchaseRequest(
    @SerialName("quote_id") val quoteId: String,
    @SerialName("quote_version") val quoteVersion: String,
    @SerialName("accepted_terms_version") val acceptedTermsVersion: String,
    @SerialName("accepted_privacy_notice_version") val acceptedPrivacyNoticeVersion: String
)

internal const val DEMO_NOT_ACCEPTED_VERSION = "DEMO_ONLY_NOT_ACCEPTED"

@Serializable
internal data class PurchaseResponse(
    @SerialName("purchase_id") val purchaseId: String,
    val status: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("status_reason_code") val statusReasonCode: String
)

@Serializable
private data class InstallmentsResponse(
    @SerialName("purchase_id") val purchaseId: String,
    val installments: List<InstallmentDto>
)

@Serializable
internal data class CreatePaymentIntentRequest(
    @SerialName("payment_method_id") val paymentMethodId: String
)

@Serializable
internal data class DemoPaymentEventRequest(
    @SerialName("event_type") val eventType: String
)

@Serializable
internal data class PaymentIntentResponse(
    @SerialName("payment_id") val paymentId: String,
    @SerialName("installment_id") val installmentId: String,
    @SerialName("amount_cents") val amountCents: Long,
    val status: String,
    @SerialName("next_action_url") val nextActionUrl: String? = null,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("status_reason_code") val statusReasonCode: String? = null,
    @SerialName("provider_reference") val providerReference: String? = null,
    @SerialName("provider_amount_cents") val providerAmountCents: Long? = null,
    @SerialName("reconciliation_status") val reconciliationStatus: String = "NOT_RECONCILED",
    @SerialName("demo_only") val demoOnly: Boolean = true,
    val events: List<PaymentEventRecord> = emptyList()
)

internal const val DEMO_PAYMENT_METHOD = "DEMO_MANUAL"

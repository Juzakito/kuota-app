package com.kuota.backend

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import jakarta.mail.AuthenticationFailedException
import jakarta.mail.Message
import jakarta.mail.MessagingException
import jakarta.mail.PasswordAuthentication
import jakarta.mail.SendFailedException
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Base64
import java.util.Properties
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

const val CLAIM_RESPONSE_BUSINESS_DAYS = 15
private const val MAX_DELIVERY_ATTEMPTS = 12

private val claimTypes = setOf("RECLAMO", "QUEJA")
private val goodTypes = setOf("PRODUCTO", "SERVICIO")
private val claimStatuses = setOf("RECIBIDO", "EN_ATENCION", "RESPONDIDO")
private val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

@Serializable
data class ClaimSubmissionRequest(
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
data class ClaimDelivery(
    val channel: String,
    val status: String,
    val attempts: Int = 0,
    @SerialName("next_attempt_epoch_ms") val nextAttemptEpochMs: Long = 0,
    @SerialName("last_error") val lastError: String? = null,
    @SerialName("external_id") val externalId: String? = null,
    @SerialName("delivered_at") val deliveredAt: String? = null
)

@Serializable
data class ClaimRecord(
    @SerialName("claim_number") val claimNumber: String,
    @SerialName("user_id") val userId: String,
    val fingerprint: String,
    val submission: ClaimSubmissionRequest,
    val status: String,
    @SerialName("received_at") val receivedAt: String,
    @SerialName("response_due_date") val responseDueDate: String,
    @SerialName("response_text") val responseText: String? = null,
    @SerialName("responded_at") val respondedAt: String? = null,
    val deliveries: List<ClaimDelivery> = emptyList()
)

@Serializable
data class ClaimChannelStatus(val channel: String, val status: String)

@Serializable
data class ClaimReceiptResponse(
    @SerialName("claim_number") val claimNumber: String,
    @SerialName("client_claim_id") val clientClaimId: String,
    val type: String,
    val status: String,
    @SerialName("received_at") val receivedAt: String,
    @SerialName("response_due_date") val responseDueDate: String,
    @SerialName("response_business_days") val responseBusinessDays: Int = CLAIM_RESPONSE_BUSINESS_DAYS,
    val channels: List<ClaimChannelStatus>,
    @SerialName("response_text") val responseText: String? = null,
    @SerialName("responded_at") val respondedAt: String? = null
)

@Serializable
data class ClaimListResponse(val claims: List<ClaimReceiptResponse>)

@Serializable
data class OperatorClaimSummary(
    @SerialName("claim_number") val claimNumber: String,
    val type: String,
    val status: String,
    @SerialName("help_topic") val helpTopic: String?,
    @SerialName("consumer_name") val consumerName: String,
    @SerialName("document_masked") val documentMasked: String,
    @SerialName("email_masked") val emailMasked: String,
    @SerialName("received_at") val receivedAt: String,
    @SerialName("response_due_date") val responseDueDate: String,
    @SerialName("business_days_left") val businessDaysLeft: Int,
    val channels: List<ClaimDelivery>
)

@Serializable
data class OperatorClaimStatusRequest(
    val status: String,
    @SerialName("response_text") val responseText: String? = null
)

@Serializable
private data class PersistedClaims(
    @SerialName("sequence_by_year") val sequenceByYear: Map<String, Int> = emptyMap(),
    val claims: List<ClaimRecord> = emptyList()
)

sealed interface ClaimSubmitResult {
    data class Created(val record: ClaimRecord, val replayed: Boolean) : ClaimSubmitResult
    data class Rejected(val status: HttpStatusCode, val code: String, val message: String) : ClaimSubmitResult
}

fun ClaimRecord.toReceipt() = ClaimReceiptResponse(
    claimNumber = claimNumber,
    clientClaimId = submission.clientClaimId,
    type = submission.type,
    status = status,
    receivedAt = receivedAt,
    responseDueDate = responseDueDate,
    channels = deliveries.map { ClaimChannelStatus(it.channel, it.status) },
    responseText = responseText,
    respondedAt = respondedAt
)

class ClaimStore(private val file: Path, private val channelNames: () -> List<String>) {
    private val lock = Any()
    private val sequenceByYear = linkedMapOf<String, Int>()
    private val claims = linkedMapOf<String, ClaimRecord>()

    init {
        readJsonFile(file, PersistedClaims.serializer())?.let { persisted ->
            sequenceByYear.putAll(persisted.sequenceByYear)
            persisted.claims.forEach { claims[it.claimNumber] = it }
        }
    }

    fun submit(userId: String, request: ClaimSubmissionRequest): ClaimSubmitResult = synchronized(lock) {
        validate(request)?.let { return it }
        val fingerprint = sha256(storeJson.encodeToString(ClaimSubmissionRequest.serializer(), request.copy(createdAtDevice = null)))
        claims.values.firstOrNull { it.submission.clientClaimId == request.clientClaimId }?.let { existing ->
            return if (existing.userId == userId && existing.fingerprint == fingerprint) {
                ClaimSubmitResult.Created(existing, replayed = true)
            } else {
                ClaimSubmitResult.Rejected(HttpStatusCode.Conflict, "IDEMPOTENCY_KEY_REUSED", "Este identificador ya se usó para otra hoja de reclamación.")
            }
        }

        val now = ZonedDateTime.now(LIMA_ZONE)
        val year = now.year.toString()
        val sequence = (sequenceByYear[year] ?: 0) + 1
        sequenceByYear[year] = sequence
        val claimNumber = "LR-$year-${sequence.toString().padStart(6, '0')}"
        val record = ClaimRecord(
            claimNumber = claimNumber,
            userId = userId,
            fingerprint = fingerprint,
            submission = request.copy(
                consumerName = request.consumerName.trim(),
                email = request.email.trim(),
                detail = request.detail.trim(),
                consumerRequest = request.consumerRequest.trim()
            ),
            status = "RECIBIDO",
            receivedAt = now.toOffsetDateTime().toString(),
            responseDueDate = PeruBusinessCalendar.addBusinessDays(now.toLocalDate(), CLAIM_RESPONSE_BUSINESS_DAYS).toString(),
            deliveries = channelNames().map { ClaimDelivery(channel = it, status = "PENDIENTE") }
        )
        claims[claimNumber] = record
        persist()
        ClaimSubmitResult.Created(record, replayed = false)
    }

    fun listForUser(userId: String): List<ClaimRecord> = synchronized(lock) {
        claims.values.filter { it.userId == userId }.sortedByDescending { it.receivedAt }
    }

    fun find(claimNumber: String): ClaimRecord? = synchronized(lock) { claims[claimNumber] }

    fun listAll(): List<ClaimRecord> = synchronized(lock) { claims.values.sortedBy { it.responseDueDate } }

    fun updateStatus(claimNumber: String, status: String, responseText: String?): ClaimRecord? = synchronized(lock) {
        val current = claims[claimNumber] ?: return null
        val updated = current.copy(
            status = status,
            responseText = responseText?.trim()?.takeIf(String::isNotEmpty) ?: current.responseText,
            respondedAt = if (status == "RESPONDIDO") Instant.now().toString() else current.respondedAt
        )
        claims[claimNumber] = updated
        persist()
        updated
    }

    fun dueDeliveries(nowEpochMs: Long): List<Pair<ClaimRecord, ClaimDelivery>> = synchronized(lock) {
        claims.values.flatMap { record ->
            record.deliveries
                .filter { it.status == "PENDIENTE" && it.nextAttemptEpochMs <= nowEpochMs }
                .map { record to it }
        }
    }

    fun updateDelivery(claimNumber: String, channel: String, transform: (ClaimDelivery) -> ClaimDelivery) = synchronized(lock) {
        val record = claims[claimNumber] ?: return@synchronized
        claims[claimNumber] = record.copy(deliveries = record.deliveries.map { if (it.channel == channel) transform(it) else it })
        persist()
    }

    private fun validate(request: ClaimSubmissionRequest): ClaimSubmitResult.Rejected? {
        fun reject(message: String) = ClaimSubmitResult.Rejected(HttpStatusCode.UnprocessableEntity, "CLAIM_INVALID", message)
        val documentDigits = request.documentNumber.trim()
        return when {
            request.clientClaimId.length !in 8..80 -> reject("Identificador de hoja inválido.")
            request.type !in claimTypes -> reject("Elige si es un reclamo o una queja.")
            request.goodType !in goodTypes -> reject("Indica si se trata de un producto o un servicio.")
            request.consumerName.trim().length !in 3..120 -> reject("Ingresa tu nombre completo.")
            !validDocument(request.documentType, documentDigits) -> reject("Revisa el tipo y número de documento.")
            request.address.trim().length !in 5..200 -> reject("Ingresa tu domicilio.")
            request.phone.filter(Char::isDigit).length !in 9..15 -> reject("Ingresa un teléfono válido.")
            !request.email.trim().matches(emailRegex) -> reject("Ingresa un correo válido para recibir la copia y la respuesta.")
            request.goodDescription.trim().length !in 3..200 -> reject("Describe el producto o servicio.")
            (request.amountClaimedCents ?: 0) < 0 -> reject("El monto reclamado no puede ser negativo.")
            request.detail.trim().length !in 10..2000 -> reject("Describe lo ocurrido (mínimo 10 caracteres).")
            request.consumerRequest.trim().length !in 5..1000 -> reject("Indica qué solicitas a Kuota.")
            else -> null
        }
    }

    private fun validDocument(type: String, number: String): Boolean = when (type) {
        "DNI" -> number.length == 8 && number.all(Char::isDigit)
        "RUC" -> number.length == 11 && number.all(Char::isDigit)
        "CE" -> number.length in 8..12 && number.all(Char::isLetterOrDigit)
        "PASAPORTE" -> number.length in 6..12 && number.all(Char::isLetterOrDigit)
        else -> false
    }

    private fun persist() {
        writeJsonFileAtomically(file, PersistedClaims.serializer(), PersistedClaims(sequenceByYear.toMap(), claims.values.toList()))
    }
}

sealed interface ForwardResult {
    data class Delivered(val externalId: String?) : ForwardResult
    data class Retryable(val code: String) : ForwardResult
    data class Permanent(val code: String) : ForwardResult
}

interface ClaimForwarder {
    val channel: String
    fun deliver(record: ClaimRecord): ForwardResult
}

private object ProviderIdentity {
    val legalName = env("KUOTA_LEGAL_NAME") ?: "Kuota Perú S.A.C. [DEMO]"
    val ruc = env("KUOTA_RUC") ?: "00000000000 [DEMO]"
    val address = env("KUOTA_LEGAL_ADDRESS") ?: "Lima, Perú [DEMO]"
}

private fun claimSubject(record: ClaimRecord): String {
    val label = if (record.submission.type == "QUEJA") "Queja" else "Reclamo"
    return "[Libro de Reclamaciones] $label ${record.claimNumber} · responder antes del ${record.responseDueDate}"
}

private fun formatPen(cents: Long): String = "S/ %d.%02d".format(cents / 100, cents % 100)

internal fun claimSheetText(record: ClaimRecord): String {
    val s = record.submission
    return buildString {
        appendLine("HOJA DE RECLAMACIÓN VIRTUAL N° ${record.claimNumber}")
        appendLine("Fecha de recepción: ${record.receivedAt} (America/Lima)")
        appendLine("Plazo de respuesta: $CLAIM_RESPONSE_BUSINESS_DAYS días hábiles · vence el ${record.responseDueDate}")
        appendLine()
        appendLine("1. PROVEEDOR")
        appendLine("Razón social: ${ProviderIdentity.legalName} · RUC: ${ProviderIdentity.ruc}")
        appendLine("Domicilio: ${ProviderIdentity.address}")
        appendLine()
        appendLine("2. CONSUMIDOR")
        appendLine("Nombre: ${s.consumerName}")
        appendLine("Documento: ${s.documentType} ${s.documentNumber}")
        appendLine("Domicilio: ${s.address}")
        appendLine("Teléfono: ${s.phone} · Correo: ${s.email}")
        appendLine()
        appendLine("3. BIEN CONTRATADO")
        appendLine("Tipo: ${s.goodType} · Descripción: ${s.goodDescription}")
        s.amountClaimedCents?.let { appendLine("Monto reclamado: ${formatPen(it)}") }
        s.operationReference?.takeIf(String::isNotBlank)?.let { appendLine("Operación relacionada: $it") }
        appendLine()
        appendLine("4. DETALLE (${s.type})")
        s.helpTopic?.let { appendLine("Tema: $it") }
        appendLine(s.detail)
        appendLine()
        appendLine("Pedido del consumidor:")
        appendLine(s.consumerRequest)
        appendLine()
        appendLine("RECLAMO: disconformidad relacionada con los productos o servicios.")
        appendLine("QUEJA: disconformidad no relacionada con los productos o servicios, o malestar respecto a la atención.")
        appendLine("La formulación del reclamo no impide acudir a otras vías de solución de controversias ni es requisito previo para interponer una denuncia ante el INDECOPI.")
    }
}

class LocalArchiveForwarder(private val directory: Path) : ClaimForwarder {
    override val channel = "ARCHIVO_LOCAL"
    override fun deliver(record: ClaimRecord): ForwardResult = try {
        Files.createDirectories(directory)
        val target = directory.resolve("${record.claimNumber}.txt")
        Files.writeString(target, claimSheetText(record))
        ForwardResult.Delivered(target.fileName.toString())
    } catch (_: IOException) {
        ForwardResult.Retryable("IO_ERROR")
    }
}

private val httpClient: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()

private fun postJson(url: String, authorization: String, body: String): ForwardResult = try {
    val response = httpClient.send(
        HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .header("Content-Type", "application/json")
            .header("Authorization", authorization)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build(),
        HttpResponse.BodyHandlers.ofString()
    )
    when (response.statusCode()) {
        in 200..299 -> ForwardResult.Delivered(extractTicketId(response.body()))
        429, in 500..599 -> ForwardResult.Retryable("HTTP_${response.statusCode()}")
        else -> ForwardResult.Permanent("HTTP_${response.statusCode()}")
    }
} catch (_: IOException) {
    ForwardResult.Retryable("NETWORK_ERROR")
} catch (_: InterruptedException) {
    Thread.currentThread().interrupt()
    ForwardResult.Retryable("INTERRUPTED")
}

private fun extractTicketId(body: String): String? = runCatching {
    val root = storeJson.parseToJsonElement(body).jsonObject
    (root["ticket"]?.jsonObject?.get("id") ?: root["id"])?.jsonPrimitive?.content
}.getOrNull()

private fun basicAuth(user: String, secret: String): String =
    "Basic " + Base64.getEncoder().encodeToString("$user:$secret".toByteArray(Charsets.UTF_8))

private fun dueAtEndOfDay(date: String): String =
    LocalDate.parse(date).atTime(18, 0).atZone(LIMA_ZONE).toInstant().toString()

class ZendeskForwarder(private val subdomain: String, private val email: String, private val apiToken: String) : ClaimForwarder {
    override val channel = "ZENDESK"
    override fun deliver(record: ClaimRecord): ForwardResult {
        val body = buildJsonObject {
            putJsonObject("ticket") {
                put("subject", claimSubject(record))
                put("type", "task")
                put("priority", if (record.submission.type == "RECLAMO") "high" else "normal")
                put("due_at", dueAtEndOfDay(record.responseDueDate))
                put("external_id", record.claimNumber)
                putJsonObject("comment") {
                    put("body", claimSheetText(record))
                    put("public", false)
                }
                putJsonObject("requester") {
                    put("name", record.submission.consumerName)
                    put("email", record.submission.email)
                }
                putJsonArray("tags") {
                    add("libro_reclamaciones")
                    add(record.submission.type.lowercase())
                    add("indecopi_15_dias_habiles")
                }
            }
        }
        return postJson("https://$subdomain.zendesk.com/api/v2/tickets.json", basicAuth("$email/token", apiToken), body.toString())
    }
}

class FreshdeskForwarder(private val domain: String, private val apiKey: String) : ClaimForwarder {
    override val channel = "FRESHDESK"
    override fun deliver(record: ClaimRecord): ForwardResult {
        val html = claimSheetText(record).lines().joinToString("<br>") { line ->
            line.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        }
        val received = LocalDate.parse(record.receivedAt.take(10))
        val firstResponse = PeruBusinessCalendar.addBusinessDays(received, 2).toString()
        val body = buildJsonObject {
            put("subject", claimSubject(record))
            put("description", html)
            put("name", record.submission.consumerName)
            put("email", record.submission.email)
            put("priority", if (record.submission.type == "RECLAMO") 3 else 2)
            put("status", 2)
            put("fr_due_by", dueAtEndOfDay(firstResponse))
            put("due_by", dueAtEndOfDay(record.responseDueDate))
            putJsonArray("tags") {
                add("libro_reclamaciones")
                add(record.submission.type.lowercase())
                add(record.claimNumber)
            }
        }
        return postJson("https://$domain.freshdesk.com/api/v2/tickets", basicAuth(apiKey, "X"), body.toString())
    }
}

class SmtpForwarder(
    override val channel: String,
    private val config: SmtpConfig,
    private val recipient: (ClaimRecord) -> String,
    private val subject: (ClaimRecord) -> String,
    private val body: (ClaimRecord) -> String
) : ClaimForwarder {
    override fun deliver(record: ClaimRecord): ForwardResult = try {
        val message = MimeMessage(config.session()).apply {
            setFrom(InternetAddress(config.from))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient(record)))
            setSubject(subject(record), "UTF-8")
            setText(body(record), "UTF-8")
            setHeader("X-Kuota-Claim", record.claimNumber)
        }
        Transport.send(message)
        ForwardResult.Delivered(message.messageID)
    } catch (_: AuthenticationFailedException) {
        ForwardResult.Permanent("SMTP_AUTH_FAILED")
    } catch (error: SendFailedException) {
        if (error.invalidAddresses?.isNotEmpty() == true) ForwardResult.Permanent("SMTP_INVALID_ADDRESS") else ForwardResult.Retryable("SMTP_SEND_FAILED")
    } catch (_: MessagingException) {
        ForwardResult.Retryable("SMTP_UNAVAILABLE")
    }
}

data class SmtpConfig(
    val host: String,
    val port: Int,
    val user: String?,
    val password: String?,
    val from: String,
    val startTls: Boolean,
    val ssl: Boolean
) {
    fun session(): Session {
        val props = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", port.toString())
            put("mail.smtp.auth", (user != null).toString())
            put("mail.smtp.starttls.enable", startTls.toString())
            put("mail.smtp.starttls.required", startTls.toString())
            put("mail.smtp.ssl.enable", ssl.toString())
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "15000")
            put("mail.smtp.writetimeout", "15000")
        }
        val authenticator = if (user != null && password != null) {
            object : jakarta.mail.Authenticator() {
                override fun getPasswordAuthentication() = PasswordAuthentication(user, password)
            }
        } else null
        return Session.getInstance(props, authenticator)
    }

    companion object {
        fun fromEnv(): SmtpConfig? {
            val host = env("KUOTA_SMTP_HOST") ?: return null
            val from = env("KUOTA_SMTP_FROM") ?: return null
            val port = env("KUOTA_SMTP_PORT")?.toIntOrNull() ?: 587
            return SmtpConfig(
                host = host,
                port = port,
                user = env("KUOTA_SMTP_USER"),
                password = env("KUOTA_SMTP_PASSWORD"),
                from = from,
                startTls = env("KUOTA_SMTP_STARTTLS")?.toBooleanStrictOrNull() ?: (port == 587),
                ssl = env("KUOTA_SMTP_SSL")?.toBooleanStrictOrNull() ?: (port == 465)
            )
        }
    }
}

fun configuredClaimForwarders(dataDirectory: Path): List<ClaimForwarder> = buildList {
    add(LocalArchiveForwarder(dataDirectory.resolve("support-outbox")))
    val zendeskSubdomain = env("KUOTA_ZENDESK_SUBDOMAIN")
    val zendeskEmail = env("KUOTA_ZENDESK_EMAIL")
    val zendeskToken = env("KUOTA_ZENDESK_API_TOKEN")
    if (zendeskSubdomain != null && zendeskEmail != null && zendeskToken != null) {
        add(ZendeskForwarder(zendeskSubdomain, zendeskEmail, zendeskToken))
    }
    val freshdeskDomain = env("KUOTA_FRESHDESK_DOMAIN")
    val freshdeskKey = env("KUOTA_FRESHDESK_API_KEY")
    if (freshdeskDomain != null && freshdeskKey != null) add(FreshdeskForwarder(freshdeskDomain, freshdeskKey))
    val smtp = SmtpConfig.fromEnv()
    val supportInbox = env("KUOTA_SUPPORT_EMAIL")
    if (smtp != null && supportInbox != null) {
        add(SmtpForwarder("CORREO_SOPORTE", smtp, { supportInbox }, ::claimSubject, ::claimSheetText))
    }
    if (smtp != null) {
        add(
            SmtpForwarder(
                "COPIA_CONSUMIDOR",
                smtp,
                { it.submission.email },
                { "Constancia de tu ${if (it.submission.type == "QUEJA") "queja" else "reclamo"} ${it.claimNumber} · Kuota" },
                { record ->
                    "Hola ${record.submission.consumerName}:\n\nRecibimos tu hoja de reclamación. Te responderemos por este medio a más tardar el " +
                        "${record.responseDueDate} ($CLAIM_RESPONSE_BUSINESS_DAYS días hábiles).\n\n" +
                        "Kuota nunca te pedirá tu contraseña, código OTP ni PIN.\n\n" + claimSheetText(record)
                }
            )
        )
    }
}

class ClaimForwardingService(private val store: ClaimStore, private val forwarders: List<ClaimForwarder>) {
    private val logger = LoggerFactory.getLogger("kuota.claims")
    private val executor = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "claim-forwarding").apply { isDaemon = true }
    }

    val channels: List<String> get() = forwarders.map { it.channel }

    fun start() {
        logger.info("Claim forwarding channels: {}", channels)
        executor.scheduleWithFixedDelay({ runCatching { drain() } }, 5, 30, TimeUnit.SECONDS)
    }

    fun kick() {
        executor.execute { runCatching { drain() } }
    }

    private fun drain() {
        val byChannel = forwarders.associateBy { it.channel }
        store.dueDeliveries(System.currentTimeMillis()).forEach { (record, delivery) ->
            val forwarder = byChannel[delivery.channel]
            if (forwarder == null) {
                store.updateDelivery(record.claimNumber, delivery.channel) { it.copy(status = "DESACTIVADO") }
                return@forEach
            }
            val attempt = delivery.attempts + 1
            val result = runCatching { forwarder.deliver(record) }.getOrElse { ForwardResult.Retryable("UNEXPECTED_ERROR") }
            store.updateDelivery(record.claimNumber, delivery.channel) { current ->
                when (result) {
                    is ForwardResult.Delivered -> current.copy(
                        status = "ENVIADO", attempts = attempt, lastError = null,
                        externalId = result.externalId, deliveredAt = Instant.now().toString()
                    )
                    is ForwardResult.Permanent -> current.copy(status = "FALLIDO", attempts = attempt, lastError = result.code)
                    is ForwardResult.Retryable -> if (attempt >= MAX_DELIVERY_ATTEMPTS) {
                        current.copy(status = "FALLIDO", attempts = attempt, lastError = result.code)
                    } else {
                        val backoffMinutes = minOf(1L shl (attempt - 1), 360L)
                        current.copy(
                            attempts = attempt,
                            lastError = result.code,
                            nextAttemptEpochMs = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(backoffMinutes)
                        )
                    }
                }
            }
            val outcome = when (result) {
                is ForwardResult.Delivered -> "ENVIADO"
                is ForwardResult.Permanent -> "FALLIDO:${result.code}"
                is ForwardResult.Retryable -> "REINTENTO:${result.code}"
            }
            logger.info("Claim {} channel {} attempt {} -> {}", record.claimNumber, delivery.channel, attempt, outcome)
        }
    }
}

private fun sha256(value: String): String =
    MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

private fun businessDaysLeft(dueDate: String): Int {
    val today = LocalDate.now(LIMA_ZONE)
    val due = LocalDate.parse(dueDate)
    if (!due.isAfter(today)) return 0
    var count = 0
    var cursor = today
    while (cursor.isBefore(due)) {
        cursor = cursor.plusDays(1)
        if (PeruBusinessCalendar.isBusinessDay(cursor)) count++
    }
    return count
}

private fun maskEmail(email: String): String {
    val at = email.indexOf('@')
    return if (at <= 1) "***" else email.first() + "***" + email.substring(at)
}

fun Route.supportRoutes(
    store: ClaimStore,
    forwarding: ClaimForwardingService,
    onStatusChanged: (ClaimRecord) -> Unit
) {
    post("/support/claims") {
        val user = call.requireUser() ?: return@post
        val request = runCatching { call.receive<ClaimSubmissionRequest>() }.getOrElse {
            call.respond(HttpStatusCode.BadRequest, call.apiError("CLAIM_MALFORMED", "No se pudo leer la hoja de reclamación."))
            return@post
        }
        val idempotencyKey = call.request.headers["Idempotency-Key"]
        if (idempotencyKey != null && idempotencyKey != request.clientClaimId) {
            call.respond(HttpStatusCode.BadRequest, call.apiError("IDEMPOTENCY_KEY_MISMATCH", "Idempotency-Key debe coincidir con client_claim_id."))
            return@post
        }
        when (val result = store.submit(user.userId, request)) {
            is ClaimSubmitResult.Rejected -> call.respond(result.status, call.apiError(result.code, result.message))
            is ClaimSubmitResult.Created -> {
                if (!result.replayed) forwarding.kick()
                call.respond(if (result.replayed) HttpStatusCode.OK else HttpStatusCode.Created, result.record.toReceipt())
            }
        }
    }

    get("/support/claims") {
        val user = call.requireUser() ?: return@get
        call.respond(ClaimListResponse(store.listForUser(user.userId).map { it.toReceipt() }))
    }

    get("/support/claims/{claimNumber}") {
        val user = call.requireUser() ?: return@get
        val record = store.find(call.parameters["claimNumber"].orEmpty())?.takeIf { it.userId == user.userId }
        if (record == null) {
            call.respond(HttpStatusCode.NotFound, call.apiError("CLAIM_NOT_FOUND", "No encontramos esa hoja de reclamación."))
        } else {
            call.respond(record.toReceipt())
        }
    }

    get("/demo/support/claims") {
        call.respond(store.listAll().map { record ->
            OperatorClaimSummary(
                claimNumber = record.claimNumber,
                type = record.submission.type,
                status = record.status,
                helpTopic = record.submission.helpTopic,
                consumerName = record.submission.consumerName,
                documentMasked = "${record.submission.documentType} ${maskTail(record.submission.documentNumber, 3)}",
                emailMasked = maskEmail(record.submission.email),
                receivedAt = record.receivedAt,
                responseDueDate = record.responseDueDate,
                businessDaysLeft = businessDaysLeft(record.responseDueDate),
                channels = record.deliveries
            )
        })
    }

    post("/demo/support/claims/{claimNumber}/status") {
        val request = call.receive<OperatorClaimStatusRequest>()
        if (request.status !in claimStatuses) {
            call.respond(HttpStatusCode.UnprocessableEntity, call.apiError("STATUS_INVALID", "Estado permitido: ${claimStatuses.joinToString()}"))
            return@post
        }
        if (request.status == "RESPONDIDO" && request.responseText.isNullOrBlank()) {
            call.respond(HttpStatusCode.UnprocessableEntity, call.apiError("RESPONSE_REQUIRED", "Incluye response_text al responder."))
            return@post
        }
        val updated = store.updateStatus(call.parameters["claimNumber"].orEmpty(), request.status, request.responseText)
        if (updated == null) {
            call.respond(HttpStatusCode.NotFound, call.apiError("CLAIM_NOT_FOUND", "No existe esa hoja."))
        } else {
            onStatusChanged(updated)
            call.respond(updated.toReceipt())
        }
    }
}

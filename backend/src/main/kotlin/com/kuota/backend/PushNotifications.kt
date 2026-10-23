package com.kuota.backend

import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

data class DueInstallment(
    val purchaseId: String,
    val installmentId: String,
    val sequenceNumber: Int,
    val amountCents: Long,
    val dueAt: String,
    val status: String
)

@Serializable
data class NotificationPreferences(
    @SerialName("installment_reminders") val installmentReminders: Boolean = true,
    @SerialName("support_updates") val supportUpdates: Boolean = true,
    val news: Boolean = false
)

@Serializable
data class DeviceRegistrationRequest(
    val token: String,
    val platform: String = "android",
    @SerialName("app_version") val appVersion: String? = null,
    val preferences: NotificationPreferences = NotificationPreferences()
)

@Serializable
data class DeviceRecord(
    val token: String,
    @SerialName("user_id") val userId: String,
    val platform: String,
    @SerialName("app_version") val appVersion: String?,
    val preferences: NotificationPreferences,
    @SerialName("registered_at") val registeredAt: String,
    @SerialName("last_seen_at") val lastSeenAt: String
)

@Serializable
data class DeviceRegistrationResponse(
    @SerialName("device_ref") val deviceRef: String,
    @SerialName("push_mode") val pushMode: String,
    val preferences: NotificationPreferences,
    @SerialName("quiet_hours") val quietHours: String,
    val timezone: String = "America/Lima"
)

@Serializable
data class PushOutboxEntry(
    val id: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("user_id") val userId: String,
    @SerialName("token_masked") val tokenMasked: String,
    val type: String,
    val title: String,
    val status: String,
    val error: String? = null
)

@Serializable
data class TestPushResponse(
    @SerialName("push_mode") val pushMode: String,
    val devices: Int,
    val delivered: Int
)

@Serializable
data class ReminderDispatch(
    @SerialName("installment_id") val installmentId: String,
    val kind: String,
    val devices: Int
)

@Serializable
data class ReminderRunReport(
    val today: String,
    @SerialName("quiet_hours_active") val quietHoursActive: Boolean,
    val evaluated: Int,
    val dispatched: List<ReminderDispatch>,
    val deferred: List<String>,
    @SerialName("push_mode") val pushMode: String
)

/** Dispositivos sin Firebase registran un token "pull-<uuid>" y consultan su bandeja periódicamente. */
const val PULL_TOKEN_PREFIX = "pull-"

@Serializable
data class InboxMessage(
    val id: String,
    @SerialName("created_at") val createdAt: String,
    val data: Map<String, String>
)

@Serializable
data class InboxResponse(val messages: List<InboxMessage>)

@Serializable
data class InboxAckRequest(val ids: List<String> = emptyList())

@Serializable
private data class PersistedDevices(
    val devices: List<DeviceRecord> = emptyList(),
    @SerialName("sent_reminder_keys") val sentReminderKeys: List<String> = emptyList(),
    val outbox: List<PushOutboxEntry> = emptyList(),
    val inboxes: Map<String, List<InboxMessage>> = emptyMap()
)

class DeviceStore(private val file: Path) {
    private val lock = Any()
    private val devices = linkedMapOf<String, DeviceRecord>()
    private val sentReminderKeys = linkedSetOf<String>()
    private val outbox = ArrayDeque<PushOutboxEntry>()
    private val inboxes = linkedMapOf<String, List<InboxMessage>>()

    init {
        readJsonFile(file, PersistedDevices.serializer())?.let { persisted ->
            persisted.devices.forEach { devices[it.token] = it }
            sentReminderKeys.addAll(persisted.sentReminderKeys)
            outbox.addAll(persisted.outbox)
            inboxes.putAll(persisted.inboxes)
        }
    }

    fun enqueueInbox(token: String, message: InboxMessage) = synchronized(lock) {
        val cutoff = Instant.now().minus(7, ChronoUnit.DAYS).toString()
        inboxes[token] = ((inboxes[token] ?: emptyList()) + message).filter { it.createdAt >= cutoff }.takeLast(50)
        persist()
    }

    fun inboxFor(userId: String, token: String): List<InboxMessage>? = synchronized(lock) {
        if (devices[token]?.userId != userId) return null
        inboxes[token] ?: emptyList()
    }

    fun ackInbox(userId: String, token: String, ids: Set<String>) = synchronized(lock) {
        if (devices[token]?.userId != userId) return
        val remaining = inboxes[token]?.filterNot { it.id in ids } ?: return
        if (remaining.isEmpty()) inboxes.remove(token) else inboxes[token] = remaining
        persist()
    }

    fun register(userId: String, request: DeviceRegistrationRequest): DeviceRecord = synchronized(lock) {
        val now = Instant.now().toString()
        val record = DeviceRecord(
            token = request.token,
            userId = userId,
            platform = request.platform.take(20),
            appVersion = request.appVersion?.take(30),
            preferences = request.preferences,
            registeredAt = devices[request.token]?.registeredAt ?: now,
            lastSeenAt = now
        )
        devices[request.token] = record
        persist()
        record
    }

    fun unregister(userId: String, token: String): Boolean = synchronized(lock) {
        val removed = devices[token]?.takeIf { it.userId == userId }?.let { devices.remove(token) } != null
        if (removed) {
            inboxes.remove(token)
            persist()
        }
        removed
    }

    fun removeInvalid(token: String) = synchronized(lock) {
        if (devices.remove(token) != null) {
            inboxes.remove(token)
            persist()
        }
    }

    fun devicesFor(userId: String): List<DeviceRecord> = synchronized(lock) { devices.values.filter { it.userId == userId } }

    fun allDevices(): List<DeviceRecord> = synchronized(lock) { devices.values.toList() }

    fun wasReminderSent(key: String): Boolean = synchronized(lock) { key in sentReminderKeys }

    fun markReminderSent(key: String) = synchronized(lock) {
        sentReminderKeys.add(key)
        while (sentReminderKeys.size > 5_000) sentReminderKeys.remove(sentReminderKeys.first())
        persist()
    }

    fun appendOutbox(entry: PushOutboxEntry) = synchronized(lock) {
        outbox.addFirst(entry)
        while (outbox.size > 200) outbox.removeLast()
        persist()
    }

    fun outbox(): List<PushOutboxEntry> = synchronized(lock) { outbox.toList() }

    private fun persist() {
        writeJsonFileAtomically(
            file,
            PersistedDevices.serializer(),
            PersistedDevices(devices.values.toList(), sentReminderKeys.toList(), outbox.toList(), inboxes.toMap())
        )
    }
}

sealed interface PushSendResult {
    data class Sent(val messageId: String) : PushSendResult
    data object InvalidToken : PushSendResult
    data class Failed(val code: String) : PushSendResult
}

interface PushSender {
    val mode: String
    fun send(token: String, data: Map<String, String>, highPriority: Boolean): PushSendResult
}

object DryRunPushSender : PushSender {
    override val mode = "DRY_RUN"
    override fun send(token: String, data: Map<String, String>, highPriority: Boolean): PushSendResult =
        PushSendResult.Sent("dry-run-${UUID.randomUUID().toString().take(8)}")
}

class FcmHttpV1Sender(serviceAccountFile: Path, projectIdOverride: String?) : PushSender {
    override val mode = "FCM"
    private val credentials: GoogleCredentials = Files.newInputStream(serviceAccountFile).use { stream ->
        GoogleCredentials.fromStream(stream).createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
    }
    private val projectId: String = projectIdOverride
        ?: (credentials as? ServiceAccountCredentials)?.projectId
        ?: error("KUOTA_FCM_PROJECT_ID es obligatorio si la cuenta de servicio no incluye project_id")
    private val client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()

    override fun send(token: String, data: Map<String, String>, highPriority: Boolean): PushSendResult {
        val body = buildJsonObject {
            putJsonObject("message") {
                put("token", token)
                putJsonObject("data") { data.forEach { (key, value) -> put(key, value) } }
                putJsonObject("android") {
                    put("priority", if (highPriority) "HIGH" else "NORMAL")
                    put("ttl", "86400s")
                    put("collapse_key", data["notification_id"] ?: "kuota")
                }
            }
        }
        return try {
            val accessToken = synchronized(credentials) {
                credentials.refreshIfExpired()
                credentials.accessToken.tokenValue
            }
            val response = client.send(
                HttpRequest.newBuilder(URI.create("https://fcm.googleapis.com/v1/projects/$projectId/messages:send"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer $accessToken")
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            )
            when {
                response.statusCode() in 200..299 -> PushSendResult.Sent(
                    runCatching { storeJson.parseToJsonElement(response.body()).jsonObject["name"]?.jsonPrimitive?.content }.getOrNull() ?: "fcm"
                )
                response.statusCode() == 404 || response.body().contains("UNREGISTERED") -> PushSendResult.InvalidToken
                response.statusCode() == 400 && response.body().contains("registration token", ignoreCase = true) -> PushSendResult.InvalidToken
                else -> PushSendResult.Failed("HTTP_${response.statusCode()}")
            }
        } catch (_: IOException) {
            PushSendResult.Failed("NETWORK_ERROR")
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            PushSendResult.Failed("INTERRUPTED")
        }
    }
}

fun configuredPushSender(): PushSender {
    val logger = LoggerFactory.getLogger("kuota.push")
    val path = env("KUOTA_FCM_SERVICE_ACCOUNT")?.let(Path::of) ?: return DryRunPushSender.also {
        logger.info("FCM sin configurar (KUOTA_FCM_SERVICE_ACCOUNT). Las notificaciones quedan en el outbox DRY_RUN.")
    }
    return runCatching { FcmHttpV1Sender(path, env("KUOTA_FCM_PROJECT_ID")) }
        .onSuccess { logger.info("FCM HTTP v1 activo.") }
        .getOrElse { error ->
            logger.warn("No se pudo cargar la cuenta de servicio FCM ({}). Se usa DRY_RUN.", error.javaClass.simpleName)
            DryRunPushSender
        }
}

data class PushMessage(
    val type: String,
    val title: String,
    val body: String,
    val deepLink: String,
    val highPriority: Boolean = false,
    val extras: Map<String, String> = emptyMap()
)

class PushService(private val devices: DeviceStore, private val sender: PushSender) {
    private val logger = LoggerFactory.getLogger("kuota.push")
    val mode: String get() = sender.mode

    fun sendTo(targets: List<DeviceRecord>, message: PushMessage): Int {
        var delivered = 0
        val notificationId = UUID.randomUUID().toString()
        targets.forEach { device ->
            val data = buildMap {
                put("type", message.type)
                put("title", message.title)
                put("body", message.body)
                put("deep_link", message.deepLink)
                put("notification_id", notificationId)
                put("sent_at", Instant.now().toString())
                putAll(message.extras)
            }
            val isPull = device.token.startsWith(PULL_TOKEN_PREFIX)
            val result = if (isPull) {
                devices.enqueueInbox(device.token, InboxMessage(notificationId, Instant.now().toString(), data))
                PushSendResult.Sent("inbox")
            } else {
                sender.send(device.token, data, message.highPriority)
            }
            val (status, error) = when (result) {
                is PushSendResult.Sent -> (if (isPull) "EN_BANDEJA" else if (sender.mode == "FCM") "ENVIADO" else "DRY_RUN") to null
                PushSendResult.InvalidToken -> "TOKEN_INVALIDO" to "UNREGISTERED"
                is PushSendResult.Failed -> "FALLIDO" to result.code
            }
            if (result is PushSendResult.Sent) delivered++
            if (result == PushSendResult.InvalidToken) devices.removeInvalid(device.token)
            devices.appendOutbox(
                PushOutboxEntry(
                    id = notificationId,
                    createdAt = Instant.now().toString(),
                    userId = device.userId,
                    tokenMasked = maskTail(device.token, 6),
                    type = message.type,
                    title = message.title,
                    status = status,
                    error = error
                )
            )
            logger.info("Push {} -> device {} status {}", message.type, maskTail(device.token, 6), status)
        }
        return delivered
    }

    fun notifyUser(userId: String, message: PushMessage, wants: (NotificationPreferences) -> Boolean): Int =
        sendTo(devices.devicesFor(userId).filter { wants(it.preferences) }, message)
}

class ReminderScheduler(
    private val devices: DeviceStore,
    private val push: PushService,
    private val installments: () -> List<DueInstallment>
) {
    private val logger = LoggerFactory.getLogger("kuota.reminders")
    // [VALIDACIÓN LEGAL] Código de Protección al Consumidor art. 62: no enviar comunicaciones de cobranza
    // en horario nocturno ni fines de semana/feriados. Valores por defecto conservadores y configurables.
    private val quietStart = env("KUOTA_QUIET_HOURS_START")?.toIntOrNull()?.coerceIn(0, 23) ?: 20
    private val quietEnd = env("KUOTA_QUIET_HOURS_END")?.toIntOrNull()?.coerceIn(0, 23) ?: 7
    private val intervalMinutes = env("KUOTA_REMINDER_INTERVAL_MINUTES")?.toLongOrNull()?.coerceAtLeast(1) ?: 30
    private val executor = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "installment-reminders").apply { isDaemon = true }
    }
    private val dateFormat = DateTimeFormatter.ofPattern("dd/MM")

    val quietHoursLabel: String get() = "%02d:00–%02d:00".format(quietStart, quietEnd)

    fun start() {
        executor.scheduleWithFixedDelay({
            runCatching { run(LocalDate.now(LIMA_ZONE), LocalTime.now(LIMA_ZONE), ignoreQuietHours = false) }
                .onFailure { logger.warn("Reminder run failed: {}", it.javaClass.simpleName) }
        }, 1, intervalMinutes, TimeUnit.MINUTES)
    }

    private fun isQuiet(time: LocalTime): Boolean {
        val hour = time.hour
        return if (quietStart > quietEnd) hour >= quietStart || hour < quietEnd else hour in quietStart until quietEnd
    }

    @Synchronized
    fun run(today: LocalDate, now: LocalTime, ignoreQuietHours: Boolean): ReminderRunReport {
        val quiet = !ignoreQuietHours && isQuiet(now)
        val dispatched = mutableListOf<ReminderDispatch>()
        val deferred = mutableListOf<String>()
        val candidates = installments().filter { it.status == "PROGRAMMED" || it.status == "PAYMENT_PENDING" }
        val targets = devices.allDevices().filter { it.preferences.installmentReminders }

        candidates.forEach { installment ->
            val due = runCatching { LocalDate.parse(installment.dueAt) }.getOrNull() ?: return@forEach
            val daysUntil = ChronoUnit.DAYS.between(today, due)
            val kind = when (daysUntil) {
                1L, 2L -> "UPCOMING"
                0L -> "DUE_TODAY"
                -1L, -2L, -3L -> "OVERDUE"
                else -> return@forEach
            }
            val key = "${installment.installmentId}:$kind"
            if (devices.wasReminderSent(key)) return@forEach
            if (quiet || (kind == "OVERDUE" && !ignoreQuietHours && !PeruBusinessCalendar.isBusinessDay(today))) {
                deferred += key
                return@forEach
            }
            if (targets.isEmpty()) {
                deferred += "$key:SIN_DISPOSITIVOS"
                return@forEach
            }
            val dueLabel = due.format(dateFormat)
            val message = when (kind) {
                "UPCOMING" -> PushMessage(
                    type = "INSTALLMENT_REMINDER",
                    title = if (daysUntil == 1L) "Tu cuota vence mañana" else "Tu cuota vence pronto",
                    body = "La cuota ${installment.sequenceNumber} vence el $dueLabel. Revisa el detalle en Kuota.",
                    deepLink = "kuota://compras"
                )
                "DUE_TODAY" -> PushMessage(
                    type = "INSTALLMENT_REMINDER",
                    title = "Tu cuota vence hoy",
                    body = "La cuota ${installment.sequenceNumber} vence hoy. Mantén tu plan al día desde Kuota.",
                    deepLink = "kuota://compras",
                    highPriority = true
                )
                else -> PushMessage(
                    type = "INSTALLMENT_OVERDUE",
                    title = "Tienes una cuota pendiente",
                    body = "La cuota ${installment.sequenceNumber} venció el $dueLabel. Revisa tus opciones de pago o escríbenos si necesitas ayuda.",
                    deepLink = "kuota://compras",
                    highPriority = true
                )
            }.copy(
                extras = mapOf(
                    "kind" to kind,
                    "installment_id" to installment.installmentId,
                    "purchase_id" to installment.purchaseId,
                    "amount_cents" to installment.amountCents.toString(),
                    "due_at" to installment.dueAt
                )
            )
            val delivered = push.sendTo(targets, message)
            if (delivered > 0) devices.markReminderSent(key)
            dispatched += ReminderDispatch(installment.installmentId, kind, delivered)
        }
        if (dispatched.isNotEmpty()) logger.info("Reminder run {}: {} dispatched, {} deferred", today, dispatched.size, deferred.size)
        return ReminderRunReport(today.toString(), quiet, candidates.size, dispatched, deferred, push.mode)
    }
}

fun Route.pushRoutes(devices: DeviceStore, push: PushService, scheduler: ReminderScheduler) {
    post("/devices") {
        val user = call.requireUser() ?: return@post
        val request = runCatching { call.receive<DeviceRegistrationRequest>() }.getOrNull()
        if (request == null || request.token.length !in 20..4096) {
            call.respond(HttpStatusCode.BadRequest, call.apiError("DEVICE_TOKEN_INVALID", "Token de dispositivo inválido."))
            return@post
        }
        val record = devices.register(user.userId, request)
        call.respond(
            DeviceRegistrationResponse(
                deviceRef = maskTail(record.token, 6),
                pushMode = if (record.token.startsWith(PULL_TOKEN_PREFIX)) "PULL" else push.mode,
                preferences = record.preferences,
                quietHours = scheduler.quietHoursLabel
            )
        )
    }

    delete("/devices/{token}") {
        val user = call.requireUser() ?: return@delete
        devices.unregister(user.userId, call.parameters["token"].orEmpty())
        call.respond(HttpStatusCode.NoContent)
    }

    get("/devices/{token}/inbox") {
        val user = call.requireUser() ?: return@get
        val messages = devices.inboxFor(user.userId, call.parameters["token"].orEmpty())
        if (messages == null) {
            call.respond(HttpStatusCode.NotFound, call.apiError("DEVICE_NOT_REGISTERED", "Dispositivo no registrado."))
        } else {
            call.respond(InboxResponse(messages))
        }
    }

    post("/devices/{token}/inbox/ack") {
        val user = call.requireUser() ?: return@post
        val request = runCatching { call.receive<InboxAckRequest>() }.getOrNull() ?: InboxAckRequest()
        devices.ackInbox(user.userId, call.parameters["token"].orEmpty(), request.ids.toSet())
        call.respond(HttpStatusCode.NoContent)
    }

    post("/devices/test-push") {
        val user = call.requireUser() ?: return@post
        val targets = devices.devicesFor(user.userId)
        val delivered = push.sendTo(
            targets,
            PushMessage(
                type = "TEST",
                title = "Notificaciones activas",
                body = "Así te avisaremos de tus cuotas y de tus reclamos.",
                deepLink = "kuota://notificaciones"
            )
        )
        call.respond(TestPushResponse(push.mode, targets.size, delivered))
    }

    post("/demo/reminders/run") {
        val today = call.request.queryParameters["today"]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: LocalDate.now(LIMA_ZONE)
        val ignoreQuiet = call.request.queryParameters["ignore_quiet_hours"] == "true"
        call.respond(scheduler.run(today, LocalTime.now(LIMA_ZONE), ignoreQuiet))
    }

    get("/demo/push/outbox") {
        call.respond(devices.outbox())
    }
}

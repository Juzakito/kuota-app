package com.kouta.app.core.push

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.kouta.app.BuildConfig
import com.kouta.app.data.local.AppSettings
import com.kouta.app.data.local.LocalNotificationSettings
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.data.remote.ApiOutcome
import com.kouta.app.data.remote.DeviceRegistrationBody
import com.kouta.app.data.remote.InboxAckBody
import com.kouta.app.data.remote.KuotaApiClient
import com.kouta.app.data.remote.NotificationPreferencesDto
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

sealed interface PushStatus {
    data object NoSession : PushStatus
    data class Registered(val mode: String, val quietHours: String?) : PushStatus
    data class Error(val message: String) : PushStatus
}

object PushRegistrar {
    fun isFirebaseConfigured(context: Context): Boolean =
        BuildConfig.FIREBASE_CONFIGURED && runCatching { FirebaseApp.getApps(context).isNotEmpty() }.getOrDefault(false)

    fun isPullToken(token: String?): Boolean = token?.startsWith("pull-") == true

    /** Token FCM si Firebase y Play Services están disponibles; si no, token de consulta periódica. */
    private suspend fun resolveToken(context: Context): String {
        val settings = AppSettings(context)
        if (isFirebaseConfigured(context)) {
            settings.fcmToken?.let { return it }
            fetchFcmToken()?.let {
                settings.fcmToken = it
                return it
            }
        }
        return settings.pullToken
    }

    // Sin Google Play Services la tarea falla con SERVICE_NOT_AVAILABLE: se devuelve null y se usa modo consulta.
    private suspend fun fetchFcmToken(): String? = withTimeoutOrNull(15_000) {
        suspendCancellableCoroutine { continuation ->
            runCatching {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    continuation.resume(if (task.isSuccessful) task.result else null)
                }
            }.onFailure { continuation.resume(null) }
        }
    }

    fun registeredToken(context: Context): String? = AppSettings(context).registeredFingerprint?.substringBefore('|')

    suspend fun register(context: Context, force: Boolean = false): PushStatus {
        if (!KuotaApiClient.hasSession(context)) return PushStatus.NoSession
        val settings = AppSettings(context)
        val notificationSettings = runCatching { SalesJournalStore(context).readBusinessProfile()?.notificationSettings }
            .getOrNull() ?: LocalNotificationSettings()
        val preferences = NotificationPreferencesDto(
            installmentReminders = notificationSettings.installmentRemindersEnabled,
            supportUpdates = notificationSettings.supportUpdatesEnabled,
            news = notificationSettings.newsAndPromotionsEnabled
        )
        val token = resolveToken(context)
        val fingerprint = "$token|${settings.serverUrl}|$preferences"
        val cachedMode = settings.pushMode
        if (!force && settings.registeredFingerprint == fingerprint && cachedMode != null) {
            return PushStatus.Registered(cachedMode, settings.quietHours)
        }
        return when (val outcome = KuotaApiClient.call(context) { registerDevice(DeviceRegistrationBody(token = token, preferences = preferences)) }) {
            is ApiOutcome.Ok -> {
                settings.registeredFingerprint = fingerprint
                settings.pushMode = outcome.value.pushMode
                settings.quietHours = outcome.value.quietHours
                settings.lastPushError = null
                PushStatus.Registered(outcome.value.pushMode, outcome.value.quietHours)
            }
            ApiOutcome.SessionExpired -> PushStatus.NoSession
            is ApiOutcome.Rejected -> PushStatus.Error(outcome.message).also { settings.lastPushError = outcome.message }
            is ApiOutcome.Unreachable -> PushStatus.Error("No se pudo conectar con ${outcome.serverUrl}").also {
                settings.lastPushError = it.message
            }
        }
    }

    /** Descarga y procesa los avisos encolados para dispositivos en modo consulta. Devuelve false si no hubo conexión. */
    suspend fun pollInbox(context: Context): Boolean {
        val token = registeredToken(context)
        if (!isPullToken(token) || token == null || !KuotaApiClient.hasSession(context)) return true
        return when (val outcome = KuotaApiClient.call(context) { inbox(token) }) {
            is ApiOutcome.Ok -> {
                val messages = outcome.value.messages
                messages.sortedBy { it.createdAt }.forEach { IncomingNotifications.handle(context, it.id, it.data) }
                if (messages.isNotEmpty()) {
                    KuotaApiClient.call(context) { ackInbox(token, InboxAckBody(messages.map { it.id })) }
                }
                true
            }
            is ApiOutcome.Rejected -> {
                // 404: el servidor perdió el registro; se fuerza un nuevo registro en la próxima sincronización.
                if (outcome.httpCode == 404) AppSettings(context).registeredFingerprint = null
                true
            }
            ApiOutcome.SessionExpired -> true
            is ApiOutcome.Unreachable -> false
        }
    }

    suspend fun unregister(context: Context) {
        val token = registeredToken(context) ?: return
        if (KuotaApiClient.hasSession(context)) KuotaApiClient.call(context) { unregisterDevice(token) }
        AppSettings(context).registeredFingerprint = null
    }
}

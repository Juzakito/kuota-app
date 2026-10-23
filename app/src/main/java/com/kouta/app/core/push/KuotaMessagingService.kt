package com.kouta.app.core.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kouta.app.core.sync.KuotaSync
import com.kouta.app.data.local.AppSettings
import java.util.UUID

/** Recibe mensajes "data-only" del backend (FCM HTTP v1) y los convierte en avisos locales. */
class KuotaMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        AppSettings(this).fcmToken = token
        KuotaSync.enqueue(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        if (data.isEmpty()) return
        IncomingNotifications.handle(this, message.messageId ?: UUID.randomUUID().toString(), data)
    }
}

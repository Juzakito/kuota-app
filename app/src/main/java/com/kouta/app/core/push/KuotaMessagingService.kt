package com.kouta.app.core.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kouta.app.core.sync.KuotaSync
import com.kouta.app.data.local.AppSettings
import java.util.UUID

/** Recibe mensajes del backend (FCM) y los convierte en avisos locales para el ciclo de compra y cuotas. */
class KuotaMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        AppSettings(this).fcmToken = token
        KuotaSync.enqueue(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val payloadData = message.data.toMutableMap()

        // Si el push trae notificación nativa FCM, la extraemos para presentación local
        message.notification?.let { notif ->
            if (!payloadData.containsKey("title")) payloadData["title"] = notif.title.orEmpty()
            if (!payloadData.containsKey("body")) payloadData["body"] = notif.body.orEmpty()
        }

        if (payloadData.isEmpty()) return

        IncomingNotifications.handle(
            this,
            message.messageId ?: UUID.randomUUID().toString(),
            payloadData
        )
    }
}

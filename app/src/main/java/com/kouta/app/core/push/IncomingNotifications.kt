package com.kouta.app.core.push

import android.content.Context
import com.kouta.app.core.sync.KuotaSync
import com.kouta.app.core.util.NotificationHelper
import com.kouta.app.data.local.LocalNotificationItem
import com.kouta.app.data.local.LocalNotificationSettings
import com.kouta.app.data.local.SalesJournalStore

object IncomingNotifications {
    /** Guarda el mensaje en la bandeja interna y lo muestra en la barra si el usuario lo quiere. */
    fun handle(context: Context, fallbackId: String, data: Map<String, String>) {
        val store = SalesJournalStore(context)
        val type = data["type"] ?: "NEWS"
        val item = LocalNotificationItem(
            id = data["notification_id"] ?: fallbackId,
            type = type,
            title = data["title"] ?: "Kuota",
            body = data["body"].orEmpty(),
            deepLink = data["deep_link"],
            amountCents = data["amount_cents"]?.toLongOrNull(),
            receivedAtEpochMillis = System.currentTimeMillis()
        )
        val alreadyStored = runCatching { store.readNotifications().any { it.id == item.id } }.getOrDefault(false)
        if (alreadyStored) return
        runCatching { store.addNotification(item) }
        val settings = runCatching { store.readBusinessProfile()?.notificationSettings }.getOrNull()
            ?: LocalNotificationSettings()
        if (NotificationHelper.isWanted(type, settings)) NotificationHelper.show(context, item, settings)
        if (type.startsWith("SUPPORT")) KuotaSync.enqueue(context)
        KuotaEvents.bump()
    }
}

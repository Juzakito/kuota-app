package com.kouta.app.core.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.compose.ui.graphics.toArgb
import com.kouta.app.MainActivity
import com.kouta.app.R
import com.kouta.app.core.designsystem.theme.KuotaBlue
import com.kouta.app.data.local.LocalNotificationItem
import com.kouta.app.data.local.LocalNotificationSettings
import com.kouta.app.domain.model.PurchaseOffer

object NotificationHelper {
    const val EXTRA_DEEP_LINK = "com.kouta.app.extra.DEEP_LINK"

    const val CHANNEL_INSTALLMENTS = "kuota_cuotas"
    const val CHANNEL_SUPPORT = "kuota_soporte"
    const val CHANNEL_NEWS = "kuota_novedades"
    private const val LEGACY_CHANNEL = "kuota_payment_reminders"

    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.deleteNotificationChannel(LEGACY_CHANNEL)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(CHANNEL_INSTALLMENTS, "Cuotas y pagos", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Recordatorios de vencimiento y estado de tus cuotas."
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
                },
                NotificationChannel(CHANNEL_SUPPORT, "Ayuda y reclamos", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Avances y respuestas de tus reclamos y consultas."
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
                },
                NotificationChannel(CHANNEL_NEWS, "Novedades", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Novedades y promociones. Desactivadas por defecto."
                }
            )
        )
    }

    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun canPost(context: Context): Boolean =
        hasPermission(context) && NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun channelFor(type: String): String = when {
        type.startsWith("INSTALLMENT") -> CHANNEL_INSTALLMENTS
        type.startsWith("SUPPORT") || type == "TEST" -> CHANNEL_SUPPORT
        else -> CHANNEL_NEWS
    }

    /** ¿El usuario quiere ver este tipo en la barra? (la bandeja interna lo guarda igual). */
    fun isWanted(type: String, settings: LocalNotificationSettings): Boolean = when (channelFor(type)) {
        CHANNEL_INSTALLMENTS -> settings.installmentRemindersEnabled
        CHANNEL_SUPPORT -> settings.supportUpdatesEnabled || type == "TEST"
        else -> settings.newsAndPromotionsEnabled
    }

    fun show(context: Context, item: LocalNotificationItem, settings: LocalNotificationSettings): Boolean {
        if (!canPost(context)) return false
        ensureChannels(context)
        val channel = channelFor(item.type)
        val amountLine = item.amountCents?.let { "Monto: ${PurchaseOffer.formatPen(it)}" }
        val fullText = listOfNotNull(item.body, amountLine).joinToString("\n")

        val publicTitle = when (channel) {
            CHANNEL_INSTALLMENTS -> "Recordatorio de Kuota"
            CHANNEL_SUPPORT -> "Novedad en Ayuda y reclamos"
            else -> "Kuota"
        }
        val publicVersion = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_kuota)
            .setContentTitle(publicTitle)
            .setContentText("Desbloquea tu teléfono para ver el detalle.")
            .build()

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_kuota)
            .setColor(KuotaBlue.toArgb())
            .setContentTitle(item.title)
            .setContentText(item.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fullText))
            .setCategory(if (channel == CHANNEL_INSTALLMENTS) NotificationCompat.CATEGORY_REMINDER else NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(if (channel == CHANNEL_INSTALLMENTS) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(if (settings.lockScreenDetail) NotificationCompat.VISIBILITY_PUBLIC else NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion)
            .setContentIntent(contentIntent(context, item))
            .setAutoCancel(true)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(item.id.hashCode(), notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    private fun contentIntent(context: Context, item: LocalNotificationItem): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DEEP_LINK, item.deepLink ?: "kuota://notificaciones")
        }
        return PendingIntent.getActivity(
            context,
            item.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

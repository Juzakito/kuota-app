package com.kouta.app.core.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.kouta.app.data.local.SaleSource
import com.kouta.app.data.local.SalesJournalStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Servicio de captura automática de ventas vía notificaciones de Yape y Plin (PRD SAL-02).
 * Procesa únicamente notificaciones de las apps de pago autorizadas en el dispositivo.
 */
class PaymentNotificationListenerService : NotificationListenerService() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName.orEmpty()
        // Lista blanca oficial: BCP Yape y apps que integran Plin (Interbank, BBVA, Scotiabank)
        val isYape = pkg.contains("bcp.innovacxion.yapeapp") || pkg.contains("yape")
        val isPlin = pkg.contains("interbank.plin") || pkg.contains("plin") || pkg.contains("bbva") || pkg.contains("scotiabank")

        if (!isYape && !isPlin) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getString("android.title").orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()
        val content = "$title $text"

        // Extraer monto en soles: ej: "te envió S/ 50.00" o "recibiste S/ 120"
        val regex = Regex("""(?:S/\.?|soles)\s*([\d]+(?:[.,]\d{1,2})?)""", RegexOption.IGNORE_CASE)
        val match = regex.find(content) ?: return
        val amountStr = match.groupValues[1].replace(",", ".")
        val amount = amountStr.toDoubleOrNull() ?: return
        val amountCents = (amount * 100).toLong()

        if (amountCents <= 0) return

        val source = if (isYape) SaleSource.YAPE else SaleSource.PLIN
        val store = SalesJournalStore(applicationContext)

        scope.launch {
            runCatching {
                store.saveSale(
                    existing = null,
                    amountCents = amountCents,
                    now = System.currentTimeMillis(),
                    isVerified = true,
                    productName = if (isYape) "Cobro verificado Yape" else "Cobro verificado Plin",
                    paymentMethod = if (isYape) "Yape" else "Plin",
                    source = source
                )
                Log.d("KuotaNotification", "Venta capturada automáticamente: $amountCents centavos ($source)")
            }
        }
    }
}

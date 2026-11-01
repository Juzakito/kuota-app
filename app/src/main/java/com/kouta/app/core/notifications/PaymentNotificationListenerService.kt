package com.kouta.app.core.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.kouta.app.di.AppEntryPoint
import com.kuota.core.data.parser.BankNotificationParser
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Servicio de captura automática de ventas mediante NotificationListenerService (PRD §8.3 y AGENTS.md).
 *
 * REGLAS ESTRICTAS DE CALIDAD Y SEGURIDAD:
 * 1. Lista blanca de paquetes autorizados (Yape, Plin, BCP, BBVA, Interbank, Scotiabank, Mercado Pago).
 * 2. Cero PII: El texto crudo de la notificación se descarta inmediatamente tras el parseo en memoria.
 * 3. Cero almacenamiento local o mocks: Se envía directamente al backend seguro vía SalesRepository.
 * 4. Deduplicación por txHash criptográfico.
 * 5. NO utiliza AccessibilityService.
 */
class PaymentNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName.orEmpty()
        if (!BankNotificationParser.isWhitelistedPackage(packageName)) {
            return
        }

        val extras = sbn.notification?.extras ?: return
        val title = extras.getString("android.title").orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()

        if (title.isBlank() && text.isBlank()) {
            return
        }

        // 1. Parseo en memoria en el dispositivo
        val parsedResult = BankNotificationParser.parseNotification(
            packageName = packageName,
            title = title,
            text = text,
            timestampMs = sbn.postTime.takeIf { it > 0 } ?: System.currentTimeMillis()
        )

        // 2. DESCARTE INMEDIATO DEL TEXTO CRUDO (CERO PII)
        // 'title' y 'text' quedan fuera de alcance y no se persisten en logs ni base de datos.
        if (parsedResult == null) {
            return
        }

        val record = BankNotificationParser.toSaleRecord(parsedResult)

        // 3. Subida en lote/transaccional a Firebase mediante SalesRepository
        serviceScope.launch {
            runCatching {
                val entryPoint = EntryPointAccessors.fromApplication(
                    applicationContext,
                    AppEntryPoint::class.java
                )
                val salesRepo = entryPoint.salesRepository()
                salesRepo.uploadSalesBatch(listOf(record)).getOrThrow()
                Log.d("KuotaSales", "Venta verificada registrada: ${record.amountCents} céntimos (${record.source})")
            }.onFailure { err ->
                Log.w("KuotaSales", "Error al registrar venta desde notificación", err)
            }
        }
    }
}

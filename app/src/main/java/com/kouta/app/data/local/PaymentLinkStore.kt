package com.kouta.app.data.local

import android.content.Context
import com.kouta.app.domain.model.PaymentLink
import com.kouta.app.domain.model.PaymentLinkStatus
import com.kouta.app.domain.model.PurchaseOffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

class PaymentLinkStore(private val context: Context) {

    private val journalStore by lazy { SalesJournalStore(context) }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    private val storageFile: File
        get() = File(context.filesDir, "kuota_payment_links.json")

    private val lock = Any()

    suspend fun getAll(): List<PaymentLink> = withContext(Dispatchers.IO) {
        synchronized(lock) {
            readInternal()
        }
    }

    suspend fun getById(id: String): PaymentLink? = withContext(Dispatchers.IO) {
        synchronized(lock) {
            readInternal().firstOrNull { it.id == id }
        }
    }

    suspend fun createLink(
        sellerId: String,
        sellerName: String,
        customerName: String,
        customerDni: String,
        customerPhone: String,
        customerEmail: String = "",
        concept: String,
        quantity: Int = 1,
        unitPriceCents: Long = 0L,
        totalAmountCents: Long,
        receiptType: String = "BOLETA",
        internalNote: String = "",
        validityHours: Int = 24
    ): PaymentLink = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val current = readInternal()
            val linkId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val expiresAt = now + (validityHours.toLong() * 3600_000L)

            val encClient = URLEncoder.encode(customerName.trim().ifBlank { "Cliente" }, StandardCharsets.UTF_8.name())
            val encSeller = URLEncoder.encode(sellerName.trim().ifBlank { "Vendedor Kuota" }, StandardCharsets.UTF_8.name())
            val encConcept = URLEncoder.encode(concept.trim().ifBlank { "Venta" }, StandardCharsets.UTF_8.name())
            val url = "https://kuota.pe/pay/$linkId?m=$totalAmountCents&c=$encClient&v=$encSeller&p=$encConcept"

            val feeCents = (totalAmountCents * 1L) / 100L
            val netAmountCents = totalAmountCents - feeCents

            val newLink = PaymentLink(
                id = linkId,
                sellerId = sellerId,
                sellerName = sellerName,
                customerName = customerName.trim(),
                customerDni = customerDni.trim(),
                customerPhone = customerPhone.trim(),
                customerEmail = customerEmail.trim(),
                concept = concept.trim(),
                quantity = quantity.coerceAtLeast(1),
                unitPriceCents = unitPriceCents,
                amountCents = totalAmountCents,
                feeCents = feeCents,
                netAmountCents = netAmountCents,
                receiptType = receiptType,
                internalNote = internalNote.trim(),
                linkUrl = url,
                status = PaymentLinkStatus.GENERADO,
                expiresAtEpochMillis = expiresAt,
                createdAtEpochMillis = now
            )

            writeInternal(listOf(newLink) + current)
            newLink
        }
    }

    suspend fun markAsSent(linkId: String): PaymentLink? = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val list = readInternal().toMutableList()
            val index = list.indexOfFirst { it.id == linkId }
            if (index == -1) return@withContext null

            val p = list[index]
            if (p.status == PaymentLinkStatus.GENERADO) {
                val updated = p.copy(status = PaymentLinkStatus.ENVIADO)
                list[index] = updated
                writeInternal(list)
                updated
            } else {
                p
            }
        }
    }

    /**
     * PRD 6.4 B: Cuando un link es pagado, la venta se registra automáticamente en Finanzas
     * y queda marcada como COMPROBADA por construcción (PRD 6.7).
     */
    suspend fun markAsPaid(linkId: String): PaymentLink? = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val list = readInternal().toMutableList()
            val index = list.indexOfFirst { it.id == linkId }
            if (index == -1) return@withContext null

            val p = list[index]
            if (p.status == PaymentLinkStatus.PAGADO) return@withContext p

            val now = System.currentTimeMillis()
            val updated = p.copy(
                status = PaymentLinkStatus.PAGADO,
                paidAtEpochMillis = now
            )
            list[index] = updated
            writeInternal(list)

            // Registrar automáticamente como venta comprobada en Finanzas
            journalStore.saveSale(
                existing = null,
                amountCents = p.amountCents,
                now = now,
                isVerified = true,
                productName = "${p.concept} (x${p.quantity})",
                paymentMethod = "Link de Pago",
                source = SaleSource.LINK_PAGO
            )

            // Notificación para el comerciante
            journalStore.addNotification(
                LocalNotificationItem(
                    id = UUID.randomUUID().toString(),
                    type = "PAYMENT_RECEIVED",
                    title = "¡Cobro por Link exitoso!",
                    body = "Tu cliente ${p.customerName} pagó ${PurchaseOffer.formatPen(p.amountCents)} por ${p.concept}. Neto acreditado: ${PurchaseOffer.formatPen(p.netAmountCents)} (Comisión Kuota: 1%).",
                    receivedAtEpochMillis = now
                )
            )

            updated
        }
    }

    suspend fun cancelLink(linkId: String): Boolean = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val list = readInternal().toMutableList()
            val index = list.indexOfFirst { it.id == linkId }
            if (index == -1) return@withContext false

            val updated = list[index].copy(status = PaymentLinkStatus.ANULADO)
            list[index] = updated
            writeInternal(list)
            true
        }
    }

    private fun readInternal(): List<PaymentLink> {
        val file = storageFile
        if (!file.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<List<PaymentLink>>(file.readText())
        }.getOrDefault(emptyList())
    }

    private fun writeInternal(list: List<PaymentLink>) {
        val file = storageFile
        val temp = File(context.filesDir, "kuota_payment_links.tmp")
        temp.writeText(json.encodeToString(list))
        if (!temp.renameTo(file)) {
            file.delete()
            temp.renameTo(file)
        }
    }
}

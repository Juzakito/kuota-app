package com.kouta.app.data.local

import android.content.Context
import com.kouta.app.domain.model.CreditInstallment
import com.kouta.app.domain.model.CreditLevel
import com.kouta.app.domain.model.CreditPurchase
import com.kouta.app.domain.model.PurchaseOffer
import com.kouta.app.domain.model.PurchasePhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

class CreditPurchaseStore(private val context: Context) {

    private val appSettings by lazy { AppSettings(context) }
    private val journalStore by lazy { SalesJournalStore(context) }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    private val storageFile: File
        get() = File(context.filesDir, "kuota_credit_purchases.json")

    private val lock = Any()

    suspend fun getAll(): List<CreditPurchase> = withContext(Dispatchers.IO) {
        synchronized(lock) {
            readInternal()
        }
    }

    suspend fun getActivePurchase(): CreditPurchase? = withContext(Dispatchers.IO) {
        synchronized(lock) {
            readInternal().firstOrNull { it.isOpen }
        }
    }

    suspend fun getById(id: String): CreditPurchase? = withContext(Dispatchers.IO) {
        synchronized(lock) {
            readInternal().firstOrNull { it.id == id }
        }
    }

    suspend fun createPurchase(
        supplierId: String,
        supplierName: String,
        supplierPhone: String,
        supplierPaymentDetails: String,
        buyerId: String,
        buyerName: String,
        buyerPhone: String,
        totalAmountCents: Long,
        creditLevelNumber: Int,
        downPaymentPercent: Int
    ): CreditPurchase = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val current = readInternal()
            // Check Rule 3.2: Only 1 open purchase at a time
            val existingOpen = current.firstOrNull { it.isOpen }
            if (existingOpen != null) {
                return@withContext existingOpen
            }

            val downPaymentCents = (totalAmountCents * downPaymentPercent) / 100
            val financedCents = totalAmountCents - downPaymentCents
            val kuotaFeeCents = (totalAmountCents * 2) / 100
            val supplierPayout48hCents = ((totalAmountCents * 98) / 100) - downPaymentCents

            val newPurchase = CreditPurchase(
                id = UUID.randomUUID().toString(),
                supplierId = supplierId,
                supplierName = supplierName,
                supplierPhone = supplierPhone,
                supplierPaymentDetails = supplierPaymentDetails,
                buyerId = buyerId,
                buyerName = buyerName,
                buyerPhone = buyerPhone,
                totalAmountCents = totalAmountCents,
                creditLevelNumber = creditLevelNumber,
                downPaymentPercent = downPaymentPercent,
                downPaymentCents = downPaymentCents,
                financedAmountCents = financedCents,
                supplierPayout48hCents = supplierPayout48hCents.coerceAtLeast(0L),
                kuotaFeeCents = kuotaFeeCents,
                phase = PurchasePhase.PHASE_1_DOWN_PAYMENT_PENDING,
                createdAtEpochMillis = System.currentTimeMillis()
            )

            writeInternal(listOf(newPurchase) + current)
            newPurchase
        }
    }

    suspend fun submitDownPaymentReceipt(purchaseId: String, receiptUri: String): CreditPurchase? = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val list = readInternal().toMutableList()
            val index = list.indexOfFirst { it.id == purchaseId }
            if (index == -1) return@withContext null

            val updated = list[index].copy(
                receiptUri = receiptUri,
                receiptUploadedAtEpochMillis = System.currentTimeMillis(),
                phase = PurchasePhase.PHASE_1_DOWN_PAYMENT_SUBMITTED
            )
            list[index] = updated
            writeInternal(list)
            updated
        }
    }

    suspend fun supplierConfirmDownPayment(purchaseId: String): CreditPurchase? = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val list = readInternal().toMutableList()
            val index = list.indexOfFirst { it.id == purchaseId }
            if (index == -1) return@withContext null

            val updated = list[index].copy(
                confirmedBySupplierAtEpochMillis = System.currentTimeMillis(),
                phase = PurchasePhase.PHASE_2_INSTALLMENTS_SELECTION
            )
            list[index] = updated
            writeInternal(list)
            updated
        }
    }

    suspend fun confirmInstallmentPlan(
        purchaseId: String,
        termDays: Int,
        installmentCount: Int
    ): CreditPurchase? = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val list = readInternal().toMutableList()
            val index = list.indexOfFirst { it.id == purchaseId }
            if (index == -1) return@withContext null

            val p = list[index]
            val count = installmentCount.coerceAtLeast(1)
            val baseAmount = p.financedAmountCents / count
            val remainder = p.financedAmountCents % count
            val now = System.currentTimeMillis()
            val intervalMillis = (termDays.toLong() * 24L * 60L * 60L * 1000L) / count

            val installments = (1..count).map { num ->
                val amount = if (num == 1) baseAmount + remainder else baseAmount
                val due = now + (num * intervalMillis)
                CreditInstallment(
                    number = num,
                    amountCents = amount,
                    dueEpochMillis = due,
                    isPaid = false
                )
            }

            val updated = p.copy(
                selectedTermDays = termDays,
                installmentCount = count,
                installments = installments,
                phase = PurchasePhase.ACTIVE_IN_REPAYMENT
            )
            list[index] = updated
            writeInternal(list)

            // Reduce available credit line by financed amount (PRD 6.2 item 5)
            val level = CreditLevel.fromNumber(appSettings.creditLevelNumber)
            val newAvailable = (level.maxLineCents - p.financedAmountCents).coerceAtLeast(0L)
            appSettings.availableCreditLineCents = newAvailable
            journalStore.updateAvailableLine(newAvailable)
            journalStore.addNotification(
                LocalNotificationItem(
                    id = UUID.randomUUID().toString(),
                    type = "PURCHASE",
                    title = "¡Plan de cuotas activado!",
                    body = "Tu compra en ${p.supplierName} por ${PurchaseOffer.formatPen(p.totalAmountCents)} quedó activa en $count cuotas. Kuota desembolsará en 48h.",
                    receivedAtEpochMillis = now
                )
            )

            updated
        }
    }

    suspend fun payInstallment(purchaseId: String, installmentNumber: Int): CreditPurchase? = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val list = readInternal().toMutableList()
            val index = list.indexOfFirst { it.id == purchaseId }
            if (index == -1) return@withContext null

            val p = list[index]
            val now = System.currentTimeMillis()
            val updatedInstallments = p.installments.map { inst ->
                if (inst.number == installmentNumber) {
                    inst.copy(isPaid = true, paidAtEpochMillis = now)
                } else {
                    inst
                }
            }

            val allPaid = updatedInstallments.all { it.isPaid }
            val updated = p.copy(
                installments = updatedInstallments,
                phase = if (allPaid) PurchasePhase.COMPLETED else p.phase,
                completedAtEpochMillis = if (allPaid) now else p.completedAtEpochMillis
            )
            list[index] = updated
            writeInternal(list)

            // Restore available credit line as installments are repaid
            val level = CreditLevel.fromNumber(appSettings.creditLevelNumber)
            val remainingDebt = updatedInstallments.filter { !it.isPaid }.sumOf { it.amountCents }
            val newAvailable = (level.maxLineCents - remainingDebt).coerceAtLeast(0L)
            appSettings.availableCreditLineCents = newAvailable
            journalStore.updateAvailableLine(newAvailable)

            if (allPaid) {
                journalStore.addNotification(
                    LocalNotificationItem(
                        id = UUID.randomUUID().toString(),
                        type = "PURCHASE",
                        title = "¡Crédito liquidado al 100%!",
                        body = "Completaste todas las cuotas de tu compra en ${p.supplierName}. Tu línea de crédito se ha restablecido a ${PurchaseOffer.formatPen(level.maxLineCents)}.",
                        receivedAtEpochMillis = now
                    )
                )
            } else {
                journalStore.addNotification(
                    LocalNotificationItem(
                        id = UUID.randomUUID().toString(),
                        type = "PURCHASE",
                        title = "Cuota #$installmentNumber pagada",
                        body = "Tu pago se registró con éxito. Tu línea de crédito disponible aumentó a ${PurchaseOffer.formatPen(newAvailable)}.",
                        receivedAtEpochMillis = now
                    )
                )
            }

            updated
        }
    }

    suspend fun cancelPurchase(purchaseId: String): Boolean = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val list = readInternal().toMutableList()
            val index = list.indexOfFirst { it.id == purchaseId }
            if (index == -1) return@withContext false

            val updated = list[index].copy(
                phase = PurchasePhase.CANCELLED
            )
            list[index] = updated
            writeInternal(list)

            // Restore credit line if needed
            val level = CreditLevel.fromNumber(appSettings.creditLevelNumber)
            appSettings.availableCreditLineCents = level.maxLineCents
            journalStore.updateAvailableLine(level.maxLineCents)

            true
        }
    }

    private fun readInternal(): List<CreditPurchase> {
        val file = storageFile
        if (!file.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<List<CreditPurchase>>(file.readText())
        }.getOrDefault(emptyList())
    }

    private fun writeInternal(list: List<CreditPurchase>) {
        val file = storageFile
        val temp = File(context.filesDir, "kuota_credit_purchases.tmp")
        temp.writeText(json.encodeToString(list))
        if (!temp.renameTo(file)) {
            file.delete()
            temp.renameTo(file)
        }
    }
}

package com.kouta.app.data.local

import android.content.Context
import com.kouta.app.domain.model.CreditInstallment
import com.kouta.app.domain.model.CreditPurchase
import com.kouta.app.domain.model.PurchasePhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

class CreditPurchaseStore(private val context: Context) {

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
            updated
        }
    }

    suspend fun payInstallment(purchaseId: String, installmentNumber: Int): CreditPurchase? = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val list = readInternal().toMutableList()
            val index = list.indexOfFirst { it.id == purchaseId }
            if (index == -1) return@withContext null

            val p = list[index]
            val updatedInstallments = p.installments.map { inst ->
                if (inst.number == installmentNumber) {
                    inst.copy(isPaid = true, paidAtEpochMillis = System.currentTimeMillis())
                } else {
                    inst
                }
            }

            val allPaid = updatedInstallments.all { it.isPaid }
            val updated = p.copy(
                installments = updatedInstallments,
                phase = if (allPaid) PurchasePhase.COMPLETED else p.phase,
                completedAtEpochMillis = if (allPaid) System.currentTimeMillis() else p.completedAtEpochMillis
            )
            list[index] = updated
            writeInternal(list)
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

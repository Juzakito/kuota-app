package com.kuota.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.kuota.core.model.CreditPurchase
import com.kuota.core.model.Installment
import com.kuota.core.model.InstallmentScheduleItem
import com.kuota.core.model.PurchaseQuote
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface PurchaseRepository {
    suspend fun quotePurchase(totalAmountCents: Long, installmentsCount: Int): Result<PurchaseQuote>
    suspend fun createPurchase(
        supplierId: String,
        supplierName: String,
        totalAmountCents: Long,
        installmentsCount: Int,
        notes: String? = null
    ): Result<CreditPurchase>
    fun observePurchase(purchaseId: String): Flow<CreditPurchase?>
    suspend fun providerApprovePurchase(purchaseId: String): Result<Boolean>
    suspend fun buyerConfirmPurchase(purchaseId: String): Result<Boolean>
    suspend fun cancelPurchase(purchaseId: String, reason: String? = null): Result<Boolean>
    fun observeUserInstallments(buyerUid: String): Flow<List<Installment>>
    fun observeUserPurchases(buyerUid: String): Flow<List<CreditPurchase>>
}

@Singleton
class FirebasePurchaseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    private val auth: FirebaseAuth
) : PurchaseRepository {

    override suspend fun quotePurchase(
        totalAmountCents: Long,
        installmentsCount: Int
    ): Result<PurchaseQuote> = runCatching {
        val payload = mapOf(
            "totalAmountCents" to totalAmountCents,
            "installmentsCount" to installmentsCount
        )

        val task = functions.getHttpsCallable("quotePurchase").call(payload).await()
        val data = task.getData() as? Map<*, *> ?: emptyMap<String, Any>()
        val quoteMap = data["quote"] as? Map<*, *> ?: emptyMap<String, Any>()

        parseQuoteMap(quoteMap)
    }

    override suspend fun createPurchase(
        supplierId: String,
        supplierName: String,
        totalAmountCents: Long,
        installmentsCount: Int,
        notes: String?
    ): Result<CreditPurchase> = runCatching {
        val payload = mapOf(
            "supplierId" to supplierId,
            "supplierName" to supplierName,
            "totalAmountCents" to totalAmountCents,
            "installmentsCount" to installmentsCount,
            "notes" to notes
        )

        val task = functions.getHttpsCallable("createPurchase").call(payload).await()
        val data = task.getData() as? Map<*, *> ?: emptyMap<String, Any>()

        val purchaseId = (data["purchaseId"] as? String).orEmpty()
        val status = (data["status"] as? String) ?: "QUOTED"
        val expiresAt = (data["expiresAtEpochMillis"] as? Number)?.toLong() ?: 0L
        val quoteMap = data["quote"] as? Map<*, *> ?: emptyMap<String, Any>()
        val quote = parseQuoteMap(quoteMap)

        CreditPurchase(
            purchaseId = purchaseId,
            buyerUid = auth.currentUser?.uid.orEmpty(),
            providerUid = supplierId,
            supplierName = supplierName,
            notes = notes,
            status = status,
            totalAmountCents = quote.totalAmountCents,
            downPaymentCents = quote.downPaymentCents,
            downPaymentPercent = quote.downPaymentPercent,
            financedAmountCents = quote.financedAmountCents,
            buyerFeeCents = quote.buyerFeeCents,
            totalDebtCents = quote.totalDebtCents,
            installmentsCount = quote.installmentsCount,
            quote = quote,
            expiresAtEpochMillis = expiresAt,
            createdAtEpochMillis = System.currentTimeMillis()
        )
    }

    override fun observePurchase(purchaseId: String): Flow<CreditPurchase?> = callbackFlow {
        if (purchaseId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("purchases")
            .document(purchaseId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data ?: emptyMap<String, Any>()
                    val quoteMap = data["quote"] as? Map<*, *> ?: emptyMap<String, Any>()
                    val quote = parseQuoteMap(quoteMap)

                    val expiresAt = when (val exp = data["expiresAt"]) {
                        is com.google.firebase.Timestamp -> exp.toDate().time
                        is Number -> exp.toLong()
                        else -> 0L
                    }

                    val purchase = CreditPurchase(
                        purchaseId = snapshot.id,
                        buyerUid = (data["buyerUid"] as? String).orEmpty(),
                        providerUid = (data["providerUid"] as? String).orEmpty(),
                        supplierName = (data["supplierName"] as? String).orEmpty(),
                        notes = data["notes"] as? String,
                        status = (data["status"] as? String) ?: "QUOTED",
                        totalAmountCents = (data["totalAmountCents"] as? Number)?.toLong() ?: 0L,
                        downPaymentCents = (data["downPaymentCents"] as? Number)?.toLong() ?: 0L,
                        downPaymentPercent = (data["downPaymentPercent"] as? Number)?.toInt() ?: 30,
                        financedAmountCents = (data["financedAmountCents"] as? Number)?.toLong() ?: 0L,
                        buyerFeeCents = (data["buyerFeeCents"] as? Number)?.toLong() ?: 0L,
                        totalDebtCents = (data["totalDebtCents"] as? Number)?.toLong() ?: 0L,
                        installmentsCount = (data["installmentsCount"] as? Number)?.toInt() ?: 4,
                        providerFeeCents = (data["providerFeeCents"] as? Number)?.toLong() ?: 0L,
                        providerNetCents = (data["providerNetCents"] as? Number)?.toLong() ?: 0L,
                        providerAdvanceCents = (data["providerAdvanceCents"] as? Number)?.toLong() ?: 0L,
                        providerDeferredCents = (data["providerDeferredCents"] as? Number)?.toLong() ?: 0L,
                        quote = quote,
                        expiresAtEpochMillis = expiresAt,
                        createdAtEpochMillis = (data["createdAtEpochMillis"] as? Number)?.toLong() ?: 0L
                    )
                    trySend(purchase)
                } else {
                    trySend(null)
                }
            }

        awaitClose { registration.remove() }
    }

    override suspend fun providerApprovePurchase(purchaseId: String): Result<Boolean> = runCatching {
        val payload = mapOf("purchaseId" to purchaseId)
        val task = functions.getHttpsCallable("providerApprovePurchase").call(payload).await()
        val data = task.getData() as? Map<*, *> ?: emptyMap<String, Any>()
        (data["success"] as? Boolean) ?: false
    }

    override suspend fun buyerConfirmPurchase(purchaseId: String): Result<Boolean> = runCatching {
        val payload = mapOf("purchaseId" to purchaseId)
        val task = functions.getHttpsCallable("buyerConfirmPurchase").call(payload).await()
        val data = task.getData() as? Map<*, *> ?: emptyMap<String, Any>()
        (data["success"] as? Boolean) ?: false
    }

    override suspend fun cancelPurchase(purchaseId: String, reason: String?): Result<Boolean> = runCatching {
        val payload = mapOf("purchaseId" to purchaseId, "reason" to (reason ?: "Cancelado"))
        val task = functions.getHttpsCallable("cancelPurchase").call(payload).await()
        val data = task.getData() as? Map<*, *> ?: emptyMap<String, Any>()
        (data["success"] as? Boolean) ?: false
    }

    override fun observeUserInstallments(buyerUid: String): Flow<List<Installment>> = callbackFlow {
        if (buyerUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("installments")
            .whereEqualTo("buyerUid", buyerUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        Installment(
                            installmentId = doc.id,
                            purchaseId = (data["purchaseId"] as? String).orEmpty(),
                            buyerUid = (data["buyerUid"] as? String).orEmpty(),
                            providerUid = (data["providerUid"] as? String).orEmpty(),
                            supplierName = (data["supplierName"] as? String).orEmpty(),
                            installmentNumber = (data["installmentNumber"] as? Number)?.toInt() ?: 1,
                            totalInstallments = (data["totalInstallments"] as? Number)?.toInt() ?: 4,
                            dueDate = (data["dueDate"] as? String).orEmpty(),
                            amountCents = (data["amountCents"] as? Number)?.toLong() ?: 0L,
                            status = (data["status"] as? String) ?: "PENDING",
                            paidAt = data["paidAt"] as? String
                        )
                    }.sortedBy { it.dueDate }

                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    override fun observeUserPurchases(buyerUid: String): Flow<List<CreditPurchase>> = callbackFlow {
        if (buyerUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("purchases")
            .whereEqualTo("buyerUid", buyerUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        CreditPurchase(
                            purchaseId = doc.id,
                            buyerUid = (data["buyerUid"] as? String).orEmpty(),
                            providerUid = (data["providerUid"] as? String).orEmpty(),
                            supplierName = (data["supplierName"] as? String).orEmpty(),
                            notes = data["notes"] as? String,
                            status = (data["status"] as? String) ?: "QUOTED",
                            totalAmountCents = (data["totalAmountCents"] as? Number)?.toLong() ?: 0L,
                            downPaymentCents = (data["downPaymentCents"] as? Number)?.toLong() ?: 0L,
                            downPaymentPercent = (data["downPaymentPercent"] as? Number)?.toInt() ?: 30,
                            financedAmountCents = (data["financedAmountCents"] as? Number)?.toLong() ?: 0L,
                            buyerFeeCents = (data["buyerFeeCents"] as? Number)?.toLong() ?: 0L,
                            totalDebtCents = (data["totalDebtCents"] as? Number)?.toLong() ?: 0L,
                            installmentsCount = (data["installmentsCount"] as? Number)?.toInt() ?: 4,
                            expiresAtEpochMillis = when (val exp = data["expiresAt"]) {
                                is com.google.firebase.Timestamp -> exp.toDate().time
                                is Number -> exp.toLong()
                                else -> 0L
                            }
                        )
                    }
                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    private fun parseQuoteMap(map: Map<*, *>): PurchaseQuote {
        val installmentsList = (map["installments"] as? List<*>)?.mapNotNull { item ->
            val iMap = item as? Map<*, *> ?: return@mapNotNull null
            InstallmentScheduleItem(
                installmentNumber = (iMap["installmentNumber"] as? Number)?.toInt() ?: 1,
                dueDate = (iMap["dueDate"] as? String).orEmpty(),
                amountCents = (iMap["amountCents"] as? Number)?.toLong() ?: 0L,
                status = (iMap["status"] as? String) ?: "PENDING"
            )
        } ?: emptyList()

        return PurchaseQuote(
            totalAmountCents = (map["totalAmountCents"] as? Number)?.toLong() ?: 0L,
            downPaymentPercent = (map["downPaymentPercent"] as? Number)?.toInt() ?: 30,
            downPaymentCents = (map["downPaymentCents"] as? Number)?.toLong() ?: 0L,
            financedAmountCents = (map["financedAmountCents"] as? Number)?.toLong() ?: 0L,
            buyerFeePercent = (map["buyerFeePercent"] as? Number)?.toInt() ?: 5,
            buyerFeeCents = (map["buyerFeeCents"] as? Number)?.toLong() ?: 0L,
            totalDebtCents = (map["totalDebtCents"] as? Number)?.toLong() ?: 0L,
            installmentsCount = (map["installmentsCount"] as? Number)?.toInt() ?: 4,
            installments = installmentsList,
            providerFeePercent = (map["providerFeePercent"] as? Number)?.toInt() ?: 2,
            providerFeeCents = (map["providerFeeCents"] as? Number)?.toLong() ?: 0L,
            providerNetCents = (map["providerNetCents"] as? Number)?.toLong() ?: 0L,
            providerAdvancePercent = (map["providerAdvancePercent"] as? Number)?.toInt() ?: 60,
            providerAdvanceCents = (map["providerAdvanceCents"] as? Number)?.toLong() ?: 0L,
            providerDeferredCents = (map["providerDeferredCents"] as? Number)?.toLong() ?: 0L,
            calculatedAt = (map["calculatedAt"] as? String).orEmpty()
        )
    }
}

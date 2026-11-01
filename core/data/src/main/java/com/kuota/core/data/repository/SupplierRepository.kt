package com.kuota.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.kuota.core.model.CreditPurchase
import com.kuota.core.model.ExpectedPayout
import com.kuota.core.model.PaymentLink
import com.kuota.core.model.ProviderProfile
import com.kuota.core.model.SupplierPayoutSummary
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface SupplierRepository {
    suspend fun generateSupplierQr(amountCents: Long? = null): Result<String>
    suspend fun createPaymentLink(
        amountCents: Long,
        clientPhone: String,
        clientDni: String? = null,
        concept: String? = null
    ): Result<PaymentLink>
    suspend fun getExpectedEarnings(): Result<SupplierPayoutSummary>
    fun observeSupplierPayouts(providerUid: String): Flow<List<ExpectedPayout>>
    fun observePublicProviders(categoryFilter: String? = null, query: String? = null): Flow<List<ProviderProfile>>
    fun observePaymentLinks(providerUid: String): Flow<List<PaymentLink>>
    fun observePendingPurchasesForApproval(providerUid: String): Flow<List<CreditPurchase>>
}

@Singleton
class FirebaseSupplierRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    private val auth: FirebaseAuth
) : SupplierRepository {

    override suspend fun generateSupplierQr(amountCents: Long?): Result<String> = runCatching {
        val payload = mutableMapOf<String, Any>()
        if (amountCents != null && amountCents > 0) {
            payload["amountCents"] = amountCents
        }

        val task = functions.getHttpsCallable("generateSupplierQr").call(payload).await()
        val data = task.getData() as? Map<*, *> ?: emptyMap<String, Any>()
        (data["signedQr"] as? String).orEmpty()
    }

    override suspend fun createPaymentLink(
        amountCents: Long,
        clientPhone: String,
        clientDni: String?,
        concept: String?
    ): Result<PaymentLink> = runCatching {
        val payload = mapOf(
            "amountCents" to amountCents,
            "clientPhone" to clientPhone,
            "clientDni" to clientDni,
            "concept" to concept
        )

        val task = functions.getHttpsCallable("createPaymentLink").call(payload).await()
        val data = task.getData() as? Map<*, *> ?: emptyMap<String, Any>()

        PaymentLink(
            linkId = (data["linkId"] as? String).orEmpty(),
            providerUid = auth.currentUser?.uid.orEmpty(),
            amountCents = (data["amountCents"] as? Number)?.toLong() ?: amountCents,
            feeCents = (data["feeCents"] as? Number)?.toLong() ?: 0L,
            netCents = (data["netCents"] as? Number)?.toLong() ?: 0L,
            clientPhone = clientPhone,
            clientDni = clientDni,
            concept = concept,
            shortUrl = (data["shortUrl"] as? String).orEmpty(),
            whatsappUrl = (data["whatsappUrl"] as? String).orEmpty(),
            status = "ACTIVE",
            expiresAt = (data["expiresAt"] as? String).orEmpty(),
            createdAt = System.currentTimeMillis().toString()
        )
    }

    override suspend fun getExpectedEarnings(): Result<SupplierPayoutSummary> = runCatching {
        val task = functions.getHttpsCallable("getSupplierExpectedEarnings").call().await()
        val data = task.getData() as? Map<*, *> ?: emptyMap<String, Any>()

        val rawPayouts = (data["pendingPayouts"] as? List<*>)?.mapNotNull { item ->
            val pMap = item as? Map<*, *> ?: return@mapNotNull null
            ExpectedPayout(
                payoutId = (pMap["payoutId"] as? String).orEmpty(),
                purchaseId = (pMap["purchaseId"] as? String).orEmpty(),
                amountCents = (pMap["amountCents"] as? Number)?.toLong() ?: 0L,
                type = (pMap["type"] as? String) ?: "ADVANCE_60",
                dueDate = (pMap["dueDate"] as? String).orEmpty(),
                status = (pMap["status"] as? String) ?: "PENDING",
                bankAccountMasked = (pMap["bankAccountMasked"] as? String) ?: "BCP Soles ****4812"
            )
        } ?: emptyList()

        SupplierPayoutSummary(
            totalPendingAdvanceCents = (data["totalPendingAdvanceCents"] as? Number)?.toLong() ?: 0L,
            totalPendingDeferredCents = (data["totalPendingDeferredCents"] as? Number)?.toLong() ?: 0L,
            totalExpectedCents = (data["totalExpectedCents"] as? Number)?.toLong() ?: 0L,
            pendingPayouts = rawPayouts
        )
    }

    override fun observeSupplierPayouts(providerUid: String): Flow<List<ExpectedPayout>> = callbackFlow {
        if (providerUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("payouts")
            .whereEqualTo("providerUid", providerUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        ExpectedPayout(
                            payoutId = doc.id,
                            purchaseId = (data["purchaseId"] as? String).orEmpty(),
                            amountCents = (data["amountCents"] as? Number)?.toLong() ?: 0L,
                            type = (data["type"] as? String) ?: "ADVANCE_60",
                            dueDate = (data["dueDate"] as? String).orEmpty(),
                            status = (data["status"] as? String) ?: "PENDING",
                            bankAccountMasked = (data["bankAccountMasked"] as? String) ?: "BCP Soles ****4812"
                        )
                    }.sortedBy { it.dueDate }

                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    override fun observePublicProviders(
        categoryFilter: String?,
        query: String?
    ): Flow<List<ProviderProfile>> = callbackFlow {
        val registration = firestore.collection("providers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    var list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        ProviderProfile(
                            providerUid = doc.id,
                            businessName = (data["businessName"] as? String).orEmpty(),
                            category = (data["category"] as? String) ?: "General",
                            ruc = data["ruc"] as? String,
                            phone = (data["phone"] as? String).orEmpty(),
                            address = (data["address"] as? String).orEmpty(),
                            district = (data["district"] as? String) ?: "Lima",
                            department = (data["department"] as? String) ?: "Lima",
                            isVerified = (data["isVerified"] as? Boolean) ?: true,
                            staticQrPayload = (data["staticQrPayload"] as? String).orEmpty(),
                            activePlan = (data["activePlan"] as? String) ?: "BASIC"
                        )
                    }

                    if (!categoryFilter.isNullOrBlank() && categoryFilter != "Todos") {
                        list = list.filter { it.category.equals(categoryFilter, ignoreCase = true) }
                    }

                    if (!query.isNullOrBlank()) {
                        val cleanQ = query.trim().lowercase()
                        list = list.filter {
                            it.businessName.lowercase().contains(cleanQ) ||
                            it.district.lowercase().contains(cleanQ) ||
                            it.category.lowercase().contains(cleanQ)
                        }
                    }

                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    override fun observePaymentLinks(providerUid: String): Flow<List<PaymentLink>> = callbackFlow {
        if (providerUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("payment_links")
            .whereEqualTo("providerUid", providerUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        PaymentLink(
                            linkId = doc.id,
                            providerUid = (data["providerUid"] as? String).orEmpty(),
                            supplierName = (data["supplierName"] as? String).orEmpty(),
                            amountCents = (data["amountCents"] as? Number)?.toLong() ?: 0L,
                            feeCents = (data["feeCents"] as? Number)?.toLong() ?: 0L,
                            netCents = (data["netCents"] as? Number)?.toLong() ?: 0L,
                            clientPhone = (data["clientPhone"] as? String).orEmpty(),
                            clientDni = data["clientDni"] as? String,
                            concept = data["concept"] as? String,
                            shortUrl = (data["shortUrl"] as? String).orEmpty(),
                            whatsappUrl = (data["whatsappUrl"] as? String).orEmpty(),
                            status = (data["status"] as? String) ?: "ACTIVE"
                        )
                    }
                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    override fun observePendingPurchasesForApproval(providerUid: String): Flow<List<CreditPurchase>> = callbackFlow {
        if (providerUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("purchases")
            .whereEqualTo("providerUid", providerUid)
            .whereEqualTo("status", "QUOTED")
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
}

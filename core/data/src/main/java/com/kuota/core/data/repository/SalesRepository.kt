package com.kuota.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.kuota.core.model.ProductItem
import com.kuota.core.model.SaleRecord
import com.kuota.core.model.SaleSource
import com.kuota.core.model.SalesMonthSummary
import com.kuota.core.model.ScoreEvaluation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface SalesRepository {
    suspend fun uploadSalesBatch(sales: List<SaleRecord>): Result<Int>
    fun observeRecentSales(uid: String): Flow<List<SaleRecord>>
    fun observeSalesSummary(uid: String): Flow<SalesMonthSummary>
    suspend fun recomputeScore(): Result<ScoreEvaluation>
    fun observeUserProducts(uid: String): Flow<List<ProductItem>>
    suspend fun addProduct(product: ProductItem): Result<String>
    suspend fun registerManualSale(amountCents: Long, productName: String?, isOcr: Boolean): Result<SaleRecord>
}

@Singleton
class FirebaseSalesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    private val auth: FirebaseAuth
) : SalesRepository {

    override suspend fun uploadSalesBatch(sales: List<SaleRecord>): Result<Int> = runCatching {
        if (sales.isEmpty()) return@runCatching 0

        val payload = mapOf(
            "sales" to sales.map { sale ->
                mapOf(
                    "amountCents" to sale.amountCents,
                    "timestampMs" to sale.timestampMs,
                    "source" to sale.source.name,
                    "isVerified" to sale.isVerified,
                    "txHash" to sale.txHash,
                    "customerSnippet" to sale.customerSnippet,
                    "productName" to sale.productName
                )
            }
        )

        val task = functions.getHttpsCallable("registerSalesBatch").call(payload).await()
        val data = task.getData() as? Map<*, *> ?: emptyMap<String, Any>()
        val inserted = (data["insertedCount"] as? Number)?.toInt() ?: 0
        inserted
    }

    override fun observeRecentSales(uid: String): Flow<List<SaleRecord>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val query = firestore.collection("users")
            .document(uid)
            .collection("sales")
            .orderBy("timestampMs", Query.Direction.DESCENDING)
            .limit(40)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val sales = snapshot?.documents?.mapNotNull { doc ->
                val amountCents = doc.getLong("amountCents") ?: 0L
                val timestampMs = doc.getLong("timestampMs") ?: 0L
                val sourceStr = doc.getString("source") ?: "MANUAL_DECLARED"
                val source = runCatching { SaleSource.valueOf(sourceStr) }.getOrDefault(SaleSource.MANUAL_DECLARED)
                val isVerified = doc.getBoolean("isVerified") ?: false
                val txHash = doc.getString("txHash") ?: doc.id
                val customerSnippet = doc.getString("customerSnippet")
                val productName = doc.getString("productName")
                val category = doc.getString("category")

                SaleRecord(
                    id = doc.id,
                    amountCents = amountCents,
                    timestampMs = timestampMs,
                    source = source,
                    isVerified = isVerified,
                    txHash = txHash,
                    customerSnippet = customerSnippet,
                    productName = productName,
                    category = category
                )
            }.orEmpty()

            trySend(sales)
        }

        awaitClose { listener.remove() }
    }

    override fun observeSalesSummary(uid: String): Flow<SalesMonthSummary> = callbackFlow {
        if (uid.isBlank()) {
            trySend(SalesMonthSummary())
            close()
            return@callbackFlow
        }

        val summaryDoc = firestore.collection("users")
            .document(uid)
            .collection("summary")
            .document("current")

        val listener = summaryDoc.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                trySend(SalesMonthSummary())
                return@addSnapshotListener
            }

            val data = snapshot.data ?: emptyMap<String, Any>()
            val totalMonthCents = (data["currentMonthSalesCents"] as? Number)?.toLong() ?: 0L
            val totalCount = (data["salesCount"] as? Number)?.toInt() ?: 0

            trySend(
                SalesMonthSummary(
                    totalVerifiedCents = totalMonthCents,
                    totalUnverifiedCents = 0L,
                    totalSalesCents = totalMonthCents,
                    verifiedCount = totalCount,
                    unverifiedCount = 0
                )
            )
        }

        awaitClose { listener.remove() }
    }

    override suspend fun recomputeScore(): Result<ScoreEvaluation> = runCatching {
        val task = functions.getHttpsCallable("computeScore").call().await()
        val data = task.getData() as? Map<*, *> ?: emptyMap<String, Any>()

        val score = (data["score"] as? Number)?.toInt() ?: 350
        val level = (data["level"] as? Number)?.toInt() ?: 1
        val previousScore = (data["previousScore"] as? Number)?.toInt() ?: 350
        val previousLevel = (data["previousLevel"] as? Number)?.toInt() ?: 1
        val creditLimitCents = (data["creditLimitCents"] as? Number)?.toLong() ?: 70_000L
        val policyVersion = data["policyVersion"] as? String ?: "scoring_v2_2026"
        val flags = (data["flags"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

        ScoreEvaluation(
            score = score,
            level = level,
            previousScore = previousScore,
            previousLevel = previousLevel,
            components = emptyMap(),
            flags = flags,
            evaluatedAtMs = System.currentTimeMillis(),
            policyVersion = policyVersion,
            creditLimitCents = creditLimitCents
        )
    }

    override fun observeUserProducts(uid: String): Flow<List<ProductItem>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(uid)
            .collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val products = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val priceCents = doc.getLong("priceCents") ?: 0L
                    val category = doc.getString("category") ?: "General"
                    val isAvailable = doc.getBoolean("isAvailable") ?: true

                    ProductItem(
                        id = doc.id,
                        name = name,
                        priceCents = priceCents,
                        category = category,
                        isAvailable = isAvailable
                    )
                }

                trySend(products)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun addProduct(product: ProductItem): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
        val productsCol = firestore.collection("users").document(uid).collection("products")
        val docRef = if (product.id.isNotBlank()) {
            productsCol.document(product.id)
        } else {
            productsCol.document()
        }

        val data: Map<String, Any> = mapOf(
            "name" to product.name,
            "priceCents" to product.priceCents,
            "category" to product.category,
            "isAvailable" to product.isAvailable,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        docRef.set(data).await()
        docRef.id
    }

    override suspend fun registerManualSale(
        amountCents: Long,
        productName: String?,
        isOcr: Boolean
    ): Result<SaleRecord> = runCatching {
        val source = if (isOcr) SaleSource.MANUAL_OCR else SaleSource.MANUAL_DECLARED
        val now = System.currentTimeMillis()
        val txHash = "manual_${UUID.randomUUID().toString().replace("-", "")}"

        val record = SaleRecord(
            id = txHash,
            amountCents = amountCents,
            timestampMs = now,
            source = source,
            isVerified = isOcr,
            txHash = txHash,
            customerSnippet = if (isOcr) "Comprobante OCR" else "Declaración manual",
            productName = productName ?: "Venta rápida"
        )

        uploadSalesBatch(listOf(record)).getOrThrow()
        record
    }
}

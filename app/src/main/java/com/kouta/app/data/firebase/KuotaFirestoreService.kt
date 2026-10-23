package com.kouta.app.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.kouta.app.data.local.AppSettings
import com.kouta.app.data.local.LocalBusinessProfile
import com.kouta.app.data.local.ManualSale
import com.kouta.app.data.local.SaleSyncStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Servicio de base de datos real con Cloud Firestore (kuota-android-d049f).
 * Gestiona autenticación, sincronización de ventas, perfil de comercio MYPE,
 * solicitudes KYC y Libro de Reclamaciones con persistencia local SQLite y réplica en la nube.
 */
object KuotaFirestoreService {

    private const val TAG = "KuotaFirestoreService"
    private var isInitialized = false

    private fun getDb(): FirebaseFirestore {
        val db = FirebaseFirestore.getInstance()
        if (!isInitialized) {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
            isInitialized = true
        }
        return db
    }

    /**
     * Obtiene el ID estable del comerciante MYPE. Si no está autenticado,
     * realiza inicio de sesión anónimo transparente con Firebase Auth.
     */
    suspend fun getOrInitMerchantId(context: Context): String {
        val auth = FirebaseAuth.getInstance()
        val current = auth.currentUser
        if (current != null) return current.uid

        return try {
            val authResult = auth.signInAnonymously().await()
            val uid = authResult.user?.uid ?: "mype_${System.currentTimeMillis()}"
            AppSettings(context).linkedPhone?.let { phone ->
                // Asocia el teléfono del comerciante en Firestore
                getDb().collection("merchants").document(uid).set(
                    mapOf("phone" to phone, "authType" to "ANONYMOUS_LINKED"),
                    SetOptions.merge()
                )
            }
            uid
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo autenticar anónimamente, usando ID persistente local: ${e.message}")
            AppSettings(context).pullToken
        }
    }

    /**
     * Guarda o actualiza una venta directamente en Cloud Firestore (/merchants/{id}/sales/{saleId}).
     * La escritura dispara automáticamente el trigger onSaleUpdated hacia Supabase.
     */
    suspend fun saveSale(context: Context, sale: ManualSale): Boolean {
        return try {
            val db = getDb()
            val merchantId = getOrInitMerchantId(context)
            val docRef = db.collection("merchants")
                .document(merchantId)
                .collection("sales")
                .document(sale.id)

            val payload = hashMapOf(
                "saleId" to sale.id,
                "merchantId" to merchantId,
                "totalAmountCents" to sale.amountCents,
                "status" to "PENDING",
                "currency" to "PEN",
                "revision" to sale.revision,
                "recordedAtEpochMillis" to sale.recordedAtEpochMillis,
                "deletedAtEpochMillis" to sale.deletedAtEpochMillis,
                "createdAt" to com.google.firebase.Timestamp(Date(sale.recordedAtEpochMillis)),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            docRef.set(payload, SetOptions.merge()).await()
            Log.i(TAG, "Venta ${sale.id} guardada exitosamente en Firestore para merchant $merchantId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando venta ${sale.id} en Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * Elimina una venta directamente en Cloud Firestore.
     */
    suspend fun deleteSale(context: Context, saleId: String): Boolean {
        return try {
            val db = getDb()
            val merchantId = getOrInitMerchantId(context)
            db.collection("merchants")
                .document(merchantId)
                .collection("sales")
                .document(saleId)
                .delete()
                .await()
            Log.i(TAG, "Venta $saleId eliminada en Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando venta $saleId en Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * Observa el flujo de ventas en tiempo real desde Cloud Firestore.
     * Funciona online y offline gracias a la caché persistente de Firestore.
     */
    fun observeSales(context: Context): Flow<List<ManualSale>> = callbackFlow {
        val db = getDb()
        val auth = FirebaseAuth.getInstance()
        val merchantId = auth.currentUser?.uid ?: AppSettings(context).pullToken

        val query = db.collection("merchants")
            .document(merchantId)
            .collection("sales")
            .orderBy("recordedAtEpochMillis", Query.Direction.DESCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error escuchando ventas en Firestore: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    val saleId = doc.getString("saleId") ?: doc.id
                    val cents = doc.getLong("totalAmountCents") ?: 0L
                    val recordedAt = doc.getLong("recordedAtEpochMillis") ?: System.currentTimeMillis()
                    val deletedAt = doc.getLong("deletedAtEpochMillis")
                    val revision = doc.getLong("revision")?.toInt() ?: 1

                    if (deletedAt != null) null
                    else ManualSale(
                        id = saleId,
                        amountCents = cents,
                        recordedAtEpochMillis = recordedAt,
                        revision = revision,
                        syncStatus = SaleSyncStatus.SYNCED,
                        syncedAtEpochMillis = System.currentTimeMillis()
                    )
                }
                trySend(list)
            }
        }

        awaitClose { registration.remove() }
    }

    /**
     * Sincroniza el perfil del negocio MYPE en Cloud Firestore (/merchants/{id}).
     */
    suspend fun saveMerchantProfile(context: Context, profile: LocalBusinessProfile): Boolean {
        return try {
            val db = getDb()
            val merchantId = getOrInitMerchantId(context)
            val docRef = db.collection("merchants").document(merchantId)

            val payload = hashMapOf(
                "merchantId" to merchantId,
                "businessName" to profile.businessName,
                "ownerName" to profile.ownerName,
                "phone" to profile.phone,
                "ruc" to profile.ruc,
                "preferredPaymentMethod" to profile.preferredPaymentMethod,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            docRef.set(payload, SetOptions.merge()).await()
            Log.i(TAG, "Perfil MYPE sincronizado en Firestore: ${profile.businessName}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando perfil MYPE en Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * Actualiza el medio de pago preferido en Cloud Firestore (/merchants/{id}).
     */
    suspend fun updatePreferredPaymentMethod(context: Context, method: String): Boolean {
        return try {
            val db = getDb()
            val merchantId = getOrInitMerchantId(context)
            db.collection("merchants").document(merchantId).set(
                mapOf(
                    "preferredPaymentMethod" to method,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
            Log.i(TAG, "Preferencia de pago '$method' guardada en Firestore para merchant $merchantId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando medio de pago en Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * Registra una solicitud de verificación KYC formal en Cloud Firestore.
     */
    suspend fun submitKycRequest(context: Context, dni: String, fullName: String): String {
        val db = getDb()
        val merchantId = getOrInitMerchantId(context)
        val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val sequence = (100000..999999).random()
        val requestId = "KYC-$year-$sequence"

        val payload = hashMapOf(
            "requestId" to requestId,
            "merchantId" to merchantId,
            "dni" to dni,
            "fullName" to fullName,
            "kycStatus" to "PENDING_REVIEW",
            "consentGiven" to true,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("kyc_requests").document(requestId).set(payload).await()
        db.collection("merchants").document(merchantId).set(
            mapOf(
                "kycStatus" to "PENDING_REVIEW",
                "kycSummary" to "$fullName · DNI ****${dni.takeLast(4)}",
                "kycRequestId" to requestId,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()

        Log.i(TAG, "Solicitud KYC $requestId guardada en Firestore")
        return requestId
    }

    /**
     * Registra una reclamación del Libro de Reclamaciones (Ley N° 29571) en Cloud Firestore (/claims/{claimNumber}).
     * La creación dispara el trigger onClaimCreated que la replica hacia Supabase claims_mirror.
     */
    suspend fun submitClaim(
        context: Context,
        fullName: String,
        documentType: String,
        documentNumber: String,
        email: String,
        phone: String,
        detail: String,
        consumerRequest: String,
        amountClaimedCents: Long? = null
    ): String {
        val db = getDb()
        val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val sequence = (100000..999999).random()
        val claimNumber = "REC-$year-$sequence"

        val payload = hashMapOf(
            "claim_number" to claimNumber,
            "consumer_name" to fullName,
            "document_type" to documentType,
            "document_number" to documentNumber,
            "email" to email,
            "phone" to phone,
            "detail" to detail,
            "consumer_request" to consumerRequest,
            "amount_claimed_cents" to amountClaimedCents,
            "status" to "RECEIVED",
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("claims").document(claimNumber).set(payload).await()
        Log.i(TAG, "Reclamación $claimNumber guardada en Firestore")
        return claimNumber
    }

    /**
     * Observa el estado de verificación KYC del comerciante en tiempo real.
     */
    fun observeMerchantKycStatus(context: Context): Flow<String?> = callbackFlow {
        val db = getDb()
        val auth = FirebaseAuth.getInstance()
        val merchantId = auth.currentUser?.uid ?: AppSettings(context).pullToken

        val registration = db.collection("merchants").document(merchantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error escuchando estado KYC: ${error.message}")
                    return@addSnapshotListener
                }
                val status = snapshot?.getString("kycStatus")
                trySend(status)
            }
        awaitClose { registration.remove() }
    }
}

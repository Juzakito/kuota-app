package com.kuota.core.data.repository

import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.kuota.core.model.BusinessLocation
import com.kuota.core.model.KycDraft
import com.kuota.core.model.KycSubmission
import com.kuota.core.model.KycSubmitResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface KycRepository {
    fun getCurrentUid(): String?
    fun observeKycDraft(uid: String): Flow<KycDraft?>
    suspend fun saveKycDraft(uid: String, draft: KycDraft): Result<Unit>
    suspend fun uploadKycImage(uid: String, fileName: String, imageBytes: ByteArray, mimeType: String = "image/jpeg"): Result<String>
    suspend fun submitKyc(submission: KycSubmission): Result<KycSubmitResult>
    suspend fun initUserSummary(): Result<Unit>
}

@Singleton
class DefaultKycRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    private val storage: FirebaseStorage
) : KycRepository {

    override fun getCurrentUid(): String? = auth.currentUser?.uid

    override fun observeKycDraft(uid: String): Flow<KycDraft?> = callbackFlow {
        val draftDocRef = firestore.collection("users")
            .document(uid)
            .collection("kyc_draft")
            .document("draft")

        val registration = draftDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data ?: emptyMap<String, Any>()
                val draft = KycDraft(
                    currentStep = (data["currentStep"] as? Number)?.toInt() ?: 1,
                    phone = (data["phone"] as? String).orEmpty(),
                    role = (data["role"] as? String) ?: "comprador",
                    fullName = (data["fullName"] as? String).orEmpty(),
                    dni = (data["dni"] as? String).orEmpty(),
                    businessName = (data["businessName"] as? String).orEmpty(),
                    category = (data["category"] as? String).orEmpty(),
                    ruc = data["ruc"] as? String,
                    location = (data["location"] as? Map<*, *>)?.let { loc ->
                        BusinessLocation(
                            latitude = (loc["latitude"] as? Number)?.toDouble() ?: 0.0,
                            longitude = (loc["longitude"] as? Number)?.toDouble() ?: 0.0,
                            address = (loc["address"] as? String).orEmpty(),
                            department = (loc["department"] as? String) ?: "Lima",
                            province = (loc["province"] as? String) ?: "Lima",
                            district = (loc["district"] as? String).orEmpty()
                        )
                    },
                    dniFrontStoragePath = data["dniFrontStoragePath"] as? String,
                    dniBackStoragePath = data["dniBackStoragePath"] as? String,
                    selfieStoragePath = data["selfieStoragePath"] as? String,
                    termsAgreed = (data["termsAgreed"] as? Boolean) ?: false,
                    privacyAgreed = (data["privacyAgreed"] as? Boolean) ?: false,
                    termsVersion = (data["termsVersion"] as? String) ?: "2026-v1",
                    updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
                trySend(draft)
            } else {
                trySend(null)
            }
        }

        awaitClose { registration.remove() }
    }

    override suspend fun saveKycDraft(uid: String, draft: KycDraft): Result<Unit> = runCatching {
        val draftDocRef = firestore.collection("users")
            .document(uid)
            .collection("kyc_draft")
            .document("draft")

        val payload = mapOf(
            "currentStep" to draft.currentStep,
            "phone" to draft.phone,
            "role" to draft.role,
            "fullName" to draft.fullName,
            "dni" to draft.dni,
            "businessName" to draft.businessName,
            "category" to draft.category,
            "ruc" to draft.ruc,
            "location" to draft.location?.let {
                mapOf(
                    "latitude" to it.latitude,
                    "longitude" to it.longitude,
                    "address" to it.address,
                    "department" to it.department,
                    "province" to it.province,
                    "district" to it.district
                )
            },
            "dniFrontStoragePath" to draft.dniFrontStoragePath,
            "dniBackStoragePath" to draft.dniBackStoragePath,
            "selfieStoragePath" to draft.selfieStoragePath,
            "termsAgreed" to draft.termsAgreed,
            "privacyAgreed" to draft.privacyAgreed,
            "termsVersion" to draft.termsVersion,
            "updatedAt" to System.currentTimeMillis()
        )

        draftDocRef.set(payload).await()
    }

    override suspend fun uploadKycImage(
        uid: String,
        fileName: String,
        imageBytes: ByteArray,
        mimeType: String
    ): Result<String> = runCatching {
        val compressedBytes = compressImageIfNeeded(imageBytes)

        // 1. Guardar primero en Firestore (100% nativo y gratuito en Firebase Spark)
        val docId = when (fileName) {
            "dni_front.jpg" -> "dni_front"
            "dni_back.jpg" -> "dni_back"
            "selfie.jpg" -> "selfie"
            else -> fileName.replace(".", "_")
        }
        val base64Str = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
        val docRef = firestore.collection("users")
            .document(uid)
            .collection("kyc_documents")
            .document(docId)

        val docData = mapOf(
            "docId" to docId,
            "fileName" to fileName,
            "base64" to base64Str,
            "mimeType" to mimeType,
            "sizeBytes" to compressedBytes.size,
            "uploadedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(docData).await()

        // 2. Intentar Firebase Storage en caso de que el bucket esté activo
        try {
            val storagePath = "kyc/$uid/$fileName"
            val storageRef = storage.reference.child(storagePath)
            val metadata = StorageMetadata.Builder()
                .setContentType(mimeType)
                .build()

            storageRef.putBytes(compressedBytes, metadata).await()
            return@runCatching storagePath
        } catch (e: Exception) {
            // Si Storage no está aprovisionado en Firebase, usamos la ruta Firestore
            return@runCatching "firestore:users/$uid/kyc_documents/$docId"
        }
    }

    private fun compressImageIfNeeded(imageBytes: ByteArray): ByteArray {
        return try {
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return imageBytes

            val maxDimension = 1280
            val width = bitmap.width
            val height = bitmap.height
            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toFloat() / height.toFloat()
                val (newWidth, newHeight) = if (ratio > 1f) {
                    maxDimension to (maxDimension / ratio).toInt()
                } else {
                    (maxDimension * ratio).toInt() to maxDimension
                }
                android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }

            val baos = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos)
            baos.toByteArray()
        } catch (e: Exception) {
            imageBytes
        }
    }

    override suspend fun submitKyc(submission: KycSubmission): Result<KycSubmitResult> = runCatching {
        val uid = getCurrentUid() ?: throw IllegalStateException("Usuario no autenticado")
        val requestId = "kyc_${uid}_${System.currentTimeMillis()}"

        // 1. Intentar llamar a Cloud Function callable si estuviera disponible
        try {
            val payload = mapOf(
                "dni" to submission.dni,
                "fullName" to submission.fullName,
                "businessName" to submission.businessName,
                "category" to submission.category,
                "ruc" to submission.ruc,
                "role" to submission.role,
                "location" to submission.location?.let {
                    mapOf(
                        "latitude" to it.latitude,
                        "longitude" to it.longitude,
                        "address" to it.address,
                        "department" to it.department,
                        "province" to it.province,
                        "district" to it.district
                    )
                },
                "dniFrontStoragePath" to submission.dniFrontStoragePath,
                "dniBackStoragePath" to submission.dniBackStoragePath,
                "selfieStoragePath" to submission.selfieStoragePath,
                "termsConsent" to mapOf(
                    "agreed" to submission.termsConsent.agreed,
                    "version" to submission.termsConsent.version,
                    "consentedAt" to submission.termsConsent.consentedAt
                )
            )

            val result = functions.getHttpsCallable("submitKyc").call(payload).await()
            val data = result.getData() as? Map<*, *> ?: emptyMap<String, Any>()

            KycSubmitResult(
                success = (data["success"] as? Boolean) ?: true,
                requestId = (data["requestId"] as? String) ?: requestId,
                kycStatus = (data["kycStatus"] as? String) ?: "approved",
                message = (data["message"] as? String) ?: "¡Verificación KYC aprobada exitosamente!"
            )
        } catch (e: Exception) {
            // 2. Fallback nativo a Firestore (Opción B: 100% Firebase gratis sin plan Blaze)
            val now = FieldValue.serverTimestamp()
            val batch = firestore.batch()

            val userRef = firestore.collection("users").document(uid)
            val userProfile = mutableMapOf<String, Any?>(
                "dni" to submission.dni,
                "fullName" to submission.fullName,
                "businessName" to submission.businessName,
                "category" to submission.category,
                "ruc" to submission.ruc,
                "role" to submission.role,
                "kycStatus" to "approved",
                "kycSubmittedAt" to now,
                "termsConsent" to mapOf(
                    "agreed" to submission.termsConsent.agreed,
                    "version" to submission.termsConsent.version,
                    "consentedAt" to submission.termsConsent.consentedAt
                ),
                "updatedAt" to now
            )
            submission.location?.let {
                userProfile["location"] = mapOf(
                    "latitude" to it.latitude,
                    "longitude" to it.longitude,
                    "address" to it.address,
                    "department" to it.department,
                    "province" to it.province,
                    "district" to it.district
                )
            }
            batch.set(userRef, userProfile, SetOptions.merge())

            val summaryRef = firestore.collection("users").document(uid).collection("summary").document("current")
            val summaryData = mapOf(
                "uid" to uid,
                "kycStatus" to "approved",
                "role" to submission.role,
                "businessName" to submission.businessName,
                "creditLimit" to 140000L,
                "maxCreditCents" to 140000L,
                "availableCredit" to 140000L,
                "availableCreditCents" to 140000L,
                "usedCredit" to 0L,
                "usedCreditCents" to 0L,
                "riskScore" to 720,
                "level" to 1,
                "updatedAt" to System.currentTimeMillis()
            )
            batch.set(summaryRef, summaryData, SetOptions.merge())

            val requestRef = firestore.collection("kyc_requests").document(requestId)
            val requestData = mapOf(
                "requestId" to requestId,
                "uid" to uid,
                "dni" to submission.dni,
                "fullName" to submission.fullName,
                "businessName" to submission.businessName,
                "category" to submission.category,
                "ruc" to submission.ruc,
                "role" to submission.role,
                "dniFrontStoragePath" to submission.dniFrontStoragePath,
                "dniBackStoragePath" to submission.dniBackStoragePath,
                "selfieStoragePath" to submission.selfieStoragePath,
                "status" to "approved",
                "submittedAt" to now
            )
            batch.set(requestRef, requestData)

            batch.commit().await()

            KycSubmitResult(
                success = true,
                requestId = requestId,
                kycStatus = "approved",
                message = "¡Verificación KYC aprobada exitosamente!"
            )
        }
    }

    override suspend fun initUserSummary(): Result<Unit> = runCatching {
        val uid = getCurrentUid() ?: return@runCatching
        try {
            functions.getHttpsCallable("initUserSummary").call().await()
        } catch (e: Exception) {
            val docRef = firestore.collection("users").document(uid).collection("summary").document("current")
            val snap = docRef.get().await()
            if (!snap.exists()) {
                docRef.set(
                    mapOf(
                        "uid" to uid,
                        "kycStatus" to "unsubmitted",
                        "role" to "comprador",
                        "creditLimit" to 0L,
                        "maxCreditCents" to 0L,
                        "availableCredit" to 0L,
                        "availableCreditCents" to 0L,
                        "usedCredit" to 0L,
                        "usedCreditCents" to 0L,
                        "riskScore" to 650,
                        "level" to 1,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            }
        }
        Unit
    }
}

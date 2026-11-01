package com.kuota.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
        val storagePath = "kyc/$uid/$fileName"
        val storageRef = storage.reference.child(storagePath)
        val metadata = StorageMetadata.Builder()
            .setContentType(mimeType)
            .build()

        storageRef.putBytes(imageBytes, metadata).await()
        storagePath
    }

    override suspend fun submitKyc(submission: KycSubmission): Result<KycSubmitResult> = runCatching {
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
            success = (data["success"] as? Boolean) ?: false,
            requestId = data["requestId"] as? String,
            kycStatus = (data["kycStatus"] as? String) ?: "pending",
            message = (data["message"] as? String) ?: "En proceso de verificación."
        )
    }

    override suspend fun initUserSummary(): Result<Unit> = runCatching {
        functions.getHttpsCallable("initUserSummary").call().await()
        Unit
    }
}

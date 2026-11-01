package com.kuota.core.model

import kotlinx.serialization.Serializable

/**
 * Estados del proceso de verificación KYC (PRD §8.1 y §6.1).
 */
@Serializable
enum class KycStatus(val rawValue: String) {
    UNSUBMITTED("unsubmitted"),
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    companion object {
        fun fromRaw(value: String?): KycStatus {
            return entries.firstOrNull { it.rawValue.equals(value, ignoreCase = true) }
                ?: when (value?.uppercase()) {
                    "VERIFIED" -> APPROVED
                    "REJECTED_INVALID_DNI" -> REJECTED
                    "PENDING_REVIEW" -> PENDING
                    else -> UNSUBMITTED
                }
        }
    }
}

/**
 * Geolocalización del negocio (PRD §8.1).
 */
@Serializable
data class BusinessLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val department: String = "Lima",
    val province: String = "Lima",
    val district: String = ""
)

/**
 * Consentimiento legal versionado conforme a la Ley N° 29733 (PRD §8.1).
 */
@Serializable
data class TermsConsent(
    val agreed: Boolean = false,
    val version: String = "2026-v1",
    val consentedAt: String = ""
)

/**
 * Borrador persistente del flujo de Onboarding en Firestore `users/{uid}/kyc_draft/draft`.
 * Permite autoguardado en tiempo real para que el usuario no pierda avance si sale de la app.
 */
@Serializable
data class KycDraft(
    val currentStep: Int = 1,
    val phone: String = "",
    val role: String = "comprador", // 'comprador' | 'proveedor' | 'ambos'
    val fullName: String = "",
    val dni: String = "",
    val businessName: String = "",
    val category: String = "",
    val ruc: String? = null,
    val location: BusinessLocation? = null,
    val dniFrontStoragePath: String? = null,
    val dniBackStoragePath: String? = null,
    val selfieStoragePath: String? = null,
    val termsAgreed: Boolean = false,
    val privacyAgreed: Boolean = false,
    val termsVersion: String = "2026-v1",
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Payload enviado a la Cloud Function callable `submitKyc`.
 */
@Serializable
data class KycSubmission(
    val dni: String,
    val fullName: String,
    val businessName: String,
    val category: String,
    val ruc: String? = null,
    val role: String = "comprador",
    val location: BusinessLocation? = null,
    val dniFrontStoragePath: String,
    val dniBackStoragePath: String,
    val selfieStoragePath: String,
    val termsConsent: TermsConsent
)

/**
 * Resultado devuelto por la Cloud Function callable `submitKyc`.
 */
@Serializable
data class KycSubmitResult(
    val success: Boolean,
    val requestId: String? = null,
    val kycStatus: String,
    val message: String
)

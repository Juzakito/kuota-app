package com.kuota.backend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestOtpRequest(
    val phone: String,
    val email: String? = null
)

@Serializable
data class RequestOtpResponse(
    @SerialName("otp_code_debug") val otpCodeDebug: String,
    val message: String,
    @SerialName("expires_in_seconds") val expiresInSeconds: Int = 300
)

@Serializable
data class VerifyOtpRequest(
    val phone: String,
    @SerialName("otp_code") val otpCode: String
)

@Serializable
data class VerifyOtpResponse(
    @SerialName("token_type") val tokenType: String = "Bearer",
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in_seconds") val expiresInSeconds: Long = JWT_TTL_SECONDS,
    val user: UserProfileDto
)

@Serializable
data class UserProfileDto(
    @SerialName("user_id") val userId: String,
    val phone: String,
    val email: String,
    @SerialName("kyc_status") val kycStatus: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class SubmitKycDniRequest(
    val dni: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("document_photo_base64") val documentPhotoBase64: String? = null
)

@Serializable
data class SubmitKycDniResponse(
    @SerialName("kyc_status") val kycStatus: String, // "VERIFIED_MOCK", "PENDING_REVIEW"
    val dni: String,
    @SerialName("full_name") val fullName: String,
    val message: String
)

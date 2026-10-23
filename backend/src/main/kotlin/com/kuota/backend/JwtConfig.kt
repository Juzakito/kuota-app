package com.kuota.backend

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.time.Clock
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal const val JWT_ISSUER = "kuota-peru-auth"
internal const val JWT_MIN_SECRET_LENGTH = 32
internal const val JWT_TTL_SECONDS = 24 * 60 * 60L

@Serializable
private data class JwtHeader(
    val alg: String = "HS256",
    val typ: String = "JWT"
)

@Serializable
private data class JwtPayload(
    val sub: String,
    val phone: String,
    val iss: String = JWT_ISSUER,
    val iat: Long,
    val exp: Long
)

data class JwtUserClaims(
    val userId: String,
    val phone: String
)

internal class JwtService(
    private val secret: String,
    private val clock: Clock = Clock.systemUTC()
) {
    init {
        require(secret.length >= JWT_MIN_SECRET_LENGTH) {
            "JWT secret must have at least $JWT_MIN_SECRET_LENGTH characters."
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun makeToken(userId: String, phone: String): String {
        val issuedAt = clock.instant().epochSecond
        val headerJson = json.encodeToString(JwtHeader())
        val payloadJson = json.encodeToString(
            JwtPayload(
                sub = userId,
                phone = phone,
                iat = issuedAt,
                exp = issuedAt + JWT_TTL_SECONDS
            )
        )

        val encoder = Base64.getUrlEncoder().withoutPadding()
        val encodedHeader = encoder.encodeToString(headerJson.toByteArray(Charsets.UTF_8))
        val encodedPayload = encoder.encodeToString(payloadJson.toByteArray(Charsets.UTF_8))

        val signatureContent = "$encodedHeader.$encodedPayload"
        return "$signatureContent.${hmacSha256(signatureContent)}"
    }

    fun verifyToken(token: String): JwtUserClaims? {
        val parts = token.trim().split(".")
        if (parts.size != 3 || parts.any(String::isEmpty)) return null

        val encodedHeader = parts[0]
        val encodedPayload = parts[1]
        val signatureContent = "$encodedHeader.$encodedPayload"
        val expectedSignature = hmacSha256(signatureContent)
        if (!MessageDigest.isEqual(
                parts[2].toByteArray(Charsets.UTF_8),
                expectedSignature.toByteArray(Charsets.UTF_8)
            )
        ) {
            return null
        }

        return try {
            val decoder = Base64.getUrlDecoder()
            val headerJson = String(decoder.decode(encodedHeader), Charsets.UTF_8)
            val header = json.decodeFromString<JwtHeader>(headerJson)
            if (header.alg != "HS256") return null

            val payloadJson = String(decoder.decode(encodedPayload), Charsets.UTF_8)
            val payload = json.decodeFromString<JwtPayload>(payloadJson)
            if (payload.iss != JWT_ISSUER || payload.sub.isBlank()) return null
            if (payload.exp <= clock.instant().epochSecond) return null

            JwtUserClaims(userId = payload.sub, phone = payload.phone)
        } catch (_: Exception) {
            null
        }
    }

    private fun hmacSha256(data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val hash = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
    }
}

internal object JwtConfig {
    @Volatile
    private var service: JwtService? = null

    fun initialize(secret: String) {
        service = JwtService(secret)
    }

    fun makeToken(userId: String, phone: String): String =
        service?.makeToken(userId, phone) ?: error("JWT service is not initialized.")

    fun verifyToken(token: String): JwtUserClaims? = service?.verifyToken(token)
}

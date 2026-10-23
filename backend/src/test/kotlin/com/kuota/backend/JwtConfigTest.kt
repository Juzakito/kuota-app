package com.kuota.backend

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JwtConfigTest {

    private val secret = "k".repeat(JWT_MIN_SECRET_LENGTH)
    private val issuedAt = Instant.parse("2026-10-22T12:00:00Z")
    private val clock = Clock.fixed(issuedAt, ZoneOffset.UTC)
    private val jwt = JwtService(secret, clock)

    @Test
    fun `round trip returns the signed claims`() {
        val token = jwt.makeToken("user-987654321", "987654321")

        assertEquals(JwtUserClaims("user-987654321", "987654321"), jwt.verifyToken(token))
    }

    @Test
    fun `issued tokens declare the algorithm, issuer and ttl explicitly`() {
        val parts = jwt.makeToken("user-1", "987654321").split(".")

        assertTrue(decode(parts[0]).contains("\"alg\":\"HS256\""))
        assertTrue(decode(parts[1]).contains("\"iss\":\"$JWT_ISSUER\""))
        assertTrue(decode(parts[1]).contains("\"exp\":${issuedAt.epochSecond + JWT_TTL_SECONDS}"))
    }

    @Test
    fun `tokens remain valid until the ttl boundary and expire afterwards`() {
        val token = jwt.makeToken("user-1", "987654321")

        val justBefore = JwtService(secret, Clock.fixed(issuedAt.plusSeconds(JWT_TTL_SECONDS - 1), ZoneOffset.UTC))
        assertNotNull(justBefore.verifyToken(token))

        val atBoundary = JwtService(secret, Clock.fixed(issuedAt.plusSeconds(JWT_TTL_SECONDS), ZoneOffset.UTC))
        assertNull(atBoundary.verifyToken(token))
    }

    @Test
    fun `tampering with the payload invalidates the signature`() {
        val parts = jwt.makeToken("user-1", "987654321").split(".")
        val payload = decode(parts[1]).replace("user-1", "user-2")
        val tampered = "${parts[0]}.${encode(payload)}.${parts[2]}"

        assertNull(jwt.verifyToken(tampered))
    }

    @Test
    fun `a token signed with another secret is rejected`() {
        val other = JwtService("o".repeat(JWT_MIN_SECRET_LENGTH), clock)

        assertNull(other.verifyToken(jwt.makeToken("user-1", "987654321")))
    }

    @Test
    fun `an unsigned alg none token is rejected`() {
        val payload = """{"sub":"user-1","phone":"987654321","iss":"$JWT_ISSUER","iat":${issuedAt.epochSecond},"exp":${issuedAt.epochSecond + JWT_TTL_SECONDS}}"""
        val token = sign(secret, """{"alg":"none","typ":"JWT"}""", payload)

        assertNull(jwt.verifyToken(token))
    }

    @Test
    fun `a token from another issuer is rejected`() {
        val payload = """{"sub":"user-1","phone":"987654321","iss":"otro-emisor","iat":${issuedAt.epochSecond},"exp":${issuedAt.epochSecond + JWT_TTL_SECONDS}}"""
        val token = sign(secret, """{"alg":"HS256","typ":"JWT"}""", payload)

        assertNull(jwt.verifyToken(token))
    }

    @Test
    fun `a token without exp is rejected`() {
        val payload = """{"sub":"user-1","phone":"987654321","iss":"$JWT_ISSUER","iat":${issuedAt.epochSecond}}"""
        val token = sign(secret, """{"alg":"HS256","typ":"JWT"}""", payload)

        assertNull(jwt.verifyToken(token))
    }

    @Test
    fun `malformed tokens are rejected`() {
        assertNull(jwt.verifyToken(""))
        assertNull(jwt.verifyToken("a.b"))
        assertNull(jwt.verifyToken("a.b.c"))
        assertNull(jwt.verifyToken(".."))
    }

    @Test
    fun `secrets shorter than the minimum are rejected`() {
        assertFailsWith<IllegalArgumentException> {
            JwtService("short")
        }
    }

    private fun sign(secret: String, header: String, payload: String): String {
        val content = "${encode(header)}.${encode(payload)}"
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val signature = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(mac.doFinal(content.toByteArray(Charsets.UTF_8)))
        return "$content.$signature"
    }

    private fun encode(value: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))

    private fun decode(value: String): String =
        String(Base64.getUrlDecoder().decode(value), Charsets.UTF_8)
}

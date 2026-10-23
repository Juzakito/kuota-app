package com.kouta.app.core.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom

class PinSecurityManagerTest {

    @Test
    fun `isValidPinFormat only accepts exactly 6 numeric digits`() {
        assertTrue(PinSecurityManager.isValidPinFormat("000000"))
        assertTrue(PinSecurityManager.isValidPinFormat("482915"))
        assertFalse(PinSecurityManager.isValidPinFormat(""))
        assertFalse(PinSecurityManager.isValidPinFormat("12345"))
        assertFalse(PinSecurityManager.isValidPinFormat("1234567"))
        assertFalse(PinSecurityManager.isValidPinFormat("12a456"))
        assertFalse(PinSecurityManager.isValidPinFormat("12 456"))
    }

    @Test
    fun `deriveKeyPbkdf2 and constantTimeEquals verify correct PIN and reject wrong PIN`() {
        val random = SecureRandom()
        val salt1 = ByteArray(PinSecurityManager.SALT_BYTES).also { random.nextBytes(it) }
        val salt2 = ByteArray(PinSecurityManager.SALT_BYTES).also { random.nextBytes(it) }

        val verifier1 = PinSecurityManager.deriveKeyPbkdf2("739204", salt1)
        val verifier1Repeat = PinSecurityManager.deriveKeyPbkdf2("739204", salt1)
        val verifierWrongPin = PinSecurityManager.deriveKeyPbkdf2("739205", salt1)
        val verifierDifferentSalt = PinSecurityManager.deriveKeyPbkdf2("739204", salt2)

        assertEquals(32, verifier1.size) // 256 bits = 32 bytes
        assertTrue(PinSecurityManager.constantTimeEquals(verifier1, verifier1Repeat))
        assertFalse(PinSecurityManager.constantTimeEquals(verifier1, verifierWrongPin))
        assertFalse(PinSecurityManager.constantTimeEquals(verifier1, verifierDifferentSalt))
    }

    @Test
    fun `computeCooldownMillis enforces progressive backoff after 3 failed attempts`() {
        assertEquals(0L, PinSecurityManager.computeCooldownMillis(1))
        assertEquals(0L, PinSecurityManager.computeCooldownMillis(2))
        assertEquals(30_000L, PinSecurityManager.computeCooldownMillis(3))
        assertEquals(60_000L, PinSecurityManager.computeCooldownMillis(4))
        assertEquals(300_000L, PinSecurityManager.computeCooldownMillis(5))
        assertEquals(900_000L, PinSecurityManager.computeCooldownMillis(6))
        assertEquals(900_000L, PinSecurityManager.computeCooldownMillis(10))
    }

    @Test
    fun `PersistedPinState reports isConfigured accurately`() {
        val unconfigured = PersistedPinState()
        assertFalse(unconfigured.isConfigured)

        val configured = PersistedPinState(
            saltBase64 = "c2FsdDEyMzQ1Njc4OTAxMg==",
            verifierBase64 = "dmVyaWZpZXIxMjM0NTY3ODkwMTIzNDU2Nzg5MDEy",
            isLocked = true
        )
        assertTrue(configured.isConfigured)
        assertTrue(configured.isLocked)
    }

    @Test
    fun `PersistedPinState biometricEnabled requires explicit consent and defaults to false`() {
        val defaultState = PersistedPinState()
        assertFalse("La biometría nunca debe activarse sin consentimiento explícito", defaultState.biometricEnabled)

        val enabledState = defaultState.copy(biometricEnabled = true)
        assertTrue(enabledState.biometricEnabled)
    }

    @Test
    fun `BiometricCapability labels provide clear user-facing messages`() {
        val none = BiometricAuthManager.getCapabilityLabel(BiometricCapability.NoneEnrolled)
        assertTrue(none.contains("No tienes huellas ni rostro"))

        val noHw = BiometricAuthManager.getCapabilityLabel(BiometricCapability.NoHardware)
        assertTrue(noHw.contains("no cuenta con lector"))

        val unavail = BiometricAuthManager.getCapabilityLabel(BiometricCapability.HardwareUnavailable)
        assertTrue(unavail.contains("no está disponible"))
    }
}

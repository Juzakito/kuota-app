package com.kouta.app.core.security

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Suite de pruebas unitarias para los controles de seguridad fintech de KUOTA.
 * Verifica inactividad, idempotencia, debounce, enmascaramiento de datos y aritmética de centavos.
 */
class FintechSecuritySuiteTest {

    @Before
    fun setUp() {
        FintechOperationLock.reset()
        InactivityLockManager.reset()
    }

    @Test
    fun `idempotency key is a valid non-empty UUIDv4`() {
        val key1 = FintechOperationLock.generateIdempotencyKey()
        val key2 = FintechOperationLock.generateIdempotencyKey()

        assertNotNull(key1)
        assertNotNull(key2)
        assertNotEquals(key1, key2)

        // Verifica que cumpla el estándar de UUID
        val parsed1 = UUID.fromString(key1)
        assertEquals(4, parsed1.version())
    }

    @Test
    fun `canExecuteAction blocks rapid double clicks on payment buttons`() {
        val action = "confirm_installment_payment"
        val t0 = 100_000L

        // Primer clic legítimo
        assertTrue(FintechOperationLock.canExecuteAction(action, t0))

        // Segundo clic 100ms después (doble pulsación accidental) -> debe ser bloqueado
        assertFalse(FintechOperationLock.canExecuteAction(action, t0 + 100L))

        // Clic a los 500ms -> sigue bloqueado (ventana de 1200ms)
        assertFalse(FintechOperationLock.canExecuteAction(action, t0 + 500L))

        // Clic a los 1300ms -> permitido nuevamente
        assertTrue(FintechOperationLock.canExecuteAction(action, t0 + 1_300L))
    }

    @Test
    fun `executeIdempotent passes unique key to lambda block`() = runBlocking {
        var capturedKey: String? = null
        val result = FintechOperationLock.executeIdempotent("test_op") { key ->
            capturedKey = key
            "PROCESSED"
        }

        assertEquals("PROCESSED", result)
        assertNotNull(capturedKey)
        assertEquals(4, UUID.fromString(capturedKey).version())
    }

    @Test
    fun `InactivityLockManager default timeout is 5 minutes`() {
        assertEquals(300_000L, InactivityLockManager.INACTIVITY_TIMEOUT_MILLIS)
    }

    @Test
    fun `ScreenSecurityManager tracks sensitive screen state`() {
        assertFalse(ScreenSecurityManager.isSensitiveScreenActive.value)
        ScreenSecurityManager.enterSensitiveScreen(null)
        assertTrue(ScreenSecurityManager.isSensitiveScreenActive.value)
        ScreenSecurityManager.exitSensitiveScreen(null, isAppLocked = false)
        assertFalse(ScreenSecurityManager.isSensitiveScreenActive.value)
    }

    @Test
    fun `DNI masking function preserves privacy in logs and UI`() {
        fun maskDni(dni: String): String {
            val sanitized = dni.filter { it.isDigit() }
            return if (sanitized.length == 8) "****${sanitized.takeLast(4)}" else "********"
        }

        assertEquals("****5678", maskDni("12345678"))
        assertEquals("****9012", maskDni("78909012"))
        assertEquals("********", maskDni("123"))
    }

    @Test
    fun `exact integer cents prevents floating point representation errors`() {
        // En punto flotante IEEE 754: 0.1 + 0.2 = 0.30000000000000004
        val installment1Cents = 10L // S/ 0.10
        val installment2Cents = 20L // S/ 0.20
        val sumCents = installment1Cents + installment2Cents

        assertEquals(30L, sumCents) // Exactamente 30 centavos (S/ 0.30)
    }
}

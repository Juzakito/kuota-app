package com.kouta.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreditLevelAndPurchasesTest {

    @Test
    fun verifyAllSixCreditLevelsHierarchy() {
        val levels = CreditLevel.values()
        assertEquals("Debe haber exactamente 6 niveles de crédito", 6, levels.size)

        // Nivel 1
        val n1 = CreditLevel.NIVEL_1
        assertEquals(1, n1.levelNumber)
        assertEquals(300_00L, n1.minLineCents)
        assertEquals(50, n1.downPaymentPercentage)

        // Nivel 6
        val n6 = CreditLevel.NIVEL_6
        assertEquals(6, n6.levelNumber)
        assertEquals(10_000_00L, n6.maxLineCents)
        assertEquals(0, n6.downPaymentPercentage)

        // Monotonic increase of credit limits
        for (i in 0 until levels.size - 1) {
            assertTrue(
                "Nivel ${levels[i + 1].levelNumber} debe tener mayor límite que Nivel ${levels[i].levelNumber}",
                levels[i + 1].maxLineCents > levels[i].maxLineCents
            )
            assertTrue(
                "Nivel ${levels[i + 1].levelNumber} debe tener menor o igual cuota inicial que Nivel ${levels[i].levelNumber}",
                levels[i + 1].downPaymentPercentage <= levels[i].downPaymentPercentage
            )
        }
    }

    @Test
    fun verifyDownPaymentAndSupplierPayoutFormulas() {
        // Venta de S/ 200.00 en Nivel 1 (50% inicial)
        val n1 = CreditLevel.NIVEL_1
        val totalCents = 200_00L
        val downPaymentCents = n1.calculateDownPaymentCents(totalCents)
        val financedCents = n1.calculateFinancedAmountCents(totalCents)

        assertEquals("Inicial 50% de S/ 200 debe ser S/ 100", 100_00L, downPaymentCents)
        assertEquals("Saldo financiado debe ser S/ 100", 100_00L, financedCents)

        // Fórmula proveedor en 48h: 98% * venta - inicial = 98% * 200 - 100 = 196 - 100 = S/ 96.00
        val supplierPayoutCents = n1.calculateSupplierPayout48hCents(totalCents, downPaymentCents)
        assertEquals("Liquidación 48h debe ser S/ 96.00", 96_00L, supplierPayoutCents)

        // Venta de S/ 5,000 en Nivel 6 (0% inicial)
        val n6 = CreditLevel.NIVEL_6
        val bigSaleCents = 5_000_00L
        val n6DownPayment = n6.calculateDownPaymentCents(bigSaleCents)
        assertEquals("Inicial Nivel 6 es 0%", 0L, n6DownPayment)

        val n6SupplierPayout = n6.calculateSupplierPayout48hCents(bigSaleCents, n6DownPayment)
        // 98% de 5000 = 4900
        assertEquals("Liquidación al 98% es S/ 4,900.00", 4_900_00L, n6SupplierPayout)
    }

    @Test
    fun verifyRule32Enforcement() {
        val n1 = CreditLevel.NIVEL_1

        // 1. Con transacción abierta -> debe rechazar
        val checkBlocked = PurchaseEligibilityCheck.validate(
            totalAmountCents = 150_00L,
            currentLevel = n1,
            availableLineCents = 300_00L,
            hasActiveOpenTransaction = true
        )
        assertFalse("Debe bloquearse si hay transacción activa (Regla 3.2)", checkBlocked.isEligible)
        assertNotNull(checkBlocked.errorMessage)
        assertTrue(checkBlocked.errorMessage!!.contains("Regla 3.2"))

        // 2. Sin transacción abierta y dentro de línea -> debe aprobar
        val checkApproved = PurchaseEligibilityCheck.validate(
            totalAmountCents = 150_00L,
            currentLevel = n1,
            availableLineCents = 300_00L,
            hasActiveOpenTransaction = false
        )
        assertTrue("Debe aprobarse si está dentro de línea", checkApproved.isEligible)
        assertEquals(75_00L, checkApproved.downPaymentCents)
        assertEquals(75_00L, checkApproved.financedCents)
        assertNull(checkApproved.errorMessage)

        // 3. Monto mayor a línea disponible -> debe rechazar
        val checkOverLimit = PurchaseEligibilityCheck.validate(
            totalAmountCents = 500_00L,
            currentLevel = n1,
            availableLineCents = 300_00L,
            hasActiveOpenTransaction = false
        )
        assertFalse("Debe bloquearse si excede la línea", checkOverLimit.isEligible)
        assertTrue(checkOverLimit.errorMessage!!.contains("excede tu línea"))
    }

    @Test
    fun verifyUserRoles() {
        assertEquals(UserRole.VENDEDOR, UserRole.fromCode("VENDEDOR"))
        assertEquals(UserRole.PROVEEDOR, UserRole.fromCode("PROVEEDOR"))
        assertEquals(UserRole.PROVEEDOR, UserRole.fromCode("proveedor"))
        assertEquals(UserRole.VENDEDOR, UserRole.fromCode("other"))
    }
}

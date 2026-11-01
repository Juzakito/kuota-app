package com.kuota.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests unitarios para el manejo de dinero en céntimos (Long)
 * según AGENTS.md Regla 3:
 * "Dinero en céntimos (Long), moneda PEN, zona America/Lima. Cero Double/Float para montos."
 */
class MoneyTest {

    @Test
    fun `test money formatting matches Peruvian Soles`() {
        val money = Money(70_000L) // S/ 700.00
        assertEquals("S/ 700.00", money.toFormattedString())

        val small = Money(19_900L) // S/ 199.00
        assertEquals("S/ 199.00", small.toFormattedString())

        val large = Money(10_000_00L) // S/ 10,000.00
        assertEquals("S/ 10,000.00", large.toFormattedString())
    }

    @Test
    fun `test money addition and subtraction in cents without rounding errors`() {
        val amount = Money(70_000L)
        val initial30Pct = Money(21_000L)
        val financed = amount - initial30Pct

        assertEquals(49_000L, financed.cents)
        assertEquals("S/ 490.00", financed.toFormattedString())

        // 5% de fee fijo sobre financiado: 49000 * 5 / 100 = 2450 céntimos (S/ 24.50)
        val fee = Money((financed.cents * 5L) / 100L)
        assertEquals(2_450L, fee.cents)
        assertEquals("S/ 24.50", fee.toFormattedString())

        val totalDebt = financed + fee
        assertEquals(51_450L, totalDebt.cents)
        assertEquals("S/ 514.50", totalDebt.toFormattedString())
    }

    @Test
    fun `test installment division and remainder absorption`() {
        val totalDebtCents = 51_450L // S/ 514.50 en 4 cuotas
        val installmentsCount = 4
        val baseCuotaCents = totalDebtCents / installmentsCount // 12862 céntimos (S/ 128.62)
        val remainderCents = totalDebtCents - (baseCuotaCents * installmentsCount) // 2 céntimos

        assertEquals(12_862L, baseCuotaCents)
        assertEquals(2L, remainderCents)

        // Las primeras 3 cuotas son de 12862 céntimos
        val cuota1 = baseCuotaCents
        val cuota2 = baseCuotaCents
        val cuota3 = baseCuotaCents
        // La última cuota absorbe el residuo
        val cuota4 = baseCuotaCents + remainderCents

        assertEquals(12_864L, cuota4)
        assertEquals(totalDebtCents, cuota1 + cuota2 + cuota3 + cuota4)
    }
}

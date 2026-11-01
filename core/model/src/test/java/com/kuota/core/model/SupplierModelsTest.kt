package com.kuota.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupplierModelsTest {

    @Test
    fun testExpectedPayoutHelpers() {
        val payout = ExpectedPayout(
            payoutId = "PAYOUT-01",
            type = "ADVANCE_60",
            status = "PENDING"
        )
        assertTrue(payout.isAdvance)
        assertFalse(payout.isSettled)

        val settledPayout = payout.copy(status = "SETTLED")
        assertTrue(settledPayout.isSettled)
    }

    @Test
    fun testPosSavingsCalculation() {
        // Ventas S/ 10,000 (1,000,000 céntimos)
        val salesCents = 1_000_000L
        val posCommission = (salesCents * 35) / 1000 // 3.5% = 35,000
        val kuotaCommission = (salesCents * 20) / 1000 // 2.0% = 20,000
        val planCost = 9_440L // S/ 94.40
        val totalKuotaCost = kuotaCommission + planCost // 29,440
        val savings = posCommission - totalKuotaCost // 5,560 céntimos

        val calculation = PosSavingsCalculation(
            monthlyCardSalesCents = salesCents,
            posCommissionCents = posCommission,
            kuotaCommissionCents = kuotaCommission,
            planSubscriptionCents = planCost,
            totalKuotaCostCents = totalKuotaCost,
            monthlyNetSavingsCents = savings,
            isAboveThreshold = salesCents >= 630_000L
        )

        assertEquals(35_000L, calculation.posCommissionCents)
        assertEquals(20_000L, calculation.kuotaCommissionCents)
        assertEquals(5_560L, calculation.monthlyNetSavingsCents)
        assertTrue(calculation.isAboveThreshold)
    }
}

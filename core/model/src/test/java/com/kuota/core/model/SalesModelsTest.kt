package com.kuota.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SalesModelsTest {

    @Test
    fun `sale record formats money in soles correctly`() {
        val sale = SaleRecord(
            id = "sale_123",
            amountCents = 150_50L, // S/ 150.50
            timestampMs = 1700000000000L,
            source = SaleSource.YAPE,
            isVerified = true,
            txHash = "hash123",
            customerSnippet = "Juan P."
        )

        assertEquals("S/ 150.50", sale.amountInSoles)
        assertTrue(sale.isVerified)
        assertEquals(SaleSource.YAPE, sale.source)
    }

    @Test
    fun `sales month summary calculates totals and formatting`() {
        val summary = SalesMonthSummary(
            totalVerifiedCents = 350_000L, // S/ 3,500.00
            totalUnverifiedCents = 50_000L, // S/ 500.00
            totalSalesCents = 400_000L, // S/ 4,000.00
            verifiedCount = 20,
            unverifiedCount = 5
        )

        assertEquals("S/ 4,000.00", summary.totalInSoles)
        assertEquals("S/ 3,500.00", summary.verifiedInSoles)
        assertEquals("S/ 500.00", summary.unverifiedInSoles)
        assertEquals(25, summary.verifiedCount + summary.unverifiedCount)
    }

    @Test
    fun `score evaluation encapsulates versioned rules and components`() {
        val eval = ScoreEvaluation(
            score = 750,
            level = 2,
            previousScore = 680,
            previousLevel = 1,
            components = mapOf(
                "verified_volume" to 420,
                "punctuality" to 250,
                "longevity" to 80
            ),
            flags = listOf("VERIFIED_SELLER", "ELIGIBLE_FOR_PROMOTION"),
            evaluatedAtMs = 1730000000000L,
            policyVersion = "scoring_v2_2026",
            creditLimitCents = 140_000L // S/ 1,400.00
        )

        assertEquals(750, eval.score)
        assertEquals(2, eval.level)
        assertTrue(eval.flags.contains("VERIFIED_SELLER"))
        assertEquals(420, eval.components["verified_volume"])
    }
}

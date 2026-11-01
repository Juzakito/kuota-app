package com.kuota.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests unitarios para las reglas de negocio de configuración (colección config)
// según AGENTS.md Regla 6.
class ConfigModelsTest {

    @Test
    fun `test level tier down payment percent conversion`() {
        val tier1 = LevelTierConfig(
            level = 1,
            name = "Nivel 1",
            maxCreditCents = 70_000L,
            downPaymentBasisPoints = 3000 // 30.00%
        )
        assertEquals(30.0f, tier1.downPaymentPercent, 0.001f)
        assertEquals(70_000L, tier1.maxCreditCents)

        val tier2 = LevelTierConfig(
            level = 2,
            name = "Nivel 2",
            maxCreditCents = 120_000L,
            downPaymentBasisPoints = 2500 // 25.00%
        )
        assertEquals(25.0f, tier2.downPaymentPercent, 0.001f)
    }

    @Test
    fun `test fees config matches PRD parameters`() {
        val fees = FeesConfig(
            buyerFinancingBasisPoints = 500, // 5.00%
            providerTransactionBasisPoints = 200, // 2.00%
            minPurchaseCents = 19_900L // S/ 199.00
        )
        assertEquals(500, fees.buyerFinancingBasisPoints)
        assertEquals(200, fees.providerTransactionBasisPoints)
        assertEquals(19_900L, fees.minPurchaseCents)
    }

    @Test
    fun `test terms config weekly installments cadence`() {
        val term15 = TermOptionConfig(days = 15, installmentsCount = 2, cadenceDays = 7)
        val term30 = TermOptionConfig(days = 30, installmentsCount = 4, cadenceDays = 7)
        val term60 = TermOptionConfig(days = 60, installmentsCount = 8, cadenceDays = 7)

        assertEquals(2, term15.installmentsCount)
        assertEquals(4, term30.installmentsCount)
        assertEquals(8, term60.installmentsCount)
        assertEquals(7, term30.cadenceDays)
    }
}

package com.kouta.app.feature.payments

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KuotaCardCatalogTest {

    @Test
    fun `tiers are ordered from basic Classic to Black Infinite`() {
        val ids = KuotaCardCatalog.map { it.id }
        assertEquals(
            listOf(
                KuotaCardId.CLASSIC,
                KuotaCardId.GOLD,
                KuotaCardId.PLATINUM,
                KuotaCardId.BLACK_INFINITE
            ),
            ids
        )
    }

    @Test
    fun `credit limits strictly increase across tiers`() {
        val maxLimits = KuotaCardCatalog.map { it.maxCreditCents }
        assertTrue(maxLimits.zipWithNext().all { (a, b) -> a < b })
    }

    @Test
    fun `resolveCurrentDemoTierId maps paid installments to expected tier`() {
        assertEquals(KuotaCardId.CLASSIC, resolveCurrentDemoTierId(0))
        assertEquals(KuotaCardId.GOLD, resolveCurrentDemoTierId(1))
        assertEquals(KuotaCardId.PLATINUM, resolveCurrentDemoTierId(2))
        assertEquals(KuotaCardId.BLACK_INFINITE, resolveCurrentDemoTierId(3))
        assertEquals(KuotaCardId.BLACK_INFINITE, resolveCurrentDemoTierId(6))
    }
}

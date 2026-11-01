package com.kuota.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreditPurchaseModelsTest {

    @Test
    fun testPurchaseStatusParsing() {
        val purchase = CreditPurchase(
            purchaseId = "PUR-001",
            status = "DOWN_PAYMENT_CONFIRMED"
        )
        assertEquals(PurchaseStatus.DOWN_PAYMENT_CONFIRMED, purchase.purchaseStatus)
    }

    @Test
    fun testExpirationCheck() {
        val pastTime = System.currentTimeMillis() - 1000
        val expiredPurchase = CreditPurchase(
            purchaseId = "PUR-002",
            status = "QUOTED",
            expiresAtEpochMillis = pastTime
        )
        assertTrue(expiredPurchase.isExpired)

        val futureTime = System.currentTimeMillis() + 15 * 60 * 1000
        val activePurchase = CreditPurchase(
            purchaseId = "PUR-003",
            status = "QUOTED",
            expiresAtEpochMillis = futureTime
        )
        assertFalse(activePurchase.isExpired)
    }

    @Test
    fun testInstallmentStatusHelpers() {
        val pending = Installment(installmentId = "inst_1", status = "PENDING")
        assertTrue(pending.isPending)
        assertFalse(pending.isPaid)

        val paid = Installment(installmentId = "inst_2", status = "PAID")
        assertTrue(paid.isPaid)
        assertFalse(paid.isPending)
    }
}

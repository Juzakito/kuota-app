package com.kuota.core.data.parser

import com.kuota.core.model.SaleSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BankNotificationParserTest {

    @Test
    fun `parseAmountToCents converts integer and decimal soles to exact cents without Float`() {
        assertEquals(5000L, BankNotificationParser.parseAmountToCents("50.00"))
        assertEquals(5000L, BankNotificationParser.parseAmountToCents("50,00"))
        assertEquals(1250L, BankNotificationParser.parseAmountToCents("12.5"))
        assertEquals(12050L, BankNotificationParser.parseAmountToCents("120.50"))
        assertEquals(30000L, BankNotificationParser.parseAmountToCents("300"))
        assertEquals(99L, BankNotificationParser.parseAmountToCents("0.99"))
        assertEquals(0L, BankNotificationParser.parseAmountToCents("invalid"))
    }

    @Test
    fun `parses Yape notification successfully and masks customer name`() {
        val packageName = "com.bcp.innovacxion.yapeapp"
        val title = "Yape"
        val text = "Carlos Mendoza te envió S/ 85.50"
        val timestamp = 1730000000000L

        val result = BankNotificationParser.parseNotification(packageName, title, text, timestamp)

        assertNotNull(result)
        assertEquals(8550L, result!!.amountCents)
        assertEquals(SaleSource.YAPE, result.source)
        assertTrue(result.isVerified)
        assertTrue(result.customerSnippet?.contains("C***") == true)
        assertFalse(result.txHash.isBlank())
    }

    @Test
    fun `parses Plin Interbank notification successfully`() {
        val packageName = "pe.com.interbank.mobilebanking"
        val title = "Plin"
        val text = "Recibiste S/ 250.00 de Maria Flores"
        val timestamp = 1730000000000L

        val result = BankNotificationParser.parseNotification(packageName, title, text, timestamp)

        assertNotNull(result)
        assertEquals(25000L, result!!.amountCents)
        assertEquals(SaleSource.PLIN, result.source)
        assertTrue(result.isVerified)
    }

    @Test
    fun `rejects non whitelisted app notifications`() {
        val packageName = "com.whatsapp"
        val title = "Mensaje nuevo"
        val text = "Te envié S/ 100 por el pedido"

        val result = BankNotificationParser.parseNotification(packageName, title, text)
        assertNull(result)
    }

    @Test
    fun `idempotent txHash generates identical hash for duplicate notifications within same minute`() {
        val hash1 = BankNotificationParser.generateTxHash(
            packageName = "com.bcp.innovacxion.yapeapp",
            amountCents = 4000L,
            timestampMs = 1730000001000L,
            senderSnippet = "A***"
        )
        val hash2 = BankNotificationParser.generateTxHash(
            packageName = "com.bcp.innovacxion.yapeapp",
            amountCents = 4000L,
            timestampMs = 1730000030000L, // 29 seconds later in same minute
            senderSnippet = "A***"
        )
        assertEquals(hash1, hash2)
    }
}

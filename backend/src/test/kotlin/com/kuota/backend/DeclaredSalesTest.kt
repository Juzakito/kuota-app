package com.kuota.backend

import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeclaredSalesTest {

    @TempDir
    lateinit var directory: Path

    private val now = Instant.parse("2026-10-20T18:00:00Z")

    @Test
    fun `sync is idempotent and applies revisions`() {
        val store = DeclaredSalesStore(directory.resolve("sales.json"))
        val sale = sale(revision = 0, amountCents = 15_000)

        val first = store.sync("user-a", listOf(sale), now)
        assertEquals(1, first.accepted)
        assertEquals(0, first.duplicates)
        assertTrue(first.rejected.isEmpty())

        val replay = store.sync("user-a", listOf(sale), now)
        assertEquals(0, replay.accepted)
        assertEquals(1, replay.duplicates)

        val correction = sale.copy(amountCents = 18_000, revision = 1)
        val updated = store.sync("user-a", listOf(correction), now.plusSeconds(60))
        assertEquals(1, updated.accepted)

        val stored = store.listForUser("user-a").single()
        assertEquals(18_000, stored.amountCents)
        assertEquals(1, stored.revision)
        assertFalse(stored.verified)
    }

    @Test
    fun `conflicting or stale revisions are rejected`() {
        val store = DeclaredSalesStore(directory.resolve("sales.json"))
        val sale = sale(revision = 1, amountCents = 20_000)
        store.sync("user-a", listOf(sale), now)

        val conflict = store.sync("user-a", listOf(sale.copy(amountCents = 21_000)), now)
        assertEquals("SALE_REVISION_CONFLICT", conflict.rejected.single().code)

        val stale = store.sync("user-a", listOf(sale.copy(revision = 0)), now)
        assertEquals("SALE_REVISION_STALE", stale.rejected.single().code)
        assertEquals(20_000, store.listForUser("user-a").single().amountCents)
    }

    @Test
    fun `tombstones are stored and users cannot see each other sales`() {
        val store = DeclaredSalesStore(directory.resolve("sales.json"))
        store.sync("user-a", listOf(sale(revision = 0)), now)
        store.sync("user-a", listOf(sale(revision = 1, deleted = true)), now)
        store.sync("user-b", listOf(sale(revision = 0, clientId = "sale-user-b-0001")), now)

        assertTrue(store.listForUser("user-a").single().deleted)
        assertEquals(1, store.listForUser("user-b").size)
        assertEquals("sale-user-b-0001", store.listForUser("user-b").single().clientSaleId)
    }

    @Test
    fun `invalid sales are rejected without persisting`() {
        val store = DeclaredSalesStore(directory.resolve("sales.json"))
        val outcome = store.sync(
            "user-a",
            listOf(
                sale(revision = 0, amountCents = 0),
                sale(revision = -1),
                sale(revision = 0, occurredAt = now.plusSeconds(3_600).toString()),
                sale(revision = 0, source = "YAPE"),
                sale(revision = 0, clientId = "bad id")
            ),
            now
        )

        assertEquals(0, outcome.accepted)
        assertEquals(setOf("SALE_AMOUNT_INVALID", "SALE_REVISION_INVALID", "SALE_DATE_FUTURE", "SALE_SOURCE_NOT_SUPPORTED", "SALE_ID_INVALID"), outcome.rejected.map { it.code }.toSet())
        assertTrue(store.listForUser("user-a").isEmpty())
    }

    @Test
    fun `sales survive store restarts`() {
        val file = directory.resolve("sales.json")
        DeclaredSalesStore(file).sync("user-a", listOf(sale(revision = 0)), now)

        val reopened = DeclaredSalesStore(file)
        assertEquals(1, reopened.listForUser("user-a").size)
    }

    private fun sale(
        revision: Int,
        clientId: String = "sale-demo-0001",
        amountCents: Long = 12_500,
        occurredAt: String = now.minusSeconds(3_600).toString(),
        source: String = "MANUAL",
        deleted: Boolean = false
    ) = DeclaredSaleInput(
        clientSaleId = clientId,
        amountCents = amountCents,
        occurredAt = occurredAt,
        source = source,
        revision = revision,
        deleted = deleted
    )
}

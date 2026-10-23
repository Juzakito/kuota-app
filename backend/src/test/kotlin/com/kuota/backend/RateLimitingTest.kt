package com.kuota.backend

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RateLimitingTest {

    @Test
    fun `allows requests up to the limit and then blocks`() {
        var now = 0L
        val limiter = InMemoryRateLimiter(clock = { now })

        assertTrue(limiter.check("key", limit = 2, windowSeconds = 60).allowed)
        assertTrue(limiter.check("key", limit = 2, windowSeconds = 60).allowed)

        val blocked = limiter.check("key", limit = 2, windowSeconds = 60)
        assertFalse(blocked.allowed)
        assertEquals(60, blocked.retryAfterSeconds)
    }

    @Test
    fun `requests are counted independently per key`() {
        var now = 0L
        val limiter = InMemoryRateLimiter(clock = { now })

        assertTrue(limiter.check("phone-a", limit = 1, windowSeconds = 60).allowed)
        assertFalse(limiter.check("phone-a", limit = 1, windowSeconds = 60).allowed)
        assertTrue(limiter.check("phone-b", limit = 1, windowSeconds = 60).allowed)
    }

    @Test
    fun `the window resets after the configured duration`() {
        var now = 1_000L
        val limiter = InMemoryRateLimiter(clock = { now })

        assertTrue(limiter.check("key", limit = 1, windowSeconds = 10).allowed)
        assertFalse(limiter.check("key", limit = 1, windowSeconds = 10).allowed)

        now += 10_000
        assertTrue(limiter.check("key", limit = 1, windowSeconds = 10).allowed)
    }

    @Test
    fun `retry after reports the remaining window rounded up`() {
        var now = 1_000L
        val limiter = InMemoryRateLimiter(clock = { now })

        assertTrue(limiter.check("key", limit = 1, windowSeconds = 10).allowed)
        now += 4_500
        val blocked = limiter.check("key", limit = 1, windowSeconds = 10)

        assertFalse(blocked.allowed)
        assertEquals(6, blocked.retryAfterSeconds)
    }

    @Test
    fun `reset clears every bucket`() {
        val limiter = InMemoryRateLimiter(clock = { 0L })

        assertTrue(limiter.check("key", limit = 1, windowSeconds = 60).allowed)
        assertFalse(limiter.check("key", limit = 1, windowSeconds = 60).allowed)

        limiter.reset()
        assertTrue(limiter.check("key", limit = 1, windowSeconds = 60).allowed)
    }
}

package com.kuota.backend

internal data class RateLimitDecision(
    val allowed: Boolean,
    val retryAfterSeconds: Long = 0
)

internal class InMemoryRateLimiter(
    private val clock: () -> Long = System::currentTimeMillis,
    private val maxBuckets: Int = 10_000
) {
    private data class Bucket(val windowStartMillis: Long, var count: Int)

    private val lock = Any()
    private val buckets = mutableMapOf<String, Bucket>()

    fun check(key: String, limit: Int, windowSeconds: Long): RateLimitDecision {
        require(limit > 0) { "limit must be positive." }
        require(windowSeconds > 0) { "windowSeconds must be positive." }

        val now = clock()
        val windowMillis = windowSeconds * 1_000
        synchronized(lock) {
            pruneExpired(now, windowMillis)
            val bucket = buckets[key]
            if (bucket == null || now - bucket.windowStartMillis >= windowMillis) {
                buckets[key] = Bucket(now, 1)
                return RateLimitDecision(true)
            }
            if (bucket.count < limit) {
                bucket.count += 1
                return RateLimitDecision(true)
            }
            val remainingMillis = (bucket.windowStartMillis + windowMillis - now).coerceAtLeast(0)
            return RateLimitDecision(false, ((remainingMillis + 999) / 1_000).coerceAtLeast(1))
        }
    }

    fun reset() {
        synchronized(lock) { buckets.clear() }
    }

    private fun pruneExpired(now: Long, windowMillis: Long) {
        if (buckets.size < maxBuckets) return
        val iterator = buckets.entries.iterator()
        while (iterator.hasNext()) {
            if (now - iterator.next().value.windowStartMillis >= windowMillis) iterator.remove()
        }
    }
}

package com.kouta.app.core.designsystem.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KuotaMotionTest {

    @Test
    fun `resolveDurationMs returns zero when accessibility reduced motion is active`() {
        assertEquals(0, KuotaMotion.resolveDurationMs(KuotaMotion.MICRO_DURATION_MS, 0f))
        assertEquals(0, KuotaMotion.resolveDurationMs(KuotaMotion.SCREEN_ENTER_DURATION_MS, -1f))
    }

    @Test
    fun `resolveDurationMs preserves base durations at normal system scale`() {
        assertEquals(KuotaMotion.MICRO_DURATION_MS, KuotaMotion.resolveDurationMs(KuotaMotion.MICRO_DURATION_MS, 1.0f))
        assertEquals(KuotaMotion.STATE_DURATION_MS, KuotaMotion.resolveDurationMs(KuotaMotion.STATE_DURATION_MS, 1.0f))
        assertEquals(KuotaMotion.SCREEN_ENTER_DURATION_MS, KuotaMotion.resolveDurationMs(KuotaMotion.SCREEN_ENTER_DURATION_MS, 1.0f))
        assertEquals(KuotaMotion.SCREEN_EXIT_DURATION_MS, KuotaMotion.resolveDurationMs(KuotaMotion.SCREEN_EXIT_DURATION_MS, 1.0f))
    }

    @Test
    fun `resolveDurationMs clamps extreme animator scales within fintech safe bounds`() {
        val halfScale = KuotaMotion.resolveDurationMs(200, 0.25f)
        val highScale = KuotaMotion.resolveDurationMs(200, 5.0f)
        assertEquals(100, halfScale)
        assertEquals(300, highScale)
    }

    @Test
    fun `motion timing constants stay within fintech UX guidelines`() {
        assertTrue(KuotaMotion.MICRO_DURATION_MS in 150..250)
        assertTrue(KuotaMotion.STATE_DURATION_MS in 150..250)
        assertTrue(KuotaMotion.SCREEN_ENTER_DURATION_MS in 200..350)
        assertTrue(KuotaMotion.SCREEN_EXIT_DURATION_MS in 150..300)
    }
}

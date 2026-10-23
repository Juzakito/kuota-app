package com.kouta.app.core.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KuotaAnalyticsTest {

    @Before
    fun setUp() {
        KuotaAnalytics.clearForTesting()
    }

    @Test
    fun `catalog contains all 17 PRD section 15 events`() {
        val names = KuotaAnalyticsEvent.entries.map { it.eventName }.toSet()
        assertEquals(17, names.size)
        assertTrue(names.contains("onboarding_started"))
        assertTrue(names.contains("business_profile_completed"))
        assertTrue(names.contains("sales_source_authorized"))
        assertTrue(names.contains("sales_sync_status"))
        assertTrue(names.contains("eligibility_submitted"))
        assertTrue(names.contains("eligibility_result"))
        assertTrue(names.contains("inventory_quote_created"))
        assertTrue(names.contains("offer_viewed"))
        assertTrue(names.contains("offer_accepted"))
        assertTrue(names.contains("supplier_disbursement_status"))
        assertTrue(names.contains("plan_activated"))
        assertTrue(names.contains("installment_viewed"))
        assertTrue(names.contains("payment_attempt_created"))
        assertTrue(names.contains("payment_status_changed"))
        assertTrue(names.contains("supplier_settlement_status"))
        assertTrue(names.contains("support_case_created"))
        assertTrue(names.contains("refund_status_changed"))
    }

    @Test
    fun `sanitizeProperties strips PII keys and sensitive values`() {
        val sanitized = KuotaAnalytics.sanitizeProperties(
            mapOf(
                "risk_tier" to "PROGRESS_10",
                "down_payment_bps" to "1000",
                "dni" to "12345678",
                "phone" to "987654321",
                "email" to "cliente@bodega.pe",
                "token" to "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U",
                "status" to "Correo: cliente@bodega.pe o DNI 45678912"
            )
        )
        assertEquals("PROGRESS_10", sanitized["risk_tier"])
        assertEquals("1000", sanitized["down_payment_bps"])
        assertFalse(sanitized.containsKey("dni"))
        assertFalse(sanitized.containsKey("phone"))
        assertFalse(sanitized.containsKey("email"))
        assertFalse(sanitized.containsKey("token"))
        // Value containing email/DNI digits is also blocked even if key is allowed
        assertFalse(sanitized.containsKey("status"))
    }

    @Test
    fun `track records sanitized event in session history`() {
        val record = KuotaAnalytics.track(
            KuotaAnalyticsEvent.ELIGIBILITY_RESULT,
            mapOf(
                "policy_version" to "DEMO_POLICY_2026_V2",
                "risk_tier" to "PROGRESS_10",
                "down_payment_bps" to "1000",
                "consumer_name" to "Juan Perez"
            )
        )
        assertNotNull(record)
        assertEquals("eligibility_result", record!!.eventName)
        assertEquals("DEMO_POLICY_2026_V2", record.properties["policy_version"])
        assertEquals("PROGRESS_10", record.properties["risk_tier"])
        assertEquals("1000", record.properties["down_payment_bps"])
        assertFalse(record.properties.containsKey("consumer_name"))
        assertEquals(1, KuotaAnalytics.events.value.size)
    }
}

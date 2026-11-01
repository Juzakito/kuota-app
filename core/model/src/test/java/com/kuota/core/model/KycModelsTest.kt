package com.kuota.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KycModelsTest {

    @Test
    fun `KycStatus fromRaw resolves standard values`() {
        assertEquals(KycStatus.UNSUBMITTED, KycStatus.fromRaw("unsubmitted"))
        assertEquals(KycStatus.PENDING, KycStatus.fromRaw("pending"))
        assertEquals(KycStatus.APPROVED, KycStatus.fromRaw("approved"))
        assertEquals(KycStatus.REJECTED, KycStatus.fromRaw("rejected"))
    }

    @Test
    fun `KycStatus fromRaw resolves legacy aliases`() {
        assertEquals(KycStatus.APPROVED, KycStatus.fromRaw("VERIFIED"))
        assertEquals(KycStatus.PENDING, KycStatus.fromRaw("PENDING_REVIEW"))
        assertEquals(KycStatus.REJECTED, KycStatus.fromRaw("REJECTED_INVALID_DNI"))
        assertEquals(KycStatus.UNSUBMITTED, KycStatus.fromRaw(null))
        assertEquals(KycStatus.UNSUBMITTED, KycStatus.fromRaw("unknown_state"))
    }

    @Test
    fun `KycDraft defaults to uncommitted state and step 1`() {
        val draft = KycDraft()
        assertEquals(1, draft.currentStep)
        assertEquals("comprador", draft.role)
        assertFalse(draft.termsAgreed)
        assertFalse(draft.privacyAgreed)
        assertEquals("2026-v1", draft.termsVersion)
    }

    @Test
    fun `TermsConsent records version and agreement`() {
        val consent = TermsConsent(agreed = true, version = "2026-v1", consentedAt = "2026-11-01T12:00:00Z")
        assertTrue(consent.agreed)
        assertEquals("2026-v1", consent.version)
    }
}

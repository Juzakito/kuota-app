package com.kuota.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ProviderProfile(
    val providerUid: String = "",
    val businessName: String = "",
    val category: String = "General",
    val ruc: String? = null,
    val phone: String = "",
    val address: String = "",
    val district: String = "Lima",
    val department: String = "Lima",
    val isVerified: Boolean = true,
    val staticQrPayload: String = "",
    val activePlan: String = "BASIC" // "BASIC" | "PRO"
)

@Serializable
data class PaymentLink(
    val linkId: String = "",
    val providerUid: String = "",
    val supplierName: String = "",
    val amountCents: Long = 0L,
    val feeCents: Long = 0L,
    val netCents: Long = 0L,
    val clientPhone: String = "",
    val clientDni: String? = null,
    val concept: String? = null,
    val shortUrl: String = "",
    val whatsappUrl: String = "",
    val status: String = "ACTIVE", // ACTIVE, PAID, EXPIRED
    val expiresAt: String = "",
    val createdAt: String = ""
)

@Serializable
data class ExpectedPayout(
    val payoutId: String = "",
    val purchaseId: String = "",
    val amountCents: Long = 0L,
    val type: String = "ADVANCE_60", // ADVANCE_60 | DEFERRED_WEEKLY
    val dueDate: String = "",
    val status: String = "PENDING", // PENDING, SETTLED, FAILED
    val bankAccountMasked: String = "BCP Soles ****4812"
) {
    val isAdvance: Boolean get() = type == "ADVANCE_60"
    val isSettled: Boolean get() = status == "SETTLED"
}

@Serializable
data class SupplierPayoutSummary(
    val totalPendingAdvanceCents: Long = 0L,
    val totalPendingDeferredCents: Long = 0L,
    val totalExpectedCents: Long = 0L,
    val pendingPayouts: List<ExpectedPayout> = emptyList()
)

@Serializable
data class PosSavingsCalculation(
    val monthlyCardSalesCents: Long = 0L,
    val posCommissionCents: Long = 0L,
    val kuotaCommissionCents: Long = 0L,
    val planSubscriptionCents: Long = 9_440L, // S/ 80 + IGV = 94.40
    val totalKuotaCostCents: Long = 0L,
    val monthlyNetSavingsCents: Long = 0L,
    val isAboveThreshold: Boolean = true,
    val thresholdWarning: String? = null
)

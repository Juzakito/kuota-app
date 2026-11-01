package com.kuota.core.model

import kotlinx.serialization.Serializable

/**
 * Resumen consolidado del usuario para Home en 1 sola lectura:
 * `users/{uid}/summary/current` (PRD §6.1 y AGENTS.md Criterios de Calidad).
 */
@Serializable
data class UserSummary(
    val uid: String = "",
    val level: Int = 1,
    val maxCreditCents: Long = 70_000L,
    val usedCreditCents: Long = 0L,
    val availableCreditCents: Long = 70_000L,
    val nextDueInstallmentCents: Long? = null,
    val nextDueDate: String? = null,
    val overdueCount: Int = 0,
    val kycStatus: String = "none", // 'none', 'pending', 'approved', 'rejected'
    val role: String = "buyer",     // 'buyer', 'provider', 'both'
    val businessName: String? = null,
    val riskScore: Int = 650,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isApproved: Boolean get() = kycStatus == "approved"
    val hasAvailableCredit: Boolean get() = availableCreditCents > 0L
    val isOverdue: Boolean get() = overdueCount > 0
}

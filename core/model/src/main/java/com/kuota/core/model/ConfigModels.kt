package com.kuota.core.model

import kotlinx.serialization.Serializable

// Modelos de configuración de negocio de Firestore (colección config).
// Regla dura de AGENTS.md:
// "Valores de negocio en configuración (config/levels, config/fees, config/terms,
// config/collections, config/scoring), nunca constantes en código."

@Serializable
data class LevelTierConfig(
    val level: Int = 1,
    val name: String = "Nivel 1",
    val maxCreditCents: Long = 70_000L,
    val downPaymentBasisPoints: Int = 3000, // 30.00%
    val enabled: Boolean = true
) {
    val downPaymentPercent: Float get() = downPaymentBasisPoints / 100f
}

@Serializable
data class LevelsConfig(
    val version: String = "2.0.0",
    val tiers: List<LevelTierConfig> = emptyList()
)

@Serializable
data class FeesConfig(
    val version: String = "2.0.0",
    val buyerFinancingBasisPoints: Int = 500, // 5.00% fijo sobre financiado
    val providerTransactionBasisPoints: Int = 200, // 2.00% comisión Kuota Payments
    val minPurchaseCents: Long = 19_900L, // S/ 199.00
    val basicMonthlySubCents: Long = 8_000L, // S/ 80.00 + IGV
    val proMonthlySubCents: Long = 12_000L  // S/ 120.00 + IGV
)

@Serializable
data class TermOptionConfig(
    val days: Int = 30,
    val installmentsCount: Int = 4,
    val cadenceDays: Int = 7,
    val label: String = "30 días",
    val enabled: Boolean = true
)

@Serializable
data class TermsConfig(
    val version: String = "2.0.0",
    val terms: List<TermOptionConfig> = emptyList()
)

@Serializable
data class ScoringConfig(
    val version: String = "2.0.0",
    val minScoreToPromote: Int = 720,
    val minConsecutiveOnTimePayments: Int = 3,
    val maxLateDaysBeforeDemote: Int = 7
)

package com.kuota.core.model

import kotlinx.serialization.Serializable

/**
 * Fuentes de ventas admitidas en Kuota App según PRD §8.3 y AGENTS.md.
 */
@Serializable
enum class SaleSource {
    YAPE,
    PLIN,
    BCP,
    BBVA,
    INTERBANK,
    SCOTIABANK,
    MERCADO_PAGO,
    MANUAL_OCR,
    MANUAL_DECLARED
}

/**
 * Registro de venta individual inmutable.
 * Cumple con la regla de calidad de AGENTS.md:
 * - Dinero estrictamente en céntimos (Long).
 * - Cero PII: no almacena el texto crudo de la notificación.
 * - Deduplicación garantizada mediante txHash idempotente.
 */
@Serializable
data class SaleRecord(
    val id: String = "",
    val amountCents: Long = 0L,
    val timestampMs: Long = 0L,
    val source: SaleSource = SaleSource.MANUAL_DECLARED,
    val isVerified: Boolean = false,
    val txHash: String = "",
    val customerSnippet: String? = null,
    val productName: String? = null,
    val category: String? = null
) {
    val amountInSoles: String get() = amountCents.toPriceString()
}

/**
 * Resumen consolidado de ventas del mes en tiempo real.
 */
@Serializable
data class SalesMonthSummary(
    val totalVerifiedCents: Long = 0L,
    val totalUnverifiedCents: Long = 0L,
    val totalSalesCents: Long = 0L,
    val verifiedCount: Int = 0,
    val unverifiedCount: Int = 0
) {
    val totalInSoles: String get() = totalSalesCents.toPriceString()
    val verifiedInSoles: String get() = totalVerifiedCents.toPriceString()
    val unverifiedInSoles: String get() = totalUnverifiedCents.toPriceString()
}

/**
 * Producto del catálogo del vendedor.
 */
@Serializable
data class ProductItem(
    val id: String = "",
    val name: String = "",
    val priceCents: Long = 0L,
    val category: String = "General",
    val isAvailable: Boolean = true
) {
    val priceInSoles: String get() = priceCents.toPriceString()
}

/**
 * Evaluación de score crediticio Kuota basada en reglas versionadas.
 */
@Serializable
data class ScoreEvaluation(
    val score: Int = 300,
    val level: Int = 1,
    val previousScore: Int = 300,
    val previousLevel: Int = 1,
    val components: Map<String, Int> = emptyMap(),
    val flags: List<String> = emptyList(),
    val evaluatedAtMs: Long = 0L,
    val policyVersion: String = "scoring_v2_2026",
    val creditLimitCents: Long = 70_000L
)

/**
 * Estado de recordatorios y cobranza en el cron diario.
 */
enum class InstallmentReminderStage {
    T_MINUS_3, // 3 días antes de vencimiento
    T_MINUS_1, // 1 día antes
    T_ZERO,    // Día del vencimiento
    T_PLUS_1,  // 1 día después (mora inicial)
    T_PLUS_3,  // 3 días después (mora crítica)
    NONE
}

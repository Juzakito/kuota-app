package com.kouta.app.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Sistema de 6 Niveles de Crédito de Kuota Perú según la sección 3 del PRD v0.3.
 * Reemplaza el catálogo tradicional de tarjetas por una línea de crédito dinámica y progresiva.
 */
enum class CreditLevel(
    val levelNumber: Int,
    val title: String,
    val minLineCents: Long,
    val maxLineCents: Long,
    val downPaymentPercentage: Int,
    val maxTermDescription: String,
    val availableTermsDays: List<Int>,
    val accentColor: Color
) {
    NIVEL_1(
        levelNumber = 1,
        title = "Nivel 1",
        minLineCents = 300_00L,   // S/ 300.00
        maxLineCents = 700_00L,   // S/ 700.00
        downPaymentPercentage = 50,
        maxTermDescription = "3, 7 o 15 días",
        availableTermsDays = listOf(3, 7, 15),
        accentColor = Color(0xFF2D5FCC) // Azul Kuota Strong
    ),
    NIVEL_2(
        levelNumber = 2,
        title = "Nivel 2",
        minLineCents = 700_00L,   // S/ 700.00
        maxLineCents = 1_500_00L, // S/ 1,500.00
        downPaymentPercentage = 40,
        maxTermDescription = "hasta 30 días",
        availableTermsDays = listOf(7, 15, 30),
        accentColor = Color(0xFF0E7490) // Turquesa oscuro
    ),
    NIVEL_3(
        levelNumber = 3,
        title = "Nivel 3",
        minLineCents = 1_500_00L, // S/ 1,500.00
        maxLineCents = 2_500_00L, // S/ 2,500.00
        downPaymentPercentage = 30,
        maxTermDescription = "hasta 60 días",
        availableTermsDays = listOf(15, 30, 60),
        accentColor = Color(0xFF10B981) // Verde esmeralda
    ),
    NIVEL_4(
        levelNumber = 4,
        title = "Nivel 4",
        minLineCents = 2_500_00L, // S/ 2,500.00
        maxLineCents = 5_000_00L, // S/ 5,000.00
        downPaymentPercentage = 20,
        maxTermDescription = "hasta 3 meses",
        availableTermsDays = listOf(30, 60, 90),
        accentColor = Color(0xFFF59E0B) // Ámbar cálido
    ),
    NIVEL_5(
        levelNumber = 5,
        title = "Nivel 5",
        minLineCents = 5_000_00L, // S/ 5,000.00
        maxLineCents = 7_000_00L, // S/ 7,000.00
        downPaymentPercentage = 10,
        maxTermDescription = "hasta 5 meses",
        availableTermsDays = listOf(30, 60, 90, 150),
        accentColor = Color(0xFF8B5CF6) // Púrpura financiero
    ),
    NIVEL_6(
        levelNumber = 6,
        title = "Nivel 6",
        minLineCents = 7_000_00L,  // S/ 7,000.00
        maxLineCents = 10_000_00L, // S/ 10,000.00
        downPaymentPercentage = 0,
        maxTermDescription = "hasta 1 año",
        availableTermsDays = listOf(30, 60, 90, 180, 365),
        accentColor = Color(0xFF06B6D4) // Cian brillante
    );

    /**
     * Calcula la cuota inicial en centavos enteros para este nivel.
     */
    fun calculateDownPaymentCents(totalAmountCents: Long): Long {
        if (downPaymentPercentage <= 0) return 0L
        return (totalAmountCents * downPaymentPercentage) / 100
    }

    /**
     * Calcula el saldo neto a financiar en cuotas.
     */
    fun calculateFinancedAmountCents(totalAmountCents: Long): Long {
        return totalAmountCents - calculateDownPaymentCents(totalAmountCents)
    }

    /**
     * Calcula el monto que Kuota liquida al proveedor dentro de 48 horas (PRD sección 10).
     * Fórmula: pago de Kuota = 98% × venta − cuota inicial.
     */
    fun calculateSupplierPayout48hCents(totalAmountCents: Long, downPaymentCents: Long): Long {
        val totalSupplierShare98 = (totalAmountCents * 98L) / 100L
        return (totalSupplierShare98 - downPaymentCents).coerceAtLeast(0L)
    }

    val downPaymentPercent: Int get() = downPaymentPercentage
    val maxAmountCents: Long get() = maxLineCents
    val minMonthlySalesCents: Long get() = minLineCents
    val maxTermDays: Int get() = availableTermsDays.last()
    val badgeColorHex: Long get() = accentColor.value.toLong()

    companion object {
        fun fromLevelNumber(number: Int): CreditLevel = when (number) {
            1 -> NIVEL_1
            2 -> NIVEL_2
            3 -> NIVEL_3
            4 -> NIVEL_4
            5 -> NIVEL_5
            6 -> NIVEL_6
            else -> NIVEL_1
        }

        fun fromNumber(number: Int): CreditLevel = fromLevelNumber(number)

        val LEVEL_1 get() = NIVEL_1
        val LEVEL_2 get() = NIVEL_2
        val LEVEL_3 get() = NIVEL_3
        val LEVEL_4 get() = NIVEL_4
        val LEVEL_5 get() = NIVEL_5
        val LEVEL_6 get() = NIVEL_6

        /**
         * Todos los vendedores nuevos inician en el Nivel 1 con una línea base de S/ 300.00.
         */
        val INITIAL_LEVEL = NIVEL_1
        const val INITIAL_LINE_CENTS = 300_00L
    }
}

data class PurchaseEligibilityCheck(
    val isEligible: Boolean,
    val downPaymentCents: Long,
    val financedCents: Long,
    val maxTermDays: Int,
    val errorMessage: String? = null
) {
    companion object {
        /**
         * Valida una compra a crédito según las reglas del PRD v0.3:
         * 1. Una sola transacción abierta a la vez (regla 3.2).
         * 2. Monto total dentro de la línea de crédito disponible.
         */
        fun validate(
            totalAmountCents: Long,
            currentLevel: CreditLevel,
            availableLineCents: Long,
            hasActiveOpenTransaction: Boolean
        ): PurchaseEligibilityCheck {
            if (hasActiveOpenTransaction) {
                return PurchaseEligibilityCheck(
                    isEligible = false,
                    downPaymentCents = 0L,
                    financedCents = 0L,
                    maxTermDays = currentLevel.availableTermsDays.last(),
                    errorMessage = "Tienes una transacción abierta. Paga tus cuotas pendientes antes de iniciar otra compra a crédito (Regla 3.2)."
                )
            }

            if (totalAmountCents <= 0L) {
                return PurchaseEligibilityCheck(
                    isEligible = false,
                    downPaymentCents = 0L,
                    financedCents = 0L,
                    maxTermDays = currentLevel.availableTermsDays.last(),
                    errorMessage = "Ingresa un monto válido mayor a S/ 0.00."
                )
            }

            if (totalAmountCents > availableLineCents) {
                val availableFormatted = PurchaseOffer.formatPen(availableLineCents)
                return PurchaseEligibilityCheck(
                    isEligible = false,
                    downPaymentCents = 0L,
                    financedCents = 0L,
                    maxTermDays = currentLevel.availableTermsDays.last(),
                    errorMessage = "El monto excede tu línea disponible de $availableFormatted en ${currentLevel.title}."
                )
            }

            val downPayment = currentLevel.calculateDownPaymentCents(totalAmountCents)
            val financed = currentLevel.calculateFinancedAmountCents(totalAmountCents)

            return PurchaseEligibilityCheck(
                isEligible = true,
                downPaymentCents = downPayment,
                financedCents = financed,
                maxTermDays = currentLevel.availableTermsDays.last(),
                errorMessage = null
            )
        }
    }
}

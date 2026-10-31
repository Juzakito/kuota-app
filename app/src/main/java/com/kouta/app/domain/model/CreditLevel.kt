package com.kouta.app.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Sistema de 6 Niveles de Crédito de Kuota Perú según el PRD Kuota 2.0 (Modelo Cashea B2B).
 * Las cuotas son semanales por defecto.
 * La línea se consume con el Saldo Financiado (Compra - Inicial), no con el total de compra.
 * Comisión plana Kuota: 5% sobre la compra total.
 */
enum class CreditLevel(
    val levelNumber: Int,
    val tierName: String,
    val title: String,
    val minLineCents: Long,
    val maxLineCents: Long,
    val downPaymentPercentage: Int,
    val maxTermDescription: String,
    val defaultInstallmentCount: Int,
    val availableInstallmentCounts: List<Int>,
    val requirementsToUnlock: String,
    val accentColor: Color
) {
    NIVEL_1(
        levelNumber = 1,
        tierName = "Brote",
        title = "Nivel 1 · Brote",
        minLineCents = 300_00L,   // S/ 300.00
        maxLineCents = 700_00L,   // S/ 700.00
        downPaymentPercentage = 50,
        maxTermDescription = "3 cuotas semanales",
        defaultInstallmentCount = 3,
        availableInstallmentCounts = listOf(3),
        requirementsToUnlock = "DNI verificado y cuenta vinculada",
        accentColor = Color(0xFF2457F0) // Action 500
    ),
    NIVEL_2(
        levelNumber = 2,
        tierName = "Raíz",
        title = "Nivel 2 · Raíz",
        minLineCents = 700_00L,   // S/ 700.00
        maxLineCents = 1_500_00L, // S/ 1,500.00
        downPaymentPercentage = 40,
        maxTermDescription = "3 cuotas semanales",
        defaultInstallmentCount = 3,
        availableInstallmentCounts = listOf(3),
        requirementsToUnlock = "3 cuotas puntuales y ventas verificadas",
        accentColor = Color(0xFF0E7490) // Cian profundo
    ),
    NIVEL_3(
        levelNumber = 3,
        tierName = "Tallo",
        title = "Nivel 3 · Tallo",
        minLineCents = 1_500_00L, // S/ 1,500.00
        maxLineCents = 3_000_00L, // S/ 3,000.00
        downPaymentPercentage = 30,
        maxTermDescription = "3 a 4 cuotas semanales",
        defaultInstallmentCount = 3,
        availableInstallmentCounts = listOf(3, 4),
        requirementsToUnlock = "8 cuotas puntuales y umbral de ventas",
        accentColor = Color(0xFF10B981) // Verde esmeralda
    ),
    NIVEL_4(
        levelNumber = 4,
        tierName = "Rama",
        title = "Nivel 4 · Rama",
        minLineCents = 3_000_00L, // S/ 3,000.00
        maxLineCents = 5_000_00L, // S/ 5,000.00
        downPaymentPercentage = 25,
        maxTermDescription = "4 a 6 cuotas semanales",
        defaultInstallmentCount = 4,
        availableInstallmentCounts = listOf(4, 5, 6),
        requirementsToUnlock = "15 cuotas puntuales y umbral de ventas",
        accentColor = Color(0xFFF5A524) // Ámbar cálido
    ),
    NIVEL_5(
        levelNumber = 5,
        tierName = "Copa",
        title = "Nivel 5 · Copa",
        minLineCents = 5_000_00L, // S/ 5,000.00
        maxLineCents = 8_000_00L, // S/ 8,000.00
        downPaymentPercentage = 20,
        maxTermDescription = "6 cuotas semanales",
        defaultInstallmentCount = 6,
        availableInstallmentCounts = listOf(4, 6),
        requirementsToUnlock = "25 cuotas puntuales y umbral de ventas",
        accentColor = Color(0xFF8B5CF6) // Púrpura tech
    ),
    NIVEL_6(
        levelNumber = 6,
        tierName = "Fruto",
        title = "Nivel 6 · Fruto",
        minLineCents = 8_000_00L,  // S/ 8,000.00
        maxLineCents = 12_000_00L, // S/ 12,000.00
        downPaymentPercentage = 10,
        maxTermDescription = "6 a 12 cuotas semanales",
        defaultInstallmentCount = 6,
        availableInstallmentCounts = listOf(6, 8, 10, 12),
        requirementsToUnlock = "40 cuotas puntuales y umbral de ventas",
        accentColor = Color(0xFF5EE0E8) // Cian brillante
    );

    /**
     * Calcula la cuota inicial en centavos enteros para este nivel.
     */
    fun calculateDownPaymentCents(totalAmountCents: Long): Long {
        if (downPaymentPercentage <= 0) return 0L
        return (totalAmountCents * downPaymentPercentage) / 100
    }

    /**
     * Calcula el saldo neto a financiar (Compra - Inicial).
     * Este monto es el que consume la línea de crédito disponible.
     */
    fun calculateFinancedAmountCents(totalAmountCents: Long): Long {
        return totalAmountCents - calculateDownPaymentCents(totalAmountCents)
    }

    /**
     * Calcula la comisión plana de Kuota (5% de la compra total).
     */
    fun calculateKuotaFeeCents(totalAmountCents: Long): Long {
        return (totalAmountCents * 5L) / 100L
    }

    /**
     * Calcula el monto total que pagará el comprador (Inicial + Financiado + Comisión = Compra + 5%).
     */
    fun calculateTotalToPayCents(totalAmountCents: Long): Long {
        return totalAmountCents + calculateKuotaFeeCents(totalAmountCents)
    }

    /**
     * Calcula la cuota semanal regular.
     */
    fun calculateWeeklyInstallmentCents(totalAmountCents: Long, count: Int): Long {
        val installmentsCount = count.coerceAtLeast(1)
        val financed = calculateFinancedAmountCents(totalAmountCents)
        val fee = calculateKuotaFeeCents(totalAmountCents)
        return (financed + fee) / installmentsCount
    }

    /**
     * Calcula la compra máxima que puede realizarse según la línea disponible y el porcentaje de inicial.
     * Fórmula: Compra Máxima = Línea / (1 - % Inicial)
     * Ej: Con S/ 700 disponible y 50% de inicial, compra máxima es S/ 1,400 (S/ 700 inicial + S/ 700 saldo).
     */
    fun calculateMaxPurchaseCents(availableLineCents: Long): Long {
        val remainingFactor = (100 - downPaymentPercentage).coerceAtLeast(1)
        return (availableLineCents * 100L) / remainingFactor
    }

    /**
     * Calcula el monto que Kuota liquida al proveedor dentro de 48 horas (PRD sección 10).
     * Fórmula: pago de Kuota = 98% × venta − cuota inicial recibida.
     */
    fun calculateSupplierPayout48hCents(totalAmountCents: Long, downPaymentCents: Long): Long {
        val totalSupplierShare98 = (totalAmountCents * 98L) / 100L
        return (totalSupplierShare98 - downPaymentCents).coerceAtLeast(0L)
    }

    val downPaymentPercent: Int get() = downPaymentPercentage
    val maxAmountCents: Long get() = maxLineCents
    val minMonthlySalesCents: Long get() = minLineCents
    val availableTermsDays: List<Int> get() = availableInstallmentCounts.map { it * 7 }
    val maxTermDays: Int get() = defaultInstallmentCount * 7
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
         * Todos los vendedores nuevos inician en el Nivel 1 con una línea base de S/ 700.00.
         */
        val INITIAL_LEVEL = NIVEL_1
        const val INITIAL_LINE_CENTS = 700_00L
    }
}

data class PurchaseEligibilityCheck(
    val isEligible: Boolean,
    val downPaymentCents: Long,
    val financedCents: Long,
    val kuotaFeeCents: Long,
    val weeklyInstallmentCents: Long,
    val totalToPayCents: Long,
    val maxPurchaseCents: Long,
    val installmentCount: Int,
    val errorMessage: String? = null
) {
    companion object {
        /**
         * Valida una compra a crédito según las reglas del PRD Kuota 2.0 (Modelo Cashea):
         * 1. Una sola transacción abierta a la vez.
         * 2. El saldo financiado (Compra - Inicial) NO debe superar la línea disponible.
         */
        fun validate(
            totalAmountCents: Long,
            currentLevel: CreditLevel,
            availableLineCents: Long,
            hasActiveOpenTransaction: Boolean,
            installmentCount: Int = currentLevel.defaultInstallmentCount
        ): PurchaseEligibilityCheck {
            val maxPurchase = currentLevel.calculateMaxPurchaseCents(availableLineCents)

            if (hasActiveOpenTransaction) {
                return PurchaseEligibilityCheck(
                    isEligible = false,
                    downPaymentCents = 0L,
                    financedCents = 0L,
                    kuotaFeeCents = 0L,
                    weeklyInstallmentCents = 0L,
                    totalToPayCents = 0L,
                    maxPurchaseCents = maxPurchase,
                    installmentCount = installmentCount,
                    errorMessage = "Tienes una compra abierta en curso. Paga tus cuotas pendientes para liberar línea y realizar otra compra."
                )
            }

            if (totalAmountCents <= 0L) {
                return PurchaseEligibilityCheck(
                    isEligible = false,
                    downPaymentCents = 0L,
                    financedCents = 0L,
                    kuotaFeeCents = 0L,
                    weeklyInstallmentCents = 0L,
                    totalToPayCents = 0L,
                    maxPurchaseCents = maxPurchase,
                    installmentCount = installmentCount,
                    errorMessage = "Ingresa un monto válido mayor a S/ 0.00."
                )
            }

            val downPayment = currentLevel.calculateDownPaymentCents(totalAmountCents)
            val financed = currentLevel.calculateFinancedAmountCents(totalAmountCents)
            val fee = currentLevel.calculateKuotaFeeCents(totalAmountCents)
            val weekly = currentLevel.calculateWeeklyInstallmentCents(totalAmountCents, installmentCount)
            val totalPay = currentLevel.calculateTotalToPayCents(totalAmountCents)

            if (financed > availableLineCents) {
                val maxFormatted = PurchaseOffer.formatPen(maxPurchase)
                return PurchaseEligibilityCheck(
                    isEligible = false,
                    downPaymentCents = downPayment,
                    financedCents = financed,
                    kuotaFeeCents = fee,
                    weeklyInstallmentCents = weekly,
                    totalToPayCents = totalPay,
                    maxPurchaseCents = maxPurchase,
                    installmentCount = installmentCount,
                    errorMessage = "Con tu inicial del ${currentLevel.downPaymentPercentage}% puedes comprar hasta $maxFormatted (Saldo financiado excede tu línea disponible de ${PurchaseOffer.formatPen(availableLineCents)})."
                )
            }

            return PurchaseEligibilityCheck(
                isEligible = true,
                downPaymentCents = downPayment,
                financedCents = financed,
                kuotaFeeCents = fee,
                weeklyInstallmentCents = weekly,
                totalToPayCents = totalPay,
                maxPurchaseCents = maxPurchase,
                installmentCount = installmentCount,
                errorMessage = null
            )
        }
    }
}

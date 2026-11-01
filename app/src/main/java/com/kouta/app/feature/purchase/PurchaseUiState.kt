package com.kouta.app.feature.purchase

import com.kuota.core.model.CreditPurchase
import com.kuota.core.model.PurchaseQuote

enum class PurchaseFlowStep {
    STEP_1_DETAILS,     // Detalle de compra y cálculo de cuotas
    STEP_2_WAITING,     // Pago de inicial y espera a confirmación del proveedor (TTL 15 min)
    STEP_3_CONFIRM,     // Aprobación final del comprador
    STEP_SUCCESS        // Compra exitosa y crédito activado
}

data class PurchaseUiState(
    val currentStep: PurchaseFlowStep = PurchaseFlowStep.STEP_1_DETAILS,
    val amountInput: String = "1000",
    val amountCents: Long = 100_000L,
    val supplierId: String = "SUPP-001",
    val supplierName: String = "Distribuidora San Juan MYPE",
    val selectedInstallmentsCount: Int = 4,
    val availableInstallmentOptions: List<Int> = listOf(3, 4, 6),
    val quote: PurchaseQuote? = null,
    val activePurchase: CreditPurchase? = null,
    val remainingSeconds: Int = 900, // 15 minutos en segundos
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEligible: Boolean = true
)

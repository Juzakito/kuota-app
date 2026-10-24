package com.kouta.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PurchasePhase {
    PHASE_1_DOWN_PAYMENT_PENDING,
    PHASE_1_DOWN_PAYMENT_SUBMITTED,
    PHASE_2_INSTALLMENTS_SELECTION,
    ACTIVE_IN_REPAYMENT,
    COMPLETED,
    CANCELLED
}

@Serializable
data class CreditInstallment(
    val number: Int,
    val amountCents: Long,
    val dueEpochMillis: Long,
    val isPaid: Boolean = false,
    val paidAtEpochMillis: Long? = null
)

@Serializable
data class CreditPurchase(
    val id: String,
    val supplierId: String,
    val supplierName: String,
    val supplierPhone: String = "",
    val supplierPaymentDetails: String = "Yape / Plin / BCP",
    val buyerId: String = "",
    val buyerName: String = "",
    val buyerPhone: String = "",
    val totalAmountCents: Long,
    val creditLevelNumber: Int,
    val downPaymentPercent: Int,
    val downPaymentCents: Long,
    val financedAmountCents: Long,
    val supplierPayout48hCents: Long,
    val kuotaFeeCents: Long,
    val receiptUri: String? = null,
    val receiptUploadedAtEpochMillis: Long? = null,
    val phase: PurchasePhase = PurchasePhase.PHASE_1_DOWN_PAYMENT_PENDING,
    val selectedTermDays: Int? = null,
    val installmentCount: Int? = null,
    val installments: List<CreditInstallment> = emptyList(),
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val confirmedBySupplierAtEpochMillis: Long? = null,
    val completedAtEpochMillis: Long? = null
) {
    val isOpen: Boolean
        get() = phase in setOf(
            PurchasePhase.PHASE_1_DOWN_PAYMENT_PENDING,
            PurchasePhase.PHASE_1_DOWN_PAYMENT_SUBMITTED,
            PurchasePhase.PHASE_2_INSTALLMENTS_SELECTION,
            PurchasePhase.ACTIVE_IN_REPAYMENT
        )

    val nextPendingInstallment: CreditInstallment?
        get() = installments.firstOrNull { !it.isPaid }

    val totalPaidInstallments: Int
        get() = installments.count { it.isPaid }
}

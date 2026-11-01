package com.kuota.core.model

import kotlinx.serialization.Serializable

/**
 * Estados del ciclo de vida de una compra al crédito (PRD §8.5 & AGENTS.md Fase 2).
 * TTL de 15 minutos entre QUOTED y confirmaciones.
 */
enum class PurchaseStatus {
    QUOTED,                   // Cotizada por el comprador, esperando aprobación de inicial
    DOWN_PAYMENT_CONFIRMED,   // Inicial confirmada por el proveedor, esperando confirmación del comprador
    CONFIRMED,                // Compra confirmada por ambas partes, cuotas activas
    CANCELLED,                // Cancelada por el usuario o proveedor
    EXPIRED                   // Expirada por superar el TTL de 15 minutos
}

@Serializable
data class InstallmentScheduleItem(
    val installmentNumber: Int = 1,
    val dueDate: String = "",
    val amountCents: Long = 0L,
    val status: String = "PENDING" // PENDING, PAID, OVERDUE
)

@Serializable
data class PurchaseQuote(
    val totalAmountCents: Long = 0L,
    val downPaymentPercent: Int = 30,
    val downPaymentCents: Long = 0L,
    val financedAmountCents: Long = 0L,
    val buyerFeePercent: Int = 5,
    val buyerFeeCents: Long = 0L,
    val totalDebtCents: Long = 0L,
    val installmentsCount: Int = 4,
    val installments: List<InstallmentScheduleItem> = emptyList(),
    val providerFeePercent: Int = 2,
    val providerFeeCents: Long = 0L,
    val providerNetCents: Long = 0L,
    val providerAdvancePercent: Int = 60,
    val providerAdvanceCents: Long = 0L,
    val providerDeferredCents: Long = 0L,
    val calculatedAt: String = ""
)

@Serializable
data class CreditPurchase(
    val purchaseId: String = "",
    val buyerUid: String = "",
    val providerUid: String = "",
    val supplierName: String = "",
    val notes: String? = null,
    val status: String = "QUOTED",
    val totalAmountCents: Long = 0L,
    val downPaymentCents: Long = 0L,
    val downPaymentPercent: Int = 30,
    val financedAmountCents: Long = 0L,
    val buyerFeeCents: Long = 0L,
    val totalDebtCents: Long = 0L,
    val installmentsCount: Int = 4,
    val providerFeeCents: Long = 0L,
    val providerNetCents: Long = 0L,
    val providerAdvanceCents: Long = 0L,
    val providerDeferredCents: Long = 0L,
    val quote: PurchaseQuote? = null,
    val expiresAtEpochMillis: Long = 0L,
    val createdAtEpochMillis: Long = 0L,
    val confirmedAtEpochMillis: Long? = null
) {
    val purchaseStatus: PurchaseStatus
        get() = try {
            PurchaseStatus.valueOf(status)
        } catch (e: Exception) {
            PurchaseStatus.QUOTED
        }

    val isExpired: Boolean
        get() = expiresAtEpochMillis > 0 && System.currentTimeMillis() > expiresAtEpochMillis && (status == "QUOTED" || status == "DOWN_PAYMENT_CONFIRMED")
}

@Serializable
data class Installment(
    val installmentId: String = "",
    val purchaseId: String = "",
    val buyerUid: String = "",
    val providerUid: String = "",
    val supplierName: String = "",
    val installmentNumber: Int = 1,
    val totalInstallments: Int = 4,
    val dueDate: String = "",
    val amountCents: Long = 0L,
    val status: String = "PENDING", // PENDING, PAID, OVERDUE
    val paidAt: String? = null
) {
    val isPaid: Boolean get() = status == "PAID"
    val isOverdue: Boolean get() = status == "OVERDUE"
    val isPending: Boolean get() = status == "PENDING"
}

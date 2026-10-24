package com.kouta.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentLinkStatus(val label: String) {
    GENERADO("Generado"),
    ENVIADO("Enviado"),
    PAGADO("Pagado"),
    VENCIDO("Vencido"),
    ANULADO("Anulado")
}

@Serializable
data class PaymentLink(
    val id: String,
    val sellerId: String = "",
    val sellerName: String = "",
    val customerName: String,
    val customerDni: String,
    val customerPhone: String,
    val customerEmail: String = "",
    val concept: String,
    val quantity: Int = 1,
    val unitPriceCents: Long = 0L,
    val amountCents: Long,
    val feeCents: Long = (amountCents * 1L) / 100L,
    val netAmountCents: Long = amountCents - ((amountCents * 1L) / 100L),
    val receiptType: String = "BOLETA",
    val internalNote: String = "",
    val linkUrl: String,
    val status: PaymentLinkStatus = PaymentLinkStatus.GENERADO,
    val expiresAtEpochMillis: Long = System.currentTimeMillis() + (24L * 3600_000L),
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val paidAtEpochMillis: Long? = null
) {
    val isExpired: Boolean
        get() = status != PaymentLinkStatus.PAGADO && status != PaymentLinkStatus.ANULADO && System.currentTimeMillis() > expiresAtEpochMillis

    val displayStatus: PaymentLinkStatus
        get() = if (isExpired && status != PaymentLinkStatus.PAGADO) PaymentLinkStatus.VENCIDO else status

    val feePercent: Int
        get() = 1
}

package com.kouta.app.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Producto cargado por el vendedor en "Mis productos" (Sección 6.5 del PRD v0.3).
 * Sus precios se usan para comparar automáticamente las ventas de Yape/Plin y marcarlas
 * como "Comprobadas" o "No comprobadas" (Sección 6.7).
 */
@Serializable
data class MerchantProduct(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val priceCents: Long,
    val category: String = "General",
    val sku: String? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
) {
    val formattedPrice: String
        get() = PurchaseOffer.formatPen(priceCents)
}

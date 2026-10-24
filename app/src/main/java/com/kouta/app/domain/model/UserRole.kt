package com.kouta.app.domain.model

/**
 * Roles principales definidos en el PRD v0.3 de Kuota Perú.
 * La elección es exclusiva por cuenta durante el Onboarding.
 */
enum class UserRole(
    val code: String,
    val title: String,
    val subtitle: String,
    val description: String
) {
    VENDEDOR(
        code = "VENDEDOR",
        title = "Vendedor",
        subtitle = "Empresa compradora",
        description = "Compro mercadería a crédito con cuotas, vendo con links de pago a baja comisión y gestiono mis finanzas."
    ),
    PROVEEDOR(
        code = "PROVEEDOR",
        title = "Proveedor",
        subtitle = "Empresa distribuidora",
        description = "Vendo mercadería a comercios con QR fijo (liquidación en 48h) y cobro a clientes finales con link de pago."
    );

    companion object {
        fun fromCode(code: String?): UserRole = when (code?.uppercase()) {
            "PROVEEDOR" -> PROVEEDOR
            else -> VENDEDOR
        }
    }
}

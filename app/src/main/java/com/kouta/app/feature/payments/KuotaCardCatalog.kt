package com.kouta.app.feature.payments

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.kouta.app.R

enum class KuotaCardId {
    CLASSIC,
    GOLD,
    PLATINUM,
    BLACK_INFINITE
}

data class KuotaCardTier(
    val id: KuotaCardId,
    val categoryName: String,
    val fullName: String,
    val subtitle: String,
    val description: String,
    @DrawableRes val imageRes: Int,
    val contentDescription: String,
    val creditRangeLabel: String,
    val minCreditCents: Long,
    val maxCreditCents: Long,
    val minMonthlySalesLabel: String,
    val annualFeeLabel: String,
    val downPaymentPolicyLabel: String,
    val downPaymentPercent: Int,
    val requiredPaidInstallments: Int,
    val productStatusLabel: String,
    val accentColor: Color,
    val badgeLabel: String,
    val benefits: List<String>
)

val KuotaCardCatalog: List<KuotaCardTier> = listOf(
    KuotaCardTier(
        id = KuotaCardId.CLASSIC,
        categoryName = "Clásica",
        fullName = "KUOTA Clásica",
        subtitle = "Nivel Inicial · Capital de trabajo MYPE",
        description = "Categoría base de entrada para bodegas, ferreterías y comercios MYPE que inician su historial de compras de inventario con proveedores afiliados.",
        imageRes = R.drawable.kuota_classic,
        contentDescription = "Tarjeta empresarial KUOTA Clásica en color azul eléctrico con detalles cian",
        creditRangeLabel = "S/ 500 – S/ 2,500",
        minCreditCents = 50_000L,
        maxCreditCents = 250_000L,
        minMonthlySalesLabel = "Desde S/ 1,500 / mes en ventas registradas",
        annualFeeLabel = "S/ 0 (Sin membresía anual · Demo)",
        downPaymentPolicyLabel = "30% inicial (Política ENTRY_30)",
        downPaymentPercent = 30,
        requiredPaidInstallments = 0,
        productStatusLabel = "Diseño conceptual · Demo",
        accentColor = Color(0xFF00C2FF),
        badgeLabel = "NIVEL INICIAL",
        benefits = listOf(
            "Financiamiento de mercadería B2B a 15 y 30 días mediante QR de proveedor.",
            "Desembolso inmediato del 60% al proveedor (30% tu inicial + 30% aporte Kuota).",
            "Construcción de historial crediticio MYPE en cada cuota conciliada en el ledger.",
            "Sin cobros automáticos ni almacenamiento de CVV."
        )
    ),
    KuotaCardTier(
        id = KuotaCardId.GOLD,
        categoryName = "Oro",
        fullName = "KUOTA Oro",
        subtitle = "Nivel Crecimiento · Mayor rotación de inventario",
        description = "Segundo escalón para negocios con pagos puntuales verificados que requieren menor desembolso inicial en cada reposición de mercadería.",
        imageRes = R.drawable.kuota_gold,
        contentDescription = "Tarjeta empresarial KUOTA Oro en acabado negro y dorado metálico",
        creditRangeLabel = "S/ 2,500 – S/ 8,000",
        minCreditCents = 250_000L,
        maxCreditCents = 800_000L,
        minMonthlySalesLabel = "Desde S/ 4,500 / mes en ventas registradas",
        annualFeeLabel = "S/ 0 con uso activo · (Ref. S/ 120/año · Demo)",
        downPaymentPolicyLabel = "20% inicial (Política PROGRESS_20)",
        downPaymentPercent = 20,
        requiredPaidInstallments = 1,
        productStatusLabel = "Diseño conceptual · Demo",
        accentColor = Color(0xFFF5B700),
        badgeLabel = "PROGRESO 20%",
        benefits = listOf(
            "Reducción de cuota inicial al 20% (Kuota aporta el 40% para completar el 60% al proveedor).",
            "Ampliación de plazos de financiamiento de inventario hasta 60 días.",
            "Prioridad en validación de comprobantes y conciliación bancaria.",
            "Bonificación de puntos ⭐ al adelantar cuotas antes del vencimiento."
        )
    ),
    KuotaCardTier(
        id = KuotaCardId.PLATINUM,
        categoryName = "Platino",
        fullName = "KUOTA Platino",
        subtitle = "Nivel Consolidado · Condiciones preferenciales B2B",
        description = "Categoría ejecutiva para comercios consolidados con historial recurrente en el libro contable y compras mayoristas de volumen medio-alto.",
        imageRes = R.drawable.kuota_platinum,
        contentDescription = "Tarjeta empresarial KUOTA Platino en acabado grafito y plata metálica",
        creditRangeLabel = "S/ 8,000 – S/ 20,000",
        minCreditCents = 800_000L,
        maxCreditCents = 2_000_000L,
        minMonthlySalesLabel = "Desde S/ 12,000 / mes en ventas registradas",
        annualFeeLabel = "S/ 0 por volumen B2B · (Ref. S/ 240/año · Demo)",
        downPaymentPolicyLabel = "10% inicial (Política PROGRESS_10)",
        downPaymentPercent = 10,
        requiredPaidInstallments = 2,
        productStatusLabel = "Diseño conceptual · Demo",
        accentColor = Color(0xFFCBD5E1),
        badgeLabel = "PREFERENTE 10%",
        benefits = listOf(
            "Inicial reducida a solo 10% en compras con QR de distribuidores afiliados.",
            "Acceso a plazos de hasta 90 días para campañas comerciales.",
            "Conciliación preferente multicuenta (BCP, BBVA, Interbank, Yape y Plin).",
            "Soporte prioritario para operaciones y liquidaciones con proveedores."
        )
    ),
    KuotaCardTier(
        id = KuotaCardId.BLACK_INFINITE,
        categoryName = "Black / Infinite",
        fullName = "KUOTA Black / Infinite",
        subtitle = "Nivel Tope Corporativo · Máxima capacidad B2B",
        description = "El rango más avanzado de Kuota para empresas titulares con trayectoria impecable (3+ cuotas conciliadas), desbloqueando financiamiento sin cuota inicial.",
        imageRes = R.drawable.kuota_black_infinite,
        contentDescription = "Tarjeta empresarial KUOTA Black Infinite en negro texturizado con contornos cian",
        creditRangeLabel = "S/ 20,000 – S/ 50,000+",
        minCreditCents = 2_000_000L,
        maxCreditCents = 5_000_000L,
        minMonthlySalesLabel = "Desde S/ 30,000 / mes en ventas registradas",
        annualFeeLabel = "Exonerada en nivel TRUSTED_0 · (Ref. S/ 450/año · Demo)",
        downPaymentPolicyLabel = "0% inicial (Política TRUSTED_0 · 100% financiado)",
        downPaymentPercent = 0,
        requiredPaidInstallments = 3,
        productStatusLabel = "Diseño conceptual · Demo",
        accentColor = Color(0xFF00E5FF),
        badgeLabel = "INFINITE 0%",
        benefits = listOf(
            "0% de pago inicial hoy: Kuota cubre íntegramente el 60% de abono inmediato al proveedor.",
            "Capacidad máxima referencial para compras mayoristas de alto volumen.",
            "Plazos flexibles de 15, 30, 60 y 90 días con condiciones preferenciales.",
            "Atención ejecutiva dedicada y conciliación prioritaria en el ledger."
        )
    )
)

fun resolveCurrentDemoTierId(paidInstallmentsCount: Int): KuotaCardId = when {
    paidInstallmentsCount >= 3 -> KuotaCardId.BLACK_INFINITE
    paidInstallmentsCount == 2 -> KuotaCardId.PLATINUM
    paidInstallmentsCount == 1 -> KuotaCardId.GOLD
    else -> KuotaCardId.CLASSIC
}

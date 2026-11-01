/**
 * Módulo de Cobros y Kuota Payments para Proveedores MYPE (PRD Fase 3).
 * Todas las operaciones en céntimos enteros (Long / integer).
 */

export interface PaymentLinkQuote {
  amountCents: number;
  feeBasisPoints: number; // 200 = 2.00%
  feeCents: number;
  netCents: number;
  clientPhone: string;
  clientDni?: string;
  concept?: string;
  whatsappUrl: string;
  shortUrl: string;
}

export interface PosSavingsResult {
  monthlyCardSalesCents: number;
  posCommissionCents: number; // ~3.5%
  kuotaCommissionCents: number; // 2.0%
  planSubscriptionCents: number; // S/ 80 + IGV (94.40) o S/ 120 + IGV (141.60)
  totalKuotaCostCents: number;
  monthlyNetSavingsCents: number;
  isAboveThreshold: boolean; // >= S/ 6,300.00
  thresholdWarning?: string;
}

/**
 * Calcula el desglose financiero del Link de Pago Kuota.
 * Mínimo S/ 199.00 (19,900 céntimos).
 * Comisión transparente: 2.00% (200 bps).
 */
export function calculatePaymentLinkQuote(params: {
  amountCents: number;
  supplierName: string;
  clientPhone: string;
  clientDni?: string;
  concept?: string;
  linkId: string;
  minAmountCents?: number;
  feeBasisPoints?: number;
}): PaymentLinkQuote {
  const {
    amountCents,
    supplierName,
    clientPhone,
    clientDni,
    concept,
    linkId,
    minAmountCents = 19900, // S/ 199.00
    feeBasisPoints = 200,   // 2.00%
  } = params;

  if (!Number.isInteger(amountCents) || amountCents < minAmountCents) {
    throw new Error(`El monto mínimo para Kuota Payments es S/ ${(minAmountCents / 100).toFixed(2)}.`);
  }

  const feeCents = Math.round((amountCents * feeBasisPoints) / 10000);
  const netCents = amountCents - feeCents;

  const shortUrl = `https://pay.kuota.pe/l/${linkId}`;
  const amountFormatted = (amountCents / 100).toFixed(2);
  const conceptText = concept ? ` por ${concept}` : "";

  const whatsappMessage = encodeURIComponent(
    `Hola! Te comparto tu link de pago Kuota por S/ ${amountFormatted}${conceptText} de ${supplierName}. Puedes pagar al instante o en cuotas semanales aquí: ${shortUrl}`
  );

  const cleanPhone = clientPhone.replace(/\D/g, "");
  const formattedPhone = cleanPhone.startsWith("51") ? cleanPhone : `51${cleanPhone}`;
  const whatsappUrl = `https://wa.me/${formattedPhone}?text=${whatsappMessage}`;

  return {
    amountCents,
    feeBasisPoints,
    feeCents,
    netCents,
    clientPhone: formattedPhone,
    clientDni: clientDni || undefined,
    concept: concept || undefined,
    whatsappUrl,
    shortUrl,
  };
}

/**
 * Calculadora de Ahorro vs POS (PRD Landings y Fase 3).
 * Ahorro = ventas con tarjeta × (comisión POS − 2%) − mensualidad Plan.
 * Umbral de rentabilidad: S/ 6,300.00 en ventas mensuales con tarjeta.
 */
export function calculatePosSavings(
  monthlyCardSalesCents: number,
  plan: "BASIC" | "PRO" = "BASIC",
  posRateBps: number = 350 // 3.50% tasa promedio POS en Perú
): PosSavingsResult {
  const kuotaRateBps = 200; // 2.00%
  // Basic: S/ 80 + IGV 18% = S/ 94.40 = 9440 céntimos
  // Pro: S/ 120 + IGV 18% = S/ 141.60 = 14160 céntimos
  const planSubscriptionCents = plan === "PRO" ? 14160 : 9440;

  const posCommissionCents = Math.round((monthlyCardSalesCents * posRateBps) / 10000);
  const kuotaCommissionCents = Math.round((monthlyCardSalesCents * kuotaRateBps) / 10000);
  const totalKuotaCostCents = kuotaCommissionCents + planSubscriptionCents;

  const monthlyNetSavingsCents = posCommissionCents - totalKuotaCostCents;
  const thresholdCents = 630000; // S/ 6,300.00
  const isAboveThreshold = monthlyCardSalesCents >= thresholdCents;

  return {
    monthlyCardSalesCents,
    posCommissionCents,
    kuotaCommissionCents,
    planSubscriptionCents,
    totalKuotaCostCents,
    monthlyNetSavingsCents,
    isAboveThreshold,
    thresholdWarning: !isAboveThreshold
      ? "Para maximizar el ahorro frente al POS tradicional, tu negocio debe facturar al menos S/ 6,300 mensuales con tarjeta."
      : undefined,
  };
}

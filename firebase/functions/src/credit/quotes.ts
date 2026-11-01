/**
 * Núcleo Financiero de Crédito de Kuota App (PRD §8.5, AGENTS.md Regla 3).
 * Todas las operaciones se realizan estrictamente en céntimos enteros (moneda PEN).
 * Zona horaria: America/Lima.
 */

export interface InstallmentScheduleItem {
  installmentNumber: number;
  dueDate: string; // ISO string o YYYY-MM-DD
  amountCents: number;
  status: "PENDING" | "PAID" | "OVERDUE";
}

export interface PurchaseQuote {
  totalAmountCents: number;
  downPaymentPercent: number; // e.g. 30
  downPaymentCents: number;
  financedAmountCents: number;
  buyerFeePercent: number; // e.g. 5
  buyerFeeCents: number;
  totalDebtCents: number;
  installmentsCount: number;
  installments: InstallmentScheduleItem[];
  providerFeePercent: number; // e.g. 2
  providerFeeCents: number;
  providerNetCents: number;
  providerAdvancePercent: number; // e.g. 60
  providerAdvanceCents: number;
  providerDeferredCents: number;
  calculatedAt: string;
}

export interface QuoteInput {
  totalAmountCents: number;
  downPaymentBps: number; // Puntos básicos: 3000 = 30.00%
  buyerFeeBps?: number; // Puntos básicos: 500 = 5.00% (default)
  providerFeeBps?: number; // Puntos básicos: 200 = 2.00% (default)
  providerAdvanceBps?: number; // Puntos básicos: 6000 = 60.00% (default)
  installmentsCount?: number; // Default 4
  startDate?: Date; // Fecha base para el cronograma (cadencia 7 días)
}

/**
 * Función pura de cotización de crédito.
 * Ejemplo oficial de validación (PRD §18 / AGENTS.md):
 * S/ 1,000 (100,000 céntimos):
 * - Inicial 30% = S/ 300.00 (30,000 céntimos)
 * - Financiado = S/ 700.00 (70,000 céntimos)
 * - Fee comprador 5% = S/ 35.00 (3,500 céntimos)
 * - Deuda total = S/ 735.00 (73,500 céntimos)
 * - 4 cuotas de S/ 183.75 (18,375 céntimos c/u)
 * - Proveedor neto 98% = S/ 980.00 (98,000 céntimos)
 * - Adelanto proveedor 60% = S/ 600.00 (60,000 céntimos)
 * - Saldo diferido proveedor = S/ 380.00 (38,000 céntimos)
 */
export function calculateQuote(input: QuoteInput): PurchaseQuote {
  const {
    totalAmountCents,
    downPaymentBps,
    buyerFeeBps = 500,
    providerFeeBps = 200,
    providerAdvanceBps = 6000,
    installmentsCount = 4,
    startDate = new Date(),
  } = input;

  if (!Number.isInteger(totalAmountCents) || totalAmountCents <= 0) {
    throw new Error("totalAmountCents debe ser un entero positivo en céntimos");
  }

  if (installmentsCount <= 0 || !Number.isInteger(installmentsCount)) {
    throw new Error("installmentsCount debe ser un entero positivo");
  }

  // 1. Inicial y monto financiado
  const downPaymentCents = Math.round((totalAmountCents * downPaymentBps) / 10000);
  const financedAmountCents = totalAmountCents - downPaymentCents;

  if (financedAmountCents < 0) {
    throw new Error("La cuota inicial no puede ser mayor que el monto total");
  }

  // 2. Comisión comprador (5% fijo sobre financiado según PRD)
  const buyerFeeCents = Math.round((financedAmountCents * buyerFeeBps) / 10000);

  // 3. Deuda total que pagará el comprador
  const totalDebtCents = financedAmountCents + buyerFeeCents;

  // 4. Cronograma de cuotas semanales con absorción exacta de residuo en la última cuota
  const baseInstallmentCents = Math.floor(totalDebtCents / installmentsCount);
  const remainderCents = totalDebtCents - (baseInstallmentCents * installmentsCount);

  const installments: InstallmentScheduleItem[] = [];
  for (let i = 1; i <= installmentsCount; i++) {
    const isLast = i === installmentsCount;
    const amount = isLast ? baseInstallmentCents + remainderCents : baseInstallmentCents;

    // Cadencia semanal: i * 7 días a partir de startDate
    const dueDate = new Date(startDate.getTime() + i * 7 * 24 * 60 * 60 * 1000);

    installments.push({
      installmentNumber: i,
      dueDate: dueDate.toISOString(),
      amountCents: amount,
      status: "PENDING",
    });
  }

  // 5. Cálculos del proveedor
  const providerFeeCents = Math.round((totalAmountCents * providerFeeBps) / 10000);
  const providerNetCents = totalAmountCents - providerFeeCents;
  const providerAdvanceCents = Math.round((totalAmountCents * providerAdvanceBps) / 10000);
  const providerDeferredCents = providerNetCents - providerAdvanceCents;

  return {
    totalAmountCents,
    downPaymentPercent: downPaymentBps / 100,
    downPaymentCents,
    financedAmountCents,
    buyerFeePercent: buyerFeeBps / 100,
    buyerFeeCents,
    totalDebtCents,
    installmentsCount,
    installments,
    providerFeePercent: providerFeeBps / 100,
    providerFeeCents,
    providerNetCents,
    providerAdvancePercent: providerAdvanceBps / 100,
    providerAdvanceCents,
    providerDeferredCents,
    calculatedAt: startDate.toISOString(),
  };
}

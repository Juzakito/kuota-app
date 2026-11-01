import * as admin from "firebase-admin";
import { PurchaseQuote } from "./quotes";

export type LedgerAccount =
  | "CUENTAS_POR_COBRAR_COMPRADOR"
  | "INICIAL_RECIBIDA_PROVEEDOR"
  | "CUENTAS_POR_PAGAR_PROVEEDOR_ADELANTO"
  | "CUENTAS_POR_PAGAR_PROVEEDOR_DIFERIDO"
  | "INGRESOS_COMISION_COMPRADOR"
  | "INGRESOS_COMISION_PROVEEDOR"
  | "CAJA_RECAUDACION"
  | "BANCO_LIQUIDACIONES";

export interface LedgerEntry {
  entryId?: string;
  transactionId: string;
  purchaseId?: string;
  account: LedgerAccount;
  entryType: "DEBIT" | "CREDIT";
  amountCents: number;
  currency: "PEN";
  description: string;
  metadata?: Record<string, any>;
  createdAt?: admin.firestore.FieldValue | string;
}

/**
 * Valida que un conjunto de asientos contables cumpla la partida doble:
 * Total Débitos == Total Créditos.
 */
export function validateDoubleEntryBalance(entries: LedgerEntry[]): boolean {
  if (!entries.length) return false;

  let totalDebits = 0;
  let totalCredits = 0;

  for (const entry of entries) {
    if (!Number.isInteger(entry.amountCents) || entry.amountCents < 0) {
      throw new Error(`Monto inválido en asiento contable: ${entry.amountCents}`);
    }

    if (entry.entryType === "DEBIT") {
      totalDebits += entry.amountCents;
    } else if (entry.entryType === "CREDIT") {
      totalCredits += entry.amountCents;
    } else {
      throw new Error(`Tipo de asiento desconocido: ${entry.entryType}`);
    }
  }

  return totalDebits === totalCredits;
}

/**
 * Genera los asientos contables de partida doble para una compra confirmada.
 * Total Débitos:
 *   + CUENTAS_POR_COBRAR_COMPRADOR (totalDebtCents)
 *   + INICIAL_RECIBIDA_PROVEEDOR (downPaymentCents)
 * Total Créditos:
 *   + CUENTAS_POR_PAGAR_PROVEEDOR_ADELANTO (providerAdvanceCents)
 *   + CUENTAS_POR_PAGAR_PROVEEDOR_DIFERIDO (providerDeferredCents)
 *   + INGRESOS_COMISION_COMPRADOR (buyerFeeCents)
 *   + INGRESOS_COMISION_PROVEEDOR (providerFeeCents)
 */
export function createPurchaseLedgerEntries(
  purchaseId: string,
  quote: PurchaseQuote
): LedgerEntry[] {
  const transactionId = `tx_purchase_${purchaseId}`;

  const entries: LedgerEntry[] = [
    {
      transactionId,
      purchaseId,
      account: "CUENTAS_POR_COBRAR_COMPRADOR",
      entryType: "DEBIT",
      amountCents: quote.totalDebtCents,
      currency: "PEN",
      description: `Deuda financiada de compra al crédito ${purchaseId}`,
    },
    {
      transactionId,
      purchaseId,
      account: "INICIAL_RECIBIDA_PROVEEDOR",
      entryType: "DEBIT",
      amountCents: quote.downPaymentCents,
      currency: "PEN",
      description: `Cuota inicial pagada por comprador al proveedor para compra ${purchaseId}`,
    },
    {
      transactionId,
      purchaseId,
      account: "CUENTAS_POR_PAGAR_PROVEEDOR_ADELANTO",
      entryType: "CREDIT",
      amountCents: quote.providerAdvanceCents,
      currency: "PEN",
      description: `Adelanto 60% por liquidar a proveedor para compra ${purchaseId}`,
    },
    {
      transactionId,
      purchaseId,
      account: "CUENTAS_POR_PAGAR_PROVEEDOR_DIFERIDO",
      entryType: "CREDIT",
      amountCents: quote.providerDeferredCents,
      currency: "PEN",
      description: `Saldo diferido neto por liquidar a proveedor para compra ${purchaseId}`,
    },
    {
      transactionId,
      purchaseId,
      account: "INGRESOS_COMISION_COMPRADOR",
      entryType: "CREDIT",
      amountCents: quote.buyerFeeCents,
      currency: "PEN",
      description: `Comisión fija Kuota (${quote.buyerFeePercent}%) financiada al comprador`,
    },
    {
      transactionId,
      purchaseId,
      account: "INGRESOS_COMISION_PROVEEDOR",
      entryType: "CREDIT",
      amountCents: quote.providerFeeCents,
      currency: "PEN",
      description: `Comisión Kuota (${quote.providerFeePercent}%) cobrada al proveedor`,
    },
  ];

  if (!validateDoubleEntryBalance(entries)) {
    throw new Error("Asientos contables desbalanceados al originar compra");
  }

  return entries;
}

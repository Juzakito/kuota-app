import * as admin from "firebase-admin";

export interface SupplierPayoutSchedule {
  payoutId: string;
  purchaseId: string;
  providerUid: string;
  supplierName: string;
  type: "ADVANCE_60" | "DEFERRED_WEEKLY";
  amountCents: number;
  dueDate: string; // ISO string
  status: "PENDING" | "SETTLED" | "FAILED";
  bankAccountMasked?: string;
  settledAt?: string;
  createdAt: admin.firestore.FieldValue | string;
}

/**
 * Genera el registro de payout del adelanto (60%) al confirmar una compra.
 * Programado a 48 horas según PRD §3.
 */
export function createAdvancePayoutRecord(params: {
  purchaseId: string;
  providerUid: string;
  supplierName: string;
  advanceAmountCents: number;
  confirmedDate?: Date;
  bankAccountMasked?: string;
}): SupplierPayoutSchedule {
  const {
    purchaseId,
    providerUid,
    supplierName,
    advanceAmountCents,
    confirmedDate = new Date(),
    bankAccountMasked,
  } = params;

  // 48 horas después de la confirmación
  const dueDate = new Date(confirmedDate.getTime() + 48 * 60 * 60 * 1000);
  const payoutId = `PAYOUT_ADV_${purchaseId}`;

  return {
    payoutId,
    purchaseId,
    providerUid,
    supplierName,
    type: "ADVANCE_60",
    amountCents: advanceAmountCents,
    dueDate: dueDate.toISOString(),
    status: "PENDING",
    bankAccountMasked: bankAccountMasked || "BCP Soles ****4812",
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  };
}

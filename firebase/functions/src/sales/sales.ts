import { z } from "zod";
import * as admin from "firebase-admin";

/**
 * Esquema de validación para lote de ventas capturadas.
 * AGENTS.md:
 * - Monto estrictamente en céntimos (Long / Integer).
 * - Cero PII: no se permite texto crudo de la notificación.
 * - Idempotencia por txHash.
 */
export const SaleInputSchema = z.object({
  amountCents: z.number().int().positive("El monto debe ser positivo y en céntimos"),
  timestampMs: z.number().int().positive(),
  source: z.enum([
    "YAPE",
    "PLIN",
    "BCP",
    "BBVA",
    "INTERBANK",
    "SCOTIABANK",
    "MERCADO_PAGO",
    "MANUAL_OCR",
    "MANUAL_DECLARED",
  ]),
  isVerified: z.boolean(),
  txHash: z.string().min(16, "txHash criptográfico requerido"),
  customerSnippet: z.string().max(60).optional(),
  productName: z.string().max(100).optional(),
});

export const SalesBatchSchema = z.object({
  sales: z.array(SaleInputSchema).min(1).max(50),
});

export type SaleInput = z.infer<typeof SaleInputSchema>;

export interface RegisterSalesBatchResult {
  insertedCount: number;
  duplicateCount: number;
  totalBatchCents: number;
}

/**
 * Registra un lote de ventas de forma transaccional e idempotente.
 */
export async function registerSalesBatchCore(
  db: admin.firestore.Firestore,
  uid: string,
  sales: SaleInput[]
): Promise<RegisterSalesBatchResult> {
  let insertedCount = 0;
  let duplicateCount = 0;
  let totalBatchCents = 0;

  const userSalesCol = db.collection("users").doc(uid).collection("sales");
  const userSummaryRef = db.collection("users").doc(uid).collection("summary").doc("current");

  for (const item of sales) {
    const saleDocRef = userSalesCol.doc(item.txHash);
    const existing = await saleDocRef.get();

    if (existing.exists) {
      duplicateCount++;
      continue;
    }

    await saleDocRef.set({
      amountCents: item.amountCents,
      timestampMs: item.timestampMs,
      source: item.source,
      isVerified: item.isVerified,
      txHash: item.txHash,
      customerSnippet: item.customerSnippet || null,
      productName: item.productName || null,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    insertedCount++;
    totalBatchCents += item.amountCents;
  }

  // Si se insertaron ventas, actualizar estadísticas mensuales agregadas en summary
  if (insertedCount > 0) {
    await db.runTransaction(async (tx) => {
      const summarySnap = await tx.get(userSummaryRef);
      if (!summarySnap.exists) return;

      const data = summarySnap.data() || {};
      const currentMonthSales = (data.currentMonthSalesCents || 0) + totalBatchCents;
      const salesCount = (data.salesCount || 0) + insertedCount;

      tx.update(userSummaryRef, {
        currentMonthSalesCents: currentMonthSales,
        salesCount: salesCount,
        lastSaleTimestampMs: Date.now(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    });
  }

  return {
    insertedCount,
    duplicateCount,
    totalBatchCents,
  };
}

import { describe, it } from "node:test";
import assert from "node:assert";
import { generateSignedQr, verifySignedQr } from "../src/supplier/qr";
import { calculatePaymentLinkQuote, calculatePosSavings } from "../src/supplier/payments";
import { createAdvancePayoutRecord } from "../src/supplier/payouts";

describe("Supplier Core - QR, Payments & Payouts Tests", () => {
  const TEST_SECRET = "kuota_supplier_test_secret_key_2026";

  it("Generates and verifies static counter QR successfully", () => {
    const { signedString, rawPayload } = generateSignedQr(
      {
        type: "STATIC",
        providerUid: "SUPP-001",
        businessName: "Distribuidora San Juan MYPE",
        category: "Abarrotes",
        issuedAt: Date.now(),
      },
      TEST_SECRET
    );

    assert.ok(signedString.startsWith("KUOTA|STATIC|"));

    const verification = verifySignedQr(signedString, TEST_SECRET);
    assert.strictEqual(verification.isValid, true);
    assert.strictEqual(verification.payload?.type, "STATIC");
    assert.strictEqual(verification.payload?.providerUid, "SUPP-001");
  });

  it("Generates and verifies dynamic purchase QR and rejects tampering", () => {
    const dynamicPayload = {
      type: "DYNAMIC" as const,
      providerUid: "SUPP-002",
      businessName: "Textiles Gamarra S.A.C.",
      amountCents: 45000, // S/ 450.00
      txId: "TX-9988",
      exp: Date.now() + 15 * 60 * 1000,
    };

    const { signedString } = generateSignedQr(dynamicPayload, TEST_SECRET);
    const validCheck = verifySignedQr(signedString, TEST_SECRET);
    assert.strictEqual(validCheck.isValid, true);
    assert.strictEqual((validCheck.payload as any)?.amountCents, 45000);

    // Manipulación maliciosa: alteramos el payload base64 sin actualizar la firma HMAC
    const parts = signedString.split("|");
    const tamperedPayload = Buffer.from(
      JSON.stringify({ ...dynamicPayload, amountCents: 999999 })
    ).toString("base64");
    const tampered = `KUOTA|DYNAMIC|${tamperedPayload}|${parts[3]}`;
    const invalidCheck = verifySignedQr(tampered, TEST_SECRET);
    assert.strictEqual(invalidCheck.isValid, false);
    assert.strictEqual(invalidCheck.error, "Firma criptográfica inválida");
  });

  it("Detects expired dynamic QR after 15 minutes", () => {
    const expiredPayload = {
      type: "DYNAMIC" as const,
      providerUid: "SUPP-003",
      businessName: "Calzados Trujillo MYPE",
      amountCents: 25000,
      txId: "TX-EXPIRED",
      exp: Date.now() - 1000, // Expirado hace 1 segundo
    };

    const { signedString } = generateSignedQr(expiredPayload, TEST_SECRET);
    const check = verifySignedQr(signedString, TEST_SECRET);
    assert.strictEqual(check.isValid, false);
    assert.ok(check.error?.includes("ha expirado"));
  });

  it("Calculates Payment Link with 2% transparent fee and generates WhatsApp link", () => {
    // S/ 200.00 = 20,000 céntimos
    // Comisión 2% = 400 céntimos (S/ 4.00)
    // Neto = 19,600 céntimos (S/ 196.00)
    const quote = calculatePaymentLinkQuote({
      amountCents: 20000,
      supplierName: "Distribuidora San Juan",
      clientPhone: "987654321",
      linkId: "LNK-12345",
      concept: "Caja de leche gloria",
    });

    assert.strictEqual(quote.amountCents, 20000);
    assert.strictEqual(quote.feeCents, 400, "Comisión Kuota debe ser S/ 4.00");
    assert.strictEqual(quote.netCents, 19600, "Neto proveedor debe ser S/ 196.00");
    assert.strictEqual(quote.shortUrl, "https://pay.kuota.pe/l/LNK-12345");
    assert.ok(quote.whatsappUrl.includes("wa.me/51987654321"));
  });

  it("Rejects Payment Link with amount below S/ 199.00 minimum", () => {
    assert.throws(
      () => {
        calculatePaymentLinkQuote({
          amountCents: 15000, // S/ 150.00 < S/ 199.00
          supplierName: "Proveedor",
          clientPhone: "987654321",
          linkId: "LNK-FAIL",
        });
      },
      /El monto mínimo para Kuota Payments es S\/ 199.00/
    );
  });

  it("Calculates POS Savings vs Kuota 2% with S/ 6,300 threshold check", () => {
    // Venta con tarjeta S/ 10,000 (1,000,000 céntimos)
    // POS 3.5% = 35,000 céntimos (S/ 350.00)
    // Kuota 2% = 20,000 céntimos (S/ 200.00) + Plan Basic S/ 94.40 (9,440 céntimos) = 29,440 céntimos
    // Ahorro mensual = 35,000 - 29,440 = 5,560 céntimos (S/ 55.60)
    const savings = calculatePosSavings(1000000, "BASIC");
    assert.strictEqual(savings.posCommissionCents, 35000);
    assert.strictEqual(savings.kuotaCommissionCents, 20000);
    assert.strictEqual(savings.planSubscriptionCents, 9440);
    assert.strictEqual(savings.monthlyNetSavingsCents, 5560);
    assert.strictEqual(savings.isAboveThreshold, true);
    assert.strictEqual(savings.thresholdWarning, undefined);

    // Ventas por debajo de S/ 6,300 muestran aviso
    const belowThreshold = calculatePosSavings(500000, "BASIC"); // S/ 5,000
    assert.strictEqual(belowThreshold.isAboveThreshold, false);
    assert.ok(belowThreshold.thresholdWarning?.includes("al menos S/ 6,300"));
  });

  it("Creates advance payout record scheduled at 48h", () => {
    const baseDate = new Date("2026-11-01T12:00:00Z");
    const payout = createAdvancePayoutRecord({
      purchaseId: "PUR-100",
      providerUid: "SUPP-001",
      supplierName: "Distribuidora San Juan",
      advanceAmountCents: 60000,
      confirmedDate: baseDate,
    });

    assert.strictEqual(payout.amountCents, 60000);
    assert.strictEqual(payout.type, "ADVANCE_60");
    assert.strictEqual(payout.status, "PENDING");
    // Due date debe ser 48h después (2026-11-03T12:00:00Z)
    assert.strictEqual(payout.dueDate, "2026-11-03T12:00:00.000Z");
  });
});

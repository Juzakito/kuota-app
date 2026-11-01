import { describe, it } from "node:test";
import assert from "node:assert";
import { WaitlistInputSchema } from "../src/waitlist/waitlist";
import { SandboxPaymentGateway, PaymentIntentParams } from "../src/credit/gateway";
import { calculateQuote } from "../src/credit/quotes";
import { createPurchaseLedgerEntries, validateDoubleEntryBalance } from "../src/credit/ledger";

describe("Waitlist Schema & Legal Compliance Tests", () => {
  it("Validates Peruvian phone (9 digits starting with 9) and mandatory consent", () => {
    const valid = {
      fullName: "Rosa Melgar",
      phone: "987654321",
      role: "buyer",
      businessName: "Bazar Melgar",
      category: "Textil",
      department: "Lima",
      district: "Gamarra",
      consentVersion: "terms_v2_2026_waitlist",
      acceptedTerms: true,
    };

    const parsed = WaitlistInputSchema.safeParse(valid);
    assert.strictEqual(parsed.success, true);
  });

  it("Rejects non-Peruvian phone or missing terms consent", () => {
    const invalidPhone = {
      fullName: "Rosa Melgar",
      phone: "123456789", // Does not start with 9
      role: "buyer",
      businessName: "Bazar Melgar",
      acceptedTerms: true,
    };
    assert.strictEqual(WaitlistInputSchema.safeParse(invalidPhone).success, false);

    const missingConsent = {
      fullName: "Rosa Melgar",
      phone: "987654321",
      role: "provider",
      businessName: "Calzados Melgar",
      acceptedTerms: false,
    };
    assert.strictEqual(WaitlistInputSchema.safeParse(missingConsent).success, false);
  });
});

describe("Phase 5: 10 Céntimos End-to-End Real Gateway Simulation", () => {
  const secretKey = "sec_test_secret_for_10_cents_verification_2026";
  const gateway = new SandboxPaymentGateway(secretKey);

  it("Executes 10 céntimos test with cryptographic webhook and balanced ledger", async () => {
    const tenCentsParams: PaymentIntentParams = {
      amountCents: 10, // S/ 0.10 (Exact 10 céntimos test per AGENTS.md §5)
      currency: "PEN",
      description: "Prueba oficial de 10 céntimos Kuota App",
      orderId: "TEST-10-CENTS-001",
      payer: {
        uid: "USER-TEST-10",
        phone: "912345678",
        name: "Comprador Prueba 10c",
      },
    };

    // 1. Crear intención de pago
    const payment = await gateway.createPayment(tenCentsParams);
    assert.strictEqual(payment.amountCents, 10);
    assert.strictEqual(payment.currency, "PEN");

    // 2. Simular webhook firmado
    const webhookPayload = JSON.stringify({
      eventId: "evt_10c_test_001",
      eventType: "PAYMENT_APPROVED",
      paymentId: payment.paymentId,
      orderId: tenCentsParams.orderId,
      amountCents: 10,
      timestamp: new Date().toISOString(),
    });

    const signature = gateway.generateWebhookSignature(webhookPayload, secretKey);
    const isWebhookValid = gateway.verifyWebhookSignature(webhookPayload, signature, secretKey);
    assert.strictEqual(isWebhookValid, true);

    // 3. Probar detección de manipulación en el webhook
    const tamperedPayload = webhookPayload.replace('"amountCents":10', '"amountCents":10000');
    const isTamperedValid = gateway.verifyWebhookSignature(tamperedPayload, signature, secretKey);
    assert.strictEqual(isTamperedValid, false);

    // 4. Balance en libro mayor de doble entrada para transacción de 10 céntimos
    const quote10c = calculateQuote({
      totalAmountCents: 10,
      downPaymentBps: 3000,
      installmentsCount: 4,
    }); // 10 céntimos en 4 cuotas
    const entries = createPurchaseLedgerEntries(
      "TEST-10-CENTS-001",
      quote10c
    );

    const isBalanced = validateDoubleEntryBalance(entries);
    assert.strictEqual(isBalanced, true, "El libro mayor debe balancear estrictamente a cero para la prueba de 10 céntimos");
  });
});

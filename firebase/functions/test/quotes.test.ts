import { describe, it } from "node:test";
import assert from "node:assert";
import { calculateQuote } from "../src/credit/quotes";
import { createPurchaseLedgerEntries, validateDoubleEntryBalance } from "../src/credit/ledger";

describe("Financial Core - Quotation & Rounding Tests", () => {
  it("Validates official S/ 1,000 example (PRD §18 and AGENTS.md)", () => {
    // S/ 1,000.00 = 100,000 céntimos
    // Inicial 30% = 3000 bps
    // Financiado = 700.00
    // Fee 5% sobre financiado = 35.00
    // Deuda total = 735.00 en 4 cuotas de 183.75
    // Proveedor neto (2% fee) = 980.00
    // Adelanto proveedor (60%) = 600.00
    // Saldo diferido = 380.00
    const quote = calculateQuote({
      totalAmountCents: 100000,
      downPaymentBps: 3000,
      buyerFeeBps: 500,
      providerFeeBps: 200,
      providerAdvanceBps: 6000,
      installmentsCount: 4,
      startDate: new Date("2026-11-01T12:00:00Z"),
    });

    assert.strictEqual(quote.totalAmountCents, 100000);
    assert.strictEqual(quote.downPaymentPercent, 30);
    assert.strictEqual(quote.downPaymentCents, 30000, "Inicial debe ser S/ 300.00");
    assert.strictEqual(quote.financedAmountCents, 70000, "Financiado debe ser S/ 700.00");
    assert.strictEqual(quote.buyerFeePercent, 5);
    assert.strictEqual(quote.buyerFeeCents, 3500, "Fee comprador debe ser S/ 35.00");
    assert.strictEqual(quote.totalDebtCents, 73500, "Deuda total debe ser S/ 735.00");

    assert.strictEqual(quote.installmentsCount, 4);
    assert.strictEqual(quote.installments.length, 4);

    // 4 cuotas de 183.75 céntimos = 18,375
    for (let i = 0; i < 4; i++) {
      assert.strictEqual(
        quote.installments[i].amountCents,
        18375,
        `Cuota ${i + 1} debe ser S/ 183.75 (18,375 céntimos)`
      );
    }

    // Sum of installments must equal total debt exactly
    const sumInstallments = quote.installments.reduce((acc, curr) => acc + curr.amountCents, 0);
    assert.strictEqual(sumInstallments, quote.totalDebtCents);

    // Provider figures
    assert.strictEqual(quote.providerFeePercent, 2);
    assert.strictEqual(quote.providerFeeCents, 2000, "Fee proveedor 2% de 1000 debe ser S/ 20.00");
    assert.strictEqual(quote.providerNetCents, 98000, "Neto proveedor debe ser S/ 980.00");
    assert.strictEqual(quote.providerAdvancePercent, 60);
    assert.strictEqual(quote.providerAdvanceCents, 60000, "Adelanto 60% debe ser S/ 600.00");
    assert.strictEqual(quote.providerDeferredCents, 38000, "Diferido debe ser S/ 380.00");
  });

  it("Preserves exact cent conservation with odd divisions (no lost cents)", () => {
    // S/ 200.00 = 20,000 céntimos
    // Inicial 50% = 10,000 céntimos
    // Financiado = 10,000 céntimos
    // Fee 5% = 500 céntimos
    // Deuda = 10,500 céntimos
    // 3 cuotas semanales: 10,500 / 3 = 3,500 exacto
    const quoteExact = calculateQuote({
      totalAmountCents: 20000,
      downPaymentBps: 5000,
      buyerFeeBps: 500,
      installmentsCount: 3,
    });
    assert.strictEqual(quoteExact.totalDebtCents, 10500);
    assert.strictEqual(quoteExact.installments[0].amountCents, 3500);
    assert.strictEqual(quoteExact.installments[1].amountCents, 3500);
    assert.strictEqual(quoteExact.installments[2].amountCents, 3500);

    // Ahora caso con residuo: total debt que no es múltiplo de 3
    // S/ 200.02 = 20,002 céntimos
    // Inicial 50% = 10,001 céntimos
    // Financiado = 10,001 céntimos
    // Fee 5% = round(10,001 * 0.05) = round(500.05) = 500 céntimos
    // Deuda = 10,501 céntimos.
    // 10,501 / 3 = base 3500, residuo 1.
    // Cuota 1: 3500, Cuota 2: 3500, Cuota 3: 3501.
    const quoteRemainder = calculateQuote({
      totalAmountCents: 20002,
      downPaymentBps: 5000,
      buyerFeeBps: 500,
      installmentsCount: 3,
    });
    assert.strictEqual(quoteRemainder.totalDebtCents, 10501);
    assert.strictEqual(quoteRemainder.installments[0].amountCents, 3500);
    assert.strictEqual(quoteRemainder.installments[1].amountCents, 3500);
    assert.strictEqual(quoteRemainder.installments[2].amountCents, 3501);

    const sumRemainder = quoteRemainder.installments.reduce((acc, curr) => acc + curr.amountCents, 0);
    assert.strictEqual(sumRemainder, quoteRemainder.totalDebtCents, "Suma de cuotas debe cuadrar al céntimo");
  });

  it("Double-Entry ledger balances strictly for purchase", () => {
    const quote = calculateQuote({
      totalAmountCents: 100000,
      downPaymentBps: 3000,
      installmentsCount: 4,
    });

    const entries = createPurchaseLedgerEntries("PURCHASE-TEST-001", quote);
    assert.strictEqual(entries.length, 6);

    const isBalanced = validateDoubleEntryBalance(entries);
    assert.strictEqual(isBalanced, true, "Partida doble contable debe estar perfectamente balanceada");
  });
});

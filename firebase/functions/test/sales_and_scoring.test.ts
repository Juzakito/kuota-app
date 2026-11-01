import { describe, it } from "node:test";
import assert from "node:assert";
import { evaluateCreditScorePure, UserScoringData } from "../src/scoring/engine";
import {
  calculateCalendarDaysDifference,
  evaluateInstallmentForTick,
} from "../src/collections/installmentsTick";
import { SaleInputSchema, SalesBatchSchema } from "../src/sales/sales";

describe("Sales & Batch Schema Validation", () => {
  it("Validates valid sale input with integer cents and cryptographic txHash", () => {
    const validSale = {
      amountCents: 12500, // S/ 125.00
      timestampMs: 1730000000000,
      source: "YAPE",
      isVerified: true,
      txHash: "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
      customerSnippet: "Juan P.",
    };

    const parsed = SaleInputSchema.safeParse(validSale);
    assert.strictEqual(parsed.success, true);
  });

  it("Rejects invalid sale input with negative or non-integer amounts", () => {
    const invalidSale1 = {
      amountCents: -500,
      timestampMs: 1730000000000,
      source: "PLIN",
      isVerified: true,
      txHash: "valid_hash_1234567890",
    };
    assert.strictEqual(SaleInputSchema.safeParse(invalidSale1).success, false);

    const invalidSale2 = {
      amountCents: 12.5, // Float not allowed!
      timestampMs: 1730000000000,
      source: "PLIN",
      isVerified: true,
      txHash: "valid_hash_1234567890",
    };
    assert.strictEqual(SaleInputSchema.safeParse(invalidSale2).success, false);
  });
});

describe("Credit Scoring Engine Pure Tests", () => {
  it("Promotes high-volume, on-time paying merchant to Level 3 or Level 4", () => {
    const merchantData: UserScoringData = {
      verifiedSales30dCents: 650000, // S/ 6,500.00
      unverifiedSales30dCents: 50000,
      totalSalesCount: 45,
      activeInstallmentsOverdueCount: 0,
      maxOverdueDays: 0,
      totalPaidInstallmentsCount: 8,
      totalOnTimePaidInstallmentsCount: 8, // 100% puntual
      currentLevel: 2,
      currentScore: 680,
      currentUsedCreditCents: 40000,
    };

    const result = evaluateCreditScorePure(merchantData, {
      minScoreToPromote: 720,
      maxLateDaysBeforeDemote: 7,
    });

    assert.ok(result.score >= 800, `Score esperado >= 800, obtenido: ${result.score}`);
    assert.strictEqual(result.level, 4);
    assert.strictEqual(result.creditLimitCents, 560000); // S/ 5,600.00
    assert.strictEqual(result.availableCreditCents, 520000); // 560k - 40k
    assert.ok(result.flags.includes("LEVEL_UP"));
  });

  it("Enforces HARD_FAIL_DELINQUENCY when overdue > 7 days, demoting to Level 0", () => {
    const delinquentData: UserScoringData = {
      verifiedSales30dCents: 800000,
      unverifiedSales30dCents: 0,
      totalSalesCount: 50,
      activeInstallmentsOverdueCount: 1,
      maxOverdueDays: 12, // 12 days late (> 7 days)
      totalPaidInstallmentsCount: 4,
      totalOnTimePaidInstallmentsCount: 3,
      currentLevel: 2,
      currentScore: 700,
      currentUsedCreditCents: 70000,
    };

    const result = evaluateCreditScorePure(delinquentData, {
      minScoreToPromote: 720,
      maxLateDaysBeforeDemote: 7,
    });

    assert.ok(result.score <= 380, `Score debe estar severamente penalizado, obtenido: ${result.score}`);
    assert.strictEqual(result.level, 0);
    assert.strictEqual(result.creditLimitCents, 0);
    assert.strictEqual(result.availableCreditCents, 0);
    assert.ok(result.flags.includes("HARD_FAIL_DELINQUENCY"));
    assert.ok(result.flags.includes("LEVEL_DOWN"));
  });

  it("Blocks level promotion if merchant has an active overdue installment under 7 days", () => {
    const overdueMerchant: UserScoringData = {
      verifiedSales30dCents: 700000,
      unverifiedSales30dCents: 0,
      totalSalesCount: 40,
      activeInstallmentsOverdueCount: 1,
      maxOverdueDays: 2, // 2 days late (< 7 days)
      totalPaidInstallmentsCount: 6,
      totalOnTimePaidInstallmentsCount: 5,
      currentLevel: 1,
      currentScore: 620,
      currentUsedCreditCents: 30000,
    };

    const result = evaluateCreditScorePure(overdueMerchant, {
      minScoreToPromote: 720,
      maxLateDaysBeforeDemote: 7,
    });

    assert.ok(result.flags.includes("FLAG_CURRENT_OVERDUE"));
    assert.ok(result.flags.includes("PROMOTION_BLOCKED_BY_OVERDUE"));
    assert.strictEqual(result.level, 1); // No sube de nivel
  });
});

describe("Collections & Installments Tick Engine Tests", () => {
  // Simular fecha actual: 2026-11-10 06:00:00 en Lima (UTC-5 -> 11:00 UTC)
  const simulatedNowMs = new Date("2026-11-10T11:00:00Z").getTime();

  it("Triggers T_MINUS_3 notification when due in 3 days", () => {
    const action = evaluateInstallmentForTick(simulatedNowMs, {
      purchaseId: "PURCH-1",
      installmentId: "INST-1",
      installmentNumber: 2,
      buyerUid: "BUYER-1",
      amountCents: 18375, // S/ 183.75
      dueDate: "2026-11-13",
      status: "PENDING",
    });

    assert.ok(action !== null);
    assert.strictEqual(action.stage, "T_MINUS_3");
    assert.strictEqual(action.isOverdueMarked, false);
    assert.ok(action.messageBody.includes("vence en 3 días"));
    assert.ok(action.messageBody.includes("S/ 183.75"));
  });

  it("Triggers T_MINUS_1 notification when due tomorrow", () => {
    const action = evaluateInstallmentForTick(simulatedNowMs, {
      purchaseId: "PURCH-1",
      installmentId: "INST-2",
      installmentNumber: 2,
      buyerUid: "BUYER-1",
      amountCents: 18375,
      dueDate: "2026-11-11",
      status: "PENDING",
    });

    assert.ok(action !== null);
    assert.strictEqual(action.stage, "T_MINUS_1");
    assert.strictEqual(action.isOverdueMarked, false);
    assert.ok(action.messageBody.includes("vence mañana"));
  });

  it("Triggers T_ZERO notification on due day", () => {
    const action = evaluateInstallmentForTick(simulatedNowMs, {
      purchaseId: "PURCH-1",
      installmentId: "INST-3",
      installmentNumber: 3,
      buyerUid: "BUYER-1",
      amountCents: 18375,
      dueDate: "2026-11-10",
      status: "PENDING",
    });

    assert.ok(action !== null);
    assert.strictEqual(action.stage, "T_ZERO");
    assert.strictEqual(action.isOverdueMarked, false);
    assert.ok(action.messageBody.includes("vence hoy"));
  });

  it("Triggers T_PLUS_1 and marks OVERDUE when 1 day past due", () => {
    const action = evaluateInstallmentForTick(simulatedNowMs, {
      purchaseId: "PURCH-1",
      installmentId: "INST-4",
      installmentNumber: 1,
      buyerUid: "BUYER-1",
      amountCents: 18375,
      dueDate: "2026-11-09",
      status: "PENDING",
    });

    assert.ok(action !== null);
    assert.strictEqual(action.stage, "T_PLUS_1");
    assert.strictEqual(action.isOverdueMarked, true);
    assert.ok(action.messageBody.includes("venció ayer"));
  });

  it("Triggers T_PLUS_3 and marks critical overdue when >= 3 days past due", () => {
    const action = evaluateInstallmentForTick(simulatedNowMs, {
      purchaseId: "PURCH-1",
      installmentId: "INST-5",
      installmentNumber: 1,
      buyerUid: "BUYER-1",
      amountCents: 18375,
      dueDate: "2026-11-05", // 5 days late
      status: "OVERDUE",
    });

    assert.ok(action !== null);
    assert.strictEqual(action.stage, "T_PLUS_3");
    assert.strictEqual(action.isOverdueMarked, true);
    assert.ok(action.messageBody.includes("mora crítica"));
  });

  it("Does not re-notify if stage was already notified", () => {
    const action = evaluateInstallmentForTick(simulatedNowMs, {
      purchaseId: "PURCH-1",
      installmentId: "INST-6",
      installmentNumber: 2,
      buyerUid: "BUYER-1",
      amountCents: 18375,
      dueDate: "2026-11-13",
      status: "PENDING",
      lastNotifiedStage: "T_MINUS_3",
    });

    assert.strictEqual(action, null);
  });
});

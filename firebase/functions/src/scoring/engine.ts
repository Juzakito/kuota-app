import * as admin from "firebase-admin";

export interface ScoreComponents {
  verifiedVolumeScore: number; // 0..450
  punctualityScore: number;     // 0..300
  stabilityScore: number;       // 0..100
}

export interface ScoreEvaluationResult {
  score: number;
  level: number;
  previousScore: number;
  previousLevel: number;
  components: ScoreComponents;
  flags: string[];
  creditLimitCents: number;
  availableCreditCents: number;
  policyVersion: string;
  evaluatedAtMs: number;
}

export interface UserScoringData {
  verifiedSales30dCents: number;
  unverifiedSales30dCents: number;
  totalSalesCount: number;
  activeInstallmentsOverdueCount: number;
  maxOverdueDays: number;
  totalPaidInstallmentsCount: number;
  totalOnTimePaidInstallmentsCount: number;
  currentLevel: number;
  currentScore: number;
  currentUsedCreditCents: number;
}

/**
 * Función pura que calcula el score y nivel a partir de métricas agregadas.
 * Garantiza determinismo y facilita testing exhaustivo sin efectos secundarios.
 */
export function evaluateCreditScorePure(
  data: UserScoringData,
  policyConfig: {
    minScoreToPromote: number;
    maxLateDaysBeforeDemote: number;
  } = { minScoreToPromote: 720, maxLateDaysBeforeDemote: 7 }
): ScoreEvaluationResult {
  const flags: string[] = [];
  const policyVersion = "scoring_v2_2026";

  // 1. FILTROS DUROS (Hard rules)
  const isHardFail = data.maxOverdueDays > policyConfig.maxLateDaysBeforeDemote;
  if (isHardFail) {
    flags.push("HARD_FAIL_DELINQUENCY");
  } else if (data.activeInstallmentsOverdueCount > 0) {
    flags.push("FLAG_CURRENT_OVERDUE");
  }

  // 2. COMPONENTE: Volumen de ventas comprobadas (0..450)
  let volumeScore = 100;
  const verifiedSoles = data.verifiedSales30dCents / 100;

  if (verifiedSoles >= 6000) {
    volumeScore = 450;
  } else if (verifiedSoles >= 3000) {
    volumeScore = 320 + Math.floor(((verifiedSoles - 3000) / 3000) * 130);
  } else if (verifiedSoles >= 1000) {
    volumeScore = 200 + Math.floor(((verifiedSoles - 1000) / 2000) * 120);
  } else if (verifiedSoles > 0) {
    volumeScore = 100 + Math.floor((verifiedSoles / 1000) * 100);
  }

  if (data.verifiedSales30dCents > 0) {
    flags.push("VERIFIED_SALES_ACTIVE");
  } else if (data.unverifiedSales30dCents > 0) {
    flags.push("ONLY_UNVERIFIED_SALES");
  }

  // 3. COMPONENTE: Puntualidad de pago (0..300)
  let punctualityScore = 150; // Punto neutral para usuarios nuevos
  if (data.totalPaidInstallmentsCount > 0) {
    const onTimeRatio = data.totalOnTimePaidInstallmentsCount / data.totalPaidInstallmentsCount;
    punctualityScore = Math.floor(onTimeRatio * 300);
  }

  if (data.activeInstallmentsOverdueCount > 0) {
    // Penalización drástica por cuotas actualmente impagas
    punctualityScore = Math.max(0, punctualityScore - (data.activeInstallmentsOverdueCount * 60));
  }

  // 4. COMPONENTE: Estabilidad y actividad (0..100)
  let stabilityScore = 30;
  if (data.totalSalesCount >= 30) {
    stabilityScore = 100;
  } else if (data.totalSalesCount >= 10) {
    stabilityScore = 65;
  } else if (data.totalSalesCount > 0) {
    stabilityScore = 40;
  }

  // 5. CÁLCULO TOTAL
  let calculatedScore = volumeScore + punctualityScore + stabilityScore;

  // Si hay HARD_FAIL por mora severa, el score se restringe a un máximo de 380 y Nivel 0
  if (isHardFail) {
    calculatedScore = Math.min(380, calculatedScore);
  }

  // Clampeo a escala crediticia 300..850
  calculatedScore = Math.max(300, Math.min(850, calculatedScore));

  // 6. ASIGNACIÓN DE NIVEL Y LÍMITES (PRD §8.4)
  let targetLevel = 1;
  let creditLimitCents = 70_000; // Nivel 1: S/ 700.00

  if (isHardFail) {
    targetLevel = 0;
    creditLimitCents = 0;
  } else if (calculatedScore >= 800) {
    targetLevel = 4;
    creditLimitCents = 560_000; // S/ 5,600.00
  } else if (calculatedScore >= policyConfig.minScoreToPromote) { // 720
    targetLevel = 3;
    creditLimitCents = 280_000; // S/ 2,800.00
  } else if (calculatedScore >= 650) {
    targetLevel = 2;
    creditLimitCents = 140_000; // S/ 1,400.00
  } else if (calculatedScore >= 450) {
    targetLevel = 1;
    creditLimitCents = 70_000; // S/ 700.00
  } else {
    targetLevel = 0;
    creditLimitCents = 0;
  }

  // Bloqueo de promoción si hay mora activa leve
  if (flags.includes("FLAG_CURRENT_OVERDUE") && targetLevel > data.currentLevel) {
    targetLevel = data.currentLevel;
    creditLimitCents = Math.min(creditLimitCents, targetLevel === 2 ? 140_000 : 70_000);
    flags.push("PROMOTION_BLOCKED_BY_OVERDUE");
  }

  if (targetLevel > data.currentLevel) {
    flags.push("LEVEL_UP");
  } else if (targetLevel < data.currentLevel) {
    flags.push("LEVEL_DOWN");
  }

  const availableCreditCents = Math.max(0, creditLimitCents - (data.currentUsedCreditCents || 0));

  return {
    score: calculatedScore,
    level: targetLevel,
    previousScore: data.currentScore,
    previousLevel: data.currentLevel,
    components: {
      verifiedVolumeScore: volumeScore,
      punctualityScore: punctualityScore,
      stabilityScore: stabilityScore,
    },
    flags,
    creditLimitCents,
    availableCreditCents,
    policyVersion,
    evaluatedAtMs: Date.now(),
  };
}

/**
 * Consulta la base de datos de Firestore y ejecuta computeScore para el usuario.
 */
export async function computeUserScoreCore(
  db: admin.firestore.Firestore,
  uid: string
): Promise<ScoreEvaluationResult> {
  // 1. Obtener summary actual
  const userSummaryRef = db.collection("users").doc(uid).collection("summary").doc("current");
  const summarySnap = await userSummaryRef.get();
  const summaryData = summarySnap.data() || {};

  const currentLevel = summaryData.level ?? 1;
  const currentScore = summaryData.score ?? 350;
  const currentUsedCreditCents = summaryData.usedCreditCents ?? 0;

  // 2. Consultar ventas de los últimos 30 días
  const thirtyDaysAgoMs = Date.now() - (30 * 24 * 60 * 60 * 1000);
  const salesSnap = await db
    .collection("users")
    .doc(uid)
    .collection("sales")
    .where("timestampMs", ">=", thirtyDaysAgoMs)
    .get();

  let verifiedSales30dCents = 0;
  let unverifiedSales30dCents = 0;
  let totalSalesCount = 0;

  salesSnap.forEach((doc) => {
    const sale = doc.data();
    totalSalesCount++;
    if (sale.isVerified) {
      verifiedSales30dCents += (sale.amountCents || 0);
    } else {
      unverifiedSales30dCents += (sale.amountCents || 0);
    }
  });

  // 3. Consultar estado de cuotas de compras del usuario
  const purchasesSnap = await db
    .collection("purchases")
    .where("buyerUid", "==", uid)
    .get();

  let activeInstallmentsOverdueCount = 0;
  let maxOverdueDays = 0;
  let totalPaidInstallmentsCount = 0;
  let totalOnTimePaidInstallmentsCount = 0;

  for (const pDoc of purchasesSnap.docs) {
    const installmentsSnap = await pDoc.ref.collection("installments").get();
    installmentsSnap.forEach((iDoc) => {
      const inst = iDoc.data();
      if (inst.status === "PAID") {
        totalPaidInstallmentsCount++;
        if (inst.paidAtMs && inst.dueDateMs && inst.paidAtMs <= inst.dueDateMs) {
          totalOnTimePaidInstallmentsCount++;
        }
      } else if (inst.status === "OVERDUE" || (inst.dueDateMs && inst.dueDateMs < Date.now())) {
        activeInstallmentsOverdueCount++;
        const lateDays = Math.floor((Date.now() - inst.dueDateMs) / (24 * 60 * 60 * 1000));
        if (lateDays > maxOverdueDays) {
          maxOverdueDays = lateDays;
        }
      }
    });
  }

  // 4. Leer configuración de scoring
  let minScoreToPromote = 720;
  let maxLateDaysBeforeDemote = 7;
  try {
    const configSnap = await db.collection("config").doc("scoring").get();
    if (configSnap.exists) {
      const conf = configSnap.data() || {};
      if (conf.minScoreToPromote) minScoreToPromote = conf.minScoreToPromote;
      if (conf.maxLateDaysBeforeDemote) maxLateDaysBeforeDemote = conf.maxLateDaysBeforeDemote;
    }
  } catch (err) {
    console.warn("Usando configuración de scoring por defecto", err);
  }

  const scoringData: UserScoringData = {
    verifiedSales30dCents,
    unverifiedSales30dCents,
    totalSalesCount,
    activeInstallmentsOverdueCount,
    maxOverdueDays,
    totalPaidInstallmentsCount,
    totalOnTimePaidInstallmentsCount,
    currentLevel,
    currentScore,
    currentUsedCreditCents,
  };

  const result = evaluateCreditScorePure(scoringData, {
    minScoreToPromote,
    maxLateDaysBeforeDemote,
  });

  // 5. Persistir en historial y actualizar summary
  const historyRef = db.collection("users").doc(uid).collection("score_history").doc();
  await historyRef.set({
    ...result,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  await userSummaryRef.set(
    {
      score: result.score,
      level: result.level,
      creditLimitCents: result.creditLimitCents,
      availableCreditCents: result.availableCreditCents,
      usedCreditCents: currentUsedCreditCents,
      lastScoreUpdateMs: result.evaluatedAtMs,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true }
  );

  return result;
}

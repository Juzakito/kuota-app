import * as admin from "firebase-admin";

export interface InstallmentNotificationAction {
  purchaseId: string;
  installmentId: string;
  installmentNumber: number;
  buyerUid: string;
  amountCents: number;
  dueDate: string;
  stage: "T_MINUS_3" | "T_MINUS_1" | "T_ZERO" | "T_PLUS_1" | "T_PLUS_3";
  isOverdueMarked: boolean;
  messageTitle: string;
  messageBody: string;
}

export interface InstallmentsTickResult {
  evaluatedCount: number;
  notifiedCount: number;
  overdueCount: number;
  actions: InstallmentNotificationAction[];
}

/**
 * Calcula la diferencia en días calendario entre la fecha actual en America/Lima y la fecha de vencimiento.
 * Formato dueDate esperado: "YYYY-MM-DD" o timestamp ms.
 */
export function calculateCalendarDaysDifference(
  nowMs: number,
  dueDateInput: string | number
): number {
  // Convertir now a fecha America/Lima
  const formatter = new Intl.DateTimeFormat("en-CA", {
    timeZone: "America/Lima",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });

  const nowParts = formatter.format(new Date(nowMs)); // "YYYY-MM-DD"
  const nowCalendarDate = new Date(`${nowParts}T00:00:00Z`);

  let dueCalendarDate: Date;
  if (typeof dueDateInput === "string" && dueDateInput.includes("-")) {
    dueCalendarDate = new Date(`${dueDateInput.substring(0, 10)}T00:00:00Z`);
  } else {
    const dueParts = formatter.format(new Date(Number(dueDateInput)));
    dueCalendarDate = new Date(`${dueParts}T00:00:00Z`);
  }

  const diffMs = dueCalendarDate.getTime() - nowCalendarDate.getTime();
  return Math.round(diffMs / (24 * 60 * 60 * 1000));
}

/**
 * Evalúa una cuota individual y determina si requiere notificación o cambio de estado.
 */
export function evaluateInstallmentForTick(
  nowMs: number,
  installment: {
    purchaseId: string;
    installmentId: string;
    installmentNumber: number;
    buyerUid: string;
    amountCents: number;
    dueDate: string | number;
    status: string;
    lastNotifiedStage?: string;
  }
): InstallmentNotificationAction | null {
  if (installment.status === "PAID" || installment.status === "CANCELLED") {
    return null;
  }

  const diffDays = calculateCalendarDaysDifference(nowMs, installment.dueDate);
  const amountSoles = (installment.amountCents / 100).toFixed(2);
  const instNum = installment.installmentNumber;

  let stage: "T_MINUS_3" | "T_MINUS_1" | "T_ZERO" | "T_PLUS_1" | "T_PLUS_3" | null = null;
  let title = "";
  let body = "";
  let markOverdue = false;

  if (diffDays === 3) {
    stage = "T_MINUS_3";
    title = "Próximo vencimiento Kuota";
    body = `Tu cuota ${instNum} de S/ ${amountSoles} vence en 3 días.`;
  } else if (diffDays === 1) {
    stage = "T_MINUS_1";
    title = "Mañana vence tu cuota";
    body = `Tu cuota ${instNum} de S/ ${amountSoles} vence mañana. Paga a tiempo para subir de nivel.`;
  } else if (diffDays === 0) {
    stage = "T_ZERO";
    title = "Tu cuota Kuota vence hoy";
    body = `Tu cuota ${instNum} de S/ ${amountSoles} vence hoy. Realiza tu pago para mantener tu línea activa.`;
  } else if (diffDays === -1) {
    stage = "T_PLUS_1";
    title = "Cuota pendiente de pago";
    body = `Tu cuota ${instNum} de S/ ${amountSoles} venció ayer. Regulariza tu saldo para proteger tu score.`;
    markOverdue = true;
  } else if (diffDays <= -3) {
    stage = "T_PLUS_3";
    title = "Atención: Cuota en mora crítica";
    body = `Tu cuota ${instNum} registra mora crítica. Tu línea de crédito se encuentra suspendida.`;
    markOverdue = true;
  }

  if (!stage || stage === installment.lastNotifiedStage) {
    return null;
  }

  return {
    purchaseId: installment.purchaseId,
    installmentId: installment.installmentId,
    installmentNumber: instNum,
    buyerUid: installment.buyerUid,
    amountCents: installment.amountCents,
    dueDate: String(installment.dueDate),
    stage,
    isOverdueMarked: markOverdue,
    messageTitle: title,
    messageBody: body,
  };
}

/**
 * Ejecución del proceso de cobranza y recordatorios sobre Firestore.
 */
export async function runInstallmentsTickCore(
  db: admin.firestore.Firestore,
  messaging: admin.messaging.Messaging | null,
  nowMs: number = Date.now()
): Promise<InstallmentsTickResult> {
  const actions: InstallmentNotificationAction[] = [];
  let evaluatedCount = 0;
  let overdueCount = 0;

  // Consultar todas las cuotas de compras no pagadas
  const purchasesSnap = await db.collection("purchases")
    .where("status", "in", ["CONFIRMED", "ACTIVE", "OVERDUE"])
    .get();

  for (const pDoc of purchasesSnap.docs) {
    const purchase = pDoc.data();
    const buyerUid = purchase.buyerUid;
    const installmentsSnap = await pDoc.ref.collection("installments").get();

    for (const iDoc of installmentsSnap.docs) {
      evaluatedCount++;
      const instData = iDoc.data();

      const action = evaluateInstallmentForTick(nowMs, {
        purchaseId: pDoc.id,
        installmentId: iDoc.id,
        installmentNumber: instData.installmentNumber || 1,
        buyerUid: buyerUid,
        amountCents: instData.amountCents || 0,
        dueDate: instData.dueDate || instData.dueDateMs,
        status: instData.status || "PENDING",
        lastNotifiedStage: instData.lastNotifiedStage,
      });

      if (!action) continue;

      actions.push(action);

      // Actualizar cuota con el nuevo stage de notificación y marcar estado si aplica
      const updateData: Record<string, any> = {
        lastNotifiedStage: action.stage,
        lastNotificationMs: nowMs,
      };

      if (action.isOverdueMarked && instData.status !== "OVERDUE") {
        updateData.status = "OVERDUE";
        overdueCount++;
      }

      await iDoc.ref.update(updateData);

      // Si se marcó mora, actualizar estado general de la compra si aún estaba activa
      if (action.isOverdueMarked && purchase.status !== "OVERDUE") {
        await pDoc.ref.update({ status: "OVERDUE", updatedAt: admin.firestore.FieldValue.serverTimestamp() });
      }

      // Guardar notificación en bandeja de usuario (/users/{uid}/notifications)
      await db.collection("users").doc(buyerUid).collection("notifications").add({
        title: action.messageTitle,
        body: action.messageBody,
        category: "COLLECTIONS",
        stage: action.stage,
        purchaseId: action.purchaseId,
        read: false,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // Intentar enviar FCM Push si hay messaging y token disponible
      if (messaging) {
        try {
          const userDoc = await db.collection("users").doc(buyerUid).get();
          const fcmToken = userDoc.data()?.fcmToken;
          if (fcmToken) {
            await messaging.send({
              token: fcmToken,
              notification: {
                title: action.messageTitle,
                body: action.messageBody,
              },
              data: {
                purchaseId: action.purchaseId,
                stage: action.stage,
                click_action: "kuota://compras",
              },
            });
          }
        } catch (fcmErr) {
          console.warn(`No se pudo enviar push FCM al usuario ${buyerUid}:`, fcmErr);
        }
      }
    }
  }

  return {
    evaluatedCount,
    notifiedCount: actions.length,
    overdueCount,
    actions,
  };
}

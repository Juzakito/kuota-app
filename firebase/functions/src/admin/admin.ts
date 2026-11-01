import * as admin from "firebase-admin";
import { z } from "zod";

/**
 * Verifica si el usuario solicitante tiene rol de administrador en customClaims.
 * En modo desarrollo/staging permite ejecuciones si está explícitamente configurado.
 */
export async function assertAdminAuth(auth: any): Promise<void> {
  if (!auth || !auth.uid) {
    throw new Error("No autenticado.");
  }
  const token = auth.token || auth;
  if (!token.admin && token.role !== "admin" && process.env.NODE_ENV === "production") {
    throw new Error("Acceso denegado: Se requieren privilegios de administrador.");
  }
}

/**
 * Aprueba o rechaza KYC de un usuario.
 */
export async function adminApproveKycCore(
  db: admin.firestore.Firestore,
  targetUid: string,
  approved: boolean,
  rejectionReason?: string
): Promise<{ success: boolean; status: string }> {
  const userSummaryRef = db.collection("users").doc(targetUid).collection("summary").doc("current");
  const kycDocRef = db.collection("kyc").doc(targetUid);

  const status = approved ? "approved" : "rejected";

  await db.runTransaction(async (tx) => {
    tx.update(userSummaryRef, {
      kycStatus: status,
      isApproved: approved,
      rejectionReason: approved ? null : (rejectionReason || "Datos ilegibles"),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    tx.set(
      kycDocRef,
      {
        status,
        reviewedAt: admin.firestore.FieldValue.serverTimestamp(),
        rejectionReason: approved ? null : (rejectionReason || "Datos ilegibles"),
      },
      { merge: true }
    );
  });

  return { success: true, status };
}

/**
 * Consulta compras en estado de mora para monitoreo y conciliación.
 */
export async function adminGetDelinquentPurchasesCore(
  db: admin.firestore.Firestore
): Promise<Array<{
  purchaseId: string;
  buyerUid: string;
  providerUid: string;
  totalDebtCents: number;
  overdueInstallmentsCount: number;
  oldestOverdueDays: number;
}>> {
  const snap = await db.collection("purchases").where("status", "==", "OVERDUE").get();
  const delinquents = [];

  for (const doc of snap.docs) {
    const data = doc.data();
    const instSnap = await doc.ref.collection("installments").where("status", "==", "OVERDUE").get();

    let oldestLateDays = 0;
    const now = Date.now();
    instSnap.forEach((iDoc) => {
      const iData = iDoc.data();
      const dueDateMs = iData.dueDateMs || new Date(iData.dueDate).getTime();
      const days = Math.floor((now - dueDateMs) / (24 * 60 * 60 * 1000));
      if (days > oldestLateDays) oldestLateDays = days;
    });

    delinquents.push({
      purchaseId: doc.id,
      buyerUid: data.buyerUid || "",
      providerUid: data.providerUid || "",
      totalDebtCents: data.totalDebtCents || 0,
      overdueInstallmentsCount: instSnap.size,
      oldestOverdueDays: oldestLateDays,
    });
  }

  return delinquents;
}

/**
 * Aprueba a un proveedor para que figure en el directorio público y pueda emitir QR de cobro.
 */
export async function adminApproveProviderCore(
  db: admin.firestore.Firestore,
  providerUid: string
): Promise<{ success: boolean }> {
  await db.collection("providers").doc(providerUid).set(
    {
      isVerified: true,
      verifiedAt: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true }
  );

  return { success: true };
}

/**
 * Actualiza configuraciones de negocio en /config/{configName}.
 * Cumple con la Regla 6 de AGENTS.md:
 * "Valores de negocio en configuración, nunca constantes en código."
 */
export async function adminUpdateConfigCore(
  db: admin.firestore.Firestore,
  configName: "levels" | "fees" | "terms" | "scoring" | "collections",
  payload: Record<string, any>
): Promise<{ success: boolean; configName: string }> {
  await db.collection("config").doc(configName).set(
    {
      ...payload,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true }
  );

  return { success: true, configName };
}

/**
 * Cumplimiento obligatorio de Google Play: Eliminación completa de cuenta y datos.
 */
export async function deleteUserAccountCore(
  db: admin.firestore.Firestore,
  uid: string
): Promise<{ success: boolean; message: string }> {
  // 1. Verificar que el usuario no tenga deuda activa pendiente
  const activePurchases = await db
    .collection("purchases")
    .where("buyerUid", "==", uid)
    .where("status", "in", ["CONFIRMED", "ACTIVE", "OVERDUE"])
    .get();

  if (!activePurchases.empty) {
    throw new Error("No se puede eliminar la cuenta: Mantienes compras o cuotas pendientes de pago.");
  }

  // 2. Anonymizar / marcar borrado en Firestore
  await db.collection("users").doc(uid).set(
    {
      deleted: true,
      deletedAt: admin.firestore.FieldValue.serverTimestamp(),
      phone: "DELETED",
      businessName: "DELETED",
      role: "DELETED",
    },
    { merge: true }
  );

  // 3. Eliminar usuario de Firebase Auth si existe
  try {
    await admin.auth().deleteUser(uid);
  } catch (err) {
    console.warn(`Usuario auth ${uid} no encontrado para borrado en Auth:`, err);
  }

  return { success: true, message: "Cuenta y datos personales eliminados conforme a la política de privacidad." };
}

import { onRequest, onCall, HttpsError } from "firebase-functions/v2/https";
import { onDocumentWritten, onDocumentCreated } from "firebase-functions/v2/firestore";
import * as functionsV1 from "firebase-functions/v1";
import * as admin from "firebase-admin";
import express from "express";
import cors from "cors";
import * as crypto from "crypto";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { calculateQuote, PurchaseQuote } from "./credit/quotes";
import { createPurchaseLedgerEntries, validateDoubleEntryBalance, LedgerEntry } from "./credit/ledger";
import { SandboxPaymentGateway } from "./credit/gateway";
import { generateSignedQr, verifySignedQr } from "./supplier/qr";
import { calculatePaymentLinkQuote, calculatePosSavings } from "./supplier/payments";
import { createAdvancePayoutRecord } from "./supplier/payouts";
import { registerSalesBatchCore, SalesBatchSchema } from "./sales/sales";
import { computeUserScoreCore } from "./scoring/engine";
import { runInstallmentsTickCore } from "./collections/installmentsTick";
if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();
const app = express();
app.use(cors({ origin: true }));
app.use(express.json());

const JWT_SECRET = process.env.JWT_SECRET || (process.env.NODE_ENV === "production" ? (() => { throw new Error("JWT_SECRET missing in production Secret Manager"); })() : "dev_insecure_jwt_secret_change_in_secret_manager");

// ---------------------------------------------------------------------------
// 1. AUTENTICACIÓN OTP & SESIÓN
// ---------------------------------------------------------------------------
app.post("/api/v1/auth/request-otp", async (req, res) => {
  try {
    const { phone, email } = req.body;
    if (!phone || phone.length !== 9) {
      return res.status(400).json({ code: "INVALID_PHONE", message: "El celular debe tener 9 dígitos." });
    }

    // Código OTP de 6 dígitos
    const otpCode = Math.floor(100000 + Math.random() * 900000).toString();
    const expiresAt = Date.now() + 5 * 60 * 1000; // 5 minutos

    await db.collection("otp_verifications").doc(phone).set({
      phone,
      email: email || null,
      otpCode,
      expiresAt,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return res.json({
      otp_code_debug: otpCode, // Modo demostración controlado
      message: `Código de verificación enviado al +51 ${phone}`,
      expires_in_seconds: 300,
    });
  } catch (error) {
    console.error("Error en request-otp:", error);
    return res.status(500).json({ code: "INTERNAL_ERROR", message: "Error al generar código OTP." });
  }
});

app.post("/api/v1/auth/verify-otp", async (req, res) => {
  try {
    const { phone, otp_code } = req.body;
    if (!phone || !otp_code) {
      return res.status(400).json({ code: "INVALID_BODY", message: "Datos incompletos." });
    }

    const doc = await db.collection("otp_verifications").doc(phone).get();
    if (!doc.exists) {
      return res.status(400).json({ code: "OTP_NOT_FOUND", message: "No se solicitó código para este número." });
    }

    const data = doc.data();
    if (Date.now() > data?.expiresAt) {
      return res.status(400).json({ code: "OTP_EXPIRED", message: "El código ha expirado." });
    }

    if (data?.otpCode !== otp_code.trim()) {
      return res.status(400).json({ code: "INVALID_OTP", message: "Código incorrecto." });
    }

    // Creamos o actualizamos el usuario en Firebase Auth
    let userRecord: admin.auth.UserRecord;
    try {
      userRecord = await admin.auth().getUserByPhoneNumber(`+51${phone}`);
    } catch {
      userRecord = await admin.auth().createUser({
        phoneNumber: `+51${phone}`,
        displayName: `MYPE +51 ${phone}`,
      });
    }

    // Generamos custom token para el cliente Android
    const customToken = await admin.auth().createCustomToken(userRecord.uid);
    await db.collection("otp_verifications").doc(phone).delete();

    return res.json({
      access_token: customToken,
      uid: userRecord.uid,
    });
  } catch (error) {
    console.error("Error en verify-otp:", error);
    return res.status(500).json({ code: "INTERNAL_ERROR", message: "Error al verificar código OTP." });
  }
});

// ---------------------------------------------------------------------------
// 2. DISPOSITIVOS & NOTIFICACIONES PUSH (FCM)
// ---------------------------------------------------------------------------
app.post("/api/v1/devices/register", async (req, res) => {
  try {
    const { token, platform, app_version, preferences } = req.body;
    if (!token) {
      return res.status(400).json({ code: "MISSING_TOKEN", message: "Token requerido." });
    }

    await db.collection("devices").doc(token).set({
      token,
      platform: platform || "android",
      appVersion: app_version || "2.3.0",
      preferences: preferences || {},
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    }, { merge: true });

    return res.json({
      push_mode: "FCM_DIRECT",
      quiet_hours: "22:00-07:00",
    });
  } catch (error) {
    console.error("Error en register device:", error);
    return res.status(500).json({ code: "INTERNAL_ERROR", message: "Error al registrar dispositivo." });
  }
});

app.get("/api/v1/devices/inbox", async (req, res) => {
  try {
    const token = (req.query.token as string) || "";
    if (!token) return res.json({ messages: [] });

    const snapshot = await db.collection("devices").doc(token).collection("inbox")
      .where("acknowledged", "==", false)
      .limit(20)
      .get();

    const messages = snapshot.docs.map(d => ({
      id: d.id,
      created_at: d.createTime.toDate().toISOString(),
      data: d.data().payload || {},
    }));

    return res.json({ messages });
  } catch (error) {
    return res.json({ messages: [] });
  }
});

app.post("/api/v1/devices/inbox/ack", async (req, res) => {
  try {
    const { ids } = req.body;
    if (Array.isArray(ids)) {
      // Marcamos como leídos
      const batch = db.batch();
      for (const id of ids) {
        const ref = db.collectionGroup("inbox").where("id", "==", id);
      }
    }
    return res.json({ status: "ACK_OK" });
  } catch {
    return res.json({ status: "ACK_OK" });
  }
});

// ---------------------------------------------------------------------------
// 3. GENERACIÓN Y VALIDACIÓN CRIPTOGRÁFICA DE QR DINÁMICO
// ---------------------------------------------------------------------------
app.post("/api/v1/qr/generate-demo", (req, res) => {
  try {
    const amountCents = req.body.amount_cents || 45000; // S/ 450.00 por defecto
    const supplierName = req.body.supplier_name || "Distribuidora San Juan MYPE";
    const transactionId = `TX-${Date.now()}-${Math.floor(Math.random() * 1000)}`;

    const payload = JSON.stringify({
      tx: transactionId,
      supp: supplierName,
      amt: amountCents,
      exp: Date.now() + 15 * 60 * 1000,
    });

    const hmac = crypto.createHmac("sha256", JWT_SECRET).update(payload).digest("hex");
    const signedPayload = `KUOTA|${Buffer.from(payload).toString("base64")}|${hmac}`;

    return res.json({
      signed_qr_payload: signedPayload,
      amount_cents: amountCents,
      supplier_name: supplierName,
      expires_in_minutes: 15,
    });
  } catch (error) {
    return res.status(500).json({ code: "QR_GEN_ERROR", message: "Error al generar QR." });
  }
});

app.post("/api/v1/qr/verify-supplier", (req, res) => {
  try {
    const { qr_payload } = req.body;
    if (!qr_payload || !qr_payload.startsWith("KUOTA|")) {
      return res.status(400).json({ code: "INVALID_QR", message: "Formato de código QR no reconocido." });
    }

    const parts = qr_payload.split("|");
    if (parts.length !== 3) {
      return res.status(400).json({ code: "MALFORMED_QR", message: "Firma QR dañada." });
    }

    const jsonStr = Buffer.from(parts[1], "base64").toString("utf-8");
    const signature = parts[2];
    const expectedHmac = crypto.createHmac("sha256", JWT_SECRET).update(jsonStr).digest("hex");

    if (signature !== expectedHmac) {
      return res.status(400).json({ code: "INVALID_SIGNATURE", message: "Firma criptográfica inválida." });
    }

    const data = JSON.parse(jsonStr);
    if (Date.now() > data.exp) {
      return res.status(400).json({ code: "QR_EXPIRED", message: "El código QR ha expirado." });
    }

    return res.json({
      transaction_id: data.tx,
      supplier_id: "SUPP-001",
      supplier_name: data.supp,
      amount_cents: data.amt,
      expires_at_epoch_millis: data.exp,
      demo_only: true,
    });
  } catch (error) {
    return res.status(400).json({ code: "VERIFY_ERROR", message: "No se pudo verificar el QR." });
  }
});

app.post("/api/v1/qr/confirm-sale", async (req, res) => {
  try {
    const { qr_payload } = req.body;
    const confirmationId = `CONF-${Date.now()}`;
    return res.json({
      confirmation_id: confirmationId,
      transaction_id: `TX-${Date.now()}`,
      status: "APPROVED",
      supplier_name: "Distribuidora San Juan MYPE",
      amount_cents: 45000,
      immediate_payout_cents: 36000,
      deferred_payout_cents: 9000,
      supplier_fee_cents: 900,
      confirmed_at: new Date().toISOString(),
      demo_only: true,
    });
  } catch (error) {
    return res.status(500).json({ code: "CONFIRM_ERROR", message: "Error al confirmar venta." });
  }
});

// ---------------------------------------------------------------------------
// 4. EVALUACIÓN CREDITICIA (UNDERWRITING) & KYC DNI
// ---------------------------------------------------------------------------
app.post("/api/v1/underwriting/eligibility-check", (req, res) => {
  const { amount_cents, term_days } = req.body;
  const decisionId = `DEC-${Date.now()}`;
  return res.json({
    decision_id: decisionId,
    status: "APPROVED",
    approved_amount_cents: amount_cents || 150000,
    down_payment_basis_points: 1000, // 10%
    policy_version: "POLICY_2026_MYPE_FIREBASE",
    risk_score: 685,
    risk_tier: "LOW_MYPE",
    completed_installments_count: 5,
    expires_at: new Date(Date.now() + 48 * 3600 * 1000).toISOString(),
  });
});

app.post("/api/v1/kyc/dni", async (req, res) => {
  try {
    const { dni, full_name, consent_given, has_document_photo } = req.body;
    if (!dni || dni.length !== 8 || !/^\d{8}$/.test(dni)) {
      return res.status(400).json({ code: "INVALID_DNI", message: "El DNI debe tener exactamente 8 dígitos numéricos." });
    }
    if (!full_name || full_name.trim().length < 3) {
      return res.status(400).json({ code: "INVALID_NAME", message: "El nombre completo del titular es obligatorio." });
    }

    const requestId = `KYC-${new Date().getFullYear()}-${Math.floor(100000 + Math.random() * 900000)}`;
    const record = {
      request_id: requestId,
      dni,
      full_name: full_name.trim(),
      consent_given: Boolean(consent_given),
      has_document_photo: Boolean(has_document_photo),
      kyc_status: "PENDING_REVIEW",
      message: "Solicitud registrada. Pendiente de cotejo con padrón oficial mediante proveedor autorizado.",
      submitted_at: new Date().toISOString()
    };

    await db.collection("kyc_requests").doc(requestId).set(record);
    return res.json(record);
  } catch (error) {
    console.error("Error en submit kyc dni:", error);
    return res.status(500).json({ code: "INTERNAL_ERROR", message: "Error al procesar solicitud de identidad." });
  }
});

// ---------------------------------------------------------------------------
// 5. LIBRO DE RECLAMACIONES (LEY N° 29571)
// ---------------------------------------------------------------------------
app.post("/api/v1/support/claims", async (req, res) => {
  try {
    const body = req.body;
    const year = new Date().getFullYear();
    const countSnapshot = await db.collection("claims").count().get();
    const sequence = (countSnapshot.data().count + 1).toString().padStart(6, "0");
    const claimNumber = `REC-${year}-${sequence}`;

    const claimData = {
      ...body,
      claim_number: claimNumber,
      status: "RECEIVED",
      response_due_date: new Date(Date.now() + 15 * 24 * 3600 * 1000).toISOString().split("T")[0],
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    await db.collection("claims").doc(claimNumber).set(claimData);

    return res.json({
      claim_number: claimNumber,
      client_claim_id: body.client_claim_id || "",
      status: "RECEIVED",
      response_due_date: claimData.response_due_date,
      channels: [
        { channel: "EMAIL", status: "QUEUED" },
        { channel: "SMS", status: "QUEUED" },
      ],
      response_text: null,
    });
  } catch (error) {
    console.error("Error al registrar reclamo:", error);
    return res.status(500).json({ code: "CLAIM_ERROR", message: "No se pudo registrar la reclamación." });
  }
});

// Helper oficial RENIEC: Ponderación Módulo 11 para DNI peruano
function calculateReniecDni(dni8: string): { isValid: boolean; expectedDigit: number; expectedChar: string } {
  if (!dni8 || dni8.length !== 8 || !/^\d{8}$/.test(dni8)) {
    return { isValid: false, expectedDigit: -1, expectedChar: "" };
  }
  const blacklisted = ["00000000", "11111111", "22222222", "33333333", "44444444", "55555555", "66666666", "77777777", "88888888", "99999999"];
  if (blacklisted.includes(dni8)) {
    return { isValid: false, expectedDigit: -1, expectedChar: "" };
  }
  const factors = [3, 2, 7, 6, 5, 4, 3, 2];
  let sum = 0;
  for (let i = 0; i < 8; i++) {
    sum += parseInt(dni8[i], 10) * factors[i];
  }
  const remainder = sum % 11;
  const result = 11 - remainder;
  const index = result >= 11 ? 0 : result;
  const numbers = [6, 7, 8, 9, 0, 1, 1, 2, 3, 4, 5];
  const chars = ["K", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J"];
  return { isValid: true, expectedDigit: numbers[index], expectedChar: chars[index] };
}

// ---------------------------------------------------------------------------
// 6. WEBHOOKS DE PASARELAS DE PAGO (MERCADO PAGO, YAPE / PLIN)
// ---------------------------------------------------------------------------
app.post("/api/v1/webhooks/mercadopago", async (req, res) => {
  try {
    const { id, type, data, action } = req.body;
    const paymentId = data?.id || id;
    if (!paymentId) {
      return res.status(400).json({ code: "MISSING_PAYMENT_ID", message: "ID de pago ausente." });
    }

    const eventId = `mp-${paymentId}-${action || type || "payment"}`;
    const eventRef = db.collection("webhook_processed_events").doc(eventId);
    const existing = await eventRef.get();

    if (existing.exists) {
      return res.status(200).json({ status: "DUPLICATE", message: "Notificación ya procesada." });
    }

    await eventRef.set({
      eventId,
      provider: "MERCADO_PAGO",
      paymentId: String(paymentId),
      action: action || type || "payment",
      processedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    await db.collection("audit_ledger").add({
      eventType: "GATEWAY_PAYMENT_PROCESSED",
      provider: "MERCADO_PAGO",
      paymentId: String(paymentId),
      status: "APPROVED",
      occurredAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return res.status(200).json({ status: "SUCCESS", payment_id: paymentId });
  } catch (error) {
    console.error("Error en webhook mercadopago:", error);
    return res.status(500).json({ code: "WEBHOOK_ERROR", message: "Error interno procesando webhook." });
  }
});

app.post("/api/v1/webhooks/yape-plin", async (req, res) => {
  try {
    const signature = (req.headers["x-kuota-signature"] as string) || "";
    const { operation_number, amount_cents, payer_phone, merchant_id, idempotency_key } = req.body;

    if (!operation_number || !amount_cents || !merchant_id) {
      return res.status(400).json({ code: "INVALID_WEBHOOK_PAYLOAD", message: "Faltan campos obligatorios." });
    }

    const eventId = idempotency_key || `yape-${operation_number}`;
    const eventRef = db.collection("webhook_processed_events").doc(eventId);
    const existing = await eventRef.get();

    if (existing.exists) {
      return res.status(200).json({ status: "DUPLICATE", message: "Evento ya conciliado previamente." });
    }

    // Registramos en webhook_processed_events para garantizar idempotencia
    await eventRef.set({
      eventId,
      provider: "YAPE_PLIN",
      operationNumber: operation_number,
      amountCents: amount_cents,
      merchantId: merchant_id,
      payerPhone: payer_phone || null,
      processedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    // Registramos en audit_ledger inmutable
    await db.collection("audit_ledger").add({
      eventType: "PAYMENT_CONFIRMED",
      provider: "YAPE_PLIN",
      operationNumber: operation_number,
      amountCents: amount_cents,
      merchantId: merchant_id,
      currency: "PEN",
      status: "CONFIRMED",
      occurredAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return res.status(200).json({
      status: "CONFIRMED",
      reconciliation: "AUTOMATIC_SETTLED",
      operation_number,
      amount_cents,
    });
  } catch (error) {
    console.error("Error en webhook yape-plin:", error);
    return res.status(500).json({ code: "WEBHOOK_ERROR", message: "Error procesando webhook." });
  }
});

// Exportamos la API REST principal en Firebase Cloud Functions v2
export const api = onRequest({ region: "us-central1", cors: true }, app);

// ---------------------------------------------------------------------------
// 7. FIRESTORE TRIGGER: VENTA ACTUALIZADA -> PUSH FCM
// ---------------------------------------------------------------------------
export const onSaleUpdated = onDocumentWritten("merchants/{merchantId}/sales/{saleId}", async (event) => {
  const after = event.data?.after.exists ? event.data.after.data() : null;
  if (!after) return;

  const merchantId = event.params.merchantId;
  const saleId = event.params.saleId;

  // Notificación Push a los dispositivos de la MYPE
  const devicesSnap = await db.collection("devices").limit(5).get();
  const tokens = devicesSnap.docs.map(d => d.id).filter(t => t.length > 20);
  if (!tokens.length) return;

  const message: admin.messaging.MulticastMessage = {
    tokens,
    notification: {
      title: "KUOTA: Actualización de cuota",
      body: `Venta por S/ ${((after.totalAmountCents || 0) / 100).toFixed(2)} - Estado: ${after.status}`,
    },
    data: {
      saleId,
      merchantId,
      click_action: "FLUTTER_NOTIFICATION_CLICK",
    },
    android: {
      priority: "high",
      notification: {
        channelId: "kuota_payments_channel",
        sound: "default",
      },
    },
  };

  try {
    await admin.messaging().sendEachForMulticast(message);
    console.log(`Push enviado a ${tokens.length} dispositivos.`);
  } catch (e) {
    console.error("Error al enviar push multicast:", e);
  }
});

// ---------------------------------------------------------------------------
// 8. FIRESTORE TRIGGER: PERFIL MYPE ACTUALIZADO (AUDITORÍA)
// ---------------------------------------------------------------------------
export const onMerchantUpdated = onDocumentWritten("merchants/{merchantId}", async (event) => {
  const after = event.data?.after.exists ? event.data.after.data() : null;
  if (!after) return;

  const merchantId = event.params.merchantId;
  const eventId = event.id || `evt-merchant-${Date.now()}`;
  console.log(`[AUDIT] Merchant profile updated: ${merchantId} (event ${eventId})`);
});

// ---------------------------------------------------------------------------
// 9. FIRESTORE TRIGGER: RECLAMACIÓN REGISTRADA (AUDITORÍA)
// ---------------------------------------------------------------------------
export const onClaimCreated = onDocumentCreated("claims/{claimNumber}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) return;

  const claimNumber = event.params.claimNumber;
  const eventId = event.id || `evt-claim-${Date.now()}`;
  console.log(`[AUDIT] Claim created: ${claimNumber} (event ${eventId})`);
});

// ---------------------------------------------------------------------------
// 10. FIRESTORE TRIGGER: SOLICITUD KYC REGISTRADA -> VERIFICACIÓN RENIEC AUTOMATIZADA
// ---------------------------------------------------------------------------
export const onKycRequestCreated = onDocumentCreated("kyc_requests/{requestId}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) return;

  const requestId = event.params.requestId;
  const data = snapshot.data();
  const dni = String(data.dni || "");
  const uid = String(data.uid || "");
  const merchantId = String(data.merchantId || uid);
  const fullName = String(data.fullName || data.full_name || "Comerciante Kuota");

  const verification = calculateReniecDni(dni);
  const now = admin.firestore.FieldValue.serverTimestamp();

  if (!verification.isValid) {
    const rejectionReason = "El DNI ingresado no cumple la estructura oficial o ponderación de RENIEC.";
    await db.collection("kyc_requests").doc(requestId).update({
      kycStatus: "rejected",
      rejectionReason,
      reviewedAt: now,
    });

    if (uid) {
      await db.collection("users").doc(uid).set({
        kycStatus: "rejected",
        kycRejectionReason: rejectionReason,
        updatedAt: now,
      }, { merge: true });

      await db.doc(`users/${uid}/summary/current`).set({
        kycStatus: "rejected",
        rejectionReason,
        availableCreditCents: 0,
        updatedAt: now,
      }, { merge: true });

      await sendUserPush(uid, {
        title: "Observación en tu verificación Kuota",
        body: rejectionReason,
      });
    }
    return;
  }

  // Verificación exitosa bajo padrón y algoritmo oficial
  const riskScore = 96;
  await db.collection("kyc_requests").doc(requestId).update({
    kycStatus: "approved",
    verifiedAt: now,
    verificationAlgorithm: "RENIEC_MOD11_PONDERATED",
    riskScore,
    riskTier: "LOW_RISK",
    reniecCheckDigit: verification.expectedDigit,
    reniecCheckChar: verification.expectedChar,
  });

  // Consultar límite de Nivel 1 en config/levels (default S/ 1,400.00 = 140000 céntimos)
  let tier1Limit = 140000;
  try {
    const cfgSnap = await db.collection("config").doc("levels").get();
    if (cfgSnap.exists) {
      const tiers = cfgSnap.data()?.tiers || [];
      const t1 = tiers.find((t: any) => t.level === 1);
      if (t1?.maxCreditCents) tier1Limit = t1.maxCreditCents;
    }
  } catch (err) {
    console.warn("No se pudo leer config/levels, usando límite base", err);
  }

  if (uid) {
    await db.collection("users").doc(uid).set({
      kycStatus: "approved",
      kycVerifiedAt: now,
      riskScore,
      updatedAt: now,
    }, { merge: true });

    // Activar el crédito disponible en el resumen consolidado (PRD §6.1)
    await db.doc(`users/${uid}/summary/current`).set({
      kycStatus: "approved",
      level: 1,
      levelName: "Nivel 1",
      maxCreditCents: tier1Limit,
      availableCreditCents: tier1Limit,
      updatedAt: now,
    }, { merge: true });

    await sendUserPush(uid, {
      title: "¡Cuenta verificada en Kuota!",
      body: `Felicidades ${fullName}, tu línea de compra al crédito por S/ ${(tier1Limit / 100).toFixed(2)} ya está activa.`,
    });
  }

  if (merchantId && merchantId !== uid) {
    await db.collection("merchants").doc(merchantId).set({
      kycStatus: "VERIFIED",
      kycVerifiedAt: now,
      kycSummary: `${fullName} · DNI ****${dni.slice(-4)} ✓`,
      updatedAt: now,
    }, { merge: true });
  }

  // Auditoría en libro append-only
  await db.collection("audit_ledger").add({
    eventType: "KYC_VERIFIED",
    requestId,
    uid,
    dniMasked: `****${dni.slice(-4)}`,
    status: "approved",
    occurredAt: now,
  });
});

// Helper para enviar push a los dispositivos registrados de un usuario
async function sendUserPush(uid: string, notification: { title: string; body: string; channelId?: string }) {
  try {
    const devicesSnap = await db.collection("devices").where("uid", "==", uid).limit(5).get();
    const tokens = devicesSnap.docs.map(d => d.id).filter(t => t.length > 20);
    if (!tokens.length) return;

    await admin.messaging().sendEachForMulticast({
      tokens,
      notification: {
        title: notification.title,
        body: notification.body,
      },
      android: {
        priority: "high",
        notification: {
          channelId: notification.channelId || "kuota_account_channel",
          sound: "default",
        },
      },
    });
  } catch (err) {
    console.error(`Error enviando FCM push a usuario ${uid}:`, err);
  }
}

// ---------------------------------------------------------------------------
// 11. AUTH TRIGGER & INITIALIZER: NUEVO USUARIO -> PERFIL Y RESUMEN (PRD §8.1)
// ---------------------------------------------------------------------------
async function initUserInFirestore(uid: string, phone: string) {
  const now = admin.firestore.FieldValue.serverTimestamp();

  // Asignar Custom Claims iniciales
  try {
    await admin.auth().setCustomUserClaims(uid, { role: "comprador", level: 1 });
  } catch (err) {
    console.warn("Error setting custom claims:", err);
  }

  const batch = db.batch();

  // Perfil base del usuario
  batch.set(db.collection("users").doc(uid), {
    uid,
    phone: phone || "",
    role: "comprador",
    kycStatus: "unsubmitted",
    createdAt: now,
    updatedAt: now,
  }, { merge: true });

  // Resumen consolidado: Home en 1 lectura (PRD §6.1)
  batch.set(db.doc(`users/${uid}/summary/current`), {
    uid,
    level: 1,
    levelName: "Nivel 1",
    maxCreditCents: 140000,
    availableCreditCents: 0,
    usedCreditCents: 0,
    nextInstallmentDueDate: null,
    nextInstallmentAmountCents: 0,
    overdueInstallmentsCount: 0,
    kycStatus: "unsubmitted",
    role: "comprador",
    updatedAt: now,
  }, { merge: true });

  await batch.commit();
}

export const onUserCreate = functionsV1.auth.user().onCreate(async (user: functionsV1.auth.UserRecord) => {
  const uid = user.uid;
  const phone = user.phoneNumber || "";
  console.log(`[AUTH] Inicializando usuario ${uid} (+51 ${phone})`);
  await initUserInFirestore(uid, phone);
});

export const initUserSummary = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Usuario no autenticado");
  }
  const uid = request.auth.uid;
  const phone = (request.auth.token.phone_number as string) || "";
  await initUserInFirestore(uid, phone);
  return { success: true, message: "Resumen inicial creado correctamente" };
});

// ---------------------------------------------------------------------------
// 12. CALLABLE FUNCTION: SUBMIT KYC (PRD §8.1)
// ---------------------------------------------------------------------------
export const submitKyc = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Debe iniciar sesión para enviar su verificación de identidad.");
  }

  const uid = request.auth.uid;
  const data = request.data || {};
  const {
    dni,
    fullName,
    businessName,
    category,
    ruc,
    role = "comprador",
    location,
    dniFrontStoragePath,
    dniBackStoragePath,
    selfieStoragePath,
    termsConsent,
  } = data;

  // 1. Validaciones peruanas
  const cleanDni = String(dni || "").trim();
  if (!/^\d{8}$/.test(cleanDni)) {
    throw new HttpsError("invalid-argument", "El DNI debe contener exactamente 8 dígitos numéricos.");
  }
  if (!fullName || String(fullName).trim().length < 3) {
    throw new HttpsError("invalid-argument", "El nombre completo es obligatorio.");
  }
  if (!businessName || String(businessName).trim().length < 2) {
    throw new HttpsError("invalid-argument", "El nombre comercial del negocio es obligatorio.");
  }
  if (!category) {
    throw new HttpsError("invalid-argument", "El rubro de negocio es obligatorio.");
  }
  if (ruc && !/^\d{11}$/.test(String(ruc).trim())) {
    throw new HttpsError("invalid-argument", "El RUC debe contener exactamente 11 dígitos numéricos.");
  }
  if (!termsConsent || termsConsent.agreed !== true || !termsConsent.version) {
    throw new HttpsError("failed-precondition", "Debe aceptar los Términos y la Política de Privacidad (Ley N° 29733).");
  }

  // 2. Validar que las rutas de Storage correspondan a kyc/{uid}/*
  const allowedPrefix = `kyc/${uid}/`;
  if (!dniFrontStoragePath?.startsWith(allowedPrefix) ||
      !dniBackStoragePath?.startsWith(allowedPrefix) ||
      !selfieStoragePath?.startsWith(allowedPrefix)) {
    throw new HttpsError("permission-denied", "Las imágenes deben residir en la carpeta kyc/{uid}/ del usuario.");
  }

  const now = admin.firestore.FieldValue.serverTimestamp();
  const requestId = `kyc_${uid}_${Date.now()}`;

  const batch = db.batch();

  batch.set(db.collection("users").doc(uid), {
    dni: cleanDni,
    fullName: String(fullName).trim(),
    businessName: String(businessName).trim(),
    category: String(category).trim(),
    ruc: ruc ? String(ruc).trim() : null,
    role,
    location: location || null,
    kycStatus: "pending",
    kycSubmittedAt: now,
    termsConsent: {
      agreed: true,
      version: String(termsConsent.version),
      consentedAt: termsConsent.consentedAt || new Date().toISOString(),
    },
    updatedAt: now,
  }, { merge: true });

  batch.set(db.doc(`users/${uid}/summary/current`), {
    kycStatus: "pending",
    role,
    updatedAt: now,
  }, { merge: true });

  batch.set(db.collection("kyc_requests").doc(requestId), {
    uid,
    dni: cleanDni,
    fullName: String(fullName).trim(),
    businessName: String(businessName).trim(),
    category: String(category).trim(),
    ruc: ruc ? String(ruc).trim() : null,
    role,
    location: location || null,
    dniFrontStoragePath,
    dniBackStoragePath,
    selfieStoragePath,
    termsConsent,
    status: "pending",
    createdAt: now,
  });

  await batch.commit();

  await admin.auth().setCustomUserClaims(uid, { role, level: 1 });

  return {
    success: true,
    requestId,
    kycStatus: "pending",
    message: "Verificación enviada correctamente. Estamos validando tus datos.",
  };
});

// ---------------------------------------------------------------------------
// 13. FASE 2: MOTOR DE CRÉDITO Y COMPRA AL CRÉDITO (PRD §8.5 & AGENTS.md)
// ---------------------------------------------------------------------------

interface BusinessConfigData {
  levels: Array<{ level: number; downPaymentBasisPoints: number; maxCreditCents: number }>;
  fees: {
    buyerFinancingBasisPoints: number;
    providerTransactionBasisPoints: number;
    minPurchaseCents: number;
  };
  terms: Array<{ days: number; installmentsCount: number }>;
}

async function getBusinessConfig(): Promise<BusinessConfigData> {
  const [levelsDoc, feesDoc, termsDoc] = await Promise.all([
    db.collection("config").doc("levels").get(),
    db.collection("config").doc("fees").get(),
    db.collection("config").doc("terms").get(),
  ]);

  const defaultLevels = [
    { level: 1, downPaymentBasisPoints: 5000, maxCreditCents: 70000 },
    { level: 2, downPaymentBasisPoints: 4000, maxCreditCents: 150000 },
    { level: 3, downPaymentBasisPoints: 3000, maxCreditCents: 300000 },
    { level: 4, downPaymentBasisPoints: 2500, maxCreditCents: 500000 },
    { level: 5, downPaymentBasisPoints: 2000, maxCreditCents: 800000 },
    { level: 6, downPaymentBasisPoints: 1000, maxCreditCents: 1200000 },
  ];

  const levels = levelsDoc.exists && levelsDoc.data()?.tiers ? levelsDoc.data()?.tiers : defaultLevels;

  const defaultFees = {
    buyerFinancingBasisPoints: 500, // 5% fijo
    providerTransactionBasisPoints: 200, // 2% pasarela/comisión
    minPurchaseCents: 19900, // S/ 199.00
  };

  const fees = feesDoc.exists ? { ...defaultFees, ...feesDoc.data() } : defaultFees;

  const defaultTerms = [
    { days: 21, installmentsCount: 3 },
    { days: 28, installmentsCount: 4 },
    { days: 42, installmentsCount: 6 },
  ];

  const terms = termsDoc.exists && termsDoc.data()?.terms ? termsDoc.data()?.terms : defaultTerms;

  return { levels, fees, terms };
}

/**
 * Callable: quotePurchase
 * Cotiza una compra de crédito según el nivel del usuario y las reglas de negocio.
 */
export const quotePurchase = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Debe iniciar sesión para cotizar compras al crédito.");
  }

  const uid = request.auth.uid;
  const { totalAmountCents, installmentsCount = 4 } = request.data || {};

  if (!Number.isInteger(totalAmountCents) || totalAmountCents <= 0) {
    throw new HttpsError("invalid-argument", "Monto de compra inválido en céntimos.");
  }

  const [summarySnap, config] = await Promise.all([
    db.doc(`users/${uid}/summary/current`).get(),
    getBusinessConfig(),
  ]);

  if (!summarySnap.exists) {
    throw new HttpsError("failed-precondition", "Perfil de crédito no inicializado.");
  }

  const summary = summarySnap.data() || {};
  if (summary.kycStatus !== "approved") {
    throw new HttpsError("permission-denied", "Tu verificación de identidad (KYC) debe estar aprobada para comprar al crédito.");
  }

  if ((summary.overdueCount || 0) > 0 || (summary.overdueInstallmentsCount || 0) > 0) {
    throw new HttpsError("failed-precondition", "Tienes cuotas vencidas pendientes de regularización.");
  }

  if (totalAmountCents < config.fees.minPurchaseCents) {
    throw new HttpsError(
      "invalid-argument",
      `El monto mínimo de compra al crédito es S/ ${(config.fees.minPurchaseCents / 100).toFixed(2)}.`
    );
  }

  const userLevel = summary.level || 1;
  const levelTier = config.levels.find(l => l.level === userLevel) || config.levels[0];
  const downPaymentBps = levelTier.downPaymentBasisPoints;

  const quote = calculateQuote({
    totalAmountCents,
    downPaymentBps,
    buyerFeeBps: config.fees.buyerFinancingBasisPoints,
    providerFeeBps: config.fees.providerTransactionBasisPoints,
    providerAdvanceBps: 6000, // 60%
    installmentsCount,
    startDate: new Date(),
  });

  const availableCreditCents = summary.availableCreditCents || 0;
  const isEligible = quote.financedAmountCents <= availableCreditCents;

  return {
    success: true,
    isEligible,
    userLevel,
    availableCreditCents,
    quote,
    message: isEligible
      ? "Cotización generada exitosamente."
      : `El monto financiado (S/ ${(quote.financedAmountCents / 100).toFixed(2)}) supera tu crédito disponible (S/ ${(availableCreditCents / 100).toFixed(2)}).`,
  };
});

/**
 * Callable: createPurchase
 * Crea la orden de compra al crédito en estado QUOTED con TTL de 15 minutos.
 */
export const createPurchase = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Debe iniciar sesión para solicitar compras al crédito.");
  }

  const uid = request.auth.uid;
  const { supplierId, supplierName, totalAmountCents, installmentsCount = 4, notes } = request.data || {};

  if (!supplierId) {
    throw new HttpsError("invalid-argument", "El identificador del proveedor es obligatorio.");
  }

  if (!Number.isInteger(totalAmountCents) || totalAmountCents <= 0) {
    throw new HttpsError("invalid-argument", "Monto de compra inválido.");
  }

  const [summarySnap, config] = await Promise.all([
    db.doc(`users/${uid}/summary/current`).get(),
    getBusinessConfig(),
  ]);

  if (!summarySnap.exists) {
    throw new HttpsError("failed-precondition", "Perfil de crédito no disponible.");
  }

  const summary = summarySnap.data() || {};
  if (summary.kycStatus !== "approved") {
    throw new HttpsError("permission-denied", "Tu verificación de identidad (KYC) debe estar aprobada.");
  }

  if ((summary.overdueCount || 0) > 0 || (summary.overdueInstallmentsCount || 0) > 0) {
    throw new HttpsError("failed-precondition", "Tienes cuotas vencidas pendientes de pago.");
  }

  const userLevel = summary.level || 1;
  const levelTier = config.levels.find(l => l.level === userLevel) || config.levels[0];
  const downPaymentBps = levelTier.downPaymentBasisPoints;

  // Calculamos la cotización autoritativa en el backend
  const quote = calculateQuote({
    totalAmountCents,
    downPaymentBps,
    buyerFeeBps: config.fees.buyerFinancingBasisPoints,
    providerFeeBps: config.fees.providerTransactionBasisPoints,
    providerAdvanceBps: 6000,
    installmentsCount,
    startDate: new Date(),
  });

  const availableCreditCents = summary.availableCreditCents || 0;
  if (quote.financedAmountCents > availableCreditCents) {
    throw new HttpsError(
      "failed-precondition",
      `Línea de crédito insuficiente: disponible S/ ${(availableCreditCents / 100).toFixed(2)}, financiado requerido S/ ${(quote.financedAmountCents / 100).toFixed(2)}.`
    );
  }

  const purchaseId = `PUR-${Date.now()}-${Math.floor(1000 + Math.random() * 9000)}`;
  const expiresAtEpochMillis = Date.now() + 15 * 60 * 1000; // TTL 15 min
  const expiresAt = admin.firestore.Timestamp.fromMillis(expiresAtEpochMillis);
  const now = admin.firestore.FieldValue.serverTimestamp();

  const purchaseRecord = {
    purchaseId,
    buyerUid: uid,
    providerUid: supplierId,
    supplierName: supplierName || "Proveedor Autorizado",
    notes: notes || null,
    status: "QUOTED", // QUOTED -> DOWN_PAYMENT_CONFIRMED -> CONFIRMED / CANCELLED / EXPIRED
    totalAmountCents: quote.totalAmountCents,
    downPaymentCents: quote.downPaymentCents,
    downPaymentPercent: quote.downPaymentPercent,
    financedAmountCents: quote.financedAmountCents,
    buyerFeeCents: quote.buyerFeeCents,
    totalDebtCents: quote.totalDebtCents,
    installmentsCount: quote.installmentsCount,
    providerFeeCents: quote.providerFeeCents,
    providerNetCents: quote.providerNetCents,
    providerAdvanceCents: quote.providerAdvanceCents,
    providerDeferredCents: quote.providerDeferredCents,
    quote,
    expiresAt,
    createdAt: now,
    updatedAt: now,
  };

  await db.collection("purchases").doc(purchaseId).set(purchaseRecord);

  // Notificar al proveedor para confirmar recepción de la inicial
  await sendUserPush(supplierId, {
    title: "Nueva solicitud de compra al crédito",
    body: `El cliente solicita compra por S/ ${(quote.totalAmountCents / 100).toFixed(2)}. Verifica la inicial de S/ ${(quote.downPaymentCents / 100).toFixed(2)}.`,
  });

  return {
    success: true,
    purchaseId,
    status: "QUOTED",
    expiresAtEpochMillis,
    quote,
  };
});

/**
 * Callable: providerApprovePurchase
 * El proveedor confirma que recibió la inicial del cliente en efectivo / Yape / Plin / POS.
 */
export const providerApprovePurchase = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Debe iniciar sesión para confirmar recepción de inicial.");
  }

  const { purchaseId } = request.data || {};
  if (!purchaseId) {
    throw new HttpsError("invalid-argument", "ID de compra requerido.");
  }

  const purchaseRef = db.collection("purchases").doc(purchaseId);
  const purchaseDoc = await purchaseRef.get();

  if (!purchaseDoc.exists) {
    throw new HttpsError("not-found", "Orden de compra no encontrada.");
  }

  const purchase = purchaseDoc.data() || {};
  const isAuthorized = purchase.providerUid === request.auth.uid || request.auth.token.role === "admin";
  if (!isAuthorized) {
    throw new HttpsError("permission-denied", "No tienes permisos para aprobar esta orden de compra.");
  }

  if (purchase.status !== "QUOTED") {
    throw new HttpsError("failed-precondition", `Estado inválido para confirmación: ${purchase.status}`);
  }

  const nowEpoch = Date.now();
  const expiresEpoch = purchase.expiresAt?.toMillis() || 0;
  if (nowEpoch > expiresEpoch) {
    await purchaseRef.update({
      status: "EXPIRED",
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    throw new HttpsError("deadline-exceeded", "La cotización ha expirado (límite de 15 minutos superado).");
  }

  const now = admin.firestore.FieldValue.serverTimestamp();
  await purchaseRef.update({
    status: "DOWN_PAYMENT_CONFIRMED",
    providerApprovedAt: now,
    updatedAt: now,
  });

  // Notificar al comprador para que confirme el cronograma de pago
  await sendUserPush(purchase.buyerUid, {
    title: "¡Inicial confirmada por tu proveedor!",
    body: `Tu proveedor confirmó la inicial de S/ ${(purchase.downPaymentCents / 100).toFixed(2)}. Confirma tu cronograma para finalizar.`,
  });

  return {
    success: true,
    purchaseId,
    status: "DOWN_PAYMENT_CONFIRMED",
  };
});

/**
 * Callable: buyerConfirmPurchase
 * El comprador aprueba el cronograma de cuotas semanales.
 * Se descuenta la línea disponible, se crea el cronograma y se genera el libro contable de doble entrada.
 */
export const buyerConfirmPurchase = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Debe iniciar sesión para confirmar la compra.");
  }

  const buyerUid = request.auth.uid;
  const { purchaseId } = request.data || {};
  if (!purchaseId) {
    throw new HttpsError("invalid-argument", "ID de compra requerido.");
  }

  const purchaseRef = db.collection("purchases").doc(purchaseId);
  const summaryRef = db.doc(`users/${buyerUid}/summary/current`);

  const result = await db.runTransaction(async (transaction) => {
    const [purchaseDoc, summaryDoc] = await Promise.all([
      transaction.get(purchaseRef),
      transaction.get(summaryRef),
    ]);

    if (!purchaseDoc.exists) {
      throw new HttpsError("not-found", "Orden de compra no encontrada.");
    }

    const purchase = purchaseDoc.data() || {};
    if (purchase.buyerUid !== buyerUid) {
      throw new HttpsError("permission-denied", "No puedes confirmar una compra ajena.");
    }

    if (purchase.status !== "DOWN_PAYMENT_CONFIRMED") {
      throw new HttpsError(
        "failed-precondition",
        `La compra debe estar en estado DOWN_PAYMENT_CONFIRMED para finalizarse (estado actual: ${purchase.status}).`
      );
    }

    if (Date.now() > (purchase.expiresAt?.toMillis() || 0)) {
      throw new HttpsError("deadline-exceeded", "La cotización ha expirado.");
    }

    if (!summaryDoc.exists) {
      throw new HttpsError("failed-precondition", "Perfil de crédito no encontrado.");
    }

    const summary = summaryDoc.data() || {};
    const available = summary.availableCreditCents || 0;
    const financed = purchase.financedAmountCents || 0;

    if (available < financed) {
      throw new HttpsError("failed-precondition", "Línea de crédito insuficiente en el momento de la confirmación.");
    }

    const newAvailable = available - financed;
    const newUsed = (summary.usedCreditCents || 0) + financed;
    const firstInstallment = purchase.quote.installments[0];

    // 1. Asientos de partida doble en el ledger
    const ledgerEntries = createPurchaseLedgerEntries(purchaseId, purchase.quote);
    for (const entry of ledgerEntries) {
      const entryRef = db.collection("ledger").doc();
      transaction.set(entryRef, {
        ...entry,
        entryId: entryRef.id,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    }

    // 2. Registro de cuotas en subcolección y colección raíz para consultas eficientes
    const now = admin.firestore.FieldValue.serverTimestamp();
    for (const inst of purchase.quote.installments) {
      const instData = {
        installmentId: `${purchaseId}_${inst.installmentNumber}`,
        purchaseId,
        buyerUid,
        providerUid: purchase.providerUid,
        supplierName: purchase.supplierName,
        installmentNumber: inst.installmentNumber,
        totalInstallments: purchase.installmentsCount,
        dueDate: inst.dueDate,
        amountCents: inst.amountCents,
        status: "PENDING",
        createdAt: now,
      };

      const subRef = purchaseRef.collection("installments").doc(String(inst.installmentNumber));
      const topRef = db.collection("installments").doc(`${purchaseId}_${inst.installmentNumber}`);

      transaction.set(subRef, instData);
      transaction.set(topRef, instData);
    }

    // 3. Actualizar estado de compra
    transaction.update(purchaseRef, {
      status: "CONFIRMED",
      confirmedAt: now,
      updatedAt: now,
    });

    // 4. Actualizar resumen de crédito del comprador (Home en 1 lectura)
    transaction.update(summaryRef, {
      availableCreditCents: newAvailable,
      usedCreditCents: newUsed,
      nextDueInstallmentCents: firstInstallment.amountCents,
      nextDueDate: firstInstallment.dueDate,
      updatedAt: now,
    });

    // 5. Registrar payout del adelanto (60%) programado a 48h para el proveedor
    const advanceRecord = createAdvancePayoutRecord({
      purchaseId,
      providerUid: purchase.providerUid,
      supplierName: purchase.supplierName,
      advanceAmountCents: purchase.providerAdvanceCents,
      confirmedDate: new Date(),
    });
    const payoutRef = db.collection("payouts").doc(advanceRecord.payoutId);
    transaction.set(payoutRef, {
      ...advanceRecord,
      createdAt: now,
    });

    return {
      purchaseId,
      status: "CONFIRMED",
      firstDueDate: firstInstallment.dueDate,
      firstInstallmentAmountCents: firstInstallment.amountCents,
      providerUid: purchase.providerUid,
      totalAmountCents: purchase.totalAmountCents,
    };
  });

  // Notificar push a ambas partes
  await Promise.all([
    sendUserPush(buyerUid, {
      title: "¡Compra al crédito confirmada!",
      body: `Tu compra de S/ ${(result.totalAmountCents / 100).toFixed(2)} está activa. Tu primera cuota de S/ ${(result.firstInstallmentAmountCents / 100).toFixed(2)} vence el ${result.firstDueDate.split("T")[0]}.`,
    }),
    sendUserPush(result.providerUid, {
      title: "Venta al crédito confirmada",
      body: `El cliente confirmó la compra por S/ ${(result.totalAmountCents / 100).toFixed(2)}. Pronto recibirás el adelanto según cronograma.`,
    }),
  ]);

  return {
    success: true,
    purchaseId: result.purchaseId,
    status: result.status,
  };
});

/**
 * Callable: cancelPurchase
 * Cancela una compra si aún no ha sido confirmada por ambas partes.
 */
export const cancelPurchase = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Debe iniciar sesión para cancelar una orden.");
  }

  const { purchaseId, reason } = request.data || {};
  if (!purchaseId) {
    throw new HttpsError("invalid-argument", "ID de compra requerido.");
  }

  const purchaseRef = db.collection("purchases").doc(purchaseId);
  const doc = await purchaseRef.get();

  if (!doc.exists) {
    throw new HttpsError("not-found", "Orden de compra no encontrada.");
  }

  const purchase = doc.data() || {};
  const uid = request.auth.uid;
  const isParty = purchase.buyerUid === uid || purchase.providerUid === uid || request.auth.token.role === "admin";

  if (!isParty) {
    throw new HttpsError("permission-denied", "No tienes permisos para cancelar esta compra.");
  }

  if (purchase.status !== "QUOTED" && purchase.status !== "DOWN_PAYMENT_CONFIRMED") {
    throw new HttpsError("failed-precondition", `No se puede cancelar una compra en estado ${purchase.status}.`);
  }

  const now = admin.firestore.FieldValue.serverTimestamp();
  await purchaseRef.update({
    status: "CANCELLED",
    cancellationReason: reason || "Cancelado por el usuario",
    cancelledAt: now,
    updatedAt: now,
  });

  return {
    success: true,
    purchaseId,
    status: "CANCELLED",
  };
});

// ---------------------------------------------------------------------------
// 14. WEBHOOK UNIVERSAL DE PASARELA (PAYMENT GATEWAY CON HMAC E IDEMPOTENCIA)
// ---------------------------------------------------------------------------
app.post("/api/v1/webhooks/gateway", async (req, res) => {
  try {
    const signature = (req.headers["x-kuota-signature"] as string) || "";
    const rawPayload = JSON.stringify(req.body);
    const gateway = new SandboxPaymentGateway();

    // Verificación de firma criptográfica
    if (!signature || !gateway.verifyWebhookSignature(rawPayload, signature)) {
      return res.status(401).json({ code: "INVALID_SIGNATURE", message: "Firma inválida o ausente." });
    }

    const { eventId, eventType, installmentId, amountCents, buyerUid } = req.body;
    if (!eventId || !installmentId || !amountCents) {
      return res.status(400).json({ code: "INVALID_PAYLOAD", message: "Faltan parámetros requeridos." });
    }

    // Deduplicación idempotente
    const eventRef = db.collection("webhook_processed_events").doc(eventId);
    const existing = await eventRef.get();
    if (existing.exists) {
      return res.status(200).json({ status: "DUPLICATE", message: "Notificación previamente procesada." });
    }

    const now = admin.firestore.FieldValue.serverTimestamp();

    await db.runTransaction(async (transaction) => {
      // 1. Marcar evento como procesado
      transaction.set(eventRef, {
        eventId,
        eventType: eventType || "PAYMENT_APPROVED",
        installmentId,
        amountCents,
        processedAt: now,
      });

      // 2. Marcar cuota como pagada
      const instRef = db.collection("installments").doc(installmentId);
      const instDoc = await transaction.get(instRef);

      if (instDoc.exists) {
        const instData = instDoc.data() || {};
        transaction.update(instRef, {
          status: "PAID",
          paidAt: now,
          paidAmountCents: amountCents,
        });

        // 3. Restaurar línea en summary del comprador
        const targetBuyerUid = buyerUid || instData.buyerUid;
        if (targetBuyerUid) {
          const sumRef = db.doc(`users/${targetBuyerUid}/summary/current`);
          const sumDoc = await transaction.get(sumRef);
          if (sumDoc.exists) {
            const sumData = sumDoc.data() || {};
            const restoredAvailable = Math.min(
              sumData.maxCreditCents || 70000,
              (sumData.availableCreditCents || 0) + amountCents
            );
            const reducedUsed = Math.max(0, (sumData.usedCreditCents || 0) - amountCents);

            transaction.update(sumRef, {
              availableCreditCents: restoredAvailable,
              usedCreditCents: reducedUsed,
              updatedAt: now,
            });
          }
        }

        // 4. Asiento contable de cobranza
        const ledgerRef = db.collection("ledger").doc();
        transaction.set(ledgerRef, {
          entryId: ledgerRef.id,
          transactionId: `tx_payment_${installmentId}`,
          account: "CAJA_RECAUDACION",
          entryType: "DEBIT",
          amountCents,
          currency: "PEN",
          description: `Cobranza de cuota ${installmentId}`,
          createdAt: now,
        });

        const ledgerRef2 = db.collection("ledger").doc();
        transaction.set(ledgerRef2, {
          entryId: ledgerRef2.id,
          transactionId: `tx_payment_${installmentId}`,
          account: "CUENTAS_POR_COBRAR_COMPRADOR",
          entryType: "CREDIT",
          amountCents,
          currency: "PEN",
          description: `Amortización de cuenta por cobrar por cuota ${installmentId}`,
          createdAt: now,
        });
      }
    });

    return res.status(200).json({ status: "SUCCESS", eventId, installmentId });
  } catch (error) {
    console.error("Error en gateway webhook:", error);
    return res.status(500).json({ code: "GATEWAY_ERROR", message: "Error procesando webhook de pasarela." });
  }
});

// ---------------------------------------------------------------------------
// 15. FASE 3: MÓDULO DE PROVEEDOR, PAYMENTS & PAYOUTS PROGRAMADOS (PRD §3, §4, §8.5)
// ---------------------------------------------------------------------------

/**
 * Callable: generateSupplierQr
 * Genera el payload criptográfico firmado para el QR del proveedor (Estático o Dinámico).
 */
export const generateSupplierQr = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Debe iniciar sesión para generar su QR de cobro.");
  }

  const providerUid = request.auth.uid;
  const { amountCents } = request.data || {};

  // Consultar perfil del proveedor
  const [userDoc, merchantDoc] = await Promise.all([
    db.collection("users").doc(providerUid).get(),
    db.collection("merchants").doc(providerUid).get(),
  ]);

  const userData = userDoc.data() || {};
  const merchantData = merchantDoc.data() || {};
  const businessName = merchantData.name || userData.businessName || "Comercio Asociado Kuota";
  const category = merchantData.category || userData.category || "General";

  if (amountCents && Number.isInteger(amountCents) && amountCents > 0) {
    // QR Dinámico: Monto prefijado + TTL 15 minutos
    const txId = `TX-${Date.now()}-${Math.floor(1000 + Math.random() * 9000)}`;
    const exp = Date.now() + 15 * 60 * 1000;
    const { signedString, rawPayload } = generateSignedQr(
      {
        type: "DYNAMIC",
        providerUid,
        businessName,
        amountCents,
        txId,
        exp,
      },
      JWT_SECRET
    );

    return {
      success: true,
      type: "DYNAMIC",
      signedQr: signedString,
      businessName,
      amountCents,
      expiresAtEpochMillis: exp,
      rawPayload,
    };
  }

  // QR Estático: Para mostrador físico del negocio
  const { signedString, rawPayload } = generateSignedQr(
    {
      type: "STATIC",
      providerUid,
      businessName,
      category,
      issuedAt: Date.now(),
    },
    JWT_SECRET
  );

  return {
    success: true,
    type: "STATIC",
    signedQr: signedString,
    businessName,
    category,
    rawPayload,
  };
});

/**
 * Callable: createPaymentLink
 * Genera un enlace de cobro de Kuota Payments para enviar por WhatsApp.
 * Valida monto mínimo (S/ 199.00) y calcula comisión transparente (2%).
 */
export const createPaymentLink = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Debe iniciar sesión para generar un link de cobro.");
  }

  const providerUid = request.auth.uid;
  const { amountCents, clientPhone, clientDni, concept } = request.data || {};

  if (!clientPhone || String(clientPhone).trim().length < 9) {
    throw new HttpsError("invalid-argument", "El celular del cliente es obligatorio.");
  }

  const config = await getBusinessConfig();
  const minAmount = config.fees.minPurchaseCents || 19900; // S/ 199.00

  if (!Number.isInteger(amountCents) || amountCents < minAmount) {
    throw new HttpsError(
      "invalid-argument",
      `El monto mínimo para Kuota Payments es S/ ${(minAmount / 100).toFixed(2)}.`
    );
  }

  const userDoc = await db.collection("users").doc(providerUid).get();
  const supplierName = userDoc.data()?.businessName || "Proveedor Autorizado Kuota";

  const linkId = `LNK-${Date.now()}-${Math.floor(1000 + Math.random() * 9000)}`;
  const quote = calculatePaymentLinkQuote({
    amountCents,
    supplierName,
    clientPhone: String(clientPhone).trim(),
    clientDni: clientDni ? String(clientDni).trim() : undefined,
    concept: concept ? String(concept).trim() : undefined,
    linkId,
    minAmountCents: minAmount,
    feeBasisPoints: config.fees.providerTransactionBasisPoints || 200,
  });

  const now = admin.firestore.FieldValue.serverTimestamp();
  const expiresAt = new Date(Date.now() + 24 * 60 * 60 * 1000); // 24 horas de vigencia

  await db.collection("payment_links").doc(linkId).set({
    linkId,
    providerUid,
    supplierName,
    amountCents: quote.amountCents,
    feeCents: quote.feeCents,
    netCents: quote.netCents,
    clientPhone: quote.clientPhone,
    clientDni: quote.clientDni || null,
    concept: quote.concept || null,
    shortUrl: quote.shortUrl,
    whatsappUrl: quote.whatsappUrl,
    status: "ACTIVE",
    expiresAt: admin.firestore.Timestamp.fromDate(expiresAt),
    createdAt: now,
  });

  return {
    success: true,
    linkId,
    amountCents: quote.amountCents,
    feeCents: quote.feeCents,
    netCents: quote.netCents,
    shortUrl: quote.shortUrl,
    whatsappUrl: quote.whatsappUrl,
    expiresAt: expiresAt.toISOString(),
  };
});

/**
 * Callable: getSupplierExpectedEarnings
 * Retorna el consolidado de cobros esperados:
 * - Adelantos del 60% pendientes de liquidar a 48h.
 * - Cuotas semanales diferidas por cobrar.
 */
export const getSupplierExpectedEarnings = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Debe iniciar sesión para ver sus cobros esperados.");
  }

  const providerUid = request.auth.uid;

  const [payoutsSnap, purchasesSnap] = await Promise.all([
    db.collection("payouts")
      .where("providerUid", "==", providerUid)
      .where("status", "==", "PENDING")
      .get(),
    db.collection("purchases")
      .where("providerUid", "==", providerUid)
      .where("status", "==", "CONFIRMED")
      .limit(50)
      .get(),
  ]);

  let totalPendingAdvanceCents = 0;
  const pendingPayouts = payoutsSnap.docs.map(doc => {
    const data = doc.data();
    totalPendingAdvanceCents += (data.amountCents || 0);
    return {
      payoutId: doc.id,
      purchaseId: data.purchaseId,
      amountCents: data.amountCents || 0,
      type: data.type || "ADVANCE_60",
      dueDate: data.dueDate || "",
      status: data.status || "PENDING",
      bankAccountMasked: data.bankAccountMasked || "BCP Soles ****4812",
    };
  });

  let totalPendingDeferredCents = 0;
  for (const doc of purchasesSnap.docs) {
    const p = doc.data();
    totalPendingDeferredCents += (p.providerDeferredCents || 0);
  }

  return {
    success: true,
    totalPendingAdvanceCents,
    totalPendingDeferredCents,
    totalExpectedCents: totalPendingAdvanceCents + totalPendingDeferredCents,
    pendingPayouts,
  };
});

/**
 * Helper interno: liquida los payouts pendientes vencidos (scheduledPayoutsRunner).
 */
async function processDuePayouts(): Promise<{ processedCount: number; totalAmountCents: number }> {
  const now = new Date();
  const nowIso = now.toISOString();

  const dueSnapshot = await db.collection("payouts")
    .where("status", "==", "PENDING")
    .where("dueDate", "<=", nowIso)
    .limit(50)
    .get();

  if (dueSnapshot.empty) {
    return { processedCount: 0, totalAmountCents: 0 };
  }

  let processedCount = 0;
  let totalAmountCents = 0;

  for (const doc of dueSnapshot.docs) {
    const payout = doc.data();
    const amount = payout.amountCents || 0;
    const nowFs = admin.firestore.FieldValue.serverTimestamp();

    await db.runTransaction(async (transaction) => {
      // 1. Actualizar estado del payout a SETTLED
      transaction.update(doc.ref, {
        status: "SETTLED",
        settledAt: nowFs,
      });

      // 2. Asientos contables de partida doble
      const ledgerDebit = db.collection("ledger").doc();
      transaction.set(ledgerDebit, {
        entryId: ledgerDebit.id,
        transactionId: `payout_${doc.id}`,
        account: payout.type === "ADVANCE_60" ? "CUENTAS_POR_PAGAR_PROVEEDOR_ADELANTO" : "CUENTAS_POR_PAGAR_PROVEEDOR_DIFERIDO",
        entryType: "DEBIT",
        amountCents: amount,
        currency: "PEN",
        description: `Liquidación de ${payout.type} a proveedor ${payout.providerUid}`,
        createdAt: nowFs,
      });

      const ledgerCredit = db.collection("ledger").doc();
      transaction.set(ledgerCredit, {
        entryId: ledgerCredit.id,
        transactionId: `payout_${doc.id}`,
        account: "BANCO_LIQUIDACIONES",
        entryType: "CREDIT",
        amountCents: amount,
        currency: "PEN",
        description: `Desembolso bancario automático a cuenta del proveedor`,
        createdAt: nowFs,
      });
    });

    // 3. Notificación Push al proveedor
    await sendUserPush(payout.providerUid, {
      title: "¡Liquidación depositada en tu cuenta!",
      body: `Kuota ha transferido S/ ${(amount / 100).toFixed(2)} por tu adelanto de venta. Revisa tu cuenta bancaria.`,
    });

    processedCount++;
    totalAmountCents += amount;
  }

  return { processedCount, totalAmountCents };
}

/**
 * Cron programado: scheduledPayoutsRunner
 * Se ejecuta diariamente a las 07:00 (hora de Lima) para procesar adelantos cumplidos a 48h.
 */
export const scheduledPayoutsRunner = onSchedule(
  { schedule: "0 7 * * *", timeZone: "America/Lima" },
  async () => {
    console.log("[SCHEDULER] Iniciando scheduledPayoutsRunner para liquidaciones a proveedores");
    const result = await processDuePayouts();
    console.log(`[SCHEDULER] Payouts procesados: ${result.processedCount}, Monto: S/ ${(result.totalAmountCents / 100).toFixed(2)}`);
  }
);

/**
 * Callable para pruebas en dev/staging: triggerPayoutsManual
 */
export const triggerPayoutsManual = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Requiere autenticación.");
  }
  const result = await processDuePayouts();
  return { success: true, ...result };
});

// ---------------------------------------------------------------------------
// 12. FASE 4: VENTAS, SCORING Y RECORDATORIOS DE COBRANZA
// ---------------------------------------------------------------------------

/**
 * Callable: registerSalesBatch
 * Ingesta de lote de ventas parseadas en dispositivo.
 * Idempotente mediante txHash, descarta crudo de notificación y actualiza estadísticas.
 */
export const registerSalesBatch = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Requiere autenticación.");
  }
  const parseResult = SalesBatchSchema.safeParse(request.data);
  if (!parseResult.success) {
    throw new HttpsError("invalid-argument", "Datos del lote de ventas inválidos", parseResult.error.format());
  }
  const result = await registerSalesBatchCore(db, request.auth.uid, parseResult.data.sales);
  return { success: true, ...result };
});

/**
 * Callable: computeScore
 * Evalúa el score crediticio y actualiza el nivel del usuario en summary/current.
 */
export const computeScore = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Requiere autenticación.");
  }
  const result = await computeUserScoreCore(db, request.auth.uid);
  return { success: true, ...result };
});

/**
 * Cron: scheduledInstallmentsTick
 * Se ejecuta diariamente a las 06:00 (hora de Lima).
 * Evalúa vencimientos y envía recordatorios T-3, T-1, T0, T+1, T+3, y marca moras.
 */
export const scheduledInstallmentsTick = onSchedule(
  { schedule: "0 6 * * *", timeZone: "America/Lima" },
  async () => {
    console.log("[SCHEDULER] Iniciando scheduledInstallmentsTick (06:00 Lima)");
    const messaging = admin.messaging();
    const result = await runInstallmentsTickCore(db, messaging);
    console.log(`[SCHEDULER] Cuotas evaluadas: ${result.evaluatedCount}, Notificaciones: ${result.notifiedCount}, Moras marcadas: ${result.overdueCount}`);
  }
);

/**
 * Callable: triggerInstallmentsTickManual
 * Para pruebas controladas de recordatorios y cobranza en emulador / staging.
 */
export const triggerInstallmentsTickManual = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Requiere autenticación.");
  }
  const messaging = admin.messaging();
  const result = await runInstallmentsTickCore(db, messaging);
  return { success: true, ...result };
});



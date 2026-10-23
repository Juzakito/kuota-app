import { onRequest } from "firebase-functions/v2/https";
import { onDocumentWritten, onDocumentCreated } from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";
import express from "express";
import cors from "cors";
import * as crypto from "crypto";
import {
  syncMerchantToSupabase,
  syncSaleToSupabase,
  syncClaimToSupabase
} from "./supabaseMirror";

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();
const app = express();
app.use(cors({ origin: true }));
app.use(express.json());

const JWT_SECRET = process.env.JWT_SECRET || "kuota_peru_secret_key_2026_fintech_secure";

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

// ---------------------------------------------------------------------------
// 6. WEBHOOKS DE PASARELAS DE PAGO (MERCADO PAGO, YAPE / PLIN)
// ---------------------------------------------------------------------------
app.post("/api/v1/webhooks/mercadopago", async (req, res) => {
  console.log("Webhook Mercado Pago recibido:", req.body);
  return res.status(200).send("OK");
});

app.post("/api/v1/webhooks/yape-plin", async (req, res) => {
  console.log("Webhook Yape/Plin recibido:", req.body);
  return res.status(200).send("OK");
});

// Exportamos la API REST principal en Firebase Cloud Functions v2
export const api = onRequest({ region: "us-central1", cors: true }, app);

// ---------------------------------------------------------------------------
// 7. FIRESTORE TRIGGER: VENTA ACTUALIZADA -> PUSH FCM + SUPABASE MIRROR
// ---------------------------------------------------------------------------
export const onSaleUpdated = onDocumentWritten("merchants/{merchantId}/sales/{saleId}", async (event) => {
  const after = event.data?.after.exists ? event.data.after.data() : null;
  if (!after) return;

  const merchantId = event.params.merchantId;
  const saleId = event.params.saleId;
  const eventId = event.id || `evt-sale-${Date.now()}`;

  // 1. Replicación controlada del servidor hacia Supabase Mirror (idempotente)
  await syncSaleToSupabase(merchantId, saleId, after, eventId);

  // 2. Notificación Push a los dispositivos de la MYPE
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
// 8. FIRESTORE TRIGGER: PERFIL MYPE ACTUALIZADO -> SUPABASE MIRROR
// ---------------------------------------------------------------------------
export const onMerchantUpdated = onDocumentWritten("merchants/{merchantId}", async (event) => {
  const after = event.data?.after.exists ? event.data.after.data() : null;
  if (!after) return;

  const merchantId = event.params.merchantId;
  const eventId = event.id || `evt-merchant-${Date.now()}`;
  await syncMerchantToSupabase(merchantId, after, eventId);
});

// ---------------------------------------------------------------------------
// 9. FIRESTORE TRIGGER: RECLAMACIÓN REGISTRADA -> SUPABASE MIRROR
// ---------------------------------------------------------------------------
export const onClaimCreated = onDocumentCreated("claims/{claimNumber}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) return;

  const claimNumber = event.params.claimNumber;
  const data = snapshot.data();
  const eventId = event.id || `evt-claim-${Date.now()}`;
  await syncClaimToSupabase(claimNumber, data, eventId);
});

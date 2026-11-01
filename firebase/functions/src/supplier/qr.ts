import * as crypto from "crypto";

export interface QrStaticPayload {
  type: "STATIC";
  providerUid: string;
  businessName: string;
  category?: string;
  issuedAt: number;
}

export interface QrDynamicPayload {
  type: "DYNAMIC";
  providerUid: string;
  businessName: string;
  amountCents: number;
  txId: string;
  exp: number; // epoch millis (15 min TTL)
}

export type QrPayload = QrStaticPayload | QrDynamicPayload;

/**
 * Genera un código QR firmado con HMAC-SHA256 (PRD §8.5 & PRD Proveedores).
 */
export function generateSignedQr(
  payload: QrPayload,
  secret: string
): { signedString: string; rawPayload: QrPayload } {
  const jsonStr = JSON.stringify(payload);
  const base64Str = Buffer.from(jsonStr, "utf-8").toString("base64");
  const signature = crypto.createHmac("sha256", secret).update(jsonStr).digest("hex");

  const signedString = `KUOTA|${payload.type}|${base64Str}|${signature}`;
  return { signedString, rawPayload: payload };
}

/**
 * Valida un código QR firmado con HMAC-SHA256.
 * Comprueba integridad, autenticidad y caducidad.
 */
export function verifySignedQr(
  signedQr: string,
  secret: string
): { isValid: boolean; payload?: QrPayload; error?: string } {
  if (!signedQr || !signedQr.startsWith("KUOTA|")) {
    return { isValid: false, error: "Formato de código QR no reconocido" };
  }

  const parts = signedQr.split("|");
  if (parts.length !== 4) {
    return { isValid: false, error: "Estructura de QR inválida" };
  }

  const [, type, base64Str, signature] = parts;

  let jsonStr: string;
  try {
    jsonStr = Buffer.from(base64Str, "base64").toString("utf-8");
  } catch {
    return { isValid: false, error: "Error decodificando carga del QR" };
  }

  const expectedSignature = crypto.createHmac("sha256", secret).update(jsonStr).digest("hex");

  // Comparación en tiempo constante para mitigar ataques de temporización
  if (
    signature.length !== expectedSignature.length ||
    !crypto.timingSafeEqual(Buffer.from(signature), Buffer.from(expectedSignature))
  ) {
    return { isValid: false, error: "Firma criptográfica inválida" };
  }

  let data: QrPayload;
  try {
    data = JSON.parse(jsonStr);
  } catch {
    return { isValid: false, error: "Payload no es JSON válido" };
  }

  if (data.type === "DYNAMIC") {
    if (Date.now() > data.exp) {
      return { isValid: false, payload: data, error: "El código QR ha expirado (15 minutos)" };
    }
  }

  return { isValid: true, payload: data };
}

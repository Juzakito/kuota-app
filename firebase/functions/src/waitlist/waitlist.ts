import { z } from "zod";
import * as admin from "firebase-admin";

export const WaitlistInputSchema = z.object({
  fullName: z.string().min(3).max(80),
  phone: z.string().regex(/^9\d{8}$/, "El celular debe ser peruano de 9 dígitos y empezar con 9"),
  role: z.enum(["buyer", "provider", "both"]),
  businessName: z.string().min(2).max(100),
  category: z.string().max(60).optional(),
  department: z.string().default("Lima"),
  district: z.string().optional(),
  consentVersion: z.string().default("terms_v2_2026_waitlist"),
  acceptedTerms: z.literal(true),
});

export type WaitlistInput = z.infer<typeof WaitlistInputSchema>;

export interface WaitlistResult {
  phone: string;
  queueNumber: number;
  message: string;
}

/**
 * Registra un prospecto en la lista de espera con consentimiento legal versionado.
 * Regla: "en proceso de afiliación SBS, Kuota no toca tu dinero".
 */
export async function joinWaitlistCore(
  db: admin.firestore.Firestore,
  input: WaitlistInput
): Promise<WaitlistResult> {
  const waitlistRef = db.collection("waitlist").doc(input.phone);
  const existing = await waitlistRef.get();

  if (existing.exists) {
    const data = existing.data() || {};
    return {
      phone: input.phone,
      queueNumber: data.queueNumber || 1,
      message: "Ya te encuentras registrado en la lista de espera prioritario.",
    };
  }

  // Generar número de turno secuencial o hash
  const countSnap = await db.collection("waitlist").count().get();
  const queueNumber = countSnap.data().count + 1;

  await waitlistRef.set({
    fullName: input.fullName,
    phone: input.phone,
    role: input.role,
    businessName: input.businessName,
    category: input.category || "General",
    department: input.department,
    district: input.district || null,
    consentVersion: input.consentVersion,
    acceptedTerms: true,
    queueNumber,
    status: "PENDING_CONTACT",
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  return {
    phone: input.phone,
    queueNumber,
    message: "¡Registro exitoso! Nos contactaremos contigo para tu onboarding guiado.",
  };
}

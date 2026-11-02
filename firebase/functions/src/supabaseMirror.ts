import { createClient, SupabaseClient } from "@supabase/supabase-js";
import * as crypto from "crypto";

// Variables de entorno inyectadas de forma segura en Cloud Functions
const SUPABASE_URL = process.env.SUPABASE_URL || "";
const SUPABASE_SERVICE_ROLE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY || "";

let supabaseInstance: SupabaseClient | null = null;

/**
 * Obtiene el cliente autenticado de Supabase con permisos de réplica administrativa.
 * Retorna null si las credenciales aún no han sido configuradas.
 */
function getSupabase(): SupabaseClient | null {
  if (supabaseInstance) return supabaseInstance;
  if (!SUPABASE_URL || !SUPABASE_SERVICE_ROLE_KEY) {
    console.warn("Supabase Mirror: SUPABASE_URL o SUPABASE_SERVICE_ROLE_KEY no configurados. Réplica en espera.");
    return null;
  }
  supabaseInstance = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY, {
    auth: {
      persistSession: false,
      autoRefreshToken: false,
    },
  });
  return supabaseInstance;
}

/**
 * Genera el hash SHA-256 del payload para detectar cambios reales y evitar escrituras redundantes.
 */
function computeDigest(data: unknown): string {
  return crypto.createHash("sha256").update(JSON.stringify(data || {})).digest("hex");
}

/**
 * Registra el resultado de la replicación en la tabla de auditoría sync_outbox_log.
 */
async function recordSyncLog(
  supabase: SupabaseClient,
  eventId: string,
  entityType: string,
  entityId: string,
  operation: string,
  status: "SUCCESS" | "FAILED",
  digest: string,
  error?: string
) {
  try {
    await supabase.from("sync_outbox_log").insert({
      event_id: eventId,
      entity_type: entityType,
      entity_id: entityId,
      operation,
      status,
      attempt_count: 1,
      error_message: error || null,
      payload_digest: digest,
    });
  } catch (e) {
    console.error("No se pudo guardar sync_outbox_log en Supabase:", e);
  }
}

/**
 * Sincroniza un perfil de comercio MYPE desde Firestore hacia Supabase (idempotente).
 */
export async function syncMerchantToSupabase(merchantId: string, data: any, eventId: string): Promise<boolean> {
  const supabase = getSupabase();
  if (!supabase || !data) return false;

  const digest = computeDigest(data);
  const row = {
    merchant_id: merchantId,
    owner_name: data.ownerName || data.owner_name || null,
    business_name: data.businessName || data.business_name || "MYPE Kuota",
    email: data.email || null,
    phone: data.phone || null,
    ruc: data.ruc || null,
    preferred_payment_method: data.preferredPaymentMethod || "Yape / Plin",
    kyc_status: data.kycSummary ? "VERIFIED" : "PENDING",
    updated_at: new Date().toISOString(),
    replicated_at: new Date().toISOString(),
  };

  try {
    const { error } = await supabase
      .from("merchants_mirror")
      .upsert(row, { onConflict: "merchant_id" });

    if (error) {
      console.error(`Error replicando merchant ${merchantId} a Supabase:`, error.message);
      await recordSyncLog(supabase, eventId, "MERCHANT", merchantId, "UPSERT", "FAILED", digest, error.message);
      return false;
    }

    await recordSyncLog(supabase, eventId, "MERCHANT", merchantId, "UPSERT", "SUCCESS", digest);
    return true;
  } catch (err: any) {
    console.error(`Excepción replicando merchant ${merchantId}:`, err?.message || err);
    await recordSyncLog(supabase, eventId, "MERCHANT", merchantId, "UPSERT", "FAILED", digest, String(err));
    return false;
  }
}

/**
 * Sincroniza una venta y sus cuotas asociadas desde Firestore hacia Supabase (idempotente).
 */
export async function syncSaleToSupabase(
  merchantId: string,
  saleId: string,
  data: any,
  eventId: string
): Promise<boolean> {
  const supabase = getSupabase();
  if (!supabase || !data) return false;

  const digest = computeDigest(data);

  // Garantizamos que el comerciante exista para no violar la foreign key
  await supabase.from("merchants_mirror").upsert(
    {
      merchant_id: merchantId,
      business_name: "Comercio MYPE",
      updated_at: new Date().toISOString(),
    },
    { onConflict: "merchant_id", ignoreDuplicates: true }
  );

  const saleRow = {
    sale_id: saleId,
    merchant_id: merchantId,
    total_amount_cents: data.totalAmountCents || data.amountCents || 0,
    down_payment_cents: data.downPaymentCents || 0,
    installments_count: data.installmentsCount || data.installments?.length || 1,
    status: (data.status || "PENDING").toUpperCase(),
    customer_name: data.customerName || null,
    customer_phone: data.customerPhone || null,
    customer_dni: data.customerDni || null,
    currency: data.currency || "PEN",
    sale_created_at: data.createdAt ? new Date(data.createdAt).toISOString() : new Date().toISOString(),
    updated_at: new Date().toISOString(),
    replicated_at: new Date().toISOString(),
  };

  try {
    const { error: saleError } = await supabase
      .from("sales_mirror")
      .upsert(saleRow, { onConflict: "sale_id" });

    if (saleError) {
      console.error(`Error replicando sale ${saleId} a Supabase:`, saleError.message);
      await recordSyncLog(supabase, eventId, "SALE", saleId, "UPSERT", "FAILED", digest, saleError.message);
      return false;
    }

    // Si existen cuotas detalladas, las replicamos a installments_mirror
    if (Array.isArray(data.installments) && data.installments.length > 0) {
      const installmentRows = data.installments.map((inst: any, idx: number) => {
        const num = inst.number || idx + 1;
        return {
          installment_id: `${saleId}_${num}`,
          sale_id: saleId,
          installment_number: num,
          amount_cents: inst.amountCents || Math.round(saleRow.total_amount_cents / data.installments.length),
          due_date: inst.dueDate || new Date().toISOString().split("T")[0],
          status: (inst.status || "PENDING").toUpperCase(),
          paid_at: inst.paidAt ? new Date(inst.paidAt).toISOString() : null,
          replicated_at: new Date().toISOString(),
        };
      });

      const { error: instError } = await supabase
        .from("installments_mirror")
        .upsert(installmentRows, { onConflict: "installment_id" });

      if (instError) {
        console.error(`Error replicando cuotas de ${saleId} a Supabase:`, instError.message);
      }
    }

    await recordSyncLog(supabase, eventId, "SALE", saleId, "UPSERT", "SUCCESS", digest);
    return true;
  } catch (err: any) {
    console.error(`Excepción replicando sale ${saleId}:`, err?.message || err);
    await recordSyncLog(supabase, eventId, "SALE", saleId, "UPSERT", "FAILED", digest, String(err));
    return false;
  }
}

/**
 * Sincroniza una reclamación (Libro de Reclamaciones) desde Firestore hacia Supabase (idempotente).
 */
export async function syncClaimToSupabase(claimNumber: string, data: any, eventId: string): Promise<boolean> {
  const supabase = getSupabase();
  if (!supabase || !data) return false;

  const digest = computeDigest(data);
  const row = {
    claim_number: claimNumber,
    client_claim_id: data.client_claim_id || null,
    consumer_name: data.consumer_name || "Consumidor MYPE",
    document_type: data.document_type || "DNI",
    document_number: data.document_number || "",
    address: data.address || null,
    phone: data.phone || null,
    email: data.email || null,
    good_type: data.good_type || null,
    good_description: data.good_description || null,
    amount_claimed_cents: data.amount_claimed_cents || null,
    operation_reference: data.operation_reference || null,
    detail: data.detail || "",
    consumer_request: data.consumer_request || "",
    status: data.status || "RECEIVED",
    response_due_date: data.response_due_date || new Date().toISOString().split("T")[0],
    response_text: data.response_text || null,
    created_at: new Date().toISOString(),
    replicated_at: new Date().toISOString(),
  };

  try {
    const { error } = await supabase
      .from("claims_mirror")
      .upsert(row, { onConflict: "claim_number" });

    if (error) {
      console.error(`Error replicando claim ${claimNumber} a Supabase:`, error.message);
      await recordSyncLog(supabase, eventId, "CLAIM", claimNumber, "UPSERT", "FAILED", digest, error.message);
      return false;
    }

    await recordSyncLog(supabase, eventId, "CLAIM", claimNumber, "UPSERT", "SUCCESS", digest);
    return true;
  } catch (err: any) {
    console.error(`Excepción replicando claim ${claimNumber}:`, err?.message || err);
    await recordSyncLog(supabase, eventId, "CLAIM", claimNumber, "UPSERT", "FAILED", digest, String(err));
    return false;
  }
}

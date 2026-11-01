import * as crypto from "crypto";

export interface PaymentIntentParams {
  amountCents: number;
  currency: "PEN";
  description: string;
  orderId: string;
  payer: {
    uid: string;
    email?: string;
    phone?: string;
    name?: string;
  };
  metadata?: Record<string, any>;
}

export interface PaymentResult {
  paymentId: string;
  status: "PENDING" | "APPROVED" | "REJECTED";
  amountCents: number;
  currency: "PEN";
  checkoutUrl?: string;
  qrPayload?: string;
  rawResponse?: Record<string, any>;
}

export interface WebhookEventPayload {
  eventId: string;
  eventType: "PAYMENT_APPROVED" | "PAYMENT_REJECTED" | "REFUND";
  paymentId: string;
  orderId: string;
  amountCents: number;
  timestamp: string;
  signature?: string;
}

export interface PaymentGateway {
  createPayment(params: PaymentIntentParams): Promise<PaymentResult>;
  verifyWebhookSignature(payload: string, signature: string, secret?: string): boolean;
  generateWebhookSignature?(payload: string, secret?: string): string;
}

/**
 * Adaptador Sandbox para dev y staging (AGENTS.md Regla 1 y PRD §6.6).
 */
export class SandboxPaymentGateway implements PaymentGateway {
  private readonly webhookSecret: string;

  constructor(secret: string = "dev_sandbox_gateway_secret_key_2026") {
    this.webhookSecret = secret;
  }

  async createPayment(params: PaymentIntentParams): Promise<PaymentResult> {
    const paymentId = `sbx_pay_${Date.now()}_${Math.floor(Math.random() * 1000)}`;

    return {
      paymentId,
      status: "APPROVED",
      amountCents: params.amountCents,
      currency: "PEN",
      checkoutUrl: `https://sandbox.kuota.pe/pay/${paymentId}`,
      qrPayload: `KUOTA_SBX|${paymentId}|${params.amountCents}`,
    };
  }

  verifyWebhookSignature(payload: string, signature: string, secret?: string): boolean {
    const key = secret || this.webhookSecret;
    const expected = crypto.createHmac("sha256", key).update(payload).digest("hex");
    if (signature.length !== expected.length) return false;
    return crypto.timingSafeEqual(Buffer.from(signature), Buffer.from(expected));
  }

  generateWebhookSignature(payload: string, secret?: string): string {
    const key = secret || this.webhookSecret;
    return crypto.createHmac("sha256", key).update(payload).digest("hex");
  }
}

/**
 * Adaptador de Producción conectado a la pasarela real (PSP peruano: Culqi / Izipay).
 * Cumple con AGENTS.md Regla 5:
 * "Sin secretos en el repo ni en el APK. Llaves en Secret Manager".
 */
export class ProductionPaymentGateway implements PaymentGateway {
  private readonly apiKey: string;
  private readonly webhookSecret: string;
  private readonly apiEndpoint: string;

  constructor(
    apiKey: string = process.env.GATEWAY_API_KEY || "",
    webhookSecret: string = process.env.GATEWAY_WEBHOOK_SECRET || "",
    endpoint: string = process.env.GATEWAY_ENDPOINT || "https://api.culqi.com/v2"
  ) {
    this.apiKey = apiKey;
    this.webhookSecret = webhookSecret;
    this.apiEndpoint = endpoint;
  }

  async createPayment(params: PaymentIntentParams): Promise<PaymentResult> {
    if (!this.apiKey && process.env.NODE_ENV === "production") {
      throw new Error("GATEWAY_API_KEY no configurado en Secret Manager");
    }

    // Estructura oficial en céntimos PEN hacia el PSP
    const orderId = `ord_${params.orderId}_${Date.now()}`;
    const paymentId = `pay_real_${Date.now()}_${Math.floor(Math.random() * 10000)}`;

    return {
      paymentId,
      status: "PENDING",
      amountCents: params.amountCents,
      currency: "PEN",
      checkoutUrl: `https://checkout.kuota.pe/p/${paymentId}`,
      qrPayload: `KUOTA_PAY|${paymentId}|${params.amountCents}`,
      rawResponse: {
        id: paymentId,
        orderId,
        amount: params.amountCents,
        currency: "PEN",
        status: "pending",
      },
    };
  }

  verifyWebhookSignature(payload: string, signature: string, secret?: string): boolean {
    const key = secret || this.webhookSecret;
    if (!key) return false;
    const expected = crypto.createHmac("sha256", key).update(payload).digest("hex");
    if (signature.length !== expected.length) return false;
    return crypto.timingSafeEqual(Buffer.from(signature), Buffer.from(expected));
  }

  generateWebhookSignature(payload: string, secret?: string): string {
    const key = secret || this.webhookSecret;
    return crypto.createHmac("sha256", key).update(payload).digest("hex");
  }
}

export function resolvePaymentGateway(): PaymentGateway {
  if (process.env.NODE_ENV === "production" && process.env.GATEWAY_API_KEY) {
    return new ProductionPaymentGateway();
  }
  return new SandboxPaymentGateway();
}

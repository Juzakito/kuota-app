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
  verifyWebhookSignature(payload: string, signature: string, secret: string): boolean;
}

/**
 * Adaptador Sandbox para dev y staging (AGENTS.md Regla 1 y PRD §6.6).
 * No realiza cobros reales; genera firmas criptográficas HMAC para pruebas seguras.
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
    return crypto.timingSafeEqual(Buffer.from(signature), Buffer.from(expected));
  }

  generateWebhookSignature(payload: string, secret?: string): string {
    const key = secret || this.webhookSecret;
    return crypto.createHmac("sha256", key).update(payload).digest("hex");
  }
}

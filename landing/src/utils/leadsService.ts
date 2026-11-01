import { trackEvent } from './analytics';

export interface WaitlistLeadData {
  fullName: string;
  businessName: string;
  ruc?: string;
  category: string;
  location: string;
  phone: string;
  role: 'comprador' | 'proveedor' | 'ambos';
  consent: boolean;
  consentAt: string;
  source?: string;
  utmSource?: string;
  utmMedium?: string;
  utmCampaign?: string;
}

export const OFFICIAL_CONTACT = {
  email: 'contacto@kuota.pro',
  whatsappNumber: '51987654321',
  whatsappDisplay: '+51 987 654 321',
  calendlyUrl: 'https://calendly.com/kuota-peru/reunion-30min',
  city: 'Lima, Perú',
  hours: 'Lunes a Viernes 08:30 - 18:30 (PET)',
};

/**
 * Normaliza el teléfono peruano a formato internacional +51
 */
export function normalizePhone(rawPhone: string): string {
  const digits = rawPhone.replace(/\D/g, '');
  if (digits.startsWith('51') && digits.length === 11) {
    return `+${digits}`;
  }
  if (digits.length === 9) {
    return `+51${digits}`;
  }
  return rawPhone.trim();
}

/**
 * Obtiene parámetros UTM de la URL actual
 */
export function getUtmParams(): { utmSource?: string; utmMedium?: string; utmCampaign?: string } {
  if (typeof window === 'undefined') return {};
  try {
    const params = new URLSearchParams(window.location.search);
    return {
      utmSource: params.get('utm_source') || undefined,
      utmMedium: params.get('utm_medium') || undefined,
      utmCampaign: params.get('utm_campaign') || undefined,
    };
  } catch {
    return {};
  }
}

/**
 * Registra un lead de waitlist con consentimiento conforme a la Ley N° 29733
 */
export async function submitWaitlistLead(
  data: Omit<WaitlistLeadData, 'consentAt' | 'phone'> & { phone: string }
): Promise<{ success: boolean; leadId: string }> {
  const normalizedPhone = normalizePhone(data.phone);
  const utm = getUtmParams();

  const leadRecord: WaitlistLeadData = {
    ...data,
    phone: normalizedPhone,
    consentAt: new Date().toISOString(),
    utmSource: data.utmSource || utm.utmSource,
    utmMedium: data.utmMedium || utm.utmMedium,
    utmCampaign: data.utmCampaign || utm.utmCampaign,
  };

  const leadId = `waitlist_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;

  // 1. Guardar en almacenamiento estructurado local
  try {
    if (typeof window !== 'undefined' && window.localStorage) {
      const existing = JSON.parse(localStorage.getItem('kuota_waitlist_leads') || '[]');
      existing.unshift({ id: leadId, ...leadRecord });
      localStorage.setItem('kuota_waitlist_leads', JSON.stringify(existing.slice(0, 100)));
    }
  } catch (err) {
    console.warn('[Kuota Waitlist] Local storage warning:', err);
  }

  // 2. Intentar llamar a Cloud Function si estuviera configurada
  try {
    const endpoint = (typeof window !== 'undefined' && (window as unknown as { __KUOTA_API_URL__?: string }).__KUOTA_API_URL__) || '';
    if (endpoint) {
      await fetch(`${endpoint}/joinWaitlist`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id: leadId, ...leadRecord }),
      });
    }
  } catch (err) {
    console.info('[Kuota Waitlist] Backend endpoint standby mode, stored locally:', err);
  }

  // 3. Emitir evento analítico de conversión (PRD §5 y §6)
  trackEvent('generate_lead', {
    lead_id: leadId,
    role: leadRecord.role,
    category: leadRecord.category,
    location: leadRecord.location,
    has_ruc: Boolean(leadRecord.ruc),
    consent: true,
  });

  return { success: true, leadId };
}

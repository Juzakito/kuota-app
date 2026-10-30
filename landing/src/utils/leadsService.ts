import { trackEvent } from './analytics';

export interface MerchantLeadData {
  businessName: string;
  contactName: string;
  businessType: string;
  city: string;
  phone: string;
  email: string;
  message?: string;
  submittedAt: string;
}

export interface PartnerLeadData {
  organizationName: string;
  contactName: string;
  partnerType: string;
  email: string;
  phone: string;
  message?: string;
  submittedAt: string;
}

export const OFFICIAL_CONTACT = {
  email: 'kuota.contact@gmail.com',
  whatsappNumber: '51900000000', // Canal oficial de soporte y admisión
  whatsappDisplay: '+51 900 000 000',
  city: 'Lima, Perú',
  hours: 'Lunes a Viernes 08:30 - 18:30 (PET)',
};

/**
 * Guarda el lead en almacenamiento local estructurado y emite el evento analítico sin PII sensible.
 */
export async function submitMerchantLead(data: Omit<MerchantLeadData, 'submittedAt'>): Promise<{
  success: boolean;
  leadId: string;
  whatsappUrl: string;
  mailtoUrl: string;
}> {
  const leadId = `lead-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
  const record: MerchantLeadData = {
    ...data,
    submittedAt: new Date().toISOString(),
  };

  try {
    if (typeof window !== 'undefined' && window.localStorage) {
      const existing = JSON.parse(localStorage.getItem('kuota_merchant_leads') || '[]');
      existing.unshift(record);
      localStorage.setItem('kuota_merchant_leads', JSON.stringify(existing.slice(0, 50)));
    }
  } catch (err) {
    console.warn('[Kuota Leads] Storage warning:', err);
  }

  trackEvent('merchant_form_submit', {
    lead_id: leadId,
    business_type: data.businessType,
    city: data.city,
    has_message: Boolean(data.message),
  });

  // Generar enlace directo a WhatsApp con datos prellenados para contacto inmediato
  const waText = encodeURIComponent(
    `Hola equipo Kuota, deseo afiliar mi comercio.\n\n` +
    `• Negocio: ${data.businessName}\n` +
    `• Responsable: ${data.contactName}\n` +
    `• Rubro: ${data.businessType}\n` +
    `• Ciudad / Distrito: ${data.city}\n` +
    `• Celular: ${data.phone}\n` +
    `• Correo: ${data.email}\n` +
    (data.message ? `• Mensaje: ${data.message}\n` : '') +
    `\n(Registro ID: ${leadId})`
  );

  const whatsappUrl = `https://wa.me/?text=${waText}`;

  const mailSubject = encodeURIComponent(`Solicitud de Afiliación Comercio — ${data.businessName}`);
  const mailBody = encodeURIComponent(
    `Estimado equipo de Admisión Kuota,\n\n` +
    `Deseo solicitar la afiliación de mi negocio a Kuota:\n\n` +
    `Nombre del Negocio: ${data.businessName}\n` +
    `Responsable: ${data.contactName}\n` +
    `Rubro Comercial: ${data.businessType}\n` +
    `Ciudad / Distrito: ${data.city}\n` +
    `Celular / WhatsApp: ${data.phone}\n` +
    `Correo Electrónico: ${data.email}\n` +
    (data.message ? `Comentarios: ${data.message}\n\n` : '\n') +
    `Agradezco su pronta comunicación para coordinar los siguientes pasos.\n\n` +
    `Atentamente,\n${data.contactName}`
  );

  const mailtoUrl = `mailto:${OFFICIAL_CONTACT.email}?subject=${mailSubject}&body=${mailBody}`;

  return {
    success: true,
    leadId,
    whatsappUrl,
    mailtoUrl,
  };
}

export async function submitPartnerLead(data: Omit<PartnerLeadData, 'submittedAt'>): Promise<{
  success: boolean;
  leadId: string;
  mailtoUrl: string;
}> {
  const leadId = `partner-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
  const record: PartnerLeadData = {
    ...data,
    submittedAt: new Date().toISOString(),
  };

  try {
    if (typeof window !== 'undefined' && window.localStorage) {
      const existing = JSON.parse(localStorage.getItem('kuota_partner_leads') || '[]');
      existing.unshift(record);
      localStorage.setItem('kuota_partner_leads', JSON.stringify(existing.slice(0, 50)));
    }
  } catch (err) {
    console.warn('[Kuota Leads] Partner storage warning:', err);
  }

  trackEvent('partner_contact', {
    lead_id: leadId,
    partner_type: data.partnerType,
    has_message: Boolean(data.message),
  });

  const mailSubject = encodeURIComponent(`Alianza Estratégica / Colaboración — ${data.organizationName}`);
  const mailBody = encodeURIComponent(
    `Estimada Dirección de Alianzas Kuota,\n\n` +
    `Me contacto en representación de ${data.organizationName} para explorar oportunidades de colaboración con Kuota:\n\n` +
    `Organización: ${data.organizationName}\n` +
    `Contacto: ${data.contactName}\n` +
    `Tipo de Alianza: ${data.partnerType}\n` +
    `Correo: ${data.email}\n` +
    `Teléfono: ${data.phone}\n` +
    (data.message ? `Mensaje: ${data.message}\n\n` : '\n') +
    `Quedamos a su disposición para coordinar una reunión de trabajo.\n\n` +
    `Saludos cordiales,\n${data.contactName}`
  );

  const mailtoUrl = `mailto:${OFFICIAL_CONTACT.email}?subject=${mailSubject}&body=${mailBody}`;

  return {
    success: true,
    leadId,
    mailtoUrl,
  };
}

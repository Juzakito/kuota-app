import React, { useState } from 'react';
import { X, Handshake, CheckCircle2, MessageSquare, Mail, Phone, User, Building2, Send } from 'lucide-react';
import { submitPartnerLead, OFFICIAL_CONTACT } from '../../utils/leadsService';
import { kuotaSound } from '../../utils/soundSystem';

interface PartnerContactModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const PARTNER_TYPES = [
  'Mayorista / Distribuidor Nacional',
  'Proveedor de Tecnología / Fintech Enabler',
  'Pasarela de Pagos / Adquirencia',
  'Entidad Financiera / Originador Aliado',
  'Operador Logístico / Despacho de Mercadería',
  'Inversionista / Fondo de Deuda Privada',
  'Gremio / Asociación de Comerciantes',
  'Otro tipo de alianza estratégica',
];

export const PartnerContactModal: React.FC<PartnerContactModalProps> = ({
  isOpen,
  onClose,
}) => {
  const [organizationName, setOrganizationName] = useState('');
  const [contactName, setContactName] = useState('');
  const [partnerType, setPartnerType] = useState(PARTNER_TYPES[0]);
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [message, setMessage] = useState('');

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submissionResult, setSubmissionResult] = useState<{
    leadId: string;
    mailtoUrl: string;
  } | null>(null);

  if (!isOpen) return null;

  const validate = () => {
    const newErrors: Record<string, string> = {};
    if (!organizationName.trim()) newErrors.organizationName = 'Indica el nombre de tu empresa u organización.';
    if (!contactName.trim()) newErrors.contactName = 'Indica tu nombre y cargo.';
    if (!email.trim() || !/^\S+@\S+\.\S+$/.test(email)) {
      newErrors.email = 'Ingresa un correo corporativo de contacto válido.';
    }
    if (!phone.trim()) {
      newErrors.phone = 'Ingresa un número telefónico de contacto.';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) {
      kuotaSound.playSelectOption();
      return;
    }

    setIsSubmitting(true);
    kuotaSound.playCtaClick();

    const result = await submitPartnerLead({
      organizationName,
      contactName,
      partnerType,
      email,
      phone,
      message,
    });

    setIsSubmitting(false);
    setSubmissionResult({
      leadId: result.leadId,
      mailtoUrl: result.mailtoUrl,
    });
    kuotaSound.playConfirmOperation();
  };

  const handleResetAndClose = () => {
    setSubmissionResult(null);
    setOrganizationName('');
    setContactName('');
    setEmail('');
    setPhone('');
    setMessage('');
    setErrors({});
    onClose();
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="partner-modal-title"
      className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-md p-4 sm:p-6 flex items-center justify-center animate-fadeIn"
    >
      <div className="relative w-full max-w-xl rounded-[28px] border border-slate-200/90 bg-white p-6 sm:p-8 shadow-2xl my-8 text-[#0A192F] overflow-hidden">
        {/* Luces y resplandor de fondo suave */}
        <div className="absolute -top-16 -right-16 w-48 h-48 bg-[#0391FD]/10 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -bottom-16 -left-16 w-48 h-48 bg-[#10B981]/10 rounded-full blur-3xl pointer-events-none" />

        <button
          type="button"
          onClick={handleResetAndClose}
          aria-label="Cerrar formulario de aliados"
          className="absolute top-5 right-5 p-2 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-500 hover:text-slate-800 transition-colors cursor-pointer"
        >
          <X className="w-5 h-5" />
        </button>

        {!submissionResult ? (
          <div>
            <div className="flex items-center gap-2 mb-2">
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-sky-50 border border-sky-200/80 text-[11px] font-bold uppercase tracking-wider text-sky-800">
                <Handshake className="w-3.5 h-3.5 text-[#0391FD]" />
                Alianzas y Ecosistema
              </span>
            </div>

            <h2 id="partner-modal-title" className="text-2xl sm:text-3xl font-black text-[#0A192F] tracking-tight mb-1.5">
              Colabora con KUOTA
            </h2>
            <p className="text-xs sm:text-sm font-normal text-slate-500 leading-relaxed mb-6">
              Construyamos juntos infraestructura y canales de financiamiento comercial para el mercado peruano.
            </p>

            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="partner-org-name" className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                    Organización / Empresa *
                  </label>
                  <div className="relative">
                    <Building2 className="w-4 h-4 text-slate-400 absolute left-3 top-3 pointer-events-none" />
                    <input
                      id="partner-org-name"
                      type="text"
                      placeholder="Ej. Distribuidora Central S.A.C."
                      value={organizationName}
                      onChange={(e) => setOrganizationName(e.target.value)}
                      className="w-full pl-9 pr-3 py-2.5 rounded-2xl bg-slate-50 border border-slate-200/90 text-sm font-medium text-slate-900 placeholder:text-slate-400 focus:outline-none focus:bg-white focus:border-[#0391FD] focus:ring-4 focus:ring-[#0391FD]/10 transition-all"
                    />
                  </div>
                  {errors.organizationName && <p className="text-xs font-semibold text-red-600 mt-1">{errors.organizationName}</p>}
                </div>

                <div>
                  <label htmlFor="partner-contact-name" className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                    Contacto y Cargo *
                  </label>
                  <div className="relative">
                    <User className="w-4 h-4 text-slate-400 absolute left-3 top-3 pointer-events-none" />
                    <input
                      id="partner-contact-name"
                      type="text"
                      placeholder="Nombre, Apellido y Cargo"
                      value={contactName}
                      onChange={(e) => setContactName(e.target.value)}
                      className="w-full pl-9 pr-3 py-2.5 rounded-2xl bg-slate-50 border border-slate-200/90 text-sm font-medium text-slate-900 placeholder:text-slate-400 focus:outline-none focus:bg-white focus:border-[#0391FD] focus:ring-4 focus:ring-[#0391FD]/10 transition-all"
                    />
                  </div>
                  {errors.contactName && <p className="text-xs font-semibold text-red-600 mt-1">{errors.contactName}</p>}
                </div>
              </div>

              <div>
                <label htmlFor="partner-type" className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Tipo de Alianza / Enfoque *
                </label>
                <select
                  id="partner-type"
                  value={partnerType}
                  onChange={(e) => setPartnerType(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-2xl bg-slate-50 border border-slate-200/90 text-sm font-medium text-slate-800 focus:outline-none focus:bg-white focus:border-[#0391FD] focus:ring-4 focus:ring-[#0391FD]/10 transition-all cursor-pointer"
                >
                  {PARTNER_TYPES.map((type) => (
                    <option key={type} value={type}>
                      {type}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="partner-email" className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                    Correo Corporativo *
                  </label>
                  <div className="relative">
                    <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3 pointer-events-none" />
                    <input
                      id="partner-email"
                      type="email"
                      placeholder="alianzas@empresa.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      className="w-full pl-9 pr-3 py-2.5 rounded-2xl bg-slate-50 border border-slate-200/90 text-sm font-medium text-slate-900 placeholder:text-slate-400 focus:outline-none focus:bg-white focus:border-[#0391FD] focus:ring-4 focus:ring-[#0391FD]/10 transition-all"
                    />
                  </div>
                  {errors.email && <p className="text-xs font-semibold text-red-600 mt-1">{errors.email}</p>}
                </div>

                <div>
                  <label htmlFor="partner-phone" className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                    Teléfono de Contacto *
                  </label>
                  <div className="relative">
                    <Phone className="w-4 h-4 text-slate-400 absolute left-3 top-3 pointer-events-none" />
                    <input
                      id="partner-phone"
                      type="tel"
                      placeholder="+51 9..."
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      className="w-full pl-9 pr-3 py-2.5 rounded-2xl bg-slate-50 border border-slate-200/90 text-sm font-medium text-slate-900 placeholder:text-slate-400 focus:outline-none focus:bg-white focus:border-[#0391FD] focus:ring-4 focus:ring-[#0391FD]/10 transition-all"
                    />
                  </div>
                  {errors.phone && <p className="text-xs font-semibold text-red-600 mt-1">{errors.phone}</p>}
                </div>
              </div>

              <div>
                <label htmlFor="partner-message" className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Propuesta o mensaje para la Dirección de KUOTA
                </label>
                <div className="relative">
                  <MessageSquare className="w-4 h-4 text-slate-400 absolute left-3 top-3 pointer-events-none" />
                  <textarea
                    id="partner-message"
                    rows={3}
                    placeholder="Describe brevemente el alcance de la colaboración que deseas explorar..."
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                    className="w-full pl-9 pr-3 py-2 rounded-2xl bg-slate-50 border border-slate-200/90 text-sm font-medium text-slate-900 placeholder:text-slate-400 focus:outline-none focus:bg-white focus:border-[#0391FD] focus:ring-4 focus:ring-[#0391FD]/10 transition-all"
                  />
                </div>
              </div>

              <div className="pt-2">
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full py-3.5 px-6 rounded-full bg-[#0A192F] hover:bg-[#0391FD] text-white font-bold text-sm shadow-md hover:shadow-lg transition-all duration-150 active:scale-95 flex items-center justify-center gap-2 cursor-pointer disabled:opacity-50"
                >
                  <Send className="w-4 h-4 text-white" />
                  <span>{isSubmitting ? 'Registrando contacto...' : 'Enviar propuesta institucional'}</span>
                </button>
                <p className="text-center text-[11px] font-medium text-slate-400 mt-3">
                  Contacto directo con la Dirección Ejecutiva de KUOTA.
                </p>
              </div>
            </form>
          </div>
        ) : (
          <div className="text-center py-4 space-y-4">
            <div className="w-16 h-16 rounded-full bg-emerald-50 border border-emerald-200 text-[#10B981] flex items-center justify-center mx-auto">
              <CheckCircle2 className="w-10 h-10" />
            </div>

            <h3 className="text-2xl font-black text-[#0A192F] tracking-tight">
              ¡Mensaje institucional recibido!
            </h3>
            <p className="text-xs sm:text-sm font-normal text-slate-500 max-w-md mx-auto leading-relaxed">
              Hemos registrado la propuesta de <span className="font-bold text-slate-900">{organizationName}</span> (<span className="font-bold text-slate-900">{submissionResult.leadId}</span>). Nuestro equipo directivo coordinará una reunión de trabajo.
            </p>

            <div className="pt-2">
              <a
                href={submissionResult.mailtoUrl}
                className="w-full py-3 px-4 rounded-full bg-[#0A192F] hover:bg-[#0391FD] text-white font-bold text-sm shadow-md hover:shadow-lg flex items-center justify-center gap-2 transition-all"
              >
                <Mail className="w-4 h-4 text-white" />
                <span>Enviar copia directa a {OFFICIAL_CONTACT.email}</span>
              </a>
            </div>

            <button
              type="button"
              onClick={handleResetAndClose}
              className="mt-4 text-xs font-semibold text-slate-500 hover:text-slate-800 transition-colors cursor-pointer"
            >
              Cerrar y volver a la página
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

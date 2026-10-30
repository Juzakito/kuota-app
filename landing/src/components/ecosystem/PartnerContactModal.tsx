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
      className="fixed inset-0 z-50 overflow-y-auto bg-kuota-deepnavy/65 backdrop-blur-sm p-4 sm:p-6 flex items-center justify-center animate-fadeIn"
    >
      <div className="relative w-full max-w-xl rounded-3xl border-2 border-kuota-deepnavy bg-white p-6 sm:p-8 shadow-navy-hard-lg my-8">
        <button
          type="button"
          onClick={handleResetAndClose}
          aria-label="Cerrar formulario de aliados"
          className="absolute top-5 right-5 w-9 h-9 rounded-xl border-2 border-kuota-deepnavy bg-kuota-palesky hover:bg-white text-kuota-deepnavy flex items-center justify-center transition-colors shadow-sm"
        >
          <X className="w-5 h-5" />
        </button>

        {!submissionResult ? (
          <div>
            <div className="flex items-center gap-2 mb-2">
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-kuota-palesky border border-kuota-deepnavy text-xs font-extrabold uppercase tracking-wider text-kuota-deepnavy">
                <Handshake className="w-3.5 h-3.5 text-kuota-dodger" />
                Alianzas y Ecosistema
              </span>
            </div>

            <h2 id="partner-modal-title" className="text-2xl sm:text-3xl font-extrabold text-kuota-deepnavy tracking-tight mb-2">
              Colabora con KUOTA
            </h2>
            <p className="text-sm font-semibold text-kuota-crayola mb-6">
              Construyamos juntos infraestructura y canales de financiamiento comercial para el mercado peruano.
            </p>

            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="partner-org-name" className="block text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-1">
                    Organización / Empresa *
                  </label>
                  <div className="relative">
                    <Building2 className="w-4 h-4 text-kuota-deepnavy/40 absolute left-3 top-3 pointer-events-none" />
                    <input
                      id="partner-org-name"
                      type="text"
                      placeholder="Ej. Distribuidora Central S.A.C."
                      value={organizationName}
                      onChange={(e) => setOrganizationName(e.target.value)}
                      className="w-full pl-9 pr-3 py-2.5 rounded-xl border-2 border-kuota-deepnavy text-sm font-bold text-kuota-deepnavy placeholder:text-kuota-deepnavy/40 focus:outline-none focus:ring-2 focus:ring-kuota-dodger"
                    />
                  </div>
                  {errors.organizationName && <p className="text-xs font-bold text-red-600 mt-1">{errors.organizationName}</p>}
                </div>

                <div>
                  <label htmlFor="partner-contact-name" className="block text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-1">
                    Contacto y Cargo *
                  </label>
                  <div className="relative">
                    <User className="w-4 h-4 text-kuota-deepnavy/40 absolute left-3 top-3 pointer-events-none" />
                    <input
                      id="partner-contact-name"
                      type="text"
                      placeholder="Nombre, Apellido y Cargo"
                      value={contactName}
                      onChange={(e) => setContactName(e.target.value)}
                      className="w-full pl-9 pr-3 py-2.5 rounded-xl border-2 border-kuota-deepnavy text-sm font-bold text-kuota-deepnavy placeholder:text-kuota-deepnavy/40 focus:outline-none focus:ring-2 focus:ring-kuota-dodger"
                    />
                  </div>
                  {errors.contactName && <p className="text-xs font-bold text-red-600 mt-1">{errors.contactName}</p>}
                </div>
              </div>

              <div>
                <label htmlFor="partner-type" className="block text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-1">
                  Tipo de Alianza / Enfoque *
                </label>
                <select
                  id="partner-type"
                  value={partnerType}
                  onChange={(e) => setPartnerType(e.target.value)}
                  className="w-full px-3 py-2.5 rounded-xl border-2 border-kuota-deepnavy text-sm font-bold text-kuota-deepnavy bg-white focus:outline-none focus:ring-2 focus:ring-kuota-dodger"
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
                  <label htmlFor="partner-email" className="block text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-1">
                    Correo Corporativo *
                  </label>
                  <div className="relative">
                    <Mail className="w-4 h-4 text-kuota-deepnavy/40 absolute left-3 top-3 pointer-events-none" />
                    <input
                      id="partner-email"
                      type="email"
                      placeholder="alianzas@empresa.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      className="w-full pl-9 pr-3 py-2.5 rounded-xl border-2 border-kuota-deepnavy text-sm font-bold text-kuota-deepnavy placeholder:text-kuota-deepnavy/40 focus:outline-none focus:ring-2 focus:ring-kuota-dodger"
                    />
                  </div>
                  {errors.email && <p className="text-xs font-bold text-red-600 mt-1">{errors.email}</p>}
                </div>

                <div>
                  <label htmlFor="partner-phone" className="block text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-1">
                    Teléfono de Contacto *
                  </label>
                  <div className="relative">
                    <Phone className="w-4 h-4 text-kuota-deepnavy/40 absolute left-3 top-3 pointer-events-none" />
                    <input
                      id="partner-phone"
                      type="tel"
                      placeholder="+51 9..."
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      className="w-full pl-9 pr-3 py-2.5 rounded-xl border-2 border-kuota-deepnavy text-sm font-bold text-kuota-deepnavy placeholder:text-kuota-deepnavy/40 focus:outline-none focus:ring-2 focus:ring-kuota-dodger"
                    />
                  </div>
                  {errors.phone && <p className="text-xs font-bold text-red-600 mt-1">{errors.phone}</p>}
                </div>
              </div>

              <div>
                <label htmlFor="partner-message" className="block text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-1">
                  Propuesta o mensaje para la Dirección de KUOTA
                </label>
                <div className="relative">
                  <MessageSquare className="w-4 h-4 text-kuota-deepnavy/40 absolute left-3 top-3 pointer-events-none" />
                  <textarea
                    id="partner-message"
                    rows={3}
                    placeholder="Describe brevemente el alcance de la colaboración que deseas explorar..."
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                    className="w-full pl-9 pr-3 py-2 rounded-xl border-2 border-kuota-deepnavy text-sm font-bold text-kuota-deepnavy placeholder:text-kuota-deepnavy/40 focus:outline-none focus:ring-2 focus:ring-kuota-dodger"
                  />
                </div>
              </div>

              <div className="pt-2">
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full py-3.5 px-6 rounded-2xl bg-kuota-deepnavy hover:bg-kuota-dodger text-white font-extrabold text-sm sm:text-base border-2 border-kuota-deepnavy shadow-navy-hard transition-all duration-150 hover:-translate-y-0.5 active:translate-y-0 flex items-center justify-center gap-2"
                >
                  <Send className="w-4 h-4 text-kuota-palesky" />
                  <span>{isSubmitting ? 'Registrando contacto...' : 'Enviar propuesta institucional'}</span>
                </button>
                <p className="text-center text-[11px] font-bold text-kuota-deepnavy/65 mt-2.5">
                  Contacto directo con la Dirección Ejecutiva de KUOTA.
                </p>
              </div>
            </form>
          </div>
        ) : (
          <div className="text-center py-4 space-y-4">
            <div className="w-16 h-16 rounded-full bg-blue-100 border-2 border-kuota-deepnavy text-kuota-deepnavy flex items-center justify-center mx-auto">
              <CheckCircle2 className="w-10 h-10 text-kuota-dodger" />
            </div>

            <h3 className="text-2xl font-extrabold text-kuota-deepnavy">
              ¡Mensaje institucional recibido!
            </h3>
            <p className="text-sm font-semibold text-kuota-crayola max-w-md mx-auto">
              Hemos registrado la propuesta de <span className="font-extrabold text-kuota-deepnavy">{organizationName}</span> (<span className="font-extrabold text-kuota-deepnavy">{submissionResult.leadId}</span>). Nuestro equipo directivo coordinará una reunión de trabajo.
            </p>

            <div className="pt-2">
              <a
                href={submissionResult.mailtoUrl}
                className="w-full py-3 px-4 rounded-xl bg-kuota-deepnavy hover:bg-kuota-dodger text-white font-extrabold text-sm border-2 border-kuota-deepnavy shadow-navy-hard flex items-center justify-center gap-2 transition-transform hover:-translate-y-0.5"
              >
                <Mail className="w-4 h-4 text-kuota-dodger" />
                <span>Enviar copia directa a {OFFICIAL_CONTACT.email}</span>
              </a>
            </div>

            <button
              type="button"
              onClick={handleResetAndClose}
              className="mt-4 text-xs font-extrabold text-kuota-deepnavy underline hover:text-kuota-dodger"
            >
              Cerrar y volver a la página
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

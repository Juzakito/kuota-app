import React, { useState } from 'react';
import { X, Store, CheckCircle2, MessageSquare, Mail, Phone, MapPin, User, Building, Send } from 'lucide-react';
import { submitMerchantLead, OFFICIAL_CONTACT } from '../../utils/leadsService';
import { kuotaSound } from '../../utils/soundSystem';

interface MerchantAffiliationModalProps {
  isOpen: boolean;
  onClose: () => void;
  defaultBusinessType?: string;
}

const BUSINESS_TYPES = [
  'Bodega / Minimarket',
  'Ferretería / Construcción',
  'Tienda de Ropa / Calzado',
  'Tecnología / Telefonía / Accesorios',
  'Farmacia / Botica',
  'Distribuidora / Mayorista de Abarrotes',
  'Librería / Bazar',
  'Restaurante / Comida rápida',
  'Otro comercio minorista o independiente',
];

export const MerchantAffiliationModal: React.FC<MerchantAffiliationModalProps> = ({
  isOpen,
  onClose,
  defaultBusinessType = 'Bodega / Minimarket',
}) => {
  const [businessName, setBusinessName] = useState('');
  const [contactName, setContactName] = useState('');
  const [businessType, setBusinessType] = useState(defaultBusinessType);
  const [city, setCity] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submissionResult, setSubmissionResult] = useState<{
    leadId: string;
    whatsappUrl: string;
    mailtoUrl: string;
  } | null>(null);

  if (!isOpen) return null;

  const validate = () => {
    const newErrors: Record<string, string> = {};
    if (!businessName.trim()) newErrors.businessName = 'Ingresa el nombre comercial de tu negocio.';
    if (!contactName.trim()) newErrors.contactName = 'Ingresa tu nombre y apellido.';
    if (!city.trim()) newErrors.city = 'Indica tu ciudad o distrito (ej. Lima Norte, Arequipa, etc.).';
    if (!phone.trim() || phone.replace(/\D/g, '').length < 9) {
      newErrors.phone = 'Ingresa un celular válido de 9 dígitos para contactarte por WhatsApp.';
    }
    if (!email.trim() || !/^\S+@\S+\.\S+$/.test(email)) {
      newErrors.email = 'Ingresa un correo electrónico de contacto válido.';
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

    const result = await submitMerchantLead({
      businessName,
      contactName,
      businessType,
      city,
      phone,
      email,
      message,
    });

    setIsSubmitting(false);
    setSubmissionResult({
      leadId: result.leadId,
      whatsappUrl: result.whatsappUrl,
      mailtoUrl: result.mailtoUrl,
    });
    kuotaSound.playConfirmOperation();
  };

  const handleResetAndClose = () => {
    setSubmissionResult(null);
    setBusinessName('');
    setContactName('');
    setCity('');
    setPhone('');
    setEmail('');
    setMessage('');
    setErrors({});
    onClose();
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="merchant-modal-title"
      className="fixed inset-0 z-50 overflow-y-auto bg-kuota-deepnavy/65 backdrop-blur-sm p-4 sm:p-6 flex items-center justify-center animate-fadeIn"
    >
      <div className="relative w-full max-w-xl rounded-3xl border-2 border-kuota-deepnavy bg-white p-6 sm:p-8 shadow-navy-hard-lg my-8">
        <button
          type="button"
          onClick={handleResetAndClose}
          aria-label="Cerrar formulario de afiliación"
          className="absolute top-5 right-5 w-9 h-9 rounded-xl border-2 border-kuota-deepnavy bg-kuota-palesky hover:bg-white text-kuota-deepnavy flex items-center justify-center transition-colors shadow-sm"
        >
          <X className="w-5 h-5" />
        </button>

        {!submissionResult ? (
          <div>
            <div className="flex items-center gap-2 mb-2">
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-kuota-palesky border border-kuota-deepnavy text-xs font-extrabold uppercase tracking-wider text-kuota-deepnavy">
                <Store className="w-3.5 h-3.5 text-kuota-dodger" />
                Admisión de Comercios
              </span>
            </div>

            <h2 id="merchant-modal-title" className="text-2xl sm:text-3xl font-extrabold text-kuota-deepnavy tracking-tight mb-2">
              Afiliar mi comercio a KUOTA
            </h2>
            <p className="text-sm font-semibold text-kuota-crayola mb-6">
              Ofrece compras en cuotas a tus clientes, dinamiza tus colocaciones y gestiona tu recaudación con respaldo tecnológico.
            </p>

            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="modal-business-name" className="block text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-1">
                    Nombre del Negocio *
                  </label>
                  <div className="relative">
                    <Building className="w-4 h-4 text-kuota-deepnavy/40 absolute left-3 top-3 pointer-events-none" />
                    <input
                      id="modal-business-name"
                      type="text"
                      placeholder="Ej. Bodega Don Lucho"
                      value={businessName}
                      onChange={(e) => setBusinessName(e.target.value)}
                      className="w-full pl-9 pr-3 py-2.5 rounded-xl border-2 border-kuota-deepnavy text-sm font-bold text-kuota-deepnavy placeholder:text-kuota-deepnavy/40 focus:outline-none focus:ring-2 focus:ring-kuota-dodger"
                    />
                  </div>
                  {errors.businessName && <p className="text-xs font-bold text-red-600 mt-1">{errors.businessName}</p>}
                </div>

                <div>
                  <label htmlFor="modal-contact-name" className="block text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-1">
                    Responsable / Titular *
                  </label>
                  <div className="relative">
                    <User className="w-4 h-4 text-kuota-deepnavy/40 absolute left-3 top-3 pointer-events-none" />
                    <input
                      id="modal-contact-name"
                      type="text"
                      placeholder="Tu nombre y apellido"
                      value={contactName}
                      onChange={(e) => setContactName(e.target.value)}
                      className="w-full pl-9 pr-3 py-2.5 rounded-xl border-2 border-kuota-deepnavy text-sm font-bold text-kuota-deepnavy placeholder:text-kuota-deepnavy/40 focus:outline-none focus:ring-2 focus:ring-kuota-dodger"
                    />
                  </div>
                  {errors.contactName && <p className="text-xs font-bold text-red-600 mt-1">{errors.contactName}</p>}
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="modal-business-type" className="block text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-1">
                    Rubro Comercial *
                  </label>
                  <select
                    id="modal-business-type"
                    value={businessType}
                    onChange={(e) => setBusinessType(e.target.value)}
                    className="w-full px-3 py-2.5 rounded-xl border-2 border-kuota-deepnavy text-sm font-bold text-kuota-deepnavy bg-white focus:outline-none focus:ring-2 focus:ring-kuota-dodger"
                  >
                    {BUSINESS_TYPES.map((type) => (
                      <option key={type} value={type}>
                        {type}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label htmlFor="modal-city" className="block text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-1">
                    Ciudad / Distrito *
                  </label>
                  <div className="relative">
                    <MapPin className="w-4 h-4 text-kuota-deepnavy/40 absolute left-3 top-3 pointer-events-none" />
                    <input
                      id="modal-city"
                      type="text"
                      placeholder="Ej. Los Olivos, Lima"
                      value={city}
                      onChange={(e) => setCity(e.target.value)}
                      className="w-full pl-9 pr-3 py-2.5 rounded-xl border-2 border-kuota-deepnavy text-sm font-bold text-kuota-deepnavy placeholder:text-kuota-deepnavy/40 focus:outline-none focus:ring-2 focus:ring-kuota-dodger"
                    />
                  </div>
                  {errors.city && <p className="text-xs font-bold text-red-600 mt-1">{errors.city}</p>}
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="modal-phone" className="block text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-1">
                    Celular WhatsApp *
                  </label>
                  <div className="relative">
                    <Phone className="w-4 h-4 text-kuota-deepnavy/40 absolute left-3 top-3 pointer-events-none" />
                    <input
                      id="modal-phone"
                      type="tel"
                      placeholder="987 654 321"
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      className="w-full pl-9 pr-3 py-2.5 rounded-xl border-2 border-kuota-deepnavy text-sm font-bold text-kuota-deepnavy placeholder:text-kuota-deepnavy/40 focus:outline-none focus:ring-2 focus:ring-kuota-dodger"
                    />
                  </div>
                  {errors.phone && <p className="text-xs font-bold text-red-600 mt-1">{errors.phone}</p>}
                </div>

                <div>
                  <label htmlFor="modal-email" className="block text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-1">
                    Correo Electrónico *
                  </label>
                  <div className="relative">
                    <Mail className="w-4 h-4 text-kuota-deepnavy/40 absolute left-3 top-3 pointer-events-none" />
                    <input
                      id="modal-email"
                      type="email"
                      placeholder="contacto@minegocio.pe"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      className="w-full pl-9 pr-3 py-2.5 rounded-xl border-2 border-kuota-deepnavy text-sm font-bold text-kuota-deepnavy placeholder:text-kuota-deepnavy/40 focus:outline-none focus:ring-2 focus:ring-kuota-dodger"
                    />
                  </div>
                  {errors.email && <p className="text-xs font-bold text-red-600 mt-1">{errors.email}</p>}
                </div>
              </div>

              <div>
                <label htmlFor="modal-message" className="block text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-1">
                  Mensaje o detalle adicional (opcional)
                </label>
                <div className="relative">
                  <MessageSquare className="w-4 h-4 text-kuota-deepnavy/40 absolute left-3 top-3 pointer-events-none" />
                  <textarea
                    id="modal-message"
                    rows={2}
                    placeholder="Cuéntanos sobre tu volumen de ventas o preguntas específicas..."
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
                  className="w-full py-3.5 px-6 rounded-2xl bg-kuota-dodger hover:bg-kuota-crayola text-white font-extrabold text-sm sm:text-base border-2 border-kuota-deepnavy shadow-navy-hard transition-all duration-150 hover:-translate-y-0.5 active:translate-y-0 flex items-center justify-center gap-2"
                >
                  <Send className="w-4 h-4" />
                  <span>{isSubmitting ? 'Registrando solicitud...' : 'Enviar solicitud de afiliación'}</span>
                </button>
                <p className="text-center text-[11px] font-bold text-kuota-deepnavy/65 mt-2.5">
                  Tus datos se procesan con reserva y confidencialidad según la Ley N° 29733 (Perú).
                </p>
              </div>
            </form>
          </div>
        ) : (
          <div className="text-center py-4 space-y-4">
            <div className="w-16 h-16 rounded-full bg-emerald-100 border-2 border-emerald-600 text-emerald-600 flex items-center justify-center mx-auto">
              <CheckCircle2 className="w-10 h-10" />
            </div>

            <h3 className="text-2xl font-extrabold text-kuota-deepnavy">
              ¡Solicitud de afiliación registrada!
            </h3>
            <p className="text-sm font-semibold text-kuota-crayola max-w-md mx-auto">
              Hemos guardado tu ficha de comercio (<span className="font-extrabold text-kuota-deepnavy">{submissionResult.leadId}</span>). Nuestro equipo comercial revisará tus datos para el piloto oficial de KUOTA.
            </p>

            <div className="rounded-2xl bg-kuota-palesky/50 border border-kuota-deepnavy p-4 text-left text-xs font-bold text-kuota-deepnavy space-y-1.5">
              <div className="flex justify-between">
                <span className="text-kuota-crayola">Comercio:</span>
                <span>{businessName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-kuota-crayola">Rubro:</span>
                <span>{businessType}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-kuota-crayola">Contacto:</span>
                <span>{contactName} · {phone}</span>
              </div>
            </div>

            <div className="space-y-2 pt-2">
              <a
                href={submissionResult.whatsappUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="w-full py-3 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-extrabold text-sm border-2 border-kuota-deepnavy shadow-navy-hard flex items-center justify-center gap-2 transition-transform hover:-translate-y-0.5"
              >
                <Phone className="w-4 h-4" />
                <span>Confirmar de inmediato por WhatsApp</span>
              </a>

              <a
                href={submissionResult.mailtoUrl}
                className="w-full py-2.5 px-4 rounded-xl bg-white hover:bg-kuota-palesky/60 text-kuota-deepnavy font-extrabold text-xs border border-kuota-deepnavy flex items-center justify-center gap-2"
              >
                <Mail className="w-4 h-4 text-kuota-dodger" />
                <span>Enviar respaldo por correo a {OFFICIAL_CONTACT.email}</span>
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

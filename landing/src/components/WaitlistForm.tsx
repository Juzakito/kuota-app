import React, { useState } from 'react';
import { CheckCircle2, ShieldCheck, ArrowRight, Loader2 } from 'lucide-react';
import { submitWaitlistLead } from '../utils/leadsService';

export interface WaitlistFormProps {
  defaultRole?: 'comprador' | 'proveedor' | 'ambos';
  onSuccess?: () => void;
  className?: string;
  title?: string;
  subtitle?: string;
}

const CATEGORIES = [
  'Bodega / Abarrotes',
  'Textil / Confecciones',
  'Ferretería',
  'Autopartes',
  'Otro',
];

export const WaitlistForm: React.FC<WaitlistFormProps> = ({
  defaultRole = 'comprador',
  onSuccess,
  className = '',
  title = 'Únete a la lista de espera oficial',
  subtitle = 'Lanzamiento progresivo por zonas y rubros en Lima y provincias. Prioridad para negocios registrados.',
}) => {
  const [fullName, setFullName] = useState('');
  const [businessName, setBusinessName] = useState('');
  const [ruc, setRuc] = useState('');
  const [category, setCategory] = useState(CATEGORIES[0]);
  const [location, setLocation] = useState('');
  const [phone, setPhone] = useState('');
  const [role, setRole] = useState<'comprador' | 'proveedor' | 'ambos'>(defaultRole);
  const [consent, setConsent] = useState(false);
  const [honeypot, setHoneypot] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    // Detección anti-bot por honeypot
    if (honeypot) {
      console.warn('Bot submission prevented.');
      return;
    }

    if (!fullName.trim()) {
      setErrorMessage('Por favor, ingresa tu nombre completo.');
      return;
    }
    if (!businessName.trim()) {
      setErrorMessage('Por favor, ingresa el nombre de tu negocio.');
      return;
    }
    if (!location.trim()) {
      setErrorMessage('Por favor, indica tu distrito o zona.');
      return;
    }
    const cleanPhone = phone.replace(/\D/g, '');
    if (cleanPhone.length < 9) {
      setErrorMessage('Por favor, ingresa un número de WhatsApp válido (9 dígitos).');
      return;
    }
    if (!consent) {
      setErrorMessage('Debes aceptar la política de privacidad y tratamiento de datos conforme a la Ley N° 29733.');
      return;
    }

    setIsSubmitting(true);
    try {
      await submitWaitlistLead({
        fullName: fullName.trim(),
        businessName: businessName.trim(),
        ruc: ruc.trim() || undefined,
        category,
        location: location.trim(),
        phone: cleanPhone,
        role,
        consent,
      });

      if (onSuccess) {
        onSuccess();
      } else {
        // Redirigir a /gracias
        if (typeof window !== 'undefined') {
          const url = new URL(window.location.href);
          url.pathname = '/gracias';
          url.searchParams.set('perfil', 'gracias');
          window.location.href = url.toString();
        }
      }
    } catch (err) {
      console.error('Error submitting waitlist form:', err);
      setErrorMessage('Ocurrió un problema de conexión al guardar tu registro. Por favor, reintenta.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={`bg-paper rounded-card border border-fog p-6 md:p-8 max-w-xl mx-auto shadow-card ${className}`}>
      <div className="mb-6 text-center">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-pill bg-linen text-forest text-xs font-semibold mb-3">
          <ShieldCheck className="w-3.5 h-3.5" />
          <span>Acceso anticipado oficial</span>
        </div>
        <h3 className="text-2xl md:text-3xl font-extrabold text-obsidian tracking-tight">
          {title}
        </h3>
        <p className="text-sm text-charcoal/80 mt-2">
          {subtitle}
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Campo honeypot anti-bot */}
        <div className="hidden" aria-hidden="true">
          <input
            type="text"
            name="website_url_check"
            tabIndex={-1}
            value={honeypot}
            onChange={(e) => setHoneypot(e.target.value)}
            autoComplete="off"
          />
        </div>

        {/* Selector de Rol en pills */}
        <div>
          <label className="block text-xs font-bold uppercase tracking-wider text-charcoal mb-2">
            ¿Cómo deseas participar?
          </label>
          <div className="grid grid-cols-3 gap-2">
            {[
              { id: 'comprador', label: 'Comprador' },
              { id: 'proveedor', label: 'Proveedor' },
              { id: 'ambos', label: 'Ambos' },
            ].map((item) => (
              <button
                key={item.id}
                type="button"
                onClick={() => setRole(item.id as 'comprador' | 'proveedor' | 'ambos')}
                className={`py-2 px-3 text-xs md:text-sm font-semibold rounded-pill transition-all ${
                  role === item.id
                    ? 'bg-forest text-lime shadow-sm'
                    : 'bg-fog text-charcoal hover:bg-linen/60'
                }`}
              >
                {item.label}
              </button>
            ))}
          </div>
        </div>

        {/* Nombre y Negocio */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-semibold text-charcoal mb-1">
              Nombre completo <span className="text-alarm">*</span>
            </label>
            <input
              type="text"
              required
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="Ej. José Morales"
              className="w-full px-3.5 py-2.5 text-sm bg-paper border border-pebble/40 rounded-card text-charcoal placeholder:text-pebble focus:border-forest focus:outline-none transition-colors"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold text-charcoal mb-1">
              Nombre de tu negocio <span className="text-alarm">*</span>
            </label>
            <input
              type="text"
              required
              value={businessName}
              onChange={(e) => setBusinessName(e.target.value)}
              placeholder="Ej. Bodega San Martín"
              className="w-full px-3.5 py-2.5 text-sm bg-paper border border-pebble/40 rounded-card text-charcoal placeholder:text-pebble focus:border-forest focus:outline-none transition-colors"
            />
          </div>
        </div>

        {/* RUC (opcional) y Zona/Distrito */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-semibold text-charcoal mb-1">
              RUC 10 u 11 dígitos <span className="text-xs text-pebble font-normal">(Opcional)</span>
            </label>
            <input
              type="text"
              maxLength={11}
              value={ruc}
              onChange={(e) => setRuc(e.target.value.replace(/\D/g, ''))}
              placeholder="Ej. 10458796321"
              className="w-full px-3.5 py-2.5 text-sm bg-paper border border-pebble/40 rounded-card text-charcoal placeholder:text-pebble focus:border-forest focus:outline-none transition-colors"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold text-charcoal mb-1">
              Distrito / Zona <span className="text-alarm">*</span>
            </label>
            <input
              type="text"
              required
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              placeholder="Ej. Gamarra, La Victoria"
              className="w-full px-3.5 py-2.5 text-sm bg-paper border border-pebble/40 rounded-card text-charcoal placeholder:text-pebble focus:border-forest focus:outline-none transition-colors"
            />
          </div>
        </div>

        {/* Rubro (Chips) */}
        <div>
          <label className="block text-xs font-semibold text-charcoal mb-1.5">
            Rubro comercial
          </label>
          <div className="flex flex-wrap gap-2">
            {CATEGORIES.map((cat) => (
              <button
                key={cat}
                type="button"
                onClick={() => setCategory(cat)}
                className={`text-xs px-3 py-1.5 rounded-pill font-medium transition-colors ${
                  category === cat
                    ? 'bg-forest text-lime'
                    : 'bg-fog text-charcoal hover:bg-linen/50'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>

        {/* WhatsApp */}
        <div>
          <label className="block text-xs font-semibold text-charcoal mb-1">
            Número de WhatsApp <span className="text-alarm">*</span>
          </label>
          <div className="flex">
            <span className="inline-flex items-center px-3 text-sm bg-fog border border-r-0 border-pebble/40 rounded-l-card text-charcoal font-medium">
              🇵🇪 +51
            </span>
            <input
              type="tel"
              required
              maxLength={9}
              value={phone}
              onChange={(e) => setPhone(e.target.value.replace(/\D/g, ''))}
              placeholder="987654321"
              className="flex-1 px-3.5 py-2.5 text-sm bg-paper border border-pebble/40 rounded-r-card text-charcoal placeholder:text-pebble focus:border-forest focus:outline-none transition-colors"
            />
          </div>
        </div>

        {/* Consentimiento Ley N° 29733 */}
        <div className="pt-2">
          <label className="flex items-start gap-2.5 cursor-pointer text-xs text-charcoal leading-relaxed">
            <input
              type="checkbox"
              required
              checked={consent}
              onChange={(e) => setConsent(e.target.checked)}
              className="mt-0.5 w-4 h-4 rounded text-forest focus:ring-forest cursor-pointer accent-forest"
            />
            <span>
              Acepto la Política de Privacidad y el tratamiento de mis datos personales conforme a la{' '}
              <strong>Ley N° 29733</strong> (Ley de Protección de Datos Personales del Perú) para recibir novedades del lanzamiento progresivo de Kuota.
            </span>
          </label>
        </div>

        {/* Error si existiera */}
        {errorMessage && (
          <div className="p-3 bg-red-50 border border-alarm/30 rounded-card text-alarm text-xs font-medium">
            {errorMessage}
          </div>
        )}

        {/* Botón CTA Primario Pill Lima */}
        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full py-3.5 px-6 rounded-pill bg-lime text-forest font-bold text-sm hover:bg-lime-hover active:scale-[0.99] transition-all flex items-center justify-center gap-2 cursor-pointer shadow-sm disabled:opacity-60"
        >
          {isSubmitting ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              <span>Registrando...</span>
            </>
          ) : (
            <>
              <span>Registrarme en la lista de espera</span>
              <ArrowRight className="w-4 h-4" />
            </>
          )}
        </button>

        <div className="flex items-center justify-center gap-1.5 text-[11px] text-pebble pt-1">
          <CheckCircle2 className="w-3.5 h-3.5 text-spruce" />
          <span>Sin compromisos comerciales. Lanzamiento progresivo.</span>
        </div>
      </form>
    </div>
  );
};

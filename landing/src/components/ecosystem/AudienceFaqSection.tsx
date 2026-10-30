import React, { useState } from 'react';
import { HelpCircle, ChevronDown, ShoppingBag, Store, Handshake, CheckCircle2 } from 'lucide-react';
import { ScrollReveal } from '../../utils/motionSystem';
import { trackEvent } from '../../utils/analytics';
import { kuotaSound } from '../../utils/soundSystem';

interface FaqItem {
  id: string;
  question: string;
  answer: string;
}

const BUYER_FAQS: FaqItem[] = [
  {
    id: 'b-1',
    question: '¿Qué es KUOTA?',
    answer:
      'KUOTA es una plataforma digital fintech diseñada para facilitar el financiamiento comercial y la compra de mercadería en cuotas, conectando a emprendedores y compradores con comercios y distribuidores participantes en el Perú.',
  },
  {
    id: 'b-2',
    question: '¿Cómo puedo comprar en cuotas?',
    answer:
      'Ubica un comercio o distribuidor afiliado, selecciona tus productos o mercadería y escanea el código QR o link de Kuota. Al confirmar la operación y abonar tu cuota inicial, el saldo restante se divide en cuotas transparentes según las condiciones de tu nivel.',
  },
  {
    id: 'b-3',
    question: '¿Cómo encuentro comercios participantes?',
    answer:
      'A través del directorio de la aplicación Kuota y en los establecimientos físicos que exhiben el distintivo Kuota QR en mostrador en las principales zonas comerciales.',
  },
  {
    id: 'b-4',
    question: '¿Dónde puedo consultar mis cuotas y vencimientos?',
    answer:
      'En todo momento desde la aplicación móvil de Kuota. Tienes acceso a tu cronograma detallado de fechas, el importe de cada cuota y avisos preventivos antes de cada vencimiento.',
  },
  {
    id: 'b-5',
    question: '¿Cómo realizo mis pagos?',
    answer:
      'Puedes pagar tus cuotas de forma digital a través de billeteras digitales (Yape o Plin), transferencias bancarias o en ventanilla/agentes autorizados según los canales habilitados para tu cuenta.',
  },
];

const MERCHANT_FAQS: FaqItem[] = [
  {
    id: 'm-1',
    question: '¿Cómo puedo afiliar mi negocio a KUOTA?',
    answer:
      'Completando el formulario oficial de afiliación en esta web. Nuestro equipo de admisión se pondrá en contacto para validar la información de tu negocio, acordar las condiciones operativas y entregarte tus herramientas de cobro.',
  },
  {
    id: 'm-2',
    question: '¿Qué necesito para participar como comercio?',
    answer:
      'Contar con un negocio comercial activo en Perú (con RUC o DNI para personas con negocio), número de celular para contacto y una cuenta bancaria o billetera para la recepción de liquidaciones.',
  },
  {
    id: 'm-3',
    question: '¿Cómo funcionan las colocaciones?',
    answer:
      'Cuando un comprador calificado adquiere mercadería en tu establecimiento, la operación se registra como colocación en la plataforma. Kuota estructura el financiamiento del comprador para que puedas despachar con respaldo.',
  },
  {
    id: 'm-4',
    question: '¿Cómo se realiza el seguimiento de recaudación?',
    answer:
      'Tienes acceso a un panel de control comercial donde visualizas cada venta, el estado de las cuotas programadas y la conciliación correspondiente, sin necesidad de cobranzas manuales.',
  },
  {
    id: 'm-5',
    question: '¿Cómo recibo soporte para mi comercio?',
    answer:
      'Dispones de un canal prioritario de atención por WhatsApp y correo electrónico oficial (kuota.contact@gmail.com) con el equipo de soporte de KUOTA en Lima.',
  },
];

const PARTNER_FAQS: FaqItem[] = [
  {
    id: 'p-1',
    question: '¿Cómo puedo colaborar o establecer una alianza con KUOTA?',
    answer:
      'A través del botón "Contactar a KUOTA" o escribiendo a kuota.contact@gmail.com. Evaluamos colaboraciones con distribuidores mayoristas, entidades financieras originadoras, enablers de pagos y operadores logísticos.',
  },
  {
    id: 'p-2',
    question: '¿Qué oportunidades de integración existen?',
    answer:
      'Integración de pasarelas de pago, homologación de catálogos mayoristas para colocación de mercadería, provisión de infraestructura de conciliación y programas de formalización para microempresarios.',
  },
  {
    id: 'p-3',
    question: '¿Cuál es el estado regulatorio y de lanzamiento de KUOTA?',
    answer:
      'KUOTA se encuentra actualmente en fase de piloto operativo y preparación técnica y legal hacia su lanzamiento oficial el 10 de noviembre de 2026 en Lima, Perú.',
  },
];

export const AudienceFaqSection: React.FC = () => {
  const [activeAudience, setActiveAudience] = useState<'compradores' | 'comercios' | 'aliados'>('compradores');
  const [openFaqId, setOpenFaqId] = useState<string | null>(BUYER_FAQS[0].id);

  const currentFaqs =
    activeAudience === 'compradores'
      ? BUYER_FAQS
      : activeAudience === 'comercios'
      ? MERCHANT_FAQS
      : PARTNER_FAQS;

  const handleToggleFaq = (id: string, qText: string) => {
    kuotaSound.playSelectOption();
    const next = openFaqId === id ? null : id;
    setOpenFaqId(next);
    trackEvent('faq_interaction', { faq_id: id, audience: activeAudience, is_open: Boolean(next) });
  };

  const handleAudienceTabChange = (audience: 'compradores' | 'comercios' | 'aliados') => {
    kuotaSound.playSelectOption();
    setActiveAudience(audience);
    const newFaqs =
      audience === 'compradores'
        ? BUYER_FAQS
        : audience === 'comercios'
        ? MERCHANT_FAQS
        : PARTNER_FAQS;
    setOpenFaqId(newFaqs[0].id);
    trackEvent('faq_audience_change', { audience });
  };

  return (
    <section
      id="faq"
      aria-labelledby="faq-heading"
      className="py-16 sm:py-24 bg-kuota-palesky/35 border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-10">
        <ScrollReveal delayMs={0} direction="up" className="text-center">
          <span className="inline-block px-3.5 py-1 rounded-full bg-white border-2 border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-3">
            Resolución de Dudas
          </span>
          <h2
            id="faq-heading"
            className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-kuota-deepnavy tracking-tight"
          >
            Preguntas Frecuentes
          </h2>
          <p className="mt-3 text-base sm:text-lg font-semibold text-kuota-crayola">
            Respuestas claras y fundamentadas según tu perfil en el ecosistema.
          </p>
        </ScrollReveal>

        {/* Selector de Pestañas de Audiencia */}
        <div className="flex justify-center">
          <div
            role="tablist"
            aria-label="Pestañas de preguntas frecuentes"
            className="inline-flex p-1.5 rounded-2xl bg-white border-2 border-kuota-deepnavy gap-1 shadow-sm"
          >
            <button
              type="button"
              role="tab"
              aria-selected={activeAudience === 'compradores'}
              onClick={() => handleAudienceTabChange('compradores')}
              className={`px-4 py-2 rounded-xl text-xs sm:text-sm font-extrabold transition-all flex items-center gap-1.5 ${
                activeAudience === 'compradores'
                  ? 'bg-kuota-dodger text-white shadow-sm'
                  : 'text-kuota-deepnavy hover:bg-kuota-palesky/50'
              }`}
            >
              <ShoppingBag className="w-3.5 h-3.5" />
              <span>Compradores</span>
            </button>

            <button
              type="button"
              role="tab"
              aria-selected={activeAudience === 'comercios'}
              onClick={() => handleAudienceTabChange('comercios')}
              className={`px-4 py-2 rounded-xl text-xs sm:text-sm font-extrabold transition-all flex items-center gap-1.5 ${
                activeAudience === 'comercios'
                  ? 'bg-kuota-deepnavy text-white shadow-sm'
                  : 'text-kuota-deepnavy hover:bg-kuota-palesky/50'
              }`}
            >
              <Store className="w-3.5 h-3.5 text-kuota-palesky" />
              <span>Comercios</span>
            </button>

            <button
              type="button"
              role="tab"
              aria-selected={activeAudience === 'aliados'}
              onClick={() => handleAudienceTabChange('aliados')}
              className={`px-4 py-2 rounded-xl text-xs sm:text-sm font-extrabold transition-all flex items-center gap-1.5 ${
                activeAudience === 'aliados'
                  ? 'bg-kuota-crayola text-white shadow-sm'
                  : 'text-kuota-deepnavy hover:bg-kuota-palesky/50'
              }`}
            >
              <Handshake className="w-3.5 h-3.5" />
              <span>Aliados</span>
            </button>
          </div>
        </div>

        {/* Acordeones de Preguntas */}
        <div className="space-y-3">
          {currentFaqs.map((faq) => {
            const isOpen = openFaqId === faq.id;
            return (
              <div
                key={faq.id}
                className={`rounded-2xl border-2 transition-all duration-200 bg-white ${
                  isOpen
                    ? 'border-kuota-deepnavy shadow-navy-hard -translate-y-0.5'
                    : 'border-kuota-deepnavy/25 hover:border-kuota-deepnavy shadow-sm'
                }`}
              >
                <button
                  type="button"
                  aria-expanded={isOpen}
                  onClick={() => handleToggleFaq(faq.id, faq.question)}
                  className="w-full p-4 sm:p-5 text-left flex items-center justify-between gap-4 font-extrabold text-base sm:text-lg text-kuota-deepnavy"
                >
                  <span className="flex items-center gap-3">
                    <HelpCircle className="w-5 h-5 text-kuota-dodger shrink-0" />
                    <span>{faq.question}</span>
                  </span>
                  <ChevronDown
                    className={`w-5 h-5 text-kuota-deepnavy shrink-0 transition-transform duration-200 ${
                      isOpen ? 'rotate-180 text-kuota-dodger' : ''
                    }`}
                  />
                </button>

                {isOpen && (
                  <div className="px-5 pb-5 pt-1 border-t border-kuota-deepnavy/10 text-xs sm:text-sm font-semibold text-kuota-crayola leading-relaxed animate-fadeIn">
                    <p>{faq.answer}</p>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};

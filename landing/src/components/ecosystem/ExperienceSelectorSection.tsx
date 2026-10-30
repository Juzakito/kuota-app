import React from 'react';
import { ShoppingBag, Store, Handshake, ArrowRight, CheckCircle2, ChevronRight, Sparkles } from 'lucide-react';
import { ScrollReveal, TiltCard } from '../../utils/motionSystem';
import { trackEvent } from '../../utils/analytics';
import { kuotaSound } from '../../utils/soundSystem';

interface ExperienceSelectorSectionProps {
  onSelectBuyer: () => void;
  onSelectMerchant: () => void;
  onOpenPartnerModal: () => void;
}

export const ExperienceSelectorSection: React.FC<ExperienceSelectorSectionProps> = ({
  onSelectBuyer,
  onSelectMerchant,
  onOpenPartnerModal,
}) => {
  return (
    <section
      id="selector"
      aria-labelledby="selector-heading"
      className="py-16 sm:py-24 bg-white border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <ScrollReveal delayMs={0} direction="up" className="text-center max-w-3xl mx-auto">
          <span className="inline-block px-3.5 py-1 rounded-full bg-kuota-palesky border-2 border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-3">
            Audiencias y Participantes
          </span>
          <h2
            id="selector-heading"
            className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-kuota-deepnavy tracking-tight"
          >
            KUOTA conecta diferentes oportunidades
          </h2>
          <p className="mt-3 text-base sm:text-lg font-semibold text-kuota-crayola">
            Identifica tu rol en el ecosistema y accede a la experiencia diseñada para ti.
          </p>
        </ScrollReveal>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 lg:gap-8 items-stretch">
          
          {/* Tarjeta 1: Compradores */}
          <ScrollReveal delayMs={80} direction="up">
            <TiltCard maxTiltDeg={3} className="h-full">
              <div className="h-full rounded-3xl border-2 border-kuota-deepnavy bg-kuota-palesky/40 p-6 sm:p-7 flex flex-col justify-between shadow-navy-hard-lg transition-transform duration-200 hover:-translate-y-1">
                <div>
                  <div className="flex items-center justify-between mb-5">
                    <span className="w-12 h-12 rounded-2xl bg-kuota-dodger text-white border-2 border-kuota-deepnavy flex items-center justify-center shadow-sm">
                      <ShoppingBag className="w-6 h-6" />
                    </span>
                    <span className="px-3 py-1 rounded-full bg-white border border-kuota-deepnavy text-[11px] font-extrabold text-kuota-deepnavy uppercase tracking-wider">
                      Para Personas
                    </span>
                  </div>

                  <h3 className="text-2xl font-extrabold text-kuota-deepnavy mb-2">
                    Compradores
                  </h3>
                  <p className="text-base font-extrabold text-kuota-dodger mb-3">
                    Encuentra nuevas formas de comprar.
                  </p>
                  <p className="text-xs sm:text-sm font-semibold text-kuota-crayola leading-relaxed mb-6">
                    Accede a mercadería y productos en comercios participantes, organiza tus pagos en cuotas y consulta tus vencimientos sin costos ocultos.
                  </p>

                  <ul className="space-y-2 mb-6 text-xs sm:text-sm font-bold text-kuota-deepnavy">
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-kuota-dodger shrink-0" />
                      <span>Compra financiada en cuotas claras</span>
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-kuota-dodger shrink-0" />
                      <span>Calendario y recordatorios de pago</span>
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-kuota-dodger shrink-0" />
                      <span>Seguimiento de compromisos desde la app</span>
                    </li>
                  </ul>
                </div>

                <button
                  type="button"
                  onClick={() => {
                    kuotaSound.playSelectOption();
                    trackEvent('profile_switch', { to: 'compradores', source: 'selector_card' });
                    onSelectBuyer();
                  }}
                  className="w-full py-3.5 px-5 rounded-2xl bg-kuota-dodger hover:bg-kuota-crayola text-white font-extrabold text-sm border-2 border-kuota-deepnavy shadow-navy-hard flex items-center justify-center gap-2 transition-transform hover:-translate-y-0.5 active:translate-y-0"
                >
                  <span>Descubrir KUOTA</span>
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>
            </TiltCard>
          </ScrollReveal>

          {/* Tarjeta 2: Comercios */}
          <ScrollReveal delayMs={140} direction="up">
            <TiltCard maxTiltDeg={3} className="h-full">
              <div className="h-full rounded-3xl border-2 border-kuota-deepnavy bg-white p-6 sm:p-7 flex flex-col justify-between shadow-navy-hard-lg transition-transform duration-200 hover:-translate-y-1 relative">
                <span className="absolute -top-3 left-1/2 -translate-x-1/2 px-3 py-0.5 rounded-full bg-kuota-deepnavy text-white text-[10px] font-extrabold uppercase tracking-wider">
                  Piloto Activo
                </span>

                <div>
                  <div className="flex items-center justify-between mb-5">
                    <span className="w-12 h-12 rounded-2xl bg-kuota-deepnavy text-white border-2 border-kuota-deepnavy flex items-center justify-center shadow-sm">
                      <Store className="w-6 h-6 text-kuota-palesky" />
                    </span>
                    <span className="px-3 py-1 rounded-full bg-kuota-palesky border border-kuota-deepnavy text-[11px] font-extrabold text-kuota-deepnavy uppercase tracking-wider">
                      Para Negocios
                    </span>
                  </div>

                  <h3 className="text-2xl font-extrabold text-kuota-deepnavy mb-2">
                    Comercios
                  </h3>
                  <p className="text-base font-extrabold text-kuota-deepnavy mb-3">
                    Amplía las posibilidades de tu negocio.
                  </p>
                  <p className="text-xs sm:text-sm font-semibold text-kuota-crayola leading-relaxed mb-6">
                    Ofrece ventas en cuotas en tu mostrador, dinamiza tus colocaciones comerciales y mantén el control organizado de tu recaudación.
                  </p>

                  <ul className="space-y-2 mb-6 text-xs sm:text-sm font-bold text-kuota-deepnavy">
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-kuota-deepnavy shrink-0" />
                      <span>Más oportunidades de venta y ticket</span>
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-kuota-deepnavy shrink-0" />
                      <span>Gestión de colocaciones y despacho</span>
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-kuota-deepnavy shrink-0" />
                      <span>Monitoreo de cobro y recaudación</span>
                    </li>
                  </ul>
                </div>

                <button
                  type="button"
                  onClick={() => {
                    kuotaSound.playCtaClick();
                    trackEvent('profile_switch', { to: 'comercios', source: 'selector_card' });
                    onSelectMerchant();
                  }}
                  className="w-full py-3.5 px-5 rounded-2xl bg-kuota-deepnavy hover:bg-kuota-dodger text-white font-extrabold text-sm border-2 border-kuota-deepnavy shadow-navy-hard flex items-center justify-center gap-2 transition-transform hover:-translate-y-0.5 active:translate-y-0"
                >
                  <span>Afiliar mi comercio</span>
                  <ArrowRight className="w-4 h-4 text-kuota-palesky" />
                </button>
              </div>
            </TiltCard>
          </ScrollReveal>

          {/* Tarjeta 3: Aliados Estratégicos */}
          <ScrollReveal delayMs={200} direction="up">
            <TiltCard maxTiltDeg={3} className="h-full">
              <div className="h-full rounded-3xl border-2 border-kuota-deepnavy bg-kuota-palesky/40 p-6 sm:p-7 flex flex-col justify-between shadow-navy-hard-lg transition-transform duration-200 hover:-translate-y-1">
                <div>
                  <div className="flex items-center justify-between mb-5">
                    <span className="w-12 h-12 rounded-2xl bg-white text-kuota-deepnavy border-2 border-kuota-deepnavy flex items-center justify-center shadow-sm">
                      <Handshake className="w-6 h-6 text-kuota-dodger" />
                    </span>
                    <span className="px-3 py-1 rounded-full bg-white border border-kuota-deepnavy text-[11px] font-extrabold text-kuota-deepnavy uppercase tracking-wider">
                      Institucional
                    </span>
                  </div>

                  <h3 className="text-2xl font-extrabold text-kuota-deepnavy mb-2">
                    Aliados
                  </h3>
                  <p className="text-base font-extrabold text-kuota-crayola mb-3">
                    Construyamos nuevas oportunidades.
                  </p>
                  <p className="text-xs sm:text-sm font-semibold text-kuota-crayola leading-relaxed mb-6">
                    Colabora con KUOTA en originación, financiamiento, canales de distribución mayorista o integración de infraestructura tecnológica de pagos.
                  </p>

                  <ul className="space-y-2 mb-6 text-xs sm:text-sm font-bold text-kuota-deepnavy">
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-kuota-dodger shrink-0" />
                      <span>Ecosistema comercial conectado</span>
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-kuota-dodger shrink-0" />
                      <span>Integración de capacidades técnicas</span>
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-kuota-dodger shrink-0" />
                      <span>Desarrollo conjunto de soluciones</span>
                    </li>
                  </ul>
                </div>

                <button
                  type="button"
                  onClick={() => {
                    kuotaSound.playSelectOption();
                    trackEvent('partner_contact_click', { source: 'selector_card' });
                    onOpenPartnerModal();
                  }}
                  className="w-full py-3.5 px-5 rounded-2xl bg-white hover:bg-kuota-palesky text-kuota-deepnavy font-extrabold text-sm border-2 border-kuota-deepnavy shadow-navy-hard flex items-center justify-center gap-2 transition-transform hover:-translate-y-0.5 active:translate-y-0"
                >
                  <span>Contactar a KUOTA</span>
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>
            </TiltCard>
          </ScrollReveal>

        </div>
      </div>
    </section>
  );
};

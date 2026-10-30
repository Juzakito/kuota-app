import React from 'react';
import { ShoppingBag, Store, ArrowRight, ShieldCheck, Sparkles, Layers, QrCode, TrendingUp, CheckCircle2 } from 'lucide-react';
import { ScrollReveal, TiltCard, FinancialCardMotion, CircularImageReveal } from '../../utils/motionSystem';
import { trackEvent } from '../../utils/analytics';
import { kuotaSound } from '../../utils/soundSystem';

interface EcosystemHeroSectionProps {
  onSelectBuyer: () => void;
  onOpenMerchantAffiliation: () => void;
  onExploreEcosystem: () => void;
}

export const EcosystemHeroSection: React.FC<EcosystemHeroSectionProps> = ({
  onSelectBuyer,
  onOpenMerchantAffiliation,
  onExploreEcosystem,
}) => {
  return (
    <section
      id="hero-ecosistema"
      aria-labelledby="ecosystem-hero-title"
      className="relative pt-8 sm:pt-12 pb-16 sm:pb-24 overflow-hidden border-b-2 border-kuota-deepnavy bg-gradient-to-b from-kuota-palesky via-kuota-palesky/70 to-white/60"
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-8 items-center">
          
          {/* Columna Izquierda: Mensaje y Conversión Inmediata (< 5s) */}
          <div className="lg:col-span-7 space-y-6">
            <ScrollReveal delayMs={0} direction="up">
              <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white border-2 border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider shadow-sm">
                <Sparkles className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                <span>Ecosistema Fintech de Compras en Cuotas</span>
              </div>
            </ScrollReveal>

            <ScrollReveal delayMs={80} direction="up">
              <h1
                id="ecosystem-hero-title"
                className="text-3xl sm:text-5xl lg:text-6xl font-extrabold text-kuota-deepnavy tracking-tight leading-[1.08]"
              >
                Conectamos oportunidades para{' '}
                <span className="text-kuota-dodger underline decoration-kuota-deepnavy decoration-4 underline-offset-4">
                  comprar y vender
                </span>{' '}
                en cuotas.
              </h1>
            </ScrollReveal>

            <ScrollReveal delayMs={140} direction="up">
              <p className="text-base sm:text-xl font-semibold text-kuota-crayola max-w-2xl leading-relaxed">
                KUOTA conecta compradores y comercios a través de una experiencia digital que facilita nuevas oportunidades de compra, venta y gestión de pagos.
              </p>
            </ScrollReveal>

            {/* CTAs Principales */}
            <ScrollReveal delayMs={200} direction="up">
              <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3.5 pt-2">
                <button
                  type="button"
                  onClick={() => {
                    kuotaSound.playCtaClick();
                    trackEvent('hero_cta_buyer', { source: 'ecosystem_hero' });
                    onSelectBuyer();
                  }}
                  className="px-6 py-4 rounded-2xl bg-kuota-dodger hover:bg-kuota-crayola text-white font-extrabold text-base border-2 border-kuota-deepnavy shadow-navy-hard transition-all duration-150 hover:-translate-y-0.5 active:translate-y-0 flex items-center justify-center gap-2.5"
                >
                  <ShoppingBag className="w-5 h-5" />
                  <span>Quiero comprar</span>
                  <ArrowRight className="w-4 h-4 ml-0.5" />
                </button>

                <button
                  type="button"
                  onClick={() => {
                    kuotaSound.playCtaClick();
                    trackEvent('hero_cta_merchant', { source: 'ecosystem_hero' });
                    onOpenMerchantAffiliation();
                  }}
                  className="px-6 py-4 rounded-2xl bg-kuota-deepnavy hover:bg-kuota-dodger text-white font-extrabold text-base border-2 border-kuota-deepnavy shadow-navy-hard transition-all duration-150 hover:-translate-y-0.5 active:translate-y-0 flex items-center justify-center gap-2.5"
                >
                  <Store className="w-5 h-5 text-kuota-palesky" />
                  <span>Quiero afiliar mi negocio</span>
                </button>
              </div>

              <div className="pt-3">
                <button
                  type="button"
                  onClick={() => {
                    kuotaSound.playSelectOption();
                    onExploreEcosystem();
                  }}
                  className="inline-flex items-center gap-2 text-xs sm:text-sm font-extrabold text-kuota-deepnavy hover:text-kuota-dodger transition-colors"
                >
                  <Layers className="w-4 h-4 text-kuota-dodger" />
                  <span>Conoce nuestro ecosistema integral (Comprador ↔ Kuota ↔ Comercio) ↓</span>
                </button>
              </div>
            </ScrollReveal>

            {/* Badges de Confianza Rápida */}
            <ScrollReveal delayMs={260} direction="up">
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 pt-4 border-t border-kuota-deepnavy/15 text-xs font-bold text-kuota-deepnavy">
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-kuota-dodger shrink-0" />
                  <span>3 partes conectadas</span>
                </div>
                <div className="flex items-center gap-2">
                  <ShieldCheck className="w-4 h-4 text-kuota-dodger shrink-0" />
                  <span>Control de colocaciones</span>
                </div>
                <div className="flex items-center gap-2 col-span-2 sm:col-span-1">
                  <TrendingUp className="w-4 h-4 text-kuota-dodger shrink-0" />
                  <span>Recaudación trazable</span>
                </div>
              </div>
            </ScrollReveal>
          </div>

          {/* Columna Derecha: Composición Visual Editorial del Ecosistema */}
          <div className="lg:col-span-5 relative flex justify-center">
            <ScrollReveal delayMs={120} direction="right">
              <div className="relative w-full max-w-md">
                
                {/* Composición Fotográfica Circular (Comprador + Emprendedor Comercio) */}
                <div className="relative flex justify-center items-center">
                  <CircularImageReveal
                    src="/images/hero-negocio-kuota.jpg"
                    alt="Emprendedor y comerciante peruano abasteciendo su negocio"
                    sizeClassName="w-60 h-60 sm:w-72 sm:h-72"
                    topBadge={
                      <div className="px-3 py-1 rounded-full bg-white border-2 border-kuota-deepnavy text-[11px] font-extrabold text-kuota-deepnavy shadow-sm">
                        Comercio Participante ✓
                      </div>
                    }
                    bottomBadge={
                      <div className="px-3 py-1 rounded-full bg-kuota-dodger text-white text-[11px] font-extrabold border-2 border-kuota-deepnavy shadow-sm">
                        Venta en cuotas activa
                      </div>
                    }
                    className="z-10"
                  />
                  
                  {/* Tarjeta flotante superior: Operación en Cuotas */}
                  <div className="absolute -top-4 -left-2 sm:-left-4 z-20">
                    <FinancialCardMotion floating className="shadow-navy-hard-lg">
                      <div className="rounded-2xl bg-white border-2 border-kuota-deepnavy p-3.5 space-y-1.5">
                        <div className="flex items-center justify-between gap-3 text-xs font-extrabold text-kuota-deepnavy">
                          <span>Operación Comercial</span>
                          <span className="px-2 py-0.5 rounded bg-kuota-dodger text-white text-[10px] font-extrabold">
                            Cuotas 3x
                          </span>
                        </div>
                        <div className="text-xl font-extrabold text-kuota-deepnavy">
                          S/ 1,200.00
                        </div>
                        <div className="text-[11px] font-semibold text-kuota-crayola">
                          Mercadería entregada en lote
                        </div>
                      </div>
                    </FinancialCardMotion>
                  </div>

                  {/* Tarjeta flotante inferior: Estado de Recaudación */}
                  <div className="absolute -bottom-6 -right-2 sm:-right-4 z-20">
                    <TiltCard maxTiltDeg={3}>
                      <div className="rounded-2xl bg-white border-2 border-kuota-deepnavy p-3.5 shadow-navy-hard space-y-2">
                        <div className="flex items-center justify-between gap-3 text-xs font-extrabold text-kuota-deepnavy">
                          <span className="flex items-center gap-1.5">
                            <QrCode className="w-4 h-4 text-kuota-dodger" />
                            Pago QR / Link
                          </span>
                          <span className="px-2 py-0.5 rounded bg-emerald-100 text-emerald-700 text-[10px] font-extrabold">
                            Al día
                          </span>
                        </div>
                        <div className="text-[11px] font-semibold text-kuota-crayola">
                          Recaudación programada · Día 20 / 40 / 60
                        </div>
                      </div>
                    </TiltCard>
                  </div>
                </div>

              </div>
            </ScrollReveal>
          </div>

        </div>
      </div>
    </section>
  );
};

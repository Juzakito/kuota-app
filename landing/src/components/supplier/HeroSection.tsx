import React from 'react';
import { ArrowRight, ShieldCheck, Clock } from 'lucide-react';
import { ScheduleCallButton } from './ScheduleCallButton';
import { PRICING_CONFIG, formatPercentEs } from '../../config/pricingConfig';
import { trackSupplierEvent } from '../../utils/analytics';

/**
 * PRD §5.2: Hero
 * - Título (H1): "No pierdas ventas por culpa de las comisiones".
 * - Subtexto: "Tus clientes compran con tarjeta. Los POS te quitan tus ganancias con altas comisiones. Kuota te ofrece una comisión más baja y opciones de crédito para tus clientes con 0 riesgo para ti."
 * - CTA primario: "Agenda una call". CTA secundario: "Ver cómo funciona" (ancla a Producto).
 * - Imagen (IMG-01): foto de un dueño de negocio cobrando en su mostrador con un celular en la mano (1200×900).
 *   Sobre la imagen, una tarjeta flotante con "POS 3,5% → Kuota 2%".
 * - Móvil: la imagen pasa debajo del texto; la tarjeta flotante se mantiene.
 */
export const HeroSection: React.FC = () => {
  const posFeeText = formatPercentEs(PRICING_CONFIG.posAverageFeePercent);
  const kuotaFeeText = formatPercentEs(PRICING_CONFIG.kuotaPaymentsFeePercent);

  return (
    <section
      id="inicio"
      aria-labelledby="hero-supplier-heading"
      className="relative overflow-hidden pt-8 pb-14 sm:py-16 lg:py-20 border-b-2 border-kuota-deepnavy bg-grid-pattern"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-8 items-center">
          {/* Columna de Copy y CTAs */}
          <div className="lg:col-span-6 flex flex-col items-start text-left">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white border-2 border-kuota-deepnavy shadow-sm mb-5">
              <span className="w-2.5 h-2.5 rounded-full bg-kuota-dodger" aria-hidden="true" />
              <span className="text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider">
                Kuota para Proveedores y Comercios
              </span>
            </div>

            <h1
              id="hero-supplier-heading"
              className="text-4xl sm:text-5xl lg:text-[3.25rem] font-extrabold text-kuota-deepnavy tracking-tight leading-[1.08] mb-5"
            >
              No pierdas ventas por culpa de las{' '}
              <span className="text-kuota-dodger underline decoration-kuota-deepnavy decoration-4 underline-offset-4">
                comisiones
              </span>
            </h1>

            <p className="text-lg sm:text-xl font-semibold text-kuota-crayola leading-relaxed mb-7 max-w-xl">
              Tus clientes compran con tarjeta. Los POS te quitan tus ganancias con altas comisiones. Kuota te ofrece una comisión más baja y opciones de crédito para tus clientes con{' '}
              <strong className="text-kuota-deepnavy font-extrabold">
                {PRICING_CONFIG.supplierDefaultRisk} riesgo para ti.
              </strong>
            </p>

            {/* Píldoras de respaldo rápido */}
            <div className="flex flex-wrap gap-2 mb-7">
              <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/90 border border-kuota-deepnavy text-xs sm:text-sm font-bold text-kuota-deepnavy">
                <ShieldCheck className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                Comisión {kuotaFeeText} sin costo de POS
              </span>
              <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/90 border border-kuota-deepnavy text-xs sm:text-sm font-bold text-kuota-deepnavy">
                <Clock className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                Liquidación en {PRICING_CONFIG.settlementBusinessHours} horas hábiles
              </span>
            </div>

            {/* CTAs: Primario y Secundario */}
            <div className="w-full sm:w-auto flex flex-col sm:flex-row items-stretch sm:items-center gap-3.5">
              <ScheduleCallButton
                section="hero"
                label="Agenda una call"
                size="lg"
              />

              <a
                href="#producto"
                onClick={() =>
                  trackSupplierEvent('cta_secondary_click', {
                    section: 'hero',
                    target: '#producto',
                  })
                }
                className="inline-flex items-center justify-center gap-2 px-6 py-4 rounded-2xl bg-white hover:bg-kuota-palesky text-kuota-deepnavy font-extrabold text-base sm:text-lg border-2 border-kuota-deepnavy shadow-navy-hard transition-all"
              >
                <span>Ver cómo funciona</span>
                <ArrowRight className="w-5 h-5 text-kuota-dodger" aria-hidden="true" />
              </a>
            </div>
          </div>

          {/* Columna Derecha (Debajo en móvil): Imagen IMG-01 + Tarjeta flotante "POS 3,5% → Kuota 2%" */}
          <div className="lg:col-span-6 relative">
            <div className="relative rounded-3xl overflow-hidden border-2 border-kuota-deepnavy bg-white shadow-navy-hard aspect-[4/3] w-full">
              <img
                src="/images/hero-negocio-kuota.jpg"
                alt="Dueño de negocio cobrando en su mostrador con Kuota QR y celular"
                className="w-full h-full object-cover"
              />
            </div>

            {/* Tarjeta flotante sobre la imagen (se mantiene en móvil y desktop — PRD §5.2) */}
            <div className="absolute -bottom-5 left-4 right-4 sm:left-auto sm:right-6 sm:w-auto z-20 flex justify-center">
              <div className="rounded-2xl bg-kuota-deepnavy text-white border-2 border-kuota-deepnavy px-5 py-3.5 shadow-navy-hard-lg flex items-center gap-3">
                <span className="text-xs uppercase font-extrabold tracking-wider text-kuota-palesky">
                  Comisión
                </span>
                <div className="flex items-center gap-2 text-base sm:text-xl font-extrabold">
                  <span className="line-through text-kuota-palesky/75">
                    POS {posFeeText}
                  </span>
                  <span className="text-kuota-dodger" aria-hidden="true">
                    →
                  </span>
                  <span className="px-2.5 py-0.5 rounded-lg bg-kuota-dodger text-white">
                    Kuota {kuotaFeeText}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};


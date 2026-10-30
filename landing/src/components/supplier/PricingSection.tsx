import React from 'react';
import { Sparkles, Check, TrendingUp } from 'lucide-react';
import { PRICING_PLANS } from '../../config/pricingConfig';
import { ScheduleCallButton } from './ScheduleCallButton';

/**
 * PRD §5.8: Precios
 * - Tres cards: Basic, Pro (destacada visualmente) y Open.
 * - Todos los precios de suscripción se muestran "+ IGV".
 * - Card Open incluye una mini escalera de niveles (IMG-21, 600×200) con la leyenda:
 *   "Sube de nivel mientras vendes: más ventas = menos % por transacción. Los límites de transacción dependen de tu nivel."
 * - Cada card lleva "Agenda una call".
 */
export const PricingSection: React.FC = () => {
  return (
    <section
      id="precios"
      aria-labelledby="supplier-pricing-heading"
      className="py-16 sm:py-20 border-b-2 border-kuota-deepnavy bg-white/45"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-12">
          <span className="inline-block px-3.5 py-1 rounded-full bg-white border-2 border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-3">
            Planes transparentes
          </span>
          <h2
            id="supplier-pricing-heading"
            className="text-3xl sm:text-4xl font-extrabold text-kuota-deepnavy tracking-tight mb-3"
          >
            Elige el plan ideal para tu negocio
          </h2>
          <p className="text-base sm:text-lg font-semibold text-kuota-crayola">
            Sin costos ocultos por equipo POS. Todos los precios de suscripción se muestran + IGV.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-7 items-stretch">
          {PRICING_PLANS.map((plan) => {
            const isPro = Boolean(plan.highlighted);
            return (
              <article
                key={plan.id}
                className={`relative rounded-3xl border-2 border-kuota-deepnavy p-6 sm:p-8 flex flex-col justify-between transition-transform ${
                  isPro
                    ? 'bg-kuota-deepnavy text-white shadow-navy-hard-lg lg:-translate-y-2'
                    : 'bg-white text-kuota-deepnavy shadow-navy-hard'
                }`}
              >
                {isPro && (
                  <div className="absolute -top-4 left-1/2 -translate-x-1/2 px-4 py-1 rounded-full bg-kuota-dodger text-white border-2 border-kuota-deepnavy text-xs font-extrabold uppercase tracking-wider flex items-center gap-1.5 shadow-sm">
                    <Sparkles className="w-3.5 h-3.5 text-kuota-palesky" aria-hidden="true" />
                    Más elegido · Recomendado
                  </div>
                )}

                <div>
                  <div className="flex items-center justify-between gap-2 mb-4">
                    <h3
                      className={`text-2xl font-extrabold ${
                        isPro ? 'text-white' : 'text-kuota-deepnavy'
                      }`}
                    >
                      {plan.name}
                    </h3>
                    <span
                      className={`px-3 py-1 rounded-xl text-xs font-extrabold border ${
                        isPro
                          ? 'bg-kuota-dodger text-white border-kuota-palesky/40'
                          : 'bg-kuota-palesky text-kuota-deepnavy border-kuota-deepnavy'
                      }`}
                    >
                      {plan.commissionLabel}
                    </span>
                  </div>

                  {/* Precio de suscripción */}
                  <div className="mb-6 pb-5 border-b border-current/15">
                    <span
                      className={`block text-xs font-extrabold uppercase tracking-wider mb-1 ${
                        isPro ? 'text-kuota-palesky' : 'text-kuota-crayola'
                      }`}
                    >
                      Suscripción mensual
                    </span>
                    <div className="text-3xl sm:text-4xl font-extrabold tracking-tight">
                      {plan.priceLabel}
                    </div>
                  </div>

                  {/* Detalles de la tabla PRD §5.8 */}
                  <dl className="space-y-3.5 text-sm mb-6">
                    <div className="flex items-start justify-between gap-3">
                      <dt
                        className={`font-bold ${
                          isPro ? 'text-kuota-palesky' : 'text-kuota-crayola'
                        }`}
                      >
                        Comisión
                      </dt>
                      <dd className="font-extrabold text-right">
                        {plan.commissionLabel}
                      </dd>
                    </div>

                    <div className="flex items-start justify-between gap-3">
                      <dt
                        className={`font-bold ${
                          isPro ? 'text-kuota-palesky' : 'text-kuota-crayola'
                        }`}
                      >
                        Monto mínimo
                      </dt>
                      <dd className="font-extrabold text-right">
                        {plan.minAmountLabel}
                      </dd>
                    </div>

                    <div className="flex items-start justify-between gap-3">
                      <dt
                        className={`font-bold ${
                          isPro ? 'text-kuota-palesky' : 'text-kuota-crayola'
                        }`}
                      >
                        Límite
                      </dt>
                      <dd className="font-extrabold text-right">
                        {plan.limitLabel}
                      </dd>
                    </div>

                    <div className="flex items-start justify-between gap-3 pt-2 border-t border-current/15">
                      <dt
                        className={`font-bold ${
                          isPro ? 'text-kuota-palesky' : 'text-kuota-crayola'
                        }`}
                      >
                        Extras
                      </dt>
                      <dd className="font-extrabold text-right flex items-center gap-1">
                        {plan.extrasLabel !== '–' && (
                          <Check
                            className="w-4 h-4 text-kuota-dodger shrink-0"
                            aria-hidden="true"
                          />
                        )}
                        <span>{plan.extrasLabel}</span>
                      </dd>
                    </div>
                  </dl>

                  {/* Card Open: Mini escalera de niveles IMG-21 (600×200) + leyenda oficial (§5.8) */}
                  {plan.includesOpenLadder && (
                    <div
                      role="img"
                      aria-label="IMG-21: Mini escalera de niveles del plan Open (600×200)"
                      className="rounded-2xl border-2 border-kuota-deepnavy bg-kuota-palesky/65 p-3.5 mb-6 text-kuota-deepnavy"
                    >
                      <div className="flex items-center justify-between text-[10px] font-extrabold uppercase mb-2">
                        <span className="px-2.5 py-0.5 rounded-full bg-kuota-deepnavy text-white">
                          Escalera de Niveles Plan Open
                        </span>
                        <span className="px-2.5 py-0.5 rounded-full bg-white border border-kuota-deepnavy text-kuota-deepnavy">
                          Bonificación Progresiva
                        </span>
                      </div>

                      {/* Mini escalera visual ascendente */}
                      <div className="h-14 grid grid-cols-5 gap-1.5 items-end px-2 py-1.5 bg-white/90 rounded-xl border border-kuota-deepnavy mb-2.5">
                        {[35, 50, 65, 82, 100].map((h, i) => (
                          <div
                            key={h}
                            style={{ height: `${h}%` }}
                            className="rounded-t bg-kuota-dodger border border-kuota-deepnavy flex items-end justify-center"
                          >
                            <span className="text-[9px] font-extrabold text-white leading-none pb-0.5">
                              N{i + 1}
                            </span>
                          </div>
                        ))}
                      </div>

                      <p className="text-xs font-extrabold text-kuota-deepnavy leading-snug flex items-start gap-1.5">
                        <TrendingUp
                          className="w-4 h-4 text-kuota-dodger shrink-0 mt-0.5"
                          aria-hidden="true"
                        />
                        <span>
                          Sube de nivel mientras vendes: más ventas = menos % por transacción. Los límites de transacción dependen de tu nivel.
                        </span>
                      </p>
                    </div>
                  )}
                </div>

                <ScheduleCallButton
                  section={`precios_${plan.id}`}
                  label="Agenda una call"
                  size="md"
                  className="w-full"
                />
              </article>
            );
          })}
        </div>
      </div>
    </section>
  );
};


import React from 'react';
import { Sparkles, Check, TrendingUp } from 'lucide-react';
import { PRICING_PLANS } from '../../config/pricingConfig';
import { ScheduleCallButton } from './ScheduleCallButton';

/**
 * PRD §5.8 + Master Context:
 * - Tres cards: Basic, Pro (destacada visualmente) y Open.
 * - Todos los precios de suscripción se muestran "+ IGV".
 * - Mini escalera de niveles del plan Open integrada con estilo fintech limpio.
 */
export const PricingSection: React.FC = () => {
  return (
    <section
      id="precios"
      aria-labelledby="supplier-pricing-heading"
      className="py-16 sm:py-24 border-b border-slate-200/80 bg-white"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <div className="text-center max-w-2xl mx-auto">
          <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full bg-sky-50 border border-sky-200/80 text-xs font-bold text-[#0391FD] uppercase tracking-wider mb-3 shadow-xs">
            Planes transparentes
          </span>
          <h2
            id="supplier-pricing-heading"
            className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-[#0A192F] tracking-tight mb-3"
          >
            Elige el plan ideal para tu negocio
          </h2>
          <p className="text-base sm:text-lg font-normal text-slate-600">
            Sin costos ocultos por equipo POS ni membresías forzadas. Todos los precios de suscripción se muestran + IGV.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-7 items-stretch">
          {PRICING_PLANS.map((plan) => {
            const isPro = Boolean(plan.highlighted);
            return (
              <article
                key={plan.id}
                className={`relative rounded-[28px] p-6 sm:p-8 flex flex-col justify-between transition-all duration-200 ${
                  isPro
                    ? 'bg-[#0A192F] text-white border border-white/10 shadow-2xl lg:-translate-y-2'
                    : 'bg-white text-slate-800 border border-slate-200/90 shadow-md hover:shadow-xl'
                }`}
              >
                {isPro && (
                  <div className="absolute -top-3.5 left-1/2 -translate-x-1/2 px-4 py-1 rounded-full bg-[#0391FD] text-white text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 shadow-md">
                    <Sparkles className="w-3.5 h-3.5 text-white" aria-hidden="true" />
                    Más elegido · Recomendado
                  </div>
                )}

                <div>
                  <div className="flex items-center justify-between gap-2 mb-4">
                    <h3
                      className={`text-2xl font-extrabold ${
                        isPro ? 'text-white' : 'text-[#0A192F]'
                      }`}
                    >
                      {plan.name}
                    </h3>
                    <span
                      className={`px-3 py-1 rounded-full text-xs font-bold ${
                        isPro
                          ? 'bg-[#0391FD] text-white shadow-xs'
                          : 'bg-sky-50 text-[#0391FD] border border-sky-200/80'
                      }`}
                    >
                      {plan.commissionLabel}
                    </span>
                  </div>

                  {/* Precio de suscripción */}
                  <div className="mb-6 pb-5 border-b border-current/15">
                    <span
                      className={`block text-xs font-bold uppercase tracking-wider mb-1 ${
                        isPro ? 'text-slate-400' : 'text-slate-500'
                      }`}
                    >
                      Suscripción mensual
                    </span>
                    <div className="text-3xl sm:text-4xl font-extrabold tracking-tight">
                      {plan.priceLabel}
                    </div>
                  </div>

                  {/* Detalles del plan */}
                  <dl className="space-y-3.5 text-sm mb-6">
                    <div className="flex items-start justify-between gap-3">
                      <dt
                        className={`font-medium ${
                          isPro ? 'text-slate-300' : 'text-slate-500'
                        }`}
                      >
                        Comisión
                      </dt>
                      <dd className="font-bold text-right">
                        {plan.commissionLabel}
                      </dd>
                    </div>

                    <div className="flex items-start justify-between gap-3">
                      <dt
                        className={`font-medium ${
                          isPro ? 'text-slate-300' : 'text-slate-500'
                        }`}
                      >
                        Monto mínimo
                      </dt>
                      <dd className="font-bold text-right">
                        {plan.minAmountLabel}
                      </dd>
                    </div>

                    <div className="flex items-start justify-between gap-3">
                      <dt
                        className={`font-medium ${
                          isPro ? 'text-slate-300' : 'text-slate-500'
                        }`}
                      >
                        Límite
                      </dt>
                      <dd className="font-bold text-right">
                        {plan.limitLabel}
                      </dd>
                    </div>

                    <div className="flex items-start justify-between gap-3 pt-2 border-t border-current/15">
                      <dt
                        className={`font-medium ${
                          isPro ? 'text-slate-300' : 'text-slate-500'
                        }`}
                      >
                        Extras
                      </dt>
                      <dd className="font-bold text-right flex items-center gap-1">
                        {plan.extrasLabel !== '–' && (
                          <Check
                            className="w-4 h-4 text-[#10B981] shrink-0"
                            aria-hidden="true"
                          />
                        )}
                        <span>{plan.extrasLabel}</span>
                      </dd>
                    </div>
                  </dl>

                  {/* Card Open: Mini escalera de niveles */}
                  {plan.includesOpenLadder && (
                    <div
                      role="img"
                      aria-label="Mini escalera de niveles del plan Open"
                      className="rounded-2xl border border-slate-200/90 bg-slate-50 p-4 mb-6 text-slate-800"
                    >
                      <div className="flex items-center justify-between text-[10px] font-bold uppercase mb-2.5">
                        <span className="px-2.5 py-0.5 rounded-full bg-[#0A192F] text-white">
                          Escalera Plan Open
                        </span>
                        <span className="px-2.5 py-0.5 rounded-full bg-emerald-50 text-emerald-800 border border-emerald-200">
                          Bonificación Progresiva
                        </span>
                      </div>

                      {/* Mini escalera visual ascendente */}
                      <div className="h-14 grid grid-cols-5 gap-1.5 items-end px-2 py-1.5 bg-white rounded-xl border border-slate-200/80 mb-2.5">
                        {[35, 50, 65, 82, 100].map((h, i) => (
                          <div
                            key={h}
                            style={{ height: `${h}%` }}
                            className="rounded-t bg-[#0391FD] flex items-end justify-center"
                          >
                            <span className="text-[9px] font-bold text-white leading-none pb-0.5">
                              N{i + 1}
                            </span>
                          </div>
                        ))}
                      </div>

                      <p className="text-xs font-medium text-slate-600 leading-snug flex items-start gap-1.5">
                        <TrendingUp
                          className="w-4 h-4 text-[#0391FD] shrink-0 mt-0.5"
                          aria-hidden="true"
                        />
                        <span>
                          Sube de nivel mientras vendes: más volumen = menor porcentaje por transacción.
                        </span>
                      </p>
                    </div>
                  )}
                </div>

                <ScheduleCallButton
                  section={`precios_${plan.id}`}
                  label="Agenda una llamada"
                  size="md"
                  variant={isPro ? 'primary' : 'navy'}
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

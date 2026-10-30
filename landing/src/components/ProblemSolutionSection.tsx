import React from 'react';
import { XCircle, CheckCircle2, AlertTriangle, Zap } from 'lucide-react';
import { ScrollReveal, TiltCard } from '../utils/motionSystem';

/**
 * PRD §6.2 + FASE 3.C: Problema y solución con entrada escalonada y profundidad 3D.
 */
export const ProblemSolutionSection: React.FC = () => {
  const problemPoints = [
    'Intereses bancarios desmedidos y cobros sorpresa',
    'Fechas de pago inflexibles que ahogan tu rotación',
    'Presión y cobranzas informales que amenazan tu tranquilidad',
  ];

  const kuotaPoints = [
    'Interés transparente del ~5% flat sin letra chica',
    'Fechas calibradas al ritmo de venta de tu mercadería',
    'Aprobación inmediata sin papeleo excesivo',
  ];

  return (
    <section
      id="problema-solucion"
      aria-labelledby="problem-solution-heading"
      className="py-16 sm:py-24 border-b border-slate-200/80 bg-slate-50/50"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <h2 id="problem-solution-heading" className="sr-only">
          El problema tradicional y la solución con Kuota
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 lg:gap-8 items-stretch">
          {/* Tarjeta 1: El problema tradicional */}
          <ScrollReveal delayMs={0} direction="left" className="h-full">
            <TiltCard maxTiltDeg={2} className="h-full">
              <article className="h-full rounded-[28px] bg-white border border-slate-200/90 p-6 sm:p-8 flex flex-col justify-between shadow-sm">
                <div>
                  <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full bg-rose-50 border border-rose-200 text-rose-800 text-xs font-bold uppercase tracking-wider mb-5">
                    <AlertTriangle className="w-3.5 h-3.5 text-rose-600" aria-hidden="true" />
                    El problema tradicional
                  </div>

                  <h3 className="text-2xl sm:text-3xl font-extrabold text-[#0A192F] leading-tight mb-6">
                    Necesitas reabastecer tu negocio, pero te quedaste sin liquidez
                  </h3>

                  <ul className="space-y-4">
                    {problemPoints.map((point) => (
                      <li
                        key={point}
                        className="flex items-start gap-3 text-sm sm:text-base font-medium text-slate-700"
                      >
                        <XCircle
                          className="w-5 h-5 text-rose-500 shrink-0 mt-0.5"
                          aria-hidden="true"
                        />
                        <span>{point}</span>
                      </li>
                    ))}
                  </ul>
                </div>

                <div className="mt-8 pt-4 border-t border-slate-200/80 text-xs font-bold text-slate-500 uppercase tracking-wide">
                  Préstamos informales o banca tradicional lenta
                </div>
              </article>
            </TiltCard>
          </ScrollReveal>

          {/* Tarjeta 2: Con Kuota */}
          <ScrollReveal delayMs={120} direction="right" className="h-full">
            <TiltCard maxTiltDeg={2.5} className="h-full">
              <article className="h-full rounded-[28px] bg-[#0A192F] text-white border border-white/10 p-6 sm:p-8 flex flex-col justify-between shadow-xl relative overflow-hidden">
                {/* Resplandor suave */}
                <div className="absolute top-0 right-0 w-48 h-48 bg-[#0391FD]/15 rounded-full blur-3xl pointer-events-none" />

                <div className="relative z-10">
                  <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full bg-sky-500/15 border border-sky-400/30 text-[#38BDF8] text-xs font-bold uppercase tracking-wider mb-5">
                    <Zap className="w-3.5 h-3.5 text-[#38BDF8]" aria-hidden="true" />
                    Con Kuota
                  </div>

                  <h3 className="text-2xl sm:text-3xl font-extrabold text-white leading-tight mb-6">
                    Financia tu mercadería en 3 clics y paga a kuotas
                  </h3>

                  <ul className="space-y-3.5">
                    {kuotaPoints.map((point) => (
                      <li
                        key={point}
                        className="flex items-start gap-3 text-sm sm:text-base font-bold text-white bg-white/5 border border-white/10 rounded-2xl p-4 transition-transform duration-150 hover:translate-x-1"
                      >
                        <CheckCircle2
                          className="w-5 h-5 text-[#38BDF8] shrink-0 mt-0.5"
                          aria-hidden="true"
                        />
                        <span>{point}</span>
                      </li>
                    ))}
                  </ul>
                </div>

                <div className="relative z-10 mt-8 pt-4 border-t border-white/10 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 text-xs font-medium text-slate-300">
                  <span>Sin garantías inmobiliarias · Sin trabas innecesarias</span>
                  <span className="px-2.5 py-1 rounded-full bg-emerald-500/20 text-[#10B981] font-bold border border-emerald-500/30">
                    100% digital
                  </span>
                </div>
              </article>
            </TiltCard>
          </ScrollReveal>
        </div>
      </div>
    </section>
  );
};

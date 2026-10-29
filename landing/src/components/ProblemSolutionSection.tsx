import React from 'react';
import { XCircle, CheckCircle2, AlertTriangle, Zap } from 'lucide-react';

/**
 * PRD §6.2: Problema y solución
 * - Dos tarjetas lado a lado (apiladas en móvil).
 * - Tarjeta "El problema" (tono apagado, borde Deep Navy):
 *   - Título: Necesitas reabastecer tu negocio, pero te quedaste sin caja
 *   - Puntos: intereses altísimos; fechas de pago inflexibles; te atormentan hasta que pagues.
 * - Tarjeta "Con Kuota" (resaltada en Dodger Blue):
 *   - Título: Con Kuota, financia tu compra en 3 clics
 *   - Puntos: 5% de interés; sin papeleo innecesario.
 */
export const ProblemSolutionSection: React.FC = () => {
  const problemPoints = [
    'Intereses altísimos',
    'Fechas de pago inflexibles',
    'Te atormentan hasta que pagues',
  ];

  const kuotaPoints = [
    '5% de interés',
    'Sin papeleo innecesario',
  ];

  return (
    <section
      id="problema-solucion"
      aria-labelledby="problem-solution-heading"
      className="py-14 sm:py-20 border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <h2 id="problem-solution-heading" className="sr-only">
          El problema tradicional y la solución con Kuota
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 lg:gap-8 items-stretch">
          {/* Tarjeta 1: El problema (tono apagado, borde Deep Navy) */}
          <article className="rounded-3xl bg-white/60 border-2 border-kuota-deepnavy p-6 sm:p-8 flex flex-col justify-between shadow-navy-hard">
            <div>
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-kuota-palesky border border-kuota-deepnavy text-kuota-deepnavy text-xs font-extrabold uppercase tracking-wider mb-4">
                <AlertTriangle className="w-3.5 h-3.5" aria-hidden="true" />
                El problema
              </div>

              <h3 className="text-2xl sm:text-3xl font-extrabold text-kuota-deepnavy leading-tight mb-6">
                Necesitas reabastecer tu negocio, pero te quedaste sin caja
              </h3>

              <ul className="space-y-4">
                {problemPoints.map((point) => (
                  <li
                    key={point}
                    className="flex items-start gap-3 text-base sm:text-lg font-semibold text-kuota-deepnavy/85"
                  >
                    <XCircle
                      className="w-6 h-6 text-kuota-deepnavy shrink-0 mt-0.5"
                      aria-hidden="true"
                    />
                    <span>{point}</span>
                  </li>
                ))}
              </ul>
            </div>

            <div className="mt-8 pt-4 border-t border-kuota-deepnavy/20 text-xs font-bold text-kuota-deepnavy/75 uppercase tracking-wide">
              Préstamos informales o banca tradicional rígida
            </div>
          </article>

          {/* Tarjeta 2: Con Kuota (resaltada en Dodger Blue) */}
          <article className="rounded-3xl bg-kuota-dodger text-white border-2 border-kuota-deepnavy p-6 sm:p-8 flex flex-col justify-between shadow-navy-hard-lg relative overflow-hidden">
            <div>
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-kuota-deepnavy text-white text-xs font-extrabold uppercase tracking-wider mb-4">
                <Zap className="w-3.5 h-3.5 text-kuota-palesky" aria-hidden="true" />
                Con Kuota
              </div>

              <h3 className="text-2xl sm:text-3xl font-extrabold text-white leading-tight mb-6">
                Con Kuota, financia tu compra en 3 clics
              </h3>

              <ul className="space-y-4">
                {kuotaPoints.map((point) => (
                  <li
                    key={point}
                    className="flex items-start gap-3 text-lg sm:text-xl font-extrabold text-white bg-kuota-deepnavy/25 border border-white/30 rounded-2xl px-4 py-3.5"
                  >
                    <CheckCircle2
                      className="w-6 h-6 text-kuota-palesky shrink-0 mt-0.5"
                      aria-hidden="true"
                    />
                    <span>{point}</span>
                  </li>
                ))}
              </ul>
            </div>

            <div className="mt-8 pt-4 border-t border-white/30 flex items-center justify-between text-xs sm:text-sm font-extrabold text-white">
              <span>Sin garantía inmobiliaria · Sin historial Infocorp</span>
              <span className="px-2.5 py-1 rounded-md bg-kuota-deepnavy text-kuota-palesky">
                100% digital
              </span>
            </div>
          </article>
        </div>
      </div>
    </section>
  );
};

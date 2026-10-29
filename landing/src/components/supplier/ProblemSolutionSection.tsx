import React from 'react';
import { BarChart3, CalendarCheck2, XCircle, CheckCircle2 } from 'lucide-react';
import { ImagePlaceholder } from '../ImagePlaceholder';
import { PRICING_CONFIG, formatPercentEs } from '../../config/pricingConfig';

/**
 * PRD §5.3: Problema + solución
 * - Dos cards lado a lado (apiladas en móvil). Cada card muestra el problema arriba y la solución de Kuota abajo.
 * - Card 1 – Sin comisiones excesivas:
 *   - Problema: "Con un POS pierdes 3,5% de comisión en promedio" (3,5% tachado).
 *   - Solución: "Con Kuota, 2% sin costos por el POS" (2% resaltado).
 *   - Gráfico (IMG-02): dos barras horizontales comparando 3,5% frente a 2% (800×400).
 * - Card 2 – Sin fiado, sin cobranza:
 *   - Problema: "Vendes fiado, no te pagan y tienes una tarea más de cobranza."
 *   - Solución: "Con Kuota, tus clientes compran en kuotas y tú tienes 0 riesgo de impago para tu negocio."
 *   - Ilustración (IMG-03): cuaderno de fiados tachado junto a un calendario de kuotas pagadas (800×400).
 */
export const ProblemSolutionSection: React.FC = () => {
  const posFeeText = formatPercentEs(PRICING_CONFIG.posAverageFeePercent);
  const kuotaFeeText = formatPercentEs(PRICING_CONFIG.kuotaPaymentsFeePercent);

  return (
    <section
      id="problema-solucion"
      aria-labelledby="supplier-problem-solution-heading"
      className="py-16 sm:py-20 border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-10">
          <span className="inline-block px-3.5 py-1 rounded-full bg-white border-2 border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-3">
            Ahorro y tranquilidad
          </span>
          <h2
            id="supplier-problem-solution-heading"
            className="text-3xl sm:text-4xl font-extrabold text-kuota-deepnavy tracking-tight"
          >
            Más margen en cada venta, cero dolores de cobranza
          </h2>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-stretch">
          {/* Card 1 – Sin comisiones excesivas */}
          <article className="rounded-3xl bg-white border-2 border-kuota-deepnavy p-6 sm:p-8 shadow-navy-hard-lg flex flex-col justify-between">
            <div>
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-kuota-palesky border border-kuota-deepnavy text-kuota-deepnavy text-xs font-extrabold uppercase tracking-wider mb-4">
                <BarChart3 className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                Sin comisiones excesivas
              </div>

              {/* Problema arriba */}
              <div className="rounded-2xl bg-kuota-palesky/45 border-2 border-kuota-deepnavy/30 p-4 mb-4 flex items-start gap-3">
                <XCircle className="w-6 h-6 text-kuota-deepnavy shrink-0 mt-0.5" aria-hidden="true" />
                <div>
                  <span className="block text-xs font-extrabold uppercase tracking-wider text-kuota-deepnavy/70 mb-0.5">
                    Problema
                  </span>
                  <p className="text-base sm:text-lg font-bold text-kuota-deepnavy">
                    Con un POS pierdes{' '}
                    <span className="line-through decoration-2 font-extrabold text-kuota-deepnavy">
                      {posFeeText}
                    </span>{' '}
                    de comisión en promedio
                  </p>
                </div>
              </div>

              {/* Solución abajo */}
              <div className="rounded-2xl bg-kuota-dodger text-white border-2 border-kuota-deepnavy p-4 mb-6 flex items-start gap-3 shadow-navy-hard">
                <CheckCircle2 className="w-6 h-6 text-kuota-palesky shrink-0 mt-0.5" aria-hidden="true" />
                <div>
                  <span className="block text-xs font-extrabold uppercase tracking-wider text-kuota-palesky mb-0.5">
                    Solución Kuota
                  </span>
                  <p className="text-lg sm:text-xl font-extrabold text-white">
                    Con Kuota,{' '}
                    <span className="px-2 py-0.5 rounded-lg bg-kuota-deepnavy text-kuota-palesky">
                      {kuotaFeeText}
                    </span>{' '}
                    sin costos por el POS
                  </p>
                </div>
              </div>
            </div>

            {/* Gráfico (IMG-02): dos barras horizontales comparando 3,5% frente a 2% */}
            <ImagePlaceholder
              id="IMG-02"
              label={`Gráfico de barras horizontales comparando ${posFeeText} frente a ${kuotaFeeText}`}
              dimensionsLabel="800×400"
              aspectClass="aspect-[2/1] w-full"
            >
              <div className="w-full max-w-md space-y-3 px-2 text-left">
                <div>
                  <div className="flex justify-between text-xs font-extrabold text-kuota-deepnavy mb-1">
                    <span>POS Tradicional (Promedio)</span>
                    <span className="line-through">{posFeeText}</span>
                  </div>
                  <div className="w-full h-5 rounded-lg bg-kuota-palesky border border-kuota-deepnavy overflow-hidden">
                    <div className="h-full bg-kuota-deepnavy/65 w-full" />
                  </div>
                </div>
                <div>
                  <div className="flex justify-between text-xs font-extrabold text-kuota-deepnavy mb-1">
                    <span>Kuota Payments (QR / Link)</span>
                    <span className="text-kuota-dodger">{kuotaFeeText}</span>
                  </div>
                  <div className="w-full h-5 rounded-lg bg-kuota-palesky border border-kuota-deepnavy overflow-hidden">
                    <div
                      className="h-full bg-kuota-dodger"
                      style={{
                        width: `${Math.round(
                          (PRICING_CONFIG.kuotaPaymentsFeePercent /
                            PRICING_CONFIG.posAverageFeePercent) *
                            100
                        )}%`,
                      }}
                    />
                  </div>
                </div>
              </div>
            </ImagePlaceholder>
          </article>

          {/* Card 2 – Sin fiado, sin cobranza */}
          <article className="rounded-3xl bg-white border-2 border-kuota-deepnavy p-6 sm:p-8 shadow-navy-hard-lg flex flex-col justify-between">
            <div>
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-kuota-palesky border border-kuota-deepnavy text-kuota-deepnavy text-xs font-extrabold uppercase tracking-wider mb-4">
                <CalendarCheck2 className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                Sin fiado, sin cobranza
              </div>

              {/* Problema arriba */}
              <div className="rounded-2xl bg-kuota-palesky/45 border-2 border-kuota-deepnavy/30 p-4 mb-4 flex items-start gap-3">
                <XCircle className="w-6 h-6 text-kuota-deepnavy shrink-0 mt-0.5" aria-hidden="true" />
                <div>
                  <span className="block text-xs font-extrabold uppercase tracking-wider text-kuota-deepnavy/70 mb-0.5">
                    Problema
                  </span>
                  <p className="text-base sm:text-lg font-bold text-kuota-deepnavy">
                    Vendes fiado, no te pagan y tienes una tarea más de cobranza.
                  </p>
                </div>
              </div>

              {/* Solución abajo */}
              <div className="rounded-2xl bg-kuota-dodger text-white border-2 border-kuota-deepnavy p-4 mb-6 flex items-start gap-3 shadow-navy-hard">
                <CheckCircle2 className="w-6 h-6 text-kuota-palesky shrink-0 mt-0.5" aria-hidden="true" />
                <div>
                  <span className="block text-xs font-extrabold uppercase tracking-wider text-kuota-palesky mb-0.5">
                    Solución Kuota
                  </span>
                  <p className="text-lg sm:text-xl font-extrabold text-white">
                    Con Kuota, tus clientes compran en kuotas y tú tienes{' '}
                    <span className="px-2 py-0.5 rounded-lg bg-kuota-deepnavy text-kuota-palesky">
                      {PRICING_CONFIG.supplierDefaultRisk} riesgo de impago
                    </span>{' '}
                    para tu negocio.
                  </p>
                </div>
              </div>
            </div>

            {/* Ilustración (IMG-03): cuaderno de fiados tachado junto a un calendario de kuotas pagadas */}
            <ImagePlaceholder
              id="IMG-03"
              label="Cuaderno de fiados tachado junto a un calendario de kuotas pagadas"
              dimensionsLabel="800×400"
              aspectClass="aspect-[2/1] w-full"
            >
              <div className="grid grid-cols-2 gap-3 w-full max-w-md px-2">
                <div className="rounded-xl bg-kuota-palesky/60 border border-kuota-deepnavy p-2.5 text-center">
                  <span className="block text-xs font-extrabold line-through text-kuota-deepnavy/75">
                    Cuaderno de fiados
                  </span>
                  <span className="text-[11px] font-bold text-kuota-deepnavy">
                    Eliminado ✕
                  </span>
                </div>
                <div className="rounded-xl bg-kuota-deepnavy text-white border border-kuota-deepnavy p-2.5 text-center">
                  <span className="block text-xs font-extrabold text-kuota-palesky">
                    Calendario Kuota
                  </span>
                  <span className="text-[11px] font-bold text-white">
                    Liquidado en 48h ✓
                  </span>
                </div>
              </div>
            </ImagePlaceholder>
          </article>
        </div>
      </div>
    </section>
  );
};


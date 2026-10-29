import React from 'react';
import {
  BarChart3,
  CalendarCheck2,
  XCircle,
  CheckCircle2,
  TrendingDown,
  ShieldCheck,
  Clock,
  Sparkles,
} from 'lucide-react';
import { PRICING_CONFIG, formatPercentEs } from '../../config/pricingConfig';

/**
 * PRD §5.3: Problema + solución
 * - Dos cards lado a lado (apiladas en móvil). Cada card muestra el problema arriba y la solución de Kuota abajo.
 * - Card 1 – Sin comisiones excesivas:
 *   - Problema: "Con un POS pierdes 3,5% de comisión en promedio" (3,5% tachado).
 *   - Solución: "Con Kuota, 2% sin costos por el POS" (2% resaltado).
 *   - Gráfico comparativo interactivo de alto nivel visual comparando 3,5% frente a 2%.
 * - Card 2 – Sin fiado, sin cobranza:
 *   - Problema: "Vendes fiado, no te pagan y tienes una tarea más de cobranza."
 *   - Solución: "Con Kuota, tus clientes compran en kuotas y tú tienes 0 riesgo de impago para tu negocio."
 *   - Ilustración visual comparativa: cuaderno de fiados eliminado vs calendario de liquidación 48h.
 */
export const ProblemSolutionSection: React.FC = () => {
  const posFeeText = formatPercentEs(PRICING_CONFIG.posAverageFeePercent);
  const kuotaFeeText = formatPercentEs(PRICING_CONFIG.kuotaPaymentsFeePercent);
  const kuotaBarWidthPercent = Math.round(
    (PRICING_CONFIG.kuotaPaymentsFeePercent /
      PRICING_CONFIG.posAverageFeePercent) *
      100
  );
  const relativeSavingsPercent = Math.round(
    ((PRICING_CONFIG.posAverageFeePercent -
      PRICING_CONFIG.kuotaPaymentsFeePercent) /
      PRICING_CONFIG.posAverageFeePercent) *
      100
  );

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

            {/* Gráfico financiero comparativo premium (reemplaza el placeholder IMG-02) */}
            <div
              role="img"
              aria-label={`Gráfico comparativo de comisiones: POS tradicional ${posFeeText} frente a Kuota ${kuotaFeeText}`}
              className="rounded-2xl bg-kuota-palesky/40 border-2 border-kuota-deepnavy p-5 sm:p-6 shadow-sm flex flex-col justify-between space-y-5"
            >
              {/* Encabezado del gráfico */}
              <div className="flex flex-wrap items-center justify-between gap-2 pb-3 border-b border-kuota-deepnavy/15">
                <div>
                  <span className="text-[11px] font-extrabold uppercase tracking-wider text-kuota-crayola block">
                    Comparativa de comisión por venta
                  </span>
                  <h3 className="text-sm sm:text-base font-extrabold text-kuota-deepnavy">
                    POS Tradicional ({posFeeText}) vs. Kuota ({kuotaFeeText})
                  </h3>
                </div>
                <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-kuota-deepnavy text-white text-xs font-extrabold">
                  <TrendingDown className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                  -{relativeSavingsPercent}% costo de comisión
                </span>
              </div>

              {/* Barras horizontales comparativas con desglose */}
              <div className="space-y-4">
                {/* Barra 1: POS Tradicional */}
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between text-xs sm:text-sm">
                    <span className="font-bold text-kuota-deepnavy/85">
                      POS Tradicional (Promedio)
                    </span>
                    <div className="flex items-center gap-2">
                      <span className="text-[11px] font-semibold text-kuota-deepnavy/70">
                        S/ 35 por cada S/ 1,000
                      </span>
                      <span className="px-2 py-0.5 rounded-md bg-kuota-deepnavy/15 text-kuota-deepnavy font-extrabold line-through tabular-nums">
                        {posFeeText}
                      </span>
                    </div>
                  </div>
                  <div className="relative w-full h-9 rounded-xl bg-white border-2 border-kuota-deepnavy overflow-hidden p-1">
                    <div className="h-full w-full rounded-lg bg-kuota-deepnavy/60 flex items-center justify-between px-3 text-[11px] font-extrabold text-white">
                      <span>Comisión alta + alquiler de equipo</span>
                      <span className="tabular-nums">{posFeeText}</span>
                    </div>
                  </div>
                </div>

                {/* Barra 2: Kuota Payments */}
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between text-xs sm:text-sm">
                    <span className="font-extrabold text-kuota-deepnavy flex items-center gap-1.5">
                      <Sparkles className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                      Kuota Payments (QR / Link)
                    </span>
                    <div className="flex items-center gap-2">
                      <span className="text-[11px] font-bold text-kuota-crayola">
                        S/ 20 por cada S/ 1,000
                      </span>
                      <span className="px-2.5 py-0.5 rounded-md bg-kuota-dodger text-white font-extrabold tabular-nums shadow-sm">
                        {kuotaFeeText}
                      </span>
                    </div>
                  </div>
                  <div className="relative w-full h-9 rounded-xl bg-white border-2 border-kuota-deepnavy overflow-hidden p-1 flex items-center">
                    <div
                      className="h-full rounded-lg bg-kuota-dodger flex items-center justify-between px-3 text-[11px] font-extrabold text-white shadow-sm transition-all duration-500"
                      style={{ width: `${kuotaBarWidthPercent}%` }}
                    >
                      <span className="truncate">Tarifa plana Kuota</span>
                      <span className="tabular-nums">{kuotaFeeText}</span>
                    </div>
                    <div className="flex-1 flex items-center justify-center px-2">
                      <span className="text-[11px] font-extrabold text-kuota-dodger whitespace-nowrap">
                        ✓ Ahorras 1,5% en cada cobro
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Resumen inferior de impacto real */}
              <div className="grid grid-cols-3 gap-2 pt-2 border-t border-kuota-deepnavy/15 text-center">
                <div className="rounded-xl bg-white border border-kuota-deepnavy/25 p-2.5">
                  <span className="block text-[10px] font-bold uppercase text-kuota-deepnavy/70">
                    Equipo POS físico
                  </span>
                  <span className="text-xs sm:text-sm font-extrabold text-kuota-deepnavy">
                    S/ 0 (No requiere)
                  </span>
                </div>
                <div className="rounded-xl bg-white border border-kuota-deepnavy/25 p-2.5">
                  <span className="block text-[10px] font-bold uppercase text-kuota-deepnavy/70">
                    En S/ 20,000 / mes
                  </span>
                  <span className="text-xs sm:text-sm font-extrabold text-kuota-dodger">
                    +S/ 300 ahorro/mes
                  </span>
                </div>
                <div className="rounded-xl bg-kuota-deepnavy text-white border border-kuota-deepnavy p-2.5">
                  <span className="block text-[10px] font-bold uppercase text-kuota-palesky">
                    Ahorro anual
                  </span>
                  <span className="text-xs sm:text-sm font-extrabold text-white">
                    +S/ 3,600 / año
                  </span>
                </div>
              </div>
            </div>
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

            {/* Ilustración comparativa de flujo de cobranza (reemplaza el placeholder IMG-03) */}
            <div
              role="img"
              aria-label="Comparativa entre cuaderno de fiados tradicional y liquidación garantizada en 48 horas con Kuota"
              className="rounded-2xl bg-kuota-palesky/40 border-2 border-kuota-deepnavy p-5 sm:p-6 shadow-sm flex flex-col justify-between space-y-5"
            >
              <div className="flex flex-wrap items-center justify-between gap-2 pb-3 border-b border-kuota-deepnavy/15">
                <div>
                  <span className="text-[11px] font-extrabold uppercase tracking-wider text-kuota-crayola block">
                    Flujo de caja garantizado
                  </span>
                  <h3 className="text-sm sm:text-base font-extrabold text-kuota-deepnavy">
                    Del cuaderno de fiados a liquidación en {PRICING_CONFIG.settlementBusinessHours}h
                  </h3>
                </div>
                <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-kuota-deepnavy text-white text-xs font-extrabold">
                  <ShieldCheck className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                  {PRICING_CONFIG.supplierDefaultRisk} riesgo de impago
                </span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
                {/* Antes: Cuaderno de fiados */}
                <div className="rounded-2xl bg-white/80 border-2 border-kuota-deepnavy/30 p-4 flex flex-col justify-between">
                  <div>
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <span className="text-xs font-extrabold line-through text-kuota-deepnavy/70">
                        Cuaderno de fiados
                      </span>
                      <span className="px-2 py-0.5 rounded bg-kuota-deepnavy/15 text-kuota-deepnavy text-[10px] font-extrabold">
                        Antes ✕
                      </span>
                    </div>
                    <ul className="space-y-1.5 text-xs font-semibold text-kuota-deepnavy/75">
                      <li className="flex items-center gap-1.5">
                        <XCircle className="w-3.5 h-3.5 text-kuota-deepnavy/60 shrink-0" aria-hidden="true" />
                        <span>Capital inmovilizado</span>
                      </li>
                      <li className="flex items-center gap-1.5">
                        <XCircle className="w-3.5 h-3.5 text-kuota-deepnavy/60 shrink-0" aria-hidden="true" />
                        <span>Riesgo de atraso o pérdida</span>
                      </li>
                      <li className="flex items-center gap-1.5">
                        <XCircle className="w-3.5 h-3.5 text-kuota-deepnavy/60 shrink-0" aria-hidden="true" />
                        <span>Perseguir pagos por WhatsApp</span>
                      </li>
                    </ul>
                  </div>
                </div>

                {/* Ahora: Calendario Kuota */}
                <div className="rounded-2xl bg-kuota-deepnavy text-white border-2 border-kuota-deepnavy p-4 flex flex-col justify-between shadow-sm">
                  <div>
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <span className="text-xs font-extrabold text-kuota-palesky">
                        Calendario aKuotas!
                      </span>
                      <span className="px-2 py-0.5 rounded bg-kuota-dodger text-white text-[10px] font-extrabold">
                        Con Kuota ✓
                      </span>
                    </div>
                    <ul className="space-y-1.5 text-xs font-semibold text-kuota-palesky">
                      <li className="flex items-center gap-1.5">
                        <CheckCircle2 className="w-3.5 h-3.5 text-kuota-dodger shrink-0" aria-hidden="true" />
                        <span>Recibes la inicial al instante</span>
                      </li>
                      <li className="flex items-center gap-1.5">
                        <CheckCircle2 className="w-3.5 h-3.5 text-kuota-dodger shrink-0" aria-hidden="true" />
                        <span>Saldo liquidado en {PRICING_CONFIG.settlementBusinessHours}h hábiles</span>
                      </li>
                      <li className="flex items-center gap-1.5">
                        <CheckCircle2 className="w-3.5 h-3.5 text-kuota-dodger shrink-0" aria-hidden="true" />
                        <span>Kuota asume 100% la cobranza</span>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>

              <div className="rounded-xl bg-white border border-kuota-deepnavy/25 px-3.5 py-2.5 flex items-center justify-between gap-2 text-xs font-extrabold text-kuota-deepnavy">
                <span className="flex items-center gap-1.5">
                  <Clock className="w-4 h-4 text-kuota-dodger shrink-0" aria-hidden="true" />
                  Liquidación completa al proveedor
                </span>
                <span className="px-2.5 py-0.5 rounded-lg bg-kuota-palesky text-kuota-deepnavy border border-kuota-deepnavy/20">
                  {PRICING_CONFIG.settlementBusinessHours} horas hábiles
                </span>
              </div>
            </div>
          </article>
        </div>
      </div>
    </section>
  );
};

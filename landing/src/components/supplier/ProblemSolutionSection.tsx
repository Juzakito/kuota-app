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
 * PRD §5.3 + Master Context Calibration:
 * - Card 1: Sin comisiones excesivas (POS 3.5% vs Kuota Payments 1.0%).
 * - Card 2: Sin fiado, sin cobranza (Venta a crédito B2B con 100% cobro anticipado para el proveedor y 0 riesgo).
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
      className="py-16 sm:py-24 border-b border-slate-200/80 bg-slate-50/50"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <div className="text-center max-w-2xl mx-auto">
          <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full bg-sky-50 border border-sky-200/80 text-xs font-bold text-[#0391FD] uppercase tracking-wider mb-3 shadow-xs">
            Ahorro y tranquilidad
          </span>
          <h2
            id="supplier-problem-solution-heading"
            className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-[#0A192F] tracking-tight"
          >
            Más margen en cada venta, cero dolores de cobranza
          </h2>
          <p className="mt-2 text-base sm:text-lg font-normal text-slate-600">
            Diseñado para distribuidores, mayoristas y comercios que mueven mercadería por encima de S/ 199.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-stretch">
          {/* Card 1 – Sin comisiones excesivas */}
          <article className="rounded-[28px] bg-white border border-slate-200/90 p-6 sm:p-8 shadow-xl flex flex-col justify-between">
            <div>
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-sky-50 border border-sky-200 text-[#0391FD] text-xs font-bold uppercase tracking-wider mb-5">
                <BarChart3 className="w-3.5 h-3.5 text-[#0391FD]" aria-hidden="true" />
                Sin comisiones excesivas
              </div>

              {/* Problema arriba */}
              <div className="rounded-2xl bg-rose-50/60 border border-rose-200/70 p-4 mb-4 flex items-start gap-3">
                <XCircle className="w-5 h-5 text-rose-500 shrink-0 mt-0.5" aria-hidden="true" />
                <div>
                  <span className="block text-xs font-bold uppercase tracking-wider text-rose-700 mb-0.5">
                    Problema habitual
                  </span>
                  <p className="text-sm sm:text-base font-bold text-slate-800">
                    Con un POS tradicional pierdes{' '}
                    <span className="line-through decoration-2 text-rose-600">
                      {posFeeText}
                    </span>{' '}
                    de comisión en promedio por cada cobro
                  </p>
                </div>
              </div>

              {/* Solución abajo */}
              <div className="rounded-2xl bg-[#0A192F] text-white border border-white/10 p-4 mb-6 flex items-start gap-3 shadow-md">
                <CheckCircle2 className="w-5 h-5 text-[#38BDF8] shrink-0 mt-0.5" aria-hidden="true" />
                <div>
                  <span className="block text-xs font-bold uppercase tracking-wider text-[#38BDF8] mb-0.5">
                    Solución Kuota Payments
                  </span>
                  <p className="text-sm sm:text-base font-bold text-white">
                    Con Kuota Payments pagas solo{' '}
                    <span className="px-2 py-0.5 rounded-md bg-[#0391FD] text-white font-extrabold">
                      {kuotaFeeText}
                    </span>{' '}
                    sin costo mensual por alquiler de equipo
                  </p>
                </div>
              </div>
            </div>

            {/* Gráfico financiero comparativo */}
            <div
              role="img"
              aria-label={`Gráfico comparativo de comisiones: POS tradicional ${posFeeText} frente a Kuota ${kuotaFeeText}`}
              className="rounded-2xl bg-slate-50 border border-slate-200/90 p-5 sm:p-6 shadow-xs flex flex-col justify-between space-y-5"
            >
              {/* Encabezado del gráfico */}
              <div className="flex flex-wrap items-center justify-between gap-2 pb-3 border-b border-slate-200/80">
                <div>
                  <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500 block">
                    Comparativa de comisión por cobro
                  </span>
                  <h3 className="text-sm sm:text-base font-extrabold text-[#0A192F]">
                    POS Tradicional ({posFeeText}) vs. Kuota ({kuotaFeeText})
                  </h3>
                </div>
                <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-emerald-50 text-emerald-800 border border-emerald-200 text-xs font-bold">
                  <TrendingDown className="w-3.5 h-3.5 text-[#10B981]" aria-hidden="true" />
                  -{relativeSavingsPercent}% de comisión
                </span>
              </div>

              {/* Barras horizontales comparativas */}
              <div className="space-y-4">
                {/* Barra 1: POS Tradicional */}
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between text-xs sm:text-sm font-medium text-slate-700">
                    <span>POS Tradicional (Promedio)</span>
                    <div className="flex items-center gap-2">
                      <span className="text-[11px] text-slate-500">
                        S/ 35 por cada S/ 1,000
                      </span>
                      <span className="px-2 py-0.5 rounded bg-slate-200 text-slate-600 font-bold line-through tabular-nums">
                        {posFeeText}
                      </span>
                    </div>
                  </div>
                  <div className="relative w-full h-8 rounded-xl bg-slate-200 overflow-hidden p-0.5">
                    <div className="h-full w-full rounded-lg bg-slate-400 flex items-center justify-between px-3 text-[11px] font-bold text-white">
                      <span>Comisión alta + costo de equipo</span>
                      <span className="tabular-nums">{posFeeText}</span>
                    </div>
                  </div>
                </div>

                {/* Barra 2: Kuota Payments */}
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between text-xs sm:text-sm font-bold text-[#0A192F]">
                    <span className="flex items-center gap-1.5">
                      <Sparkles className="w-3.5 h-3.5 text-[#0391FD]" aria-hidden="true" />
                      Kuota Payments (QR / Link)
                    </span>
                    <div className="flex items-center gap-2">
                      <span className="text-[11px] text-[#0391FD] font-semibold">
                        Solo S/ 10 por cada S/ 1,000
                      </span>
                      <span className="px-2.5 py-0.5 rounded-full bg-[#0391FD] text-white font-extrabold tabular-nums shadow-xs">
                        {kuotaFeeText}
                      </span>
                    </div>
                  </div>
                  <div className="relative w-full h-8 rounded-xl bg-slate-200 overflow-hidden p-0.5 flex items-center">
                    <div
                      className="h-full rounded-lg bg-[#0391FD] flex items-center justify-between px-3 text-[11px] font-bold text-white shadow-xs transition-all duration-500"
                      style={{ width: `${Math.max(28, kuotaBarWidthPercent)}%` }}
                    >
                      <span className="truncate">Tarifa plana</span>
                      <span className="tabular-nums">{kuotaFeeText}</span>
                    </div>
                    <div className="flex-1 flex items-center justify-center px-2">
                      <span className="text-[11px] font-bold text-[#0391FD] whitespace-nowrap">
                        ✓ Ahorras 2,5% por cobro
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Resumen inferior de impacto real */}
              <div className="grid grid-cols-3 gap-2 pt-2 border-t border-slate-200/80 text-center">
                <div className="rounded-xl bg-white border border-slate-200/80 p-2.5">
                  <span className="block text-[10px] font-semibold uppercase text-slate-500">
                    Equipo POS físico
                  </span>
                  <span className="text-xs sm:text-sm font-extrabold text-[#0A192F]">
                    S/ 0 (No requiere)
                  </span>
                </div>
                <div className="rounded-xl bg-white border border-slate-200/80 p-2.5">
                  <span className="block text-[10px] font-semibold uppercase text-slate-500">
                    En S/ 20,000 / mes
                  </span>
                  <span className="text-xs sm:text-sm font-extrabold text-[#0391FD]">
                    +S/ 500 ahorro/mes
                  </span>
                </div>
                <div className="rounded-xl bg-[#0A192F] text-white border border-white/10 p-2.5">
                  <span className="block text-[10px] font-semibold uppercase text-slate-300">
                    Ahorro anual
                  </span>
                  <span className="text-xs sm:text-sm font-extrabold text-[#10B981]">
                    +S/ 6,000 / año
                  </span>
                </div>
              </div>
            </div>
          </article>

          {/* Card 2 – Sin fiado, sin cobranza */}
          <article className="rounded-[28px] bg-white border border-slate-200/90 p-6 sm:p-8 shadow-xl flex flex-col justify-between">
            <div>
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-bold uppercase tracking-wider mb-5">
                <CalendarCheck2 className="w-3.5 h-3.5 text-[#10B981]" aria-hidden="true" />
                Sin fiado, sin cobranza
              </div>

              {/* Problema arriba */}
              <div className="rounded-2xl bg-rose-50/60 border border-rose-200/70 p-4 mb-4 flex items-start gap-3">
                <XCircle className="w-5 h-5 text-rose-500 shrink-0 mt-0.5" aria-hidden="true" />
                <div>
                  <span className="block text-xs font-bold uppercase tracking-wider text-rose-700 mb-0.5">
                    Problema habitual
                  </span>
                  <p className="text-sm sm:text-base font-bold text-slate-800">
                    Vendes fiado, se atrasan los pagos y pierdes tiempo persiguiendo clientes
                  </p>
                </div>
              </div>

              {/* Solución abajo */}
              <div className="rounded-2xl bg-[#0A192F] text-white border border-white/10 p-4 mb-6 flex items-start gap-3 shadow-md">
                <CheckCircle2 className="w-5 h-5 text-[#38BDF8] shrink-0 mt-0.5" aria-hidden="true" />
                <div>
                  <span className="block text-xs font-bold uppercase tracking-wider text-[#38BDF8] mb-0.5">
                    Solución Venta a Crédito Kuota
                  </span>
                  <p className="text-sm sm:text-base font-bold text-white">
                    Tus clientes compran en cuotas, tú cobras al 100% al contado y tienes{' '}
                    <span className="px-2 py-0.5 rounded-md bg-[#10B981] text-white font-extrabold">
                      0% riesgo de impago
                    </span>
                  </p>
                </div>
              </div>
            </div>

            {/* Ilustración comparativa de flujo */}
            <div
              role="img"
              aria-label="Comparativa entre cuaderno de fiados tradicional y liquidación garantizada en 48 horas con Kuota"
              className="rounded-2xl bg-slate-50 border border-slate-200/90 p-5 sm:p-6 shadow-xs flex flex-col justify-between space-y-5"
            >
              <div className="flex flex-wrap items-center justify-between gap-2 pb-3 border-b border-slate-200/80">
                <div>
                  <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500 block">
                    Flujo de caja garantizado
                  </span>
                  <h3 className="text-sm sm:text-base font-extrabold text-[#0A192F]">
                    Del cuaderno de fiados a liquidación en {PRICING_CONFIG.settlementBusinessHours}h
                  </h3>
                </div>
                <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-emerald-50 text-emerald-800 border border-emerald-200 text-xs font-bold">
                  <ShieldCheck className="w-3.5 h-3.5 text-[#10B981]" aria-hidden="true" />
                  0% riesgo para el comercio
                </span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
                {/* Antes: Cuaderno de fiados */}
                <div className="rounded-2xl bg-white border border-slate-200/80 p-4 flex flex-col justify-between shadow-xs">
                  <div>
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <span className="text-xs font-bold line-through text-slate-500">
                        Cuaderno de fiados
                      </span>
                      <span className="px-2 py-0.5 rounded bg-rose-50 text-rose-700 text-[10px] font-bold border border-rose-200">
                        Antes ✕
                      </span>
                    </div>
                    <ul className="space-y-1.5 text-xs font-medium text-slate-600">
                      <li className="flex items-center gap-1.5">
                        <XCircle className="w-3.5 h-3.5 text-rose-500 shrink-0" aria-hidden="true" />
                        <span>Capital inmovilizado</span>
                      </li>
                      <li className="flex items-center gap-1.5">
                        <XCircle className="w-3.5 h-3.5 text-rose-500 shrink-0" aria-hidden="true" />
                        <span>Riesgo de atraso o pérdida</span>
                      </li>
                      <li className="flex items-center gap-1.5">
                        <XCircle className="w-3.5 h-3.5 text-rose-500 shrink-0" aria-hidden="true" />
                        <span>Cobranzas incómodas</span>
                      </li>
                    </ul>
                  </div>
                </div>

                {/* Ahora: Calendario Kuota */}
                <div className="rounded-2xl bg-[#0A192F] text-white border border-white/10 p-4 flex flex-col justify-between shadow-md">
                  <div>
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <span className="text-xs font-bold text-slate-300">
                        Cobro con Kuota
                      </span>
                      <span className="px-2 py-0.5 rounded bg-[#10B981] text-white text-[10px] font-bold">
                        Con Kuota ✓
                      </span>
                    </div>
                    <ul className="space-y-1.5 text-xs font-medium text-slate-300">
                      <li className="flex items-center gap-1.5">
                        <CheckCircle2 className="w-3.5 h-3.5 text-[#38BDF8] shrink-0" aria-hidden="true" />
                        <span>Recibes la inicial al instante</span>
                      </li>
                      <li className="flex items-center gap-1.5">
                        <CheckCircle2 className="w-3.5 h-3.5 text-[#38BDF8] shrink-0" aria-hidden="true" />
                        <span>Saldo liquidado en 48h hábiles</span>
                      </li>
                      <li className="flex items-center gap-1.5">
                        <CheckCircle2 className="w-3.5 h-3.5 text-[#38BDF8] shrink-0" aria-hidden="true" />
                        <span>Kuota asume 100% el riesgo</span>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>

              <div className="rounded-xl bg-white border border-slate-200/80 px-3.5 py-2.5 flex items-center justify-between gap-2 text-xs font-bold text-slate-800">
                <span className="flex items-center gap-1.5">
                  <Clock className="w-4 h-4 text-[#0391FD] shrink-0" aria-hidden="true" />
                  Liquidación bancaria garantizada
                </span>
                <span className="px-2.5 py-0.5 rounded-full bg-sky-50 text-[#0391FD] border border-sky-200 font-bold">
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

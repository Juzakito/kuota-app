import React, { useState } from 'react';
import { Calculator, TrendingUp, Info } from 'lucide-react';
import {
  PRICING_CONFIG,
  formatPercentEs,
} from '../../config/pricingConfig';
import { ScheduleCallButton } from './ScheduleCallButton';
import { trackSupplierEvent } from '../../utils/analytics';
import { ScrollReveal, AnimatedNumber } from '../../utils/motionSystem';
import { kuotaSound } from '../../utils/soundSystem';

/**
 * PRD §5.6: Úsalo – calculadora de ahorro
 * - Entradas:
 *   - Venta mensual con tarjeta, en soles (campo numérico con deslizador; valor por defecto sugerido: S/ 20,000).
 *   - Ticket promedio (opcional, solo informativo en la versión 1).
 * - Cálculos (parametrizados):
 *   - Costo con POS = venta mensual × 3,5%.
 *   - Costo con Kuota = venta mensual × 2%.
 *   - Ahorro mensual = costo con POS − costo con Kuota.
 *   - Ahorro anual = ahorro mensual × 12.
 * - Salida:
 *   - Ahorro mensual y anual en soles, en tamaño grande con `AnimatedNumber`.
 *   - Barra comparativa de dos colores (POS vs Kuota) que se actualiza con la entrada.
 *   - Nota al pie: "Cálculo referencial con una comisión promedio de POS de 3,5%. No incluye la suscripción del plan."
 *   - CTA debajo: "Agenda una call y calcula tu caso".
 */
export const SavingsCalculatorSection: React.FC = () => {
  const [monthlySales, setMonthlySales] = useState<number>(
    PRICING_CONFIG.calculatorDefaultMonthlySalesSoles
  );
  const [averageTicket, setAverageTicket] = useState<string>(
    String(PRICING_CONFIG.calculatorDefaultAverageTicketSoles)
  );

  const safeMonthlySales = Math.max(0, monthlySales || 0);

  const posCost =
    safeMonthlySales * (PRICING_CONFIG.posAverageFeePercent / 100);
  const kuotaCost =
    safeMonthlySales * (PRICING_CONFIG.kuotaPaymentsFeePercent / 100);
  const monthlySavings = Math.max(0, posCost - kuotaCost);
  const annualSavings = monthlySavings * 12;

  const kuotaBarRatioPercent =
    posCost > 0 ? Math.round((kuotaCost / posCost) * 100) : 57;

  const posFeeText = formatPercentEs(PRICING_CONFIG.posAverageFeePercent);
  const kuotaFeeText = formatPercentEs(PRICING_CONFIG.kuotaPaymentsFeePercent);

  const handleSalesChange = (nextVal: number, playAudio = false) => {
    const clamped = Math.min(
      Math.max(0, nextVal),
      PRICING_CONFIG.calculatorMaxMonthlySalesSoles * 5
    );
    if (playAudio) {
      kuotaSound.playSelectOption();
    }
    setMonthlySales(clamped);
    trackSupplierEvent('calculator_interaction', {
      monthly_sales_soles: clamped,
      monthly_savings_soles: Math.round(
        clamped *
          ((PRICING_CONFIG.posAverageFeePercent -
            PRICING_CONFIG.kuotaPaymentsFeePercent) /
            100)
      ),
    });
  };

  const estimatedTransactions =
    Number(averageTicket) > 0
      ? Math.round(safeMonthlySales / Number(averageTicket))
      : null;

  return (
    <section
      id="calculadora"
      aria-labelledby="calculator-heading"
      className="py-16 sm:py-24 bg-white border-b border-slate-200/90"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <ScrollReveal delayMs={0} direction="up">
          <div className="text-center max-w-2xl mx-auto">
            <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full bg-sky-50 border border-sky-200 text-xs font-bold text-[#0391FD] uppercase tracking-wider mb-3">
              <Calculator className="w-3.5 h-3.5 text-[#0391FD]" aria-hidden="true" />
              Calculadora de ahorro POS
            </span>
            <h2
              id="calculator-heading"
              className="text-3xl sm:text-4xl lg:text-5xl font-black text-[#0A192F] tracking-tight mb-2"
            >
              Descubre cuánto dinero recuperas cada mes
            </h2>
            <p className="text-base sm:text-lg font-normal text-slate-600">
              Kuota Payments cobra solo <strong>1% por transacción</strong> frente al 3,5%–5% de los POS tradicionales y el 2,95% de Yape Empresa.
            </p>
          </div>
        </ScrollReveal>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-stretch">
          {/* Columna de Entradas */}
          <ScrollReveal
            delayMs={80}
            direction="left"
            className="lg:col-span-6 rounded-[28px] bg-white border border-slate-200/90 p-6 sm:p-8 shadow-xl flex flex-col justify-between"
          >
            <div className="space-y-6">
              {/* Entrada 1: Venta mensual con tarjeta (campo numérico + deslizador) */}
              <div>
                <div className="flex items-center justify-between gap-2 mb-2">
                  <label
                    htmlFor="monthly-sales-input"
                    className="text-sm sm:text-base font-bold text-[#0A192F]"
                  >
                    Venta mensual con tarjeta / links (S/)
                  </label>
                  <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-slate-100 text-slate-700 border border-slate-200">
                    Sugerido: S/ 20,000
                  </span>
                </div>

                <div className="relative mb-4">
                  <span className="absolute left-4 top-1/2 -translate-y-1/2 text-lg font-bold text-slate-400">
                    S/
                  </span>
                  <input
                    id="monthly-sales-input"
                    type="number"
                    min={0}
                    step={500}
                    value={monthlySales}
                    onChange={(e) => handleSalesChange(Number(e.target.value))}
                    className="w-full pl-11 pr-4 py-3.5 rounded-2xl border border-slate-200/90 bg-slate-50 text-xl sm:text-2xl font-black text-[#0A192F] focus:bg-white focus:border-[#0391FD] focus:ring-4 focus:ring-[#0391FD]/10 outline-none transition-all"
                  />
                </div>

                <input
                  type="range"
                  aria-label="Deslizador de venta mensual con tarjeta"
                  min={PRICING_CONFIG.calculatorMinMonthlySalesSoles}
                  max={PRICING_CONFIG.calculatorMaxMonthlySalesSoles}
                  step={PRICING_CONFIG.calculatorStepSoles}
                  value={Math.min(
                    safeMonthlySales,
                    PRICING_CONFIG.calculatorMaxMonthlySalesSoles
                  )}
                  onChange={(e) => handleSalesChange(Number(e.target.value))}
                  className="w-full h-2.5 bg-slate-200 rounded-lg appearance-none cursor-pointer accent-[#0391FD]"
                />
                <div className="flex justify-between text-xs font-semibold text-slate-500 mt-2">
                  {[1000, 20000, 50000, 100000, 200000].map((preset) => (
                    <button
                      key={preset}
                      type="button"
                      onClick={() => handleSalesChange(preset, true)}
                      className={`hover:text-[#0391FD] transition-colors cursor-pointer ${
                        safeMonthlySales === preset
                          ? 'text-[#0391FD] font-bold underline'
                          : ''
                      }`}
                    >
                      S/ {(preset / 1000).toLocaleString('es-PE')}k
                    </button>
                  ))}
                </div>
              </div>

              {/* Entrada 2: Ticket promedio */}
              <div className="pt-4 border-t border-slate-100">
                <div className="flex items-center justify-between gap-2 mb-2">
                  <label
                    htmlFor="avg-ticket-input"
                    className="text-sm font-bold text-[#0A192F]"
                  >
                    Ticket promedio en soles{' '}
                    <span className="text-xs font-normal text-slate-500">
                      (mínimo sugerido S/ 199)
                    </span>
                  </label>
                </div>
                <div className="relative">
                  <span className="absolute left-4 top-1/2 -translate-y-1/2 text-sm font-bold text-slate-400">
                    S/
                  </span>
                  <input
                    id="avg-ticket-input"
                    type="number"
                    min={0}
                    placeholder="Ej. 250"
                    value={averageTicket}
                    onChange={(e) => setAverageTicket(e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200/90 bg-slate-50 text-base font-semibold text-[#0A192F] focus:bg-white focus:border-[#0391FD] outline-none"
                  />
                </div>
                {estimatedTransactions !== null && estimatedTransactions > 0 && (
                  <p className="text-xs font-medium text-slate-500 mt-2">
                    Equivale aproximadamente a{' '}
                    <strong className="text-slate-900 font-bold">
                      {estimatedTransactions} cobros al mes
                    </strong>
                    .
                  </p>
                )}
              </div>
            </div>

            {/* Comparativa de Comisiones de Mercado */}
            <div className="mt-6 pt-4 border-t border-slate-100 space-y-2">
              <span className="text-xs font-bold text-slate-600 block">Comparativa de comisiones en Perú:</span>
              <div className="grid grid-cols-3 gap-2 text-center text-xs">
                <div className="p-2 rounded-xl bg-slate-50 border border-slate-200/80">
                  <span className="block text-[10px] text-slate-500 font-medium">POS Tradicional</span>
                  <span className="font-bold text-slate-800">3,5% – 5%</span>
                </div>
                <div className="p-2 rounded-xl bg-slate-50 border border-slate-200/80">
                  <span className="block text-[10px] text-slate-500 font-medium">Yape Empresa</span>
                  <span className="font-bold text-slate-800">2,95%</span>
                </div>
                <div className="p-2 rounded-xl bg-sky-50 border border-sky-200">
                  <span className="block text-[10px] text-[#0391FD] font-bold">Kuota Payments</span>
                  <span className="font-black text-[#0391FD]">1,0%</span>
                </div>
              </div>
            </div>
          </ScrollReveal>

          {/* Columna de Salida: Ahorro mensual y anual + Barra comparativa + CTA */}
          <ScrollReveal
            delayMs={150}
            direction="right"
            className="lg:col-span-6 rounded-[28px] bg-[#0A192F] text-white border border-slate-800 p-6 sm:p-8 shadow-2xl flex flex-col justify-between relative overflow-hidden"
          >
            <div className="absolute top-0 right-0 w-64 h-64 bg-[#0391FD]/10 rounded-full blur-[90px] pointer-events-none" />

            <div className="relative z-10">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/20 border border-emerald-400/30 text-emerald-300 text-xs font-bold uppercase tracking-wider mb-5">
                <TrendingUp className="w-3.5 h-3.5 text-[#10B981]" aria-hidden="true" />
                Tu ahorro directo en caja con Kuota
              </div>

              {/* Ahorro mensual y anual en tamaño grande */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
                <div className="rounded-2xl bg-white/5 border border-white/10 p-4">
                  <span className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">
                    Ahorro mensual
                  </span>
                  <div className="text-3xl sm:text-4xl font-black text-white tracking-tight">
                    <AnimatedNumber value={Math.round(monthlySavings)} prefix="S/ " />
                  </div>
                  <span className="text-xs font-normal text-slate-400">
                    cada mes en tu negocio
                  </span>
                </div>

                <div className="rounded-2xl bg-[#0391FD] border border-white/20 p-4 shadow-lg">
                  <span className="block text-xs font-bold uppercase tracking-wider text-white/90 mb-1">
                    Ahorro anual
                  </span>
                  <div className="text-3xl sm:text-4xl font-black text-white tracking-tight">
                    <AnimatedNumber value={Math.round(annualSavings)} prefix="S/ " />
                  </div>
                  <span className="text-xs font-semibold text-sky-100">
                    en 12 meses de operación
                  </span>
                </div>
              </div>

              {/* Barra comparativa de dos colores (POS vs Kuota) */}
              <div className="rounded-2xl bg-white/5 border border-white/10 p-4 space-y-4 mb-6">
                <div>
                  <div className="flex justify-between text-xs sm:text-sm font-bold mb-1.5">
                    <span className="text-slate-400">
                      Costo mensual con POS tradicional ({posFeeText})
                    </span>
                    <AnimatedNumber
                      value={Math.round(posCost)}
                      prefix="S/ "
                      className="text-white"
                    />
                  </div>
                  <div className="w-full h-3 rounded-full bg-white/10 overflow-hidden p-0.5">
                    <div className="h-full rounded-full bg-slate-500 w-full transition-all duration-300" />
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-xs sm:text-sm font-bold mb-1.5">
                    <span className="text-[#38BDF8]">
                      Costo mensual con Kuota Payments ({kuotaFeeText})
                    </span>
                    <AnimatedNumber
                      value={Math.round(kuotaCost)}
                      prefix="S/ "
                      className="text-[#38BDF8] font-black"
                    />
                  </div>
                  <div className="w-full h-3 rounded-full bg-white/10 overflow-hidden p-0.5">
                    <div
                      className="h-full rounded-full bg-[#0391FD] transition-all duration-300"
                      style={{ width: `${Math.max(15, kuotaBarRatioPercent)}%` }}
                    />
                  </div>
                </div>
              </div>
            </div>

            {/* CTA debajo: "Agenda una call y calcula tu caso" */}
            <div className="relative z-10 pt-2">
              <ScheduleCallButton
                section="calculadora"
                label="Agenda una llamada y calcula tu caso"
                size="lg"
                className="w-full"
              />
            </div>
          </ScrollReveal>
        </div>
      </div>
    </section>
  );
};

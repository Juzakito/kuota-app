import React, { useState } from 'react';
import { Calculator, TrendingUp, Info } from 'lucide-react';
import {
  PRICING_CONFIG,
  formatPercentEs,
  formatSoles,
} from '../../config/pricingConfig';
import { ScheduleCallButton } from './ScheduleCallButton';
import { trackSupplierEvent } from '../../utils/analytics';

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
 *   - Ahorro mensual y anual en soles, en tamaño grande.
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

  const handleSalesChange = (nextVal: number) => {
    const clamped = Math.min(
      Math.max(0, nextVal),
      PRICING_CONFIG.calculatorMaxMonthlySalesSoles * 5
    );
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
      className="py-16 sm:py-20 border-b-2 border-kuota-deepnavy bg-white/45"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-10">
          <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full bg-white border-2 border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-3">
            <Calculator className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
            Calculadora de ahorro
          </span>
          <h2
            id="calculator-heading"
            className="text-3xl sm:text-4xl font-extrabold text-kuota-deepnavy tracking-tight mb-2"
          >
            Descubre cuánto dinero recuperas cada mes
          </h2>
          <p className="text-base sm:text-lg font-semibold text-kuota-crayola">
            Ajusta tu volumen mensual de ventas con tarjeta y compara al instante.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-stretch">
          {/* Columna de Entradas */}
          <div className="lg:col-span-6 rounded-3xl bg-white border-2 border-kuota-deepnavy p-6 sm:p-8 shadow-navy-hard-lg flex flex-col justify-between">
            <div className="space-y-6">
              {/* Entrada 1: Venta mensual con tarjeta (campo numérico + deslizador) */}
              <div>
                <div className="flex items-center justify-between gap-2 mb-2">
                  <label
                    htmlFor="monthly-sales-input"
                    className="text-sm sm:text-base font-extrabold text-kuota-deepnavy"
                  >
                    Venta mensual con tarjeta (S/)
                  </label>
                  <span className="text-xs font-extrabold px-2.5 py-1 rounded-lg bg-kuota-palesky text-kuota-deepnavy border border-kuota-deepnavy">
                    Sugerido: S/ 20,000
                  </span>
                </div>

                <div className="relative mb-4">
                  <span className="absolute left-4 top-1/2 -translate-y-1/2 text-lg font-extrabold text-kuota-deepnavy">
                    S/
                  </span>
                  <input
                    id="monthly-sales-input"
                    type="number"
                    min={0}
                    step={500}
                    value={monthlySales}
                    onChange={(e) => handleSalesChange(Number(e.target.value))}
                    className="w-full pl-11 pr-4 py-3.5 rounded-2xl border-2 border-kuota-deepnavy bg-kuota-palesky/40 text-xl sm:text-2xl font-extrabold text-kuota-deepnavy focus:bg-white"
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
                  className="w-full h-3 bg-kuota-palesky rounded-lg appearance-none cursor-pointer accent-kuota-dodger border border-kuota-deepnavy"
                />
                <div className="flex justify-between text-xs font-bold text-kuota-deepnavy/75 mt-1.5">
                  <span>S/ 1,000</span>
                  <span>S/ 50,000</span>
                  <span>S/ 100,000</span>
                  <span>S/ 200,000</span>
                </div>
              </div>

              {/* Entrada 2: Ticket promedio (opcional, solo informativo en v1) */}
              <div className="pt-4 border-t border-kuota-deepnavy/15">
                <div className="flex items-center justify-between gap-2 mb-2">
                  <label
                    htmlFor="avg-ticket-input"
                    className="text-sm font-extrabold text-kuota-deepnavy"
                  >
                    Ticket promedio en soles{' '}
                    <span className="text-xs font-semibold text-kuota-crayola">
                      (opcional, informativo)
                    </span>
                  </label>
                </div>
                <div className="relative">
                  <span className="absolute left-4 top-1/2 -translate-y-1/2 text-sm font-extrabold text-kuota-deepnavy">
                    S/
                  </span>
                  <input
                    id="avg-ticket-input"
                    type="number"
                    min={0}
                    placeholder="Ej. 250"
                    value={averageTicket}
                    onChange={(e) => setAverageTicket(e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl border-2 border-kuota-deepnavy bg-white text-base font-bold text-kuota-deepnavy"
                  />
                </div>
                {estimatedTransactions !== null && estimatedTransactions > 0 && (
                  <p className="text-xs font-bold text-kuota-crayola mt-2">
                    Equivale aproximadamente a{' '}
                    <strong className="text-kuota-deepnavy">
                      {estimatedTransactions} cobros al mes
                    </strong>
                    .
                  </p>
                )}
              </div>
            </div>

            {/* Nota al pie requerida por PRD §5.6 */}
            <div className="mt-6 pt-4 border-t border-kuota-deepnavy/15 flex items-start gap-2 text-xs font-bold text-kuota-deepnavy/80">
              <Info className="w-4 h-4 text-kuota-dodger shrink-0 mt-0.5" aria-hidden="true" />
              <span>
                Cálculo referencial con una comisión promedio de POS de {posFeeText}. No incluye la suscripción del plan.
              </span>
            </div>
          </div>

          {/* Columna de Salida: Ahorro mensual y anual + Barra comparativa + CTA */}
          <div className="lg:col-span-6 rounded-3xl bg-kuota-deepnavy text-white border-2 border-kuota-deepnavy p-6 sm:p-8 shadow-navy-hard-lg flex flex-col justify-between">
            <div>
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-kuota-dodger/25 border border-kuota-palesky/30 text-kuota-palesky text-xs font-extrabold uppercase tracking-wider mb-5">
                <TrendingUp className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                Tu ahorro estimado con Kuota
              </div>

              {/* Ahorro mensual y anual en tamaño grande */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
                <div className="rounded-2xl bg-white/10 border border-white/20 p-4">
                  <span className="block text-xs font-extrabold uppercase tracking-wider text-kuota-palesky mb-1">
                    Ahorro mensual
                  </span>
                  <div className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
                    {formatSoles(monthlySavings)}
                  </div>
                  <span className="text-xs font-semibold text-kuota-palesky/80">
                    cada mes en tu caja
                  </span>
                </div>

                <div className="rounded-2xl bg-kuota-dodger border-2 border-kuota-palesky p-4 shadow-sm">
                  <span className="block text-xs font-extrabold uppercase tracking-wider text-white/90 mb-1">
                    Ahorro anual
                  </span>
                  <div className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
                    {formatSoles(annualSavings)}
                  </div>
                  <span className="text-xs font-bold text-kuota-palesky">
                    en 12 meses de operación
                  </span>
                </div>
              </div>

              {/* Barra comparativa de dos colores (POS vs Kuota) que se actualiza con la entrada */}
              <div className="rounded-2xl bg-white/10 border border-white/20 p-4 space-y-4 mb-6">
                <div>
                  <div className="flex justify-between text-xs sm:text-sm font-extrabold mb-1.5">
                    <span className="text-kuota-palesky">
                      Costo mensual con POS ({posFeeText})
                    </span>
                    <span className="text-white">{formatSoles(posCost)}</span>
                  </div>
                  <div className="w-full h-4 rounded-full bg-white/15 overflow-hidden p-0.5">
                    <div className="h-full rounded-full bg-kuota-palesky/60 w-full transition-all duration-300" />
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-xs sm:text-sm font-extrabold mb-1.5">
                    <span className="text-kuota-dodger">
                      Costo mensual con Kuota ({kuotaFeeText})
                    </span>
                    <span className="text-kuota-palesky">
                      {formatSoles(kuotaCost)}
                    </span>
                  </div>
                  <div className="w-full h-4 rounded-full bg-white/15 overflow-hidden p-0.5">
                    <div
                      className="h-full rounded-full bg-kuota-dodger transition-all duration-300"
                      style={{ width: `${kuotaBarRatioPercent}%` }}
                    />
                  </div>
                </div>
              </div>
            </div>

            {/* CTA debajo: "Agenda una call y calcula tu caso" (PRD §5.6) */}
            <ScheduleCallButton
              section="calculadora"
              label="Agenda una call y calcula tu caso"
              size="lg"
              className="w-full"
            />
          </div>
        </div>
      </div>
    </section>
  );
};


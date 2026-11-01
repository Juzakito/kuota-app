import React, { useState, useMemo } from 'react';
import { Calculator, TrendingUp, Sparkles, CheckCircle2, Info, ArrowRight } from 'lucide-react';
import { trackEvent } from '../utils/analytics';

export interface SupplierSavingsCalculatorProps {
  onScheduleCall?: () => void;
  className?: string;
}

export const SupplierSavingsCalculator: React.FC<SupplierSavingsCalculatorProps> = ({
  onScheduleCall,
  className = '',
}) => {
  // Entradas (PRD §4.2)
  const [monthlySales, setMonthlySales] = useState<number>(20000);
  const [posFeePercent, setPosFeePercent] = useState<number>(3.5);
  const [posRental, setPosRental] = useState<number>(0);

  const SUBSCRIPTION_WITH_IGV = 94.40; // S/ 80 + 18% IGV (PRD §4.2)
  const KUOTA_PAYMENTS_FEE = 0.02; // 2% fija Kuota Payments

  const calculation = useMemo(() => {
    const feeDiff = (posFeePercent / 100) - KUOTA_PAYMENTS_FEE;
    const breakEven = feeDiff > 0 ? SUBSCRIPTION_WITH_IGV / feeDiff : 0;
    const rawSavings = (monthlySales * feeDiff) + posRental - SUBSCRIPTION_WITH_IGV;
    const isAboveBreakEven = monthlySales >= breakEven;
    const annualSavings = rawSavings > 0 ? rawSavings * 12 : 0;

    return {
      feeDiffPercent: feeDiff * 100,
      breakEvenSoles: Math.round(breakEven),
      monthlySavings: Math.max(0, rawSavings),
      annualSavings,
      isAboveBreakEven,
      costPos: (monthlySales * (posFeePercent / 100)) + posRental,
      costKuota: (monthlySales * KUOTA_PAYMENTS_FEE) + SUBSCRIPTION_WITH_IGV,
    };
  }, [monthlySales, posFeePercent, posRental]);

  const handleSalesChange = (val: number) => {
    setMonthlySales(val);
    trackEvent('use_calculator', {
      type: 'proveedor',
      monthly_sales: val,
      pos_fee: posFeePercent,
      pos_rental: posRental,
    });
  };

  return (
    <div className={`bg-paper rounded-card border border-fog p-6 md:p-8 shadow-card ${className}`} id="calculadora-ahorro">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 border-b border-fog">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-pill bg-linen text-forest text-xs font-semibold mb-2">
            <Calculator className="w-3.5 h-3.5" />
            <span>Calculadora de ahorro Kuota Payments</span>
          </div>
          <h3 className="text-2xl md:text-3xl font-extrabold text-obsidian tracking-tight">
            Compara tu costo frente al POS tradicional
          </h3>
          <p className="text-sm text-charcoal/80 mt-1">
            2% de comisión por transacción con QR y Link por WhatsApp. Sin costos ocultos.
          </p>
        </div>

        <div className="text-xs px-3 py-1.5 rounded-pill bg-fog text-charcoal font-medium">
          Punto de equilibrio aprox.: <strong>S/ {calculation.breakEvenSoles.toLocaleString('es-PE')}</strong> al mes
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 pt-6">
        {/* Entradas */}
        <div className="lg:col-span-7 space-y-6">
          {/* Ventas con tarjeta o QR */}
          <div>
            <div className="flex justify-between items-center mb-2">
              <label className="text-sm font-bold text-obsidian">
                Ventas mensuales con tarjeta y QR
              </label>
              <div className="text-xl font-extrabold text-forest tabular-nums">
                S/ {monthlySales.toLocaleString('es-PE')}
              </div>
            </div>

            <input
              type="range"
              min={1000}
              max={100000}
              step={1000}
              value={monthlySales}
              onChange={(e) => handleSalesChange(Number(e.target.value))}
              className="w-full h-2 bg-fog rounded-pill appearance-none cursor-pointer"
            />

            <div className="flex justify-between text-xs text-pebble mt-2">
              <span>S/ 1,000</span>
              <span>S/ 50,000</span>
              <span>S/ 100,000</span>
            </div>
          </div>

          {/* Comisión del POS actual y alquiler */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-charcoal mb-1">
                Comisión actual de tu POS (%)
              </label>
              <div className="flex">
                <input
                  type="number"
                  step="0.1"
                  min="2.1"
                  max="10.0"
                  value={posFeePercent}
                  onChange={(e) => setPosFeePercent(Number(e.target.value))}
                  className="w-full px-3 py-2 text-sm bg-paper border border-pebble/40 rounded-card text-charcoal focus:border-forest focus:outline-none"
                />
                <span className="inline-flex items-center px-3 bg-fog border border-l-0 border-pebble/40 rounded-r-card text-xs font-bold text-charcoal -ml-1">
                  %
                </span>
              </div>
              <span className="text-[11px] text-pebble mt-1 block">
                Promedio de mercado: 3,5% a 5% + IGV
              </span>
            </div>

            <div>
              <label className="block text-xs font-semibold text-charcoal mb-1">
                Alquiler mensual del POS (S/)
              </label>
              <div className="flex">
                <span className="inline-flex items-center px-3 bg-fog border border-r-0 border-pebble/40 rounded-l-card text-xs font-bold text-charcoal">
                  S/
                </span>
                <input
                  type="number"
                  step="5"
                  min="0"
                  value={posRental}
                  onChange={(e) => setPosRental(Number(e.target.value))}
                  placeholder="0"
                  className="w-full px-3 py-2 text-sm bg-paper border border-pebble/40 rounded-r-card text-charcoal focus:border-forest focus:outline-none"
                />
              </div>
              <span className="text-[11px] text-pebble mt-1 block">
                Opcional (si tu proveedor cobra renta de equipo)
              </span>
            </div>
          </div>

          <div className="p-4 rounded-card bg-fog text-charcoal text-xs space-y-1.5 border border-fog">
            <div className="font-bold text-obsidian">Condiciones del Plan Basic Kuota:</div>
            <div>• Comisión de <strong>2%</strong> por transacción (vs. {posFeePercent}% de tu POS actual).</div>
            <div>• Suscripción de <strong>S/ 80 + IGV</strong> (S/ 94.40 mensual).</div>
            <div>• Liquidación en <strong>48 horas hábiles</strong> directo a tu cuenta bancaria.</div>
          </div>
        </div>

        {/* Salida Comparativa */}
        <div className="lg:col-span-5 bg-linen/50 rounded-card p-6 border border-spruce/20 flex flex-col justify-between">
          <div className="space-y-5">
            <div className="text-xs font-bold uppercase tracking-wider text-forest">
              Diagnóstico de tu negocio
            </div>

            {calculation.isAboveBreakEven ? (
              <>
                <div className="p-4 rounded-card bg-paper border border-spruce/20">
                  <div className="text-xs text-charcoal">Ahorro mensual estimado con Kuota</div>
                  <div className="text-3xl font-black text-forest tabular-nums mt-1">
                    S/ {calculation.monthlySavings.toFixed(2)}
                  </div>
                  <div className="text-xs text-spruce font-semibold mt-1">
                    Proyección anual: S/ {calculation.annualSavings.toFixed(2)} ahorrados
                  </div>
                </div>

                <div className="space-y-2 text-xs text-charcoal pt-1">
                  <div className="flex justify-between py-1 border-b border-fog">
                    <span>Costo mensual con POS actual:</span>
                    <span className="font-semibold tabular-nums text-obsidian">
                      S/ {calculation.costPos.toFixed(2)}
                    </span>
                  </div>
                  <div className="flex justify-between py-1 border-b border-fog">
                    <span>Costo mensual con Kuota (2% + sub):</span>
                    <span className="font-semibold tabular-nums text-forest">
                      S/ {calculation.costKuota.toFixed(2)}
                    </span>
                  </div>
                </div>
              </>
            ) : (
              // Mensaje obligatorio del PRD §4.2 cuando ventas < equilibrio:
              // "si ventas < equilibrio: cambiar el mensaje a 'Aceptar tarjeta sin POS y cobrar por link a clientes de WhatsApp = ventas nuevas' (no mostrar ahorro negativo)."
              <div className="p-5 rounded-card bg-paper border border-forest/20 space-y-3">
                <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-pill bg-linen text-forest text-xs font-bold">
                  <Sparkles className="w-3.5 h-3.5 text-forest" />
                  <span>Oportunidad de crecimiento</span>
                </div>
                <div className="text-base font-extrabold text-forest leading-snug">
                  Aceptar tarjeta sin POS y cobrar por link a clientes de WhatsApp = ventas nuevas.
                </div>
                <p className="text-xs text-charcoal/80 leading-relaxed">
                  Con ventas de S/ {monthlySales.toLocaleString('es-PE')}, la ventaja principal de Kuota es permitirte cobrar a clientes a distancia por WhatsApp y en mostrador con QR sin comprar equipos costosos.
                </p>
              </div>
            )}

            <div className="flex items-start gap-2 text-[11px] text-charcoal/70">
              <CheckCircle2 className="w-4 h-4 text-spruce shrink-0 mt-0.5" />
              <span>
                Tarjetas BCP, BBVA, Interbank, Scotiabank, Yape y Plin centralizados en un solo código QR.
              </span>
            </div>
          </div>

          <div className="pt-6 border-t border-spruce/20">
            {onScheduleCall ? (
              <button
                type="button"
                onClick={onScheduleCall}
                className="w-full py-3.5 px-4 rounded-pill bg-forest text-lime font-bold text-xs md:text-sm hover:bg-forest-dark transition-colors flex items-center justify-center gap-2 cursor-pointer shadow-sm"
              >
                <span>Agenda una llamada y calcula tu caso</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            ) : (
              <a
                href="#calendly"
                className="w-full py-3.5 px-4 rounded-pill bg-forest text-lime font-bold text-xs md:text-sm hover:bg-forest-dark transition-colors flex items-center justify-center gap-2 cursor-pointer shadow-sm"
              >
                <span>Agenda una llamada y calcula tu caso</span>
                <ArrowRight className="w-4 h-4" />
              </a>
            )}
            <p className="text-[11px] text-pebble text-center mt-2">
              Reunión de 30 minutos sin compromiso con un asesor especialista.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

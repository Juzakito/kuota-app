import React, { useState, useMemo } from 'react';
import { Calculator, Calendar, AlertCircle, CheckCircle2, Info, ChevronRight } from 'lucide-react';
import { trackEvent } from '../utils/analytics';

export interface BuyerCreditCalculatorProps {
  onProceedToWaitlist?: () => void;
  className?: string;
}

export const BuyerCreditCalculator: React.FC<BuyerCreditCalculatorProps> = ({
  onProceedToWaitlist,
  className = '',
}) => {
  // Entradas
  const [amount, setAmount] = useState<number>(700);
  const [level, setLevel] = useState<number>(1);
  const [termDays, setTermDays] = useState<15 | 30 | 60>(30);

  // Límites según nivel (PRD §3.1)
  const levelLimits = {
    1: { max: 700, min: 199, initialPct: 0.30, label: 'Nivel 1 (Inicial 30% · hasta S/ 700)' },
    2: { max: 1200, min: 199, initialPct: 0.25, label: 'Nivel 2 (Inicial 25% · hasta S/ 1,200)' },
  };

  const currentLevelConfig = level === 1 ? levelLimits[1] : levelLimits[2];

  // Cálculos en céntimos para precisión matemática pura (PRD §3.1)
  const calculation = useMemo(() => {
    const clampedAmount = Math.max(199, amount);
    const amountCents = Math.round(clampedAmount * 100);
    const initialCents = Math.round(amountCents * currentLevelConfig.initialPct);
    const financedCents = amountCents - initialCents;
    const interestCents = Math.round(financedCents * 0.05); // 5% fijo sobre financiado
    const totalRepayCents = financedCents + interestCents;

    const numInstallments = termDays === 15 ? 2 : termDays === 30 ? 4 : 8;
    const baseCuotaCents = Math.floor(totalRepayCents / numInstallments);
    const remainderCents = totalRepayCents - (baseCuotaCents * numInstallments);

    const schedule: { week: number; amount: number }[] = [];
    for (let i = 1; i <= numInstallments; i++) {
      const isLast = i === numInstallments;
      const cuotaCents = isLast ? baseCuotaCents + remainderCents : baseCuotaCents;
      schedule.push({
        week: i,
        amount: cuotaCents / 100,
      });
    }

    return {
      initialSoles: initialCents / 100,
      financedSoles: financedCents / 100,
      interestSoles: interestCents / 100,
      totalRepaySoles: totalRepayCents / 100,
      numInstallments,
      schedule,
    };
  }, [amount, currentLevelConfig.initialPct, termDays]);

  const handleAmountChange = (val: number) => {
    setAmount(val);
    trackEvent('use_calculator', {
      type: 'comprador',
      amount: val,
      level,
      termDays,
    });
  };

  const isAboveLevel1Cap = level === 1 && amount > 700;

  return (
    <div className={`bg-paper rounded-card border border-fog p-6 md:p-8 shadow-card ${className}`} id="calculadora">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 border-b border-fog">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-pill bg-linen text-forest text-xs font-semibold mb-2">
            <Calculator className="w-3.5 h-3.5" />
            <span>Simulador oficial Kuota</span>
          </div>
          <h3 className="text-2xl md:text-3xl font-extrabold text-obsidian tracking-tight">
            Calcula tu compra en cuotas
          </h3>
          <p className="text-sm text-charcoal/80 mt-1">
            5% de costo fijo sobre el saldo financiado. No aumenta por elegir más semanas.
          </p>
        </div>

        {/* Nivel Selector */}
        <div className="flex items-center gap-1.5 bg-fog p-1 rounded-pill">
          <button
            type="button"
            onClick={() => {
              setLevel(1);
              if (amount > 700) setAmount(700);
            }}
            className={`px-3 py-1.5 text-xs font-semibold rounded-pill transition-all ${
              level === 1 ? 'bg-forest text-lime shadow-sm' : 'text-charcoal hover:text-obsidian'
            }`}
          >
            Nivel 1
          </button>
          <button
            type="button"
            onClick={() => setLevel(2)}
            className={`px-3 py-1.5 text-xs font-semibold rounded-pill transition-all ${
              level === 2 ? 'bg-forest text-lime shadow-sm' : 'text-charcoal hover:text-obsidian'
            }`}
          >
            Nivel 2
          </button>
          <span className="px-2 py-1 text-[11px] font-medium text-pebble">
            Niveles 3-6 (Próx.)
          </span>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 pt-6">
        {/* Columna Izquierda: Entradas */}
        <div className="lg:col-span-7 space-y-6">
          {/* 1. Monto de compra */}
          <div>
            <div className="flex justify-between items-center mb-2">
              <label className="text-sm font-bold text-obsidian">
                Monto de compra en mercadería
              </label>
              <div className="text-xl font-extrabold text-forest tabular-nums">
                S/ {amount.toLocaleString('es-PE')}
              </div>
            </div>

            <input
              type="range"
              min={199}
              max={level === 1 ? 700 : 1200}
              step={10}
              value={amount}
              onChange={(e) => handleAmountChange(Number(e.target.value))}
              className="w-full h-2 bg-fog rounded-pill appearance-none cursor-pointer"
            />

            <div className="flex justify-between text-xs text-pebble mt-2">
              <span>Mín. S/ 199</span>
              <span>Tope Nivel {level}: S/ {currentLevelConfig.max.toLocaleString('es-PE')}</span>
            </div>

            {isAboveLevel1Cap && (
              <div className="mt-2.5 p-3 rounded-card bg-amber-50 border border-amber-200 flex items-start gap-2 text-xs text-amber-900">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5 text-amber-600" />
                <span>Con el <strong>Nivel 1</strong> puedes comprar hasta S/ 700. Al pagar puntual accedes a Nivel 2 (hasta S/ 1,200).</span>
              </div>
            )}
          </div>

          {/* 2. Plazo en semanas */}
          <div>
            <label className="block text-sm font-bold text-obsidian mb-2.5">
              Plazo para pagar el saldo
            </label>
            <div className="grid grid-cols-3 gap-3">
              {[
                { days: 15 as const, cuotas: 2, label: '15 días', sub: '2 cuotas' },
                { days: 30 as const, cuotas: 4, label: '30 días', sub: '4 cuotas' },
                { days: 60 as const, cuotas: 8, label: '60 días', sub: '8 cuotas' },
              ].map((opt) => (
                <button
                  key={opt.days}
                  type="button"
                  onClick={() => setTermDays(opt.days)}
                  className={`p-3 rounded-card border text-left transition-all ${
                    termDays === opt.days
                      ? 'border-forest bg-linen text-forest font-bold ring-1 ring-forest'
                      : 'border-fog bg-paper hover:bg-fog/50 text-charcoal'
                  }`}
                >
                  <div className="text-sm font-bold">{opt.label}</div>
                  <div className="text-xs text-charcoal/70 mt-0.5">{opt.sub}</div>
                </button>
              ))}
            </div>
            <div className="text-[11px] text-pebble mt-2">
              * Pagos semanales con recordatorio automático a tu WhatsApp.
            </div>
          </div>

          {/* Mensaje clave del PRD sobre interés fijo */}
          <div className="p-4 rounded-card bg-linen/60 border border-spruce/20 text-forest text-xs leading-relaxed flex items-start gap-2.5">
            <Info className="w-4 h-4 shrink-0 text-spruce mt-0.5" />
            <div>
              <strong>Regla de transparencia Kuota:</strong> El interés es fijo (<strong>5% del monto financiado</strong>, tope bimestral) y <strong>no aumenta aunque elijas más cuotas</strong>.
            </div>
          </div>
        </div>

        {/* Columna Derecha: Resumen de Financiamiento */}
        <div className="lg:col-span-5 bg-fog/60 rounded-card p-5 border border-fog flex flex-col justify-between">
          <div className="space-y-4">
            <div className="text-xs font-bold uppercase tracking-wider text-charcoal/70">
              Resumen de tu compra
            </div>

            {/* Inicial hoy */}
            <div className="p-4 rounded-card bg-paper border border-fog flex items-center justify-between">
              <div>
                <div className="text-xs text-charcoal">Cuota inicial hoy ({currentLevelConfig.initialPct * 100}%)</div>
                <div className="text-xs text-pebble">Pagas al proveedor al instante</div>
              </div>
              <div className="text-xl font-extrabold text-forest tabular-nums">
                S/ {calculation.initialSoles.toFixed(2)}
              </div>
            </div>

            {/* Saldo Financiado y Costo */}
            <div className="space-y-2 text-xs text-charcoal px-1">
              <div className="flex justify-between">
                <span>Saldo a financiar:</span>
                <span className="font-semibold tabular-nums">S/ {calculation.financedSoles.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span>Costo de servicio Kuota (5% fijo):</span>
                <span className="font-semibold text-spruce tabular-nums">+ S/ {calculation.interestSoles.toFixed(2)}</span>
              </div>
              <div className="flex justify-between pt-2 border-t border-pebble/20 font-bold text-obsidian text-sm">
                <span>Total a pagar en cuotas:</span>
                <span className="text-forest tabular-nums">S/ {calculation.totalRepaySoles.toFixed(2)}</span>
              </div>
            </div>

            {/* Calendario de Cuotas Semanales */}
            <div className="pt-2">
              <div className="flex items-center gap-1.5 text-xs font-bold text-obsidian mb-2">
                <Calendar className="w-3.5 h-3.5 text-forest" />
                <span>Calendario de {calculation.numInstallments} cuotas semanales</span>
              </div>
              <div className="space-y-1.5 max-h-44 overflow-y-auto pr-1">
                {calculation.schedule.map((item) => (
                  <div
                    key={item.week}
                    className="flex justify-between items-center py-1.5 px-3 rounded bg-paper text-xs border border-fog/80"
                  >
                    <span className="text-charcoal font-medium">Semana {item.week}</span>
                    <span className="font-bold text-forest tabular-nums">
                      S/ {item.amount.toFixed(2)}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="pt-5 mt-4 border-t border-fog">
            {onProceedToWaitlist ? (
              <button
                type="button"
                onClick={onProceedToWaitlist}
                className="w-full py-3 px-4 rounded-pill bg-lime text-forest font-bold text-xs md:text-sm hover:bg-lime-hover transition-colors flex items-center justify-center gap-2 cursor-pointer shadow-sm"
              >
                <span>Solicitar comprar a este monto</span>
                <ChevronRight className="w-4 h-4" />
              </button>
            ) : (
              <a
                href="#waitlist"
                className="w-full py-3 px-4 rounded-pill bg-lime text-forest font-bold text-xs md:text-sm hover:bg-lime-hover transition-colors flex items-center justify-center gap-2 cursor-pointer shadow-sm"
              >
                <span>Comprar con Kuota</span>
                <ChevronRight className="w-4 h-4" />
              </a>
            )}

            <p className="text-[11px] text-pebble text-center mt-2.5">
              Simulación referencial. El crédito está sujeto a evaluación.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

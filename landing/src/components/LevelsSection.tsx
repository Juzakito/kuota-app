import React, { useState } from 'react';
import { TrendingUp, Calendar, Percent, Wallet, Sparkles } from 'lucide-react';
import { LANDING_CREDIT_LEVELS, CreditLevelLandingItem } from '../data/landingData';
import { trackEvent } from '../utils/analytics';
import { ScrollReveal } from '../utils/motionSystem';
import { kuotaSound } from '../utils/soundSystem';

/**
 * PRD §6.6 + FASE 3 & 6: Niveles a tu medida con transiciones sincronizadas,
 * microinteracciones táctiles y sonido opcional.
 */
export const LevelsSection: React.FC = () => {
  const [selectedLevelNumber, setSelectedLevelNumber] = useState<number>(1);

  const currentLevel: CreditLevelLandingItem =
    LANDING_CREDIT_LEVELS.find((item) => item.level === selectedLevelNumber) ||
    LANDING_CREDIT_LEVELS[0];

  const handleSelectLevel = (item: CreditLevelLandingItem) => {
    if (item.level === selectedLevelNumber) return;
    kuotaSound.playSelectOption();
    setSelectedLevelNumber(item.level);
    trackEvent('level_switcher_change', {
      level: item.level,
      purchase_amount: item.purchaseAmount,
      down_payment: item.downPayment,
      max_term: item.maxTerm,
    });
  };

  const handleKeyDown = (
    e: React.KeyboardEvent<HTMLButtonElement>,
    idx: number
  ) => {
    if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
      e.preventDefault();
      const next =
        LANDING_CREDIT_LEVELS[(idx + 1) % LANDING_CREDIT_LEVELS.length];
      handleSelectLevel(next);
    } else if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') {
      e.preventDefault();
      const prev =
        LANDING_CREDIT_LEVELS[
          (idx - 1 + LANDING_CREDIT_LEVELS.length) %
            LANDING_CREDIT_LEVELS.length
        ];
      handleSelectLevel(prev);
    }
  };

  const termBarWidthPercent = Math.max(
    14,
    Math.round((currentLevel.maxTermDays / 360) * 100)
  );

  return (
    <section
      id="niveles"
      aria-labelledby="levels-heading"
      className="py-16 sm:py-24 bg-white border-b border-slate-200/80"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-10">
        <ScrollReveal delayMs={0} direction="up">
          <div className="text-center max-w-2xl mx-auto">
            <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full bg-sky-50 border border-sky-200/80 text-xs font-bold text-[#0391FD] uppercase tracking-wider mb-3 shadow-xs">
              <TrendingUp className="w-3.5 h-3.5 text-[#0391FD]" aria-hidden="true" />
              Escalera de crecimiento
            </span>
            <h2
              id="levels-heading"
              className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-[#0A192F] tracking-tight mb-3"
            >
              Niveles a tu medida
            </h2>
            <p className="text-base sm:text-lg font-normal text-slate-600">
              Explora cómo mejoran tus condiciones conforme avanzas del Nivel 1 al Nivel 6.
            </p>
          </div>
        </ScrollReveal>

        {/* Switcher de 6 pestañas (Nivel 1 a 6) */}
        <ScrollReveal delayMs={70} direction="up">
          <div
            role="tablist"
            aria-label="Seleccionar nivel de crédito Kuota"
            className="grid grid-cols-3 sm:grid-cols-6 gap-2.5 max-w-3xl mx-auto"
          >
            {LANDING_CREDIT_LEVELS.map((item, idx) => {
              const isActive = item.level === currentLevel.level;
              return (
                <button
                  key={item.level}
                  type="button"
                  role="tab"
                  id={`level-tab-${item.level}`}
                  aria-selected={isActive}
                  aria-controls="level-active-panel"
                  onClick={() => handleSelectLevel(item)}
                  onKeyDown={(e) => handleKeyDown(e, idx)}
                  className={`py-3 px-3 rounded-2xl border font-bold text-sm sm:text-base transition-all duration-200 flex flex-col items-center justify-center cursor-pointer active:scale-95 ${
                    isActive
                      ? 'bg-[#0391FD] text-white border-[#0391FD] shadow-md -translate-y-0.5'
                      : 'bg-slate-50 hover:bg-slate-100 text-slate-700 border-slate-200/90'
                  }`}
                >
                  <span className="text-[11px] uppercase tracking-wider opacity-80">
                    Nivel
                  </span>
                  <span className="text-lg sm:text-xl leading-none mt-0.5 tabular-nums font-extrabold">
                    {item.level}
                  </span>
                </button>
              );
            })}
          </div>
        </ScrollReveal>

        {/* Franja fija bajo el switcher */}
        <ScrollReveal delayMs={120} direction="up">
          <div className="max-w-3xl mx-auto rounded-2xl bg-[#0A192F] text-white border border-white/10 px-5 py-3.5 shadow-xl flex flex-col sm:flex-row items-center justify-between gap-2 text-center sm:text-left">
            <div className="flex items-center gap-2">
              <Sparkles className="w-5 h-5 text-[#38BDF8] shrink-0" aria-hidden="true" />
              <span className="text-xs sm:text-sm font-medium">
                <strong className="font-extrabold text-[#38BDF8]">
                  ~5% de interés flat bimestral
                </strong>
                , transparente y sin penalidades ocultas.
              </span>
            </div>
            <span className="text-xs sm:text-sm font-bold text-slate-300">
              Tú eliges en cuántas kuotas pagar.
            </span>
          </div>
        </ScrollReveal>

        {/* Contenido principal: Card central dinámica + Gráfico de escalera ascendente */}
        <div
          id="level-active-panel"
          role="tabpanel"
          aria-labelledby={`level-tab-${currentLevel.level}`}
          className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-stretch pt-2"
        >
          {/* Card central que cambia según el nivel */}
          <ScrollReveal delayMs={150} direction="left" className="lg:col-span-6">
            <div className="h-full rounded-[28px] bg-white border border-slate-200/90 p-6 sm:p-8 shadow-xl flex flex-col justify-between">
              <div key={currentLevel.level} className="animate-fadeIn">
                <div className="flex items-center justify-between gap-3 mb-6 pb-4 border-b border-slate-100">
                  <div>
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
                      Condiciones del nivel seleccionado
                    </span>
                    <h3 className="text-2xl sm:text-3xl font-extrabold text-[#0A192F]">
                      Nivel {currentLevel.level}
                    </h3>
                  </div>
                  <span
                    style={{
                      backgroundColor: currentLevel.badgeBgHex,
                      color: currentLevel.badgeTextHex,
                    }}
                    className="px-3.5 py-1.5 rounded-full border border-slate-200/80 text-xs sm:text-sm font-bold transition-colors duration-300 shadow-xs"
                  >
                    Nivel {currentLevel.level} de 6
                  </span>
                </div>

                {/* 3 indicadores clave */}
                <div className="space-y-4">
                  <div className="rounded-2xl bg-slate-50 border border-slate-200/80 p-4">
                    <div className="flex items-center justify-between gap-2 mb-1">
                      <span className="inline-flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-slate-700">
                        <Wallet className="w-4 h-4 text-[#0391FD]" aria-hidden="true" />
                        Monto de compras
                      </span>
                      <span className="text-xs font-medium text-slate-500">
                        Línea de mercadería
                      </span>
                    </div>
                    <div className="text-2xl sm:text-3xl font-extrabold text-[#0A192F] tracking-tight tabular-nums">
                      {currentLevel.purchaseAmount}
                    </div>
                  </div>

                  <div className="rounded-2xl bg-slate-50 border border-slate-200/80 p-4">
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <span className="inline-flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-slate-700">
                        <Percent className="w-4 h-4 text-[#0391FD]" aria-hidden="true" />
                        Cuota inicial
                      </span>
                      <span className="text-xl sm:text-2xl font-extrabold text-[#0A192F] tabular-nums">
                        {currentLevel.downPayment}
                      </span>
                    </div>
                    <div className="w-full h-2.5 rounded-full bg-slate-200 overflow-hidden">
                      <div
                        className="h-full bg-[#0391FD] transition-all duration-500"
                        style={{ width: `${currentLevel.downPaymentPercent}%` }}
                      />
                    </div>
                    <p className="text-xs font-medium text-slate-500 mt-1.5">
                      {currentLevel.downPaymentPercent === 0
                        ? '¡Sin cuota inicial en el Nivel 6!'
                        : 'Disminuye 10% en cada nivel que subes'}
                    </p>
                  </div>

                  <div className="rounded-2xl bg-slate-50 border border-slate-200/80 p-4">
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <span className="inline-flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-slate-700">
                        <Calendar className="w-4 h-4 text-[#0391FD]" aria-hidden="true" />
                        Plazo para pagar
                      </span>
                      <span className="text-xl sm:text-2xl font-extrabold text-[#0391FD] tabular-nums">
                        {currentLevel.maxTerm}
                      </span>
                    </div>
                    <div className="w-full h-2.5 rounded-full bg-slate-200 overflow-hidden">
                      <div
                        className="h-full bg-[#10B981] transition-all duration-500"
                        style={{ width: `${termBarWidthPercent}%` }}
                      />
                    </div>
                    <p className="text-xs font-medium text-slate-500 mt-1.5">
                      Más tiempo para vender tu mercadería y pagar con holgura
                    </p>
                  </div>
                </div>
              </div>

              <div className="mt-6 pt-4 border-t border-slate-100 text-xs sm:text-sm font-semibold text-slate-600">
                {currentLevel.summaryTag}
              </div>
            </div>
          </ScrollReveal>

          {/* Gráfico de escalera ascendente de 6 niveles */}
          <ScrollReveal delayMs={220} direction="right" className="lg:col-span-6">
            <div className="h-full rounded-[28px] bg-white border border-slate-200/90 p-6 sm:p-8 shadow-xl flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between gap-2 mb-2">
                  <h3 className="text-lg sm:text-xl font-extrabold text-[#0A192F]">
                    Escalera ascendente de 6 niveles
                  </h3>
                  <span className="text-xs font-medium text-slate-500">
                    Toca cualquier peldaño
                  </span>
                </div>
                <p className="text-xs sm:text-sm font-normal text-slate-600 mb-6">
                  Cada nivel desbloquea mayor cupo comercial: tu cuota inicial disminuye y tu plazo de pago se amplía.
                </p>
              </div>

              <div
                role="group"
                aria-label="Gráfico de escalera de niveles 1 a 6"
                className="h-64 sm:h-72 grid grid-cols-6 gap-2 sm:gap-3 items-end pt-6 pb-2 px-2 rounded-2xl bg-slate-50 border border-slate-200/80"
              >
                {LANDING_CREDIT_LEVELS.map((stepItem) => {
                  const isStepActive = stepItem.level === currentLevel.level;
                  return (
                    <button
                      key={stepItem.level}
                      type="button"
                      onClick={() => handleSelectLevel(stepItem)}
                      aria-label={`Nivel ${stepItem.level}: ${stepItem.purchaseAmount}, inicial ${stepItem.downPayment}, plazo ${stepItem.maxTerm}`}
                      aria-pressed={isStepActive}
                      className="h-full flex flex-col justify-end items-center group focus:outline-none cursor-pointer"
                    >
                      <span
                        className={`text-[10px] sm:text-xs font-bold mb-1.5 text-center leading-tight transition-transform duration-200 ${
                          isStepActive
                            ? 'text-[#0391FD] scale-110 font-extrabold'
                            : 'text-slate-500'
                        }`}
                      >
                        {stepItem.downPayment}
                        <span className="block text-[9px] font-normal opacity-75">
                          inic.
                        </span>
                      </span>

                      <div
                        style={{
                          height: `${stepItem.stepHeightPercent}%`,
                          backgroundColor: stepItem.barColorHex,
                        }}
                        className={`w-full rounded-t-xl transition-all duration-300 flex flex-col items-center justify-between py-2 ${
                          isStepActive
                            ? 'ring-4 ring-[#0391FD]/30 -translate-y-1 shadow-md'
                            : 'opacity-85 hover:opacity-100'
                        }`}
                      >
                        <span
                          className={`text-xs sm:text-sm font-extrabold ${
                            stepItem.level >= 3 ? 'text-white' : 'text-[#0A192F]'
                          }`}
                        >
                          N{stepItem.level}
                        </span>
                        <span
                          className={`text-[9px] sm:text-[10px] font-bold px-1 text-center leading-none ${
                            stepItem.level >= 3
                              ? 'text-sky-200'
                              : 'text-slate-700'
                          }`}
                        >
                          {stepItem.maxTermDays}d
                        </span>
                      </div>
                    </button>
                  );
                })}
              </div>

              <div className="mt-6 overflow-x-auto rounded-2xl border border-slate-200/90">
                <table className="w-full text-left border-collapse text-xs sm:text-sm">
                  <thead>
                    <tr className="bg-[#0A192F] text-white font-bold">
                      <th className="py-2.5 px-3">Nivel</th>
                      <th className="py-2.5 px-3">Monto de compras</th>
                      <th className="py-2.5 px-3">Cuota inicial</th>
                      <th className="py-2.5 px-3">Plazo para pagar</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 bg-white">
                    {LANDING_CREDIT_LEVELS.map((row) => {
                      const isRowActive = row.level === currentLevel.level;
                      return (
                        <tr
                          key={row.level}
                          onClick={() => handleSelectLevel(row)}
                          className={`cursor-pointer transition-colors duration-150 ${
                            isRowActive
                              ? 'bg-sky-50 font-bold text-[#0391FD]'
                              : 'hover:bg-slate-50 font-medium text-slate-700'
                          }`}
                        >
                          <td className="py-2 px-3">{row.level}</td>
                          <td className="py-2 px-3">{row.purchaseAmount}</td>
                          <td className="py-2 px-3">{row.downPayment}</td>
                          <td className="py-2 px-3">{row.maxTerm}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          </ScrollReveal>
        </div>
      </div>
    </section>
  );
};

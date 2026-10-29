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
      className="py-14 sm:py-20 border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <ScrollReveal delayMs={0} direction="up">
          <div className="text-center max-w-2xl mx-auto mb-10">
            <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full bg-white border-2 border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-3">
              <TrendingUp className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
              Escalera de crecimiento
            </span>
            <h2
              id="levels-heading"
              className="text-3xl sm:text-4xl font-extrabold text-kuota-deepnavy tracking-tight mb-3"
            >
              Niveles a tu medida
            </h2>
            <p className="text-base sm:text-lg font-semibold text-kuota-crayola">
              Explora cómo mejoran tus condiciones conforme avanzas del Nivel 1 al Nivel 6.
            </p>
          </div>
        </ScrollReveal>

        {/* Switcher de 6 pestañas (Nivel 1 a 6) */}
        <ScrollReveal delayMs={70} direction="up">
          <div
            role="tablist"
            aria-label="Seleccionar nivel de crédito Kuota"
            className="grid grid-cols-3 sm:grid-cols-6 gap-2.5 max-w-3xl mx-auto mb-6"
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
                  className={`py-3 px-3 rounded-2xl border-2 border-kuota-deepnavy font-extrabold text-sm sm:text-base transition-all duration-200 flex flex-col items-center justify-center active:scale-95 ${
                    isActive
                      ? 'bg-kuota-dodger text-white shadow-navy-hard -translate-y-0.5'
                      : 'bg-white/85 hover:bg-white text-kuota-deepnavy'
                  }`}
                >
                  <span className="text-[11px] uppercase tracking-wider opacity-85">
                    Nivel
                  </span>
                  <span className="text-lg sm:text-xl leading-none mt-0.5 tabular-nums">
                    {item.level}
                  </span>
                </button>
              );
            })}
          </div>
        </ScrollReveal>

        {/* Franja fija bajo el switcher (PRD §6.6) */}
        <ScrollReveal delayMs={120} direction="up">
          <div className="max-w-3xl mx-auto mb-8 rounded-2xl bg-kuota-deepnavy text-white border-2 border-kuota-deepnavy px-5 py-3.5 shadow-navy-hard flex flex-col sm:flex-row items-center justify-between gap-2 text-center sm:text-left">
            <div className="flex items-center gap-2">
              <Sparkles className="w-5 h-5 text-kuota-dodger shrink-0" aria-hidden="true" />
              <span className="text-sm sm:text-base font-medium">
                <strong className="font-extrabold text-kuota-palesky">
                  5% de interés SIEMPRE
                </strong>
                , con límite flat bimestral.
              </span>
            </div>
            <span className="text-sm sm:text-base font-bold italic text-kuota-palesky">
              Tú eliges en cuántas kuotas pagar.
            </span>
          </div>
        </ScrollReveal>

        {/* Contenido principal: Card central dinámica + Gráfico de escalera ascendente */}
        <div
          id="level-active-panel"
          role="tabpanel"
          aria-labelledby={`level-tab-${currentLevel.level}`}
          className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-stretch"
        >
          {/* Card central que cambia según el nivel */}
          <ScrollReveal delayMs={150} direction="left" className="lg:col-span-6">
            <div className="h-full rounded-3xl bg-white border-2 border-kuota-deepnavy p-6 sm:p-8 shadow-navy-hard-lg flex flex-col justify-between">
              <div key={currentLevel.level} className="animate-fadeIn">
                <div className="flex items-center justify-between gap-3 mb-6 pb-4 border-b-2 border-kuota-palesky">
                  <div>
                    <span className="text-xs font-extrabold uppercase tracking-wider text-kuota-crayola">
                      Condiciones del nivel seleccionado
                    </span>
                    <h3 className="text-2xl sm:text-3xl font-extrabold text-kuota-deepnavy">
                      Nivel {currentLevel.level}
                    </h3>
                  </div>
                  <span
                    style={{
                      backgroundColor: currentLevel.badgeBgHex,
                      color: currentLevel.badgeTextHex,
                    }}
                    className="px-3.5 py-1.5 rounded-xl border-2 border-kuota-deepnavy text-xs sm:text-sm font-extrabold transition-colors duration-300"
                  >
                    Nivel {currentLevel.level} de 6
                  </span>
                </div>

                {/* 3 indicadores clave */}
                <div className="space-y-5">
                  <div className="rounded-2xl bg-kuota-palesky/70 border-2 border-kuota-deepnavy p-4">
                    <div className="flex items-center justify-between gap-2 mb-1">
                      <span className="inline-flex items-center gap-1.5 text-xs font-extrabold uppercase tracking-wider text-kuota-deepnavy">
                        <Wallet className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                        Monto de compras
                      </span>
                      <span className="text-xs font-bold text-kuota-crayola">
                        Línea de mercadería
                      </span>
                    </div>
                    <div className="text-2xl sm:text-3xl font-extrabold text-kuota-deepnavy tracking-tight tabular-nums">
                      {currentLevel.purchaseAmount}
                    </div>
                  </div>

                  <div className="rounded-2xl bg-white border-2 border-kuota-deepnavy p-4">
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <span className="inline-flex items-center gap-1.5 text-xs font-extrabold uppercase tracking-wider text-kuota-deepnavy">
                        <Percent className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                        Cuota inicial
                      </span>
                      <span className="text-2xl font-extrabold text-kuota-deepnavy tabular-nums">
                        {currentLevel.downPayment}
                      </span>
                    </div>
                    <div className="w-full h-3 rounded-full bg-kuota-palesky border border-kuota-deepnavy overflow-hidden">
                      <div
                        className="h-full bg-kuota-crayola transition-all duration-500"
                        style={{ width: `${currentLevel.downPaymentPercent}%` }}
                      />
                    </div>
                    <p className="text-xs font-bold text-kuota-crayola mt-1.5">
                      {currentLevel.downPaymentPercent === 0
                        ? '¡Sin cuota inicial en el Nivel 6!'
                        : 'Disminuye 10% en cada nivel que subes'}
                    </p>
                  </div>

                  <div className="rounded-2xl bg-white border-2 border-kuota-deepnavy p-4">
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <span className="inline-flex items-center gap-1.5 text-xs font-extrabold uppercase tracking-wider text-kuota-deepnavy">
                        <Calendar className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                        Plazo para pagar
                      </span>
                      <span className="text-xl sm:text-2xl font-extrabold text-kuota-dodger tabular-nums">
                        {currentLevel.maxTerm}
                      </span>
                    </div>
                    <div className="w-full h-3 rounded-full bg-kuota-palesky border border-kuota-deepnavy overflow-hidden">
                      <div
                        className="h-full bg-kuota-dodger transition-all duration-500"
                        style={{ width: `${termBarWidthPercent}%` }}
                      />
                    </div>
                    <p className="text-xs font-bold text-kuota-crayola mt-1.5">
                      Más tiempo para vender tu mercadería y pagar con holgura
                    </p>
                  </div>
                </div>
              </div>

              <div className="mt-6 pt-4 border-t border-kuota-deepnavy/15 text-xs sm:text-sm font-bold text-kuota-deepnavy">
                {currentLevel.summaryTag}
              </div>
            </div>
          </ScrollReveal>

          {/* Gráfico de escalera ascendente de 6 niveles */}
          <ScrollReveal delayMs={220} direction="right" className="lg:col-span-6">
            <div className="h-full rounded-3xl bg-white border-2 border-kuota-deepnavy p-6 sm:p-8 shadow-navy-hard-lg flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between gap-2 mb-2">
                  <h3 className="text-lg sm:text-xl font-extrabold text-kuota-deepnavy">
                    Escalera ascendente de 6 niveles
                  </h3>
                  <span className="text-xs font-bold text-kuota-crayola">
                    Toca cualquier peldaño
                  </span>
                </div>
                <p className="text-xs sm:text-sm font-semibold text-kuota-crayola mb-6">
                  Cada nivel tiene su propio tono azul de marca. Al subir de nivel, tu cuota inicial baja y tu plazo crece.
                </p>
              </div>

              <div
                role="group"
                aria-label="Gráfico de escalera de niveles 1 a 6"
                className="h-64 sm:h-72 grid grid-cols-6 gap-2.5 sm:gap-3.5 items-end pt-6 pb-2 px-2 rounded-2xl bg-kuota-palesky/55 border-2 border-kuota-deepnavy"
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
                      className="h-full flex flex-col justify-end items-center group focus:outline-none"
                    >
                      <span
                        className={`text-[10px] sm:text-xs font-extrabold mb-1.5 text-center leading-tight transition-transform duration-200 ${
                          isStepActive
                            ? 'text-kuota-deepnavy scale-110'
                            : 'text-kuota-deepnavy/75'
                        }`}
                      >
                        {stepItem.downPayment}
                        <span className="block text-[9px] font-bold opacity-80">
                          inic.
                        </span>
                      </span>

                      <div
                        style={{
                          height: `${stepItem.stepHeightPercent}%`,
                          backgroundColor: stepItem.barColorHex,
                        }}
                        className={`w-full rounded-t-xl border-2 border-kuota-deepnavy transition-all duration-300 flex flex-col items-center justify-between py-2 ${
                          isStepActive
                            ? 'ring-4 ring-kuota-dodger/45 -translate-y-1 shadow-navy-hard'
                            : 'opacity-80 hover:opacity-100'
                        }`}
                      >
                        <span
                          className={`text-xs sm:text-sm font-extrabold ${
                            stepItem.level >= 3 ? 'text-white' : 'text-kuota-deepnavy'
                          }`}
                        >
                          N{stepItem.level}
                        </span>
                        <span
                          className={`text-[9px] sm:text-[10px] font-bold px-1 text-center leading-none ${
                            stepItem.level >= 3
                              ? 'text-kuota-palesky'
                              : 'text-kuota-deepnavy'
                          }`}
                        >
                          {stepItem.maxTermDays}d
                        </span>
                      </div>
                    </button>
                  );
                })}
              </div>

              <div className="mt-6 overflow-x-auto rounded-2xl border-2 border-kuota-deepnavy">
                <table className="w-full text-left border-collapse text-xs sm:text-sm">
                  <thead>
                    <tr className="bg-kuota-deepnavy text-white font-extrabold">
                      <th className="py-2.5 px-3">Nivel</th>
                      <th className="py-2.5 px-3">Monto de compras</th>
                      <th className="py-2.5 px-3">Cuota inicial</th>
                      <th className="py-2.5 px-3">Plazo para pagar</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-kuota-deepnavy/20 bg-white">
                    {LANDING_CREDIT_LEVELS.map((row) => {
                      const isRowActive = row.level === currentLevel.level;
                      return (
                        <tr
                          key={row.level}
                          onClick={() => handleSelectLevel(row)}
                          className={`cursor-pointer transition-colors duration-150 ${
                            isRowActive
                              ? 'bg-kuota-palesky font-extrabold text-kuota-deepnavy'
                              : 'hover:bg-kuota-palesky/40 font-semibold text-kuota-deepnavy/85'
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

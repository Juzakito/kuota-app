import React, { useState } from 'react';
import { Smartphone, CheckCircle2, Info, ChevronRight } from 'lucide-react';
import { PRODUCT_CHIPS, ProductChipItem } from '../data/landingData';
import { trackEvent } from '../utils/analytics';

/**
 * PRD §6.4: Producto: 3 chips con captura interactiva
 * - Captura de la app a la izquierda y 3 chips a la derecha.
 * - Al hacer clic en un chip cambia la captura y se despliega su texto.
 * - El primer chip está activo por defecto.
 * - En móvil, la captura va arriba y los chips debajo.
 */
export const ProductChipsSection: React.FC = () => {
  const [activeChipId, setActiveChipId] = useState<string>(PRODUCT_CHIPS[0].id);

  const activeChip: ProductChipItem =
    PRODUCT_CHIPS.find((c) => c.id === activeChipId) || PRODUCT_CHIPS[0];

  const handleSelectChip = (chip: ProductChipItem) => {
    if (chip.id === activeChipId) return;
    setActiveChipId(chip.id);
    trackEvent('product_chip_change', {
      chip_id: chip.id,
      chip_index: chip.index,
      chip_title: chip.title,
    });
  };

  const handleKeyDown = (
    e: React.KeyboardEvent<HTMLButtonElement>,
    index: number
  ) => {
    if (e.key === 'ArrowDown' || e.key === 'ArrowRight') {
      e.preventDefault();
      const next = PRODUCT_CHIPS[(index + 1) % PRODUCT_CHIPS.length];
      handleSelectChip(next);
    } else if (e.key === 'ArrowUp' || e.key === 'ArrowLeft') {
      e.preventDefault();
      const prev =
        PRODUCT_CHIPS[(index - 1 + PRODUCT_CHIPS.length) % PRODUCT_CHIPS.length];
      handleSelectChip(prev);
    }
  };

  return (
    <section
      id="producto"
      aria-labelledby="product-chips-heading"
      className="py-14 sm:py-20 border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="max-w-2xl mb-10">
          <span className="inline-block px-3 py-1 rounded-full bg-white border-2 border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-3">
            Cómo funciona Kuota
          </span>
          <h2
            id="product-chips-heading"
            className="text-3xl sm:text-4xl font-extrabold text-kuota-deepnavy tracking-tight"
          >
            Diseñado para que tu negocio nunca pare de surtirse
          </h2>
        </div>

        {/* En móvil: captura arriba, chips debajo. En desktop: captura a la izquierda (5 cols), chips a la derecha (7 cols) */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-10 items-center">
          {/* Captura interactiva (placeholder) — Izquierda en desktop, arriba en móvil */}
          <div className="lg:col-span-5 flex justify-center">
            <div
              role="img"
              aria-label={activeChip.placeholderLabel}
              aria-live="polite"
              className="w-full max-w-sm rounded-3xl border-2 border-kuota-deepnavy bg-white p-5 shadow-navy-hard-lg placeholder-stripes transition-all duration-300"
            >
              {/* Metadatos del placeholder */}
              <div className="flex items-center justify-between gap-2 mb-4">
                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-kuota-deepnavy text-white text-xs font-bold uppercase">
                  <Smartphone className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                  Placeholder · {activeChip.placeholderId}
                </span>
                <span className="px-2.5 py-1 rounded-md bg-kuota-palesky border border-kuota-deepnavy text-kuota-deepnavy text-xs font-extrabold">
                  Proporción {activeChip.placeholderAspectRatio}
                </span>
              </div>

              {/* Simulación de pantalla móvil según el chip activo */}
              <div className="rounded-2xl border-2 border-kuota-deepnavy bg-kuota-deepnavy text-white p-4 sm:p-5 mb-4">
                <div className="flex items-center justify-between mb-3 pb-2.5 border-b border-white/15">
                  <span className="text-xs font-extrabold uppercase tracking-wider text-kuota-palesky">
                    Paso {activeChip.index} de 3
                  </span>
                  <span className="px-2 py-0.5 rounded bg-kuota-dodger text-white text-xs font-extrabold">
                    {activeChip.title}
                  </span>
                </div>

                <h3 className="text-lg font-extrabold text-white mb-1">
                  {activeChip.screenMockTitle}
                </h3>
                <p className="text-xs text-kuota-palesky font-medium mb-4">
                  {activeChip.screenMockSubtitle}
                </p>

                <div className="space-y-2.5">
                  {activeChip.screenMockMetrics.map((metric) => (
                    <div
                      key={metric.label}
                      className="rounded-xl bg-white/10 border border-white/15 px-3.5 py-2.5 flex items-center justify-between gap-2"
                    >
                      <span className="text-xs text-kuota-palesky font-medium">
                        {metric.label}
                      </span>
                      <span className="text-xs sm:text-sm font-extrabold text-white">
                        {metric.value}
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Etiqueta oficial del placeholder requerida por PRD */}
              <div className="rounded-xl bg-kuota-palesky border border-kuota-deepnavy px-3.5 py-2.5">
                <p className="text-xs sm:text-sm font-extrabold text-kuota-deepnavy">
                  {activeChip.placeholderLabel}
                </p>
              </div>
            </div>
          </div>

          {/* 3 Chips interactivos — Derecha en desktop, debajo en móvil */}
          <div
            role="tablist"
            aria-label="Características del producto Kuota"
            className="lg:col-span-7 flex flex-col gap-4"
          >
            {PRODUCT_CHIPS.map((chip, idx) => {
              const isSelected = chip.id === activeChip.id;
              return (
                <div
                  key={chip.id}
                  className={`rounded-2xl border-2 border-kuota-deepnavy transition-all duration-200 ${
                    isSelected
                      ? 'bg-white shadow-navy-hard-lg'
                      : 'bg-white/60 hover:bg-white/90 shadow-sm'
                  }`}
                >
                  <button
                    type="button"
                    role="tab"
                    id={`tab-${chip.id}`}
                    aria-selected={isSelected}
                    aria-controls={`panel-${chip.id}`}
                    onClick={() => handleSelectChip(chip)}
                    onKeyDown={(e) => handleKeyDown(e, idx)}
                    className="w-full text-left p-5 sm:p-6 flex items-center justify-between gap-4"
                  >
                    <div className="flex items-center gap-3.5">
                      <span
                        className={`w-9 h-9 rounded-xl border-2 border-kuota-deepnavy flex items-center justify-center text-sm font-extrabold shrink-0 transition-colors ${
                          isSelected
                            ? 'bg-kuota-dodger text-white'
                            : 'bg-kuota-palesky text-kuota-deepnavy'
                        }`}
                      >
                        {chip.index}
                      </span>
                      <span className="text-xl sm:text-2xl font-extrabold text-kuota-deepnavy">
                        {chip.title}
                      </span>
                    </div>

                    <span
                      className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-extrabold border border-kuota-deepnavy transition-colors ${
                        isSelected
                          ? 'bg-kuota-dodger text-white'
                          : 'bg-kuota-palesky text-kuota-deepnavy'
                      }`}
                    >
                      {isSelected ? 'Activo' : 'Ver'}
                      <ChevronRight
                        className={`w-4 h-4 transition-transform ${
                          isSelected ? 'rotate-90' : ''
                        }`}
                        aria-hidden="true"
                      />
                    </span>
                  </button>

                  {isSelected && (
                    <div
                      id={`panel-${chip.id}`}
                      role="tabpanel"
                      aria-labelledby={`tab-${chip.id}`}
                      className="px-5 sm:px-6 pb-6 pt-1 border-t border-kuota-deepnavy/15 space-y-3"
                    >
                      <ul className="space-y-2.5">
                        {chip.bullets.map((bullet) => (
                          <li
                            key={bullet}
                            className="flex items-start gap-2.5 text-base sm:text-lg font-semibold text-kuota-crayola"
                          >
                            <CheckCircle2
                              className="w-5 h-5 text-kuota-dodger shrink-0 mt-1"
                              aria-hidden="true"
                            />
                            <span className="text-kuota-deepnavy/90">{bullet}</span>
                          </li>
                        ))}
                      </ul>

                      {chip.note && (
                        <div className="mt-3 rounded-xl bg-kuota-palesky border border-kuota-deepnavy p-3.5 flex items-start gap-2.5">
                          <Info
                            className="w-5 h-5 text-kuota-deepnavy shrink-0 mt-0.5"
                            aria-hidden="true"
                          />
                          <p className="text-sm font-bold italic text-kuota-deepnavy">
                            Nota: {chip.note}
                          </p>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
};

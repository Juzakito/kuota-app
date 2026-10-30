import React, { useState } from 'react';
import { Smartphone, Clock, ChevronRight, ShieldCheck } from 'lucide-react';
import {
  SUPPLIER_PRODUCT_CHIPS,
  SupplierProductChipItem,
  PRICING_CONFIG,
} from '../../config/pricingConfig';
import { trackSupplierEvent } from '../../utils/analytics';
import { kuotaSound } from '../../utils/soundSystem';

/**
 * PRD §5.5: Producto – tres chips
 * - A la izquierda, capturas de celular. A la derecha, tres chips.
 * - Al hacer clic en un chip cambian la captura y el texto asociado.
 * - El primer chip está activo al cargar.
 * - En móvil, los chips van arriba y la captura debajo.
 * - Badge fijo bajo los chips: "Liquidación en 48 horas hábiles en todas las modalidades".
 */
export const ProductChipsSection: React.FC = () => {
  const [activeChipId, setActiveChipId] = useState<string>(
    SUPPLIER_PRODUCT_CHIPS[0].id
  );
  const [activeCaptureIdx, setActiveCaptureIdx] = useState<number>(0);

  const activeChip: SupplierProductChipItem =
    SUPPLIER_PRODUCT_CHIPS.find((c) => c.id === activeChipId) ||
    SUPPLIER_PRODUCT_CHIPS[0];

  const handleSelectChip = (chip: SupplierProductChipItem) => {
    kuotaSound.playSelectOption();
    setActiveChipId(chip.id);
    setActiveCaptureIdx(0);
    trackSupplierEvent('supplier_product_chip_change', {
      chip_id: chip.id,
      chip_title: chip.title,
      captures_count: chip.captures.length,
    });
  };

  const handleKeyDown = (
    e: React.KeyboardEvent<HTMLButtonElement>,
    idx: number
  ) => {
    if (e.key === 'ArrowDown' || e.key === 'ArrowRight') {
      e.preventDefault();
      const next =
        SUPPLIER_PRODUCT_CHIPS[(idx + 1) % SUPPLIER_PRODUCT_CHIPS.length];
      handleSelectChip(next);
    } else if (e.key === 'ArrowUp' || e.key === 'ArrowLeft') {
      e.preventDefault();
      const prev =
        SUPPLIER_PRODUCT_CHIPS[
          (idx - 1 + SUPPLIER_PRODUCT_CHIPS.length) %
            SUPPLIER_PRODUCT_CHIPS.length
        ];
      handleSelectChip(prev);
    }
  };

  const currentCapture =
    activeChip.captures[activeCaptureIdx] || activeChip.captures[0];

  return (
    <section
      id="producto"
      aria-labelledby="supplier-product-heading"
      className="py-16 sm:py-20 border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="max-w-2xl mb-10">
          <span className="inline-block px-3.5 py-1 rounded-full bg-white border-2 border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-3">
            Modalidades de cobro
          </span>
          <h2
            id="supplier-product-heading"
            className="text-3xl sm:text-4xl font-extrabold text-kuota-deepnavy tracking-tight"
          >
            Tres formas de cobrar, todas desde tu celular
          </h2>
        </div>

        {/* En móvil: chips arriba (order-1) y captura debajo (order-2). En desktop: captura izquierda (order-1), chips derecha (order-2) */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-10 items-start">
          {/* Columna de Capturas (IMG-10 a IMG-16): izquierda en desktop, debajo en móvil */}
          <div className="order-2 lg:order-1 lg:col-span-5 flex flex-col items-center">
            <div className="w-full max-w-sm rounded-3xl border-2 border-kuota-deepnavy bg-white p-5 shadow-navy-hard-lg placeholder-stripes">
              {/* Selector de secuencia de pantallas del chip actual */}
              <div className="flex items-center justify-between gap-2 mb-3">
                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-kuota-deepnavy text-white text-xs font-bold uppercase">
                  <Smartphone className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                  Placeholder · {currentCapture.placeholderId}
                </span>
                <span className="px-2.5 py-1 rounded-md bg-kuota-palesky border border-kuota-deepnavy text-kuota-deepnavy text-xs font-extrabold">
                  {currentCapture.dimensions}
                </span>
              </div>

              {/* Pestañas de secuencia (Principal + Complementaria o 3 pasos de aKuotas!) */}
              <div
                role="group"
                aria-label="Secuencia de capturas del flujo"
                className="grid grid-cols-2 sm:flex sm:flex-wrap gap-1.5 mb-4"
              >
                {activeChip.captures.map((cap, i) => {
                  const isCapActive = i === activeCaptureIdx;
                  return (
                    <button
                      key={cap.placeholderId}
                      type="button"
                      onClick={() => setActiveCaptureIdx(i)}
                      className={`flex-1 py-1.5 px-2.5 rounded-xl border border-kuota-deepnavy text-xs font-extrabold transition-colors ${
                        isCapActive
                          ? 'bg-kuota-dodger text-white'
                          : 'bg-kuota-palesky/80 text-kuota-deepnavy hover:bg-kuota-palesky'
                      }`}
                    >
                      {cap.placeholderId} ({i + 1}/{activeChip.captures.length})
                    </button>
                  );
                })}
              </div>

              {/* Mockup esquemático de la captura 390×800 con transición animada */}
              <div
                key={`${activeChip.id}-${activeCaptureIdx}`}
                role="img"
                aria-label={`${currentCapture.placeholderId}: ${currentCapture.description}`}
                aria-live="polite"
                className="rounded-2xl border-2 border-kuota-deepnavy bg-kuota-deepnavy text-white p-5 mb-4 kuota-section-arrive"
              >
                <div className="flex items-center justify-between text-xs text-kuota-palesky font-bold pb-2.5 mb-3 border-b border-white/15">
                  <span>{currentCapture.mockHeader}</span>
                  <span className="px-2 py-0.5 rounded bg-kuota-dodger text-white font-extrabold">
                    {currentCapture.placeholderId}
                  </span>
                </div>

                <div className="rounded-xl bg-kuota-dodger/25 border border-kuota-palesky/40 p-3.5 mb-3 text-center">
                  <div className="text-base sm:text-lg font-extrabold text-white">
                    {currentCapture.mockHighlight}
                  </div>
                </div>

                <div className="space-y-2">
                  {currentCapture.mockRows.map((row) => (
                    <div
                      key={row.label}
                      className="rounded-xl bg-white/10 border border-white/15 px-3 py-2 flex items-center justify-between gap-2 text-xs"
                    >
                      <span className="text-kuota-palesky font-medium">
                        {row.label}
                      </span>
                      <span className="font-extrabold text-white">
                        {row.value}
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Etiqueta descriptiva del inventario de imágenes (§11) */}
              <div className="rounded-xl bg-kuota-palesky border border-kuota-deepnavy px-3.5 py-2.5 text-left">
                <p className="text-xs sm:text-sm font-extrabold text-kuota-deepnavy">
                  {currentCapture.placeholderId}: {currentCapture.description}
                </p>
              </div>
            </div>
          </div>

          {/* Columna de 3 Chips + Badge fijo debajo: derecha en desktop, arriba en móvil */}
          <div className="order-1 lg:order-2 lg:col-span-7 flex flex-col gap-4">
            <div
              role="tablist"
              aria-label="Soluciones de cobro Kuota para proveedores"
              className="flex flex-col gap-4"
            >
              {SUPPLIER_PRODUCT_CHIPS.map((chip, idx) => {
                const isSelected = chip.id === activeChip.id;
                return (
                  <div
                    key={chip.id}
                    className={`rounded-2xl border-2 border-kuota-deepnavy transition-all duration-200 ${
                      isSelected
                        ? 'bg-white shadow-navy-hard-lg'
                        : 'bg-white/65 hover:bg-white/95 shadow-sm'
                    }`}
                  >
                    <button
                      type="button"
                      role="tab"
                      id={`supplier-tab-${chip.id}`}
                      aria-selected={isSelected}
                      aria-controls={`supplier-panel-${chip.id}`}
                      onClick={() => handleSelectChip(chip)}
                      onKeyDown={(e) => handleKeyDown(e, idx)}
                      className="w-full text-left p-5 sm:p-6 flex items-center justify-between gap-3"
                    >
                      <div className="flex flex-wrap items-center gap-3">
                        <span
                          className={`w-9 h-9 rounded-xl border-2 border-kuota-deepnavy flex items-center justify-center text-sm font-extrabold shrink-0 ${
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
                        {chip.badge && (
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-kuota-deepnavy text-kuota-palesky text-xs font-extrabold uppercase tracking-wider">
                            <ShieldCheck className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                            {chip.badge}
                          </span>
                        )}
                      </div>

                      <ChevronRight
                        className={`w-5 h-5 text-kuota-deepnavy shrink-0 transition-transform ${
                          isSelected ? 'rotate-90 text-kuota-dodger' : ''
                        }`}
                        aria-hidden="true"
                      />
                    </button>

                    {isSelected && (
                      <div
                        id={`supplier-panel-${chip.id}`}
                        role="tabpanel"
                        aria-labelledby={`supplier-tab-${chip.id}`}
                        className="px-5 sm:px-6 pb-6 pt-2 border-t border-kuota-deepnavy/15 space-y-3"
                      >
                        <p className="text-base sm:text-lg font-bold text-kuota-deepnavy/90 leading-relaxed">
                          {chip.description}
                        </p>

                        <div className="flex flex-wrap items-center gap-2 pt-1">
                          <span className="text-xs font-extrabold text-kuota-crayola">
                            Capturas asociadas:
                          </span>
                          {chip.captures.map((cap, capIdx) => (
                            <button
                              key={cap.placeholderId}
                              type="button"
                              onClick={() => setActiveCaptureIdx(capIdx)}
                              className={`text-xs font-extrabold px-2.5 py-1 rounded-lg border border-kuota-deepnavy transition-colors ${
                                activeCaptureIdx === capIdx
                                  ? 'bg-kuota-dodger text-white'
                                  : 'bg-kuota-palesky text-kuota-deepnavy hover:bg-kuota-dodger/20'
                              }`}
                            >
                              {cap.placeholderId}: {cap.title}
                            </button>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>

            {/* Badge fijo bajo los chips (PRD §5.5) */}
            <div className="rounded-2xl bg-kuota-deepnavy text-white border-2 border-kuota-deepnavy px-5 py-4 shadow-navy-hard flex items-center gap-3">
              <Clock className="w-6 h-6 text-kuota-dodger shrink-0" aria-hidden="true" />
              <span className="text-sm sm:text-base font-extrabold tracking-tight">
                Liquidación en {PRICING_CONFIG.settlementBusinessHours} horas hábiles en todas las modalidades
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};


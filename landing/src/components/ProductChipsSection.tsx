import React, { useEffect, useState } from 'react';
import {
  Smartphone,
  CheckCircle2,
  Info,
  ChevronRight,
  Calendar,
  Play,
  RotateCcw,
  ShieldCheck,
  TrendingUp,
  BarChart3,
  PackageCheck,
} from 'lucide-react';
import { PRODUCT_CHIPS, ProductChipItem } from '../data/landingData';
import { trackEvent } from '../utils/analytics';
import { ScrollReveal, TiltCard } from '../utils/motionSystem';
import { kuotaSound } from '../utils/soundSystem';

interface DemoStageItem {
  stage: number;
  key: string;
  label: string;
  title: string;
  subtitle: string;
}

const DEMO_STAGES: DemoStageItem[] = [
  {
    stage: 1,
    key: 'descubrimiento',
    label: '1. Descubrimiento',
    title: 'Aparece tu compra de mercadería',
    subtitle: 'Eliges el lote con tu proveedor afiliado y escaneas el código QR desde Kuota.',
  },
  {
    stage: 2,
    key: 'transformacion',
    label: '2. Selección y división',
    title: 'El monto se transforma en kuotas claras',
    subtitle: 'Visualizas al instante tu cuota inicial y el saldo fraccionado con 5% de interés flat.',
  },
  {
    stage: 3,
    key: 'cronograma',
    label: '3. Elección de kuotas',
    title: 'Calendario y línea de tiempo de pagos',
    subtitle: 'Tus kuotas se distribuyen con fechas claras para que pagues con las ventas de tu mercadería.',
  },
  {
    stage: 4,
    key: 'confirmacion',
    label: '4. Confirmación',
    title: 'Operación confirmada en 3 clics',
    subtitle: 'Sin papeleo ni historial Infocorp. Tu proveedor despacha tu mercadería de inmediato.',
  },
  {
    stage: 5,
    key: 'seguimiento',
    label: '5. Seguimiento',
    title: 'Control de pagos y subida de nivel',
    subtitle: 'Cada kuota pagada a tiempo actualiza tu progreso y desbloquea mayor capital en el siguiente nivel.',
  },
];

const DEMO_INSTALLMENTS = [
  {
    id: 'inicial',
    label: 'Cuota Inicial (30%)',
    dateLabel: 'Hoy · Al confirmar',
    amount: 'S/ 360',
    statusByStage: (s: number) => (s >= 4 ? 'Pagada' : 'Por confirmar'),
  },
  {
    id: 'kuota-1',
    label: 'Kuota 1 de 3',
    dateLabel: 'Día 20 · 30 Nov',
    amount: 'S/ 300',
    statusByStage: (s: number) => (s >= 5 ? 'Pagada' : s >= 3 ? 'Programada' : 'En cálculo'),
  },
  {
    id: 'kuota-2',
    label: 'Kuota 2 de 3',
    dateLabel: 'Día 40 · 20 Dic',
    amount: 'S/ 300',
    statusByStage: (s: number) => (s >= 5 ? 'Pagada' : s >= 3 ? 'Programada' : 'En cálculo'),
  },
  {
    id: 'kuota-3',
    label: 'Kuota 3 de 3',
    dateLabel: 'Día 60 · 09 Ene',
    amount: 'S/ 300',
    statusByStage: (s: number) => (s >= 5 ? 'Proxima / Al día' : s >= 3 ? 'Programada' : 'En cálculo'),
  },
];

/**
 * PRD §6.4 + FASE 3.B, 3.D y 3.E:
 * - 3 chips interactivos con visualización animada específica para cada beneficio.
 * - Demostración visual interactiva de compra en kuotas (5 etapas conectadas con calendario,
 *   estados de pago y confirmación).
 */
export const ProductChipsSection: React.FC = () => {
  const [activeChipId, setActiveChipId] = useState<string>(PRODUCT_CHIPS[0].id);
  const [demoStage, setDemoStage] = useState<number>(3);
  const [isAutoPlaying, setIsAutoPlaying] = useState<boolean>(false);

  const activeChip: ProductChipItem =
    PRODUCT_CHIPS.find((c) => c.id === activeChipId) || PRODUCT_CHIPS[0];

  const currentStageMeta =
    DEMO_STAGES.find((s) => s.stage === demoStage) || DEMO_STAGES[2];

  const handleSelectChip = (chip: ProductChipItem) => {
    if (chip.id === activeChipId) return;
    kuotaSound.playSelectOption();
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

  const handleSelectStage = (stageNum: number) => {
    setIsAutoPlaying(false);
    setDemoStage(stageNum);
    if (stageNum === 4) {
      kuotaSound.playConfirmOperation();
    } else if (stageNum === 5) {
      kuotaSound.playDemoComplete();
    } else {
      kuotaSound.playSelectOption();
    }
    trackEvent('installment_demo_stage_select', { stage: stageNum });
  };

  const handleStartAutoDemo = () => {
    kuotaSound.playCtaClick();
    setDemoStage(1);
    setIsAutoPlaying(true);
    trackEvent('installment_demo_autoplay_start', {});
  };

  useEffect(() => {
    if (!isAutoPlaying) return;
    const timer = window.setTimeout(() => {
      if (demoStage < 5) {
        const next = demoStage + 1;
        setDemoStage(next);
        if (next === 4) {
          kuotaSound.playConfirmOperation();
        } else if (next === 5) {
          kuotaSound.playDemoComplete();
          setIsAutoPlaying(false);
        } else {
          kuotaSound.playSelectOption();
        }
      } else {
        setIsAutoPlaying(false);
      }
    }, 1500);

    return () => window.clearTimeout(timer);
  }, [isAutoPlaying, demoStage]);

  return (
    <section
      id="producto"
      aria-labelledby="product-chips-heading"
      className="py-14 sm:py-20 border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-14">
        <ScrollReveal delayMs={0} direction="up">
          <div className="max-w-2xl">
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
        </ScrollReveal>

        {/* Bloque 1: Captura interactiva + 3 Chips de beneficios con visualización dinámica (FASE 3.D) */}
        <div
          id="beneficios"
          className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-10 items-center"
        >
          {/* Captura interactiva con microanimaciones explicativas según el beneficio activo */}
          <ScrollReveal delayMs={80} direction="left" className="lg:col-span-5 flex justify-center">
            <TiltCard maxTiltDeg={2.5} className="w-full max-w-sm">
              <div
                role="img"
                aria-label={activeChip.placeholderLabel}
                aria-live="polite"
                className="w-full rounded-3xl border-2 border-kuota-deepnavy bg-white p-5 shadow-navy-hard-lg placeholder-stripes transition-all duration-300"
              >
                <div className="flex items-center justify-between gap-2 mb-4">
                  <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-kuota-deepnavy text-white text-xs font-bold uppercase">
                    <Smartphone className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                    Placeholder · {activeChip.placeholderId}
                  </span>
                  <span className="px-2.5 py-1 rounded-md bg-kuota-palesky border border-kuota-deepnavy text-kuota-deepnavy text-xs font-extrabold">
                    Proporción {activeChip.placeholderAspectRatio}
                  </span>
                </div>

                {/* Pantalla dinámica con animación visual explicativa del beneficio (FASE 3.D) */}
                <div
                  key={activeChip.id}
                  className="rounded-2xl border-2 border-kuota-deepnavy bg-kuota-deepnavy text-white p-4 sm:p-5 mb-4 animate-fadeIn"
                >
                  <div className="flex items-center justify-between mb-3 pb-2.5 border-b border-white/15">
                    <span className="text-xs font-extrabold uppercase tracking-wider text-kuota-palesky">
                      Beneficio {activeChip.index} de 3
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
                    {activeChip.screenMockMetrics.map((metric, mIdx) => (
                      <div
                        key={metric.label}
                        style={{ animationDelay: `${mIdx * 75}ms` }}
                        className="rounded-xl bg-white/10 border border-white/15 px-3.5 py-2.5 flex items-center justify-between gap-2 animate-fadeIn"
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

                  {/* Indicador gráfico específico por beneficio (FASE 3.D) */}
                  <div className="mt-4 pt-3 border-t border-white/15">
                    {activeChip.id === 'sin-papeleo' && (
                      <div className="flex items-center justify-between text-[11px] font-bold text-kuota-palesky">
                        <span className="inline-flex items-center gap-1.5">
                          <ShieldCheck className="w-4 h-4 text-kuota-dodger" />
                          Protección de identidad activa
                        </span>
                        <span className="text-white font-extrabold">100% digital</span>
                      </div>
                    )}

                    {activeChip.id === 'invierte-mas' && (
                      <div className="space-y-1.5">
                        <div className="flex items-center justify-between text-[11px] font-bold text-kuota-palesky">
                          <span className="inline-flex items-center gap-1">
                            <TrendingUp className="w-3.5 h-3.5 text-kuota-dodger" />
                            Progresión de línea (N1 → N6)
                          </span>
                          <span className="text-white font-extrabold">Hasta S/ 30,000</span>
                        </div>
                        <div className="grid grid-cols-6 gap-1 h-2">
                          {[30, 45, 60, 75, 90, 100].map((val, i) => (
                            <div
                              key={val}
                              className={`rounded-full ${
                                i < 4 ? 'bg-kuota-dodger' : 'bg-white/25'
                              }`}
                            />
                          ))}
                        </div>
                      </div>
                    )}

                    {activeChip.id === 'mas-compras' && (
                      <div className="space-y-1.5">
                        <div className="flex items-center justify-between text-[11px] font-bold text-kuota-palesky">
                          <span className="inline-flex items-center gap-1">
                            <BarChart3 className="w-3.5 h-3.5 text-kuota-dodger" />
                            Meta para próxima compra
                          </span>
                          <span className="text-white font-extrabold">78% lista</span>
                        </div>
                        <div className="w-full h-2 rounded-full bg-white/15 overflow-hidden">
                          <div className="h-full w-[78%] rounded-full bg-kuota-dodger transition-all duration-500" />
                        </div>
                      </div>
                    )}
                  </div>
                </div>

                <div className="rounded-xl bg-kuota-palesky border border-kuota-deepnavy px-3.5 py-2.5">
                  <p className="text-xs sm:text-sm font-extrabold text-kuota-deepnavy">
                    {activeChip.placeholderLabel}
                  </p>
                </div>
              </div>
            </TiltCard>
          </ScrollReveal>

          {/* 3 Chips interactivos */}
          <div
            role="tablist"
            aria-label="Características del producto Kuota"
            className="lg:col-span-7 flex flex-col gap-4"
          >
            {PRODUCT_CHIPS.map((chip, idx) => {
              const isSelected = chip.id === activeChip.id;
              return (
                <ScrollReveal key={chip.id} delayMs={idx * 90} direction="right">
                  <div
                    className={`rounded-2xl border-2 border-kuota-deepnavy transition-all duration-200 ${
                      isSelected
                        ? 'bg-white shadow-navy-hard-lg -translate-y-0.5'
                        : 'bg-white/65 hover:bg-white/95 shadow-sm'
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
                          className={`w-9 h-9 rounded-xl border-2 border-kuota-deepnavy flex items-center justify-center text-sm font-extrabold shrink-0 transition-colors duration-200 ${
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
                        className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-extrabold border border-kuota-deepnavy transition-colors duration-200 ${
                          isSelected
                            ? 'bg-kuota-dodger text-white'
                            : 'bg-kuota-palesky text-kuota-deepnavy'
                        }`}
                      >
                        {isSelected ? 'Activo' : 'Ver'}
                        <ChevronRight
                          className={`w-4 h-4 transition-transform duration-200 ${
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
                        className="px-5 sm:px-6 pb-6 pt-1 border-t border-kuota-deepnavy/15 space-y-3 animate-fadeIn"
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
                </ScrollReveal>
              );
            })}
          </div>
        </div>

        {/* Bloque 2: Experiencia Interactiva de Compra en Kuotas y Proceso en 5 etapas (FASE 3.B y 3.E) */}
        <ScrollReveal delayMs={100} direction="up">
          <div className="rounded-3xl bg-white border-2 border-kuota-deepnavy p-6 sm:p-8 lg:p-10 shadow-navy-hard-lg">
            {/* Cabecera de la demostración */}
            <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 pb-6 border-b-2 border-kuota-palesky">
              <div>
                <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-kuota-palesky border border-kuota-deepnavy text-[11px] font-extrabold uppercase tracking-wider text-kuota-deepnavy mb-2">
                  <span>Demostración interactiva · Datos demostrativos</span>
                </div>
                <h3 className="text-2xl sm:text-3xl font-extrabold text-kuota-deepnavy tracking-tight">
                  Así se transforma una compra de mercadería en kuotas
                </h3>
                <p className="text-sm sm:text-base font-semibold text-kuota-crayola mt-1">
                  Recorre las 5 etapas o reproduce la secuencia para ver cómo se distribuyen tus pagos.
                </p>
              </div>

              <div className="flex items-center gap-2.5 shrink-0">
                <button
                  type="button"
                  onClick={handleStartAutoDemo}
                  className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-kuota-dodger hover:bg-kuota-crayola text-white font-extrabold text-xs sm:text-sm border-2 border-kuota-deepnavy shadow-navy-hard transition-all duration-150 hover:-translate-y-0.5 active:translate-y-0"
                >
                  <Play className="w-4 h-4" aria-hidden="true" />
                  <span>{isAutoPlaying ? 'Simulando...' : 'Simular flujo completo'}</span>
                </button>
                <button
                  type="button"
                  onClick={() => handleSelectStage(1)}
                  aria-label="Reiniciar demostración a la etapa 1"
                  className="p-2.5 rounded-xl bg-kuota-palesky/70 hover:bg-kuota-palesky text-kuota-deepnavy border-2 border-kuota-deepnavy transition-colors"
                >
                  <RotateCcw className="w-4 h-4" aria-hidden="true" />
                </button>
              </div>
            </div>

            {/* Navegador de las 5 etapas del proceso (FASE 3.E) */}
            <div className="mt-6">
              <div
                role="tablist"
                aria-label="Etapas de una compra con Kuota"
                className="grid grid-cols-2 sm:grid-cols-5 gap-2"
              >
                {DEMO_STAGES.map((item) => {
                  const isCurrent = item.stage === demoStage;
                  const isCompleted = item.stage < demoStage;
                  return (
                    <button
                      key={item.key}
                      type="button"
                      role="tab"
                      aria-selected={isCurrent}
                      onClick={() => handleSelectStage(item.stage)}
                      className={`text-left px-3.5 py-2.5 rounded-xl border-2 font-extrabold text-xs transition-all duration-200 flex items-center justify-between gap-2 ${
                        isCurrent
                          ? 'bg-kuota-deepnavy text-white border-kuota-deepnavy shadow-sm -translate-y-0.5'
                          : isCompleted
                            ? 'bg-kuota-palesky text-kuota-deepnavy border-kuota-deepnavy'
                            : 'bg-white hover:bg-kuota-palesky/40 text-kuota-deepnavy/75 border-kuota-deepnavy/35'
                      }`}
                    >
                      <span className="truncate">{item.label}</span>
                      {isCompleted && (
                        <CheckCircle2 className="w-3.5 h-3.5 text-kuota-dodger shrink-0" />
                      )}
                    </button>
                  );
                })}
              </div>

              {/* Barra de progreso conectada entre etapas */}
              <div className="mt-3 w-full h-2 rounded-full bg-kuota-palesky border border-kuota-deepnavy overflow-hidden">
                <div
                  className="h-full bg-kuota-dodger transition-all duration-500"
                  style={{ width: `${(demoStage / DEMO_STAGES.length) * 100}%` }}
                />
              </div>
            </div>

            {/* Panel visual de la compra y línea de tiempo de kuotas (FASE 3.B) */}
            <div className="mt-6 grid grid-cols-1 lg:grid-cols-12 gap-6 items-stretch">
              {/* Resumen de la operación demostrativa */}
              <div className="lg:col-span-5 rounded-2xl bg-kuota-deepnavy text-white p-5 sm:p-6 flex flex-col justify-between border-2 border-kuota-deepnavy">
                <div>
                  <div className="flex items-center justify-between gap-2 text-xs font-bold text-kuota-palesky mb-3">
                    <span className="inline-flex items-center gap-1.5">
                      <PackageCheck className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                      Compra demostrativa (Nivel 3)
                    </span>
                    <span className="px-2 py-0.5 rounded bg-white/10 text-white text-[11px] font-extrabold">
                      Etapa {demoStage} de 5
                    </span>
                  </div>

                  <div key={demoStage} className="animate-fadeIn">
                    <h4 className="text-lg sm:text-xl font-extrabold text-white">
                      {currentStageMeta.title}
                    </h4>
                    <p className="text-xs sm:text-sm text-kuota-palesky/90 mt-1 mb-5">
                      {currentStageMeta.subtitle}
                    </p>
                  </div>

                  {/* Tarjeta de desglose numérico */}
                  <div className="rounded-xl bg-white/[0.06] border border-white/15 p-4 space-y-2.5 text-xs sm:text-sm">
                    <div className="flex items-center justify-between">
                      <span className="text-kuota-palesky">Lote de mercadería:</span>
                      <span className="font-extrabold text-white tabular-nums">S/ 1,200.00</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-kuota-palesky">Interés Kuota (5% flat):</span>
                      <span className="font-extrabold text-kuota-dodger tabular-nums">+ S/ 60.00</span>
                    </div>
                    <div className="pt-2 border-t border-white/15 flex items-center justify-between">
                      <span className="font-bold text-white">Total dividido en kuotas:</span>
                      <span className="text-base font-extrabold text-white tabular-nums">
                        S/ 1,260.00
                      </span>
                    </div>
                  </div>
                </div>

                {/* Estado de confirmación */}
                <div className="mt-5 pt-4 border-t border-white/15 flex items-center justify-between text-xs">
                  <span className="text-kuota-palesky font-semibold">
                    Estado de la operación:
                  </span>
                  <span
                    className={`px-2.5 py-1 rounded-md font-extrabold uppercase tracking-wide ${
                      demoStage >= 4
                        ? 'bg-kuota-dodger text-white'
                        : 'bg-white/10 text-kuota-palesky'
                    }`}
                  >
                    {demoStage === 1 && '1. Lote seleccionado'}
                    {demoStage === 2 && '2. Kuotas calculadas'}
                    {demoStage === 3 && '3. Fechas asignadas'}
                    {demoStage === 4 && '4. Compra confirmada ✓'}
                    {demoStage === 5 && '5. Pagos en seguimiento ✓'}
                  </span>
                </div>
              </div>

              {/* Línea de tiempo / Calendario de kuotas (FASE 3.B) */}
              <div className="lg:col-span-7 rounded-2xl bg-kuota-palesky/45 border-2 border-kuota-deepnavy p-5 sm:p-6 flex flex-col justify-between">
                <div>
                  <div className="flex items-center justify-between gap-2 mb-4">
                    <span className="inline-flex items-center gap-1.5 text-xs sm:text-sm font-extrabold text-kuota-deepnavy uppercase tracking-wider">
                      <Calendar className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                      Calendario demostrativo de kuotas
                    </span>
                    <span className="text-xs font-bold text-kuota-crayola">
                      Plazo hasta 60 días
                    </span>
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    {DEMO_INSTALLMENTS.map((inst, idx) => {
                      const status = inst.statusByStage(demoStage);
                      const isPaid = status === 'Pagada';
                      const isHighlighted =
                        (demoStage === 2 && idx === 0) ||
                        (demoStage === 3 && idx >= 1) ||
                        demoStage >= 4;

                      return (
                        <div
                          key={inst.id}
                          className={`rounded-xl border-2 p-3.5 transition-all duration-300 ${
                            isPaid
                              ? 'bg-white border-kuota-dodger shadow-sm'
                              : isHighlighted
                                ? 'bg-white border-kuota-deepnavy shadow-sm'
                                : 'bg-white/60 border-kuota-deepnavy/35 opacity-80'
                          }`}
                        >
                          <div className="flex items-center justify-between gap-2 mb-1">
                            <span className="text-xs font-extrabold text-kuota-deepnavy">
                              {inst.label}
                            </span>
                            <span
                              className={`text-[10px] font-extrabold px-2 py-0.5 rounded-full border ${
                                isPaid
                                  ? 'bg-kuota-dodger text-white border-kuota-deepnavy'
                                  : 'bg-kuota-palesky text-kuota-deepnavy border-kuota-deepnavy/40'
                              }`}
                            >
                              {status}
                            </span>
                          </div>

                          <div className="flex items-baseline justify-between mt-2">
                            <span className="text-xs font-bold text-kuota-crayola">
                              {inst.dateLabel}
                            </span>
                            <span className="text-lg font-extrabold text-kuota-deepnavy tabular-nums">
                              {inst.amount}
                            </span>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>

                {/* Pie explicativo del simulador */}
                <div className="mt-5 pt-3.5 border-t border-kuota-deepnavy/15 flex flex-wrap items-center justify-between gap-2 text-xs font-bold text-kuota-deepnavy">
                  <span>
                    * Ejemplo demostrativo referencial para una compra de S/ 1,200 en Nivel 3.
                  </span>
                  <button
                    type="button"
                    onClick={() => handleSelectStage(demoStage < 5 ? demoStage + 1 : 1)}
                    className="inline-flex items-center gap-1 text-kuota-dodger hover:text-kuota-deepnavy font-extrabold transition-colors"
                  >
                    <span>{demoStage < 5 ? 'Siguiente etapa' : 'Volver a etapa 1'}</span>
                    <ChevronRight className="w-4 h-4" aria-hidden="true" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </ScrollReveal>
      </div>
    </section>
  );
};

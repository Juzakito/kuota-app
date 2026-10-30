import React, { useEffect, useRef, useState } from 'react';
import {
  ShieldCheck,
  Sparkles,
  Smartphone,
  CheckCircle2,
  Calendar,
  ShoppingBag,
  Layers,
  ArrowRight,
} from 'lucide-react';
import { PlayStoreButton } from './PlayStoreButton';
import {
  ScrollReveal,
  CircularImageReveal,
  FinancialCardMotion,
  AnimatedNumber,
  AnimatedCTA,
} from '../utils/motionSystem';
import { kuotaSound } from '../utils/soundSystem';

interface HeroNarrativeStep {
  step: number;
  tag: string;
  title: string;
  detail: string;
  amountSoles: number;
  installmentText: string;
  statusBadge: string;
}

const HERO_NARRATIVE_STEPS: HeroNarrativeStep[] = [
  {
    step: 1,
    tag: '1. Descubrimiento',
    title: 'Lote de abarrotes mayorista',
    detail: 'Comercio distribuidor afiliado · QR Kuota',
    amountSoles: 1200,
    installmentText: 'Mercadería lista para abastecer',
    statusBadge: 'Producto elegido',
  },
  {
    step: 2,
    tag: '2. Monto de compra',
    title: 'Monto total + 5% interés flat',
    detail: 'Sin comisiones ocultas ni letras chicas',
    amountSoles: 1260,
    installmentText: 'S/ 1,200 lote + S/ 60 interés (5%)',
    statusBadge: 'Cálculo transparente',
  },
  {
    step: 3,
    tag: '3. División en kuotas',
    title: 'Inicial 30% + 3 kuotas fijas',
    detail: 'Inicial hoy: S/ 360 · Saldo en 3 kuotas',
    amountSoles: 300,
    installmentText: '3 kuotas de S/ 300 cada 20 días',
    statusBadge: 'Dividido en kuotas',
  },
  {
    step: 4,
    tag: '4. Calendario de pagos',
    title: 'Fechas alineadas a tus ventas',
    detail: '30 Nov · 20 Dic · 09 Ene (Plazo 60 días)',
    amountSoles: 300,
    installmentText: 'Pagas conforme rota tu mercadería',
    statusBadge: 'Cronograma activo',
  },
  {
    step: 5,
    tag: '5. Confirmación visual',
    title: '¡Compra financiada en 3 clics!',
    detail: 'Proveedor notificado · Despacho inmediato',
    amountSoles: 1260,
    installmentText: 'Suma puntos para subir al Nivel 4',
    statusBadge: 'Confirmado ✓',
  },
];

/**
 * PRD §6.1 + SISTEMA DE ANIMACIÓN FINTECH (§2 y §3 — HERO EXPERIENCIA DE ENTRADA):
 * - Entrada animada del titular y aparición progresiva del subtítulo.
 * - CTA con microinteracciones táctiles y sonoras.
 * - Composición visual circular del emprendedor (`CircularImageReveal`) + tarjetas
 *   financieras flotantes en capas (`FinancialCardMotion`).
 * - Narrativa sincronizada de compra financiada en 5 pasos (con soporte táctil de toque y swipe).
 */
export const HeroSection: React.FC = () => {
  const [activeStepIdx, setActiveStepIdx] = useState<number>(2);
  const [isPaused, setIsPaused] = useState<boolean>(false);
  const touchStartXRef = useRef<number | null>(null);

  useEffect(() => {
    if (isPaused) return;
    const timer = window.setInterval(() => {
      setActiveStepIdx((prev) => (prev + 1) % HERO_NARRATIVE_STEPS.length);
    }, 3200);
    return () => window.clearInterval(timer);
  }, [isPaused]);

  const currentNarrative = HERO_NARRATIVE_STEPS[activeStepIdx];

  const handleSelectNarrativeStep = (idx: number) => {
    setIsPaused(true);
    setActiveStepIdx(idx);
    if (idx === 4) {
      kuotaSound.playConfirmOperation();
    } else {
      kuotaSound.playSelectOption();
    }
  };

  const handleTouchStart = (e: React.TouchEvent<HTMLDivElement>) => {
    if (e.touches.length === 1) {
      touchStartXRef.current = e.touches[0].clientX;
    }
  };

  const handleTouchEnd = (e: React.TouchEvent<HTMLDivElement>) => {
    if (touchStartXRef.current === null || e.changedTouches.length === 0) return;
    const deltaX = e.changedTouches[0].clientX - touchStartXRef.current;
    touchStartXRef.current = null;
    if (Math.abs(deltaX) > 36) {
      const nextIdx =
        deltaX < 0
          ? (activeStepIdx + 1) % HERO_NARRATIVE_STEPS.length
          : (activeStepIdx - 1 + HERO_NARRATIVE_STEPS.length) %
            HERO_NARRATIVE_STEPS.length;
      handleSelectNarrativeStep(nextIdx);
    }
  };

  return (
    <section
      id="hero-compradores"
      aria-labelledby="hero-heading"
      className="relative overflow-hidden pt-8 pb-16 sm:py-16 lg:py-20 border-b border-slate-200/80 bg-grid-pattern"
    >
      {/* Iluminación ambiental discreta */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -top-24 right-1/4 w-96 h-96 rounded-full bg-kuota-dodger/15 blur-3xl"
      />

      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-8 items-center">
          {/* Columna Izquierda: Copy de alto impacto y CTAs con aparición progresiva */}
          <div className="lg:col-span-6 flex flex-col items-start text-left">
            <ScrollReveal delayMs={0} direction="up">
              <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white/95 border border-slate-200/80 shadow-sm mb-5 backdrop-blur-sm">
                <span
                  className="w-2.5 h-2.5 rounded-full bg-[#0391FD] animate-pulse"
                  aria-hidden="true"
                />
                <span className="text-xs font-bold text-[#0A192F] uppercase tracking-wider">
                  Kuota para Emprendedores y Negocios
                </span>
              </div>
            </ScrollReveal>

            <ScrollReveal delayMs={80} direction="up">
              <h1
                id="hero-heading"
                className="text-4xl sm:text-5xl lg:text-[3.4rem] font-black text-[#0A192F] tracking-tight leading-[1.06] mb-5"
              >
                Financia tu crecimiento{' '}
                <span className="text-[#0391FD] underline decoration-[#0A192F]/20 decoration-4 underline-offset-4">
                  sin apuros
                </span>
              </h1>
            </ScrollReveal>

            <ScrollReveal delayMs={160} direction="up">
              <p className="text-base sm:text-lg font-medium text-slate-600 leading-relaxed mb-6 max-w-xl">
                Confiamos en ti, emprendedor, que cada día con esfuerzo saca adelante a su familia.{' '}
                <strong className="text-[#0A192F] font-bold">
                  Justos somos más.
                </strong>{' '}
                Crece tu negocio con Kuota.
              </p>
            </ScrollReveal>

            {/* Píldoras de propuesta de valor */}
            <ScrollReveal delayMs={230} direction="up">
              <div className="flex flex-wrap gap-2 mb-7">
                <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white border border-slate-200/80 text-xs sm:text-sm font-bold text-slate-800 shadow-sm">
                  <Sparkles className="w-4 h-4 text-[#0391FD]" aria-hidden="true" />
                  En 3 clics
                </span>
                <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white border border-slate-200/80 text-xs sm:text-sm font-bold text-slate-800 shadow-sm">
                  <ShieldCheck className="w-4 h-4 text-[#10B981]" aria-hidden="true" />
                  ~5% de interés
                </span>
                <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white border border-slate-200/80 text-xs sm:text-sm font-bold text-slate-800 shadow-sm">
                  <CheckCircle2 className="w-4 h-4 text-[#0391FD]" aria-hidden="true" />
                  Sin papeleo ni Infocorp
                </span>
              </div>
            </ScrollReveal>

            {/* CTAs Primario y Secundario */}
            <ScrollReveal delayMs={300} direction="up" className="w-full sm:w-auto">
              <div className="w-full sm:w-auto flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
                <PlayStoreButton
                  section="hero"
                  label="Descargar en Play Store"
                  size="lg"
                  className="w-full sm:w-auto"
                />
                <AnimatedCTA
                  href="#compra-en-kuotas"
                  variant="secondary"
                  className="py-4 px-5"
                >
                  <span>Ver compra en kuotas</span>
                  <ArrowRight className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                </AnimatedCTA>
              </div>
            </ScrollReveal>
          </div>

          {/* Columna Derecha: Composición Fotográfica Circular + Capas Financieras Sincronizadas */}
          <ScrollReveal
            delayMs={140}
            direction="scale"
            className="lg:col-span-6 relative flex flex-col items-center"
          >
            <div className="relative w-full flex flex-col items-center justify-center py-2">
              <CircularImageReveal
                src="/images/hero-negocio-kuota.jpg"
                alt="Emprendedor peruano en su negocio abastecido con Kuota"
                sizeClassName="w-60 h-60 sm:w-80 sm:h-80 lg:w-[21rem] lg:h-[21rem]"
                objectPosition="center center"
                topBadge={
                  <div className="rounded-2xl border border-slate-200/80 bg-white/95 px-3 py-1.5 sm:px-3.5 sm:py-2 shadow-lg backdrop-blur-md flex items-center gap-2">
                    <span className="w-6 h-6 sm:w-7 sm:h-7 rounded-xl bg-[#0391FD] text-white flex items-center justify-center shrink-0">
                      <ShoppingBag className="w-3.5 h-3.5 sm:w-4 sm:h-4" aria-hidden="true" />
                    </span>
                    <div className="text-left">
                      <span className="block text-[9px] sm:text-[10px] font-bold uppercase tracking-wider text-slate-500">
                        Compra financiada
                      </span>
                      <span className="text-[11px] sm:text-sm font-bold text-[#0A192F]">
                        3 kuotas · ~5% flat
                      </span>
                    </div>
                  </div>
                }
                floatingRightBadge={
                  <div className="rounded-2xl border border-white/10 bg-[#0A192F]/95 text-white px-3 py-1.5 sm:px-3.5 sm:py-2.5 shadow-xl backdrop-blur-md">
                    <div className="flex items-center gap-1.5 text-[9px] sm:text-[10px] font-bold uppercase tracking-wider text-slate-300">
                      <Calendar className="w-3 h-3 sm:w-3.5 sm:h-3.5 text-[#0391FD]" aria-hidden="true" />
                      <span>Calendario claro</span>
                    </div>
                    <div className="text-[11px] sm:text-sm font-bold text-white mt-0.5 tabular-nums">
                      Día 20 · Día 40 · Día 60
                    </div>
                  </div>
                }
              />

              {/* Tarjeta narrativa sincronizada de compra en kuotas + Tarjeta de Nivel (IMG-HERO-02) */}
              <div className="w-full max-w-md mt-6 grid grid-cols-1 sm:grid-cols-12 gap-3 z-20">
                {/* Narrativa interactiva en 5 pasos (Datos demostrativos — §3) */}
                <FinancialCardMotion
                  maxTiltDeg={2.2}
                  className="sm:col-span-7"
                >
                  <div
                    onTouchStart={handleTouchStart}
                    onTouchEnd={handleTouchEnd}
                    className="h-full rounded-[24px] border border-slate-200/80 bg-white p-4 sm:p-5 shadow-lg flex flex-col justify-between"
                  >
                    <div>
                      <div className="flex items-center justify-between gap-1.5 mb-2">
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-sky-50 border border-sky-200/80 text-[10px] font-bold uppercase text-[#0391FD]">
                          <Layers className="w-3 h-3 text-[#0391FD]" aria-hidden="true" />
                          Demostrativo
                        </span>
                        <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-[#0391FD] text-white">
                          {currentNarrative.statusBadge}
                        </span>
                      </div>

                      <div key={currentNarrative.step} className="animate-fadeIn">
                        <span className="block text-[11px] font-bold uppercase tracking-wider text-slate-400">
                          {currentNarrative.tag}
                        </span>
                        <h2 className="text-sm sm:text-base font-bold text-[#0A192F] leading-snug mt-0.5">
                          {currentNarrative.title}
                        </h2>
                        <p className="text-xs font-normal text-slate-500 mt-0.5">
                          {currentNarrative.detail}
                        </p>
                      </div>
                    </div>

                    <div className="mt-3 pt-2.5 border-t border-slate-100">
                      <div className="flex items-baseline justify-between gap-2">
                        <span className="text-[11px] font-medium text-slate-500 truncate">
                          {currentNarrative.installmentText}
                        </span>
                        <AnimatedNumber
                          value={currentNarrative.amountSoles}
                          prefix="S/ "
                          className="text-base sm:text-lg font-black text-[#0A192F] shrink-0"
                        />
                      </div>

                      {/* Botones táctiles de escena (1 a 5) cómodos para celular y PC */}
                      <div
                        role="tablist"
                        aria-label="Secuencia demostrativa de compra en kuotas"
                        className="grid grid-cols-5 gap-1.5 mt-3"
                      >
                        {HERO_NARRATIVE_STEPS.map((item, idx) => {
                          const isActive = idx === activeStepIdx;
                          return (
                            <button
                              key={item.step}
                              type="button"
                              role="tab"
                              aria-selected={isActive}
                              aria-label={item.tag}
                              onClick={() => handleSelectNarrativeStep(idx)}
                              className={`min-h-[32px] rounded-xl border text-xs font-bold touch-manipulation transition-all duration-200 active:scale-95 cursor-pointer ${
                                isActive
                                  ? 'bg-[#0391FD] text-white border-[#0391FD] shadow-sm'
                                  : 'bg-slate-50 text-slate-600 border-slate-200/80 hover:bg-slate-100'
                              }`}
                            >
                              {item.step}
                            </button>
                          );
                        })}
                      </div>
                    </div>
                  </div>
                </FinancialCardMotion>

                {/* Tarjeta de Nivel Kuota (IMG-HERO-02 preservada) */}
                <FinancialCardMotion
                  maxTiltDeg={2.2}
                  className="sm:col-span-5 rounded-[24px] border border-white/10 bg-[#0A192F] text-white p-4 shadow-xl flex flex-col justify-between"
                >
                  <div>
                    <div className="flex items-center justify-between gap-1 mb-2">
                      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-white/10 text-white text-[10px] font-bold uppercase">
                        <Smartphone className="w-3 h-3 text-[#0391FD]" aria-hidden="true" />
                        Niveles
                      </span>
                      <span className="text-[10px] font-bold text-[#0A192F] bg-white px-2 py-0.5 rounded-full">
                        Activo
                      </span>
                    </div>

                    <div className="text-[11px] text-slate-300 font-medium">
                      Línea Comercial
                    </div>
                    <div className="text-sm sm:text-base font-black tracking-tight tabular-nums text-white mt-0.5">
                      S/300 – S/1,000
                    </div>

                    <div className="w-full h-1.5 rounded-full bg-white/15 overflow-hidden my-2.5">
                      <div className="h-full w-3/4 rounded-full bg-[#0391FD] transition-all duration-500" />
                    </div>
                  </div>

                  <p className="text-[10px] font-medium text-slate-300 leading-tight">
                    Hasta S/ 30,000 en Nivel 6 con cumplimiento récord.
                  </p>
                </FinancialCardMotion>
              </div>
            </div>
          </ScrollReveal>
        </div>
      </div>
    </section>
  );
};

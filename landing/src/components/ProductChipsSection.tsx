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
  Store,
  CreditCard,
  Layers,
  Sparkles,
  QrCode,
  ArrowRight,
} from 'lucide-react';
import { PRODUCT_CHIPS, ProductChipItem } from '../data/landingData';
import { trackEvent } from '../utils/analytics';
import {
  ScrollReveal,
  TiltCard,
  CircularImageReveal,
  FinancialCardMotion,
  AnimatedNumber,
  InstallmentTimeline,
  TimelineInstallmentItem,
  PaymentProgress,
  AnimatedCTA,
} from '../utils/motionSystem';
import { PlayStoreButton } from './PlayStoreButton';
import { kuotaSound } from '../utils/soundSystem';

interface DemoSceneItem {
  scene: number;
  key: string;
  label: string;
  title: string;
  subtitle: string;
  actionTag: string;
}

/**
 * §4 — SECCIÓN: COMPRA EN CUOTAS (6 Escenas Narrativas):
 * Escena 1: Un comprador encuentra un producto en un comercio afiliado.
 * Escena 2: Selecciona la alternativa de pago en cuotas.
 * Escena 3: El monto de compra se divide visualmente en cuotas.
 * Escena 4: Aparece un calendario con las fechas correspondientes.
 * Escena 5: Se muestra una confirmación visual de la compra.
 * Escena 6: El comprador puede visualizar el seguimiento de sus pagos.
 */
const INSTALLMENT_SCENES: DemoSceneItem[] = [
  {
    scene: 1,
    key: 'escena-1-comercio',
    label: 'Escena 1 · Comercio afiliado',
    title: 'Encuentras el lote que necesita tu negocio en un comercio afiliado',
    subtitle:
      'Eliges tu mercadería con el distribuidor o mayorista participante y escaneas su código QR Kuota desde tu celular.',
    actionTag: '1. Producto en comercio afiliado',
  },
  {
    scene: 2,
    key: 'escena-2-alternativa',
    label: 'Escena 2 · Pago en kuotas',
    title: 'Seleccionas la alternativa de pago en kuotas sin papeleo',
    subtitle:
      'Eliges financiar con aKuotas! aplicando tu línea activa con 5% de interés flat bimestral, sin trámite bancario ni Infocorp.',
    actionTag: '2. Alternativa aKuotas! elegida',
  },
  {
    scene: 3,
    key: 'escena-3-division',
    label: 'Escena 3 · División en kuotas',
    title: 'El monto de compra se divide visualmente en kuotas claras',
    subtitle:
      'Observas al instante tu cuota inicial (30% en Nivel 3) y cómo el saldo restante se fracciona en 3 kuotas fijas.',
    actionTag: '3. Monto dividido en kuotas',
  },
  {
    scene: 4,
    key: 'escena-4-calendario',
    label: 'Escena 4 · Calendario de pagos',
    title: 'Aparece el calendario con las fechas exactas de tus kuotas',
    subtitle:
      'Tus fechas (Día 20, Día 40 y Día 60) quedan organizadas para que pagues con la rotación real de tu mercadería.',
    actionTag: '4. Calendario asignado',
  },
  {
    scene: 5,
    key: 'escena-5-confirmacion',
    label: 'Escena 5 · Confirmación visual',
    title: 'Confirmación visual en 3 clics y despacho inmediato',
    subtitle:
      'Confirmas la operación desde tu celular. El comercio recibe la conformidad al instante y te entrega el lote.',
    actionTag: '5. Compra confirmada ✓',
  },
  {
    scene: 6,
    key: 'escena-6-seguimiento',
    label: 'Escena 6 · Seguimiento de pagos',
    title: 'Visualizas el seguimiento de tus pagos y subes de nivel',
    subtitle:
      'Controlas tus kuotas pagadas desde la plataforma y desbloqueas hasta S/ 30,000 conforme avanzas hacia el Nivel 6.',
    actionTag: '6. Seguimiento activo ✓',
  },
];

interface EditorialBenefitScene {
  id: 'A' | 'B' | 'C' | 'D' | 'E';
  code: string;
  headline: string;
  highlight: string;
  description: string;
  ctaLabel: string;
  ctaTarget: string;
  imageSrc: string;
  imageAlt: string;
  imagePosition: string;
  badgeTopTitle: string;
  badgeTopValue: string;
  badgeRightTitle: string;
  badgeRightValue: string;
}

/**
 * §5 — SECCIÓN: BENEFICIOS PARA COMPRADORES (Composición Editorial de 2 Columnas A–E):
 */
const EDITORIAL_BENEFIT_SCENES: EditorialBenefitScene[] = [
  {
    id: 'A',
    code: 'Escena A · Organización',
    headline: 'Compra lo que necesitas y',
    highlight: 'organiza tus pagos',
    description:
      'Abastece tu bodega, minimarket o negocio cuando surge la oportunidad y estructura tu compra en kuotas fijas con solo 5% de interés flat.',
    ctaLabel: 'Simular compra en kuotas',
    ctaTarget: '#compra-en-kuotas',
    imageSrc: '/images/hero-negocio-kuota.jpg',
    imageAlt: 'Emprendedor abasteciendo su negocio y organizando pagos con Kuota',
    imagePosition: 'center center',
    badgeTopTitle: 'Lote de mercadería',
    badgeTopValue: 'Dividido en 3 kuotas',
    badgeRightTitle: 'Tasa transparente',
    badgeRightValue: '5% interés flat',
  },
  {
    id: 'B',
    code: 'Escena B · Visibilidad',
    headline: 'Visualiza tus kuotas y',
    highlight: 'próximas fechas',
    description:
      'Consulta en todo momento tu cronograma de vencimientos, recordatorios preventivos y el importe exacto de cada kuota antes de pagar.',
    ctaLabel: 'Ver calendario demostrativo',
    ctaTarget: '#compra-en-kuotas',
    imageSrc: '/images/formalizacion-kuota.jpg',
    imageAlt: 'Emprendedores revisando su calendario de pagos y fechas en Kuota',
    imagePosition: 'left center',
    badgeTopTitle: 'Próximo vencimiento',
    badgeTopValue: 'Día 20 · S/ 300',
    badgeRightTitle: 'Alertas preventivas',
    badgeRightValue: '0 cargos sorpresa',
  },
  {
    id: 'C',
    code: 'Escena C · Red de comercios',
    headline: 'Accede a comercios y',
    highlight: 'distribuidores participantes',
    description:
      'Compra directamente en mayoristas, distribuidoras de abarrotes, tecnología, textil y ferreterías afiliadas escaneando el QR de Kuota.',
    ctaLabel: 'Explorar niveles de compra',
    ctaTarget: '#niveles',
    imageSrc: '/images/hero-negocio-kuota.jpg',
    imageAlt: 'Comercio mayorista afiliado a la red de pagos Kuota',
    imagePosition: 'right center',
    badgeTopTitle: 'Pago en mostrador',
    badgeTopValue: 'QR y Link Kuota',
    badgeRightTitle: 'Despacho de lote',
    badgeRightValue: 'Inmediato en tienda',
  },
  {
    id: 'D',
    code: 'Escena D · Compromisos',
    headline: 'Mantén el seguimiento de',
    highlight: 'tus compromisos',
    description:
      'Cada kuota cumplida a tiempo fortalece tu historial dentro de Kuota, reduce tu cuota inicial hasta 0% y amplía tu plazo hasta 360 días.',
    ctaLabel: 'Ver escalera de niveles',
    ctaTarget: '#niveles',
    imageSrc: '/images/formalizacion-kuota.jpg',
    imageAlt: 'Seguimiento de compromisos de pago y crecimiento de nivel en Kuota',
    imagePosition: 'center center',
    badgeTopTitle: 'Progreso de nivel',
    badgeTopValue: 'Nivel 3 → Nivel 4',
    badgeRightTitle: 'Línea máxima N6',
    badgeRightValue: 'Hasta S/ 30,000',
  },
  {
    id: 'E',
    code: 'Escena E · Plataforma única',
    headline: 'Gestiona tu experiencia de compra',
    highlight: 'desde una plataforma',
    description:
      'Centraliza tu línea disponible, tus lotes financiados, tus comprobantes de pago y tu acompañamiento de formalización RUC 10 / RUC 20.',
    ctaLabel: 'Conocer respaldo Kuota',
    ctaTarget: '#nosotros',
    imageSrc: '/images/hero-negocio-kuota.jpg',
    imageAlt: 'Gestión integral de compras financiadas desde la plataforma Kuota',
    imagePosition: 'center top',
    badgeTopTitle: 'Control todo en uno',
    badgeTopValue: 'App 100% digital',
    badgeRightTitle: 'Formalización',
    badgeRightValue: 'Ruta RUC 10 / 20',
  },
];

const DEMO_LOT_AMOUNTS = [600, 1200, 2400] as const;

export const ProductChipsSection: React.FC = () => {
  const [activeChipId, setActiveChipId] = useState<string>(PRODUCT_CHIPS[0].id);
  const [demoScene, setDemoScene] = useState<number>(3);
  const [demoAmountSoles, setDemoAmountSoles] = useState<number>(1200);
  const [isAutoPlaying, setIsAutoPlaying] = useState<boolean>(false);
  const [activeEditorialId, setActiveEditorialId] = useState<'A' | 'B' | 'C' | 'D' | 'E'>('A');

  const activeChip: ProductChipItem =
    PRODUCT_CHIPS.find((c) => c.id === activeChipId) || PRODUCT_CHIPS[0];

  const currentSceneMeta =
    INSTALLMENT_SCENES.find((s) => s.scene === demoScene) || INSTALLMENT_SCENES[2];

  const activeEditorial =
    EDITORIAL_BENEFIT_SCENES.find((s) => s.id === activeEditorialId) ||
    EDITORIAL_BENEFIT_SCENES[0];

  // Cálculos demostrativos transparentes (Nivel 3: 30% inicial, 5% interés flat bimestral)
  const flatInterestSoles = Math.round(demoAmountSoles * 0.05);
  const totalWithInterestSoles = demoAmountSoles + flatInterestSoles;
  const initialPaymentSoles = Math.round(demoAmountSoles * 0.3);
  const remainingBalanceSoles = totalWithInterestSoles - initialPaymentSoles;
  const eachKuotaSoles = Math.round(remainingBalanceSoles / 3);

  const timelineItems: TimelineInstallmentItem[] = [
    {
      id: 'inicial',
      label: 'Cuota Inicial (30%)',
      dateLabel: 'Hoy · Al confirmar',
      amountSoles: initialPaymentSoles,
      status:
        demoScene >= 5
          ? 'Pagada'
          : demoScene >= 3
            ? 'Confirmada'
            : 'En cálculo',
      highlight: demoScene === 3 || demoScene === 5,
    },
    {
      id: 'kuota-1',
      label: 'Kuota 1 de 3',
      dateLabel: 'Día 20 · 30 Nov',
      amountSoles: eachKuotaSoles,
      status:
        demoScene === 6
          ? 'Pagada'
          : demoScene >= 4
            ? 'Programada'
            : 'En cálculo',
      highlight: demoScene === 4 || demoScene === 6,
    },
    {
      id: 'kuota-2',
      label: 'Kuota 2 de 3',
      dateLabel: 'Día 40 · 20 Dic',
      amountSoles: eachKuotaSoles,
      status:
        demoScene === 6
          ? 'Pagada'
          : demoScene >= 4
            ? 'Programada'
            : 'En cálculo',
      highlight: demoScene === 4 || demoScene === 6,
    },
    {
      id: 'kuota-3',
      label: 'Kuota 3 de 3',
      dateLabel: 'Día 60 · 09 Ene',
      amountSoles: eachKuotaSoles,
      status:
        demoScene === 6
          ? 'Próxima'
          : demoScene >= 4
            ? 'Programada'
            : 'En cálculo',
      highlight: demoScene === 4,
    },
  ];

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

  const handleSelectScene = (sceneNum: number) => {
    setIsAutoPlaying(false);
    setDemoScene(sceneNum);
    if (sceneNum === 5) {
      kuotaSound.playConfirmOperation();
    } else if (sceneNum === 6) {
      kuotaSound.playDemoComplete();
    } else {
      kuotaSound.playSelectOption();
    }
    trackEvent('installment_demo_stage_select', { stage: sceneNum });
  };

  const handleStartAutoDemo = () => {
    kuotaSound.playCtaClick();
    setDemoScene(1);
    setIsAutoPlaying(true);
    trackEvent('installment_demo_autoplay_start', { total_scenes: 6 });
  };

  useEffect(() => {
    if (!isAutoPlaying) return;
    const timer = window.setTimeout(() => {
      if (demoScene < INSTALLMENT_SCENES.length) {
        const next = demoScene + 1;
        setDemoScene(next);
        if (next === 5) {
          kuotaSound.playConfirmOperation();
        } else if (next === 6) {
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
  }, [isAutoPlaying, demoScene]);

  const handleSelectEditorial = (id: 'A' | 'B' | 'C' | 'D' | 'E') => {
    kuotaSound.playSelectOption();
    setActiveEditorialId(id);
    trackEvent('editorial_benefit_scene_select', { scene_id: id });
  };

  return (
    <section
      id="producto"
      aria-labelledby="product-chips-heading"
      className="py-14 sm:py-20 border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-16">
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

        {/* BLOQUE 1: Captura interactiva + 3 Chips de beneficios (PRD §6.4 preservado) */}
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

                {/* Pantalla dinámica con animación visual explicativa del beneficio */}
                <div
                  key={activeChip.id}
                  className="rounded-2xl border-2 border-kuota-deepnavy bg-kuota-deepnavy text-white p-4 sm:p-5 mb-4 kuota-section-arrive"
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

        {/* BLOQUE 2 (§4): SECCIÓN PROTAGONISTA — COMPRA EN CUOTAS (Narrativa Interactiva en 6 Escenas) */}
        <ScrollReveal delayMs={100} direction="up">
          <div
            id="compra-en-kuotas"
            className="rounded-3xl bg-white border-2 border-kuota-deepnavy p-6 sm:p-8 lg:p-10 shadow-navy-hard-lg"
          >
            {/* Cabecera + Selector de monto demostrativo + Reproductor de escenas */}
            <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-5 pb-6 border-b-2 border-kuota-palesky">
              <div>
                <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-kuota-palesky border border-kuota-deepnavy text-[11px] font-extrabold uppercase tracking-wider text-kuota-deepnavy mb-2">
                  <Sparkles className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                  <span>Sección: Compra en Kuotas · 6 Escenas · Datos demostrativos</span>
                </div>
                <h3 className="text-2xl sm:text-3xl font-extrabold text-kuota-deepnavy tracking-tight">
                  Así funciona una compra financiada paso a paso
                </h3>
                <p className="text-sm sm:text-base font-semibold text-kuota-crayola mt-1">
                  Recorre las 6 escenas o cambia el monto demostrativo para ver cómo se calculan tus kuotas en tiempo real.
                </p>
              </div>

              <div className="flex flex-wrap items-center gap-2.5 shrink-0">
                {/* Selector de lote demostrativo con transición de números */}
                <div
                  role="group"
                  aria-label="Monto de lote demostrativo"
                  className="inline-flex items-center rounded-xl bg-kuota-palesky/70 border-2 border-kuota-deepnavy p-1 gap-1"
                >
                  {DEMO_LOT_AMOUNTS.map((amt) => {
                    const isAmtActive = demoAmountSoles === amt;
                    return (
                      <button
                        key={amt}
                        type="button"
                        onClick={() => {
                          kuotaSound.playSelectOption();
                          setDemoAmountSoles(amt);
                        }}
                        className={`px-2.5 py-1.5 rounded-lg text-xs font-extrabold transition-all ${
                          isAmtActive
                            ? 'bg-kuota-deepnavy text-white shadow-sm'
                            : 'text-kuota-deepnavy hover:bg-white/80'
                        }`}
                      >
                        S/ {amt.toLocaleString('es-PE')}
                      </button>
                    );
                  })}
                </div>

                <button
                  type="button"
                  onClick={handleStartAutoDemo}
                  className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-kuota-dodger hover:bg-kuota-crayola text-white font-extrabold text-xs sm:text-sm border-2 border-kuota-deepnavy shadow-navy-hard transition-all duration-150 hover:-translate-y-0.5 active:translate-y-0"
                >
                  <Play className="w-4 h-4" aria-hidden="true" />
                  <span>{isAutoPlaying ? 'Reproduciendo 6 escenas...' : 'Simular las 6 escenas'}</span>
                </button>
                <button
                  type="button"
                  onClick={() => handleSelectScene(1)}
                  aria-label="Reiniciar demostración a la escena 1"
                  className="p-2.5 rounded-xl bg-kuota-palesky/70 hover:bg-kuota-palesky text-kuota-deepnavy border-2 border-kuota-deepnavy transition-colors"
                >
                  <RotateCcw className="w-4 h-4" aria-hidden="true" />
                </button>
              </div>
            </div>

            {/* Navegador de las 6 escenas (§4) */}
            <div className="mt-6">
              <div
                role="tablist"
                aria-label="Las 6 escenas de una compra en kuotas"
                className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-2"
              >
                {INSTALLMENT_SCENES.map((item) => {
                  const isCurrent = item.scene === demoScene;
                  const isCompleted = item.scene < demoScene;
                  return (
                    <button
                      key={item.key}
                      type="button"
                      role="tab"
                      aria-selected={isCurrent}
                      onClick={() => handleSelectScene(item.scene)}
                      className={`text-left px-3 py-2.5 rounded-xl border-2 font-extrabold text-xs transition-all duration-200 flex items-center justify-between gap-1.5 ${
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

              {/* Barra de progreso narrativa conectada */}
              <div className="mt-4">
                <PaymentProgress
                  label={`Recorrido narrativo · Escena ${demoScene} de ${INSTALLMENT_SCENES.length}`}
                  currentStep={demoScene}
                  totalSteps={INSTALLMENT_SCENES.length}
                  statusText={currentSceneMeta.actionTag}
                />
              </div>
            </div>

            {/* Panel visual de la compra y línea de tiempo de kuotas */}
            <div className="mt-6 grid grid-cols-1 lg:grid-cols-12 gap-6 items-stretch">
              {/* Columna Izquierda: Tarjeta de operación + desglose animado */}
              <div className="lg:col-span-5 rounded-2xl bg-kuota-deepnavy text-white p-5 sm:p-6 flex flex-col justify-between border-2 border-kuota-deepnavy">
                <div>
                  <div className="flex items-center justify-between gap-2 text-xs font-bold text-kuota-palesky mb-3">
                    <span className="inline-flex items-center gap-1.5">
                      <PackageCheck className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                      Compra demostrativa (Nivel 3)
                    </span>
                    <span className="px-2 py-0.5 rounded bg-kuota-dodger text-white text-[11px] font-extrabold">
                      Escena {demoScene} / 6
                    </span>
                  </div>

                  <div key={demoScene} className="kuota-section-arrive">
                    <h4 className="text-lg sm:text-xl font-extrabold text-white leading-snug">
                      {currentSceneMeta.title}
                    </h4>
                    <p className="text-xs sm:text-sm text-kuota-palesky/90 mt-1.5 mb-5 leading-relaxed">
                      {currentSceneMeta.subtitle}
                    </p>
                  </div>

                  {/* Tarjeta de desglose numérico con AnimatedNumber */}
                  <div className="rounded-xl bg-white/[0.07] border border-white/15 p-4 space-y-2.5 text-xs sm:text-sm">
                    <div className="flex items-center justify-between">
                      <span className="text-kuota-palesky flex items-center gap-1.5">
                        <Store className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                        Lote en comercio afiliado:
                      </span>
                      <AnimatedNumber
                        value={demoAmountSoles}
                        prefix="S/ "
                        decimals={2}
                        className="font-extrabold text-white"
                      />
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-kuota-palesky">Interés Kuota (5% flat):</span>
                      <AnimatedNumber
                        value={flatInterestSoles}
                        prefix="+ S/ "
                        decimals={2}
                        className="font-extrabold text-kuota-dodger"
                      />
                    </div>
                    <div className="pt-2 border-t border-white/15 flex items-center justify-between">
                      <span className="font-bold text-white">Total dividido en kuotas:</span>
                      <AnimatedNumber
                        value={totalWithInterestSoles}
                        prefix="S/ "
                        decimals={2}
                        className="text-base font-extrabold text-white"
                      />
                    </div>
                  </div>

                  {/* Barra de proporción visual (Inicial 30% vs 3 Kuotas) */}
                  <div className="mt-4 space-y-1.5">
                    <div className="flex items-center justify-between text-[11px] font-bold text-kuota-palesky">
                      <span>Inicial hoy (30%): S/ {initialPaymentSoles}</span>
                      <span>3 kuotas de S/ {eachKuotaSoles}</span>
                    </div>
                    <div className="w-full h-2.5 rounded-full bg-white/15 overflow-hidden flex gap-0.5 p-0.5">
                      <div className="h-full w-[30%] rounded-l-full bg-kuota-palesky transition-all duration-300" />
                      <div className="h-full w-[23.3%] bg-kuota-dodger transition-all duration-300" />
                      <div className="h-full w-[23.3%] bg-kuota-dodger/85 transition-all duration-300" />
                      <div className="h-full flex-1 rounded-r-full bg-kuota-dodger/70 transition-all duration-300" />
                    </div>
                  </div>
                </div>

                {/* Estado de la escena */}
                <div className="mt-5 pt-4 border-t border-white/15 flex items-center justify-between text-xs">
                  <span className="text-kuota-palesky font-semibold">
                    Estado en esta escena:
                  </span>
                  <span
                    className={`px-2.5 py-1 rounded-md font-extrabold uppercase tracking-wide ${
                      demoScene >= 5
                        ? 'bg-kuota-dodger text-white'
                        : 'bg-white/10 text-kuota-palesky'
                    }`}
                  >
                    {currentSceneMeta.actionTag}
                  </span>
                </div>
              </div>

              {/* Columna Derecha: Calendario de Kuotas (`InstallmentTimeline`) + Seguimiento */}
              <div className="lg:col-span-7 rounded-2xl bg-kuota-palesky/45 border-2 border-kuota-deepnavy p-5 sm:p-6 flex flex-col justify-between">
                <div>
                  <div className="flex flex-wrap items-center justify-between gap-2 mb-4">
                    <span className="inline-flex items-center gap-1.5 text-xs sm:text-sm font-extrabold text-kuota-deepnavy uppercase tracking-wider">
                      <Calendar className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                      Calendario y seguimiento de kuotas
                    </span>
                    <span className="text-xs font-extrabold px-2.5 py-1 rounded-full bg-white border border-kuota-deepnavy text-kuota-deepnavy">
                      Plazo hasta 60 días · Nivel 3
                    </span>
                  </div>

                  <InstallmentTimeline
                    items={timelineItems}
                    onSelectItem={(_, idx) => {
                      handleSelectScene(Math.min(6, idx + 3));
                    }}
                  />

                  {/* Barra de seguimiento de pagos (destacada en Escena 6) */}
                  <div className="mt-4 rounded-xl bg-white border-2 border-kuota-deepnavy p-3.5">
                    <PaymentProgress
                      label="Progreso de cumplimiento del lote"
                      currentStep={demoScene === 6 ? 3 : demoScene >= 5 ? 1 : 0}
                      totalSteps={4}
                      statusText={
                        demoScene === 6
                          ? '3 de 4 pagos cumplidos · +85 pts hacia Nivel 4'
                          : demoScene >= 5
                            ? 'Cuota inicial pagada · Mercadería entregada'
                            : 'Selecciona Escena 5 o 6 para ver confirmación y seguimiento'
                      }
                    />
                  </div>
                </div>

                {/* Pie explicativo del simulador */}
                <div className="mt-5 pt-3.5 border-t border-kuota-deepnavy/15 flex flex-wrap items-center justify-between gap-2 text-xs font-bold text-kuota-deepnavy">
                  <span>
                    * Datos demostrativos referenciales para un lote de S/ {demoAmountSoles.toLocaleString('es-PE')} en Nivel 3.
                  </span>
                  <button
                    type="button"
                    onClick={() =>
                      handleSelectScene(
                        demoScene < INSTALLMENT_SCENES.length ? demoScene + 1 : 1
                      )
                    }
                    className="inline-flex items-center gap-1 text-kuota-dodger hover:text-kuota-deepnavy font-extrabold transition-colors"
                  >
                    <span>
                      {demoScene < INSTALLMENT_SCENES.length
                        ? `Ir a Escena ${demoScene + 1}`
                        : 'Reiniciar en Escena 1'}
                    </span>
                    <ChevronRight className="w-4 h-4" aria-hidden="true" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </ScrollReveal>

        {/* BLOQUE 3 (§5): SECCIÓN BENEFICIOS PARA COMPRADORES — COMPOSICIÓN EDITORIAL DE 2 COLUMNAS (Escenas A–E) */}
        <ScrollReveal delayMs={120} direction="up">
          <div className="rounded-3xl bg-white/85 border-2 border-kuota-deepnavy p-6 sm:p-8 lg:p-10 shadow-navy-hard-lg">
            {/* Selector superior de Escenas Editoriales A–E */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-6 border-b-2 border-kuota-palesky">
              <div>
                <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-kuota-deepnavy text-white text-[11px] font-extrabold uppercase tracking-wider mb-2">
                  <Layers className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                  Experiencia para Compradores · 5 Escenas Editoriales
                </span>
                <h3 className="text-2xl sm:text-3xl font-extrabold text-kuota-deepnavy tracking-tight">
                  Beneficios pensados para el ritmo real de tu negocio
                </h3>
              </div>

              <div
                role="tablist"
                aria-label="Escenas de beneficios para compradores"
                className="flex flex-wrap items-center gap-1.5"
              >
                {EDITORIAL_BENEFIT_SCENES.map((scene) => {
                  const isSelected = scene.id === activeEditorial.id;
                  return (
                    <button
                      key={scene.id}
                      type="button"
                      role="tab"
                      aria-selected={isSelected}
                      onClick={() => handleSelectEditorial(scene.id)}
                      className={`px-3.5 py-2 rounded-xl border-2 border-kuota-deepnavy text-xs font-extrabold transition-all duration-200 ${
                        isSelected
                          ? 'bg-kuota-dodger text-white shadow-navy-hard -translate-y-0.5'
                          : 'bg-kuota-palesky/60 hover:bg-kuota-palesky text-kuota-deepnavy'
                      }`}
                    >
                      Escena {scene.id}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Composición Editorial de Dos Columnas (§5): Izquierda Titular + Descripción + CTA / Derecha Fotografía Circular + Componente Financiero Animado */}
            <div
              key={activeEditorial.id}
              className="mt-8 grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-10 items-center kuota-section-arrive"
            >
              {/* Lado Izquierdo: Titular grande, descripción breve y CTA correspondiente */}
              <div className="lg:col-span-6 flex flex-col items-start text-left">
                <span className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-kuota-palesky border border-kuota-deepnavy text-xs font-extrabold uppercase tracking-wider text-kuota-deepnavy mb-4">
                  {activeEditorial.code}
                </span>

                <h4 className="text-3xl sm:text-4xl lg:text-[2.65rem] font-extrabold text-kuota-deepnavy tracking-tight leading-[1.08] mb-4">
                  {activeEditorial.headline}{' '}
                  <span className="text-kuota-dodger underline decoration-kuota-deepnavy decoration-4 underline-offset-4">
                    {activeEditorial.highlight}
                  </span>
                </h4>

                <p className="text-base sm:text-lg font-semibold text-kuota-crayola leading-relaxed mb-7">
                  {activeEditorial.description}
                </p>

                <div className="flex flex-wrap items-center gap-3 w-full sm:w-auto">
                  <PlayStoreButton
                    section={`beneficio-editorial-${activeEditorial.id}`}
                    label="Descargar en Play Store"
                    size="md"
                  />
                  <AnimatedCTA
                    href={activeEditorial.ctaTarget}
                    variant="secondary"
                  >
                    <span>{activeEditorial.ctaLabel}</span>
                    <ArrowRight className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                  </AnimatedCTA>
                </div>
              </div>

              {/* Lado Derecho: Fotografía Circular + Ilustración de Producto + Componente Financiero Animado */}
              <div className="lg:col-span-6 flex flex-col items-center">
                <CircularImageReveal
                  src={activeEditorial.imageSrc}
                  alt={activeEditorial.imageAlt}
                  sizeClassName="w-56 h-56 sm:w-72 sm:h-72"
                  objectPosition={activeEditorial.imagePosition}
                  topBadge={
                    <div className="rounded-2xl border-2 border-kuota-deepnavy bg-white px-3.5 py-2 shadow-navy-hard">
                      <span className="block text-[10px] font-extrabold uppercase tracking-wider text-kuota-crayola">
                        {activeEditorial.badgeTopTitle}
                      </span>
                      <span className="text-xs sm:text-sm font-extrabold text-kuota-deepnavy">
                        {activeEditorial.badgeTopValue}
                      </span>
                    </div>
                  }
                  floatingRightBadge={
                    <div className="rounded-2xl border-2 border-kuota-deepnavy bg-kuota-deepnavy text-white px-3.5 py-2 shadow-navy-hard">
                      <span className="block text-[10px] font-extrabold uppercase tracking-wider text-kuota-palesky">
                        {activeEditorial.badgeRightTitle}
                      </span>
                      <span className="text-xs sm:text-sm font-extrabold text-white">
                        {activeEditorial.badgeRightValue}
                      </span>
                    </div>
                  }
                />

                {/* Componente financiero animado único por cada escena A–E (§5) */}
                <FinancialCardMotion
                  maxTiltDeg={2.4}
                  className="w-full max-w-md mt-6 rounded-2xl border-2 border-kuota-deepnavy bg-kuota-deepnavy text-white p-4 sm:p-5 shadow-navy-hard-lg"
                >
                  {activeEditorial.id === 'A' && (
                    <div className="space-y-3">
                      <div className="flex items-center justify-between text-xs font-extrabold text-kuota-palesky">
                        <span className="inline-flex items-center gap-1.5">
                          <CreditCard className="w-4 h-4 text-kuota-dodger" />
                          Organizador demostrativo de compra
                        </span>
                        <span className="px-2 py-0.5 rounded bg-kuota-dodger text-white">
                          3 kuotas
                        </span>
                      </div>
                      <div className="grid grid-cols-3 gap-2 text-center">
                        <div className="rounded-xl bg-white/10 border border-white/15 p-2.5">
                          <span className="block text-[10px] text-kuota-palesky font-bold">
                            Inicial (30%)
                          </span>
                          <span className="text-sm font-extrabold text-white">S/ 360</span>
                        </div>
                        <div className="rounded-xl bg-kuota-dodger/30 border border-kuota-dodger p-2.5">
                          <span className="block text-[10px] text-kuota-palesky font-bold">
                            Cada kuota
                          </span>
                          <span className="text-sm font-extrabold text-white">S/ 300</span>
                        </div>
                        <div className="rounded-xl bg-white/10 border border-white/15 p-2.5">
                          <span className="block text-[10px] text-kuota-palesky font-bold">
                            Interés flat
                          </span>
                          <span className="text-sm font-extrabold text-kuota-dodger">5%</span>
                        </div>
                      </div>
                    </div>
                  )}

                  {activeEditorial.id === 'B' && (
                    <div className="space-y-2.5">
                      <div className="flex items-center justify-between text-xs font-extrabold text-kuota-palesky">
                        <span className="inline-flex items-center gap-1.5">
                          <Calendar className="w-4 h-4 text-kuota-dodger" />
                          Próximas fechas programadas (Demostrativo)
                        </span>
                        <span className="text-white">60 días</span>
                      </div>
                      <div className="grid grid-cols-3 gap-2 text-xs">
                        {[
                          { d: '30 Nov', k: 'Kuota 1', m: 'S/ 300' },
                          { d: '20 Dic', k: 'Kuota 2', m: 'S/ 300' },
                          { d: '09 Ene', k: 'Kuota 3', m: 'S/ 300' },
                        ].map((f) => (
                          <div
                            key={f.k}
                            className="rounded-xl bg-white/10 border border-white/15 p-2.5 text-center"
                          >
                            <span className="block text-[10px] text-kuota-palesky font-bold">
                              {f.d}
                            </span>
                            <span className="block font-extrabold text-white mt-0.5">
                              {f.m}
                            </span>
                            <span className="block text-[10px] text-kuota-dodger font-extrabold">
                              {f.k}
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {activeEditorial.id === 'C' && (
                    <div className="space-y-2.5">
                      <div className="flex items-center justify-between text-xs font-extrabold text-kuota-palesky">
                        <span className="inline-flex items-center gap-1.5">
                          <QrCode className="w-4 h-4 text-kuota-dodger" />
                          Rubros de comercios participantes
                        </span>
                        <span className="px-2 py-0.5 rounded bg-kuota-dodger text-white">
                          Red Kuota
                        </span>
                      </div>
                      <div className="flex flex-wrap gap-1.5">
                        {[
                          'Distribuidoras de abarrotes',
                          'Mayoristas de bebidas',
                          'Ferreterías',
                          'Tiendas de tecnología',
                          'Textil y calzado',
                          'Empaques e insumos',
                        ].map((sector) => (
                          <span
                            key={sector}
                            className="px-2.5 py-1 rounded-lg bg-white/10 border border-white/15 text-[11px] font-bold text-white"
                          >
                            ✓ {sector}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}

                  {activeEditorial.id === 'D' && (
                    <div className="space-y-2.5">
                      <div className="flex items-center justify-between text-xs font-extrabold text-kuota-palesky">
                        <span className="inline-flex items-center gap-1.5">
                          <TrendingUp className="w-4 h-4 text-kuota-dodger" />
                          Seguimiento de compromisos (Demostrativo)
                        </span>
                        <span className="text-kuota-dodger font-extrabold">
                          Puntualidad 100%
                        </span>
                      </div>
                      <div className="w-full h-2.5 rounded-full bg-white/15 overflow-hidden p-0.5">
                        <div className="h-full w-4/5 rounded-full bg-kuota-dodger transition-all duration-500" />
                      </div>
                      <div className="flex items-center justify-between text-[11px] text-kuota-palesky font-semibold">
                        <span>Inicial baja de 50% (N1) → 0% (N6)</span>
                        <span className="text-white font-extrabold">Plazo hasta 360d</span>
                      </div>
                    </div>
                  )}

                  {activeEditorial.id === 'E' && (
                    <div className="space-y-2.5">
                      <div className="flex items-center justify-between text-xs font-extrabold text-kuota-palesky">
                        <span className="inline-flex items-center gap-1.5">
                          <Smartphone className="w-4 h-4 text-kuota-dodger" />
                          Plataforma integral Kuota
                        </span>
                        <span className="px-2 py-0.5 rounded bg-kuota-dodger text-white">
                          Todo en un lugar
                        </span>
                      </div>
                      <div className="grid grid-cols-2 gap-2 text-xs">
                        <div className="rounded-xl bg-white/10 border border-white/15 p-2.5">
                          <span className="block text-[10px] text-kuota-palesky font-bold">
                            Mis compras y kuotas
                          </span>
                          <span className="font-extrabold text-white">
                            Cronograma + QR
                          </span>
                        </div>
                        <div className="rounded-xl bg-white/10 border border-white/15 p-2.5">
                          <span className="block text-[10px] text-kuota-palesky font-bold">
                            Crecimiento formal
                          </span>
                          <span className="font-extrabold text-white">
                            Mentorías RUC 10/20
                          </span>
                        </div>
                      </div>
                    </div>
                  )}
                </FinancialCardMotion>
              </div>
            </div>
          </div>
        </ScrollReveal>
      </div>
    </section>
  );
};

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
  FileCheck2,
  ShoppingBag,
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

/** Iconografía por beneficio (chips de "Cómo funciona Kuota") */
const CHIP_ICONS: Record<string, React.ComponentType<{ className?: string }>> = {
  'sin-papeleo': FileCheck2,
  'invierte-mas': TrendingUp,
  'mas-compras': ShoppingBag,
};

export const ProductChipsSection: React.FC = () => {
  const [activeChipId, setActiveChipId] = useState<string>(PRODUCT_CHIPS[0].id);
  const [demoScene, setDemoScene] = useState<number>(3);
  const [demoAmountSoles, setDemoAmountSoles] = useState<number>(1200);
  const [isAutoPlaying, setIsAutoPlaying] = useState<boolean>(false);
  const [activeEditorialId, setActiveEditorialId] = useState<'A' | 'B' | 'C' | 'D' | 'E'>('A');
  const sceneTouchStartX = React.useRef<number | null>(null);
  const editorialTouchStartX = React.useRef<number | null>(null);

  const [isChipAutoRotating, setIsChipAutoRotating] = useState<boolean>(true);
  const [isChipsHovered, setIsChipsHovered] = useState<boolean>(false);
  const [isChipsInView, setIsChipsInView] = useState<boolean>(false);
  const chipsBlockRef = React.useRef<HTMLDivElement | null>(null);

  const activeChip: ProductChipItem =
    PRODUCT_CHIPS.find((c) => c.id === activeChipId) || PRODUCT_CHIPS[0];
  const ActiveChipIcon = CHIP_ICONS[activeChip.id] || Sparkles;

  // Auto-avance de chips: solo corre cuando el bloque es visible y respeta reduced-motion
  useEffect(() => {
    if (
      typeof window !== 'undefined' &&
      window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
    ) {
      setIsChipAutoRotating(false);
      return;
    }
    const el = chipsBlockRef.current;
    if (!el || typeof IntersectionObserver === 'undefined') return;
    const observer = new IntersectionObserver(
      ([entry]) => setIsChipsInView(entry.isIntersecting),
      { threshold: 0.35 }
    );
    observer.observe(el);
    return () => observer.disconnect();
  }, []);

  const handleChipProgressEnd = () => {
    const idx = PRODUCT_CHIPS.findIndex((c) => c.id === activeChipId);
    const next = PRODUCT_CHIPS[(idx + 1) % PRODUCT_CHIPS.length];
    setActiveChipId(next.id);
  };

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

  const handleSelectChip = (chip: ProductChipItem, fromUser = false) => {
    if (fromUser) setIsChipAutoRotating(false);
    if (chip.id === activeChipId) return;
    if (fromUser) kuotaSound.playSelectOption();
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
      handleSelectChip(next, true);
      document.getElementById(`tab-${next.id}`)?.focus();
    } else if (e.key === 'ArrowUp' || e.key === 'ArrowLeft') {
      e.preventDefault();
      const prev =
        PRODUCT_CHIPS[(index - 1 + PRODUCT_CHIPS.length) % PRODUCT_CHIPS.length];
      handleSelectChip(prev, true);
      document.getElementById(`tab-${prev.id}`)?.focus();
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

  const handleSceneSwipeStart = (e: React.TouchEvent<HTMLDivElement>) => {
    if (e.touches.length === 1) {
      sceneTouchStartX.current = e.touches[0].clientX;
    }
  };

  const handleSceneSwipeEnd = (e: React.TouchEvent<HTMLDivElement>) => {
    if (sceneTouchStartX.current === null || e.changedTouches.length === 0) return;
    const deltaX = e.changedTouches[0].clientX - sceneTouchStartX.current;
    sceneTouchStartX.current = null;
    if (Math.abs(deltaX) > 42) {
      const nextScene =
        deltaX < 0
          ? demoScene < INSTALLMENT_SCENES.length
            ? demoScene + 1
            : 1
          : demoScene > 1
            ? demoScene - 1
            : INSTALLMENT_SCENES.length;
      handleSelectScene(nextScene);
    }
  };

  const handleEditorialSwipeStart = (e: React.TouchEvent<HTMLDivElement>) => {
    if (e.touches.length === 1) {
      editorialTouchStartX.current = e.touches[0].clientX;
    }
  };

  const handleEditorialSwipeEnd = (e: React.TouchEvent<HTMLDivElement>) => {
    if (editorialTouchStartX.current === null || e.changedTouches.length === 0) return;
    const deltaX = e.changedTouches[0].clientX - editorialTouchStartX.current;
    editorialTouchStartX.current = null;
    if (Math.abs(deltaX) > 42) {
      const currentIdx = EDITORIAL_BENEFIT_SCENES.findIndex(
        (s) => s.id === activeEditorialId
      );
      const nextIdx =
        deltaX < 0
          ? (currentIdx + 1) % EDITORIAL_BENEFIT_SCENES.length
          : (currentIdx - 1 + EDITORIAL_BENEFIT_SCENES.length) %
            EDITORIAL_BENEFIT_SCENES.length;
      handleSelectEditorial(EDITORIAL_BENEFIT_SCENES[nextIdx].id);
    }
  };

  return (
    <section
      id="producto"
      aria-labelledby="product-chips-heading"
      className="py-16 sm:py-24 border-b border-slate-200/80 bg-[#F4F6F8]"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12 sm:space-y-16">
        <ScrollReveal delayMs={0} direction="up">
          <div className="max-w-2xl">
            <span className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full bg-slate-100 border border-slate-200 text-xs font-bold text-slate-800 uppercase tracking-wider mb-3">
              <Sparkles className="w-3.5 h-3.5 text-[#0391FD]" />
              Cómo funciona Kuota
            </span>
            <h2
              id="product-chips-heading"
              className="text-3xl sm:text-4xl font-extrabold text-[#070A0F] tracking-tight leading-tight"
            >
              Diseñado para que tu negocio{' '}
              <span className="bg-gradient-to-r from-[#0391FD] to-[#10B981] bg-clip-text text-transparent">
                nunca pare de surtirse
              </span>
            </h2>
            <p className="mt-3 text-base sm:text-lg font-medium text-slate-600">
              Tres pilares para comprar mercadería al por mayor en kuotas directamente desde tu celular.
            </p>
          </div>
        </ScrollReveal>

        {/* BLOQUE 1: Mockup de celular interactivo + 3 Chips de beneficios */}
        <div
          id="beneficios"
          ref={chipsBlockRef}
          onMouseEnter={() => setIsChipsHovered(true)}
          onMouseLeave={() => setIsChipsHovered(false)}
          className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-center"
        >
          {/* Mockup de celular con la pantalla explicativa del beneficio activo */}
          <ScrollReveal delayMs={80} direction="left" className="lg:col-span-5 flex justify-center">
            <TiltCard maxTiltDeg={2.5} className="w-full max-w-[310px] sm:max-w-[340px]">
              <div className="relative">
                {/* Halo decorativo detrás del teléfono */}
                <div
                  aria-hidden="true"
                  className="absolute -inset-6 sm:-inset-8 rounded-[3rem] bg-[#0391FD]/15 blur-2xl"
                />

                <div
                  role="img"
                  aria-label={activeChip.placeholderLabel}
                  aria-live="polite"
                  className="relative rounded-[2.8rem] border border-white/20 bg-[#070A0F] p-3 shadow-2xl"
                >
                  {/* Pantalla del Celular */}
                  <div className="relative rounded-[2.2rem] bg-gradient-to-b from-[#0A0F14] via-[#0F172A] to-[#0A0F14] border border-white/10 overflow-hidden min-h-[460px] sm:min-h-[490px] flex flex-col text-white">
                    {/* Barra de estado + Dynamic Island */}
                    <div className="relative flex items-center justify-between px-5 pt-3 pb-2 text-[10px] font-bold text-white/70">
                      <span>9:41</span>
                      <span
                        aria-hidden="true"
                        className="absolute left-1/2 -translate-x-1/2 top-2 h-4 w-20 rounded-full bg-black/90 flex items-center justify-center"
                      >
                        <span className="w-2 h-2 rounded-full bg-emerald-400/80 mr-1" />
                      </span>
                      <span className="inline-flex items-center gap-1">
                        <Smartphone className="w-3 h-3 text-emerald-400" aria-hidden="true" />
                        Kuota App
                      </span>
                    </div>

                    <div key={activeChip.id} className="flex-1 flex flex-col px-4 sm:px-5 pb-5 pt-2">
                      {/* PANTALLA 1: SIN PAPELEO (KYC & LÍNEA INSTANTÁNEA) */}
                      {activeChip.id === 'sin-papeleo' && (
                        <div className="space-y-3.5 kuota-screen-in">
                          <div className="flex items-center justify-between">
                            <span className="text-[10px] font-extrabold uppercase tracking-wider text-emerald-400 bg-emerald-500/15 border border-emerald-500/30 px-2.5 py-0.5 rounded-full flex items-center gap-1">
                              <ShieldCheck className="w-3 h-3" />
                              RENIEC Validado ✓
                            </span>
                            <span className="text-[10px] font-bold text-slate-400">Paso 1 de 3</span>
                          </div>

                          <div className="p-3.5 rounded-2xl bg-white/[0.06] border border-white/10 space-y-2">
                            <div className="flex items-center justify-between text-xs">
                              <span className="text-slate-400">Titular DNI</span>
                              <span className="font-bold text-white">7284•••• (Activo)</span>
                            </div>
                            <div className="flex items-center justify-between text-xs">
                              <span className="text-slate-400">Billeteras</span>
                              <span className="font-bold text-emerald-400 flex items-center gap-1">
                                Yape & Plin ✓
                              </span>
                            </div>
                            <div className="flex items-center justify-between text-xs">
                              <span className="text-slate-400">Garantía / Aval</span>
                              <span className="font-bold text-slate-300">0% Requerido</span>
                            </div>
                          </div>

                          <div className="p-4 rounded-2xl bg-gradient-to-br from-emerald-500/20 to-transparent border border-emerald-500/30 space-y-1 text-center">
                            <span className="text-[10px] font-extrabold uppercase tracking-wider text-emerald-300">
                              Línea Pre-Aprobada Inmediata
                            </span>
                            <div className="text-2xl sm:text-3xl font-black text-white tabular-nums tracking-tight">
                              S/ 5,000.00
                            </div>
                            <p className="text-[11px] text-slate-300 pt-1">
                              Disponible en 60 segundos para comprar mercadería
                            </p>
                          </div>

                          <div className="rounded-xl bg-white/[0.04] p-3 text-center border border-white/5">
                            <span className="text-[11px] font-bold text-emerald-300 flex items-center justify-center gap-1.5">
                              <CheckCircle2 className="w-3.5 h-3.5" />
                              100% digital · Sin trámites bancarios
                            </span>
                          </div>
                        </div>
                      )}

                      {/* PANTALLA 2: INVIERTE MÁS, CRECE MÁS (NIVELES & LÍNEA) */}
                      {activeChip.id === 'invierte-mas' && (
                        <div className="space-y-3.5 kuota-screen-in">
                          <div className="flex items-center justify-between">
                            <span className="text-[10px] font-extrabold uppercase tracking-wider text-cyan-400 bg-cyan-500/15 border border-cyan-500/30 px-2.5 py-0.5 rounded-full flex items-center gap-1">
                              <TrendingUp className="w-3 h-3" />
                              Nivel 02 · Expansión
                            </span>
                            <span className="text-[10px] font-bold text-slate-400">Paso 2 de 3</span>
                          </div>

                          <div className="p-4 rounded-2xl bg-gradient-to-br from-[#0391FD]/20 to-transparent border border-[#0391FD]/30 space-y-1">
                            <span className="text-[10px] font-bold text-slate-300">Línea de Capital Activa</span>
                            <div className="text-2xl sm:text-3xl font-black text-white tabular-nums tracking-tight">
                              S/ 50,000.00
                            </div>
                            <div className="text-[11px] text-emerald-400 font-bold flex items-center gap-1 pt-0.5">
                              <span>↑ Desbloqueada por pagos al día</span>
                            </div>
                          </div>

                          <div className="p-3.5 rounded-2xl bg-white/[0.06] border border-white/10 space-y-2.5">
                            <div className="flex justify-between items-center text-xs">
                              <span className="text-slate-400">Historial de Cuotas</span>
                              <span className="text-emerald-400 font-bold">4 de 4 pagadas ✓</span>
                            </div>
                            <div className="grid grid-cols-4 gap-1.5 h-1.5">
                              <div className="bg-emerald-400 rounded-full" />
                              <div className="bg-emerald-400 rounded-full" />
                              <div className="bg-emerald-400 rounded-full" />
                              <div className="bg-emerald-400 rounded-full" />
                            </div>
                            <div className="flex justify-between text-[11px] text-slate-300 pt-1">
                              <span>Próximo salto:</span>
                              <span className="font-extrabold text-white">Nivel Maestro (S/ 200K+)</span>
                            </div>
                          </div>

                          <div className="grid grid-cols-2 gap-2 text-center text-xs">
                            <div className="p-2.5 rounded-xl bg-white/[0.05] border border-white/10">
                              <span className="block text-[10px] text-slate-400">Plazos Flexibles</span>
                              <span className="font-extrabold text-white">30, 60 y 90d</span>
                            </div>
                            <div className="p-2.5 rounded-xl bg-white/[0.05] border border-white/10">
                              <span className="block text-[10px] text-slate-400">Cuota Inicial</span>
                              <span className="font-extrabold text-emerald-400">Baja al 10%</span>
                            </div>
                          </div>
                        </div>
                      )}

                      {/* PANTALLA 3: TU CEREBRO DE COMPRAS CON NEXO IA */}
                      {activeChip.id === 'mas-compras' && (
                        <div className="space-y-3 kuota-screen-in">
                          <div className="flex items-center justify-between">
                            <span className="text-[10px] font-extrabold uppercase tracking-wider text-sky-400 bg-sky-500/15 border border-sky-500/30 px-2.5 py-0.5 rounded-full flex items-center gap-1">
                              <Sparkles className="w-3 h-3 text-cyan-300" />
                              Nexo IA Copiloto
                            </span>
                            <span className="text-[10px] font-bold text-slate-400">Paso 3 de 3</span>
                          </div>

                          <div className="p-3.5 rounded-2xl bg-amber-500/15 border border-amber-500/30 space-y-1">
                            <div className="flex items-center gap-1.5 text-amber-300 text-xs font-bold">
                              <span>⚠️ Alerta de Quiebre de Stock</span>
                            </div>
                            <p className="text-xs text-white font-medium">
                              Arroz Costeño 50kg & Aceite Primor 1L se agotarán en <strong>4 días</strong>.
                            </p>
                          </div>

                          <div className="p-3.5 rounded-2xl bg-white/[0.06] border border-white/10 space-y-2">
                            <div className="flex justify-between items-center text-xs">
                              <span className="text-slate-400">Orden Sugerida Mayorista</span>
                              <span className="font-bold text-white tabular-nums">S/ 3,850.00</span>
                            </div>
                            <div className="flex justify-between items-center text-xs">
                              <span className="text-slate-400">Plan en Kuotas</span>
                              <span className="font-bold text-emerald-400">3 kuotas de S/ 1,283</span>
                            </div>
                            <div className="flex justify-between items-center text-xs">
                              <span className="text-slate-400">Proveedor</span>
                              <span className="text-slate-300">Distribuidora Central</span>
                            </div>
                          </div>

                          <div className="w-full py-2.5 px-3 rounded-xl bg-gradient-to-r from-[#0391FD] to-[#10B981] text-center text-xs font-extrabold text-white shadow-md">
                            Aprobar Compra en Kuotas ✓
                          </div>
                        </div>
                      )}
                    </div>

                    {/* Indicador home inferior */}
                    <div aria-hidden="true" className="pb-2 flex justify-center">
                      <span className="h-1 w-24 rounded-full bg-white/30" />
                    </div>
                  </div>
                </div>

                {/* Indicadores de paso bajo el teléfono */}
                <div className="relative mt-5 flex items-center justify-center gap-2" aria-hidden="true">
                  {PRODUCT_CHIPS.map((chip) => (
                    <span
                      key={chip.id}
                      className={`h-2 rounded-full transition-all duration-300 ${
                        chip.id === activeChip.id ? 'w-8 bg-[#0391FD]' : 'w-2 bg-slate-300'
                      }`}
                    />
                  ))}
                </div>
              </div>
            </TiltCard>
          </ScrollReveal>

          {/* 3 Chips interactivos (acordeón con auto-avance) */}
          <div
            role="tablist"
            aria-label="Características del producto Kuota"
            aria-orientation="vertical"
            className="lg:col-span-7 flex flex-col gap-3.5 sm:gap-4"
          >
            {PRODUCT_CHIPS.map((chip, idx) => {
              const isSelected = chip.id === activeChip.id;
              const ChipIcon = CHIP_ICONS[chip.id] || Sparkles;
              return (
                <ScrollReveal key={chip.id} delayMs={idx * 90} direction="right">
                  <div
                    className={`relative overflow-hidden rounded-2xl border transition-all duration-300 ${
                      isSelected
                        ? 'border-[#0391FD] bg-white shadow-lg ring-1 ring-[#0391FD]/20 lg:-translate-y-0.5'
                        : 'border-slate-200/90 bg-white/70 hover:bg-white hover:border-slate-300 shadow-sm'
                    }`}
                  >
                    {/* Acento lateral */}
                    <span
                      aria-hidden="true"
                      className={`absolute left-0 top-0 bottom-0 w-1.5 bg-gradient-to-b from-[#0391FD] to-[#10B981] transition-transform duration-300 origin-top ${
                        isSelected ? 'scale-y-100' : 'scale-y-0'
                      }`}
                    />

                    <button
                      type="button"
                      role="tab"
                      id={`tab-${chip.id}`}
                      aria-selected={isSelected}
                      aria-controls={`panel-${chip.id}`}
                      tabIndex={isSelected ? 0 : -1}
                      onClick={() => handleSelectChip(chip, true)}
                      onKeyDown={(e) => handleKeyDown(e, idx)}
                      className="w-full text-left px-5 py-4 sm:px-6 sm:py-5 flex items-center justify-between gap-3 min-h-[64px]"
                    >
                      <div className="flex items-center gap-3 sm:gap-4 min-w-0">
                        <span
                          className={`w-10 h-10 sm:w-11 sm:h-11 rounded-xl flex items-center justify-center shrink-0 transition-colors duration-300 ${
                            isSelected
                              ? 'bg-[#0391FD] text-white shadow-md'
                              : 'bg-slate-100 text-slate-700'
                          }`}
                        >
                          <ChipIcon className="w-5 h-5" aria-hidden="true" />
                        </span>
                        <div className="min-w-0">
                          <span className="block text-[11px] font-extrabold uppercase tracking-wider text-[#0391FD]">
                            Paso 0{chip.index}
                          </span>
                          <span className="block text-lg sm:text-xl font-extrabold text-[#070A0F] leading-tight">
                            {chip.title}
                          </span>
                        </div>
                      </div>

                      <span
                        className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 transition-all duration-300 ${
                          isSelected
                            ? 'bg-[#070A0F] text-white rotate-90 shadow-sm'
                            : 'bg-slate-100 text-slate-600'
                        }`}
                        aria-hidden="true"
                      >
                        <ChevronRight className="w-4 h-4" />
                      </span>
                    </button>

                    <div
                      id={`panel-${chip.id}`}
                      role="tabpanel"
                      aria-labelledby={`tab-${chip.id}`}
                      aria-hidden={!isSelected}
                      data-open={isSelected}
                      className="kuota-accordion"
                    >
                      <div>
                        <div className="px-5 sm:px-6 pb-5 sm:pb-6 pt-1 sm:pl-[5.25rem] space-y-3">
                          <ul className="space-y-2.5">
                            {chip.bullets.map((bullet) => (
                              <li
                                key={bullet}
                                className="flex items-start gap-2.5 text-sm sm:text-base font-medium text-slate-700"
                              >
                                <CheckCircle2
                                  className="w-5 h-5 text-emerald-500 shrink-0 mt-0.5"
                                  aria-hidden="true"
                                />
                                <span>{bullet}</span>
                              </li>
                            ))}
                          </ul>

                          {chip.note && (
                            <div className="rounded-xl bg-slate-50 border border-slate-200/80 p-3 flex items-start gap-2.5">
                              <Info
                                className="w-4 h-4 text-[#0391FD] shrink-0 mt-0.5"
                                aria-hidden="true"
                              />
                              <p className="text-xs sm:text-sm text-slate-600">
                                <strong className="text-slate-900">Nota:</strong> {chip.note}
                              </p>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>

                    {/* Barra de auto-avance */}
                    {isSelected && isChipAutoRotating && (
                      <div aria-hidden="true" className="absolute left-0 right-0 bottom-0 h-1 bg-slate-100">
                        <div
                          key={`${chip.id}-progress`}
                          onAnimationEnd={handleChipProgressEnd}
                          style={{
                            animationPlayState:
                              isChipsHovered || !isChipsInView ? 'paused' : 'running',
                          }}
                          className="h-full bg-gradient-to-r from-[#0391FD] to-[#10B981] kuota-chip-progress"
                        />
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
            className="rounded-[32px] bg-white border border-slate-200/90 p-6 sm:p-8 lg:p-10 shadow-xl"
          >
            {/* Cabecera + Selector de monto demostrativo + Reproductor de escenas */}
            <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-5 pb-6 border-b border-slate-100">
              <div>
                <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-sky-50 border border-sky-200 text-[11px] font-bold uppercase tracking-wider text-[#0391FD] mb-2 shadow-xs">
                  <Sparkles className="w-3.5 h-3.5 text-[#0391FD]" aria-hidden="true" />
                  <span>Sección: Compra en Kuotas · 6 Escenas · Datos demostrativos</span>
                </div>
                <h3 className="text-2xl sm:text-3xl font-extrabold text-[#0A192F] tracking-tight">
                  Así funciona una compra financiada paso a paso
                </h3>
                <p className="text-sm sm:text-base font-normal text-slate-600 mt-1">
                  Recorre las 6 escenas o cambia el monto demostrativo para ver cómo se calculan tus kuotas en tiempo real.
                </p>
              </div>

              <div className="flex flex-wrap items-center gap-2.5 shrink-0">
                {/* Selector de lote demostrativo con transición de números */}
                <div
                  role="group"
                  aria-label="Monto de lote demostrativo"
                  className="inline-flex items-center rounded-full bg-slate-100 border border-slate-200 p-1 gap-1"
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
                  className="inline-flex items-center gap-2 px-5 py-2.5 rounded-full bg-[#0391FD] hover:bg-[#0277D4] text-white font-bold text-xs sm:text-sm shadow-xs transition-all duration-150 hover:-translate-y-0.5 active:translate-y-0 cursor-pointer"
                >
                  <Play className="w-4 h-4" aria-hidden="true" />
                  <span>{isAutoPlaying ? 'Reproduciendo 6 escenas...' : 'Simular las 6 escenas'}</span>
                </button>
                <button
                  type="button"
                  onClick={() => handleSelectScene(1)}
                  aria-label="Reiniciar demostración a la escena 1"
                  className="p-2.5 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-700 border border-slate-200 transition-colors cursor-pointer"
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
                      className={`text-left px-3.5 py-2.5 rounded-2xl border font-bold text-xs transition-all duration-200 flex items-center justify-between gap-1.5 cursor-pointer ${
                        isCurrent
                          ? 'bg-[#0A192F] text-white border-[#0A192F] shadow-sm -translate-y-0.5'
                          : isCompleted
                            ? 'bg-emerald-50 text-emerald-800 border-emerald-200/80'
                            : 'bg-white hover:bg-slate-50 text-slate-700 border-slate-200/80'
                      }`}
                    >
                      <span className="truncate">{item.label}</span>
                      {isCompleted && (
                        <CheckCircle2 className="w-3.5 h-3.5 text-[#10B981] shrink-0" />
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
            <div
              onTouchStart={handleSceneSwipeStart}
              onTouchEnd={handleSceneSwipeEnd}
              className="mt-6 grid grid-cols-1 lg:grid-cols-12 gap-6 items-stretch"
            >
              {/* Columna Izquierda: Tarjeta de operación + desglose animado */}
              <div className="lg:col-span-5 rounded-[24px] bg-[#0A192F] text-white p-5 sm:p-6 flex flex-col justify-between border border-white/10 shadow-xl">
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
              <div className="lg:col-span-7 rounded-[24px] bg-slate-50 border border-slate-200/90 p-5 sm:p-6 flex flex-col justify-between shadow-xs">
                <div>
                  <div className="flex flex-wrap items-center justify-between gap-2 mb-4">
                    <span className="inline-flex items-center gap-1.5 text-xs sm:text-sm font-bold text-[#0A192F] uppercase tracking-wider">
                      <Calendar className="w-4 h-4 text-[#0391FD]" aria-hidden="true" />
                      Calendario y seguimiento de kuotas
                    </span>
                    <span className="text-xs font-bold px-3 py-1 rounded-full bg-white border border-slate-200 text-slate-700 shadow-xs">
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
                  <div className="mt-4 rounded-xl bg-white border border-slate-200/90 p-3.5 shadow-xs">
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
                <div className="mt-5 pt-3.5 border-t border-slate-100 flex flex-wrap items-center justify-between gap-2 text-xs font-medium text-slate-600">
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
                    className="inline-flex items-center gap-1 text-[#0391FD] hover:text-[#0A192F] font-bold transition-colors cursor-pointer"
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
          <div className="rounded-[32px] bg-white border border-slate-200/80 p-6 sm:p-8 lg:p-10 shadow-xl">
            {/* Selector superior de Escenas Editoriales A–E */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-6 border-b border-slate-100">
              <div>
                <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#0A192F] text-white text-[11px] font-bold uppercase tracking-wider mb-2">
                  <Layers className="w-3.5 h-3.5 text-[#0391FD]" aria-hidden="true" />
                  Experiencia para Compradores · 5 Escenas Editoriales
                </span>
                <h3 className="text-2xl sm:text-3xl font-black text-[#0A192F] tracking-tight">
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
                      className={`px-4 py-2 rounded-full border text-xs font-bold transition-all duration-200 cursor-pointer ${
                        isSelected
                          ? 'bg-[#0391FD] text-white border-[#0391FD] shadow-sm -translate-y-0.5'
                          : 'bg-slate-100 hover:bg-slate-200 text-slate-700 border-slate-200/80'
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
              onTouchStart={handleEditorialSwipeStart}
              onTouchEnd={handleEditorialSwipeEnd}
              className="mt-8 grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-10 items-center kuota-section-arrive"
            >
              {/* Lado Izquierdo: Titular grande, descripción breve y CTA correspondiente */}
              <div className="lg:col-span-6 flex flex-col items-start text-left">
                <span className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-sky-50 border border-sky-200/80 text-xs font-bold uppercase tracking-wider text-[#0391FD] mb-4">
                  {activeEditorial.code}
                </span>

                <h4 className="text-3xl sm:text-4xl lg:text-[2.65rem] font-black text-[#0A192F] tracking-tight leading-[1.08] mb-4">
                  {activeEditorial.headline}{' '}
                  <span className="text-[#0391FD] underline decoration-[#0A192F]/20 decoration-4 underline-offset-4">
                    {activeEditorial.highlight}
                  </span>
                </h4>

                <p className="text-base sm:text-lg font-normal text-slate-600 leading-relaxed mb-7">
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
                    <div className="rounded-2xl border border-slate-200/80 bg-white/95 px-3.5 py-2 shadow-lg backdrop-blur-md">
                      <span className="block text-[10px] font-bold uppercase tracking-wider text-slate-400">
                        {activeEditorial.badgeTopTitle}
                      </span>
                      <span className="text-xs sm:text-sm font-bold text-[#0A192F]">
                        {activeEditorial.badgeTopValue}
                      </span>
                    </div>
                  }
                  floatingRightBadge={
                    <div className="rounded-2xl border border-white/10 bg-[#0A192F]/95 text-white px-3.5 py-2 shadow-xl backdrop-blur-md">
                      <span className="block text-[10px] font-bold uppercase tracking-wider text-slate-300">
                        {activeEditorial.badgeRightTitle}
                      </span>
                      <span className="text-xs sm:text-sm font-bold text-white">
                        {activeEditorial.badgeRightValue}
                      </span>
                    </div>
                  }
                />

                {/* Componente financiero animado único por cada escena A–E (§5) */}
                <FinancialCardMotion
                  maxTiltDeg={2.4}
                  className="w-full max-w-md mt-6 rounded-[24px] border border-white/10 bg-[#0A192F] text-white p-5 shadow-xl"
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

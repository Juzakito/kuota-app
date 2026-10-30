import React, { useState } from 'react';
import {
  Smartphone,
  Clock,
  ChevronRight,
  ShieldCheck,
  Store,
  CheckCircle2,
  Calendar,
  BarChart3,
  Layers,
  CreditCard,
} from 'lucide-react';
import {
  SUPPLIER_PRODUCT_CHIPS,
  SupplierProductChipItem,
  PRICING_CONFIG,
} from '../../config/pricingConfig';
import { trackSupplierEvent } from '../../utils/analytics';
import {
  ScrollReveal,
  CircularImageReveal,
  FinancialCardMotion,
  AnimatedNumber,
  PaymentProgress,
} from '../../utils/motionSystem';
import { ScheduleCallButton } from './ScheduleCallButton';
import { kuotaSound } from '../../utils/soundSystem';

interface PeruvianMerchantSector {
  id: string;
  name: string;
  contextTitle: string;
  contextDescription: string;
  demoProductLabel: string;
  demoTicketSoles: number;
  imageSrc: string;
  imagePosition: string;
}

/**
 * §6 — EXPERIENCIA VISUAL PARA COMERCIOS:
 * Situaciones reales del comercio peruano (Bodegas, Minimarkets, Tiendas de tecnología,
 * Tiendas de ropa, Ferreterías, Comercios locales).
 */
const PERUVIAN_MERCHANT_SECTORS: PeruvianMerchantSector[] = [
  {
    id: 'bodegas',
    name: 'Bodegas',
    contextTitle: 'Bodegas y abarrotes de barrio',
    contextDescription:
      'Cobra con QR en mostrador con solo 2% de comisión y elimina el cuaderno de fiados ofreciendo aKuotas! sin riesgo para tu caja.',
    demoProductLabel: 'Pedido de abarrotes y bebidas (Demostrativo)',
    demoTicketSoles: 450,
    imageSrc: '/images/hero-negocio-kuota.jpg',
    imagePosition: 'center center',
  },
  {
    id: 'minimarkets',
    name: 'Minimarkets',
    contextTitle: 'Minimarkets y autoservicios',
    contextDescription:
      'Agiliza la fila en caja sin alquilar equipos POS costosos y recibe la liquidación de tus cobros en 48 horas hábiles.',
    demoProductLabel: 'Canasta familiar / surtido semanal (Demostrativo)',
    demoTicketSoles: 680,
    imageSrc: '/images/hero-negocio-kuota.jpg',
    imagePosition: 'left center',
  },
  {
    id: 'tecnologia',
    name: 'Tiendas de tecnología',
    contextTitle: 'Tiendas de tecnología y accesorios',
    contextDescription:
      'Permite que tus clientes financien equipos o lotes de accesorios en kuotas mientras tu tienda cobra seguro en 48h.',
    demoProductLabel: 'Equipo móvil y accesorios de trabajo (Demostrativo)',
    demoTicketSoles: 1500,
    imageSrc: '/images/formalizacion-kuota.jpg',
    imagePosition: 'center center',
  },
  {
    id: 'ropa',
    name: 'Tiendas de ropa',
    contextTitle: 'Tiendas de ropa, calzado y textil',
    contextDescription:
      'Cierra ventas por mostrador o por WhatsApp con Kuota Link y ofrece pago fraccionado en campañas sin asumir cobranza.',
    demoProductLabel: 'Lote de prendas de temporada (Demostrativo)',
    demoTicketSoles: 900,
    imageSrc: '/images/hero-negocio-kuota.jpg',
    imagePosition: 'right center',
  },
  {
    id: 'ferreterias',
    name: 'Ferreterías',
    contextTitle: 'Ferreterías y materiales de construcción',
    contextDescription:
      'Facilita la compra de herramientas y materiales a maestros y negocios locales con colocaciones aKuotas! y 0% riesgo de impago.',
    demoProductLabel: 'Kit de herramientas y materiales (Demostrativo)',
    demoTicketSoles: 1800,
    imageSrc: '/images/formalizacion-kuota.jpg',
    imagePosition: 'left center',
  },
  {
    id: 'comercios-locales',
    name: 'Comercios locales',
    contextTitle: 'Comercios locales y distribuidoras mayoristas',
    contextDescription:
      'Conecta con compradores de la red Kuota, ofrece alternativas de pago desde tu celular y lleva el control de tus liquidaciones.',
    demoProductLabel: 'Lote mayorista para negocio (Demostrativo)',
    demoTicketSoles: 2400,
    imageSrc: '/images/hero-negocio-kuota.jpg',
    imagePosition: 'center top',
  },
];

type MerchantDemoTabId =
  | 'compra'
  | 'colocacion'
  | 'calendario'
  | 'recaudacion'
  | 'indicadores';

interface MerchantDemoTab {
  id: MerchantDemoTabId;
  label: string;
  shortTitle: string;
}

const MERCHANT_DEMO_TABS: MerchantDemoTab[] = [
  { id: 'compra', label: '1. Compra realizada', shortTitle: 'Cobro 2% QR / Link' },
  { id: 'colocacion', label: '2. Operación de colocación', shortTitle: 'Venta con aKuotas!' },
  { id: 'calendario', label: '3. Calendario de cuotas', shortTitle: 'Gestión Kuota' },
  { id: 'recaudacion', label: '4. Seguimiento de recaudación', shortTitle: 'Liquidación 48h' },
  { id: 'indicadores', label: '5. Indicadores de ventas', shortTitle: 'Resumen operativo' },
];

/**
 * PRD §5.5 + §6 EXPERIENCIA VISUAL PARA COMERCIOS:
 * - Tres chips interactivos de cobro (Kuota QR, Kuota Link, aKuotas!) con sus capturas IMG-10 a IMG-16.
 * - Narrativa visual independiente para el comercio peruano con los 6 rubros y las 5 demostraciones
 *   gráficas operativas (claramente identificadas como demostrativas).
 */
export const ProductChipsSection: React.FC = () => {
  const [activeChipId, setActiveChipId] = useState<string>(
    SUPPLIER_PRODUCT_CHIPS[0].id
  );
  const [activeCaptureIdx, setActiveCaptureIdx] = useState<number>(0);
  const [selectedSectorId, setSelectedSectorId] = useState<string>(
    PERUVIAN_MERCHANT_SECTORS[0].id
  );
  const [activeDemoTab, setActiveDemoTab] = useState<MerchantDemoTabId>('colocacion');

  const activeChip: SupplierProductChipItem =
    SUPPLIER_PRODUCT_CHIPS.find((c) => c.id === activeChipId) ||
    SUPPLIER_PRODUCT_CHIPS[0];

  const activeSector: PeruvianMerchantSector =
    PERUVIAN_MERCHANT_SECTORS.find((s) => s.id === selectedSectorId) ||
    PERUVIAN_MERCHANT_SECTORS[0];

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

  // Cálculos demostrativos para el rubro seleccionado
  const ticketSoles = activeSector.demoTicketSoles;
  const kuotaFeeSoles = Math.round(ticketSoles * 0.02);
  const netMerchantSettlementSoles = ticketSoles - kuotaFeeSoles;
  const buyerInitialSoles = Math.round(ticketSoles * 0.3);
  const buyerKuotaSoles = Math.round(((ticketSoles * 1.05) - buyerInitialSoles) / 3);

  return (
    <section
      id="producto"
      aria-labelledby="supplier-product-heading"
      className="py-16 sm:py-20 border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-16">
        <ScrollReveal delayMs={0} direction="up">
          <div className="max-w-2xl">
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
        </ScrollReveal>

        {/* BLOQUE 1: Capturas de celular (IMG-10 a IMG-16) + 3 Chips de Producto (PRD §5.5 preservado) */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-10 items-start">
          {/* Columna de Capturas (IMG-10 a IMG-16): izquierda en desktop, debajo en móvil */}
          <ScrollReveal
            delayMs={80}
            direction="left"
            className="order-2 lg:order-1 lg:col-span-5 flex flex-col items-center"
          >
            <div className="w-full max-w-sm rounded-3xl border border-slate-200/90 bg-white p-5 shadow-xl">
              <div className="flex items-center justify-between gap-2 mb-3">
                <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#070A0F] text-white text-xs font-bold uppercase">
                  <Smartphone className="w-3.5 h-3.5 text-emerald-400" aria-hidden="true" />
                  {currentCapture.title}
                </span>
                <span className="px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-700 text-xs font-bold border border-emerald-500/20">
                  Flujo Digital 100%
                </span>
              </div>

              {/* Pestañas de secuencia */}
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
                      onClick={() => {
                        kuotaSound.playSelectOption();
                        setActiveCaptureIdx(i);
                      }}
                      className={`flex-1 py-1.5 px-2.5 rounded-xl text-xs font-bold transition-all ${
                        isCapActive
                          ? 'bg-[#0391FD] text-white shadow-sm'
                          : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                      }`}
                    >
                      Paso 0{i + 1}: {cap.title}
                    </button>
                  );
                })}
              </div>

              {/* Mockup esquemático de la captura con transición animada */}
              <div
                key={`${activeChip.id}-${activeCaptureIdx}`}
                role="img"
                aria-label={currentCapture.description}
                aria-live="polite"
                className="rounded-2xl border border-white/10 bg-[#0A0F14] text-white p-5 mb-4 shadow-inner"
              >
                <div className="flex items-center justify-between text-xs text-slate-400 font-bold pb-2.5 mb-3 border-b border-white/10">
                  <span>{currentCapture.mockHeader}</span>
                  <span className="px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 font-bold border border-emerald-500/30 text-[10px]">
                    Operación Activa
                  </span>
                </div>

                <div className="rounded-xl bg-[#0391FD]/15 border border-[#0391FD]/30 p-3.5 mb-3 text-center">
                  <div className="text-base sm:text-lg font-black text-white">
                    {currentCapture.mockHighlight}
                  </div>
                </div>

                <div className="space-y-2">
                  {currentCapture.mockRows.map((row) => (
                    <div
                      key={row.label}
                      className="rounded-xl bg-white/[0.06] border border-white/10 px-3.5 py-2.5 flex items-center justify-between gap-2 text-xs"
                    >
                      <span className="text-slate-400 font-medium">
                        {row.label}
                      </span>
                      <span className="font-extrabold text-white">
                        {row.value}
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="rounded-xl bg-slate-50 border border-slate-200/80 px-3.5 py-2.5 text-left">
                <p className="text-xs sm:text-sm font-semibold text-slate-700">
                  {currentCapture.description}
                </p>
              </div>
            </div>
          </ScrollReveal>

          {/* Columna de 3 Chips + Badge fijo debajo */}
          <div className="order-1 lg:order-2 lg:col-span-7 flex flex-col gap-4">
            <div
              role="tablist"
              aria-label="Soluciones de cobro Kuota para proveedores"
              className="flex flex-col gap-4"
            >
              {SUPPLIER_PRODUCT_CHIPS.map((chip, idx) => {
                const isSelected = chip.id === activeChip.id;
                return (
                  <ScrollReveal key={chip.id} delayMs={idx * 90} direction="right">
                    <div
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
                          className="px-5 sm:px-6 pb-6 pt-2 border-t border-kuota-deepnavy/15 space-y-3 animate-fadeIn"
                        >
                          <p className="text-base sm:text-lg font-bold text-kuota-deepnavy/90 leading-relaxed">
                            {chip.description}
                          </p>

                          <div className="flex flex-wrap items-center gap-2 pt-1">
                            <span className="text-xs font-bold text-slate-500">
                              Pantallas del flujo:
                            </span>
                            {chip.captures.map((cap, capIdx) => (
                              <button
                                key={cap.placeholderId}
                                type="button"
                                onClick={() => {
                                  kuotaSound.playSelectOption();
                                  setActiveCaptureIdx(capIdx);
                                }}
                                className={`text-xs font-bold px-2.5 py-1 rounded-lg transition-all ${
                                  activeCaptureIdx === capIdx
                                    ? 'bg-[#0391FD] text-white shadow-sm'
                                    : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                                }`}
                              >
                                Paso 0{capIdx + 1}: {cap.title}
                              </button>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  </ScrollReveal>
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

        {/* BLOQUE 2 (§6): EXPERIENCIA VISUAL PARA COMERCIOS PERUANOS + 5 DEMOSTRACIONES OPERATIVAS */}
        <ScrollReveal delayMs={110} direction="up">
          <div className="rounded-3xl bg-white border-2 border-kuota-deepnavy p-6 sm:p-8 lg:p-10 shadow-navy-hard-lg">
            {/* Cabecera y selector de rubros del comercio peruano (§6) */}
            <div className="pb-6 border-b-2 border-kuota-palesky space-y-4">
              <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-3">
                <div>
                  <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-kuota-palesky border border-kuota-deepnavy text-[11px] font-extrabold uppercase tracking-wider text-kuota-deepnavy mb-2">
                    <Store className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                    Experiencia Visual para Comercios · Datos demostrativos
                  </span>
                  <h3 className="text-2xl sm:text-3xl font-extrabold text-kuota-deepnavy tracking-tight">
                    Adaptado a la realidad del comercio peruano
                  </h3>
                  <p className="text-sm sm:text-base font-semibold text-kuota-crayola mt-1">
                    Selecciona el rubro de tu negocio y explora cómo funciona una operación de colocación, su calendario y su liquidación en 48h.
                  </p>
                </div>

                <ScheduleCallButton
                  section="experiencia-comercios"
                  label="Agenda una call"
                  size="md"
                  className="shrink-0"
                />
              </div>

              {/* 6 Rubros del Comercio Peruano (§6) */}
              <div
                role="tablist"
                aria-label="Rubros del comercio peruano"
                className="flex flex-wrap gap-2"
              >
                {PERUVIAN_MERCHANT_SECTORS.map((sector) => {
                  const isSectorActive = sector.id === activeSector.id;
                  return (
                    <button
                      key={sector.id}
                      type="button"
                      role="tab"
                      aria-selected={isSectorActive}
                      onClick={() => {
                        kuotaSound.playSelectOption();
                        setSelectedSectorId(sector.id);
                        trackSupplierEvent('merchant_sector_select', {
                          sector_id: sector.id,
                        });
                      }}
                      className={`px-3.5 py-2 rounded-xl border-2 border-kuota-deepnavy text-xs sm:text-sm font-extrabold transition-all duration-200 ${
                        isSectorActive
                          ? 'bg-kuota-dodger text-white shadow-navy-hard -translate-y-0.5'
                          : 'bg-kuota-palesky/55 hover:bg-kuota-palesky text-kuota-deepnavy'
                      }`}
                    >
                      {sector.name}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Composición dividida: Fotografía Circular del Emprendedor + 5 Demostraciones Gráficas (§6) */}
            <div className="mt-8 grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
              {/* Lado Izquierdo: Fotografía circular y contexto del rubro */}
              <div className="lg:col-span-5 flex flex-col items-center lg:items-start text-center lg:text-left">
                <div className="w-full flex justify-center mb-6">
                  <CircularImageReveal
                    src={activeSector.imageSrc}
                    alt={activeSector.contextTitle}
                    sizeClassName="w-56 h-56 sm:w-64 sm:h-64"
                    objectPosition={activeSector.imagePosition}
                    topBadge={
                      <div className="rounded-2xl border-2 border-kuota-deepnavy bg-white px-3 py-1.5 shadow-navy-hard text-left">
                        <span className="block text-[10px] font-extrabold uppercase text-kuota-crayola">
                          Rubro activo
                        </span>
                        <span className="text-xs font-extrabold text-kuota-deepnavy">
                          {activeSector.name}
                        </span>
                      </div>
                    }
                    floatingRightBadge={
                      <div className="rounded-2xl border-2 border-kuota-deepnavy bg-kuota-deepnavy text-white px-3 py-1.5 shadow-navy-hard text-left">
                        <span className="block text-[10px] font-extrabold uppercase text-kuota-palesky">
                          Ticket demostrativo
                        </span>
                        <AnimatedNumber
                          value={ticketSoles}
                          prefix="S/ "
                          className="text-xs sm:text-sm font-extrabold text-white"
                        />
                      </div>
                    }
                  />
                </div>

                <h4 className="text-xl sm:text-2xl font-extrabold text-kuota-deepnavy">
                  {activeSector.contextTitle}
                </h4>
                <p className="text-sm sm:text-base font-semibold text-kuota-crayola mt-2 leading-relaxed">
                  {activeSector.contextDescription}
                </p>
              </div>

              {/* Lado Derecho: Las 5 Demostraciones Gráficas Operativas (§6) */}
              <div className="lg:col-span-7 space-y-4">
                <div
                  role="tablist"
                  aria-label="Demostraciones operativas para comercios"
                  className="grid grid-cols-2 sm:grid-cols-5 gap-1.5"
                >
                  {MERCHANT_DEMO_TABS.map((tab) => {
                    const isTabActive = tab.id === activeDemoTab;
                    return (
                      <button
                        key={tab.id}
                        type="button"
                        role="tab"
                        aria-selected={isTabActive}
                        onClick={() => {
                          kuotaSound.playSelectOption();
                          setActiveDemoTab(tab.id);
                        }}
                        className={`p-2.5 rounded-xl border-2 text-left transition-all ${
                          isTabActive
                            ? 'bg-kuota-deepnavy text-white border-kuota-deepnavy shadow-sm'
                            : 'bg-kuota-palesky/45 hover:bg-kuota-palesky text-kuota-deepnavy border-kuota-deepnavy/35'
                        }`}
                      >
                        <span className="block text-[11px] font-extrabold leading-tight">
                          {tab.label}
                        </span>
                        <span
                          className={`block text-[10px] font-semibold mt-0.5 ${
                            isTabActive ? 'text-kuota-palesky' : 'text-kuota-crayola'
                          }`}
                        >
                          {tab.shortTitle}
                        </span>
                      </button>
                    );
                  })}
                </div>

                {/* Panel de la demostración gráfica activa */}
                <FinancialCardMotion
                  maxTiltDeg={2}
                  className="rounded-2xl bg-kuota-deepnavy text-white border-2 border-kuota-deepnavy p-5 sm:p-6 shadow-navy-hard-lg"
                >
                  <div className="flex flex-wrap items-center justify-between gap-2 pb-3 mb-4 border-b border-white/15">
                    <span className="inline-flex items-center gap-1.5 text-xs font-extrabold uppercase tracking-wider text-kuota-palesky">
                      <Layers className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                      {activeSector.demoProductLabel}
                    </span>
                    <span className="px-2.5 py-0.5 rounded-full bg-kuota-dodger text-white text-[11px] font-extrabold">
                      Datos demostrativos
                    </span>
                  </div>

                  {activeDemoTab === 'compra' && (
                    <div key="compra" className="space-y-3 kuota-section-arrive">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-bold text-kuota-palesky">
                          1. Compra realizada en {activeSector.name}
                        </span>
                        <span className="text-xs font-extrabold px-2 py-0.5 rounded bg-white/10 text-white">
                          Kuota QR / Link
                        </span>
                      </div>
                      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                        <div className="rounded-xl bg-white/10 border border-white/15 p-3">
                          <span className="block text-[11px] text-kuota-palesky">
                            Monto cobrado
                          </span>
                          <AnimatedNumber
                            value={ticketSoles}
                            prefix="S/ "
                            decimals={2}
                            className="text-lg font-extrabold text-white"
                          />
                        </div>
                        <div className="rounded-xl bg-white/10 border border-white/15 p-3">
                          <span className="block text-[11px] text-kuota-palesky">
                            Comisión Kuota (2%)
                          </span>
                          <AnimatedNumber
                            value={kuotaFeeSoles}
                            prefix="- S/ "
                            decimals={2}
                            className="text-lg font-extrabold text-kuota-dodger"
                          />
                        </div>
                        <div className="rounded-xl bg-kuota-dodger/30 border border-kuota-dodger p-3">
                          <span className="block text-[11px] text-kuota-palesky">
                            Neto para tu comercio
                          </span>
                          <AnimatedNumber
                            value={netMerchantSettlementSoles}
                            prefix="S/ "
                            decimals={2}
                            className="text-lg font-extrabold text-white"
                          />
                        </div>
                      </div>
                    </div>
                  )}

                  {activeDemoTab === 'colocacion' && (
                    <div key="colocacion" className="space-y-3 kuota-section-arrive">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-bold text-kuota-palesky">
                          2. Operación de colocación con aKuotas!
                        </span>
                        <span className="text-xs font-extrabold px-2 py-0.5 rounded bg-kuota-dodger text-white">
                          0% riesgo para tu negocio
                        </span>
                      </div>
                      <p className="text-xs text-kuota-palesky/90">
                        Tu cliente divide su compra de S/ {ticketSoles} en kuotas desde su celular. Tú entregas la mercadería con respaldo total de Kuota:
                      </p>
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
                        <div className="rounded-xl bg-white/10 border border-white/15 p-3.5 space-y-1">
                          <span className="text-kuota-palesky font-bold block">
                            Para el comprador (Demostrativo)
                          </span>
                          <div className="font-extrabold text-white text-sm">
                            Inicial S/ {buyerInitialSoles} + 3 kuotas de S/ {buyerKuotaSoles}
                          </div>
                          <span className="text-kuota-dodger font-bold block">
                            5% de interés flat asumido por el comprador
                          </span>
                        </div>
                        <div className="rounded-xl bg-kuota-dodger/25 border border-kuota-palesky/40 p-3.5 space-y-1">
                          <span className="text-kuota-palesky font-bold block">
                            Para tu comercio ({activeSector.name})
                          </span>
                          <div className="font-extrabold text-white text-sm">
                            Recibes S/ {netMerchantSettlementSoles}.00 netos en 48h
                          </div>
                          <span className="text-kuota-palesky font-bold block">
                            Kuota asume el 100% de la gestión de cobro
                          </span>
                        </div>
                      </div>
                    </div>
                  )}

                  {activeDemoTab === 'calendario' && (
                    <div key="calendario" className="space-y-3 kuota-section-arrive">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-bold text-kuota-palesky flex items-center gap-1.5">
                          <Calendar className="w-4 h-4 text-kuota-dodger" />
                          3. Calendario de cuotas del comprador (Gestionado por Kuota)
                        </span>
                        <span className="text-xs font-extrabold text-white">
                          Sin cobranza para ti
                        </span>
                      </div>
                      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs">
                        {[
                          { title: 'Inicial (Hoy)', amount: buyerInitialSoles, state: 'Confirmada ✓' },
                          { title: 'Kuota 1 (Día 20)', amount: buyerKuotaSoles, state: 'Recauda Kuota' },
                          { title: 'Kuota 2 (Día 40)', amount: buyerKuotaSoles, state: 'Recauda Kuota' },
                          { title: 'Kuota 3 (Día 60)', amount: buyerKuotaSoles, state: 'Recauda Kuota' },
                        ].map((item) => (
                          <div
                            key={item.title}
                            className="rounded-xl bg-white/10 border border-white/15 p-2.5"
                          >
                            <span className="block text-[10px] text-kuota-palesky font-bold">
                              {item.title}
                            </span>
                            <span className="block text-sm font-extrabold text-white mt-0.5">
                              S/ {item.amount}
                            </span>
                            <span className="block text-[10px] text-kuota-dodger font-extrabold mt-0.5">
                              {item.state}
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {activeDemoTab === 'recaudacion' && (
                    <div key="recaudacion" className="space-y-3 kuota-section-arrive">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-bold text-kuota-palesky flex items-center gap-1.5">
                          <CheckCircle2 className="w-4 h-4 text-kuota-dodger" />
                          4. Seguimiento de recaudación y abono al comercio
                        </span>
                        <span className="text-xs font-extrabold text-kuota-dodger">
                          48 horas hábiles
                        </span>
                      </div>
                      <div className="rounded-xl bg-white p-3.5 text-kuota-deepnavy">
                        <PaymentProgress
                          label="Ciclo de liquidación bancaria al comercio"
                          currentStep={3}
                          totalSteps={3}
                          statusText={`S/ ${netMerchantSettlementSoles}.00 abonados en cuenta`}
                        />
                      </div>
                      <div className="grid grid-cols-3 gap-2 text-[11px] font-bold text-kuota-palesky text-center">
                        <div className="rounded-lg bg-white/10 p-2">
                          1. Cobro confirmado
                        </div>
                        <div className="rounded-lg bg-white/10 p-2">
                          2. Conciliación Kuota
                        </div>
                        <div className="rounded-lg bg-kuota-dodger text-white p-2">
                          3. Abono 48h ✓
                        </div>
                      </div>
                    </div>
                  )}

                  {activeDemoTab === 'indicadores' && (
                    <div key="indicadores" className="space-y-3 kuota-section-arrive">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-bold text-kuota-palesky flex items-center gap-1.5">
                          <BarChart3 className="w-4 h-4 text-kuota-dodger" />
                          5. Indicadores operativos en plataforma (Vista demostrativa)
                        </span>
                        <span className="text-xs font-extrabold text-white">
                          Control en tiempo real
                        </span>
                      </div>
                      <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5 text-xs">
                        <div className="rounded-xl bg-white/10 border border-white/15 p-3">
                          <span className="block text-[10px] text-kuota-palesky font-bold uppercase">
                            Modalidades activas
                          </span>
                          <span className="text-sm font-extrabold text-white flex items-center gap-1 mt-1">
                            <CreditCard className="w-3.5 h-3.5 text-kuota-dodger" />
                            QR · Link · aKuotas!
                          </span>
                        </div>
                        <div className="rounded-xl bg-white/10 border border-white/15 p-3">
                          <span className="block text-[10px] text-kuota-palesky font-bold uppercase">
                            Tarifa por operación
                          </span>
                          <span className="text-sm font-extrabold text-kuota-dodger mt-1 block">
                            2% plana (vs 3,5% POS)
                          </span>
                        </div>
                        <div className="rounded-xl bg-white/10 border border-white/15 p-3">
                          <span className="block text-[10px] text-kuota-palesky font-bold uppercase">
                            Riesgo de cobranza
                          </span>
                          <span className="text-sm font-extrabold text-white mt-1 block">
                            0% para el comercio
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

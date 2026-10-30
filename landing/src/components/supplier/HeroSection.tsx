import React from 'react';
import { ArrowRight, ShieldCheck, Clock, QrCode, CheckCircle2 } from 'lucide-react';
import { ScheduleCallButton } from './ScheduleCallButton';
import { PRICING_CONFIG, formatPercentEs } from '../../config/pricingConfig';
import { trackSupplierEvent } from '../../utils/analytics';
import {
  smoothScrollToAnchor,
  ScrollReveal,
  CircularImageReveal,
  FinancialCardMotion,
} from '../../utils/motionSystem';
import { kuotaSound } from '../../utils/soundSystem';

/**
 * PRD §5.2 + SISTEMA DE ANIMACIÓN FINTECH (§2, §3 y §6 — HERO PROVEEDORES Y COMERCIOS):
 * - Título (H1): "No pierdas ventas por culpa de las comisiones".
 * - CTA primario: "Agenda una call". CTA secundario: "Ver cómo funciona" (ancla a #producto).
 * - Composición visual circular del comercio (`CircularImageReveal`) + tarjeta flotante
 *   destacada "POS 3,5% → Kuota 1%" y estado de liquidación en 48h con 0% riesgo.
 */
export const HeroSection: React.FC = () => {
  const posFeeText = formatPercentEs(PRICING_CONFIG.posAverageFeePercent);
  const kuotaFeeText = formatPercentEs(PRICING_CONFIG.kuotaPaymentsFeePercent);

  return (
    <section
      id="hero-proveedores"
      aria-labelledby="hero-supplier-heading"
      className="relative overflow-hidden pt-8 pb-16 sm:py-16 lg:py-20 border-b border-slate-200/80 bg-slate-50/40"
    >
      {/* Halo ambiental sutil */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -top-24 right-1/4 w-96 h-96 rounded-full bg-[#0391FD]/10 blur-3xl"
      />

      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-8 items-center">
          {/* Columna de Copy y CTAs */}
          <div className="lg:col-span-6 flex flex-col items-start text-left">
            <ScrollReveal delayMs={0} direction="up">
              <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white border border-slate-200/90 shadow-xs mb-5">
                <span
                  className="w-2.5 h-2.5 rounded-full bg-[#10B981] animate-pulse"
                  aria-hidden="true"
                />
                <span className="text-xs font-bold text-[#0A192F] uppercase tracking-wider">
                  Kuota para Proveedores y Comercios
                </span>
              </div>
            </ScrollReveal>

            <ScrollReveal delayMs={80} direction="up">
              <h1
                id="hero-supplier-heading"
                className="text-4xl sm:text-5xl lg:text-[3.25rem] font-extrabold text-[#0A192F] tracking-tight leading-[1.08] mb-5"
              >
                No pierdas ventas por culpa de las{' '}
                <span className="text-[#0391FD]">
                  comisiones
                </span>
              </h1>
            </ScrollReveal>

            <ScrollReveal delayMs={160} direction="up">
              <p className="text-base sm:text-xl font-normal text-slate-600 leading-relaxed mb-7 max-w-xl">
                Tus clientes compran con tarjeta. Los POS tradicionales se quedan con tu margen. Kuota Payments te cobra solo{' '}
                <strong className="text-[#0391FD] font-bold">1% de comisión</strong> y te permite vender a crédito B2B con{' '}
                <strong className="text-[#0A192F] font-bold">
                  0% riesgo de impago para ti.
                </strong>
              </p>
            </ScrollReveal>

            {/* Píldoras de respaldo rápido */}
            <ScrollReveal delayMs={230} direction="up">
              <div className="flex flex-wrap gap-2.5 mb-7">
                <span className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-white border border-slate-200/90 text-xs sm:text-sm font-semibold text-slate-700 shadow-xs">
                  <ShieldCheck className="w-4 h-4 text-[#10B981]" aria-hidden="true" />
                  Comisión {kuotaFeeText} sin costo de alquiler POS
                </span>
                <span className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-white border border-slate-200/90 text-xs sm:text-sm font-semibold text-slate-700 shadow-xs">
                  <Clock className="w-4 h-4 text-[#0391FD]" aria-hidden="true" />
                  Liquidación en {PRICING_CONFIG.settlementBusinessHours} horas hábiles
                </span>
              </div>
            </ScrollReveal>

            {/* CTAs: Primario y Secundario */}
            <ScrollReveal delayMs={300} direction="up" className="w-full sm:w-auto">
              <div className="w-full sm:w-auto flex flex-col sm:flex-row items-stretch sm:items-center gap-3.5">
                <ScheduleCallButton
                  section="hero"
                  label="Agenda una llamada"
                  size="lg"
                />

                <a
                  href="#producto"
                  onClick={(e) => {
                    e.preventDefault();
                    kuotaSound.playSelectOption();
                    trackSupplierEvent('cta_secondary_click', {
                      section: 'hero',
                      target: '#producto',
                    });
                    smoothScrollToAnchor('#producto');
                  }}
                  className="inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-full bg-white hover:bg-slate-50 text-[#0A192F] font-bold text-sm sm:text-base border border-slate-200/90 shadow-xs transition-all hover:-translate-y-0.5 active:scale-95 cursor-pointer"
                >
                  <span>Ver cómo funciona</span>
                  <ArrowRight className="w-4 h-4 text-[#0391FD]" aria-hidden="true" />
                </a>
              </div>
            </ScrollReveal>
          </div>

          {/* Columna Derecha: Fotografía Circular del Comercio (IMG-01) + Tarjeta Flotante "POS 3,5% → Kuota 1%" */}
          <ScrollReveal
            delayMs={150}
            direction="scale"
            className="lg:col-span-6 relative flex flex-col items-center"
          >
            <CircularImageReveal
              src="/images/hero-negocio-kuota.jpg"
              alt="Dueño de negocio cobrando en su mostrador con Kuota QR y celular"
              sizeClassName="w-64 h-64 sm:w-80 sm:h-80 lg:w-[21rem] lg:h-[21rem]"
              objectPosition="center center"
              topBadge={
                <div className="rounded-2xl border border-slate-200/90 bg-white px-3.5 py-2 shadow-md flex items-center gap-2">
                  <span className="w-7 h-7 rounded-xl bg-[#0391FD] text-white flex items-center justify-center shrink-0">
                    <QrCode className="w-4 h-4" aria-hidden="true" />
                  </span>
                  <div className="text-left">
                    <span className="block text-[10px] font-bold uppercase tracking-wider text-slate-500">
                      Cobro desde celular
                    </span>
                    <span className="text-xs sm:text-sm font-bold text-[#0A192F]">
                      QR · Link · Cuotas B2B
                    </span>
                  </div>
                </div>
              }
              floatingRightBadge={
                <div className="rounded-2xl border border-slate-200/90 bg-white px-3.5 py-2 shadow-md">
                  <div className="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-wider text-emerald-800">
                    <CheckCircle2 className="w-3.5 h-3.5 text-[#10B981]" aria-hidden="true" />
                    <span>Riesgo comercio</span>
                  </div>
                  <div className="text-xs sm:text-sm font-extrabold text-[#0A192F] mt-0.5">
                    0% riesgo de impago
                  </div>
                </div>
              }
            />

            {/* Tarjeta flotante destacada sobre la composición (POS 3,5% → Kuota 1%) */}
            <FinancialCardMotion
              maxTiltDeg={2.2}
              className="mt-6 w-full max-w-md z-20"
            >
              <div className="rounded-2xl bg-[#0A192F] text-white border border-white/10 px-5 py-4 shadow-xl flex flex-wrap items-center justify-between gap-3">
                <div>
                  <span className="block text-[10px] uppercase font-bold tracking-wider text-slate-400">
                    Comparativa de comisión
                  </span>
                  <span className="text-xs font-semibold text-slate-300">
                    Liquidación en {PRICING_CONFIG.settlementBusinessHours}h hábiles
                  </span>
                </div>
                <div className="flex items-center gap-2 text-base sm:text-lg font-extrabold">
                  <span className="line-through text-slate-400">
                    POS {posFeeText}
                  </span>
                  <span className="text-[#38BDF8]" aria-hidden="true">
                    →
                  </span>
                  <span className="px-3 py-1 rounded-full bg-[#0391FD] text-white shadow-sm">
                    Kuota {kuotaFeeText}
                  </span>
                </div>
              </div>
            </FinancialCardMotion>
          </ScrollReveal>
        </div>
      </div>
    </section>
  );
};

import React from 'react';
import { ShoppingBag, Store, ArrowRight, Sparkles, Calculator, CheckCircle2 } from 'lucide-react';
import { AudienceMode } from './UnifiedNavbar';
import { PRICING_CONFIG, formatPercentEs } from '../config/pricingConfig';
import { smoothScrollToAnchor } from '../utils/motionSystem';

interface EcosystemProps {
  mode: AudienceMode;
  onModeChange: (mode: AudienceMode, source: string) => void;
}

/**
 * Barra interactiva superior del Ecosistema Kuota 360°:
 * Permite identificar y alternar de forma atractiva entre el viaje de Compradores (B2C/MYPE)
 * y el viaje de Proveedores (B2B/Comercios).
 */
export const EcosystemRibbon: React.FC<EcosystemProps> = ({ mode, onModeChange }) => {
  const kuotaFee = formatPercentEs(PRICING_CONFIG.kuotaPaymentsFeePercent);
  const posFee = formatPercentEs(PRICING_CONFIG.posAverageFeePercent);

  return (
    <div className="w-full bg-white/75 border-b-2 border-kuota-deepnavy py-3 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-stretch md:items-center justify-between gap-2.5">
        <div className="flex items-center gap-2 text-xs sm:text-sm font-extrabold text-kuota-deepnavy">
          <span className="inline-flex items-center justify-center w-6 h-6 rounded-lg bg-kuota-dodger text-white shrink-0">
            <Sparkles className="w-3.5 h-3.5" aria-hidden="true" />
          </span>
          <span>Ecosistema Kuota: elige tu perfil para personalizar la experiencia</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 md:w-auto">
          {/* Tarjeta selector: Compradores */}
          <button
            type="button"
            onClick={() => onModeChange('compradores', 'ecosystem_ribbon')}
            className={`group text-left rounded-xl px-3.5 py-2 border-2 transition-all flex items-center justify-between gap-3 ${
              mode === 'compradores'
                ? 'bg-kuota-dodger text-white border-kuota-deepnavy shadow-navy-hard -translate-y-0.5'
                : 'bg-kuota-palesky/55 hover:bg-kuota-palesky text-kuota-deepnavy border-kuota-deepnavy/40 hover:border-kuota-deepnavy'
            }`}
          >
            <div className="flex items-center gap-2.5">
              <div
                className={`w-8 h-8 rounded-lg flex items-center justify-center border shrink-0 ${
                  mode === 'compradores'
                    ? 'bg-white text-kuota-deepnavy border-kuota-deepnavy'
                    : 'bg-white text-kuota-dodger border-kuota-deepnavy/30'
                }`}
              >
                <ShoppingBag className="w-4 h-4" aria-hidden="true" />
              </div>
              <div>
                <div className="text-xs font-extrabold leading-tight flex items-center gap-1.5">
                  <span>Para Compradores</span>
                  {mode === 'compradores' && (
                    <span className="text-[10px] px-1.5 py-0.2 rounded bg-kuota-deepnavy text-white">
                      Activo
                    </span>
                  )}
                </div>
                <p
                  className={`text-[11px] font-bold leading-tight mt-0.5 ${
                    mode === 'compradores' ? 'text-kuota-palesky' : 'text-kuota-crayola'
                  }`}
                >
                  Mercadería a kuotas · 5% interés · Sin Infocorp
                </p>
              </div>
            </div>
          </button>

          {/* Tarjeta selector: Proveedores */}
          <button
            type="button"
            onClick={() => onModeChange('proveedores', 'ecosystem_ribbon')}
            className={`group text-left rounded-xl px-3.5 py-2 border-2 transition-all flex items-center justify-between gap-3 ${
              mode === 'proveedores'
                ? 'bg-kuota-deepnavy text-white border-kuota-deepnavy shadow-navy-hard -translate-y-0.5'
                : 'bg-kuota-palesky/55 hover:bg-kuota-palesky text-kuota-deepnavy border-kuota-deepnavy/40 hover:border-kuota-deepnavy'
            }`}
          >
            <div className="flex items-center gap-2.5">
              <div
                className={`w-8 h-8 rounded-lg flex items-center justify-center border shrink-0 ${
                  mode === 'proveedores'
                    ? 'bg-kuota-dodger text-white border-white/40'
                    : 'bg-white text-kuota-deepnavy border-kuota-deepnavy/30'
                }`}
              >
                <Store className="w-4 h-4" aria-hidden="true" />
              </div>
              <div>
                <div className="text-xs font-extrabold leading-tight flex items-center gap-1.5">
                  <span>Para Proveedores</span>
                  {mode === 'proveedores' && (
                    <span className="text-[10px] px-1.5 py-0.2 rounded bg-kuota-dodger text-white">
                      Activo
                    </span>
                  )}
                </div>
                <p
                  className={`text-[11px] font-bold leading-tight mt-0.5 ${
                    mode === 'proveedores' ? 'text-kuota-palesky' : 'text-kuota-crayola'
                  }`}
                >
                  POS {posFee} → Kuota {kuotaFee} · 0 riesgo · 48h
                </p>
              </div>
            </div>
          </button>
        </div>
      </div>
    </div>
  );
};

/**
 * Sección puente entre ambos lados del ecosistema (Cross-Pollination Card).
 */
export const CrossAudienceBanner: React.FC<EcosystemProps> = ({ mode, onModeChange }) => {
  const kuotaFee = formatPercentEs(PRICING_CONFIG.kuotaPaymentsFeePercent);
  const posFee = formatPercentEs(PRICING_CONFIG.posAverageFeePercent);

  if (mode === 'compradores') {
    return (
      <section
        aria-label="Kuota para Proveedores"
        className="w-full py-10 sm:py-12 border-b-2 border-kuota-deepnavy bg-gradient-to-r from-white/70 via-kuota-palesky to-white/70"
      >
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="rounded-3xl bg-kuota-deepnavy text-white border-2 border-kuota-deepnavy p-6 sm:p-8 lg:p-10 shadow-navy-hard-lg flex flex-col lg:flex-row items-start lg:items-center justify-between gap-6">
            <div className="space-y-3 max-w-2xl">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-kuota-dodger text-white text-xs font-extrabold uppercase tracking-wider">
                <Store className="w-3.5 h-3.5" aria-hidden="true" />
                <span>¿También vendes al por mayor o tienes un comercio?</span>
              </div>
              <h2 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
                Pasa de pagar {posFee} en POS a solo {kuotaFee} con Kuota Proveedores
              </h2>
              <p className="text-sm sm:text-base font-medium text-kuota-palesky leading-relaxed">
                Cobra con QR o Link de pago sin alquiler de POS y ofrece financiamiento{' '}
                <strong className="text-white">aKuotas! con 0 riesgo de impago</strong> y liquidación en 48 horas hábiles.
              </p>
              <div className="flex flex-wrap gap-3 pt-1 text-xs font-bold text-kuota-palesky">
                <span className="inline-flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-kuota-dodger" />
                  Comisión {kuotaFee} por transacción
                </span>
                <span className="inline-flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-kuota-dodger" />
                  Calculadora de ahorro en vivo
                </span>
                <span className="inline-flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-kuota-dodger" />
                  0 riesgo de impago
                </span>
              </div>
            </div>

            <div className="flex flex-col sm:flex-row lg:flex-col gap-3 w-full lg:w-auto shrink-0">
              <button
                type="button"
                onClick={() => {
                  onModeChange('proveedores', 'cross_banner_primary');
                  smoothScrollToAnchor('#inicio');
                }}
                className="inline-flex items-center justify-center gap-2 px-6 py-4 rounded-xl bg-kuota-dodger hover:bg-kuota-crayola text-white font-extrabold text-base border-2 border-white shadow-dodger-glow transition-all hover:-translate-y-0.5"
              >
                <Store className="w-5 h-5" aria-hidden="true" />
                <span>Ver Kuota para Proveedores</span>
                <ArrowRight className="w-4 h-4" aria-hidden="true" />
              </button>
              <button
                type="button"
                onClick={() => {
                  onModeChange('proveedores', 'cross_banner_calculator');
                  window.setTimeout(() => {
                    smoothScrollToAnchor('#calculadora');
                  }, 210);
                }}
                className="inline-flex items-center justify-center gap-2 px-5 py-3 rounded-xl bg-white/10 hover:bg-white/20 text-white font-bold text-sm border border-kuota-palesky/40 transition-colors"
              >
                <Calculator className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                <span>Simular ahorro en comisiones</span>
              </button>
            </div>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section
      aria-label="Kuota para Compradores"
      className="w-full py-10 sm:py-12 border-b-2 border-kuota-deepnavy bg-gradient-to-r from-white/70 via-kuota-palesky to-white/70"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="rounded-3xl bg-white border-2 border-kuota-deepnavy p-6 sm:p-8 lg:p-10 shadow-navy-hard-lg flex flex-col lg:flex-row items-start lg:items-center justify-between gap-6">
          <div className="space-y-3 max-w-2xl">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-kuota-palesky border border-kuota-deepnavy text-kuota-deepnavy text-xs font-extrabold uppercase tracking-wider">
              <ShoppingBag className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
              <span>¿Buscas financiar mercadería para tu negocio?</span>
            </div>
            <h2 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-kuota-deepnavy">
              Compra mercadería en kuotas en 3 clics, sin garantía ni Infocorp
            </h2>
            <p className="text-sm sm:text-base font-medium text-kuota-crayola leading-relaxed">
              Conoce cómo tus clientes emprendedores acceden hasta a{' '}
              <strong className="text-kuota-deepnavy">S/ 30,000 de capital</strong> con 5% de interés y suben del Nivel 1 al Nivel 6 pagando a tiempo.
            </p>
          </div>

          <button
            type="button"
            onClick={() => {
              onModeChange('compradores', 'cross_banner_primary');
              smoothScrollToAnchor('#inicio');
            }}
            className="inline-flex items-center justify-center gap-2 px-6 py-4 rounded-xl bg-kuota-dodger hover:bg-kuota-crayola text-white font-extrabold text-base border-2 border-kuota-deepnavy shadow-navy-hard transition-all hover:-translate-y-0.5 shrink-0"
          >
            <ShoppingBag className="w-5 h-5" aria-hidden="true" />
            <span>Ver Kuota para Compradores</span>
            <ArrowRight className="w-4 h-4" aria-hidden="true" />
          </button>
        </div>
      </div>
    </section>
  );
};

/**
 * Pastilla flotante inferior derecha para cambiar de vista en cualquier momento del scroll.
 */
export const FloatingAudienceSwitch: React.FC<EcosystemProps> = ({ mode, onModeChange }) => {
  return (
    <div className="fixed bottom-4 right-4 z-40 hidden sm:flex items-center">
      <div className="rounded-full bg-white/95 backdrop-blur-md border-2 border-kuota-deepnavy p-1 shadow-navy-hard flex items-center gap-1">
        <button
          type="button"
          onClick={() => onModeChange('compradores', 'floating_switch')}
          className={`inline-flex items-center gap-1.5 px-3.5 py-2 rounded-full text-xs font-extrabold transition-all ${
            mode === 'compradores'
              ? 'bg-kuota-dodger text-white'
              : 'text-kuota-deepnavy hover:bg-kuota-palesky/60'
          }`}
        >
          <ShoppingBag className="w-3.5 h-3.5" aria-hidden="true" />
          <span>Compradores</span>
        </button>
        <button
          type="button"
          onClick={() => onModeChange('proveedores', 'floating_switch')}
          className={`inline-flex items-center gap-1.5 px-3.5 py-2 rounded-full text-xs font-extrabold transition-all ${
            mode === 'proveedores'
              ? 'bg-kuota-deepnavy text-white'
              : 'text-kuota-deepnavy hover:bg-kuota-palesky/60'
          }`}
        >
          <Store className="w-3.5 h-3.5" aria-hidden="true" />
          <span>Proveedores</span>
        </button>
      </div>
    </div>
  );
};

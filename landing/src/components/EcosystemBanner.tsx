import React from 'react';
import { ShoppingBag, Store, ArrowRight, Sparkles, Calculator, CheckCircle2, Layers } from 'lucide-react';
import { AudienceMode } from './UnifiedNavbar';
import { PRICING_CONFIG, formatPercentEs } from '../config/pricingConfig';
import { smoothScrollToAnchor } from '../utils/motionSystem';

interface EcosystemProps {
  mode: AudienceMode;
  onModeChange: (mode: AudienceMode, source: string) => void;
}

/**
 * Barra interactiva superior del Ecosistema Kuota 360°
 */
export const EcosystemRibbon: React.FC<EcosystemProps> = ({ mode, onModeChange }) => {
  const kuotaFee = formatPercentEs(PRICING_CONFIG.kuotaPaymentsFeePercent);
  const posFee = formatPercentEs(PRICING_CONFIG.posAverageFeePercent);

  return (
    <div className="w-full bg-white/90 backdrop-blur-md border-b border-slate-200/80 py-3 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-stretch md:items-center justify-between gap-2.5">
        <div className="flex items-center gap-2 text-xs sm:text-sm font-bold text-[#0A192F]">
          <span className="inline-flex items-center justify-center w-6 h-6 rounded-lg bg-[#0391FD] text-white shrink-0">
            <Sparkles className="w-3.5 h-3.5" aria-hidden="true" />
          </span>
          <span>Ecosistema Kuota: elige tu perfil para personalizar la experiencia</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 md:w-auto">
          {/* Tarjeta selector: Compradores */}
          <button
            type="button"
            onClick={() => onModeChange('compradores', 'ecosystem_ribbon')}
            className={`group text-left rounded-2xl px-3.5 py-2 border transition-all flex items-center justify-between gap-3 cursor-pointer ${
              mode === 'compradores'
                ? 'bg-[#0391FD] text-white border-[#0391FD] shadow-md -translate-y-0.5'
                : 'bg-slate-50 hover:bg-slate-100 text-slate-700 border-slate-200/80'
            }`}
          >
            <div className="flex items-center gap-2.5">
              <div
                className={`w-8 h-8 rounded-xl flex items-center justify-center border shrink-0 ${
                  mode === 'compradores'
                    ? 'bg-white text-[#0391FD] border-white/40'
                    : 'bg-white text-slate-700 border-slate-200'
                }`}
              >
                <ShoppingBag className="w-4 h-4" aria-hidden="true" />
              </div>
              <div>
                <div className="text-xs font-bold leading-tight flex items-center gap-1.5">
                  <span>Para Compradores</span>
                  {mode === 'compradores' && (
                    <span className="text-[10px] px-1.5 py-0.2 rounded-full bg-[#0A192F] text-white">
                      Activo
                    </span>
                  )}
                </div>
                <p
                  className={`text-[11px] font-medium leading-tight mt-0.5 ${
                    mode === 'compradores' ? 'text-sky-100' : 'text-slate-500'
                  }`}
                >
                  Mercadería a kuotas · ~5% interés · Sin Infocorp
                </p>
              </div>
            </div>
          </button>

          {/* Tarjeta selector: Proveedores */}
          <button
            type="button"
            onClick={() => onModeChange('proveedores', 'ecosystem_ribbon')}
            className={`group text-left rounded-2xl px-3.5 py-2 border transition-all flex items-center justify-between gap-3 cursor-pointer ${
              mode === 'proveedores'
                ? 'bg-[#0A192F] text-white border-[#0A192F] shadow-md -translate-y-0.5'
                : 'bg-slate-50 hover:bg-slate-100 text-slate-700 border-slate-200/80'
            }`}
          >
            <div className="flex items-center gap-2.5">
              <div
                className={`w-8 h-8 rounded-xl flex items-center justify-center border shrink-0 ${
                  mode === 'proveedores'
                    ? 'bg-[#10B981] text-white border-white/40'
                    : 'bg-white text-slate-700 border-slate-200'
                }`}
              >
                <Store className="w-4 h-4" aria-hidden="true" />
              </div>
              <div>
                <div className="text-xs font-bold leading-tight flex items-center gap-1.5">
                  <span>Para Proveedores</span>
                  {mode === 'proveedores' && (
                    <span className="text-[10px] px-1.5 py-0.2 rounded-full bg-[#10B981] text-white">
                      Activo
                    </span>
                  )}
                </div>
                <p
                  className={`text-[11px] font-medium leading-tight mt-0.5 ${
                    mode === 'proveedores' ? 'text-slate-300' : 'text-slate-500'
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

  const isMerchant = mode === 'proveedores' || mode === 'comercios';

  if (!isMerchant) {
    return (
      <section
        aria-label="Kuota para Proveedores"
        className="w-full py-12 sm:py-16 border-b border-slate-200/80 bg-slate-50/60"
      >
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="rounded-[32px] bg-[#0A192F] text-white border border-white/10 p-6 sm:p-8 lg:p-10 shadow-2xl flex flex-col lg:flex-row items-start lg:items-center justify-between gap-8 relative overflow-hidden">
            {/* Ambient glows */}
            <div className="absolute -top-24 -right-24 w-80 h-80 rounded-full bg-[#0391FD]/10 blur-3xl pointer-events-none" />
            <div className="absolute -bottom-24 -left-24 w-80 h-80 rounded-full bg-[#10B981]/10 blur-3xl pointer-events-none" />

            <div className="space-y-4 max-w-2xl relative z-10">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#10B981]/15 border border-[#10B981]/30 text-[#10B981] text-xs font-bold uppercase tracking-wider">
                <Store className="w-3.5 h-3.5" aria-hidden="true" />
                <span>¿También vendes al por mayor o tienes un comercio?</span>
              </div>
              <h2 className="text-2xl sm:text-3xl font-black tracking-tight text-white leading-tight">
                Pasa de pagar {posFee} en POS a solo {kuotaFee} con Kuota Proveedores
              </h2>
              <p className="text-sm sm:text-base font-normal text-slate-300 leading-relaxed">
                Cobra con QR o Link de pago sin alquiler de POS y ofrece financiamiento{' '}
                <strong className="text-white font-semibold">aKuotas! con 0 riesgo de impago</strong> y liquidación en 48 horas hábiles.
              </p>
              <div className="flex flex-wrap gap-4 pt-1 text-xs font-medium text-slate-300">
                <span className="inline-flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-[#10B981]" />
                  Comisión {kuotaFee} por transacción
                </span>
                <span className="inline-flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-[#10B981]" />
                  Calculadora de ahorro en vivo
                </span>
                <span className="inline-flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-[#10B981]" />
                  0 riesgo de impago
                </span>
              </div>
            </div>

            <div className="flex flex-col sm:flex-row lg:flex-col gap-3 w-full lg:w-auto shrink-0 relative z-10">
              <button
                type="button"
                onClick={() => {
                  onModeChange('proveedores', 'cross_banner_primary');
                  smoothScrollToAnchor('#inicio');
                }}
                className="inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-full bg-[#0391FD] hover:bg-[#0277d4] text-white font-bold text-sm shadow-md hover:shadow-lg transition-all active:scale-95 cursor-pointer"
              >
                <Store className="w-4 h-4" aria-hidden="true" />
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
                className="inline-flex items-center justify-center gap-2 px-5 py-3 rounded-full bg-white/10 hover:bg-white/15 text-white font-bold text-xs sm:text-sm border border-white/15 transition-all cursor-pointer"
              >
                <Calculator className="w-4 h-4 text-[#0391FD]" aria-hidden="true" />
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
      className="w-full py-12 sm:py-16 border-b border-slate-200/80 bg-slate-50/60"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="rounded-[32px] bg-white border border-slate-200/80 p-6 sm:p-8 lg:p-10 shadow-xl flex flex-col lg:flex-row items-start lg:items-center justify-between gap-8 relative overflow-hidden">
          <div className="space-y-4 max-w-2xl">
            <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full bg-sky-50 border border-sky-200/80 text-[#0391FD] text-xs font-bold uppercase tracking-wider">
              <ShoppingBag className="w-3.5 h-3.5 text-[#0391FD]" aria-hidden="true" />
              <span>¿Buscas financiar mercadería para tu negocio?</span>
            </div>
            <h2 className="text-2xl sm:text-3xl font-black tracking-tight text-[#0A192F] leading-tight">
              Compra mercadería en kuotas en 3 clics, sin garantía ni Infocorp
            </h2>
            <p className="text-sm sm:text-base font-normal text-slate-600 leading-relaxed">
              Conoce cómo tus clientes emprendedores acceden hasta a{' '}
              <strong className="text-[#0A192F] font-bold">S/ 30,000 de capital</strong> con ~5% de interés y suben del Nivel 1 al Nivel 6 pagando a tiempo.
            </p>
          </div>

          <button
            type="button"
            onClick={() => {
              onModeChange('compradores', 'cross_banner_primary');
              smoothScrollToAnchor('#inicio');
            }}
            className="inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-full bg-[#0A192F] hover:bg-[#0391FD] text-white font-bold text-sm shadow-md hover:shadow-lg transition-all active:scale-95 shrink-0 cursor-pointer"
          >
            <ShoppingBag className="w-4 h-4" aria-hidden="true" />
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
  const isEcosystem = mode === 'ecosistema';
  const isBuyer = mode === 'compradores';
  const isMerchant = mode === 'comercios' || mode === 'proveedores';

  return (
    <div className="fixed bottom-4 right-4 z-40 hidden sm:flex items-center">
      <div className="rounded-full bg-white/95 backdrop-blur-md border border-slate-200/90 p-1 shadow-[0_8px_30px_rgba(0,0,0,0.12)] flex items-center gap-1">
        <button
          type="button"
          onClick={() => onModeChange('ecosistema', 'floating_switch')}
          className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold transition-all select-none cursor-pointer ${
            isEcosystem
              ? 'bg-[#070A0F] text-white shadow-sm'
              : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
          }`}
        >
          <Layers className={`w-3.5 h-3.5 ${isEcosystem ? 'text-[#10B981]' : 'text-slate-500'}`} aria-hidden="true" />
          <span>Ecosistema</span>
        </button>

        <button
          type="button"
          onClick={() => onModeChange('compradores', 'floating_switch')}
          className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold transition-all select-none cursor-pointer ${
            isBuyer
              ? 'bg-[#0391FD] text-white shadow-sm'
              : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
          }`}
        >
          <ShoppingBag className={`w-3.5 h-3.5 ${isBuyer ? 'text-white' : 'text-slate-500'}`} aria-hidden="true" />
          <span>Compradores</span>
        </button>

        <button
          type="button"
          onClick={() => onModeChange('comercios', 'floating_switch')}
          className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold transition-all select-none cursor-pointer ${
            isMerchant
              ? 'bg-[#059669] text-white shadow-sm'
              : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
          }`}
        >
          <Store className={`w-3.5 h-3.5 ${isMerchant ? 'text-white' : 'text-slate-500'}`} aria-hidden="true" />
          <span>Comercios</span>
        </button>
      </div>
    </div>
  );
};

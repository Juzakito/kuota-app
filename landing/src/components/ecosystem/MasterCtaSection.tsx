import React from 'react';
import { motion } from 'framer-motion';
import { ShoppingBag, Store, Handshake, ArrowRight, Sparkles, CheckCircle2 } from 'lucide-react';
import { ScrollReveal } from '../../utils/motionSystem';
import { trackEvent } from '../../utils/analytics';
import { kuotaSound } from '../../utils/soundSystem';

interface MasterCtaSectionProps {
  onSelectBuyer: () => void;
  onOpenMerchantAffiliation: () => void;
  onOpenPartnerModal: () => void;
}

export const MasterCtaSection: React.FC<MasterCtaSectionProps> = ({
  onSelectBuyer,
  onOpenMerchantAffiliation,
  onOpenPartnerModal,
}) => {
  return (
    <section
      id="cta-final"
      aria-labelledby="master-cta-heading"
      className="py-16 sm:py-24 bg-gradient-to-b from-white to-kuota-palesky/70 border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <ScrollReveal delayMs={0} direction="up">
          <div className="rounded-[32px] bg-[#0A192F] text-white p-8 sm:p-12 lg:p-16 border border-slate-800 shadow-2xl relative overflow-hidden text-center space-y-8">
            {/* Resplandor ambiental de fondo */}
            <div className="absolute top-0 right-1/4 w-80 h-80 bg-[#0391FD]/15 rounded-full blur-[100px] pointer-events-none" />
            <div className="absolute bottom-0 left-1/4 w-80 h-80 bg-[#10B981]/15 rounded-full blur-[100px] pointer-events-none" />

            <div className="relative z-10 max-w-3xl mx-auto space-y-4">
              <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full bg-white/10 border border-white/15 text-xs font-bold uppercase tracking-wider text-[#38BDF8]">
                <Sparkles className="w-3.5 h-3.5 text-[#38BDF8]" />
                Ecosistema Abierto
              </span>

              <h2
                id="master-cta-heading"
                className="text-3xl sm:text-4xl lg:text-5xl font-black text-white tracking-tight leading-tight"
              >
                El próximo paso de tu experiencia comercial comienza con KUOTA.
              </h2>

              <p className="text-base sm:text-lg font-normal text-slate-300 max-w-2xl mx-auto leading-relaxed">
                Tanto si buscas abastecerte en cuotas, multiplicar las ventas de tu negocio o sumar capacidades como aliado estratégico.
              </p>
            </div>

            {/* 3 Rutas de Conversión con Animaciones */}
            <div className="relative z-10 grid grid-cols-1 sm:grid-cols-3 gap-4 max-w-4xl mx-auto pt-2">
              <motion.button
                type="button"
                whileHover={{ y: -6, scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                onClick={() => {
                  kuotaSound.playCtaClick();
                  trackEvent('master_cta_buyer', { source: 'master_cta' });
                  onSelectBuyer();
                }}
                className="p-6 rounded-2xl bg-[#0391FD] hover:bg-[#0277D4] text-white font-bold text-sm sm:text-base border border-white/20 shadow-lg flex flex-col items-center justify-center gap-2.5 transition-colors cursor-pointer"
              >
                <div className="w-12 h-12 rounded-xl bg-white/10 flex items-center justify-center">
                  <ShoppingBag className="w-6 h-6 text-white" />
                </div>
                <span>Quiero comprar</span>
                <span className="text-xs font-normal text-sky-100">Para personas & microempresarios</span>
              </motion.button>

              <motion.button
                type="button"
                whileHover={{ y: -6, scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                onClick={() => {
                  kuotaSound.playCtaClick();
                  trackEvent('master_cta_merchant', { source: 'master_cta' });
                  onOpenMerchantAffiliation();
                }}
                className="p-6 rounded-2xl bg-white hover:bg-slate-50 text-[#0A192F] font-bold text-sm sm:text-base border border-slate-200 shadow-lg flex flex-col items-center justify-center gap-2.5 transition-colors cursor-pointer"
              >
                <div className="w-12 h-12 rounded-xl bg-slate-100 flex items-center justify-center">
                  <Store className="w-6 h-6 text-[#10B981]" />
                </div>
                <span>Quiero afiliar mi negocio</span>
                <span className="text-xs font-normal text-slate-500">Para bodegas & comercios</span>
              </motion.button>

              <motion.button
                type="button"
                whileHover={{ y: -6, scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                onClick={() => {
                  kuotaSound.playSelectOption();
                  trackEvent('master_cta_partner', { source: 'master_cta' });
                  onOpenPartnerModal();
                }}
                className="p-6 rounded-2xl bg-white/10 hover:bg-white/15 text-white font-bold text-sm sm:text-base border border-white/15 shadow-sm flex flex-col items-center justify-center gap-2.5 transition-colors cursor-pointer"
              >
                <div className="w-12 h-12 rounded-xl bg-white/10 flex items-center justify-center">
                  <Handshake className="w-6 h-6 text-[#38BDF8]" />
                </div>
                <span>Quiero colaborar</span>
                <span className="text-xs font-normal text-slate-300">Alianzas e instituciones</span>
              </motion.button>
            </div>

            <div className="relative z-10 pt-4 flex flex-wrap items-center justify-center gap-6 text-xs font-medium text-slate-400">
              <span className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-[#10B981]" />
                Atención personalizada
              </span>
              <span className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-[#10B981]" />
                Piloto en Perú
              </span>
              <span className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-[#10B981]" />
                Lanzamiento oficial 10.11.2026
              </span>
            </div>

          </div>
        </ScrollReveal>
      </div>
    </section>
  );
};

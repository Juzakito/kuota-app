import React from 'react';
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
          <div className="rounded-3xl bg-kuota-deepnavy text-white p-8 sm:p-12 lg:p-16 border-2 border-kuota-deepnavy shadow-navy-hard-lg relative overflow-hidden text-center space-y-8">
            
            <div className="relative z-10 max-w-3xl mx-auto space-y-4">
              <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full bg-white/10 border border-white/20 text-xs font-extrabold uppercase tracking-wider text-kuota-palesky">
                <Sparkles className="w-3.5 h-3.5 text-kuota-dodger" />
                Ecosistema Abierto
              </span>

              <h2
                id="master-cta-heading"
                className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-white tracking-tight leading-tight"
              >
                El próximo paso de tu experiencia comercial comienza con KUOTA.
              </h2>

              <p className="text-base sm:text-lg font-semibold text-kuota-palesky max-w-2xl mx-auto">
                Tanto si buscas abastecerte en cuotas, multiplicar las ventas de tu negocio o sumar capacidades como aliado estratégico.
              </p>
            </div>

            {/* 3 Rutas de Conversión */}
            <div className="relative z-10 grid grid-cols-1 sm:grid-cols-3 gap-4 max-w-4xl mx-auto pt-2">
              <button
                type="button"
                onClick={() => {
                  kuotaSound.playCtaClick();
                  trackEvent('master_cta_buyer', { source: 'master_cta' });
                  onSelectBuyer();
                }}
                className="p-5 rounded-2xl bg-kuota-dodger hover:bg-kuota-crayola text-white font-extrabold text-sm sm:text-base border-2 border-white/30 shadow-navy-hard flex flex-col items-center justify-center gap-2 transition-transform hover:-translate-y-1 active:translate-y-0"
              >
                <ShoppingBag className="w-6 h-6" />
                <span>Quiero comprar</span>
                <span className="text-xs font-semibold text-white/80">Para personas & microempresarios</span>
              </button>

              <button
                type="button"
                onClick={() => {
                  kuotaSound.playCtaClick();
                  trackEvent('master_cta_merchant', { source: 'master_cta' });
                  onOpenMerchantAffiliation();
                }}
                className="p-5 rounded-2xl bg-white hover:bg-kuota-palesky text-kuota-deepnavy font-extrabold text-sm sm:text-base border-2 border-kuota-deepnavy shadow-navy-hard flex flex-col items-center justify-center gap-2 transition-transform hover:-translate-y-1 active:translate-y-0"
              >
                <Store className="w-6 h-6 text-kuota-dodger" />
                <span>Quiero afiliar mi negocio</span>
                <span className="text-xs font-semibold text-kuota-crayola">Para bodegas & comercios</span>
              </button>

              <button
                type="button"
                onClick={() => {
                  kuotaSound.playSelectOption();
                  trackEvent('master_cta_partner', { source: 'master_cta' });
                  onOpenPartnerModal();
                }}
                className="p-5 rounded-2xl bg-white/10 hover:bg-white/20 text-white font-extrabold text-sm sm:text-base border-2 border-white/30 shadow-sm flex flex-col items-center justify-center gap-2 transition-transform hover:-translate-y-1 active:translate-y-0"
              >
                <Handshake className="w-6 h-6 text-kuota-palesky" />
                <span>Quiero colaborar</span>
                <span className="text-xs font-semibold text-kuota-palesky/75">Alianzas e instituciones</span>
              </button>
            </div>

            <div className="relative z-10 pt-4 flex flex-wrap items-center justify-center gap-4 text-xs font-bold text-kuota-palesky/80">
              <span className="flex items-center gap-1.5">
                <CheckCircle2 className="w-4 h-4 text-kuota-dodger" />
                Atención personalizada
              </span>
              <span className="flex items-center gap-1.5">
                <CheckCircle2 className="w-4 h-4 text-kuota-dodger" />
                Piloto en Perú
              </span>
              <span className="flex items-center gap-1.5">
                <CheckCircle2 className="w-4 h-4 text-kuota-dodger" />
                Lanzamiento oficial 10.11.2026
              </span>
            </div>

          </div>
        </ScrollReveal>
      </div>
    </section>
  );
};

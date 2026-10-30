import React from 'react';
import { motion } from 'framer-motion';
import { ShoppingBag, Store, ArrowRight, ShieldCheck, Sparkles, Layers, QrCode, TrendingUp, CheckCircle2 } from 'lucide-react';
import { ScrollReveal, TiltCard, FinancialCardMotion, CircularImageReveal } from '../../utils/motionSystem';
import { trackEvent } from '../../utils/analytics';
import { kuotaSound } from '../../utils/soundSystem';

interface EcosystemHeroSectionProps {
  onSelectBuyer: () => void;
  onOpenMerchantAffiliation: () => void;
  onExploreEcosystem: () => void;
}

export const EcosystemHeroSection: React.FC<EcosystemHeroSectionProps> = ({
  onSelectBuyer,
  onOpenMerchantAffiliation,
  onExploreEcosystem,
}) => {
  return (
    <section
      id="hero-ecosistema"
      aria-labelledby="ecosystem-hero-title"
      className="relative pt-8 sm:pt-12 pb-16 sm:pb-24 overflow-hidden border-b-2 border-kuota-deepnavy bg-gradient-to-b from-kuota-palesky via-kuota-palesky/70 to-white/60"
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-8 items-center">
          
          {/* Columna Izquierda: Mensaje y Conversión Inmediata (< 5s) */}
          <div className="lg:col-span-7 space-y-6">
            <ScrollReveal delayMs={0} direction="up">
              <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white border-2 border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider shadow-sm">
                <Sparkles className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                <span>Ecosistema Fintech de Compras en Cuotas</span>
              </div>
            </ScrollReveal>

            <ScrollReveal delayMs={80} direction="up">
              <h1
                id="ecosystem-hero-title"
                className="text-3xl sm:text-5xl lg:text-6xl font-extrabold text-kuota-deepnavy tracking-tight leading-[1.08]"
              >
                Conectamos oportunidades para{' '}
                <span className="text-kuota-dodger underline decoration-kuota-deepnavy decoration-4 underline-offset-4">
                  comprar y vender
                </span>{' '}
                en cuotas.
              </h1>
            </ScrollReveal>

            <ScrollReveal delayMs={140} direction="up">
              <p className="text-base sm:text-xl font-semibold text-kuota-crayola max-w-2xl leading-relaxed">
                KUOTA conecta compradores y comercios a través de una experiencia digital que facilita nuevas oportunidades de compra, venta y gestión de pagos.
              </p>
            </ScrollReveal>

            {/* CTAs Principales con Micro-Interacciones */}
            <ScrollReveal delayMs={200} direction="up">
              <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3.5 pt-2">
                <motion.button
                  type="button"
                  whileHover={{ y: -2, scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  onClick={() => {
                    kuotaSound.playCtaClick();
                    trackEvent('hero_cta_buyer', { source: 'ecosystem_hero' });
                    onSelectBuyer();
                  }}
                  className="px-7 py-4 rounded-full bg-[#0391FD] hover:bg-[#0277D4] text-white font-bold text-base shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2.5 cursor-pointer"
                >
                  <ShoppingBag className="w-5 h-5" />
                  <span>Quiero comprar</span>
                  <ArrowRight className="w-4 h-4 ml-0.5" />
                </motion.button>

                <motion.button
                  type="button"
                  whileHover={{ y: -2, scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  onClick={() => {
                    kuotaSound.playCtaClick();
                    trackEvent('hero_cta_merchant', { source: 'ecosystem_hero' });
                    onOpenMerchantAffiliation();
                  }}
                  className="px-7 py-4 rounded-full bg-[#0A192F] hover:bg-[#0391FD] text-white font-bold text-base shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2.5 cursor-pointer"
                >
                  <Store className="w-5 h-5 text-[#38BDF8]" />
                  <span>Quiero afiliar mi negocio</span>
                </motion.button>
              </div>

              <div className="pt-3">
                <button
                  type="button"
                  onClick={() => {
                    kuotaSound.playSelectOption();
                    onExploreEcosystem();
                  }}
                  className="inline-flex items-center gap-2 text-xs sm:text-sm font-bold text-[#0A192F] hover:text-[#0391FD] transition-colors cursor-pointer"
                >
                  <Layers className="w-4 h-4 text-[#0391FD]" />
                  <span>Conoce nuestro ecosistema integral (Comprador ↔ Kuota ↔ Comercio) ↓</span>
                </button>
              </div>
            </ScrollReveal>

            {/* Badges de Confianza Rápida */}
            <ScrollReveal delayMs={260} direction="up">
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 pt-4 border-t border-kuota-deepnavy/15 text-xs font-bold text-kuota-deepnavy">
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-kuota-dodger shrink-0" />
                  <span>3 partes conectadas</span>
                </div>
                <div className="flex items-center gap-2">
                  <ShieldCheck className="w-4 h-4 text-kuota-dodger shrink-0" />
                  <span>Control de colocaciones</span>
                </div>
                <div className="flex items-center gap-2 col-span-2 sm:col-span-1">
                  <TrendingUp className="w-4 h-4 text-kuota-dodger shrink-0" />
                  <span>Recaudación trazable</span>
                </div>
              </div>
            </ScrollReveal>
          </div>

          {/* Columna Derecha: Composición Visual Editorial del Ecosistema */}
          <div className="lg:col-span-5 relative flex justify-center">
            <ScrollReveal delayMs={120} direction="right">
              <div className="relative w-full max-w-md">
                
                {/* Composición Fotográfica Circular (Comprador + Emprendedor Comercio) */}
                <div className="relative flex justify-center items-center">
                  <CircularImageReveal
                    src="/images/hero-negocio-kuota.jpg"
                    alt="Emprendedor y comerciante peruano abasteciendo su negocio"
                    sizeClassName="w-60 h-60 sm:w-72 sm:h-72"
                    topBadge={
                      <div className="px-3 py-1 rounded-full bg-white border-2 border-kuota-deepnavy text-[11px] font-extrabold text-kuota-deepnavy shadow-sm">
                        Comercio Participante ✓
                      </div>
                    }
                    bottomBadge={
                      <div className="px-3 py-1 rounded-full bg-kuota-dodger text-white text-[11px] font-extrabold border-2 border-kuota-deepnavy shadow-sm">
                        Venta en cuotas activa
                      </div>
                    }
                    className="z-10"
                  />
                  
                  {/* Tarjeta flotante superior: Operación en Cuotas con Levitation Motion */}
                  <motion.div
                    animate={{ y: [0, -8, 0] }}
                    transition={{ duration: 4.2, repeat: Infinity, ease: 'easeInOut' }}
                    className="absolute -top-4 -left-2 sm:-left-4 z-20 pointer-events-auto"
                  >
                    <div className="rounded-2xl bg-white border border-slate-200/90 p-4 space-y-1.5 shadow-xl">
                      <div className="flex items-center justify-between gap-3 text-xs font-bold text-[#0A192F]">
                        <span>Operación Comercial</span>
                        <span className="px-2 py-0.5 rounded-full bg-[#0391FD] text-white text-[10px] font-bold">
                          Cuotas 3x
                        </span>
                      </div>
                      <div className="text-xl font-black text-[#0A192F]">
                        S/ 1,200.00
                      </div>
                      <div className="text-[11px] font-medium text-slate-500">
                        Mercadería entregada en lote
                      </div>
                    </div>
                  </motion.div>

                  {/* Tarjeta flotante inferior: Estado de Recaudación con Levitation Motion */}
                  <motion.div
                    animate={{ y: [0, 8, 0] }}
                    transition={{ duration: 4.8, repeat: Infinity, ease: 'easeInOut' }}
                    className="absolute -bottom-6 -right-2 sm:-right-4 z-20 pointer-events-auto"
                  >
                    <div className="rounded-2xl bg-white border border-slate-200/90 p-4 shadow-xl space-y-2">
                      <div className="flex items-center justify-between gap-3 text-xs font-bold text-[#0A192F]">
                        <span className="flex items-center gap-1.5">
                          <QrCode className="w-4 h-4 text-[#0391FD]" />
                          Pago QR / Link
                        </span>
                        <span className="px-2 py-0.5 rounded-full bg-emerald-50 text-[#10B981] border border-emerald-200 text-[10px] font-bold">
                          Al día
                        </span>
                      </div>
                      <div className="text-[11px] font-medium text-slate-500">
                        Recaudación programada · Día 20 / 40 / 60
                      </div>
                    </div>
                  </motion.div>
                </div>

              </div>
            </ScrollReveal>
          </div>

        </div>
      </div>
    </section>
  );
};

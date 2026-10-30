import React from 'react';
import { motion } from 'framer-motion';
import { ShoppingBag, Store, Handshake, ArrowRight, CheckCircle2, ChevronRight, Sparkles } from 'lucide-react';
import { ScrollReveal } from '../../utils/motionSystem';
import { trackEvent } from '../../utils/analytics';
import { kuotaSound } from '../../utils/soundSystem';

interface ExperienceSelectorSectionProps {
  onSelectBuyer: () => void;
  onSelectMerchant: () => void;
  onOpenPartnerModal: () => void;
}

export const ExperienceSelectorSection: React.FC<ExperienceSelectorSectionProps> = ({
  onSelectBuyer,
  onSelectMerchant,
  onOpenPartnerModal,
}) => {
  return (
    <section
      id="selector"
      aria-labelledby="selector-heading"
      className="py-16 sm:py-24 bg-white border-b border-slate-200/80"
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <ScrollReveal delayMs={0} direction="up" className="text-center max-w-3xl mx-auto">
          <span className="inline-block px-3.5 py-1 rounded-full bg-sky-50 border border-sky-200/80 text-xs font-bold text-[#0391FD] uppercase tracking-wider mb-3">
            Audiencias y Participantes
          </span>
          <h2
            id="selector-heading"
            className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-[#0A192F] tracking-tight"
          >
            KUOTA conecta diferentes oportunidades
          </h2>
          <p className="mt-3 text-base sm:text-lg font-normal text-slate-600">
            Identifica tu rol en el ecosistema y accede a la experiencia diseñada para ti.
          </p>
        </ScrollReveal>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 lg:gap-8 items-stretch">
          
          {/* Tarjeta 1: Compradores */}
          <ScrollReveal delayMs={80} direction="up">
            <motion.div
              whileHover={{ y: -8 }}
              transition={{ duration: 0.25, ease: 'easeOut' }}
              className="h-full rounded-[28px] border border-slate-200/90 bg-white p-6 sm:p-8 flex flex-col justify-between shadow-lg hover:shadow-2xl transition-shadow relative overflow-hidden group"
            >
              <div className="absolute top-0 right-0 w-32 h-32 bg-[#0391FD]/5 rounded-full blur-2xl pointer-events-none" />

              <div>
                <div className="flex items-center justify-between mb-5">
                  <span className="w-12 h-12 rounded-2xl bg-sky-50 text-[#0391FD] border border-sky-200/80 flex items-center justify-center shadow-xs">
                    <ShoppingBag className="w-6 h-6" />
                  </span>
                  <span className="px-3 py-1 rounded-full bg-slate-100 border border-slate-200 text-[11px] font-bold text-slate-700 uppercase tracking-wider">
                    Para Personas
                  </span>
                </div>

                <h3 className="text-2xl font-black text-[#0A192F] mb-1.5">
                  Compradores
                </h3>
                <p className="text-sm font-bold text-[#0391FD] mb-3">
                  Encuentra nuevas formas de comprar.
                </p>
                <p className="text-xs sm:text-sm font-normal text-slate-600 leading-relaxed mb-6">
                  Accede a mercadería y productos en comercios participantes, organiza tus pagos en cuotas y consulta tus vencimientos sin costos ocultos.
                </p>

                <ul className="space-y-2.5 mb-6 text-xs sm:text-sm font-semibold text-slate-700">
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-[#0391FD] shrink-0" />
                    <span>Compra financiada en cuotas claras</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-[#0391FD] shrink-0" />
                    <span>Calendario y recordatorios de pago</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-[#0391FD] shrink-0" />
                    <span>Seguimiento de compromisos desde la app</span>
                  </li>
                </ul>
              </div>

              <button
                type="button"
                onClick={() => {
                  kuotaSound.playSelectOption();
                  trackEvent('profile_switch', { to: 'compradores', source: 'selector_card' });
                  onSelectBuyer();
                }}
                className="w-full py-3.5 px-5 rounded-full bg-[#0391FD] hover:bg-[#0277D4] text-white font-bold text-sm shadow-md hover:shadow-lg flex items-center justify-center gap-2 transition-all active:scale-95 cursor-pointer"
              >
                <span>Descubrir KUOTA</span>
                <ChevronRight className="w-4 h-4 transition-transform group-hover:translate-x-1" />
              </button>
            </motion.div>
          </ScrollReveal>

          {/* Tarjeta 2: Comercios */}
          <ScrollReveal delayMs={140} direction="up">
            <motion.div
              whileHover={{ y: -8 }}
              transition={{ duration: 0.25, ease: 'easeOut' }}
              className="h-full rounded-[28px] border border-[#059669]/40 bg-white p-6 sm:p-8 flex flex-col justify-between shadow-xl hover:shadow-2xl transition-shadow relative overflow-hidden group"
            >
              <span className="absolute top-4 right-4 px-3 py-0.5 rounded-full bg-emerald-50 border border-emerald-200 text-emerald-800 text-[10px] font-bold uppercase tracking-wider">
                Piloto Activo
              </span>
              <div className="absolute top-0 right-0 w-32 h-32 bg-[#10B981]/5 rounded-full blur-2xl pointer-events-none" />

              <div>
                <div className="flex items-center justify-between mb-5">
                  <span className="w-12 h-12 rounded-2xl bg-emerald-50 text-[#059669] border border-emerald-200/80 flex items-center justify-center shadow-xs">
                    <Store className="w-6 h-6" />
                  </span>
                  <span className="px-3 py-1 rounded-full bg-slate-100 border border-slate-200 text-[11px] font-bold text-slate-700 uppercase tracking-wider">
                    Para Negocios
                  </span>
                </div>

                <h3 className="text-2xl font-black text-[#0A192F] mb-1.5">
                  Comercios
                </h3>
                <p className="text-sm font-bold text-[#059669] mb-3">
                  Amplía las posibilidades de tu negocio.
                </p>
                <p className="text-xs sm:text-sm font-normal text-slate-600 leading-relaxed mb-6">
                  Ofrece ventas en cuotas en tu mostrador, dinamiza tus colocaciones comerciales y mantén el control organizado de tu recaudación.
                </p>

                <ul className="space-y-2.5 mb-6 text-xs sm:text-sm font-semibold text-slate-700">
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-[#059669] shrink-0" />
                    <span>Más oportunidades de venta y ticket</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-[#059669] shrink-0" />
                    <span>Gestión de colocaciones y despacho</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-[#059669] shrink-0" />
                    <span>Monitoreo de cobro y recaudación</span>
                  </li>
                </ul>
              </div>

              <button
                type="button"
                onClick={() => {
                  kuotaSound.playCtaClick();
                  trackEvent('profile_switch', { to: 'comercios', source: 'selector_card' });
                  onSelectMerchant();
                }}
                className="w-full py-3.5 px-5 rounded-full bg-[#0A192F] hover:bg-[#0391FD] text-white font-bold text-sm shadow-md hover:shadow-lg flex items-center justify-center gap-2 transition-all active:scale-95 cursor-pointer"
              >
                <span>Afiliar mi comercio</span>
                <ArrowRight className="w-4 h-4 text-white transition-transform group-hover:translate-x-1" />
              </button>
            </motion.div>
          </ScrollReveal>

          {/* Tarjeta 3: Aliados Estratégicos */}
          <ScrollReveal delayMs={200} direction="up">
            <motion.div
              whileHover={{ y: -8 }}
              transition={{ duration: 0.25, ease: 'easeOut' }}
              className="h-full rounded-[28px] border border-slate-200/90 bg-white p-6 sm:p-8 flex flex-col justify-between shadow-lg hover:shadow-2xl transition-shadow relative overflow-hidden group"
            >
              <div className="absolute top-0 right-0 w-32 h-32 bg-[#0A192F]/5 rounded-full blur-2xl pointer-events-none" />

              <div>
                <div className="flex items-center justify-between mb-5">
                  <span className="w-12 h-12 rounded-2xl bg-slate-100 text-[#0A192F] border border-slate-200/80 flex items-center justify-center shadow-xs">
                    <Handshake className="w-6 h-6 text-[#0391FD]" />
                  </span>
                  <span className="px-3 py-1 rounded-full bg-slate-100 border border-slate-200 text-[11px] font-bold text-slate-700 uppercase tracking-wider">
                    Institucional
                  </span>
                </div>

                <h3 className="text-2xl font-black text-[#0A192F] mb-1.5">
                  Aliados
                </h3>
                <p className="text-sm font-bold text-slate-600 mb-3">
                  Construyamos nuevas oportunidades.
                </p>
                <p className="text-xs sm:text-sm font-normal text-slate-600 leading-relaxed mb-6">
                  Colabora con KUOTA en originación, financiamiento, canales de distribución mayorista o integración de infraestructura tecnológica de pagos.
                </p>

                <ul className="space-y-2.5 mb-6 text-xs sm:text-sm font-semibold text-slate-700">
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-[#0391FD] shrink-0" />
                    <span>Ecosistema comercial conectado</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-[#0391FD] shrink-0" />
                    <span>Integración de capacidades técnicas</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-[#0391FD] shrink-0" />
                    <span>Desarrollo conjunto de soluciones</span>
                  </li>
                </ul>
              </div>

              <button
                type="button"
                onClick={() => {
                  kuotaSound.playSelectOption();
                  trackEvent('partner_contact_click', { source: 'selector_card' });
                  onOpenPartnerModal();
                }}
                className="w-full py-3.5 px-5 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold text-sm border border-slate-200/80 flex items-center justify-center gap-2 transition-all active:scale-95 cursor-pointer"
              >
                <span>Contactar a KUOTA</span>
                <ChevronRight className="w-4 h-4 transition-transform group-hover:translate-x-1" />
              </button>
            </motion.div>
          </ScrollReveal>

        </div>
      </div>
    </section>
  );
};

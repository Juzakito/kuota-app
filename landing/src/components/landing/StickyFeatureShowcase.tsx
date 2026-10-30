import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ArrowRight, Sparkles, CheckCircle2, Shield, Bell, ArrowUpRight } from 'lucide-react';
import { STICKY_FEATURES, StickyFeatureItem } from '../../config/fintechData';

interface StickyFeatureShowcaseProps {
  onOpenEvaluationModal: () => void;
}

export const StickyFeatureShowcase: React.FC<StickyFeatureShowcaseProps> = ({
  onOpenEvaluationModal,
}) => {
  const [activeStepIndex, setActiveStepIndex] = useState(0);
  const activeFeature = STICKY_FEATURES[activeStepIndex];

  return (
    <section id="plataforma" className="relative py-8">
      <div className="max-w-7xl mx-auto mx-3 sm:mx-6 lg:mx-auto rounded-[32px] md:rounded-[48px] bg-white border border-slate-200/80 p-6 sm:p-10 md:p-14 shadow-[0_8px_30px_rgb(0,0,0,0.03)]">
        {/* Header de Sección */}
        <div className="max-w-3xl mb-10">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-slate-100 border border-slate-200/60 text-xs font-semibold text-slate-800 tracking-tight mb-3">
            <span className="w-2 h-2 rounded-full bg-[#06B6D4]" />
            <span>Plataforma & Ecosistema de Operaciones</span>
          </div>
          <h2 className="text-2xl sm:text-3xl md:text-4xl lg:text-[42px] font-bold text-[#070A0F] tracking-tight leading-tight">
            Conoce qué comprar{' '}
            <span className="bg-clip-text text-transparent bg-gradient-to-r from-[#10B981] to-[#06B6D4]">
              antes de quebrar stock
            </span>
          </h2>
          <p className="text-sm sm:text-base text-slate-600 mt-2">
            La combinación definitiva entre software de inventario con algoritmos predictivos y financiamiento comercial directo a proveedores.
          </p>
        </div>

        {/* GRID DE 2 COLUMNAS */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-start">
          {/* COLUMNA IZQUIERDA: VISUALIZADOR OSCURO CON ARO CÓNICO */}
          <div className="lg:col-span-6 lg:sticky lg:top-28">
            <div className="rounded-[32px] bg-[#070A0F] border border-white/10 p-6 sm:p-8 relative overflow-hidden min-h-[480px] sm:min-h-[520px] flex flex-col justify-between shadow-2xl">
              {/* Aro Conico Giratorio en el fondo (Exacto al segundo 00:25 del video) */}
              <motion.div
                animate={{ rotate: 360 }}
                transition={{ duration: 18, repeat: Infinity, ease: 'linear' }}
                className="absolute inset-0 m-auto w-80 h-80 rounded-full opacity-20 pointer-events-none"
                style={{
                  background:
                    'conic-gradient(from 180deg at 50% 50%, #10B981 0deg, #06B6D4 180deg, transparent 360deg)',
                  filter: 'blur(30px)',
                }}
              />

              {/* Cabecera del visualizador */}
              <div className="relative z-10 flex items-center justify-between border-b border-white/10 pb-4">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded-full bg-[#10B981]" />
                  <span className="text-xs font-bold text-white tracking-wide">
                    {activeFeature.mockupState.screenTitle}
                  </span>
                </div>
                <span className="text-[10px] font-mono text-emerald-300 bg-emerald-500/10 border border-emerald-500/20 px-2 py-0.5 rounded-full">
                  {activeFeature.mockupState.badgeValue}
                </span>
              </div>

              {/* Contenido Dinámico de la Pantalla */}
              <div className="relative z-10 my-4 space-y-3">
                <div className="text-[11px] text-slate-400">
                  {activeFeature.mockupState.screenSubtitle}
                </div>

                <div className="text-xl sm:text-2xl font-bold text-white tracking-tight tabular-nums">
                  {activeFeature.mockupState.highlightMetric}
                </div>

                {/* Items de lista simulados */}
                <div className="space-y-2 pt-2">
                  {activeFeature.mockupState.items.map((item, i) => (
                    <motion.div
                      key={`${activeFeature.id}-${i}`}
                      initial={{ opacity: 0, x: -10 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: i * 0.08 }}
                      className="p-3 rounded-xl bg-white/5 border border-white/10 flex items-center justify-between"
                    >
                      <div>
                        <div className="text-xs font-semibold text-slate-200">{item.name}</div>
                        <div className="text-[10px] text-slate-400">{item.qty}</div>
                      </div>
                      {item.alert ? (
                        <span className="text-[10px] font-bold text-amber-400 bg-amber-500/10 px-2 py-0.5 rounded">
                          {item.alert}
                        </span>
                      ) : item.kuota ? (
                        <span className="text-[10px] font-bold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded">
                          {item.kuota}
                        </span>
                      ) : null}
                    </motion.div>
                  ))}
                </div>
              </div>

              {/* Floating Notification Pill (Glassmorphism Parallax) */}
              <AnimatePresence mode="wait">
                <motion.div
                  key={activeFeature.id}
                  initial={{ opacity: 0, y: 12, scale: 0.95 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: -10, scale: 0.95 }}
                  transition={{ duration: 0.3 }}
                  className="relative z-20 mt-2 p-3.5 rounded-2xl bg-[#121A26]/95 border border-white/15 backdrop-blur-xl shadow-xl flex items-center justify-between"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full bg-emerald-500/20 text-[#8FD694] flex items-center justify-center font-bold text-xs shrink-0">
                      ⚡
                    </div>
                    <div>
                      <div className="text-xs font-bold text-white">
                        {activeFeature.mockupState.floatingNotification}
                      </div>
                      <div className="text-[10px] text-slate-400">
                        {activeFeature.mockupState.floatingSub}
                      </div>
                    </div>
                  </div>
                  <span className="text-[10px] font-bold text-emerald-400 bg-emerald-500/10 px-2 py-1 rounded-full shrink-0">
                    Auto
                  </span>
                </motion.div>
              </AnimatePresence>
            </div>
          </div>

          {/* COLUMNA DERECHA: LOS 4 BLOQUES NARRATIVOS */}
          <div className="lg:col-span-6 space-y-4">
            {STICKY_FEATURES.map((feature, index) => {
              const isActive = index === activeStepIndex;
              return (
                <div
                  key={feature.id}
                  onClick={() => setActiveStepIndex(index)}
                  className={`p-6 sm:p-8 rounded-[28px] border transition-all cursor-pointer ${
                    isActive
                      ? 'bg-slate-50 border-emerald-500/40 shadow-sm ring-1 ring-emerald-500/20'
                      : 'bg-white border-slate-200/70 hover:border-slate-300 hover:bg-slate-50/50'
                  }`}
                >
                  <div className="flex items-center justify-between mb-3">
                    <span
                      className={`text-xs font-bold uppercase tracking-wider px-3 py-1 rounded-full ${
                        isActive
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-slate-100 text-slate-600'
                      }`}
                    >
                      {feature.pillBadge}
                    </span>
                    <span className="text-xs font-mono font-bold text-slate-400">
                      0{index + 1} / 04
                    </span>
                  </div>

                  <h3 className="text-lg sm:text-xl font-bold text-slate-900 tracking-tight leading-snug">
                    {feature.headline}
                  </h3>

                  <p className="text-xs sm:text-sm text-slate-600 mt-2 leading-relaxed">
                    {feature.description}
                  </p>

                  <div className="mt-4 pt-4 border-t border-slate-200/60 flex items-center justify-between">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        onOpenEvaluationModal();
                      }}
                      className={`inline-flex items-center text-xs font-bold transition-colors ${
                        isActive
                          ? 'text-emerald-700 hover:text-emerald-800'
                          : 'text-slate-700 hover:text-slate-900'
                      }`}
                    >
                      <span>{feature.ctaText}</span>
                      <ArrowRight className="w-3.5 h-3.5 ml-1.5" />
                    </button>
                    {isActive && (
                      <span className="text-[10px] text-emerald-600 font-semibold flex items-center gap-1">
                        <CheckCircle2 className="w-3 h-3" />
                        Vista Activa
                      </span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
};

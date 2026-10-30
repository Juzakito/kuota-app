'use client';

import React from 'react';
import { motion } from 'framer-motion';
import { Sparkles, ArrowRight, ShieldCheck, CheckCircle2, TrendingUp, Package, Bot, Zap } from 'lucide-react';

interface HeroBentoSplitProps {
  onOpenEvaluationModal: () => void;
  onExploreDemo: () => void;
}

export const HeroBentoSplit: React.FC<HeroBentoSplitProps> = ({
  onOpenEvaluationModal,
  onExploreDemo,
}) => {
  return (
    <section className="relative pt-24 sm:pt-28 pb-4">
      {/* MACRO-CONTAINER BENTO CURVO (LIGHT) */}
      <div className="max-w-7xl mx-auto mx-3 sm:mx-6 lg:mx-auto rounded-[32px] md:rounded-[48px] bg-white border border-slate-200/80 p-6 sm:p-10 md:p-14 shadow-[0_8px_30px_rgb(0,0,0,0.03)] relative overflow-hidden">
        {/* Glow de fondo cálido sutil */}
        <div className="absolute top-0 right-0 w-96 h-96 bg-[#8FD694]/10 rounded-full blur-3xl pointer-events-none" />

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-center relative z-10">
          {/* COLUMNA IZQUIERDA (7 COLS) */}
          <div className="lg:col-span-7 flex flex-col justify-center space-y-6">
            {/* Pill de categoría */}
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-slate-100 border border-slate-200/60 w-fit text-xs font-semibold text-slate-800 tracking-tight">
              <span className="w-2 h-2 rounded-full bg-[#10B981] animate-ping" />
              <span className="font-bold text-slate-900">Sistema Operativo de Abastecimiento B2B</span>
            </div>

            {/* Titular H1 de Impacto Directo */}
            <h1 className="text-3xl sm:text-4xl md:text-5xl lg:text-[54px] font-bold text-[#070A0F] tracking-tight leading-[1.08]">
              Tu Inventario con 94.5% de precisión y{' '}
              <span className="bg-clip-text text-transparent bg-gradient-to-r from-[#10B981] via-[#059669] to-[#047857]">
                pago en Kuotas
              </span>
            </h1>

            {/* 3 Líneas directas de valor (estilo exacto del video iO) */}
            <div className="space-y-3.5 pt-2">
              <div className="flex items-start gap-3">
                <div className="w-5 h-5 rounded-full bg-emerald-100 text-emerald-700 flex items-center justify-center shrink-0 mt-0.5">
                  <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                </div>
                <p className="text-sm sm:text-base text-slate-700 font-medium">
                  Anticipa quiebres de stock <span className="text-slate-900 font-bold">30 días antes</span> con el copiloto Nexo IA.
                </p>
              </div>

              <div className="flex items-start gap-3">
                <div className="w-5 h-5 rounded-full bg-emerald-100 text-emerald-700 flex items-center justify-center shrink-0 mt-0.5">
                  <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                </div>
                <p className="text-sm sm:text-base text-slate-700 font-medium">
                  Genera hasta <span className="text-slate-900 font-bold">S/ 48,600</span> en ahorro operativo y libera tu capital de trabajo.
                </p>
              </div>

              <div className="flex items-start gap-3">
                <div className="w-5 h-5 rounded-full bg-emerald-100 text-emerald-700 flex items-center justify-center shrink-0 mt-0.5">
                  <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                </div>
                <p className="text-sm sm:text-base text-slate-700 font-medium">
                  Conecta tu tienda o ERP en minutos.{' '}
                  <a
                    href="#transparencia"
                    className="text-slate-900 underline font-semibold hover:text-[#10B981] transition-colors"
                  >
                    Aplican T&C transparentes.
                  </a>
                </p>
              </div>
            </div>

            {/* CTAs Principales */}
            <div className="pt-4 flex flex-wrap items-center gap-4">
              <button
                onClick={onOpenEvaluationModal}
                className="inline-flex items-center justify-center px-8 py-4 rounded-full text-sm sm:text-base font-bold bg-[#0B0D10] text-white hover:bg-[#1E293B] hover:scale-[1.02] active:scale-[0.98] transition-all shadow-[0_10px_25px_rgba(0,0,0,0.12)] group"
              >
                <span>Empezar a Kuotear</span>
                <ArrowRight className="w-4 h-4 ml-2 group-hover:translate-x-1 transition-transform" />
              </button>

              <button
                onClick={onExploreDemo}
                className="inline-flex items-center justify-center px-6 py-4 rounded-full text-sm sm:text-base font-semibold text-slate-800 hover:text-slate-950 hover:bg-slate-100/80 transition-all border border-slate-200/80"
              >
                <span>Ver Demo en Vivo</span>
                <span className="ml-1 text-slate-400">→</span>
              </button>
            </div>

            {/* Micro Trust Badge */}
            <div className="pt-2 flex items-center gap-3 text-xs text-slate-500 font-medium">
              <div className="flex -space-x-1.5">
                <div className="w-6 h-6 rounded-full bg-emerald-500 text-white text-[10px] flex items-center justify-center font-bold">
                  S/
                </div>
                <div className="w-6 h-6 rounded-full bg-cyan-500 text-white text-[10px] flex items-center justify-center font-bold">
                  IA
                </div>
                <div className="w-6 h-6 rounded-full bg-indigo-600 text-white text-[10px] flex items-center justify-center font-bold">
                  V
                </div>
              </div>
              <span>Líneas pre-aprobadas con RUC · Respaldo institucional por VALT</span>
            </div>
          </div>

          {/* COLUMNA DERECHA (5 COLS) — ESCENA 3D / DARK MONOLITH */}
          <div className="lg:col-span-5">
            <div className="rounded-[32px] bg-[#0A0F14] border border-white/10 p-6 sm:p-8 relative overflow-hidden min-h-[460px] flex flex-col justify-between shadow-2xl">
              {/* Luces y Auroras Radiales */}
              <div className="absolute -top-16 -right-16 w-64 h-64 bg-[#10B981]/25 rounded-full blur-[90px] pointer-events-none" />
              <div className="absolute -bottom-16 -left-16 w-64 h-64 bg-[#38BDF8]/20 rounded-full blur-[90px] pointer-events-none" />

              {/* Header de la escena interna */}
              <div className="flex items-center justify-between relative z-10">
                <div className="flex items-center gap-2">
                  <div className="w-2.5 h-2.5 rounded-full bg-[#10B981] animate-ping" />
                  <span className="text-xs font-semibold text-emerald-400 uppercase tracking-wider">
                    Copiloto Nexo IA Activo
                  </span>
                </div>
                <span className="text-[11px] font-mono text-slate-400 bg-white/5 border border-white/10 px-2.5 py-1 rounded-full">
                  v2.4 Live
                </span>
              </div>

              {/* Centro de la Escena: Orbe Nexo + Levitation */}
              <div className="my-auto py-8 relative flex flex-col items-center justify-center">
                {/* Halo orbital pulsante */}
                <motion.div
                  animate={{ rotate: 360 }}
                  transition={{ duration: 20, repeat: Infinity, ease: 'linear' }}
                  className="absolute w-56 h-56 rounded-full border border-dashed border-emerald-500/30"
                />

                {/* Orbe Central Nexo */}
                <motion.div
                  animate={{ y: [0, -10, 0] }}
                  transition={{ duration: 4, repeat: Infinity, ease: 'easeInOut' }}
                  className="relative z-10 w-28 h-28 sm:w-32 sm:h-32 rounded-full bg-gradient-to-tr from-[#0F766E] via-[#10B981] to-[#38BDF8] p-1 shadow-[0_0_50px_rgba(16,185,129,0.5)] flex items-center justify-center"
                >
                  <div className="w-full h-full rounded-full bg-[#070A0F]/90 backdrop-blur-md flex flex-col items-center justify-center text-center p-2">
                    <Sparkles className="w-8 h-8 text-[#8FD694] mb-1" />
                    <span className="text-[11px] font-bold text-white tracking-wider">NEXO IA</span>
                    <span className="text-[9px] text-emerald-300">94.5% Precisión</span>
                  </div>
                </motion.div>

                {/* Floating Card: Orden Aprobada */}
                <motion.div
                  initial={{ opacity: 0, y: 15 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.3 }}
                  className="absolute -bottom-4 right-0 sm:-right-2 z-20 bg-[#161F2E]/90 border border-emerald-500/30 rounded-2xl p-3 shadow-2xl backdrop-blur-md max-w-[220px]"
                >
                  <div className="flex items-center gap-2 mb-1">
                    <div className="w-5 h-5 rounded-full bg-[#10B981] text-[#070A0F] flex items-center justify-center font-bold text-[10px]">
                      ✓
                    </div>
                    <span className="text-[11px] font-semibold text-white">Orden Aprobada</span>
                  </div>
                  <div className="text-base font-bold text-white tabular-nums tracking-tight">
                    S/ 15,400.00
                  </div>
                  <div className="text-[10px] text-slate-300">
                    Despacho mayorista en camino
                  </div>
                </motion.div>

                {/* Floating Card: Alerta de Quiebre */}
                <motion.div
                  initial={{ opacity: 0, y: -15 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.5 }}
                  className="absolute -top-3 left-0 sm:-left-2 z-20 bg-[#161F2E]/90 border border-cyan-500/30 rounded-2xl p-3 shadow-2xl backdrop-blur-md max-w-[210px]"
                >
                  <div className="flex items-center gap-2 mb-0.5">
                    <span className="text-xs">⚡</span>
                    <span className="text-[11px] font-semibold text-cyan-300">Alerta 30 Días</span>
                  </div>
                  <div className="text-xs text-white font-medium">
                    Reposición de 48 unidades
                  </div>
                  <div className="text-[10px] text-emerald-400 font-semibold">
                    Evita pérdida de S/ 4,200
                  </div>
                </motion.div>
              </div>

              {/* Footer de la tarjeta con stats reales */}
              <div className="pt-4 border-t border-white/10 grid grid-cols-3 gap-2 text-center relative z-10">
                <div>
                  <div className="text-xs sm:text-sm font-bold text-white tabular-nums">0 Días</div>
                  <div className="text-[10px] text-slate-400">Sin Quiebres</div>
                </div>
                <div>
                  <div className="text-xs sm:text-sm font-bold text-[#8FD694] tabular-nums">30, 60, 90d</div>
                  <div className="text-[10px] text-slate-400">En Kuotas</div>
                </div>
                <div>
                  <div className="text-xs sm:text-sm font-bold text-cyan-400 tabular-nums">S/ 0.00</div>
                  <div className="text-[10px] text-slate-400">Membresía</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

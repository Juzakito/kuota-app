import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ArrowRight, Sparkles, CheckCircle2, ShieldCheck, Zap, Award, Layers } from 'lucide-react';
import { GAMIFICATION_LEVELS, GamificationLevelItem } from '../../config/fintechData';

interface LevelsGamification3DProps {
  onOpenEvaluationModal: () => void;
}

export const LevelsGamification3D: React.FC<LevelsGamification3DProps> = ({
  onOpenEvaluationModal,
}) => {
  const [selectedLevelId, setSelectedLevelId] = useState<string>('expansion');

  const selectedLevel =
    GAMIFICATION_LEVELS.find((l) => l.id === selectedLevelId) || GAMIFICATION_LEVELS[1];

  return (
    <section id="niveles" className="relative py-8">
      <div className="max-w-7xl mx-auto mx-3 sm:mx-6 lg:mx-auto rounded-[32px] md:rounded-[48px] bg-[#080C14] border border-white/10 p-6 sm:p-12 md:p-16 relative overflow-hidden shadow-2xl">
        {/* Luces de fondo y resplandor radial */}
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-gradient-to-tr from-[#10B981]/15 via-[#06B6D4]/10 to-transparent rounded-full blur-[120px] pointer-events-none" />

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-center relative z-10">
          {/* COLUMNA IZQUIERDA (5 COLS) */}
          <div className="lg:col-span-5 space-y-6">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white/5 border border-white/10 text-xs font-semibold text-emerald-400 tracking-wide">
              <Award className="w-3.5 h-3.5 text-[#8FD694]" />
              <span>Gamificación Financiera B2B</span>
            </div>

            <h2 className="text-2xl sm:text-3xl md:text-4xl lg:text-[44px] font-bold text-white tracking-tight leading-[1.1]">
              ¡Mientras más usas Kuota,{' '}
              <span className="bg-clip-text text-transparent bg-gradient-to-r from-[#8FD694] via-[#34D399] to-[#2DD4BF]">
                mayor es tu poder de compra!
              </span>
            </h2>

            <p className="text-sm sm:text-base text-slate-300 leading-relaxed">
              Cada nivel desbloquea mejores tasas de financiamiento, más días de gracia y acceso a compras conjuntas con descuentos mayoristas directos de fábrica.
            </p>

            <div className="pt-2 flex flex-wrap items-center gap-4">
              <button
                onClick={onOpenEvaluationModal}
                className="inline-flex items-center justify-center px-7 py-3.5 rounded-full text-xs sm:text-sm font-bold bg-white text-[#070A0F] hover:bg-[#8FD694] hover:text-[#070A0F] active:scale-95 transition-all shadow-lg group"
              >
                <span>Conoce los Niveles Kuota</span>
                <ArrowRight className="w-4 h-4 ml-2 group-hover:translate-x-1 transition-transform" />
              </button>

              <div className="text-xs text-slate-400 flex items-center gap-1.5">
                <ShieldCheck className="w-4 h-4 text-emerald-400" />
                <span>Sin avales personales ni trámites</span>
              </div>
            </div>
          </div>

          {/* COLUMNA DERECHA (7 COLS): PEDESTALES 3D CON INSIGNIAS FLOTANTES */}
          <div className="lg:col-span-7 flex flex-col items-center">
            {/* Contenedor de 3 Pedestales */}
            <div className="w-full grid grid-cols-3 gap-3 sm:gap-6 items-end justify-center min-h-[300px] sm:min-h-[340px] pt-8">
              {/* PEDESTAL 1: S/ IMPULSO */}
              <div
                onClick={() => setSelectedLevelId('impulso')}
                className="flex flex-col items-center cursor-pointer group"
              >
                {/* Insignia Flotante Plata */}
                <motion.div
                  animate={{ y: [0, -8, 0] }}
                  transition={{ duration: 3.2, repeat: Infinity, ease: 'easeInOut' }}
                  className={`w-16 h-16 sm:w-20 sm:h-20 rounded-2xl p-1 flex items-center justify-center mb-4 transition-transform group-hover:scale-105 ${
                    selectedLevelId === 'impulso'
                      ? 'bg-gradient-to-br from-slate-200 to-slate-400 shadow-[0_0_30px_rgba(255,255,255,0.3)] ring-2 ring-white/50'
                      : 'bg-white/10 border border-white/20'
                  }`}
                >
                  <div className="w-full h-full rounded-xl bg-[#0F141C] flex flex-col items-center justify-center text-center">
                    <span className="text-lg sm:text-2xl font-black text-slate-200">S/</span>
                    <span className="text-[9px] font-bold text-slate-400">15K</span>
                  </div>
                </motion.div>

                {/* Pedestal Base */}
                <div
                  className={`w-full h-24 sm:h-28 rounded-t-2xl border-t border-x transition-colors flex flex-col items-center justify-end p-2 ${
                    selectedLevelId === 'impulso'
                      ? 'bg-white/15 border-white/30'
                      : 'bg-white/5 border-white/10 group-hover:bg-white/10'
                  }`}
                >
                  <span className="text-[10px] sm:text-xs font-bold text-slate-300">Impulso</span>
                </div>
              </div>

              {/* PEDESTAL 2: 200K MAESTRO VALT (CENTRAL PROMINENTE) */}
              <div
                onClick={() => setSelectedLevelId('maestro')}
                className="flex flex-col items-center cursor-pointer group -translate-y-4"
              >
                {/* Badge flotante VIP */}
                <span className="text-[9px] font-extrabold uppercase px-2 py-0.5 rounded-full bg-emerald-500 text-black mb-2 shadow-md">
                  Corporativo
                </span>

                {/* Insignia Flotante Oro/Esmeralda */}
                <motion.div
                  animate={{ y: [0, -12, 0] }}
                  transition={{ duration: 3.8, repeat: Infinity, ease: 'easeInOut', delay: 0.2 }}
                  className={`w-20 h-20 sm:w-24 sm:h-24 rounded-2xl p-1 flex items-center justify-center mb-4 transition-transform group-hover:scale-110 ${
                    selectedLevelId === 'maestro'
                      ? 'bg-gradient-to-br from-[#10B981] via-[#06B6D4] to-[#3B82F6] shadow-[0_0_40px_rgba(16,185,129,0.5)] ring-2 ring-emerald-400'
                      : 'bg-gradient-to-br from-emerald-500/20 to-cyan-500/20 border border-emerald-500/40'
                  }`}
                >
                  <div className="w-full h-full rounded-xl bg-[#080C14] flex flex-col items-center justify-center text-center">
                    <span className="text-xl sm:text-2xl font-black text-transparent bg-clip-text bg-gradient-to-r from-emerald-300 to-cyan-300">
                      200K
                    </span>
                    <span className="text-[9px] font-bold text-emerald-400">VALT</span>
                  </div>
                </motion.div>

                {/* Pedestal Base Mayor */}
                <div
                  className={`w-full h-32 sm:h-36 rounded-t-2xl border-t border-x transition-colors flex flex-col items-center justify-end p-2 ${
                    selectedLevelId === 'maestro'
                      ? 'bg-gradient-to-t from-emerald-950/40 via-white/20 to-white/25 border-emerald-400/50'
                      : 'bg-white/5 border-white/10 group-hover:bg-white/10'
                  }`}
                >
                  <span className="text-[10px] sm:text-xs font-bold text-emerald-400">
                    Maestro Valt
                  </span>
                </div>
              </div>

              {/* PEDESTAL 3: 50K EXPANSIÓN */}
              <div
                onClick={() => setSelectedLevelId('expansion')}
                className="flex flex-col items-center cursor-pointer group"
              >
                {/* Insignia Flotante Turquesa */}
                <motion.div
                  animate={{ y: [0, -8, 0] }}
                  transition={{ duration: 3.4, repeat: Infinity, ease: 'easeInOut', delay: 0.4 }}
                  className={`w-16 h-16 sm:w-20 sm:h-20 rounded-2xl p-1 flex items-center justify-center mb-4 transition-transform group-hover:scale-105 ${
                    selectedLevelId === 'expansion'
                      ? 'bg-gradient-to-br from-cyan-400 to-blue-500 shadow-[0_0_30px_rgba(6,182,212,0.4)] ring-2 ring-cyan-400/60'
                      : 'bg-white/10 border border-white/20'
                  }`}
                >
                  <div className="w-full h-full rounded-xl bg-[#0F141C] flex flex-col items-center justify-center text-center">
                    <span className="text-lg sm:text-2xl font-black text-cyan-300">50K</span>
                    <span className="text-[9px] font-bold text-slate-400">Pymes</span>
                  </div>
                </motion.div>

                {/* Pedestal Base */}
                <div
                  className={`w-full h-24 sm:h-28 rounded-t-2xl border-t border-x transition-colors flex flex-col items-center justify-end p-2 ${
                    selectedLevelId === 'expansion'
                      ? 'bg-white/15 border-white/30'
                      : 'bg-white/5 border-white/10 group-hover:bg-white/10'
                  }`}
                >
                  <span className="text-[10px] sm:text-xs font-bold text-slate-300">Expansión</span>
                </div>
              </div>
            </div>

            {/* LÍNEA DE TIEMPO HORIZONTAL CONECTORA */}
            <div className="w-full max-w-md mx-auto my-6 px-4">
              <div className="relative flex items-center justify-between">
                {/* Línea de fondo */}
                <div className="absolute inset-x-0 h-1 bg-white/15 rounded-full" />
                {/* Línea iluminada activa */}
                <div
                  className="absolute left-0 h-1 bg-gradient-to-r from-[#10B981] to-[#06B6D4] rounded-full transition-all duration-300"
                  style={{
                    width:
                      selectedLevelId === 'impulso'
                        ? '15%'
                        : selectedLevelId === 'expansion'
                        ? '65%'
                        : '100%',
                  }}
                />

                {GAMIFICATION_LEVELS.map((lvl) => {
                  const isCur = lvl.id === selectedLevelId;
                  return (
                    <button
                      key={lvl.id}
                      onClick={() => setSelectedLevelId(lvl.id)}
                      className={`relative z-10 w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold transition-all ${
                        isCur
                          ? 'bg-[#10B981] text-black ring-4 ring-emerald-500/30 scale-110'
                          : 'bg-[#161F2E] text-slate-400 border border-white/20 hover:text-white'
                      }`}
                    >
                      {lvl.badgeSymbol}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* TARJETA DINÁMICA DE BENEFICIOS DEL NIVEL SELECCIONADO */}
            <AnimatePresence mode="wait">
              <motion.div
                key={selectedLevel.id}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ duration: 0.2 }}
                className="w-full p-6 rounded-2xl bg-white/5 border border-white/15 backdrop-blur-md"
              >
                <div className="flex items-center justify-between border-b border-white/10 pb-3 mb-4">
                  <div>
                    <h4 className="text-base font-bold text-white flex items-center gap-2">
                      {selectedLevel.name}
                      <span className="text-xs px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 font-mono">
                        {selectedLevel.maxAmount}
                      </span>
                    </h4>
                    <p className="text-xs text-slate-400 mt-0.5">{selectedLevel.requirements}</p>
                  </div>
                  <span className="text-xs font-mono font-bold text-cyan-300 bg-white/5 px-3 py-1 rounded-full border border-white/10">
                    {selectedLevel.terms}
                  </span>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
                  <div className="p-3 rounded-xl bg-black/40 border border-white/5 flex items-center gap-2.5">
                    <span className="text-emerald-400 font-bold text-base">%</span>
                    <div>
                      <div className="text-slate-400 text-[10px]">Tasa Mensual Fija</div>
                      <div className="text-white font-semibold">{selectedLevel.rate}</div>
                    </div>
                  </div>

                  <div className="p-3 rounded-xl bg-black/40 border border-white/5 flex items-center gap-2.5">
                    <Zap className="w-4 h-4 text-cyan-400" />
                    <div>
                      <div className="text-slate-400 text-[10px]">Beneficio B2B</div>
                      <div className="text-white font-semibold">{selectedLevel.cashback}</div>
                    </div>
                  </div>
                </div>
              </motion.div>
            </AnimatePresence>
          </div>
        </div>
      </div>
    </section>
  );
};

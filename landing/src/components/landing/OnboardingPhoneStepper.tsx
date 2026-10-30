'use client';

import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ArrowRight, CheckCircle2, Smartphone, ShieldCheck, Sparkles, Building2 } from 'lucide-react';
import { ONBOARDING_STEPS, OnboardingStepItem } from '../../config/fintechData';

interface OnboardingPhoneStepperProps {
  onOpenEvaluationModal: () => void;
}

export const OnboardingPhoneStepper: React.FC<OnboardingPhoneStepperProps> = ({
  onOpenEvaluationModal,
}) => {
  const [activeStepIndex, setActiveStepIndex] = useState(0);
  const currentStep = ONBOARDING_STEPS[activeStepIndex];

  return (
    <section id="como-empezar" className="relative py-12">
      <div className="max-w-7xl mx-auto mx-3 sm:mx-6 lg:mx-auto rounded-[32px] md:rounded-[48px] bg-white border border-slate-200/80 p-6 sm:p-10 md:p-16 shadow-[0_8px_30px_rgb(0,0,0,0.03)]">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-14 items-center">
          {/* COLUMNA IZQUIERDA (5 COLS): MOCKUP DE TELÉFONO SOBRE PEDESTAL */}
          <div className="lg:col-span-5 flex flex-col items-center justify-center relative">
            {/* Halo Turquesa Circular de Fondo */}
            <div className="absolute w-72 h-72 rounded-full bg-gradient-to-tr from-[#10B981]/20 to-[#06B6D4]/20 blur-3xl pointer-events-none" />

            {/* Teléfono Mockup */}
            <div className="relative z-10 w-[280px] sm:w-[310px] h-[520px] rounded-[44px] bg-[#070A0F] border-[8px] border-slate-900 shadow-2xl overflow-hidden p-4 flex flex-col justify-between">
              {/* Notch y barra superior */}
              <div className="flex items-center justify-between px-3 pt-1 text-[10px] text-slate-400 font-mono">
                <span>9:41</span>
                <div className="w-16 h-3 rounded-full bg-slate-800" />
                <span>5G 100%</span>
              </div>

              {/* Pantalla Dinámica Sincronizada */}
              <AnimatePresence mode="wait">
                <motion.div
                  key={currentStep.step}
                  initial={{ opacity: 0, y: 15 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -15 }}
                  transition={{ duration: 0.25 }}
                  className="my-auto space-y-4 py-2"
                >
                  <div className="text-center space-y-1">
                    <span className="text-[10px] font-mono text-emerald-400 uppercase tracking-widest font-semibold">
                      {currentStep.screenHighlight}
                    </span>
                    <h4 className="text-sm font-bold text-white px-2">
                      {currentStep.screenSub}
                    </h4>
                  </div>

                  {/* Tarjetas de Datos de la Pantalla */}
                  <div className="space-y-2.5">
                    {currentStep.phoneDetails.map((detail, idx) => (
                      <div
                        key={idx}
                        className="p-3 rounded-2xl bg-white/5 border border-white/10 flex flex-col"
                      >
                        <span className="text-[10px] text-slate-400 font-medium">
                          {detail.label}
                        </span>
                        <span className="text-xs font-bold text-white mt-0.5">
                          {detail.value}
                        </span>
                      </div>
                    ))}
                  </div>

                  {/* Estado Visual de la Operación */}
                  <div className="p-3 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 text-center">
                    <span className="text-[11px] font-bold text-emerald-300 flex items-center justify-center gap-1.5">
                      <CheckCircle2 className="w-3.5 h-3.5" />
                      Paso {currentStep.step} de 06 Completado
                    </span>
                  </div>
                </motion.div>
              </AnimatePresence>

              {/* Botón Inferior del Teléfono */}
              <button
                onClick={onOpenEvaluationModal}
                className="w-full py-2.5 rounded-2xl bg-white text-[#070A0F] font-bold text-xs hover:bg-[#8FD694] transition-colors"
              >
                Continuar Flujo
              </button>
            </div>
          </div>

          {/* COLUMNA DERECHA (7 COLS): STEPPER INTERACTIVO DE 6 PASOS */}
          <div className="lg:col-span-7 space-y-6">
            <div>
              <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-slate-100 border border-slate-200/60 text-xs font-semibold text-slate-800 tracking-tight mb-3">
                <Smartphone className="w-3.5 h-3.5 text-emerald-600" />
                <span>¿Cómo empezar a Kuotear?</span>
              </div>
              <h2 className="text-2xl sm:text-3xl md:text-4xl font-extrabold text-[#070A0F] tracking-tight leading-tight">
                Descubre cómo activar tu línea y Cerebro de Compras
              </h2>
              <p className="text-xs sm:text-sm text-slate-600 mt-2">
                Un proceso 100% digital diseñado para dueños de negocio que necesitan respuestas rápidas sin perder el tiempo en agencias bancarias.
              </p>
            </div>

            {/* LISTA DE 6 PASOS CLICABLES */}
            <div className="space-y-2.5 pt-2">
              {ONBOARDING_STEPS.map((stepItem, index) => {
                const isActive = index === activeStepIndex;
                return (
                  <div
                    key={stepItem.step}
                    onClick={() => setActiveStepIndex(index)}
                    className={`p-3.5 sm:p-4 rounded-2xl border transition-all cursor-pointer flex items-start gap-4 ${
                      isActive
                        ? 'bg-slate-50 border-emerald-500/50 shadow-sm ring-1 ring-emerald-500/20'
                        : 'bg-white border-slate-200/70 hover:border-slate-300 hover:bg-slate-50/50'
                    }`}
                  >
                    {/* Número Círculo */}
                    <div
                      className={`w-9 h-9 sm:w-10 sm:h-10 rounded-full flex items-center justify-center font-bold text-xs sm:text-sm shrink-0 transition-colors ${
                        isActive
                          ? 'bg-[#8FD694]/30 text-[#065F46] ring-2 ring-emerald-500/40'
                          : 'bg-slate-100 text-slate-400'
                      }`}
                    >
                      {stepItem.step}
                    </div>

                    {/* Texto del Paso */}
                    <div className="flex-1">
                      <h3
                        className={`text-xs sm:text-sm font-bold leading-snug transition-colors ${
                          isActive ? 'text-slate-950' : 'text-slate-700'
                        }`}
                      >
                        {stepItem.title}
                      </h3>
                      {isActive && (
                        <p className="text-xs text-slate-600 mt-1 leading-relaxed">
                          {stepItem.description}
                        </p>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>

            {/* CTA Final del Stepper */}
            <div className="pt-2">
              <button
                onClick={onOpenEvaluationModal}
                className="inline-flex items-center justify-center px-8 py-3.5 rounded-full text-xs sm:text-sm font-bold bg-[#0B0D10] text-white hover:bg-[#1E293B] hover:scale-[1.02] active:scale-[0.98] transition-all shadow-md group"
              >
                <span>Solicitar Evaluación Inmediata</span>
                <ArrowRight className="w-4 h-4 ml-2 group-hover:translate-x-1 transition-transform" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

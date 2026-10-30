import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Store,
  QrCode,
  PackageCheck,
  Calendar,
  CheckCircle2,
  TrendingUp,
  ArrowRight,
  Sparkles,
  ShoppingBag,
  Clock,
  Play,
  RotateCcw,
} from 'lucide-react';
import { ScrollReveal, TiltCard, AnimatedNumber, PaymentProgress } from '../../utils/motionSystem';
import { trackEvent } from '../../utils/analytics';
import { kuotaSound } from '../../utils/soundSystem';

interface MerchantStage {
  stage: number;
  label: string;
  headline: string;
  description: string;
  merchantAction: string;
  badge: string;
}

const MERCHANT_STAGES: MerchantStage[] = [
  {
    stage: 1,
    label: '1. Oportunidad',
    headline: 'El comercio recibe una oportunidad de compra con pago en cuotas',
    description: 'Un cliente en el mostrador o por catálogo solicita mercadería y elige pagar con la opción Kuota QR / Link.',
    merchantAction: 'Generar QR de cobro en app Kuota Comercio',
    badge: 'Oportunidad Detectada',
  },
  {
    stage: 2,
    label: '2. Registro',
    headline: 'Se registra y valida la operación en el sistema',
    description: 'El comprador escanea, confirma su cuota inicial y el sistema valida la disponibilidad en segundos.',
    merchantAction: 'Conformidad inmediata en pantalla',
    badge: 'Operación Registrada',
  },
  {
    stage: 3,
    label: '3. Colocación',
    headline: 'Se visualiza la colocación y se autoriza el despacho',
    description: 'El comercio recibe la liquidación inicial pactada y procede a entregar la mercadería con total tranquilidad.',
    merchantAction: 'Entrega de lote o mercadería al comprador',
    badge: 'Colocación Confirmada',
  },
  {
    stage: 4,
    label: '4. Calendario',
    headline: 'Se programa el calendario de cuotas del comprador',
    description: 'Las cuotas quedan fijadas (ej. Día 20, 40 y 60) con recordatorios automáticos gestionados por la plataforma.',
    merchantAction: 'Cronograma visible en panel de control',
    badge: 'Cuotas Programadas',
  },
  {
    stage: 5,
    label: '5. Recaudación',
    headline: 'Seguimiento de recaudación y cierre exitoso',
    description: 'El comercio consulta el estado de recaudación de cada cuota y la conciliación correspondiente sin gestiones manuales.',
    merchantAction: 'Recaudación conciliada al 100%',
    badge: 'Recaudación Completa',
  },
];

interface MerchantOperationDemoProps {
  onOpenAffiliation: () => void;
}

export const MerchantOperationDemo: React.FC<MerchantOperationDemoProps> = ({
  onOpenAffiliation,
}) => {
  const [currentStageNum, setCurrentStageNum] = useState<number>(3);

  const currentStage =
    MERCHANT_STAGES.find((s) => s.stage === currentStageNum) || MERCHANT_STAGES[2];

  const handleStageSelect = (stageNum: number) => {
    kuotaSound.playSelectOption();
    setCurrentStageNum(stageNum);
    trackEvent('merchant_operation_stage_select', { stage: stageNum });
  };

  return (
    <section
      id="operacion-comercio"
      aria-labelledby="merchant-op-heading"
      className="py-16 sm:py-24 bg-slate-50/70 border-b border-slate-200/80"
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <ScrollReveal delayMs={0} direction="up" className="max-w-3xl">
          <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full bg-white border border-slate-200/80 text-xs font-bold text-[#0A192F] uppercase tracking-wider mb-3 shadow-xs">
            Para Comercios y Emprendedores
          </span>
          <h2
            id="merchant-op-heading"
            className="text-3xl sm:text-4xl lg:text-5xl font-black text-[#0A192F] tracking-tight"
          >
            Así opera KUOTA en el día a día de tu negocio
          </h2>
          <p className="mt-3 text-base sm:text-lg font-normal text-slate-600">
            Desde la atención en mostrador hasta la conciliación final de cada cuota.
          </p>
        </ScrollReveal>

        {/* Panel Interactivo de las 5 Etapas del Comercio con Animaciones */}
        <ScrollReveal delayMs={80} direction="up">
          <div className="rounded-[28px] bg-white border border-slate-200/90 p-6 sm:p-8 lg:p-10 shadow-xl space-y-8 overflow-hidden relative">
            
            {/* Navegador de Etapas */}
            <div
              role="tablist"
              aria-label="5 etapas de la operación en comercios"
              className="grid grid-cols-2 sm:grid-cols-5 gap-2.5"
            >
              {MERCHANT_STAGES.map((item) => {
                const isSelected = item.stage === currentStageNum;
                const isPast = item.stage < currentStageNum;
                return (
                  <motion.button
                    key={item.stage}
                    type="button"
                    role="tab"
                    aria-selected={isSelected}
                    onClick={() => handleStageSelect(item.stage)}
                    whileHover={{ y: -3, scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    className={`p-3.5 rounded-2xl border text-left transition-all flex flex-col justify-between gap-1.5 cursor-pointer ${
                      isSelected
                        ? 'bg-[#0A192F] text-white border-[#0A192F] shadow-lg ring-2 ring-[#0391FD]/20'
                        : isPast
                        ? 'bg-emerald-50/80 text-emerald-900 border-emerald-200'
                        : 'bg-white hover:bg-slate-50 text-slate-700 border-slate-200/90 shadow-xs'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-[11px] font-bold uppercase tracking-wider">
                        Paso {item.stage}
                      </span>
                      {isPast && <CheckCircle2 className="w-3.5 h-3.5 text-[#10B981]" />}
                    </div>
                    <span className="text-xs font-bold leading-tight">
                      {item.label.split('. ')[1]}
                    </span>
                  </motion.button>
                );
              })}
            </div>

            {/* Barra de progreso de la operación */}
            <PaymentProgress
              label={`Flujo del comercio · Etapa ${currentStageNum} de 5`}
              currentStep={currentStageNum}
              totalSteps={MERCHANT_STAGES.length}
              statusText={currentStage.badge}
            />

            {/* Escaparate de la Etapa Seleccionada con Transición Fluida */}
            <AnimatePresence mode="wait">
              <motion.div
                key={currentStage.stage}
                initial={{ opacity: 0, x: 12 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -12 }}
                transition={{ duration: 0.22, ease: [0.16, 1, 0.3, 1] }}
                className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-stretch pt-2"
              >
                {/* Información y Acción */}
                <div className="lg:col-span-7 flex flex-col justify-between space-y-6">
                  <div className="space-y-3">
                    <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-50 border border-emerald-200 text-xs font-bold text-emerald-800 uppercase tracking-wider">
                      <Sparkles className="w-3.5 h-3.5 text-[#10B981]" />
                      <span>{currentStage.badge}</span>
                    </div>

                    <h3 className="text-2xl sm:text-3xl font-black text-[#0A192F] leading-tight tracking-tight">
                      {currentStage.headline}
                    </h3>

                    <p className="text-sm sm:text-base font-normal text-slate-600 leading-relaxed">
                      {currentStage.description}
                    </p>
                  </div>

                  <div className="rounded-2xl bg-slate-50 border border-slate-200/90 p-4 flex items-center justify-between gap-4 shadow-xs">
                    <div className="text-xs font-bold text-[#0A192F]">
                      <span className="text-slate-500 block text-[11px] uppercase tracking-wider mb-0.5">Acción del comercio:</span>
                      <span className="text-sm font-semibold">{currentStage.merchantAction}</span>
                    </div>
                    <span className="w-10 h-10 rounded-2xl bg-white border border-slate-200 flex items-center justify-center shrink-0 shadow-xs">
                      <CheckCircle2 className="w-5 h-5 text-[#10B981]" />
                    </span>
                  </div>

                  <div className="flex flex-wrap items-center gap-3 pt-2">
                    <button
                      type="button"
                      onClick={() => {
                        kuotaSound.playCtaClick();
                        onOpenAffiliation();
                      }}
                      className="px-6 py-3.5 rounded-full bg-[#0A192F] hover:bg-[#0391FD] text-white font-bold text-sm shadow-md hover:shadow-lg flex items-center gap-2 transition-all active:scale-95 cursor-pointer"
                    >
                      <Store className="w-4 h-4" />
                      <span>Quiero afiliar mi comercio</span>
                    </button>

                    <button
                      type="button"
                      onClick={() => handleStageSelect(currentStageNum < 5 ? currentStageNum + 1 : 1)}
                      className="px-5 py-3.5 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold text-sm border border-slate-200/80 shadow-xs flex items-center gap-2 transition-all cursor-pointer"
                    >
                      <span>{currentStageNum < 5 ? 'Siguiente paso →' : 'Reiniciar demo ↺'}</span>
                    </button>
                  </div>
                </div>

                {/* Mockup del Panel Comercial Kuota */}
                <div className="lg:col-span-5 flex justify-center">
                  <div className="w-full max-w-sm rounded-[24px] border border-slate-800 bg-[#0A192F] text-white p-5 shadow-2xl space-y-4 relative overflow-hidden">
                    <div className="absolute top-0 right-0 w-32 h-32 bg-[#0391FD]/10 rounded-full blur-2xl pointer-events-none" />

                    <div className="flex items-center justify-between border-b border-white/10 pb-3 relative z-10">
                      <div className="flex items-center gap-2">
                        <Store className="w-4 h-4 text-[#38BDF8]" />
                        <span className="text-xs font-bold tracking-wider">KUOTA COMERCIO</span>
                      </div>
                      <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 text-[10px] font-bold border border-emerald-400/30">
                        Panel en vivo
                      </span>
                    </div>

                    <div className="rounded-2xl bg-white/5 p-4 space-y-2 border border-white/10 relative z-10">
                      <div className="flex justify-between text-xs text-slate-300">
                        <span>Lote / Pedido #KTA-8821</span>
                        <span className="text-white font-bold">Bodega Central</span>
                      </div>
                      <div className="flex justify-between items-end">
                        <div>
                          <span className="text-[10px] text-slate-400 block">Total Operación</span>
                          <span className="text-xl font-black text-white">S/ 1,200.00</span>
                        </div>
                        <span className="px-2.5 py-1 rounded-full bg-emerald-500/20 text-emerald-300 text-xs font-bold border border-emerald-400/30">
                          {currentStage.badge}
                        </span>
                      </div>
                    </div>

                    <div className="space-y-2 text-xs font-semibold text-slate-300 relative z-10">
                      <div className="flex justify-between p-2.5 rounded-xl bg-white/5 border border-white/10">
                        <span>Inicial del comprador:</span>
                        <span className="text-white font-bold">S/ 360.00 (30%) ✓</span>
                      </div>
                      <div className="flex justify-between p-2.5 rounded-xl bg-white/5 border border-white/10">
                        <span>3 Kuotas programadas:</span>
                        <span className="text-white font-bold">S/ 300 c/u</span>
                      </div>
                      <div className="flex justify-between p-2.5 rounded-xl bg-white/5 border border-white/10">
                        <span>Próximo vencimiento:</span>
                        <span className="text-[#38BDF8] font-bold">Día 20 · Automático</span>
                      </div>
                    </div>

                    <p className="text-[10px] text-slate-400 text-center italic relative z-10">
                      Simulación de interfaz demostrativa para el comercio.
                    </p>
                  </div>
                </div>

              </motion.div>
            </AnimatePresence>
          </div>
        </ScrollReveal>
      </div>
    </section>
  );
};

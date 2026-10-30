import React, { useState } from 'react';
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
      className="py-16 sm:py-24 bg-kuota-palesky/40 border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <ScrollReveal delayMs={0} direction="up" className="max-w-3xl">
          <span className="inline-block px-3.5 py-1 rounded-full bg-white border-2 border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-3">
            Para Comercios y Emprendedores
          </span>
          <h2
            id="merchant-op-heading"
            className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-kuota-deepnavy tracking-tight"
          >
            Así opera KUOTA en el día a día de tu negocio
          </h2>
          <p className="mt-3 text-base sm:text-lg font-semibold text-kuota-crayola">
            Desde la atención en mostrador hasta la conciliación final de cada cuota.
          </p>
        </ScrollReveal>

        {/* Panel Interactivo de las 5 Etapas del Comercio */}
        <ScrollReveal delayMs={80} direction="up">
          <div className="rounded-3xl bg-white border-2 border-kuota-deepnavy p-6 sm:p-8 lg:p-10 shadow-navy-hard-lg space-y-8">
            
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
                  <button
                    key={item.stage}
                    type="button"
                    role="tab"
                    aria-selected={isSelected}
                    onClick={() => handleStageSelect(item.stage)}
                    className={`p-3 rounded-2xl border-2 text-left transition-all flex flex-col justify-between gap-1.5 ${
                      isSelected
                        ? 'bg-kuota-deepnavy text-white border-kuota-deepnavy shadow-navy-hard -translate-y-1'
                        : isPast
                        ? 'bg-kuota-palesky/70 text-kuota-deepnavy border-kuota-deepnavy'
                        : 'bg-white hover:bg-kuota-palesky/30 text-kuota-deepnavy border-kuota-deepnavy/25'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-[11px] font-extrabold uppercase">
                        Paso {item.stage}
                      </span>
                      {isPast && <CheckCircle2 className="w-3.5 h-3.5 text-kuota-dodger" />}
                    </div>
                    <span className="text-xs font-extrabold leading-tight">
                      {item.label.split('. ')[1]}
                    </span>
                  </button>
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

            {/* Escaparate de la Etapa Seleccionada */}
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-stretch pt-2">
              
              {/* Información y Acción */}
              <div className="lg:col-span-7 flex flex-col justify-between space-y-6">
                <div className="space-y-3">
                  <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-kuota-palesky border border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy uppercase">
                    <Sparkles className="w-3.5 h-3.5 text-kuota-dodger" />
                    <span>{currentStage.badge}</span>
                  </div>

                  <h3 className="text-2xl sm:text-3xl font-extrabold text-kuota-deepnavy leading-tight">
                    {currentStage.headline}
                  </h3>

                  <p className="text-sm sm:text-base font-semibold text-kuota-crayola leading-relaxed">
                    {currentStage.description}
                  </p>
                </div>

                <div className="rounded-2xl bg-kuota-palesky/40 border-2 border-kuota-deepnavy p-4 flex items-center justify-between gap-4">
                  <div className="text-xs font-extrabold text-kuota-deepnavy">
                    <span className="text-kuota-crayola block text-[11px] uppercase">Acción del comercio:</span>
                    <span className="text-sm">{currentStage.merchantAction}</span>
                  </div>
                  <span className="w-10 h-10 rounded-xl bg-white border border-kuota-deepnavy flex items-center justify-center shrink-0">
                    <CheckCircle2 className="w-5 h-5 text-kuota-dodger" />
                  </span>
                </div>

                <div className="flex flex-wrap items-center gap-3 pt-2">
                  <button
                    type="button"
                    onClick={() => {
                      kuotaSound.playCtaClick();
                      onOpenAffiliation();
                    }}
                    className="px-6 py-3.5 rounded-2xl bg-kuota-dodger hover:bg-kuota-crayola text-white font-extrabold text-sm border-2 border-kuota-deepnavy shadow-navy-hard flex items-center gap-2 transition-transform hover:-translate-y-0.5 active:translate-y-0"
                  >
                    <Store className="w-4 h-4" />
                    <span>Quiero afiliar mi comercio</span>
                  </button>

                  <button
                    type="button"
                    onClick={() => handleStageSelect(currentStageNum < 5 ? currentStageNum + 1 : 1)}
                    className="px-4 py-3.5 rounded-2xl bg-white hover:bg-kuota-palesky text-kuota-deepnavy font-extrabold text-sm border-2 border-kuota-deepnavy shadow-sm flex items-center gap-2"
                  >
                    <span>{currentStageNum < 5 ? 'Siguiente paso →' : 'Reiniciar demo ↺'}</span>
                  </button>
                </div>
              </div>

              {/* Mockup del Panel Comercial Kuota */}
              <div className="lg:col-span-5 flex justify-center">
                <TiltCard maxTiltDeg={2.5} className="w-full max-w-sm">
                  <div className="rounded-3xl border-2 border-kuota-deepnavy bg-kuota-deepnavy text-white p-5 shadow-navy-hard-lg space-y-4">
                    <div className="flex items-center justify-between border-b border-white/15 pb-3">
                      <div className="flex items-center gap-2">
                        <Store className="w-4 h-4 text-kuota-dodger" />
                        <span className="text-xs font-extrabold">KUOTA COMERCIO</span>
                      </div>
                      <span className="px-2 py-0.5 rounded bg-kuota-dodger text-[10px] font-extrabold text-white">
                        Panel en vivo
                      </span>
                    </div>

                    <div className="rounded-xl bg-white/10 p-3.5 space-y-2 border border-white/15">
                      <div className="flex justify-between text-xs text-kuota-palesky">
                        <span>Lote / Pedido #KTA-8821</span>
                        <span className="text-white font-extrabold">Bodega Central</span>
                      </div>
                      <div className="flex justify-between items-end">
                        <div>
                          <span className="text-[10px] text-kuota-palesky block">Total Operación</span>
                          <span className="text-xl font-extrabold text-white">S/ 1,200.00</span>
                        </div>
                        <span className="px-2 py-1 rounded-lg bg-emerald-500/20 text-emerald-300 text-xs font-extrabold border border-emerald-400/30">
                          {currentStage.badge}
                        </span>
                      </div>
                    </div>

                    <div className="space-y-2 text-xs font-bold text-kuota-palesky">
                      <div className="flex justify-between p-2 rounded-lg bg-white/5 border border-white/10">
                        <span>Inicial del comprador:</span>
                        <span className="text-white font-extrabold">S/ 360.00 (30%) ✓</span>
                      </div>
                      <div className="flex justify-between p-2 rounded-lg bg-white/5 border border-white/10">
                        <span>3 Kuotas programadas:</span>
                        <span className="text-white font-extrabold">S/ 300 c/u</span>
                      </div>
                      <div className="flex justify-between p-2 rounded-lg bg-white/5 border border-white/10">
                        <span>Próximo vencimiento:</span>
                        <span className="text-kuota-dodger font-extrabold">Día 20 · Automático</span>
                      </div>
                    </div>

                    <p className="text-[10px] text-white/60 text-center italic">
                      Simulación de interfaz demostrativa para el comercio.
                    </p>
                  </div>
                </TiltCard>
              </div>

            </div>
          </div>
        </ScrollReveal>
      </div>
    </section>
  );
};

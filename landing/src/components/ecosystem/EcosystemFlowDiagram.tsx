import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Users,
  Building,
  Store,
  Layers,
  Search,
  ShoppingCart,
  Coins,
  Calendar,
  CheckCircle2,
  TrendingUp,
  ArrowRight,
  ShieldCheck,
  Sparkles,
} from 'lucide-react';
import { ScrollReveal, PaymentProgress } from '../../utils/motionSystem';
import { trackEvent } from '../../utils/analytics';
import { kuotaSound } from '../../utils/soundSystem';

interface FlowStep {
  step: number;
  label: string;
  actor: 'COMPRADOR' | 'KUOTA' | 'COMERCIO' | 'ECOSISTEMA';
  title: string;
  description: string;
  detail: string;
  icon: React.ComponentType<{ className?: string }>;
  tag: string;
}

const FLOW_STEPS: FlowStep[] = [
  {
    step: 1,
    label: '01. Descubrimiento',
    actor: 'COMPRADOR',
    title: 'El comprador encuentra productos en un comercio participante',
    description: 'Encuentra el lote, insumo o mercadería en la tienda, bodega o distribuidor afiliado y escanea el código Kuota.',
    detail: 'Canal físico o digital · Validación de stock y términos en tiempo real.',
    icon: Search,
    tag: 'Acceso Comercial',
  },
  {
    step: 2,
    label: '02. Compra',
    actor: 'COMPRADOR',
    title: 'Selecciona la alternativa de compra en cuotas',
    description: 'El comprador elige fraccionar el importe de la operación según su nivel y disponibilidad en la plataforma.',
    detail: 'Condiciones transparentes · Cuota inicial accesible · Sin trámites bancarios engorrosos.',
    icon: ShoppingCart,
    tag: 'Opción Financiera',
  },
  {
    step: 3,
    label: '03. Colocación',
    actor: 'KUOTA',
    title: 'KUOTA registra la operación de colocación comercial',
    description: 'La plataforma valida la operación y habilita la colocación para que el comercio prepare y despache la mercadería con respaldo.',
    detail: 'Registro en ledger operativo · Notificación inmediata al comercio.',
    icon: Coins,
    tag: 'Motor de Colocación',
  },
  {
    step: 4,
    label: '04. Cuotas',
    actor: 'KUOTA',
    title: 'Se distribuye el calendario de cuotas y fechas',
    description: 'Se estructura el cronograma claro (ej. Día 20, Día 40, Día 60) alineado con los ciclos de venta de la mercadería.',
    detail: 'Cuotas programadas · Recordatorios preventivos antes de cada vencimiento.',
    icon: Calendar,
    tag: 'Gestión de Cuotas',
  },
  {
    step: 5,
    label: '05. Recaudación',
    actor: 'COMERCIO',
    title: 'Gestión de recaudación, pagos y conciliación',
    description: 'Los pagos de cada cuota se canalizan a través de billeteras o pasarelas homologadas y se concilian automáticamente.',
    detail: 'Conciliación digital · Registro transparente de cada abono.',
    icon: CheckCircle2,
    tag: 'Control de Recaudación',
  },
  {
    step: 6,
    label: '06. Seguimiento',
    actor: 'ECOSISTEMA',
    title: 'Seguimiento integral y crecimiento de la relación',
    description: 'El cumplimiento a tiempo eleva el historial del comprador, reduce su cuota inicial y permite al comercio vender más volumen.',
    detail: 'Crecimiento de niveles · Mayor rotación y fidelización para el comercio.',
    icon: TrendingUp,
    tag: 'Escala Sostenible',
  },
];

export const EcosystemFlowDiagram: React.FC = () => {
  const [activeStepNum, setActiveStepNum] = useState<number>(3);

  const activeStep = FLOW_STEPS.find((s) => s.step === activeStepNum) || FLOW_STEPS[2];
  const StepIcon = activeStep.icon;

  const handleStepClick = (stepNum: number) => {
    kuotaSound.playSelectOption();
    setActiveStepNum(stepNum);
    trackEvent('ecosystem_flow_step_select', { step: stepNum, label: FLOW_STEPS[stepNum - 1].label });
  };

  return (
    <section
      id="diagrama"
      aria-labelledby="diagram-heading"
      className="py-16 sm:py-24 bg-slate-50/70 border-b border-slate-200/80"
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <ScrollReveal delayMs={0} direction="up" className="text-center max-w-3xl mx-auto">
          <span className="inline-block px-3.5 py-1 rounded-full bg-white border border-slate-200/90 text-xs font-bold text-[#0A192F] uppercase tracking-wider mb-3 shadow-xs">
            Arquitectura Operativa
          </span>
          <h2
            id="diagram-heading"
            className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-[#0A192F] tracking-tight"
          >
            Cómo funciona el Ecosistema KUOTA
          </h2>
          <p className="mt-3 text-base sm:text-lg font-normal text-slate-600">
            KUOTA no es solo una app de pagos: es la plataforma que articula la relación comercial y financiera entre compradores y comercios.
          </p>
        </ScrollReveal>

        {/* Diagrama de Relación Tripartita: COMPRADOR ↔ KUOTA ↔ COMERCIO */}
        <ScrollReveal delayMs={80} direction="up">
          <div className="rounded-[28px] bg-white border border-slate-200/90 p-6 sm:p-8 shadow-xl">
            <div className="text-center mb-6">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
                Relación Integral en 3 Dimensiones
              </span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-center">
              {/* Actor 1: Comprador */}
              <motion.div
                animate={{
                  scale: activeStep.actor === 'COMPRADOR' ? 1.02 : 1,
                  y: activeStep.actor === 'COMPRADOR' ? -4 : 0,
                }}
                transition={{ duration: 0.2 }}
                className={`rounded-2xl border p-5 text-center transition-all ${
                  activeStep.actor === 'COMPRADOR'
                    ? 'border-[#0391FD] bg-[#0391FD]/10 shadow-md ring-2 ring-[#0391FD]/20'
                    : 'border-slate-200/80 bg-slate-50/50 hover:bg-slate-50'
                }`}
              >
                <div className="w-12 h-12 rounded-2xl bg-[#0391FD] text-white flex items-center justify-center mx-auto mb-3 shadow-md">
                  <Users className="w-6 h-6" />
                </div>
                <h3 className="text-lg font-black text-[#0A192F]">COMPRADOR</h3>
                <p className="text-xs font-normal text-slate-500 mt-1 leading-relaxed">
                  Accede a compras financiadas, organiza sus cuotas y construye historial comercial positivo.
                </p>
              </motion.div>

              {/* Actor 2: Plataforma KUOTA (Centro) */}
              <motion.div
                animate={{
                  scale: activeStep.actor === 'KUOTA' || activeStep.actor === 'ECOSISTEMA' ? 1.03 : 1,
                  y: activeStep.actor === 'KUOTA' || activeStep.actor === 'ECOSISTEMA' ? -4 : 0,
                }}
                transition={{ duration: 0.2 }}
                className={`rounded-2xl border p-6 text-center transition-all relative ${
                  activeStep.actor === 'KUOTA' || activeStep.actor === 'ECOSISTEMA'
                    ? 'border-[#0A192F] bg-[#0A192F] text-white shadow-xl ring-4 ring-[#0391FD]/20'
                    : 'border-slate-800 bg-[#0A192F] text-white shadow-md'
                }`}
              >
                <span className="absolute -top-3 left-1/2 -translate-x-1/2 px-3 py-0.5 rounded-full bg-[#10B981] text-white text-[10px] font-bold uppercase tracking-wider shadow-sm">
                  Articulador Central
                </span>
                <div className="w-14 h-14 rounded-2xl bg-white/10 text-white border border-white/20 flex items-center justify-center mx-auto mb-3 shadow-inner">
                  <Layers className="w-7 h-7 text-[#38BDF8]" />
                </div>
                <h3 className="text-xl font-black text-white">KUOTA</h3>
                <p className="text-xs font-normal text-slate-300 mt-1 leading-relaxed">
                  Motor de colocaciones, administración de cuotas, trazabilidad de recaudación y soporte integral.
                </p>
              </motion.div>

              {/* Actor 3: Comercio */}
              <motion.div
                animate={{
                  scale: activeStep.actor === 'COMERCIO' ? 1.02 : 1,
                  y: activeStep.actor === 'COMERCIO' ? -4 : 0,
                }}
                transition={{ duration: 0.2 }}
                className={`rounded-2xl border p-5 text-center transition-all ${
                  activeStep.actor === 'COMERCIO'
                    ? 'border-[#10B981] bg-emerald-50/80 shadow-md ring-2 ring-emerald-500/20'
                    : 'border-slate-200/80 bg-slate-50/50 hover:bg-slate-50'
                }`}
              >
                <div className="w-12 h-12 rounded-2xl bg-[#059669] text-white flex items-center justify-center mx-auto mb-3 shadow-md">
                  <Store className="w-6 h-6 text-white" />
                </div>
                <h3 className="text-lg font-black text-[#0A192F]">COMERCIO</h3>
                <p className="text-xs font-normal text-slate-500 mt-1 leading-relaxed">
                  Vende más mercadería, ofrece cuotas en mostrador y recibe seguimiento organizado de su recaudación.
                </p>
              </motion.div>
            </div>

            {/* Barra de progreso de las 6 etapas operativas */}
            <div className="mt-8 pt-6 border-t border-slate-100">
              <PaymentProgress
                label={`Proceso operativo Kuota · Etapa ${activeStepNum} de 6`}
                currentStep={activeStepNum}
                totalSteps={FLOW_STEPS.length}
                statusText={activeStep.tag}
              />
            </div>
          </div>
        </ScrollReveal>

        {/* Las 6 Etapas Operativas (Interactivas con Animaciones) */}
        <div className="space-y-6">
          <div
            role="tablist"
            aria-label="Etapas del flujo operativo Kuota"
            className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-2.5"
          >
            {FLOW_STEPS.map((s) => {
              const isSelected = s.step === activeStepNum;
              const Icon = s.icon;
              return (
                <motion.button
                  key={s.step}
                  type="button"
                  role="tab"
                  aria-selected={isSelected}
                  onClick={() => handleStepClick(s.step)}
                  whileHover={{ y: -3, scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  className={`p-3.5 rounded-2xl border text-left transition-all flex flex-col justify-between gap-2.5 cursor-pointer ${
                    isSelected
                      ? 'bg-[#0A192F] text-white border-[#0A192F] shadow-lg ring-2 ring-[#0391FD]/20'
                      : 'bg-white hover:bg-slate-50 text-slate-800 border-slate-200/90 shadow-xs'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <Icon className={`w-4 h-4 ${isSelected ? 'text-[#38BDF8]' : 'text-slate-400'}`} />
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                      isSelected ? 'bg-[#0391FD] text-white' : 'bg-slate-100 text-slate-600'
                    }`}>
                      Paso {s.step}
                    </span>
                  </div>
                  <span className="text-xs font-bold leading-tight">
                    {s.label.split('. ')[1]}
                  </span>
                </motion.button>
              );
            })}
          </div>

          {/* Panel Detallado de la Etapa Seleccionada (Animación Fluida AnimatePresence) */}
          <AnimatePresence mode="wait">
            <motion.div
              key={activeStep.step}
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -12 }}
              transition={{ duration: 0.22, ease: [0.16, 1, 0.3, 1] }}
              className="rounded-[28px] bg-white border border-slate-200/90 p-6 sm:p-8 shadow-xl flex flex-col md:flex-row items-start md:items-center justify-between gap-6 overflow-hidden relative"
            >
              <div className="space-y-2.5 max-w-2xl relative z-10">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="px-3 py-1 rounded-full bg-slate-100 border border-slate-200 text-[11px] font-bold text-slate-700 uppercase tracking-wider">
                    Etapa {activeStep.step} de 6 · Actor: {activeStep.actor}
                  </span>
                  <span className="px-3 py-1 rounded-full bg-emerald-50 border border-emerald-200 text-emerald-800 text-[11px] font-bold">
                    {activeStep.tag}
                  </span>
                </div>
                <h3 className="text-2xl sm:text-3xl font-black text-[#0A192F] tracking-tight">
                  {activeStep.title}
                </h3>
                <p className="text-sm font-normal text-slate-600 leading-relaxed">
                  {activeStep.description}
                </p>
                <div className="pt-2 text-xs font-semibold text-slate-700 flex items-center gap-2">
                  <ShieldCheck className="w-4 h-4 text-[#10B981] shrink-0" />
                  <span>{activeStep.detail}</span>
                </div>
              </div>

              <div className="w-full md:w-auto shrink-0 flex items-center justify-end relative z-10">
                <div className="w-20 h-20 rounded-2xl bg-slate-50 border border-slate-200/90 flex items-center justify-center shadow-inner">
                  <StepIcon className="w-10 h-10 text-[#0391FD]" />
                </div>
              </div>
            </motion.div>
          </AnimatePresence>
        </div>
      </div>
    </section>
  );
};

import React, { useState } from 'react';
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
      className="py-16 sm:py-24 bg-kuota-palesky/35 border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <ScrollReveal delayMs={0} direction="up" className="text-center max-w-3xl mx-auto">
          <span className="inline-block px-3.5 py-1 rounded-full bg-white border-2 border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-3">
            Arquitectura Operativa
          </span>
          <h2
            id="diagram-heading"
            className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-kuota-deepnavy tracking-tight"
          >
            Cómo funciona el Ecosistema KUOTA
          </h2>
          <p className="mt-3 text-base sm:text-lg font-semibold text-kuota-crayola">
            KUOTA no es solo una app de pagos: es la plataforma que articula la relación comercial y financiera entre compradores y comercios.
          </p>
        </ScrollReveal>

        {/* Diagrama de Relación Tripartita: COMPRADOR ↔ KUOTA ↔ COMERCIO */}
        <ScrollReveal delayMs={80} direction="up">
          <div className="rounded-3xl bg-white border-2 border-kuota-deepnavy p-6 sm:p-8 shadow-navy-hard-lg">
            <div className="text-center mb-6">
              <span className="text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider">
                Relación Integral en 3 Dimensiones
              </span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-center">
              {/* Actor 1: Comprador */}
              <div
                className={`rounded-2xl border-2 p-5 text-center transition-all ${
                  activeStep.actor === 'COMPRADOR'
                    ? 'border-kuota-dodger bg-kuota-dodger/10 shadow-sm -translate-y-1'
                    : 'border-kuota-deepnavy/25 bg-white'
                }`}
              >
                <div className="w-12 h-12 rounded-xl bg-kuota-dodger text-white border-2 border-kuota-deepnavy flex items-center justify-center mx-auto mb-3 shadow-sm">
                  <Users className="w-6 h-6" />
                </div>
                <h3 className="text-lg font-extrabold text-kuota-deepnavy">COMPRADOR</h3>
                <p className="text-xs font-semibold text-kuota-crayola mt-1">
                  Accede a compras financiadas, organiza sus cuotas y construye historial comercial positivo.
                </p>
              </div>

              {/* Actor 2: Plataforma KUOTA (Centro) */}
              <div
                className={`rounded-2xl border-2 p-6 text-center transition-all relative ${
                  activeStep.actor === 'KUOTA'
                    ? 'border-kuota-deepnavy bg-kuota-deepnavy text-white shadow-navy-hard -translate-y-1'
                    : 'border-kuota-deepnavy bg-kuota-deepnavy/90 text-white'
                }`}
              >
                <span className="absolute -top-3 left-1/2 -translate-x-1/2 px-3 py-0.5 rounded-full bg-kuota-dodger text-white text-[10px] font-extrabold uppercase">
                  Articulador
                </span>
                <div className="w-14 h-14 rounded-2xl bg-white text-kuota-deepnavy border-2 border-kuota-deepnavy flex items-center justify-center mx-auto mb-3 shadow-sm">
                  <Layers className="w-7 h-7 text-kuota-dodger" />
                </div>
                <h3 className="text-xl font-extrabold text-white">KUOTA</h3>
                <p className="text-xs font-semibold text-kuota-palesky mt-1">
                  Motor de colocaciones, administración de cuotas, trazabilidad de recaudación y soporte integral.
                </p>
              </div>

              {/* Actor 3: Comercio */}
              <div
                className={`rounded-2xl border-2 p-5 text-center transition-all ${
                  activeStep.actor === 'COMERCIO'
                    ? 'border-kuota-deepnavy bg-kuota-palesky/50 shadow-sm -translate-y-1'
                    : 'border-kuota-deepnavy/25 bg-white'
                }`}
              >
                <div className="w-12 h-12 rounded-xl bg-kuota-deepnavy text-white border-2 border-kuota-deepnavy flex items-center justify-center mx-auto mb-3 shadow-sm">
                  <Store className="w-6 h-6 text-kuota-palesky" />
                </div>
                <h3 className="text-lg font-extrabold text-kuota-deepnavy">COMERCIO</h3>
                <p className="text-xs font-semibold text-kuota-crayola mt-1">
                  Vende más mercadería, ofrece cuotas en mostrador y recibe seguimiento organizado de su recaudación.
                </p>
              </div>
            </div>

            {/* Barra de progreso de las 6 etapas operativas */}
            <div className="mt-8 pt-6 border-t border-kuota-deepnavy/15">
              <PaymentProgress
                label={`Proceso operativo Kuota · Etapa ${activeStepNum} de 6`}
                currentStep={activeStepNum}
                totalSteps={FLOW_STEPS.length}
                statusText={activeStep.tag}
              />
            </div>
          </div>
        </ScrollReveal>

        {/* Las 6 Etapas Operativas (Interactivas y Secuenciales) */}
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
                <button
                  key={s.step}
                  type="button"
                  role="tab"
                  aria-selected={isSelected}
                  onClick={() => handleStepClick(s.step)}
                  className={`p-3 rounded-2xl border-2 text-left transition-all flex flex-col justify-between gap-2 ${
                    isSelected
                      ? 'bg-kuota-deepnavy text-white border-kuota-deepnavy shadow-navy-hard -translate-y-1'
                      : 'bg-white hover:bg-kuota-palesky/40 text-kuota-deepnavy border-kuota-deepnavy/30'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <Icon className={`w-4 h-4 ${isSelected ? 'text-kuota-dodger' : 'text-kuota-crayola'}`} />
                    <span className={`text-[10px] font-extrabold px-1.5 py-0.5 rounded ${
                      isSelected ? 'bg-kuota-dodger text-white' : 'bg-kuota-palesky text-kuota-deepnavy'
                    }`}>
                      Paso {s.step}
                    </span>
                  </div>
                  <span className="text-xs font-extrabold leading-tight">
                    {s.label.split('. ')[1]}
                  </span>
                </button>
              );
            })}
          </div>

          {/* Panel Detallado de la Etapa Seleccionada */}
          <ScrollReveal delayMs={60} direction="up">
            <div className="rounded-3xl bg-white border-2 border-kuota-deepnavy p-6 sm:p-8 shadow-navy-hard-lg flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
              <div className="space-y-2 max-w-2xl">
                <div className="flex items-center gap-2">
                  <span className="px-2.5 py-0.5 rounded-full bg-kuota-palesky border border-kuota-deepnavy text-[11px] font-extrabold text-kuota-deepnavy uppercase">
                    Etapa {activeStep.step} de 6 · Actor: {activeStep.actor}
                  </span>
                  <span className="px-2 py-0.5 rounded bg-kuota-dodger text-white text-[11px] font-extrabold">
                    {activeStep.tag}
                  </span>
                </div>
                <h3 className="text-2xl font-extrabold text-kuota-deepnavy">
                  {activeStep.title}
                </h3>
                <p className="text-sm font-semibold text-kuota-crayola leading-relaxed">
                  {activeStep.description}
                </p>
                <div className="pt-2 text-xs font-bold text-kuota-deepnavy flex items-center gap-2">
                  <ShieldCheck className="w-4 h-4 text-kuota-dodger shrink-0" />
                  <span>{activeStep.detail}</span>
                </div>
              </div>

              <div className="w-full md:w-auto shrink-0 flex items-center justify-end">
                <div className="w-20 h-20 rounded-2xl bg-kuota-palesky/70 border-2 border-kuota-deepnavy flex items-center justify-center">
                  <StepIcon className="w-10 h-10 text-kuota-deepnavy" />
                </div>
              </div>
            </div>
          </ScrollReveal>
        </div>
      </div>
    </section>
  );
};

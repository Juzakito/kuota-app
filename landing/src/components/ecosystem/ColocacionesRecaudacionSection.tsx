import React, { useState } from 'react';
import {
  Coins,
  TrendingUp,
  Calendar,
  CheckCircle2,
  Clock,
  ArrowUpRight,
  ShieldCheck,
  FileSpreadsheet,
  AlertCircle,
  BarChart3,
  Sparkles,
} from 'lucide-react';
import { ScrollReveal, TiltCard, AnimatedNumber } from '../../utils/motionSystem';
import { trackEvent } from '../../utils/analytics';
import { kuotaSound } from '../../utils/soundSystem';

export const ColocacionesRecaudacionSection: React.FC = () => {
  const [selectedDemoTab, setSelectedDemoTab] = useState<'colocacion' | 'recaudacion'>('colocacion');

  return (
    <section
      id="colocaciones"
      aria-labelledby="colocaciones-heading"
      className="py-16 sm:py-24 bg-white border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <ScrollReveal delayMs={0} direction="up" className="text-center max-w-3xl mx-auto">
          <span className="inline-block px-3.5 py-1 rounded-full bg-kuota-palesky border-2 border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-3">
            Operación Financiera y Gestión
          </span>
          <h2
            id="colocaciones-heading"
            className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-kuota-deepnavy tracking-tight"
          >
            Colocaciones y Recaudación Trazable
          </h2>
          <p className="mt-3 text-base sm:text-lg font-semibold text-kuota-crayola">
            El motor de KUOTA organiza el ciclo completo: desde el registro del financiamiento comercial hasta la conciliación de cada cuota.
          </p>
        </ScrollReveal>

        {/* Selector de Pestañas: Colocación vs Recaudación */}
        <div className="flex justify-center">
          <div
            role="tablist"
            aria-label="Vistas del ciclo financiero"
            className="inline-flex p-1.5 rounded-2xl bg-kuota-palesky/60 border-2 border-kuota-deepnavy gap-1.5"
          >
            <button
              type="button"
              role="tab"
              aria-selected={selectedDemoTab === 'colocacion'}
              onClick={() => {
                kuotaSound.playSelectOption();
                setSelectedDemoTab('colocacion');
                trackEvent('colocaciones_tab_change', { tab: 'colocacion' });
              }}
              className={`px-5 py-2.5 rounded-xl text-xs sm:text-sm font-extrabold transition-all flex items-center gap-2 ${
                selectedDemoTab === 'colocacion'
                  ? 'bg-kuota-deepnavy text-white shadow-sm'
                  : 'text-kuota-deepnavy hover:bg-white/80'
              }`}
            >
              <Coins className="w-4 h-4 text-kuota-dodger" />
              <span>1. Ciclo de Colocación</span>
            </button>

            <button
              type="button"
              role="tab"
              aria-selected={selectedDemoTab === 'recaudacion'}
              onClick={() => {
                kuotaSound.playSelectOption();
                setSelectedDemoTab('recaudacion');
                trackEvent('colocaciones_tab_change', { tab: 'recaudacion' });
              }}
              className={`px-5 py-2.5 rounded-xl text-xs sm:text-sm font-extrabold transition-all flex items-center gap-2 ${
                selectedDemoTab === 'recaudacion'
                  ? 'bg-kuota-deepnavy text-white shadow-sm'
                  : 'text-kuota-deepnavy hover:bg-white/80'
              }`}
            >
              <TrendingUp className="w-4 h-4 text-kuota-dodger" />
              <span>2. Control de Recaudación</span>
            </button>
          </div>
        </div>

        {/* Panel Interactivo Demostrativo */}
        <ScrollReveal delayMs={80} direction="up">
          <div className="rounded-3xl border-2 border-kuota-deepnavy bg-kuota-palesky/30 p-6 sm:p-8 lg:p-10 shadow-navy-hard-lg">
            
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 pb-6 border-b border-kuota-deepnavy/20 mb-6">
              <div>
                <span className="text-xs font-extrabold uppercase tracking-wider text-kuota-crayola">
                  {selectedDemoTab === 'colocacion' ? 'Estructura de Colocación' : 'Estado de Cartera y Cobranza'}
                </span>
                <h3 className="text-xl sm:text-2xl font-extrabold text-kuota-deepnavy mt-0.5">
                  {selectedDemoTab === 'colocacion'
                    ? 'Registro y desembolso de operaciones de lote'
                    : 'Trazabilidad de pagos, fechas y conciliación de cuotas'}
                </h3>
              </div>
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-white border border-kuota-deepnavy text-[11px] font-extrabold text-kuota-deepnavy shrink-0">
                <Sparkles className="w-3.5 h-3.5 text-kuota-dodger" />
                Datos Demostrativos del Modelo
              </span>
            </div>

            {selectedDemoTab === 'colocacion' ? (
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
                {/* Métricas de Colocación */}
                <div className="lg:col-span-7 space-y-4">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div className="rounded-2xl bg-white border-2 border-kuota-deepnavy p-4 shadow-sm">
                      <span className="text-xs font-bold text-kuota-crayola block mb-1">
                        Operación comercial registrada
                      </span>
                      <div className="text-2xl font-extrabold text-kuota-deepnavy flex items-center justify-between">
                        <AnimatedNumber value={1200} prefix="S/ " decimals={2} />
                        <span className="text-xs font-extrabold px-2 py-0.5 rounded bg-kuota-palesky text-kuota-deepnavy">
                          Nivel 3
                        </span>
                      </div>
                      <p className="text-[11px] font-semibold text-kuota-deepnavy/70 mt-1">
                        Mercadería entregada al comprador
                      </p>
                    </div>

                    <div className="rounded-2xl bg-white border-2 border-kuota-deepnavy p-4 shadow-sm">
                      <span className="text-xs font-bold text-kuota-crayola block mb-1">
                        Anticipo a comercio participante
                      </span>
                      <div className="text-2xl font-extrabold text-kuota-dodger flex items-center justify-between">
                        <AnimatedNumber value={720} prefix="S/ " decimals={2} />
                        <span className="text-xs font-extrabold px-2 py-0.5 rounded bg-blue-100 text-kuota-dodger">
                          60% lote
                        </span>
                      </div>
                      <p className="text-[11px] font-semibold text-kuota-deepnavy/70 mt-1">
                        Liquidación programada en despacho
                      </p>
                    </div>
                  </div>

                  <div className="rounded-2xl bg-white border-2 border-kuota-deepnavy p-5 space-y-3 shadow-sm">
                    <div className="flex items-center justify-between text-xs font-extrabold text-kuota-deepnavy">
                      <span className="flex items-center gap-2">
                        <FileSpreadsheet className="w-4 h-4 text-kuota-dodger" />
                        Distribución del saldo por recaudar
                      </span>
                      <span>S/ 540.00 (40% + cuotas)</span>
                    </div>
                    <div className="w-full h-3 rounded-full bg-kuota-palesky flex overflow-hidden p-0.5 gap-0.5 border border-kuota-deepnavy/30">
                      <div className="h-full w-[33.3%] rounded-l-full bg-kuota-dodger" title="Kuota 1" />
                      <div className="h-full w-[33.3%] bg-kuota-dodger/80" title="Kuota 2" />
                      <div className="h-full w-[33.4%] rounded-r-full bg-kuota-dodger/60" title="Kuota 3" />
                    </div>
                    <div className="grid grid-cols-3 text-[11px] font-bold text-kuota-crayola text-center">
                      <div>Día 20 · S/ 180</div>
                      <div>Día 40 · S/ 180</div>
                      <div>Día 60 · S/ 180</div>
                    </div>
                  </div>
                </div>

                {/* Explicación Operativa */}
                <div className="lg:col-span-5 rounded-2xl bg-kuota-deepnavy text-white p-6 border-2 border-kuota-deepnavy space-y-4">
                  <div className="flex items-center gap-2 text-xs font-bold text-kuota-palesky">
                    <ShieldCheck className="w-4 h-4 text-kuota-dodger" />
                    <span>Control de Riesgo y Colocación</span>
                  </div>
                  <h4 className="text-xl font-extrabold text-white">
                    Financiamiento estructurado con respaldo en mercadería
                  </h4>
                  <p className="text-xs sm:text-sm text-kuota-palesky/90 leading-relaxed">
                    Las colocaciones de KUOTA no entregan efectivo sin destino: financian compras concretas de mercadería con comercios y distribuidores verificados, reduciendo el riesgo de desvío de fondos.
                  </p>
                  <div className="pt-2 border-t border-white/15 text-xs text-white/80 font-bold space-y-1">
                    <div className="flex items-center justify-between">
                      <span>Validación de comercio:</span>
                      <span className="text-kuota-dodger font-extrabold">Requerida ✓</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span>Moneda de liquidación:</span>
                      <span className="text-white font-extrabold">PEN (Soles)</span>
                    </div>
                  </div>
                </div>
              </div>
            ) : (
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
                {/* Calendario y Trazabilidad de Recaudación */}
                <div className="lg:col-span-7 space-y-4">
                  <div className="rounded-2xl bg-white border-2 border-kuota-deepnavy p-4 space-y-3 shadow-sm">
                    <span className="text-xs font-extrabold text-kuota-deepnavy flex items-center justify-between">
                      <span>Cronograma de Cobro y Vencimientos</span>
                      <span className="px-2 py-0.5 rounded bg-emerald-100 text-emerald-700 text-[11px]">
                        Tasa de cumplimiento: 98%
                      </span>
                    </span>

                    <div className="space-y-2">
                      <div className="p-3 rounded-xl bg-kuota-palesky/30 border border-kuota-deepnavy/20 flex items-center justify-between text-xs font-bold text-kuota-deepnavy">
                        <div className="flex items-center gap-2.5">
                          <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                          <div>
                            <span className="block font-extrabold">Kuota 1 · Cobrada</span>
                            <span className="text-[11px] text-kuota-crayola">Conciliada por QR / Billetera</span>
                          </div>
                        </div>
                        <span className="font-extrabold">S/ 180.00</span>
                      </div>

                      <div className="p-3 rounded-xl bg-blue-50 border border-kuota-dodger flex items-center justify-between text-xs font-bold text-kuota-deepnavy">
                        <div className="flex items-center gap-2.5">
                          <Clock className="w-4 h-4 text-kuota-dodger shrink-0 animate-pulse" />
                          <div>
                            <span className="block font-extrabold text-kuota-deepnavy">Kuota 2 · Próximo vencimiento</span>
                            <span className="text-[11px] text-kuota-crayola">Aviso preventivo enviado</span>
                          </div>
                        </div>
                        <span className="font-extrabold text-kuota-dodger">S/ 180.00</span>
                      </div>

                      <div className="p-3 rounded-xl bg-white border border-kuota-deepnavy/20 flex items-center justify-between text-xs font-bold text-kuota-deepnavy">
                        <div className="flex items-center gap-2.5">
                          <Calendar className="w-4 h-4 text-kuota-crayola shrink-0" />
                          <div>
                            <span className="block font-extrabold">Kuota 3 · Programada</span>
                            <span className="text-[11px] text-kuota-crayola">En ciclo de venta regular</span>
                          </div>
                        </div>
                        <span className="font-extrabold text-kuota-crayola">S/ 180.00</span>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Explicación de Recaudación */}
                <div className="lg:col-span-5 rounded-2xl bg-white border-2 border-kuota-deepnavy p-6 space-y-4 shadow-sm">
                  <div className="flex items-center gap-2 text-xs font-extrabold text-kuota-deepnavy">
                    <BarChart3 className="w-4 h-4 text-kuota-dodger" />
                    <span>Conciliación Digital y Notificación</span>
                  </div>
                  <h4 className="text-xl font-extrabold text-kuota-deepnavy">
                    Reporte transparente para el comercio
                  </h4>
                  <p className="text-xs sm:text-sm font-semibold text-kuota-crayola leading-relaxed">
                    El comercio visualiza en tiempo real el estado de cada cuota y la conciliación correspondiente, sin necesidad de perseguir comprobantes físicos por WhatsApp ni cuadres manuales al final del mes.
                  </p>
                  <div className="p-3 rounded-xl bg-kuota-palesky/50 border border-kuota-deepnavy text-xs font-bold text-kuota-deepnavy flex items-start gap-2">
                    <ShieldCheck className="w-4 h-4 text-kuota-deepnavy shrink-0 mt-0.5" />
                    <span>Liquidación ordenada sin cargos sorpresa.</span>
                  </div>
                </div>
              </div>
            )}

            <div className="mt-6 pt-4 border-t border-kuota-deepnavy/15 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 text-xs font-semibold text-kuota-deepnavy/75">
              <span>* Cifras y porcentajes son ilustrativos para la demostración del modelo operativo.</span>
              <span className="font-extrabold text-kuota-deepnavy">Piloto en preparación · Lanzamiento 10.11.2026</span>
            </div>

          </div>
        </ScrollReveal>
      </div>
    </section>
  );
};

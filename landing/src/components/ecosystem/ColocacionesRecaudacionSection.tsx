import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
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
      className="py-16 sm:py-24 bg-white border-b border-slate-200/80"
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <ScrollReveal delayMs={0} direction="up" className="text-center max-w-3xl mx-auto">
          <span className="inline-block px-3.5 py-1 rounded-full bg-sky-50 border border-sky-200/80 text-xs font-bold text-[#0391FD] uppercase tracking-wider mb-3">
            Operación Financiera y Gestión
          </span>
          <h2
            id="colocaciones-heading"
            className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-[#0A192F] tracking-tight"
          >
            Colocaciones y Recaudación Trazable
          </h2>
          <p className="mt-3 text-base sm:text-lg font-normal text-slate-600">
            El motor de KUOTA organiza el ciclo completo: desde el registro del financiamiento comercial hasta la conciliación de cada cuota.
          </p>
        </ScrollReveal>

        {/* Selector de Pestañas: Colocación vs Recaudación (Modern Fintech Pill) */}
        <div className="flex justify-center">
          <div
            role="tablist"
            aria-label="Vistas del ciclo financiero"
            className="inline-flex p-1 rounded-full bg-slate-100 border border-slate-200/90 gap-1 shadow-inner"
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
              className={`px-5 py-2.5 rounded-full text-xs sm:text-sm font-bold transition-all flex items-center gap-2 cursor-pointer active:scale-95 ${
                selectedDemoTab === 'colocacion'
                  ? 'bg-[#0A192F] text-white shadow-sm'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-white/80'
              }`}
            >
              <Coins className="w-4 h-4 text-[#38BDF8]" />
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
              className={`px-5 py-2.5 rounded-full text-xs sm:text-sm font-bold transition-all flex items-center gap-2 cursor-pointer active:scale-95 ${
                selectedDemoTab === 'recaudacion'
                  ? 'bg-[#0A192F] text-white shadow-sm'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-white/80'
              }`}
            >
              <TrendingUp className="w-4 h-4 text-[#10B981]" />
              <span>2. Control de Recaudación</span>
            </button>
          </div>
        </div>

        {/* Panel Interactivo Demostrativo con Animaciones */}
        <ScrollReveal delayMs={80} direction="up">
          <div className="rounded-[28px] border border-slate-200/90 bg-white p-6 sm:p-8 lg:p-10 shadow-xl overflow-hidden relative">
            
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 pb-6 border-b border-slate-100 mb-6">
              <div>
                <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
                  {selectedDemoTab === 'colocacion' ? 'Estructura de Colocación' : 'Estado de Cartera y Cobranza'}
                </span>
                <h3 className="text-xl sm:text-2xl font-black text-[#0A192F] tracking-tight mt-0.5">
                  {selectedDemoTab === 'colocacion'
                    ? 'Registro y desembolso de operaciones de lote'
                    : 'Trazabilidad de pagos, fechas y conciliación de cuotas'}
                </h3>
              </div>
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 border border-emerald-200 text-[11px] font-bold text-emerald-800 shrink-0">
                <Sparkles className="w-3.5 h-3.5 text-[#10B981]" />
                Datos Demostrativos del Modelo
              </span>
            </div>

            <AnimatePresence mode="wait">
              {selectedDemoTab === 'colocacion' ? (
                <motion.div
                  key="colocacion"
                  initial={{ opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -12 }}
                  transition={{ duration: 0.24, ease: [0.16, 1, 0.3, 1] }}
                  className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center"
                >
                  {/* Métricas de Colocación */}
                  <div className="lg:col-span-7 space-y-4">
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      <div className="rounded-2xl bg-slate-50 border border-slate-200/80 p-4 shadow-xs">
                        <span className="text-xs font-semibold text-slate-500 block mb-1">
                          Operación comercial registrada
                        </span>
                        <div className="text-2xl font-black text-[#0A192F] flex items-center justify-between">
                          <AnimatedNumber value={1200} prefix="S/ " decimals={2} />
                          <span className="text-xs font-bold px-2 py-0.5 rounded-full bg-slate-200 text-slate-800">
                            Nivel 3
                          </span>
                        </div>
                        <p className="text-[11px] font-medium text-slate-500 mt-1">
                          Mercadería entregada al comprador
                        </p>
                      </div>

                      <div className="rounded-2xl bg-slate-50 border border-slate-200/80 p-4 shadow-xs">
                        <span className="text-xs font-semibold text-slate-500 block mb-1">
                          Anticipo a comercio participante
                        </span>
                        <div className="text-2xl font-black text-[#0391FD] flex items-center justify-between">
                          <AnimatedNumber value={720} prefix="S/ " decimals={2} />
                          <span className="text-xs font-bold px-2 py-0.5 rounded-full bg-sky-100 text-[#0391FD]">
                            60% lote
                          </span>
                        </div>
                        <p className="text-[11px] font-medium text-slate-500 mt-1">
                          Liquidación programada en despacho
                        </p>
                      </div>
                    </div>

                    <div className="rounded-2xl bg-slate-50 border border-slate-200/80 p-5 space-y-3 shadow-xs">
                      <div className="flex items-center justify-between text-xs font-bold text-[#0A192F]">
                        <span className="flex items-center gap-2">
                          <FileSpreadsheet className="w-4 h-4 text-[#0391FD]" />
                          Distribución del saldo por recaudar
                        </span>
                        <span className="font-mono">S/ 540.00 (40% + cuotas)</span>
                      </div>
                      <div className="w-full h-3 rounded-full bg-slate-200 flex overflow-hidden p-0.5 gap-0.5">
                        <motion.div
                          initial={{ width: 0 }}
                          animate={{ width: '33.3%' }}
                          transition={{ duration: 0.6, ease: 'easeOut' }}
                          className="h-full rounded-l-full bg-[#0391FD]"
                          title="Kuota 1"
                        />
                        <motion.div
                          initial={{ width: 0 }}
                          animate={{ width: '33.3%' }}
                          transition={{ duration: 0.6, delay: 0.1, ease: 'easeOut' }}
                          className="h-full bg-[#0391FD]/80"
                          title="Kuota 2"
                        />
                        <motion.div
                          initial={{ width: 0 }}
                          animate={{ width: '33.4%' }}
                          transition={{ duration: 0.6, delay: 0.2, ease: 'easeOut' }}
                          className="h-full rounded-r-full bg-[#0391FD]/60"
                          title="Kuota 3"
                        />
                      </div>
                      <div className="grid grid-cols-3 text-[11px] font-semibold text-slate-500 text-center">
                        <div>Día 20 · S/ 180</div>
                        <div>Día 40 · S/ 180</div>
                        <div>Día 60 · S/ 180</div>
                      </div>
                    </div>
                  </div>

                  {/* Explicación Operativa */}
                  <div className="lg:col-span-5 rounded-2xl bg-[#0A192F] text-white p-6 border border-slate-800 space-y-4 shadow-lg">
                    <div className="flex items-center gap-2 text-xs font-bold text-[#38BDF8]">
                      <ShieldCheck className="w-4 h-4 text-[#10B981]" />
                      <span>Control de Riesgo y Colocación</span>
                    </div>
                    <h4 className="text-xl font-black text-white">
                      Financiamiento estructurado con respaldo en mercadería
                    </h4>
                    <p className="text-xs sm:text-sm text-slate-300 leading-relaxed font-normal">
                      Las colocaciones de KUOTA no entregan efectivo sin destino: financian compras concretas de mercadería con comercios y distribuidores verificados, reduciendo el riesgo de desvío de fondos.
                    </p>
                    <div className="pt-2 border-t border-white/10 text-xs text-slate-300 font-semibold space-y-1.5">
                      <div className="flex items-center justify-between">
                        <span>Validación de comercio:</span>
                        <span className="text-[#10B981] font-bold">Requerida ✓</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span>Moneda de liquidación:</span>
                        <span className="text-white font-bold">PEN (Soles)</span>
                      </div>
                    </div>
                  </div>
                </motion.div>
              ) : (
                <motion.div
                  key="recaudacion"
                  initial={{ opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -12 }}
                  transition={{ duration: 0.24, ease: [0.16, 1, 0.3, 1] }}
                  className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center"
                >
                  {/* Calendario y Trazabilidad de Recaudación */}
                  <div className="lg:col-span-7 space-y-4">
                    <div className="rounded-2xl bg-slate-50 border border-slate-200/80 p-5 space-y-3 shadow-xs">
                      <span className="text-xs font-bold text-[#0A192F] flex items-center justify-between">
                        <span>Cronograma de Cobro y Vencimientos</span>
                        <span className="px-2.5 py-0.5 rounded-full bg-emerald-100 text-emerald-800 text-[11px] font-bold">
                          Tasa de cumplimiento: 98%
                        </span>
                      </span>

                      <div className="space-y-2.5">
                        <div className="p-3.5 rounded-2xl bg-white border border-slate-200/90 flex items-center justify-between text-xs font-bold text-[#0A192F] shadow-xs">
                          <div className="flex items-center gap-2.5">
                            <CheckCircle2 className="w-4 h-4 text-[#10B981] shrink-0" />
                            <div>
                              <span className="block font-bold">Kuota 1 · Cobrada</span>
                              <span className="text-[11px] font-normal text-slate-500">Conciliada por QR / Billetera</span>
                            </div>
                          </div>
                          <span className="font-black text-[#0A192F]">S/ 180.00</span>
                        </div>

                        <div className="p-3.5 rounded-2xl bg-sky-50/80 border border-sky-200 flex items-center justify-between text-xs font-bold text-[#0A192F]">
                          <div className="flex items-center gap-2.5">
                            <Clock className="w-4 h-4 text-[#0391FD] shrink-0 animate-pulse" />
                            <div>
                              <span className="block font-bold text-[#0A192F]">Kuota 2 · Próximo vencimiento</span>
                              <span className="text-[11px] font-normal text-slate-500">Aviso preventivo enviado</span>
                            </div>
                          </div>
                          <span className="font-black text-[#0391FD]">S/ 180.00</span>
                        </div>

                        <div className="p-3.5 rounded-2xl bg-white border border-slate-200/80 flex items-center justify-between text-xs font-bold text-slate-600">
                          <div className="flex items-center gap-2.5">
                            <Calendar className="w-4 h-4 text-slate-400 shrink-0" />
                            <div>
                              <span className="block font-bold">Kuota 3 · Programada</span>
                              <span className="text-[11px] font-normal text-slate-500">En ciclo de venta regular</span>
                            </div>
                          </div>
                          <span className="font-semibold text-slate-500">S/ 180.00</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Explicación de Recaudación */}
                  <div className="lg:col-span-5 rounded-2xl bg-slate-50 border border-slate-200/80 p-6 space-y-4 shadow-xs">
                    <div className="flex items-center gap-2 text-xs font-bold text-[#0391FD]">
                      <BarChart3 className="w-4 h-4 text-[#0391FD]" />
                      <span>Conciliación Digital y Notificación</span>
                    </div>
                    <h4 className="text-xl font-black text-[#0A192F]">
                      Reporte transparente para el comercio
                    </h4>
                    <p className="text-xs sm:text-sm font-normal text-slate-600 leading-relaxed">
                      El comercio visualiza en tiempo real el estado de cada cuota y la conciliación correspondiente, sin necesidad de perseguir comprobantes físicos por WhatsApp ni cuadres manuales al final del mes.
                    </p>
                    <div className="p-3.5 rounded-2xl bg-white border border-slate-200/80 text-xs font-medium text-slate-700 flex items-start gap-2 shadow-xs">
                      <ShieldCheck className="w-4 h-4 text-[#10B981] shrink-0 mt-0.5" />
                      <span>Liquidación ordenada sin cargos sorpresa ni letras de cambio físicas.</span>
                    </div>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>

            <div className="mt-6 pt-4 border-t border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 text-xs font-medium text-slate-500">
              <span>* Cifras y porcentajes son ilustrativos para la demostración del modelo operativo.</span>
              <span className="font-bold text-slate-800">Piloto en preparación · Lanzamiento 10.11.2026</span>
            </div>

          </div>
        </ScrollReveal>
      </div>
    </section>
  );
};

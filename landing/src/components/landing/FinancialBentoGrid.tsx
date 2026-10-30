'use client';

import React from 'react';
import { ShieldCheck, Lock, Cloud, Award, CheckCircle2, TrendingUp, Sparkles, Building } from 'lucide-react';
import { FINANCIAL_BENTO_CARDS, FinancialMetricCard } from '../../config/fintechData';

export const FinancialBentoGrid: React.FC = () => {
  return (
    <section id="transparencia" className="relative py-12">
      <div className="max-w-7xl mx-auto mx-3 sm:mx-6 lg:mx-auto space-y-12">
        {/* SUB-BLOQUE A: BENTO FINANCIERO DE TRANSPARENCIA */}
        <div className="rounded-[32px] md:rounded-[48px] bg-white border border-slate-200/80 p-6 sm:p-10 md:p-14 shadow-[0_8px_30px_rgb(0,0,0,0.03)]">
          <div className="max-w-3xl mb-10">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-slate-100 border border-slate-200/60 text-xs font-semibold text-slate-800 tracking-tight mb-3">
              <Sparkles className="w-3.5 h-3.5 text-emerald-600" />
              <span>Transparencia & Condiciones Financieras</span>
            </div>
            <h2 className="text-2xl sm:text-3xl md:text-4xl font-extrabold text-[#070A0F] tracking-tight leading-tight">
              Lo que debes saber sobre tu línea Kuota
            </h2>
            <p className="text-xs sm:text-sm text-slate-600 mt-2">
              Sin letras chicas ni cláusulas abusivas. Especificaciones técnicas y comerciales claras desde el primer día.
            </p>
          </div>

          {/* GRID DE 5 TARJETAS BENTO */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {FINANCIAL_BENTO_CARDS.map((card, idx) => {
              const isLarge = idx === 0 || idx === 1;
              return (
                <div
                  key={card.id}
                  className={`rounded-[28px] p-7 flex flex-col justify-between transition-all hover:scale-[1.01] ${
                    card.isDark
                      ? 'bg-[#090D14] text-white border border-white/10 shadow-xl'
                      : 'bg-[#F8FAFC] text-slate-900 border border-slate-200/80 shadow-sm'
                  } ${idx === 0 ? 'lg:col-span-2' : ''}`}
                >
                  <div className="flex items-center justify-between mb-4">
                    <span
                      className={`text-[10px] font-bold uppercase tracking-wider px-3 py-1 rounded-full ${
                        card.isDark
                          ? 'bg-white/10 text-emerald-400 border border-white/10'
                          : 'bg-emerald-100 text-emerald-800'
                      }`}
                    >
                      {card.badge}
                    </span>
                    <span
                      className={`text-xs font-mono font-bold ${
                        card.isDark ? 'text-slate-400' : 'text-slate-500'
                      }`}
                    >
                      0{idx + 1}
                    </span>
                  </div>

                  <div className="my-3">
                    <h3
                      className={`text-sm font-semibold mb-1 ${
                        card.isDark ? 'text-slate-300' : 'text-slate-600'
                      }`}
                    >
                      {card.title}
                    </h3>
                    <div
                      className={`text-3xl sm:text-4xl font-extrabold tracking-tight tabular-nums ${
                        card.isDark ? 'text-white' : 'text-slate-950'
                      }`}
                    >
                      {card.value}
                    </div>
                  </div>

                  <p
                    className={`text-xs leading-relaxed mt-2 pt-3 border-t ${
                      card.isDark
                        ? 'text-slate-400 border-white/10'
                        : 'text-slate-600 border-slate-200/70'
                    }`}
                  >
                    {card.description}
                  </p>
                </div>
              );
            })}
          </div>
        </div>

        {/* SUB-BLOQUE B: RESPALDO INDUSTRIAL Y DE SEGURIDAD */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Card 1: Puntuación Global */}
          <div className="rounded-[32px] bg-[#070A0F] border border-white/10 p-7 text-white flex flex-col justify-between shadow-xl">
            <div>
              <span className="text-xs font-mono text-emerald-400 font-bold uppercase tracking-wider">
                Satisfacción Verificada
              </span>
              <div className="text-3xl sm:text-4xl font-extrabold text-white mt-2 tabular-nums">
                4.9 / 5.0 ★
              </div>
              <p className="text-xs text-slate-300 mt-2 leading-relaxed">
                Más de 300+ empresas comerciales y distribuidoras en Perú gestionan sus compras y cuotas con Kuota.
              </p>
            </div>
            <div className="pt-4 border-t border-white/10 flex items-center gap-2 text-[11px] text-emerald-300">
              <CheckCircle2 className="w-4 h-4 text-[#10B981]" />
              <span>99.2% de retención de clientes</span>
            </div>
          </div>

          {/* Card 2: Seguridad de Grado Bancario */}
          <div className="rounded-[32px] bg-white border border-slate-200/80 p-7 text-slate-900 flex flex-col justify-between shadow-sm">
            <div>
              <span className="text-xs font-mono text-indigo-700 font-bold uppercase tracking-wider">
                Infraestructura Cloud
              </span>
              <div className="text-2xl sm:text-3xl font-extrabold text-slate-950 mt-2">
                TLS 1.3 & 99.99%
              </div>
              <p className="text-xs text-slate-600 mt-2 leading-relaxed">
                Encriptación de grado bancario en reposo y tránsito sobre servidores AWS y Google Cloud con alta disponibilidad.
              </p>
            </div>
            <div className="pt-4 border-t border-slate-100 flex items-center gap-2 text-[11px] text-slate-600">
              <Lock className="w-4 h-4 text-indigo-600" />
              <span>Cumplimiento Ley N° 29733 (Datos Personales)</span>
            </div>
          </div>

          {/* Card 3: Sello de Innovación VALT */}
          <div className="rounded-[32px] bg-gradient-to-br from-[#0F766E] to-[#1E3A8A] border border-white/15 p-7 text-white flex flex-col justify-between shadow-xl">
            <div>
              <span className="text-xs font-mono text-emerald-300 font-bold uppercase tracking-wider">
                Respaldo Institucional
              </span>
              <div className="text-2xl sm:text-3xl font-extrabold text-white mt-2">
                Ecosistema VALT
              </div>
              <p className="text-xs text-slate-200 mt-2 leading-relaxed italic">
                "Reinventamos el flujo de capital para el comercio latinoamericano combinando inteligencia artificial y financiamiento sin fricción."
              </p>
            </div>
            <div className="pt-4 border-t border-white/15 flex items-center gap-2 text-[11px] text-emerald-200">
              <Award className="w-4 h-4 text-[#8FD694]" />
              <span>Estándar MIT & Stanford Innovation</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

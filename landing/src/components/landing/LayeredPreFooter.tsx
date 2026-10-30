'use client';

import React from 'react';
import { motion } from 'framer-motion';
import { ArrowRight, ShieldCheck, Sparkles, Lock, CheckCircle2, Building2 } from 'lucide-react';
import { KuotaLogo } from '../KuotaLogo';

interface LayeredPreFooterProps {
  onOpenEvaluationModal: () => void;
}

export const LayeredPreFooter: React.FC<LayeredPreFooterProps> = ({
  onOpenEvaluationModal,
}) => {
  return (
    <footer className="relative bg-[#05070A] text-white rounded-t-[48px] overflow-hidden mt-12 border-t border-white/10">
      {/* Luz cenital azul/esmeralda */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[700px] h-[350px] bg-gradient-to-b from-[#10B981]/20 via-[#06B6D4]/10 to-transparent rounded-full blur-[100px] pointer-events-none" />

      {/* SECCIÓN PRE-FOOTER ISOMÉTRICO 3D */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-20 pb-16 relative z-10 text-center">
        {/* Titular Flanqueado */}
        <div className="flex flex-col md:flex-row items-center justify-center gap-2 md:gap-6 text-3xl sm:text-5xl md:text-6xl font-black tracking-tight mb-8">
          <span className="text-white">Menos quiebres,</span>
          <span className="bg-clip-text text-transparent bg-gradient-to-r from-[#8FD694] via-[#34D399] to-[#2DD4BF]">
            más liquidez
          </span>
        </div>

        <p className="max-w-xl mx-auto text-xs sm:text-sm text-slate-300 mb-10 leading-relaxed">
          Únete a cientos de empresas que ya automatizan su reabastecimiento y kuotean mercadería sin descapitalizarse.
        </p>

        {/* Las 4 Capas Isométricas 3D en Cascada */}
        <div className="relative w-full max-w-md mx-auto h-48 sm:h-56 flex items-center justify-center my-6">
          {/* Capa 4 (Fondo) */}
          <motion.div
            initial={{ opacity: 0, y: 36 }}
            whileInView={{ opacity: 0.3, y: 36 }}
            viewport={{ once: true }}
            className="absolute w-64 sm:w-80 h-24 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-sm -rotate-6"
          />

          {/* Capa 3 (Intermedia 2) */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 0.5, y: 20 }}
            viewport={{ once: true }}
            className="absolute w-64 sm:w-80 h-24 rounded-2xl bg-white/10 border border-white/15 backdrop-blur-md -rotate-3"
          />

          {/* Capa 2 (Intermedia 1) */}
          <motion.div
            initial={{ opacity: 0, y: 6 }}
            whileInView={{ opacity: 0.7, y: 6 }}
            viewport={{ once: true }}
            className="absolute w-64 sm:w-80 h-24 rounded-2xl bg-white/15 border border-white/20 backdrop-blur-lg rotate-1"
          />

          {/* Capa 1 Superior (Tarjeta Kuota | VALT) */}
          <motion.div
            initial={{ opacity: 0, y: -24 }}
            whileInView={{ opacity: 1, y: -16 }}
            viewport={{ once: true }}
            transition={{ type: 'spring', damping: 20 }}
            className="absolute w-72 sm:w-96 p-4 rounded-2xl bg-gradient-to-r from-[#0F172A] to-[#0A0F1D] border border-emerald-400/40 shadow-[0_0_40px_rgba(16,185,129,0.3)] flex items-center justify-between z-20"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-[#10B981] to-[#38BDF8] p-0.5 flex items-center justify-center">
                <div className="w-full h-full rounded-[10px] bg-[#070A0F] flex items-center justify-center">
                  <Sparkles className="w-5 h-5 text-[#8FD694]" />
                </div>
              </div>
              <div className="text-left">
                <div className="text-xs font-bold text-white tracking-wide flex items-center gap-1.5">
                  KUOTA B2B LINE
                  <span className="text-[9px] px-1.5 py-0.2 rounded bg-emerald-500/20 text-emerald-300 font-mono">
                    ACTIVA
                  </span>
                </div>
                <div className="text-[10px] text-slate-400 font-mono">
                  Línea disponible: S/ 50,000.00
                </div>
              </div>
            </div>
            <div className="text-right">
              <span className="text-[10px] font-mono font-bold text-emerald-400 block">
                by VALT
              </span>
              <span className="text-[9px] text-slate-500">T&C Aplican</span>
            </div>
          </motion.div>
        </div>

        {/* CTA Central */}
        <div className="pt-6 flex flex-col items-center gap-3">
          <button
            onClick={onOpenEvaluationModal}
            className="inline-flex items-center justify-center px-9 py-4 rounded-full text-sm sm:text-base font-bold bg-white text-[#070A0F] hover:bg-[#8FD694] hover:text-[#070A0F] hover:scale-105 active:scale-95 transition-all shadow-[0_0_35px_rgba(255,255,255,0.25)] group"
          >
            <span>Solicitar Línea Kuota</span>
            <ArrowRight className="w-4 h-4 ml-2 group-hover:translate-x-1 transition-transform" />
          </button>
          <span className="text-[11px] text-slate-400 font-medium">
            Sin papeleos ni costo de apertura · Respuesta en minutos
          </span>
        </div>
      </div>

      {/* FOOTER CORPORATIVO (3 COLUMNAS) */}
      <div className="border-t border-white/10 bg-[#030508] py-14">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Columna Marca */}
          <div className="space-y-4">
            <div className="flex items-center gap-2.5">
              <KuotaLogo variant="inverted" className="h-7 w-auto object-contain" />
              <span className="text-[10px] uppercase font-bold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded-full border border-emerald-500/20">
                by VALT
              </span>
            </div>
            <p className="text-xs text-slate-400 leading-relaxed">
              El Sistema Operativo de Compras e Inteligencia Predictiva que conecta a comercios con sus proveedores mediante esquemas de financiamiento en cuotas.
            </p>
            <div className="flex items-center gap-2 text-xs text-slate-300">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
              <span>Sistemas 99.99% Operativos</span>
            </div>
          </div>

          {/* Columna 1: Transparencia */}
          <div className="space-y-3">
            <h4 className="text-xs font-bold text-white uppercase tracking-wider">
              Transparencia & Legal
            </h4>
            <ul className="space-y-2 text-xs text-slate-400">
              <li>
                <a href="#transparencia" className="hover:text-white transition-colors">
                  Términos y Condiciones
                </a>
              </li>
              <li>
                <a href="#transparencia" className="hover:text-white transition-colors">
                  Política de Privacidad
                </a>
              </li>
              <li>
                <a href="#transparencia" className="hover:text-white transition-colors">
                  Seguridad de Datos Comerciales
                </a>
              </li>
              <li>
                <a href="#transparencia" className="hover:text-white transition-colors">
                  Ley N° 29733 (Datos Personales)
                </a>
              </li>
              <li>
                <a href="#transparencia" className="hover:text-white transition-colors">
                  Acuerdo de Servicio (SLA)
                </a>
              </li>
            </ul>
          </div>

          {/* Columna 2: Plataforma Kuota */}
          <div className="space-y-3">
            <h4 className="text-xs font-bold text-white uppercase tracking-wider">
              Plataforma & Ecosistema
            </h4>
            <ul className="space-y-2 text-xs text-slate-400">
              <li>
                <a href="#plataforma" className="hover:text-white transition-colors">
                  Cerebro de Compras Nexo IA
                </a>
              </li>
              <li>
                <a href="#niveles" className="hover:text-white transition-colors">
                  Niveles y Líneas de Capital
                </a>
              </li>
              <li>
                <a href="#como-empezar" className="hover:text-white transition-colors">
                  Integraciones (Shopify, SAP, Excel)
                </a>
              </li>
              <li>
                <a href="#historias" className="hover:text-white transition-colors">
                  Casos de Éxito y Testimonios
                </a>
              </li>
              <li>
                <a href="#blog" className="hover:text-white transition-colors">
                  Radar B2B Blog
                </a>
              </li>
            </ul>
          </div>

          {/* Columna 3: Contacto 24/7 */}
          <div className="space-y-3">
            <h4 className="text-xs font-bold text-white uppercase tracking-wider">
              Canales de Contacto
            </h4>
            <ul className="space-y-2 text-xs text-slate-400">
              <li>
                <a
                  href="https://wa.me/51981000000"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="hover:text-emerald-400 transition-colors flex items-center gap-1.5"
                >
                  <span>WhatsApp Ejecutivo 24/7</span>
                </a>
              </li>
              <li>
                <a href="mailto:hola@kuota.pe" className="hover:text-white transition-colors">
                  hola@kuota.pe
                </a>
              </li>
              <li>
                <span className="text-slate-500">Lima, Perú · Cobertura a nivel nacional</span>
              </li>
              <li className="pt-2">
                <a
                  href="#transparencia"
                  className="inline-block text-[11px] text-slate-400 hover:text-white border border-white/10 px-2.5 py-1 rounded-lg"
                >
                  Libro de Reclamaciones Virtual
                </a>
              </li>
            </ul>
          </div>
        </div>

        {/* Barra Inferior */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-12 pt-6 border-t border-white/5 flex flex-col sm:flex-row items-center justify-between text-[11px] text-slate-400 gap-3">
          <div>
            © 2026 KUOTA Technologies Inc. Todos los derechos reservados. Con el respaldo de VALT.
          </div>
          <div className="flex items-center gap-4">
            <span>Encriptación TLS 1.3</span>
            <span>·</span>
            <span>Cloud AWS / GCP</span>
            <span>·</span>
            <span>Hecho con orgullo en Perú</span>
          </div>
        </div>
      </div>
    </footer>
  );
};

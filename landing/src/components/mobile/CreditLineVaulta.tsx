import React, { useState } from 'react';
import {
  CreditCard,
  TrendingUp,
  ArrowRight,
  ShieldCheck,
  CheckCircle2,
  FileText,
  Sparkles,
  Link as LinkIcon,
  Zap,
  RotateCw,
  Plus,
  Wifi,
  ChevronRight,
  Calculator,
  Lock,
} from 'lucide-react';

interface CreditLineVaultaProps {
  onOpenKuotear: () => void;
  onOpenCollect: () => void;
  onNavigateToTab: (tab: string) => void;
}

export const CreditLineVaulta: React.FC<CreditLineVaultaProps> = ({
  onOpenKuotear,
  onOpenCollect,
  onNavigateToTab,
}) => {
  const [autoDebitEnabled, setAutoDebitEnabled] = useState(true);
  const [showStatementModal, setShowStatementModal] = useState(false);

  return (
    <div className="w-full bg-[#0B0F19] text-white min-h-full pb-24 font-sans select-none">
      {/* ======================================================== */}
      {/* 1. CABECERA & TITULAR ESTILO VAULTA */}
      {/* ======================================================== */}
      <div className="px-5 pt-7 pb-4 flex items-center justify-between">
        <div>
          <span className="text-[11px] font-bold text-sky-400 uppercase tracking-wider">
            Financiamiento Respaldado en Ventas
          </span>
          <h1 className="text-2xl font-black text-white tracking-tight">
            Línea Kuota B2B
          </h1>
        </div>

        <button
          type="button"
          onClick={onOpenKuotear}
          aria-label="Nueva operación de kuoteo"
          className="w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 border border-white/20 flex items-center justify-center text-white transition-all cursor-pointer"
        >
          <Plus className="w-5 h-5" />
        </button>
      </div>

      <div className="px-5 space-y-6">
        {/* ======================================================== */}
        {/* 2. TARJETA ESMERILADA PRINCIPAL (Patrón Vaulta Card) */}
        {/* ======================================================== */}
        <div className="relative overflow-hidden rounded-[28px] p-6 text-white border border-white/25 shadow-2xl bg-gradient-to-br from-[#1E40AF]/90 via-[#2563EB]/80 to-[#1D4ED8]/90 backdrop-blur-xl">
          {/* Acento orbital de luz */}
          <div className="absolute -top-20 -right-20 w-52 h-52 rounded-full bg-white/20 blur-3xl pointer-events-none" />
          <div className="absolute -bottom-20 -left-20 w-52 h-52 rounded-full bg-[#60A5FA]/30 blur-3xl pointer-events-none" />

          <div className="relative z-10 flex flex-col justify-between h-44">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="w-7 h-7 rounded-lg bg-white text-[#1E40AF] font-black flex items-center justify-center text-xs shadow-sm">
                  Q
                </div>
                <span className="font-extrabold tracking-wider text-sm">KUOTA B2B</span>
              </div>
              <Wifi className="w-5 h-5 text-white/80 rotate-90" />
            </div>

            {/* Número de Tarjeta Comercial */}
            <div className="space-y-1">
              <span className="text-[10px] uppercase font-bold text-white/60 tracking-widest">
                Línea Dinámica Revolvente
              </span>
              <p className="text-lg sm:text-xl font-mono font-bold tracking-widest text-white/95">
                4218 •••• •••• 9084
              </p>
            </div>

            {/* Titular y Sello */}
            <div className="flex items-end justify-between">
              <div>
                <span className="block text-[9px] uppercase font-bold text-white/60 tracking-wider">
                  Titular Autorizado
                </span>
                <span className="text-xs sm:text-sm font-extrabold text-white tracking-wide">
                  BODEGA DON LUCHO
                </span>
              </div>

              <div className="text-right">
                <span className="inline-block px-2 py-0.5 rounded-full bg-emerald-400/20 border border-emerald-300/40 text-[9px] font-extrabold text-emerald-200">
                  ✓ Respaldado en Ventas
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* ======================================================== */}
        {/* 3. BOTONERA CIRCULAR DE CONTROL (4 Botones Azules Vaulta) */}
        {/* ======================================================== */}
        <div className="space-y-2">
          <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider px-1">
            Control de Línea y Cobros
          </h3>
          <div className="grid grid-cols-4 gap-2 text-center">
            {/* Botón 1: Kuotear Factura */}
            <button
              type="button"
              onClick={onOpenKuotear}
              className="flex flex-col items-center gap-1.5 p-2 rounded-2xl hover:bg-white/5 transition-all cursor-pointer group active:scale-95"
            >
              <div className="w-12 h-12 rounded-full bg-[#2563EB] group-hover:bg-[#1D4ED8] text-white flex items-center justify-center shadow-md shadow-blue-500/20 transition-all">
                <RotateCw className="w-5 h-5" />
              </div>
              <span className="text-[10px] font-bold text-slate-300 leading-tight">
                Kuotear<br />Factura
              </span>
            </button>

            {/* Botón 2: Subir Línea */}
            <button
              type="button"
              onClick={() => onNavigateToTab('ventas')}
              className="flex flex-col items-center gap-1.5 p-2 rounded-2xl hover:bg-white/5 transition-all cursor-pointer group active:scale-95"
            >
              <div className="w-12 h-12 rounded-full bg-[#2563EB] group-hover:bg-[#1D4ED8] text-white flex items-center justify-center shadow-md shadow-blue-500/20 transition-all">
                <TrendingUp className="w-5 h-5" />
              </div>
              <span className="text-[10px] font-bold text-slate-300 leading-tight">
                Subir<br />Línea
              </span>
            </button>

            {/* Botón 3: Crear Link */}
            <button
              type="button"
              onClick={onOpenCollect}
              className="flex flex-col items-center gap-1.5 p-2 rounded-2xl hover:bg-white/5 transition-all cursor-pointer group active:scale-95"
            >
              <div className="w-12 h-12 rounded-full bg-[#2563EB] group-hover:bg-[#1D4ED8] text-white flex items-center justify-center shadow-md shadow-blue-500/20 transition-all">
                <LinkIcon className="w-5 h-5" />
              </div>
              <span className="text-[10px] font-bold text-slate-300 leading-tight">
                Crear<br />Link
              </span>
            </button>

            {/* Botón 4: Débito Automático */}
            <button
              type="button"
              onClick={() => setAutoDebitEnabled(!autoDebitEnabled)}
              className="flex flex-col items-center gap-1.5 p-2 rounded-2xl hover:bg-white/5 transition-all cursor-pointer group active:scale-95"
            >
              <div
                className={`w-12 h-12 rounded-full flex items-center justify-center transition-all ${
                  autoDebitEnabled
                    ? 'bg-[#10B981] shadow-md shadow-emerald-500/20 text-white'
                    : 'bg-slate-700 text-slate-400'
                }`}
              >
                <Zap className="w-5 h-5" />
              </div>
              <span className="text-[10px] font-bold text-slate-300 leading-tight">
                Débito<br />{autoDebitEnabled ? 'Activo' : 'Pausa'}
              </span>
            </button>
          </div>
        </div>

        {/* ======================================================== */}
        {/* 4. TARJETAS GEMELAS DE RENDIMIENTO MENSUAL (Vaulta This Month Uses) */}
        {/* ======================================================== */}
        <div className="grid grid-cols-2 gap-3">
          {/* Tarjeta 1: Ventas del Mes vs Meta */}
          <div className="rounded-[24px] bg-[#111827] border border-[#1E293B] p-4 space-y-3 shadow-lg">
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">
                Ventas del Mes
              </span>
              <span className="px-2 py-0.5 rounded-full bg-[#2563EB]/20 border border-[#2563EB]/40 text-[10px] font-extrabold text-sky-300">
                50%
              </span>
            </div>

            <div>
              <p className="text-base sm:text-lg font-black text-white tabular-nums">
                S/ 18,000 <span className="text-xs text-slate-400 font-normal">/ 36,000</span>
              </p>
              <p className="text-[10px] text-slate-400 mt-0.5">
                Meta para Nivel 4 de Crédito
              </p>
            </div>

            {/* Barra de progreso bicolor */}
            <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
              <div className="h-full rounded-full bg-gradient-to-r from-[#2563EB] to-sky-400 w-1/2 transition-all duration-500" />
            </div>
          </div>

          {/* Tarjeta 2: Línea Utilizada vs Cupo */}
          <div className="rounded-[24px] bg-[#111827] border border-[#1E293B] p-4 space-y-3 shadow-lg">
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">
                Línea Utilizada
              </span>
              <span className="px-2 py-0.5 rounded-full bg-amber-400/20 border border-amber-300/40 text-[10px] font-extrabold text-amber-300">
                80%
              </span>
            </div>

            <div>
              <p className="text-base sm:text-lg font-black text-white tabular-nums">
                S/ 2,880 <span className="text-xs text-slate-400 font-normal">/ 3,600</span>
              </p>
              <p className="text-[10px] text-slate-400 mt-0.5">
                S/ 720 disponibles para hoy
              </p>
            </div>

            {/* Barra de progreso */}
            <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
              <div className="h-full rounded-full bg-gradient-to-r from-amber-500 to-amber-300 w-4/5 transition-all duration-500" />
            </div>
          </div>
        </div>

        {/* ======================================================== */}
        {/* 5. ACCESO A EXTRACTO COMERCIAL & CONCILIACIÓN */}
        {/* ======================================================== */}
        <div className="rounded-[24px] bg-[#111827] border border-[#1E293B] p-4 flex items-center justify-between gap-3 shadow-lg">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-white/5 border border-white/10 flex items-center justify-center text-sky-400 shrink-0">
              <FileText className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs sm:text-sm font-bold text-white">
                Reporte de Ventas y Conciliación
              </h4>
              <p className="text-[11px] text-slate-400 mt-0.5">
                Comisión 2% asumida por el proveedor
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={() => setShowStatementModal(true)}
            className="px-4 py-2 rounded-xl bg-white/10 hover:bg-white/20 text-white font-bold text-xs border border-white/15 transition-all cursor-pointer"
          >
            Ver
          </button>
        </div>
      </div>

      {/* Modal de Reporte */}
      {showStatementModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md">
          <div className="w-full max-w-sm rounded-[28px] bg-[#111827] border border-[#1E293B] p-6 text-white space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="font-extrabold text-sm">Estado de Cuenta Kuota B2B</h3>
              <button
                type="button"
                onClick={() => setShowStatementModal(false)}
                className="text-slate-400 hover:text-white text-xs font-bold"
              >
                Cerrar
              </button>
            </div>

            <div className="space-y-2.5 text-xs">
              <div className="flex justify-between text-slate-400">
                <span>Ventas totales verificadas:</span>
                <span className="font-bold text-white">S/ 124,892.65</span>
              </div>
              <div className="flex justify-between text-slate-400">
                <span>Línea crediticia otorgada:</span>
                <span className="font-bold text-white">S/ 9,000.00</span>
              </div>
              <div className="flex justify-between text-slate-400">
                <span>Comisión asumida por proveedores:</span>
                <span className="font-bold text-emerald-400">2.0% transparente</span>
              </div>
              <div className="flex justify-between text-slate-400">
                <span>Riesgo de impago para tu negocio:</span>
                <span className="font-bold text-emerald-400">0% cubierto por Kuota</span>
              </div>
            </div>

            <button
              type="button"
              onClick={() => setShowStatementModal(false)}
              className="w-full py-2.5 rounded-xl bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-bold text-xs"
            >
              Descargar comprobante en PDF
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

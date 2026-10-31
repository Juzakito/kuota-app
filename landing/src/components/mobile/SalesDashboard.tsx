import React, { useState } from 'react';
import {
  Eye,
  EyeOff,
  Search,
  Bell,
  ArrowDownLeft,
  ArrowUpRight,
  QrCode,
  Sparkles,
  Link as LinkIcon,
  Receipt,
  TrendingUp,
  CreditCard,
  ChevronRight,
  Zap,
} from 'lucide-react';

interface SalesDashboardProps {
  onOpenKuotear: () => void;
  onOpenCollect: () => void;
  onOpenQr: () => void;
  onNavigateToTab: (tab: string) => void;
}

export const SalesDashboard: React.FC<SalesDashboardProps> = ({
  onOpenKuotear,
  onOpenCollect,
  onOpenQr,
  onNavigateToTab,
}) => {
  const [balanceVisible, setBalanceVisible] = useState(true);
  const [activeBalanceType, setActiveBalanceType] = useState<'ventas' | 'linea'>('ventas');
  const [activeFilter, setActiveFilter] = useState<'todas' | 'kuotas' | 'links'>('todas');

  // Transacciones en tiempo real fieles a Oliver Bennet & Ethan Carter
  const transactions = [
    {
      id: 'tx-1',
      type: 'inflow',
      title: 'Venta Yape - Cliente Minorista',
      date: 'Hoy, 12:07 PM',
      amount: '+ S/ 200.00',
      badge: 'Venta Verificada',
      category: 'ventas',
    },
    {
      id: 'tx-2',
      type: 'inflow',
      title: 'Link de Pago - Pedido Bodega #104',
      date: 'Hoy, 10:45 AM',
      amount: '+ S/ 580.00',
      badge: 'Cobro aKuotas',
      category: 'links',
    },
    {
      id: 'tx-3',
      type: 'outflow',
      title: 'Distribuidora Alicorp - Kuota 2/4',
      date: 'Vence 18 Sep · Quedan 2 días',
      amount: '- S/ 347.00',
      badge: 'Proveedor B2B',
      category: 'kuotas',
    },
    {
      id: 'tx-4',
      type: 'inflow',
      title: 'Venta QR Plin - Mostrador Central',
      date: 'Ayer, 06:30 PM',
      amount: '+ S/ 320.00',
      badge: 'Venta Verificada',
      category: 'ventas',
    },
    {
      id: 'tx-5',
      type: 'outflow',
      title: 'Mayorista Gloria - Kuota 1/3',
      date: 'Vence 22 Sep · Quedan 6 días',
      amount: '- S/ 420.00',
      badge: 'Proveedor B2B',
      category: 'kuotas',
    },
  ];

  const filteredTransactions = transactions.filter((tx) => {
    if (activeFilter === 'todas') return true;
    if (activeFilter === 'kuotas') return tx.type === 'outflow';
    if (activeFilter === 'links') return tx.category === 'links';
    return true;
  });

  return (
    <div className="w-full bg-[#F4F7FB] min-h-full pb-24 text-slate-800 font-sans select-none">
      {/* ======================================================== */}
      {/* 1. CABECERA AZUL REAL & BALANCE INMERSIVO (Ethan Carter / Link Bank) */}
      {/* ======================================================== */}
      <div className="relative overflow-hidden bg-gradient-to-b from-[#1E3A8A] via-[#2563EB] to-[#3B82F6] px-5 pt-7 pb-8 text-white rounded-b-[36px] shadow-xl">
        {/* Glow atmosférico suave */}
        <div className="absolute -top-16 -right-16 w-60 h-60 rounded-full bg-[#60A5FA]/30 blur-3xl pointer-events-none" />
        <div className="absolute -bottom-16 -left-16 w-60 h-60 rounded-full bg-[#1D4ED8]/40 blur-3xl pointer-events-none" />

        <div className="relative z-10 space-y-6">
          {/* Barra Superior: Perfil + Avatar + Buscador y Notificaciones */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 rounded-full bg-white/20 backdrop-blur-md border border-white/30 flex items-center justify-center font-black text-white text-base shadow-sm ring-2 ring-white/20">
                DL
              </div>
              <div>
                <p className="text-[11px] text-white/80 font-medium">Buenos días 👋</p>
                <h2 className="text-base font-extrabold text-white tracking-tight leading-tight">
                  Bodega Don Lucho
                </h2>
              </div>
            </div>

            <div className="flex items-center gap-2">
              <button
                type="button"
                aria-label="Buscar ventas"
                className="w-10 h-10 rounded-full bg-white/15 hover:bg-white/25 backdrop-blur-md border border-white/25 flex items-center justify-center text-white transition-all cursor-pointer"
              >
                <Search className="w-4 h-4" />
              </button>
              <button
                type="button"
                aria-label="Notificaciones"
                className="relative w-10 h-10 rounded-full bg-white/15 hover:bg-white/25 backdrop-blur-md border border-white/25 flex items-center justify-center text-white transition-all cursor-pointer"
              >
                <Bell className="w-4 h-4" />
                <span className="absolute top-2 right-2 w-2 h-2 rounded-full bg-[#10B981] ring-2 ring-[#2563EB]" />
              </button>
            </div>
          </div>

          {/* Métrica Principal con Selector de Vista (Ventas vs Línea) */}
          <div className="space-y-1.5 pt-1">
            <div className="flex items-center justify-between">
              <div className="inline-flex items-center gap-1.5 p-1 rounded-full bg-white/15 backdrop-blur-md border border-white/20">
                <button
                  type="button"
                  onClick={() => setActiveBalanceType('ventas')}
                  className={`px-3 py-1 rounded-full text-[11px] font-bold transition-all cursor-pointer ${
                    activeBalanceType === 'ventas'
                      ? 'bg-white text-[#1E3A8A] shadow-sm'
                      : 'text-white/80 hover:text-white'
                  }`}
                >
                  Ventas del Mes
                </button>
                <button
                  type="button"
                  onClick={() => setActiveBalanceType('linea')}
                  className={`px-3 py-1 rounded-full text-[11px] font-bold transition-all cursor-pointer ${
                    activeBalanceType === 'linea'
                      ? 'bg-white text-[#1E3A8A] shadow-sm'
                      : 'text-white/80 hover:text-white'
                  }`}
                >
                  Línea Kuota
                </button>
              </div>

              <button
                type="button"
                onClick={() => setBalanceVisible(!balanceVisible)}
                className="p-2 rounded-full bg-white/10 hover:bg-white/20 text-white/90 transition-colors cursor-pointer"
              >
                {balanceVisible ? <Eye className="w-4 h-4" /> : <EyeOff className="w-4 h-4" />}
              </button>
            </div>

            {/* Gran Cifra en Blanco con Tabular Nums */}
            <div className="pt-1">
              <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight tabular-nums text-white">
                {balanceVisible ? (
                  activeBalanceType === 'ventas' ? (
                    <>
                      S/ 124,892<span className="text-white/70">.65</span>
                    </>
                  ) : (
                    <>
                      S/ 9,000<span className="text-white/70">.00</span>
                    </>
                  )
                ) : (
                  'S/ ••••••••'
                )}
              </h1>
              <p className="text-xs font-semibold text-emerald-300 flex items-center gap-1.5 mt-1">
                <span className="inline-block px-1.5 py-0.5 rounded-full bg-emerald-400/20 border border-emerald-300/40 text-[10px] font-bold text-emerald-200">
                  +8.42% Hoy
                </span>
                <span className="text-white/80">(+S/ 9,684.20 en ventas registradas)</span>
              </p>
            </div>
          </div>

          {/* Tarjetas Gemelas de Cristal Esmerilado (Link Bank / Vaulta) */}
          <div className="grid grid-cols-2 gap-3 pt-1">
            <div className="rounded-2xl bg-white/15 hover:bg-white/20 backdrop-blur-md border border-white/25 p-3.5 transition-all">
              <span className="block text-[10px] font-bold uppercase tracking-wider text-white/75">
                Ventas Yape / QR
              </span>
              <p className="text-base sm:text-lg font-black text-white mt-1 tabular-nums">
                {balanceVisible ? 'S/ 14,127.00' : 'S/ •••••'}
              </p>
              <span className="block text-[9px] font-medium text-emerald-300 mt-0.5">
                100% verificadas
              </span>
            </div>

            <div className="rounded-2xl bg-white/15 hover:bg-white/20 backdrop-blur-md border border-white/25 p-3.5 transition-all">
              <span className="block text-[10px] font-bold uppercase tracking-wider text-white/75">
                Línea para Kuotear
              </span>
              <p className="text-base sm:text-lg font-black text-white mt-1 tabular-nums">
                {balanceVisible ? 'S/ 9,000.00' : 'S/ •••••'}
              </p>
              <span className="block text-[9px] font-medium text-sky-200 mt-0.5">
                Generada por ventas
              </span>
            </div>
          </div>

          {/* Fila de 3 Botones de Acción Inmediata (Estilo Ethan Carter) */}
          <div className="grid grid-cols-12 gap-2.5 pt-1">
            <button
              type="button"
              onClick={onOpenCollect}
              className="col-span-5 py-3 rounded-full bg-white hover:bg-slate-100 text-[#1E3A8A] font-extrabold text-xs sm:text-sm flex items-center justify-center gap-1.5 shadow-md active:scale-95 transition-all cursor-pointer"
            >
              <span className="text-base font-black">+</span>
              <span>Cobrar Venta</span>
            </button>

            <button
              type="button"
              onClick={onOpenKuotear}
              className="col-span-5 py-3 rounded-full bg-white hover:bg-slate-100 text-[#1E3A8A] font-extrabold text-xs sm:text-sm flex items-center justify-center gap-1.5 shadow-md active:scale-95 transition-all cursor-pointer"
            >
              <span className="text-base font-black">⇅</span>
              <span>Kuotear Pago</span>
            </button>

            <button
              type="button"
              onClick={onOpenQr}
              aria-label="Abrir lector QR"
              className="col-span-2 py-3 rounded-2xl bg-[#0F172A] hover:bg-black text-white flex items-center justify-center shadow-md active:scale-95 transition-all cursor-pointer"
            >
              <QrCode className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>

      <div className="px-4 sm:px-6 space-y-5 -mt-2">
        {/* ======================================================== */}
        {/* 2. TARJETA INTELIGENTE "NEXO · IMPULSOR DE VENTAS" (Bill Negotiator) */}
        {/* ======================================================== */}
        <div className="rounded-[24px] bg-white border border-slate-200/90 p-5 shadow-lg relative overflow-hidden">
          <div className="flex items-center gap-2 mb-3">
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-sky-50 border border-sky-200 text-[11px] font-bold text-[#2563EB]">
              <Sparkles className="w-3.5 h-3.5 text-[#2563EB]" />
              Nexo · Impulsor de Ventas
            </span>
          </div>

          <div className="space-y-2">
            <p className="text-xs sm:text-sm text-slate-700 leading-relaxed">
              Tus ventas por Yape crecieron un <strong className="text-[#2563EB]">22%</strong> esta semana.
              Desbloqueaste <strong className="text-emerald-700 font-extrabold">+S/ 4,500</strong> de línea para pagar a tus proveedores con 0% de descapitalización.
            </p>

            <button
              type="button"
              onClick={onOpenKuotear}
              className="w-full mt-2 py-2.5 px-4 rounded-xl bg-slate-50 hover:bg-sky-50 text-[#2563EB] font-bold text-xs flex items-center justify-center gap-2 border border-slate-200 hover:border-sky-300 transition-all cursor-pointer"
            >
              <span>Kuotear pago a proveedor</span>
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* ======================================================== */}
        {/* 3. BANNER DE CRECIMIENTO DE CUENTA (Upgrade Account) */}
        {/* ======================================================== */}
        <div className="rounded-2xl bg-amber-50/80 border border-amber-200/90 p-3.5 flex items-start gap-3">
          <div className="w-8 h-8 rounded-xl bg-amber-500 text-white flex items-center justify-center shrink-0 shadow-xs">
            <Zap className="w-4 h-4" />
          </div>
          <div className="text-xs">
            <p className="font-bold text-amber-950">Aumenta tu Línea de Crédito</p>
            <p className="text-amber-800 font-normal mt-0.5 leading-snug">
              Conecta todos tus medios de pago (Yape, POS, Transferencias) para duplicar tu capacidad de compra.
            </p>
          </div>
        </div>

        {/* ======================================================== */}
        {/* 4. GRILLA DE ACCIONES RÁPIDAS (Quick Actions) */}
        {/* ======================================================== */}
        <div>
          <h3 className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-2.5 px-1">
            Acciones Rápidas B2B
          </h3>
          <div className="grid grid-cols-4 gap-2.5">
            <button
              type="button"
              onClick={onOpenCollect}
              className="p-3 rounded-2xl bg-[#EEF2FF] hover:bg-[#E0E7FF] border border-blue-100 flex flex-col items-center text-center gap-1.5 transition-all cursor-pointer active:scale-95"
            >
              <div className="w-9 h-9 rounded-xl bg-white text-[#2563EB] flex items-center justify-center shadow-xs">
                <LinkIcon className="w-4 h-4" />
              </div>
              <span className="text-[10px] font-bold text-slate-800 leading-tight">
                Links Pago
              </span>
            </button>

            <button
              type="button"
              onClick={onOpenKuotear}
              className="p-3 rounded-2xl bg-[#EEF2FF] hover:bg-[#E0E7FF] border border-blue-100 flex flex-col items-center text-center gap-1.5 transition-all cursor-pointer active:scale-95"
            >
              <div className="w-9 h-9 rounded-xl bg-white text-[#2563EB] flex items-center justify-center shadow-xs">
                <Receipt className="w-4 h-4" />
              </div>
              <span className="text-[10px] font-bold text-slate-800 leading-tight">
                Proveedores
              </span>
            </button>

            <button
              type="button"
              onClick={() => onNavigateToTab('linea')}
              className="p-3 rounded-2xl bg-[#EEF2FF] hover:bg-[#E0E7FF] border border-blue-100 flex flex-col items-center text-center gap-1.5 transition-all cursor-pointer active:scale-95"
            >
              <div className="w-9 h-9 rounded-xl bg-white text-[#2563EB] flex items-center justify-center shadow-xs">
                <TrendingUp className="w-4 h-4" />
              </div>
              <span className="text-[10px] font-bold text-slate-800 leading-tight">
                Metas Venta
              </span>
            </button>

            <button
              type="button"
              onClick={() => onNavigateToTab('linea')}
              className="p-3 rounded-2xl bg-[#EEF2FF] hover:bg-[#E0E7FF] border border-blue-100 flex flex-col items-center text-center gap-1.5 transition-all cursor-pointer active:scale-95"
            >
              <div className="w-9 h-9 rounded-xl bg-white text-[#2563EB] flex items-center justify-center shadow-xs">
                <CreditCard className="w-4 h-4" />
              </div>
              <span className="text-[10px] font-bold text-slate-800 leading-tight">
                Línea Kuota
              </span>
            </button>
          </div>
        </div>

        {/* ======================================================== */}
        {/* 5. VENTAS Y KUOTAS PROGRAMADAS (Bills & Payments) */}
        {/* ======================================================== */}
        <div className="space-y-3 pt-1">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-extrabold text-[#0A192F] tracking-tight">
              Ventas y Kuotas Programadas
            </h3>
            <button
              type="button"
              onClick={() => onNavigateToTab('ventas')}
              className="text-xs font-bold text-[#2563EB] hover:underline cursor-pointer"
            >
              Ver todas
            </button>
          </div>

          {/* Selector de Píldoras */}
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setActiveFilter('todas')}
              className={`px-3.5 py-1.5 rounded-full text-xs font-bold transition-all cursor-pointer ${
                activeFilter === 'todas'
                  ? 'bg-[#0F172A] text-white shadow-xs'
                  : 'bg-white text-slate-600 border border-slate-200'
              }`}
            >
              Todas las Ventas
            </button>
            <button
              type="button"
              onClick={() => setActiveFilter('kuotas')}
              className={`px-3.5 py-1.5 rounded-full text-xs font-bold transition-all cursor-pointer ${
                activeFilter === 'kuotas'
                  ? 'bg-[#0F172A] text-white shadow-xs'
                  : 'bg-white text-slate-600 border border-slate-200'
              }`}
            >
              Kuotas por Pagar
            </button>
            <button
              type="button"
              onClick={() => setActiveFilter('links')}
              className={`px-3.5 py-1.5 rounded-full text-xs font-bold transition-all cursor-pointer ${
                activeFilter === 'links'
                  ? 'bg-[#0F172A] text-white shadow-xs'
                  : 'bg-white text-slate-600 border border-slate-200'
              }`}
            >
              Links Activos
            </button>
          </div>

          {/* Lista de Transacciones Limpias */}
          <div className="space-y-2">
            {filteredTransactions.map((tx) => (
              <div
                key={tx.id}
                className="rounded-2xl bg-white border border-slate-200/80 p-3.5 flex items-center justify-between gap-3 shadow-xs hover:border-slate-300 transition-all"
              >
                <div className="flex items-center gap-3">
                  <div
                    className={`w-10 h-10 rounded-2xl flex items-center justify-center shrink-0 ${
                      tx.type === 'inflow'
                        ? 'bg-[#D1FAE5] text-[#10B981]'
                        : 'bg-[#FEE2E2] text-[#EF4444]'
                    }`}
                  >
                    {tx.type === 'inflow' ? (
                      <ArrowDownLeft className="w-5 h-5" />
                    ) : (
                      <ArrowUpRight className="w-5 h-5" />
                    )}
                  </div>
                  <div>
                    <h4 className="text-xs sm:text-sm font-bold text-slate-900 leading-tight">
                      {tx.title}
                    </h4>
                    <p className="text-[11px] text-slate-500 mt-0.5">{tx.date}</p>
                  </div>
                </div>

                <div className="text-right shrink-0">
                  <p
                    className={`text-xs sm:text-sm font-black tabular-nums ${
                      tx.type === 'inflow' ? 'text-[#10B981]' : 'text-slate-900'
                    }`}
                  >
                    {tx.amount}
                  </p>
                  <span
                    className={`inline-block text-[10px] font-bold px-2 py-0.5 rounded-full mt-0.5 ${
                      tx.type === 'inflow'
                        ? 'bg-emerald-50 text-emerald-800 border border-emerald-200/60'
                        : 'bg-slate-100 text-slate-700 border border-slate-200'
                    }`}
                  >
                    {tx.badge}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

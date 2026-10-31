import React, { useState } from 'react';
import {
  Home,
  Receipt,
  QrCode,
  CreditCard,
  User,
  RotateCw,
  Sparkles,
} from 'lucide-react';
import { SalesDashboard } from './SalesDashboard';
import { CreditLineVaulta } from './CreditLineVaulta';
import { KuotearModal } from './KuotearModal';
import { PaymentLinkModal } from './PaymentLinkModal';

export const MobileAppSimulator: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'inicio' | 'ventas' | 'kuotear' | 'linea' | 'cuenta'>('inicio');
  const [kuotearModalOpen, setKuotearModalOpen] = useState(false);
  const [collectModalOpen, setCollectModalOpen] = useState(false);

  return (
    <div className="w-full py-12 px-4 flex flex-col items-center justify-center bg-slate-100">
      {/* Banner Superior Explicativo */}
      <div className="max-w-md text-center mb-6">
        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100 text-[#1E3A8A] text-xs font-bold mb-2">
          <Sparkles className="w-3.5 h-3.5 text-[#2563EB]" />
          App Android Kuota 2026 · Enfoque 100% Ventas & Financiamiento
        </span>
        <h2 className="text-2xl font-black text-slate-900 tracking-tight">
          Haz que tus ventas financien tus compras
        </h2>
        <p className="text-xs text-slate-500 mt-1">
          Arquitectura visual inspirada en Ethan Carter, Link Bank y Vaulta.
        </p>
      </div>

      {/* Mockup del Teléfono */}
      <div className="relative w-full max-w-[390px] h-[844px] bg-black rounded-[52px] p-3 shadow-[0_25px_60px_-15px_rgba(0,0,0,0.35)] ring-1 ring-slate-800 flex flex-col overflow-hidden">
        {/* Isla Dinámica / Notch */}
        <div className="absolute top-5 left-1/2 -translate-x-1/2 w-28 h-7 bg-black rounded-full z-40 flex items-center justify-end px-3">
          <div className="w-2.5 h-2.5 rounded-full bg-[#111827] border border-slate-700" />
        </div>

        {/* Pantalla Interna */}
        <div className="relative w-full h-full bg-[#F4F7FB] rounded-[42px] overflow-hidden flex flex-col">
          {/* Contenido según pestaña */}
          <div className="flex-1 overflow-y-auto no-scrollbar">
            {activeTab === 'inicio' && (
              <SalesDashboard
                onOpenKuotear={() => setKuotearModalOpen(true)}
                onOpenCollect={() => setCollectModalOpen(true)}
                onOpenQr={() => setCollectModalOpen(true)}
                onNavigateToTab={(tab) => {
                  if (tab === 'linea') setActiveTab('linea');
                  if (tab === 'ventas') setActiveTab('ventas');
                }}
              />
            )}

            {activeTab === 'linea' && (
              <CreditLineVaulta
                onOpenKuotear={() => setKuotearModalOpen(true)}
                onOpenCollect={() => setCollectModalOpen(true)}
                onNavigateToTab={(tab) => {
                  if (tab === 'ventas') setActiveTab('ventas');
                  if (tab === 'inicio') setActiveTab('inicio');
                }}
              />
            )}

            {activeTab === 'ventas' && (
              <div className="p-6 pt-12 space-y-4">
                <h3 className="text-xl font-black text-[#0A192F]">Registro de Ventas</h3>
                <p className="text-xs text-slate-500">
                  Todas tus ventas ingresadas por Yape, QR, POS y Links de pago conectadas a tu línea de crédito.
                </p>
                <button
                  type="button"
                  onClick={() => setCollectModalOpen(true)}
                  className="w-full py-3 rounded-full bg-[#2563EB] text-white font-bold text-xs"
                >
                  + Generar Nuevo Link de Cobro
                </button>
              </div>
            )}

            {activeTab === 'kuotear' && (
              <div className="p-6 pt-12 space-y-4">
                <h3 className="text-xl font-black text-[#0A192F]">Kuotear Pagos a Proveedores</h3>
                <p className="text-xs text-slate-500">
                  Fracciona tus compras: 60% al inicio y el 40% restante en cuotas semanales respaldadas en tus ventas.
                </p>
                <button
                  type="button"
                  onClick={() => setKuotearModalOpen(true)}
                  className="w-full py-3 rounded-full bg-[#2563EB] text-white font-bold text-xs"
                >
                  Kuotear Factura Ahora
                </button>
              </div>
            )}

            {activeTab === 'cuenta' && (
              <div className="p-6 pt-12 space-y-4">
                <h3 className="text-xl font-black text-[#0A192F]">Cuenta y Medios Conectados</h3>
                <p className="text-xs text-slate-500">
                  Gestiona tus canales de recaudación (Yape, Plin, BCP, BBVA, Interbank) para aumentar tu límite crediticio.
                </p>
              </div>
            )}
          </div>

          {/* Barra de Navegación Inferior Limpia de 5 Pestañas (Design Token Fiel al PRD) */}
          <div className="w-full bg-white/95 backdrop-blur-md border-t border-slate-200 py-2.5 px-4 flex items-center justify-around z-30">
            <button
              type="button"
              onClick={() => setActiveTab('inicio')}
              className={`flex flex-col items-center gap-1 transition-all ${
                activeTab === 'inicio' ? 'text-[#2563EB]' : 'text-slate-400 hover:text-slate-600'
              }`}
            >
              <Home className="w-5 h-5" />
              <span className="text-[10px] font-bold">Inicio</span>
            </button>

            <button
              type="button"
              onClick={() => setActiveTab('ventas')}
              className={`flex flex-col items-center gap-1 transition-all ${
                activeTab === 'ventas' ? 'text-[#2563EB]' : 'text-slate-400 hover:text-slate-600'
              }`}
            >
              <Receipt className="w-5 h-5" />
              <span className="text-[10px] font-bold">Ventas</span>
            </button>

            {/* Botón Central Destacado: Kuotear */}
            <button
              type="button"
              onClick={() => {
                setActiveTab('kuotear');
                setKuotearModalOpen(true);
              }}
              className="flex flex-col items-center gap-1 -mt-4 text-[#2563EB]"
            >
              <div className="w-12 h-12 rounded-full bg-gradient-to-tr from-[#1E3A8A] to-[#2563EB] text-white flex items-center justify-center shadow-lg shadow-blue-500/30 ring-4 ring-white active:scale-95 transition-all">
                <RotateCw className="w-5 h-5" />
              </div>
              <span className="text-[10px] font-extrabold text-[#2563EB]">Kuotear</span>
            </button>

            <button
              type="button"
              onClick={() => setActiveTab('linea')}
              className={`flex flex-col items-center gap-1 transition-all ${
                activeTab === 'linea' ? 'text-[#2563EB]' : 'text-slate-400 hover:text-slate-600'
              }`}
            >
              <CreditCard className="w-5 h-5" />
              <span className="text-[10px] font-bold">Línea</span>
            </button>

            <button
              type="button"
              onClick={() => setActiveTab('cuenta')}
              className={`flex flex-col items-center gap-1 transition-all ${
                activeTab === 'cuenta' ? 'text-[#2563EB]' : 'text-slate-400 hover:text-slate-600'
              }`}
            >
              <User className="w-5 h-5" />
              <span className="text-[10px] font-bold">Cuenta</span>
            </button>
          </div>
        </div>

        {/* Modales */}
        <KuotearModal
          isOpen={kuotearModalOpen}
          onClose={() => setKuotearModalOpen(false)}
          onSuccess={() => setActiveTab('linea')}
        />

        <PaymentLinkModal
          isOpen={collectModalOpen}
          onClose={() => setCollectModalOpen(false)}
        />
      </div>
    </div>
  );
};

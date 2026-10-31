import React, { useState } from 'react';
import {
  X,
  Store,
  Calendar,
  CheckCircle2,
  ArrowRight,
  ShieldCheck,
  Building,
  DollarSign,
  AlertCircle,
} from 'lucide-react';

interface KuotearModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const KuotearModal: React.FC<KuotearModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [supplierName, setSupplierName] = useState('Distribuidora Alicorp S.A.A.');
  const [invoiceAmount, setInvoiceAmount] = useState<number>(3500);
  const [numWeeks, setNumWeeks] = useState<number>(4);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  if (!isOpen) return null;

  // Reglas oficiales PRD Kuota 2.0 (Modelo Cashea B2B)
  const downPaymentPercent = 50; // Nivel 1 Brote: 50% inicial, 3 cuotas semanales
  const downPayment = Math.round((invoiceAmount * downPaymentPercent) / 100);
  const financedAmount = invoiceAmount - downPayment;
  const kuotaFee = Math.round(invoiceAmount * 0.05); // 5% comisión plana comprador
  const totalToPay = invoiceAmount + kuotaFee;
  const weeklyInstallment = Math.round((financedAmount + kuotaFee) / numWeeks);
  const supplierFee = Math.round(invoiceAmount * 0.02); // 2% retenido por Kuota al comercio
  const supplierPayout48h = invoiceAmount - downPayment - supplierFee;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setTimeout(() => {
      setIsSubmitting(false);
      setSubmitted(true);
      setTimeout(() => {
        onSuccess();
        onClose();
        setSubmitted(false);
      }, 1800);
    }, 900);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/70 backdrop-blur-md">
      <div className="relative w-full max-w-md rounded-[32px] bg-white border border-slate-200 p-6 sm:p-7 shadow-2xl text-slate-800 overflow-hidden">
        {/* Glow sutil */}
        <div className="absolute -top-16 -right-16 w-48 h-48 bg-[#2563EB]/10 rounded-full blur-3xl pointer-events-none" />

        {/* Botón Cerrar */}
        <button
          type="button"
          onClick={onClose}
          className="absolute top-5 right-5 p-2 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-500 transition-colors cursor-pointer"
        >
          <X className="w-5 h-5" />
        </button>

        {!submitted ? (
          <div>
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-50 border border-blue-200 text-[11px] font-bold text-[#2563EB] mb-2">
              <Store className="w-3.5 h-3.5 text-[#2563EB]" />
              Financiamiento a Proveedores B2B
            </div>

            <h2 className="text-xl sm:text-2xl font-black text-[#0A192F] tracking-tight">
              Kuotear Pago de Factura
            </h2>
            <p className="text-xs text-slate-500 font-normal leading-relaxed mb-5">
              Paga tu cuota inicial directamente y Kuota financia el saldo a 0% de interés en cuotas semanales.
            </p>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Proveedor
                </label>
                <div className="relative">
                  <Building className="absolute left-3.5 top-3 w-4 h-4 text-slate-400" />
                  <input
                    type="text"
                    required
                    value={supplierName}
                    onChange={(e) => setSupplierName(e.target.value)}
                    className="w-full pl-10 pr-3 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs font-semibold text-slate-900 focus:outline-none focus:bg-white focus:border-[#2563EB]"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Monto Total de la Factura (PEN)
                </label>
                <div className="relative">
                  <span className="absolute left-3.5 top-2.5 text-xs font-bold text-slate-400">S/</span>
                  <input
                    type="number"
                    min={199}
                    step={50}
                    required
                    value={invoiceAmount}
                    onChange={(e) => setInvoiceAmount(Number(e.target.value) || 0)}
                    className="w-full pl-9 pr-3 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-sm font-black text-slate-900 focus:outline-none focus:bg-white focus:border-[#2563EB]"
                  />
                </div>
              </div>

              {/* Desglose Automático Modelo Cashea B2B Oficial Kuota */}
              <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200 space-y-2.5">
                <div className="flex items-center justify-between text-xs font-semibold">
                  <span className="text-slate-600">Cuota Inicial ({downPaymentPercent}% al contado):</span>
                  <span className="text-sm font-black text-[#0A192F]">S/ {downPayment.toLocaleString('es-PE')}</span>
                </div>
                <div className="flex items-center justify-between text-xs font-semibold">
                  <span className="text-slate-600">Saldo Financiado ({100 - downPaymentPercent}%):</span>
                  <span className="text-sm font-black text-[#2563EB]">S/ {financedAmount.toLocaleString('es-PE')}</span>
                </div>
                <div className="flex items-center justify-between text-xs font-semibold">
                  <span className="text-slate-600">Comisión Kuota (5% plana):</span>
                  <span className="text-xs font-bold text-slate-700">+S/ {kuotaFee.toLocaleString('es-PE')}</span>
                </div>
                <div className="pt-2 border-t border-slate-200 flex items-center justify-between text-xs">
                  <span className="text-slate-500 font-medium">{numWeeks} cuotas semanales de:</span>
                  <span className="font-extrabold text-[#10B981]">S/ {weeklyInstallment} / semana</span>
                </div>
                <div className="pt-1 flex items-center justify-between text-[11px] text-slate-400">
                  <span>Liquidación a proveedor en 48h (neto 98%):</span>
                  <span className="font-semibold text-slate-600">S/ {supplierPayout48h.toLocaleString('es-PE')}</span>
                </div>
              </div>

              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full py-3.5 rounded-full bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-extrabold text-xs sm:text-sm flex items-center justify-center gap-2 shadow-md hover:shadow-lg transition-all cursor-pointer active:scale-95 disabled:opacity-50"
              >
                {isSubmitting ? (
                  <span>Validando respaldo con ventas...</span>
                ) : (
                  <>
                    <span>Kuotear Pago Ahora</span>
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </form>

            <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-500">
              <span className="flex items-center gap-1.5 font-medium">
                <ShieldCheck className="w-3.5 h-3.5 text-[#10B981]" />
                0% riesgo de impago
              </span>
              <span className="font-medium text-slate-400">
                Liquidación 48h hábiles
              </span>
            </div>
          </div>
        ) : (
          <div className="py-8 text-center space-y-3">
            <div className="w-14 h-14 rounded-full bg-emerald-100 text-[#10B981] flex items-center justify-center mx-auto shadow-sm">
              <CheckCircle2 className="w-8 h-8" />
            </div>
            <h3 className="text-xl font-black text-[#0A192F]">¡Pago Kuoteado con Éxito!</h3>
            <p className="text-xs text-slate-600 max-w-xs mx-auto">
              Se emitió la conformidad a {supplierName}. Tu cuota inicial de S/ {downPayment} ha sido registrada y tus {numWeeks} cuotas semanales quedaron programadas en tu panel.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

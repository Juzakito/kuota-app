import React, { useState } from 'react';
import {
  X,
  Link as LinkIcon,
  Copy,
  Check,
  Share2,
  DollarSign,
  QrCode,
  Sparkles,
} from 'lucide-react';

interface PaymentLinkModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const PaymentLinkModal: React.FC<PaymentLinkModalProps> = ({
  isOpen,
  onClose,
}) => {
  const [concept, setConcept] = useState('Venta de Mercadería - Lote Minorista');
  const [amount, setAmount] = useState<number>(450);
  const [generatedLink, setGeneratedLink] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);

  if (!isOpen) return null;

  const handleGenerate = (e: React.FormEvent) => {
    e.preventDefault();
    const link = `https://kuota-ia.vercel.app/pay?id=lnk_${Math.random().toString(36).substr(2, 9)}&amt=${amount}`;
    setGeneratedLink(link);
  };

  const handleCopy = () => {
    if (generatedLink) {
      navigator.clipboard?.writeText(generatedLink);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/70 backdrop-blur-md">
      <div className="relative w-full max-w-md rounded-[32px] bg-white border border-slate-200 p-6 sm:p-7 shadow-2xl text-slate-800 overflow-hidden">
        <button
          type="button"
          onClick={() => {
            setGeneratedLink(null);
            onClose();
          }}
          className="absolute top-5 right-5 p-2 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-500 transition-colors cursor-pointer"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 border border-emerald-200 text-[11px] font-bold text-emerald-800 mb-2">
          <LinkIcon className="w-3.5 h-3.5 text-[#10B981]" />
          Cobro Multicanal B2B
        </div>

        <h2 className="text-xl sm:text-2xl font-black text-[#0A192F] tracking-tight">
          Crear Link de Pago con Cuotas
        </h2>
        <p className="text-xs text-slate-500 font-normal leading-relaxed mb-5">
          Permite a tus clientes pagar al contado o en cuotas mientras tú cobras con la comisión más baja del mercado (1%).
        </p>

        {!generatedLink ? (
          <form onSubmit={handleGenerate} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                Concepto de Venta
              </label>
              <input
                type="text"
                required
                value={concept}
                onChange={(e) => setConcept(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs font-semibold text-slate-900 focus:outline-none focus:bg-white focus:border-[#2563EB]"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                Monto a Cobrar (PEN)
              </label>
              <div className="relative">
                <span className="absolute left-3.5 top-2.5 text-xs font-bold text-slate-400">S/</span>
                <input
                  type="number"
                  min={10}
                  step={10}
                  required
                  value={amount}
                  onChange={(e) => setAmount(Number(e.target.value) || 0)}
                  className="w-full pl-9 pr-3 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-sm font-black text-slate-900 focus:outline-none focus:bg-white focus:border-[#2563EB]"
                />
              </div>
            </div>

            <button
              type="submit"
              className="w-full py-3.5 rounded-full bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-extrabold text-xs sm:text-sm shadow-md hover:shadow-lg transition-all cursor-pointer active:scale-95"
            >
              Generar Link de Cobro
            </button>
          </form>
        ) : (
          <div className="space-y-4 pt-2">
            <div className="p-3.5 rounded-2xl bg-slate-50 border border-slate-200 break-all text-xs font-mono text-[#2563EB]">
              {generatedLink}
            </div>

            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                onClick={handleCopy}
                className="py-3 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold text-xs flex items-center justify-center gap-1.5 transition-all cursor-pointer"
              >
                {copied ? <Check className="w-4 h-4 text-emerald-600" /> : <Copy className="w-4 h-4" />}
                <span>{copied ? '¡Copiado!' : 'Copiar Link'}</span>
              </button>

              <button
                type="button"
                onClick={() => {
                  if (navigator.share) {
                    navigator.share({ title: concept, url: generatedLink });
                  } else {
                    handleCopy();
                  }
                }}
                className="py-3 rounded-xl bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-bold text-xs flex items-center justify-center gap-1.5 transition-all cursor-pointer"
              >
                <Share2 className="w-4 h-4" />
                <span>Compartir WhatsApp</span>
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

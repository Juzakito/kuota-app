'use client';

import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { MessageSquare, X, Sparkles, ShieldCheck, CheckCircle2, ArrowRight, Building, Phone, User } from 'lucide-react';
import { submitMerchantLead, OFFICIAL_CONTACT } from '../../utils/leadsService';

interface FloatingLeadWidgetsProps {
  isOpen: boolean;
  onClose: () => void;
  onOpen: () => void;
}

export const FloatingLeadWidgets: React.FC<FloatingLeadWidgetsProps> = ({
  isOpen,
  onClose,
  onOpen,
}) => {
  const [docNumber, setDocNumber] = useState('');
  const [contactName, setContactName] = useState('');
  const [phone, setPhone] = useState('');
  const [businessType, setBusinessType] = useState('Bodega / Minimarket');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [whatsappRedirectUrl, setWhatsappRedirectUrl] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!docNumber || !phone) return;

    setIsSubmitting(true);
    try {
      const result = await submitMerchantLead({
        businessName: contactName || `Empresa ${docNumber}`,
        contactName: contactName || 'Gerente / Propietario',
        businessType: businessType,
        city: 'Lima',
        phone: phone,
        email: `${phone.replace(/\D/g, '')}@kuota-lead.pe`,
        message: `Solicitud de línea rápida Kuota B2B. Documento: ${docNumber}. Rubro: ${businessType}.`,
      });

      setWhatsappRedirectUrl(result.whatsappUrl);
      setIsSuccess(true);
    } catch (err) {
      console.error('Error submitting lead:', err);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReset = () => {
    setIsSuccess(false);
    setDocNumber('');
    setPhone('');
    setContactName('');
    onClose();
  };

  return (
    <>
      {/* 1. BOTÓN CÁPSULA VERDE PERSISTENTE (INFERIOR DERECHA) */}
      <div className="fixed bottom-6 right-6 z-40 flex items-center gap-2">
        <button
          onClick={onOpen}
          aria-label="Kuotear pedido o solicitar ayuda"
          className="bg-[#10B981] hover:bg-[#059669] text-white font-bold rounded-full px-5 py-3.5 shadow-2xl flex items-center gap-2.5 transition-all hover:scale-105 active:scale-95 group focus:outline-none"
        >
          <div className="relative">
            <MessageSquare className="w-5 h-5 fill-current" />
            <span className="absolute -top-1 -right-1 w-2 h-2 rounded-full bg-white animate-ping" />
          </div>
          <span className="text-xs sm:text-sm tracking-tight">Kuotear pedido / Ayuda</span>
        </button>
      </div>

      {/* 2. POP-UP FLOTANTE DESPLEGABLE DE CAPTURA RÁPIDA */}
      <AnimatePresence>
        {isOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: 15 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: 15 }}
              transition={{ duration: 0.2 }}
              className="relative w-full max-w-md rounded-[32px] bg-[#0A0F17] border border-white/15 p-6 sm:p-8 text-white shadow-2xl overflow-hidden"
            >
              {/* Luces y resplandor de fondo */}
              <div className="absolute top-0 right-0 w-64 h-64 bg-[#10B981]/20 rounded-full blur-[90px] pointer-events-none" />

              {/* Botón cerrar */}
              <button
                onClick={handleReset}
                className="absolute top-5 right-5 p-2 rounded-full bg-white/10 hover:bg-white/20 text-slate-300 hover:text-white transition-colors"
              >
                <X className="w-5 h-5" />
              </button>

              {!isSuccess ? (
                <div>
                  <div className="flex items-center gap-2 mb-2">
                    <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                    <span className="text-xs font-mono font-semibold text-emerald-400 uppercase tracking-wider">
                      Admisión Inmediata B2B
                    </span>
                  </div>

                  <h3 className="text-xl sm:text-2xl font-bold tracking-tight text-white mb-1">
                    Solicita tu línea Kuota
                  </h3>
                  <p className="text-xs text-slate-400 mb-6">
                    Sin papeleos ni costo de apertura. Ingresa tu RUC o DNI para evaluar tu poder de compra en 2 horas.
                  </p>

                  <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                      <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                        RUC o DNI de tu negocio
                      </label>
                      <div className="relative">
                        <Building className="absolute left-3.5 top-3.5 w-4 h-4 text-slate-500" />
                        <input
                          type="text"
                          required
                          value={docNumber}
                          onChange={(e) => setDocNumber(e.target.value)}
                          placeholder="Ej. 20601234567 o 45678912"
                          className="w-full pl-10 pr-4 py-3 rounded-2xl bg-white/5 border border-white/10 text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none focus:border-emerald-500 transition-colors"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                        Nombre de tu negocio o contacto
                      </label>
                      <div className="relative">
                        <User className="absolute left-3.5 top-3.5 w-4 h-4 text-slate-500" />
                        <input
                          type="text"
                          value={contactName}
                          onChange={(e) => setContactName(e.target.value)}
                          placeholder="Ej. Distribuidora Lima Norte"
                          className="w-full pl-10 pr-4 py-3 rounded-2xl bg-white/5 border border-white/10 text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none focus:border-emerald-500 transition-colors"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                        Celular WhatsApp
                      </label>
                      <div className="relative">
                        <Phone className="absolute left-3.5 top-3.5 w-4 h-4 text-slate-500" />
                        <input
                          type="tel"
                          required
                          value={phone}
                          onChange={(e) => setPhone(e.target.value)}
                          placeholder="Ej. 981 234 567"
                          className="w-full pl-10 pr-4 py-3 rounded-2xl bg-white/5 border border-white/10 text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none focus:border-emerald-500 transition-colors"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                        Rubro Comercial
                      </label>
                      <select
                        value={businessType}
                        onChange={(e) => setBusinessType(e.target.value)}
                        className="w-full px-3.5 py-3 rounded-2xl bg-[#161D29] border border-white/10 text-white text-xs sm:text-sm focus:outline-none focus:border-emerald-500"
                      >
                        <option value="Bodega / Minimarket">Bodega / Minimarket</option>
                        <option value="Ferretería / Construcción">Ferretería / Construcción</option>
                        <option value="Textil / Confecciones Gamarra">Textil / Confecciones Gamarra</option>
                        <option value="Farmacia / Salud">Farmacia / Salud</option>
                        <option value="Distribuidora Mayorista">Distribuidora Mayorista</option>
                        <option value="Comercio Electrónico / Tienda">Comercio Electrónico / Tienda</option>
                        <option value="Otro">Otro Comercio</option>
                      </select>
                    </div>

                    <button
                      type="submit"
                      disabled={isSubmitting}
                      className="w-full mt-2 py-3.5 rounded-full bg-white text-[#070A0F] font-bold text-xs sm:text-sm hover:bg-[#8FD694] active:scale-95 transition-all shadow-lg flex items-center justify-center gap-2 disabled:opacity-50"
                    >
                      {isSubmitting ? (
                        <span>Evaluando datos...</span>
                      ) : (
                        <>
                          <span>Evaluar mi empresa ahora</span>
                          <ArrowRight className="w-4 h-4" />
                        </>
                      )}
                    </button>
                  </form>

                  <div className="mt-4 pt-3 border-t border-white/10 flex items-center justify-between text-[11px] text-slate-400">
                    <span className="flex items-center gap-1">
                      <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
                      100% Confidencial
                    </span>
                    <span>Evaluación 100% Digital</span>
                  </div>
                </div>
              ) : (
                <div className="text-center py-4 space-y-4">
                  <div className="w-16 h-16 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center mx-auto mb-2">
                    <CheckCircle2 className="w-10 h-10 text-[#10B981]" />
                  </div>

                  <h3 className="text-xl sm:text-2xl font-bold text-white">
                    ¡Solicitud Pre-Calificada!
                  </h3>

                  <p className="text-xs sm:text-sm text-slate-300 leading-relaxed max-w-sm mx-auto">
                    Hemos registrado tu documento <strong>{docNumber}</strong>. Un ejecutivo de Kuota se pondrá en contacto contigo para verificar tu línea de capital.
                  </p>

                  <div className="p-4 rounded-2xl bg-white/5 border border-white/10 text-left text-xs space-y-1.5">
                    <div className="flex justify-between text-slate-400">
                      <span>Línea estimada:</span>
                      <span className="text-emerald-400 font-bold font-mono">Hasta S/ 15,000</span>
                    </div>
                    <div className="flex justify-between text-slate-400">
                      <span>Plazos disponibles:</span>
                      <span className="text-white font-medium">30 y 60 días</span>
                    </div>
                    <div className="flex justify-between text-slate-400">
                      <span>Membresía base:</span>
                      <span className="text-white font-medium">S/ 0.00</span>
                    </div>
                  </div>

                  <div className="space-y-2 pt-2">
                    {whatsappRedirectUrl && (
                      <a
                        href={whatsappRedirectUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="w-full py-3.5 rounded-full bg-[#10B981] hover:bg-[#059669] text-white font-bold text-xs sm:text-sm transition-colors flex items-center justify-center gap-2 shadow-lg"
                      >
                        <MessageSquare className="w-4 h-4 fill-current" />
                        <span>Continuar por WhatsApp Inmediato</span>
                      </a>
                    )}
                    <button
                      onClick={handleReset}
                      className="w-full py-2.5 rounded-full text-xs text-slate-400 hover:text-white transition-colors"
                    >
                      Cerrar ventana
                    </button>
                  </div>
                </div>
              )}
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </>
  );
};

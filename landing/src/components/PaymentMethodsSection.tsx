import React from 'react';
import { CreditCard, ShieldCheck } from 'lucide-react';
import { PAYMENT_METHODS } from '../data/landingData';
import { ScrollReveal } from '../utils/motionSystem';

/**
 * PRD §6.3 + FASE 3.C: Medios de pago con aparición escalonada y elevación táctil/hover.
 */
export const PaymentMethodsSection: React.FC = () => {
  return (
    <section
      id="medios-de-pago"
      aria-labelledby="payment-methods-heading"
      className="w-full bg-white border-b border-slate-200/80 py-8 sm:py-10"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
          {/* Título y Subtexto */}
          <ScrollReveal delayMs={0} direction="left">
            <div className="flex flex-col sm:flex-row sm:items-center gap-3 lg:max-w-md">
              <div className="w-12 h-12 rounded-2xl bg-sky-50 border border-sky-200 flex items-center justify-center shrink-0 shadow-xs">
                <CreditCard className="w-6 h-6 text-[#0391FD]" aria-hidden="true" />
              </div>
              <div>
                <h2
                  id="payment-methods-heading"
                  className="text-lg sm:text-xl font-extrabold text-[#0A192F] leading-snug"
                >
                  Pagas por tu método de pago favorito
                </h2>
                <p className="text-xs sm:text-sm font-medium text-slate-500 flex items-center gap-1.5 mt-0.5">
                  <ShieldCheck className="w-4 h-4 text-[#10B981] shrink-0" aria-hidden="true" />
                  <span>Compatibilidad con tarjetas, Yape, Plin y transferencias bancarias.</span>
                </p>
              </div>
            </div>
          </ScrollReveal>

          {/* Fila de Logos con stagger */}
          <div
            role="list"
            aria-label="Métodos de pago compatibles"
            className="grid grid-cols-3 sm:grid-cols-6 gap-2.5 sm:gap-3 flex-1 lg:max-w-2xl"
          >
            {PAYMENT_METHODS.map((method, idx) => (
              <ScrollReveal
                key={method.id}
                delayMs={idx * 55}
                direction="up"
              >
                <div
                  role="listitem"
                  aria-label={`Logo ${method.name}`}
                  className="group rounded-2xl bg-slate-50 hover:bg-white border border-slate-200/80 px-3 py-3 flex flex-col items-center justify-between text-center transition-all duration-200 hover:-translate-y-1 hover:shadow-md hover:border-[#0391FD]/40 active:scale-95 shadow-xs"
                >
                  <div className="h-9 sm:h-10 w-full flex items-center justify-center">
                    <img
                      src={method.logoUrl}
                      alt={`Logo oficial de ${method.name}`}
                      loading="lazy"
                      className={`max-h-full max-w-full object-contain transition-transform duration-200 group-hover:scale-105 ${
                        method.id === 'yape' ? 'rounded-md h-8 sm:h-9' : ''
                      } ${method.id === 'plin' ? 'h-8 sm:h-9' : ''}`}
                    />
                  </div>
                  <span className="text-[11px] font-bold text-slate-700 tracking-tight mt-1.5">
                    {method.name}
                  </span>
                </div>
              </ScrollReveal>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};

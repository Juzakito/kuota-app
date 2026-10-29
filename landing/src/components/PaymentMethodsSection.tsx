import React from 'react';
import { CreditCard, Shield } from 'lucide-react';
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
      className="w-full bg-white/80 border-b-2 border-kuota-deepnavy py-7 sm:py-9"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
          {/* Título y Subtexto */}
          <ScrollReveal delayMs={0} direction="left">
            <div className="flex flex-col sm:flex-row sm:items-center gap-3 lg:max-w-md">
              <div className="w-11 h-11 rounded-2xl bg-kuota-palesky border-2 border-kuota-deepnavy flex items-center justify-center shrink-0">
                <CreditCard className="w-5 h-5 text-kuota-deepnavy" aria-hidden="true" />
              </div>
              <div>
                <h2
                  id="payment-methods-heading"
                  className="text-lg sm:text-xl font-extrabold text-kuota-deepnavy leading-snug"
                >
                  Pagas por tu método de pago favorito
                </h2>
                <p className="text-sm font-bold text-kuota-crayola flex items-center gap-1.5 mt-0.5">
                  <Shield className="w-3.5 h-3.5 text-kuota-dodger shrink-0" aria-hidden="true" />
                  <span>Las tarjetas que acepta 360 pay.</span>
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
                  className="group rounded-xl bg-white hover:bg-kuota-palesky/40 border-2 border-kuota-deepnavy px-3 py-2.5 flex flex-col items-center justify-between text-center transition-all duration-200 hover:-translate-y-1 hover:shadow-navy-hard active:scale-95 shadow-sm"
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
                  <span className="text-[11px] font-extrabold text-kuota-deepnavy tracking-tight mt-1.5">
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

import React from 'react';
import { CreditCard, Shield } from 'lucide-react';
import { PAYMENT_METHODS } from '../data/landingData';

/**
 * PRD §6.3: Medios de pago
 * - Banner delgado a todo el ancho.
 * - Título: Pagas por tu método de pago favorito
 * - Subtexto: Las tarjetas que acepta 360 pay.
 * - Logos en fila: BCP, BBVA, Interbank, Scotiabank, Yape y Plin (en móvil, grilla de 3 columnas).
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

          {/* Fila de Logos: Grilla de 3 columnas en móvil, fila de 6 en desktop (PRD §6.3) */}
          <div
            role="list"
            aria-label="Métodos de pago compatibles"
            className="grid grid-cols-3 sm:grid-cols-6 gap-2.5 sm:gap-3 flex-1 lg:max-w-2xl"
          >
            {PAYMENT_METHODS.map((method) => (
              <div
                key={method.id}
                role="listitem"
                aria-label={`Logo ${method.name}`}
                className="group rounded-xl bg-kuota-palesky/80 hover:bg-kuota-palesky border-2 border-kuota-deepnavy px-3 py-2.5 flex flex-col items-center justify-center text-center transition-transform hover:-translate-y-0.5"
              >
                <span className="text-[10px] font-bold uppercase tracking-wider text-kuota-deepnavy/70">
                  {method.category === 'Billetera digital' ? 'Billetera' : 'Banco'}
                </span>
                <span className="text-sm sm:text-base font-extrabold text-kuota-deepnavy tracking-tight mt-0.5">
                  {method.name}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};

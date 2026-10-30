import React from 'react';
import { ShieldCheck } from 'lucide-react';
import { SOCIAL_PROOF_LOGOS } from '../../config/pricingConfig';

/**
 * Prueba social y canales de recaudación compatibles:
 * - Muestra las tarjetas y medios de pago con los que opera la recaudación en Perú
 * - Banner horizontal con logos de BCP, BBVA, Interbank, Scotiabank, Yape y Plin
 */
export const SocialProofSection: React.FC = () => {
  const carouselItems = [...SOCIAL_PROOF_LOGOS, ...SOCIAL_PROOF_LOGOS];

  return (
    <section
      id="prueba-social"
      aria-labelledby="social-proof-heading"
      className="w-full bg-white border-b border-slate-200/80 py-8 overflow-hidden"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 mb-6">
          <h2
            id="social-proof-heading"
            className="text-base sm:text-lg font-extrabold text-[#0A192F] flex items-center gap-2"
          >
            <ShieldCheck className="w-5 h-5 text-[#10B981] shrink-0" aria-hidden="true" />
            <span>Canales y medios de recaudación compatibles</span>
          </h2>
          <span className="text-xs font-medium text-slate-500">
            Billeteras digitales, tarjetas de crédito/débito y transferencias
          </span>
        </div>

        {/* Desktop / Tablet: Fila completa de 6 logos */}
        <div
          role="list"
          aria-label="Canales y billeteras compatibles"
          className="hidden sm:grid sm:grid-cols-6 gap-3.5"
        >
          {SOCIAL_PROOF_LOGOS.map((logo) => (
            <div
              key={logo.id}
              role="listitem"
              aria-label={`Logo oficial de ${logo.name}`}
              className="rounded-2xl bg-slate-50 hover:bg-white border border-slate-200/80 px-3.5 py-3 flex flex-col items-center justify-between text-center shadow-xs hover:shadow-md transition-all duration-200 hover:-translate-y-0.5"
            >
              <div className="h-9 sm:h-10 w-full flex items-center justify-center">
                <img
                  src={logo.logoUrl}
                  alt={`Logo oficial de ${logo.name}`}
                  loading="lazy"
                  className={`max-h-full max-w-full object-contain ${
                    logo.id === 'yape' ? 'rounded-md h-8 sm:h-9' : ''
                  } ${logo.id === 'plin' ? 'h-8 sm:h-9' : ''}`}
                />
              </div>
              <span className="text-[11px] font-bold text-slate-700 tracking-tight mt-1.5">
                {logo.name}
              </span>
            </div>
          ))}
        </div>

        {/* Móvil: Carrusel horizontal continuo */}
        <div
          aria-label="Carrusel de medios de pago"
          className="sm:hidden relative w-full overflow-hidden"
        >
          <div className="flex w-max animate-marquee gap-3">
            {carouselItems.map((logo, idx) => (
              <div
                key={`${logo.id}-${idx}`}
                className="w-36 shrink-0 rounded-2xl bg-slate-50 border border-slate-200/80 px-3 py-3 flex flex-col items-center justify-between text-center shadow-xs"
              >
                <div className="h-9 w-full flex items-center justify-center">
                  <img
                    src={logo.logoUrl}
                    alt={`Logo oficial de ${logo.name}`}
                    loading="lazy"
                    className={`max-h-full max-w-full object-contain ${
                      logo.id === 'yape' ? 'rounded-md h-8' : ''
                    } ${logo.id === 'plin' ? 'h-8' : ''}`}
                  />
                </div>
                <span className="text-[11px] font-bold text-slate-700 mt-1">
                  {logo.name}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};

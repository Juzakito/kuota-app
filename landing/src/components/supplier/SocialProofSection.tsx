import React from 'react';
import { ShieldCheck } from 'lucide-react';
import { SOCIAL_PROOF_LOGOS } from '../../config/pricingConfig';

/**
 * Prueba social y canales de recaudación compatibles:
 * - Muestra las tarjetas y medios de pago con los que opera la recaudación en Perú
 * - Banner horizontal con logos de BCP, BBVA, Interbank, Scotiabank, Yape y Plin
 * - Claridad y transparencia respecto a la compatibilidad técnica
 */
export const SocialProofSection: React.FC = () => {
  const carouselItems = [...SOCIAL_PROOF_LOGOS, ...SOCIAL_PROOF_LOGOS];

  return (
    <section
      id="prueba-social"
      aria-labelledby="social-proof-heading"
      className="w-full bg-white/90 border-b-2 border-kuota-deepnavy py-6 sm:py-8 overflow-hidden"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 mb-5">
          <h2
            id="social-proof-heading"
            className="text-base sm:text-lg font-extrabold text-kuota-deepnavy flex items-center gap-2"
          >
            <ShieldCheck className="w-5 h-5 text-kuota-dodger shrink-0" aria-hidden="true" />
            <span>Canales y medios de recaudación compatibles</span>
          </h2>
          <span className="text-xs font-bold text-kuota-crayola">
            Billeteras digitales, tarjetas y transferencias en Perú
          </span>
        </div>

        {/* Desktop / Tablet: Fila completa de 6 logos */}
        <div
          role="list"
          aria-label="Canales y billeteras compatibles"
          className="hidden sm:grid sm:grid-cols-6 gap-3"
        >
          {SOCIAL_PROOF_LOGOS.map((logo) => (
            <div
              key={logo.id}
              role="listitem"
              aria-label={`Logo oficial de ${logo.name}`}
              className="rounded-xl bg-white hover:bg-kuota-palesky/40 border-2 border-kuota-deepnavy px-3 py-2.5 flex flex-col items-center justify-between text-center shadow-sm transition-transform hover:-translate-y-0.5"
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
              <span className="text-[11px] font-extrabold text-kuota-deepnavy tracking-tight mt-1.5">
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
                className="w-36 shrink-0 rounded-xl bg-white border-2 border-kuota-deepnavy px-3 py-2.5 flex flex-col items-center justify-between text-center shadow-sm"
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
                <span className="text-[11px] font-extrabold text-kuota-deepnavy mt-1">
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

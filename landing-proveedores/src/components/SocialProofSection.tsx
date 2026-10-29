import React from 'react';
import { Shield } from 'lucide-react';
import { SOCIAL_PROOF_LOGOS } from '../config/pricingConfig';

/**
 * PRD §5.4: Prueba social
 * - Banner delgado a todo lo ancho con los logos de BCP, BBVA, Interbank, Scotiabank, Yape y Plin (IMG-04 a IMG-09, 160×60 cada uno).
 * - Texto de apoyo: "Las tarjetas que acepta 360 Pay".
 * - En móvil, los logos se desplazan en un carrusel horizontal automático.
 */
export const SocialProofSection: React.FC = () => {
  const carouselItems = [...SOCIAL_PROOF_LOGOS, ...SOCIAL_PROOF_LOGOS];

  return (
    <section
      id="prueba-social"
      aria-labelledby="social-proof-heading"
      className="w-full bg-white/85 border-b-2 border-kuota-deepnavy py-6 sm:py-8 overflow-hidden"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 mb-5">
          <h2
            id="social-proof-heading"
            className="text-base sm:text-lg font-extrabold text-kuota-deepnavy flex items-center gap-2"
          >
            <Shield className="w-5 h-5 text-kuota-dodger shrink-0" aria-hidden="true" />
            <span>Las tarjetas que acepta 360 Pay</span>
          </h2>
          <span className="text-xs font-bold text-kuota-crayola">
            Placeholders oficiales IMG-04 a IMG-09 (160×60)
          </span>
        </div>

        {/* Desktop / Tablet: Fila completa de 6 logos */}
        <div
          role="list"
          aria-label="Bancos y billeteras compatibles con 360 Pay"
          className="hidden sm:grid sm:grid-cols-6 gap-3"
        >
          {SOCIAL_PROOF_LOGOS.map((logo) => (
            <div
              key={logo.id}
              role="listitem"
              aria-label={`${logo.placeholderId}: Logo ${logo.name} (${logo.dimensions})`}
              className="rounded-xl bg-kuota-palesky/75 border-2 border-kuota-deepnavy px-3 py-2.5 flex flex-col items-center justify-center text-center shadow-sm"
            >
              <div className="flex items-center justify-between w-full text-[9px] font-extrabold text-kuota-deepnavy/75 mb-0.5">
                <span>{logo.placeholderId}</span>
                <span>{logo.dimensions}</span>
              </div>
              <span className="text-base font-extrabold text-kuota-deepnavy tracking-tight">
                {logo.name}
              </span>
            </div>
          ))}
        </div>

        {/* Móvil: Carrusel horizontal automático (PRD §5.4) */}
        <div
          aria-label="Carrusel de medios de pago"
          className="sm:hidden relative w-full overflow-hidden"
        >
          <div className="flex w-max animate-marquee gap-3">
            {carouselItems.map((logo, idx) => (
              <div
                key={`${logo.id}-${idx}`}
                className="w-36 shrink-0 rounded-xl bg-kuota-palesky/80 border-2 border-kuota-deepnavy px-3 py-2.5 flex flex-col items-center justify-center text-center"
              >
                <div className="flex items-center justify-between w-full text-[9px] font-extrabold text-kuota-deepnavy/75 mb-0.5">
                  <span>{logo.placeholderId}</span>
                  <span>{logo.dimensions}</span>
                </div>
                <span className="text-sm font-extrabold text-kuota-deepnavy">
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

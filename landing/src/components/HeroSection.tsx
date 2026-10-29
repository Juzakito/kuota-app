import React from 'react';
import { ShieldCheck, Sparkles, Store, Smartphone } from 'lucide-react';
import { PlayStoreButton } from './PlayStoreButton';
import { ImagePlaceholder } from './ImagePlaceholder';

/**
 * PRD §6.1: Hero
 * - Titular (H1 único): Financia tu crecimiento sin apuros
 * - Subtexto: Confiamos en ti, emprendedor, que cada día con esfuerzo saca adelante a su familia. Justos somos más. Crece tu negocio con Kuota.
 * - CTA principal: Descargar en Play Store
 * - Elementos:
 *   - Logo pequeño de Kuota.
 *   - Placeholder de imagen: emprendedor en su negocio (tienda, bodega o puesto).
 *   - Placeholder de captura de la app (pantalla de inicio con tarjeta de nivel), flotando sobre la imagen en desktop; debajo del texto en móvil.
 */
export const HeroSection: React.FC = () => {
  return (
    <section
      id="inicio"
      aria-labelledby="hero-heading"
      className="relative overflow-hidden pt-8 pb-14 sm:py-16 lg:py-20 border-b-2 border-kuota-deepnavy bg-grid-pattern"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-8 items-center">
          {/* Columna de Copy y CTA */}
          <div className="lg:col-span-6 flex flex-col items-start text-left">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white border-2 border-kuota-deepnavy shadow-sm mb-5">
              <span className="w-2.5 h-2.5 rounded-full bg-kuota-dodger" aria-hidden="true" />
              <span className="text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider">
                Kuota para Emprendedores y Negocios
              </span>
            </div>

            <h1
              id="hero-heading"
              className="text-4xl sm:text-5xl lg:text-[3.35rem] font-extrabold text-kuota-deepnavy tracking-tight leading-[1.08] mb-5"
            >
              Financia tu crecimiento{' '}
              <span className="text-kuota-dodger underline decoration-kuota-deepnavy decoration-4 underline-offset-4">
                sin apuros
              </span>
            </h1>

            <p className="text-lg sm:text-xl font-semibold text-kuota-crayola leading-relaxed mb-7 max-w-xl">
              Confiamos en ti, emprendedor, que cada día con esfuerzo saca adelante a su familia.{' '}
              <strong className="text-kuota-deepnavy font-extrabold">Justos somos más.</strong>{' '}
              Crece tu negocio con Kuota.
            </p>

            {/* Píldoras de propuesta de valor (PRD §1) */}
            <div className="flex flex-wrap gap-2 mb-7">
              <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/90 border border-kuota-deepnavy text-xs sm:text-sm font-bold text-kuota-deepnavy">
                <Sparkles className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                En 3 clics
              </span>
              <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/90 border border-kuota-deepnavy text-xs sm:text-sm font-bold text-kuota-deepnavy">
                <ShieldCheck className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                5% de interés
              </span>
              <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/90 border border-kuota-deepnavy text-xs sm:text-sm font-bold text-kuota-deepnavy">
                Sin papeleo ni Infocorp
              </span>
            </div>

            <div className="w-full sm:w-auto flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
              <PlayStoreButton
                section="hero"
                label="Descargar en Play Store"
                size="lg"
                className="w-full sm:w-auto"
              />
            </div>
          </div>

          {/* Columna Visual: Placeholder de emprendedor + Placeholder de captura de la app */}
          <div className="lg:col-span-6 relative">
            {/* Placeholder principal: Emprendedor en su negocio */}
            <ImagePlaceholder
              id="IMG-HERO-01"
              label="Emprendedor en su negocio (tienda, bodega o puesto)"
              aspectRatioLabel="4:3"
              aspectClass="aspect-[4/3] w-full"
              className="bg-white/90"
            >
              <div className="flex flex-col items-center max-w-xs px-3">
                <div className="w-16 h-16 rounded-2xl bg-kuota-palesky border-2 border-kuota-deepnavy flex items-center justify-center mb-3 shadow-sm">
                  <Store className="w-8 h-8 text-kuota-deepnavy" aria-hidden="true" />
                </div>
                <span className="text-sm font-extrabold text-kuota-deepnavy">
                  Fotografía principal del emprendedor
                </span>
                <span className="text-xs font-semibold text-kuota-crayola mt-1">
                  Bodega, tienda o puesto abastecido de mercadería
                </span>
              </div>
            </ImagePlaceholder>

            {/* Placeholder de captura de la app: flotando sobre la imagen en desktop; debajo en móvil (PRD §6.1) */}
            <div className="mt-5 lg:mt-0 lg:absolute lg:-bottom-6 lg:-left-6 lg:w-64 z-20">
              <div
                role="img"
                aria-label="Captura de la app: pantalla de inicio con tarjeta de nivel"
                className="rounded-2xl border-2 border-kuota-deepnavy bg-white p-3.5 shadow-navy-hard-lg"
              >
                <div className="flex items-center justify-between gap-2 mb-2.5">
                  <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-kuota-deepnavy text-white text-[11px] font-bold uppercase">
                    <Smartphone className="w-3 h-3 text-kuota-dodger" aria-hidden="true" />
                    IMG-HERO-02
                  </span>
                  <span className="text-[11px] font-extrabold text-kuota-deepnavy bg-kuota-palesky px-2 py-0.5 rounded border border-kuota-deepnavy">
                    App 9:16
                  </span>
                </div>

                {/* Esquema visual de la tarjeta de nivel dentro del placeholder */}
                <div className="rounded-xl bg-kuota-deepnavy text-white p-3 mb-2.5 border border-kuota-deepnavy">
                  <div className="flex items-center justify-between text-[11px] text-kuota-palesky font-semibold mb-1">
                    <span>Tarjeta de Nivel Kuota</span>
                    <span className="px-1.5 py-0.5 rounded bg-kuota-dodger text-white font-bold">
                      Nivel activo
                    </span>
                  </div>
                  <div className="text-base font-extrabold tracking-tight">
                    S/300 – S/1,000
                  </div>
                  <div className="text-[11px] text-kuota-palesky mt-1">
                    Financia en 3 clics · 5% interés
                  </div>
                </div>

                <p className="text-xs font-bold text-kuota-deepnavy leading-tight">
                  Captura (placeholder): pantalla de inicio con tarjeta de nivel
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

import React from 'react';
import { Award, Users } from 'lucide-react';
import { ImagePlaceholder } from './ImagePlaceholder';
import { ScrollReveal, TiltCard } from '../utils/motionSystem';

/**
 * PRD §6.5 + FASE 6: Crecemos junto a ti (formalización) con reveal progresivo y profundidad 3D.
 */
export const FormalizationSection: React.FC = () => {
  return (
    <section
      id="formalizacion"
      aria-labelledby="formalization-heading"
      className="py-14 sm:py-20 border-b-2 border-kuota-deepnavy bg-white/45"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-center">
          {/* Columna de Texto */}
          <ScrollReveal
            delayMs={0}
            direction="left"
            className="order-2 lg:order-1 lg:col-span-6 flex flex-col items-start text-left"
          >
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-kuota-dodger text-white border-2 border-kuota-deepnavy shadow-navy-hard mb-5 transition-transform duration-200 hover:-translate-y-0.5">
              <Award className="w-5 h-5 text-kuota-palesky shrink-0" aria-hidden="true" />
              <span className="text-sm sm:text-base font-extrabold tracking-tight">
                0% de interés en tu formalización
              </span>
            </div>

            <h2
              id="formalization-heading"
              className="text-3xl sm:text-4xl font-extrabold text-kuota-deepnavy tracking-tight mb-5"
            >
              Crecemos junto a ti
            </h2>

            <p className="text-lg sm:text-xl font-semibold text-kuota-crayola leading-relaxed">
              Si aún no cuentas con un RUC 20 o RUC 10, sabemos que ser formal también cuesta. Te ofrecemos soluciones de financiamiento para tu formalización con{' '}
              <strong className="text-kuota-deepnavy font-extrabold">0% de interés</strong>, y te acompañamos a seguir creciendo con mentorías y talleres junto a expertos.
            </p>
          </ScrollReveal>

          {/* Columna de Imagen Única */}
          <ScrollReveal
            delayMs={120}
            direction="right"
            className="order-1 lg:order-2 lg:col-span-6"
          >
            <TiltCard maxTiltDeg={2.5}>
              <ImagePlaceholder
                id="IMG-FORMAL-01"
                label="Emprendedor en mentoría o taller con expertos, o trámite de formalización"
                aspectRatioLabel="4:3"
                aspectClass="aspect-[4/3] w-full"
                className="rounded-3xl border-2 border-kuota-deepnavy"
              >
                <div className="flex flex-col items-center max-w-xs px-4">
                  <div className="w-16 h-16 rounded-2xl bg-kuota-palesky border-2 border-kuota-deepnavy flex items-center justify-center mb-3">
                    <Users className="w-8 h-8 text-kuota-deepnavy" aria-hidden="true" />
                  </div>
                  <span className="text-sm font-extrabold text-kuota-deepnavy">
                    Acompañamiento, mentorías y formalización RUC 10 / RUC 20
                  </span>
                </div>
              </ImagePlaceholder>
            </TiltCard>
          </ScrollReveal>
        </div>
      </div>
    </section>
  );
};

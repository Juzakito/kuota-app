import React from 'react';
import { Award } from 'lucide-react';
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

          {/* Columna de Imagen Oficial */}
          <ScrollReveal
            delayMs={120}
            direction="right"
            className="order-1 lg:order-2 lg:col-span-6"
          >
            <TiltCard maxTiltDeg={2.5}>
              <div className="relative rounded-3xl overflow-hidden border-2 border-kuota-deepnavy bg-white shadow-navy-hard aspect-[4/3] w-full">
                <img
                  src="/images/formalizacion-kuota.jpg"
                  alt="Emprendedores en taller de mentoría y formalización RUC 10 / RUC 20 con Kuota"
                  className="w-full h-full object-cover"
                />
              </div>
            </TiltCard>
          </ScrollReveal>
        </div>
      </div>
    </section>
  );
};

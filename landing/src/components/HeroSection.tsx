import React from 'react';
import { ShieldCheck, Sparkles, Smartphone, CheckCircle2 } from 'lucide-react';
import { PlayStoreButton } from './PlayStoreButton';
import { ScrollReveal, TiltCard } from '../utils/motionSystem';

/**
 * PRD §6.1 + FASE 3.A (HERO PRINCIPAL):
 * - Entrada cinematográfica sutil y escalonada del contenido.
 * - Título con aparición progresiva y subtítulo con transición independiente.
 * - CTA con microinteracciones táctiles y sonoras opcionales.
 * - Tarjeta financiera digital con movimiento flotante, profundidad 3D e iluminación ambiental discreta.
 */
export const HeroSection: React.FC = () => {
  return (
    <section
      id="inicio"
      aria-labelledby="hero-heading"
      className="relative overflow-hidden pt-8 pb-14 sm:py-16 lg:py-20 border-b-2 border-kuota-deepnavy bg-grid-pattern"
    >
      {/* Iluminación ambiental discreta */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -top-24 right-1/4 w-96 h-96 rounded-full bg-kuota-dodger/15 blur-3xl"
      />

      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-8 items-center">
          {/* Columna de Copy y CTA con aparición progresiva sincronizada */}
          <div className="lg:col-span-6 flex flex-col items-start text-left">
            <ScrollReveal delayMs={0} direction="up">
              <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white border-2 border-kuota-deepnavy shadow-sm mb-5">
                <span className="w-2.5 h-2.5 rounded-full bg-kuota-dodger animate-pulse" aria-hidden="true" />
                <span className="text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider">
                  Kuota para Emprendedores y Negocios
                </span>
              </div>
            </ScrollReveal>

            <ScrollReveal delayMs={90} direction="up">
              <h1
                id="hero-heading"
                className="text-4xl sm:text-5xl lg:text-[3.35rem] font-extrabold text-kuota-deepnavy tracking-tight leading-[1.08] mb-5"
              >
                Financia tu crecimiento{' '}
                <span className="text-kuota-dodger underline decoration-kuota-deepnavy decoration-4 underline-offset-4">
                  sin apuros
                </span>
              </h1>
            </ScrollReveal>

            <ScrollReveal delayMs={180} direction="up">
              <p className="text-lg sm:text-xl font-semibold text-kuota-crayola leading-relaxed mb-7 max-w-xl">
                Confiamos en ti, emprendedor, que cada día con esfuerzo saca adelante a su familia.{' '}
                <strong className="text-kuota-deepnavy font-extrabold">Justos somos más.</strong>{' '}
                Crece tu negocio con Kuota.
              </p>
            </ScrollReveal>

            {/* Píldoras de propuesta de valor con microinteracciones */}
            <ScrollReveal delayMs={260} direction="up">
              <div className="flex flex-wrap gap-2 mb-7">
                <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/95 border border-kuota-deepnavy text-xs sm:text-sm font-bold text-kuota-deepnavy transition-transform duration-150 hover:-translate-y-0.5">
                  <Sparkles className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                  En 3 clics
                </span>
                <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/95 border border-kuota-deepnavy text-xs sm:text-sm font-bold text-kuota-deepnavy transition-transform duration-150 hover:-translate-y-0.5">
                  <ShieldCheck className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                  5% de interés
                </span>
                <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/95 border border-kuota-deepnavy text-xs sm:text-sm font-bold text-kuota-deepnavy transition-transform duration-150 hover:-translate-y-0.5">
                  <CheckCircle2 className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                  Sin papeleo ni Infocorp
                </span>
              </div>
            </ScrollReveal>

            <ScrollReveal delayMs={340} direction="up" className="w-full sm:w-auto">
              <div className="w-full sm:w-auto flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
                <PlayStoreButton
                  section="hero"
                  label="Descargar en Play Store"
                  size="lg"
                  className="w-full sm:w-auto"
                />
              </div>
            </ScrollReveal>
          </div>

          {/* Columna Visual: Profundidad 3D + Tarjeta financiera flotante */}
          <ScrollReveal delayMs={160} direction="scale" className="lg:col-span-6 relative">
            <TiltCard maxTiltDeg={2.5}>
              <div className="relative rounded-3xl overflow-hidden border-2 border-kuota-deepnavy bg-white shadow-navy-hard aspect-[4/3] w-full">
                <img
                  src="/images/hero-negocio-kuota.jpg"
                  alt="Emprendedor en su negocio abastecido con Kuota"
                  className="w-full h-full object-cover"
                />
              </div>
            </TiltCard>

            {/* Tarjeta digital con movimiento flotante suave y profundidad (FASE 3.A) */}
            <div className="mt-5 lg:mt-0 lg:absolute lg:-bottom-6 lg:-left-6 lg:w-68 z-20 animate-floatCard">
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

                {/* Tarjeta financiera digital con indicador de línea activa */}
                <div className="rounded-xl bg-kuota-deepnavy text-white p-3 mb-2.5 border border-kuota-deepnavy">
                  <div className="flex items-center justify-between text-[11px] text-kuota-palesky font-semibold mb-1">
                    <span>Tarjeta de Nivel Kuota</span>
                    <span className="px-1.5 py-0.5 rounded bg-kuota-dodger text-white font-bold">
                      Nivel activo
                    </span>
                  </div>
                  <div className="text-base font-extrabold tracking-tight tabular-nums">
                    S/300 – S/1,000
                  </div>
                  <div className="w-full h-1.5 rounded-full bg-white/15 overflow-hidden my-2">
                    <div className="h-full w-3/4 rounded-full bg-kuota-dodger" />
                  </div>
                  <div className="text-[11px] text-kuota-palesky">
                    Financia en 3 clics · 5% interés
                  </div>
                </div>

                <p className="text-xs font-bold text-kuota-deepnavy leading-tight">
                  Captura (placeholder): pantalla de inicio con tarjeta de nivel
                </p>
              </div>
            </div>
          </ScrollReveal>
        </div>
      </div>
    </section>
  );
};

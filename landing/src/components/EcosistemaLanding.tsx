import React from 'react';
import { ArrowRight, QrCode, ShieldCheck, ShoppingBag, CreditCard, Sparkles, CheckCircle2 } from 'lucide-react';
import { ProblemSection } from './ProblemSection';
import { FiveStepsSection } from './FiveStepsSection';
import { TwoSolutionsSection } from './TwoSolutionsSection';
import { LevelsLadderSection } from './LevelsLadderSection';
import { TrustSection } from './TrustSection';
import { FaqSection } from './FaqSection';
import { WaitlistForm } from './WaitlistForm';

export interface EcosistemaLandingProps {
  onSelectBuyer: () => void;
  onSelectSupplier: () => void;
}

export const EcosistemaLanding: React.FC<EcosistemaLandingProps> = ({
  onSelectBuyer,
  onSelectSupplier,
}) => {
  return (
    <div className="space-y-0">
      {/* 1. HERO SECTION (PRD §2) */}
      <section className="relative overflow-hidden bg-paper pt-12 md:pt-20 pb-16 md:pb-24 border-b border-fog">
        <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            {/* Titular y CTAs (Izquierda) */}
            <div className="lg:col-span-7 space-y-6 text-left">
              <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-pill bg-linen text-forest text-xs font-bold">
                <span className="w-2 h-2 rounded-pill bg-spruce animate-pulse" />
                <span>Microcrédito B2B y recaudación inteligente para Perú</span>
              </div>

              {/* Titular en Inter Black 900 mayúsculas según DESIGN.md y PRD §1 */}
              <h1 className="text-4xl sm:text-6xl lg:text-7xl font-black text-obsidian tracking-[-0.035em] leading-[0.98] uppercase">
                HAZ QUE TUS VENTAS FINANCIEN TUS COMPRAS.
              </h1>

              <p className="text-base sm:text-lg text-charcoal/80 max-w-xl font-normal leading-relaxed">
                Compra y vende al crédito. Sin que nadie arriesgue su negocio. Cuotas semanales fijas con solo 5% de costo o cobra con tarjeta y billeteras digitales al 2% de comisión.
              </p>

              {/* CTAs Principales en Pills según DESIGN.md */}
              <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 pt-2">
                <button
                  type="button"
                  onClick={onSelectBuyer}
                  className="py-4 px-8 rounded-pill bg-lime text-forest font-bold text-sm sm:text-base hover:bg-lime-hover active:scale-[0.99] transition-all flex items-center justify-center gap-2 cursor-pointer shadow-card"
                >
                  <span>Soy comprador</span>
                  <ArrowRight className="w-4 h-4" />
                </button>

                <button
                  type="button"
                  onClick={onSelectSupplier}
                  className="py-4 px-8 rounded-pill bg-paper border border-forest text-forest font-bold text-sm sm:text-base hover:bg-fog active:scale-[0.99] transition-all flex items-center justify-center gap-2 cursor-pointer"
                >
                  <span>Soy proveedor</span>
                </button>
              </div>

              <div className="flex flex-wrap items-center gap-4 text-xs text-charcoal/70 pt-2">
                <div className="flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                  <span>Sin trámites bancarios</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                  <span>5% costo fijo plano</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                  <span>En proceso de afiliación a la SBS</span>
                </div>
              </div>
            </div>

            {/* Mockup Real de la App Kuota (Home Screen) (Derecha) */}
            <div className="lg:col-span-5 flex justify-center">
              <div className="w-full max-w-[340px] bg-paper rounded-[32px] p-4 border-4 border-forest shadow-2xl space-y-4">
                {/* Cabecera del mockup */}
                <div className="flex items-center justify-between pb-2 border-b border-fog">
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 rounded-pill bg-forest text-lime flex items-center justify-center font-black text-xs">
                      K
                    </div>
                    <div>
                      <div className="text-xs font-bold text-obsidian">Bodega San Martín</div>
                      <div className="text-[10px] text-pebble">José Morales · Lima</div>
                    </div>
                  </div>
                  <span className="px-2.5 py-0.5 rounded-pill bg-linen text-forest text-[11px] font-bold">
                    Nivel 1
                  </span>
                </div>

                {/* Tarjeta de Línea de Crédito */}
                <div className="bg-forest rounded-card p-4 text-paper space-y-3">
                  <div className="flex justify-between items-start">
                    <div>
                      <div className="text-[10px] text-lime font-bold uppercase tracking-wider">
                        Línea de crédito disponible
                      </div>
                      <div className="text-2xl font-black text-paper tabular-nums mt-0.5">
                        S/ 700.00
                      </div>
                    </div>
                    <span className="text-[10px] px-2 py-0.5 rounded-pill bg-spruce text-lime font-bold">
                      Inicial 30%
                    </span>
                  </div>
                  <div className="w-full bg-spruce h-1.5 rounded-pill overflow-hidden">
                    <div className="bg-lime h-full w-full rounded-pill" />
                  </div>
                  <div className="flex justify-between text-[10px] text-paper/80">
                    <span>Monto inicial activado</span>
                    <span>Pagos semanales</span>
                  </div>
                </div>

                {/* Botón Central Comprar QR */}
                <div className="p-3 bg-linen rounded-card border border-spruce/20 text-center space-y-2">
                  <div className="text-xs font-bold text-forest">
                    ¿Vas a abastecer mercadería hoy?
                  </div>
                  <button
                    type="button"
                    onClick={onSelectBuyer}
                    className="w-full py-2.5 px-4 rounded-pill bg-forest text-lime font-bold text-xs flex items-center justify-center gap-2 shadow-sm"
                  >
                    <QrCode className="w-4 h-4" />
                    <span>Escanear QR del proveedor</span>
                  </button>
                </div>

                {/* Resumen de Próxima Cuota */}
                <div className="p-3 bg-fog rounded-card border border-fog space-y-1.5">
                  <div className="flex justify-between items-center text-[11px]">
                    <span className="text-charcoal font-semibold">Próxima cuota (Semana 1)</span>
                    <span className="font-bold text-forest tabular-nums">S/ 128.62</span>
                  </div>
                  <div className="flex justify-between items-center text-[10px] text-pebble">
                    <span>Costo Kuota (5% fijo)</span>
                    <span>Sin moras abusivas</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 2. PROBLEMA (PRD §2) */}
      <ProblemSection />

      {/* 3. CÓMO FUNCIONA (5 PASOS ANIMADOS) (PRD §2) */}
      <FiveStepsSection />

      {/* 4. DOS SOLUCIONES (PRD §2) */}
      <TwoSolutionsSection
        onSelectBuyer={onSelectBuyer}
        onSelectSupplier={onSelectSupplier}
      />

      {/* 5. NIVELES (PRD §2) */}
      <LevelsLadderSection />

      {/* 6. CONFIANZA Y RESPALDO (PRD §2) */}
      <TrustSection />

      {/* 7. PREGUNTAS FRECUENTES (PRD §2) */}
      <FaqSection audience="general" />

      {/* 8. FORMULARIO DE WAITLIST (PRD §2 y §5) */}
      <section className="py-16 md:py-24 bg-fog/30" id="waitlist">
        <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
          <WaitlistForm defaultRole="ambos" />
        </div>
      </section>
    </div>
  );
};

import React from 'react';
import { ShieldCheck, Lock, Landmark, CheckCircle2 } from 'lucide-react';

export const TrustSection: React.FC = () => {
  return (
    <section id="confianza" className="py-16 md:py-24 bg-fog/40 border-b border-fog">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
        <div className="text-center max-w-2xl mx-auto mb-12">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-pill bg-linen text-forest text-xs font-semibold mb-3">
            <ShieldCheck className="w-3.5 h-3.5" />
            <span>Seguridad, legalidad y respaldo</span>
          </div>
          <h2 className="text-3xl md:text-4xl font-black text-obsidian tracking-tight">
            TRANSPARENCIA TOTAL EN CADA OPERACIÓN
          </h2>
          <p className="text-sm md:text-base text-charcoal mt-2">
            Construimos tecnología pensada en el crecimiento real del microempresario peruano, con estándares institucionales y sin letras chicas.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Tarjeta 1: En proceso de afiliación a la SBS */}
          <div className="bg-paper p-6 rounded-card border border-fog shadow-subtle flex flex-col justify-between">
            <div className="space-y-4">
              <div className="w-10 h-10 rounded-pill bg-linen text-forest flex items-center justify-center">
                <Landmark className="w-5 h-5 text-forest" />
              </div>
              <h3 className="text-lg font-bold text-obsidian">
                En proceso de afiliación a la SBS
              </h3>
              <p className="text-xs md:text-sm text-charcoal/80 leading-relaxed">
                Operamos bajo el marco regulatorio peruano para empresas fintech de crédito y recaudación, en proceso formal de afiliación ante la Superintendencia de Banca, Seguros y AFP (SBS).
              </p>
            </div>
            <div className="mt-4 pt-4 border-t border-fog flex items-center gap-2 text-xs font-semibold text-spruce">
              <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
              <span>Conforme a normativa peruana</span>
            </div>
          </div>

          {/* Tarjeta 2: Kuota no toca tu dinero */}
          <div className="bg-paper p-6 rounded-card border border-fog shadow-subtle flex flex-col justify-between">
            <div className="space-y-4">
              <div className="w-10 h-10 rounded-pill bg-linen text-forest flex items-center justify-center">
                <Lock className="w-5 h-5 text-forest" />
              </div>
              <h3 className="text-lg font-bold text-obsidian">
                Kuota no toca tu dinero
              </h3>
              <p className="text-xs md:text-sm text-charcoal/80 leading-relaxed">
                Los cobros y pagos pasan exclusivamente a través de pasarelas y plataformas intermediarias autorizadas con relación bancaria directa. Tu dinero va del cliente a tu cuenta bancaria.
              </p>
            </div>
            <div className="mt-4 pt-4 border-t border-fog flex items-center gap-2 text-xs font-semibold text-spruce">
              <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
              <span>Cuentas bancarias directas</span>
            </div>
          </div>

          {/* Tarjeta 3: Respaldo Científica del Sur */}
          <div className="bg-paper p-6 rounded-card border border-fog shadow-subtle flex flex-col justify-between">
            <div className="space-y-4">
              <div className="h-10 flex items-center">
                <img
                  src="/logos/cientifica-del-sur.png"
                  alt="Universidad Científica del Sur"
                  className="h-8 w-auto object-contain"
                />
              </div>
              <h3 className="text-lg font-bold text-obsidian">
                Respaldo: Científica del Sur
              </h3>
              <p className="text-xs md:text-sm text-charcoal/80 leading-relaxed">
                Proyecto incubado y respaldado con rigor metodológico e investigación de campo en microfinanzas junto a la Universidad Científica del Sur, validado con más de 100 bodegueros.
              </p>
            </div>
            <div className="mt-4 pt-4 border-t border-fog flex items-center gap-2 text-xs font-semibold text-spruce">
              <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
              <span>Validación académica e investigación</span>
            </div>
          </div>
        </div>

        {/* Banner de Cuotas Fijas */}
        <div className="mt-8 p-5 rounded-card bg-forest text-paper flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="space-y-1 text-center sm:text-left">
            <div className="text-sm font-bold text-lime">
              Cero comisiones sorpresas ni gastos de cobranza abusivos
            </div>
            <div className="text-xs text-paper/80">
              Siempre sabrás exactamente cuánto pagarás cada semana antes de confirmar cualquier compra.
            </div>
          </div>
          <div className="shrink-0">
            <span className="px-4 py-2 rounded-pill bg-lime text-forest text-xs font-bold uppercase tracking-wider">
              5% Costo Fijo
            </span>
          </div>
        </div>
      </div>
    </section>
  );
};

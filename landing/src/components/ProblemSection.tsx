import React from 'react';
import { AlertTriangle, TrendingDown, CreditCard, HeartHandshake } from 'lucide-react';

export const ProblemSection: React.FC = () => {
  return (
    <section className="py-16 md:py-24 bg-paper border-b border-fog">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
        <div className="text-center max-w-2xl mx-auto mb-14">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-pill bg-fog text-charcoal text-xs font-semibold mb-3">
            <AlertTriangle className="w-3.5 h-3.5 text-forest" />
            <span>La realidad de emprender en el Perú</span>
          </div>
          <h2 className="text-3xl md:text-4xl font-black text-obsidian tracking-tight uppercase">
            EL CRÉDITO TRADICIONAL LE DA LA ESPALDA A QUIEN MÁS LO TRABAJA
          </h2>
          <p className="text-sm md:text-base text-charcoal/80 mt-2">
            Las micro y pequeñas empresas mueven el país, pero enfrentan barreras de liquidez y costos financieros desproporcionados.
          </p>
        </div>

        {/* 3 Cifras clave del PRD */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="p-6 rounded-card bg-fog/40 border border-fog hover:border-pebble/40 transition-colors">
            <div className="text-4xl md:text-5xl font-black text-obsidian tabular-nums tracking-tight">
              99,2%
            </div>
            <div className="text-sm font-bold text-forest mt-2">
              De las empresas en Perú son MYPEs
            </div>
            <p className="text-xs text-charcoal/80 mt-2 leading-relaxed">
              Generan más del 60% del empleo nacional, pero la mayoría queda fuera del crédito bancario ágil por falta de estados financieros formales.
            </p>
          </div>

          <div className="p-6 rounded-card bg-fog/40 border border-fog hover:border-pebble/40 transition-colors">
            <div className="text-4xl md:text-5xl font-black text-alarm tabular-nums tracking-tight">
              8 de 10
            </div>
            <div className="text-sm font-bold text-forest mt-2">
              Negocios cierran antes de cumplir el primer año
            </div>
            <p className="text-xs text-charcoal/80 mt-2 leading-relaxed">
              La principal causa no es la falta de clientes, sino el estrangulamiento de liquidez: comprar mercadería al contado antes de haber cobrado.
            </p>
          </div>

          <div className="p-6 rounded-card bg-fog/40 border border-fog hover:border-pebble/40 transition-colors">
            <div className="text-4xl md:text-5xl font-black text-obsidian tabular-nums tracking-tight">
              3,5% – 5%
            </div>
            <div className="text-sm font-bold text-forest mt-2">
              Comisión de los POS tradicionales
            </div>
            <p className="text-xs text-charcoal/80 mt-2 leading-relaxed">
              Los terminales de tarjeta físicos cobran alquiler mensual, costos de mantenimiento y comisiones elevadas que se comen el margen comercial del vendedor.
            </p>
          </div>
        </div>

        {/* Frase destacada del PRD */}
        <div className="mt-12 p-8 rounded-card bg-linen border border-spruce/20 text-center max-w-3xl mx-auto space-y-2">
          <HeartHandshake className="w-8 h-8 text-forest mx-auto mb-1" />
          <blockquote className="text-xl md:text-2xl font-extrabold text-forest tracking-tight">
            "Quien trabaja cada día merece confianza para crecer."
          </blockquote>
          <p className="text-xs md:text-sm text-forest/80">
            En Kuota reemplazamos el papeleo bancario y la usura informal con tecnología de microcrédito transparente y trazable.
          </p>
        </div>
      </div>
    </section>
  );
};

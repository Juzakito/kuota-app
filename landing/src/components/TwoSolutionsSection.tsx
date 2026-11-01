import React from 'react';
import { ShoppingBag, CreditCard, ArrowRight, CheckCircle2, ShieldCheck, Zap } from 'lucide-react';

export interface TwoSolutionsSectionProps {
  onSelectBuyer: () => void;
  onSelectSupplier: () => void;
}

export const TwoSolutionsSection: React.FC<TwoSolutionsSectionProps> = ({
  onSelectBuyer,
  onSelectSupplier,
}) => {
  return (
    <section className="py-16 md:py-24 bg-paper border-b border-fog" id="soluciones">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
        <div className="text-center max-w-2xl mx-auto mb-14">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-pill bg-linen text-forest text-xs font-semibold mb-3">
            <span>Diseñado para ambos lados del mostrador</span>
          </div>
          <h2 className="text-3xl md:text-4xl font-black text-obsidian tracking-tight uppercase">
            DOS SOLUCIONES. UN SOLO ECOSISTEMA DE CONFIANZA.
          </h2>
          <p className="text-sm md:text-base text-charcoal/80 mt-2">
            Tanto si necesitas abastecer tu negocio con mercadería como si quieres cobrar con menores comisiones y cero riesgo.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Solución 1: Comprar al Crédito */}
          <div className="bg-fog/30 rounded-hero p-8 border border-fog hover:border-forest/40 transition-all flex flex-col justify-between shadow-subtle">
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <div className="w-12 h-12 rounded-pill bg-linen text-forest flex items-center justify-center">
                  <ShoppingBag className="w-6 h-6 text-forest" />
                </div>
                <span className="text-xs px-3 py-1 rounded-pill bg-forest text-lime font-bold uppercase tracking-wider">
                  Para Compradores
                </span>
              </div>

              <div>
                <h3 className="text-2xl font-black text-obsidian tracking-tight">
                  Comprar al crédito
                </h3>
                <p className="text-sm text-charcoal/80 mt-2 leading-relaxed">
                  Financia tus compras de mercadería directamente en mayoristas y distribuidores aliados. Paga tu inicial y liquida el saldo en semanas con solo 5% fijo.
                </p>
              </div>

              <div className="space-y-2.5 pt-2 text-xs md:text-sm text-charcoal">
                <div className="flex items-center gap-2.5">
                  <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                  <span>Desde S/ 199 hasta S/ 700 en Nivel 1.</span>
                </div>
                <div className="flex items-center gap-2.5">
                  <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                  <span>Plazos semanales de 15, 30 y 60 días.</span>
                </div>
                <div className="flex items-center gap-2.5">
                  <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                  <span>5% fijo sobre el monto financiado, sin variaciones.</span>
                </div>
              </div>
            </div>

            <div className="pt-8 mt-6 border-t border-fog">
              <button
                type="button"
                onClick={onSelectBuyer}
                className="w-full py-3.5 px-6 rounded-pill bg-lime text-forest font-bold text-sm hover:bg-lime-hover transition-colors flex items-center justify-center gap-2 cursor-pointer shadow-subtle"
              >
                <span>Conocer cómo comprar al crédito</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* Solución 2: Kuota Payments */}
          <div className="bg-forest rounded-hero p-8 text-paper flex flex-col justify-between shadow-card">
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <div className="w-12 h-12 rounded-pill bg-spruce text-lime flex items-center justify-center">
                  <CreditCard className="w-6 h-6 text-lime" />
                </div>
                <span className="text-xs px-3 py-1 rounded-pill bg-spruce text-lime font-bold uppercase tracking-wider">
                  Para Proveedores
                </span>
              </div>

              <div>
                <h3 className="text-2xl font-black text-paper tracking-tight">
                  Kuota Payments
                </h3>
                <p className="text-sm text-paper/80 mt-2 leading-relaxed">
                  Cobra a tus clientes en mostrador con QR y a distancia con Link por WhatsApp. Acepta todas las tarjetas bancarias, Yape y Plin con solo 2% de comisión.
                </p>
              </div>

              <div className="space-y-2.5 pt-2 text-xs md:text-sm text-paper/90">
                <div className="flex items-center gap-2.5">
                  <CheckCircle2 className="w-4 h-4 text-lime shrink-0" />
                  <span>2% de comisión por transacción (vs. 3,5% - 5% de POS tradicional).</span>
                </div>
                <div className="flex items-center gap-2.5">
                  <CheckCircle2 className="w-4 h-4 text-lime shrink-0" />
                  <span>Sin comprar aparatos POS ni pagar alquiler mensual abusivo.</span>
                </div>
                <div className="flex items-center gap-2.5">
                  <CheckCircle2 className="w-4 h-4 text-lime shrink-0" />
                  <span>Liquidación en 48 horas hábiles directo a tu cuenta bancaria.</span>
                </div>
              </div>
            </div>

            <div className="pt-8 mt-6 border-t border-spruce">
              <button
                type="button"
                onClick={onSelectSupplier}
                className="w-full py-3.5 px-6 rounded-pill bg-paper text-forest font-bold text-sm hover:bg-fog transition-colors flex items-center justify-center gap-2 cursor-pointer"
              >
                <span>Ver soluciones para proveedores</span>
                <ArrowRight className="w-4 h-4 text-forest" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

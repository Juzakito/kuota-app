import React from 'react';
import { ArrowRight, QrCode, ShieldCheck, CheckCircle2, Store, Shirt, Wrench, Laptop, ShoppingCart, Sparkles } from 'lucide-react';
import { BuyerCreditCalculator } from './BuyerCreditCalculator';
import { LevelsLadderSection } from './LevelsLadderSection';
import { TrustSection } from './TrustSection';
import { FaqSection } from './FaqSection';
import { WaitlistForm } from './WaitlistForm';

export const CompradoresLanding: React.FC = () => {
  const scrollToCalculator = () => {
    const el = document.getElementById('calculadora');
    if (el) el.scrollIntoView({ behavior: 'smooth' });
  };

  const scrollToWaitlist = () => {
    const el = document.getElementById('waitlist-comprador');
    if (el) el.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <div className="space-y-0">
      {/* 1. HERO COMPRADORES */}
      <section className="relative overflow-hidden bg-paper pt-12 md:pt-20 pb-16 md:pb-24 border-b border-fog">
        <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            <div className="lg:col-span-7 space-y-6 text-left">
              <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-pill bg-linen text-forest text-xs font-bold">
                <span>Para bodegas, minimarkets y microempresarios</span>
              </div>

              {/* Titular oficial PRD §3 */}
              <h1 className="text-4xl sm:text-6xl lg:text-7xl font-black text-obsidian tracking-[-0.035em] leading-[0.98] uppercase">
                COMPRA HOY LO QUE AYER NO PODÍAS. PAGA EN CUOTAS SEMANALES.
              </h1>

              <p className="text-base sm:text-lg text-charcoal/80 max-w-xl font-normal leading-relaxed">
                Adquiere mercadería para tu negocio en comercios y distribuidores afiliados. Paga tu inicial y financia el saldo en semanas con solo 5% fijo. Sin trámites bancarios ni usura informal.
              </p>

              <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 pt-2">
                <button
                  type="button"
                  onClick={scrollToCalculator}
                  className="py-4 px-8 rounded-pill bg-lime text-forest font-bold text-sm sm:text-base hover:bg-lime-hover active:scale-[0.99] transition-all flex items-center justify-center gap-2 cursor-pointer shadow-card"
                >
                  <span>Calcular mi compra</span>
                  <ArrowRight className="w-4 h-4" />
                </button>

                <button
                  type="button"
                  onClick={scrollToWaitlist}
                  className="py-4 px-8 rounded-pill bg-paper border border-forest text-forest font-bold text-sm sm:text-base hover:bg-fog active:scale-[0.99] transition-all flex items-center justify-center gap-2 cursor-pointer"
                >
                  <span>Unirme a la lista de espera</span>
                </button>
              </div>

              <div className="flex flex-wrap items-center gap-4 text-xs text-charcoal/70 pt-2">
                <div className="flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                  <span>Sin papeleos bancarios</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                  <span>5% costo fijo no acumulativo</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                  <span>Pagos en 15, 30 o 60 días</span>
                </div>
              </div>
            </div>

            {/* Mockup de Compra en Mostrador */}
            <div className="lg:col-span-5 flex justify-center">
              <div className="w-full max-w-[340px] bg-paper rounded-[32px] p-5 border-4 border-forest shadow-2xl space-y-4">
                <div className="text-center pb-2 border-b border-fog">
                  <div className="text-xs font-bold text-forest uppercase tracking-wider">
                    Solicitud de Compra
                  </div>
                  <div className="text-sm font-extrabold text-obsidian mt-0.5">
                    Mayorista Santa Anita
                  </div>
                </div>

                <div className="p-4 bg-fog/70 rounded-card space-y-2 text-xs">
                  <div className="flex justify-between">
                    <span className="text-charcoal">Monto compra:</span>
                    <span className="font-extrabold text-obsidian">S/ 700.00</span>
                  </div>
                  <div className="flex justify-between text-forest font-bold">
                    <span>Inicial hoy (30%):</span>
                    <span>S/ 210.00</span>
                  </div>
                  <div className="flex justify-between text-pebble">
                    <span>Saldo financiado:</span>
                    <span>S/ 490.00</span>
                  </div>
                  <div className="flex justify-between text-spruce font-semibold">
                    <span>Costo Kuota (5% fijo):</span>
                    <span>S/ 24.50</span>
                  </div>
                  <div className="pt-2 border-t border-pebble/20 flex justify-between font-black text-forest text-sm">
                    <span>Total en cuotas:</span>
                    <span>S/ 514.50</span>
                  </div>
                </div>

                <div className="p-3 bg-linen rounded-card border border-spruce/20 text-center">
                  <div className="text-[11px] text-forest font-bold">
                    4 cuotas semanales de S/ 128.62
                  </div>
                  <div className="text-[10px] text-spruce mt-0.5">
                    Última cuota absorbe céntimos residuales
                  </div>
                </div>

                <button
                  type="button"
                  onClick={scrollToCalculator}
                  className="w-full py-2.5 rounded-pill bg-forest text-lime font-bold text-xs"
                >
                  Probar con mi propio monto
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 2. FLUJO DE 3 PASOS EN < 1 MINUTO (PRD §3) */}
      <section className="py-14 bg-fog/30 border-b border-fog">
        <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
          <div className="text-center max-w-xl mx-auto mb-10">
            <h2 className="text-2xl md:text-3xl font-black text-obsidian uppercase tracking-tight">
              TU COMPRA LISTA EN MENOS DE 1 MINUTO
            </h2>
            <p className="text-xs md:text-sm text-charcoal/80 mt-1">
              Directamente en la tienda o almacén de tu proveedor de confianza.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="p-6 rounded-card bg-paper border border-fog shadow-subtle space-y-3">
              <div className="w-10 h-10 rounded-pill bg-linen text-forest flex items-center justify-center font-black text-sm">
                1
              </div>
              <h3 className="text-base font-bold text-obsidian">
                Escanea el QR Kuota
              </h3>
              <p className="text-xs text-charcoal/80 leading-relaxed">
                Llegas al mostrador del proveedor afiliado, abres tu app Kuota y escaneas el código QR del comercio.
              </p>
            </div>

            <div className="p-6 rounded-card bg-paper border border-fog shadow-subtle space-y-3">
              <div className="w-10 h-10 rounded-pill bg-linen text-forest flex items-center justify-center font-black text-sm">
                2
              </div>
              <h3 className="text-base font-bold text-obsidian">
                Paga tu inicial al instante
              </h3>
              <p className="text-xs text-charcoal/80 leading-relaxed">
                Cancela tu inicial desde el 30% usando Yape, Plin o tu cuenta de ahorros directamente al comercio.
              </p>
            </div>

            <div className="p-6 rounded-card bg-paper border border-fog shadow-subtle space-y-3">
              <div className="w-10 h-10 rounded-pill bg-linen text-forest flex items-center justify-center font-black text-sm">
                3
              </div>
              <h3 className="text-base font-bold text-obsidian">
                Lleva tu mercadería y paga semanal
              </h3>
              <p className="text-xs text-charcoal/80 leading-relaxed">
                Despachas tu pedido de inmediato y pagas el saldo en cuotas semanales acordes a las ventas de tu negocio.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* 3. CALCULADORA DE COMPRADORES (PRD §3.1) */}
      <section className="py-16 md:py-24 bg-paper border-b border-fog">
        <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
          <BuyerCreditCalculator onProceedToWaitlist={scrollToWaitlist} />
        </div>
      </section>

      {/* 4. PARA QUIÉN ES KUOTA (PRD §3) */}
      <section className="py-16 bg-fog/30 border-b border-fog">
        <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
          <div className="text-center max-w-2xl mx-auto mb-12">
            <h2 className="text-3xl font-black text-obsidian tracking-tight uppercase">
              DISEÑADO PARA LOS NEGOCIOS DEL DÍA A DÍA
            </h2>
            <p className="text-sm text-charcoal/80 mt-1">
              Soluciones a la medida de los rubros que mueven la economía real.
            </p>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
            {[
              { icon: <Store className="w-6 h-6 text-forest" />, title: 'Bodegas y Minimarkets', desc: 'Bebidas, abarrotes y snacks' },
              { icon: <Shirt className="w-6 h-6 text-forest" />, title: 'Textil y Confección', desc: 'Telas, insumos e hilos' },
              { icon: <Wrench className="w-6 h-6 text-forest" />, title: 'Ferreterías', desc: 'Herramientas y materiales' },
              { icon: <ShoppingCart className="w-6 h-6 text-forest" />, title: 'Abarrotes y Mercados', desc: 'Sacos y cajas al por mayor' },
              { icon: <Laptop className="w-6 h-6 text-forest" />, title: 'Revendedores Digitales', desc: 'Accesorios y tecnología' },
            ].map((rubro) => (
              <div key={rubro.title} className="p-5 rounded-card bg-paper border border-fog text-center space-y-2 shadow-subtle">
                <div className="w-12 h-12 rounded-pill bg-linen flex items-center justify-center mx-auto">
                  {rubro.icon}
                </div>
                <div className="text-sm font-bold text-obsidian">{rubro.title}</div>
                <div className="text-xs text-charcoal/70">{rubro.desc}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* 5. NIVELES DE CRÉDITO */}
      <LevelsLadderSection />

      {/* 6. TRANSPARENCIA Y CONFIANZA */}
      <TrustSection />

      {/* 7. PREGUNTAS FRECUENTES COMPRADORES */}
      <FaqSection audience="compradores" />

      {/* 8. FORMULARIO DE WAITLIST */}
      <section className="py-16 md:py-24 bg-fog/30" id="waitlist-comprador">
        <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
          <WaitlistForm
            defaultRole="comprador"
            title="Únete a la lista de espera de compradores"
            subtitle="Regístrate para ser de los primeros negocios en financiar mercadería con Kuota al iniciar en tu zona."
          />
        </div>
      </section>
    </div>
  );
};

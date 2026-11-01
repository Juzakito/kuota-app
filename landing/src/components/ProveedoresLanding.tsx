import React from 'react';
import { ArrowRight, QrCode, CreditCard, ShieldCheck, CheckCircle2, MessageSquare, Calendar, Sparkles, Building2 } from 'lucide-react';
import { SupplierSavingsCalculator } from './SupplierSavingsCalculator';
import { TrustSection } from './TrustSection';
import { FaqSection } from './FaqSection';
import { WaitlistForm } from './WaitlistForm';
import { OFFICIAL_CONTACT } from '../utils/leadsService';

export const ProveedoresLanding: React.FC = () => {
  const scrollToCalculator = () => {
    const el = document.getElementById('calculadora-ahorro');
    if (el) el.scrollIntoView({ behavior: 'smooth' });
  };

  const scrollToWaitlist = () => {
    const el = document.getElementById('waitlist-proveedor');
    if (el) el.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <div className="space-y-0">
      {/* 1. HERO PROVEEDORES */}
      <section className="relative overflow-hidden bg-forest text-paper pt-12 md:pt-20 pb-16 md:pb-24 border-b border-spruce">
        <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            <div className="lg:col-span-7 space-y-6 text-left">
              <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-pill bg-spruce text-lime text-xs font-bold">
                <span>Para distribuidores, mayoristas y comercios</span>
              </div>

              {/* Titular oficial PRD §4 */}
              <h1 className="text-4xl sm:text-6xl lg:text-7xl font-black text-paper tracking-[-0.035em] leading-[0.98] uppercase">
                VENDE AL CRÉDITO SIN RIESGO DE IMPAGO.
              </h1>

              <p className="text-base sm:text-lg text-paper/80 max-w-xl font-normal leading-relaxed">
                Cobra con tarjeta, Yape y Plin con solo 2% de comisión. Vende a tus clientes recurrentes sin que asumas el riesgo de cobranza. Liquidación en 48 horas hábiles.
              </p>

              <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 pt-2">
                <a
                  href={OFFICIAL_CONTACT.calendlyUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="py-4 px-8 rounded-pill bg-lime text-forest font-bold text-sm sm:text-base hover:bg-lime-hover active:scale-[0.99] transition-all flex items-center justify-center gap-2 cursor-pointer shadow-card"
                >
                  <Calendar className="w-4 h-4" />
                  <span>Agendar reunión de 30 min</span>
                </a>

                <button
                  type="button"
                  onClick={scrollToCalculator}
                  className="py-4 px-8 rounded-pill bg-paper border border-paper text-forest font-bold text-sm sm:text-base hover:bg-fog active:scale-[0.99] transition-all flex items-center justify-center gap-2 cursor-pointer"
                >
                  <span>Calcular mi ahorro vs POS</span>
                </button>
              </div>

              <div className="flex flex-wrap items-center gap-4 text-xs text-paper/70 pt-2">
                <div className="flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-lime shrink-0" />
                  <span>2% por transacción fija</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-lime shrink-0" />
                  <span>Sin costo por aparato POS</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-lime shrink-0" />
                  <span>Liquidación en 48 h hábiles</span>
                </div>
              </div>
            </div>

            {/* Mockup "Mi QR" Proveedor (Derecha) */}
            <div className="lg:col-span-5 flex justify-center">
              <div className="w-full max-w-[340px] bg-paper text-charcoal rounded-[32px] p-6 border-4 border-lime shadow-2xl space-y-4 text-center">
                <div className="text-xs font-extrabold text-forest uppercase tracking-wider">
                  Mi QR Kuota en Mostrador
                </div>
                <div className="text-lg font-black text-obsidian">
                  Distribuidora Central S.A.C.
                </div>

                {/* Código QR Ilustrativo */}
                <div className="w-44 h-44 mx-auto bg-fog p-3 rounded-card border-2 border-forest flex flex-col items-center justify-center">
                  <QrCode className="w-32 h-32 text-forest" />
                </div>

                <div className="text-xs text-charcoal font-semibold">
                  Acepta BCP, BBVA, Interbank, Scotiabank, Yape y Plin
                </div>

                <div className="p-3 bg-linen rounded-card border border-spruce/20 text-xs text-forest space-y-1">
                  <div className="font-bold">Comisión fija de 2%</div>
                  <div className="text-[11px] text-spruce">Dinero directo en tu cuenta bancaria</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 2. DOS PRODUCTOS CLAROS (PRD §4.1) */}
      <section className="py-16 md:py-24 bg-paper border-b border-fog">
        <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
          <div className="text-center max-w-2xl mx-auto mb-14">
            <h2 className="text-3xl md:text-4xl font-black text-obsidian tracking-tight uppercase">
              DOS FORMAS DE POTENCIAR TUS VENTAS
            </h2>
            <p className="text-sm md:text-base text-charcoal/80 mt-2">
              Cobranza ágil digital hoy y crédito al comprador sin riesgo de mora mañana.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            {/* Producto A: Kuota Payments (Activo) */}
            <div className="p-8 rounded-hero bg-fog/30 border border-fog space-y-6 shadow-subtle flex flex-col justify-between">
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="px-3 py-1 rounded-pill bg-linen text-forest text-xs font-bold uppercase">
                    Producto Activo
                  </span>
                  <span className="text-sm font-extrabold text-forest">2% de comisión</span>
                </div>
                <h3 className="text-2xl font-black text-obsidian">
                  Kuota Payments
                </h3>
                <p className="text-sm text-charcoal/80 leading-relaxed">
                  Cobra en mostrador con tu código QR oficial y a clientes a distancia mediante Links por WhatsApp con monto, teléfono y concepto precargados.
                </p>
                <div className="space-y-2 text-xs md:text-sm text-charcoal pt-2">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span>Sin alquiler mensual abusivo de equipos POS.</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span>Acepta tarjetas de crédito, débito, Yape y Plin.</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span>Liquidación bancaria automática en 48 horas hábiles.</span>
                  </div>
                </div>
              </div>

              <div className="pt-4 border-t border-fog">
                <button
                  type="button"
                  onClick={scrollToCalculator}
                  className="w-full py-3 px-4 rounded-pill bg-lime text-forest font-bold text-xs md:text-sm hover:bg-lime-hover transition-colors flex items-center justify-center gap-2 shadow-sm"
                >
                  <span>Calcular ahorro con Kuota Payments</span>
                  <ArrowRight className="w-4 h-4" />
                </button>
              </div>
            </div>

            {/* Producto B: Vender a Kuotas (aKuotas!) (Próximamente) */}
            <div className="p-8 rounded-hero bg-fog/30 border border-fog space-y-6 shadow-subtle flex flex-col justify-between opacity-90">
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="px-3 py-1 rounded-pill bg-paper border border-pebble/30 text-pebble text-xs font-bold uppercase">
                    Lo que viene · Fase 2
                  </span>
                  <span className="text-xs font-bold text-spruce">Plan Open S/ 99 + IGV</span>
                </div>
                <h3 className="text-2xl font-black text-obsidian">
                  Vender a Kuotas (aKuotas!)
                </h3>
                <p className="text-sm text-charcoal/80 leading-relaxed">
                  Permite a tus clientes comprar mercadería en cuotas semanales mientras tú cobras tu venta por adelantado. El 100% del riesgo de mora lo asume Kuota.
                </p>
                <div className="space-y-2 text-xs md:text-sm text-charcoal pt-2">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span>Tus clientes compran más volumen al pagar en cuotas.</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span>Recibes un adelanto al instante y el saldo en semanas.</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span>Cero cobranza manual: nosotros gestionamos los cobros.</span>
                  </div>
                </div>
              </div>

              <div className="pt-4 border-t border-fog">
                <button
                  type="button"
                  onClick={scrollToWaitlist}
                  className="w-full py-3 px-4 rounded-pill bg-forest text-lime font-bold text-xs md:text-sm hover:bg-forest-dark transition-colors flex items-center justify-center gap-2"
                >
                  <span>Registrarme para acceso prioritario</span>
                  <ArrowRight className="w-4 h-4 text-lime" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 3. CALCULADORA DE BOLSILLO PROVEEDORES (PRD §4.2) */}
      <section className="py-16 md:py-24 bg-fog/20 border-b border-fog">
        <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
          <SupplierSavingsCalculator onScheduleCall={() => window.open(OFFICIAL_CONTACT.calendlyUrl, '_blank')} />
        </div>
      </section>

      {/* 4. TARJETA INFORMATIVA: VENDER AL CRÉDITO (PRD §4.3) */}
      <section className="py-16 bg-paper border-b border-fog">
        <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
          <div className="p-8 md:p-10 rounded-hero bg-forest text-paper space-y-6">
            <div className="max-w-2xl">
              <span className="text-xs px-3 py-1 rounded-pill bg-spruce text-lime font-bold uppercase tracking-wider">
                Modelo de liquidación garantizada
              </span>
              <h2 className="text-2xl md:text-3xl font-black text-paper tracking-tight mt-3">
                ¿CÓMO FUNCIONA UNA VENTA AL CRÉDITO DE S/ 1,000?
              </h2>
              <p className="text-xs md:text-sm text-paper/80 mt-2 leading-relaxed">
                Ejemplo real de flujo de fondos con comisión transparente y sin sorpresas.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 pt-2">
              <div className="p-4 rounded-card bg-spruce/60 border border-spruce space-y-1">
                <div className="text-[11px] text-paper/70">Monto de la venta</div>
                <div className="text-xl font-black text-paper tabular-nums">S/ 1,000.00</div>
                <div className="text-[10px] text-paper/60">Mercadería despachada</div>
              </div>

              <div className="p-4 rounded-card bg-spruce/60 border border-spruce space-y-1">
                <div className="text-[11px] text-paper/70">Neto para el proveedor</div>
                <div className="text-xl font-black text-lime tabular-nums">S/ 980.00</div>
                <div className="text-[10px] text-paper/60">Comisión Kuota: 2% (S/ 20)</div>
              </div>

              <div className="p-4 rounded-card bg-spruce/60 border border-spruce space-y-1">
                <div className="text-[11px] text-paper/70">Adelanto al instante</div>
                <div className="text-xl font-black text-paper tabular-nums">S/ 600.00</div>
                <div className="text-[10px] text-paper/60">Inicial + desembolso inmediato</div>
              </div>

              <div className="p-4 rounded-card bg-spruce/60 border border-spruce space-y-1">
                <div className="text-[11px] text-paper/70">Saldo restante</div>
                <div className="text-xl font-black text-paper tabular-nums">S/ 380.00</div>
                <div className="text-[10px] text-paper/60">En menos de 30 días garantizado</div>
              </div>
            </div>

            <div className="p-4 rounded-card bg-spruce border border-lime/30 text-xs text-lime font-bold text-center">
              * El riesgo de mora o impago lo asume 100% Kuota. Tu empresa nunca pierde su mercadería ni asume juicios de cobranza.
            </div>
          </div>
        </div>
      </section>

      {/* 5. PLANES Y CONDICIONES VISIBLES (PRD §4.1) */}
      <section className="py-16 md:py-24 bg-fog/30 border-b border-fog" id="planes">
        <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
          <div className="text-center max-w-2xl mx-auto mb-14">
            <h2 className="text-3xl md:text-4xl font-black text-obsidian tracking-tight uppercase">
              PLANES CLAROS Y SIN COSTOS OCULTOS
            </h2>
            <p className="text-sm md:text-base text-charcoal/80 mt-2">
              Elige el plan ideal según el volumen y la cantidad de vendedores de tu empresa.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-4xl mx-auto">
            {/* Plan Basic */}
            <div className="p-8 rounded-hero bg-paper border border-fog shadow-card flex flex-col justify-between space-y-6">
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-xs font-bold px-3 py-1 rounded-pill bg-linen text-forest uppercase">
                    Plan Basic
                  </span>
                  <span className="text-xs text-pebble">Para comercios y bodegas</span>
                </div>

                <div className="space-y-1">
                  <div className="text-3xl font-black text-obsidian">
                    S/ 80 <span className="text-sm font-normal text-pebble">+ IGV / mes</span>
                  </div>
                  <div className="text-xs text-forest font-semibold">
                    (S/ 94.40 con IGV mensual)
                  </div>
                </div>

                <div className="space-y-3 pt-3 text-xs md:text-sm text-charcoal">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span><strong>2%</strong> de comisión por transacción.</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span>Hasta <strong>S/ 5,000</strong> por transacción (mínimo S/ 199).</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span>Código QR para mostrador + Link por WhatsApp.</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span>Liquidación en 48 horas hábiles.</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span>1 usuario / administrador.</span>
                  </div>
                </div>
              </div>

              <div className="pt-4 border-t border-fog">
                <a
                  href={OFFICIAL_CONTACT.calendlyUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-full py-3.5 px-4 rounded-pill bg-forest text-lime font-bold text-xs md:text-sm hover:bg-forest-dark transition-colors flex items-center justify-center gap-2 cursor-pointer shadow-sm"
                >
                  <span>Afiliarme al Plan Basic</span>
                  <ArrowRight className="w-4 h-4" />
                </a>
              </div>
            </div>

            {/* Plan Pro */}
            <div className="p-8 rounded-hero bg-paper border-2 border-forest shadow-card flex flex-col justify-between space-y-6 relative">
              <div className="absolute -top-3 right-6 bg-lime text-forest text-[11px] font-black uppercase px-3 py-1 rounded-pill shadow-sm">
                Mayor volumen
              </div>

              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-xs font-bold px-3 py-1 rounded-pill bg-forest text-lime uppercase">
                    Plan Pro
                  </span>
                  <span className="text-xs text-pebble">Para mayoristas y distribuidoras</span>
                </div>

                <div className="space-y-1">
                  <div className="text-3xl font-black text-obsidian">
                    S/ 120 <span className="text-sm font-normal text-pebble">+ IGV / mes</span>
                  </div>
                  <div className="text-xs text-forest font-semibold">
                    (S/ 141.60 con IGV mensual)
                  </div>
                </div>

                <div className="space-y-3 pt-3 text-xs md:text-sm text-charcoal">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span><strong>2%</strong> de comisión por transacción.</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span><strong>Sin límite</strong> por transacción (más de S/ 5,000).</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span>Módulo de cobranza masiva con enlaces en lote.</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span>Liquidación preferencial en 48 horas hábiles.</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-spruce shrink-0" />
                    <span><strong>Hasta 5 usuarios</strong> (vendedores en ruta y cajeros).</span>
                  </div>
                </div>
              </div>

              <div className="pt-4 border-t border-fog">
                <a
                  href={OFFICIAL_CONTACT.calendlyUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-full py-3.5 px-4 rounded-pill bg-lime text-forest font-bold text-xs md:text-sm hover:bg-lime-hover transition-colors flex items-center justify-center gap-2 cursor-pointer shadow-sm"
                >
                  <span>Afiliarme al Plan Pro</span>
                  <ArrowRight className="w-4 h-4" />
                </a>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 6. EMBED CALENDLY (PRD §5 y §4) */}
      <section className="py-16 bg-paper border-b border-fog" id="calendly">
        <div className="max-w-[800px] mx-auto px-4 sm:px-6 text-center space-y-6">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-pill bg-linen text-forest text-xs font-semibold">
            <Calendar className="w-3.5 h-3.5" />
            <span>Reunión de 30 minutos</span>
          </div>
          <h2 className="text-3xl font-black text-obsidian tracking-tight uppercase">
            COORDINA CON UN ASESOR COMERCIAL DE KUOTA
          </h2>
          <p className="text-sm text-charcoal/80 max-w-lg mx-auto">
            Te mostramos la plataforma en vivo, calculamos la tasa de ahorro de tu negocio y habilitamos tu código QR para empezar a cobrar.
          </p>

          <div className="p-6 rounded-card bg-fog/40 border border-fog space-y-4">
            <p className="text-xs text-charcoal">
              Haz clic en el botón para seleccionar fecha y hora directamente en nuestro calendario:
            </p>
            <a
              href={OFFICIAL_CONTACT.calendlyUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 py-4 px-8 rounded-pill bg-forest text-lime font-bold text-sm hover:bg-forest-dark transition-all shadow-md"
            >
              <Calendar className="w-4 h-4" />
              <span>Abrir agenda de reuniones (Calendly)</span>
              <ArrowRight className="w-4 h-4 text-lime" />
            </a>
          </div>
        </div>
      </section>

      {/* 7. SEGURIDAD Y CONFIANZA */}
      <TrustSection />

      {/* 8. PREGUNTAS FRECUENTES PROVEEDORES */}
      <FaqSection audience="proveedores" />

      {/* 9. FORMULARIO DE WAITLIST PROVEEDORES */}
      <section className="py-16 md:py-24 bg-fog/30" id="waitlist-proveedor">
        <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
          <WaitlistForm
            defaultRole="proveedor"
            title="Afiliación y Lista de Espera de Proveedores"
            subtitle="Regístrate para recibir tu kit de cobro con QR y acceder a Kuota Payments con solo 2% de comisión."
          />
        </div>
      </section>
    </div>
  );
};

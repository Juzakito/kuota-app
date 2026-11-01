import React, { useState } from 'react';
import { HelpCircle, ChevronDown, ChevronUp } from 'lucide-react';

export interface FaqItem {
  q: string;
  a: string;
}

export interface FaqSectionProps {
  audience?: 'general' | 'compradores' | 'proveedores';
}

const GENERAL_FAQS: FaqItem[] = [
  {
    q: '¿Cómo funciona exactamente Kuota para mi negocio?',
    a: 'Kuota es un ecosistema que conecta a compradores y proveedores de microempresas. Los compradores pueden adquirir mercadería pagando una inicial y financiando el saldo en cuotas semanales con solo 5% fijo. Los proveedores cobran por adelantado y aumentan sus ventas sin asumir el riesgo de impago.',
  },
  {
    q: '¿Cuál es el costo del servicio? ¿Hay intereses ocultos?',
    a: 'No hay intereses ocultos ni letras chicas. Para compras al crédito, el costo es un 5% fijo y plano sobre el monto financiado con tope bimestral; este costo NO aumenta aunque elijas 2, 4 u 8 semanas. Para cobros con Kuota Payments, la comisión es del 2% por transacción.',
  },
  {
    q: '¿Están regulados por la SBS?',
    a: 'Kuota se encuentra en proceso formal de afiliación ante la Superintendencia de Banca, Seguros y AFP (SBS) dentro del marco regulatorio para empresas de tecnología financiera. Además, no captamos dinero del público: todos los pagos pasan por pasarelas intermediarias autorizadas con relación bancaria directa.',
  },
  {
    q: '¿Qué requisitos necesito para solicitar financiamiento?',
    a: 'DNI peruano vigente, un negocio en marcha (bodega, bazar, confección, ferretería, etc.), número de WhatsApp y una cuenta bancaria o billetera digital (Yape o Plin) a tu nombre. La evaluación se realiza en minutos mediante nuestro motor de riesgo.',
  },
  {
    q: '¿Qué pasa si me atraso en una cuota semanal?',
    a: 'Te recomendamos avisarnos de inmediato para reprogramar con anticipación. El retraso congela temporalmente tu cupo de compra y no te permite subir de nivel. Nunca cobramos penalidades abusivas ni recurrimos a prácticas informales de cobranza.',
  },
  {
    q: '¿Cómo cobra un proveedor con Kuota Payments?',
    a: 'El proveedor recibe un código QR para su mostrador y un enlace dinámico para cobrar a distancia por WhatsApp. Acepta todas las tarjetas bancarias (BCP, BBVA, Interbank, Scotiabank), Yape y Plin con solo 2% de comisión y liquidación en 48 horas hábiles a su cuenta.',
  },
];

const BUYER_FAQS: FaqItem[] = [
  {
    q: '¿Por qué el costo es 5% fijo si elijo más semanas?',
    a: 'Porque en Kuota creemos en la previsibilidad. El costo del 5% se calcula una sola vez sobre el monto que financias. Si financias S/ 490, el costo es S/ 24.50, tanto si pagas en 2 semanas como en 8 semanas. No cobramos intereses compuestos.',
  },
  {
    q: '¿Cuánto es lo máximo que puedo comprar al iniciar?',
    a: 'Todos los negocios nuevos inician en el Nivel 1 con un cupo de hasta S/ 700 y una inicial del 30%. Conforme pagas tus cuotas semanales puntualmente, desbloqueas el Nivel 2 (hasta S/ 1,200 con 25% de inicial).',
  },
  {
    q: '¿Tengo que dejar alguna garantía o firmar letras?',
    a: 'No. No pedimos títulos de propiedad ni garantías prendarias. Tu mejor garantía es tu historial de compras y tu cumplimiento semanal.',
  },
  {
    q: '¿Dónde puedo comprar con Kuota?',
    a: 'En toda nuestra red de comercios, mayoristas y distribuidores afiliados en Lima y provincias. Cada día sumamos nuevos proveedores a la red.',
  },
];

const SUPPLIER_FAQS: FaqItem[] = [
  {
    q: '¿Quién asume el riesgo de no pago si un cliente se atrasa?',
    a: 'El riesgo de crédito lo asume Kuota. Tú despachas la mercadería tranquilo y recibes tus fondos asegurados conforme al calendario pactado.',
  },
  {
    q: '¿Por qué la comisión es de solo 2% frente al 3.5% o 5% de mi POS?',
    a: 'Al operar de forma 100% digital con tecnología QR y enlaces directos, eliminamos el costo de fabricación de aparatos físicos, alquileres de hardware y márgenes abusivos de intermediarios tradicionales.',
  },
  {
    q: '¿Cuándo recibo el dinero de mis ventas?',
    a: 'Las transacciones procesadas con Kuota Payments se liquidan en 48 horas hábiles directo en la cuenta bancaria de tu negocio en cualquier banco del Perú.',
  },
  {
    q: '¿Puedo tener a mis vendedores y cajeros cobrando con la misma cuenta?',
    a: 'Sí, el Plan Pro permite hasta 5 usuarios simultáneos para que tus vendedores en ruta o cajeros en tienda cobren con el mismo QR o generen links sin acceder a tus fondos.',
  },
];

export const FaqSection: React.FC<FaqSectionProps> = ({ audience = 'general' }) => {
  const [openIndex, setOpenIndex] = useState<number | null>(0);

  const faqs =
    audience === 'compradores'
      ? BUYER_FAQS
      : audience === 'proveedores'
      ? SUPPLIER_FAQS
      : GENERAL_FAQS;

  return (
    <section className="py-16 md:py-24 bg-paper border-b border-fog" id="faq">
      <div className="max-w-[800px] mx-auto px-4 sm:px-6">
        <div className="text-center mb-12">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-pill bg-linen text-forest text-xs font-semibold mb-3">
            <HelpCircle className="w-3.5 h-3.5" />
            <span>Preguntas frecuentes</span>
          </div>
          <h2 className="text-3xl md:text-4xl font-black text-obsidian tracking-tight uppercase">
            RESOLVEMOS TUS DUDAS SIN RODEOS
          </h2>
          <p className="text-sm md:text-base text-charcoal/80 mt-2">
            Información clara sobre el funcionamiento, comisiones y seguridad de Kuota.
          </p>
        </div>

        <div className="space-y-3">
          {faqs.map((faq, idx) => {
            const isOpen = openIndex === idx;
            return (
              <div
                key={faq.q}
                className="border border-fog rounded-card overflow-hidden transition-colors bg-paper"
              >
                <button
                  type="button"
                  onClick={() => setOpenIndex(isOpen ? null : idx)}
                  className="w-full py-4 px-5 text-left flex items-center justify-between gap-4 font-bold text-sm md:text-base text-obsidian hover:bg-fog/30 transition-colors cursor-pointer"
                >
                  <span>{faq.q}</span>
                  {isOpen ? (
                    <ChevronUp className="w-4 h-4 text-forest shrink-0" />
                  ) : (
                    <ChevronDown className="w-4 h-4 text-pebble shrink-0" />
                  )}
                </button>

                {isOpen && (
                  <div className="px-5 pb-5 pt-1 text-xs md:text-sm text-charcoal/80 leading-relaxed border-t border-fog/60 bg-fog/20">
                    {faq.a}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};

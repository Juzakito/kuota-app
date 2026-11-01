import React, { useState } from 'react';
import { QrCode, DollarSign, Wallet, Calendar, TrendingUp, ChevronRight } from 'lucide-react';

interface Step {
  num: number;
  title: string;
  desc: string;
  tag: string;
  icon: React.ReactNode;
  detail: {
    label: string;
    value: string;
    sub: string;
  };
}

const STEPS: Step[] = [
  {
    num: 1,
    title: 'Escanea el QR del proveedor',
    desc: 'Llegas al comercio o distribuidor afiliado y abres tu app Kuota para escanear su código QR exclusivo en el mostrador.',
    tag: 'En segundos',
    icon: <QrCode className="w-5 h-5 text-forest" />,
    detail: {
      label: 'Proveedor detectado',
      value: 'Distribuidora San Juan',
      sub: 'Afiliado verificado · Lima',
    },
  },
  {
    num: 2,
    title: 'Digita el monto de tu compra',
    desc: 'Ingresas el total exacto de la mercadería que te estás llevando. Desde S/ 199 hasta el límite disponible de tu nivel.',
    tag: 'Sin trámites',
    icon: <DollarSign className="w-5 h-5 text-forest" />,
    detail: {
      label: 'Monto de compra',
      value: 'S/ 700.00',
      sub: 'Mercadería aprobada para financiamiento',
    },
  },
  {
    num: 3,
    title: 'Paga tu cuota inicial',
    desc: 'Pagas la inicial directamente al comercio desde tu Yape, Plin o transferencia bancaria (desde 30% en Nivel 1).',
    tag: 'Al instante',
    icon: <Wallet className="w-5 h-5 text-forest" />,
    detail: {
      label: 'Inicial requerida (30%)',
      value: 'S/ 210.00',
      sub: 'Pagado en tiempo real al comercio',
    },
  },
  {
    num: 4,
    title: 'Elige cuántas semanas',
    desc: 'Seleccionas tu plazo preferido: 15 días (2 cuotas), 30 días (4 cuotas) o 60 días (8 cuotas). Costo fijo del 5%.',
    tag: '5% Fijo',
    icon: <Calendar className="w-5 h-5 text-forest" />,
    detail: {
      label: '4 cuotas semanales',
      value: 'S/ 128.62 / semana',
      sub: 'Fechas fijas acordes a tus cobros',
    },
  },
  {
    num: 5,
    title: 'Paga puntual y sube de nivel',
    desc: 'Cancela tus cuotas a tiempo y desbloquea mayores montos de compra y menor porcentaje de inicial en la siguiente compra.',
    tag: 'Mayor cupo',
    icon: <TrendingUp className="w-5 h-5 text-forest" />,
    detail: {
      label: 'Próximo nivel alcanzable',
      value: 'Nivel 2 (Hasta S/ 1,200)',
      sub: 'Inicial reducida al 25%',
    },
  },
];

export const FiveStepsSection: React.FC = () => {
  const [activeStep, setActiveStep] = useState(0);

  return (
    <section className="py-16 md:py-24 bg-fog/30 border-b border-fog" id="como-funciona">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
        <div className="text-center max-w-2xl mx-auto mb-14">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-pill bg-linen text-forest text-xs font-semibold mb-3">
            <span>Flujo simple en 5 pasos</span>
          </div>
          <h2 className="text-3xl md:text-4xl font-black text-obsidian tracking-tight uppercase">
            COMPRAR AL CRÉDITO ES TAN FÁCIL COMO PAGAR CON QR
          </h2>
          <p className="text-sm md:text-base text-charcoal/80 mt-2">
            Sin formularios interminables, sin firmas notariales. Todo desde tu teléfono en menos de 60 segundos en el mostrador.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
          {/* Lista de pasos interactiva (Izquierda) */}
          <div className="lg:col-span-7 space-y-3">
            {STEPS.map((step, idx) => {
              const isActive = activeStep === idx;
              return (
                <div
                  key={step.num}
                  onClick={() => setActiveStep(idx)}
                  className={`p-4 md:p-5 rounded-card border transition-all cursor-pointer ${
                    isActive
                      ? 'bg-paper border-forest shadow-card'
                      : 'bg-paper/60 border-fog hover:bg-paper hover:border-pebble/40'
                  }`}
                >
                  <div className="flex items-start gap-4">
                    <span
                      className={`w-8 h-8 rounded-pill flex items-center justify-center text-xs font-bold shrink-0 transition-colors ${
                        isActive ? 'bg-forest text-lime' : 'bg-fog text-charcoal'
                      }`}
                    >
                      {step.num}
                    </span>

                    <div className="flex-1">
                      <div className="flex items-center justify-between gap-2">
                        <h3 className={`text-base font-bold ${isActive ? 'text-obsidian' : 'text-charcoal'}`}>
                          {step.title}
                        </h3>
                        <span className="text-[11px] font-semibold px-2 py-0.5 rounded-pill bg-linen text-forest">
                          {step.tag}
                        </span>
                      </div>
                      <p className="text-xs text-charcoal/80 mt-1 leading-relaxed">
                        {step.desc}
                      </p>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Tarjeta de visualización simulada (Derecha) */}
          <div className="lg:col-span-5">
            <div className="bg-forest rounded-hero p-6 md:p-8 text-paper shadow-card space-y-6">
              <div className="flex items-center justify-between border-b border-spruce pb-4">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-pill bg-linen text-forest flex items-center justify-center">
                    {STEPS[activeStep].icon}
                  </div>
                  <div>
                    <div className="text-[11px] text-lime font-bold uppercase tracking-wider">
                      Paso {STEPS[activeStep].num} de 5
                    </div>
                    <div className="text-sm font-bold text-paper">
                      {STEPS[activeStep].title}
                    </div>
                  </div>
                </div>
                <span className="text-xs px-2.5 py-1 rounded-pill bg-spruce text-lime font-semibold">
                  En vivo
                </span>
              </div>

              {/* Mockup de la pantalla del celular */}
              <div className="bg-paper rounded-card p-5 text-charcoal space-y-4">
                <div className="text-xs font-bold text-pebble uppercase tracking-wider">
                  {STEPS[activeStep].detail.label}
                </div>
                <div className="text-2xl font-black text-obsidian">
                  {STEPS[activeStep].detail.value}
                </div>
                <div className="text-xs text-spruce font-medium">
                  ✓ {STEPS[activeStep].detail.sub}
                </div>

                <div className="pt-3 border-t border-fog flex items-center justify-between text-xs text-charcoal">
                  <span>Validación instantánea</span>
                  <span className="font-bold text-forest">Kuota App</span>
                </div>
              </div>

              {/* Botones de navegación del carrusel */}
              <div className="flex items-center justify-between pt-2 text-xs text-paper/80">
                <span>Haz clic en cada paso para ver el detalle</span>
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => setActiveStep((prev) => (prev > 0 ? prev - 1 : STEPS.length - 1))}
                    className="px-3 py-1.5 rounded-pill bg-spruce text-paper hover:bg-forest-dark cursor-pointer font-bold"
                  >
                    ← Anterior
                  </button>
                  <button
                    type="button"
                    onClick={() => setActiveStep((prev) => (prev < STEPS.length - 1 ? prev + 1 : 0))}
                    className="px-3 py-1.5 rounded-pill bg-lime text-forest hover:bg-lime-hover cursor-pointer font-bold"
                  >
                    Siguiente →
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

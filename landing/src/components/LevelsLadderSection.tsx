import React from 'react';
import { TrendingUp, ShieldCheck, Lock, Award, CheckCircle2 } from 'lucide-react';

export const LevelsLadderSection: React.FC = () => {
  const levels = [
    {
      level: 1,
      title: 'Nivel 1 · Inicial',
      amount: 'Hasta S/ 700',
      initialPct: '30% inicial',
      status: 'Activo',
      badge: 'Tu punto de partida',
      desc: 'El primer paso para construir tu historial crediticio con Kuota. Acceso inmediato con DNI y validación de negocio.',
    },
    {
      level: 2,
      title: 'Nivel 2 · Confiable',
      amount: 'Hasta S/ 1,200',
      initialPct: '25% inicial',
      status: 'Desbloqueable',
      badge: 'Menor inicial',
      desc: 'Se activa al completar tus primeras compras con pagos puntuales semanales. Tu inicial baja al 25%.',
    },
    {
      level: 3,
      title: 'Nivel 3 · Avanzado',
      amount: 'Monto mayor',
      initialPct: '20% inicial',
      status: 'Próximamente',
      badge: 'Próximamente',
      desc: 'Cupo ampliado para mercadería de mayor volumen según tu récord de cumplimiento.',
    },
    {
      level: 4,
      title: 'Nivel 4 · Consolidado',
      amount: 'Monto preferencial',
      initialPct: 'Inicial reducida',
      status: 'Próximamente',
      badge: 'Próximamente',
      desc: 'Línea de crédito para compras recurrentes de alto giro.',
    },
    {
      level: 5,
      title: 'Nivel 5 · Líder',
      amount: 'Monto corporativo',
      initialPct: 'Condiciones VIP',
      status: 'Próximamente',
      badge: 'Próximamente',
      desc: 'Atención prioritaria y despacho continuo de inventario.',
    },
    {
      level: 6,
      title: 'Nivel 6 · Master',
      amount: 'Cupo máximo',
      initialPct: 'Mínima inicial',
      status: 'Próximamente',
      badge: 'Próximamente',
      desc: 'La máxima confianza de Kuota para abastecer cadenas de tiendas y microempresas en expansión.',
    },
  ];

  return (
    <section className="py-16 md:py-24 bg-fog/30 border-b border-fog" id="niveles">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6">
        <div className="text-center max-w-2xl mx-auto mb-14">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-pill bg-linen text-forest text-xs font-semibold mb-3">
            <TrendingUp className="w-3.5 h-3.5" />
            <span>Escalera de confianza Kuota</span>
          </div>
          <h2 className="text-3xl md:text-4xl font-black text-obsidian tracking-tight uppercase">
            PAGAR A TIEMPO TE SUBE DE NIVEL Y AUMENTA TU CUPO
          </h2>
          <p className="text-sm md:text-base text-charcoal/80 mt-2">
            Empiezas en el Nivel 1. Cada compra que liquidas puntualmente reduce tu porcentaje de inicial y aumenta el monto disponible para tu negocio.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {levels.map((lvl) => {
            const isN1 = lvl.level === 1;
            const isN2 = lvl.level === 2;
            const isFuture = lvl.level >= 3;

            return (
              <div
                key={lvl.level}
                className={`p-6 rounded-card border transition-all flex flex-col justify-between ${
                  isN1
                    ? 'bg-paper border-forest ring-1 ring-forest shadow-card'
                    : isN2
                    ? 'bg-paper border-fog hover:border-spruce/40 shadow-subtle'
                    : 'bg-fog/50 border-fog/80 opacity-80'
                }`}
              >
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span
                      className={`text-xs font-bold px-2.5 py-1 rounded-pill ${
                        isN1
                          ? 'bg-forest text-lime'
                          : isN2
                          ? 'bg-linen text-forest'
                          : 'bg-paper text-pebble border border-fog'
                      }`}
                    >
                      {lvl.badge}
                    </span>
                    <span className="text-xs font-extrabold text-charcoal">
                      Nivel {lvl.level}
                    </span>
                  </div>

                  <div>
                    <h3 className="text-xl font-black text-obsidian tracking-tight">
                      {lvl.amount}
                    </h3>
                    <div className="text-xs font-bold text-spruce mt-0.5">
                      {lvl.initialPct}
                    </div>
                  </div>

                  <p className="text-xs text-charcoal/80 leading-relaxed">
                    {lvl.desc}
                  </p>
                </div>

                <div className="mt-5 pt-4 border-t border-fog flex items-center justify-between text-xs">
                  {isFuture ? (
                    <span className="inline-flex items-center gap-1.5 text-pebble font-medium">
                      <Lock className="w-3.5 h-3.5" />
                      <span>Disponible en fase 2</span>
                    </span>
                  ) : (
                    <span className="inline-flex items-center gap-1.5 text-forest font-semibold">
                      <CheckCircle2 className="w-3.5 h-3.5 text-forest" />
                      <span>{lvl.status}</span>
                    </span>
                  )}
                  <span className="text-[11px] text-pebble">Sin letra chica</span>
                </div>
              </div>
            );
          })}
        </div>

        <div className="mt-10 p-4 rounded-card bg-linen/50 border border-spruce/20 text-center max-w-2xl mx-auto text-xs text-forest">
          <strong>Regla de oro Kuota:</strong> El historial es tuyo. Si siempre cumples tus fechas de pago semanales, tu negocio accede a mejores condiciones sin depender de bancos tradicionales.
        </div>
      </div>
    </section>
  );
};

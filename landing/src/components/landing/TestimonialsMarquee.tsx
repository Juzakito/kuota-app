import React from 'react';
import { ShieldCheck, Star } from 'lucide-react';
import { TESTIMONIALS_ROW_1, TESTIMONIALS_ROW_2 } from '../../config/fintechData';

export const TestimonialsMarquee: React.FC = () => {
  return (
    <section id="testimonios" className="relative py-16 overflow-hidden bg-[#F4F6F8]">
      {/* Encabezado Centrado */}
      <div className="max-w-3xl mx-auto text-center px-4 mb-12">
        <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white border border-slate-200/80 text-xs font-semibold text-slate-800 tracking-tight mb-3 shadow-xs">
          <ShieldCheck className="w-4 h-4 text-emerald-600" />
          <span>Comunidad & Reseñas Verificadas</span>
        </div>
        <h2 className="text-3xl sm:text-4xl md:text-5xl font-extrabold text-[#070A0F] tracking-tight leading-tight">
          Tu operación, <span className="text-slate-900 underline decoration-[#10B981] decoration-wavy decoration-2">nuestra inspiración</span>
        </h2>
        <p className="text-xs sm:text-sm text-slate-600 mt-3 max-w-xl mx-auto leading-relaxed">
          En Kuota valoramos las decisiones de cada gerente y dueño de negocio. Tus datos comerciales están 100% encriptados y protegidos bajo estándares bancarios.
        </p>
      </div>

      {/* MARQUEE CONTAINER CON MÁSCARA DE DESVANECIMIENTO */}
      <div
        className="space-y-4"
        style={{
          maskImage:
            'linear-gradient(to right, transparent, black 10%, black 90%, transparent)',
          WebkitMaskImage:
            'linear-gradient(to right, transparent, black 10%, black 90%, transparent)',
        }}
      >
        {/* FILA 1: DESPLAZAMIENTO A LA IZQUIERDA */}
        <div className="flex w-max gap-4 animate-[marquee_45s_linear_infinite] hover:[animation-play-state:paused]">
          {[...TESTIMONIALS_ROW_1, ...TESTIMONIALS_ROW_1].map((item, idx) => (
            <div
              key={`row1-${item.id}-${idx}`}
              className="w-[340px] sm:w-[380px] p-6 rounded-[24px] bg-white border border-slate-200/80 shadow-sm flex flex-col justify-between shrink-0 hover:shadow-md transition-shadow"
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm ${item.avatarBg}`}
                    >
                      {item.author.charAt(0)}
                    </div>
                    <div>
                      <div className="text-sm font-bold text-slate-900">{item.company}</div>
                      <div className="text-[11px] text-slate-500">
                        {item.location} · {item.years}
                      </div>
                    </div>
                  </div>
                  <div className="flex text-amber-400">
                    {[...Array(5)].map((_, i) => (
                      <Star key={i} className="w-3.5 h-3.5 fill-current" />
                    ))}
                  </div>
                </div>

                <p className="text-xs sm:text-sm text-slate-700 leading-relaxed italic">
                  "{item.quote}"
                </p>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
                <span className="text-[11px] text-slate-500">{item.author}</span>
                <span className="text-xs font-bold text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-200/60">
                  {item.metricValue} {item.metricLabel}
                </span>
              </div>
            </div>
          ))}
        </div>

        {/* FILA 2: DESPLAZAMIENTO A LA DERECHA */}
        <div className="flex w-max gap-4 animate-[marquee_50s_linear_infinite_reverse] hover:[animation-play-state:paused]">
          {[...TESTIMONIALS_ROW_2, ...TESTIMONIALS_ROW_2].map((item, idx) => (
            <div
              key={`row2-${item.id}-${idx}`}
              className="w-[340px] sm:w-[380px] p-6 rounded-[24px] bg-white border border-slate-200/80 shadow-sm flex flex-col justify-between shrink-0 hover:shadow-md transition-shadow"
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm ${item.avatarBg}`}
                    >
                      {item.author.charAt(0)}
                    </div>
                    <div>
                      <div className="text-sm font-bold text-slate-900">{item.company}</div>
                      <div className="text-[11px] text-slate-500">
                        {item.location} · {item.years}
                      </div>
                    </div>
                  </div>
                  <div className="flex text-amber-400">
                    {[...Array(5)].map((_, i) => (
                      <Star key={i} className="w-3.5 h-3.5 fill-current" />
                    ))}
                  </div>
                </div>

                <p className="text-xs sm:text-sm text-slate-700 leading-relaxed italic">
                  "{item.quote}"
                </p>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
                <span className="text-[11px] text-slate-500">{item.author}</span>
                <span className="text-xs font-bold text-cyan-700 bg-cyan-50 px-2.5 py-1 rounded-full border border-cyan-200/60">
                  {item.metricValue} {item.metricLabel}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

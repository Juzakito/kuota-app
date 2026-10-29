import React from 'react';
import { Linkedin, UserCheck, Building2 } from 'lucide-react';
import {
  SUPPLIER_TEAM_MEMBERS,
  SUPPLIER_TRUSTED_INSTITUTIONS,
} from '../../config/pricingConfig';

/**
 * PRD §5.7: Confianza – quiénes somos
 * - Título: "Quiénes somos".
 * - Cards con foto y enlace a LinkedIn:
 *   - José González – CTO (IMG-17)
 *   - Josué Lovatón – CEO (IMG-18)
 *   - Rolando Quiroz – Advisor (IMG-19)
 * - Texto breve del equipo.
 * - Debajo: "Instituciones que confían en nosotros" con el logo de Científica del Sur (IMG-20, 200×80).
 */
export const TrustSection: React.FC = () => {
  return (
    <section
      id="nosotros"
      aria-labelledby="supplier-trust-heading"
      className="py-16 sm:py-20 border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-10">
          <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full bg-white border-2 border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy uppercase tracking-wider mb-3">
            <UserCheck className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
            Equipo e instituciones
          </span>
          <h2
            id="supplier-trust-heading"
            className="text-3xl sm:text-4xl font-extrabold text-kuota-deepnavy tracking-tight mb-3"
          >
            Quiénes somos
          </h2>
          <p className="text-base sm:text-lg font-semibold text-kuota-crayola leading-relaxed">
            Construimos infraestructura de pagos y financiamiento comercial para que los proveedores y distribuidores del Perú vendan más, cobren más rápido y reduzcan sus costos operativos.
          </p>
        </div>

        {/* Cards del equipo: IMG-17, IMG-18, IMG-19 */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 lg:gap-8 mb-14">
          {SUPPLIER_TEAM_MEMBERS.map((member) => (
            <article
              key={member.id}
              className="rounded-3xl bg-white border-2 border-kuota-deepnavy p-5 sm:p-6 shadow-navy-hard flex flex-col justify-between"
            >
              <div>
                {member.photoUrl ? (
                  <div className="aspect-square w-full rounded-2xl border-2 border-kuota-deepnavy bg-kuota-palesky overflow-hidden mb-4">
                    <img
                      src={member.photoUrl}
                      alt={`${member.name} — ${member.role}`}
                      className="w-full h-full object-cover object-top transition-transform duration-300 hover:scale-105"
                    />
                  </div>
                ) : (
                  <div
                    role="img"
                    aria-label={`${member.placeholderId}: Foto de ${member.name} (${member.dimensions})`}
                    className="aspect-square w-full rounded-2xl border-2 border-kuota-deepnavy bg-kuota-palesky/65 placeholder-stripes p-3.5 flex flex-col justify-between mb-4"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <span className="px-2 py-0.5 rounded bg-kuota-deepnavy text-white text-[10px] font-bold uppercase">
                        Placeholder · {member.placeholderId}
                      </span>
                      <span className="px-2 py-0.5 rounded bg-white border border-kuota-deepnavy text-kuota-deepnavy text-[10px] font-extrabold">
                        {member.dimensions}
                      </span>
                    </div>

                    <div className="my-auto flex flex-col items-center text-center">
                      <div className="w-16 h-16 rounded-full bg-kuota-deepnavy text-white border-2 border-white flex items-center justify-center text-xl font-extrabold shadow-sm">
                        {member.name
                          .split(' ')
                          .map((n) => n[0])
                          .join('')}
                      </div>
                      <span className="text-xs font-bold text-kuota-deepnavy mt-2">
                        Foto de {member.name}
                      </span>
                    </div>

                    <div className="text-[11px] font-bold text-kuota-deepnavy bg-white/90 border border-kuota-deepnavy rounded-lg px-2.5 py-1 text-center">
                      {member.placeholderId} · {member.dimensions}
                    </div>
                  </div>
                )}

                <h3 className="text-xl font-extrabold text-kuota-deepnavy">
                  {member.name}
                </h3>
                <p className="text-sm sm:text-base font-extrabold text-kuota-dodger mt-0.5">
                  {member.role}
                </p>
              </div>

              <div className="mt-5 pt-4 border-t border-kuota-deepnavy/15">
                <a
                  href={member.linkedinUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  aria-label={`LinkedIn de ${member.name}`}
                  className="inline-flex items-center gap-2 text-xs sm:text-sm font-extrabold text-kuota-deepnavy hover:text-kuota-dodger transition-colors"
                >
                  <span className="w-7 h-7 rounded-lg bg-kuota-palesky border border-kuota-deepnavy flex items-center justify-center">
                    <Linkedin className="w-4 h-4 text-kuota-deepnavy" aria-hidden="true" />
                  </span>
                  <span>Ver en LinkedIn</span>
                </a>
              </div>
            </article>
          ))}
        </div>

        {/* Instituciones que confían en nosotros: IMG-20 */}
        <div className="pt-8 border-t-2 border-kuota-deepnavy/20 text-center">
          <h3 className="text-xl sm:text-2xl font-extrabold text-kuota-deepnavy mb-6">
            Instituciones que confían en nosotros
          </h3>

          <div
            role="list"
            aria-label="Instituciones que confían en Kuota"
            className="flex flex-wrap items-center justify-center gap-5"
          >
            {SUPPLIER_TRUSTED_INSTITUTIONS.map((inst) => (
              <div
                key={inst.id}
                role="listitem"
                aria-label={`${inst.placeholderId}: Logo de ${inst.name} (${inst.dimensions})`}
                className="rounded-2xl bg-white border-2 border-kuota-deepnavy px-7 py-5 shadow-navy-hard flex items-center gap-4 min-w-[270px] sm:min-w-[320px]"
              >
                <div className="w-12 h-12 rounded-xl bg-kuota-palesky border-2 border-kuota-deepnavy flex items-center justify-center shrink-0">
                  <Building2 className="w-6 h-6 text-kuota-deepnavy" aria-hidden="true" />
                </div>
                <div className="text-left">
                  <div className="flex items-center gap-1.5 mb-1">
                    <span className="text-[10px] font-extrabold uppercase tracking-wider px-2 py-0.5 rounded bg-kuota-deepnavy text-white">
                      {inst.placeholderId}
                    </span>
                    <span className="text-[10px] font-extrabold px-2 py-0.5 rounded bg-kuota-palesky text-kuota-deepnavy border border-kuota-deepnavy">
                      {inst.dimensions}
                    </span>
                  </div>
                  <div className="text-lg font-extrabold text-kuota-deepnavy leading-tight">
                    {inst.name}
                  </div>
                  <div className="text-xs font-semibold text-kuota-crayola">
                    {inst.subtitle}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};


import React from 'react';
import { UserCheck, Building2 } from 'lucide-react';
import { TEAM_MEMBERS, TRUSTED_INSTITUTIONS } from '../data/landingData';
import { ScrollReveal, TiltCard } from '../utils/motionSystem';

const LinkedinIcon: React.FC<{ className?: string }> = ({ className = 'w-4 h-4' }) => (
  <svg
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className={className}
    aria-hidden="true"
  >
    <path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z" />
    <rect width="4" height="12" x="2" y="9" />
    <circle cx="4" cy="4" r="2" />
  </svg>
);

/**
 * PRD §6.7 + FASE 3.F: Confianza con aparición progresiva y profundidad sutil.
 */
export const TrustSection: React.FC = () => {
  return (
    <section
      id="confianza"
      aria-labelledby="trust-heading"
      className="py-16 sm:py-24 border-b border-slate-200/80 bg-slate-50/50"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <div>
          <ScrollReveal delayMs={0} direction="up">
            <div className="text-center max-w-2xl mx-auto mb-10">
              <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full bg-sky-50 border border-sky-200/80 text-xs font-bold text-[#0391FD] uppercase tracking-wider mb-3 shadow-xs">
                <UserCheck className="w-3.5 h-3.5 text-[#0391FD]" aria-hidden="true" />
                Respaldo humano
              </span>
              <h2
                id="trust-heading"
                className="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-[#0A192F] tracking-tight"
              >
                Quiénes somos
              </h2>
            </div>
          </ScrollReveal>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 lg:gap-8">
            {TEAM_MEMBERS.map((member, idx) => (
              <ScrollReveal key={member.id} delayMs={idx * 100} direction="up">
                <TiltCard maxTiltDeg={2.5} className="h-full">
                  <article className="h-full rounded-[28px] bg-white border border-slate-200/90 p-5 sm:p-6 shadow-md hover:shadow-xl transition-shadow flex flex-col justify-between">
                    <div>
                      {member.photoUrl ? (
                        <div className="aspect-square w-full rounded-2xl border border-slate-200/90 bg-slate-100 overflow-hidden mb-4 shadow-xs">
                          <img
                            src={member.photoUrl}
                            alt={`${member.name} — ${member.role}`}
                            className="w-full h-full object-cover object-top transition-transform duration-300 hover:scale-105"
                          />
                        </div>
                      ) : (
                        <div
                          role="img"
                          aria-label={`Foto placeholder de ${member.name} (${member.role})`}
                          className="aspect-square w-full rounded-2xl border border-slate-200/90 bg-slate-100 p-3.5 flex flex-col justify-between mb-4 shadow-xs"
                        >
                          <div className="flex items-center justify-between gap-2">
                            <span className="px-2.5 py-0.5 rounded-full bg-[#0A192F] text-white text-[10px] font-bold uppercase">
                              Equipo Directivo
                            </span>
                            <span className="px-2.5 py-0.5 rounded-full bg-emerald-50 text-emerald-800 border border-emerald-200 text-[10px] font-bold">
                              Verificado ✓
                            </span>
                          </div>

                          <div className="my-auto flex flex-col items-center text-center">
                            <div className="w-16 h-16 rounded-full bg-[#0A192F] text-white border-2 border-white flex items-center justify-center text-xl font-extrabold shadow-sm">
                              {member.name
                                .split(' ')
                                .map((n) => n[0])
                                .join('')}
                            </div>
                            <span className="text-xs font-bold text-slate-800 mt-2">
                              {member.name}
                            </span>
                          </div>

                          <div className="text-[11px] font-bold text-slate-700 bg-white border border-slate-200 rounded-lg px-2.5 py-1 text-center shadow-xs">
                            {member.role} · Kuota Finanzas
                          </div>
                        </div>
                      )}

                      <h3 className="text-xl font-extrabold text-[#0A192F]">
                        {member.name}
                      </h3>
                      <p className="text-sm font-bold text-[#0391FD] mt-0.5">
                        {member.role}
                      </p>
                    </div>

                    <div className="mt-5 pt-4 border-t border-slate-100">
                      <a
                        href={member.linkedinUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        aria-label={`Perfil de LinkedIn de ${member.name}`}
                        className="group inline-flex items-center gap-2 text-xs sm:text-sm font-bold text-slate-700 hover:text-[#0391FD] transition-colors duration-150"
                      >
                        <span className="w-7 h-7 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center transition-transform duration-150 group-hover:scale-110 group-hover:bg-[#0391FD] group-hover:text-white">
                          <LinkedinIcon className="w-3.5 h-3.5" />
                        </span>
                        <span>Ver en LinkedIn</span>
                      </a>
                    </div>
                  </article>
                </TiltCard>
              </ScrollReveal>
            ))}
          </div>
        </div>

        {/* Subsección 2: Instituciones que confían en nosotros */}
        <ScrollReveal delayMs={150} direction="up">
          <div className="pt-8 border-t border-slate-200/80">
            <div className="text-center max-w-xl mx-auto mb-6">
              <h3 className="text-xl sm:text-2xl font-extrabold text-[#0A192F]">
                Instituciones que confían en nosotros
              </h3>
            </div>

            <div
              role="list"
              aria-label="Instituciones que respaldan a Kuota"
              className="flex flex-wrap items-center justify-center gap-5"
            >
              {TRUSTED_INSTITUTIONS.map((inst) => (
                <div
                  key={inst.id}
                  role="listitem"
                  aria-label={`Logo de ${inst.name}`}
                  className="rounded-2xl bg-white border border-slate-200/90 px-6 py-4 shadow-sm hover:shadow-md flex items-center gap-4 min-w-[270px] sm:min-w-[320px] transition-transform duration-200 hover:-translate-y-0.5"
                >
                  {inst.logoUrl ? (
                    <div className="w-16 h-16 rounded-xl border border-slate-200/90 overflow-hidden shrink-0 shadow-xs">
                      <img
                        src={inst.logoUrl}
                        alt={inst.subtitle}
                        className="w-full h-full object-cover"
                      />
                    </div>
                  ) : (
                    <div className="w-12 h-12 rounded-xl bg-sky-50 border border-sky-200 flex items-center justify-center shrink-0">
                      <Building2 className="w-6 h-6 text-[#0391FD]" aria-hidden="true" />
                    </div>
                  )}
                  <div className="text-left">
                    <span className="inline-block text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-sky-50 text-[#0391FD] border border-sky-200/80 mb-1">
                      Respaldo Institucional
                    </span>
                    <div className="text-base sm:text-lg font-extrabold text-[#0A192F] leading-tight">
                      {inst.name}
                    </div>
                    <div className="text-xs font-normal text-slate-500">
                      {inst.subtitle}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </ScrollReveal>
      </div>
    </section>
  );
};

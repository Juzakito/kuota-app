import React, { useEffect, useState } from 'react';
import { ArrowRight, Clock } from 'lucide-react';
import {
  LAUNCH_CONFIG,
  CountdownSnapshot,
  calculateCountdownSnapshot,
} from '../config/launchConfig';
import { trackSupplierEvent } from '../utils/analytics';

function resolveEffectiveTargetIso(): {
  targetIso: string;
  launchDayEndIso: string;
} {
  if (typeof window === 'undefined') {
    return {
      targetIso: LAUNCH_CONFIG.targetIsoLima,
      launchDayEndIso: LAUNCH_CONFIG.launchDayEndIsoLima,
    };
  }

  const params = new URLSearchParams(window.location.search);
  const sim = params.get('countdown_sim');

  if (sim === 'near_zero') {
    const target = new Date(Date.now() + 5000).toISOString();
    const end = new Date(Date.now() + 3600 * 1000).toISOString();
    return { targetIso: target, launchDayEndIso: end };
  }

  if (sim === 'launch_day') {
    const target = new Date(Date.now() - 1000).toISOString();
    const end = new Date(Date.now() + 3600 * 1000).toISOString();
    return { targetIso: target, launchDayEndIso: end };
  }

  if (sim === 'live') {
    const target = new Date(Date.now() - 86400 * 2000).toISOString();
    const end = new Date(Date.now() - 86400 * 1000).toISOString();
    return { targetIso: target, launchDayEndIso: end };
  }

  return {
    targetIso: LAUNCH_CONFIG.targetIsoLima,
    launchDayEndIso: LAUNCH_CONFIG.launchDayEndIsoLima,
  };
}

export const LaunchCountdownSection: React.FC = () => {
  const [{ targetIso, launchDayEndIso }] = useState(resolveEffectiveTargetIso);
  const [snapshot, setSnapshot] = useState<CountdownSnapshot>(() =>
    calculateCountdownSnapshot(Date.now(), targetIso, launchDayEndIso)
  );

  useEffect(() => {
    const updateCounter = () => {
      const next = calculateCountdownSnapshot(Date.now(), targetIso, launchDayEndIso);
      setSnapshot((prev) => {
        if (
          prev.phase === next.phase &&
          prev.days === next.days &&
          prev.hours === next.hours &&
          prev.minutes === next.minutes &&
          prev.seconds === next.seconds
        ) {
          return prev;
        }
        return next;
      });
    };

    updateCounter();
    const intervalId = window.setInterval(updateCounter, 1000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [targetIso, launchDayEndIso]);

  const primaryHref = '#agendar-call';
  const secondaryHref = '#calculadora';

  const handlePrimaryClick = () => {
    trackSupplierEvent('countdown_primary_cta_click', {
      phase: snapshot.phase,
      destination: primaryHref,
    });
  };

  const handleSecondaryClick = () => {
    trackSupplierEvent('countdown_secondary_cta_click', {
      phase: snapshot.phase,
      destination: secondaryHref,
    });
  };

  const timeUnits = [
    { key: 'days', label: 'DÍAS', value: snapshot.formattedDays },
    { key: 'hours', label: 'HORAS', value: snapshot.formattedHours },
    { key: 'minutes', label: 'MINUTOS', value: snapshot.formattedMinutes },
    { key: 'seconds', label: 'SEGUNDOS', value: snapshot.formattedSeconds },
  ];

  return (
    <section
      id="lanzamiento-oficial"
      aria-labelledby="launch-countdown-heading"
      className="w-full py-10 sm:py-14 border-b-2 border-kuota-deepnavy bg-white/60"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="rounded-3xl bg-kuota-deepnavy text-white border-2 border-kuota-deepnavy shadow-navy-hard-lg overflow-hidden">
          <div className="px-5 sm:px-8 py-3.5 bg-white/[0.04] border-b border-white/10 flex flex-wrap items-center justify-between gap-3">
            <div className="inline-flex items-center gap-2.5">
              <span className="relative flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-kuota-dodger opacity-75" />
                <span className="relative inline-flex rounded-full h-2 w-2 bg-kuota-dodger" />
              </span>
              <span className="text-[11px] sm:text-xs font-extrabold tracking-[0.18em] uppercase text-kuota-palesky">
                {snapshot.phase === 'upcoming' && LAUNCH_CONFIG.copy.eyebrow}
                {snapshot.phase === 'launch_day' && LAUNCH_CONFIG.copy.launchDayStatus}
                {snapshot.phase === 'live' && LAUNCH_CONFIG.copy.postLaunchStatus}
              </span>
            </div>

            <div className="inline-flex items-center gap-2 text-[11px] sm:text-xs font-bold tracking-wider uppercase text-kuota-palesky/85">
              <Clock className="w-3.5 h-3.5 text-kuota-dodger shrink-0" aria-hidden="true" />
              <span>{LAUNCH_CONFIG.dateDisplayLong}</span>
              <span className="text-white/30">|</span>
              <span className="font-mono font-extrabold text-white bg-white/10 px-2 py-0.5 rounded border border-white/15">
                {LAUNCH_CONFIG.dateCode}
              </span>
            </div>
          </div>

          <div className="p-6 sm:p-8 lg:p-10 grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-10 items-center">
            <div className="lg:col-span-5 space-y-4 text-left">
              <p className="text-xs font-extrabold tracking-[0.16em] uppercase text-kuota-dodger">
                {LAUNCH_CONFIG.copy.eyebrow} · {LAUNCH_CONFIG.dateCode}
              </p>

              <h2
                id="launch-countdown-heading"
                className="text-2xl sm:text-3xl lg:text-[32px] font-extrabold tracking-tight text-white leading-[1.15]"
              >
                {LAUNCH_CONFIG.copy.title}
              </h2>

              <p className="text-sm sm:text-base font-medium text-kuota-palesky/90 leading-relaxed">
                {LAUNCH_CONFIG.copy.description}
              </p>

              <p className="text-xs font-bold text-kuota-palesky/75 border-l-2 border-kuota-dodger pl-3 py-0.5">
                Pagos con tarjeta al 2% de comisión y ventas en kuotas con 0 riesgo de impago.
              </p>

              <div className="pt-2 flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
                <a
                  href={primaryHref}
                  onClick={handlePrimaryClick}
                  className="inline-flex items-center justify-center gap-2 px-5 py-3 rounded-xl bg-kuota-dodger hover:bg-kuota-crayola text-white font-extrabold text-sm border border-white/25 transition-all duration-150 hover:-translate-y-0.5"
                >
                  <span>{LAUNCH_CONFIG.copy.primaryCta}</span>
                  <ArrowRight className="w-4 h-4 shrink-0" aria-hidden="true" />
                </a>

                <a
                  href={secondaryHref}
                  onClick={handleSecondaryClick}
                  className="inline-flex items-center justify-center gap-2 px-5 py-3 rounded-xl bg-white/[0.06] hover:bg-white/[0.12] text-kuota-palesky hover:text-white font-bold text-sm border border-white/15 transition-colors"
                >
                  <span>{LAUNCH_CONFIG.copy.secondaryCta}</span>
                </a>
              </div>
            </div>

            <div className="lg:col-span-7">
              <div className="rounded-2xl bg-white/[0.04] border border-white/15 p-5 sm:p-7">
                <div className="flex flex-wrap items-center justify-between gap-2 pb-4 mb-5 border-b border-white/10">
                  <span className="text-[11px] sm:text-xs font-extrabold tracking-[0.18em] uppercase text-kuota-palesky">
                    {snapshot.phase === 'upcoming' && LAUNCH_CONFIG.copy.counterHeader}
                    {snapshot.phase === 'launch_day' && LAUNCH_CONFIG.copy.launchDayStatus}
                    {snapshot.phase === 'live' && LAUNCH_CONFIG.copy.postLaunchStatus}
                  </span>
                  <span className="text-[11px] font-medium text-kuota-palesky/70">
                    {LAUNCH_CONFIG.timeDisplayLima}
                  </span>
                </div>

                {snapshot.phase !== 'live' ? (
                  <>
                    <div
                      role="timer"
                      aria-live="polite"
                      aria-atomic="true"
                      aria-label={`Cuenta regresiva al lanzamiento: ${snapshot.days} días, ${snapshot.hours} horas, ${snapshot.minutes} minutos y ${snapshot.seconds} segundos`}
                      className="grid grid-cols-4 gap-2 sm:gap-3 md:gap-4 items-center"
                    >
                      {timeUnits.map((unit, index) => (
                        <div key={unit.key} className="relative flex items-center">
                          <div className="w-full rounded-xl bg-kuota-deepnavy/90 border border-white/15 px-2 py-3.5 sm:px-4 sm:py-5 text-center shadow-inner">
                            <div className="overflow-hidden">
                              <span
                                key={`${unit.key}-${unit.value}`}
                                className="block font-sans font-extrabold tabular-nums tracking-tight text-2xl sm:text-4xl md:text-5xl text-white leading-none animate-digitSlide"
                              >
                                {unit.value}
                              </span>
                            </div>
                            <span className="block mt-2 text-[9px] sm:text-[11px] font-extrabold tracking-[0.14em] uppercase text-kuota-palesky/80">
                              {unit.label}
                            </span>
                          </div>

                          {index < timeUnits.length - 1 && (
                            <span
                              aria-hidden="true"
                              className="hidden sm:inline-block absolute -right-2.5 md:-right-3 top-1/2 -translate-y-3 text-lg md:text-2xl font-bold text-kuota-dodger/70 select-none"
                            >
                              :
                            </span>
                          )}
                        </div>
                      ))}
                    </div>

                    <div className="mt-5 pt-4 border-t border-white/10 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 text-xs">
                      <span className="font-bold tracking-widest uppercase text-kuota-palesky/70 text-[10px] sm:text-[11px]">
                        DÍAS · HORAS · MINUTOS · SEGUNDOS
                      </span>

                      {snapshot.phase === 'launch_day' ? (
                        <span className="inline-flex items-center gap-2 px-3 py-1 rounded-md bg-kuota-dodger text-white font-extrabold text-xs tracking-wide uppercase">
                          {LAUNCH_CONFIG.copy.launchDayStatus}
                        </span>
                      ) : (
                        <span className="font-semibold text-kuota-palesky/85">
                          Lanzamiento oficial:{' '}
                          <strong className="text-white font-extrabold">
                            {LAUNCH_CONFIG.dateCode}
                          </strong>
                        </span>
                      )}
                    </div>
                  </>
                ) : (
                  <div className="py-6 sm:py-8 px-4 rounded-xl bg-kuota-deepnavy/90 border border-kuota-dodger/40 text-center space-y-3">
                    <span className="inline-block px-3 py-1 rounded-md bg-kuota-dodger/20 border border-kuota-dodger text-kuota-dodger text-xs font-extrabold tracking-[0.18em] uppercase">
                      {LAUNCH_CONFIG.copy.postLaunchStatus}
                    </span>
                    <p className="text-lg sm:text-xl font-extrabold text-white">
                      La plataforma Kuota ya se encuentra disponible en todo el Perú.
                    </p>
                    <p className="text-xs sm:text-sm text-kuota-palesky/85 max-w-md mx-auto">
                      Agenda tu activación comercial para cobrar con 2% de comisión y habilitar aKuotas! en tu negocio.
                    </p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

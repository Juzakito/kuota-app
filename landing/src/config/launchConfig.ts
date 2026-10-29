/**
 * Configuración centralizada del lanzamiento oficial de Kuota.
 * Modificar `targetIsoLima` aquí actualiza automáticamente el cálculo en tiempo real,
 * las etiquetas de fecha y la transición de estados en toda la landing page.
 */

export type LaunchPhase = 'upcoming' | 'launch_day' | 'live';

export interface CountdownSnapshot {
  phase: LaunchPhase;
  totalRemainingMs: number;
  days: number;
  hours: number;
  minutes: number;
  seconds: number;
  formattedDays: string;
  formattedHours: string;
  formattedMinutes: string;
  formattedSeconds: string;
}

export const LAUNCH_CONFIG = {
  /**
   * 10 de noviembre de 2026 a las 09:00 AM, hora oficial de Perú (America/Lima, UTC-5 permanente).
   */
  targetIsoLima: '2026-11-10T09:00:00-05:00',
  /**
   * Fin del día de lanzamiento (10 de noviembre de 2026 a las 23:59:59 America/Lima).
   * Permite distinguir entre ESTADO 2 ("¡HOY ES EL LANZAMIENTO!") y ESTADO 3 ("YA ESTAMOS EN VIVO").
   */
  launchDayEndIsoLima: '2026-11-10T23:59:59-05:00',
  dateCode: '10.11.2026',
  dateDisplayLong: '10 DE NOVIEMBRE DE 2026',
  timeDisplayLima: '09:00 AM · America/Lima (UTC-5)',
  copy: {
    eyebrow: 'EL LANZAMIENTO SE ACERCA',
    counterHeader: 'EL FUTURO ESTÁ POR COMENZAR',
    title: 'Estamos preparando algo extraordinario.',
    description:
      'Una nueva forma de gestionar, decidir y crecer está a punto de llegar. Muy pronto podrás descubrir todo lo que hemos construido.',
    primaryCta: 'Descubrir la plataforma',
    secondaryCta: 'Conocer más',
    launchDayStatus: '¡HOY ES EL LANZAMIENTO!',
    postLaunchStatus: 'YA ESTAMOS EN VIVO',
  },
} as const;

function padTwoDigits(value: number): string {
  return String(Math.max(0, Math.floor(value))).padStart(2, '0');
}

/**
 * Calcula de forma pura e invariante a la zona horaria del navegador el estado exacto
 * de la cuenta regresiva con respecto a `America/Lima (UTC-5)`.
 */
export function calculateCountdownSnapshot(
  nowEpochMs: number = Date.now(),
  targetIso: string = LAUNCH_CONFIG.targetIsoLima,
  launchDayEndIso: string = LAUNCH_CONFIG.launchDayEndIsoLima
): CountdownSnapshot {
  const targetMs = new Date(targetIso).getTime();
  const launchDayEndMs = new Date(launchDayEndIso).getTime();

  if (nowEpochMs < targetMs) {
    const diffMs = Math.max(0, targetMs - nowEpochMs);
    const totalSeconds = Math.floor(diffMs / 1000);

    const days = Math.floor(totalSeconds / 86400);
    const hours = Math.floor((totalSeconds % 86400) / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    return {
      phase: 'upcoming',
      totalRemainingMs: diffMs,
      days,
      hours,
      minutes,
      seconds,
      formattedDays: padTwoDigits(days),
      formattedHours: padTwoDigits(hours),
      formattedMinutes: padTwoDigits(minutes),
      formattedSeconds: padTwoDigits(seconds),
    };
  }

  if (nowEpochMs <= launchDayEndMs) {
    return {
      phase: 'launch_day',
      totalRemainingMs: 0,
      days: 0,
      hours: 0,
      minutes: 0,
      seconds: 0,
      formattedDays: '00',
      formattedHours: '00',
      formattedMinutes: '00',
      formattedSeconds: '00',
    };
  }

  return {
    phase: 'live',
    totalRemainingMs: 0,
    days: 0,
    hours: 0,
    minutes: 0,
    seconds: 0,
    formattedDays: '00',
    formattedHours: '00',
    formattedMinutes: '00',
    formattedSeconds: '00',
  };
}

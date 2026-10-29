export interface AnalyticsEventRecord {
  id: string;
  event: string;
  params: Record<string, string | number | boolean>;
  timestamp: string;
}

declare global {
  interface Window {
    dataLayer?: Array<Record<string, unknown>>;
    kuotaAnalyticsHistory?: AnalyticsEventRecord[];
  }
}

const listeners = new Set<(events: AnalyticsEventRecord[]) => void>();

export function trackEvent(
  event: string,
  params: Record<string, string | number | boolean> = {}
): void {
  const record: AnalyticsEventRecord = {
    id: `${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
    event,
    params,
    timestamp: new Date().toLocaleTimeString('es-PE', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    }),
  };

  if (typeof window !== 'undefined') {
    window.dataLayer = window.dataLayer || [];
    window.dataLayer.push({
      event,
      ...params,
      timestamp: new Date().toISOString(),
    });

    window.kuotaAnalyticsHistory = [
      record,
      ...(window.kuotaAnalyticsHistory || []),
    ].slice(0, 30);

    listeners.forEach((cb) => cb(window.kuotaAnalyticsHistory || []));
  }

  // Registro estructurado en consola para auditoría de eventos (PRD §7)
  console.info(`[Kuota Analytics] ${event}`, params);
}

export function subscribeAnalytics(
  callback: (events: AnalyticsEventRecord[]) => void
): () => void {
  listeners.add(callback);
  if (typeof window !== 'undefined' && window.kuotaAnalyticsHistory) {
    callback(window.kuotaAnalyticsHistory);
  }
  return () => {
    listeners.delete(callback);
  };
}

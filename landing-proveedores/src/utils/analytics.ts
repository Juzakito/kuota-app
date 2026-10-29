export interface AnalyticsEventRecord {
  id: string;
  event: string;
  params: Record<string, string | number | boolean>;
  timestamp: string;
}

declare global {
  interface Window {
    dataLayer?: Array<Record<string, unknown>>;
    kuotaSupplierAnalyticsHistory?: AnalyticsEventRecord[];
  }
}

export function trackSupplierEvent(
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
      landing: 'proveedores',
      ...params,
      timestamp: new Date().toISOString(),
    });

    window.kuotaSupplierAnalyticsHistory = [
      record,
      ...(window.kuotaSupplierAnalyticsHistory || []),
    ].slice(0, 40);
  }

  console.info(`[Kuota Proveedores Analytics] ${event}`, params);
}

export function trackScrollDepth(): () => void {
  if (typeof window === 'undefined') return () => {};

  const milestones = [25, 50, 75, 100];
  const fired = new Set<number>();

  const onScroll = () => {
    const scrollTop = window.scrollY;
    const docHeight = document.documentElement.scrollHeight - window.innerHeight;
    if (docHeight <= 0) return;

    const percent = Math.min(100, Math.round((scrollTop / docHeight) * 100));
    for (const milestone of milestones) {
      if (percent >= milestone && !fired.has(milestone)) {
        fired.add(milestone);
        trackSupplierEvent('scroll_depth', { percent: milestone });
      }
    }
  };

  window.addEventListener('scroll', onScroll, { passive: true });
  return () => window.removeEventListener('scroll', onScroll);
}


import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  CheckCircle2,
  Calendar,
  ExternalLink,
  Clock,
  Video,
  UserCheck,
  RefreshCw,
  Sparkles,
} from 'lucide-react';
import { CAL_COM_CONFIG } from '../../config/pricingConfig';
import { trackSupplierEvent } from '../../utils/analytics';

const JOSE_HOST = CAL_COM_CONFIG.hosts.find((h) => h.id === 'jose') || CAL_COM_CONFIG.hosts[0];
const JOSUE_HOST = CAL_COM_CONFIG.hosts.find((h) => h.id === 'josue') || CAL_COM_CONFIG.hosts[1];

/**
 * Obtiene y alterna automáticamente el turno entre José y Josué (Round-Robin 50/50)
 */
function getAutoAssignedFounder(): 'jose' | 'josue' {
  if (typeof window === 'undefined') return 'jose';
  try {
    const lastAssigned = window.localStorage.getItem('kuota_calendly_rr_last');
    const next: 'jose' | 'josue' = lastAssigned === 'jose' ? 'josue' : 'jose';
    window.localStorage.setItem('kuota_calendly_rr_last', next);
    return next;
  } catch {
    return 'jose';
  }
}

/**
 * Construye la URL de embed oficial de Calendly con la paleta de colores de Kuota
 */
function buildCalendlyEmbedUrl(baseUrl: string): string {
  const domain =
    typeof window !== 'undefined' && window.location.hostname
      ? window.location.hostname
      : 'localhost';
  const params = new URLSearchParams({
    embed_domain: domain,
    embed_type: 'Inline',
    hide_gdpr_banner: '1',
    background_color: 'ffffff',
    text_color: '0a192f',
    primary_color: '0391fd',
  });
  return `${baseUrl}?${params.toString()}`;
}

export const CalCtaSection: React.FC = () => {
  const sectionRef = useRef<HTMLElement | null>(null);
  const [shouldLoadEmbed, setShouldLoadEmbed] = useState(false);
  const [selectedHostId, setSelectedHostId] = useState<string>('jose');
  const [autoAssignedId, setAutoAssignedId] = useState<'jose' | 'josue'>('jose');
  const [iframeLoading, setIframeLoading] = useState<boolean>(true);
  const [bookingConfirmed, setBookingConfirmed] = useState<boolean>(false);

  // Resolver el anfitrión real activo (directo o por turno automático Round-Robin)
  const effectiveFounder = useMemo(() => {
    if (selectedHostId === 'round-robin') {
      return autoAssignedId === 'josue' ? JOSUE_HOST : JOSE_HOST;
    }
    if (selectedHostId === 'josue') return JOSUE_HOST;
    return JOSE_HOST;
  }, [selectedHostId, autoAssignedId]);

  const embedUrl = useMemo(
    () => buildCalendlyEmbedUrl(effectiveFounder.calLink),
    [effectiveFounder.calLink]
  );

  // Carga diferida (lazy) cuando la sección entra o se acerca al viewport
  useEffect(() => {
    const node = sectionRef.current;
    if (!node) return;

    if ('IntersectionObserver' in window) {
      const observer = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (entry.isIntersecting) {
              setShouldLoadEmbed(true);
              observer.disconnect();
            }
          });
        },
        { rootMargin: '300px' }
      );
      observer.observe(node);
      return () => observer.disconnect();
    } else {
      setShouldLoadEmbed(true);
    }
  }, []);

  // Escuchar eventos postMessage de Calendly para confirmar la reserva
  useEffect(() => {
    const handleMessage = (e: MessageEvent) => {
      try {
        if (
          typeof e.data === 'object' &&
          e.data?.event === 'calendly.event_scheduled'
        ) {
          setBookingConfirmed(true);
          trackSupplierEvent('calendly_event_scheduled', {
            host_id: effectiveFounder.id,
            host_name: effectiveFounder.name,
          });
        }
      } catch {
        // Ignorar mensajes de otros orígenes
      }
    };
    window.addEventListener('message', handleMessage);
    return () => window.removeEventListener('message', handleMessage);
  }, [effectiveFounder]);

  const callBullets = [
    {
      title: 'Demostración de Kuota Payments',
      detail:
        'Verás en vivo cómo cobrar con solo 1% de comisión usando Kuota QR y Link desde el celular.',
    },
    {
      title: 'Cálculo de ahorro frente a tu POS',
      detail:
        'Revisamos tu volumen actual y proyectamos el ahorro mensual y anual que recuperas.',
    },
    {
      title: 'Activación de venta a crédito B2B',
      detail:
        'Te mostramos cómo cobrar al 100% al contado mientras tus clientes pagan en cuotas sin riesgo para ti.',
    },
  ];

  const handleHostSelect = (hostId: string) => {
    setBookingConfirmed(false);
    setIframeLoading(true);

    if (hostId === 'round-robin') {
      const assigned = getAutoAssignedFounder();
      setAutoAssignedId(assigned);
      setSelectedHostId('round-robin');
      trackSupplierEvent('cal_host_selected', {
        mode: 'round_robin',
        assigned_host: assigned,
      });
    } else {
      setSelectedHostId(hostId);
      trackSupplierEvent('cal_host_selected', {
        mode: 'direct',
        host_id: hostId,
      });
    }
  };

  return (
    <section
      id="agendar-call"
      ref={sectionRef}
      aria-labelledby="cal-cta-heading"
      className="py-16 sm:py-24 border-b border-slate-200/80 bg-slate-50/40"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="rounded-[32px] bg-[#0A192F] text-white border border-white/10 p-6 sm:p-10 lg:p-12 shadow-2xl relative overflow-hidden">
          {/* Luces de fondo */}
          <div className="absolute top-0 right-1/4 w-80 h-80 bg-[#0391FD]/15 rounded-full blur-[100px] pointer-events-none" />
          <div className="absolute bottom-0 left-1/4 w-80 h-80 bg-[#10B981]/15 rounded-full blur-[100px] pointer-events-none" />

          <div className="relative z-10 grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-10 items-start">
            {/* Izquierda: Título + 3 Bullets de la llamada + Accesos rápidos a José y Josué */}
            <div className="lg:col-span-5 flex flex-col justify-between space-y-6">
              <div>
                <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full bg-white/10 border border-white/15 text-[#38BDF8] text-xs font-bold uppercase tracking-wider mb-4">
                  <Video
                    className="w-3.5 h-3.5 text-[#38BDF8]"
                    aria-hidden="true"
                  />
                  Llamada personalizada · 30 min
                </span>

                <h2
                  id="cal-cta-heading"
                  className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight leading-tight mb-4"
                >
                  Agenda una videollamada con el equipo
                </h2>

                <p className="text-sm sm:text-base font-normal text-slate-300 mb-6 leading-relaxed">
                  Conversa directamente con{' '}
                  <strong className="text-white font-bold">José</strong> o{' '}
                  <strong className="text-white font-bold">Josué</strong>{' '}
                  para activar Kuota en tu negocio:
                </p>

                <ul className="space-y-3.5">
                  {callBullets.map((item) => (
                    <li
                      key={item.title}
                      className="rounded-2xl bg-white/5 border border-white/10 p-4 flex items-start gap-3"
                    >
                      <CheckCircle2
                        className="w-5 h-5 text-[#38BDF8] shrink-0 mt-0.5"
                        aria-hidden="true"
                      />
                      <div>
                        <div className="text-sm sm:text-base font-bold text-white">
                          {item.title}
                        </div>
                        <p className="text-xs sm:text-sm font-normal text-slate-300 mt-0.5">
                          {item.detail}
                        </p>
                      </div>
                    </li>
                  ))}
                </ul>
              </div>

              {/* Tarjeta de enlaces directos a los calendarios de José y Josué */}
              <div className="rounded-2xl bg-white/5 border border-white/10 p-4 space-y-3">
                <div className="text-xs font-bold uppercase tracking-wider text-slate-400 flex items-center gap-1.5">
                  <Calendar className="w-3.5 h-3.5 text-[#38BDF8]" aria-hidden="true" />
                  Enlaces directos de Calendly (30 min)
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-1 gap-2">
                  <a
                    href={JOSE_HOST.calLink}
                    target="_blank"
                    rel="noopener noreferrer"
                    onClick={() =>
                      trackSupplierEvent('calendly_direct_open', {
                        host_id: 'jose',
                        url: JOSE_HOST.calLink,
                      })
                    }
                    className="flex items-center justify-between gap-2 px-3.5 py-2.5 rounded-xl bg-white/10 hover:bg-[#0391FD] text-white border border-white/15 text-xs font-bold transition-colors"
                  >
                    <span>Agendar con José González (CTO)</span>
                    <ExternalLink className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                  </a>
                  <a
                    href={JOSUE_HOST.calLink}
                    target="_blank"
                    rel="noopener noreferrer"
                    onClick={() =>
                      trackSupplierEvent('calendly_direct_open', {
                        host_id: 'josue',
                        url: JOSUE_HOST.calLink,
                      })
                    }
                    className="flex items-center justify-between gap-2 px-3.5 py-2.5 rounded-xl bg-white/10 hover:bg-[#0391FD] text-white border border-white/15 text-xs font-bold transition-colors"
                  >
                    <span>Agendar con Josué Lovatón (CEO)</span>
                    <ExternalLink className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                  </a>
                </div>
              </div>
            </div>

            {/* Derecha: Calendario Calendly en vivo automatizado */}
            <div className="lg:col-span-7 flex flex-col">
              <div className="rounded-[28px] bg-white text-slate-800 border border-slate-200/90 p-4 sm:p-6 shadow-xl">
                {/* Selector de anfitrión: José, Josué o Turno Automático */}
                <div className="mb-4 pb-4 border-b border-slate-100">
                  <div className="flex flex-wrap items-center justify-between gap-2 mb-2.5">
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-700 flex items-center gap-1.5">
                      <UserCheck
                        className="w-4 h-4 text-[#0391FD]"
                        aria-hidden="true"
                      />
                      Elige con quién agendar tu sesión
                    </span>
                    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-sky-50 text-[#0391FD] text-[11px] font-bold border border-sky-200">
                      <Clock className="w-3 h-3 text-[#0391FD]" aria-hidden="true" />
                      30 min · Calendly en vivo
                    </span>
                  </div>

                  <div
                    role="group"
                    aria-label="Seleccionar miembro del equipo para la llamada"
                    className="grid grid-cols-1 sm:grid-cols-3 gap-2"
                  >
                    {CAL_COM_CONFIG.hosts.map((host) => {
                      const isSelected = host.id === selectedHostId;
                      return (
                        <button
                          key={host.id}
                          type="button"
                          onClick={() => handleHostSelect(host.id)}
                          aria-pressed={isSelected}
                          className={`p-2.5 rounded-xl border text-left transition-all cursor-pointer ${
                            isSelected
                              ? 'bg-[#0391FD] text-white border-[#0391FD] shadow-xs'
                              : 'bg-slate-50 hover:bg-slate-100 text-slate-700 border-slate-200/90'
                          }`}
                        >
                          <div className="text-xs font-bold leading-tight flex items-center justify-between gap-1">
                            <span>{host.name}</span>
                            {host.id === 'round-robin' && (
                              <RefreshCw className="w-3 h-3 shrink-0 opacity-80" aria-hidden="true" />
                            )}
                          </div>
                          <div
                            className={`text-[10px] font-medium mt-0.5 ${
                              isSelected
                                ? 'text-sky-100'
                                : 'text-slate-500'
                            }`}
                          >
                            {host.role}
                          </div>
                        </button>
                      );
                    })}
                  </div>

                  {/* Barra de estado del calendario activo */}
                  <div className="mt-3 flex flex-wrap items-center justify-between gap-2 px-3.5 py-2 rounded-xl bg-slate-50 border border-slate-200/80">
                    <div className="flex items-center gap-2 text-xs font-medium text-slate-700">
                      <Sparkles className="w-3.5 h-3.5 text-[#0391FD] shrink-0" aria-hidden="true" />
                      <span>
                        Calendario activo:{' '}
                        <strong className="font-bold text-[#0A192F]">
                          {effectiveFounder.name}
                        </strong>
                        {selectedHostId === 'round-robin' && (
                          <span className="ml-1 text-[11px] text-slate-500">
                            (asignado automáticamente)
                          </span>
                        )}
                      </span>
                    </div>

                    <a
                      href={effectiveFounder.calLink}
                      target="_blank"
                      rel="noopener noreferrer"
                      onClick={() =>
                        trackSupplierEvent('calendly_header_open_tab', {
                          host_id: effectiveFounder.id,
                          url: effectiveFounder.calLink,
                        })
                      }
                      className="inline-flex items-center gap-1 text-[11px] font-bold text-[#0391FD] hover:underline"
                    >
                      <span>Abrir en pestaña nueva</span>
                      <ExternalLink className="w-3 h-3" aria-hidden="true" />
                    </a>
                  </div>
                </div>

                {/* Confirmación automática */}
                {bookingConfirmed && (
                  <div className="mb-4 rounded-xl bg-emerald-50 text-emerald-900 border border-emerald-200 p-3.5 flex items-center justify-between gap-3">
                    <div className="flex items-center gap-2 text-xs sm:text-sm font-bold">
                      <CheckCircle2 className="w-5 h-5 text-[#10B981] shrink-0" aria-hidden="true" />
                      <span>
                        ¡Tu llamada de 30 min con {effectiveFounder.name} ha sido agendada con éxito!
                      </span>
                    </div>
                  </div>
                )}

                {/* Cuerpo del calendario Calendly embebido */}
                {!shouldLoadEmbed ? (
                  <div className="h-[580px] flex items-center justify-center text-sm font-medium text-slate-500">
                    Cargando calendario de disponibilidad…
                  </div>
                ) : (
                  <div className="relative w-full h-[580px] sm:h-[640px] rounded-2xl overflow-hidden border border-slate-200/90 bg-white">
                    {iframeLoading && (
                      <div className="absolute inset-0 z-10 flex flex-col items-center justify-center bg-slate-50/80 backdrop-blur-[2px] p-6 text-center">
                        <div className="w-8 h-8 rounded-full border-3 border-[#0391FD] border-t-transparent animate-spin mb-3" />
                        <p className="text-sm font-bold text-[#0A192F]">
                          Conectando con la agenda de {effectiveFounder.name}…
                        </p>
                        <p className="text-xs font-normal text-slate-500 mt-1">
                          Selecciona el día y la hora que mejor te acomode
                        </p>
                      </div>
                    )}
                    <iframe
                      key={effectiveFounder.calLink}
                      title={`Calendly — ${effectiveFounder.name} (30 min)`}
                      src={embedUrl}
                      loading="lazy"
                      onLoad={() => setIframeLoading(false)}
                      className="w-full h-full border-0"
                    />
                  </div>
                )}
              </div>

              {/* Enlace de respaldo */}
              <div className="mt-3 flex flex-wrap items-center justify-between gap-2 text-xs sm:text-sm">
                <span className="text-slate-400 font-medium">
                  Sincronizado en tiempo real con Google Calendar vía Calendly
                </span>
                <a
                  href={effectiveFounder.calLink || CAL_COM_CONFIG.fallbackUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  onClick={() =>
                    trackSupplierEvent('cal_fallback_link_click', {
                      host_id: effectiveFounder.id,
                      url: effectiveFounder.calLink,
                    })
                  }
                  className="inline-flex items-center gap-1.5 font-bold text-slate-300 hover:text-white underline underline-offset-4"
                >
                  <span>
                    ¿No carga? Agenda aquí con {effectiveFounder.name.split(' ')[0]}
                  </span>
                  <ExternalLink className="w-3.5 h-3.5" aria-hidden="true" />
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

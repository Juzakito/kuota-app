import React, { useEffect, useRef, useState } from 'react';
import {
  CheckCircle2,
  Calendar,
  ExternalLink,
  Clock,
  Video,
  UserCheck,
} from 'lucide-react';
import { CAL_COM_CONFIG } from '../config/pricingConfig';
import { trackSupplierEvent } from '../utils/analytics';

/**
 * PRD §5.9 y §6: CTA final con cal.com (cargado de forma diferida / lazy)
 * - Título: "Agenda una call con el equipo".
 * - Izquierda: tres bullets sobre lo que se verá en la llamada:
 *   1. Demostración del producto
 *   2. Cálculo de tu ahorro
 *   3. Plan recomendado
 * - Derecha: embed de cal.com para agendar con José o Josué (permite elegir con quién agendar).
 * - Debajo del embed: enlace de respaldo "¿No carga? Agenda aquí" que abre cal.com en otra pestaña.
 */
export const CalCtaSection: React.FC = () => {
  const sectionRef = useRef<HTMLElement | null>(null);
  const [shouldLoadEmbed, setShouldLoadEmbed] = useState(false);
  const [selectedHostId, setSelectedHostId] = useState<string>(
    CAL_COM_CONFIG.hosts[0].id
  );
  const [selectedSlot, setSelectedSlot] = useState<string>('10:00 AM');
  const [bookingConfirmed, setBookingConfirmed] = useState<boolean>(false);
  const [showLiveIframe, setShowLiveIframe] = useState<boolean>(false);

  const selectedHost =
    CAL_COM_CONFIG.hosts.find((h) => h.id === selectedHostId) ||
    CAL_COM_CONFIG.hosts[0];

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
        { rootMargin: '250px' }
      );
      observer.observe(node);
      return () => observer.disconnect();
    } else {
      setShouldLoadEmbed(true);
    }
  }, []);

  const callBullets = [
    {
      title: 'Demostración del producto',
      detail: 'Verás en vivo cómo funcionan Kuota QR, Kuota Link y aKuotas! desde el celular.',
    },
    {
      title: 'Cálculo de tu ahorro',
      detail: 'Revisamos tu volumen actual con POS y proyectamos el ahorro neto mensual y anual.',
    },
    {
      title: 'Plan recomendado',
      detail: 'Te ayudamos a elegir entre Basic, Pro u Open según el ticket y giro de tu negocio.',
    },
  ];

  const handleHostSelect = (hostId: string) => {
    setSelectedHostId(hostId);
    setBookingConfirmed(false);
    trackSupplierEvent('cal_host_selected', { host_id: hostId });
  };

  const handleConfirmDemoBooking = () => {
    setBookingConfirmed(true);
    trackSupplierEvent('cal_booking_completed', {
      host_id: selectedHost.id,
      host_name: selectedHost.name,
      slot: selectedSlot,
    });
  };

  return (
    <section
      id="agendar-call"
      ref={sectionRef}
      aria-labelledby="cal-cta-heading"
      className="py-16 sm:py-20 border-b-2 border-kuota-deepnavy"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="rounded-3xl bg-kuota-deepnavy text-white border-2 border-kuota-deepnavy p-6 sm:p-10 lg:p-12 shadow-navy-hard-lg">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-10 items-start">
            {/* Izquierda: Título + 3 Bullets de la llamada */}
            <div className="lg:col-span-5 flex flex-col justify-between">
              <div>
                <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full bg-kuota-dodger/25 border border-kuota-palesky/40 text-kuota-palesky text-xs font-extrabold uppercase tracking-wider mb-4">
                  <Video className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                  Llamada personalizada · 20 min
                </span>

                <h2
                  id="cal-cta-heading"
                  className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight leading-tight mb-4"
                >
                  Agenda una call con el equipo
                </h2>

                <p className="text-base font-semibold text-kuota-palesky mb-6">
                  Conversa directamente con{' '}
                  <strong className="text-white font-extrabold">José</strong> o{' '}
                  <strong className="text-white font-extrabold">Josué</strong>{' '}
                  para activar Kuota en tu negocio:
                </p>

                <ul className="space-y-4">
                  {callBullets.map((item) => (
                    <li
                      key={item.title}
                      className="rounded-2xl bg-white/10 border border-white/15 p-4 flex items-start gap-3"
                    >
                      <CheckCircle2
                        className="w-6 h-6 text-kuota-dodger shrink-0 mt-0.5"
                        aria-hidden="true"
                      />
                      <div>
                        <div className="text-base font-extrabold text-white">
                          {item.title}
                        </div>
                        <p className="text-xs sm:text-sm font-medium text-kuota-palesky/90 mt-0.5">
                          {item.detail}
                        </p>
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            </div>

            {/* Derecha: Embed de cal.com (lazy) + enlace de respaldo */}
            <div className="lg:col-span-7 flex flex-col">
              <div className="rounded-2xl bg-white text-kuota-deepnavy border-2 border-kuota-deepnavy p-5 sm:p-6 shadow-navy-hard">
                {/* Selector de anfitrión: José, Josué o Round-Robin (PRD §5.9 y P4) */}
                <div className="mb-4 pb-4 border-b border-kuota-deepnavy/15">
                  <div className="flex flex-wrap items-center justify-between gap-2 mb-2.5">
                    <span className="text-xs font-extrabold uppercase tracking-wider text-kuota-deepnavy flex items-center gap-1.5">
                      <UserCheck className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                      Elige con quién agendar en Cal.com
                    </span>
                    <button
                      type="button"
                      onClick={() => setShowLiveIframe((prev) => !prev)}
                      className="text-xs font-extrabold text-kuota-dodger hover:underline"
                    >
                      {showLiveIframe
                        ? 'Ver selector rápido'
                        : 'Cargar iframe externo cal.com'}
                    </button>
                  </div>

                  <div
                    role="group"
                    aria-label="Seleccionar miembro del equipo para la llamada"
                    className="grid grid-cols-1 sm:grid-cols-3 gap-2"
                  >
                    {CAL_COM_CONFIG.hosts.map((host) => {
                      const isSelected = host.id === selectedHost.id;
                      return (
                        <button
                          key={host.id}
                          type="button"
                          onClick={() => handleHostSelect(host.id)}
                          aria-pressed={isSelected}
                          className={`p-2.5 rounded-xl border-2 border-kuota-deepnavy text-left transition-all ${
                            isSelected
                              ? 'bg-kuota-dodger text-white shadow-sm'
                              : 'bg-kuota-palesky/60 hover:bg-kuota-palesky text-kuota-deepnavy'
                          }`}
                        >
                          <div className="text-xs font-extrabold leading-tight">
                            {host.name}
                          </div>
                          <div
                            className={`text-[10px] font-semibold mt-0.5 ${
                              isSelected
                                ? 'text-kuota-palesky'
                                : 'text-kuota-deepnavy/75'
                            }`}
                          >
                            {host.role}
                          </div>
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Cuerpo del widget Cal.com cargado de forma diferida (lazy) */}
                {!shouldLoadEmbed ? (
                  <div className="h-72 flex items-center justify-center text-sm font-bold text-kuota-crayola">
                    Cargando calendario de disponibilidad…
                  </div>
                ) : showLiveIframe ? (
                  <div className="w-full h-96 rounded-xl overflow-hidden border-2 border-kuota-deepnavy bg-kuota-palesky/30">
                    <iframe
                      title={`Calendario Cal.com — ${selectedHost.name}`}
                      src={`${selectedHost.calLink}?embed=true`}
                      loading="lazy"
                      className="w-full h-full border-0"
                    />
                  </div>
                ) : (
                  <div className="rounded-2xl bg-kuota-palesky/45 border-2 border-kuota-deepnavy p-4 sm:p-5">
                    <div className="flex items-center justify-between gap-2 mb-4">
                      <div>
                        <span className="text-xs font-extrabold uppercase tracking-wider text-kuota-crayola">
                          Cal.com Embed · Lazy Loaded
                        </span>
                        <h3 className="text-lg font-extrabold text-kuota-deepnavy">
                          Sesión con {selectedHost.name}
                        </h3>
                      </div>
                      <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-white border border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy">
                        <Clock className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
                        20 min
                      </span>
                    </div>

                    <p className="text-xs font-bold text-kuota-deepnavy/80 mb-3">
                      Selecciona un horario disponible para tu demostración personalizada:
                    </p>

                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 mb-4">
                      {['10:00 AM', '11:30 AM', '03:00 PM', '05:00 PM'].map(
                        (slot) => (
                          <button
                            key={slot}
                            type="button"
                            onClick={() => {
                              setSelectedSlot(slot);
                              setBookingConfirmed(false);
                            }}
                            className={`py-2.5 px-3 rounded-xl border-2 border-kuota-deepnavy text-xs font-extrabold transition-colors ${
                              selectedSlot === slot
                                ? 'bg-kuota-deepnavy text-white'
                                : 'bg-white text-kuota-deepnavy hover:bg-kuota-palesky'
                            }`}
                          >
                            {slot}
                          </button>
                        )
                      )}
                    </div>

                    {bookingConfirmed ? (
                      <div className="rounded-xl bg-kuota-dodger text-white border-2 border-kuota-deepnavy p-4 text-center">
                        <p className="text-sm font-extrabold">
                          ¡Horario seleccionado ({selectedSlot}) con {selectedHost.name}!
                        </p>
                        <a
                          href={selectedHost.calLink}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="mt-2 inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-kuota-deepnavy text-white text-xs font-extrabold"
                        >
                          <span>Finalizar reserva en Cal.com</span>
                          <ExternalLink className="w-3.5 h-3.5" aria-hidden="true" />
                        </a>
                      </div>
                    ) : (
                      <button
                        type="button"
                        onClick={handleConfirmDemoBooking}
                        className="w-full py-3.5 px-5 rounded-xl bg-kuota-dodger hover:bg-kuota-crayola text-white font-extrabold text-sm sm:text-base border-2 border-kuota-deepnavy shadow-navy-hard flex items-center justify-center gap-2 transition-all"
                      >
                        <Calendar className="w-4 h-4" aria-hidden="true" />
                        <span>
                          Confirmar horario ({selectedSlot}) con {selectedHost.name}
                        </span>
                      </button>
                    )}
                  </div>
                )}
              </div>

              {/* Enlace de respaldo requerido por PRD §5.9 */}
              <div className="mt-3 text-center sm:text-right">
                <a
                  href={selectedHost.calLink || CAL_COM_CONFIG.fallbackUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  onClick={() =>
                    trackSupplierEvent('cal_fallback_link_click', {
                      host_id: selectedHost.id,
                      url: selectedHost.calLink,
                    })
                  }
                  className="inline-flex items-center gap-1.5 text-xs sm:text-sm font-extrabold text-kuota-palesky hover:text-white underline underline-offset-4"
                >
                  <span>¿No carga? Agenda aquí</span>
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

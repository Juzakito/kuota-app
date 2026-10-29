import React, { useState } from 'react';
import { Mail, FileText, Shield, X } from 'lucide-react';
import { KuotaLogo } from './KuotaLogo';
import { PlayStoreButton } from './PlayStoreButton';
import { ScrollReveal } from '../utils/motionSystem';
import { kuotaSound } from '../utils/soundSystem';

/**
 * PRD §6.8 + FASE 3.G: CTA final + Pie de página con aparición progresiva y microinteracciones.
 */
export const CtaFooterSection: React.FC = () => {
  const [legalModal, setLegalModal] = useState<'terms' | 'privacy' | null>(null);

  const openModal = (type: 'terms' | 'privacy') => {
    kuotaSound.playSelectOption();
    setLegalModal(type);
  };

  return (
    <>
      <section
        id="descargar"
        aria-labelledby="final-cta-heading"
        className="py-14 sm:py-20"
      >
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <ScrollReveal delayMs={0} direction="scale">
            <div className="rounded-3xl bg-kuota-deepnavy text-white border-2 border-kuota-deepnavy p-8 sm:p-12 lg:p-14 shadow-navy-hard-lg text-center flex flex-col items-center">
              <span className="inline-block px-3.5 py-1 rounded-full bg-kuota-dodger/25 border border-kuota-palesky/40 text-kuota-palesky text-xs font-extrabold uppercase tracking-wider mb-4">
                Empieza hoy en 3 clics
              </span>

              <h2
                id="final-cta-heading"
                className="text-2xl sm:text-4xl lg:text-5xl font-extrabold text-white tracking-tight max-w-3xl leading-tight mb-8"
              >
                Descarga la app, completa tu perfil y empieza a financiar tu crecimiento a kuotas.
              </h2>

              <PlayStoreButton
                section="cta_final"
                label="Descargar en Play Store"
                size="lg"
                variant="inverted"
              />
            </div>
          </ScrollReveal>
        </div>
      </section>

      <footer className="border-t-2 border-kuota-deepnavy bg-white/75 py-10">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6 pb-8 border-b border-kuota-deepnavy/20">
            <div>
              <KuotaLogo size="md" />
              <p className="text-xs sm:text-sm font-semibold text-kuota-deepnavy/80 mt-2 max-w-sm">
                Financiamiento ágil de mercadería para emprendedores y pequeños comerciantes del Perú.
              </p>
            </div>

            <div className="flex flex-wrap items-center gap-5 text-sm font-extrabold text-kuota-deepnavy">
              <button
                type="button"
                onClick={() => openModal('terms')}
                className="group inline-flex items-center gap-1.5 hover:text-kuota-dodger transition-colors duration-150"
              >
                <FileText className="w-4 h-4 text-kuota-dodger transition-transform duration-150 group-hover:scale-110" aria-hidden="true" />
                <span>Términos y condiciones</span>
              </button>

              <button
                type="button"
                onClick={() => openModal('privacy')}
                className="group inline-flex items-center gap-1.5 hover:text-kuota-dodger transition-colors duration-150"
              >
                <Shield className="w-4 h-4 text-kuota-dodger transition-transform duration-150 group-hover:scale-110" aria-hidden="true" />
                <span>Política de privacidad</span>
              </button>

              <a
                href="mailto:contacto@kuota.pe"
                className="group inline-flex items-center gap-1.5 hover:text-kuota-dodger transition-colors duration-150"
              >
                <Mail className="w-4 h-4 text-kuota-dodger transition-transform duration-150 group-hover:scale-110" aria-hidden="true" />
                <span>Contacto: contacto@kuota.pe</span>
              </a>
            </div>
          </div>

          <div className="pt-6 flex flex-col sm:flex-row items-center justify-between gap-2 text-xs font-bold text-kuota-deepnavy/75">
            <span>© {new Date().getFullYear()} Kuota. Todos los derechos reservados.</span>
            <span>Hecho para emprendedores que mueven el Perú.</span>
          </div>
        </div>
      </footer>

      {legalModal && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="legal-modal-title"
          className="fixed inset-0 z-50 bg-kuota-deepnavy/60 backdrop-blur-xs flex items-center justify-center p-4 animate-fadeIn"
        >
          <div className="w-full max-w-lg rounded-3xl bg-white border-2 border-kuota-deepnavy p-6 sm:p-8 shadow-navy-hard-lg">
            <div className="flex items-center justify-between gap-4 mb-4 pb-3 border-b border-kuota-deepnavy/20">
              <h3
                id="legal-modal-title"
                className="text-xl font-extrabold text-kuota-deepnavy"
              >
                {legalModal === 'terms'
                  ? 'Términos y Condiciones de Uso'
                  : 'Política de Privacidad y Datos Personales'}
              </h3>
              <button
                type="button"
                onClick={() => setLegalModal(null)}
                aria-label="Cerrar ventana legal"
                className="w-8 h-8 rounded-lg bg-kuota-palesky border border-kuota-deepnavy flex items-center justify-center text-kuota-deepnavy hover:bg-kuota-dodger hover:text-white transition-colors"
              >
                <X className="w-4 h-4" aria-hidden="true" />
              </button>
            </div>

            <div className="text-sm font-semibold text-kuota-deepnavy/90 space-y-3 leading-relaxed">
              {legalModal === 'terms' ? (
                <>
                  <p>
                    Kuota facilita el financiamiento de compras de mercadería para emprendedores y comerciantes bajo un esquema de niveles progresivos (Niveles 1 al 6).
                  </p>
                  <p>
                    Cada operación aplica un interés de 5% con límite flat bimestral, conforme al cronograma de kuotas elegido por el usuario dentro de la aplicación móvil oficial.
                  </p>
                </>
              ) : (
                <>
                  <p>
                    En cumplimiento de la Ley N° 29733 (Ley de Protección de Datos Personales del Perú), Kuota protege tu información personal (DNI y teléfono afiliado) con cifrado de grado financiero.
                  </p>
                  <p>
                    Tus datos se utilizan exclusivamente para validar tu identidad, asignar tu nivel de compras y procesar tus pagos a través de los rieles autorizados.
                  </p>
                </>
              )}
            </div>

            <div className="mt-6 flex justify-end">
              <button
                type="button"
                onClick={() => setLegalModal(null)}
                className="px-5 py-2.5 rounded-xl bg-kuota-dodger text-white font-extrabold text-sm border-2 border-kuota-deepnavy shadow-navy-hard transition-transform duration-150 hover:-translate-y-0.5 active:translate-y-0"
              >
                Entendido
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

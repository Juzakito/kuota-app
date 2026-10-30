import React, { useState } from 'react';
import { Mail, FileText, Shield, X, Sparkles } from 'lucide-react';
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
        className="py-16 sm:py-24 bg-gradient-to-b from-white to-slate-50"
      >
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <ScrollReveal delayMs={0} direction="scale">
            <div className="rounded-[32px] bg-[#0A192F] text-white p-8 sm:p-14 lg:p-16 border border-white/10 shadow-2xl relative overflow-hidden text-center flex flex-col items-center">
              {/* Resplandor ambiental de fondo */}
              <div className="absolute top-0 right-1/4 w-72 h-72 bg-[#0391FD]/20 rounded-full blur-[90px] pointer-events-none" />
              <div className="absolute bottom-0 left-1/4 w-72 h-72 bg-[#10B981]/15 rounded-full blur-[90px] pointer-events-none" />

              <div className="relative z-10 max-w-3xl mx-auto flex flex-col items-center space-y-4">
                <span className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full bg-white/10 border border-white/15 text-[#38BDF8] text-xs font-bold uppercase tracking-wider mb-2">
                  <Sparkles className="w-3.5 h-3.5 text-[#38BDF8]" aria-hidden="true" />
                  Empieza hoy en 3 clics
                </span>

                <h2
                  id="final-cta-heading"
                  className="text-2xl sm:text-4xl lg:text-5xl font-extrabold text-white tracking-tight leading-tight"
                >
                  Descarga la app, completa tu perfil y empieza a financiar tu crecimiento a kuotas.
                </h2>

                <p className="text-base sm:text-lg font-normal text-slate-300 max-w-2xl leading-relaxed pb-4">
                  Súmate a la red de emprendedores peruanos que crecen con mercadería financiada sin ataduras.
                </p>

                <PlayStoreButton
                  section="cta_final"
                  label="Descargar en Play Store"
                  size="lg"
                  variant="inverted"
                />
              </div>
            </div>
          </ScrollReveal>
        </div>
      </section>

      <footer className="border-t border-slate-200/80 bg-white py-12">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6 pb-8 border-b border-slate-100">
            <div>
              <KuotaLogo size="md" />
              <p className="text-xs sm:text-sm font-normal text-slate-500 mt-2 max-w-sm">
                Financiamiento ágil de mercadería para emprendedores y pequeños comerciantes del Perú.
              </p>
            </div>

            <div className="flex flex-wrap items-center gap-5 text-xs sm:text-sm font-semibold text-slate-700">
              <button
                type="button"
                onClick={() => openModal('terms')}
                style={{ background: 'transparent', border: 'none', padding: 0 }}
                className="appearance-none !bg-transparent !border-0 text-slate-700 hover:text-[#0391FD] transition-colors inline-flex items-center gap-1.5 cursor-pointer"
              >
                <FileText className="w-4 h-4 text-[#0391FD]" aria-hidden="true" />
                <span>Términos y condiciones</span>
              </button>

              <button
                type="button"
                onClick={() => openModal('privacy')}
                style={{ background: 'transparent', border: 'none', padding: 0 }}
                className="appearance-none !bg-transparent !border-0 text-slate-700 hover:text-[#0391FD] transition-colors inline-flex items-center gap-1.5 cursor-pointer"
              >
                <Shield className="w-4 h-4 text-[#0391FD]" aria-hidden="true" />
                <span>Política de privacidad</span>
              </button>

              <a
                href="mailto:kuota.contact@gmail.com"
                className="hover:text-[#0391FD] transition-colors inline-flex items-center gap-1.5"
              >
                <Mail className="w-4 h-4 text-[#0391FD]" aria-hidden="true" />
                <span>kuota.contact@gmail.com</span>
              </a>
            </div>
          </div>

          <div className="pt-6 flex flex-col sm:flex-row items-center justify-between gap-2 text-xs font-medium text-slate-500">
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
          className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-md flex items-center justify-center p-4 animate-fadeIn"
        >
          <div className="w-full max-w-lg rounded-[28px] bg-white border border-slate-200/90 p-6 sm:p-8 shadow-2xl relative overflow-hidden">
            <div className="flex items-center justify-between gap-4 mb-4 pb-3 border-b border-slate-100">
              <h3
                id="legal-modal-title"
                className="text-xl font-extrabold text-[#0A192F]"
              >
                {legalModal === 'terms'
                  ? 'Términos y Condiciones de Uso'
                  : 'Política de Privacidad y Datos Personales'}
              </h3>
              <button
                type="button"
                onClick={() => setLegalModal(null)}
                aria-label="Cerrar ventana legal"
                className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-500 hover:text-slate-800 flex items-center justify-center transition-colors cursor-pointer"
              >
                <X className="w-4 h-4" aria-hidden="true" />
              </button>
            </div>

            <div className="text-xs sm:text-sm font-normal text-slate-600 space-y-3 leading-relaxed">
              {legalModal === 'terms' ? (
                <>
                  <p>
                    Kuota facilita el financiamiento de compras de mercadería para emprendedores y comerciantes bajo un esquema de niveles progresivos (Niveles 1 al 6).
                  </p>
                  <p>
                    Cada operación aplica un interés de ~5% flat bimestral, conforme al cronograma de kuotas elegido por el usuario dentro de la aplicación móvil oficial.
                  </p>
                </>
              ) : (
                <>
                  <p>
                    En cumplimiento de la Ley N° 29733 (Ley de Protección de Datos Personales del Perú), Kuota protege tu información personal con cifrado de grado financiero.
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
                className="px-6 py-2.5 rounded-full bg-[#0A192F] hover:bg-[#0391FD] text-white font-bold text-xs sm:text-sm shadow-md transition-all cursor-pointer"
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

import React, { useState } from 'react';
import { Mail, FileText, Shield, Linkedin, Instagram, X } from 'lucide-react';
import { KuotaLogo } from '../KuotaLogo';

/**
 * PRD §5.10: Footer
 * - Logo, correo de contacto, redes sociales y enlaces legales (términos y política de privacidad).
 */
export const FooterSection: React.FC = () => {
  const [legalModal, setLegalModal] = useState<'terms' | 'privacy' | null>(null);

  return (
    <>
      <footer className="bg-white/80 py-10 border-t border-kuota-deepnavy/20">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6 pb-8 border-b border-kuota-deepnavy/20">
            <div>
              <KuotaLogo size="md" />
              <p className="text-xs sm:text-sm font-semibold text-kuota-deepnavy/80 mt-2 max-w-sm">
                Soluciones de cobro con tarjeta, QR, Link y ventas en kuotas con 0 riesgo para negocios proveedores.
              </p>
            </div>

            {/* Correo de contacto, redes sociales y enlaces legales */}
            <div className="flex flex-wrap items-center gap-5 text-sm font-extrabold text-kuota-deepnavy">
              <a
                href="mailto:proveedores@kuota.pe"
                className="inline-flex items-center gap-1.5 hover:text-kuota-dodger transition-colors"
              >
                <Mail className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                <span>proveedores@kuota.pe</span>
              </a>

              <button
                type="button"
                onClick={() => setLegalModal('terms')}
                className="inline-flex items-center gap-1.5 hover:text-kuota-dodger transition-colors"
              >
                <FileText className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                <span>Términos y condiciones</span>
              </button>

              <button
                type="button"
                onClick={() => setLegalModal('privacy')}
                className="inline-flex items-center gap-1.5 hover:text-kuota-dodger transition-colors"
              >
                <Shield className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
                <span>Política de privacidad</span>
              </button>

              <div className="flex items-center gap-2">
                <a
                  href="https://www.linkedin.com"
                  target="_blank"
                  rel="noopener noreferrer"
                  aria-label="LinkedIn de Kuota"
                  className="w-9 h-9 rounded-xl bg-kuota-palesky border border-kuota-deepnavy flex items-center justify-center text-kuota-deepnavy hover:bg-kuota-dodger hover:text-white transition-colors"
                >
                  <Linkedin className="w-4 h-4" aria-hidden="true" />
                </a>
                <a
                  href="https://www.instagram.com"
                  target="_blank"
                  rel="noopener noreferrer"
                  aria-label="Instagram de Kuota"
                  className="w-9 h-9 rounded-xl bg-kuota-palesky border border-kuota-deepnavy flex items-center justify-center text-kuota-deepnavy hover:bg-kuota-dodger hover:text-white transition-colors"
                >
                  <Instagram className="w-4 h-4" aria-hidden="true" />
                </a>
              </div>
            </div>
          </div>

          <div className="pt-6 flex flex-col sm:flex-row items-center justify-between gap-2 text-xs font-bold text-kuota-deepnavy/75">
            <span>© {new Date().getFullYear()} Kuota. Todos los derechos reservados.</span>
            <span>Liquidación en 48 horas hábiles · 0 riesgo de impago para el proveedor.</span>
          </div>
        </div>
      </footer>

      {legalModal && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="supplier-legal-title"
          className="fixed inset-0 z-50 bg-kuota-deepnavy/60 backdrop-blur-xs flex items-center justify-center p-4"
        >
          <div className="w-full max-w-lg rounded-3xl bg-white border-2 border-kuota-deepnavy p-6 sm:p-8 shadow-navy-hard-lg">
            <div className="flex items-center justify-between gap-4 mb-4 pb-3 border-b border-kuota-deepnavy/20">
              <h3
                id="supplier-legal-title"
                className="text-xl font-extrabold text-kuota-deepnavy"
              >
                {legalModal === 'terms'
                  ? 'Términos Comerciales para Proveedores'
                  : 'Política de Privacidad y Protección de Datos'}
              </h3>
              <button
                type="button"
                onClick={() => setLegalModal(null)}
                aria-label="Cerrar"
                className="w-8 h-8 rounded-lg bg-kuota-palesky border border-kuota-deepnavy flex items-center justify-center text-kuota-deepnavy"
              >
                <X className="w-4 h-4" aria-hidden="true" />
              </button>
            </div>

            <div className="text-sm font-semibold text-kuota-deepnavy/90 space-y-3 leading-relaxed">
              {legalModal === 'terms' ? (
                <>
                  <p>
                    Kuota provee soluciones tecnológicas de recaudo (Kuota QR y Kuota Link) y financiamiento comercial (aKuotas!) con liquidación en 48 horas hábiles en cuentas bancarias autorizadas del comercio proveedor.
                  </p>
                  <p>
                    Las tarifas de suscripción mensual están expresadas en Soles más IGV conforme al plan contratado (Basic, Pro u Open).
                  </p>
                </>
              ) : (
                <>
                  <p>
                    En cumplimiento de la Ley N° 29733, Kuota resguarda la información comercial, tributaria (RUC) y bancaria de los proveedores afiliados bajo estándares de seguridad financiera.
                  </p>
                </>
              )}
            </div>

            <div className="mt-6 flex justify-end">
              <button
                type="button"
                onClick={() => setLegalModal(null)}
                className="px-5 py-2.5 rounded-xl bg-kuota-dodger text-white font-extrabold text-sm border-2 border-kuota-deepnavy shadow-navy-hard"
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


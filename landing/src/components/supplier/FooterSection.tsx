import React, { useState } from 'react';
import { Mail, FileText, Shield, X } from 'lucide-react';
import { KuotaLogo } from '../KuotaLogo';

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

const InstagramIcon: React.FC<{ className?: string }> = ({ className = 'w-4 h-4' }) => (
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
    <rect width="20" height="20" x="2" y="2" rx="5" ry="5" />
    <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z" />
    <line x1="17.5" x2="17.51" y1="6.5" y2="6.5" />
  </svg>
);

/**
 * PRD §5.10: Footer para Proveedores y Comercios
 */
export const FooterSection: React.FC = () => {
  const [legalModal, setLegalModal] = useState<'terms' | 'privacy' | null>(null);

  return (
    <>
      <footer className="bg-white py-12 border-t border-slate-200/80">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6 pb-8 border-b border-slate-100">
            <div>
              <KuotaLogo size="md" />
              <p className="text-xs sm:text-sm font-normal text-slate-500 mt-2 max-w-sm">
                Soluciones de cobro con tarjeta, QR, Link y ventas en cuotas con 0 riesgo para negocios proveedores.
              </p>
            </div>

            {/* Correo de contacto, redes sociales y enlaces legales */}
            <div className="flex flex-wrap items-center gap-5 text-xs sm:text-sm font-semibold text-slate-700">
              <a
                href="mailto:kuota.contact@gmail.com"
                className="inline-flex items-center gap-1.5 hover:text-[#0391FD] transition-colors"
              >
                <Mail className="w-4 h-4 text-[#0391FD]" aria-hidden="true" />
                <span>kuota.contact@gmail.com</span>
              </a>

              <button
                type="button"
                onClick={() => setLegalModal('terms')}
                style={{ background: 'transparent', border: 'none', padding: 0 }}
                className="appearance-none !bg-transparent !border-0 text-slate-700 hover:text-[#0391FD] transition-colors inline-flex items-center gap-1.5 cursor-pointer"
              >
                <FileText className="w-4 h-4 text-[#0391FD]" aria-hidden="true" />
                <span>Términos y condiciones</span>
              </button>

              <button
                type="button"
                onClick={() => setLegalModal('privacy')}
                style={{ background: 'transparent', border: 'none', padding: 0 }}
                className="appearance-none !bg-transparent !border-0 text-slate-700 hover:text-[#0391FD] transition-colors inline-flex items-center gap-1.5 cursor-pointer"
              >
                <Shield className="w-4 h-4 text-[#0391FD]" aria-hidden="true" />
                <span>Política de privacidad</span>
              </button>

              <div className="flex items-center gap-2">
                <a
                  href="https://www.linkedin.com"
                  target="_blank"
                  rel="noopener noreferrer"
                  aria-label="LinkedIn de Kuota"
                  className="w-8 h-8 rounded-full bg-slate-100 hover:bg-[#0391FD] hover:text-white border border-slate-200 flex items-center justify-center text-slate-700 transition-colors"
                >
                  <LinkedinIcon className="w-3.5 h-3.5" />
                </a>
                <a
                  href="https://www.instagram.com"
                  target="_blank"
                  rel="noopener noreferrer"
                  aria-label="Instagram de Kuota"
                  className="w-8 h-8 rounded-full bg-slate-100 hover:bg-[#0391FD] hover:text-white border border-slate-200 flex items-center justify-center text-slate-700 transition-colors"
                >
                  <InstagramIcon className="w-3.5 h-3.5" />
                </a>
              </div>
            </div>
          </div>

          <div className="pt-6 flex flex-col sm:flex-row items-center justify-between gap-2 text-xs font-medium text-slate-500">
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
          className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-md flex items-center justify-center p-4 animate-fadeIn"
        >
          <div className="w-full max-w-lg rounded-[28px] bg-white border border-slate-200/90 p-6 sm:p-8 shadow-2xl relative overflow-hidden">
            <div className="flex items-center justify-between gap-4 mb-4 pb-3 border-b border-slate-100">
              <h3
                id="supplier-legal-title"
                className="text-xl font-extrabold text-[#0A192F]"
              >
                {legalModal === 'terms'
                  ? 'Términos Comerciales para Proveedores'
                  : 'Política de Privacidad y Protección de Datos'}
              </h3>
              <button
                type="button"
                onClick={() => setLegalModal(null)}
                aria-label="Cerrar"
                className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-500 hover:text-slate-800 flex items-center justify-center transition-colors cursor-pointer"
              >
                <X className="w-4 h-4" aria-hidden="true" />
              </button>
            </div>

            <div className="text-xs sm:text-sm font-normal text-slate-600 space-y-3 leading-relaxed">
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

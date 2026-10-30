import React from 'react';
import { Mail, ShieldCheck, MapPin, Store, ShoppingBag, Handshake } from 'lucide-react';
import { KuotaLogo } from '../KuotaLogo';
import { OFFICIAL_CONTACT } from '../../utils/leadsService';
import { kuotaSound } from '../../utils/soundSystem';

interface InstitutionalFooterProps {
  onSelectBuyer: () => void;
  onSelectMerchant: () => void;
  onOpenPartnerModal: () => void;
}

export const InstitutionalFooter: React.FC<InstitutionalFooterProps> = ({
  onSelectBuyer,
  onSelectMerchant,
  onOpenPartnerModal,
}) => {
  return (
    <footer className="w-full bg-[#070E1A] text-white border-t border-white/10 pt-16 pb-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-8 lg:gap-10">
          
          {/* Columna 1 y 2: Identidad y Propuesta */}
          <div className="lg:col-span-2 space-y-4">
            <KuotaLogo size="md" variant="inverted" />
            <p className="text-xs sm:text-sm font-normal text-slate-300 leading-relaxed max-w-sm">
              Plataforma fintech peruana orientada a facilitar el acceso a compras mediante cuotas, articulando operaciones entre compradores, comercios y canales de recaudación.
            </p>
            <div className="pt-2 text-xs text-slate-300 space-y-2">
              <div className="flex items-center gap-2">
                <MapPin className="w-4 h-4 text-[#38BDF8] shrink-0" />
                <span>Lima, Perú · Cobertura nacional en expansión</span>
              </div>
              <div className="flex items-center gap-2">
                <Mail className="w-4 h-4 text-[#38BDF8] shrink-0" />
                <a
                  href={`mailto:${OFFICIAL_CONTACT.email}`}
                  className="text-slate-300 hover:text-[#38BDF8] transition-colors"
                >
                  {OFFICIAL_CONTACT.email}
                </a>
              </div>
            </div>
          </div>

          {/* Columna 3: Para Compradores */}
          <div className="space-y-3.5">
            <span className="text-xs font-bold uppercase tracking-wider text-[#38BDF8] flex items-center gap-1.5">
              <ShoppingBag className="w-3.5 h-3.5 text-[#38BDF8]" />
              Compradores
            </span>
            <ul className="space-y-2.5 text-xs text-slate-300">
              <li>
                <button
                  type="button"
                  onClick={() => {
                    kuotaSound.playSelectOption();
                    onSelectBuyer();
                  }}
                  className="text-xs font-medium text-slate-300 hover:text-white transition-colors text-left bg-transparent border-0 p-0 m-0 outline-none cursor-pointer block"
                >
                  Experiencia de Comprador
                </button>
              </li>
              <li>
                <a
                  href="#producto"
                  className="text-xs font-medium text-slate-300 hover:text-white transition-colors text-left bg-transparent border-0 p-0 m-0 cursor-pointer block"
                >
                  Cómo comprar en cuotas
                </a>
              </li>
              <li>
                <a
                  href="#beneficios"
                  className="text-xs font-medium text-slate-300 hover:text-white transition-colors text-left bg-transparent border-0 p-0 m-0 cursor-pointer block"
                >
                  Beneficios y flexibilidad
                </a>
              </li>
              <li>
                <a
                  href="#niveles"
                  className="text-xs font-medium text-slate-300 hover:text-white transition-colors text-left bg-transparent border-0 p-0 m-0 cursor-pointer block"
                >
                  Escalera de niveles
                </a>
              </li>
            </ul>
          </div>

          {/* Columna 4: Para Comercios */}
          <div className="space-y-3.5">
            <span className="text-xs font-bold uppercase tracking-wider text-[#10B981] flex items-center gap-1.5">
              <Store className="w-3.5 h-3.5 text-[#10B981]" />
              Comercios
            </span>
            <ul className="space-y-2.5 text-xs text-slate-300">
              <li>
                <button
                  type="button"
                  onClick={() => {
                    kuotaSound.playSelectOption();
                    onSelectMerchant();
                  }}
                  className="text-xs font-medium text-slate-300 hover:text-white transition-colors text-left bg-transparent border-0 p-0 m-0 outline-none cursor-pointer block"
                >
                  Portal de Comercios
                </button>
              </li>
              <li>
                <a
                  href="#operacion-comercio"
                  className="text-xs font-medium text-slate-300 hover:text-white transition-colors text-left bg-transparent border-0 p-0 m-0 cursor-pointer block"
                >
                  Operación y despacho
                </a>
              </li>
              <li>
                <a
                  href="#colocaciones"
                  className="text-xs font-medium text-slate-300 hover:text-white transition-colors text-left bg-transparent border-0 p-0 m-0 cursor-pointer block"
                >
                  Colocaciones y recaudación
                </a>
              </li>
              <li>
                <a
                  href="#calculadora"
                  className="text-xs font-medium text-slate-300 hover:text-white transition-colors text-left bg-transparent border-0 p-0 m-0 cursor-pointer block"
                >
                  Calculadora de ahorro POS
                </a>
              </li>
            </ul>
          </div>

          {/* Columna 5: Ecosistema y Aliados */}
          <div className="space-y-3.5">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-200 flex items-center gap-1.5">
              <Handshake className="w-3.5 h-3.5 text-slate-200" />
              Institucional
            </span>
            <ul className="space-y-2.5 text-xs text-slate-300">
              <li>
                <a
                  href="#diagrama"
                  className="text-xs font-medium text-slate-300 hover:text-white transition-colors text-left bg-transparent border-0 p-0 m-0 cursor-pointer block"
                >
                  Diagrama del ecosistema
                </a>
              </li>
              <li>
                <button
                  type="button"
                  onClick={() => {
                    kuotaSound.playSelectOption();
                    onOpenPartnerModal();
                  }}
                  className="text-xs font-medium text-slate-300 hover:text-white transition-colors text-left bg-transparent border-0 p-0 m-0 outline-none cursor-pointer block"
                >
                  Alianzas estratégicas
                </button>
              </li>
              <li>
                <a
                  href="#faq"
                  className="text-xs font-medium text-slate-300 hover:text-white transition-colors text-left bg-transparent border-0 p-0 m-0 cursor-pointer block"
                >
                  Preguntas frecuentes
                </a>
              </li>
              <li className="pt-1">
                <span className="text-[11px] font-bold text-[#10B981] bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20 inline-block">
                  Lanzamiento: 10 Nov 2026
                </span>
              </li>
            </ul>
          </div>

        </div>

        {/* Declaración de Transparencia y Cumplimiento Legal */}
        <div className="pt-8 border-t border-white/10 space-y-4">
          <div className="flex items-start gap-3 bg-white/[0.03] rounded-2xl p-4 sm:p-5 border border-white/10 backdrop-blur-sm">
            <ShieldCheck className="w-5 h-5 text-[#10B981] shrink-0 mt-0.5" />
            <div className="space-y-1">
              <p className="text-xs font-bold text-white">
                Aviso de transparencia y marco legal en preparación
              </p>
              <p className="text-[11px] leading-relaxed text-slate-400">
                KUOTA desarrolla actualmente su infraestructura operativa, técnica y contractual en el marco de la legislación peruana aplicable (Ley N° 29733 de Protección de Datos Personales, Código de Protección y Defensa del Consumidor y disposiciones regulatorias pertinentes). La información mostrada en este sitio público tiene fines informativos y demostrativos respecto del modelo en fase de piloto, con miras a su lanzamiento oficial el 10 de noviembre de 2026.
              </p>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 text-slate-400 text-xs">
            <p>© {new Date().getFullYear()} KUOTA. Todos los derechos reservados.</p>
            <div className="flex flex-wrap items-center gap-3 text-[11px] text-slate-400">
              <span className="hover:text-slate-200 transition-colors">Términos y Condiciones (En preparación)</span>
              <span>·</span>
              <span className="hover:text-slate-200 transition-colors">Política de Privacidad Ley 29733</span>
              <span>·</span>
              <span className="hover:text-slate-200 transition-colors">Libro de Reclamaciones Virtual</span>
            </div>
          </div>
        </div>

      </div>
    </footer>
  );
};

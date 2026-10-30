import React from 'react';
import { Mail, ShieldCheck, MapPin, Sparkles, Store, ShoppingBag, Handshake } from 'lucide-react';
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
    <footer className="w-full bg-kuota-deepnavy text-white border-t-2 border-kuota-deepnavy pt-14 pb-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-8 lg:gap-10">
          
          {/* Columna 1 y 2: Identidad y Propuesta */}
          <div className="lg:col-span-2 space-y-4">
            <KuotaLogo size="md" />
            <p className="text-xs sm:text-sm font-semibold text-kuota-palesky/90 leading-relaxed max-w-sm">
              Plataforma fintech peruana orientada a facilitar el acceso a compras mediante cuotas, articulando operaciones entre compradores, comercios y canales de recaudación.
            </p>
            <div className="pt-2 text-xs font-bold text-white/80 space-y-1.5">
              <div className="flex items-center gap-2">
                <MapPin className="w-4 h-4 text-kuota-dodger shrink-0" />
                <span>Lima, Perú · Cobertura nacional en expansión</span>
              </div>
              <div className="flex items-center gap-2">
                <Mail className="w-4 h-4 text-kuota-dodger shrink-0" />
                <a href={`mailto:${OFFICIAL_CONTACT.email}`} className="hover:text-kuota-dodger transition-colors">
                  {OFFICIAL_CONTACT.email}
                </a>
              </div>
            </div>
          </div>

          {/* Columna 3: Para Compradores */}
          <div className="space-y-3">
            <span className="text-xs font-extrabold uppercase tracking-wider text-kuota-dodger flex items-center gap-1.5">
              <ShoppingBag className="w-3.5 h-3.5" />
              Compradores
            </span>
            <ul className="space-y-2 text-xs font-bold text-kuota-palesky/90">
              <li>
                <button
                  type="button"
                  onClick={() => {
                    kuotaSound.playSelectOption();
                    onSelectBuyer();
                  }}
                  className="hover:text-white transition-colors text-left"
                >
                  Experiencia de Comprador
                </button>
              </li>
              <li>
                <a href="#producto" className="hover:text-white transition-colors">
                  Cómo comprar en cuotas
                </a>
              </li>
              <li>
                <a href="#beneficios" className="hover:text-white transition-colors">
                  Beneficios y flexibilidad
                </a>
              </li>
              <li>
                <a href="#niveles" className="hover:text-white transition-colors">
                  Escalera de niveles
                </a>
              </li>
            </ul>
          </div>

          {/* Columna 4: Para Comercios */}
          <div className="space-y-3">
            <span className="text-xs font-extrabold uppercase tracking-wider text-kuota-dodger flex items-center gap-1.5">
              <Store className="w-3.5 h-3.5" />
              Comercios
            </span>
            <ul className="space-y-2 text-xs font-bold text-kuota-palesky/90">
              <li>
                <button
                  type="button"
                  onClick={() => {
                    kuotaSound.playSelectOption();
                    onSelectMerchant();
                  }}
                  className="hover:text-white transition-colors text-left"
                >
                  Portal de Comercios
                </button>
              </li>
              <li>
                <a href="#operacion-comercio" className="hover:text-white transition-colors">
                  Operación y despacho
                </a>
              </li>
              <li>
                <a href="#colocaciones" className="hover:text-white transition-colors">
                  Colocaciones y recaudación
                </a>
              </li>
              <li>
                <a href="#calculadora" className="hover:text-white transition-colors">
                  Calculadora de ahorro POS
                </a>
              </li>
            </ul>
          </div>

          {/* Columna 5: Ecosistema y Aliados */}
          <div className="space-y-3">
            <span className="text-xs font-extrabold uppercase tracking-wider text-kuota-dodger flex items-center gap-1.5">
              <Handshake className="w-3.5 h-3.5" />
              Institucional
            </span>
            <ul className="space-y-2 text-xs font-bold text-kuota-palesky/90">
              <li>
                <a href="#diagrama" className="hover:text-white transition-colors">
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
                  className="hover:text-white transition-colors text-left"
                >
                  Alianzas estratégicas
                </button>
              </li>
              <li>
                <a href="#faq" className="hover:text-white transition-colors">
                  Preguntas frecuentes
                </a>
              </li>
              <li>
                <span className="text-[11px] font-extrabold text-kuota-dodger block">
                  Lanzamiento: 10 Nov 2026
                </span>
              </li>
            </ul>
          </div>

        </div>

        {/* Declaración de Transparencia y Cumplimiento Legal */}
        <div className="pt-8 border-t border-white/15 space-y-4 text-[11px] leading-relaxed text-kuota-palesky/75">
          <div className="flex items-start gap-2.5 bg-white/5 rounded-2xl p-4 border border-white/10">
            <ShieldCheck className="w-4 h-4 text-kuota-dodger shrink-0 mt-0.5" />
            <div>
              <p className="font-bold text-white mb-1">
                Aviso de transparencia y marco legal en preparación
              </p>
              <p>
                KUOTA desarrolla actualmente su infraestructura operativa, técnica y contractual en el marco de la legislación peruana aplicable (Ley N° 29733 de Protección de Datos Personales, Código de Protección y Defensa del Consumidor y disposiciones regulatorias pertinentes). La información mostrada en este sitio público tiene fines informativos y demostrativos respecto del modelo en fase de piloto, con miras a su lanzamiento oficial el 10 de noviembre de 2026.
              </p>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 text-white/60 font-semibold">
            <p>© {new Date().getFullYear()} KUOTA. Todos los derechos reservados.</p>
            <div className="flex flex-wrap items-center gap-4 text-[11px]">
              <span>Términos y Condiciones (En preparación)</span>
              <span>·</span>
              <span>Política de Privacidad Ley 29733</span>
              <span>·</span>
              <span>Libro de Reclamaciones Virtual</span>
            </div>
          </div>
        </div>

      </div>
    </footer>
  );
};

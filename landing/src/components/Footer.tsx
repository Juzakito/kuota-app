import React from 'react';
import { ShieldCheck, Mail, MapPin, ExternalLink } from 'lucide-react';
import { KuotaLogo } from './KuotaLogo';
import { OFFICIAL_CONTACT } from '../utils/leadsService';

export interface FooterProps {
  onNavigate: (mode: 'ecosistema' | 'compradores' | 'proveedores') => void;
}

export const Footer: React.FC<FooterProps> = ({ onNavigate }) => {
  return (
    <footer className="bg-paper border-t border-fog py-12 px-4 sm:px-6 text-charcoal">
      <div className="max-w-[1200px] mx-auto space-y-10">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Logo & Misión */}
          <div className="md:col-span-2 space-y-4">
            <KuotaLogo variant="dark" className="h-7 w-auto" />
            <p className="text-xs md:text-sm text-charcoal/80 max-w-sm leading-relaxed">
              La plataforma de compra y venta al crédito B2B para microempresas peruanas. Impulsamos el crecimiento del comercio local con financiamiento transparente y cobros digitales al menor costo.
            </p>
            <div className="flex items-center gap-2 text-xs text-forest font-semibold">
              <ShieldCheck className="w-4 h-4 text-forest" />
              <span>En proceso de afiliación a la SBS · Respaldo Científica del Sur</span>
            </div>
          </div>

          {/* Enlaces de Navegación */}
          <div className="space-y-3">
            <div className="text-xs font-bold uppercase tracking-wider text-obsidian">
              Navegación
            </div>
            <ul className="space-y-2 text-xs">
              <li>
                <button
                  type="button"
                  onClick={() => onNavigate('ecosistema')}
                  className="hover:text-forest transition-colors text-left"
                >
                  Inicio / Ecosistema
                </button>
              </li>
              <li>
                <button
                  type="button"
                  onClick={() => onNavigate('compradores')}
                  className="hover:text-forest transition-colors text-left"
                >
                  Comprar al crédito (Para bodegas)
                </button>
              </li>
              <li>
                <button
                  type="button"
                  onClick={() => onNavigate('proveedores')}
                  className="hover:text-forest transition-colors text-left"
                >
                  Proveedores y Kuota Payments (2%)
                </button>
              </li>
              <li>
                <a
                  href="#faq"
                  className="hover:text-forest transition-colors"
                >
                  Preguntas frecuentes
                </a>
              </li>
            </ul>
          </div>

          {/* Contacto y Legal */}
          <div className="space-y-3">
            <div className="text-xs font-bold uppercase tracking-wider text-obsidian">
              Contacto y Canales
            </div>
            <ul className="space-y-2 text-xs text-charcoal/80">
              <li className="flex items-center gap-2">
                <Mail className="w-3.5 h-3.5 text-forest shrink-0" />
                <a href={`mailto:${OFFICIAL_CONTACT.email}`} className="hover:text-forest">
                  {OFFICIAL_CONTACT.email}
                </a>
              </li>
              <li className="flex items-center gap-2">
                <MapPin className="w-3.5 h-3.5 text-forest shrink-0" />
                <span>{OFFICIAL_CONTACT.city}</span>
              </li>
              <li className="pt-2 text-[11px] text-pebble">
                Horario de atención: {OFFICIAL_CONTACT.hours}
              </li>
            </ul>
          </div>
        </div>

        {/* Cláusula legal estricta de PRD §1 y AGENTS.md */}
        <div className="pt-8 border-t border-fog text-[11px] text-pebble space-y-2 leading-relaxed">
          <p>
            <strong>Aviso Legal y Regulatorio:</strong> Kuota se encuentra en proceso de afiliación formal ante la Superintendencia de Banca, Seguros y AFP (SBS). Kuota no es una entidad bancaria ni realiza intermediación financiera directa con captación de depósitos del público. Todos los cobros y transferencias de dinero pasan exclusivamente a través de plataformas intermediarias autorizadas con relación directa con el banco. Kuota no custodia ni toca los fondos de los usuarios.
          </p>
          <div className="flex flex-col sm:flex-row justify-between items-center gap-2 pt-2 border-t border-fog/60">
            <div>
              © {new Date().getFullYear()} Kuota Technologies S.A.C. Todos los derechos reservados.
            </div>
            <div>
              Protección de Datos Personales — Cumplimiento estricto de la Ley N° 29733 (Perú).
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};

import React, { useState } from 'react';
import { Menu, X, ShoppingBag, Store } from 'lucide-react';
import { KuotaLogo } from './KuotaLogo';
import { PlayStoreButton } from './PlayStoreButton';
import { ScheduleCallButton } from './supplier/ScheduleCallButton';
import { trackEvent } from '../utils/analytics';

export type AudienceMode = 'compradores' | 'proveedores';

interface UnifiedNavbarProps {
  mode: AudienceMode;
  onModeChange: (mode: AudienceMode, source: string) => void;
}

const BUYER_LINKS = [
  { label: 'Producto', href: '#producto' },
  { label: 'Beneficios', href: '#beneficios' },
  { label: 'Niveles', href: '#niveles' },
  { label: 'Nosotros', href: '#nosotros' },
];

const SUPPLIER_LINKS = [
  { label: 'Producto', href: '#producto' },
  { label: 'Calculadora', href: '#calculadora' },
  { label: 'Precios', href: '#precios' },
  { label: 'Nosotros', href: '#nosotros' },
];

export const UnifiedNavbar: React.FC<UnifiedNavbarProps> = ({
  mode,
  onModeChange,
}) => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const navLinks = mode === 'compradores' ? BUYER_LINKS : SUPPLIER_LINKS;

  const handleLinkClick = (label: string) => {
    trackEvent('click_nav_link', { section: label.toLowerCase(), audience: mode });
    setMobileMenuOpen(false);
  };

  return (
    <header className="sticky top-0 z-50 w-full bg-kuota-palesky/90 backdrop-blur-md border-b-2 border-kuota-deepnavy">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 h-16 sm:h-20 flex items-center justify-between gap-3">
        {/* Izquierda: Logo oficial Kuota + Selector único de Perfil */}
        <div className="flex items-center gap-3 sm:gap-5">
          <a
            href="#inicio"
            aria-label="Kuota — Ir al inicio"
            className="focus-visible:outline-none shrink-0"
          >
            <KuotaLogo size="md" />
          </a>

          {/* Pill Switcher único Compradores / Proveedores */}
          <div
            role="tablist"
            aria-label="Seleccionar perfil de usuario"
            className="inline-flex items-center p-1 rounded-full bg-white border-2 border-kuota-deepnavy shadow-sm"
          >
            <button
              type="button"
              role="tab"
              aria-selected={mode === 'compradores'}
              onClick={() => onModeChange('compradores', 'navbar_pill')}
              className={`inline-flex items-center gap-1.5 px-2.5 sm:px-3.5 py-1.5 rounded-full text-[11px] sm:text-xs font-extrabold transition-all ${
                mode === 'compradores'
                  ? 'bg-kuota-dodger text-white shadow-sm'
                  : 'text-kuota-deepnavy hover:bg-kuota-palesky/60'
              }`}
            >
              <ShoppingBag className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
              <span>Compradores</span>
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={mode === 'proveedores'}
              onClick={() => onModeChange('proveedores', 'navbar_pill')}
              className={`inline-flex items-center gap-1.5 px-2.5 sm:px-3.5 py-1.5 rounded-full text-[11px] sm:text-xs font-extrabold transition-all ${
                mode === 'proveedores'
                  ? 'bg-kuota-deepnavy text-white shadow-sm'
                  : 'text-kuota-deepnavy hover:bg-kuota-palesky/60'
              }`}
            >
              <Store className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
              <span>Proveedores</span>
            </button>
          </div>
        </div>

        {/* Centro: Navegación Desktop */}
        <nav
          aria-label="Navegación principal"
          className="hidden md:flex items-center gap-5 lg:gap-7"
        >
          {navLinks.map((link) => (
            <a
              key={link.href}
              href={link.href}
              onClick={() => handleLinkClick(link.label)}
              className="text-sm font-bold text-kuota-deepnavy hover:text-kuota-dodger transition-colors py-1"
            >
              {link.label}
            </a>
          ))}
        </nav>

        {/* Derecha: CTA contextual en Desktop o botón hamburguesa en Móvil */}
        <div className="flex items-center gap-2">
          <div className="hidden md:block">
            {mode === 'compradores' ? (
              <PlayStoreButton
                section="navbar"
                label="Descargar app"
                size="sm"
              />
            ) : (
              <ScheduleCallButton
                section="navbar"
                label="Agenda una call"
                size="sm"
              />
            )}
          </div>

          {/* Botón hamburguesa solo en móvil (< md) */}
          <button
            type="button"
            onClick={() => setMobileMenuOpen((prev) => !prev)}
            aria-expanded={mobileMenuOpen}
            aria-controls="mobile-nav-menu"
            aria-label={mobileMenuOpen ? 'Cerrar menú de navegación' : 'Abrir menú de navegación'}
            className="md:hidden inline-flex items-center justify-center p-2 rounded-xl border-2 border-kuota-deepnavy bg-white text-kuota-deepnavy hover:bg-kuota-palesky transition-colors"
          >
            {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>
      </div>

      {/* Menú desplegable móvil */}
      {mobileMenuOpen && (
        <nav
          id="mobile-nav-menu"
          aria-label="Navegación móvil"
          className="md:hidden bg-white border-b-2 border-kuota-deepnavy px-4 pt-3 pb-5 space-y-3 shadow-lg"
        >
          <div className="flex flex-col space-y-1">
            {navLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                onClick={() => handleLinkClick(link.label)}
                className="px-3 py-2.5 rounded-xl text-base font-bold text-kuota-deepnavy hover:bg-kuota-palesky transition-colors"
              >
                {link.label}
              </a>
            ))}
          </div>

          <div className="pt-2 border-t border-kuota-deepnavy/15">
            {mode === 'compradores' ? (
              <PlayStoreButton
                section="navbar"
                size="md"
                className="w-full"
              />
            ) : (
              <ScheduleCallButton
                section="navbar_mobile"
                size="md"
                className="w-full"
                onClickExtra={() => setMobileMenuOpen(false)}
              />
            )}
          </div>
        </nav>
      )}
    </header>
  );
};

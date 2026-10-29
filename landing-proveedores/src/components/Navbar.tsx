import React, { useState } from 'react';
import { Menu, X } from 'lucide-react';
import { KuotaLogo } from './KuotaLogo';
import { ScheduleCallButton } from './ScheduleCallButton';

/**
 * PRD §5.1: Navbar fija
 * - Logo de Kuota a la izquierda.
 * - Enlaces de ancla: Producto, Precios, Nosotros.
 * - Botón "Agenda una call" a la derecha, estilo primario (Dodger Blue).
 * - En móvil: logo + botón; los enlaces pasan a un menú hamburguesa.
 */
export const Navbar: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const navLinks = [
    { label: 'Producto', href: '#producto' },
    { label: 'Precios', href: '#precios' },
    { label: 'Nosotros', href: '#nosotros' },
  ];

  return (
    <header className="sticky top-0 z-50 w-full bg-kuota-palesky/95 backdrop-blur-md border-b-2 border-kuota-deepnavy">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-3">
        {/* Logo de Kuota a la izquierda */}
        <a
          href="#inicio"
          aria-label="Kuota Proveedores — Ir al inicio"
          className="inline-flex items-center shrink-0"
        >
          <KuotaLogo size="sm" />
        </a>

        {/* Enlaces de ancla en desktop */}
        <nav
          aria-label="Navegación principal"
          className="hidden md:flex items-center gap-8 text-sm font-extrabold text-kuota-deepnavy"
        >
          {navLinks.map((link) => (
            <a
              key={link.href}
              href={link.href}
              className="hover:text-kuota-dodger transition-colors"
            >
              {link.label}
            </a>
          ))}
        </nav>

        {/* Derecha: Botón "Agenda una call" + Botón menú hamburguesa en móvil */}
        <div className="flex items-center gap-2.5">
          <ScheduleCallButton
            section="navbar"
            label="Agenda una call"
            size="sm"
          />

          <button
            type="button"
            onClick={() => setMobileMenuOpen((prev) => !prev)}
            aria-expanded={mobileMenuOpen}
            aria-controls="mobile-nav-menu"
            aria-label={mobileMenuOpen ? 'Cerrar menú' : 'Abrir menú'}
            className="md:hidden w-10 h-10 rounded-xl bg-white border-2 border-kuota-deepnavy flex items-center justify-center text-kuota-deepnavy hover:bg-kuota-dodger hover:text-white transition-colors"
          >
            {mobileMenuOpen ? (
              <X className="w-5 h-5" aria-hidden="true" />
            ) : (
              <Menu className="w-5 h-5" aria-hidden="true" />
            )}
          </button>
        </div>
      </div>

      {/* Menú hamburguesa desplegable en móvil */}
      {mobileMenuOpen && (
        <nav
          id="mobile-nav-menu"
          aria-label="Menú móvil"
          className="md:hidden border-t-2 border-kuota-deepnavy bg-white px-4 py-4 space-y-2 shadow-navy-hard"
        >
          {navLinks.map((link) => (
            <a
              key={link.href}
              href={link.href}
              onClick={() => setMobileMenuOpen(false)}
              className="block px-4 py-2.5 rounded-xl font-extrabold text-kuota-deepnavy hover:bg-kuota-palesky transition-colors"
            >
              {link.label}
            </a>
          ))}
        </nav>
      )}
    </header>
  );
};

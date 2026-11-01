import React, { useState } from 'react';
import { Menu, X, ArrowUpRight } from 'lucide-react';
import { KuotaLogo } from './KuotaLogo';

export type LandingMode = 'ecosistema' | 'compradores' | 'proveedores' | 'gracias';

export interface NavbarProps {
  mode: LandingMode;
  onModeChange: (nextMode: LandingMode) => void;
}

export const Navbar: React.FC<NavbarProps> = ({ mode, onModeChange }) => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const navItems: { id: LandingMode; label: string; href: string }[] = [
    { id: 'ecosistema', label: 'Ecosistema', href: '/' },
    { id: 'compradores', label: 'Comprar al crédito', href: '/comprar' },
    { id: 'proveedores', label: 'Proveedores & POS', href: '/proveedores' },
  ];

  const handleNavClick = (targetMode: LandingMode, href: string) => {
    onModeChange(targetMode);
    setMobileMenuOpen(false);
  };

  return (
    <header className="sticky top-0 z-50 bg-paper/95 backdrop-blur-md border-b border-fog">
      <div className="max-w-[1200px] mx-auto h-16 px-4 sm:px-6 flex items-center justify-between gap-4">
        {/* Logo Kuota */}
        <div
          onClick={() => handleNavClick('ecosistema', '/')}
          className="cursor-pointer flex items-center gap-2 select-none"
        >
          <KuotaLogo variant="dark" className="h-7 w-auto" />
        </div>

        {/* Segmented Pill Navigation (Desktop) - DESIGN.md */}
        <nav className="hidden md:flex items-center bg-fog p-1 rounded-pill">
          {navItems.map((item) => {
            const isActive = mode === item.id;
            return (
              <button
                key={item.id}
                type="button"
                onClick={() => handleNavClick(item.id, item.href)}
                className={`px-4 py-1.5 text-xs font-semibold rounded-pill transition-all cursor-pointer ${
                  isActive
                    ? 'bg-lime text-forest font-bold shadow-subtle'
                    : 'text-charcoal hover:text-forest'
                }`}
              >
                {item.label}
              </button>
            );
          })}
        </nav>

        {/* Acciones de la derecha (Pill Buttons) */}
        <div className="hidden lg:flex items-center gap-3">
          <button
            type="button"
            onClick={() => handleNavClick('proveedores', '/proveedores')}
            className="px-4 py-2 text-xs font-semibold rounded-pill border border-forest text-forest hover:bg-fog transition-colors cursor-pointer"
          >
            Soy proveedor
          </button>
          <button
            type="button"
            onClick={() => handleNavClick('compradores', '/comprar')}
            className="px-4 py-2 text-xs font-bold rounded-pill bg-lime text-forest hover:bg-lime-hover transition-colors cursor-pointer shadow-subtle"
          >
            Soy comprador
          </button>
        </div>

        {/* Botón menú móvil */}
        <div className="md:hidden flex items-center">
          <button
            type="button"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="p-2 text-forest rounded-card hover:bg-fog"
            aria-label="Alternar menú"
          >
            {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </div>

      {/* Menú Móvil Desplegable */}
      {mobileMenuOpen && (
        <div className="md:hidden bg-paper border-b border-fog px-4 py-4 space-y-3">
          <div className="flex flex-col space-y-1">
            {navItems.map((item) => (
              <button
                key={item.id}
                type="button"
                onClick={() => handleNavClick(item.id, item.href)}
                className={`text-left px-4 py-2.5 rounded-card text-sm font-semibold transition-colors ${
                  mode === item.id ? 'bg-linen text-forest' : 'text-charcoal hover:bg-fog'
                }`}
              >
                {item.label}
              </button>
            ))}
          </div>

          <div className="pt-3 border-t border-fog flex flex-col gap-2">
            <button
              type="button"
              onClick={() => handleNavClick('proveedores', '/proveedores')}
              className="w-full py-2.5 px-4 text-xs font-semibold rounded-pill border border-forest text-forest text-center hover:bg-fog"
            >
              Soy proveedor
            </button>
            <button
              type="button"
              onClick={() => handleNavClick('compradores', '/comprar')}
              className="w-full py-2.5 px-4 text-xs font-bold rounded-pill bg-lime text-forest text-center hover:bg-lime-hover shadow-subtle"
            >
              Soy comprador
            </button>
          </div>
        </div>
      )}
    </header>
  );
};

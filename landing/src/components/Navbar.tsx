import React from 'react';
import { KuotaLogo } from './KuotaLogo';
import { PlayStoreButton } from './PlayStoreButton';

export const Navbar: React.FC = () => {
  return (
    <header className="sticky top-0 z-50 w-full bg-white/90 backdrop-blur-md border-b border-slate-200/80">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-4">
        <a
          href="#inicio"
          aria-label="Kuota — Ir al inicio"
          className="inline-flex items-center"
        >
          <KuotaLogo size="sm" />
        </a>

        <nav
          aria-label="Secciones principales"
          className="hidden md:flex items-center gap-6 text-sm font-semibold text-slate-700"
        >
          <a href="#problema-solucion" className="hover:text-[#0391FD] transition-colors">
            Beneficios
          </a>
          <a href="#producto" className="hover:text-[#0391FD] transition-colors">
            Cómo funciona
          </a>
          <a href="#formalizacion" className="hover:text-[#0391FD] transition-colors">
            Formalización
          </a>
          <a href="#niveles" className="hover:text-[#0391FD] transition-colors">
            Niveles
          </a>
          <a href="#confianza" className="hover:text-[#0391FD] transition-colors">
            Nosotros
          </a>
        </nav>

        <PlayStoreButton
          section="navbar"
          label="Descargar"
          size="sm"
        />
      </div>
    </header>
  );
};

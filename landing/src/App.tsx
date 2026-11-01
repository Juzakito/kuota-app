'use client';

import React, { useEffect, useState } from 'react';
import { Navbar, LandingMode } from './components/Navbar';
import { EcosistemaLanding } from './components/EcosistemaLanding';
import { CompradoresLanding } from './components/CompradoresLanding';
import { ProveedoresLanding } from './components/ProveedoresLanding';
import { GraciasPage } from './components/GraciasPage';
import { Footer } from './components/Footer';
import { trackEvent } from './utils/analytics';

function getInitialMode(): LandingMode {
  if (typeof window === 'undefined') return 'ecosistema';

  const pathname = window.location.pathname.toLowerCase();
  const searchParams = new URLSearchParams(window.location.search);
  const perfilParam = searchParams.get('perfil') || searchParams.get('modo');
  const hash = window.location.hash.toLowerCase();

  if (pathname.includes('/comprar') || perfilParam === 'compradores' || hash === '#compradores') {
    return 'compradores';
  }
  if (
    pathname.includes('/proveedores') ||
    perfilParam === 'proveedores' ||
    perfilParam === 'comercios' ||
    hash === '#proveedores' ||
    hash === '#comercios'
  ) {
    return 'proveedores';
  }
  if (pathname.includes('/gracias') || perfilParam === 'gracias' || hash === '#gracias') {
    return 'gracias';
  }

  return 'ecosistema';
}

export const App: React.FC = () => {
  const [mode, setMode] = useState<LandingMode>('ecosistema');

  useEffect(() => {
    const initial = getInitialMode();
    setMode(initial);

    const handlePopState = () => {
      setMode(getInitialMode());
    };

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const handleModeChange = (nextMode: LandingMode) => {
    setMode(nextMode);
    trackEvent('switch_landing_mode', { mode: nextMode });

    if (typeof window !== 'undefined') {
      try {
        const url = new URL(window.location.href);
        if (nextMode === 'ecosistema') {
          url.pathname = '/';
          url.searchParams.delete('perfil');
          url.searchParams.delete('modo');
          url.hash = '';
        } else if (nextMode === 'compradores') {
          url.pathname = '/comprar';
          url.searchParams.set('perfil', 'compradores');
          url.hash = '';
        } else if (nextMode === 'proveedores') {
          url.pathname = '/proveedores';
          url.searchParams.set('perfil', 'proveedores');
          url.hash = '';
        } else if (nextMode === 'gracias') {
          url.pathname = '/gracias';
          url.searchParams.set('perfil', 'gracias');
          url.hash = '';
        }
        window.history.pushState({}, '', url.toString());
      } catch {
        // Soporte para entornos estáticos
      }
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  useEffect(() => {
    if (typeof document !== 'undefined') {
      const titles = {
        ecosistema: 'Compra y vende al crédito para tu negocio | Kuota',
        compradores: 'Compra mercadería en cuotas semanales | Kuota',
        proveedores: 'Vende al crédito y cobra con 2% de comisión | Kuota',
        gracias: '¡Gracias por confiar en Kuota! | Kuota',
      };
      document.title = titles[mode] || titles.ecosistema;
    }
  }, [mode]);

  return (
    <div className="min-h-screen flex flex-col bg-paper text-charcoal font-sans antialiased selection:bg-lime selection:text-forest">
      {/* Barra de navegación unificada */}
      <Navbar mode={mode} onModeChange={handleModeChange} />

      {/* Contenido principal según el modo activo */}
      <main className="flex-1">
        {mode === 'ecosistema' && (
          <EcosistemaLanding
            onSelectBuyer={() => handleModeChange('compradores')}
            onSelectSupplier={() => handleModeChange('proveedores')}
          />
        )}

        {mode === 'compradores' && <CompradoresLanding />}

        {mode === 'proveedores' && <ProveedoresLanding />}

        {mode === 'gracias' && (
          <GraciasPage onGoHome={() => handleModeChange('ecosistema')} />
        )}
      </main>

      {/* Footer general */}
      <Footer onNavigate={(target) => handleModeChange(target)} />
    </div>
  );
};

export default App;

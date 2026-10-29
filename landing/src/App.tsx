import React, { useEffect, useRef, useState } from 'react';
import { UnifiedNavbar, AudienceMode } from './components/UnifiedNavbar';
import { CrossAudienceBanner } from './components/EcosystemBanner';
import { LaunchCountdownSection } from './components/LaunchCountdownSection';

// Secciones Compradores (B2C / Emprendedores)
import { HeroSection as BuyerHeroSection } from './components/HeroSection';
import { ProblemSolutionSection as BuyerProblemSolutionSection } from './components/ProblemSolutionSection';
import { PaymentMethodsSection as BuyerPaymentMethodsSection } from './components/PaymentMethodsSection';
import { ProductChipsSection as BuyerProductChipsSection } from './components/ProductChipsSection';
import { FormalizationSection as BuyerFormalizationSection } from './components/FormalizationSection';
import { LevelsSection as BuyerLevelsSection } from './components/LevelsSection';
import { TrustSection as BuyerTrustSection } from './components/TrustSection';
import { CtaFooterSection as BuyerCtaFooterSection } from './components/CtaFooterSection';

// Secciones Proveedores (B2B / Comercios)
import { HeroSection as SupplierHeroSection } from './components/supplier/HeroSection';
import { ProblemSolutionSection as SupplierProblemSolutionSection } from './components/supplier/ProblemSolutionSection';
import { SocialProofSection as SupplierSocialProofSection } from './components/supplier/SocialProofSection';
import { ProductChipsSection as SupplierProductChipsSection } from './components/supplier/ProductChipsSection';
import { SavingsCalculatorSection as SupplierSavingsCalculatorSection } from './components/supplier/SavingsCalculatorSection';
import { TrustSection as SupplierTrustSection } from './components/supplier/TrustSection';
import { PricingSection as SupplierPricingSection } from './components/supplier/PricingSection';
import { CalCtaSection as SupplierCalCtaSection } from './components/supplier/CalCtaSection';
import { FooterSection as SupplierFooterSection } from './components/supplier/FooterSection';

import { trackEvent } from './utils/analytics';

function getInitialAudienceMode(): AudienceMode {
  if (typeof window === 'undefined') return 'compradores';
  const params = new URLSearchParams(window.location.search);
  const perfil = params.get('perfil') || params.get('modo');
  if (
    perfil === 'proveedores' ||
    window.location.hash === '#proveedores' ||
    window.location.hash === '#agendar-call'
  ) {
    return 'proveedores';
  }
  return 'compradores';
}

export const App: React.FC = () => {
  const [mode, setMode] = useState<AudienceMode>('compradores');
  const milestonesTracked = useRef<Set<number>>(new Set());
  const levelsSectionTracked = useRef<boolean>(false);

  useEffect(() => {
    setMode(getInitialAudienceMode());
  }, []);

  const handleModeChange = (nextMode: AudienceMode, source: string) => {
    if (nextMode === mode) return;
    setMode(nextMode);
    trackEvent('switch_audience_mode', { mode: nextMode, source });

    if (typeof window !== 'undefined') {
      const url = new URL(window.location.href);
      url.searchParams.set('perfil', nextMode);
      window.history.replaceState({}, '', url.toString());
    }
  };

  useEffect(() => {
    if (typeof document !== 'undefined') {
      document.title =
        mode === 'compradores'
          ? 'Kuota — Financia tu crecimiento sin apuros | Compra mercadería a kuotas'
          : 'Kuota Proveedores — Reduce comisiones POS al 2% y cobra con tarjeta o en kuotas';
    }
  }, [mode]);

  useEffect(() => {
    const handleScroll = () => {
      const scrollTop = window.scrollY;
      const docHeight =
        document.documentElement.scrollHeight - window.innerHeight;
      if (docHeight <= 0) return;

      const percent = Math.round((scrollTop / docHeight) * 100);
      [25, 50, 75, 100].forEach((milestone) => {
        if (
          percent >= milestone &&
          !milestonesTracked.current.has(milestone)
        ) {
          milestonesTracked.current.add(milestone);
          trackEvent('scroll_depth', { depth_percent: milestone, audience: mode });
        }
      });

      if (mode === 'compradores' && !levelsSectionTracked.current) {
        const levelsEl = document.getElementById('niveles');
        if (levelsEl) {
          const rect = levelsEl.getBoundingClientRect();
          if (rect.top <= window.innerHeight * 0.8) {
            levelsSectionTracked.current = true;
            trackEvent('scroll_to_levels_section', { section: 'niveles' });
          }
        }
      }
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, [mode]);

  return (
    <div
      id="inicio"
      className="min-h-screen flex flex-col bg-kuota-palesky text-kuota-crayola font-sans"
    >
      {/* 1. Navbar unificada con selector único de perfil Compradores / Proveedores */}
      <UnifiedNavbar mode={mode} onModeChange={handleModeChange} />

      {/* 2. Contenido principal según el perfil activo */}
      <main key={mode} className="flex-1 animate-fadeIn">
        {mode === 'compradores' ? (
          <>
            <BuyerHeroSection />
            <LaunchCountdownSection mode="compradores" />
            <BuyerProblemSolutionSection />
            <BuyerPaymentMethodsSection />
            <BuyerProductChipsSection />
            <BuyerFormalizationSection />
            <BuyerLevelsSection />
            <CrossAudienceBanner mode={mode} onModeChange={handleModeChange} />
            <BuyerTrustSection />
            <BuyerCtaFooterSection />
          </>
        ) : (
          <>
            <SupplierHeroSection />
            <LaunchCountdownSection mode="proveedores" />
            <SupplierProblemSolutionSection />
            <SupplierSocialProofSection />
            <SupplierProductChipsSection />
            <SupplierSavingsCalculatorSection />
            <SupplierTrustSection />
            <SupplierPricingSection />
            <CrossAudienceBanner mode={mode} onModeChange={handleModeChange} />
            <SupplierCalCtaSection />
            <SupplierFooterSection />
          </>
        )}
      </main>
    </div>
  );
};

export default App;

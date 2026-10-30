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
  const [renderedMode, setRenderedMode] = useState<AudienceMode>('compradores');
  const [transitionStage, setTransitionStage] = useState<
    'idle' | 'out' | 'in-prep'
  >('idle');
  const [slideDir, setSlideDir] = useState<1 | -1>(1);

  const stageTimerRef = useRef<number | null>(null);
  const enterTimerRef = useRef<number | null>(null);
  const milestonesTracked = useRef<Set<number>>(new Set());
  const levelsSectionTracked = useRef<boolean>(false);

  const clearTransitionTimers = () => {
    if (stageTimerRef.current !== null) {
      window.clearTimeout(stageTimerRef.current);
      stageTimerRef.current = null;
    }
    if (enterTimerRef.current !== null) {
      window.clearTimeout(enterTimerRef.current);
      enterTimerRef.current = null;
    }
  };

  useEffect(() => {
    const initial = getInitialAudienceMode();
    setMode(initial);
    setRenderedMode(initial);

    const handlePopState = () => {
      const next = getInitialAudienceMode();
      setMode(next);
      setRenderedMode(next);
      setTransitionStage('idle');
    };
    window.addEventListener('popstate', handlePopState);
    return () => {
      window.removeEventListener('popstate', handlePopState);
      clearTransitionTimers();
    };
  }, []);

  const handleModeChange = (nextMode: AudienceMode, source: string) => {
    if (nextMode === mode && transitionStage === 'idle') return;

    const direction: 1 | -1 = nextMode === 'proveedores' ? 1 : -1;
    setSlideDir(direction);
    setMode(nextMode);
    trackEvent('switch_audience_mode', { mode: nextMode, source });

    if (typeof window !== 'undefined') {
      try {
        const url = new URL(window.location.href);
        url.searchParams.set('perfil', nextMode);
        window.history.replaceState({}, '', url.toString());
      } catch {
        // Ignore URL replace errors in restricted environments
      }
    }

    clearTransitionTimers();
    setTransitionStage('out');

    stageTimerRef.current = window.setTimeout(() => {
      setRenderedMode(nextMode);
      setTransitionStage('in-prep');

      enterTimerRef.current = window.setTimeout(() => {
        setTransitionStage('idle');
      }, 28);
    }, 170);
  };

  useEffect(() => {
    if (typeof document !== 'undefined') {
      document.title =
        mode === 'compradores'
          ? 'Kuota — Financia tu crecimiento sin apuros | Compra mercadería a kuotas'
          : 'Kuota Proveedores — Reduce comisiones POS al 2% y cobra con tarjeta o en kuotas';

      const iconHref = '/logos/kuota-icon.png';
      const existingIcons = document.querySelectorAll<HTMLLinkElement>(
        'link[rel="icon"], link[rel="shortcut icon"], link[rel="apple-touch-icon"]'
      );
      if (existingIcons.length > 0) {
        existingIcons.forEach((linkEl) => {
          linkEl.type = 'image/png';
          linkEl.href = iconHref;
        });
      } else {
        const linkEl = document.createElement('link');
        linkEl.rel = 'icon';
        linkEl.type = 'image/png';
        linkEl.href = iconHref;
        document.head.appendChild(linkEl);
      }
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

  const mainTransitionStyle: React.CSSProperties =
    transitionStage === 'out'
      ? {
          opacity: 0,
          transform: `translate3d(${slideDir * -26}px, 6px, 0) scale(0.992)`,
          filter: 'blur(2px)',
          transition:
            'opacity 170ms cubic-bezier(0.4, 0, 0.2, 1), transform 170ms cubic-bezier(0.4, 0, 0.2, 1), filter 170ms cubic-bezier(0.4, 0, 0.2, 1)',
          willChange: 'opacity, transform, filter',
        }
      : transitionStage === 'in-prep'
      ? {
          opacity: 0,
          transform: `translate3d(${slideDir * 26}px, 10px, 0) scale(0.992)`,
          filter: 'blur(2px)',
          transition: 'none',
          willChange: 'opacity, transform, filter',
        }
      : {
          opacity: 1,
          transform: 'translate3d(0, 0, 0) scale(1)',
          filter: 'blur(0px)',
          transition:
            'opacity 380ms cubic-bezier(0.16, 1, 0.3, 1), transform 380ms cubic-bezier(0.16, 1, 0.3, 1), filter 380ms cubic-bezier(0.16, 1, 0.3, 1)',
        };

  return (
    <div
      id="inicio"
      className="min-h-screen flex flex-col bg-kuota-palesky text-kuota-crayola font-sans overflow-x-hidden"
    >
      {/* 1. Navbar unificada con selector animado de perfil Compradores / Proveedores */}
      <UnifiedNavbar
        mode={mode}
        isSwitching={transitionStage !== 'idle'}
        onModeChange={handleModeChange}
      />

      {/* 2. Contenido principal con transición direccional suave entre ambos perfiles */}
      <main
        key={renderedMode}
        style={mainTransitionStyle}
        className="flex-1"
      >
        {renderedMode === 'compradores' ? (
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

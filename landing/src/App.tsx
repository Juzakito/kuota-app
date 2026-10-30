import React, { useEffect, useRef, useState } from 'react';
import { UnifiedNavbar, AudienceMode } from './components/UnifiedNavbar';
import { CrossAudienceBanner } from './components/EcosystemBanner';
import { LaunchCountdownSection } from './components/LaunchCountdownSection';

// Modales globales de captación y alianzas
import { MerchantAffiliationModal } from './components/ecosystem/MerchantAffiliationModal';
import { PartnerContactModal } from './components/ecosystem/PartnerContactModal';

// Secciones del Ecosistema Principal (Hub /)
import { EcosystemHeroSection } from './components/ecosystem/EcosystemHeroSection';
import { ExperienceSelectorSection } from './components/ecosystem/ExperienceSelectorSection';
import { EcosystemFlowDiagram } from './components/ecosystem/EcosystemFlowDiagram';
import { MerchantOperationDemo } from './components/ecosystem/MerchantOperationDemo';
import { ColocacionesRecaudacionSection } from './components/ecosystem/ColocacionesRecaudacionSection';
import { AudienceFaqSection } from './components/ecosystem/AudienceFaqSection';
import { MasterCtaSection } from './components/ecosystem/MasterCtaSection';
import { InstitutionalFooter } from './components/ecosystem/InstitutionalFooter';

// Secciones Compradores (B2C / Emprendedores)
import { HeroSection as BuyerHeroSection } from './components/HeroSection';
import { ProblemSolutionSection as BuyerProblemSolutionSection } from './components/ProblemSolutionSection';
import { PaymentMethodsSection as BuyerPaymentMethodsSection } from './components/PaymentMethodsSection';
import { ProductChipsSection as BuyerProductChipsSection } from './components/ProductChipsSection';
import { FormalizationSection as BuyerFormalizationSection } from './components/FormalizationSection';
import { LevelsSection as BuyerLevelsSection } from './components/LevelsSection';
import { TrustSection as BuyerTrustSection } from './components/TrustSection';
import { CtaFooterSection as BuyerCtaFooterSection } from './components/CtaFooterSection';

// Secciones Proveedores / Comercios (B2B)
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
import { smoothScrollToAnchor } from './utils/motionSystem';

function getInitialAudienceMode(): AudienceMode {
  if (typeof window === 'undefined') return 'ecosistema';
  const params = new URLSearchParams(window.location.search);
  const perfil = params.get('perfil') || params.get('modo');
  if (
    perfil === 'proveedores' ||
    perfil === 'comercios' ||
    window.location.hash === '#proveedores' ||
    window.location.hash === '#comercios' ||
    window.location.hash === '#agendar-call'
  ) {
    return 'comercios';
  }
  if (perfil === 'compradores' || window.location.hash === '#compradores') {
    return 'compradores';
  }
  if (perfil === 'ecosistema') {
    return 'ecosistema';
  }
  // Si no hay parámetro de perfil, la vista de entrada es el Hub del Ecosistema completo
  return 'ecosistema';
}

export const App: React.FC = () => {
  const [mode, setMode] = useState<AudienceMode>('ecosistema');
  const [renderedMode, setRenderedMode] = useState<AudienceMode>('ecosistema');
  const [transitionStage, setTransitionStage] = useState<'idle' | 'out' | 'in-prep'>('idle');
  const [slideDir, setSlideDir] = useState<1 | -1>(1);

  // Estados de modales interactivos
  const [merchantModalOpen, setMerchantModalOpen] = useState(false);
  const [partnerModalOpen, setPartnerModalOpen] = useState(false);

  const stageTimerRef = useRef<number | null>(null);
  const enterTimerRef = useRef<number | null>(null);
  const milestonesTracked = useRef<Set<number>>(new Set());

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

    const direction: 1 | -1 =
      nextMode === 'compradores' ? 1 : nextMode === 'comercios' ? 1 : -1;
    setSlideDir(direction);
    setMode(nextMode);
    trackEvent('switch_audience_mode', { mode: nextMode, source });

    if (typeof window !== 'undefined') {
      try {
        const url = new URL(window.location.href);
        if (nextMode === 'ecosistema') {
          url.searchParams.delete('perfil');
          url.searchParams.delete('modo');
        } else {
          url.searchParams.set('perfil', nextMode);
        }
        window.history.replaceState({}, '', url.toString());
      } catch {
        // Ignorar restricciones en entornos de testing
      }
    }

    clearTransitionTimers();
    setTransitionStage('out');

    stageTimerRef.current = window.setTimeout(() => {
      setRenderedMode(nextMode);
      setTransitionStage('in-prep');

      enterTimerRef.current = window.setTimeout(() => {
        setTransitionStage('idle');
        smoothScrollToAnchor('#inicio');
      }, 35);
    }, 180);
  };

  useEffect(() => {
    if (typeof document !== 'undefined') {
      document.title =
        mode === 'ecosistema'
          ? 'KUOTA — Ecosistema Fintech de Compras en Cuotas | Conectamos Oportunidades'
          : mode === 'compradores'
          ? 'Kuota — Financia tu crecimiento sin apuros | Compra mercadería a kuotas'
          : 'Kuota Comercios — Ofrece ventas en cuotas y gestiona tu recaudación';

      const iconHref = '/logos/kuota-icon.png';
      const existingIcons = document.querySelectorAll<HTMLLinkElement>(
        'link[rel="icon"], link[rel="shortcut icon"], link[rel="apple-touch-icon"]'
      );
      if (existingIcons.length > 0) {
        existingIcons.forEach((linkEl) => {
          linkEl.type = 'image/png';
          linkEl.href = iconHref;
        });
      }
    }
  }, [mode]);

  useEffect(() => {
    const handleScroll = () => {
      const scrollTop = window.scrollY;
      const docHeight = document.documentElement.scrollHeight - window.innerHeight;
      if (docHeight <= 0) return;

      const percent = Math.round((scrollTop / docHeight) * 100);
      [25, 50, 75, 100].forEach((milestone) => {
        if (percent >= milestone && !milestonesTracked.current.has(milestone)) {
          milestonesTracked.current.add(milestone);
          trackEvent('scroll_depth', { depth_percent: milestone, audience: mode });
        }
      });
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, [mode]);

  const mainTransitionStyle: React.CSSProperties =
    transitionStage === 'out'
      ? {
          opacity: 0,
          transform: `translate3d(${slideDir * -24}px, 6px, 0) scale(0.992)`,
          filter: 'blur(2px)',
          transition: 'opacity 180ms ease, transform 180ms ease, filter 180ms ease',
        }
      : transitionStage === 'in-prep'
      ? {
          opacity: 0,
          transform: `translate3d(${slideDir * 24}px, 8px, 0) scale(0.992)`,
          filter: 'blur(2px)',
          transition: 'none',
        }
      : {
          opacity: 1,
          transform: 'translate3d(0, 0, 0) scale(1)',
          filter: 'blur(0px)',
          transition: 'opacity 360ms cubic-bezier(0.16, 1, 0.3, 1), transform 360ms cubic-bezier(0.16, 1, 0.3, 1)',
        };

  return (
    <div
      id="inicio"
      className="min-h-screen flex flex-col bg-kuota-palesky text-kuota-crayola font-sans overflow-x-hidden"
    >
      {/* 1. Navbar Unificada con Selector Tripartito de Perfil (Ecosistema / Compradores / Comercios) */}
      <UnifiedNavbar
        mode={mode}
        isSwitching={transitionStage !== 'idle'}
        onModeChange={handleModeChange}
        onOpenMerchantAffiliation={() => setMerchantModalOpen(true)}
      />

      {/* 2. Modales Globales de Conversión */}
      <MerchantAffiliationModal
        isOpen={merchantModalOpen}
        onClose={() => setMerchantModalOpen(false)}
      />
      <PartnerContactModal
        isOpen={partnerModalOpen}
        onClose={() => setPartnerModalOpen(false)}
      />

      {/* 3. Contenido Principal según el Perfil Activo */}
      <main key={renderedMode} style={mainTransitionStyle} className="flex-1">
        {renderedMode === 'ecosistema' ? (
          <>
            {/* HUB ECOSISTEMA PRINCIPAL */}
            <EcosystemHeroSection
              onSelectBuyer={() => handleModeChange('compradores', 'hero_cta_buyer')}
              onOpenMerchantAffiliation={() => setMerchantModalOpen(true)}
              onExploreEcosystem={() => smoothScrollToAnchor('#diagrama')}
            />
            <LaunchCountdownSection mode="ecosistema" />
            <ExperienceSelectorSection
              onSelectBuyer={() => handleModeChange('compradores', 'experience_selector')}
              onSelectMerchant={() => handleModeChange('comercios', 'experience_selector')}
              onOpenPartnerModal={() => setPartnerModalOpen(true)}
            />
            <EcosystemFlowDiagram />
            <MerchantOperationDemo
              onOpenAffiliation={() => setMerchantModalOpen(true)}
            />
            <ColocacionesRecaudacionSection />
            <SupplierSocialProofSection />
            <AudienceFaqSection />
            <MasterCtaSection
              onSelectBuyer={() => handleModeChange('compradores', 'master_cta_buyer')}
              onOpenMerchantAffiliation={() => setMerchantModalOpen(true)}
              onOpenPartnerModal={() => setPartnerModalOpen(true)}
            />
            <InstitutionalFooter
              onSelectBuyer={() => handleModeChange('compradores', 'footer_link')}
              onSelectMerchant={() => handleModeChange('comercios', 'footer_link')}
              onOpenPartnerModal={() => setPartnerModalOpen(true)}
            />
          </>
        ) : renderedMode === 'compradores' ? (
          <>
            {/* VISTA COMPRADORES / EMPRENDEDORES */}
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
            {/* VISTA COMERCIOS / PROVEEDORES */}
            <SupplierHeroSection />
            <LaunchCountdownSection mode="proveedores" />
            <MerchantOperationDemo
              onOpenAffiliation={() => setMerchantModalOpen(true)}
            />
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

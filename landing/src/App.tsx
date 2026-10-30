'use client';

import React, { useEffect, useRef, useState } from 'react';
import { NavbarCapsule } from './components/landing/NavbarCapsule';
import { HeroBentoSplit } from './components/landing/HeroBentoSplit';
import { OrbitalMetricsSection } from './components/landing/OrbitalMetricsSection';
import { StickyFeatureShowcase } from './components/landing/StickyFeatureShowcase';
import { VideoStoriesCarousel } from './components/landing/VideoStoriesCarousel';
import { LevelsGamification3D } from './components/landing/LevelsGamification3D';
import { TestimonialsMarquee } from './components/landing/TestimonialsMarquee';
import { EditorialBlogCarousel } from './components/landing/EditorialBlogCarousel';
import { OnboardingPhoneStepper } from './components/landing/OnboardingPhoneStepper';
import { FinancialBentoGrid } from './components/landing/FinancialBentoGrid';
import { FaqSplitAccordion } from './components/landing/FaqSplitAccordion';
import { LayeredPreFooter } from './components/landing/LayeredPreFooter';
import { FloatingLeadWidgets } from './components/landing/FloatingLeadWidgets';

// Modales globales de captación y alianzas
import { MerchantAffiliationModal } from './components/ecosystem/MerchantAffiliationModal';
import { PartnerContactModal } from './components/ecosystem/PartnerContactModal';

// Vistas secundarias especializadas por query param
import { UnifiedNavbar, AudienceMode } from './components/UnifiedNavbar';
import { HeroSection as BuyerHeroSection } from './components/HeroSection';
import { ProblemSolutionSection as BuyerProblemSolutionSection } from './components/ProblemSolutionSection';
import { PaymentMethodsSection as BuyerPaymentMethodsSection } from './components/PaymentMethodsSection';
import { ProductChipsSection as BuyerProductChipsSection } from './components/ProductChipsSection';
import { FormalizationSection as BuyerFormalizationSection } from './components/FormalizationSection';
import { LevelsSection as BuyerLevelsSection } from './components/LevelsSection';
import { TrustSection as BuyerTrustSection } from './components/TrustSection';
import { CtaFooterSection as BuyerCtaFooterSection } from './components/CtaFooterSection';

import { HeroSection as SupplierHeroSection } from './components/supplier/HeroSection';
import { ProblemSolutionSection as SupplierProblemSolutionSection } from './components/supplier/ProblemSolutionSection';
import { SocialProofSection as SupplierSocialProofSection } from './components/supplier/SocialProofSection';
import { ProductChipsSection as SupplierProductChipsSection } from './components/supplier/ProductChipsSection';
import { SavingsCalculatorSection as SupplierSavingsCalculatorSection } from './components/supplier/SavingsCalculatorSection';
import { TrustSection as SupplierTrustSection } from './components/supplier/TrustSection';
import { PricingSection as SupplierPricingSection } from './components/supplier/PricingSection';
import { CalCtaSection as SupplierCalCtaSection } from './components/supplier/CalCtaSection';
import { FooterSection as SupplierFooterSection } from './components/supplier/FooterSection';
import { CrossAudienceBanner } from './components/EcosystemBanner';

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
    window.location.hash === '#comercios'
  ) {
    return 'comercios';
  }
  if (perfil === 'compradores' || window.location.hash === '#compradores') {
    return 'compradores';
  }
  // Por defecto: Ecosistema Flagship Fintech Redesign (Estilo iO / Vercel / Stripe)
  return 'ecosistema';
}

export const App: React.FC = () => {
  const [mode, setMode] = useState<AudienceMode>('ecosistema');
  const [evaluationModalOpen, setEvaluationModalOpen] = useState(false);
  const [merchantModalOpen, setMerchantModalOpen] = useState(false);
  const [partnerModalOpen, setPartnerModalOpen] = useState(false);
  const milestonesTracked = useRef<Set<number>>(new Set());

  useEffect(() => {
    const initial = getInitialAudienceMode();
    setMode(initial);

    const handlePopState = () => {
      setMode(getInitialAudienceMode());
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const handleModeChange = (nextMode: AudienceMode, source: string) => {
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
        // Ignorar restricciones en testing
      }
    }
    smoothScrollToAnchor('#inicio');
  };

  useEffect(() => {
    if (typeof document !== 'undefined') {
      document.title =
        mode === 'ecosistema'
          ? 'KUOTA — El Sistema Operativo de Compras B2B e IA Predictiva'
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

  return (
    <div
      id="inicio"
      className="min-h-screen flex flex-col bg-[#F4F6F8] text-[#070A0F] font-sans overflow-x-hidden selection:bg-[#10B981] selection:text-black"
    >
      {/* MODAL GLOBAL DE CAPTACIÓN RÁPIDA / EVALUACIÓN B2B */}
      <FloatingLeadWidgets
        isOpen={evaluationModalOpen}
        onOpen={() => setEvaluationModalOpen(true)}
        onClose={() => setEvaluationModalOpen(false)}
      />

      {/* MODALES ADICIONALES DE ALIADOS Y AFILIACIÓN */}
      <MerchantAffiliationModal
        isOpen={merchantModalOpen}
        onClose={() => setMerchantModalOpen(false)}
      />
      <PartnerContactModal
        isOpen={partnerModalOpen}
        onClose={() => setPartnerModalOpen(false)}
      />

      {/* RENDERIZADO PRINCIPAL */}
      {mode === 'ecosistema' ? (
        <>
          {/* ========================================================= */}
          {/* EXPERIENCIA MAESTRA FLAGSHIP (12 SECCIONES DEL PRD iO)    */}
          {/* ========================================================= */}

          {/* SECCIÓN 1: NAVBAR FIJO CON CÁPSULA CENTRAL */}
          <NavbarCapsule
            onOpenEvaluationModal={() => setEvaluationModalOpen(true)}
          />

          <main className="flex-1 space-y-4">
            {/* SECCIÓN 2: HERO PRINCIPAL SPLIT EN MACRO-CONTENEDOR CURVO */}
            <HeroBentoSplit
              onOpenEvaluationModal={() => setEvaluationModalOpen(true)}
              onExploreDemo={() => smoothScrollToAnchor('#plataforma')}
            />

            {/* SECCIÓN 3: EL SISTEMA ORBITAL / CÍRCULOS CONCÉNTRICOS INTERACTIVOS */}
            <OrbitalMetricsSection
              onOpenEvaluationModal={() => setEvaluationModalOpen(true)}
            />

            {/* SECCIÓN 4: SHOWCASE ASIMÉTRICO STICKY (CEREBRO IA Y ARO CÓNICO) */}
            <StickyFeatureShowcase
              onOpenEvaluationModal={() => setEvaluationModalOpen(true)}
            />

            {/* SECCIÓN 5: BLOQUE MONOLÍTICO OSCURO - CARRUSEL 3D DE VIDEO-HISTORIAS */}
            <VideoStoriesCarousel />

            {/* SECCIÓN 6: BANNER OSCURO DE GAMIFICACIÓN Y NIVELES 3D */}
            <LevelsGamification3D
              onOpenEvaluationModal={() => setEvaluationModalOpen(true)}
            />

            {/* SECCIÓN 7: MARQUEE INFINITO DE TESTIMONIOS REALES */}
            <TestimonialsMarquee />

            {/* SECCIÓN 8: CARRUSEL EDITORIAL & BLOG (NUESTRO RADAR B2B) */}
            <EditorialBlogCarousel />

            {/* SECCIÓN 9: STEPPER INTERACTIVO DE ONBOARDING */}
            <OnboardingPhoneStepper
              onOpenEvaluationModal={() => setEvaluationModalOpen(true)}
            />

            {/* SECCIÓN 10: BENTO GRID DE TRANSPARENCIA FINANCIERA */}
            <FinancialBentoGrid />

            {/* SECCIÓN 11: PREGUNTAS FRECUENTES SPLIT */}
            <FaqSplitAccordion />

            {/* SECCIÓN 12: PRE-FOOTER ANIMADO 3D + FOOTER MONOLÍTICO */}
            <LayeredPreFooter
              onOpenEvaluationModal={() => setEvaluationModalOpen(true)}
            />
          </main>
        </>
      ) : mode === 'compradores' ? (
        <>
          {/* VISTA ESPECÍFICA DE COMPRADORES */}
          <UnifiedNavbar
            mode={mode}
            onModeChange={handleModeChange}
            onOpenMerchantAffiliation={() => setEvaluationModalOpen(true)}
          />
          <main className="flex-1">
            <BuyerHeroSection />
            <BuyerProblemSolutionSection />
            <BuyerPaymentMethodsSection />
            <BuyerProductChipsSection />
            <BuyerFormalizationSection />
            <BuyerLevelsSection />
            <CrossAudienceBanner mode={mode} onModeChange={handleModeChange} />
            <BuyerTrustSection />
            <BuyerCtaFooterSection />
          </main>
        </>
      ) : (
        <>
          {/* VISTA ESPECÍFICA DE PROVEEDORES / COMERCIOS */}
          <UnifiedNavbar
            mode={mode}
            onModeChange={handleModeChange}
            onOpenMerchantAffiliation={() => setEvaluationModalOpen(true)}
          />
          <main className="flex-1">
            <SupplierHeroSection />
            <SupplierProblemSolutionSection />
            <SupplierSocialProofSection />
            <SupplierProductChipsSection />
            <SupplierSavingsCalculatorSection />
            <SupplierTrustSection />
            <SupplierPricingSection />
            <CrossAudienceBanner mode={mode} onModeChange={handleModeChange} />
            <SupplierCalCtaSection />
            <SupplierFooterSection />
          </main>
        </>
      )}
    </div>
  );
};

export default App;

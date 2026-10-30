'use client';

import React, { useEffect, useRef, useState } from 'react';
import { UnifiedNavbar, AudienceMode } from './components/UnifiedNavbar';
import { CrossAudienceBanner } from './components/EcosystemBanner';
import { LaunchCountdownSection } from './components/LaunchCountdownSection';

// Secciones del Ecosistema Principal (Hub Ecosistema)
import { EcosystemHeroSection } from './components/ecosystem/EcosystemHeroSection';
import { ExperienceSelectorSection } from './components/ecosystem/ExperienceSelectorSection';
import { EcosystemFlowDiagram } from './components/ecosystem/EcosystemFlowDiagram';
import { MerchantOperationDemo } from './components/ecosystem/MerchantOperationDemo';
import { ColocacionesRecaudacionSection } from './components/ecosystem/ColocacionesRecaudacionSection';
import { AudienceFaqSection } from './components/ecosystem/AudienceFaqSection';
import { MasterCtaSection } from './components/ecosystem/MasterCtaSection';
import { InstitutionalFooter } from './components/ecosystem/InstitutionalFooter';

// Modales globales de captación y alianzas
import { MerchantAffiliationModal } from './components/ecosystem/MerchantAffiliationModal';
import { PartnerContactModal } from './components/ecosystem/PartnerContactModal';
import { FloatingLeadWidgets } from './components/landing/FloatingLeadWidgets';

// Vistas especializadas de Compradores
import { HeroSection as BuyerHeroSection } from './components/HeroSection';
import { ProblemSolutionSection as BuyerProblemSolutionSection } from './components/ProblemSolutionSection';
import { PaymentMethodsSection as BuyerPaymentMethodsSection } from './components/PaymentMethodsSection';
import { ProductChipsSection as BuyerProductChipsSection } from './components/ProductChipsSection';
import { FormalizationSection as BuyerFormalizationSection } from './components/FormalizationSection';
import { LevelsSection as BuyerLevelsSection } from './components/LevelsSection';
import { TrustSection as BuyerTrustSection } from './components/TrustSection';
import { CtaFooterSection as BuyerCtaFooterSection } from './components/CtaFooterSection';

// Vistas especializadas de Proveedores / Comercios
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

      {/* RENDERIZADO PRINCIPAL SEGÚN EL PERFIL ACTIVO */}
      {mode === 'ecosistema' ? (
        <>
          {/* ========================================================= */}
          {/* HUB ECOSISTEMA KUOTA (Compradores ↔ KUOTA ↔ Comercios)   */}
          {/* ========================================================= */}

          {/* NAVBAR UNIFICADA CON SELECTOR DE PERFILES TRIPARTITO */}
          <UnifiedNavbar
            mode={mode}
            onModeChange={handleModeChange}
            onOpenMerchantAffiliation={() => setMerchantModalOpen(true)}
          />

          <main className="flex-1">
            {/* 1. HERO DEL ECOSISTEMA (Conectamos compras y ventas en cuotas) */}
            <EcosystemHeroSection
              onSelectBuyer={() => handleModeChange('compradores', 'hero_cta_buyer')}
              onOpenMerchantAffiliation={() => setMerchantModalOpen(true)}
              onExploreEcosystem={() => smoothScrollToAnchor('#diagrama')}
            />

            {/* 2. CONTADOR OFICIAL DE LANZAMIENTO */}
            <LaunchCountdownSection mode="ecosistema" />

            {/* 3. SELECTOR DE EXPERIENCIAS (Compradores / Comercios / Aliados) */}
            <ExperienceSelectorSection
              onSelectBuyer={() => handleModeChange('compradores', 'experience_selector')}
              onSelectMerchant={() => handleModeChange('comercios', 'experience_selector')}
              onOpenPartnerModal={() => setPartnerModalOpen(true)}
            />

            {/* 4. DIAGRAMA INTERACTIVO DEL FLUJO COMPLETO (6 Pasos) */}
            <EcosystemFlowDiagram />

            {/* 5. DEMOSTRACIÓN OPERATIVA EN COMERCIO (Mostrador, QR, Despacho) */}
            <MerchantOperationDemo
              onOpenAffiliation={() => setMerchantModalOpen(true)}
            />

            {/* 6. COLOCACIONES Y RECAUDACIÓN TRAZABLE (Ledger y Conciliación) */}
            <ColocacionesRecaudacionSection />

            {/* 7. PRUEBA SOCIAL Y RESPALDO COMERCIAL */}
            <SupplierSocialProofSection />

            {/* 8. PREGUNTAS FRECUENTES POR AUDIENCIA */}
            <AudienceFaqSection />

            {/* 9. MASTER CTA FINAL */}
            <MasterCtaSection
              onSelectBuyer={() => handleModeChange('compradores', 'master_cta_buyer')}
              onOpenMerchantAffiliation={() => setMerchantModalOpen(true)}
              onOpenPartnerModal={() => setPartnerModalOpen(true)}
            />

            {/* 10. FOOTER INSTITUCIONAL */}
            <InstitutionalFooter
              onSelectBuyer={() => handleModeChange('compradores', 'footer_link')}
              onSelectMerchant={() => handleModeChange('comercios', 'footer_link')}
              onOpenPartnerModal={() => setPartnerModalOpen(true)}
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

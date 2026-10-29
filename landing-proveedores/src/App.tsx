import React, { useEffect } from 'react';
import { Navbar } from './components/Navbar';
import { HeroSection } from './components/HeroSection';
import { ProblemSolutionSection } from './components/ProblemSolutionSection';
import { SocialProofSection } from './components/SocialProofSection';
import { ProductChipsSection } from './components/ProductChipsSection';
import { SavingsCalculatorSection } from './components/SavingsCalculatorSection';
import { TrustSection } from './components/TrustSection';
import { PricingSection } from './components/PricingSection';
import { CalCtaSection } from './components/CalCtaSection';
import { FooterSection } from './components/FooterSection';
import { trackScrollDepth } from './utils/analytics';

export const App: React.FC = () => {
  useEffect(() => {
    const cleanup = trackScrollDepth();
    return cleanup;
  }, []);

  return (
    <div className="min-h-screen bg-kuota-surface text-kuota-text flex flex-col font-sans selection:bg-kuota-tint selection:text-kuota-navy">
      {/* 1. Navbar fija */}
      <Navbar />

      <main className="flex-1">
        {/* 2. Hero */}
        <HeroSection />

        {/* 3. Problema + solución */}
        <ProblemSolutionSection />

        {/* 4. Prueba social (bancos y billeteras 360 Pay) */}
        <SocialProofSection />

        {/* 5. Producto — tres chips */}
        <ProductChipsSection />

        {/* 6. Úsalo — calculadora de ahorro */}
        <SavingsCalculatorSection />

        {/* 7. Confianza — quiénes somos */}
        <TrustSection />

        {/* 8. Precios */}
        <PricingSection />

        {/* 9. CTA final con cal.com */}
        <CalCtaSection />
      </main>

      {/* 10. Footer */}
      <FooterSection />
    </div>
  );
};

export default App;

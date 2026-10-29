import React, { useEffect, useRef } from 'react';
import { Navbar } from './components/Navbar';
import { HeroSection } from './components/HeroSection';
import { ProblemSolutionSection } from './components/ProblemSolutionSection';
import { PaymentMethodsSection } from './components/PaymentMethodsSection';
import { ProductChipsSection } from './components/ProductChipsSection';
import { FormalizationSection } from './components/FormalizationSection';
import { LevelsSection } from './components/LevelsSection';
import { TrustSection } from './components/TrustSection';
import { CtaFooterSection } from './components/CtaFooterSection';
import { trackEvent } from './utils/analytics';

export const App: React.FC = () => {
  const milestonesTracked = useRef<Set<number>>(new Set());
  const levelsSectionTracked = useRef<boolean>(false);

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
          trackEvent('scroll_depth', { depth_percent: milestone });
        }
      });

      // Métrica secundaria PRD §1: scroll hasta la sección de niveles
      if (!levelsSectionTracked.current) {
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
  }, []);

  return (
    <div className="min-h-screen flex flex-col bg-kuota-palesky text-kuota-crayola font-sans">
      {/* Barra superior fija y delgada con logo y botón Descargar siempre visible (PRD §5) */}
      <Navbar />

      {/* Contenido principal con las 8 secciones en el orden exacto del PRD §5 */}
      <main className="flex-1">
        {/* 1. Hero */}
        <HeroSection />

        {/* 2. Problema y solución */}
        <ProblemSolutionSection />

        {/* 3. Medios de pago */}
        <PaymentMethodsSection />

        {/* 4. Producto (3 chips con captura interactiva) */}
        <ProductChipsSection />

        {/* 5. Crecemos junto a ti (formalización, con 1 imagen placeholder y sin gráfico de pasos) */}
        <FormalizationSection />

        {/* 6. Niveles a tu medida (switcher 1..6 + escalera ascendente) */}
        <LevelsSection />

        {/* 7. Confianza (equipo e instituciones) */}
        <TrustSection />

        {/* 8. CTA final + Pie de página */}
        <CtaFooterSection />
      </main>
    </div>
  );
};

export default App;

import React, { useEffect, useRef, useState } from 'react';

/**
 * Escala oficial de tiempos y curvas del Sistema de Movimiento Kuota (FASE 4).
 */
export const MOTION_TOKENS = {
  duration: {
    microMs: 160,      // 120–200 ms: botones, enlaces, iconos, selectores
    normalMs: 280,     // 200–350 ms: tarjetas, pestañas, modales, barras
    sectionMs: 550,    // 400–700 ms: entradas de sección y stagger
    narrativeMs: 750,  // 600–1000 ms: demostraciones narrativas de kuotas
  },
  easing: {
    springOut: 'cubic-bezier(0.16, 1, 0.3, 1)',
    smoothOut: 'cubic-bezier(0.22, 1, 0.36, 1)',
  },
} as const;

export function usePrefersReducedMotion(): boolean {
  const [reduced, setReduced] = useState<boolean>(() => {
    if (typeof window === 'undefined' || !window.matchMedia) return false;
    return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  });

  useEffect(() => {
    if (typeof window === 'undefined' || !window.matchMedia) return;
    const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
    const handler = (event: MediaQueryListEvent) => setReduced(event.matches);

    mediaQuery.addEventListener('change', handler);
    return () => mediaQuery.removeEventListener('change', handler);
  }, []);

  return reduced;
}

interface ScrollRevealProps {
  children: React.ReactNode;
  delayMs?: number;
  direction?: 'up' | 'left' | 'right' | 'scale';
  className?: string;
  threshold?: number;
}

/**
 * Componente de aparición progresiva en scroll (FASE 6 y 7).
 * Utiliza únicamente `transform` y `opacity` con aceleración GPU, sin bloquear
 * la interacción del usuario ni causar desplazamientos de layout (CLS = 0).
 */
export const ScrollReveal: React.FC<ScrollRevealProps> = ({
  children,
  delayMs = 0,
  direction = 'up',
  className = '',
  threshold = 0.12,
}) => {
  const reducedMotion = usePrefersReducedMotion();
  const [isVisible, setIsVisible] = useState<boolean>(reducedMotion);
  const ref = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (reducedMotion) {
      setIsVisible(true);
      return;
    }

    const node = ref.current;
    if (!node || typeof IntersectionObserver === 'undefined') {
      setIsVisible(true);
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            setIsVisible(true);
            observer.unobserve(entry.target);
          }
        });
      },
      {
        threshold,
        rootMargin: '0px 0px -36px 0px',
      }
    );

    observer.observe(node);
    return () => observer.disconnect();
  }, [reducedMotion, threshold]);

  const hiddenTransform = {
    up: 'translate3d(0, 22px, 0)',
    left: 'translate3d(-22px, 0, 0)',
    right: 'translate3d(22px, 0, 0)',
    scale: 'translate3d(0, 14px, 0) scale(0.97)',
  }[direction];

  const style: React.CSSProperties = reducedMotion
    ? {}
    : {
        opacity: isVisible ? 1 : 0,
        transform: isVisible ? 'translate3d(0, 0, 0) scale(1)' : hiddenTransform,
        transitionProperty: 'opacity, transform',
        transitionDuration: `${MOTION_TOKENS.duration.sectionMs}ms`,
        transitionTimingFunction: MOTION_TOKENS.easing.springOut,
        transitionDelay: `${delayMs}ms`,
        willChange: isVisible ? 'auto' : 'opacity, transform',
      };

  return (
    <div ref={ref} style={style} className={className}>
      {children}
    </div>
  );
};

interface TiltCardProps {
  children: React.ReactNode;
  className?: string;
  maxTiltDeg?: number;
}

/**
 * Contenedor con movimiento 3D sutil en escritorio y respuesta táctil limpia en móvil (FASE 3.C y FASE 8).
 */
export const TiltCard: React.FC<TiltCardProps> = ({
  children,
  className = '',
  maxTiltDeg = 2.8,
}) => {
  const reducedMotion = usePrefersReducedMotion();
  const cardRef = useRef<HTMLDivElement | null>(null);
  const [transformStyle, setTransformStyle] = useState<string>(
    'perspective(900px) rotateX(0deg) rotateY(0deg) translate3d(0, 0, 0)'
  );

  const handleMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
    if (reducedMotion || typeof window === 'undefined' || window.innerWidth < 1024) {
      return;
    }
    const el = cardRef.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();
    const xRatio = (e.clientX - rect.left) / rect.width - 0.5;
    const yRatio = (e.clientY - rect.top) / rect.height - 0.5;

    const rotateY = (xRatio * maxTiltDeg * 2).toFixed(2);
    const rotateX = (-yRatio * maxTiltDeg * 2).toFixed(2);
    setTransformStyle(
      `perspective(900px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translate3d(0, -3px, 0)`
    );
  };

  const handleMouseLeave = () => {
    if (reducedMotion) return;
    setTransformStyle(
      'perspective(900px) rotateX(0deg) rotateY(0deg) translate3d(0, 0, 0)'
    );
  };

  return (
    <div
      ref={cardRef}
      onMouseMove={handleMouseMove}
      onMouseLeave={handleMouseLeave}
      style={
        reducedMotion
          ? undefined
          : {
              transform: transformStyle,
              transition: `transform ${MOTION_TOKENS.duration.normalMs}ms ${MOTION_TOKENS.easing.smoothOut}`,
            }
      }
      className={`active:scale-[0.99] transition-shadow ${className}`}
    >
      {children}
    </div>
  );
};

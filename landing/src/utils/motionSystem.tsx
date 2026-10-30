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
  return false;
}

let activeScrollRafId: number | null = null;

/**
 * Desplazamiento suave por hardware (requestAnimationFrame) hacia una sección
 * por su selector (`#producto`, `#beneficios`, `#niveles`, `#confianza`, etc.),
 * compensando la altura de la barra de navegación fija e iniciando una animación
 * de llegada en la sección destino. Funciona incluso cuando el sistema operativo
 * desactiva el `scroll-behavior: smooth` nativo del navegador.
 */
export function smoothScrollToAnchor(href: string): void {
  if (typeof window === 'undefined' || typeof document === 'undefined') return;

  if (activeScrollRafId !== null) {
    window.cancelAnimationFrame(activeScrollRafId);
    activeScrollRafId = null;
  }

  const isTop = href === '#inicio' || href === '#';
  const targetEl = isTop
    ? document.getElementById('inicio')
    : (document.querySelector(href) as HTMLElement | null);

  if (!targetEl && !isTop) return;

  const headerEl = document.querySelector('header');
  const headerOffset = headerEl ? headerEl.getBoundingClientRect().height : 76;

  const startY = window.scrollY || window.pageYOffset;
  const rawTargetY = isTop
    ? 0
    : startY + targetEl!.getBoundingClientRect().top - headerOffset + 2;
  const maxScrollY = Math.max(
    0,
    document.documentElement.scrollHeight - window.innerHeight
  );
  const destinationY = Math.max(0, Math.min(rawTargetY, maxScrollY));
  const distance = destinationY - startY;

  try {
    const url = new URL(window.location.href);
    url.hash = isTop ? '' : href;
    window.history.replaceState({}, '', url.toString());
  } catch {
    // Ignorar errores de historial en entornos restringidos
  }

  // Activar animación de llegada en el contenedor interno de la sección destino
  if (!isTop && targetEl) {
    const innerContainer =
      (targetEl.querySelector(':scope > div') as HTMLElement | null) || targetEl;
    innerContainer.classList.remove('kuota-section-arrive');
    // Forzar reflow para reiniciar la animación si el usuario vuelve a hacer clic
    void innerContainer.offsetWidth;
    innerContainer.classList.add('kuota-section-arrive');
    window.setTimeout(() => {
      innerContainer.classList.remove('kuota-section-arrive');
    }, 750);
  }

  if (Math.abs(distance) < 4) {
    window.scrollTo(0, destinationY);
    return;
  }

  const durationMs = Math.min(780, Math.max(460, Math.abs(distance) * 0.38));
  const startTime = performance.now();

  // Curva easeOutQuart suave y precisa
  const easeOutQuart = (t: number): number => 1 - Math.pow(1 - t, 4);

  const step = (now: number) => {
    const elapsed = now - startTime;
    const progress = Math.min(1, elapsed / durationMs);
    const eased = easeOutQuart(progress);

    window.scrollTo(0, Math.round(startY + distance * eased));

    if (progress < 1) {
      activeScrollRafId = window.requestAnimationFrame(step);
    } else {
      activeScrollRafId = null;
    }
  };

  activeScrollRafId = window.requestAnimationFrame(step);
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
 * Renderiza visible por defecto en SSR y en el primer pliegue para evitar
 * pantallas en blanco al recargar o hidratar en Next.js / Vite.
 */
export const ScrollReveal: React.FC<ScrollRevealProps> = ({
  children,
  delayMs = 0,
  direction = 'up',
  className = '',
  threshold = 0.08,
}) => {
  const reducedMotion = usePrefersReducedMotion();
  const [isVisible, setIsVisible] = useState<boolean>(true);
  const ref = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (reducedMotion || typeof window === 'undefined') {
      setIsVisible(true);
      return;
    }

    const node = ref.current;
    if (!node || typeof IntersectionObserver === 'undefined') {
      setIsVisible(true);
      return;
    }

    const rect = node.getBoundingClientRect();
    // Si el nodo aún está en un contenedor oculto de hidratación (0x0) o ya está dentro/cerca del viewport actual, mantenerlo visible
    if (
      (rect.width === 0 && rect.height === 0) ||
      rect.top < window.innerHeight * 0.95
    ) {
      setIsVisible(true);
      return;
    }

    setIsVisible(false);

    const reveal = () => setIsVisible(true);

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting || entry.intersectionRatio > 0) {
            reveal();
            observer.disconnect();
          }
        });
      },
      {
        threshold: Math.min(threshold, 0.08),
        rootMargin: '80px 0px 40px 0px',
      }
    );

    observer.observe(node);

    const handleScrollFallback = () => {
      if (!ref.current) return;
      const currentRect = ref.current.getBoundingClientRect();
      if (currentRect.top < window.innerHeight * 0.98) {
        reveal();
        window.removeEventListener('scroll', handleScrollFallback);
      }
    };

    window.addEventListener('scroll', handleScrollFallback, { passive: true });

    return () => {
      observer.disconnect();
      window.removeEventListener('scroll', handleScrollFallback);
    };
  }, [reducedMotion, threshold]);

  const hiddenTransform = {
    up: 'translate3d(0, 14px, 0)',
    left: 'translate3d(-14px, 0, 0)',
    right: 'translate3d(14px, 0, 0)',
    scale: 'translate3d(0, 10px, 0) scale(0.985)',
  }[direction];

  const style: React.CSSProperties = reducedMotion
    ? {
        opacity: 1,
        transform: 'none',
      }
    : {
        opacity: 1,
        transform: isVisible ? 'translate3d(0, 0, 0) scale(1)' : hiddenTransform,
        transitionProperty: 'transform',
        transitionDuration: `${MOTION_TOKENS.duration.sectionMs}ms`,
        transitionTimingFunction: MOTION_TOKENS.easing.springOut,
        transitionDelay: `${delayMs}ms`,
        willChange: isVisible ? 'auto' : 'transform',
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

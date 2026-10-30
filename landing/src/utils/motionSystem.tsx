import React, { useEffect, useRef, useState } from 'react';
import { Volume2, VolumeX } from 'lucide-react';
import { kuotaSound } from './soundSystem';

/**
 * Escala oficial de tiempos y curvas del Sistema de Movimiento Kuota (§8 y §12).
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
let cancelUserScrollListeners: (() => void) | null = null;

function stopActiveSmoothScroll(): void {
  if (activeScrollRafId !== null && typeof window !== 'undefined') {
    window.cancelAnimationFrame(activeScrollRafId);
    activeScrollRafId = null;
  }
  if (cancelUserScrollListeners) {
    cancelUserScrollListeners();
    cancelUserScrollListeners = null;
  }
}

/**
 * Desplazamiento suave por hardware (requestAnimationFrame) hacia una sección
 * por su selector (`#producto`, `#beneficios`, `#niveles`, `#confianza`, etc.),
 * compensando únicamente la altura de la barra superior fija (`#kuota-top-bar`)
 * y cancelándose de inmediato si el usuario toca la pantalla en móvil (`touchstart`).
 */
export function smoothScrollToAnchor(href: string): void {
  if (typeof window === 'undefined' || typeof document === 'undefined') return;

  stopActiveSmoothScroll();

  const isTop = href === '#inicio' || href === '#';
  const targetEl = isTop
    ? document.getElementById('inicio')
    : (document.querySelector(href) as HTMLElement | null);

  if (!targetEl && !isTop) return;

  const topBarEl =
    document.getElementById('kuota-top-bar') || document.querySelector('header');
  const rawBarHeight = topBarEl ? topBarEl.getBoundingClientRect().height : 68;
  // Garantizar que si el menú móvil estaba desplegado nunca se tome más de 84px de offset
  const headerOffset = Math.min(84, Math.max(56, rawBarHeight));

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

  if (!isTop && targetEl) {
    const innerContainer =
      (targetEl.querySelector(':scope > div') as HTMLElement | null) || targetEl;
    innerContainer.classList.remove('kuota-section-arrive');
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

  const durationMs = Math.min(720, Math.max(420, Math.abs(distance) * 0.36));
  const startTime = performance.now();

  const easeOutQuart = (t: number): number => 1 - Math.pow(1 - t, 4);

  // Si el usuario toca la pantalla o usa la rueda durante la animación, ceder el control de inmediato
  const handleUserInterrupt = () => {
    stopActiveSmoothScroll();
  };

  window.addEventListener('touchstart', handleUserInterrupt, { passive: true });
  window.addEventListener('wheel', handleUserInterrupt, { passive: true });

  cancelUserScrollListeners = () => {
    window.removeEventListener('touchstart', handleUserInterrupt);
    window.removeEventListener('wheel', handleUserInterrupt);
  };

  const step = (now: number) => {
    const elapsed = now - startTime;
    const progress = Math.min(1, elapsed / durationMs);
    const eased = easeOutQuart(progress);

    window.scrollTo(0, Math.round(startY + distance * eased));

    if (progress < 1) {
      activeScrollRafId = window.requestAnimationFrame(step);
    } else {
      stopActiveSmoothScroll();
    }
  };

  activeScrollRafId = window.requestAnimationFrame(step);
}

export interface ScrollRevealProps {
  children: React.ReactNode;
  delayMs?: number;
  direction?: 'up' | 'left' | 'right' | 'scale';
  className?: string;
  threshold?: number;
}

/**
 * Componente de aparición progresiva en scroll (`ScrollReveal` / `AnimatedSection`).
 * Siempre mantiene `opacity: 1` en SSR e hidratación inicial para evitar pantallas
 * en blanco al recargar (F5).
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

/**
 * Alias semántico `AnimatedSection` (§12).
 */
export const AnimatedSection = ScrollReveal;

export interface TiltCardProps {
  children: React.ReactNode;
  className?: string;
  maxTiltDeg?: number;
}

/**
 * Contenedor con movimiento 3D sutil en escritorio (`hover: hover` y `pointer: fine`).
 * En dispositivos táctiles/móviles desactiva el `perspective` y el `active:scale` del
 * contenedor padre para que los toques en botones internos respondan al 100% sin desplazamiento.
 */
export const TiltCard: React.FC<TiltCardProps> = ({
  children,
  className = '',
  maxTiltDeg = 2.8,
}) => {
  const reducedMotion = usePrefersReducedMotion();
  const cardRef = useRef<HTMLDivElement | null>(null);
  const [canHover, setCanHover] = useState<boolean>(false);
  const [transformStyle, setTransformStyle] = useState<string>('none');

  useEffect(() => {
    if (typeof window === 'undefined' || !window.matchMedia) return;
    const mq = window.matchMedia('(hover: hover) and (pointer: fine)');
    const updateCanHover = () => {
      const supported = mq.matches && window.innerWidth >= 1024;
      setCanHover(supported);
      if (!supported) {
        setTransformStyle('none');
      }
    };
    updateCanHover();
    window.addEventListener('resize', updateCanHover);
    return () => window.removeEventListener('resize', updateCanHover);
  }, []);

  const handleMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
    if (reducedMotion || !canHover) {
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
    if (reducedMotion || !canHover) return;
    setTransformStyle('none');
  };

  return (
    <div
      ref={cardRef}
      onMouseMove={canHover ? handleMouseMove : undefined}
      onMouseLeave={canHover ? handleMouseLeave : undefined}
      style={
        reducedMotion || !canHover
          ? undefined
          : {
              transform: transformStyle,
              transition: `transform ${MOTION_TOKENS.duration.normalMs}ms ${MOTION_TOKENS.easing.smoothOut}`,
            }
      }
      className={`transition-shadow ${className}`}
    >
      {children}
    </div>
  );
};

export interface FinancialCardMotionProps {
  children: React.ReactNode;
  className?: string;
  floating?: boolean;
  maxTiltDeg?: number;
}

/**
 * Componente `FinancialCardMotion` (§12): Tarjeta financiera con profundidad,
 * elevación interactiva en escritorio y respuesta táctil limpia en móvil.
 */
export const FinancialCardMotion: React.FC<FinancialCardMotionProps> = ({
  children,
  className = '',
  floating = false,
  maxTiltDeg = 2.6,
}) => {
  return (
    <TiltCard
      maxTiltDeg={maxTiltDeg}
      className={`${floating ? 'animate-floatCard' : ''} ${className}`}
    >
      {children}
    </TiltCard>
  );
};

export interface AnimatedHeadlineProps {
  badge?: React.ReactNode;
  title: React.ReactNode;
  subtitle?: React.ReactNode;
  as?: 'h1' | 'h2' | 'h3';
  id?: string;
  align?: 'left' | 'center';
  className?: string;
}

/**
 * Componente `AnimatedHeadline` (§12): Titular editorial de gran impacto con aparición
 * escalonada de insignia, titular principal y subtítulo.
 */
export const AnimatedHeadline: React.FC<AnimatedHeadlineProps> = ({
  badge,
  title,
  subtitle,
  as: HeadingTag = 'h2',
  id,
  align = 'left',
  className = '',
}) => {
  const alignClasses =
    align === 'center' ? 'text-center items-center mx-auto' : 'text-left items-start';

  return (
    <div className={`flex flex-col ${alignClasses} ${className}`}>
      {badge && (
        <ScrollReveal delayMs={0} direction="up">
          <div className="mb-3">{badge}</div>
        </ScrollReveal>
      )}
      <ScrollReveal delayMs={70} direction="up">
        <HeadingTag
          id={id}
          className="text-3xl sm:text-4xl lg:text-[2.75rem] font-extrabold text-kuota-deepnavy tracking-tight leading-[1.08]"
        >
          {title}
        </HeadingTag>
      </ScrollReveal>
      {subtitle && (
        <ScrollReveal delayMs={140} direction="up">
          <p className="text-base sm:text-lg font-semibold text-kuota-crayola leading-relaxed mt-3 max-w-2xl">
            {subtitle}
          </p>
        </ScrollReveal>
      )}
    </div>
  );
};

export interface CircularImageRevealProps {
  src: string;
  alt: string;
  sizeClassName?: string;
  objectPosition?: string;
  topBadge?: React.ReactNode;
  bottomBadge?: React.ReactNode;
  floatingRightBadge?: React.ReactNode;
  className?: string;
}

/**
 * Componente `CircularImageReveal` (§2, §5, §7, §12):
 * Fotografía circular de gran impacto con anillo orbital punteado, borde Deep Navy
 * y etiquetas financieras flotantes alrededor del emprendedor o comercio.
 */
export const CircularImageReveal: React.FC<CircularImageRevealProps> = ({
  src,
  alt,
  sizeClassName = 'w-60 h-60 sm:w-80 sm:h-80',
  objectPosition = 'center center',
  topBadge,
  bottomBadge,
  floatingRightBadge,
  className = '',
}) => {
  return (
    <div
      className={`relative inline-flex items-center justify-center select-none max-w-full ${className}`}
    >
      {/* Halo ambiental y anillo orbital punteado estilo fintech editorial */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -inset-2.5 sm:-inset-4 rounded-full border border-dashed border-[#0391FD]/25 bg-[#0391FD]/5"
      />
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -inset-1 rounded-full border border-[#0391FD]/30"
      />

      {/* Recorte fotográfico circular principal */}
      <div
        className={`relative rounded-full overflow-hidden ring-4 ring-white shadow-2xl transition-transform duration-500 lg:hover:scale-[1.02] ${sizeClassName}`}
      >
        <img
          src={src}
          alt={alt}
          style={{ objectPosition }}
          className="w-full h-full object-cover transition-transform duration-700 lg:hover:scale-105"
        />
        <div
          aria-hidden="true"
          className="pointer-events-none absolute inset-0 bg-gradient-to-t from-[#0A192F]/20 via-transparent to-transparent"
        />
      </div>

      {/* Insignias financieras flotantes (pointer-events-none para no interceptar toques en móvil) */}
      {topBadge && (
        <div className="pointer-events-none absolute -top-3 left-0 sm:-left-6 z-20 animate-floatCard">
          {topBadge}
        </div>
      )}
      {floatingRightBadge && (
        <div
          style={{ animationDelay: '900ms' }}
          className="pointer-events-none absolute -bottom-2 sm:bottom-auto sm:top-1/2 sm:-translate-y-1/2 right-0 sm:-right-8 z-20 animate-floatCard"
        >
          {floatingRightBadge}
        </div>
      )}
      {bottomBadge && (
        <div
          style={{ animationDelay: '450ms' }}
          className="pointer-events-none absolute -bottom-4 left-2 right-2 sm:left-auto sm:-right-4 z-20 flex justify-center animate-floatCard"
        >
          {bottomBadge}
        </div>
      )}
    </div>
  );
};

export interface AnimatedNumberProps {
  value: number;
  prefix?: string;
  suffix?: string;
  decimals?: number;
  durationMs?: number;
  className?: string;
}

/**
 * Transición numérica fluida por hardware (`AnimatedNumber`, §4 y §8).
 */
export const AnimatedNumber: React.FC<AnimatedNumberProps> = ({
  value,
  prefix = '',
  suffix = '',
  decimals = 0,
  durationMs = 420,
  className = '',
}) => {
  const [displayValue, setDisplayValue] = useState<number>(value);
  const previousValueRef = useRef<number>(value);

  useEffect(() => {
    const startValue = previousValueRef.current;
    const endValue = value;
    previousValueRef.current = value;

    if (Math.abs(endValue - startValue) < 0.01 || typeof window === 'undefined') {
      setDisplayValue(endValue);
      return;
    }

    let rafId: number | null = null;
    const startTime = performance.now();
    const easeOutQuart = (t: number) => 1 - Math.pow(1 - t, 4);

    const step = (now: number) => {
      const elapsed = now - startTime;
      const progress = Math.min(1, elapsed / durationMs);
      const eased = easeOutQuart(progress);
      const current = startValue + (endValue - startValue) * eased;
      setDisplayValue(current);

      if (progress < 1) {
        rafId = window.requestAnimationFrame(step);
      }
    };

    rafId = window.requestAnimationFrame(step);
    return () => {
      if (rafId !== null) window.cancelAnimationFrame(rafId);
    };
  }, [value, durationMs]);

  const formatted = displayValue.toLocaleString('es-PE', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });

  return (
    <span className={`tabular-nums ${className}`}>
      {prefix}
      {formatted}
      {suffix}
    </span>
  );
};

export interface TimelineInstallmentItem {
  id: string;
  label: string;
  dateLabel: string;
  amountSoles: number;
  status: 'Pagada' | 'Confirmada' | 'Programada' | 'Próxima' | 'En cálculo';
  highlight?: boolean;
}

export interface InstallmentTimelineProps {
  items: TimelineInstallmentItem[];
  onSelectItem?: (item: TimelineInstallmentItem, index: number) => void;
  className?: string;
}

/**
 * Componente `InstallmentTimeline` (§4 y §12): Línea de tiempo y calendario interactivo
 * de kuotas con montos animados y botones táctiles nativos.
 */
export const InstallmentTimeline: React.FC<InstallmentTimelineProps> = ({
  items,
  onSelectItem,
  className = '',
}) => {
  return (
    <div className={`grid grid-cols-1 sm:grid-cols-2 gap-3 ${className}`}>
      {items.map((inst, idx) => {
        const isPaid = inst.status === 'Pagada' || inst.status === 'Confirmada';
        const isUpcoming = inst.status === 'Próxima' || inst.status === 'Programada';

        const cardClasses = `w-full text-left rounded-2xl border p-3.5 transition-all duration-200 touch-manipulation cursor-pointer ${
          onSelectItem
            ? 'lg:hover:-translate-y-0.5 active:scale-[0.98]'
            : ''
        } ${
          isPaid
            ? 'bg-emerald-50/80 border-emerald-200 text-emerald-900 shadow-sm'
            : inst.highlight || isUpcoming
              ? 'bg-white border-slate-200 shadow-md ring-2 ring-[#0391FD]/10'
              : 'bg-slate-50/60 border-slate-200/70 opacity-85'
        }`;

        const content = (
          <>
            <div className="flex items-center justify-between gap-2 mb-1.5">
              <span className="text-xs font-bold text-[#0A192F] flex items-center gap-1.5">
                <span
                  className={`w-2 h-2 rounded-full ${
                    isPaid
                      ? 'bg-[#10B981]'
                      : isUpcoming
                        ? 'bg-[#0391FD]'
                        : 'bg-slate-300'
                  }`}
                />
                {inst.label}
              </span>
              <span
                className={`text-[10px] font-bold px-2.5 py-0.5 rounded-full border ${
                  isPaid
                    ? 'bg-emerald-50 text-emerald-800 border-emerald-200'
                    : isUpcoming
                      ? 'bg-sky-50 text-[#0391FD] border-sky-200'
                      : 'bg-slate-50 text-slate-600 border-slate-200'
                }`}
              >
                {isPaid ? `${inst.status} ✓` : inst.status}
              </span>
            </div>

            <div className="flex items-baseline justify-between mt-2">
              <span className="text-xs font-medium text-slate-500">
                {inst.dateLabel}
              </span>
              <AnimatedNumber
                value={inst.amountSoles}
                prefix="S/ "
                decimals={0}
                className="text-base sm:text-lg font-black text-[#0A192F]"
              />
            </div>
          </>
        );

        if (onSelectItem) {
          return (
            <button
              key={inst.id}
              type="button"
              onClick={() => onSelectItem(inst, idx)}
              className={cardClasses}
            >
              {content}
            </button>
          );
        }

        return (
          <div key={inst.id} className={cardClasses}>
            {content}
          </div>
        );
      })}
    </div>
  );
};

export interface PaymentProgressProps {
  label: string;
  currentStep: number;
  totalSteps: number;
  statusText?: string;
  className?: string;
}

/**
 * Componente `PaymentProgress` (§4 y §12): Barra de progreso financiera segmentada
 * con porcentaje animado.
 */
export const PaymentProgress: React.FC<PaymentProgressProps> = ({
  label,
  currentStep,
  totalSteps,
  statusText,
  className = '',
}) => {
  const safeTotal = Math.max(1, totalSteps);
  const clampedStep = Math.max(0, Math.min(currentStep, safeTotal));
  const percent = Math.round((clampedStep / safeTotal) * 100);

  return (
    <div className={`space-y-2 ${className}`}>
      <div className="flex flex-wrap items-center justify-between gap-2 text-xs font-extrabold">
        <span className="text-kuota-deepnavy uppercase tracking-wider">{label}</span>
        <div className="flex items-center gap-2">
          {statusText && (
            <span className="text-kuota-crayola font-bold">{statusText}</span>
          )}
          <span className="px-2 py-0.5 rounded-md bg-kuota-deepnavy text-white tabular-nums">
            <AnimatedNumber value={percent} suffix="%" />
          </span>
        </div>
      </div>

      <div className="w-full h-2 rounded-full bg-slate-100 overflow-hidden">
        <div
          className="h-full rounded-full bg-[#0391FD] transition-all duration-500"
          style={{ width: `${percent}%` }}
        />
      </div>
    </div>
  );
};

export interface AnimatedCTAProps {
  children: React.ReactNode;
  onClick?: () => void;
  href?: string;
  variant?: 'primary' | 'secondary' | 'navy';
  className?: string;
  ariaLabel?: string;
}

/**
 * Componente `AnimatedCTA` (§3, §8 y §12): Botón de acción con microinteracciones
 * de elevación, presión táctil y feedback sonoro opcional.
 */
export const AnimatedCTA: React.FC<AnimatedCTAProps> = ({
  children,
  onClick,
  href,
  variant = 'primary',
  className = '',
  ariaLabel,
}) => {
  const variantClasses = {
    primary:
      'bg-[#0391FD] hover:bg-[#0277d4] text-white shadow-md hover:shadow-lg',
    secondary:
      'bg-white hover:bg-slate-50 text-[#0A192F] border border-slate-200/90 shadow-sm hover:shadow',
    navy:
      'bg-[#0A192F] hover:bg-[#0391FD] text-white shadow-md hover:shadow-lg',
  }[variant];

  const baseClasses = `inline-flex items-center justify-center gap-2 px-5 py-3 rounded-full font-bold text-sm sm:text-base touch-manipulation transition-all duration-200 lg:hover:-translate-y-0.5 active:translate-y-0 active:scale-[0.98] cursor-pointer ${variantClasses} ${className}`;

  const handleInteraction = (e: React.MouseEvent<HTMLElement>) => {
    kuotaSound.playCtaClick();
    if (href && href.startsWith('#')) {
      e.preventDefault();
      smoothScrollToAnchor(href);
    }
    if (onClick) onClick();
  };

  if (href) {
    return (
      <a
        href={href}
        aria-label={ariaLabel}
        onClick={handleInteraction}
        className={baseClasses}
      >
        {children}
      </a>
    );
  }

  return (
    <button
      type="button"
      aria-label={ariaLabel}
      onClick={handleInteraction}
      className={baseClasses}
    >
      {children}
    </button>
  );
};

/**
 * Componente `SoundController` (§10 y §12): Control compacto y accesible para activar
 * o silenciar los sonidos discretos de la interfaz (desactivados por defecto).
 */
export const SoundController: React.FC<{ className?: string }> = ({
  className = '',
}) => {
  const [enabled, setEnabled] = useState<boolean>(
    () => kuotaSound.getState().enabled
  );

  useEffect(() => {
    return kuotaSound.subscribe((state) => {
      setEnabled(state.enabled);
    });
  }, []);

  const handleToggle = () => {
    kuotaSound.setEnabled(!enabled);
  };

  return (
    <button
      type="button"
      onClick={handleToggle}
      aria-pressed={enabled}
      aria-label={
        enabled
          ? 'Sonidos interactivos activados. Clic para silenciar'
          : 'Sonidos interactivos desactivados. Clic para activar'
      }
      className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full border border-slate-200/80 text-xs font-bold touch-manipulation transition-all duration-150 cursor-pointer ${
        enabled
          ? 'bg-[#0A192F] text-white shadow-sm'
          : 'bg-white hover:bg-slate-50 text-slate-700'
      } ${className}`}
    >
      {enabled ? (
        <Volume2 className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
      ) : (
        <VolumeX className="w-3.5 h-3.5 text-kuota-deepnavy/70" aria-hidden="true" />
      )}
      <span>{enabled ? 'Audio ON' : 'Audio OFF'}</span>
    </button>
  );
};

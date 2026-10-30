import React, { useEffect, useRef, useState } from 'react';
import {
  Menu,
  X,
  ShoppingBag,
  Store,
  Volume2,
  VolumeX,
  ArrowRight,
  Mail,
  Sparkles,
  ShieldCheck,
  TrendingUp,
  Users,
  Calculator,
  Tag,
} from 'lucide-react';
import { KuotaLogo } from './KuotaLogo';
import { PlayStoreButton } from './PlayStoreButton';
import { ScheduleCallButton } from './supplier/ScheduleCallButton';
import { trackEvent } from '../utils/analytics';
import { kuotaSound } from '../utils/soundSystem';
import { smoothScrollToAnchor } from '../utils/motionSystem';

export type AudienceMode = 'compradores' | 'proveedores';

interface UnifiedNavbarProps {
  mode: AudienceMode;
  isSwitching?: boolean;
  onModeChange: (mode: AudienceMode, source: string) => void;
}

const BUYER_LINKS = [
  {
    label: 'Producto',
    subtitle: 'Simulador interactivo y compra en 3 clics',
    href: '#producto',
    icon: Sparkles,
  },
  {
    label: 'Beneficios',
    subtitle: 'Sin papeleo, 5% de interés y formalización 0%',
    href: '#beneficios',
    icon: ShieldCheck,
  },
  {
    label: 'Niveles',
    subtitle: 'Escalera de crecimiento del Nivel 1 al 6',
    href: '#niveles',
    icon: TrendingUp,
  },
  {
    label: 'Nosotros',
    subtitle: 'Equipo fundador e instituciones que nos respaldan',
    href: '#confianza',
    icon: Users,
  },
];

const SUPPLIER_LINKS = [
  {
    label: 'Producto',
    subtitle: 'Kuota QR, Kuota Link y financiamiento aKuotas!',
    href: '#producto',
    icon: Sparkles,
  },
  {
    label: 'Calculadora',
    subtitle: 'Calcula tu ahorro mensual frente al POS tradicional',
    href: '#calculadora',
    icon: Calculator,
  },
  {
    label: 'Precios',
    subtitle: 'Planes Basic, Pro y Open con 0 riesgo',
    href: '#precios',
    icon: Tag,
  },
  {
    label: 'Nosotros',
    subtitle: 'Equipo fundador y agenda en vivo por Calendly',
    href: '#nosotros',
    icon: Users,
  },
];

export const UnifiedNavbar: React.FC<UnifiedNavbarProps> = ({
  mode,
  isSwitching = false,
  onModeChange,
}) => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [soundPanelOpen, setSoundPanelOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);
  const [soundState, setSoundState] = useState(() => kuotaSound.getState());

  const buyerTabRef = useRef<HTMLButtonElement | null>(null);
  const supplierTabRef = useRef<HTMLButtonElement | null>(null);
  const [pillBounds, setPillBounds] = useState<{
    left: number;
    width: number;
    ready: boolean;
  }>({ left: 4, width: 110, ready: false });

  const navLinks = mode === 'compradores' ? BUYER_LINKS : SUPPLIER_LINKS;

  const navItemRefs = useRef<Record<string, HTMLAnchorElement | null>>({});
  const [activeNavHref, setActiveNavHref] = useState<string>(navLinks[0].href);
  const [hoveredNavHref, setHoveredNavHref] = useState<string | null>(null);
  const [navPillBounds, setNavPillBounds] = useState<{
    left: number;
    width: number;
    ready: boolean;
  }>({ left: 0, width: 0, ready: false });

  useEffect(() => {
    setActiveNavHref(
      (mode === 'compradores' ? BUYER_LINKS : SUPPLIER_LINKS)[0].href
    );
    setHoveredNavHref(null);
  }, [mode]);

  useEffect(() => {
    return kuotaSound.subscribe(setSoundState);
  }, []);

  useEffect(() => {
    const links = mode === 'compradores' ? BUYER_LINKS : SUPPLIER_LINKS;
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 12);

      const spyOffset = 180;
      let currentActive = links[0].href;
      for (const item of links) {
        const el = document.querySelector(item.href);
        if (el) {
          const rect = el.getBoundingClientRect();
          if (rect.top <= spyOffset) {
            currentActive = item.href;
          }
        }
      }
      setActiveNavHref(currentActive);
    };
    handleScroll();
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, [mode]);

  useEffect(() => {
    const updatePillBounds = () => {
      const activeBtn =
        mode === 'compradores' ? buyerTabRef.current : supplierTabRef.current;
      if (activeBtn) {
        setPillBounds({
          left: activeBtn.offsetLeft,
          width: activeBtn.offsetWidth,
          ready: true,
        });
      }

      const targetHref = hoveredNavHref || activeNavHref;
      const activeNavEl = navItemRefs.current[targetHref];
      if (activeNavEl) {
        setNavPillBounds({
          left: activeNavEl.offsetLeft,
          width: activeNavEl.offsetWidth,
          ready: true,
        });
      }
    };

    updatePillBounds();
    window.addEventListener('resize', updatePillBounds);

    let ro: ResizeObserver | null = null;
    if (typeof ResizeObserver !== 'undefined') {
      ro = new ResizeObserver(updatePillBounds);
      if (buyerTabRef.current) ro.observe(buyerTabRef.current);
      if (supplierTabRef.current) ro.observe(supplierTabRef.current);
    }

    if (typeof document !== 'undefined' && 'fonts' in document) {
      document.fonts.ready.then(updatePillBounds).catch(() => {});
    }

    return () => {
      window.removeEventListener('resize', updatePillBounds);
      if (ro) ro.disconnect();
    };
  }, [mode, activeNavHref, hoveredNavHref]);

  const handleLinkClick = (
    e: React.MouseEvent<HTMLAnchorElement>,
    label: string,
    href: string
  ) => {
    e.preventDefault();
    kuotaSound.playSelectOption();
    setActiveNavHref(href);
    trackEvent('click_nav_link', {
      section: label.toLowerCase(),
      audience: mode,
    });
    setMobileMenuOpen(false);
    setSoundPanelOpen(false);
    smoothScrollToAnchor(href);
  };

  const handleLogoClick = (e: React.MouseEvent<HTMLAnchorElement>) => {
    e.preventDefault();
    kuotaSound.playSelectOption();
    setMobileMenuOpen(false);
    setSoundPanelOpen(false);
    smoothScrollToAnchor('#inicio');
  };

  const handleSwitchMode = (nextMode: AudienceMode, source: string) => {
    kuotaSound.playSelectOption();
    setMobileMenuOpen(false);
    setSoundPanelOpen(false);
    onModeChange(nextMode, source);
  };

  const highlightedNavHref = hoveredNavHref || activeNavHref;

  return (
    <header
      className={`sticky top-0 z-50 w-full border-b-2 border-kuota-deepnavy transition-all duration-300 ${
        isScrolled
          ? 'bg-kuota-palesky/95 backdrop-blur-md shadow-navy-hard'
          : 'bg-kuota-palesky/90 backdrop-blur-sm'
      }`}
    >
      {/* Barra superior fija optimizada para móvil y escritorio */}
      <div
        id="kuota-top-bar"
        className="max-w-7xl mx-auto px-2.5 sm:px-6 lg:px-8 h-16 sm:h-20 flex items-center justify-between gap-1.5 sm:gap-4 relative z-50"
      >
        {/* Izquierda: Logo oficial Kuota + Selector de Perfil con píldora deslizante medida al píxel */}
        <div className="flex items-center gap-2 sm:gap-5 min-w-0">
          <a
            href="#inicio"
            onClick={handleLogoClick}
            aria-label="Kuota — Ir al inicio"
            className="focus-visible:outline-none shrink-0 flex items-center touch-manipulation transition-transform duration-200 lg:hover:scale-[1.03] active:scale-[0.97]"
          >
            <KuotaLogo
              size="sm"
              className="sm:hidden"
            />
            <KuotaLogo
              size="md"
              className="hidden sm:inline-flex"
            />
          </a>

          {/* Pill Switcher Compradores / Proveedores adaptado a pantallas táctiles */}
          <div
            role="tablist"
            aria-label="Seleccionar perfil de usuario"
            className="relative inline-flex items-center gap-0.5 sm:gap-1 p-1 rounded-full bg-white border-2 border-kuota-deepnavy shadow-sm shrink-0"
          >
            {pillBounds.ready && (
              <span
                aria-hidden="true"
                style={{
                  left: `${pillBounds.left}px`,
                  width: `${pillBounds.width}px`,
                  backgroundColor:
                    mode === 'compradores' ? '#0391FD' : '#00235C',
                  transition:
                    'left 380ms cubic-bezier(0.22, 1, 0.36, 1), width 380ms cubic-bezier(0.22, 1, 0.36, 1), background-color 380ms cubic-bezier(0.22, 1, 0.36, 1)',
                }}
                className="pointer-events-none absolute top-1 bottom-1 rounded-full shadow-sm z-0"
              />
            )}

            <button
              ref={buyerTabRef}
              type="button"
              role="tab"
              aria-selected={mode === 'compradores'}
              onClick={() => handleSwitchMode('compradores', 'navbar_pill')}
              className={`relative z-10 inline-flex items-center justify-center gap-1 sm:gap-1.5 px-2.5 sm:px-3.5 py-1.5 rounded-full text-[11px] sm:text-xs font-extrabold whitespace-nowrap shrink-0 touch-manipulation transition-colors duration-250 ease-out active:scale-95 ${
                mode === 'compradores'
                  ? pillBounds.ready
                    ? 'text-white'
                    : 'bg-kuota-dodger text-white shadow-sm'
                  : 'bg-transparent text-kuota-deepnavy hover:text-kuota-dodger'
              }`}
            >
              <ShoppingBag
                className={`hidden min-[410px]:inline-block w-3.5 h-3.5 shrink-0 transition-transform duration-300 ${
                  mode === 'compradores' ? 'scale-110' : 'scale-100'
                }`}
                aria-hidden="true"
              />
              <span>Compradores</span>
            </button>

            <button
              ref={supplierTabRef}
              type="button"
              role="tab"
              aria-selected={mode === 'proveedores'}
              onClick={() => handleSwitchMode('proveedores', 'navbar_pill')}
              className={`relative z-10 inline-flex items-center justify-center gap-1 sm:gap-1.5 px-2.5 sm:px-3.5 py-1.5 rounded-full text-[11px] sm:text-xs font-extrabold whitespace-nowrap shrink-0 touch-manipulation transition-colors duration-250 ease-out active:scale-95 ${
                mode === 'proveedores'
                  ? pillBounds.ready
                    ? 'text-white'
                    : 'bg-kuota-deepnavy text-white shadow-sm'
                  : 'bg-transparent text-kuota-deepnavy hover:text-kuota-dodger'
              }`}
            >
              <Store
                className={`hidden min-[410px]:inline-block w-3.5 h-3.5 shrink-0 transition-transform duration-300 ${
                  mode === 'proveedores' ? 'scale-110' : 'scale-100'
                }`}
                aria-hidden="true"
              />
              <span>Proveedores</span>
            </button>
          </div>
        </div>

        {/* Centro: Navegación Desktop con cápsula deslizante y barra activa animada */}
        <nav
          aria-label="Navegación principal"
          onMouseLeave={() => setHoveredNavHref(null)}
          style={{
            opacity: isSwitching ? 0.25 : 1,
            transform: isSwitching
              ? 'translate3d(0, -4px, 0)'
              : 'translate3d(0, 0, 0)',
            transition:
              'opacity 220ms cubic-bezier(0.22, 1, 0.36, 1), transform 220ms cubic-bezier(0.22, 1, 0.36, 1)',
          }}
          className="relative hidden lg:flex items-center gap-1 xl:gap-1.5 p-1 rounded-2xl shrink-0"
        >
          {navPillBounds.ready && (
            <span
              aria-hidden="true"
              style={{
                left: `${navPillBounds.left}px`,
                width: `${navPillBounds.width}px`,
                transition:
                  'left 340ms cubic-bezier(0.22, 1, 0.36, 1), width 340ms cubic-bezier(0.22, 1, 0.36, 1)',
              }}
              className="pointer-events-none absolute top-1 bottom-1 rounded-xl bg-white/85 border border-kuota-deepnavy/20 shadow-sm z-0 flex items-end justify-center overflow-hidden"
            >
              <span className="w-7 h-[3px] rounded-full bg-kuota-dodger mb-0.5" />
            </span>
          )}

          {navLinks.map((link) => {
            const IconComponent = link.icon;
            const isHighlighted = highlightedNavHref === link.href;
            return (
              <a
                key={link.href}
                ref={(el) => {
                  navItemRefs.current[link.href] = el;
                }}
                href={link.href}
                onMouseEnter={() => setHoveredNavHref(link.href)}
                onClick={(e) => handleLinkClick(e, link.label, link.href)}
                className={`relative z-10 inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-sm font-extrabold whitespace-nowrap transition-all duration-250 active:scale-95 ${
                  isHighlighted
                    ? 'text-kuota-dodger -translate-y-[1px]'
                    : 'text-kuota-deepnavy hover:text-kuota-dodger'
                }`}
              >
                <IconComponent
                  className={`w-3.5 h-3.5 shrink-0 transition-all duration-300 ${
                    isHighlighted
                      ? 'text-kuota-dodger scale-110 -rotate-6'
                      : 'text-kuota-deepnavy/60 scale-95'
                  }`}
                  aria-hidden="true"
                />
                <span>{link.label}</span>
              </a>
            );
          })}
        </nav>

        {/* Derecha: Control de identidad sonora + CTA contextual + botón menú animado */}
        <div className="flex items-center gap-1.5 sm:gap-2.5 shrink-0">
          {/* Control opcional de Identidad Sonora Kuota (desactivado por defecto) */}
          <div className="relative">
            <button
              type="button"
              onClick={() => {
                setMobileMenuOpen(false);
                if (!soundState.enabled) {
                  kuotaSound.setEnabled(true);
                  setSoundPanelOpen(true);
                } else {
                  setSoundPanelOpen((prev) => !prev);
                }
              }}
              aria-label={
                soundState.enabled
                  ? 'Configurar sonidos de interacción (activos)'
                  : 'Activar sonidos de interacción (desactivados por defecto)'
              }
              title={
                soundState.enabled
                  ? 'Sonido interactivo activo'
                  : 'Activar sonido interactivo'
              }
              className={`w-9 h-9 rounded-xl border-2 border-kuota-deepnavy flex items-center justify-center touch-manipulation transition-all duration-200 lg:hover:-translate-y-0.5 active:scale-95 ${
                soundState.enabled
                  ? 'bg-kuota-deepnavy text-white shadow-sm'
                  : 'bg-white text-kuota-deepnavy hover:bg-kuota-palesky/70'
              }`}
            >
              {soundState.enabled ? (
                <Volume2
                  className="w-4 h-4 text-kuota-dodger animate-pulse"
                  aria-hidden="true"
                />
              ) : (
                <VolumeX className="w-4 h-4 opacity-70" aria-hidden="true" />
              )}
            </button>

            {soundPanelOpen && (
              <div
                role="dialog"
                aria-label="Control de sonido Kuota"
                className="absolute right-0 mt-2 w-60 rounded-2xl bg-white border-2 border-kuota-deepnavy p-3.5 shadow-navy-hard z-50 space-y-3 animate-fadeIn"
              >
                <div className="flex items-center justify-between gap-2">
                  <span className="text-xs font-extrabold text-kuota-deepnavy">
                    Sonido interactivo
                  </span>
                  <button
                    type="button"
                    onClick={() => kuotaSound.setEnabled(!soundState.enabled)}
                    className={`px-2.5 py-1 rounded-lg text-[11px] font-extrabold border border-kuota-deepnavy touch-manipulation transition-colors ${
                      soundState.enabled
                        ? 'bg-kuota-dodger text-white'
                        : 'bg-kuota-palesky text-kuota-deepnavy'
                    }`}
                  >
                    {soundState.enabled ? 'Activado' : 'Desactivado'}
                  </button>
                </div>

                <div>
                  <div className="flex items-center justify-between text-[11px] font-bold text-kuota-crayola mb-1">
                    <label htmlFor="kuota-sound-volume">Volumen</label>
                    <span>{Math.round(soundState.volume * 100)}%</span>
                  </div>
                  <input
                    id="kuota-sound-volume"
                    type="range"
                    min={0}
                    max={100}
                    value={Math.round(soundState.volume * 100)}
                    onChange={(e) => {
                      const val = Number(e.target.value) / 100;
                      kuotaSound.setVolume(val);
                    }}
                    className="w-full accent-kuota-dodger cursor-pointer"
                  />
                </div>

                <button
                  type="button"
                  onClick={() => setSoundPanelOpen(false)}
                  className="w-full py-2 rounded-lg bg-kuota-palesky/70 hover:bg-kuota-palesky text-xs font-extrabold text-kuota-deepnavy touch-manipulation transition-colors"
                >
                  Listo
                </button>
              </div>
            )}
          </div>

          <div
            style={{
              opacity: isSwitching ? 0.3 : 1,
              transform: isSwitching
                ? 'translate3d(0, -3px, 0) scale(0.97)'
                : 'translate3d(0, 0, 0) scale(1)',
              transition:
                'opacity 220ms cubic-bezier(0.22, 1, 0.36, 1), transform 220ms cubic-bezier(0.22, 1, 0.36, 1)',
            }}
            className="hidden lg:block shrink-0"
          >
            {mode === 'compradores' ? (
              <PlayStoreButton
                section="navbar"
                label="Descargar app"
                size="sm"
              />
            ) : (
              <ScheduleCallButton
                section="navbar"
                label="Agenda una call"
                size="sm"
              />
            )}
          </div>

          {/* Botón hamburguesa con área táctil amplia (< lg) */}
          <button
            type="button"
            onClick={() => {
              kuotaSound.playSelectOption();
              setSoundPanelOpen(false);
              setMobileMenuOpen((prev) => !prev);
            }}
            aria-expanded={mobileMenuOpen}
            aria-controls="mobile-nav-menu"
            aria-label={
              mobileMenuOpen
                ? 'Cerrar menú de navegación'
                : 'Abrir menú de navegación'
            }
            className={`lg:hidden inline-flex items-center justify-center w-10 h-10 rounded-xl border-2 border-kuota-deepnavy touch-manipulation transition-all duration-200 shrink-0 active:scale-95 ${
              mobileMenuOpen
                ? 'bg-kuota-deepnavy text-white shadow-sm'
                : 'bg-white text-kuota-deepnavy hover:bg-kuota-palesky'
            }`}
          >
            <span className="relative w-5 h-5 flex items-center justify-center">
              <Menu
                className={`w-5 h-5 absolute transition-all duration-250 ease-out ${
                  mobileMenuOpen
                    ? 'opacity-0 rotate-90 scale-50'
                    : 'opacity-100 rotate-0 scale-100'
                }`}
                aria-hidden="true"
              />
              <X
                className={`w-5 h-5 absolute transition-all duration-250 ease-out ${
                  mobileMenuOpen
                    ? 'opacity-100 rotate-0 scale-100'
                    : 'opacity-0 -rotate-90 scale-50'
                }`}
                aria-hidden="true"
              />
            </span>
          </button>
        </div>
      </div>

      {/* Fondo táctil para cerrar el menú móvil al tocar fuera */}
      {mobileMenuOpen && (
        <div
          aria-hidden="true"
          onClick={() => setMobileMenuOpen(false)}
          className="fixed inset-0 top-16 bg-kuota-deepnavy/25 backdrop-blur-[1px] z-40 lg:hidden"
        />
      )}

      {/* Menú desplegable flotante (`absolute top-full`) para no empujar el layout al hacer scroll en móvil */}
      <div
        id="mobile-nav-menu"
        aria-hidden={!mobileMenuOpen}
        className={`lg:hidden absolute top-full left-0 right-0 z-50 grid transition-all duration-300 ease-[cubic-bezier(0.22,1,0.36,1)] ${
          mobileMenuOpen
            ? 'grid-rows-[1fr] opacity-100 border-t-2 border-b-2 border-kuota-deepnavy shadow-navy-hard-lg'
            : 'grid-rows-[0fr] opacity-0 pointer-events-none border-t-0'
        }`}
      >
        <div className="overflow-hidden bg-white/98 backdrop-blur-md">
          <nav
            aria-label="Navegación desplegable"
            className="max-w-7xl mx-auto px-4 sm:px-6 pt-3 pb-5 space-y-3"
          >
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {navLinks.map((link, index) => {
                const IconComponent = link.icon;
                return (
                  <a
                    key={link.href}
                    href={link.href}
                    onClick={(e) => handleLinkClick(e, link.label, link.href)}
                    style={{
                      transitionDelay: mobileMenuOpen ? `${index * 40}ms` : '0ms',
                    }}
                    className={`group flex items-center justify-between gap-3 p-3.5 rounded-2xl border-2 border-kuota-deepnavy/15 active:border-kuota-deepnavy hover:border-kuota-deepnavy bg-kuota-palesky/30 active:bg-kuota-palesky/80 hover:bg-kuota-palesky/70 touch-manipulation transition-all duration-200 ease-out active:scale-[0.98] ${
                      mobileMenuOpen
                        ? 'translate-y-0 opacity-100'
                        : '-translate-y-2 opacity-0'
                    }`}
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <span className="w-9 h-9 rounded-xl bg-white border-2 border-kuota-deepnavy flex items-center justify-center shrink-0 transition-transform duration-200 group-hover:bg-kuota-dodger group-hover:text-white text-kuota-deepnavy">
                        <IconComponent className="w-4 h-4" aria-hidden="true" />
                      </span>
                      <div className="min-w-0">
                        <div className="text-sm sm:text-base font-extrabold text-kuota-deepnavy group-hover:text-kuota-dodger transition-colors">
                          {link.label}
                        </div>
                        <div className="text-[11px] font-semibold text-kuota-crayola truncate">
                          {link.subtitle}
                        </div>
                      </div>
                    </div>

                    <ArrowRight
                      className="w-4 h-4 text-kuota-deepnavy/60 group-hover:text-kuota-dodger shrink-0"
                      aria-hidden="true"
                    />
                  </a>
                );
              })}
            </div>

            {/* Pie del menú desplegable: Correo oficial + CTA principal */}
            <div
              style={{
                transitionDelay: mobileMenuOpen ? '160ms' : '0ms',
              }}
              className={`pt-3 border-t border-kuota-deepnavy/15 flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 transition-all duration-300 ${
                mobileMenuOpen
                  ? 'translate-y-0 opacity-100'
                  : 'translate-y-2 opacity-0'
              }`}
            >
              <a
                href="mailto:kuota.contact@gmail.com"
                onClick={() => kuotaSound.playSelectOption()}
                className="inline-flex items-center justify-center sm:justify-start gap-2 px-3.5 py-2.5 rounded-xl bg-kuota-palesky/60 hover:bg-kuota-palesky border border-kuota-deepnavy/30 text-xs font-extrabold text-kuota-deepnavy touch-manipulation transition-all duration-200"
              >
                <Mail className="w-4 h-4 text-kuota-dodger shrink-0" aria-hidden="true" />
                <span>kuota.contact@gmail.com</span>
              </a>

              <div className="w-full sm:w-auto">
                {mode === 'compradores' ? (
                  <PlayStoreButton
                    section="navbar"
                    size="md"
                    className="w-full sm:w-auto"
                  />
                ) : (
                  <ScheduleCallButton
                    section="navbar_mobile"
                    size="md"
                    className="w-full sm:w-auto"
                    onClickExtra={() => setMobileMenuOpen(false)}
                  />
                )}
              </div>
            </div>
          </nav>
        </div>
      </div>
    </header>
  );
};

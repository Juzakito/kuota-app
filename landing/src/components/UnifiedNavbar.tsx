import React, { useEffect, useRef, useState } from 'react';
import {
  Menu,
  X,
  ShoppingBag,
  Store,
  Volume2,
  VolumeX,
  ArrowRight,
  Sparkles,
  ShieldCheck,
  TrendingUp,
  Users,
  Calculator,
  Tag,
  Layers,
  HelpCircle,
} from 'lucide-react';
import { KuotaLogo } from './KuotaLogo';
import { PlayStoreButton } from './PlayStoreButton';
import { trackEvent } from '../utils/analytics';
import { kuotaSound } from '../utils/soundSystem';
import { smoothScrollToAnchor } from '../utils/motionSystem';

export type AudienceMode = 'ecosistema' | 'compradores' | 'comercios' | 'proveedores';

interface UnifiedNavbarProps {
  mode: AudienceMode;
  isSwitching?: boolean;
  onModeChange: (mode: AudienceMode, source: string) => void;
  onOpenMerchantAffiliation?: () => void;
}

const ECOSYSTEM_LINKS = [
  {
    label: 'Cómo funciona',
    subtitle: 'Diagrama interactivo del ecosistema',
    href: '#diagrama',
    icon: Sparkles,
  },
  {
    label: 'Perfiles',
    subtitle: 'Compradores, Comercios y Aliados',
    href: '#selector',
    icon: Users,
  },
  {
    label: 'Colocaciones',
    subtitle: 'Ciclo financiero y trazabilidad',
    href: '#colocaciones',
    icon: TrendingUp,
  },
  {
    label: 'Preguntas',
    subtitle: 'Dudas resueltas por audiencia',
    href: '#faq',
    icon: HelpCircle,
  },
];

const BUYER_LINKS = [
  {
    label: 'Producto',
    subtitle: 'Simulador interactivo y compra en cuotas',
    href: '#producto',
    icon: Sparkles,
  },
  {
    label: 'Beneficios',
    subtitle: 'Comisiones claras y acompañamiento',
    href: '#beneficios',
    icon: ShieldCheck,
  },
  {
    label: 'Niveles',
    subtitle: 'Escalera de crecimiento comercial',
    href: '#niveles',
    icon: TrendingUp,
  },
  {
    label: 'Nosotros',
    subtitle: 'Equipo fundador y respaldo de Kuota',
    href: '#confianza',
    icon: Users,
  },
];

const SUPPLIER_LINKS = [
  {
    label: 'Producto',
    subtitle: 'Kuota QR, Kuota Link y cobro aKuotas!',
    href: '#producto',
    icon: Sparkles,
  },
  {
    label: 'Operación',
    subtitle: 'Recepción, despacho y colocación',
    href: '#operacion-comercio',
    icon: Store,
  },
  {
    label: 'Calculadora',
    subtitle: 'Calcula tu ahorro frente al POS',
    href: '#calculadora',
    icon: Calculator,
  },
  {
    label: 'Precios',
    subtitle: 'Planes comerciales adaptados a tu negocio',
    href: '#precios',
    icon: Tag,
  },
];

export const UnifiedNavbar: React.FC<UnifiedNavbarProps> = ({
  mode,
  isSwitching = false,
  onModeChange,
  onOpenMerchantAffiliation,
}) => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [soundPanelOpen, setSoundPanelOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);
  const [soundState, setSoundState] = useState(() => kuotaSound.getState());

  const activeModeNormalized: 'ecosistema' | 'compradores' | 'comercios' =
    mode === 'proveedores' ? 'comercios' : mode;

  const navLinks =
    activeModeNormalized === 'ecosistema'
      ? ECOSYSTEM_LINKS
      : activeModeNormalized === 'compradores'
      ? BUYER_LINKS
      : SUPPLIER_LINKS;

  const navItemRefs = useRef<Record<string, HTMLAnchorElement | null>>({});
  const [activeNavHref, setActiveNavHref] = useState<string>(navLinks[0].href);
  const [hoveredNavHref, setHoveredNavHref] = useState<string | null>(null);
  const [navPillBounds, setNavPillBounds] = useState<{
    left: number;
    width: number;
    ready: boolean;
  }>({ left: 0, width: 0, ready: false });

  useEffect(() => {
    setActiveNavHref(navLinks[0].href);
    setHoveredNavHref(null);
  }, [mode]);

  useEffect(() => {
    return kuotaSound.subscribe(setSoundState);
  }, []);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 12);

      const spyOffset = 180;
      let currentActive = navLinks[0].href;
      for (const item of navLinks) {
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
  }, [navLinks]);

  useEffect(() => {
    const updateNavPillBounds = () => {
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

    updateNavPillBounds();
    window.addEventListener('resize', updateNavPillBounds);

    if (typeof document !== 'undefined' && 'fonts' in document) {
      document.fonts.ready.then(updateNavPillBounds).catch(() => {});
    }

    return () => {
      window.removeEventListener('resize', updateNavPillBounds);
    };
  }, [activeNavHref, hoveredNavHref]);

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
      audience: activeModeNormalized,
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
      className={`sticky top-0 z-50 w-full border-b transition-all duration-300 ${
        isScrolled
          ? 'bg-white/95 backdrop-blur-md border-slate-200/90 shadow-[0_4px_20px_rgba(0,0,0,0.04)]'
          : 'bg-white/90 backdrop-blur-sm border-slate-200/60'
      }`}
    >
      <div
        id="kuota-top-bar"
        className="max-w-7xl mx-auto px-2.5 sm:px-6 lg:px-8 h-16 sm:h-20 flex items-center justify-between gap-1.5 sm:gap-4 relative z-50"
      >
        {/* Izquierda: Logo oficial Kuota + Selector de 3 Perfiles (Ecosistema / Compradores / Comercios) */}
        <div className="flex items-center gap-2 sm:gap-4 min-w-0">
          <a
            href="#inicio"
            onClick={handleLogoClick}
            aria-label="Kuota — Ir al inicio"
            className="focus-visible:outline-none shrink-0 flex items-center touch-manipulation transition-transform duration-200 lg:hover:scale-[1.03] active:scale-[0.97]"
          >
            <KuotaLogo size="sm" className="sm:hidden" />
            <KuotaLogo size="md" className="hidden sm:inline-flex" />
          </a>

          {/* Capsule Switcher Tripartito (High-Contrast Modern Fintech) */}
          <div
            role="tablist"
            aria-label="Seleccionar perfil del ecosistema"
            className="inline-flex items-center gap-1 p-1 rounded-full bg-slate-100 border border-slate-200/90 shadow-inner shrink-0"
          >
            {/* Pestaña 1: Ecosistema */}
            <button
              type="button"
              role="tab"
              aria-selected={activeModeNormalized === 'ecosistema'}
              onClick={() => handleSwitchMode('ecosistema', 'navbar_pill')}
              className={`inline-flex items-center justify-center gap-1.5 px-3 sm:px-3.5 py-1.5 rounded-full text-xs font-bold whitespace-nowrap shrink-0 transition-all duration-200 select-none active:scale-95 ${
                activeModeNormalized === 'ecosistema'
                  ? 'bg-[#070A0F] text-white shadow-sm'
                  : 'bg-transparent text-slate-600 hover:text-slate-900 hover:bg-white/70'
              }`}
            >
              <Layers
                className={`w-3.5 h-3.5 shrink-0 ${
                  activeModeNormalized === 'ecosistema' ? 'text-[#10B981]' : 'text-slate-500'
                }`}
              />
              <span>Ecosistema</span>
            </button>

            {/* Pestaña 2: Compradores */}
            <button
              type="button"
              role="tab"
              aria-selected={activeModeNormalized === 'compradores'}
              onClick={() => handleSwitchMode('compradores', 'navbar_pill')}
              className={`inline-flex items-center justify-center gap-1.5 px-3 sm:px-3.5 py-1.5 rounded-full text-xs font-bold whitespace-nowrap shrink-0 transition-all duration-200 select-none active:scale-95 ${
                activeModeNormalized === 'compradores'
                  ? 'bg-[#0391FD] text-white shadow-sm'
                  : 'bg-transparent text-slate-600 hover:text-slate-900 hover:bg-white/70'
              }`}
            >
              <ShoppingBag
                className={`w-3.5 h-3.5 shrink-0 ${
                  activeModeNormalized === 'compradores' ? 'text-white' : 'text-slate-500'
                }`}
              />
              <span>Compradores</span>
            </button>

            {/* Pestaña 3: Comercios */}
            <button
              type="button"
              role="tab"
              aria-selected={activeModeNormalized === 'comercios'}
              onClick={() => handleSwitchMode('comercios', 'navbar_pill')}
              className={`inline-flex items-center justify-center gap-1.5 px-3 sm:px-3.5 py-1.5 rounded-full text-xs font-bold whitespace-nowrap shrink-0 transition-all duration-200 select-none active:scale-95 ${
                activeModeNormalized === 'comercios'
                  ? 'bg-[#059669] text-white shadow-sm'
                  : 'bg-transparent text-slate-700 hover:text-slate-950 hover:bg-white/80'
              }`}
            >
              <Store
                className={`w-3.5 h-3.5 shrink-0 ${
                  activeModeNormalized === 'comercios' ? 'text-white' : 'text-slate-500'
                }`}
              />
              <span>Comercios</span>
            </button>
          </div>
        </div>

        {/* Centro: Navegación Desktop con indicador deslizante */}
        <nav
          aria-label="Navegación principal"
          onMouseLeave={() => setHoveredNavHref(null)}
          style={{
            opacity: isSwitching ? 0.25 : 1,
            transform: isSwitching ? 'translate3d(0, -4px, 0)' : 'translate3d(0, 0, 0)',
            transition: 'opacity 220ms ease, transform 220ms ease',
          }}
          className="relative hidden lg:flex items-center gap-1 xl:gap-1.5 p-1 rounded-2xl shrink-0"
        >
          {navPillBounds.ready && (
            <span
              aria-hidden="true"
              style={{
                left: `${navPillBounds.left}px`,
                width: `${navPillBounds.width}px`,
                transition: 'left 340ms cubic-bezier(0.22, 1, 0.36, 1), width 340ms cubic-bezier(0.22, 1, 0.36, 1)',
              }}
              className="pointer-events-none absolute top-1 bottom-1 rounded-xl bg-slate-100 border border-slate-200/80 shadow-sm z-0 flex items-end justify-center overflow-hidden"
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
                className={`relative z-10 inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-sm font-bold whitespace-nowrap transition-all duration-200 ${
                  isHighlighted
                    ? 'text-kuota-dodger -translate-y-[1px]'
                    : 'text-slate-700 hover:text-kuota-dodger'
                }`}
              >
                <IconComponent className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                <span>{link.label}</span>
              </a>
            );
          })}
        </nav>

        {/* Derecha: Identidad sonora + CTA Contextual + Hamburguesa */}
        <div className="flex items-center gap-1.5 sm:gap-2.5 shrink-0">
          {/* Identidad Sonora */}
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
              aria-label={soundState.enabled ? 'Configurar sonido' : 'Activar sonido'}
              className={`w-9 h-9 rounded-xl border-2 border-kuota-deepnavy flex items-center justify-center transition-all ${
                soundState.enabled
                  ? 'bg-kuota-deepnavy text-white shadow-sm'
                  : 'bg-white text-kuota-deepnavy hover:bg-kuota-palesky/70'
              }`}
            >
              {soundState.enabled ? (
                <Volume2 className="w-4 h-4 text-kuota-dodger animate-pulse" />
              ) : (
                <VolumeX className="w-4 h-4 opacity-70" />
              )}
            </button>

            {soundPanelOpen && (
              <div
                role="dialog"
                aria-label="Control de sonido"
                className="absolute right-0 mt-2 w-56 rounded-2xl bg-white border-2 border-kuota-deepnavy p-3.5 shadow-navy-hard z-50 space-y-3 animate-fadeIn"
              >
                <div className="flex items-center justify-between">
                  <span className="text-xs font-extrabold text-kuota-deepnavy">Sonido Kuota</span>
                  <button
                    type="button"
                    onClick={() => kuotaSound.setEnabled(!soundState.enabled)}
                    className={`px-2 py-0.5 rounded text-[11px] font-extrabold border border-kuota-deepnavy ${
                      soundState.enabled ? 'bg-kuota-dodger text-white' : 'bg-kuota-palesky text-kuota-deepnavy'
                    }`}
                  >
                    {soundState.enabled ? 'Activado' : 'Desactivado'}
                  </button>
                </div>
                <input
                  type="range"
                  min={0}
                  max={100}
                  value={Math.round(soundState.volume * 100)}
                  onChange={(e) => kuotaSound.setVolume(Number(e.target.value) / 100)}
                  className="w-full accent-kuota-dodger cursor-pointer"
                />
                <button
                  type="button"
                  onClick={() => setSoundPanelOpen(false)}
                  className="w-full py-1.5 rounded-lg bg-kuota-palesky/70 text-xs font-extrabold text-kuota-deepnavy"
                >
                  Cerrar
                </button>
              </div>
            )}
          </div>

          {/* CTA Contextual en Desktop */}
          <div className="hidden lg:block shrink-0">
            {activeModeNormalized === 'ecosistema' ? (
              <button
                type="button"
                onClick={() => {
                  kuotaSound.playCtaClick();
                  if (onOpenMerchantAffiliation) {
                    onOpenMerchantAffiliation();
                  } else {
                    onModeChange('comercios', 'navbar_cta');
                  }
                }}
                className="px-4 py-2 rounded-xl bg-kuota-deepnavy hover:bg-kuota-dodger text-white text-xs font-extrabold border-2 border-kuota-deepnavy shadow-navy-hard transition-transform hover:-translate-y-0.5"
              >
                Afiliar mi comercio
              </button>
            ) : activeModeNormalized === 'compradores' ? (
              <PlayStoreButton section="navbar" label="Descargar app" size="sm" />
            ) : (
              <button
                type="button"
                onClick={() => {
                  kuotaSound.playCtaClick();
                  if (onOpenMerchantAffiliation) onOpenMerchantAffiliation();
                }}
                className="px-4 py-2 rounded-xl bg-kuota-dodger hover:bg-kuota-crayola text-white text-xs font-extrabold border-2 border-kuota-deepnavy shadow-navy-hard transition-transform hover:-translate-y-0.5"
              >
                Afiliar mi negocio
              </button>
            )}
          </div>

          {/* Hamburguesa Móvil */}
          <button
            type="button"
            onClick={() => {
              kuotaSound.playSelectOption();
              setSoundPanelOpen(false);
              setMobileMenuOpen((prev) => !prev);
            }}
            aria-expanded={mobileMenuOpen}
            aria-controls="mobile-nav-menu"
            aria-label={mobileMenuOpen ? 'Cerrar menú' : 'Abrir menú'}
            className={`lg:hidden w-10 h-10 rounded-xl border-2 border-kuota-deepnavy flex items-center justify-center transition-all ${
              mobileMenuOpen ? 'bg-kuota-deepnavy text-white' : 'bg-white text-kuota-deepnavy'
            }`}
          >
            {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </div>

      {/* Backdrop móvil */}
      {mobileMenuOpen && (
        <div
          aria-hidden="true"
          onClick={() => setMobileMenuOpen(false)}
          className="fixed inset-0 top-16 bg-kuota-deepnavy/25 backdrop-blur-[1px] z-40 lg:hidden"
        />
      )}

      {/* Menú Móvil Desplegable */}
      <div
        id="mobile-nav-menu"
        aria-hidden={!mobileMenuOpen}
        className={`lg:hidden absolute top-full left-0 right-0 z-50 grid transition-all duration-300 ${
          mobileMenuOpen
            ? 'grid-rows-[1fr] opacity-100 border-t-2 border-b-2 border-kuota-deepnavy shadow-navy-hard-lg'
            : 'grid-rows-[0fr] opacity-0 pointer-events-none'
        }`}
      >
        <div className="overflow-hidden bg-white/98 backdrop-blur-md p-4 space-y-3">
          {/* Selector de perfil para móviles */}
          <div className="p-1 rounded-2xl bg-slate-100 border border-slate-200/90 grid grid-cols-3 gap-1 mb-2">
            <button
              type="button"
              onClick={() => handleSwitchMode('ecosistema', 'mobile_drawer')}
              className={`py-2 px-1 rounded-xl text-xs font-bold flex flex-col items-center justify-center gap-1 transition-all ${
                activeModeNormalized === 'ecosistema'
                  ? 'bg-[#070A0F] text-white shadow-sm'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <Layers className={`w-4 h-4 ${activeModeNormalized === 'ecosistema' ? 'text-[#10B981]' : 'text-slate-500'}`} />
              <span>Ecosistema</span>
            </button>
            <button
              type="button"
              onClick={() => handleSwitchMode('compradores', 'mobile_drawer')}
              className={`py-2 px-1 rounded-xl text-xs font-bold flex flex-col items-center justify-center gap-1 transition-all ${
                activeModeNormalized === 'compradores'
                  ? 'bg-[#0391FD] text-white shadow-sm'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <ShoppingBag className={`w-4 h-4 ${activeModeNormalized === 'compradores' ? 'text-white' : 'text-slate-500'}`} />
              <span>Compradores</span>
            </button>
            <button
              type="button"
              onClick={() => handleSwitchMode('comercios', 'mobile_drawer')}
              className={`py-2 px-1 rounded-xl text-xs font-bold flex flex-col items-center justify-center gap-1 transition-all ${
                activeModeNormalized === 'comercios'
                  ? 'bg-[#059669] text-white shadow-sm'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <Store className={`w-4 h-4 ${activeModeNormalized === 'comercios' ? 'text-white' : 'text-slate-500'}`} />
              <span>Comercios</span>
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            {navLinks.map((link) => {
              const Icon = link.icon;
              return (
                <a
                  key={link.href}
                  href={link.href}
                  onClick={(e) => handleLinkClick(e, link.label, link.href)}
                  className="flex items-center justify-between p-3 rounded-2xl bg-kuota-palesky/30 border border-kuota-deepnavy/20 active:bg-kuota-palesky/70 text-xs font-extrabold text-kuota-deepnavy"
                >
                  <div className="flex items-center gap-2.5">
                    <Icon className="w-4 h-4 text-kuota-dodger" />
                    <div>
                      <div className="text-sm font-extrabold">{link.label}</div>
                      <div className="text-[10px] font-semibold text-kuota-crayola">{link.subtitle}</div>
                    </div>
                  </div>
                  <ArrowRight className="w-4 h-4 opacity-60" />
                </a>
              );
            })}
          </div>

          <div className="pt-2 border-t border-kuota-deepnavy/15 flex flex-col gap-2">
            <a
              href="mailto:kuota.contact@gmail.com"
              className="text-center py-2 rounded-xl bg-kuota-palesky/50 border border-kuota-deepnavy text-xs font-extrabold text-kuota-deepnavy"
            >
              kuota.contact@gmail.com
            </a>
            {activeModeNormalized === 'compradores' ? (
              <PlayStoreButton section="mobile_menu" size="md" className="w-full" />
            ) : (
              <button
                type="button"
                onClick={() => {
                  setMobileMenuOpen(false);
                  if (onOpenMerchantAffiliation) onOpenMerchantAffiliation();
                }}
                className="w-full py-3 rounded-xl bg-kuota-dodger text-white font-extrabold text-sm border-2 border-kuota-deepnavy shadow-navy-hard"
              >
                Afiliar mi comercio
              </button>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

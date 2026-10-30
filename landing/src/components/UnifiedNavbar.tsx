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
        className="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 h-16 sm:h-20 flex items-center justify-between gap-3 sm:gap-6 relative z-50"
      >
        {/* Izquierda: Logo oficial Kuota + Selector de 3 Perfiles (Ecosistema / Compradores / Comercios) */}
        <div className="flex items-center gap-3 sm:gap-4 shrink-0">
          <a
            href="#inicio"
            onClick={handleLogoClick}
            aria-label="Kuota — Ir al inicio"
            className="focus-visible:outline-none shrink-0 flex items-center touch-manipulation transition-transform duration-200 hover:scale-[1.02] active:scale-[0.98]"
          >
            <KuotaLogo size="sm" className="sm:hidden" />
            <KuotaLogo size="md" className="hidden sm:inline-flex" />
          </a>

          {/* Capsule Switcher Tripartito (High-Contrast Modern Fintech) */}
          <div
            role="tablist"
            aria-label="Seleccionar perfil del ecosistema"
            className="hidden sm:inline-flex items-center gap-1 p-1 rounded-full bg-slate-100/90 border border-slate-200/80 shadow-inner shrink-0"
          >
            {/* Pestaña 1: Ecosistema */}
            <button
              type="button"
              role="tab"
              aria-selected={activeModeNormalized === 'ecosistema'}
              onClick={() => handleSwitchMode('ecosistema', 'navbar_pill')}
              className={`inline-flex items-center justify-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap shrink-0 transition-all duration-200 select-none cursor-pointer active:scale-95 ${
                activeModeNormalized === 'ecosistema'
                  ? 'bg-[#0A192F] text-white shadow-sm'
                  : 'bg-transparent text-slate-600 hover:text-slate-900 hover:bg-white/80'
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
              className={`inline-flex items-center justify-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap shrink-0 transition-all duration-200 select-none cursor-pointer active:scale-95 ${
                activeModeNormalized === 'compradores'
                  ? 'bg-[#0391FD] text-white shadow-sm'
                  : 'bg-transparent text-slate-600 hover:text-slate-900 hover:bg-white/80'
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
              className={`inline-flex items-center justify-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap shrink-0 transition-all duration-200 select-none cursor-pointer active:scale-95 ${
                activeModeNormalized === 'comercios'
                  ? 'bg-[#059669] text-white shadow-sm'
                  : 'bg-transparent text-slate-600 hover:text-slate-900 hover:bg-white/80'
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

        {/* Centro: Navegación de secciones (Visible únicamente en pantallas ultra-amplias 2xl:flex para garantizar cero colisión en cualquier laptop o tablet) */}
        <nav
          aria-label="Navegación principal"
          onMouseLeave={() => setHoveredNavHref(null)}
          style={{
            opacity: isSwitching ? 0.25 : 1,
            transform: isSwitching ? 'translate3d(0, -4px, 0)' : 'translate3d(0, 0, 0)',
            transition: 'opacity 220ms ease, transform 220ms ease',
          }}
          className="relative hidden 2xl:flex items-center gap-1 p-1 rounded-full bg-slate-50/90 border border-slate-200/60 shrink-0"
        >
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
                className={`relative z-10 inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full text-xs font-bold whitespace-nowrap transition-all duration-200 cursor-pointer ${
                  isHighlighted
                    ? 'bg-white text-[#0391FD] shadow-sm'
                    : 'text-slate-600 hover:text-[#0A192F] hover:bg-white/60'
                }`}
              >
                <IconComponent className={`w-3.5 h-3.5 shrink-0 ${isHighlighted ? 'text-[#0391FD]' : 'text-slate-400'}`} aria-hidden="true" />
                <span>{link.label}</span>
              </a>
            );
          })}
        </nav>

        {/* Derecha: Identidad sonora + CTA Contextual + Hamburguesa */}
        <div className="flex items-center gap-2 sm:gap-3 shrink-0">
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
              className={`w-9 h-9 rounded-full flex items-center justify-center transition-all cursor-pointer ${
                soundState.enabled
                  ? 'bg-sky-50 text-[#0391FD] border border-sky-200/80 shadow-sm'
                  : 'bg-slate-100 hover:bg-slate-200 text-slate-500 hover:text-slate-800 border border-slate-200/70'
              }`}
            >
              {soundState.enabled ? (
                <Volume2 className="w-4 h-4 text-[#0391FD] animate-pulse" />
              ) : (
                <VolumeX className="w-4 h-4 opacity-70" />
              )}
            </button>

            {soundPanelOpen && (
              <div
                role="dialog"
                aria-label="Control de sonido"
                className="absolute right-0 mt-2 w-60 rounded-2xl bg-white border border-slate-200/90 p-4 shadow-xl z-50 space-y-3 animate-fadeIn text-[#0A192F]"
              >
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-slate-800">Efectos sonoros Kuota</span>
                  <button
                    type="button"
                    onClick={() => kuotaSound.setEnabled(!soundState.enabled)}
                    className={`px-2.5 py-1 rounded-full text-[11px] font-bold transition-colors cursor-pointer ${
                      soundState.enabled
                        ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                        : 'bg-slate-100 text-slate-500'
                    }`}
                  >
                    {soundState.enabled ? 'Activado' : 'Silenciado'}
                  </button>
                </div>
                <div className="space-y-1.5">
                  <div className="flex justify-between text-[11px] text-slate-500 font-medium">
                    <span>Volumen</span>
                    <span className="font-bold text-slate-700">{Math.round(soundState.volume * 100)}%</span>
                  </div>
                  <input
                    type="range"
                    min={0}
                    max={100}
                    value={Math.round(soundState.volume * 100)}
                    onChange={(e) => kuotaSound.setVolume(Number(e.target.value) / 100)}
                    className="w-full accent-[#0391FD] cursor-pointer"
                  />
                </div>
                <button
                  type="button"
                  onClick={() => setSoundPanelOpen(false)}
                  className="w-full py-1.5 rounded-full bg-slate-100 hover:bg-slate-200 text-xs font-bold text-slate-700 transition-colors cursor-pointer"
                >
                  Listo
                </button>
              </div>
            )}
          </div>

          {/* CTA Contextual en Desktop */}
          <div className="hidden sm:block shrink-0">
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
                className="px-4 sm:px-5 py-2 sm:py-2.5 rounded-full bg-[#0A192F] hover:bg-[#0391FD] text-white text-xs sm:text-sm font-bold shadow-sm hover:shadow transition-all duration-200 active:scale-95 cursor-pointer"
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
                className="px-4 sm:px-5 py-2 sm:py-2.5 rounded-full bg-[#10B981] hover:bg-[#059669] text-white text-xs sm:text-sm font-bold shadow-sm hover:shadow transition-all duration-200 active:scale-95 cursor-pointer"
              >
                Afiliar mi negocio
              </button>
            )}
          </div>

          {/* Hamburguesa (visible en pantallas inferiores a 2xl para evitar colisión de la barra) */}
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
            className={`2xl:hidden w-9 h-9 rounded-full flex items-center justify-center transition-all cursor-pointer ${
              mobileMenuOpen
                ? 'bg-[#0A192F] text-white shadow-sm'
                : 'bg-slate-100 hover:bg-slate-200 text-slate-700 border border-slate-200/80'
            }`}
          >
            {mobileMenuOpen ? <X className="w-4 h-4" /> : <Menu className="w-4 h-4" />}
          </button>
        </div>
      </div>

      {/* Backdrop móvil / tablet */}
      {mobileMenuOpen && (
        <div
          aria-hidden="true"
          onClick={() => setMobileMenuOpen(false)}
          className="fixed inset-0 top-16 sm:top-20 bg-slate-900/60 backdrop-blur-sm z-40 2xl:hidden"
        />
      )}

      {/* Menú Móvil / Tablet Desplegable (Completamente opaco y sin duplicar selector en tablet) */}
      <div
        id="mobile-nav-menu"
        aria-hidden={!mobileMenuOpen}
        className={`2xl:hidden fixed top-16 sm:top-20 inset-x-0 bottom-0 z-50 bg-[#0A192F] text-white border-t border-white/10 transition-all duration-300 overflow-y-auto ${
          mobileMenuOpen
            ? 'opacity-100 pointer-events-auto'
            : 'opacity-0 pointer-events-none'
        }`}
      >
        <div className="p-5 space-y-4 max-w-lg mx-auto">
          {/* Selector de perfil: Solo visible en móviles (< sm) ya que en tablet y desktop ya está en la barra superior */}
          <div className="sm:hidden p-1.5 rounded-2xl bg-white/5 border border-white/10 grid grid-cols-3 gap-1.5 shadow-inner">
            <button
              type="button"
              onClick={() => handleSwitchMode('ecosistema', 'mobile_drawer')}
              className={`py-2.5 px-2 rounded-xl text-xs font-bold flex flex-col items-center justify-center gap-1.5 transition-all ${
                activeModeNormalized === 'ecosistema'
                  ? 'bg-white text-[#070A0F] shadow-sm font-extrabold'
                  : 'text-slate-400 hover:text-white hover:bg-white/5'
              }`}
            >
              <Layers className={`w-4 h-4 ${activeModeNormalized === 'ecosistema' ? 'text-[#10B981]' : 'text-slate-400'}`} />
              <span>Ecosistema</span>
            </button>
            <button
              type="button"
              onClick={() => handleSwitchMode('compradores', 'mobile_drawer')}
              className={`py-2.5 px-2 rounded-xl text-xs font-bold flex flex-col items-center justify-center gap-1.5 transition-all ${
                activeModeNormalized === 'compradores'
                  ? 'bg-[#0391FD] text-white shadow-sm font-extrabold'
                  : 'text-slate-400 hover:text-white hover:bg-white/5'
              }`}
            >
              <ShoppingBag className={`w-4 h-4 ${activeModeNormalized === 'compradores' ? 'text-white' : 'text-slate-400'}`} />
              <span>Compradores</span>
            </button>
            <button
              type="button"
              onClick={() => handleSwitchMode('comercios', 'mobile_drawer')}
              className={`py-2.5 px-2 rounded-xl text-xs font-bold flex flex-col items-center justify-center gap-1.5 transition-all ${
                activeModeNormalized === 'comercios'
                  ? 'bg-[#059669] text-white shadow-sm font-extrabold'
                  : 'text-slate-400 hover:text-white hover:bg-white/5'
              }`}
            >
              <Store className={`w-4 h-4 ${activeModeNormalized === 'comercios' ? 'text-white' : 'text-slate-400'}`} />
              <span>Comercios</span>
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 pt-2">
            {navLinks.map((link) => {
              const Icon = link.icon;
              return (
                <a
                  key={link.href}
                  href={link.href}
                  onClick={(e) => handleLinkClick(e, link.label, link.href)}
                  className="flex items-center justify-between p-3.5 rounded-2xl bg-white/[0.04] border border-white/10 hover:bg-white/10 active:bg-white/15 text-white transition-colors"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-xl bg-white/10 flex items-center justify-center shrink-0">
                      <Icon className="w-4 h-4 text-[#38BDF8]" />
                    </div>
                    <div>
                      <div className="text-sm font-bold text-white">{link.label}</div>
                      <div className="text-[11px] text-slate-400">{link.subtitle}</div>
                    </div>
                  </div>
                  <ArrowRight className="w-4 h-4 text-slate-500" />
                </a>
              );
            })}
          </div>

          <div className="pt-4 border-t border-white/10 flex flex-col gap-2.5">
            <a
              href="mailto:kuota.contact@gmail.com"
              className="text-center py-2.5 rounded-xl bg-white/5 border border-white/10 text-xs font-semibold text-slate-300 hover:text-white hover:bg-white/10 transition-colors"
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
                className="w-full py-3.5 rounded-xl bg-[#0391FD] hover:bg-[#0277D4] text-white font-bold text-sm shadow-md transition-all active:scale-[0.98]"
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

import React, { useEffect, useState } from 'react';
import { Menu, X, ShoppingBag, Store, Volume2, VolumeX } from 'lucide-react';
import { KuotaLogo } from './KuotaLogo';
import { PlayStoreButton } from './PlayStoreButton';
import { ScheduleCallButton } from './supplier/ScheduleCallButton';
import { trackEvent } from '../utils/analytics';
import { kuotaSound } from '../utils/soundSystem';

export type AudienceMode = 'compradores' | 'proveedores';

interface UnifiedNavbarProps {
  mode: AudienceMode;
  onModeChange: (mode: AudienceMode, source: string) => void;
}

const BUYER_LINKS = [
  { label: 'Producto', href: '#producto' },
  { label: 'Beneficios', href: '#beneficios' },
  { label: 'Niveles', href: '#niveles' },
  { label: 'Nosotros', href: '#confianza' },
];

const SUPPLIER_LINKS = [
  { label: 'Producto', href: '#producto' },
  { label: 'Calculadora', href: '#calculadora' },
  { label: 'Precios', href: '#precios' },
  { label: 'Nosotros', href: '#nosotros' },
];

export const UnifiedNavbar: React.FC<UnifiedNavbarProps> = ({
  mode,
  onModeChange,
}) => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [soundPanelOpen, setSoundPanelOpen] = useState(false);
  const [soundState, setSoundState] = useState(() => kuotaSound.getState());

  useEffect(() => {
    return kuotaSound.subscribe(setSoundState);
  }, []);

  const navLinks = mode === 'compradores' ? BUYER_LINKS : SUPPLIER_LINKS;

  const handleLinkClick = (label: string) => {
    kuotaSound.playSelectOption();
    trackEvent('click_nav_link', { section: label.toLowerCase(), audience: mode });
    setMobileMenuOpen(false);
  };

  const handleSwitchMode = (nextMode: AudienceMode, source: string) => {
    kuotaSound.playSelectOption();
    onModeChange(nextMode, source);
  };

  return (
    <header className="sticky top-0 z-50 w-full bg-kuota-palesky/90 backdrop-blur-md border-b-2 border-kuota-deepnavy">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 h-16 sm:h-20 flex items-center justify-between gap-3">
        {/* Izquierda: Logo oficial Kuota + Selector único de Perfil */}
        <div className="flex items-center gap-3 sm:gap-5">
          <a
            href="#inicio"
            aria-label="Kuota — Ir al inicio"
            className="focus-visible:outline-none shrink-0 transition-transform duration-150 hover:scale-[1.02]"
          >
            <KuotaLogo size="md" />
          </a>

          {/* Pill Switcher único Compradores / Proveedores */}
          <div
            role="tablist"
            aria-label="Seleccionar perfil de usuario"
            className="inline-flex items-center p-1 rounded-full bg-white border-2 border-kuota-deepnavy shadow-sm"
          >
            <button
              type="button"
              role="tab"
              aria-selected={mode === 'compradores'}
              onClick={() => handleSwitchMode('compradores', 'navbar_pill')}
              className={`inline-flex items-center gap-1.5 px-2.5 sm:px-3.5 py-1.5 rounded-full text-[11px] sm:text-xs font-extrabold transition-all duration-200 ${
                mode === 'compradores'
                  ? 'bg-kuota-dodger text-white shadow-sm'
                  : 'text-kuota-deepnavy hover:bg-kuota-palesky/60'
              }`}
            >
              <ShoppingBag className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
              <span>Compradores</span>
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={mode === 'proveedores'}
              onClick={() => handleSwitchMode('proveedores', 'navbar_pill')}
              className={`inline-flex items-center gap-1.5 px-2.5 sm:px-3.5 py-1.5 rounded-full text-[11px] sm:text-xs font-extrabold transition-all duration-200 ${
                mode === 'proveedores'
                  ? 'bg-kuota-deepnavy text-white shadow-sm'
                  : 'text-kuota-deepnavy hover:bg-kuota-palesky/60'
              }`}
            >
              <Store className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
              <span>Proveedores</span>
            </button>
          </div>
        </div>

        {/* Centro: Navegación Desktop */}
        <nav
          aria-label="Navegación principal"
          className="hidden md:flex items-center gap-5 lg:gap-7"
        >
          {navLinks.map((link) => (
            <a
              key={link.href}
              href={link.href}
              onClick={() => handleLinkClick(link.label)}
              className="relative text-sm font-bold text-kuota-deepnavy hover:text-kuota-dodger transition-colors duration-150 py-1 after:content-[''] after:absolute after:left-0 after:bottom-0 after:w-full after:h-[2px] after:bg-kuota-dodger after:scale-x-0 hover:after:scale-x-100 after:transition-transform after:duration-200 after:origin-left"
            >
              {link.label}
            </a>
          ))}
        </nav>

        {/* Derecha: Control de identidad sonora + CTA contextual + menú móvil */}
        <div className="flex items-center gap-2">
          {/* Control opcional de Identidad Sonora Kuota (FASE 5 — desactivado por defecto) */}
          <div className="relative">
            <button
              type="button"
              onClick={() => {
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
              className={`w-9 h-9 rounded-xl border-2 border-kuota-deepnavy flex items-center justify-center transition-all duration-150 ${
                soundState.enabled
                  ? 'bg-kuota-deepnavy text-white shadow-sm'
                  : 'bg-white text-kuota-deepnavy hover:bg-kuota-palesky/70'
              }`}
            >
              {soundState.enabled ? (
                <Volume2 className="w-4 h-4 text-kuota-dodger" aria-hidden="true" />
              ) : (
                <VolumeX className="w-4 h-4 opacity-70" aria-hidden="true" />
              )}
            </button>

            {soundPanelOpen && (
              <div
                role="dialog"
                aria-label="Control de sonido Kuota"
                className="absolute right-0 mt-2 w-60 rounded-2xl bg-white border-2 border-kuota-deepnavy p-3.5 shadow-navy-hard z-50 space-y-3"
              >
                <div className="flex items-center justify-between gap-2">
                  <span className="text-xs font-extrabold text-kuota-deepnavy">
                    Sonido interactivo
                  </span>
                  <button
                    type="button"
                    onClick={() => kuotaSound.setEnabled(!soundState.enabled)}
                    className={`px-2.5 py-1 rounded-lg text-[11px] font-extrabold border border-kuota-deepnavy transition-colors ${
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
                  className="w-full py-1.5 rounded-lg bg-kuota-palesky/70 hover:bg-kuota-palesky text-[11px] font-extrabold text-kuota-deepnavy transition-colors"
                >
                  Listo
                </button>
              </div>
            )}
          </div>

          <div className="hidden md:block">
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

          {/* Botón hamburguesa solo en móvil (< md) */}
          <button
            type="button"
            onClick={() => setMobileMenuOpen((prev) => !prev)}
            aria-expanded={mobileMenuOpen}
            aria-controls="mobile-nav-menu"
            aria-label={mobileMenuOpen ? 'Cerrar menú de navegación' : 'Abrir menú de navegación'}
            className="md:hidden inline-flex items-center justify-center p-2 rounded-xl border-2 border-kuota-deepnavy bg-white text-kuota-deepnavy hover:bg-kuota-palesky transition-colors"
          >
            {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>
      </div>

      {/* Menú desplegable móvil */}
      {mobileMenuOpen && (
        <nav
          id="mobile-nav-menu"
          aria-label="Navegación móvil"
          className="md:hidden bg-white border-b-2 border-kuota-deepnavy px-4 pt-3 pb-5 space-y-3 shadow-lg"
        >
          <div className="flex flex-col space-y-1">
            {navLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                onClick={() => handleLinkClick(link.label)}
                className="px-3 py-2.5 rounded-xl text-base font-bold text-kuota-deepnavy hover:bg-kuota-palesky transition-colors"
              >
                {link.label}
              </a>
            ))}
          </div>

          <div className="pt-2 border-t border-kuota-deepnavy/15">
            {mode === 'compradores' ? (
              <PlayStoreButton
                section="navbar"
                size="md"
                className="w-full"
              />
            ) : (
              <ScheduleCallButton
                section="navbar_mobile"
                size="md"
                className="w-full"
                onClickExtra={() => setMobileMenuOpen(false)}
              />
            )}
          </div>
        </nav>
      )}
    </header>
  );
};

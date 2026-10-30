'use client';

import React, { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronDown, Menu, X, Sparkles, Shield, ArrowUpRight, CheckCircle2, Layers, ShoppingBag, Store } from 'lucide-react';
import { KuotaLogo } from '../KuotaLogo';
import { AudienceMode } from '../UnifiedNavbar';

interface NavbarCapsuleProps {
  onOpenEvaluationModal: () => void;
  activeSection?: string;
  mode?: AudienceMode;
  onModeChange?: (mode: AudienceMode, source: string) => void;
}

export const NavbarCapsule: React.FC<NavbarCapsuleProps> = ({
  onOpenEvaluationModal,
  activeSection = 'inicio',
  mode = 'ecosistema',
  onModeChange,
}) => {
  const [activeTab, setActiveTab] = useState<string>('plataforma');
  const [simuladorOpen, setSimuladorOpen] = useState(false);
  const [radarOpen, setRadarOpen] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  const simuladorRef = useRef<HTMLDivElement>(null);
  const radarRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (simuladorRef.current && !simuladorRef.current.contains(e.target as Node)) {
        setSimuladorOpen(false);
      }
      if (radarRef.current && !radarRef.current.contains(e.target as Node)) {
        setRadarOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const scrollTo = (hash: string, tabKey?: string) => {
    if (tabKey) setActiveTab(tabKey);
    setSimuladorOpen(false);
    setRadarOpen(false);
    setMobileMenuOpen(false);
    const target = document.querySelector(hash);
    if (target) {
      target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };

  return (
    <header
      className={`fixed top-0 inset-x-0 z-50 transition-all duration-300 ${
        scrolled
          ? 'bg-[#070A0F]/95 backdrop-blur-md border-b border-white/10 shadow-2xl py-3.5'
          : 'bg-[#070A0F] border-b border-white/5 py-4'
      }`}
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex items-center justify-between">
        {/* LOGO KUOTA + SELECTOR DE PERFIL TRIPARTITO */}
        <div className="flex items-center gap-3">
          <a
            href="#inicio"
            onClick={(e) => {
              e.preventDefault();
              scrollTo('#inicio', 'plataforma');
            }}
            className="flex items-center gap-2.5 group focus:outline-none shrink-0"
          >
            <div className="relative flex items-center">
              <KuotaLogo variant="inverted" className="h-7 w-auto object-contain" />
            </div>
          </a>

          {/* Capsule Switcher Tripartito (High-Contrast Dark Fintech) */}
          {onModeChange && (
            <div
              role="tablist"
              aria-label="Seleccionar perfil del ecosistema"
              className="hidden sm:inline-flex items-center gap-1 p-1 rounded-full bg-[#161B22]/90 border border-white/10 shadow-inner shrink-0"
            >
              <button
                type="button"
                role="tab"
                aria-selected={mode === 'ecosistema'}
                onClick={() => onModeChange('ecosistema', 'navbar_capsule')}
                className={`inline-flex items-center justify-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold whitespace-nowrap transition-all duration-200 select-none ${
                  mode === 'ecosistema'
                    ? 'bg-white text-[#070A0F] shadow-sm'
                    : 'bg-transparent text-slate-300 hover:text-white hover:bg-white/10'
                }`}
              >
                <Layers
                  className={`w-3.5 h-3.5 shrink-0 ${
                    mode === 'ecosistema' ? 'text-[#10B981]' : 'text-slate-400'
                  }`}
                />
                <span>Ecosistema</span>
              </button>

              <button
                type="button"
                role="tab"
                aria-selected={mode === 'compradores'}
                onClick={() => onModeChange('compradores', 'navbar_capsule')}
                className={`inline-flex items-center justify-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold whitespace-nowrap transition-all duration-200 select-none ${
                  mode === 'compradores'
                    ? 'bg-[#0391FD] text-white shadow-sm'
                    : 'bg-transparent text-slate-300 hover:text-white hover:bg-white/10'
                }`}
              >
                <ShoppingBag
                  className={`w-3.5 h-3.5 shrink-0 ${
                    mode === 'compradores' ? 'text-white' : 'text-slate-400'
                  }`}
                />
                <span>Compradores</span>
              </button>

              <button
                type="button"
                role="tab"
                aria-selected={mode === 'comercios'}
                onClick={() => onModeChange('comercios', 'navbar_capsule')}
                className={`inline-flex items-center justify-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold whitespace-nowrap transition-all duration-200 select-none ${
                  mode === 'comercios'
                    ? 'bg-[#10B981] text-[#070A0F] font-extrabold shadow-sm'
                    : 'bg-transparent text-slate-300 hover:text-white hover:bg-white/10'
                }`}
              >
                <Store
                  className={`w-3.5 h-3.5 shrink-0 ${
                    mode === 'comercios' ? 'text-[#070A0F]' : 'text-slate-400'
                  }`}
                />
                <span>Comercios</span>
              </button>
            </div>
          )}
        </div>

        {/* CENTRAL CAPSULE (DESKTOP) */}
        <nav className="hidden lg:flex items-center bg-[#161B22]/90 border border-white/10 rounded-full p-1.5 shadow-inner">
          {/* 1. Niveles de Línea */}
          <button
            onClick={() => scrollTo('#niveles', 'niveles')}
            className={`relative px-4 py-1.5 rounded-full text-xs font-semibold tracking-tight transition-colors ${
              activeTab === 'niveles' ? 'text-white' : 'text-slate-300 hover:text-white'
            }`}
          >
            {activeTab === 'niveles' && (
              <motion.div
                layoutId="nav-pill"
                className="absolute inset-0 rounded-full bg-white/15 border border-white/15"
                transition={{ type: 'spring', bounce: 0.2, duration: 0.4 }}
              />
            )}
            <span className="relative z-10">Niveles de Línea</span>
          </button>

          {/* 2. Refiere PYMES */}
          <button
            onClick={() => scrollTo('#testimonios', 'refiere')}
            className={`relative px-4 py-1.5 rounded-full text-xs font-semibold tracking-tight transition-colors ${
              activeTab === 'refiere' ? 'text-white' : 'text-slate-300 hover:text-white'
            }`}
          >
            {activeTab === 'refiere' && (
              <motion.div
                layoutId="nav-pill"
                className="absolute inset-0 rounded-full bg-white/15 border border-white/15"
                transition={{ type: 'spring', bounce: 0.2, duration: 0.4 }}
              />
            )}
            <span className="relative z-10 flex items-center gap-1">
              Refiere PYMES
              <span className="text-[9px] px-1 py-0.2 rounded bg-emerald-500/20 text-emerald-300 font-bold">
                +S/500
              </span>
            </span>
          </button>

          {/* 3. Plataforma & Nexo */}
          <button
            onClick={() => scrollTo('#plataforma', 'plataforma')}
            className={`relative px-4 py-1.5 rounded-full text-xs font-semibold tracking-tight transition-colors ${
              activeTab === 'plataforma' ? 'text-white' : 'text-slate-300 hover:text-white'
            }`}
          >
            {activeTab === 'plataforma' && (
              <motion.div
                layoutId="nav-pill"
                className="absolute inset-0 rounded-full bg-white/15 border border-white/15"
                transition={{ type: 'spring', bounce: 0.2, duration: 0.4 }}
              />
            )}
            <span className="relative z-10 flex items-center gap-1">
              <Sparkles className="w-3 h-3 text-[#8FD694]" />
              Plataforma & Nexo
            </span>
          </button>

          {/* 4. Kuotear Inventario */}
          <button
            onClick={() => scrollTo('#como-empezar', 'kuotear')}
            className={`relative px-4 py-1.5 rounded-full text-xs font-semibold tracking-tight transition-colors ${
              activeTab === 'kuotear' ? 'text-white' : 'text-slate-300 hover:text-white'
            }`}
          >
            {activeTab === 'kuotear' && (
              <motion.div
                layoutId="nav-pill"
                className="absolute inset-0 rounded-full bg-white/15 border border-white/15"
                transition={{ type: 'spring', bounce: 0.2, duration: 0.4 }}
              />
            )}
            <span className="relative z-10">Kuotear Inventario</span>
          </button>

          {/* 5. Simulador Dropdown */}
          <div className="relative" ref={simuladorRef}>
            <button
              onClick={() => {
                setSimuladorOpen(!simuladorOpen);
                setRadarOpen(false);
              }}
              className="flex items-center gap-1 px-3.5 py-1.5 rounded-full text-xs font-semibold text-slate-300 hover:text-white transition-colors"
            >
              <span>Simulador</span>
              <ChevronDown
                className={`w-3.5 h-3.5 transition-transform duration-200 ${
                  simuladorOpen ? 'rotate-180 text-white' : 'text-slate-400'
                }`}
              />
            </button>

            <AnimatePresence>
              {simuladorOpen && (
                <motion.div
                  initial={{ opacity: 0, y: 8, scale: 0.96 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: 6, scale: 0.96 }}
                  transition={{ duration: 0.15 }}
                  className="absolute top-full right-0 mt-2 w-64 p-2 rounded-2xl bg-[#0F141C] border border-white/15 shadow-2xl backdrop-blur-xl z-50"
                >
                  <button
                    onClick={() => scrollTo('#transparencia')}
                    className="w-full text-left p-2.5 rounded-xl hover:bg-white/10 transition-colors group flex items-start gap-2.5"
                  >
                    <div className="w-7 h-7 rounded-lg bg-emerald-500/10 text-[#8FD694] flex items-center justify-center shrink-0 mt-0.5">
                      %
                    </div>
                    <div>
                      <div className="text-xs font-semibold text-white group-hover:text-[#8FD694]">
                        Calculadora de Tasas y ROI
                      </div>
                      <div className="text-[11px] text-slate-400">
                        Compara el costo de tu crédito vs margen de venta
                      </div>
                    </div>
                  </button>
                  <button
                    onClick={() => scrollTo('#orbital')}
                    className="w-full text-left p-2.5 rounded-xl hover:bg-white/10 transition-colors group flex items-start gap-2.5"
                  >
                    <div className="w-7 h-7 rounded-lg bg-cyan-500/10 text-cyan-400 flex items-center justify-center shrink-0 mt-0.5">
                      ⚡
                    </div>
                    <div>
                      <div className="text-xs font-semibold text-white group-hover:text-cyan-400">
                        Prevención de Quiebres
                      </div>
                      <div className="text-[11px] text-slate-400">
                        Estima cuánto dinero pierdes por quiebres al mes
                      </div>
                    </div>
                  </button>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          {/* 6. Radar B2B Dropdown */}
          <div className="relative" ref={radarRef}>
            <button
              onClick={() => {
                setRadarOpen(!radarOpen);
                setSimuladorOpen(false);
              }}
              className="flex items-center gap-1 px-3.5 py-1.5 rounded-full text-xs font-semibold text-slate-300 hover:text-white transition-colors"
            >
              <span>Radar B2B</span>
              <ChevronDown
                className={`w-3.5 h-3.5 transition-transform duration-200 ${
                  radarOpen ? 'rotate-180 text-white' : 'text-slate-400'
                }`}
              />
            </button>

            <AnimatePresence>
              {radarOpen && (
                <motion.div
                  initial={{ opacity: 0, y: 8, scale: 0.96 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: 6, scale: 0.96 }}
                  transition={{ duration: 0.15 }}
                  className="absolute top-full right-0 mt-2 w-64 p-2 rounded-2xl bg-[#0F141C] border border-white/15 shadow-2xl backdrop-blur-xl z-50"
                >
                  <button
                    onClick={() => scrollTo('#historias')}
                    className="w-full text-left p-2.5 rounded-xl hover:bg-white/10 transition-colors group flex items-start gap-2.5"
                  >
                    <div className="w-7 h-7 rounded-lg bg-indigo-500/10 text-indigo-400 flex items-center justify-center shrink-0 mt-0.5">
                      ▶
                    </div>
                    <div>
                      <div className="text-xs font-semibold text-white group-hover:text-indigo-400">
                        Casos de Éxito en Video
                      </div>
                      <div className="text-[11px] text-slate-400">
                        PYMES reales abasteciéndose a cuotas
                      </div>
                    </div>
                  </button>
                  <button
                    onClick={() => scrollTo('#blog')}
                    className="w-full text-left p-2.5 rounded-xl hover:bg-white/10 transition-colors group flex items-start gap-2.5"
                  >
                    <div className="w-7 h-7 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center shrink-0 mt-0.5">
                      📖
                    </div>
                    <div>
                      <div className="text-xs font-semibold text-white group-hover:text-emerald-400">
                        Blog de Compras & IA
                      </div>
                      <div className="text-[11px] text-slate-400">
                        Estrategias de abastecimiento y ROP
                      </div>
                    </div>
                  </button>
                  <button
                    onClick={() => scrollTo('#faq')}
                    className="w-full text-left p-2.5 rounded-xl hover:bg-white/10 transition-colors group flex items-start gap-2.5"
                  >
                    <div className="w-7 h-7 rounded-lg bg-slate-500/10 text-slate-300 flex items-center justify-center shrink-0 mt-0.5">
                      ?
                    </div>
                    <div>
                      <div className="text-xs font-semibold text-white group-hover:text-white">
                        Preguntas Frecuentes
                      </div>
                      <div className="text-[11px] text-slate-400">
                        Respuestas transparentes sin letras chicas
                      </div>
                    </div>
                  </button>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </nav>

        {/* RIGHT ACTIONS */}
        <div className="flex items-center gap-4">
          <a
            href="/login"
            className="text-xs font-semibold text-slate-300 hover:text-white transition-colors hidden sm:inline-flex items-center gap-1"
          >
            Iniciar Sesión
            <ArrowUpRight className="w-3.5 h-3.5 text-slate-400" />
          </a>

          <button
            onClick={onOpenEvaluationModal}
            className="relative inline-flex items-center justify-center px-5 sm:px-6 py-2.5 rounded-full text-xs sm:text-sm font-bold tracking-tight bg-white text-[#070A0F] hover:bg-[#8FD694] hover:text-[#070A0F] active:scale-95 transition-all shadow-[0_0_20px_rgba(255,255,255,0.2)]"
          >
            Solicitar Línea Kuota
          </button>

          {/* MOBILE TOGGLE */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="lg:hidden p-2 rounded-xl text-slate-300 hover:text-white hover:bg-white/10 transition-colors"
            aria-label="Abrir menú"
          >
            {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>
      </div>

      {/* MOBILE MENU DRAWER */}
      <AnimatePresence>
        {mobileMenuOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="lg:hidden bg-[#0A0E17] border-b border-white/10 px-6 py-6 overflow-hidden"
          >
            {/* Selector de perfil para móviles */}
            {onModeChange && (
              <div className="p-1 rounded-2xl bg-white/5 border border-white/10 grid grid-cols-3 gap-1 mb-5">
                <button
                  type="button"
                  onClick={() => {
                    setMobileMenuOpen(false);
                    onModeChange('ecosistema', 'mobile_drawer');
                  }}
                  className={`py-2 px-1 rounded-xl text-xs font-bold flex flex-col items-center justify-center gap-1 transition-all ${
                    mode === 'ecosistema'
                      ? 'bg-white text-[#070A0F] shadow-sm'
                      : 'text-slate-400 hover:text-white'
                  }`}
                >
                  <Layers
                    className={`w-4 h-4 ${
                      mode === 'ecosistema' ? 'text-[#10B981]' : 'text-slate-400'
                    }`}
                  />
                  <span>Ecosistema</span>
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setMobileMenuOpen(false);
                    onModeChange('compradores', 'mobile_drawer');
                  }}
                  className={`py-2 px-1 rounded-xl text-xs font-bold flex flex-col items-center justify-center gap-1 transition-all ${
                    mode === 'compradores'
                      ? 'bg-[#0391FD] text-white shadow-sm'
                      : 'text-slate-400 hover:text-white'
                  }`}
                >
                  <ShoppingBag
                    className={`w-4 h-4 ${
                      mode === 'compradores' ? 'text-white' : 'text-slate-400'
                    }`}
                  />
                  <span>Compradores</span>
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setMobileMenuOpen(false);
                    onModeChange('comercios', 'mobile_drawer');
                  }}
                  className={`py-2 px-1 rounded-xl text-xs font-bold flex flex-col items-center justify-center gap-1 transition-all ${
                    mode === 'comercios'
                      ? 'bg-[#10B981] text-[#070A0F] font-extrabold shadow-sm'
                      : 'text-slate-400 hover:text-white'
                  }`}
                >
                  <Store
                    className={`w-4 h-4 ${
                      mode === 'comercios' ? 'text-[#070A0F]' : 'text-slate-400'
                    }`}
                  />
                  <span>Comercios</span>
                </button>
              </div>
            )}

            <div className="space-y-3">
              <button
                onClick={() => scrollTo('#niveles', 'niveles')}
                className="w-full text-left py-2.5 text-sm font-semibold text-slate-200 border-b border-white/5 flex items-center justify-between"
              >
                <span>Niveles de Línea Kuota</span>
                <span className="text-xs text-emerald-400">Hasta S/ 200K</span>
              </button>
              <button
                onClick={() => scrollTo('#plataforma', 'plataforma')}
                className="w-full text-left py-2.5 text-sm font-semibold text-slate-200 border-b border-white/5 flex items-center justify-between"
              >
                <span>Plataforma & Nexo IA</span>
                <Sparkles className="w-4 h-4 text-[#8FD694]" />
              </button>
              <button
                onClick={() => scrollTo('#como-empezar', 'kuotear')}
                className="w-full text-left py-2.5 text-sm font-semibold text-slate-200 border-b border-white/5"
              >
                Kuotear Inventario (Paso a Paso)
              </button>
              <button
                onClick={() => scrollTo('#historias')}
                className="w-full text-left py-2.5 text-sm font-semibold text-slate-200 border-b border-white/5"
              >
                Casos de Éxito en Video
              </button>
              <button
                onClick={() => scrollTo('#transparencia')}
                className="w-full text-left py-2.5 text-sm font-semibold text-slate-200 border-b border-white/5"
              >
                Transparencia & Tasas Fijas
              </button>
              <button
                onClick={() => scrollTo('#faq')}
                className="w-full text-left py-2.5 text-sm font-semibold text-slate-200 border-b border-white/5"
              >
                Preguntas Frecuentes
              </button>
              <div className="pt-3 flex flex-col gap-2.5">
                <a
                  href="/login"
                  className="w-full py-3 rounded-full text-center text-xs font-semibold text-white bg-white/10 hover:bg-white/20 transition-colors"
                >
                  Entrar a Mi Panel / Login
                </a>
                <button
                  onClick={() => {
                    setMobileMenuOpen(false);
                    onOpenEvaluationModal();
                  }}
                  className="w-full py-3 rounded-full text-center text-xs font-bold text-[#070A0F] bg-[#8FD694] hover:bg-[#7bc580] transition-colors"
                >
                  Solicitar Línea Inmediata
                </button>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </header>
  );
};

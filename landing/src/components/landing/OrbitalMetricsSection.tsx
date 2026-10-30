import React, { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  TrendingUp,
  BellRing,
  Bot,
  QrCode,
  MessageSquare,
  Boxes,
  BarChart3,
  CreditCard,
  Store,
  Coins,
  ShieldCheck,
  Zap,
  CalendarClock,
  Percent,
  Sparkles,
  ArrowRightLeft,
  Smartphone,
  ArrowRight,
} from 'lucide-react';
import { ORBITAL_SLIDES, OrbitalNodeItem } from '../../config/fintechData';

interface OrbitalMetricsSectionProps {
  onOpenEvaluationModal: () => void;
}

export const OrbitalMetricsSection: React.FC<OrbitalMetricsSectionProps> = ({
  onOpenEvaluationModal,
}) => {
  const [currentSlideIndex, setCurrentSlideIndex] = useState(0);
  const [hoveredNode, setHoveredNode] = useState<OrbitalNodeItem | null>(null);
  const [progress, setProgress] = useState(0);

  const SLIDE_DURATION = 5000; // 5 segundos
  const currentSlide = ORBITAL_SLIDES[currentSlideIndex];

  useEffect(() => {
    setProgress(0);
    const startTime = Date.now();

    const interval = setInterval(() => {
      const elapsed = Date.now() - startTime;
      const nextProgress = Math.min((elapsed / SLIDE_DURATION) * 100, 100);
      setProgress(nextProgress);

      if (elapsed >= SLIDE_DURATION) {
        setCurrentSlideIndex((prev) => (prev + 1) % ORBITAL_SLIDES.length);
      }
    }, 50);

    return () => clearInterval(interval);
  }, [currentSlideIndex]);

  // Mapa de iconos vectoriales para los nodos
  const renderIcon = (name: string, color: string) => {
    const props = { className: 'w-4 h-4', style: { color } };
    switch (name) {
      case 'TrendingUp':
        return <TrendingUp {...props} />;
      case 'BellRing':
        return <BellRing {...props} />;
      case 'Bot':
        return <Bot {...props} />;
      case 'QrCode':
        return <QrCode {...props} />;
      case 'MessageSquare':
        return <MessageSquare {...props} />;
      case 'Boxes':
        return <Boxes {...props} />;
      case 'BarChart3':
        return <BarChart3 {...props} />;
      case 'CreditCard':
        return <CreditCard {...props} />;
      case 'Store':
        return <Store {...props} />;
      case 'Coins':
        return <Coins {...props} />;
      case 'ShieldCheck':
        return <ShieldCheck {...props} />;
      case 'Zap':
        return <Zap {...props} />;
      case 'CalendarClock':
        return <CalendarClock {...props} />;
      case 'Percent':
        return <Percent {...props} />;
      case 'ArrowRightLeft':
        return <ArrowRightLeft {...props} />;
      case 'Smartphone':
        return <Smartphone {...props} />;
      default:
        return <Sparkles {...props} />;
    }
  };

  // Radios en píxeles para cálculo polar
  const getRadiusPixels = (tier: number) => {
    switch (tier) {
      case 1:
        return 145; // Anillo interno
      case 2:
        return 225; // Anillo medio
      case 3:
        return 305; // Anillo externo
      default:
        return 220;
    }
  };

  return (
    <section id="orbital" className="relative py-8">
      <div className="max-w-7xl mx-auto mx-3 sm:mx-6 lg:mx-auto rounded-[32px] md:rounded-[48px] bg-white border border-slate-200/80 p-6 sm:p-12 md:p-16 shadow-[0_8px_30px_rgb(0,0,0,0.03)] relative overflow-hidden flex flex-col items-center justify-between min-h-[720px]">
        {/* Glow de fondo */}
        <div className="absolute inset-0 bg-radial from-[#F4F6F8]/60 via-transparent to-transparent pointer-events-none" />

        {/* Badge superior */}
        <div className="relative z-10 flex flex-col items-center text-center">
          <span className="px-3.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-800 border border-emerald-200/60 uppercase tracking-wider mb-2">
            {currentSlide.badge}
          </span>
          <p className="text-xs sm:text-sm text-slate-500 font-medium">
            El poder de Kuota en números comprobables
          </p>
        </div>

        {/* EL SISTEMA ORBITAL CENTRAL */}
        <div className="relative w-full max-w-[660px] h-[480px] sm:h-[520px] flex items-center justify-center my-4 overflow-visible">
          {/* Anillos Concéntricos Físicos */}
          <div className="absolute w-[290px] h-[290px] rounded-full border border-slate-200/70 pointer-events-none" />
          <div className="absolute w-[450px] h-[450px] rounded-full border border-slate-200/60 pointer-events-none" />
          <div className="absolute w-[610px] h-[610px] rounded-full border border-slate-200/50 pointer-events-none hidden sm:block" />

          {/* Nodos Orbitando (Renderizado Polar dinámico) */}
          <AnimatePresence mode="wait">
            <motion.div
              key={currentSlide.id}
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.9 }}
              transition={{ duration: 0.4 }}
              className="absolute inset-0 flex items-center justify-center"
            >
              {currentSlide.nodes.map((node, idx) => {
                const r = getRadiusPixels(node.radius);
                const rad = (node.angle * Math.PI) / 180;
                const x = Math.round(r * Math.cos(rad));
                const y = Math.round(r * Math.sin(rad));

                return (
                  <motion.div
                    key={node.id}
                    initial={{ scale: 0, opacity: 0 }}
                    animate={{
                      scale: 1,
                      opacity: 1,
                      x: [x, x + Math.cos(idx) * 4, x],
                      y: [y, y + Math.sin(idx) * 4, y],
                    }}
                    transition={{
                      scale: { duration: 0.4, delay: idx * 0.04 },
                      x: { duration: 3.5, repeat: Infinity, ease: 'easeInOut', delay: idx * 0.15 },
                      y: { duration: 3.5, repeat: Infinity, ease: 'easeInOut', delay: idx * 0.15 },
                    }}
                    onMouseEnter={() => setHoveredNode(node)}
                    onMouseLeave={() => setHoveredNode(null)}
                    style={{ left: `calc(50% + ${x}px)`, top: `calc(50% + ${y}px)` }}
                    className="absolute -translate-x-1/2 -translate-y-1/2 z-20 group cursor-pointer"
                  >
                    <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white/95 border border-slate-200/90 shadow-sm hover:shadow-md hover:scale-110 hover:border-emerald-500/50 transition-all">
                      <div
                        className="w-6 h-6 rounded-full flex items-center justify-center shrink-0"
                        style={{ backgroundColor: `${node.color}18` }}
                      >
                        {renderIcon(node.iconName, node.color)}
                      </div>
                      <span className="text-[11px] font-semibold text-slate-800 whitespace-nowrap">
                        {node.label}
                      </span>
                    </div>

                    {/* Tooltip Hover */}
                    {hoveredNode?.id === node.id && (
                      <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-44 p-2.5 rounded-xl bg-[#070A0F] text-white shadow-xl text-center pointer-events-none z-30">
                        <div className="text-xs font-bold text-emerald-400">{node.label}</div>
                        {node.sublabel && (
                          <div className="text-[10px] text-slate-300 mt-0.5">{node.sublabel}</div>
                        )}
                        {node.metric && (
                          <div className="text-[11px] font-mono text-cyan-300 font-bold mt-1">
                            Línea: {node.metric}
                          </div>
                        )}
                      </div>
                    )}
                  </motion.div>
                );
              })}
            </motion.div>
          </AnimatePresence>

          {/* NÚMERO Y TEXTO CENTRAL */}
          <div className="relative z-10 flex flex-col items-center text-center max-w-sm px-4">
            <AnimatePresence mode="wait">
              <motion.div
                key={currentSlide.id}
                initial={{ opacity: 0, scale: 0.85, filter: 'blur(4px)' }}
                animate={{ opacity: 1, scale: 1, filter: 'blur(0px)' }}
                exit={{ opacity: 0, scale: 1.05, filter: 'blur(4px)' }}
                transition={{ duration: 0.35 }}
                className="flex flex-col items-center"
              >
                <div className="text-5xl sm:text-6xl md:text-7xl font-extrabold text-[#070A0F] tracking-tight tabular-nums flex items-baseline justify-center">
                  {currentSlide.metricPrefix && (
                    <span className="text-emerald-500 mr-2 text-4xl sm:text-5xl">
                      {currentSlide.metricPrefix}
                    </span>
                  )}
                  <span>{currentSlide.metricNumber}</span>
                  {currentSlide.metricSuffix && (
                    <span className="text-emerald-500 text-3xl ml-1">
                      {currentSlide.metricSuffix}
                    </span>
                  )}
                </div>

                <h3 className="text-base sm:text-lg font-bold text-slate-900 mt-2">
                  {currentSlide.title}
                </h3>

                <p className="text-xs sm:text-sm text-slate-600 mt-1.5 leading-relaxed">
                  {currentSlide.subtitle}
                </p>
              </motion.div>
            </AnimatePresence>
          </div>
        </div>

        {/* PARTE INFERIOR: BOTÓN CÁPSULA + PAGINADOR DE 3 PUNTOS INTELIGENTE */}
        <div className="relative z-10 flex flex-col items-center gap-5 mt-4">
          <button
            onClick={onOpenEvaluationModal}
            className="inline-flex items-center justify-center px-8 py-3.5 rounded-full text-xs sm:text-sm font-bold bg-[#0B0D10] text-white hover:bg-[#1E293B] hover:scale-[1.02] active:scale-[0.98] transition-all shadow-md group"
          >
            <span>{currentSlide.ctaText}</span>
            <ArrowRight className="w-4 h-4 ml-2 group-hover:translate-x-1 transition-transform" />
          </button>

          {/* Paginador con Barra de Progreso Sincronizada (5s) */}
          <div className="flex items-center gap-2">
            {ORBITAL_SLIDES.map((slide, index) => {
              const isActive = index === currentSlideIndex;
              return (
                <button
                  key={slide.id}
                  onClick={() => setCurrentSlideIndex(index)}
                  className="focus:outline-none transition-all py-2"
                  aria-label={`Ir a diapositiva ${index + 1}`}
                >
                  {isActive ? (
                    <div className="w-10 h-2.5 rounded-full bg-slate-200 overflow-hidden relative">
                      <div
                        className="h-full bg-[#10B981] rounded-full transition-all duration-75"
                        style={{ width: `${progress}%` }}
                      />
                    </div>
                  ) : (
                    <div className="w-2.5 h-2.5 rounded-full bg-slate-300 hover:bg-slate-400 transition-colors" />
                  )}
                </button>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
};

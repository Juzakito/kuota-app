import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronLeft, ChevronRight, Play, Eye, Clock, MapPin, X, CheckCircle2 } from 'lucide-react';
import { VIDEO_STORIES, VideoStoryItem } from '../../config/fintechData';

export const VideoStoriesCarousel: React.FC = () => {
  const [activeIndex, setActiveIndex] = useState(0);
  const [selectedStory, setSelectedStory] = useState<VideoStoryItem | null>(null);

  const prevStory = () => {
    setActiveIndex((prev) => (prev === 0 ? VIDEO_STORIES.length - 1 : prev - 1));
  };

  const nextStory = () => {
    setActiveIndex((prev) => (prev === VIDEO_STORIES.length - 1 ? 0 : prev + 1));
  };

  return (
    <section id="historias" className="relative py-8">
      {/* MONOLITO OSCURO */}
      <div className="max-w-7xl mx-auto mx-3 sm:mx-6 lg:mx-auto rounded-[32px] md:rounded-[48px] bg-[#070A0F] border border-white/10 py-16 sm:py-24 px-4 sm:px-8 relative overflow-hidden shadow-2xl">
        {/* Glow Superior Esmeralda */}
        <div className="absolute top-0 inset-x-0 h-48 bg-gradient-to-b from-[#10B981]/15 to-transparent pointer-events-none" />

        {/* Encabezado Centrado */}
        <div className="relative z-10 text-center max-w-2xl mx-auto mb-14">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white/5 border border-white/10 text-xs font-semibold text-emerald-400 mb-3 tracking-wide">
            <span className="w-2 h-2 rounded-full bg-[#10B981]" />
            <span>Casos de Éxito en Formato Reel 9:16</span>
          </div>
          <h2 className="text-2xl sm:text-3xl md:text-4xl lg:text-[44px] font-bold text-white tracking-tight leading-tight">
            Conoce cómo hacemos que abastecer tu empresa{' '}
            <span className="bg-clip-text text-transparent bg-gradient-to-r from-[#8FD694] via-[#34D399] to-[#2DD4BF]">
              sea más fácil
            </span>
          </h2>
          <p className="text-xs sm:text-sm text-slate-400 mt-3">
            Dueños de negocio peruanos comparten cómo pasaron de sufrir por falta de stock a financiar compras con un solo toque.
          </p>
        </div>

        {/* CARRUSEL COVERFLOW 3D */}
        <div className="relative z-10 flex items-center justify-center min-h-[520px] sm:min-h-[580px] overflow-hidden">
          {/* Botón Flotante Izquierdo */}
          <button
            onClick={prevStory}
            aria-label="Historia anterior"
            className="absolute left-2 sm:left-12 z-30 w-11 h-11 sm:w-12 sm:h-12 rounded-full bg-black/60 border border-white/20 text-white flex items-center justify-center hover:bg-white hover:text-black hover:scale-110 active:scale-95 transition-all shadow-xl backdrop-blur-md"
          >
            <ChevronLeft className="w-6 h-6" />
          </button>

          {/* Tarjetas Coverflow */}
          <div className="relative w-full max-w-4xl h-[480px] sm:h-[540px] flex items-center justify-center">
            {VIDEO_STORIES.map((story, index) => {
              // Calcular posición relativa respecto a activeIndex
              let offset = index - activeIndex;
              if (offset < -1) offset += VIDEO_STORIES.length;
              if (offset > 1) offset -= VIDEO_STORIES.length;

              const isCenter = offset === 0;
              const isLeft = offset === -1;
              const isRight = offset === 1;

              if (!isCenter && !isLeft && !isRight) return null;

              return (
                <motion.div
                  key={story.id}
                  onClick={() => (isCenter ? setSelectedStory(story) : setActiveIndex(index))}
                  initial={false}
                  animate={{
                    x: isCenter ? '0%' : isLeft ? '-78%' : '78%',
                    scale: isCenter ? 1 : 0.86,
                    opacity: isCenter ? 1 : 0.35,
                    zIndex: isCenter ? 20 : 10,
                  }}
                  transition={{ type: 'spring', stiffness: 260, damping: 25 }}
                  className={`absolute w-[280px] sm:w-[320px] h-[460px] sm:h-[520px] rounded-[28px] overflow-hidden cursor-pointer select-none transition-shadow ${
                    isCenter
                      ? 'border border-emerald-400/50 shadow-[0_0_50px_rgba(16,185,129,0.25)] ring-1 ring-emerald-500/30'
                      : 'border border-white/10 hover:opacity-50'
                  }`}
                >
                  {/* Fondo y Gradiente Cinematográfico */}
                  <div
                    className={`w-full h-full bg-gradient-to-b ${story.previewGradient} p-6 flex flex-col justify-between relative`}
                  >
                    {/* Badge y Duración Superior */}
                    <div className="flex items-center justify-between relative z-10">
                      <span className="px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider bg-white/10 text-emerald-300 border border-white/10 backdrop-blur-sm">
                        {story.tag}
                      </span>
                      <div className="flex items-center gap-1 text-[11px] font-mono text-slate-300 bg-black/40 px-2.5 py-0.5 rounded-full border border-white/10">
                        <Clock className="w-3 h-3 text-slate-400" />
                        <span>{story.duration}</span>
                      </div>
                    </div>

                    {/* Botón Central de Play */}
                    <div className="relative z-10 my-auto flex flex-col items-center justify-center">
                      <div className="w-16 h-16 rounded-full bg-white/90 text-[#070A0F] flex items-center justify-center shadow-2xl group-hover:scale-110 transition-transform">
                        <Play className="w-6 h-6 ml-1 fill-current text-[#070A0F]" />
                      </div>
                      <span className="text-[11px] text-white/80 font-medium mt-3 tracking-wide flex items-center gap-1">
                        <Eye className="w-3.5 h-3.5 text-emerald-400" />
                        {story.views}
                      </span>
                    </div>

                    {/* Footer de la tarjeta con Información del Negocio */}
                    <div className="relative z-10 space-y-2">
                      <div className="inline-block px-2.5 py-0.5 rounded bg-emerald-500/20 text-emerald-300 font-bold text-xs">
                        {story.highlightStat}
                      </div>
                      <h4 className="text-sm sm:text-base font-bold text-white leading-snug line-clamp-2">
                        {story.title}
                      </h4>
                      <div className="pt-1 flex items-center justify-between text-[11px] text-slate-300 border-t border-white/10">
                        <span className="font-semibold text-white">{story.businessName}</span>
                        <span className="flex items-center gap-0.5 text-slate-400">
                          <MapPin className="w-3 h-3" />
                          {story.location}
                        </span>
                      </div>
                    </div>
                  </div>
                </motion.div>
              );
            })}
          </div>

          {/* Botón Flotante Derecho */}
          <button
            onClick={nextStory}
            aria-label="Siguiente historia"
            className="absolute right-2 sm:right-12 z-30 w-11 h-11 sm:w-12 sm:h-12 rounded-full bg-black/60 border border-white/20 text-white flex items-center justify-center hover:bg-white hover:text-black hover:scale-110 active:scale-95 transition-all shadow-xl backdrop-blur-md"
          >
            <ChevronRight className="w-6 h-6" />
          </button>
        </div>

        {/* Modal de Reproducción / Caso de Estudio */}
        <AnimatePresence>
          {selectedStory && (
            <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md">
              <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.9 }}
                className="relative w-full max-w-lg bg-[#0F141C] border border-white/15 rounded-3xl p-6 sm:p-8 text-white shadow-2xl"
              >
                <button
                  onClick={() => setSelectedStory(null)}
                  className="absolute top-4 right-4 p-2 rounded-full bg-white/10 hover:bg-white/20 text-white transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>

                <div className="inline-block px-3 py-1 rounded-full text-xs font-semibold bg-emerald-500/20 text-emerald-300 mb-3">
                  {selectedStory.tag}
                </div>

                <h3 className="text-xl font-bold leading-tight mb-2">
                  {selectedStory.title}
                </h3>

                <p className="text-xs text-slate-300 mb-4">
                  {selectedStory.businessName} · {selectedStory.location}
                </p>

                <div className="aspect-video w-full rounded-2xl bg-black/70 border border-white/10 flex flex-col items-center justify-center p-6 text-center my-4">
                  <Play className="w-12 h-12 text-[#8FD694] mb-2" />
                  <p className="text-sm font-semibold text-slate-200">
                    Video Reportaje B2B · Kuota Experiencias
                  </p>
                  <p className="text-xs text-slate-400 mt-1">
                    Duración: {selectedStory.duration} · Resultados auditados por VALT
                  </p>
                </div>

                <div className="mt-4 pt-4 border-t border-white/10 flex items-center justify-between">
                  <span className="text-xs text-emerald-400 font-semibold flex items-center gap-1.5">
                    <CheckCircle2 className="w-4 h-4" />
                    Caso Verificado
                  </span>
                  <button
                    onClick={() => setSelectedStory(null)}
                    className="px-5 py-2 rounded-full text-xs font-bold bg-white text-[#070A0F] hover:bg-[#8FD694] transition-colors"
                  >
                    Entendido
                  </button>
                </div>
              </motion.div>
            </div>
          )}
        </AnimatePresence>
      </div>
    </section>
  );
};

import React, { useState } from 'react';
import { ChevronLeft, ChevronRight, ArrowRight, BookOpen, Clock } from 'lucide-react';
import { BLOG_POSTS, BlogPostItem } from '../../config/fintechData';

export const EditorialBlogCarousel: React.FC = () => {
  const [scrollIndex, setScrollIndex] = useState(0);

  const prevSlide = () => {
    setScrollIndex((prev) => Math.max(0, prev - 1));
  };

  const nextSlide = () => {
    setScrollIndex((prev) => Math.min(BLOG_POSTS.length - 2, prev + 1));
  };

  return (
    <section id="blog" className="relative py-12">
      <div className="max-w-7xl mx-auto mx-3 sm:mx-6 lg:mx-auto rounded-[32px] md:rounded-[48px] bg-white border border-slate-200/80 p-6 sm:p-10 md:p-14 shadow-[0_8px_30px_rgb(0,0,0,0.03)]">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-center">
          {/* COLUMNA IZQUIERDA (4 COLS) */}
          <div className="lg:col-span-4 flex flex-col justify-between space-y-6">
            <div className="space-y-3">
              <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-slate-100 border border-slate-200/60 text-xs font-semibold text-slate-800 tracking-tight">
                <BookOpen className="w-3.5 h-3.5 text-emerald-600" />
                <span>Radar B2B & Publicaciones</span>
              </div>
              <h2 className="text-3xl sm:text-4xl font-extrabold text-[#070A0F] tracking-tight leading-tight">
                Nuestro Blog
              </h2>
              <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                Para que estés al tanto de estrategias de abastecimiento, rotación de inventario, finanzas para PYMES y guías prácticas de IA.
              </p>
            </div>

            {/* Controles de Navegación + CTA */}
            <div className="space-y-5 pt-2">
              <div className="flex items-center gap-3">
                <button
                  onClick={prevSlide}
                  disabled={scrollIndex === 0}
                  aria-label="Artículo anterior"
                  className="w-11 h-11 rounded-full border border-slate-300 text-slate-800 flex items-center justify-center hover:bg-slate-900 hover:text-white disabled:opacity-30 disabled:hover:bg-transparent disabled:hover:text-slate-800 transition-colors"
                >
                  <ChevronLeft className="w-5 h-5" />
                </button>
                <button
                  onClick={nextSlide}
                  disabled={scrollIndex >= BLOG_POSTS.length - 2}
                  aria-label="Siguiente artículo"
                  className="w-11 h-11 rounded-full border border-slate-300 text-slate-800 flex items-center justify-center hover:bg-slate-900 hover:text-white disabled:opacity-30 disabled:hover:bg-transparent disabled:hover:text-slate-800 transition-colors"
                >
                  <ChevronRight className="w-5 h-5" />
                </button>
              </div>

              <a
                href="#blog"
                className="inline-flex items-center justify-center px-7 py-3.5 rounded-full text-xs sm:text-sm font-bold bg-[#0B0D10] text-white hover:bg-[#1E293B] transition-colors"
              >
                <span>Ver más artículos</span>
                <ArrowRight className="w-4 h-4 ml-2" />
              </a>
            </div>
          </div>

          {/* COLUMNA DERECHA (8 COLS): CARRUSEL DE TARJETAS */}
          <div className="lg:col-span-8 overflow-hidden">
            <div
              className="flex gap-6 transition-transform duration-300 ease-out"
              style={{ transform: `translateX(-${scrollIndex * 52}%)` }}
            >
              {BLOG_POSTS.map((post) => (
                <div
                  key={post.id}
                  className="w-[280px] sm:w-[340px] shrink-0 rounded-[28px] overflow-hidden border border-slate-200/70 bg-white hover:shadow-lg transition-all flex flex-col justify-between group"
                >
                  {/* Banner Superior con Gradiente */}
                  <div
                    className={`h-40 w-full bg-gradient-to-br ${post.gradient} p-5 flex flex-col justify-between relative overflow-hidden`}
                  >
                    <div className="absolute -right-6 -bottom-6 w-28 h-28 bg-emerald-500/20 rounded-full blur-2xl pointer-events-none" />
                    <span className="relative z-10 w-fit px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider bg-white/90 backdrop-blur text-slate-900">
                      {post.category}
                    </span>
                    <span className="relative z-10 text-[11px] font-mono text-emerald-300">
                      Editorial Kuota
                    </span>
                  </div>

                  {/* Cuerpo */}
                  <div className="p-6 flex-1 flex flex-col justify-between">
                    <div>
                      <h3 className="text-base font-bold text-slate-900 leading-snug group-hover:text-emerald-700 transition-colors line-clamp-2">
                        {post.title}
                      </h3>
                      <p className="text-xs text-slate-600 mt-2 line-clamp-3 leading-relaxed">
                        {post.excerpt}
                      </p>
                    </div>

                    <div className="mt-5 pt-3 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-500">
                      <span className="flex items-center gap-1">
                        <Clock className="w-3 h-3 text-slate-400" />
                        {post.readTime}
                      </span>
                      <span className="font-semibold text-slate-700">{post.updatedYear}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

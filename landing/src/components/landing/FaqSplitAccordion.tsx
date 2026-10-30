'use client';

import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronDown, HelpCircle, MessageSquare } from 'lucide-react';
import { FAQS_LIST, FaqItem } from '../../config/fintechData';

export const FaqSplitAccordion: React.FC = () => {
  const [openFaqId, setOpenFaqId] = useState<string | null>('faq-1');

  const toggleFaq = (id: string) => {
    setOpenFaqId((prev) => (prev === id ? null : id));
  };

  return (
    <section id="faq" className="relative py-16">
      <div className="max-w-7xl mx-auto mx-3 sm:mx-6 lg:mx-auto rounded-[32px] md:rounded-[48px] bg-white border border-slate-200/80 p-6 sm:p-10 md:p-16 shadow-[0_8px_30px_rgb(0,0,0,0.03)]">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-14 items-start">
          {/* COLUMNA IZQUIERDA (STICKY 4 COLS) */}
          <div className="lg:col-span-4 lg:sticky lg:top-28 space-y-6">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-slate-100 border border-slate-200/60 text-xs font-semibold text-slate-800 tracking-tight">
              <HelpCircle className="w-3.5 h-3.5 text-emerald-600" />
              <span>Dudas Resueltas</span>
            </div>

            <h2 className="text-3xl sm:text-4xl font-extrabold text-[#070A0F] tracking-tight leading-tight">
              Preguntas frecuentes
            </h2>

            <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
              Preguntar está bien. Querer cuidar cada sol de tu inventario, también. Por eso hicimos este espacio: para explicarte Kuota como nos gusta hacer los negocios... simple, transparente y sin letras chicas.
            </p>

            <div className="pt-2">
              <a
                href="https://wa.me/51981000000?text=Hola%20Kuota,%20tengo%20dudas%20sobre%20c%C3%B3mo%20financiar%20mi%20inventario"
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center justify-center px-6 py-3 rounded-full text-xs font-bold bg-[#10B981] hover:bg-[#059669] text-white transition-colors shadow-sm gap-2"
              >
                <MessageSquare className="w-4 h-4" />
                <span>Hablar con un asesor</span>
              </a>
            </div>
          </div>

          {/* COLUMNA DERECHA (8 COLS): ACORDEONES LIMPIOS */}
          <div className="lg:col-span-8 space-y-3">
            {FAQS_LIST.map((faq) => {
              const isOpen = openFaqId === faq.id;
              return (
                <div
                  key={faq.id}
                  onClick={() => toggleFaq(faq.id)}
                  className={`rounded-2xl border transition-all cursor-pointer p-5 ${
                    isOpen
                      ? 'bg-slate-50 border-emerald-500/40 shadow-xs'
                      : 'bg-white border-slate-200/80 hover:border-slate-300'
                  }`}
                >
                  <div className="flex items-center justify-between gap-4">
                    <h3 className="text-sm sm:text-base font-bold text-slate-900 leading-snug">
                      {faq.question}
                    </h3>
                    <div
                      className={`w-7 h-7 rounded-full bg-[#0B0D10] text-white flex items-center justify-center shrink-0 transition-transform duration-200 ${
                        isOpen ? 'rotate-180 bg-emerald-600' : ''
                      }`}
                    >
                      <ChevronDown className="w-4 h-4" />
                    </div>
                  </div>

                  <AnimatePresence>
                    {isOpen && (
                      <motion.div
                        initial={{ opacity: 0, height: 0 }}
                        animate={{ opacity: 1, height: 'auto' }}
                        exit={{ opacity: 0, height: 0 }}
                        transition={{ duration: 0.2 }}
                        className="overflow-hidden"
                      >
                        <p className="text-xs sm:text-sm text-slate-600 mt-3.5 pt-3 border-t border-slate-200/60 leading-relaxed">
                          {faq.answer}
                        </p>
                      </motion.div>
                    )}
                  </AnimatePresence>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
};

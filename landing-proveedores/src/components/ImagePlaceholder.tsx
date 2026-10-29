import React from 'react';
import { Image as ImageIcon } from 'lucide-react';

interface ImagePlaceholderProps {
  id: string;
  label: string;
  dimensionsLabel: string;
  aspectClass?: string;
  className?: string;
  children?: React.ReactNode;
}

/**
 * PRD §3, §10 y §11:
 * Todas las imágenes, capturas e ilustraciones se dejan como placeholders
 * con su identificador (IMG-01 .. IMG-21) y tamaño sugerido.
 */
export const ImagePlaceholder: React.FC<ImagePlaceholderProps> = ({
  id,
  label,
  dimensionsLabel,
  aspectClass = 'aspect-[4/3]',
  className = '',
  children,
}) => {
  return (
    <div
      role="img"
      aria-label={`${id}: ${label}`}
      className={`relative overflow-hidden rounded-2xl border-2 border-kuota-deepnavy bg-white/90 placeholder-stripes flex flex-col justify-between p-4 sm:p-5 shadow-navy-hard ${aspectClass} ${className}`}
    >
      {/* Cabecera de metadatos del placeholder */}
      <div className="flex items-center justify-between gap-2 z-10">
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-kuota-deepnavy text-white text-xs font-bold tracking-wide uppercase">
          <ImageIcon className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
          Placeholder · {id}
        </span>
        <span className="inline-flex items-center px-2.5 py-1 rounded-md bg-kuota-palesky border border-kuota-deepnavy text-kuota-deepnavy text-xs font-extrabold">
          {dimensionsLabel}
        </span>
      </div>

      {/* Contenido esquemático central */}
      <div className="my-auto py-3 z-10 flex flex-col items-center justify-center text-center w-full">
        {children ? (
          children
        ) : (
          <div className="w-12 h-12 rounded-2xl bg-kuota-palesky border-2 border-kuota-deepnavy flex items-center justify-center mb-2">
            <ImageIcon className="w-6 h-6 text-kuota-deepnavy" aria-hidden="true" />
          </div>
        )}
      </div>

      {/* Etiqueta descriptiva en la base */}
      <div className="z-10 bg-kuota-palesky/95 border border-kuota-deepnavy rounded-xl px-3.5 py-2 text-left">
        <p className="text-xs sm:text-sm font-bold text-kuota-deepnavy leading-snug">
          {label}
        </p>
      </div>
    </div>
  );
};

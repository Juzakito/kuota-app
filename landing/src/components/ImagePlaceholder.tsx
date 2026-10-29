import React from 'react';
import { Image as ImageIcon } from 'lucide-react';

interface ImagePlaceholderProps {
  id: string;
  label: string;
  aspectRatioLabel: string;
  aspectClass?: string;
  className?: string;
  children?: React.ReactNode;
}

/**
 * PRD §4: Todas las imágenes se entregan como placeholders
 * (caja con etiqueta y proporción definida); el equipo las carga después.
 */
export const ImagePlaceholder: React.FC<ImagePlaceholderProps> = ({
  id,
  label,
  aspectRatioLabel,
  aspectClass = 'aspect-[4/3]',
  className = '',
  children,
}) => {
  return (
    <div
      role="img"
      aria-label={label}
      className={`relative overflow-hidden rounded-2xl border-2 border-kuota-deepnavy bg-white/85 placeholder-stripes flex flex-col justify-between p-4 sm:p-5 shadow-navy-hard ${aspectClass} ${className}`}
    >
      {/* Cabecera de metadatos del placeholder */}
      <div className="flex items-center justify-between gap-2 z-10">
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-kuota-deepnavy text-white text-xs font-bold tracking-wide uppercase">
          <ImageIcon className="w-3.5 h-3.5 text-kuota-dodger" aria-hidden="true" />
          Placeholder · {id}
        </span>
        <span className="inline-flex items-center px-2.5 py-1 rounded-md bg-kuota-palesky border border-kuota-deepnavy text-kuota-deepnavy text-xs font-extrabold">
          Proporción {aspectRatioLabel}
        </span>
      </div>

      {/* Contenido central opcional (wireframe esquemático o ícono) */}
      <div className="my-auto py-3 z-10 flex flex-col items-center justify-center text-center">
        {children ? (
          children
        ) : (
          <div className="w-12 h-12 rounded-2xl bg-kuota-palesky border-2 border-kuota-deepnavy flex items-center justify-center mb-2">
            <ImageIcon className="w-6 h-6 text-kuota-deepnavy" aria-hidden="true" />
          </div>
        )}
      </div>

      {/* Etiqueta descriptiva en la base */}
      <div className="z-10 bg-kuota-palesky/95 border border-kuota-deepnavy rounded-xl px-3.5 py-2.5 text-left">
        <p className="text-xs sm:text-sm font-bold text-kuota-deepnavy leading-snug">
          {label}
        </p>
      </div>
    </div>
  );
};

import React from 'react';
import { Image as ImageIcon } from 'lucide-react';

interface ImagePlaceholderProps {
  id: string;
  label: string;
  aspectRatioLabel?: string;
  dimensionsLabel?: string;
  aspectClass?: string;
  className?: string;
  children?: React.ReactNode;
}

/**
 * Placeholder unificado para Compradores (proporción) y Proveedores (dimensiones IMG-01..IMG-21).
 */
export const ImagePlaceholder: React.FC<ImagePlaceholderProps> = ({
  id,
  label,
  aspectRatioLabel,
  dimensionsLabel,
  aspectClass = 'aspect-[4/3]',
  className = '',
  children,
}) => {
  const badgeRight = dimensionsLabel
    ? dimensionsLabel
    : aspectRatioLabel
      ? `Proporción ${aspectRatioLabel}`
      : '';

  return (
    <div
      role="img"
      aria-label={`${id}: ${label}`}
      className={`relative overflow-hidden rounded-[24px] border border-slate-200/80 bg-white/95 placeholder-stripes flex flex-col justify-between p-4 sm:p-5 shadow-lg ${aspectClass} ${className}`}
    >
      {/* Cabecera de metadatos del placeholder */}
      <div className="flex items-center justify-between gap-2 z-10">
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-[#0A192F] text-white text-xs font-bold tracking-wide uppercase">
          <ImageIcon className="w-3.5 h-3.5 text-[#0391FD]" aria-hidden="true" />
          Placeholder · {id}
        </span>
        {badgeRight && (
          <span className="inline-flex items-center px-2.5 py-1 rounded-full bg-slate-100 border border-slate-200/80 text-slate-700 text-xs font-bold">
            {badgeRight}
          </span>
        )}
      </div>

      {/* Contenido esquemático central */}
      <div className="my-auto py-3 z-10 flex flex-col items-center justify-center text-center w-full">
        {children ? (
          children
        ) : (
          <div className="w-12 h-12 rounded-2xl bg-sky-50 border border-sky-200 flex items-center justify-center mb-2">
            <ImageIcon className="w-6 h-6 text-[#0391FD]" aria-hidden="true" />
          </div>
        )}
      </div>

      {/* Etiqueta descriptiva en la base */}
      <div className="z-10 bg-slate-50/95 border border-slate-200/80 rounded-2xl px-3.5 py-2.5 text-left">
        <p className="text-xs sm:text-sm font-semibold text-slate-800 leading-snug">
          {label}
        </p>
      </div>
    </div>
  );
};

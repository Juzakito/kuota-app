import React from 'react';
import { PLAY_STORE_URL } from '../data/landingData';
import { trackEvent } from '../utils/analytics';
import { kuotaSound } from '../utils/soundSystem';

interface PlayStoreButtonProps {
  section: 'navbar' | 'hero' | 'cta_final';
  label?: string;
  size?: 'sm' | 'md' | 'lg';
  variant?: 'primary' | 'inverted';
  className?: string;
}

export const PlayStoreButton: React.FC<PlayStoreButtonProps> = ({
  section,
  label = 'Descargar en Play Store',
  size = 'md',
  variant = 'primary',
  className = '',
}) => {
  const sizeStyles = {
    sm: 'px-4 py-2 text-sm gap-2 rounded-xl',
    md: 'px-6 py-3.5 text-base gap-2.5 rounded-2xl',
    lg: 'px-7 py-4 text-base sm:text-lg gap-3 rounded-2xl',
  }[size];

  const variantStyles =
    variant === 'primary'
      ? 'bg-kuota-dodger hover:bg-kuota-crayola text-white border-2 border-kuota-deepnavy shadow-navy-hard hover:-translate-y-0.5 active:translate-y-0.5 active:scale-[0.98]'
      : 'bg-kuota-dodger hover:bg-kuota-crayola text-white border-2 border-kuota-palesky shadow-dodger-glow hover:-translate-y-0.5 active:translate-y-0.5 active:scale-[0.98]';

  const handleClick = () => {
    kuotaSound.playCtaClick();
    trackEvent('cta_download_click', {
      section,
      destination: PLAY_STORE_URL,
      button_label: label,
    });
  };

  return (
    <a
      href={PLAY_STORE_URL}
      target="_blank"
      rel="noopener noreferrer"
      onClick={handleClick}
      data-analytics-section={section}
      className={`group inline-flex items-center justify-center font-extrabold tracking-tight transition-all duration-150 ${sizeStyles} ${variantStyles} ${className}`}
    >
      {/* Ícono de Google Play con microanimación sutil */}
      <svg
        className={`${
          size === 'sm' ? 'w-4 h-4' : 'w-5 h-5'
        } shrink-0 transition-transform duration-150 group-hover:scale-110`}
        viewBox="0 0 24 24"
        fill="currentColor"
        aria-hidden="true"
      >
        <path d="M3.609 1.814L13.792 12 3.61 22.186a1.96 1.96 0 0 1-.61-1.425V3.239c0-.55.23-1.055.609-1.425zm10.89 10.893l2.302 2.302-10.937 6.333 8.635-8.635zm3.199-3.198l2.807 1.626a1 1 0 0 1 0 1.73l-2.808 1.626L15.206 12l2.492-2.491zM5.864 2.658L16.802 8.99l-2.303 2.303-8.635-8.635z" />
      </svg>
      <span>{label}</span>
    </a>
  );
};

import React from 'react';

interface KuotaLogoProps {
  size?: 'sm' | 'md' | 'lg';
  variant?: 'default' | 'inverted';
  className?: string;
}

export const KuotaLogo: React.FC<KuotaLogoProps> = ({
  size = 'md',
  variant = 'default',
  className = '',
}) => {
  const heightClasses = {
    sm: 'h-7 sm:h-8',
    md: 'h-8 sm:h-9',
    lg: 'h-10 sm:h-11',
  }[size];

  const iconSizes = {
    sm: 'w-7 h-7 sm:w-8 sm:h-8',
    md: 'w-8 h-8 sm:w-9 sm:h-9',
    lg: 'w-10 h-10 sm:w-11 sm:h-11',
  }[size];

  const wordmarkHeights = {
    sm: 'h-5 sm:h-5',
    md: 'h-5 sm:h-6',
    lg: 'h-6 sm:h-7',
  }[size];

  if (variant === 'inverted') {
    return (
      <div className={`inline-flex items-center gap-2.5 select-none ${className}`}>
        <img
          src="/logos/kuota-icon.png"
          alt=""
          aria-hidden="true"
          className={`${iconSizes} object-contain shrink-0 drop-shadow-sm`}
        />
        <img
          src="/logos/kuota-wordmark-white.png"
          alt="Kuota"
          className={`${wordmarkHeights} w-auto object-contain`}
        />
      </div>
    );
  }

  return (
    <div className={`inline-flex items-center select-none ${className}`}>
      <img
        src="/logos/kuota-logo-horizontal.png"
        alt="Kuota"
        className={`${heightClasses} w-auto object-contain`}
      />
    </div>
  );
};

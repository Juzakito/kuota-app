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
  const horizontalSizes = {
    sm: 'h-7 w-[93px] sm:h-8 sm:w-[106px]',
    md: 'h-8 w-[106px] sm:h-9 sm:w-[120px]',
    lg: 'h-10 w-[133px] sm:h-11 sm:w-[146px]',
  }[size];

  const iconSizes = {
    sm: 'w-7 h-7 sm:w-8 sm:h-8',
    md: 'w-8 h-8 sm:w-9 sm:h-9',
    lg: 'w-10 h-10 sm:w-11 sm:h-11',
  }[size];

  const wordmarkSizes = {
    sm: 'h-5 w-[57px] sm:h-5 sm:w-[57px]',
    md: 'h-5 w-[57px] sm:h-6 sm:w-[68px]',
    lg: 'h-6 w-[68px] sm:h-7 sm:w-[80px]',
  }[size];

  if (variant === 'inverted') {
    return (
      <div className={`inline-flex items-center gap-2.5 select-none shrink-0 ${className}`}>
        <img
          src="/logos/kuota-icon.png"
          alt=""
          width={528}
          height={528}
          aria-hidden="true"
          className={`${iconSizes} object-contain shrink-0 block drop-shadow-sm`}
        />
        <img
          src="/logos/kuota-wordmark-white.png"
          alt="Kuota"
          width={318}
          height={112}
          className={`${wordmarkSizes} object-contain shrink-0 block`}
        />
      </div>
    );
  }

  return (
    <div className={`inline-flex items-center select-none shrink-0 ${className}`}>
      <img
        src="/logos/kuota-logo-horizontal.png"
        alt="Kuota"
        width={545}
        height={164}
        className={`${horizontalSizes} object-contain shrink-0 block`}
      />
    </div>
  );
};

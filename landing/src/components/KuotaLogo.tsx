import React from 'react';

interface KuotaLogoProps {
  size?: 'sm' | 'md' | 'lg';
  variant?: 'default' | 'dark' | 'inverted' | 'light';
  className?: string;
}

export const KuotaLogo: React.FC<KuotaLogoProps> = ({
  size = 'md',
  variant = 'default',
  className = '',
}) => {
  const horizontalSizes = {
    sm: 'h-7 w-auto max-w-[120px]',
    md: 'h-8 w-auto max-w-[140px]',
    lg: 'h-10 w-auto max-w-[170px]',
  }[size];

  const iconSizes = {
    sm: 'w-7 h-7',
    md: 'w-8 h-8',
    lg: 'w-10 h-10',
  }[size];

  if (variant === 'inverted' || variant === 'light') {
    return (
      <div className={`inline-flex items-center gap-2 select-none shrink-0 ${className}`}>
        <img
          src="/logos/kuota-icon.png"
          alt=""
          aria-hidden="true"
          className={`${iconSizes} object-contain shrink-0 block`}
        />
        <span className="font-sans font-black text-xl tracking-tight text-paper">
          KUOTA
        </span>
      </div>
    );
  }

  return (
    <div className={`inline-flex items-center gap-2 select-none shrink-0 ${className}`}>
      <img
        src="/logos/kuota-icon.png"
        alt=""
        aria-hidden="true"
        className={`${iconSizes} object-contain shrink-0 block`}
      />
      <span className="font-sans font-black text-xl tracking-tight text-forest">
        KUOTA
      </span>
    </div>
  );
};

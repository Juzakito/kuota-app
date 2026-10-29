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
  const dimensions = {
    sm: { box: 'w-8 h-8 rounded-lg', text: 'text-xl' },
    md: { box: 'w-9 h-9 rounded-xl', text: 'text-2xl' },
    lg: { box: 'w-11 h-11 rounded-2xl', text: 'text-3xl' },
  }[size];

  const isInverted = variant === 'inverted';

  return (
    <div className={`inline-flex items-center gap-2.5 select-none ${className}`}>
      <div
        className={`${dimensions.box} flex items-center justify-center border-2 ${
          isInverted
            ? 'bg-kuota-dodger border-kuota-palesky'
            : 'bg-kuota-deepnavy border-kuota-deepnavy'
        } shadow-sm`}
        aria-hidden="true"
      >
        <svg
          viewBox="0 0 32 32"
          className="w-5 h-5"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M8 7H12.5V14.5L19.5 7H25L16.8 15.5L25.5 25H19.8L12.5 17V25H8V7Z"
            fill={isInverted ? '#FFFFFF' : '#0391FD'}
          />
          <circle
            cx="23.5"
            cy="8.5"
            r="2.5"
            fill="#CBE5FE"
          />
        </svg>
      </div>
      <span
        className={`${dimensions.text} font-extrabold tracking-tight ${
          isInverted ? 'text-white' : 'text-kuota-deepnavy'
        }`}
      >
        kuota
      </span>
    </div>
  );
};

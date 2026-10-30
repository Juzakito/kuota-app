import React from 'react';
import { CalendarCheck } from 'lucide-react';
import { trackSupplierEvent } from '../../utils/analytics';
import { smoothScrollToAnchor } from '../../utils/motionSystem';
import { kuotaSound } from '../../utils/soundSystem';

interface ScheduleCallButtonProps {
  section: string;
  label?: string;
  size?: 'sm' | 'md' | 'lg';
  variant?: 'primary' | 'navy';
  className?: string;
  onClickExtra?: () => void;
}

/**
 * PRD §4: Todos los botones "Agenda una call" llevan por ancla a la sección 9 (#agendar-call).
 * PRD §2: Registra el clic con su sección de origen.
 */
export const ScheduleCallButton: React.FC<ScheduleCallButtonProps> = ({
  section,
  label = 'Agenda una call',
  size = 'md',
  variant = 'primary',
  className = '',
  onClickExtra,
}) => {
  const sizeStyles = {
    sm: 'px-4 py-2 text-xs sm:text-sm gap-2 rounded-full',
    md: 'px-6 py-3 text-sm sm:text-base gap-2.5 rounded-full',
    lg: 'px-7 py-3.5 text-base sm:text-lg gap-3 rounded-full',
  }[size];

  const variantStyles =
    variant === 'primary'
      ? 'bg-[#0391FD] hover:bg-[#0277D4] text-white shadow-md hover:shadow-lg hover:-translate-y-0.5 active:translate-y-0 active:scale-[0.98]'
      : 'bg-[#0A192F] hover:bg-[#0391FD] text-white border border-white/15 shadow-md hover:shadow-lg hover:-translate-y-0.5 active:translate-y-0 active:scale-[0.98]';

  const handleClick = (e: React.MouseEvent<HTMLAnchorElement>) => {
    e.preventDefault();
    kuotaSound.playCtaClick();
    trackSupplierEvent('cta_schedule_call_click', {
      section,
      button_label: label,
      target_anchor: '#agendar-call',
    });
    if (onClickExtra) {
      onClickExtra();
    }
    smoothScrollToAnchor('#agendar-call');
  };

  return (
    <a
      href="#agendar-call"
      onClick={handleClick}
      data-analytics-section={section}
      className={`inline-flex items-center justify-center font-extrabold tracking-tight transition-all duration-150 ${sizeStyles} ${variantStyles} ${className}`}
    >
      <CalendarCheck
        className={size === 'sm' ? 'w-4 h-4 shrink-0' : 'w-5 h-5 shrink-0'}
        aria-hidden="true"
      />
      <span>{label}</span>
    </a>
  );
};


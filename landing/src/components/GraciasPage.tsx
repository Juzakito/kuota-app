import React, { useEffect } from 'react';
import { CheckCircle, Calendar, MessageSquare, ArrowLeft, ShieldCheck, Mail } from 'lucide-react';
import { OFFICIAL_CONTACT } from '../utils/leadsService';
import { trackEvent } from '../utils/analytics';

export interface GraciasPageProps {
  onGoHome?: () => void;
}

export const GraciasPage: React.FC<GraciasPageProps> = ({ onGoHome }) => {
  useEffect(() => {
    trackEvent('view_gracias_page');
  }, []);

  const handleBack = () => {
    if (onGoHome) {
      onGoHome();
    } else if (typeof window !== 'undefined') {
      window.location.href = '/';
    }
  };

  return (
    <div className="min-h-[80vh] flex flex-col justify-center items-center py-12 px-4">
      <div className="max-w-2xl w-full bg-paper border border-fog rounded-hero p-8 md:p-12 shadow-card text-center space-y-6">
        {/* Badge de Confirmación */}
        <div className="w-16 h-16 bg-linen text-forest rounded-pill mx-auto flex items-center justify-center">
          <CheckCircle className="w-10 h-10 text-forest" />
        </div>

        <div className="space-y-2">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-pill bg-linen text-forest text-xs font-semibold">
            <ShieldCheck className="w-3.5 h-3.5" />
            <span>Registro oficial recibido</span>
          </div>
          <h1 className="text-3xl md:text-4xl font-black text-obsidian tracking-tight">
            ¡GRACIAS POR CONFIAR EN KUOTA!
          </h1>
          <p className="text-base text-charcoal max-w-lg mx-auto">
            Hemos recibido tus datos con éxito. Te mantendremos al tanto con novedades reales y verificadas de nuestro lanzamiento progresivo.
          </p>
        </div>

        {/* Qué esperar (PRD §5) */}
        <div className="bg-fog/60 border border-fog rounded-card p-6 text-left space-y-4">
          <h2 className="text-xs font-bold uppercase tracking-wider text-charcoal/80">
            ¿Qué pasará ahora?
          </h2>
          <div className="space-y-3 text-xs md:text-sm text-charcoal">
            <div className="flex items-start gap-3">
              <span className="w-6 h-6 rounded-pill bg-forest text-lime flex items-center justify-center text-xs font-bold shrink-0 mt-0.5">
                1
              </span>
              <div>
                <strong>Lanzamiento progresivo:</strong> Iremos habilitando accesos por distritos y rubros comerciales para garantizar la máxima estabilidad y atención personalizada.
              </div>
            </div>
            <div className="flex items-start gap-3">
              <span className="w-6 h-6 rounded-pill bg-forest text-lime flex items-center justify-center text-xs font-bold shrink-0 mt-0.5">
                2
              </span>
              <div>
                <strong>Sin falsas expectativas:</strong> No enviamos spam ni fechas ficticias. Te contactaremos por WhatsApp oficial solo cuando tu zona esté activa para compras o cobros.
              </div>
            </div>
            <div className="flex items-start gap-3">
              <span className="w-6 h-6 rounded-pill bg-forest text-lime flex items-center justify-center text-xs font-bold shrink-0 mt-0.5">
                3
              </span>
              <div>
                <strong>Evaluación transparente:</strong> Toda línea de crédito se asigna en base a datos reales del negocio, comenzando con montos prudentes para cuidar tu liquidez.
              </div>
            </div>
          </div>
        </div>

        {/* Sección Calendly para Proveedores */}
        <div className="pt-2 border-t border-fog space-y-4">
          <div className="text-xs font-bold uppercase tracking-wider text-forest">
            ¿Eres proveedor o distribuidor mayorista?
          </div>
          <p className="text-xs text-charcoal max-w-md mx-auto">
            Si deseas implementar Kuota Payments (2%) o afiliar tu comercio antes del despliegue masivo, agenda una reunión de 30 minutos con nuestro equipo comercial.
          </p>
          <div className="flex flex-col sm:flex-row justify-center gap-3">
            <a
              href={OFFICIAL_CONTACT.calendlyUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center justify-center gap-2 py-3 px-6 rounded-pill bg-forest text-lime font-bold text-xs md:text-sm hover:bg-forest-dark transition-all shadow-sm"
            >
              <Calendar className="w-4 h-4" />
              <span>Agendar reunión de 30 min</span>
            </a>
            <a
              href={`https://wa.me/${OFFICIAL_CONTACT.whatsappNumber}?text=${encodeURIComponent('Hola Kuota, me acabo de registrar en la waitlist y tengo una consulta.')}`}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center justify-center gap-2 py-3 px-6 rounded-pill bg-paper border border-forest text-forest font-bold text-xs md:text-sm hover:bg-fog transition-all"
            >
              <MessageSquare className="w-4 h-4" />
              <span>Escribir por WhatsApp</span>
            </a>
          </div>
        </div>

        {/* Enlace de baja y política de respeto (PRD §5) */}
        <div className="pt-4 border-t border-fog text-[11px] text-pebble space-y-1">
          <p>
            Tratamiento seguro conforme a la Ley N° 29733 de Protección de Datos Personales del Perú.
          </p>
          <p>
            Si en algún momento prefieres que no te escribamos, simplemente respóndenos <strong>"BAJA"</strong> por WhatsApp o escribe a{' '}
            <a href={`mailto:${OFFICIAL_CONTACT.email}`} className="underline text-charcoal">
              {OFFICIAL_CONTACT.email}
            </a>{' '}
            y te retiraremos de inmediato sin preguntas.
          </p>
        </div>

        {/* Volver al inicio */}
        <div className="pt-2">
          <button
            type="button"
            onClick={handleBack}
            className="inline-flex items-center gap-2 text-xs font-semibold text-charcoal hover:text-forest transition-colors cursor-pointer"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Volver a la portada principal</span>
          </button>
        </div>
      </div>
    </div>
  );
};

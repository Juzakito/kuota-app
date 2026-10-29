/**
 * PRD §1, §5.6, §6 y §7:
 * Archivo único de parametrización de comisiones, plazos, planes y cal.com.
 * Cambiar `kuotaPaymentsFeePercent` aquí actualiza automáticamente todos los textos,
 * tarjetas, comparativas y la calculadora de ahorro sin tocar los copys.
 */

export const PRICING_CONFIG = {
  /** Comisión de la solución de payments (Kuota QR y Kuota Link) — PRD §1 y §7 */
  kuotaPaymentsFeePercent: 2,
  /** Comisión promedio de un POS tradicional de referencia — PRD §5.6 y §7 */
  posAverageFeePercent: 3.5,
  /** Comisión de aKuotas! en el plan Open — PRD §5.8 y §7 */
  akuotasOpenFeePercent: 5,
  /** Tiempo de liquidación en horas hábiles — PRD §7 */
  settlementBusinessHours: 48,
  /** Riesgo de impago para el proveedor — PRD §7 */
  supplierDefaultRisk: 0,
  /** Venta mensual por defecto sugerida en la calculadora (en soles) — PRD §5.6 */
  calculatorDefaultMonthlySalesSoles: 20000,
  calculatorMinMonthlySalesSoles: 1000,
  calculatorMaxMonthlySalesSoles: 200000,
  calculatorStepSoles: 1000,
  calculatorDefaultAverageTicketSoles: 250,
} as const;

/** Formatea un porcentaje usando coma decimal según el estilo del PRD (ej. 3.5 -> "3,5%", 2 -> "2%") */
export function formatPercentEs(value: number): string {
  return `${String(value).replace('.', ',')}%`;
}

/** Formatea montos en soles peruanos (ej. 20000 -> "S/ 20,000") */
export function formatSoles(amount: number): string {
  return `S/ ${Math.round(amount).toLocaleString('en-US')}`;
}

export interface SocialProofLogoItem {
  id: string;
  placeholderId: string;
  name: string;
  category: 'Banco' | 'Billetera';
  dimensions: string;
  logoUrl: string;
}

/** PRD §5.4 y §11: IMG-04 a IMG-09 (160×60 cada uno) */
export const SOCIAL_PROOF_LOGOS: SocialProofLogoItem[] = [
  { id: 'bcp', placeholderId: 'IMG-04', name: 'BCP', category: 'Banco', dimensions: '160×60', logoUrl: '/logos/bcp.svg' },
  { id: 'bbva', placeholderId: 'IMG-05', name: 'BBVA', category: 'Banco', dimensions: '160×60', logoUrl: '/logos/bbva.svg' },
  { id: 'interbank', placeholderId: 'IMG-06', name: 'Interbank', category: 'Banco', dimensions: '160×60', logoUrl: '/logos/interbank.svg' },
  { id: 'scotiabank', placeholderId: 'IMG-07', name: 'Scotiabank', category: 'Banco', dimensions: '160×60', logoUrl: '/logos/scotiabank.svg' },
  { id: 'yape', placeholderId: 'IMG-08', name: 'Yape', category: 'Billetera', dimensions: '160×60', logoUrl: '/logos/yape.svg' },
  { id: 'plin', placeholderId: 'IMG-09', name: 'Plin', category: 'Billetera', dimensions: '160×60', logoUrl: '/logos/plin.png' },
];

export interface ProductCaptureSpec {
  placeholderId: string;
  title: string;
  description: string;
  dimensions: string;
  mockHeader: string;
  mockHighlight: string;
  mockRows: { label: string; value: string }[];
}

export interface SupplierProductChipItem {
  id: string;
  index: number;
  title: string;
  badge?: string;
  description: string;
  captures: ProductCaptureSpec[];
}

/** PRD §5.5 y §11: Tres chips de producto con capturas IMG-10 a IMG-16 (390×800 cada una) */
export const SUPPLIER_PRODUCT_CHIPS: SupplierProductChipItem[] = [
  {
    id: 'kuota-qr',
    index: 1,
    title: 'Kuota QR',
    description: 'Digitas el monto, tu cliente escanea y paga, y listo.',
    captures: [
      {
        placeholderId: 'IMG-10',
        title: 'Pantalla del proveedor',
        description: 'Pantalla del proveedor con el monto digitado y el QR generado',
        dimensions: '390×800',
        mockHeader: 'Cobrar con Kuota QR',
        mockHighlight: 'S/ 450.00 · QR Generado',
        mockRows: [
          { label: 'Comisión aplicable', value: `${formatPercentEs(PRICING_CONFIG.kuotaPaymentsFeePercent)} por transacción` },
          { label: 'Estado del código', value: 'Listo para escanear' },
        ],
      },
      {
        placeholderId: 'IMG-11',
        title: 'Escaneo del cliente',
        description: 'El cliente escaneando y pagando',
        dimensions: '390×800',
        mockHeader: 'Pago del cliente',
        mockHighlight: 'Escaneo y confirmación exitosa ✓',
        mockRows: [
          { label: 'Medio utilizado', value: 'Tarjeta / Billetera 360 Pay' },
          { label: 'Liquidación al proveedor', value: `${PRICING_CONFIG.settlementBusinessHours} horas hábiles` },
        ],
      },
    ],
  },
  {
    id: 'kuota-link',
    index: 2,
    title: 'Kuota Link',
    description:
      'Digitas el monto, el número de teléfono y el DNI. Tu cliente recibe el link por WhatsApp para pagar de forma segura desde la app de su banco.',
    captures: [
      {
        placeholderId: 'IMG-12',
        title: 'Formulario Kuota Link',
        description: 'Formulario con monto, teléfono y DNI',
        dimensions: '390×800',
        mockHeader: 'Generar Kuota Link',
        mockHighlight: 'Monto + Teléfono + DNI',
        mockRows: [
          { label: 'Monto a cobrar', value: 'S/ 1,200.00' },
          { label: 'Celular / DNI cliente', value: '9•• ••• ••• · DNI 4•••••••' },
        ],
      },
      {
        placeholderId: 'IMG-13',
        title: 'WhatsApp y App del Banco',
        description: 'Mensaje de WhatsApp con el link y pantalla de pago en la app del banco',
        dimensions: '390×800',
        mockHeader: 'Envío por WhatsApp',
        mockHighlight: 'Link seguro abierto en app bancaria ✓',
        mockRows: [
          { label: 'Canal de envío', value: 'WhatsApp directo al cliente' },
          { label: 'Seguridad', value: 'Validación bancaria 360 Pay' },
        ],
      },
    ],
  },
  {
    id: 'akuotas',
    index: 3,
    title: 'aKuotas! (para emprendedores)',
    badge: `${PRICING_CONFIG.supplierDefaultRisk} riesgo`,
    description:
      'Tu cliente escanea el QR, elige su opción de financiamiento y tú confirmas haber recibido la cuota inicial. Listo, te liquidamos el monto faltante en 48 horas. 0 riesgo.',
    captures: [
      {
        placeholderId: 'IMG-14',
        title: 'Paso 1: Cliente elige financiamiento',
        description: 'El cliente elige su opción de financiamiento',
        dimensions: '390×800',
        mockHeader: '1. Selección de kuotas',
        mockHighlight: 'Cliente elige su plan de kuotas',
        mockRows: [
          { label: 'Compra total', value: 'S/ 2,000.00' },
          { label: 'Modalidad', value: 'Financiamiento aKuotas!' },
        ],
      },
      {
        placeholderId: 'IMG-15',
        title: 'Paso 2: Confirmas cuota inicial',
        description: 'El proveedor confirma haber recibido la cuota inicial',
        dimensions: '390×800',
        mockHeader: '2. Confirmación del proveedor',
        mockHighlight: 'Cuota inicial recibida ✓',
        mockRows: [
          { label: 'Acción del proveedor', value: 'Confirmar recepción en 1 toque' },
          { label: 'Riesgo de impago', value: `${PRICING_CONFIG.supplierDefaultRisk} riesgo para tu negocio` },
        ],
      },
      {
        placeholderId: 'IMG-16',
        title: 'Paso 3: Liquidación en 48h',
        description: 'Pantalla final de liquidación en 48 horas',
        dimensions: '390×800',
        mockHeader: '3. Liquidación garantizada',
        mockHighlight: `Monto faltante en ${PRICING_CONFIG.settlementBusinessHours}h hábiles`,
        mockRows: [
          { label: 'Abono del saldo', value: `En ${PRICING_CONFIG.settlementBusinessHours} horas hábiles` },
          { label: 'Cobranza futura', value: '100% a cargo de Kuota' },
        ],
      },
    ],
  },
];

export interface PricingPlanItem {
  id: 'basic' | 'pro' | 'open';
  name: string;
  priceSoles: number;
  priceLabel: string;
  commissionPercent: number;
  commissionLabel: string;
  minAmountLabel: string;
  limitLabel: string;
  extrasLabel: string;
  highlighted?: boolean;
  includesOpenLadder?: boolean;
}

/** PRD §5.8 y §6: Precios parametrizados en el archivo único */
export const PRICING_PLANS: PricingPlanItem[] = [
  {
    id: 'basic',
    name: 'Basic',
    priceSoles: 80,
    priceLabel: 'S/ 80 + IGV',
    commissionPercent: PRICING_CONFIG.kuotaPaymentsFeePercent,
    commissionLabel: `${formatPercentEs(PRICING_CONFIG.kuotaPaymentsFeePercent)} por transacción`,
    minAmountLabel: 'S/ 199',
    limitLabel: 'Hasta S/ 5,000 por transacción',
    extrasLabel: '–',
    highlighted: false,
  },
  {
    id: 'pro',
    name: 'Pro',
    priceSoles: 120,
    priceLabel: 'S/ 120 + IGV',
    commissionPercent: PRICING_CONFIG.kuotaPaymentsFeePercent,
    commissionLabel: `${formatPercentEs(PRICING_CONFIG.kuotaPaymentsFeePercent)} por transacción`,
    minAmountLabel: 'S/ 199',
    limitLabel: 'Más de S/ 5,000 por transacción',
    extrasLabel: 'Cobranza masiva · 5 usuarios',
    highlighted: true,
  },
  {
    id: 'open',
    name: 'Open',
    priceSoles: 99,
    priceLabel: 'S/ 99 + IGV',
    commissionPercent: PRICING_CONFIG.akuotasOpenFeePercent,
    commissionLabel: `${formatPercentEs(PRICING_CONFIG.akuotasOpenFeePercent)} por transacción`,
    minAmountLabel: 'Sin mínimo',
    limitLabel: 'Según tu nivel',
    extrasLabel: 'Solo aKuotas! · niveles de provider',
    highlighted: false,
    includesOpenLadder: true,
  },
];

export interface SupplierTeamMember {
  id: string;
  placeholderId: string;
  name: string;
  role: string;
  dimensions: string;
  linkedinUrl: string;
  photoUrl?: string;
}

/** PRD §5.7 y §11: IMG-17 a IMG-19 (400×400) */
export const SUPPLIER_TEAM_MEMBERS: SupplierTeamMember[] = [
  {
    id: 'jose-gonzalez',
    placeholderId: 'IMG-17',
    name: 'José González',
    role: 'CTO',
    dimensions: '400×400',
    linkedinUrl: 'https://www.linkedin.com',
    photoUrl: '/images/team/jose-gonzalez.png',
  },
  {
    id: 'josue-lovaton',
    placeholderId: 'IMG-18',
    name: 'Josué Lovatón',
    role: 'CEO',
    dimensions: '400×400',
    linkedinUrl: 'https://www.linkedin.com',
    photoUrl: '/images/team/josue-lovaton.png',
  },
  {
    id: 'rolando-quiroz',
    placeholderId: 'IMG-19',
    name: 'Rolando Quiroz',
    role: 'Advisor',
    dimensions: '400×400',
    linkedinUrl: 'https://www.linkedin.com',
  },
];

export interface SupplierTrustedInstitution {
  id: string;
  placeholderId: string;
  name: string;
  subtitle: string;
  dimensions: string;
  logoUrl?: string;
}

/** PRD §5.7 y §11: IMG-20 (200×80) */
export const SUPPLIER_TRUSTED_INSTITUTIONS: SupplierTrustedInstitution[] = [
  {
    id: 'cientifica-del-sur',
    placeholderId: 'IMG-20',
    name: 'Científica del Sur',
    subtitle: 'Universidad Científica del Sur',
    dimensions: '200×80',
    logoUrl: '/logos/cientifica-del-sur.png',
  },
];

/** Configuración oficial de Calendly para agendar con José o Josué (PRD §5.9 y P4) */
export const CAL_COM_CONFIG = {
  fallbackUrl: 'https://calendly.com/jmgonzalez-contact/30min',
  hosts: [
    {
      id: 'jose',
      name: 'José González',
      role: 'CTO · Integración y flujo operativo',
      calLink: 'https://calendly.com/jmgonzalez-contact/30min',
    },
    {
      id: 'josue',
      name: 'Josué Lovatón',
      role: 'CEO · Estrategia comercial y planes',
      calLink: 'https://calendly.com/2025joshoficial/30min',
    },
    {
      id: 'round-robin',
      name: 'Asignación automática (José o Josué)',
      role: 'Turno automático según disponibilidad',
      calLink: 'https://calendly.com/jmgonzalez-contact/30min',
    },
  ],
} as const;

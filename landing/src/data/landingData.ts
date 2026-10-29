export const PLAY_STORE_URL =
  'https://play.google.com/store/apps/details?id=com.kouta.app';

export interface PaymentMethodItem {
  id: string;
  name: string;
  category: 'Banco' | 'Billetera digital';
  shortCode: string;
}

export const PAYMENT_METHODS: PaymentMethodItem[] = [
  { id: 'bcp', name: 'BCP', category: 'Banco', shortCode: 'BCP' },
  { id: 'bbva', name: 'BBVA', category: 'Banco', shortCode: 'BBVA' },
  { id: 'interbank', name: 'Interbank', category: 'Banco', shortCode: 'IBK' },
  { id: 'scotiabank', name: 'Scotiabank', category: 'Banco', shortCode: 'SBP' },
  { id: 'yape', name: 'Yape', category: 'Billetera digital', shortCode: 'YAPE' },
  { id: 'plin', name: 'Plin', category: 'Billetera digital', shortCode: 'PLIN' },
];

export interface ProductChipItem {
  id: string;
  index: number;
  title: string;
  bullets: string[];
  note?: string;
  placeholderId: string;
  placeholderLabel: string;
  placeholderAspectRatio: string;
  screenMockTitle: string;
  screenMockSubtitle: string;
  screenMockMetrics: { label: string; value: string }[];
}

export const PRODUCT_CHIPS: ProductChipItem[] = [
  {
    id: 'sin-papeleo',
    index: 1,
    title: 'Sin papeleo',
    bullets: [
      'Sin garantía inmobiliaria. Sin historial Infocorp.',
      'Solo tu DNI y tu teléfono (afiliado a tu Yape o Plin), ¡y listo!',
    ],
    note: 'Si ya eres formal, tu nivel es mayor y accedes a más capital.',
    placeholderId: 'IMG-CHIP-01',
    placeholderLabel: 'Captura (placeholder): onboarding con DNI y teléfono',
    placeholderAspectRatio: '9:16',
    screenMockTitle: 'Registro rápido Kuota',
    screenMockSubtitle: 'Verificación de DNI y celular afiliado',
    screenMockMetrics: [
      { label: 'Documento', value: 'DNI validado ✓' },
      { label: 'Celular Yape / Plin', value: '+51 9•• ••• ••• ✓' },
      { label: 'Garantía / Infocorp', value: 'No requerido' },
    ],
  },
  {
    id: 'invierte-mas',
    index: 2,
    title: 'Invierte más, crece más',
    bullets: [
      'Te asignamos un nivel de inicio. Conforme pagas a tiempo y tus ventas crecen, el capital al que accedes también crece.',
      'Subir de nivel también te da menores cuotas iniciales y mejores plazos de pago.',
    ],
    placeholderId: 'IMG-CHIP-02',
    placeholderLabel: 'Captura (placeholder): tarjeta de nivel con condiciones de crédito',
    placeholderAspectRatio: '9:16',
    screenMockTitle: 'Mi Nivel Kuota',
    screenMockSubtitle: 'Condiciones activas de tu línea',
    screenMockMetrics: [
      { label: 'Capital disponible', value: 'En crecimiento ↑' },
      { label: 'Cuota inicial', value: 'Se reduce por nivel ↓' },
      { label: 'Plazo de pago', value: 'Hasta 360 días' },
    ],
  },
  {
    id: 'mas-compras',
    index: 3,
    title: 'Más compras',
    bullets: [
      'Con Kuota puedes gestionar tus ventas y saber cuánto necesitas para tu próxima compra.',
    ],
    placeholderId: 'IMG-CHIP-03',
    placeholderLabel: 'Captura (placeholder): gestión financiera con historial de ventas',
    placeholderAspectRatio: '9:16',
    screenMockTitle: 'Gestión Financiera',
    screenMockSubtitle: 'Historial de ventas y proyección de compra',
    screenMockMetrics: [
      { label: 'Ventas de la semana', value: 'Control al día' },
      { label: 'Próximo reabastecimiento', value: 'Meta calculada' },
      { label: 'Interés fijo', value: '5% por operación' },
    ],
  },
];

export interface CreditLevelLandingItem {
  level: number;
  purchaseAmount: string;
  downPayment: string;
  downPaymentPercent: number;
  maxTerm: string;
  maxTermDays: number;
  stepHeightPercent: number;
  /** Variación distintiva adaptada a los 4 azules de la paleta de marca (PRD §6.6) */
  barColorHex: string;
  badgeBgHex: string;
  badgeTextHex: string;
  summaryTag: string;
}

export const LANDING_CREDIT_LEVELS: CreditLevelLandingItem[] = [
  {
    level: 1,
    purchaseAmount: 'S/300 – S/1,000',
    downPayment: '50%',
    downPaymentPercent: 50,
    maxTerm: 'hasta 30 días',
    maxTermDays: 30,
    stepHeightPercent: 26,
    barColorHex: '#7CC0FE', // Variación entre Pale Sky y Dodger Blue
    badgeBgHex: '#CBE5FE',
    badgeTextHex: '#00235C',
    summaryTag: 'Nivel de inicio para empezar tu historial',
  },
  {
    level: 2,
    purchaseAmount: 'hasta S/3,000',
    downPayment: '40%',
    downPaymentPercent: 40,
    maxTerm: 'hasta 45 días',
    maxTermDays: 45,
    stepHeightPercent: 40,
    barColorHex: '#4CA8FE',
    badgeBgHex: '#B4DAFE',
    badgeTextHex: '#00235C',
    summaryTag: 'Mayor capital y menos cuota inicial',
  },
  {
    level: 3,
    purchaseAmount: 'S/5,000',
    downPayment: '30%',
    downPaymentPercent: 30,
    maxTerm: 'hasta 60 días',
    maxTermDays: 60,
    stepHeightPercent: 54,
    barColorHex: '#0391FD', // Dodger Blue
    badgeBgHex: '#0391FD',
    badgeTextHex: '#FFFFFF',
    summaryTag: 'Impulso sólido para surtir tu negocio',
  },
  {
    level: 4,
    purchaseAmount: 'S/7,500',
    downPayment: '20%',
    downPaymentPercent: 20,
    maxTerm: 'hasta 90 días',
    maxTermDays: 90,
    stepHeightPercent: 68,
    barColorHex: '#2F7AEF', // Crayola Blue
    badgeBgHex: '#2F7AEF',
    badgeTextHex: '#FFFFFF',
    summaryTag: 'Hasta 3 meses de plazo con solo 20% inicial',
  },
  {
    level: 5,
    purchaseAmount: 'S/10,000',
    downPayment: '10%',
    downPaymentPercent: 10,
    maxTerm: 'hasta 180 días',
    maxTermDays: 180,
    stepHeightPercent: 84,
    barColorHex: '#154EB4', // Variación entre Crayola Blue y Deep Navy
    badgeBgHex: '#154EB4',
    badgeTextHex: '#FFFFFF',
    summaryTag: 'Compras mayoristas con hasta 6 meses para pagar',
  },
  {
    level: 6,
    purchaseAmount: 'hasta S/30,000',
    downPayment: '0%',
    downPaymentPercent: 0,
    maxTerm: 'hasta 360 días',
    maxTermDays: 360,
    stepHeightPercent: 100,
    barColorHex: '#00235C', // Deep Navy
    badgeBgHex: '#00235C',
    badgeTextHex: '#FFFFFF',
    summaryTag: 'Máxima confianza: 0% de inicial y hasta 1 año de plazo',
  },
];

export interface TeamMemberItem {
  id: string;
  name: string;
  role: string;
  placeholderId: string;
  linkedinUrl: string;
}

export const TEAM_MEMBERS: TeamMemberItem[] = [
  {
    id: 'jose-gonzales',
    name: 'José Gonzales',
    role: 'CTO',
    placeholderId: 'IMG-TEAM-01',
    linkedinUrl: 'https://www.linkedin.com',
  },
  {
    id: 'josue-lovaton',
    name: 'Josué Lovatón',
    role: 'CEO',
    placeholderId: 'IMG-TEAM-02',
    linkedinUrl: 'https://www.linkedin.com',
  },
  {
    id: 'rolando-quiroz',
    name: 'Rolando Quiroz',
    role: 'Advisor',
    placeholderId: 'IMG-TEAM-03',
    linkedinUrl: 'https://www.linkedin.com',
  },
];

export interface TrustedInstitutionItem {
  id: string;
  name: string;
  subtitle: string;
  placeholderId: string;
}

/**
 * PRD §6.7: Por ahora, solo el logo de Científica del Sur.
 * El componente está diseñado como lista dinámica para agregar más logos sin rediseño.
 */
export const TRUSTED_INSTITUTIONS: TrustedInstitutionItem[] = [
  {
    id: 'cientifica-del-sur',
    name: 'Científica del Sur',
    subtitle: 'Universidad Científica del Sur',
    placeholderId: 'LOGO-INST-01',
  },
];

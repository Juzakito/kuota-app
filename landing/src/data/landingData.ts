export const PLAY_STORE_URL =
  'https://play.google.com/store/apps/details?id=com.kouta.app';

export interface PaymentMethodItem {
  id: string;
  name: string;
  category: 'Banco' | 'Billetera digital';
  shortCode: string;
  logoUrl: string;
}

export const PAYMENT_METHODS: PaymentMethodItem[] = [
  { id: 'bcp', name: 'BCP', category: 'Banco', shortCode: 'BCP', logoUrl: '/logos/bcp.svg' },
  { id: 'bbva', name: 'BBVA', category: 'Banco', shortCode: 'BBVA', logoUrl: '/logos/bbva.svg' },
  { id: 'interbank', name: 'Interbank', category: 'Banco', shortCode: 'IBK', logoUrl: '/logos/interbank.svg' },
  { id: 'scotiabank', name: 'Scotiabank', category: 'Banco', shortCode: 'SBP', logoUrl: '/logos/scotiabank.svg' },
  { id: 'yape', name: 'Yape', category: 'Billetera digital', shortCode: 'YAPE', logoUrl: '/logos/yape.svg' },
  { id: 'plin', name: 'Plin', category: 'Billetera digital', shortCode: 'PLIN', logoUrl: '/logos/plin.png' },
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
      'Sin garantía inmobiliaria ni historial de Infocorp.',
      'Solo tu DNI y tu teléfono comercial afiliado a Yape o Plin, ¡y listo!',
    ],
    note: 'Si ya cuentas con RUC activo o formalización, tu nivel de capital inicial es mayor.',
    placeholderId: 'CHIP-01',
    placeholderLabel: 'Validación de identidad digital y asignación de línea en segundos',
    placeholderAspectRatio: '9:16',
    screenMockTitle: 'Registro Express Kuota',
    screenMockSubtitle: 'Identidad digital y línea aprobada al instante',
    screenMockMetrics: [
      { label: 'Documento', value: 'DNI validado ✓' },
      { label: 'Celular Yape / Plin', value: '+51 987 654 321 ✓' },
      { label: 'Línea Pre-Aprobada', value: 'S/ 5,000.00' },
      { label: 'Tiempo de Activación', value: 'Menos de 60 segundos' },
    ],
  },
  {
    id: 'invierte-mas',
    index: 2,
    title: 'Invierte más, crece más',
    bullets: [
      'Te asignamos un nivel de inicio. Conforme compras y pagas puntualmente, tu línea de compra se amplía.',
      'Accedes a plazos más largos (30, 60 y 90 días) y menores cuotas iniciales.',
    ],
    note: 'Los comercios puntuales desbloquean hasta S/ 200,000+ en Nivel Maestro.',
    placeholderId: 'CHIP-02',
    placeholderLabel: 'Visualizador de nivel con condiciones de capital y fechas ordenadas',
    placeholderAspectRatio: '9:16',
    screenMockTitle: 'Mi Nivel Kuota',
    screenMockSubtitle: 'Línea de capital en expansión continua',
    screenMockMetrics: [
      { label: 'Nivel Activo', value: 'Nivel 2: Expansión' },
      { label: 'Capital Disponible', value: 'S/ 50,000.00' },
      { label: 'Plazos de Pago', value: '30, 60 y 90 días' },
      { label: 'Cuota Inicial', value: 'Reducida al 10%' },
    ],
  },
  {
    id: 'mas-compras',
    index: 3,
    title: 'Tu Cerebro de Compras con Nexo IA',
    bullets: [
      'Sabe exactamente cuánto necesitas comprar para tu próxima campaña o semana.',
      'Nexo IA analiza tus ventas reales y genera la orden sugerida para no perder clientes.',
    ],
    note: 'Evita quiebres de stock y compra con anticipación a precio mayorista.',
    placeholderId: 'CHIP-03',
    placeholderLabel: 'Diagnóstico predictivo de stock y orden sugerida en kuotas',
    placeholderAspectRatio: '9:16',
    screenMockTitle: 'Copiloto Nexo IA',
    screenMockSubtitle: 'Predicción de inventario y orden sugerida',
    screenMockMetrics: [
      { label: 'Alerta de Quiebre', value: 'Stock crítico en 4 días' },
      { label: 'Orden Sugerida', value: 'S/ 3,850 en mercadería' },
      { label: 'Plan en Kuotas', value: '3 kuotas de S/ 1,283' },
      { label: 'Precisión Predictiva', value: '94.5% de certeza' },
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
  photoUrl?: string;
}

export const TEAM_MEMBERS: TeamMemberItem[] = [
  {
    id: 'jose-gonzalez',
    name: 'José González',
    role: 'CTO',
    placeholderId: 'IMG-TEAM-01',
    linkedinUrl: 'https://www.linkedin.com',
    photoUrl: '/images/team/jose-gonzalez.png',
  },
  {
    id: 'josue-lovaton',
    name: 'Josué Lovatón',
    role: 'CEO',
    placeholderId: 'IMG-TEAM-02',
    linkedinUrl: 'https://www.linkedin.com',
    photoUrl: '/images/team/josue-lovaton.png',
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
  logoUrl?: string;
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
    logoUrl: '/logos/cientifica-del-sur.png',
  },
];

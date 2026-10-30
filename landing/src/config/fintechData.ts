export interface OrbitalNodeItem {
  id: string;
  label: string;
  sublabel?: string;
  iconName: string;
  radius: number; // 1: inner (160px), 2: mid (260px), 3: outer (360px)
  angle: number; // in degrees
  color: string;
  avatarUrl?: string;
  metric?: string;
}

export interface OrbitalSlide {
  id: string;
  metricNumber: string;
  metricPrefix?: string;
  metricSuffix?: string;
  title: string;
  subtitle: string;
  ctaText: string;
  badge: string;
  nodes: OrbitalNodeItem[];
}

export const ORBITAL_SLIDES: OrbitalSlide[] = [
  {
    id: 'modulos-ia',
    badge: 'Inteligencia Predictiva',
    metricPrefix: '+',
    metricNumber: '20',
    title: 'Módulos de IA Predictiva',
    subtitle: 'Funcionalidades de inteligencia y abastecimiento continuo creadas para que tu negocio nunca quiebre stock.',
    ctaText: 'Solicitar Línea Kuota',
    nodes: [
      { id: 'p90', label: 'Pronóstico 90d', sublabel: 'Predicción de demanda futura', iconName: 'TrendingUp', radius: 1, angle: 45, color: '#10B981' },
      { id: 'alerta', label: 'Alerta de Quiebre', sublabel: 'Anticipación a 30 días', iconName: 'BellRing', radius: 1, angle: 165, color: '#06B6D4' },
      { id: 'ordenes', label: 'Órdenes Automáticas', sublabel: 'Generación de PO inteligente', iconName: 'Bot', radius: 1, angle: 285, color: '#3B82F6' },
      { id: 'qr', label: 'QR de Almacén', sublabel: 'Recepción y conteo rápido', iconName: 'QrCode', radius: 2, angle: 15, color: '#8FD694' },
      { id: 'wsp', label: 'WhatsApp Cloud', sublabel: 'Aprobación de compras en chat', iconName: 'MessageSquare', radius: 2, angle: 105, color: '#10B981' },
      { id: 'erp', label: 'Integración ERP / SAP', sublabel: 'Sincronización bidireccional', iconName: 'Boxes', radius: 2, angle: 195, color: '#6366F1' },
      { id: 'sku', label: 'Analítica SKU', sublabel: 'Clasificación ABC dinámica', iconName: 'BarChart3', radius: 3, angle: 300, color: '#06B6D4' },
      { id: 'bnpl', label: 'Financiamiento BNPL', sublabel: 'Pago en 30, 60 y 90 días', iconName: 'CreditCard', radius: 3, angle: 240, color: '#8FD694' },
    ],
  },
  {
    id: 'confianza-b2b',
    badge: 'Comunidad & Respaldo',
    metricPrefix: '',
    metricNumber: '100%',
    title: 'Tecnología B2B Peruana',
    subtitle: 'Te ofrecemos una experiencia de compras segura y respaldada por arquitectura de infraestructura tecnológica y financiera de primer nivel.',
    ctaText: 'Conoce Nuestra Infraestructura',
    nodes: [
      { id: 'c1', label: 'Ferretería Industrial', sublabel: 'Lima Norte · 8 años', iconName: 'Store', radius: 1, angle: 30, color: '#10B981', metric: 'S/ 24K' },
      { id: 'c2', label: 'Distribuidora Textil', sublabel: 'Gamarra, La Victoria', iconName: 'Store', radius: 1, angle: 150, color: '#06B6D4', metric: 'S/ 45K' },
      { id: 'c3', label: 'Cadena Minimarkets', sublabel: '5 locales activos', iconName: 'Store', radius: 1, angle: 270, color: '#3B82F6', metric: 'S/ 38K' },
      { id: 'c4', label: 'Importadora Médica', sublabel: 'Arequipa Industrial', iconName: 'Store', radius: 2, angle: 75, color: '#8FD694', metric: 'S/ 80K' },
      { id: 'c5', label: 'Distribución Mayorista', sublabel: 'Trujillo y Norte Chico', iconName: 'Store', radius: 2, angle: 190, color: '#10B981', metric: 'S/ 62K' },
      { id: 'c6', label: 'Bodegas Asociadas', sublabel: 'Surco y Chorrillos', iconName: 'Store', radius: 3, angle: 330, color: '#6366F1', metric: 'S/ 15K' },
      { id: 'c7', label: 'Comercializadora Motos', sublabel: 'San Juan de Lurigancho', iconName: 'Store', radius: 3, angle: 220, color: '#06B6D4', metric: 'S/ 30K' },
    ],
  },
  {
    id: 'impacto-financiero',
    badge: 'Tracción & Liquidez',
    metricPrefix: '+ S/',
    metricNumber: '48M',
    title: 'Millones en Mercadería y Ahorro',
    subtitle: 'En inventario abastecido a tiempo y ahorro operativo liberado para que las PYMES no se descapitalicen.',
    ctaText: 'Simular Línea Kuota',
    nodes: [
      { id: 'f1', label: '+S/ 48,600', sublabel: 'Ahorro anual promedio', iconName: 'Coins', radius: 1, angle: 50, color: '#10B981' },
      { id: 'f2', label: '0 Quiebres', sublabel: '99.4% disponibilidad', iconName: 'ShieldCheck', radius: 1, angle: 170, color: '#06B6D4' },
      { id: 'f3', label: '2.4x Rotación', sublabel: 'Velocidad de inventario', iconName: 'Zap', radius: 1, angle: 290, color: '#3B82F6' },
      { id: 'f4', label: '30, 60, 90d', sublabel: 'Plazos de pago flexibles', iconName: 'CalendarClock', radius: 2, angle: 20, color: '#8FD694' },
      { id: 'f5', label: '1.8% Fijo', sublabel: 'Tasa mensual transparente', iconName: 'Percent', radius: 2, angle: 120, color: '#10B981' },
      { id: 'f6', label: 'S/ 0.00', sublabel: 'Membresía base sin costo', iconName: 'Sparkles', radius: 2, angle: 210, color: '#6366F1' },
      { id: 'f7', label: 'PEN / USD', sublabel: 'Operación multimoneda', iconName: 'ArrowRightLeft', radius: 3, angle: 315, color: '#06B6D4' },
      { id: 'f8', label: '100% Digital', sublabel: 'Sin hipotecas ni trámites', iconName: 'Smartphone', radius: 3, angle: 235, color: '#8FD694' },
    ],
  },
];

export interface StickyFeatureItem {
  id: string;
  title: string;
  headline: string;
  description: string;
  ctaText: string;
  pillBadge: string;
  mockupState: {
    screenTitle: string;
    screenSubtitle: string;
    highlightMetric: string;
    floatingNotification: string;
    floatingSub: string;
    ringColor: string;
    badgeValue: string;
    items: { name: string; qty: string; alert?: string; kuota?: string }[];
  };
}

export const STICKY_FEATURES: StickyFeatureItem[] = [
  {
    id: 'cerebro-ia',
    title: 'Inteligencia Predictiva',
    headline: 'Conoce qué SKUs rotan más y cuáles inmovilizan tu caja',
    description: 'Toda tu operación conectada en Kuota. Clasificación ABC dinámica de productos, detección temprana de sobrestock y sugerencias inteligentes de reabastecimiento antes de perder ventas.',
    ctaText: 'Explorar IA Predictiva',
    pillBadge: 'Algoritmo Nexo v2.4',
    mockupState: {
      screenTitle: 'Cerebro de Compras · Nexo IA',
      screenSubtitle: 'Clasificación ABC en tiempo real',
      highlightMetric: '94.5% Precisión de Forecast',
      floatingNotification: '⚡ Alerta Nexo: Quiebre en 4 días',
      floatingSub: 'SKU #4810 · Leche Gloria 400g x 48',
      ringColor: '#10B981',
      badgeValue: 'Alta Rotación',
      items: [
        { name: 'Arroz Extra Costeño 50kg', qty: '12 sacos rest.', alert: 'Reorden urgente' },
        { name: 'Aceite Primor Clásico 1L', qty: '28 cajas', kuota: 'S/ 1,840 (3x S/ 613)' },
        { name: 'Detergente Opal 4.5kg', qty: '18 paquetes', kuota: 'Stock óptimo' },
      ],
    },
  },
  {
    id: 'kuotear-facturas',
    title: 'Financiamiento BNPL B2B',
    headline: 'Kuotea tus facturas de proveedores: Liquidez en 1 clic',
    description: 'Abastece tu negocio hoy con los distribuidores mayoristas que ya conoces y fracciona el pago en 30, 60 o 90 días sin trámites bancarios engorrosos ni papeleo.',
    ctaText: 'Simular Financiamiento',
    pillBadge: 'Desembolso en 2 Horas',
    mockupState: {
      screenTitle: 'Facturas por Kuotear',
      screenSubtitle: 'Selecciona facturas de proveedores',
      highlightMetric: 'S/ 24,500 Línea Disponible',
      floatingNotification: '🛒 Kuotear S/ 4,520 en 3 cuotas',
      floatingSub: 'Distribuidora Alicorp Mayorista',
      ringColor: '#06B6D4',
      badgeValue: 'Tasa 1.8%',
      items: [
        { name: 'Factura F001-49210 (Alicorp)', qty: 'Vence 15 Nov', kuota: '3 cuotas de S/ 820' },
        { name: 'Factura F022-81900 (Gloria)', qty: 'Vence 22 Nov', kuota: '2 cuotas de S/ 1,450' },
        { name: 'Factura E004-10293 (Kimberly)', qty: 'Aprobada', kuota: 'S/ 3,100 financiado' },
      ],
    },
  },
  {
    id: 'whatsapp-erps',
    title: 'Conectividad Universal',
    headline: 'Órdenes de compra por WhatsApp y ERPs en tiempo real',
    description: 'Sincroniza Shopify, Mercado Libre, WooCommerce o tu ERP contable sin escribir una sola línea de código. Aprueba reabastecimientos directamente desde tu WhatsApp.',
    ctaText: 'Ver Integraciones',
    pillBadge: 'Conectores Plug & Play',
    mockupState: {
      screenTitle: 'Conexiones Activas',
      screenSubtitle: 'Sincronización multi-canal',
      highlightMetric: '6 Canales En Línea',
      floatingNotification: '💬 Nexo: ¿Autorizas Orden #891?',
      floatingSub: 'Responde "SI" para kuotear S/ 3,200',
      ringColor: '#3B82F6',
      badgeValue: 'Sincronizado',
      items: [
        { name: 'Shopify Perú Store', qty: '98 SKUs sincronizados' },
        { name: 'WhatsApp Bot Kuota', qty: 'Activo (+51 981•••)' },
        { name: 'Excel / Google Sheets', qty: 'Importación diaria' },
      ],
    },
  },
  {
    id: 'multi-sucursal',
    title: 'Control de Almacén',
    headline: 'Multi-sucursal y trazabilidad de cada despacho',
    description: 'Administra recepciones de mercadería, mermas, transferencias entre almacenes y márgenes por producto desde un único panel centralizado en la nube.',
    ctaText: 'Crear Cuenta Gratis',
    pillBadge: 'Control Total de Stock',
    mockupState: {
      screenTitle: 'Red de Almacenes',
      screenSubtitle: 'Trazabilidad y recepciones',
      highlightMetric: '99.98% Precisión en Físico',
      floatingNotification: '📦 Despacho Recibido: Local Surquillo',
      floatingSub: '32 bultos verificados con QR',
      ringColor: '#8FD694',
      badgeValue: 'Auditoría OK',
      items: [
        { name: 'Almacén Central (Lima)', qty: 'Capacidad al 74%' },
        { name: 'Tienda 01 (Los Olivos)', qty: 'Stock regular' },
        { name: 'Tienda 02 (San Juan)', qty: '1 alerta atendida' },
      ],
    },
  },
];

export interface VideoStoryItem {
  id: string;
  tag: string;
  title: string;
  businessName: string;
  location: string;
  duration: string;
  views: string;
  highlightStat: string;
  previewGradient: string;
}

export const VIDEO_STORIES: VideoStoryItem[] = [
  {
    id: 'story-1',
    tag: 'Distribución Mayorista',
    title: '¿Cómo una distribuidora redujo a cero sus quiebres de stock con Nexo IA?',
    businessName: 'Distribuidora San Mateo',
    location: 'Mercado de Productores · Santa Anita',
    duration: '1:45 min',
    views: '12.4K vistas',
    highlightStat: '-100% Quiebres',
    previewGradient: 'from-[#070A0F] via-[#0A1628] to-[#0D3B36]',
  },
  {
    id: 'story-2',
    tag: 'Caso Real · Gamarra',
    title: '¿Por qué tu capital se queda atrapado en inventario muerto y cómo liberarlo?',
    businessName: 'Confecciones Altamira',
    location: 'Gamarra · La Victoria',
    duration: '2:10 min',
    views: '18.9K vistas',
    highlightStat: '+S/ 38,000 Caja Libre',
    previewGradient: 'from-[#070A0F] via-[#111827] to-[#1E3A8A]',
  },
  {
    id: 'story-3',
    tag: 'Tutorial Rápido',
    title: 'Cómo kuotear tu primera orden de compra en 3 minutos sin pisar un banco',
    businessName: 'Ferretería La Solución',
    location: 'Comas · Lima Norte',
    duration: '1:15 min',
    views: '24.1K vistas',
    highlightStat: 'Aprobación en 2h',
    previewGradient: 'from-[#070A0F] via-[#0F2027] to-[#203A43]',
  },
];

export interface GamificationLevelItem {
  id: string;
  name: string;
  badgeSymbol: string;
  badgeLabel: string;
  maxAmount: string;
  terms: string;
  rate: string;
  cashback: string;
  requirements: string;
  color: string;
  isPopular?: boolean;
}

export const GAMIFICATION_LEVELS: GamificationLevelItem[] = [
  {
    id: 'impulso',
    name: 'Nivel Impulso',
    badgeSymbol: 'S/',
    badgeLabel: 'Nivel 01',
    maxAmount: 'Hasta S/ 15,000',
    terms: 'Plazos de 30 días',
    rate: 'Tasa base 2.4% mensual',
    cashback: '1.0% de bonificación en compras',
    requirements: 'DNI y negocio activo (mínimo 3 meses de operación comprobable)',
    color: '#94A3B8',
  },
  {
    id: 'expansion',
    name: 'Nivel Expansión',
    badgeSymbol: '50K',
    badgeLabel: 'Nivel 02',
    maxAmount: 'Hasta S/ 50,000',
    terms: 'Plazos de 30 y 60 días',
    rate: 'Tasa preferencial 1.9% mensual',
    cashback: '1.8% de bonificación en compras',
    requirements: '3 órdenes kuoteadas y pagadas puntualmente en plataforma',
    color: '#06B6D4',
    isPopular: true,
  },
  {
    id: 'maestro',
    name: 'Nivel Maestro',
    badgeSymbol: '200K',
    badgeLabel: 'Nivel Corporativo',
    maxAmount: 'Hasta S/ 200,000+',
    terms: 'Plazos de 30, 60 y 90 días',
    rate: 'Tasa institucional desde 1.4% mensual',
    cashback: '2.5% bonificación + compras conjuntas',
    requirements: 'Línea corporativa con condiciones institucionales e historial impecable',
    color: '#10B981',
  },
];

export interface TestimonialItem {
  id: string;
  author: string;
  role: string;
  company: string;
  location: string;
  years: string;
  avatarBg: string;
  quote: string;
  metricLabel: string;
  metricValue: string;
}

export const TESTIMONIALS_ROW_1: TestimonialItem[] = [
  {
    id: 't1',
    author: 'Carlos Huamán',
    role: 'Gerente General',
    company: 'Ferretería San José',
    location: 'Lima Norte',
    years: '8 años operando',
    avatarBg: 'bg-emerald-500/20 text-emerald-600',
    quote: 'Antes nos quedábamos sin cemento y fierro los fines de semana. Con las alertas de Nexo IA compramos 5 días antes y kuoteamos las facturas en 60 días. Cero estrés de caja.',
    metricLabel: 'Aumento de stock útil',
    metricValue: '+42%',
  },
  {
    id: 't2',
    author: 'Rosa Elena Mendoza',
    role: 'Fundadora',
    company: 'Distribuidora Textil Gamarra',
    location: 'La Victoria',
    years: '12 años en el rubro',
    avatarBg: 'bg-cyan-500/20 text-cyan-600',
    quote: 'Los bancos me pedían balances auditados y tardaban un mes para prestarme. Kuota me aprobó mi línea en una tarde y ahora compro rollos de tela con descuento por volumen.',
    metricLabel: 'Ahorro por lote',
    metricValue: 'S/ 8,400',
  },
  {
    id: 't3',
    author: 'Javier Quispe',
    role: 'Director de Operaciones',
    company: 'Minimarkets El Sol (3 sedes)',
    location: 'Surco y San Juan',
    years: '5 años',
    avatarBg: 'bg-indigo-500/20 text-indigo-600',
    quote: 'Sincronizamos las cajas con Kuota y Nexo nos avisa por WhatsApp exactamente qué comprar. Nos ha evitado tener mercadería vencida.',
    metricLabel: 'Reducción de merma',
    metricValue: '-85%',
  },
  {
    id: 't4',
    author: 'Maritza Villanueva',
    role: 'Gerente Comercial',
    company: 'Farmacias San Gabriel',
    location: 'Arequipa',
    years: '6 años',
    avatarBg: 'bg-emerald-500/20 text-emerald-600',
    quote: 'La transparencia de saber cuánto pago por kuotear cada factura sin cobros sorpresa es lo mejor. Kuota nos da un respaldo operativo y de liquidez único.',
    metricLabel: 'Línea activa',
    metricValue: 'S/ 50K',
  },
  {
    id: 't5',
    author: 'Diego Palacios',
    role: 'Dueño',
    company: 'Comercializadora Automotriz',
    location: 'Trujillo',
    years: '9 años',
    avatarBg: 'bg-blue-500/20 text-blue-600',
    quote: 'Poder financiar repuestos importados a 90 días nos permitió abrir una segunda sucursal sin pedir préstamo bancario.',
    metricLabel: 'Nuevos locales',
    metricValue: '+1 Sede',
  },
];

export const TESTIMONIALS_ROW_2: TestimonialItem[] = [
  {
    id: 't6',
    author: 'Miriam Soto',
    role: 'Administradora',
    company: 'Distribuciones Lácteas del Valle',
    location: 'Huancayo',
    years: '4 años',
    avatarBg: 'bg-cyan-500/20 text-cyan-600',
    quote: 'Nexo IA es como tener un jefe de compras 24/7. Las órdenes salen automáticas para que solo le dé click a aprobar.',
    metricLabel: 'Horas ahorradas/mes',
    metricValue: '35 hrs',
  },
  {
    id: 't7',
    author: 'Walter Barrientos',
    role: 'Socio Fundador',
    company: 'Bodega & Licorería Barrientos',
    location: 'Callao',
    years: '15 años',
    avatarBg: 'bg-emerald-500/20 text-emerald-600',
    quote: 'Subí de nivel en Kuota pagando puntual. Empecé con 10 mil soles y hoy tengo 50 mil para abastecerme antes de campaña navideña.',
    metricLabel: 'Nivel Kuota',
    metricValue: 'Expansión',
  },
  {
    id: 't8',
    author: 'Carmen Zavala',
    role: 'Jefa de Logística',
    company: 'Plásticos & Envases Lima',
    location: 'Ate Vitarte',
    years: '7 años',
    avatarBg: 'bg-indigo-500/20 text-indigo-600',
    quote: 'Conectar nuestro Excel de inventario nos tomó 5 minutos. La plataforma es limpia, rápida y no te llena de tecnicismos.',
    metricLabel: 'Tiempo de Setup',
    metricValue: '5 min',
  },
  {
    id: 't9',
    author: 'Gonzalo Arévalo',
    role: 'Gerente General',
    company: 'Agroinsumos del Norte',
    location: 'Piura',
    years: '11 años',
    avatarBg: 'bg-emerald-500/20 text-emerald-600',
    quote: 'El modelo de pagar mercadería en cuotas es lo que el campo y el comercio peruano necesitaban desde hace años.',
    metricLabel: 'Disponibilidad',
    metricValue: '100%',
  },
  {
    id: 't10',
    author: 'Lorena Paredes',
    role: 'Directora',
    company: 'Belleza & Cosmética Profesional',
    location: 'Miraflores',
    years: '3 años',
    avatarBg: 'bg-blue-500/20 text-blue-600',
    quote: 'La atención del equipo y el orbe Nexo en el celular hacen que gestionar pedidos sea hasta divertido.',
    metricLabel: 'Satisfacción',
    metricValue: '5/5 ★',
  },
];

export interface BlogPostItem {
  id: string;
  category: 'Tutoriales Kuota' | 'IA Predictiva' | 'Finanzas B2B';
  title: string;
  excerpt: string;
  readTime: string;
  updatedYear: string;
  gradient: string;
}

export const BLOG_POSTS: BlogPostItem[] = [
  {
    id: 'post-1',
    category: 'IA Predictiva',
    title: '¿Cómo calcular tu punto de reorden (ROP) sin congelar tu flujo de caja?',
    excerpt: 'Aprende la fórmula exacta que utiliza Nexo IA para calcular el momento óptimo de compra equilibrando stock de seguridad y liquidez diaria.',
    readTime: 'Lectura de 4 min',
    updatedYear: 'Actualizado 2026',
    gradient: 'from-[#070A0F] to-[#0F2027]',
  },
  {
    id: 'post-2',
    category: 'Tutoriales Kuota',
    title: 'Pasa las facturas de tus proveedores a cuotas en minutos con Kuota',
    excerpt: 'Paso a paso de cómo subir una factura emitida por tu mayorista y fraccionarla en 30, 60 o 90 días desde tu teléfono celular.',
    readTime: 'Lectura de 3 min',
    updatedYear: 'Guía Práctica',
    gradient: 'from-[#070A0F] to-[#1E3A8A]',
  },
  {
    id: 'post-3',
    category: 'Finanzas B2B',
    title: 'Capital de trabajo vs. Crédito bancario: La guía de supervivencia PYME',
    excerpt: 'Por qué endeudarse a largo plazo para comprar inventario corriente es el error número 1 de las empresas comerciales en Perú.',
    readTime: 'Lectura de 6 min',
    updatedYear: 'Finanzas 2026',
    gradient: 'from-[#070A0F] to-[#0D3B36]',
  },
  {
    id: 'post-4',
    category: 'Tutoriales Kuota',
    title: 'Sincroniza tu catálogo de Shopify y Mercado Libre con WhatsApp',
    excerpt: 'Configura las órdenes automáticas de compra para que tu equipo reciba y apruebe pedidos de reposición sin entrar al panel.',
    readTime: 'Lectura de 5 min',
    updatedYear: 'Integraciones',
    gradient: 'from-[#070A0F] to-[#203A43]',
  },
];

export interface OnboardingStepItem {
  step: string;
  title: string;
  description: string;
  screenHighlight: string;
  screenSub: string;
  pillColor: string;
  phoneDetails: {
    label: string;
    value: string;
    badge?: string;
  }[];
}

export const ONBOARDING_STEPS: OnboardingStepItem[] = [
  {
    step: '01',
    title: 'Crea tu cuenta empresarial o inicia sesión con Google.',
    description: 'Ingreso instantáneo en 30 segundos. Sin trámites burocráticos ni contratos físicos de 50 páginas.',
    screenHighlight: 'Paso 1: Identificación Digital',
    screenSub: 'Acceso seguro con Google Workspace o RUC',
    pillColor: '#10B981',
    phoneDetails: [
      { label: 'Método de Acceso', value: 'Google OAuth 2.0 / RUC 20' },
      { label: 'Estado de Validación', value: 'Verificación instantánea' },
      { label: 'Costo de Apertura', value: 'S/ 0.00 (Plan Base Gratuito)' },
    ],
  },
  {
    step: '02',
    title: 'Conecta tu catálogo (Shopify, Mercado Libre, ERP o Excel).',
    description: 'Nuestra plataforma se enlaza de forma transparente con tus canales de venta y hojas de cálculo para leer tu inventario.',
    screenHighlight: 'Paso 2: Sincronización de Catálogo',
    screenSub: 'Conexión en vivo de inventario y rotación',
    pillColor: '#06B6D4',
    phoneDetails: [
      { label: 'Canales Enlazados', value: 'Shopify · WooCommerce · Excel' },
      { label: 'SKUs Detectados', value: '1,420 productos activos' },
      { label: 'Última Sincronización', value: 'Hace 2 minutos (Automática)' },
    ],
  },
  {
    step: '03',
    title: 'Deja que Nexo IA analice tu demanda y detecte riesgos de quiebre.',
    description: 'El copiloto inteligente clasifica tus productos en categorías ABC y te avisa qué ítems se agotarán en los próximos 30 días.',
    screenHighlight: 'Paso 3: Diagnóstico Predictivo Nexo',
    screenSub: 'Análisis de rotación y riesgo de stockout',
    pillColor: '#3B82F6',
    phoneDetails: [
      { label: 'Precisión Proyectada', value: '94.5% de certeza histórica' },
      { label: 'Riesgos Detectados', value: '4 SKUs en peligro de quiebre' },
      { label: 'Ventas Protegidas', value: 'S/ 18,200 en pedidos salvados' },
    ],
  },
  {
    step: '04',
    title: 'Recibe tu línea de financiamiento pre-aprobada con tu RUC.',
    description: 'Evaluación algorítmica sin hipotecas inmobiliarias ni avales personales. Te asignamos una línea de capital de trabajo inmediata.',
    screenHighlight: 'Paso 4: Asignación de Línea Kuota',
    screenSub: 'Línea B2B pre-aprobada y 100% digital',
    pillColor: '#8FD694',
    phoneDetails: [
      { label: 'Línea Asignada', value: 'S/ 25,000.00' },
      { label: 'Tasa Mensual Fija', value: '1.8% fija (Nivel Expansión)' },
      { label: 'Garantía Exigida', value: 'Ninguna (Crédito comercial)' },
    ],
  },
  {
    step: '05',
    title: 'Aprueba las órdenes de compra recomendadas en un solo clic.',
    description: 'Revisa las cantidades sugeridas por Nexo IA, edítalas si lo deseas y confirma la compra con un toque desde tu móvil.',
    screenHighlight: 'Paso 5: Aprobación de Orden',
    screenSub: 'Confirmación directa de compra a proveedor',
    pillColor: '#6366F1',
    phoneDetails: [
      { label: 'Orden Generada', value: 'PO #2026-0819 (Mayorista)' },
      { label: 'Total a Pagar', value: 'S/ 8,400.00 en mercadería' },
      { label: 'Esquema de Cuotas', value: '3 cuotas de S/ 2,800 + fee' },
    ],
  },
  {
    step: '06',
    title: '¡Listo! Nosotros pagamos al proveedor hoy y tú pagas en Kuotas.',
    description: 'Tu proveedor despacha la mercadería inmediatamente. Tú vendes el stock en tu tienda y amortizas en 30, 60 o 90 días.',
    screenHighlight: 'Paso 6: Despacho y Cobro en Cuotas',
    screenSub: 'Tu proveedor cobra hoy, tú pagas después',
    pillColor: '#10B981',
    phoneDetails: [
      { label: 'Desembolso a Proveedor', value: 'Completado hoy (100%)' },
      { label: 'Estado de Despacho', value: 'En ruta a tu almacén' },
      { label: 'Primera Cuota', value: 'A los 30 días de recepción' },
    ],
  },
];

export interface FinancialMetricCard {
  id: string;
  badge: string;
  title: string;
  value: string;
  description: string;
  isDark: boolean;
  accentColor: string;
}

export const FINANCIAL_BENTO_CARDS: FinancialMetricCard[] = [
  {
    id: 'f-1',
    badge: 'Precisión Predictiva',
    title: 'Forecast IA de Demanda',
    value: '94.5% - 99.1%',
    description: 'Proyección estocástica de venta a 30, 60 y 90 días basada en tus canales y patrones estacionales.',
    isDark: true,
    accentColor: '#10B981',
  },
  {
    id: 'f-2',
    badge: 'Cero Letras Chicas',
    title: 'Comisión de Kuoteo Fija',
    value: 'Desde 1.8% / mes',
    description: 'Tasa mensual fija y transparente según tu nivel de usuario. Sin costos ocultos de mantenimiento ni seguros sorpresa.',
    isDark: false,
    accentColor: '#06B6D4',
  },
  {
    id: 'f-3',
    badge: 'Operatividad Total',
    title: 'Integración Multi-Moneda',
    value: 'Soles y Dólares',
    description: 'Opera órdenes de compra tanto en moneda nacional (PEN) como en dólares (USD) para importaciones directas.',
    isDark: true,
    accentColor: '#3B82F6',
  },
  {
    id: 'f-4',
    badge: 'Plazos Flexibles',
    title: 'Fraccionamiento de Stock',
    value: '30, 60 o 90 días',
    description: 'Elige los días de pago según el ciclo natural de rotación de cada producto para que la mercadería se pague sola.',
    isDark: false,
    accentColor: '#8FD694',
  },
  {
    id: 'f-5',
    badge: 'Sin Barreras de Entrada',
    title: 'Costo de Membresía Base',
    value: 'S/ 0.00',
    description: 'Plan inicial gratuito para digitalizar tu inventario y conectar tus canales hoy mismo. Solo pagas al kuotear.',
    isDark: true,
    accentColor: '#10B981',
  },
];

export interface FaqItem {
  id: string;
  question: string;
  answer: string;
}

export const FAQS_LIST: FaqItem[] = [
  {
    id: 'faq-1',
    question: '¿Qué es exactamente Kuota y cómo se diferencia de un banco tradicional?',
    answer: 'Kuota es un Sistema Operativo de Compras e Inteligencia Predictiva que combina software de inventario con financiamiento de mercadería (BNPL B2B). A diferencia de un banco que te pide hipotecas o balances de 3 años, Kuota evalúa la rotación real de tu negocio y te aprueba una línea de capital para pagar directamente a tus distribuidores en 30, 60 o 90 días.',
  },
  {
    id: 'faq-2',
    question: '¿Qué es Nexo IA y cómo me ayuda a no quebrar stock?',
    answer: 'Nexo es nuestro copiloto de inteligencia artificial representado como un orbe interactivo en tu plataforma. Se conecta a tus ventas históricas y predice con hasta 94.5% de precisión cuántos días de inventario te quedan de cada SKU. Cuando detecta que un producto clave se va a agotar, genera la orden de compra sugerida y te permite financiarla con un solo toque.',
  },
  {
    id: 'faq-3',
    question: '¿Cómo funciona el pago a mis proveedores al "Kuotear"?',
    answer: 'Cuando apruebas una compra en la plataforma, Kuota transfiere el 100% del monto de la factura directamente a la cuenta bancaria de tu proveedor mayorista. El proveedor te despacha la mercadería de inmediato como si le hubieras pagado al contado, y tú abonas a Kuota en cómodas cuotas quincenales o mensuales.',
  },
  {
    id: 'faq-4',
    question: '¿Qué requisitos necesito para activar mi línea Kuota?',
    answer: 'Solo necesitas ser mayor de edad con DNI o carné de extranjería vigente, tener un negocio activo en Perú con ventas comprobables (vía boletas, facturas, historial de Yape/Plin o catálogo digital) y tu número de teléfono. Si ya cuentas con RUC 10 o RUC 20 formalizado, accedes a niveles de línea más altos y tasas preferenciales.',
  },
  {
    id: 'faq-5',
    question: '¿Qué respaldo tiene Kuota y quién está detrás de la plataforma?',
    answer: 'Kuota opera bajo una arquitectura de infraestructura tecnológica y financiera de grado bancario con encriptación de datos TLS 1.3, alta disponibilidad en la nube y total transparencia operativa en cada transacción sin costos ocultos.',
  },
  {
    id: 'faq-6',
    question: '¿Tiene algún costo abrir una cuenta o usar el software?',
    answer: 'El registro y uso base de la plataforma es 100% gratuito (S/ 0.00). Puedes conectar tu inventario, ver tu diagnóstico predictivo con Nexo y consultar tu línea disponible sin costo. Solo se aplica una comisión fija y transparente (desde 1.8% mensual) al momento de kuotear una orden de compra financiada.',
  },
];

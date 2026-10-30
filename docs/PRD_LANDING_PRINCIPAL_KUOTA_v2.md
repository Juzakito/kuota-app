# PRD — Landing Page Principal de Kuota (Ecosistema & Flagship)
> **Producto:** KUOTA (`kuota-ia.vercel.app`)  
> **Versión:** 2.0 (Alineada a la Arquitectura Fintech de Alto Contraste)  
> **Fecha:** 29 de octubre de 2026 · **Estado:** Implementado y Desplegado en Producción  
> **Ecosistema de Marca:** **KUOTA** (Plataforma Comercial / *"Kuotear"*), **NEXO IA** (Copiloto Predictivo de Demanda).  
> **Documento de Diseño Rector:** `DESIGN.md` (Macro-Contenedores Bento, Ritmo Claro/Oscuro, Tipografía Plus Jakarta Sans & Inter tabular-nums).

---

## 1. Resumen Ejecutivo y Propósito

La landing page principal de **KUOTA** es la puerta de entrada al **Sistema Operativo de Compras e Inteligencia Predictiva** para el comercio en Latinoamérica. 

No es una web corporativa tradicional de folleto ni un formulario plano de captura. Es una experiencia fintech inmersiva de alto impacto (estilo *Tarjeta iO / Stripe / Vercel*) diseñada para:
1. **Posicionar a KUOTA:** Demostrar cómo las PYMES pueden abastecer su inventario con **94.5% de precisión predictiva**, anticipar quiebres de stock a 30 días y pagar sus órdenes de compra en cuotas (**"Kuotear"**) a 30, 60 o 90 días sin descapitalizarse.
2. **Presentar al Copiloto NEXO IA:** Humanizar y tangibilizar el software predictivo mediante el orbe cristalino y alertas proactivas en tiempo real.
3. **Ofrecer Respaldo Institucional y Seguridad:** Comunicar infraestructura tecnológica de nivel bancario (TLS 1.3, alta disponibilidad cloud) con estándares MIT/Stanford.
4. **Convertir Tráfico Cualificado:** Permitir el inicio de sesión directo con Google OAuth al panel interno (`/dashboard`), pre-evaluar líneas de financiamiento mediante RUC/DNI en 2 horas, o afiliar comercios proveedores.

---

## 2. Objetivos y Métricas de Negocio (KPIs)

| Objetivo Estratégico | Métrica Clave | Meta de Conversión |
| :--- | :--- | :--- |
| **Conversión a Lead Pre-aprobado** | Envíos de formulario de pre-evaluación (RUC/DNI + WhatsApp) / Visitas | ≥ 6.5% |
| **Interacción con el Sistema Orbital** | % de usuarios que completan al menos 1 ciclo orbital (5s) o hacen clic en nodos | ≥ 38% |
| **Engagement con Video-Historias** | Clics en reproducción / apertura de reels 9:16 de testimonios | ≥ 22% |
| **Tráfico al Producto / Dashboard** | Clics en "Iniciar Sesión" y "Empezar a Kuotear" que aterrizan en `/dashboard` | ≥ 12% |
| **Retención y Profundidad de Scroll** | Usuarios que alcanzan el pre-footer isométrico 3D (Scroll 75%+) | ≥ 45% |
| **Rendimiento de Carga (Core Web Vitals)** | LCP en 4G móvil ≤ 2.0s · FID ≤ 100ms · CLS = 0 (cero scroll horizontal) | 100% verde |

---

## 3. Arquitectura de Audiencia y Enrutamiento

La plataforma atiende a tres actores dentro de una misma URL con preservación de estado:
1. **Público General / Decisores Comerciales (Hub Ecosistema):** Ruta base `/` o `?perfil=ecosistema`. Presenta la experiencia maestra de 12 secciones de alto contraste.
2. **Compradores / Emprendedores de Bodegas y Tiendas:** Parámetro `?perfil=compradores` o navegación contextual. Focalizado en el acceso a líneas de capital para mercadería y cuotas semanales/quincenales.
3. **Proveedores Mayoristas y Distribuidores:** Parámetro `?perfil=comercios` o `?perfil=proveedores`. Focalizado en ventas al crédito con 0% riesgo de impago, liquidación en 24-48h y recaudación por QR/Link.

---

## 4. Requisitos Transversales de Diseño y Rendimiento

1. **Ritmo de Alto Contraste:** Alternancia cinematográfica entre macro-tarjetas blancas (`#FFFFFF` con `rounded-[32px] md:rounded-[48px]`) sobre lienzo gris hielo (`#F4F6F8`) y bloques monolíticos oscuros (`#070A0F` / `#0A0F14`) con auroras difusas en verde menta (`#8FD694`), esmeralda (`#10B981`) y cian (`#06B6D4`).
2. **Botones Cápsula Continuos:** Silueta de píldora `rounded-full px-7 py-3.5`. Negra sobre fondos claros, blanca sobre fondos oscuros.
3. **Tipografía:** `Plus Jakarta Sans` en pesos `600` y `700` (`tracking-tight`, `leading-[1.08]`) para titulares de display; `Inter` con `tabular-nums` obligatorio para cifras financieras y porcentajes.
4. **Aceleración por GPU:** Todas las animaciones espaciales implementadas con Framer Motion y transformaciones 3D (`translate3d`) a 60 FPS.
5. **Mobile-First & Touch Manipulation:** Supresión del delay táctil de 300ms (`touch-action: manipulation`) y desactivación de efectos pesados de inclinación 3D en pantallas táctiles (`@media (pointer: coarse)`).

---

## 5. Especificación Detallada de las 12 Secciones Maestras

### SECCIÓN 1: Navbar Fijo con Cápsula Central (`NavbarCapsule.tsx`)
- **Estructura:** Fijo al viewport (`fixed top-0 inset-x-0 z-50`). Al hacer scroll aplica `bg-[#070A0F]/95`, `backdrop-blur-md` y borde translúcido `border-white/10`.
- **Izquierda:** Logo oficial de **kuota** con acento menta y badge institucional `Kuota` con indicador de pulso en vivo.
- **Centro (La Cápsula de Navegación):** Píldora en `#161B22` con borde `border-white/10` y selector dinámico animado vía `framer-motion` (`layoutId="nav-pill"`):
  1. `Niveles de Línea` (smooth scroll hacia `#niveles`)
  2. `Refiere PYMES` (badge `+S/500`)
  3. `Plataforma & Nexo` (ícono `Sparkles`)
  4. `Kuotear Inventario` (smooth scroll hacia `#como-empezar`)
  5. `Simulador ⌄` (Dropdown: Calculadora de ROI + Prevención de Quiebres)
  6. `Radar B2B ⌄` (Dropdown: Casos en Video, Blog de Compras, Preguntas)
- **Derecha:** Enlace `Iniciar Sesión` conectado al flujo de autenticación real (`/login` / Google Auth) + botón cápsula blanco prominente `Solicitar Línea Kuota`.
- **Móvil:** Drawer desplegable en `#0A0E17` con acceso a todos los módulos y botones de acción rápida.

---

### SECCIÓN 2: Hero Principal Split en Macro-Contenedor Curvo (`HeroBentoSplit.tsx`)
- **Layout:** Macro-tarjeta blanca `rounded-[32px] md:rounded-[48px]` con grid asimétrico de 12 columnas (7 izquierda / 5 derecha).
- **Columna Izquierda (7 cols - Copywriting de Conversión Directa):**
  - Badge: *"Sistema Operativo de Abastecimiento B2B"*.
  - H1: *"Tu Inventario con 94.5% de precisión y pago en Kuotas"* (con gradiente esmeralda en *"pago en Kuotas"*).
  - 3 Líneas directas de impacto:
    1. *"Anticipa quiebres de stock 30 días antes con el copiloto Nexo IA."*
    2. *"Genera hasta S/ 48,600 en ahorro operativo y libera tu caja."*
    3. *"Conecta tu tienda o ERP en minutos. Aplican T&C transparentes."*
  - CTA Principal: Botón cápsula negro `Empezar a Kuotear` + enlace `Ver Demo en Vivo →`.
  - Micro-trust badge: Sellos de moneda `S/`, inteligencia `IA` y respaldo institucional `V`.
- **Columna Derecha (5 cols - Monolito Oscuro Escénico):**
  - Tarjeta en `#0A0F14` con auroras radiales esmeralda y cian.
  - Orbe cristalino de **Nexo IA** levitando suavemente (`y: [0, -10, 0]`) con halo giratorio a 20s.
  - Tarjeta flotante interactiva: *"Orden de Compra Aprobada — S/ 15,400"*.
  - Alerta temprana: *"Quiebre en 30 días — Reposición de 48 unidades"*.
  - Footer de la tarjeta con métricas clave: *0 Días sin quiebres*, *30, 60, 90d en Kuotas*, *S/ 0.00 Membresía*.

---

### SECCIÓN 3: El Sistema Orbital Concéntrico (`OrbitalMetricsSection.tsx`)
- **Geometría:** 3 círculos concéntricos perfectos con radios polares $R_1 = 145\text{px}$, $R_2 = 225\text{px}$, $R_3 = 305\text{px}$ centrados con cálculo trigonométrico:
  $$x = R \cdot \cos(\theta), \quad y = R \cdot \sin(\theta)$$
- **Ciclo Automático de 3 Diapositivas (Auto-play cada 5s):**
  - **Slide 1 (+20):** Módulos de IA predictiva (8 nodos orbitando con íconos vectoriales y tooltips interactivos: Pronóstico 90d, Alerta de Quiebre, Órdenes Automáticas, QR de Almacén, WhatsApp Cloud, ERP/SAP, Analítica SKU, BNPL).
  - **Slide 2 (100%):** Tecnología B2B respaldada por arquitectura institucional (7 avatares de gerentes y negocios reales: Ferretería Industrial, Distribuidora Textil Gamarra, Cadena Minimarkets, etc.).
  - **Slide 3 (+ S/ 48M):** Impacto financiero y mercadería abastecida (8 burbujas de impacto monetario: `+S/ 48,600 ahorro`, `0 quiebres`, `2.4x rotación`, `1.8% fijo`).
- **Controles:** Botón central `Solicitar Línea Kuota` + paginador de 3 puntos donde el activo es una cápsula expandible con barra de progreso verde del 0% al 100% sincronizada con el temporizador de 5 segundos.

---

### SECCIÓN 4: Showcase Asimétrico Sticky con Aro Cónico (`StickyFeatureShowcase.tsx`)
- **Layout:** Macro-tarjeta blanca con sistema de visualización sticky.
- **Columna Izquierda (Sticky 50%):** Monolito en `#070A0F` con **aro cónico con rotación continua en 360°** (`conic-gradient` de `#10B981` a `#06B6D4`), mockup dinámico de la app Kuota y notificaciones flotantes con glassmorphism (`⚡ Alerta Nexo`, `🛒 Kuotear S/ 4,520`).
- **Columna Derecha (4 Pasos Narrativos Clicables):**
  1. *Cerebro IA:* Clasificación ABC dinámica y prevención de stock inmovilizado.
  2. *Financiamiento BNPL B2B:* Kuoteo de facturas de proveedores a 30, 60 o 90 días con liquidez en 1 clic.
  3. *Conectividad:* Órdenes automáticas por WhatsApp y sincronización con Shopify, WooCommerce y SAP.
  4. *Control de Almacén:* Trazabilidad multi-sucursal, control de mermas y auditoría QR.

---

### SECCIÓN 5: Carrusel Coverflow 3D de Video-Historias (`VideoStoriesCarousel.tsx`)
- **Contenedor:** Monolito oscuro `#070A0F` con resplandor radial esmeralda.
- **Titular:** *"Conoce cómo hacemos que abastecer tu empresa sea más fácil"*.
- **Carrusel Reels 9:16:**
  - Tarjeta central (activa): escala 100%, borde iluminado `#10B981`, botón circular de reproducción y datos de impacto comercial comprobado.
  - Tarjetas laterales: escala 86%, opacidad 35%.
  - Controles flotantes circulares `<` y `>` para alternar casos con física `spring`.
  - Modal interactivo para visualizar reportajes de casos de estudio reales.

---

### SECCIÓN 6: Gamificación y Niveles 3D (`LevelsGamification3D.tsx`)
- **Contenedor:** Monolito `#080C14` con resplandor turquesa central.
- **Pedestales 3D e Insignias Flotantes:**
  1. **Insignia Plata (`S/`):** Nivel Impulso (hasta S/ 15,000 para capital de trabajo base).
  2. **Insignia Central Oro/Esmeralda (`200K`):** Nivel Maestro (hasta S/ 200,000+ con tasa preferencial institucional).
  3. **Insignia Turquesa (`50K`):** Nivel Expansión (hasta S/ 50,000 con 60 días de financiamiento).
- **Línea de Tiempo Conectora:** Línea horizontal interactiva que se ilumina dinámicamente y actualiza los requisitos, plazos de pago y bonificaciones de cashback B2B.

---

### SECCIÓN 7: Doble Marquesina Infinita (`TestimonialsMarquee.tsx`)
- **Layout:** Sección sobre lienzo claro `#F4F6F8` con titular *"Tu operación, nuestra inspiración"*.
- **Mecánica:** Fila 1 hacia la izquierda, Fila 2 hacia la derecha en desplazamiento continuo a 60 FPS, pausa en `hover` y máscaras de desvanecimiento lateral en gradiente.
- **Contenido:** 10 reseñas verificadas de comercios y distribuidoras de Lima Norte, Gamarra, Surco, Arequipa, Trujillo, Huancayo y Piura.

---

### SECCIÓN 8: Radar B2B / Carrusel Editorial (`EditorialBlogCarousel.tsx`)
- **Layout Split 4/8:**
  - Columna izquierda: Título *"Nuestro Blog"*, descripción de gestión de compras y controles de navegación `<` `>`.
  - Columna derecha: Carrusel de artículos con badges temáticos (*"IA Predictiva"*, *"Tutoriales Kuota"*, *"Finanzas B2B"*), tiempo de lectura y fecha de actualización 2026.

---

### SECCIÓN 9: Stepper Interactivo de Onboarding (`OnboardingPhoneStepper.tsx`)
- **Layout:** Grid de 12 columnas en macro-tarjeta blanca.
- **Izquierda (5 cols):** Mockup de teléfono en `#070A0F` sobre pedestal con halo circular, mostrando pantallas dinámicas sincronizadas con `AnimatePresence`.
- **Derecha (7 cols):** Lista interactiva de los 6 pasos numerados (`01` al `06`) con círculo verde menta activo y botón de solicitud de evaluación inmediata.

---

### SECCIÓN 10: Bento Grid de Transparencia Financiera (`FinancialBentoGrid.tsx`)
- **Sub-bloque A (5 Tarjetas de Condiciones Claras):**
  1. Forecast IA de Demanda: `94.5% - 99.1%`.
  2. Comisión de Kuoteo Fija: Desde `1.8% mensual` transparente.
  3. Integración Multi-Moneda: Operaciones en Soles (`S/`) y Dólares (`USD`).
  4. Plazos Flexibles: Fraccionamiento a `30, 60 o 90 días`.
  5. Costo de Membresía Base: `S/ 0.00` (acceso inicial gratuito).
- **Sub-bloque B (Respaldo y Seguridad):** Puntuación de satisfacción 4.9 ★★★★★, encriptación cloud TLS 1.3 con 99.99% uptime y sello de arquitectura cloud de alta disponibilidad.

---

### SECCIÓN 11: Preguntas Frecuentes Split (`FaqSplitAccordion.tsx`)
- **Layout Split 4/8:**
  - Izquierda sticky: Texto humano y honesto sobre transparencia financiera + botón de contacto directo por WhatsApp.
  - Derecha: Acordeones limpios en tarjetas blancas con botón circular negro y chevron rotatorio de 180° que cambia a verde esmeralda al expandirse.

---

### SECCIÓN 12: Pre-Footer Isométrico 3D y Footer Corporativo (`LayeredPreFooter.tsx`)
- **Pre-Footer:** Titular *"Menos quiebres, más liquidez"* con 4 capas isométricas translúcidas de la tarjeta Kuota levitando en cascada bajo luz cenital esmeralda y CTA blanco `Solicitar Línea Kuota`.
- **Footer Monolítico (3 Columnas):**
  - Columna 1: Transparencia y Legal (Términos, Privacidad, SLA, Ley N° 29733 de Protección de Datos Personales).
  - Columna 2: Plataforma y Ecosistema (Cerebro Nexo, Niveles, Integraciones, Radar B2B).
  - Columna 3: Contacto 24/7 (WhatsApp Ejecutivo, Correo oficial, Libro de Reclamaciones).
  - Barra inferior con copyright y sello `Kuota B2B`.

---

### WIDGETS PERSISTENTES: Captación y Admisión Rápida (`FloatingLeadWidgets.tsx`)
- **Botón Cápsula Verde:** Fijado en la esquina inferior derecha (`#10B981`) con icono de WhatsApp/Nexo y pulso activo.
- **Modal de Admisión Rápida:** Formulario emergente en tarjeta oscura para ingresar `RUC / DNI`, `Nombre de Empresa`, `Celular WhatsApp` y `Rubro Comercial`, conectado con pre-evaluación algorítmica y enlace de atención inmediata.

---

## 6. Eventos de Analítica y Telemetría

| Evento | Parámetros Registrados | Disparador |
| :--- | :--- | :--- |
| `nav_cta_click` | `target: evaluation_modal`, `source: navbar` | Clic en "Solicitar Línea Kuota" en el header |
| `hero_primary_cta` | `action: empezar_a_kuotear`, `viewport: desktop/mobile` | Clic en CTA principal del Hero |
| `orbital_slide_change` | `slide_index: 0/1/2`, `mode: auto/manual` | Cambio de diapositiva en el sistema orbital |
| `feature_step_select` | `step_id: cerebro/facturas/whatsapp/multisucursal` | Clic en bloque narrativo del showcase sticky |
| `video_story_open` | `story_id: 1/2/3`, `business_name: string` | Clic en video-historia coverflow |
| `level_tier_select` | `tier: impulso/expansion/maestro` | Clic en insignia de gamificación 3D |
| `onboarding_step_click`| `step_number: 1..6` | Clic en paso del stepper |
| `faq_toggle` | `faq_id: string`, `state: open/close` | Apertura o cierre de acordeón |
| `lead_form_submit` | `doc_type: ruc/dni`, `business_type: string` | Envío de formulario de evaluación rápida |
| `scroll_depth` | `depth_percent: 25/50/75/100` | Profundidad de lectura del documento |

---

## 7. Criterios de Aceptación y Validación de Calidad

1. **Fidelidad Estética:** La página alterna con precisión entre macro-contenedores blancos curvos (`rounded-[32px] md:rounded-[48px]`) y bloques oscuros profundos con iluminación radial controlada.
2. **Interactividad Fluida:** El sistema orbital cicla automáticamente cada 5 segundos y responde instantáneamente al clic manual de sus 3 puntos de progreso.
3. **Persistencia y Enrutamiento:** El botón de inicio de sesión conecta limpiamente con el flujo de autenticación del producto (`/login` / `/dashboard`) sin pantallas intermedias rotas.
4. **Cero Scroll Horizontal:** La página mantiene un ancho máximo controlado de 1280px (`max-w-7xl`) con `overflow-x: hidden` verificado en pantallas desde 360px hasta 4K.
5. **Cumplimiento Legal:** Todo cálculo y mención de tasas especifica claramente *"Aplican T&C"* e identifica la infraestructura institucional.

# KUOTA — MASTER DESIGN SYSTEM & STYLE REFERENCE
> **"High-Contrast Modern Fintech & B2B Embedded Finance Architecture"**  
> *Inspiración y Estándar de Ejecución: Tarjeta iO · Stripe · Vercel · Nubank · Apple*  
> **Ecosistema de Marca:** **KUOTA** (Plataforma Comercial / Acción de *"Kuotear"*), **NEXO IA** (Copiloto Predictivo / Orbe Cristalino), **VALT** (Infraestructura Institucional y Respaldo Corporativo).

---

## 🧭 1. FILOSOFÍA VISUAL Y RITMO CINEMATOGRÁFICO

KUOTA no es una plantilla corporativa plana ni un SaaS tradicional de formularios grises.  
Es el **Sistema Operativo de Compras e Inteligencia Predictiva** para el comercio en Latinoamérica. 

Su lenguaje visual combina la **sofisticación geométrica de las mejores fintechs globales** con la **calidez, tracción y dinamismo del comercio real**.

### El Principio de Alternancia (Ritmo Claro / Oscuro)
La experiencia NO es un lienzo plano y continuo. Está construida mediante **Macro-Contenedores Curvos ("Bento Macro-Cards")** que alternan con cadencia cinematográfica:
1. **Lienzo Base del Documento (Canvas):** `#F4F6F8` (Gris hielo ultra limpio) con micro-matices cálidos en áreas con interacción comercial.
2. **Superficies Claras (Light Macro-Cards):** `#FFFFFF` puro con esquinas pronunciadas (`rounded-[32px] md:rounded-[48px]`), bordes sutiles `border border-slate-200/80` y sombras volumétricas suaves `shadow-[0_8px_30px_rgb(0,0,0,0.03)]`.
3. **Superficies Monolíticas Oscuras (Dark Monoliths):** `#070A0F` y `#0A0F14` profundo con efectos de iluminación radial interna (auroras suaves en Verde Kuota `#8FD694` / `#10B981`, Cian `#06B6D4` y Azul/Índigo Nexo `#38BDF8` / `#6366F1` con `blur-[90px]` a `blur-[120px]`).
4. **Cápsulas Interactivas (Pill Buttons):** Todos los botones y selectores adoptan silueta de píldora continua (`rounded-full px-6 py-3` o `px-8 py-4`), invirtiendo su cromatismo según el fondo:
   - *Sobre fondos claros:* Cápsula negra profunda (`bg-[#0B0D10] text-white hover:bg-[#1E293B]`).
   - *Sobre fondos oscuros:* Cápsula blanca pura (`bg-white text-[#070A0F] hover:bg-[#8FD694]`).

---

## 🎨 2. SISTEMA DE TOKENS — PALETA CROMÁTICA

| Token | Hex / Valor | Nombre | Rol Semántico |
| :--- | :--- | :--- | :--- |
| `--color-canvas` | `#F4F6F8` | Ice Canvas | Fondo base del viewport general y separador entre macro-bloques |
| `--color-surface-white` | `#FFFFFF` | Pure White | Superficie de las macro-tarjetas bento luminosas y modales |
| `--color-monolith-black` | `#070A0F` | Void Black | Superficie base de los bloques monolíticos oscuros y header sticky |
| `--color-monolith-deep` | `#0A0F14` | Carbon Black | Superficie para tarjetas escénicas internas, pedestales y notch mockup |
| `--color-kuota-mint` | `#8FD694` | Kuota Mint | Acento de marca, insignias, acentos en logotipo y highlights |
| `--color-kuota-emerald` | `#10B981` | Emerald Signal | Señal de aprobación, estados óptimos, halos orbitales y botones primarios |
| `--color-nexo-cyan` | `#06B6D4` | Nexo Cyan | Copiloto predictivo, aros cónicos, forecast y sincronización ERP |
| `--color-nexo-blue` | `#38BDF8` | Sky Glow | Iluminación cenital secundaria, orbe Nexo y badges técnicos |
| `--color-valt-indigo` | `#6366F1` | Valt Indigo | Respaldo institucional, integraciones corporativas y multimoneda |
| `--color-slate-900` | `#0F172A` | Deep Charcoal | Tipografía de display principal sobre fondos claros y bordes |
| `--color-slate-600` | `#475569` | Muted Body | Texto de párrafos secundarios y subtítulos explicativos |
| `--color-hairline-dark` | `rgba(255,255,255,0.10)` | Hairline White | Separadores, bordes de cápsulas y líneas de mockups en bloques oscuros |
| `--color-hairline-light` | `rgba(15,23,42,0.08)` | Hairline Slate | Bordes perimetrales de las macro-tarjetas blancas |

---

## ✍️ 3. SISTEMA DE TIPOGRAFÍA Y JERARQUÍA

### Familias Tipográficas
1. **Titulares y Display:** `Plus Jakarta Sans` (pesos `600 SemiBold`, `700 Bold`, `800 ExtraBold`).
   - Tracking ultra ajustado: `tracking-tight` (`-0.03em` a `-0.04em`).
   - Interlineado compacto: `leading-[1.08]` a `leading-[1.12]` en titulares grandes.
   - Énfasis en titulares: Las 2 o 3 palabras finales clave se destacan con degradado esmeralda/menta (`bg-clip-text text-transparent bg-gradient-to-r from-[#8FD694] via-[#34D399] to-[#2DD4BF]`) o subrayado orgánico.
2. **Cuerpo, UI y Números Financieros:** `Inter` o `Plus Jakarta Sans` con propiedad obligatoria `tabular-nums` para todas las cifras, tasas (`1.8%`), montos (`S/ 15,400`), días (`30, 60, 90d`) y métricas predictivas.
3. **Identificadores Técnicos:** `JetBrains Mono` / `ui-monospace` para versiones (`v2.4 Live`), badges de nivel (`Nivel 01`), códigos PO (`#PO-2026`) y SLAs (`99.99%`).

### Escala Tipográfica (Desktop & Mobile)

| Nivel | Tamaño (Desktop) | Tamaño (Móvil) | Peso | Interlineado | Tracking | Aplicación |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Hero H1** | `54px` (`3.375rem`) | `32px - 38px` | `700 / 800` | `1.08` | `-0.035em` | Titular principal del Hero |
| **Section H2**| `42px - 44px` | `26px - 30px` | `700` | `1.12` | `-0.03em` | Títulos de secciones macro |
| **Bento H3**  | `22px - 26px` | `18px - 20px` | `700` | `1.20` | `-0.02em` | Cabeceras de tarjetas internas |
| **Subhead**   | `16px - 18px` | `14px - 15px` | `500` | `1.50` | `0em` | Subtítulos de valor bajo titulares |
| **Body UI**   | `14px` (`0.875rem`) | `13px` | `400 / 500` | `1.55` | `0em` | Párrafos, reseñas y explicaciones |
| **Captions**  | `11px - 12px` | `10px - 11px` | `600` | `1.40` | `+0.02em` | Badges, tags de estado, tooltips |
| **Big Number**| `64px - 76px` | `48px - 56px` | `800` | `1.00` | `-0.04em` | Métricas orbitales (`+20`, `100%`, `48M`) |

---

## 📐 4. FORMAS, RADIOS Y ELEVACIONES (BENTO MACRO-GEOMETRY)

En lugar de tarjetas genéricas de 8px o 12px, KUOTA adopta la arquitectura de **Macro-Contenedores Curvos**:

```text
┌────────────────────────────────────────────────────────────────────────┐
│  MACRO-CARD EXTERNA: rounded-[32px] md:rounded-[48px]                  │
│  border border-slate-200/80 (light) o border-white/10 (dark)          │
│  mx-3 sm:mx-6 lg:mx-auto max-w-7xl                                     │
│                                                                        │
│   ┌────────────────────────────────┐   ┌───────────────────────────┐   │
│   │ TARJETA INTERNA:               │   │ PÍLDORA INTERACTIVA:      │   │
│   │ rounded-[24px] md:rounded-[28px│   │ rounded-full px-6 py-3    │   │
│   │ bg-slate-50 o bg-white/5       │   │ shadow-lg active:scale-95 │   │
│   └────────────────────────────────┘   └───────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

- **Macro-Contenedores:** `rounded-[32px]` en móviles, `rounded-[48px]` en desktop (`3rem`).
- **Tarjetas Bento Internas:** `rounded-[20px]` a `rounded-[28px]`.
- **Botones y Badges:** Silueta continua `rounded-full` (`9999px`).
- **Sombras en Superficies Claras:** `shadow-[0_8px_30px_rgb(0,0,0,0.03)]` y `shadow-[0_20px_50px_rgba(0,0,0,0.05)]`.
- **Resplandores en Superficies Oscuras:** `shadow-[0_0_50px_rgba(16,185,129,0.25)]` y halos difusos con `blur-[90px]` a `blur-[120px]`.

---

## 🏛️ 5. ESTÁNDARES DE LOS 12 COMPONENTES MAESTROS

### 1. Header con Cápsula Flotante (`NavbarCapsule.tsx`)
- **Estado Sticky:** Fijo al scroll (`fixed top-0 inset-x-0 z-50`). Al desplazarse, aplica `backdrop-blur-md bg-[#070A0F]/95` y borde `border-white/10`.
- **Centro (La Cápsula):** Contenedor en `#161B22` con borde `border-white/10` y selector activo con deslizamiento elástico Framer Motion (`layoutId="nav-pill"`).
- **Acciones:** Login directo a panel `/dashboard` y CTA blanco `Solicitar Línea Kuota`.

### 2. Hero Asimétrico Split 7/5 (`HeroBentoSplit.tsx`)
- Macro-tarjeta blanca con grid de 12 columnas.
- Izquierda (7 cols): Copywriting directo con las 3 líneas de valor sin viñetas y botón cápsula negro.
- Derecha (5 cols): Escena de orbe cristalino de **Nexo IA** levitando en `y: [0, -10, 0]` a 4s, rodeado de cajas de stock y notificación flotante en tiempo real: *"Orden Aprobada — S/ 15,400"*.

### 3. Sistema Orbital Concéntrico (`OrbitalMetricsSection.tsx`)
- 3 anillos concéntricos proporcionales ($145\text{px}$, $225\text{px}$, $305\text{px}$) con borde `border-slate-200/70`.
- 3 diapositivas orbitales con rotación desfasada de nodos y temporizador inteligente de **5 segundos**.
- Paginador inferior con cápsula expandible rellena en verde (`#10B981`) de 0% a 100% sincronizada con el tiempo.

### 4. Showcase Asimétrico Sticky (`StickyFeatureShowcase.tsx`)
- Visualizador izquierdo sticky en `#070A0F` con **aro cónico con rotación continua** (`conic-gradient`) de `#10B981` a `#06B6D4`.
- Mockup dinámico de Kuota con notificaciones con efecto glassmorphism en parallax (`⚡ Alerta Nexo`, `🛒 Kuotear S/ 4,520`).
- 4 bloques narrativos clicables a la derecha (Cerebro IA, Facturas BNPL, WhatsApp/ERPs, Control de Almacén).

### 5. Carrusel Coverflow 3D 9:16 (`VideoStoriesCarousel.tsx`)
- Bloque oscuro `#070A0F` con halo radial esmeralda.
- Carrusel de video-historias en formato vertical de Reels/TikTok B2B:
  - Tarjeta central (activa): escala 100%, borde iluminado `#10B981`, botón de play con pulso sutil y caso verificado.
  - Tarjetas laterales: escala 86%, opacidad 35%.
  - Controles laterales flotantes `<` y `>` para navegación con física `spring`.

### 6. Gamificación de Niveles 3D (`LevelsGamification3D.tsx`)
- Bloque `#080C14` con tres pedestales y escudos flotando a diferentes alturas:
  - **`S/` (Impulso):** Plata/Verde hasta S/ 15,000.
  - **`200K` (Maestro VALT):** Oro/Esmeralda corporativo hasta S/ 200,000+.
  - **`50K` (Expansión):** Turquesa hasta S/ 50,000.
- Línea de tiempo horizontal conectora que se ilumina dinámicamente y actualiza los requisitos, plazos y cashback B2B.

### 7. Doble Marquesina Infinita (`TestimonialsMarquee.tsx`)
- Fila 1 hacia la izquierda, Fila 2 hacia la derecha.
- Animación CSS infinita a 60 FPS con pausa automática en `hover` y máscaras laterales suaves:
  `mask-image: linear-gradient(to right, transparent, black 10%, black 90%, transparent)`.
- 10 testimonios de comercios reales peruanos con métricas de impacto comprobables.

### 8. Radar B2B / Carrusel Editorial (`EditorialBlogCarousel.tsx`)
- Layout split 4 cols (intro + navegación `<` `>`) y 8 cols (carrusel de tarjetas con badges de categoría y tiempo de lectura).

### 9. Stepper de Onboarding 6 Pasos (`OnboardingPhoneStepper.tsx`)
- Mockup móvil con notch y pantalla en tiempo real que reacciona con `AnimatePresence` al paso seleccionado (`01` a `06`).
- Números interactivos con círculo verde menta activo (`bg-[#8FD694]/30 text-[#065F46] font-bold`).

### 10. Bento de Transparencia Financiera (`FinancialBentoGrid.tsx`)
- 5 tarjetas con métricas claras (Forecast 94.5%, Comisión fija 1.8%, PEN/USD, Plazos 30/60/90d, Membresía S/ 0.00).
- 3 tarjetas de respaldo: Satisfacción 4.9 ★★★★★, Encriptación TLS 1.3 / Cloud y Sello de Innovación VALT.

### 11. Preguntas Frecuentes Split (`FaqSplitAccordion.tsx`)
- Columna izquierda sticky con redacción honesta y botón directo a WhatsApp.
- Acordeones limpios a la derecha con botones circulares negros y chevron rotatorio de 180°.

### 12. Pre-Footer 3D y Footer Corporativo (`LayeredPreFooter.tsx`)
- Titular *"Menos quiebres, más liquidez"* con 4 capas isométricas translúcidas de la tarjeta Kuota levitando en cascada bajo luz cenital esmeralda.
- Footer corporativo de 3 columnas de Transparencia, Plataforma y Contacto con sello `by VALT`.

### 13. Widgets Flotantes Persistentes (`FloatingLeadWidgets.tsx`)
- Botón cápsula verde en esquina inferior derecha (`#10B981`) con icono de WhatsApp/Nexo.
- Modal de captura rápida con RUC/DNI, teléfono y rubro para pre-evaluación algorítmica sin fricción.

---

## 🚫 6. REGLAS INQUEBRANTABLES (DO'S AND DON'TS)

### ✅ Lo que SÍ se debe hacer
- **Mantener el ritmo visual alternante:** Cada macro-contenedor debe tener un propósito visual claro (luz vs. monolito oscuro).
- **Usar números tabulares (`tabular-nums`):** Todas las tasas (`1.8%`), cifras monetarias (`S/ 15,400`), días (`30d`) y métricas deben usar fuentes monoespaciadas o tabulares para evitar saltos de layout.
- **Botones siempre en cápsula (`rounded-full`):** Todo botón interactivo es una píldora con padding horizontal generoso (`px-7 py-3.5`).
- **Respaldo institucional transparente:** Siempre incluir el sello `by VALT` con su badge sutil en el Navbar y Pre-Footer.
- **Garantizar 60 FPS y cero scroll horizontal:** Toda animación espacial debe usar aceleración por GPU (`transform: translate3d`) y anchos máximos acotados (`max-w-7xl` con `overflow-x-hidden`).

### ❌ Lo que NUNCA se debe hacer
- **NO usar esquinas afiladas o radios pequeños (8px/12px) en macro-secciones:** Rompe la identidad visual moderna de Kuota. Las macro-tarjetas son de `32px` a `48px`.
- **NO usar gradientes saturados arbitrarios tipo "neón de los 90s":** Los gradientes en Kuota son controlados, en tonos menta (`#8FD694`), esmeralda (`#10B981`) y cian (`#06B6D4`).
- **NO inventar tasas falsas, aprobación bancaria automática ni promesas desmedidas:** Comunicar siempre *"Aplican T&C"* y mantener la transparencia comercial respaldada por VALT.
- **NO bloquear la navegación táctil en celulares:** En dispositivos táctiles, deshabilitar efectos de tilt 3D excesivos con `@media (pointer: coarse)` y priorizar el tap instantáneo (`touch-action: manipulation`).

---

## 💻 7. TOKENS CSS Y TAILWIND

```css
:root {
  /* Lienzo y Superficies */
  --canvas-base: #F4F6F8;
  --surface-card-light: #FFFFFF;
  --surface-monolith-black: #070A0F;
  --surface-monolith-card: #0A0F14;
  
  /* Acentos de Marca Kuota & Nexo */
  --kuota-mint: #8FD694;
  --kuota-emerald: #10B981;
  --nexo-cyan: #06B6D4;
  --nexo-blue: #38BDF8;
  --valt-indigo: #6366F1;
  
  /* Radios Geométricos */
  --radius-macro-mobile: 32px;
  --radius-macro-desktop: 48px;
  --radius-bento-inner: 24px;
  --radius-pill: 9999px;
  
  /* Tipografía */
  --font-display: 'Plus Jakarta Sans', system-ui, sans-serif;
  --font-body: 'Inter', system-ui, sans-serif;
  --font-mono: 'JetBrains Mono', ui-monospace, monospace;
}
```

```javascript
// tailwind.config.js extend
module.exports = {
  theme: {
    extend: {
      colors: {
        kuota: {
          mint: '#8FD694',
          emerald: '#10B981',
          cyan: '#06B6D4',
          monolith: '#070A0F',
          carbon: '#0A0F14',
          canvas: '#F4F6F8',
        },
      },
      borderRadius: {
        'macro': '48px',
        'macro-sm': '32px',
        'bento': '24px',
      },
    },
  },
};
```

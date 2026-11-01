# PRD — Landings Kuota v2 (central, proveedores, compradores)

> Complementa `PRD_Kuota_App_v6.md`. Diseño: `DESIGN.md`. Dominio principal: **kuota.pro** (proteger; no usar `.site` como principal).
> Objetivo único: **capturar leads de waitlist con consentimiento** y agendar reuniones con proveedores. No hay versión web de la app: el CTA final futuro es Google Play.

---

## 1. Reglas globales

**Qué NO decir (legal/comercial):** fecha de lanzamiento, descuentos o promociones, "regulados/afiliados a la SBS", "guardamos tu dinero", aprobación o monto garantizado de crédito, nombres de la competencia (usar "POS tradicional", "prestamistas informales").
**Qué SÍ decir:** "En proceso de afiliación a la SBS" · "Respaldo: Científica del Sur" · "Los pagos pasan por plataformas intermediarias con relación directa con el banco. Kuota no toca tu dinero" · "Crédito sujeto a evaluación" · "Lanzamiento progresivo".

**Diseño:** tokens de `DESIGN.md`. Hero en Inter Black 900 (89–105px escritorio, 44–52px móvil), tracking −0,03em, mayúsculas solo en hero. Ritmo de secciones: blanco → LinenMist → ForestInk (texto lima). CTA primario pill lima con texto ForestInk; secundario pill blanco con borde ForestInk. Un elemento lima por pantalla. Sin degradados ni sombras duras. Animaciones: reveal al scroll (fade+translate 24px, 400 ms), contadores animados, paso a paso de 5 pasos con progreso sticky, `prefers-reduced-motion` respetado.

**Técnico:** HTML/CSS/JS estático (o Next export) desplegable en **Hostinger**; Firebase JS SDK solo para `joinWaitlist` y Analytics. App Check con reCAPTCHA v3 en el formulario. Lighthouse ≥ 90 (rendimiento, accesibilidad, SEO). Imágenes WebP, fuentes Inter autoalojadas, JS < 150 KB.

---

## 2. Landing central — `kuota.pro`

**Propósito:** explicar el producto en 30 s y derivar al perfil correcto.

| # | Sección | Contenido |
|---|---|---|
| 1 | Hero | Titular: "Haz que tus ventas financien tus compras." Sub: "Compra y vende al crédito. Sin que nadie arriesgue su negocio." CTAs: **Soy comprador** (lima) · Soy proveedor (secundario). Captura real de la app (Home). |
| 2 | Problema | 3 cifras del documento de Problema: 99,2% de empresas son PYME · 8 de cada 10 negocios cierran antes del año · POS cobran 3,5–5%. Frase: "Quien trabaja cada día merece confianza para crecer." |
| 3 | Cómo funciona (5 pasos animados) | 1 Escanea el QR del proveedor · 2 Digita el monto · 3 Paga tu inicial · 4 Elige cuántas semanas · 5 Paga tus cuotas y sube de nivel. Cada paso con captura real. |
| 4 | Dos soluciones | **Comprar al crédito** (compradores) · **Kuota Payments** (cobros con QR/Link, 2%). Cada tarjeta lleva a su landing. |
| 5 | Niveles | Visual de 6 niveles (inicial baja y monto sube con pagos puntuales). Texto: "Empiezas en el Nivel 1. Pagar a tiempo te sube de nivel." Sin montos de niveles 3–6 (placeholder). |
| 6 | Confianza | Respaldo Científica del Sur · "En proceso de afiliación a la SBS" · "Kuota no toca tu dinero" · Cuotas fijas, sin letra chica. |
| 7 | Prueba social | Placeholder hasta tener testimonios reales (no inventar). Mostrar logos de eventos/programas solo con autorización. |
| 8 | FAQ | Seguridad, costo, SBS, qué pasa si me atraso, quién puede entrar, cuánto demora. Respuestas de las objeciones de las guías SDR. |
| 9 | CTA final + waitlist | Formulario corto (ver §5). |
| 10 | Footer | Legal, privacidad, contacto, LinkedIn de Kuota, enlace a landings. |

---

## 3. Landing compradores — `kuota.pro/comprar`

**Titular:** "Compra hoy lo que ayer no podías. Paga en cuotas semanales."
**Secciones:** hero con captura de Compra · flujo de 3 pasos en < 1 minuto · **calculadora** · niveles · "Para quién" (bodegas, abarrotes, textil, ferreterías, revendedores digitales) · costos claros · seguridad · FAQ · waitlist.

### 3.1 Calculadora de compra al crédito
**Entradas:** monto de compra (S/199 – S/700 en nivel 1; deslizador y campo), nivel (selector 1–2, resto "próximamente"), plazo (chips 15 / 30 / 60 días).
**Fórmulas (céntimos internos, redondeo al céntimo):**
```
inicial   = monto × inicialPct(nivel)          // N1 30%, N2 25%
financiado= monto − inicial
interés   = financiado × 5%                    // FIJO, no depende del plazo ni del # de cuotas
total     = financiado + interés               // a pagar en cuotas
cuotas    = plazo/7 → 15d:2 · 30d:4 · 60d:8 semanales
cuota     = total / cuotas  (última cuota absorbe residuo)
```
**Salida:** tarjeta con inicial hoy, interés fijo en S/, total en cuotas, calendario de cuotas (semana 1…n), nota: **"El interés es fijo (5% del monto financiado, tope bimestral) y no aumenta aunque elijas más cuotas."**
**Ejemplo precargado:** S/1.000 → inicial 300 · financiado 700 · interés 35 · total 735 · 4 cuotas de S/183,75. (Con tope de nivel 1 en S/700 el ejemplo precargado debe ser S/700 → inicial 210 · financiado 490 · interés 24,50 · total 514,50 · 3 cuotas de S/128,62 y la última de S/128,64, porque la última absorbe el residuo.)
**Reglas:** mínimo S/199; si el monto supera el tope del nivel, mensaje "Con el Nivel 1 puedes comprar hasta S/700"; 90 días no se muestra.
**Aviso:** "Simulación referencial. El crédito está sujeto a evaluación."

---

## 4. Landing proveedores — `kuota.pro/proveedores`

**Titular:** "Vende al crédito sin riesgo de impago. Cobra con tarjeta, Yape y Plin con menos comisión."
**Secciones:** hero con captura de "Mi QR" · dos productos (A: **Kuota Payments**; B: **Vender a Kuotas**, "próximamente") · cómo cobras (QR y Link por WhatsApp) · **calculadora de ahorro** · planes · liquidación 48 h hábiles · seguridad y respaldo · FAQ de objeciones · **Calendly + waitlist**.

### 4.1 Planes y condiciones visibles
- **Kuota Payments:** 2% por transacción, sin costos de POS · Plan Basic S/80 + IGV/mes, hasta S/5.000 por transacción, mínimo S/199 · Plan Pro S/120 + IGV/mes, más de S/5.000, cobranza masiva, 5 usuarios · liquidación 48 h hábiles · medios: tarjetas BCP, BBVA, Interbank y Scotiabank, Yape y Plin.
- **Vender a Kuotas (aKuotas!) — "lo que viene":** el proveedor vende a clientes sin asumir riesgo de impago. Plan Open S/99 + IGV/mes. No cotizar ni prometer.
- Sin descuentos. Sin fecha.

### 4.2 Calculadora de bolsillo (Payments)
**Entradas:** ventas con tarjeta/QR al mes (S/), comisión actual de su POS (%, por defecto 3,5), alquiler del POS (S/, opcional).
```
suscripciónConIGV = 80 × 1,18 = 94,40
ahorro = ventas × (comisiónPOS − 2%) + alquiler − 94,40
equilibrio = 94,40 / (comisiónPOS − 2%)       // con POS 3,5% ≈ S/6.293
```
**Salida:** ahorro mensual estimado (S/), punto de equilibrio, y **si ventas < equilibrio:** cambiar el mensaje a "Aceptar tarjeta sin POS y cobrar por link a clientes de WhatsApp = ventas nuevas" (no mostrar ahorro negativo).
Ejemplo: S/20.000 con POS 3,5% → ahorro S/205,60.

### 4.3 Tarjeta "Vender al crédito" (informativa)
Ejemplo visual: venta S/1.000 → recibes S/980 (2% de comisión) · S/600 al instante · el resto en pagos semanales en menos de 30 días · "El riesgo de impago lo asume Kuota." (Valores desde `config/fees`.)

---

## 5. Waitlist (compartida)

**Campos:** nombre · negocio · RUC (opcional) · rubro (chips: bodega/abarrotes, textil, ferretería, autopartes, otro) · zona/distrito · WhatsApp · rol (comprador / proveedor / ambos) · **checkbox de consentimiento** (obligatorio, enlace a privacidad) · honeypot anti-bot.
**Flujo:** envío → Cloud Function `joinWaitlist` (valida, normaliza teléfono +51, deduplica por teléfono/RUC, guarda en `waitlist/{id}` con `source`, `utm_*`, `consent: true`, `consentAt`) → redirige a **/gracias**.
**Página /gracias:** mensaje del equipo ("Gracias por confiar en Kuota. Te mantendremos al tanto con novedades reales."), qué esperar (lanzamiento progresivo, sin fechas), botón "Agendar reunión de 30 min" (proveedores) y enlace de baja. Evento de conversión `generate_lead`.
**Email/WhatsApp de confirmación** (función programada): plantilla corta con opción de baja ("Si prefieres que no te escribamos, respóndenos y te retiramos"). Cadencia posterior: 1 novedad real cada 2–3 semanas, nunca presión.
**Calendly** (proveedores): embed en /proveedores y /gracias; webhook opcional a Firestore `waitlist/{id}.meetingBooked`.

---

## 6. SEO, analítica y medición

- **Meta:** title < 60 caracteres con palabras clave ("Compra y vende al crédito para tu negocio | Kuota"), descripción < 155, Open Graph y Twitter Card con imagen de marca.
- **Datos estructurados:** Organization, FAQPage, WebSite. `robots.txt`, `sitemap.xml`, canonical.
- **Analytics (GA4 / Firebase):** `view_landing`, `use_calculator` (con parámetros no sensibles), `click_cta`, `generate_lead`, `book_meeting`. UTM guardados en el lead.
- **Embudos a medir:** visita → uso de calculadora → inicio de formulario → lead; por canal y por rubro.
- Consentimiento de cookies si se usan pixeles de terceros.

---

## 7. Criterios de aceptación

- [ ] Las 3 landings usan los tokens de `DESIGN.md` (sin azul, lima nunca como texto sobre blanco).
- [ ] Capturas reales de la app (Home, Compra, Cuotas, Mi QR); ninguna imagen inventada.
- [ ] Calculadora de compradores correcta: 5% fijo sobre lo financiado, tope bimestral, no crece con cuotas; prueba con 3 casos.
- [ ] Calculadora de proveedores con fórmula y punto de equilibrio; cambia de mensaje bajo el equilibrio.
- [ ] Waitlist guarda en Firestore con consentimiento, deduplica y redirige a /gracias.
- [ ] Sin fecha de lanzamiento, descuentos, ni afirmaciones de regulación SBS.
- [ ] Lighthouse ≥ 90 y navegación accesible por teclado; `prefers-reduced-motion`.
- [ ] Despliegue en Hostinger con HTTPS y redirección www → raíz.

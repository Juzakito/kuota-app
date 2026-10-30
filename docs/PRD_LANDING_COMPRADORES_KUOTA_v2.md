# PRD — Landing Kuota para Compradores y Emprendedores (B2B Supply BNPL)
> **Producto:** KUOTA para Compradores (`kuota-ia.vercel.app?perfil=compradores`)  
> **Versión:** 2.0 (Alineada al Ecosistema Kuota + Nexo IA + Respaldo VALT)  
> **Fecha:** 29 de octubre de 2026 · **Estado:** Implementado y Aprobado para Producción  
> **Audiencia Primaria:** Emprendedores, dueños de bodegas, minimarkets, ferreterías, tiendas de ropa y pequeños comercios que compran mercadería de forma recurrente.

---

## 1. Resumen Ejecutivo y Propuesta de Valor

La landing page de **Compradores de Kuota** está diseñada para resolver el problema fundamental del comercio minorista e independiente en Perú: **quedarse sin caja para reabastecer mercadería de alta rotación**, viéndose obligados a recurrir al fiado informal, prestamistas con cobros diarios abusivos o trámites bancarios lentos que exigen garantías inmobiliarias.

### La Propuesta de Valor Kuota:
> **"Financia tu mercadería sin quedarte sin caja. Abastécete con tus proveedores mayoristas hoy y paga en cómodas kuotas semanales o mensuales, con la asistencia de tu copiloto Nexo IA y el respaldo institucional de VALT."**

### Métricas Clave de Éxito (KPIs):
- **Conversión Principal:** Clics en *"Descargar en Play Store"* o envío del formulario de pre-evaluación rápida de línea.
- **Interacción con Niveles:** % de visitantes que navegan entre los niveles de capital (`Impulso`, `Expansión`, `Maestro`).
- **Interacción con Chips de Producto:** Clics en los módulos de reabastecimiento (*"Sin papeleo"*, *"Invierte más, crece más"*, *"Cerebro de Compras"*).
- **Rendimiento Móvil:** Cero delay táctil (300ms eliminado) y LCP ≤ 2.0s en redes móviles 4G.

---

## 2. Perfil de Usuario y Diagnóstico Operativo

- **El Dueño de Negocio Peruano:** Propietario de bodega, minimarket, puesto en mercado de abastos, distribuidora textil en Gamarra o ferretería de barrio.
- **Dolor Principal:** Las ventas son diarias, pero las facturas de proveedores vencen de golpe o exigen pago al contado para acceder a precios mayoristas con descuento.
- **Barreras Tradicionales:** Los bancos tradicionales exigen balances auditados de varios años, historial crediticio intachable en Infocorp y avales hipotecarios.
- **Comportamiento Digital:** Uso intensivo de smartphone (Android dominante), cobros y pagos cotidianos con **Yape** y **Plin**, y coordinación de despachos comerciales vía **WhatsApp**.

---

## 3. Sistema de Diseño y Lenguaje Visual

Alineado con el documento rector `DESIGN.md`:
- **Geometría Bento:** Macro-tarjetas con esquinas redondeadas (`rounded-[32px] md:rounded-[48px]`), bordes limpios en `border-slate-200/80` y sombras suaves.
- **Paleta Cromática:** Lienzo base gris hielo (`#F4F6F8`) con tarjetas blancas (`#FFFFFF`), acentos en verde menta Kuota (`#8FD694`), esmeralda de aprobación (`#10B981`) y azul marino de alto contraste (`#00235C` / `#070A0F`).
- **Tipografía:** `Plus Jakarta Sans` con tracking ajustado en titulares y fuentes tabulares (`tabular-nums`) para todos los montos, tasas y plazos en soles (`S/`).
- **Navegación Táctil Optimizada:** Propiedad `touch-action: manipulation` para eliminación del retardo de pulsación en pantallas táctiles y botones cápsula con feedback táctil elástico.

---

## 4. Estructura y Flujo de Secciones

1. **Header Sticky:** Barra fija con logotipo oficial blanco de Kuota, badge `by VALT` y botón de acción directa.
2. **Hero Principal (El Hook Emprendedor):** Título de empoderamiento, promesa de liquidez y llamado a la acción.
3. **Problema vs. Solución (Comparativa de Alto Impacto):** Contraste entre el crédito informal/bancario tradicional y la agilidad de Kuota.
4. **Medios de Pago y Cobranza:** Red integrada con Yape, Plin, BCP, BBVA, Interbank y Scotiabank.
5. **Cómo Funciona Kuota (3 Chips Interactivos):** Demostración dinámica del proceso de compra en cuotas.
6. **Acompañamiento en Formalización:** Respaldo a comercios que buscan crecer de RUC 10 a RUC 20 sin trabas.
7. **Niveles de Línea Kuota (Gamificación Financiera):** Escala de niveles según puntualidad y volumen de compras.
8. **Banner Cruzado de Audiencia:** Enlace directo para aquellos comerciantes que también son proveedores mayoristas.
9. **Confianza y Respaldo Institucional:** Equipo fundador, seguridad bancaria TLS 1.3 y respaldo VALT.
10. **CTA de Cierre y Footer:** Descarga de app móvil, formulario de evaluación express y enlaces legales transparentes.

---

## 5. Especificación por Sección

### 5.1 Hero Principal
- **Titular H1:** *"Financia tu crecimiento sin apuros. Compra mercadería a kuotas."*
- **Subtítulo:** *"Confiamos en ti, emprendedor, que cada día te levantas con esfuerzo para sacar adelante a tu familia. Abastécete con tus proveedores mayoristas hoy y fracciona tus pagos sin descapitalizarte."*
- **CTA Primario:** Botón cápsula negro `Descargar en Play Store` (o enlace a pre-evaluación digital directa).
- **Elementos Visuales:** Composición con fotografía de comercio real (bodega o minimarket peruano) y mockup móvil de la app de Kuota mostrando el límite de compras disponible y las próximas cuotas a amortizar.

---

### 5.2 Problema vs. Solución
Layout de 2 tarjetas bento comparativas lado a lado:
- **Tarjeta "El Problema Tradicional" (Superficie oscura / neutra):**
  - Título: *Necesitas reabastecer mercadería, pero tu caja se quedó corta.*
  - Puntos de dolor: Intereses abusivos de prestamistas informales; bancos que te piden hipotecas; penalidades desmedidas y pérdida de ventas por falta de stock.
- **Tarjeta "Con Kuota y Nexo IA" (Borde esmeralda / alto relieve):**
  - Título: *Con Kuota, financia tus compras mayoristas en 3 clics.*
  - Beneficios: Tasa fija y transparente (desde 1.8% mensual); evaluación con tu DNI y ventas reales; copiloto Nexo IA que te avisa cuándo reponer stock antes de que se agote.

---

### 5.3 Red de Pagos Integrada
- Banner continuo a todo el ancho.
- Título: *"Pagas y amortizas tus cuotas por tu canal favorito"*.
- Logos oficiales: **Yape, Plin, BCP, BBVA, Interbank y Scotiabank**.
- En móvil: Fila fluida con scroll horizontal táctil y animación sin tirones.

---

### 5.4 Cómo Funciona: 3 Chips Interactivos con Pantalla Dinámica
Layout split con mockup móvil a la izquierda y 3 chips de selección a la derecha:
1. **Chip 1 — Sin papeleo:**
   - *Detalle:* Sin garantía inmobiliaria ni historial de Infocorp. Solo tu DNI y tu teléfono comercial afiliado a Yape o Plin.
   - *Nota:* Si ya cuentas con RUC activo o formalización, tu nivel de capital inicial es mayor.
   - *Pantalla asociada:* Pantalla de validación de identidad digital y asignación de línea en segundos.
2. **Chip 2 — Invierte más, crece más:**
   - *Detalle:* Te asignamos un nivel de inicio. Conforme compras y pagas tus kuotas puntualmente, tu línea de compra se amplía y accedes a plazos más largos.
   - *Pantalla asociada:* Visualizador de nivel con condiciones de capital y fechas de pago ordenadas.
3. **Chip 3 — Tu Cerebro de Compras con Nexo IA:**
   - *Detalle:* Sabe exactamente cuánto necesitas comprar para tu próxima campaña. Nexo IA analiza tus ventas y genera la orden sugerida para no perder clientes.
   - *Pantalla asociada:* Diagnóstico predictivo de stock y productos con alerta de reorden urgente.

---

### 5.5 Crecemos Junto a Ti (Acompañamiento a la Formalización)
- **Propósito:** Derribar el mito de que Kuota es solo para corporaciones gigantes. Kuota impulsa la transición de comercios informales hacia la formalidad rentable.
- **Copy:** *"Si aún no cuentas con RUC formalizado, entendemos lo difícil que es empezar. Kuota evalúa tu movimiento comercial real (vía boletas, catálogo o billeteras digitales) y te acompaña con herramientas de gestión para que tu negocio crezca con bases sólidas."*
- **Badge Destacado:** *"Inclusión Financiera y Crecimiento Real"*.

---

### 5.6 Niveles de Línea Kuota (Gamificación B2B)
El usuario descubre cómo su disciplina de pago le permite desbloquear mayor poder de compra:

| Nivel | Nombre del Nivel | Línea de Capital | Plazos de Pago | Tasa Mensual Referencial | Beneficio Especial |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **01** | **Nivel Impulso** | Hasta S/ 15,000 | 30 días | 2.4% mensual fija | Acceso inmediato con DNI y negocio activo |
| **02** | **Nivel Expansión** | Hasta S/ 50,000 | 30 y 60 días | 1.9% preferencial | Plazos bimestrales y bonificación en compras |
| **03** | **Nivel Maestro VALT**| Hasta S/ 200,000+ | 30, 60 y 90 días | Desde 1.4% corporativa | Compras conjuntas con descuento mayorista de fábrica |

- **Interactividad:** Switcher con pestañas clicables que iluminan la escalera de progreso y actualizan la tarjeta central con requisitos y plazos.

---

### 5.7 Confianza, Seguridad y Respaldo Institucional
- **Seguridad Tecnológica:** Encriptación de datos de grado bancario (TLS 1.3), almacenamiento seguro en AWS/GCP y estricto cumplimiento de la Ley N° 29733 (Protección de Datos Personales).
- **Respaldo Institucional:** Ecosistema respaldado por la matriz corporativa de **VALT** con estándares internacionales de innovación financiera.
- **Atención Humana y Cercana:** Soporte directo vía WhatsApp Ejecutivo y Libro de Reclamaciones virtual accesible.

---

### 5.8 CTA de Cierre y Footer
- Macro-tarjeta de cierre en alto contraste.
- Mensaje: *"Empieza a comprar en kuotas y no pares de surtir tu negocio."*
- Botones duales: `Descargar App en Play Store` y `Evaluar mi Negocio por Web`.
- Footer transparente con términos y condiciones, política de privacidad y canales oficiales.

---

## 6. Eventos de Analítica (Telemetría de Compradores)

| Nombre de Evento | Parámetros | Objetivo de Medición |
| :--- | :--- | :--- |
| `buyer_hero_download` | `device: android/other`, `origin: hero` | Clic en CTA de descarga en cabecera |
| `buyer_chip_click` | `chip_id: sin_papeleo / invierte_mas / nexo` | Interés en beneficios específicos |
| `buyer_level_change` | `level_id: impulso / expansion / maestro` | Proyección de aspiración de capital del usuario |
| `buyer_cross_banner` | `target: proveedores` | Detección de comerciantes con doble perfil (compran y venden) |
| `buyer_scroll_depth` | `depth: 25 / 50 / 75 / 100` | Retención de lectura |

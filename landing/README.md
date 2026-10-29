# Kuota — Landing Page para Usuario Final (Compradores)

Implementación oficial de la Landing Page de Kuota para compradores y emprendedores (**PRD v1.0**).

## Stack y Sistema de Diseño (`DESIGN.md`)

- **Framework:** React 18 + Vite + TypeScript + Tailwind CSS
- **Tipografía:** `Plus Jakarta Sans`
- **Paleta oficial (Tema único Light):**
  - **Dodger Blue (`#0391FD`):** CTAs principales y elementos destacados.
  - **Crayola Blue (`#2F7AEF`):** Textos y acentos secundarios.
  - **Pale Sky (`#CBE5FE`):** Fondo principal.
  - **Deep Navy (`#00235C`):** Bordes, líneas, titulares y textos de alto contraste.

## Estructura de 8 Secciones (Orden PRD §5)

1. **Hero (`HeroSection.tsx`)** — Titular H1 único, subtexto, CTA a Play Store, placeholder de emprendedor (`4:3`) y captura flotante (`9:16`).
2. **Problema y solución (`ProblemSolutionSection.tsx`)** — Comparativa lado a lado entre el préstamo tradicional y Kuota (3 clics, 5% de interés).
3. **Medios de pago (`PaymentMethodsSection.tsx`)** — Banner de tarjetas aceptadas por 360 pay: BCP, BBVA, Interbank, Scotiabank, Yape y Plin.
4. **Producto: 3 chips interactivos (`ProductChipsSection.tsx`)** — *Sin papeleo*, *Invierte más, crece más* y *Más compras*, con actualización dinámica de captura y soporte de teclado.
5. **Crecemos junto a ti (`FormalizationSection.tsx`)** — Financiamiento de formalización con badge **0% de interés en tu formalización** y un único placeholder de imagen (`4:3`), sin gráfico de pasos.
6. **Niveles a tu medida (`LevelsSection.tsx`)** — Switcher interactivo de 6 niveles, tarjeta central dinámica, escalera ascendente de 6 niveles y franja fija (**5% de interés SIEMPRE**, con límite flat bimestral).
7. **Confianza (`TrustSection.tsx`)** — Equipo (José González — CTO, Josué Lovatón — CEO, Rolando Quiroz — Advisor) e instituciones (**Científica del Sur**, extensible en `src/data/landingData.ts`).
8. **CTA final y Pie de página (`CtaFooterSection.tsx`)** — Cierre en Deep Navy con CTA a Play Store, enlaces legales y contacto.

## Ejecución local

```bash
cd landing
npm install
npm run dev
```

Compilación para producción:

```bash
npm run build
```

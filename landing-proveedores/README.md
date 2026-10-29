# Landing Page para Proveedores — Kuota (`proveedores.kuota.pe`)

Landing page de adquisición y agendamiento B2B para **proveedores y comercios en el Perú**, construida según el **PRD – Landing Page para Proveedores | Kuota v1.0** y el sistema de diseño `DESIGN.md`.

---

## Estructura de secciones (orden oficial del PRD §6)

1. **Navbar fija** (`src/components/Navbar.tsx`) — Logo oficial Kuota, anclas `#producto`, `#precios`, `#nosotros` y CTA primario `"Agenda una call"` con menú móvil colapsable.
2. **Hero** (`src/components/HeroSection.tsx`) — H1 `"No pierdas ventas por culpa de las comisiones"`, subtítulo parametrizado, CTAs `"Agenda una call"` + `"Calcula cuánto ahorras"`, mockup flotante `"POS 3,5% → Kuota 2%"` y placeholder `IMG-01`.
3. **Problema + Solución** (`src/components/ProblemSolutionSection.tsx`) — Comparativa visual de 2 tarjetas (`IMG-02` El cobro tradicional te cuesta caro vs `IMG-03` Con Kuota vendes más y te queda más).
4. **Prueba social — 360 Pay** (`src/components/SocialProofSection.tsx`) — Logos de BCP, BBVA, Interbank, Scotiabank, Yape y Plin (`IMG-04`..`IMG-09`), grilla en desktop y carrusel horizontal automático en móvil.
5. **Producto — 3 chips interactivos** (`src/components/ProductChipsSection.tsx`) — *Kuota QR* (`IMG-10`, `IMG-11`), *Kuota Link* (`IMG-12`, `IMG-13`) y *aKuotas! (para emprendedores)* (`IMG-14`..`IMG-16`, con badge `"0 riesgo"`), más sello fijo `"Liquidación en 48 horas hábiles en todas las modalidades"`.
6. **Úsalo — Calculadora de ahorro** (`src/components/SavingsCalculatorSection.tsx`) — Slider e input sincronizados de ventas mensuales con tarjeta (por defecto `S/ 20,000`), ticket promedio opcional, barra comparativa POS 3,5% vs Kuota 2%, ahorro mensual y anual en tiempo real.
7. **Confianza — Quiénes somos** (`src/components/TrustSection.tsx`) — Tarjetas de José González (CTO, `IMG-17`), Josué Lovatón (CEO, `IMG-18`), Rolando Quiroz (Advisor – 360 Pay, `IMG-19`) y respaldo de la Universidad Científica del Sur (`IMG-20`).
8. **Precios** (`src/components/PricingSection.tsx`) — Tres tarjetas (`Basic` S/ 80 + IGV, `Pro` S/ 120 + IGV recomendada y `Open` S/ 99 + IGV con escalera de niveles 1..5 `IMG-21`).
9. **CTA final con `cal.com`** (`src/components/CalCtaSection.tsx`) — 3 bullets de valor + widget `cal.com` con carga diferida (`IntersectionObserver`), selector para agendar con **José González** o **Josué Lovatón**, y enlace de respaldo `"¿No carga? Agenda aquí"`.
10. **Footer** (`src/components/FooterSection.tsx`) — Logo, contacto (`contacto@kuota.pe`), redes sociales y enlaces legales.

---

## Configuración centralizada de tasas y planes (`src/config/pricingConfig.ts`)

Todas las tasas (`kuotaCommissionPercent: 2`, `posBenchmarkPercent: 3.5`, `akuotasMaxTeaPercent: 5`) y los precios de los planes (`Basic`, `Pro`, `Open`) viven en un único archivo:

- [`src/config/pricingConfig.ts`](./src/config/pricingConfig.ts)

Si cambia la comisión o cualquier precio de plan, basta con editar ese archivo para actualizar simultáneamente los textos y cálculos de toda la landing.

---

## Desarrollo local y compilación

```bash
cd landing-proveedores
npm install
npm run dev      # levanta en http://localhost:5174
npm run build    # genera bundle estático en dist/
```

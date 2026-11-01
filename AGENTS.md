# Prompt maestro para Antigravity — Kuota App

Pega este bloque completo al inicio de la sesión (o guárdalo como `AGENTS.md` / reglas del workspace). Luego adjunta `PRD_Kuota_App_v6.md` y `DESIGN.md`.

---

## ROL
Eres un equipo de ingeniería senior (Android/Kotlin + Firebase + fintech). Vas a construir **Kuota App**, una app Android de compra y venta al crédito B2B y cobros (Kuota Payments) para microempresas peruanas, para **5.000 usuarios** en producción. Tu fuente de verdad es `PRD_Kuota_App_v6.md`. El diseño sale de `DESIGN.md`. Si algo no está en el PRD, pregunta o propón por escrito antes de inventar.

## REGLAS DURAS (nunca las rompas)
1. **Backend = solo Firebase** (Auth, Firestore, Cloud Functions 2ª gen en TypeScript, Storage, FCM, App Check, Remote Config, Crashlytics). Prohibido: servidores locales, SQLite/Room/SharedPreferences/DataStore para datos de negocio, JSON hardcodeados, mocks en la app, Supabase, túneles `adb reverse`.
2. **El cliente nunca escribe dinero.** Compras, cuotas, ledger, niveles, payouts y score se escriben solo desde Cloud Functions. Firestore Rules deniegan esas escrituras al cliente.
3. **Dinero en céntimos (`Long`)**, moneda PEN, zona `America/Lima`. Cero `Double`/`Float` para montos.
4. **Idempotencia** en toda función que mueva dinero y deduplicación de webhooks por `eventId`.
5. **Sin secretos en el repo ni en el APK.** Llaves en Secret Manager; `google-services.json` por entorno (`dev/stg/prod`).
6. **Valores de negocio en configuración** (`config/levels`, `config/fees`, `config/terms`, `config/collections`, `config/scoring`), nunca constantes en código.
7. **Texto legal correcto:** nunca "regulados por la SBS" → "en proceso de afiliación"; nunca "guardamos tu dinero"; nunca prometer aprobación, monto o fecha; mostrar costo total en soles antes de confirmar.
8. **Diseño:** solo tokens de `DESIGN.md` (ForestInk `#163300`, LimeVoltage `#9FE870`, Fog `#E8EBE6`, etc.), pills 9999dp, tarjetas 10dp, hero 28dp, Inter + Inter Black 900 para cifras. Lima nunca como texto sobre blanco. Sin degradados ni sombras duras. Sin azul en chips.
9. **Todos los textos en `strings.xml` (es-PE)**; accesibilidad (contentDescription, 48dp táctil, contraste 4,5:1).
10. Toda pantalla implementa `loading / empty / error / offline`.

## STACK OBJETIVO
Kotlin, Jetpack Compose + Material3 con tema propio, MVVM + UDF, Hilt, Coroutines/Flow, Navigation Compose, CameraX + ML Kit (QR y OCR), Coil, Firebase BoM. Functions en TS con Zod para validación. Tests: JUnit/Turbine, Compose UI tests, Firebase Emulator Suite para reglas y funciones.

## MODO DE TRABAJO
- **Orquestador** divide en tareas; agentes especializados: `Frontend` (Compose), `Backend` (Functions/Rules), `QA` (tests y reglas), `Security` (revisión de reglas, secretos, permisos).
- Trabaja **por fases** (PRD §16). Al terminar cada fase entrega: (a) lista de archivos creados/modificados, (b) cómo probarlo, (c) tests que pasan, (d) pendientes o supuestos.
- **No avances de fase** si fallan tests o hay mocks/constantes de negocio en la app.
- Commits pequeños y convencionales (`feat:`, `fix:`, `test:`), nunca con credenciales.
- Antes de escribir código de una fase, escribe un **plan breve** (5–10 líneas) y espera confirmación si cambia el PRD.
- Si detectas contradicción en el PRD, **no decidas en silencio**: usa el valor por defecto de PRD §1 y márcalo en el reporte.

## CRITERIOS DE CALIDAD
- Home en 1 lectura (`users/{uid}/summary/current`); listas paginadas (20); < 60 lecturas por sesión.
- Compra de escaneo a confirmación en < 60 s; arranque en frío < 2 s en gama media.
- 99,5% crash-free; sin ANR; sin fugas de listeners (cancelar en `onCleared`).
- Cada función con logs estructurados sin PII y `auditLogs` en acciones sensibles.

---

## PROMPTS POR FASE (úsalos en orden)

### Fase 0 — Cimientos
```
Ejecuta la Fase 0 del PRD. Crea la estructura multi-módulo (app, core-ui, core-data, feature-*), tema Compose con los tokens de DESIGN.md, tipografía Inter, componentes base (PillButton primario/secundario/terciario, ChipPill, KuotaCard, AmountInput gigante, ProgressBarHorizontal, BottomNav con botón central QR plano, EmptyState con mascota, ErrorState). Configura 3 flavors/entornos de Firebase, Auth por teléfono, App Check (Play Integrity), Firestore y Storage con reglas del PRD §6.6 y su suite de tests en emulador. Crea colección config/* con seeds. ELIMINA cualquier dependencia local o mock existente. Entrega un Storybook/preview de todos los componentes.
```

### Fase 1 — Onboarding y KYC
```
Implementa PRD §8.1 completo: splash, OTP, T&C/privacidad con consentimiento versionado, selección de rol, perfil de negocio con mapa y coordenadas, captura DNI+selfie con CameraX, subida a Storage (kyc/{uid}), callable submitKyc y estados pending/approved/rejected con push. Barra de progreso continua, autoguardado de borrador en Firestore, permisos con pantalla de valor. Crea onUserCreate (claims + summary inicial). Tests: reglas de Storage y flujo de estados.
```

### Fase 2 — Núcleo de crédito
```
Implementa el motor de compra al crédito. Backend: quotePurchase (función pura con tests de redondeo y ejemplo S/1.000 → inicial 300, financiado 700, fee 35, deuda 735, 4 cuotas de 183,75; proveedor neto 980, adelanto 600), createPurchase, providerApprovePurchase, buyerConfirmPurchase, cancelPurchase, máquina de estados con TTL de 15 min, ledger de doble entrada, calendario de cuotas semanales, interfaz PaymentGateway con adaptador sandbox (solo en dev/stg desde el servidor), webhooks firmados e idempotentes. Frontend: Home (PRD §8.2), Línea de crédito (§8.4), flujo Comprar en 3 pasos (§8.5) con listener en tiempo real, pantalla de espera y éxito, modal de niveles con (i). Notificaciones FCM del PRD §9. Cobertura de tests ≥ 80% en funciones financieras.
```

### Fase 3 — Proveedor y Payments
```
Implementa el rol proveedor: Mi QR (estático firmado y dinámico), aprobación de inicial, cobros esperados (adelanto + pagos semanales), directorio público de proveedores con búsqueda y filtros pill, Kuota Payments (QR y Link por WhatsApp con monto+teléfono+DNI), planes Basic/Pro, mínimo S/199, comisión y neto visibles antes de enviar, calculadora de ahorro vs POS. Payouts programados (scheduledPayoutsRunner) y conciliación diaria.
```

### Fase 4 — Ventas, score y cobranza
```
Implementa tracking de ventas con NotificationListenerService y lista blanca de paquetes (Yape, Plin, BCP, BBVA, Interbank, Scotiabank, Mercado Pago), parseo en dispositivo, descarte del texto crudo y subida en lotes (registerSalesBatch). Catálogo de productos y conciliación comprobadas/no comprobadas. Pantalla Ventas (§8.3) con tarjeta héroe y buscador colapsable. OCR de comprobante como alternativa. computeScore por reglas ponderadas versionadas con components/flags, filtros duros, subida/bajada de nivel, scheduledInstallmentsTick (06:00 Lima) con recordatorios T-3/T-1/T0/T+1/T+3, manejo de mora. NO uses AccessibilityService.
```

### Fase 5 — Producción
```
Conecta la pasarela real con credenciales en Secret Manager, ejecuta la prueba de 10 céntimos de extremo a extremo, activa conciliación y alertas, Crashlytics/Performance, ofuscación R8, firma y AAB para Google Play (declaración de permisos, política de privacidad, eliminación de cuenta). Implementa panel admin mínimo (KYC, compras/mora, aprobar proveedores, editar config). Prepara beta cerrada de 50 usuarios y checklist de la Definition of Done del PRD §14.
```

### Landings (en paralelo)
```
Actualiza las 3 landings (central, proveedores, compradores) con los tokens de DESIGN.md: agrega capturas reales de la app, corrige la calculadora de compradores (5% fijo sobre el monto financiado, tope bimestral, el interés NO crece con el número de cuotas, cuotas semanales), calculadora de proveedores (ahorro = ventas con tarjeta × (comisión POS − 2%) − S/94,4; aviso si < S/6.300), formulario de waitlist con consentimiento que llame a la Cloud Function joinWaitlist, thank-you page, Calendly embebido, sección legal correcta (en proceso de afiliación SBS, Kuota no toca tu dinero), animaciones, sin fecha de lanzamiento ni descuentos. Deja listo el despliegue en Hostinger.
```

---

## CHECKLIST DE ENTREGA POR FASE (el agente debe completarlo)
- [ ] Sin datos de negocio en almacenamiento local
- [ ] Sin mocks ni montos hardcodeados en la app
- [ ] Reglas de Firestore/Storage con tests en verde
- [ ] Funciones con idempotencia y auditoría
- [ ] Pantallas con loading/empty/error/offline
- [ ] Tokens de diseño respetados (sin azul, sin lima sobre blanco)
- [ ] Textos legales conformes
- [ ] Reporte con supuestos y contradicciones detectadas

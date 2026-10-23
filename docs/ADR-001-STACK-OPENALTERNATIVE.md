# ADR-001 — Infraestructura y herramientas open source para Kuota

**Fecha:** 2026-10-21  
**Estado:** Aceptado como guía de trabajo. No sustituye decisiones legales ni comerciales.  
**Ámbito:** backend, integraciones, analítica, observabilidad e IA.  
**Relacionados:** `DECISIONES_NEGOCIO_LEGAL.md`, `IMPLEMENTATION_STATUS_PRD_v5_2.md`, `backend/build.gradle.kts`.

## 1. Contexto

Kuota ya opera con una arquitectura propia: app Android en Kotlin/Compose, backend **Ktor + PostgreSQL + Flyway + HikariCP**, ledger append-only, webhooks HMAC idempotentes, sincronización de ventas con WorkManager, push/FCM y un adaptador PSP. El modo demo no otorga crédito, no cobra cuotas y no debe exponerse a internet.

Se recibió una propuesta basada en el directorio **OpenAlternative** que sugiere incorporar Supabase, PocketBase/Appwrite, n8n/Make, Flowise/LangChain, Stripe/Lemon Squeezy, Modern Treasury, PostHog/Umami/Plausible y Grafana/BetterStack.

El problema real de Kuota hoy **no es falta de herramientas**, sino:

1. Cerrar la decisión de acreedor y mora (D-01/D-02).
2. Aprobar Términos, Privacidad y contratos.
3. Definir un PSP/adquirente local y sus credenciales.
4. Endurecer el backend antes de cualquier piloto.

Añadir infraestructura ahora duplicaría autenticación, datos y despliegue, y ampliaría la superficie de ataque sin resolver esos bloqueos.

## 2. Decisión

1. **Mantener el stack actual.** No migrar a un BaaS (Supabase, PocketBase o Appwrite). Ktor y PostgreSQL cubren las necesidades del MVP y ya tienen migraciones, ledger y pruebas.
2. **No adoptar billing internacional.** Stripe y Lemon Squeezy no cobran en Perú ni encajan con el modelo de financiamiento de inventario. El cobro real exige un PSP local y validación legal.
3. **No incorporar automatización ni IA en esta fase.** n8n, Flowise y LangChain quedan para una fase posterior, con proveedor autorizado y caso de uso validado.
4. **Analítica solo con consentimiento.** Antes de un piloto, con eventos anónimos, retención definida y cumplimiento de la Ley 29733 y el DS 016-2024-JUS.
5. **Observabilidad al desplegar.** OpenTelemetry + Grafana o BetterStack cuando exista un backend desplegado en staging/producción.
6. **Autohospedaje solo con costo total y responsable.** Ninguna herramienta self-hosted se adopta sin dueño operativo, backups, actualizaciones y plan de salida.

## 3. Matriz de evaluación

| Herramienta | Rol propuesto | Decisión | Fase / condición |
|---|---|---|---|
| **Supabase** | BaaS, Postgres, Auth, RLS | No adoptar como BaaS. Evaluar solo como Postgres gestionado. | Fase 1, si se despliega el backend. Nunca exponer claves o RLS directamente al APK. |
| **PocketBase / Appwrite** | BaaS ligero para prototipos | Descartado. | Fragmentaría autenticación y datos; no apto para datos financieros. |
| **n8n / Make** | Orquestación de integraciones | Evaluar solo n8n self-hosted para back-office. | Fase 3, con POS/ERP/proveedor autorizado. No alojar lógica crítica ni credenciales de cobro. |
| **Flowise / LangChain** | Asistente conversacional o predicción | No ahora. | Fase 4, con caso de uso validado, inferencia privada, PII redactada y revisión humana. |
| **Stripe / Lemon Squeezy** | Suscripciones y cobros | Descartado. | Kuota no es una suscripción SaaS y estos proveedores no operan cobros locales en Perú. |
| **Modern Treasury** | Conciliación bancaria | Solo referencia conceptual. | El ledger append-only ya existe. La integración bancaria requiere contrato y autorización. |
| **PostHog / Umami / Plausible** | Analítica de producto | Umami o PostHog self-hosted, con consentimiento. | Fase 2, antes del piloto. Eventos anónimos, sin PII ni datos de ventas identificables. |
| **Grafana / BetterStack** | Monitoreo y alertas | Sí, al desplegar. | Fase 1. OpenTelemetry para trazas/métricas; BetterStack si no hay guardia propia. |
| **OpenAlternative** | Directorio de alternativas | Referencia, no dependencia. | Evaluar cada herramienta por separado; el directorio no es un componente técnico. |

## 4. Plan por fases

### Fase 0 — Ahora (sin nuevas herramientas)

- Cerrar D-01/D-02 y los documentos legales.
- Definir el PSP/adquirente local y el modelo de ingresos.
- Mantener el modo demo sin datos personales reales y sin backend público.
- Completar el backlog de endurecimiento: rate limiting distribuido/gateway, backups, rotación de claves, registros de auditoría, respuesta a incidentes y prueba de penetración. Ya existen JWT con secreto externo, QR firmado y límites en proceso.

**Criterio de salida:** decisiones legales y comerciales documentadas; PSP elegido con condiciones contractuales.

### Fase 1 — Staging y observabilidad

- Desplegar el backend en un entorno privado con TLS, secretos fuera del repositorio, backups y alertas.
- Incorporar OpenTelemetry y un panel de métricas (Grafana o BetterStack).
- Probar restauración de backups y simular fallos de PSP/webhooks.
- Si se necesita Postgres gestionado, evaluar Supabase/Neon solo como hosting; la app sigue hablando con la API, no con la base.

**Criterio de salida:** staging reproducible, monitoreado y con restauración verificada.

### Fase 2 — Analítica con consentimiento (pre-piloto)

- Redactar el aviso de privacidad y el consentimiento de analítica.
- Implementar Umami o PostHog self-hosted con eventos anónimos: activación, QR escaneado, oferta vista, compra demo creada, abandono.
- Definir retención, opt-out y minimización. No enviar nombres, DNI, RUC, montos ni ventas identificables.

**Criterio de salida:** consentimiento registrado y taxonomía de eventos aprobada.

### Fase 3 — Integraciones y automatización

- Conectar POS/ERP o extractos bancarios mediante un proveedor autorizado y un gateway de conectores.
- Usar n8n solo para tareas de back-office (notificaciones internas, reintentos, alertas de soporte), nunca como fuente de verdad.
- Mantener webhooks HMAC, idempotencia y trazabilidad en el backend.

**Criterio de salida:** contrato con proveedor, pruebas de integración y reconciliación auditables.

### Fase 4 — IA (solo con caso validado)

- Pronóstico de demanda o asistente operativo con datos autorizados.
- Inferencia privada o proveedor con garantías contractuales; redacción de PII.
- Evaluación, límites de uso y revisión humana antes de cualquier decisión que afecte al usuario.

**Criterio de salida:** caso de uso aprobado, evaluación de sesgo/seguridad y presupuesto operativo.

## 5. Consecuencias

**Positivas**

- No se reescribe lo que ya funciona.
- Menor superficie de ataque y menos proveedores con acceso a datos.
- El equipo se concentra en los bloqueos legales y comerciales.
- Menor costo operativo en la fase demo.

**Negativas / trade-offs**

- Sin analítica de producto ni automatizaciones avanzadas por ahora.
- Sin observabilidad de producción hasta la Fase 1.
- La deuda de endurecimiento debe atenderse antes de un piloto real.

**Disparadores para revisar este ADR**

- Firma de contrato con PSP o entidad financiera.
- Necesidad de métricas para un piloto.
- Despliegue de un backend accesible fuera de la red local.
- Crecimiento del equipo u operación que haga inviable el autohospedaje.
- Cambio en el modelo de ingresos o en el perímetro regulatorio.

## 6. Reglas para adoptar cualquier herramienta open source

- Licencia compatible con el uso comercial y sin obligaciones ocultas.
- Proyecto mantenido, con política de seguridad y respuesta a CVEs.
- Residencia de datos y posibilidad de exportarlos.
- Costo total: infraestructura, actualizaciones, backups, monitoreo y personal.
- Plan de salida documentado.
- Ninguna herramienta con acceso a dinero real o PII se adopta antes de la validación legal.
- Sin secretos en el repositorio ni en el APK.

## 7. Fuera de alcance de este ADR

Este documento **no** decide D-01/D-02, no elige PSP, no aprueba documentos legales, no autoriza producción, no fija precios y no convierte la demo en una operación real. Esas decisiones siguen pendientes en `DECISIONES_NEGOCIO_LEGAL.md`.

## Referencias

- OpenAlternative: https://openalternative.co/
- Estado de implementación: `IMPLEMENTATION_STATUS_PRD_v5_2.md`
- Decisiones de negocio y marco legal: `DECISIONES_NEGOCIO_LEGAL.md`
- Stack del backend: `backend/build.gradle.kts`

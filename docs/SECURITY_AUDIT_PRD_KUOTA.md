# AUDITORÍA DE SEGURIDAD FINTECH — KUOTA PLATFORM
**Fecha:** Octubre 2026 | **Versión:** 1.0.0-SEC  
**Ámbito:** Android Nativo (Kotlin/Compose), Cloud Firestore, Cloud Functions, Supabase PostgreSQL, Ktor Backend.

---

## 1. Mapa de Riesgos y Evaluación de Estado Inicial

| Componente | Control Actual | Vulnerabilidad / Riesgo | Nivel de Riesgo | Medida Requerida |
| :--- | :--- | :--- | :---: | :--- |
| **Firestore Rules** | `allow read, write: if true;` en colecciones principales | Acceso no autenticado y modificación arbitraria de perfiles, ventas y solicitudes KYC. | 🔴 **CRÍTICO** | Reglas de mínimo privilegio: denegar por defecto, acceso solo al propietario (`request.auth.uid == merchantId`), inmutabilidad de campos financieros y de estado. |
| **Aprobación de KYC** | Cliente envía `kycStatus` | Riesgo de que un cliente malicioso envíe `"APPROVED"` directamente a Firestore. | 🔴 **CRÍTICO** | En reglas de Firestore, forzar que solo se permita `"PENDING_REVIEW"` al crear y prohibir actualizaciones directas del estado desde el cliente. |
| **Límites de Tarjeta / Crédito** | Cálculo en centavos en cliente | Modificación arbitraria de `creditLimitCents` o saldos en base de datos. | 🔴 **CRÍTICO** | Regla de Firestore prohíbe escritura del campo `creditLimitCents` por parte del cliente. |
| **Capturas de Pantalla / Multitarea** | `FLAG_SECURE` solo activo cuando la app está bloqueada | Fugas de datos del DNI o tarjetas en la vista de aplicaciones recientes o por malware de captura. | 🟡 **MEDIO** | Activar `FLAG_SECURE` dinámicamente en pantallas sensibles (KYC, Tarjetas, PIN). |
| **Bloqueo por Inactividad** | Bloqueo al pasar a segundo plano | No existía temporizador de inactividad mientras la app permanece en primer plano abierta. | 🟡 **MEDIO** | Implementar `SessionTimeoutManager` con ventana de 5 minutos de inactividad. |
| **Firebase App Check** | No inicializado | Peticiones a Firestore originadas desde emuladores o APKs modificados no atestiguados. | 🟡 **MEDIO** | Integrar `FirebaseAppCheck` con `PlayIntegrity` para producción y `DebugAppCheck` para desarrollo. |
| **Protección de Deep Links** | Prefijo simple `kuota://` | Inyección de intents maliciosos sin validación estricta de rutas permitidas. | 🟢 **BAJO** | Whitelist estricta de rutas de navegación permitidas (`kuota://home`, `kuota://sales`, etc.). |
| **Minimización de Datos en Logs** | `Log.i` en servicios | Exposición involuntaria de DNI o datos personales en logcat. | 🟢 **BAJO** | Enmascaramiento obligatorio en logs (`****${dni.takeLast(4)}`). |
| **Supabase Mirror** | Replicación en servidor vía Cloud Functions | Verificado: Android no tiene claves de rol administrativo de Supabase (`service_role`). | 🛡️ **SEGURO** | Mantener política: claves solo en servidor y triggers idempotentes con `sync_outbox_log`. |

---

## 2. Plan de Implementación por Fases

- **FASE B:** Autenticación, Sesiones y Bloqueo Local (Inactividad, `FLAG_SECURE`, sanitización de deep links).
- **FASE C:** Fortalecimiento de Reglas de Firebase (RBAC estricto, inmutabilidad y validación de tipos) y App Check.
- **FASE D:** Protección de Operaciones y Datos Financieros (Idempotencia y anti-doble pulsación).
- **FASE E:** Protección de KYC y Datos Personales (Consentimiento Ley N° 29733, enmascaramiento).
- **FASE F:** Prevención de Fraude y Abuso (Límites de velocidad, anti-hammering).
- **FASE G:** Verificación de Replicación Segura con Supabase.
- **FASE H:** Suite de Pruebas Automatizadas y Verificación de Compilación.

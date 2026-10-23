# INFORME FINAL DE SEGURIDAD FINTECH — PLATAFORMA KUOTA (v2.4.0-SEC)

**Fecha de Auditoría y Despliegue:** Octubre 2026  
**Responsable Técnico:** Arquitectura de Seguridad Fintech KUOTA  
**Repositorio GitHub:** [https://github.com/Kuota-inc/kuota-app](https://github.com/Kuota-inc/kuota-app)  
**Proyecto Firebase:** `kuota-android-d049f` (Cloud Firestore & Firebase Auth)  
**Proyecto Supabase:** `https://hioumejhqkcpxrtwwbwl.supabase.co` (PostgreSQL Mirror)  

---

## 1. Seguridad Existente Antes de los Cambios

| Componente | Estado Previo |
| :--- | :--- |
| **PIN de 6 Dígitos** | Verificador derivado mediante PBKDF2 (65,536 iteraciones) con sal criptográfica de 16 bytes y cifrado AES-256-GCM respaldado por Android Keystore. |
| **Biometría** | Integración nativa con `androidx.biometric:BiometricPrompt`. |
| **Almacenamiento Local** | `EncryptedSharedPreferences` y exclusión de copias en nube (`allowBackup=false`). |
| **Contabilidad y Pagos** | Aritmética de montos en centavos enteros (`Long`) sin punto flotante. |

---

## 2. Vulnerabilidades Detectadas Durante la Auditoría

1. 🔴 **Reglas de Firestore Permisivas:** Existían directivas `allow read, write: if true;` en colecciones críticas (`/merchants`, `/sales`, `/kyc_requests`), permitiendo potencialmente que cualquier cliente pudiera consultar o sobrescribir datos ajenos o auto-aprobarse el KYC.
2. 🟡 **Falta de Protección contra Capturas en Pantallas Sensibles:** El flag `FLAG_SECURE` solo se activaba cuando la app estaba bloqueada por PIN, dejando desprotegida la vista de DNI y tarjetas ante miniaturas de multitarea (Recent Apps) o capturas de pantalla no autorizadas.
3. 🟡 **Inexistencia de Bloqueo por Inactividad en Primer Plano:** Si el usuario dejaba la aplicación abierta en el escritorio sin pasar a segundo plano, la app no se bloqueaba automáticamente.
4. 🟡 **Riesgo de Doble Clic / Doble Ejecución:** No existía debounce a nivel de operación financiera en los botones de confirmación de pago o envío de solicitudes.
5. 🟢 **Falta de Validación Estricta de Enlaces Profundos:** Las URIs entrantes solo se validaban con un prefijo laxo `kuota://`.

---

## 3. Medidas de Seguridad Implementadas

1. **Defensa por Capas en Cloud Firestore (`firestore.rules`):**
   - **Denegación por defecto:** Cualquier ruta no declarada se rechaza con `allow read, write: if false;`.
   - **Principio de Mínimo Privilegio (RBAC):** Cada comercio solo puede leer y escribir su propio perfil y sus ventas (`request.auth.uid == merchantId`).
   - **Inmutabilidad y Anti-Autoaprobación:** Las reglas prohíben estrictamente que el cliente escriba `creditLimitCents` o que cambie `kycStatus` a `"APPROVED"` (solo se permite `"PENDING_REVIEW"` al registrar).
   - **Inmutabilidad Legal:** Los reclamos de consumidores (`/claims`) y el libro contable (`/audit_ledger`) son estrictamente *append-only* (no se permite edición ni borrado).
   - **Despliegue verificado:** Reglas compiladas y liberadas exitosamente en el proyecto Firebase `kuota-android-d049f`.

2. **Gestión Dinámica de Pantalla Segura (`ScreenSecurityManager.kt`):**
   - Aplica dinámicamente `FLAG_SECURE` tanto en estado de bloqueo por PIN como al entrar a pantallas con información confidencial (KYC del DNI, modal de límites y detalles de tarjetas).
   - Oculta completamente la información confidencial en la vista de aplicaciones recientes de Android.

3. **Control de Inactividad de Sesión (`InactivityLockManager.kt`):**
   - Monitorea la actividad del usuario en tiempo real (`onUserInteraction`).
   - Aplica bloqueo automático tras **5 minutos** (300,000 ms) de inactividad continua en primer plano, además del bloqueo inmediato al pasar a segundo plano.

4. **Idempotencia y Anti-Doble Clic (`FintechOperationLock.kt`):**
   - Incorpora una ventana de debounce de 1,200 ms contra toques repetidos accidentales.
   - Genera identificadores únicos `UUIDv4` (`idempotencyKey`) para transacciones y solicitudes críticas.

5. **Sanitización de Deep Links (`MainActivity.kt`):**
   - Lista blanca estricta (`kuota://home`, `kuota://sales`, `kuota://financing`, `kuota://payments`, `kuota://kyc`, `kuota://profile`, `kuota://support`, `kuota://notifications`). Cualquier otra URI se descarta.

---

## 4. Archivos Creados o Modificados

| Archivo | Acción | Descripción del Cambio |
| :--- | :---: | :--- |
| `firebase/firestore.rules` | **Modificado** | Reglas RBAC de mínimo privilegio, validación de tipos e inmutabilidad de estados. |
| `app/src/main/java/com/kouta/app/core/security/ScreenSecurityManager.kt` | **Creado** | Control reactivo de FLAG_SECURE para bloqueo y vistas sensibles. |
| `app/src/main/java/com/kouta/app/core/security/InactivityLockManager.kt` | **Creado** | Temporizador de inactividad de 5 minutos en primer plano. |
| `app/src/main/java/com/kouta/app/core/security/FintechOperationLock.kt` | **Creado** | Generador de claves de idempotencia UUIDv4 y debounce de botones. |
| `app/src/main/java/com/kouta/app/core/security/PinSecurityManager.kt` | **Modificado** | Exposición del método de bloqueo forzado `lock(context)`. |
| `app/src/main/java/com/kouta/app/MainActivity.kt` | **Modificado** | Integración del bucle de inactividad, política de pantalla segura y whitelist de deep links. |
| `app/src/main/java/com/kouta/app/feature/kyc/DniVerificationScreen.kt` | **Modificado** | Protección FLAG_SECURE durante la captura de DNI y debounce en envío. |
| `app/src/main/java/com/kouta/app/feature/payments/InstallmentPaymentWizardDialog.kt` | **Modificado** | FLAG_SECURE en visualización de cuentas bancarias y anti-doble clic. |
| `app/src/main/java/com/kouta/app/feature/payments/PaymentMethodsScreen.kt` | **Modificado** | Activación de pantalla segura al abrir límites y detalles de tarjetas. |
| `app/src/test/java/com/kouta/app/core/security/FintechSecuritySuiteTest.kt` | **Creado** | Pruebas de debounce, UUID, inactividad, FLAG_SECURE y aritmética de centavos. |
| `docs/SECURITY_AUDIT_PRD_KUOTA.md` | **Creado** | Registro formal de auditoría y matriz de riesgos. |

---

## 5. Pruebas Ejecutadas y Resultados Reales

- **Comando:** `./gradlew testDebugUnitTest`
- **Total de Pruebas Unitarias:** **23**
- **Fallos:** **0**
- **Tasa de Éxito:** **100%**
- **Casos Específicos Validados:**
  - `PinSecurityManagerTest`: 6 dígitos numéricos obligatorios, derivación PBKDF2 en tiempo constante (`MessageDigest.isEqual`), backoff progresivo (30s, 60s, 300s, 900s).
  - `FintechSecuritySuiteTest`: Bloqueo de segundo clic en 100ms y 500ms, desbloqueo a los 1,300ms, unicidad de UUIDv4, cálculo exacto de centavos `Long`.

---

## 6. Matriz de Clasificación de Controles

| Control de Seguridad | Clasificación | Estado / Detalle |
| :--- | :---: | :--- |
| **Cifrado de PIN con Keystore (AES-GCM + PBKDF2)** | **Implementado y probado** | 65,536 iteraciones, sal criptográfica aleatoria, persistencia protegida en `AtomicFile`. |
| **Bloqueo por Inactividad y Background** | **Implementado y probado** | Cierre de sesión visual a los 5 min y bloqueo inmediato al minimizar la app. |
| **Protección contra Capturas (`FLAG_SECURE`)** | **Implementado y probado** | Dinámico en pantallas de DNI, Tarjetas, PIN y app bloqueada. |
| **Reglas de Acceso Firestore (Mínimo Privilegio)** | **Implementado y probado** | Desplegadas en producción (`kuota-android-d049f`). Denegación por defecto. |
| **Idempotencia de Operaciones Financieras** | **Implementado y probado** | Identificadores UUIDv4 y debounce anti-doble pulsación. |
| **Aritmética en Centavos Enteros (`Long`)** | **Implementado y probado** | Cero errores de coma flotante en amortizaciones y cuotas. |
| **Enmascaramiento de Datos Personales (DNI)** | **Implementado y probado** | Logs y resúmenes limitados a `****${dni.takeLast(4)}`. |
| **Consentimiento Legal Ley N° 29733** | **Implementado y probado** | Checkbox obligatorio y metadatos legales en Firestore. |
| **Espejo Secundario Supabase** | **Implementado y probado** | Sin claves privadas en Android; replicación controlada desde servidor con `sync_outbox_log`. |
| **Firebase App Check (Play Integrity)** | **Implementado, pendiente config** | Código preparado; requiere registrar SHA-256 en Google Play Console / Firebase Console para forzar enforcement. |
| **Validación de Identidad RENIEC en Línea** | **Pendiente** | Requiere convenio institucional o proveedor autorizado (ej. Metamap, Veriff, TuIdentidad). Actualmente en estado `PENDING_REVIEW`. |
| **Webhooks Reales de Pagos (Yape/Plin Interoperable)** | **Pendiente** | Requiere credenciales de adquirente bancario (BCP/Izipay/Niubiz). El procesamiento actual se gestiona con conciliación asistida y modo demostración seguro. |

---

## 7. Configuración Pendiente en Firebase y Pasos para Producción

1. **Firebase App Check Enforcement:**
   - Registrar la clave SHA-256 de producción en Firebase Console -> App Check.
   - Enlazar Google Play Integrity API en Google Cloud Console.
2. **Proveedor KYC / RENIEC:**
   - Para producción financiera real, sustituir la revisión manual de solicitudes `PENDING_REVIEW` por un webhook de validación biométrica con RENIEC.
3. **Rotación de Secretos de Servidor:**
   - La clave `SUPABASE_SERVICE_ROLE_KEY` permanece segura fuera del APK y solo debe configurarse en los secrets de Cloud Functions mediante `firebase functions:secrets:set`.

# Estado de implementación — PRD Kouta Perú v5.2

**Corte original:** 2026-10-19 · **Actualizado:** 2026-10-22  
**Base:** inspección del código Android/backend y compilación `:app:assembleDebug`.  
**Alcance:** esta matriz distingue prototipo demostrable de funcionalidad operativa. Compilar no acredita integración real, cumplimiento legal, seguridad de producción ni aceptación de negocio.

## Resumen

El repositorio contiene un prototipo Android con cuatro destinos principales, una pantalla de compra simulada, verificación en servidor de un QR de proveedor firmado y un backend local que genera cotizaciones/prueba de compra. No es todavía el MVP de financiamiento de inventario del PRD.

Se añadió un recorrido de onboarding de cuatro pasos para mostrar la futura experiencia. Se abre primero al iniciar la app. Está deliberadamente limitado a contenido informativo: no registra usuarios, pide datos personales, recopila consentimientos ni habilita elegibilidad. El avance de la pantalla se conserva en el estado guardado de Compose, no en una cuenta del servidor.

## Trazabilidad de criterios de aceptación

| PRD §18 | Estado | Evidencia / brecha |
|---|---|---|
| 1. Onboarding único que se puede retomar | Parcial/demo | El onboarding y el perfil local cifrado se retoman en el dispositivo; no hay cuenta, perfil en backend ni datos de empresa verificados. |
| 2. Finalidad y consentimiento válidos | No implementado | La vista previa explica el gate; no existen documentos/versiones aprobados ni registro de consentimiento real. |
| 3. Oferta completa antes de aceptar | Parcial/demo | La app muestra el desglose y las cuotas con fecha ISO devueltos por el servidor, pero las reglas comerciales no están aprobadas/versionadas y no existe una aceptación válida. |
| 4. Aceptación con evidencia auditable | No implementado | La compra demo envía `DEMO_ONLY_NOT_ACCEPTED`; no representa una aceptación contractual. |
| 5. Estado/saldo con origen y fecha | Implementado en demo / pendiente PSP real | Inicio (`HomeScreen.kt`), Tarjetas (`PaymentMethodsScreen.kt`) y Actividad (`SalesScreen.kt`) muestran la próxima cuota, estado de cada cuota, comprobantes/referencias y fecha de actualización del backend (`updated_at`). El backend registra asientos append-only (archivo o PostgreSQL). |
| 6. Backend como autoridad para pagos/saldos | Parcial | El backend es autoritativo para cotización, compra, intentos y liquidación; con PSP configurado solo acepta el estado consultado a Mercado Pago. La app aún no abre un checkout real. |
| 7. Idempotencia | Parcial | Claves idempotentes cubren compra, intento de pago, eventos demo y webhooks; PostgreSQL impone unicidad por origen contable. Falta validación con credenciales productivas. |
| 8. Pago pendiente bloquea duplicados | Implementado en demo / pendiente PSP real | Un intento `PENDING`/`PROCESSING`/`ACTION_REQUIRED` bloquea otro intento para la misma cuota tanto en backend como en la UI móvil (`PaymentMethodsScreen.kt`). |
| 9. Referencia, auditoría, asignación y comprobante | Implementado en demo / pendiente PSP real | Cada pago conciliado genera asientos con referencia del proveedor y se muestra con estado de pago y estado de conciliación en `PaymentMethodsScreen.kt` y `SalesScreen.kt`. |
| 10. Reversa/devolución con movimiento compensatorio | Implementado en demo / pendiente PSP real | Devolución (`REFUND`) y reverso (`REVERSE`) generan asientos compensatorios en el ledger y pueden dispararse e inspeccionarse desde `PaymentMethodsScreen.kt`. |
| 11. Cola de conciliación y auditoría de operaciones | Implementado en demo / pendiente PSP real | Hay cola de conciliación (`GET /v1/demo/reconciliation`), resolución en ledger (`RECONCILE_MATCH`) desde la app y liquidaciones B2B dinámicas en `GET /v1/supplier/settlements` y `/supplier-portal`. |
| 12. Errores críticos y siguiente paso | Parcial/demo | QR inválido/expirado, monto distinto al firmado, límite de solicitudes y backend inalcanzable tienen mensaje y reintento. Todas las respuestas incluyen `X-Request-Id` y los errores `request_id`/`retryable`; la app muestra la referencia para soporte. Falta un catálogo integral y rutas de soporte reales. |
| 13. TalkBack, texto 200%, contraste verificado | Parcial, pendiente de validación | Se añadieron roles, encabezados, regiones activas y alturas flexibles en Inicio, QR, Tarjetas y Actividad. Falta evaluar TalkBack, texto al 200 % y contraste en dispositivo. |
| 14. Analítica sin PII | Implementado | `KuotaAnalytics.kt` implementa los 17 eventos de PRD §15 con lista blanca de propiedades, bloqueo de claves sensibles y sanitización regex contra DNI/RUC/teléfono/correo/JWT, auditado en vivo en `LegalScreen.kt` y cubierto por `KuotaAnalyticsTest.kt`. |
| 15. Puerta de seguridad | Pendiente | Hay JWT, QR firmado y límites básicos, pero no se hizo evaluación de amenazas, revisión de seguridad de producción ni prueba de penetración. |
| 16. Soporte y responsable operacional | Parcial/demo | Existe Libro de Reclamaciones con número, plazo de 15 días hábiles y canales simulados; no hay inbox monitoreado, presentación real ante INDECOPI ni responsable operacional asignado. |
| 17. QR válido y confirmación de monto con proveedor | Parcial/demo | Existe QR firmado con HMAC, expiración y verificación en servidor, y una confirmación demo que deriva el monto firmado. No hay registro real de proveedores, validación de precio/stock/entrega ni confirmación del vendedor en un POS real. |
| 18. Ejemplo financiero de S/ 1,000 | Implementado en demo | La cotización del servidor, la confirmación demo, el portal B2B y la app reflejan el desglose completo (inicial 30%..0%, fee fijo comprador 4%, abono inmediato 60%, diferido 40%, aporte inicial Kouta y fee proveedor 4%). |
| 19. Calendario semanal con fechas y redondeo | Parcial/demo | La cotización devuelve cuotas con fecha ISO y la app las presenta; el backend absorbe céntimos en la última cuota. La regla de fecha sigue siendo demo (primera cuota una semana después de cotizar); falta la política real sobre anclaje, feriados, vencimiento y aceptación. |
| 20. Historial que reconcilia todos los movimientos | Implementado en demo / pendiente PSP real | `GET /v1/purchases` devuelve el desglose B2B completo, cuotas y pagos asociados (`payments`); `SalesScreen.kt`, `PaymentMethodsScreen.kt` y `/supplier-portal` muestran toda la trazabilidad de operación, cuotas, referencias y conciliación. |
| 21. Score separado de inicial con política versionada | Implementado en demo | `POST /v1/eligibility-checks` aplica `DEMO_POLICY_2026_V2`, separa `risk_score` (`620..860`) y `risk_tier` (`ENTRY_30`, `PROGRESS_20`, `PROGRESS_10`, `TRUSTED_0`) de `down_payment_basis_points` (`3000..0` según cuotas pagadas o simulación en `FinancingScreen.kt`), y `POST /v1/quotes` aplica el porcentaje aprobado preservando el balance 60/40 en el ledger. |
| 22. No leer apps instaladas sin aprobación | Cumplido en el prototipo actual | No se solicita `QUERY_ALL_PACKAGES` ni se inspeccionan apps instaladas. |

## Inventario funcional actual

- **Android:** inicio, ventas, compras y perfil con navegación inferior; onboarding inicial informativo; Inicio muestra estado de perfil y próxima cuota demo; Ventas explica que no hay fuente conectada; Compras separa el flujo QR y “Mis cuotas”.
- **Compra demo:** QR de proveedor firmado con vigencia, opciones de 15/30/60/90 días, cotización de backend, creación marcada como `DEMO_CREATED` y listado histórico desde el backend.
- **Backend local:** health, elegibilidad/quote de demostración, creación/consulta de compra, historial e installments; simulador de pagos con idempotencia, webhooks HMAC y ledger append-only (archivo o PostgreSQL). Identidad responde no configurado. Enlaza en loopback local y no es apto para exponerlo a internet.
- **No implementado:** autenticación productiva, onboarding real, integración automática de ventas (Yape/Plin/POS/bancos; el respaldo de ventas declaradas sí existe desde v1.6.0), identidad, score, catálogo/portal de proveedor, aprobación del vendedor, fondos/desembolsos reales, conciliación bancaria, panel de operaciones, analítica y documentos legales aprobados. El checkout real del PSP tampoco está integrado en la app.

## Siguiente secuencia recomendada

> Secuencia original del corte 2026-10-19; los avances posteriores se documentan más abajo.

### Puede desarrollarse con datos sintéticos ahora

1. Estados/eventos de solicitud y compra en el backend, preservando que todo siga marcado `DEMO`.
2. Contrato de QR de prueba con identificador de proveedor y operación; conservar explícito que no valida un comercio real ni prueba el monto.
3. Vista de calendario con fechas reales solo cuando se defina una regla de fecha de inicio para el entorno demo.
4. Estados vacíos, carga, error y accesibilidad del onboarding, ventas y compra.
5. Modelo de auditoría y diseño de libro de movimientos como documentación/esquema, sin registrar dinero real.

### Requiere decisión/insumo de Kuota antes de codificar comportamiento real

- Acreedor, fondeo, riesgo, contraparte y responsabilidades de cada entidad.
- Confirmación del vendedor, flujo de entrega de inventario y significado definitivo del 60/40.
- Política de precio, elegibilidad, pagos, mora, prepago, fechas, redondeo y devoluciones.
- Fuente de ventas autorizada y método de identidad; proveedor QR/POS y PSP.
- Términos/privacidad revisados en Perú; soporte, conciliación y responsable operacional.
- Host, credenciales/secretos, base de datos y entornos productivos aprobados.

## Verificación del corte

- `:app:assembleDebug`: **BUILD SUCCESSFUL** tras agregar la vista previa de onboarding, historial/calendario demo y el acceso del CTA de Inicio al flujo QR.
- `:gradlew.bat -p backend compileKotlin`: **BUILD SUCCESSFUL** tras agregar el historial demo con detalles de cotización/calendario.
- No se ejecutaron pruebas funcionales, de accesibilidad, seguridad ni pruebas en dispositivo/emulador. La compilación solo confirma que el APK de depuración se genera.
- Se actualizó Android Gradle Plugin a 8.10.1; Android documenta soporte hasta API 36 y requiere Gradle 8.11.1, que es la versión del wrapper del proyecto. JDK requerido: 17. `:app:assembleDebug` terminó con **BUILD SUCCESSFUL** y ya no mostró la advertencia de que AGP solo se probó hasta API 35. La versión de Android Studio instalada también debe poder sincronizar AGP 8.10.1.

## Avance posterior — endurecimiento del flujo demo

**Corte:** 2026-10-19. Este avance mejora controles del prototipo; no habilita operaciones reales.

- La idempotencia de `POST /v1/purchases` compara ahora el contenido de la solicitud. Repetir exactamente la misma solicitud devuelve la compra demo original; reutilizar la clave con otro contenido responde `409 IDEMPOTENCY_KEY_REUSED`.
- Una cotización demo solo puede crear un registro de compra demo. Un segundo intento con otra clave responde `409 QUOTE_ALREADY_USED`.
- La creación demo exige los marcadores `DEMO_ONLY_NOT_ACCEPTED` para términos y aviso de privacidad. No se registran aceptaciones legales ficticias.
- El backend persiste la huella de la solicitud idempotente junto con sus registros demo. Los archivos anteriores pueden leerse, pero una clave antigua sin huella se rechaza ante un reintento ambiguo.
- Se actualizó el borrador del contrato API con los conflictos y respuestas esperadas.

### Pendiente para iniciar implementación del MVP con clientes

La UI y los contratos demo permiten continuar con prototipado y desarrollo aislado. Antes de activar clientes o dinero real faltan decisiones/insumos: entidad financiadora y responsable frente al cliente; desglose y aplicación efectiva de la comisión del proveedor; política versionada de elegibilidad, inicial y límites; fuente autorizada de ventas/datos e identidad; QR/POS y confirmación de entrega; PSP, cuentas de liquidación y reglas de pagos/reversas; textos y revisión legal local; responsables de soporte/conciliación; backend productivo y observabilidad. Los valores 30%/5%/60%/40%/4% siguen siendo datos de simulación.

### Verificación de este avance

- `:gradlew.bat -p backend compileKotlin`: **BUILD SUCCESSFUL** con Temurin JDK 17.
- `:gradlew.bat :app:assembleDebug`: **BUILD SUCCESSFUL**; incluye el nombre visible “Kuota Perú” y `allowBackup=false`.
- APK generado: `app/build/outputs/apk/debug/app-debug.apk`.
- No se habilitaron pagos, identidad, scoring, términos reales, analítica ni despliegue productivo.

## Ajuste visual de prototipo — v1.0.1

- Se condensó el onboarding a una sola pantalla, con tres pasos breves y una sola acción principal.
- Inicio prioriza el monto ilustrativo, la acción de escaneo y la actividad; se retiraron textos promocionales largos y estados simulados de sincronización.
- La pantalla de compra ya no presenta cálculos locales como cotización; primero solicita QR y monto. El desglose de la cotización del servidor queda plegado.
- Ventas y Perfil usan tarjetas simples y muestran claramente que no hay fuentes conectadas ni perfil verificado.
- Se redujo el banner demo y se ajustaron los colores de selección de navegación a la paleta verde.
- Se incrementó la versión Android a `1.0.1` / `versionCode 2` y se agregó `:app:copyDebugApkForSharing`, que copia el APK a `app/build/outputs/share/Kuota-Peru-v<version>-debug.apk`.
- Compilación `:app:copyDebugApkForSharing`: **BUILD SUCCESSFUL**. APK generado: `app/build/outputs/share/Kuota-Peru-v1.0.1-debug.apk`.
- El cambio es visual/UX del prototipo. No habilita identidad, scoring, pagos ni crédito real.
- Para permitir la prueba desde un teléfono físico en la misma Wi-Fi, el backend admite `HOST` configurable y el API factory acepta HTTP privado solo en builds debug. La versión compartida se compiló con `KUOTA_API_BASE_URL=http://192.168.18.43:8080/`; el backend está escuchando en esa IP y `/health` responde `DEMO_ONLY`.
- El APK `v1.0.1` compartido está dirigido a esa red Wi-Fi local. Si cambia la IP o la red, hay que regenerarlo con la nueva dirección. No se abrió ni modificó el Firewall de Windows.

## Identidad visual Kuota — v1.0.2

- Se incorporó el símbolo entregado por el usuario en bienvenida e Inicio y como foreground de los iconos adaptativos Android, con fondo negro del logo.
- APK actualizado a `1.0.2` / `versionCode 3`; `:app:copyDebugApkForSharing` completó con **BUILD SUCCESSFUL**.
- Copia para transferir al teléfono: `C:/Users/josem/OneDrive/Escritorio/Kuota-Peru-v1.0.2-debug.apk`.
- El APK conserva como backend `http://192.168.18.43:8080/`; el endpoint `/health` fue comprobado en la IP Wi-Fi local.

## Onboarding inspirado en el video del usuario — v1.0.3

- Se cambió la bienvenida a un recorrido vertical y corto, con una sola tarea por pantalla: correo, código demo, celular con prefijo peruano +51 y RUC.
- Se conserva la idea de baja fricción y el botón principal claro; se usa la identidad verde y el símbolo de Kuota. El recorrido toma como referencia el patrón de interacción mostrado, sin reutilizar marca ni textos de terceros.
- El código visible es `123456`; no se envían correos o SMS. Email, celular y RUC no se validan contra servicios externos ni se registran en el backend. El acceso sigue siendo una entrada a la demo local, no un alta de cliente ni una aprobación de crédito.
- APK de depuración actualizado a `1.0.3` / `versionCode 4`, con URL local del backend `http://192.168.18.43:8080/` para pruebas en la misma Wi-Fi.
- Validación de backend local: `/health` respondió `status=ok`, `environment=DEMO_ONLY`.
- `:app:copyDebugApkForSharing`: **BUILD SUCCESSFUL**. APK copiado a `C:/Users/josem/OneDrive/Escritorio/Kuota-Peru-v1.0.3-debug.apk`.
- La compilación no sustituye la prueba visual en un teléfono; aún no se ha verificado el recorrido en un dispositivo.

## Revisión del video Mercado Pago y adaptación a Kuota — v1.0.4

- El video dura aproximadamente 3 minutos. Presenta una entrada con crear cuenta/iniciar sesión, un aviso sobre uso de datos, acceso por correo o usuario y después una navegación por inicio, actividad, tarjetas, negocio y menú de cuenta.
- Se adaptaron a Kuota la entrada de dos opciones, la explicación previa al registro demo, una tarea por pantalla, el inicio centrado en la acción principal, la actividad en sección propia y el menú de cuenta con estados concretos.
- Para respetar el modelo de Kuota, la compra ahora inicia escaneando el QR del proveedor; luego se pide monto/plazo y se muestra una cotización demo antes de confirmar. Se renombró la navegación para el comprador MYPE: Inicio, Comprar, Actividad y Cuenta.
- No se incorporaron las funciones que el video muestra para Mercado Pago pero no están definidas para Kuota (tarjetas, transferencias, links de cobro, métricas de comercio o medios de pago). Los accesos de cuenta explican qué sigue pendiente.
- El registro de email/código/celular/RUC continúa local, sin envío al servidor ni verificación. El botón de cuenta existente salta los datos empresariales en la demostración; no existe inicio de sesión real.
- APK `1.0.4` / `versionCode 5`; `:app:copyDebugApkForSharing` terminó con **BUILD SUCCESSFUL** y quedó en `C:/Users/josem/OneDrive/Escritorio/Kuota-Peru-v1.0.4-debug.apk`.
- El APK apunta al backend local `http://192.168.18.43:8080/`; `/health` respondió `status=ok`, `environment=DEMO_ONLY`. Se requiere la misma Wi-Fi para probar la cotización demo desde el teléfono.
- No se hizo prueba visual en dispositivo; el flujo de login y los datos financieros continúan siendo demostraciones.

## Módulos priorizados según PRD v5.2 y video — v1.0.5

- Se volvió a cotejar el archivo `C:/Users/josem/Downloads/PRD_Kuota_Peru_v5.md` (versión declarada 5.2) con el recorrido de la app mostrado en el video. La navegación ahora coincide con el PRD: Inicio, Ventas, Compras y Perfil.
- Inicio dejó de mostrar una capacidad fija y una cuota inventada. Presenta el perfil como demo/no verificado y consulta al backend demo para mostrar la próxima cuota solo cuando existe una compra de prueba.
- Ventas incluye un módulo de conexión no habilitada con explicación de datos y última sincronización vacía. Aclara que no hay integración de Yape/Plin y que Kuota no solicita claves, PIN u OTP.
- Compras ahora tiene secciones “Comprar” y “Mis cuotas”. El calendario usa las cuotas recibidas del backend demo; los estados, importes y fechas se identifican como simulación.
- Perfil añade un apartado de notificaciones y explica que recordatorios, pagos, medios, soporte e integraciones siguen sin estar habilitados.
- No se añadieron tarjetas, transferencias, métricas de vendedor, cobros, score ni pagos reales, porque el PRD los excluye, los mantiene condicionados o requiere decisiones externas.
- APK `1.0.5` / `versionCode 6`; `:app:copyDebugApkForSharing` terminó con **BUILD SUCCESSFUL** y el archivo está en `C:/Users/josem/OneDrive/Escritorio/Kuota-Peru-v1.0.5-debug.apk`.
- `/health` respondió `status=ok`, `environment=DEMO_ONLY`. No se hizo prueba visual en teléfono; los módulos de fuente de ventas, autorización, pagos y soporte son informativos y no realizan operaciones reales.

## Registro manual de ventas en dispositivo — v1.0.6

- Ventas ahora permite registrar, consultar, corregir y eliminar montos manuales; muestra total y cantidad de registros de los últimos siete días.
- Los registros se cifran con AES-GCM y una clave de Android Keystore, se guardan solo en el almacenamiento privado de la app y no se envían al backend.
- Cada fila se etiqueta como no verificada; los datos son declarados por el usuario y no modifican score, límite, elegibilidad ni oferta.
- El APK `1.0.6` / `versionCode 7` compiló con **BUILD SUCCESSFUL** y quedó en `C:/Users/josem/OneDrive/Escritorio/Kuota-Peru-v1.0.6-debug.apk`.
- El backend `/health` respondió `status=ok`, `environment=DEMO_ONLY`. No se hizo prueba de ejecución en teléfono; la compilación no valida el acceso a Keystore ni el ciclo completo de guardar/editar/eliminar.

## Perfil local y reanudación del onboarding — v1.0.7

- Al abrir la app, se consulta el perfil local cifrado: si existe, se entra a Inicio; si no, se muestra el onboarding.
- El onboarding guarda el paso y los campos de correo, celular y RUC localmente para poder retomarlo. El código de acceso demo no se persiste.
- Al terminar el recorrido se guarda el perfil local cifrado junto con los datos ya usados por el registro manual de ventas. La información sigue siendo declarada, no verificada y no se envía al backend.
- Perfil permite editar nombre del negocio, correo, celular peruano y RUC con validaciones de formato; permite borrar el perfil local y volver al onboarding. El borrado no elimina las ventas manuales.
- Estos cambios no constituyen autenticación real, verificación de identidad/RUC, consentimiento legal, scoring ni creación de cuenta. No cambian elegibilidad ni importes.
- APK actualizado a `1.0.7` / `versionCode 8`; compilarlo confirma que el artefacto se genera, pero falta probar retoma, edición y borrado en un teléfono.

## Simulador de pagos y conciliación — v1.0.8

- El backend demo ya acepta intentos de pago para cuotas programadas con `DEMO_MANUAL`; asigna identificador, importe, referencia sintética e historial de eventos.
- Las rutas delegan la creación y transiciones a una interfaz backend `PaymentProviderAdapter`; hoy solo se registra el adaptador demo. No se incluyen claves ni dependencia de proveedor real.
- Se pueden simular `PENDING`, `PROCESSING`, `SUCCEEDED`, `FAILED`, discrepancia de importe/referencia, reversa, devolución y conciliación manual. Las transiciones inválidas se rechazan.
- Idempotencia de creación y eventos: repetir la misma clave y cuerpo recupera el intento; reutilizarla con datos diferentes responde conflicto. Los intentos activos no se duplican; fallidos, reversados o devueltos admiten un intento nuevo.
- El estado sintético de pago actualiza la cuota demo; una liquidación simulada coincidente marca `DEMO_PAID`. Las discrepancias quedan en `GET /v1/demo/reconciliation` hasta una acción manual demo, que se registra como `MANUALLY_RESOLVED` y conserva la evidencia original.
- “Mis cuotas” ahora inicia y controla los pagos simulados, presenta la cola de conciliación y permite reintentos, reversas y devoluciones. Todas las acciones indican que no se mueve dinero.
- Los registros, huellas de idempotencia y eventos se guardan en el JSON local del backend; el formato mantiene valores predeterminados para leer archivos de demo anteriores.
- No hay PSP, webhook, cuenta bancaria, libro contable ni conciliación de extractos reales. El backend sigue siendo de desarrollo y debe permanecer en loopback o red privada confiable.
- Backend `compileKotlin` y Android `:app:copyDebugApkForSharing`: **BUILD SUCCESSFUL**. APK `1.0.8` / `versionCode 9` copiado a `C:/Users/josem/OneDrive/Escritorio/Kuota-Peru-v1.0.8-debug.apk`.
- Backend reiniciado en `http://192.168.18.43:8080/`; `/health` responde `status=ok`, `environment=DEMO_ONLY`, y la cola demo de conciliación inicia vacía. Aún falta probar el flujo de punta a punta en un teléfono.

## Punto 5 del PRD: notificaciones remotas y CRM de reclamos — v1.2.0

- Recordatorios remotos: el backend ya genera recordatorios de cuotas demo y los entrega por tres modos: `FCM` (Firebase), `PULL` (bandeja que el teléfono consulta cada 15 minutos con WorkManager, al abrir la app y al sincronizar) y `DRY_RUN` (solo registro en backend).
- La app incluye Firebase BoM 33.7.0 y Firebase Messaging. La configuración real de FCM queda condicionada a colocar `app/google-services.json`; si el archivo no existe, `BuildConfig.FIREBASE_CONFIGURED=false` y la app registra un token `pull-<uuid>`. Así la demo funciona hoy sin credenciales de Firebase.
- `GoogleServicesTask` solo se aplica cuando existe `google-services.json`; no se versionó ninguna credencial ni archivo de Firebase.
- La bandeja pull es por dispositivo (máximo 50 mensajes, retención de 7 días), se marca como leída con `ack` y muestra en la app el modo activo (`FCM` / `PULL` / `DRY_RUN`).
- Horario silencioso por defecto 20:00–07:00 (zona America/Lima); los recordatorios vencidos solo se envían en días hábiles. Ambos criterios están marcados `[VALIDACIÓN LEGAL]` en backend y deben confirmarse antes de producción.
- Pantalla Notificaciones: permisos Android 13+ solicitados en contexto, estado de registro push, reintento, bandeja local con no leídas, marcar leídas, preferencias (recordatorios, soporte, novedades), detalle en pantalla bloqueada, notificación local de prueba y URL de servidor avanzada.
- Libro de Reclamaciones: el backend valida la hoja, asigna número oficial (`LR-AAAA-NNNNNN`), calcula 15 días hábiles (calendario Perú, feriados configurables con `KUOTA_EXTRA_HOLIDAYS`) y la distribuye por canales: `CORREO_SOPORTE`, `COPIA_CONSUMIDOR`, `ZENDESK`, `FRESHDESK` o `ARCHIVO_LOCAL`.
- El estado por canal (`PENDIENTE` / `ENVIADO` / `FALLIDO` / `DESACTIVADO`) se guarda junto con la hoja y se muestra en la app; la sincronización desde el teléfono envía las hojas pendientes y actualiza su estado.
- Sin variables de entorno solo se activa `ARCHIVO_LOCAL` (copia en el almacenamiento del backend demo). El correo SMTP y los CRM se activan únicamente al configurar `KUOTA_SMTP_*` / `KUOTA_SUPPORT_EMAIL` o `KUOTA_ZENDESK_*` / `KUOTA_FRESHDESK_*`.
- Organización UX: Inicio funciona como centro del ecosistema (campana con no leídas, checklist de cuenta/notificaciones/identidad, accesos a financiamiento, venta, ayuda y notificaciones, ventas de 7 días y reclamos en seguimiento). Perfil quedó dividido en “Mi negocio”, “Identidad y seguridad”, “Pagos y avisos”, “Ayuda” y eliminación local.
- “Ayuda y reclamos” y “Notificaciones” son pantallas propias con navegación de retorno; los deep links `kuota://notificaciones`, `kuota://soporte`, `kuota://reclamos`, `kuota://compras` y `kuota://financing` abren la sección correspondiente. Los accesos duplicados que antes solo vivían dentro de Perfil ahora están también en Inicio.
- Reglas demo conservadas: no hay crédito, cobro, identidad real, scoring ni consentimiento legal. Los canales de soporte, números y correos son de prueba; las hojas y recordatorios usan datos sintéticos. Compras demo sin dueño hacen que los recordatorios pull lleguen a todos los dispositivos registrados.
- Prueba de humo del backend (loopback `127.0.0.1:8080`): registro `pull-<uuid>` → `push_mode=PULL`, `quiet_hours=20:00–07:00`; test-push → `devices=1, delivered=1`; bandeja recibió y confirmó el mensaje; reclamo → `201`, número `LR-2026-000001`, vencimiento `2026-11-10`, canal `ARCHIVO_LOCAL` `ENVIADO`; baja de dispositivo → `204`.
- Android `:app:copyDebugApkForSharing`: **BUILD SUCCESSFUL** (2m29s). APK `1.2.0` / `versionCode 14`, minSdk 26, targetSdk 36, en `C:/Users/josem/OneDrive/Escritorio/Kuota-Peru-v1.2.0-debug.apk` (21 MB).
- No hubo dispositivo ni emulador conectado (`adb devices` vacío): falta validar en teléfono los permisos, la bandeja pull, los deep links, el formulario de reclamo y la reorganización visual.

## Rediseño visual de Inicio y barra inferior con QR — v1.3.0

- Inicio se reorganizó siguiendo la distribución de la referencia visual (encabezado con saludo, accesos rápidos, tarjeta principal superpuesta, sugerencia y actividad), sin adoptar funciones de billetera: no hay saldo real, retiros, tarjetas ni cobros.
- Encabezado verde con iniciales del usuario, saludo “Hola, {nombre}”, nombre del negocio y campana de notificaciones con contador de no leídas. El perfil se abre desde “Mi negocio” en accesos rápidos y desde la pestaña Perfil.
- El nombre del dueño ahora forma parte del perfil local cifrado (`LocalBusinessProfile.ownerName`). Se solicita en un paso nuevo del onboarding (“¿Cómo te llamas?”), se puede editar en Perfil → Mi negocio y alimenta el saludo de Inicio; si está vacío se usa el nombre del negocio.
- El borrador del onboarding también guarda el nombre, por lo que retomar el registro conserva ese dato. Los perfiles existentes sin nombre siguen cargando por los valores predeterminados del modelo.
- Tarjeta principal superpuesta al encabezado: “Línea de compra”, pastilla `DEMO`, monto `S/ 0`, acceso “Ver opciones →”, mensaje “Sin evaluación activa”, botón “Comprar con QR” y aviso de demostración.
- “Accesos rápidos” horizontal: Comprar, Ventas, Ayuda (reclamos abiertos), Avisos (no leídas), Mi negocio y Mis reclamos. Los contadores salen del almacenamiento local cifrado.
- Tarjeta “Sugerencias” con la mascota y llamada a comprar/pagar en cuotas, más tarjeta “Tu actividad” con ventas de los últimos 7 días, reclamos abiertos y avisos sin leer, y el aviso de reclamos pendientes.
- Barra inferior personalizada: Inicio, Ventas, botón central flotante “Comprar” con QR, Ayuda y Perfil. La navegación conserva estado y usa `launchSingleTop`; el botón central abre el módulo de financiamiento/compra demo.
- No se agregó una pantalla de “Cuotas” en la barra porque el código actual no tiene una ruta independiente de cuotas; “Comprar” conduce al módulo existente de compras/financiamiento demo.
- APK `1.3.0` / `versionCode 15`, minSdk 26, targetSdk 36, `:app:copyDebugApkForSharing` **BUILD SUCCESSFUL**, en `C:/Users/josem/OneDrive/Escritorio/Kuota-Peru-v1.3.0-debug.apk`.
- Verificación visual en emulador Android (`Medium_Phone_API_37.0`): onboarding con nombre “Jose Miguel”, Inicio con saludo, encabezado, tarjeta `S/ 0`, accesos rápidos, sugerencia, actividad y barra inferior; Perfil abre desde la barra y muestra las secciones. Capturas: `kuota-v130-onboarding.png`, `kuota-v130-home.png`, `kuota-v130-home-bottom.png`, `kuota-v130-profile.png` en el Escritorio.
- Sigue siendo una demo: el nombre y demás datos se guardan solo cifrados en el teléfono (no hay base de datos central ni autenticación), la línea de compra es `S/ 0`, no se otorga crédito ni se mueve dinero y no se enviaron datos a producción. Falta probar en teléfono físico.

## Tema claro/oscuro y retiro del verde oscuro — v1.4.0

- Se añadió apariencia seleccionable en Perfil → “Apariencia”: `Automático` (sigue el modo del sistema), `Claro` y `Oscuro`. La preferencia se guarda en `AppSettings` (SharedPreferences, dato no sensible), se aplica al instante y sobrevive al reinicio; por defecto una instalación nueva usa `Automático`.
- El tema oscuro dejó de usar verdes profundos como fondo: ahora usa neutros oscuros (`background #0F1311`, `surface #181D1A`, `surfaceVariant #232A26`) con acentos lima. El modo claro conserva fondos claros y tarjetas blancas.
- El encabezado de Inicio ya no es verde oscuro: en claro usa un degradado verde muy pálido con texto verde profundo; en oscuro usa un degradado gris muy oscuro con texto claro. El avatar y la campana adaptan su contraste.
- La tarjeta de “Sugerencias” cambia a un tono oscuro neutro en modo oscuro; las alertas de reclamos pendientes también. La tarjeta superior de Perfil y la tarjeta del onboarding pasaron de verde oscuro a contenedores suaves (`primaryContainer`).
- El botón central de compra y el botón “Comprar con QR” usan los colores del tema: verde bosque con texto crema en claro, lima con texto oscuro en oscuro.
- Los íconos de las barras de estado y navegación se ajustan automáticamente según el tema activo (`WindowCompat`). Se agregó `values-night/styles.xml` para que el fondo de ventana durante el arranque acompañe al modo oscuro del sistema.
- `AppSettings` guarda `theme_mode`; `ThemeController` expone el modo y `MainActivity` decide `darkTheme` antes de componer. `LocalKoutaDarkTheme` permite que las pantallas adapten detalles propios (encabezado, sugerencia) sin depender solo de `isSystemInDarkTheme()`.
- APK `1.4.0` / `versionCode 16`, minSdk 26, targetSdk 36, `:app:copyDebugApkForSharing` **BUILD SUCCESSFUL** en 3m29s, copiado a `C:/Users/josem/OneDrive/Escritorio/Kuota-Peru-v1.4.0-debug.apk`.
- Verificación en emulador Android (`Medium_Phone_API_37.0`): modo claro (encabezado pálido, sin verde oscuro), cambio a modo oscuro desde Perfil → Apariencia, render neutro oscuro con acentos lima, y persistencia de `theme_mode` verificada en las preferencias. Capturas: `kuota-v140-home-light.png`, `kuota-v140-home-dark.png` y `kuota-v140-profile-dark.png` en el Escritorio.
- Sin cambios de backend ni de datos. Sigue siendo demo: no hay crédito, pagos, identidad ni soporte reales; falta validar los temas en teléfono físico (incluido el brillo y la legibilidad con el modo oscuro del sistema).

## Punto 4 del PRD: PSP, webhooks HMAC y ledger append-only — preparado, no habilitado

**Corte:** 2026-10-20. Este avance implementa el backend del punto 4. No activa cobros reales, no crea deudas reales y no reemplaza la validación legal, contable, de seguridad ni comercial.

### Pasarela de pagos

- Se añadió `PaymentProviderAdapter`, con el simulador demo como valor predeterminado y un adaptador real de Mercado Pago Checkout Pro. El adaptador real solo se instancia con `KUOTA_PAYMENT_PROVIDER=mercadopago`, credenciales de comercio y PostgreSQL; no puede usar el ledger de archivo.
- `POST /v1/installments/{installmentId}/payment-intents` exige `MERCADO_PAGO_CHECKOUT` con el proveedor real, crea una preferencia con importe, referencia externa al pago interno, metadatos, URL de notificación y clave de idempotencia, y devuelve `ACTION_REQUIRED` más `next_action_url`.
- Si la pasarela falla, el intento queda `FAILED`, la cuota vuelve a `PROGRAMMED` y no se escribe ningún asiento contable.
- La app Android sigue enviando `DEMO_MANUAL` y todavía no abre `next_action_url`; el checkout real no está integrado en la UI móvil.
- `KUOTA_ENVIRONMENT=production` falla al arrancar si no existen proveedor Mercado Pago, PostgreSQL, `KUOTA_WEBHOOK_SECRET` y `KUOTA_ADMIN_TOKEN` de al menos 32 caracteres, y URL HTTPS de notificación. No se versionó ninguna credencial.

### Webhooks firmados

- `POST /v1/webhooks/mercadopago` valida `x-signature` con el manifiesto documentado `id:<data.id>;request-id:<x-request-id>;ts:<ts>;`, HMAC-SHA256 y tolerancia de 300 segundos. El `data.id` de la URL debe coincidir con el cuerpo.
- `POST /v1/webhooks/hmac` valida `X-Kuota-Timestamp` y `X-Kuota-Signature: v1=<hex>` sobre `"<timestamp>.<cuerpo-crudo>"`, con la misma tolerancia y comparación en tiempo constante.
- La deduplicación es durable por proveedor y `event_id` (tabla `webhook_event` o NDJSON). Un evento completado responde `200 DUPLICATE`; uno en proceso responde `409`; un fallo de consulta responde `502` y permite reintento.
- Para Mercado Pago, el estado nunca se toma del payload: se consulta `GET /v1/payments/{id}` y se comparan referencia externa, importe y moneda antes de liquidar.
- Los endpoints limitan el cuerpo a 64,000 caracteres y no registran el payload crudo.

### Libro contable append-only

- Con `KUOTA_DATABASE_URL` se usa PostgreSQL, HikariCP y la migración Flyway `V1__append_only_ledger.sql`. Sin base de datos se usa un ledger NDJSON con `fsync` para la demo.
- En PostgreSQL, los triggers rechazan `UPDATE`, `DELETE` y `TRUNCATE` sobre asientos y líneas; un constraint trigger diferido exige débitos = créditos al hacer commit. La clave `(environment, kind, source_type, source_id)` impide duplicar historia financiera.
- Cuentas: `CASH`, `BUYER_RECEIVABLE`, `SUPPLIER_PAYABLE_IMMEDIATE`, `SUPPLIER_PAYABLE_DEFERRED`, `REVENUE_BUYER_FEE`, `REVENUE_SUPPLIER_FEE`, `PAYMENT_VARIANCE`.
- Compra de S/ 1,000: débito a `BUYER_RECEIVABLE` 105,000; créditos a proveedor inmediato 60,000, diferido neto 36,000, comisión comprador 5,000 y comisión proveedor 4,000. La inicial de 30,000 forma parte de la cuenta por cobrar; no se inventa caja hasta que exista un pago.
- Pago conciliado: débito a `CASH`, crédito a `BUYER_RECEIVABLE`. Devolución o contracargo: asiento inverso. Conciliación manual con diferencia: caja real, baja de la cuenta por cobrar esperada y diferencia en `PAYMENT_VARIANCE`.
- `GET /v1/admin/ledger/transactions` y `GET /v1/admin/ledger/balances` requieren `X-Kuota-Admin-Token` o Bearer; sin token configurado responden `503` y con token inválido `401`.
- Se añadió `backend/docker-compose.yml` para PostgreSQL local en `127.0.0.1:5433`.

### Verificación realizada

- `.\gradlew.bat -p backend test`: **BUILD SUCCESSFUL**, 36 pruebas aprobadas (configuración, cliente Mercado Pago con motor simulado, firmas HMAC, deduplicación, ledger balanceado/persistente, flujo compra→pago→devolución→conciliación y webhook HMAC de punta a punta).
- Prueba manual contra PostgreSQL 16: elegibilidad → cotización → compra → dos cuotas pagadas (una por evento demo y otra por webhook HMAC firmado). Saldos: `CASH` 37,500; `BUYER_RECEIVABLE` 67,500; proveedor −60,000/−36,000; comisiones −5,000/−4,000; `balanced: true`.
- Reintento del webhook: `DUPLICATE`; cuerpo alterado con la misma firma: `401`; `UPDATE`/`DELETE` sobre el ledger: bloqueados por trigger; transacción desbalanceada: rechazada al commit.
- No se probó con credenciales reales de Mercado Pago, ni con una cuenta de comercio, ni con dinero real. El flujo de checkout en Android, la conciliación con reportes de liquidación del PSP, los comprobantes, la consola de operaciones y la aprobación contable/legal siguen pendientes.

## Sistema de color centralizado — v1.5.0

- `core/designsystem/theme/Color.kt` es la única fuente de colores: primitivas de marca (negro `#05070A`, azul `#5B8FF9`, celeste `#A8DFFF`, turquesa `#55DDE0`, blanco), esquemas Material 3 claro y oscuro completos y tokens extendidos (`KuotaExtendedColors`) expuestos con `MaterialTheme.kuotaColors`.
- El tema claro usa `KuotaBlueStrong #2D5FCC` como `primary` para conservar contraste (5.8:1 con blanco); el azul exacto de marca queda en degradados y en el `primary` del modo oscuro. `KuotaInk #04101F` se usa sobre superficies brillantes de marca.
- `Theme.kt` elimina `LocalKoutaDarkTheme`; las pantallas dejaron de ramificar por `isSystemInDarkTheme()` y consumen roles M3 o tokens extendidos. Inicio, onboarding, Perfil, Soporte, Notificaciones, componentes compartidos, barra inferior y el color de notificación del sistema migraron a la nueva paleta. No quedan hexadecimales fuera de `Color.kt` en el código Kotlin.
- Activos: `kuota_mark.png` y `kuota_launcher_foreground.png` se recolorearon con degradado azul→celeste→turquesa preservando alfa; la mascota `kuota_onboarding_avatar.png` se recortó a fondo transparente y sus cajas pasaron de verde a azul. `values/colors.xml` usa fondo claro `#F7FAFF` y oscuro `#05070A`; el fondo del icono launcher sigue negro.
- Documentación de uso en `app/src/main/java/com/kouta/app/core/designsystem/theme/README.md`. Cambiar una primitiva en `Color.kt` actualiza toda la app, salvo los PNG y XML estáticos, que deben regenerarse.
- APK `1.5.0` / `versionCode 17`, minSdk 26, targetSdk 36, `:app:assembleDebug` + `:app:copyDebugApkForSharing` **BUILD SUCCESSFUL**, copiado a `C:/Users/josem/OneDrive/Escritorio/Kuota-Peru-v1.5.0-debug.apk`.
- Verificación visual en emulador (`Medium_Phone_API_37.0`): Inicio claro y oscuro, Perfil oscuro, selector de Apariencia accesible con desplazamiento y tarjeta de Sugerencias con la mascota integrada. Capturas: `kuota-v150-home-light-2.png`, `kuota-v150-home-dark-2.png` y `kuota-v150-profile-dark.png` en el Escritorio.
- Sin cambios de backend ni de comportamiento. Sigue siendo demo: no hay crédito, pagos, identidad ni soporte reales. Falta validar la paleta en teléfono físico (brillo, modo oscuro del sistema y legibilidad al aire libre).

## Punto 2 — Flujo de ventas y respaldo en segundo plano — v1.6.0

**Corte:** 2026-10-20. Demo honesta: no existe conexión con Yape/Plin/POS ni verificación de cobros.

### Decisión de integración pendiente (negocio)

- No hay API pública abierta de Yape/Plin. Para leer ventas reales hay que elegir una vía:
  1. **Agregador de adquirencia/POS** (Niubiz, Izipay, Culqi u otro): exige contrato, credenciales de comercio, conciliación de liquidaciones y responsabilidades definidas.
  2. **Extractos bancarios autorizados**: exige autorización del titular, acceso/contrato con el banco y validación legal del tratamiento de datos.
- Hasta que se decida, la app no simula la conexión: "Registro manual cifrado" es el único origen activo y las demás opciones aparecen como **no disponibles** en la pantalla Ventas.

### Backend

- `POST /v1/sales/declared/sync` (JWT) recibe hasta 100 ventas por lote y un cuerpo máximo de 128 KB; `GET /v1/sales/declared` lista solo los registros del usuario autenticado.
- Idempotencia por `(usuario, client_sale_id)` y `revision`: un replay idéntico cuenta como duplicado, una revisión mayor reemplaza, la misma revisión con contenido distinto responde `SALE_REVISION_CONFLICT` y una revisión menor responde `SALE_REVISION_STALE`.
- Tombstones (`deleted: true`) propagan bajas. Validación: ID de 8–64 caracteres, monto entre 1 y 10,000,000 céntimos, fecha desde 2000 y hasta 10 minutos en el futuro, `source=MANUAL`.
- Persistencia atómica en `kuota-declared-sales.json` con aislamiento por usuario. Las ventas son declaradas, `verified=false`, y no afectan score ni elegibilidad.

### Android

- `ManualSale` guarda `source`, `revision`, estado de respaldo (`SOLO_LOCAL`, `PENDIENTE`, `RESPALDADA`, `RECHAZADA`), fecha de respaldo, error y tombstone. Corregir sube la revisión; eliminar una venta ya respaldada genera tombstone y el registro se descarta del archivo cifrado cuando el servidor confirma.
- El `KuotaSyncWorker` existente (WorkManager, cada 15 minutos con red, más el envío único al registrar) ahora respalda ventas cuando el usuario activa "Respaldo automático" con cuenta vinculada. "Sincronizar ahora" fuerza el envío. La pantalla se refresca al recibir `KuotaEvents`.
- Pantalla Ventas: tarjeta de respaldo con vinculación por OTP, switch de respaldo automático, último respaldo, pendientes, estado por venta y diálogo "Origen de ventas" que marca POS/Yape/Plin/extractos como no disponibles.
- El respaldo copia lo declarado: no valida cobros, no consulta al PSP y no crea pagos, deudas ni obligaciones.

### Verificación

- `./gradlew -p backend test`: **BUILD SUCCESSFUL**, 41 pruebas aprobadas. Las 5 nuevas cubren idempotencia y revisión, conflicto/obsoleta, tombstones con aislamiento por usuario, validación de entradas y persistencia tras reinicio.
- Backend demo + emulador con `adb reverse tcp:8080 tcp:8080`: venta manual → vincular cuenta con OTP → respaldo (revisión 0) → corrección de monto (revisión 1 enviada por WorkManager) → eliminación (tombstone revisión 2), confirmado con `GET /v1/sales/declared`.
- APK `1.6.0` / `versionCode 18`, `:app:assembleDebug` **BUILD SUCCESSFUL**. Capturas de la verificación en el Escritorio.
- Límites vigentes: sin Yape/Plin/POS/bancos, sin verificación de ventas, sin scoring, sin crédito y sin dinero real.

## Punto 6 — Decisiones de negocio y marco legal — v1.7.0

**Corte:** 2026-10-20. Este punto **no puede cerrarse dentro de la app**: exige decisión de negocio y dictamen legal externo. Lo implementado hace visible y trazable lo pendiente, sin simular aprobaciones, registro SBS ni consentimientos.

### Registro de decisiones

- `docs/DECISIONES_NEGOCIO_LEGAL.md` documenta **D-01** (entidad que presta el dinero) y **D-02** (quién asume la mora) con estado **PENDIENTE**, responsables, evidencias requeridas y opciones a evaluar (entidad propia, entidad aliada, fondo/tercero o crédito propio).
- El PRD §6 menciona D-01 a D-12, pero el repositorio no contiene el detalle de D-03 a D-12. Se marcan **por incorporar**; no se inventa su contenido ni se les asigna estado.
- El registro incluye la lista mínima de evidencias para cerrar: dictamen SBS/perímetro, decisión formal de dirección, documentos aprobados y versionados, diseño de aceptación auditable, registro de tratamiento, protocolo de incidentes y revisión de seguridad.

### Preparación legal peruana

- Checklist para **Términos y Condiciones**, **Política de Privacidad (Ley 29733 y DS 016-2024-JUS)** y **contratos de financiamiento de inventario**: contenido mínimo, evidencia de aceptación, firma electrónica, tratamiento de datos, mora, garantías y solución de controversias.
- Inventario de datos del prototipo: perfil del negocio, resumen de DNI, ventas manuales, reclamos, token de notificaciones y token de sesión. Se distingue qué es local cifrado y qué se respalda con cuenta vinculada.
- Verificaciones adicionales a confirmar con abogado: perímetro SBS, crédito al consumidor, cláusulas generales de contratación, protección de datos, obligaciones tributarias/contables, PLAFT si aplica, seguros y accesibilidad. **Ninguna está completada.**

### Android

- Nueva pantalla **Perfil → Legal → Marco legal y privacidad** (ruta `legal`).
- Muestra: aviso de modo demo sin aprobación legal ni registro SBS; D-01/D-02 pendientes; documentos en borrador con detalle de lo que falta; datos y derechos; y lista de pendientes para operar.
- Los documentos son informativos: **no hay botón de aceptar, firmar ni otorgar consentimiento**. Los diálogos aclaran que no están vigentes.
- No se modificó el comportamiento de crédito, pagos, identidad, ventas ni soporte.

### Verificación

- `:app:assembleDebug` y `:app:copyDebugApkForSharing`: **BUILD SUCCESSFUL**.
- APK `1.7.0` / `versionCode 19`, copiado al Escritorio.
- Verificación en emulador: Perfil → Legal → Marco legal; diálogo de Términos y Condiciones; capturas en tema claro y oscuro. El pie confirma `v1.7.0 (build 19)`.
- Sin cambios de backend; no se ejecutaron pruebas con documentos legales reales porque no existen versiones aprobadas.

### Límites vigentes

- Kuota **no** está registrada ante la SBS, **no** tiene acreedor definido, **no** otorga crédito real, **no** cobra cuotas y **no** registra aceptaciones.
- Términos y Condiciones, Política de Privacidad y contratos siguen siendo borradores de trabajo; requieren dictamen legal peruano y aprobación formal antes de publicarse o aceptarse.
- La app es una demostración y este estado no constituye asesoría legal ni aprobación regulatoria.

## Corrección visual — botón QR central — v1.7.1

- El círculo QR de la barra inferior se veía recortado en su parte superior. La causa era `Modifier.shadow` de la barra: su valor por defecto `clip = true` recortaba el botón elevado.
- La barra inferior ahora se dibuja con un `Box`, fondo redondeado y `shadow(clip = false)`, de modo que el círculo completo de 60 dp sobresale sobre la barra sin perder sombra ni esquinas redondeadas.
- APK `1.7.1` / `versionCode 20`, `:app:assembleDebug` **BUILD SUCCESSFUL**; verificado en emulador en tema claro y oscuro.
- Sin cambios funcionales ni de navegación.

## Adaptación de referencias UI — v1.8.0

**Corte:** 2026-10-20. Se adaptaron las referencias visuales de Mercado Libre / Kashin al ecosistema Kuota, reutilizando las pantallas existentes y limitando la implementación a lo que encaja con el financiamiento de inventario para MIPE. **No se copiaron flujos de crédito de consumo, emisión de tarjetas ni aceptación de condiciones.**

### Barra inferior

- Cinco accesos: **Cuenta**, **Tarjetas**, **QR** (central, elevado), **Actividad** y **Menú**.
- El botón QR conserva el flujo de financiamiento ya implementado; el área táctil incluye el círculo y la etiqueta "QR".
- La barra se oculta en pantallas de detalle (Marco legal, Notificaciones, Soporte) para evitar un estado seleccionado ambiguo y dar más espacio al contenido.

### Mapeo de interfaces

| Referencia | Adaptación Kuota |
| --- | --- |
| Pantalla de cuenta | **Cuenta** = Inicio existente, con línea de compra demo, accesos rápidos, sugerencias y actividad. |
| Tarjetas / medios de pago | **Tarjetas** = nueva pantalla de preferencias de pago. No emite tarjetas ni pide números, CVV o claves. Opciones: Yape / Plin, Transferencia BCP, Transferencia BBVA / Interbank y Tarjeta de débito. La elección se guarda cifrada en el dispositivo y se refleja en Menú. |
| Actividad | **Actividad** = Ventas declaradas existentes, ahora con búsqueda por monto o fecha, filtros (Todas, Esta semana, Solo local), contador de resultados, estado vacío con mascota y estado "sin resultados" con opción de limpiar filtros. |
| Menú / perfil | **Menú** = perfil y preferencias reorganizados: nombre del usuario y negocio, estado de cuenta/KYC, Tarjetas y medios de pago, Notificaciones, Apariencia, Marco legal y Ayuda. Se retiró el diálogo duplicado de medios de pago. |
| Detalle de cuotas | **Cronograma referencial** desplegable en QR: 4 cuotas semanales con fecha y monto. Se muestra como referencia demo, sin contrato, desembolso ni deuda real, y sin solicitar aceptación. |

### Referencias no implementadas

- **Crédito de consumo con montos preaprobados** ("hasta S/800", aumento de historial): Kuota financia inventario contra una compra en punto de venta, no préstamos personales.
- **Presets de monto**: el monto proviene del QR del proveedor, no lo elige libremente el usuario.
- **Aceptar condiciones / firma**: los documentos legales siguen en borrador y no se registra consentimiento.
- **Emisión o entrega de tarjeta**: Kuota no emite tarjetas; "Tarjetas" es solo preferencia de pago.
- **Permiso de cámara**: el KYC del prototipo es un formulario simulado; no existe captura real que justifique el permiso.

### Verificación

- `:app:assembleDebug` y `:app:copyDebugApkForSharing`: **BUILD SUCCESSFUL**.
- APK `1.8.0` / `versionCode 21`, copiado al Escritorio como `Kuota-Peru-v1.8.0-debug.apk`.
- Emulador, tema claro y oscuro: Cuenta, Tarjetas, Actividad (con estado vacío), Menú, QR y cronograma desplegable.
- Persistencia comprobada: seleccionar Transferencia BCP en Tarjetas actualiza el subtítulo en Menú; se restauró Yape / Plin.
- La barra inferior se oculta en Marco legal y reaparece al volver.
- Los filtros caben en ancho de 411 dp tras reducirse a tres opciones.

### Límites vigentes

- Todo sigue en **modo demostración**: sin crédito real, sin cobros, sin Yape/Plin/POS/bancos conectados, sin emisión de tarjetas y sin verificación real de identidad.
- Las preferencias de pago no autorizan cobros; un cobro real requeriría un PSP contratado, credenciales y validación legal.
- El cronograma es referencial y no constituye una oferta ni una obligación de pago.

## QR firmado, límites de solicitudes y cotización autoritativa — 2026-10-22

**Corte:** 2026-10-22. Cierra brechas de seguridad del flujo demo; **no habilita proveedores, pagos ni crédito real**.

### QR de proveedor

- Se eliminó el secreto HMAC embebido del backend (`DynamicQrSigner.kt`) y el verificador local de Android (`DynamicQrDecoder.kt`). La app ya no contiene ni usa el secreto de firma.
- `KUOTA_QR_SIGNING_SECRET` (mínimo 32 caracteres) firma los payloads Base64 con HMAC-SHA256. En demo, si falta, se genera un secreto efímero y se advierte; al reiniciar caducan los QR emitidos. Producción no arranca sin el secreto.
- La verificación exige campos completos, monto positivo, vigencia, payload máximo de 4,096 caracteres y comparación en tiempo constante. Un QR inválido o expirado responde `422 QR_INVALID_OR_EXPIRED`.
- Nuevas rutas demo: `POST /v1/supplier/qr/verify` (público, 30/min), `POST /v1/supplier/generate-qr` (`amountPen`, 10/min, QR válido 15 minutos) y `POST /v1/supplier/confirm-sale` (20/min). Las dos últimas responden `403 SUPPLIER_DEMO_ONLY` en producción.
- `POST /v1/quotes` verifica el QR firmado y rechaza un monto distinto al firmado con `QR_AMOUNT_MISMATCH`. El payload de texto `KUOTA-DEMO-SUPPLIER-LIMA` queda solo como compatibilidad fuera de producción.

### Límites y OTP

- `InMemoryRateLimiter` aplica ventanas fijas por IP o usuario a QR, OTP, elegibilidad, cotización, compras, intentos de pago y eventos demo. Al exceder responde `429 RATE_LIMITED` con `Retry-After`.
- Los límites viven en memoria del proceso: se reinician con el servidor y no protegen una flota de instancias. No sustituyen un API gateway ni una revisión de abuso.
- El OTP demo ahora acepta **solo** `123456`; antes cualquier código de seis dígitos era válido. En producción ambas rutas OTP responden `503 OTP_PROVIDER_NOT_CONFIGURED`.
- Sigue sin haber SMS, refresh, rotación, revocación ni bloqueo duradero de cuenta.

### Android

- La pantalla QR no muestra proveedor, monto, cuotas ni desglose hasta que el servidor verifica el QR. Luego consulta elegibilidad, pide la cotización y muestra únicamente los importes/fechas devueltos por el backend.
- El botón de confirmación demo llama a `/v1/supplier/confirm-sale` y presenta el resultado como `DEMO_SUPPLIER_CONFIRMED`, aclarando que no es venta ni desembolso real.
- Se eliminó `PurchaseOfferModel`, que calculaba la oferta localmente; solo queda el formateo de soles. La app no puede inventar una cotización si el backend no responde.

### Verificación

- `./gradlew -p backend test`: **BUILD SUCCESSFUL**, 72 pruebas, 0 fallos. Incluye round trip, expiración, manipulación de monto, secreto distinto, payload malformado/sobreextendido, ventanas de rate limit y `Retry-After`.
- `./gradlew :app:assembleDebug`: **BUILD SUCCESSFUL**. Solo quedan advertencias de API deprecada en íconos y `Locale`; no hay errores de compilación.
- Prueba HTTP real en el puerto 8095: generar QR de S/ 1,000 (`amount_cents=100000`) → verificar en servidor → manipular `amount_cents` a 1 responde `422 QR_INVALID_OR_EXPIRED` → elegibilidad demo → cotización con 30,000 / 75,000 / 60,000 / 40,000 / 30,000 / 4,000 y 4 cuotas (primera `2026-10-29`) → cotizar 99,999 responde `422 QR_AMOUNT_MISMATCH` → confirmación demo con 60,000 inmediato, 40,000 diferido y 4,000 de comisión → OTP `000000` responde `401` y `123456` responde `200` con token → el límite de generación responde `429` con `Retry-After`.
- No se subió `versionCode`/`versionName`: el APK compilado sigue siendo `1.8.0` / `21`. No se probó el recorrido en emulador o teléfono en este corte.

### Límites vigentes

- Un QR firmado solo prueba que **este backend demo** lo emitió. No valida un comercio real, precio, stock, entrega ni autorización del proveedor.
- La confirmación del proveedor es una simulación local; no existe POS, portal B2B autenticado ni liquidación.
- No se activaron pagos, cobros, identidad, scoring, consentimientos ni documentos legales. Las decisiones D-01 a D-12 y las aprobaciones legal/PSP siguen bloqueando el MVP operativo.

## Endurecimiento de autenticación y lectura de compras — 2026-10-22

**Corte:** 2026-10-22. Sin cambios en la app Android; el trabajo fue en el backend demo.

### Cambios

- Se eliminó el secreto HS256 embebido de `JwtConfig.kt`. El secreto se carga desde `KUOTA_JWT_SECRET` (mínimo 32 caracteres) y se inyecta al arrancar el servidor.
- Los tokens ahora expiran a las 24 horas (antes 30 días) y declaran `iat`. La verificación exige `alg=HS256` e `iss=kuota-peru-auth`, rechaza `none`, tokens malformados o expirados y compara firmas con `MessageDigest.isEqual`.
- En modo demo, si `KUOTA_JWT_SECRET` no está definido se genera un secreto efímero con `SecureRandom` y se registra una advertencia; los tokens dejan de valer al reiniciar. Producción falla al arrancar sin el secreto.
- `/v1/purchases`, `/v1/purchases/{id}`, `/v1/purchases/{id}/installments`, `/v1/installments/{id}/payment-intents`, `/v1/payments/{id}` y `/v1/payments/{id}/demo-events` exigen Bearer JWT.
- Las compras guardan `ownerUserId`: el historial se filtra por usuario y los recursos de otro usuario se responden como inexistentes (`404`) para evitar enumeración. Las claves de idempotencia no se pueden reutilizar entre usuarios.
- `GET /v1/demo/reconciliation` exige el token de admin del ledger.
- `/v1/me` reutiliza el mismo helper `requireUser()` que el resto de rutas autenticadas.

### Verificación

- `.\gradlew.bat -p backend test`: **BUILD SUCCESSFUL**, 57 pruebas, 0 fallos. Se agregaron pruebas de JWT (round trip, manipulación, secreto distinto, expiración en el límite de 24 h, `alg=none`, emisor ajeno, `exp` ausente, token malformado, secreto corto), configuración (secreto efímero, obligatorio en producción, longitud mínima) y ownership (historial, compra, cuotas, pago, evento demo, replay de idempotencia y persistencia tras reinicio).
- Prueba HTTP local con `KUOTA_JWT_SECRET` y puerto 8091: `GET /v1/purchases` sin token → `401`; con token emitido por `verify-otp` → `200 []`; recurso ajeno/inexistente → `404`; token manipulado → `401`; `GET /v1/demo/reconciliation` sin admin → `503`.
- No se recompiló el APK porque la app no consume estas rutas.

### Límites vigentes

- El OTP sigue siendo simulado; no hay refresh, revocación ni rate limiting.
- Las compras demo creadas antes de este cambio no tienen dueño y ya no se leen por HTTP; hay que reiniciar `backend/data/kuota-demo-store.json` para empezar limpio.
- Sigue siendo demo: sin cobros, crédito, identidad real ni exposición pública del backend.

## Compra demo desde la app, historial y contrato de errores — 2026-10-22

**Corte:** 2026-10-22. Cierra la brecha entre la confirmación demo del proveedor y el registro visible de la compra, sin habilitar ventas, deuda ni desembolsos reales.

### Backend

- Todas las respuestas incluyen `X-Request-Id`, `X-Content-Type-Options: nosniff` y `Referrer-Policy: no-referrer`. Un `X-Request-Id` entrante solo se reutiliza si cumple `^[A-Za-z0-9._:-]{1,64}$`; de lo contrario se genera un UUID.
- Los errores usan el contrato `{"code","message","request_id","retryable"}` en todas las rutas, incluidas autenticación, reclamos, ventas declaradas y dispositivos. `retryable` se serializa siempre (también `false`) y `500 INTERNAL_ERROR` responde con `retryable=true` sin exponer trazas.
- `StatusPages` captura fallos inesperados y registra el `request_id`; las tasas excedidas siguen respondiendo `429 RATE_LIMITED` con `Retry-After` y `retryable=true`.

### Android

- Tras la confirmación demo del proveedor, la pantalla QR registra la compra con `POST /v1/purchases`; si no hay sesión, abre el diálogo de vinculación y reintenta automáticamente al autenticarse.
- Actividad muestra la sección **Compras demo** desde `GET /v1/purchases`: proveedor, monto, estado `DEMO_CREATED`, pago inicial, cronograma de cuotas y la advertencia de que no es una venta, deuda, desembolso ni ingreso real.
- Los errores de API muestran el `request_id` como referencia de soporte. La compra se crea con un `Idempotency-Key` estable por cotización.
- Se reemplazaron alturas fijas por `heightIn` en Inicio, Tarjetas y Actividad, se añadieron regiones activas de accesibilidad y el cronograma de cuotas puede desplazarse horizontalmente con texto ampliado.

### Verificación

- `.\gradlew.bat -p backend test`: **BUILD SUCCESSFUL**, 79 pruebas, 0 fallos. Se agregó `ApiContractTest` con el host de pruebas de Ktor: request ID seguro, contrato de error, QR firmado y manipulado, OTP exacto `123456`, cotización con monto distinto (`QR_AMOUNT_MISMATCH`), historial de compras aislado por dueño y `429` con `Retry-After`.
- `.\gradlew :app:assembleDebug`: **BUILD SUCCESSFUL** con el flujo de registro demo y la sección de compras en Actividad.
- No se verificó en emulador o dispositivo físico en este corte; la validación de accesibilidad con texto al 200 % queda pendiente de una sesión con dispositivo conectado.

### Límites vigentes

- La compra sigue siendo `DEMO_CREATED`: no es una venta, cuenta por cobrar, deuda, ingreso ni desembolso.
- `DEMO_ONLY_NOT_ACCEPTED` no registra aceptación legal. Los documentos legales siguen en borrador.
- El backend continúa siendo local, sin PSP real, KYC real, scoring, cobranza ni exposición a internet.

## Cierre integral de módulos construibles según PRD v5.2 — 2026-10-22

**Corte:** 2026-10-22. Completa los módulos construibles de producto, backend y analítica definidos en `PRD_Kuota_Peru_v5.md` sin duplicar pantallas ni servicios, conservando la frontera honesta respecto a APIs externas y dictamen legal.

### 1. Política versionada de elegibilidad y evolución de cuota inicial 30% → 0% (PRD §18.21, §21.2)

- `POST /v1/eligibility-checks` evalúa la política versionada `DEMO_POLICY_2026_V2` y separa explícitamente `risk_score` (`620` a `860`) y `risk_tier` (`ENTRY_30`, `PROGRESS_20`, `PROGRESS_10`, `TRUSTED_0`) del porcentaje de cuota inicial (`down_payment_basis_points`: `3000`, `2000`, `1000` o `0`).
- La reducción de inicial se calcula en el backend según las cuotas pagadas/conciliadas del usuario autenticado (o el parámetro de prueba `simulated_paid_installments` en entorno demo).
- `POST /v1/quotes` aplica el `down_payment_basis_points` aprobado en la decisión vinculada, ajusta `kouta_initial_contribution_cents` para que el abono inmediato al proveedor siga siendo exactamente el 60% (`supplier_immediate_payout_cents`) y conserva la partida doble balanceada en el ledger append-only.
- En Android (`FinancingScreen.kt`), el usuario puede elegir el nivel de historial demo (`Automático`, `30%`, `20%`, `10%`, `0%`) y visualizar la versión de política, el score de riesgo y la inicial aprobada devueltos por el servidor.

### 2. Ciclo completo de pago de cuotas, comprobantes, reversos y conciliación en Android (PRD §8.7–8.14, §18.5–18.11, §18.20)

- `PaymentMethodsScreen.kt` (pestaña **Tarjetas**) conecta la preferencia de medio de pago con los planes activos de `GET /v1/purchases`, mostrando el desglose B2B completo, cada cuota programada, su último intento/comprobante (`PaymentIntentDto`) y controles interactivos contra el backend (`POST /v1/installments/{id}/payment-intents` y `POST /v1/payments/{id}/demo-events`):
  - Iniciar/reintentar pago de cuota con bloqueo de duplicados cuando hay un intento en curso.
  - Confirmar liquidación (`SUCCEED`), simular rechazo (`FAIL`), simular descuadre de importe (`SUCCEED_AMOUNT_MISMATCH`), devolución (`REFUND`), reverso (`REVERSE`) y conciliación manual en ledger (`RECONCILE_MATCH`).
- `HomeScreen.kt` (pestaña **Cuenta**) consulta `GET /v1/purchases` cuando hay sesión activa y muestra en la tarjeta principal la **próxima cuota pendiente** (monto, vencimiento, proveedor, progreso de cuotas y botón directo **Pagar o gestionar cuota** hacia Tarjetas) o el nivel de historial alcanzado (`ENTRY_30` a `TRUSTED_0`).
- `SalesScreen.kt` (pestaña **Actividad**) muestra el desglose B2B completo de cada operación y los comprobantes/referencias y estados de conciliación por cuota.

### 3. Liquidaciones dinámicas y Portal B2B del Proveedor (PRD §8.25, §8.26, §18.18, §18.20)

- Se añadió `GET /v1/supplier/settlements` en `Application.kt`, que calcula en tiempo real las operaciones B2B registradas en `DemoStore` (total mercadería, inicial del comprador, aporte inicial Kouta, abono inmediato 60%, diferido bruto 40%, fee proveedor 4%, diferido neto 36% y estado de liberación según cuotas pagadas).
- `/supplier-portal` ahora renderiza datos reales del `DemoStore` e incluye un generador interactivo de QR firmado por cualquier monto en soles junto con la tabla viva de liquidaciones y su estado de liberación.

### 4. KYC conectado al contrato del backend y Marco Legal D-01 a D-12 (PRD §6, §8.2)

- `DniVerificationScreen.kt` invoca `POST /v1/kyc/dni` (`KuotaApiClient.submitKycDni`) y refleja explícitamente si el proveedor externo no está configurado (`503 KYC_PROVIDER_NOT_CONFIGURED`), conservando el modo demo local cifrado sin afirmar verificación real con RENIEC.
- `LegalScreen.kt` y `docs/DECISIONES_NEGOCIO_LEGAL.md` incorporan la matriz completa **D-01 a D-12** de `PRD_Kuota_Peru_v5.md` (§6) con responsables e hitos límite.

### 5. Analítica de producto sin PII (PRD §15, §18.14)

- `KuotaAnalytics.kt` implementa el catálogo de 17 eventos de PRD §15 (`onboarding_started`, `business_profile_completed`, `sales_source_authorized`, `sales_sync_status`, `eligibility_submitted`, `eligibility_result`, `inventory_quote_created`, `offer_viewed`, `offer_accepted`, `supplier_disbursement_status`, `plan_activated`, `installment_viewed`, `payment_attempt_created`, `payment_status_changed`, `supplier_settlement_status`, `support_case_created`, `refund_status_changed`).
- Sanitización estricta por lista blanca de claves, bloqueo de fragmentos sensibles (`dni`, `phone`, `email`, `name`, `token`, `amount`, etc.) y filtro regex contra correos, JWTs y secuencias numéricas de 7+ dígitos.
- Auditoría en vivo dentro de `LegalScreen.kt` y pruebas unitarias en `KuotaAnalyticsTest.kt`.

### 6. Verificación ejecutada

- `.\gradlew.bat -p backend test`: **BUILD SUCCESSFUL** (80 pruebas, incluyendo la nueva prueba contractual de política versionada y evolución de cuota inicial 30% → 0%).
- `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`: **BUILD SUCCESSFUL** (pruebas unitarias de `KuotaAnalyticsTest`, `PeruBusinessCalendarTest`, `PurchaseOfferFormatTest` + generación del APK debug).

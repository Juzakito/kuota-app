# Decisiones de negocio y marco legal — PRD §6

**Corte:** 2026-10-20  
**Estado:** documento de trabajo. **No es un dictamen legal, no acredita registro ante la SBS ni aprobación de documentos.**  
**Regla:** ninguna decisión de este registro está aprobada. Mientras sigan pendientes, Kuota opera solo como demostración y no otorga crédito, no cobra cuotas y no registra aceptaciones de documentos no aprobados.

## 1. Alcance y límites de este registro

- El PRD v5.2 §6 define las decisiones **D-01 a D-12** que condicionan la operación en producción real. Todas están incorporadas en este registro y en la pantalla **Marco legal** de la app (`LegalScreen.kt`).
- Este documento prepara la decisión, no la toma. Las opciones describen caminos posibles para que dirección, finanzas y asesoría legal peruana los evalúen.
- Los "borradores" de Términos y Condiciones, Política de Privacidad y contratos son insumos de trabajo. No deben publicarse, aceptarse ni presentarse como vigentes hasta contar con dictamen y aprobación formal.
- La app no debe habilitar consentimientos, firmas ni aceptaciones mientras los documentos no estén aprobados y versionados.

## 2. Registro de decisiones

### D-01 — Definición del acreedor / entidad que presta el dinero

| Campo | Valor |
|---|---|
| Estado | **PENDIENTE** |
| Responsable de la decisión | Dirección General, con Finanzas y asesoría legal |
| Evidencia mínima para cerrar | Dictamen legal sobre perímetro regulatorio ante la SBS; modelo económico aprobado; contratos base; tratamiento contable; aprobación formal de la decisión |
| Impacto en producto | Define quién figura como acreedor, quién desembolsa, quién cobra y qué documentos debe aceptar el usuario |
| Fecha objetivo | **Sin definir** |

Preguntas que la decisión debe responder:

1. ¿Quién presta el dinero y quién es el acreedor en el contrato y en el estado de cuenta?
2. ¿Quién desembolsa al proveedor y en qué momento?
3. ¿Quién cobra las cuotas, concilia pagos y atiende reclamos?
4. ¿Quién reporta a centrales de riesgo y con qué base legal?
5. ¿Cómo se refleja la operación en el ledger y en los estados financieros?
6. ¿Qué autorizaciones, registros o contratos exige el modelo elegido?

Opciones a evaluar (ninguna seleccionada):

| Opción | Descripción | Ventajas potenciales | Condiciones / riesgos |
|---|---|---|---|
| A. Kuota como entidad financiera propia | Kuota constituye o autoriza una entidad ante la SBS y presta directamente. | Control del producto, la marca y el riesgo. | Capital, gobierno corporativo, requisitos regulatorios, plazos y carga de cumplimiento. |
| B. Entidad financiera aliada | Un banco, financiera o caja municipal otorga el crédito; Kuota origina y opera la experiencia. | Menor capital propio y menor tiempo inicial. | Dependencia comercial, integración tecnológica, reglas de suscripción y responsabilidades de cobranza. |
| C. Tercero inversionista o fondo | Un tercero adquiere o financia las cuentas por cobrar; Kuota actúa como originador o administrador. | Escala sin balance propio. | Cesión válida de derechos, garantías, cobranza, tratamiento fiscal y transparencia al usuario. |
| D. Crédito propio sin captar depósitos | Kuota financia con recursos propios, sujeto a confirmación del perímetro aplicable. | Simplicidad operativa inicial. | Exige confirmar con abogado si el modelo requiere autorización y cómo se cumplen las reglas de crédito al consumidor. |

**Estado:** no hay opción aprobada ni recomendación vinculante. La elección requiere evaluación legal y económica documentada.

### D-02 — Quién asume la mora y el riesgo de crédito

| Campo | Valor |
|---|---|
| Estado | **PENDIENTE** |
| Responsable de la decisión | Dirección General, con Finanzas, Riesgos y asesoría legal |
| Evidencia mínima para cerrar | Política de riesgo; garantías; reglas de cobranza y castigo; asientos contables; contrato que asigne la pérdida; aprobación formal |
| Dependencia | Debe ser coherente con D-01 y con los contratos de financiamiento |
| Fecha objetivo | **Sin definir** |

Opciones a evaluar (ninguna seleccionada):

| Opción | Descripción | Consecuencias a documentar |
|---|---|---|
| A. Kuota asume toda la mora | Kuota soporta la pérdida del 100% del saldo impago. | Capital, provisiones, cobranza, impacto en precios y en la comisión. |
| B. La entidad financiera asume toda la mora | Venta o cesión sin recurso; el aliado asume el riesgo. | Requisitos de elegibilidad, precio del riesgo, obligaciones de repurchase si el contrato lo prevé. |
| C. Riesgo compartido | Garantía, primera pérdida, fondo de reserva o compra de cartera con recurso parcial. | Reglas de imputación, límites, reporte y conciliación entre partes. |
| D. Un tercero asume el riesgo | Fondo, aseguradora o inversionista asume la pérdida según contrato. | Póliza o contrato de asunción, cobertura, exclusiones y subrogación. |

La decisión debe quedar reflejada en: contrato con el usuario, contrato con la entidad/fondo, política de cobranza, tratamiento contable, reporte a centrales de riesgo (si aplica) y consentimiento/información al titular de los datos.

### D-03 a D-12 — Matriz completa según PRD v5.2 §6

| ID | Decisión | Opciones / alcance del PRD | Responsable | Hito límite | Estado |
|---|---|---|---|---|---|
| **D-03** | Perímetro SBS / casa de préstamo / factoring / crédito comercial | Dictamen regulatorio sobre registro SBS o estructura comercial B2B | Legal regulatorio Perú | Antes de piloto real | **PENDIENTE** |
| **D-04** | Política y contrato de proveedor B2B (60/40, 4% fee, reversos, disputas) | Adelanto 60%, saldo 40% al cierre de cuotas, fee 4%, manejo de devoluciones | Comercial + Legal | Antes de afiliar proveedores | **PENDIENTE** |
| **D-05** | T&C, Política de Privacidad, consentimiento y cláusulas informativas | Textos definitivos conforme a Ley 29733, DS 016-2024-JUS y Ley 29571 | Legal / Privacidad | Antes de publicar app | **PENDIENTE** |
| **D-06** | Proveedor de pagos y liquidación en Perú | Selección e integración productiva (Culqi, Niubiz, Izipay, Kushki, etc.) | Producto + Finanzas + Tech | Antes de cobros reales | **PENDIENTE** |
| **D-07** | Proveedor de firma electrónica y evidencia contractual | Firma electrónica y trazabilidad auditable (Ley 27269) | Legal + Tech | Antes de contratos reales | **PENDIENTE** |
| **D-08** | Proveedor KYC / identidad / validación documental | Biometría, documento oficial y listas restrictivas | Riesgo + Legal + Tech | Antes de evaluación real | **PENDIENTE** |
| **D-09** | Fuentes válidas de ventas verificadas | Homologación de POS, facturación electrónica, adquirencia o extractos | Riesgo + Producto + Tech | Antes de usar ventas en score | **PENDIENTE** |
| **D-10** | Política de precios al comprador | Estructura legal del fee fijo 4%, interés/comisión equivalente, mora y transparencia | Dirección + Legal + Finanzas | Antes de cotizar en producción | **PENDIENTE** |
| **D-11** | Política de evolución de cuota inicial | Reglas de reducción progresiva de 30% a 0% según historial de pago y riesgo (`risk_score` separado de `down_payment_basis_points`) | Riesgo + Finanzas + Producto | Antes de habilitar mejora en producción | **PENDIENTE** |
| **D-12** | Canal operativo del Libro de Reclamaciones | Atención operativa dentro de 15 días hábiles y copia automática al consumidor | Operaciones + Legal | Antes de atención pública | **PENDIENTE** |

## 3. Dictamen legal peruano

**Estado general: PENDIENTE.** No existe aprobación formal de Términos y Condiciones, Política de Privacidad ni contratos de financiamiento de inventario. La app no registra aceptaciones de estos documentos.

### 3.1 Términos y Condiciones

Contenido mínimo a revisar y aprobar:

- Identidad y datos de contacto del proveedor del servicio.
- Descripción del servicio y de lo que **no** incluye (por ejemplo, que la app demo no otorga crédito real).
- Requisitos, elegibilidad, precios, comisiones, tasas, cargos y forma de cálculo.
- Calendario, vencimientos, pagos, mora, cobranza, cancelación anticipada y devoluciones.
- Obligaciones y derechos del usuario, del proveedor y del acreedor.
- Atención de reclamos, Libro de Reclamaciones y mecanismos de solución de controversias.
- Vigencia, modificaciones, ley aplicable y jurisdicción.
- Fecha de versión, mecanismo de aceptación y evidencia auditable (usuario, fecha, versión, hash del documento).
- Revisión de cláusulas abusivas y, si se trata de cláusulas generales de contratación, evaluación de su depósito ante INDECOPI cuando corresponda.

Estado actual en la app: **no hay documento aprobado ni botón de aceptación**. No se registra consentimiento.

### 3.2 Política de Privacidad — Ley 29733 y DS 016-2024-JUS

La política debe redactarse y validarse con abogado peruano. Como mínimo debe cubrir:

- Identidad y contacto del responsable del tratamiento y, si corresponde, del delegado de protección de datos.
- Finalidades, base legal y categorías de datos tratados.
- Origen de los datos y obligatoriedad de proporcionarlos.
- Destinatarios, encargados, transferencias nacionales o internacionales y garantías aplicables.
- Plazo de conservación y criterios de supresión.
- Medidas de seguridad y gestión de incidentes.
- Derechos de acceso, rectificación, cancelación, oposición y los demás reconocidos por la normativa.
- Canal y procedimiento para ejercer derechos y presentar consultas o reclamos.
- Uso de notificaciones, identificadores del dispositivo y servicios de terceros.
- Versión, fecha de vigencia y cambios sustanciales.

Inventario de datos del prototipo (para dimensionar la política, no para publicarla):

| Dato | Dónde se trata | Observación |
|---|---|---|
| Nombre, negocio, correo, celular y RUC | Perfil local cifrado; respaldo del servidor solo con cuenta vinculada | Datos declarados por el usuario |
| Resumen de DNI | Perfil local cifrado | La verificación de identidad es simulada; no hay KYC real |
| Ventas manuales | Archivo local cifrado; respaldo opcional en backend demo | No verificadas; no se conectan a Yape/Plin/POS/bancos |
| Reclamos y mensajes de soporte | Almacenamiento local y backend demo | No hay canal formal de soporte aprobado |
| Token de notificaciones / identificador de instalación | Backend demo cuando se vinculan notificaciones | Sin Firebase configurado no hay push real |
| Token de sesión | Almacenamiento cifrado en el teléfono | Se elimina al cerrar sesión |

Pendiente antes de publicar una política: registro del banco de datos si corresponde, evaluación de impacto cuando aplique, contratos con encargados, protocolo de brechas, retención, transferencias y designación del responsable de privacidad. **Nada de esto está aprobado.**

### 3.3 Contratos de financiamiento de inventario

La estructura contractual depende de D-01/D-02. Debe cubrir, como mínimo:

- Partes, rol del acreedor y del proveedor; identificación del comprador.
- Objeto: financiamiento de inventario o compra de cuentas por cobrar, según el modelo.
- Monto, moneda, desembolso, cronograma, cuotas, inicial, comisiones, tasas, TCEA y cargos.
- Condiciones de aprobación, documentación y verificación del negocio.
- Propiedad, posesión, traslado y seguro del inventario; obligaciones del proveedor.
- Mora, intereses moratorios, gastos de cobranza, castigo y reporte a centrales de riesgo cuando corresponda.
- Cancelación anticipada, novación, cesión, garantías y subrogación.
- Tratamiento de datos personales y autorizaciones específicas.
- Solución de controversias, ley aplicable y jurisdicción.
- Firma electrónica y evidencia (Ley 27269 y normativa aplicable), conservación y auditoría.

Documentos relacionados a preparar: contrato marco con proveedores, contrato de financiamiento con el comprador, cesión de derechos, pagaré o título equivalente si corresponde, política de cobranza y cartas de instrucción.

### 3.4 Otras verificaciones peruanas a confirmar con abogado

- Perímetro regulatorio de la SBS aplicable al modelo D-01/D-02.
- Reglas de crédito al consumidor, transparencia de tasas, publicidad y cobranza.
- Ley 29571 (protección al consumidor), Libro de Reclamaciones y cláusulas generales de contratación.
- Ley 29733 y DS 016-2024-JUS: base legal, derechos, seguridad, brechas y transferencias.
- Ley 27269 (firma y certificados digitales) para la evidencia contractual.
- Obligaciones tributarias y contables del financiamiento y de la cesión.
- Prevención de lavado de activos y financiamiento del terrorismo: confirmar si Kuota sería sujeto obligado y con qué alcance.
- Seguros, garantías y tratamiento del inventario.
- Evaluación de accesibilidad y de canales de atención.

**Ninguna de estas verificaciones está completada.**

## 4. Evidencias requeridas para cerrar el punto 6

| Evidencia | Estado | Responsable sugerido |
|---|---|---|
| Dictamen legal sobre D-01/D-02 y perímetro SBS | Pendiente | Asesoría legal externa |
| Decisión formal de dirección sobre acreedor y mora | Pendiente | Dirección General |
| Términos y Condiciones aprobados y versionados | Pendiente | Legal + Producto |
| Política de Privacidad aprobada conforme a Ley 29733 / DS 016-2024-JUS | Pendiente | Legal + Privacidad |
| Contratos de financiamiento de inventario aprobados | Pendiente | Legal + Finanzas |
| Diseño de aceptación con evidencia auditable | Pendiente | Producto + Legal + Ingeniería |
| Registro de actividades de tratamiento y retención | Pendiente | Privacidad + Ingeniería |
| Protocolo de incidentes de datos | Pendiente | Seguridad + Privacidad |
| Revisión de seguridad y aprobación de negocio | Pendiente | Seguridad + Dirección |

## 5. Qué hace hoy la app (v1.7.0)

- Muestra un estado legal informativo en **Perfil → Marco legal y privacidad** con la matriz completa **D-01 a D-12**.
- Declara que D-01 a D-12 están pendientes y que Términos, Privacidad y contratos siguen en borrador.
- No incluye botones de aceptación, firma o consentimiento de documentos no aprobados.
- No otorga crédito real, no genera deudas exigibles, no cobra cuotas reales y no mueve dinero.
- La verificación de identidad, el soporte, los pagos de cuotas y las ventas son de demostración; no hay integración productiva activa con Yape/Plin/POS/bancos ni centrales de riesgo.
- Incluye auditoría visible de analítica de producto sin PII (`KuotaAnalytics`) conforme a PRD §15 y §18.14.

## 6. Historial de cambios del registro

| Fecha | Cambio | Estado |
|---|---|---|
| 2026-10-20 | Se crea el registro; D-01/D-02 documentadas como pendientes; D-03 a D-12 marcadas por incorporar; checklist legal inicial. | Borrador de trabajo |
| 2026-10-22 | Se incorpora la matriz completa D-01 a D-12 desde `PRD_Kuota_Peru_v5.md` (§6) y se sincroniza con `LegalScreen.kt`. | Borrador de trabajo |

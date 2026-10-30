# PRD — Landing Page para Proveedores y Comercios | Kuota
> **Producto:** KUOTA para Comercios y Proveedores (`kuota-ia.vercel.app?perfil=comercios`)  
> **Versión:** 2.0 (Alineada al Ecosistema de Colocaciones, Recaudación y Respaldo VALT)  
> **Fecha:** 29 de octubre de 2026 · **Estado:** Implementado y Aprobado para Producción  
> **Audiencia Primaria:** Distribuidores mayoristas, importadores, fabricantes, almacenes comerciales y negocios proveedores que venden mercadería e insumos a crédito o al contado.

---

## 1. Resumen Ejecutivo y Propuesta de Valor

La landing page de **Comercios y Proveedores de Kuota** está diseñada para transformar el modelo tradicional de ventas al por mayor en Perú. 

Hoy, los proveedores enfrentan dos grandes cuellos de botella:
1. **Pérdida de ventas por falta de liquidez del cliente:** Sus compradores (bodegas, minimarkets, ferreterías) quieren comprar más mercadería, pero no tienen efectivo disponible en ese momento.
2. **El peligro del fiado tradicional:** Si el proveedor decide dar crédito directo de su propio bolsillo, asume el 100% del riesgo de cobranza, mora y capital de trabajo inmovilizado en cuadernos de fiado o letras impagas.

### La Solución Kuota para Proveedores:
> **"Vende más mercadería a tus clientes ofreciéndoles pago en kuotas, con 0% de riesgo de impago para tu negocio. Kuota te liquida el 100% de la factura al contado en 24-48 horas y asume la recaudación mediante la infraestructura tecnológica de VALT."**

### Métricas Clave de Conversión (KPIs):
- **Conversión Principal 1 (Distribuidores Grandes):** Reunión ejecutiva agendada mediante Cal.com para integración de catálogo y líneas mayoristas.
- **Conversión Principal 2 (Comercios y Pymes):** Afiliación digital inmediata completada con RUC vía `MerchantAffiliationModal`.
- **Interacción con Demostración Operativa:** % de visitantes que simulan el ciclo de colocación y cobranza en el demo interactivo.
- **Uso de la Calculadora de Recaudación:** Estimación de volumen mensual y ahorro frente a comisiones bancarias convencionales.

---

## 2. Los Tres Pilares del Ecosistema para Proveedores

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                       ECOSISTEMA PROVEEDOR KUOTA                            │
├─────────────────────────┬─────────────────────────┬─────────────────────────┤
│ 1. COLOCACIONES         │ 2. CANALES DE COBRO     │ 3. RECAUDACIÓN Y CONTROL│
│ Ventas a crédito para   │ Kuota QR dinámico en    │ Liquidación bancaria al │
│ tus clientes con 0%     │ almacén y Kuota Link vía│ contado en 24-48h con   │
│ riesgo de impago.       │ WhatsApp sin terminales.│ panel en tiempo real.   │
└─────────────────────────┴─────────────────────────┴─────────────────────────┘
```

1. **Colocaciones Comerciales:** Kuota financia la compra de tu cliente. Tú emites la factura comercial o boleta como siempre, Kuota te transfiere los fondos y tu cliente paga sus kuotas en plataforma.
2. **Canales de Cobro Ágiles (QR y Link):** Sin costos de alquiler de máquinas POS pesadas. Tu cliente escanea el QR en mostrador o recibe un enlace de pago por WhatsApp para validar la operación en segundos.
3. **Control y Recaudación Centralizada:** Dashboard en tiempo real (`/dashboard`) donde visualizas facturas aprobadas, recepciones confirmadas por almacén y liquidaciones bancarias directas en Soles y Dólares.

---

## 3. Sistema de Diseño y Lenguaje Visual

Alineado con el documento rector `DESIGN.md`:
- **Estética Fintech B2B de Alto Contraste:** Macro-tarjetas con bordes limpios `rounded-[32px] md:rounded-[48px]`, superficies claras `#FFFFFF` sobre lienzo `#F4F6F8` y acentos escénicos oscuros `#070A0F`.
- **Paleta Cromática:** Acentos en verde esmeralda (`#10B981`) para señales de liquidación exitosa, cian Nexo (`#06B6D4`) para sincronización de canales y azul profundo institucional (`#00235C`).
- **Botones Cápsula (`rounded-full`):** Botones de llamada a la acción prominentes con padding generoso (`px-7 py-3.5`).
- **Números Tabulares (`tabular-nums`):** Formato contable para comisiones, montos facturados, plazos y ahorros anuales proyectados.

---

## 4. Estructura General de la Página

1. **Navbar Fijo:** Logotipo oficial de Kuota, badge `by VALT` y botón de afiliación rápida / agendar llamada.
2. **Hero Principal (El Gancho Comercial B2B):** *"Más oportunidades para vender. Más posibilidades para tus clientes."*
3. **Demostración Operativa en Vivo:** Simulador interactivo del ciclo completo (Venta → Colocación → Cuotas → Liquidación en 48h).
4. **Problema vs. Solución para el Proveedor:** Comparación directa entre vender con riesgo de fiado vs. vender con respaldo Kuota.
5. **Red de Medios de Pago Integrada:** Aceptación multicanal con Yape, Plin y tarjetas de todos los bancos nacionales.
6. **Los Módulos del Producto (3 Chips Interactivos):** Kuota QR de almacén, Kuota Link por WhatsApp y aKuotas! B2B.
7. **Calculadora de Impacto y Ahorro:** Comparador interactivo de ventas adicionales proyectadas y comisiones optimizadas.
8. **Seguridad, Transparencia y Respaldo VALT:** Infraestructura cloud TLS 1.3 y cumplimiento normativo.
9. **Tarifario y Esquema de Afiliación Transparente:** Comisión competitiva y fija, sin costos ocultos de membresía.
10. **Embudo Dual de Cierre (Cal.com + Modal RUC):** Agenda con el equipo comercial o registro directo en 3 minutos.
11. **Footer Corporativo:** Enlaces legales, SLA, seguridad de datos y canales ejecutivos.

---

## 5. Especificación Detallada por Sección

### 5.1 Hero Principal
- **Titular H1:** *"Más oportunidades para vender. Más facilidades para tus clientes."*
- **Subtítulo:** *"Conecta tu catálogo o almacén con Kuota. Ofrece compras en cuotas a tus compradores recurrentes, recibe tu dinero al contado y acelera la rotación de tu mercadería con 0% riesgo de impago."*
- **CTA Primario:** Botón cápsula negro `Quiero Afiliar mi Negocio` (abre modal de captura rápida con RUC).
- **CTA Secundario:** `Agendar Llamada Estratégica` (smooth scroll hacia Cal.com).
- **Tarjeta Escénica Flotante:** Mockup visual de dashboard de proveedor mostrando:
  - *Ventas Kuoteadas:* `S/ 84,200.00`
  - *Liquidación Programada:* `Hoy a las 18:00 (100% al contado)`
  - *Riesgo de Cobranza:* `0.0% (Asumido por Kuota & VALT)`

---

### 5.2 Demostración Operativa en Vivo (`MerchantOperationDemo.tsx`)
Un componente interactivo donde el dueño o gerente del comercio puede simular una venta paso a paso:
1. **Paso 1 (Registro de Venta):** El proveedor ingresa el monto de mercadería (ej. S/ 4,500 en abarrotes o insumos).
2. **Paso 2 (Generación de QR / Link):** Se genera el código dinámico para el comprador.
3. **Paso 3 (Aprobación y Cuotas):** El comprador selecciona su plan (30, 60 o 90 días) con su línea Kuota pre-aprobada.
4. **Paso 4 (Liquidación y Despacho):** El proveedor recibe confirmación instantánea en pantalla y programa el despacho con el dinero garantizado.

---

### 5.3 Problema vs. Solución para el Proveedor
Dos macro-tarjetas bento contrastadas:
- **Tarjeta "Venta Tradicional con Riesgo" (Fondo oscuro neutro):**
  - Dolor 1: Vendes fiado y tu capital de trabajo queda atrapado en la calle por 60 o 90 días.
  - Dolor 2: Tu equipo gasta tiempo valioso cobrando por teléfono o lidiando con cuentas incobrables.
  - Dolor 3: Los POS bancarios te cobran comisiones elevadas y no le dan financiamiento real a tus clientes.
- **Tarjeta "Con Kuota y Respaldo VALT" (Borde esmeralda / alto relieve):**
  - Beneficio 1: Cobras el 100% del valor de la mercadería en 24 a 48 horas hábiles directamente en tu cuenta bancaria.
  - Beneficio 2: 0% riesgo de impago. Si el comprador se atrasa, la gestión de recaudación es responsabilidad exclusiva de Kuota.
  - Beneficio 3: Tus clientes te compran tickets más grandes (hasta +40% en volumen) porque pueden pagar en kuotas.

---

### 5.4 Red de Cobranza y Billeteras Integradas
- Banner continuo de prueba social técnica.
- Logos en alta fidelidad: **BCP, BBVA, Interbank, Scotiabank, Yape y Plin**.
- Mensaje: *"Tus clientes te pagan desde la aplicación que ya usan todos los días, sin fricción ni registros complejos."*

---

### 5.5 Módulos del Producto (3 Chips Interactivos)
Layout split con mockup de terminal / panel web a la izquierda y 3 chips de selección a la derecha:
1. **Chip 1 — Kuota QR de Mostrador y Almacén:**
   - Digitas el monto de la compra en tu celular o tablet, tu cliente escanea el QR desde su app bancaria o Kuota y la orden queda liquidada.
2. **Chip 2 — Kuota Link por WhatsApp:**
   - Para pedidos a distancia o despachos a provincia. Envías el enlace de cobro por WhatsApp y recibes la confirmación de pago en tiempo real antes de cargar el camión.
3. **Chip 3 — aKuotas! B2B para Clientes Recurrentes:**
   - Permite a tus mejores clientes acceder a financiamiento en 30, 60 o 90 días con su línea pre-aprobada por Kuota, incentivando pedidos de mayor volumen.

---

### 5.6 Calculadora de Impacto y Ahorro para Comercios
- **Entradas:** Volumen de ventas mensual estimado (deslizador en Soles, ej. S/ 30,000 a S/ 200,000+).
- **Salidas en Tiempo Real:**
  - Aumento estimado de ventas por financiamiento al cliente (+25% a +35%).
  - Capital de trabajo protegido (cero dinero atrapado en fiados).
  - Tasa de liquidación neta transparente.
- **Nota técnica:** Todas las tasas y comisiones se parametrizan en un único archivo de configuración (`pricingConfig.ts`) para garantizar coherencia en todo el código.

---

### 5.7 Tarifario y Planes Transparentes
Estructura clara de afiliación sin letras chicas ni cláusulas de permanencia forzada:
- **Plan Comercios Afiliados (Estándar):**
  - Comisión fija y transparente por transacción kuoteada.
  - Sin costo de afiliación base (S/ 0.00).
  - Incluye acceso al dashboard, generación ilimitada de QR/Links y soporte ejecutivo.
- **Plan Distribuidores Mayoristas (Corporativo):**
  - Para empresas con facturación mayorista recurrente.
  - Integración directa con ERP contable (SAP, Spring, Excel automático).
  - Soporte de gestor de cuenta asignado por VALT y cobranza multimoneda.

---

### 5.8 Embudo Dual de Conversión
Para maximizar el cierre de acuerdos comerciales con comercios de cualquier escala:
1. **Afiliación Digital Inmediata con RUC (Vía Modal):**
   - El dueño o administrador ingresa RUC 10 / RUC 20, teléfono de contacto y tipo de comercio.
   - Recibe pre-calificación en 2 horas y contacto inmediato vía WhatsApp Ejecutivo.
2. **Agenda de Reunión Ejecutiva (Vía Cal.com Embed):**
   - Para distribuidores y marcas mayoristas que requieren demostración guiada del software, revisión de márgenes y acuerdo de colocaciones masivas.
   - Enlace directo a calendario compartido con el equipo de dirección de Kuota.

---

### 5.9 Footer Corporativo
- Mismas especificaciones institucionales del PRD Maestro: enlaces legales (Términos, Privacidad, SLA, Ley N° 29733), Libro de Reclamaciones virtual y sello de infraestructura `by VALT`.

---

## 6. Eventos de Analítica (Telemetría de Proveedores)

| Nombre de Evento | Parámetros Registrados | Acción Medida |
| :--- | :--- | :--- |
| `supplier_hero_cta` | `type: affiliate / book_call`, `origin: hero` | Interés en el hook principal de ventas |
| `supplier_demo_step`| `step: 1..4`, `simulated_amount: number` | Interacción con la demostración operativa en vivo |
| `supplier_calc_use` | `monthly_sales: number`, `projected_growth: number`| Simulación de incremento de ventas |
| `supplier_chip_view`| `chip_name: qr / link / akuotas` | Módulo de cobro más demandado |
| `supplier_cal_click`| `cal_event_type: executive_call` | Apertura del calendario de reuniones Cal.com |
| `supplier_modal_lead`| `ruc: string`, `business_type: string` | Envío del formulario de afiliación rápida |

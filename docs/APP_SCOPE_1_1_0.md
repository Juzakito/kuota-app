# Kuota Perú — alcance de la app 1.1.0

## Funciones disponibles

- Registro de perfil local: correo, celular y RUC con validación de formato. El perfil se guarda cifrado en el almacenamiento privado de Android.
- Registro manual de ventas: crear, consultar, editar y eliminar montos en el mismo dispositivo. Los registros no se envían al servidor ni afectan una evaluación crediticia.
- Resumen de ventas de los últimos siete días calculado a partir de ese registro local.
- Borrado del perfil local y reanudación del registro cuando la persona vuelve a abrir la app.

## Funciones retiradas

- Código de acceso fijo y presentación del perfil local como si fuera una cuenta verificada.
- Comprador, aprobación crediticia, QR y ofertas de cuotas precargados.
- Creación de compras y cuotas ficticias, historial de deuda simulado, estados de pago manuales y conciliación sintética.
- Conexión Android al backend local de demostración y permisos de red asociados.

## Financiamiento real todavía no habilitado

La app bloquea la cotización y creación de financiamientos hasta que existan e integren: entidad financiadora y responsable contractual; condiciones comerciales versionadas; proveedor de identidad y política de evaluación; registro y firma de términos; QR/POS de proveedores; PSP y cuentas de liquidación; notificaciones, conciliación bancaria y atención de incidencias. No se debe inferir aprobación, saldo o deuda a partir de datos locales.

El servicio anterior en `backend/` se conserva como herramienta de desarrollo sin conexión desde el APK. No es un backend productivo ni debe exponerse a clientes.

## Validaciones pendientes antes de un piloto

- Probar guardado, edición, lectura y eliminación en dispositivos Android reales.
- Definir autenticación de cuenta, sincronización segura y recuperación de acceso; el perfil actual vive solo en un dispositivo.
- Confirmar condiciones de crédito, operaciones, avisos y tratamiento de datos con los responsables de producto y asesores locales antes de activar financiamiento.

# KUOTA — SUPABASE POSTGRESQL MIRROR (RÉPLICA SECUNDARIA)

Este directorio contiene las definiciones de base de datos y scripts de migración para **Supabase PostgreSQL**, funcionando exclusivamente como **espejo secundario (mirror)** de **Firebase** para auditoría contable, conciliación e inteligencia de datos de las MYPEs de KUOTA.

---

## 1. Principio Arquitectónico

```
┌─────────────────────────────────┐
│     APK KUOTA (Android)         │
│  - No se comunica con Supabase │
│  - Solo opera con Firebase      │
└────────────────┬────────────────┘
                 │ (Operaciones oficiales)
┌────────────────▼────────────────┐
│   Firebase (Backend Principal)  │
│  - Authentication               │
│  - Cloud Firestore              │
│  - Cloud Functions              │
│  - Firebase Cloud Messaging     │
└────────────────┬────────────────┘
                 │ (Replicación server-side idempotente)
┌────────────────▼────────────────┐
│   Supabase (Mirror Secundario)  │
│  - PostgreSQL                   │
│  - Tablas _mirror               │
│  - sync_outbox_log              │
│  - Auditoría e INDECOPI         │
└─────────────────────────────────┘
```

> [!IMPORTANT]
> **Seguridad:** El APK de Android **nunca** debe poseer la clave `service_role` ni conectarse directamente a Supabase. Toda la réplica se ejecuta del lado del servidor mediante los triggers de Firebase Cloud Functions en [supabaseMirror.ts](file:///c:/Users/josem/OneDrive/Escritorio/Kuota%20App/firebase/functions/src/supabaseMirror.ts).

---

## 2. Pasos para Configurar Supabase

1. Entra a [supabase.com](https://supabase.com) e inicia sesión.
2. Haz clic en **New Project**:
   * **Nombre:** `Kuota Mirror`
   * **Contraseña de base de datos:** *(Elige una contraseña segura y guárdala)*
   * **Región:** `US East (North Virginia)` o `South America (São Paulo)`.
3. Ve al **SQL Editor** en la consola de Supabase.
4. Abre y copia el contenido del archivo de migración:
   * [supabase/migrations/20261023000000_kuota_mirror.sql](file:///c:/Users/josem/OneDrive/Escritorio/Kuota%20App/supabase/migrations/20261023000000_kuota_mirror.sql)
5. Haz clic en **RUN** para ejecutar la migración. Se crearán:
   * `merchants_mirror`
   * `sales_mirror`
   * `installments_mirror`
   * `claims_mirror`
   * `sync_outbox_log` (auditoría e idempotencia)
   * Políticas RLS (Row Level Security) que bloquean accesos no autorizados.

---

## 3. Conectar Cloud Functions con Supabase

En la consola de Supabase, ve a **Project Settings** -> **API**:
* Copia el **Project URL** (`https://xxxxxxxx.supabase.co`).
* Copia el **service_role secret key** (`ey...`).

En tu terminal local, asigna los secretos a las Cloud Functions de Firebase:

```powershell
cd "c:\Users\josem\OneDrive\Escritorio\Kuota App\firebase"
firebase functions:secrets:set SUPABASE_URL
# Pega tu Project URL cuando lo solicite

firebase functions:secrets:set SUPABASE_SERVICE_ROLE_KEY
# Pega tu service_role secret key cuando lo solicite
```

O para pruebas locales, colócalos en `firebase/functions/.env`:
```env
SUPABASE_URL="https://xxxxxxxx.supabase.co"
SUPABASE_SERVICE_ROLE_KEY="eyJhbGciOi..."
```

---

## 4. Verificación de Idempotencia y Reconciliación

Cada operación replicada se registra en `sync_outbox_log` con:
* `event_id`: ID del evento en Firebase.
* `payload_digest`: Hash SHA-256 del contenido. Si el evento se reintenta con el mismo contenido, no genera registros duplicados (`ON CONFLICT DO UPDATE`).
* `attempt_count`: Contador de reintentos en caso de fallos intermitentes de red.

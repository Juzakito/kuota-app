# Sistema de color Kuota

Fuente única de verdad: `Color.kt` + `Theme.kt` en este paquete. Las pantallas no deben declarar colores hexadecimales propios; usan los roles de Material 3 o los tokens extendidos.

## Primitivas de marca

| Token | Hex | Uso |
|---|---|---|
| `KuotaBlack` | `#05070A` | Base del modo oscuro y fondo del logo |
| `KuotaBlue` | `#5B8FF9` | Azul de marca; `primary` en oscuro y degradado |
| `KuotaSky` | `#A8DFFF` | Celeste de marca; `secondary` en oscuro |
| `KuotaTurquoise` | `#55DDE0` | Turquesa de marca; `tertiary` en oscuro |
| `KuotaWhite` | `#FFFFFF` | Superficies y texto sobre azul profundo |
| `KuotaInk` | `#04101F` | Texto/iconos sobre superficies de marca |
| `KuotaBlueStrong` | `#2D5FCC` | `primary` en claro (contraste 5.8:1 con blanco) |

`KuotaBrandGradient` = azul → celeste → turquesa. Úsalo en momentos de marca (botón QR central, logo), no en textos largos ni en fondos de lectura.

## Esquemas Material 3

`KuotaLightColorScheme` y `KuotaDarkColorScheme` cubren todos los roles de `MaterialTheme.colorScheme`: primary, secondary, tertiary, containers, background, surface, error, outline e inverse. El modo oscuro usa `KuotaBlack` como fondo y azul/celeste/turquesa como acentos.

Reglas:
- Texto normal: `onSurface` / `onSurfaceVariant`; nunca `primary` sobre blanco en claro (el rol claro ya usa la variante accesible).
- Superficies de tarjetas: `surface` o `surfaceContainer*`; no hex directos.
- Estados de error: `colorScheme.error` / `errorContainer`.
- El modo dinámico de Android está desactivado por defecto para preservar la identidad.

## Tokens extendidos

Acceso: `MaterialTheme.kuotaColors`.

| Token | Uso |
|---|---|
| `brandGradient`, `onBrand` | Degradado de identidad y contenido sobre él |
| `headerGradient`, `headerContent`, `headerGlass` | Cabecera de Inicio y botones translúcidos |
| `suggestionGradient`, `suggestionContent`, `suggestionAccent` | Tarjeta promocional de Inicio |
| `mediaBackdrop` | Fondo de video/avatar en onboarding |
| `avatarTint` | Iniciales del avatar |
| `warningAccent`, `warningContainer`, `warningContent` | Avisos preventivos y banner de seguridad |
| `success`, `successContainer` | Cuotas pagadas / respuestas resueltas |
| `info`, `infoContainer` | Soporte / notificaciones informativas |
| `neutral` | Estado local o sin sincronizar |

## Ejemplos

```kotlin
// Color de una pantalla
Text("Saldo", color = MaterialTheme.colorScheme.onSurfaceVariant)

// Token propio
Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.kuotaColors.warningContainer)) {
    Text("Pendiente", color = MaterialTheme.kuotaColors.warningContent)
}

// Degradado de marca
Box(Modifier.background(Brush.linearGradient(KuotaBrandGradient), CircleShape)) {
    Icon(Icons.Default.QrCodeScanner, tint = MaterialTheme.kuotaColors.onBrand)
}
```

## Assets estáticos

El icono adaptativo (`res/drawable-nodpi/kuota_launcher_foreground.png`), el logo de onboarding (`kuota_mark.png`), la mascota (`res/drawable/kuota_onboarding_avatar.png`), `res/values/colors.xml` y el color de notificación del sistema no leen Kotlin. Si cambian las primitivas de marca, hay que regenerar los PNG con el nuevo degradado y actualizar los colores de ventana/notificación. La mascota se recoloreó con fondo transparente y cajas azules; el fondo del icono se mantiene negro (`#000000`).

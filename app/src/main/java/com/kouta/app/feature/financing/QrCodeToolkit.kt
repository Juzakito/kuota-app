package com.kouta.app.feature.financing

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import java.util.EnumMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

object QrCodeToolkit {

    fun generateQrBitmap(content: String, sizePx: Int = 640): Bitmap {
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
            put(EncodeHintType.MARGIN, 2)
        }
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (matrix.get(x, y)) AndroidColor.rgb(4, 16, 31) else AndroidColor.WHITE
            }
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }

    fun generateShareableSupplierQrCard(
        qrPayload: String,
        supplierName: String,
        amountFormatted: String,
        expiresInMinutes: Int = 15
    ): Bitmap {
        val width = 900
        val height = 1160
        val cardBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(cardBitmap)

        // Fondo oscuro de marca Kuota
        canvas.drawColor(AndroidColor.rgb(5, 7, 10))

        // Tarjeta blanca central
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
        }
        val cardRect = RectF(48f, 56f, width - 48f, height - 56f)
        canvas.drawRoundRect(cardRect, 44f, 44f, cardPaint)

        // Franja superior azul Kuota
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(45, 95, 204)
            style = Paint.Style.FILL
        }
        val headerRect = RectF(48f, 56f, width - 48f, 220f)
        canvas.drawRoundRect(headerRect, 44f, 44f, headerPaint)
        canvas.drawRect(48f, 160f, width - 48f, 220f, headerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("KUOTA PERÚ · QR DE COMPRA B2B", width / 2f, 128f, titlePaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(217, 230, 255)
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Financiamiento de inventario en cuotas", width / 2f, 176f, subtitlePaint)

        // Nombre del proveedor y monto
        val supplierPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(10, 16, 23)
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(supplierName.take(36), width / 2f, 290f, supplierPaint)

        val amountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(45, 95, 204)
            textSize = 62f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(amountFormatted, width / 2f, 368f, amountPaint)

        // Código QR centrado
        val qrSize = 560
        val qrBitmap = generateQrBitmap(qrPayload, qrSize)
        val qrLeft = (width - qrSize) / 2f
        val qrTop = 400f
        canvas.drawBitmap(qrBitmap, qrLeft, qrTop, null)

        // Pie informativo
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(70, 80, 95)
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Escanea con la cámara en la pestaña QR de Kuota", width / 2f, 1015f, footerPaint)
        canvas.drawText("Vigencia: $expiresInMinutes min · Modo demostración", width / 2f, 1055f, footerPaint)

        return cardBitmap
    }

    fun shareSupplierQrImage(
        context: Context,
        qrPayload: String,
        supplierName: String,
        amountFormatted: String
    ) {
        val bitmap = generateShareableSupplierQrCard(
            qrPayload = qrPayload,
            supplierName = supplierName,
            amountFormatted = amountFormatted
        )
        val shareDir = File(context.cacheDir, "shared_qr").apply { mkdirs() }
        val file = File(shareDir, "kuota_qr_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_TEXT,
                "QR de proveedor Kuota ($supplierName) por $amountFormatted. Escanéalo con la cámara en la pestaña QR de Kuota o súbelo desde tu galería para pagar en cuotas."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Compartir QR de pago Kuota")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun decodeQrFromUri(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream) ?: return null
                decodeQrFromBitmap(bitmap)
            }
        }.getOrNull()
    }

    fun decodeQrFromBitmap(bitmap: Bitmap): String? {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val source = RGBLuminanceSource(width, height, pixels)
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
            put(DecodeHintType.POSSIBLE_FORMATS, listOf(BarcodeFormat.QR_CODE))
            put(DecodeHintType.TRY_HARDER, true)
            put(DecodeHintType.CHARACTER_SET, "UTF-8")
        }
        val reader = MultiFormatReader().apply { setHints(hints) }

        return runCatching {
            reader.decodeWithState(BinaryBitmap(HybridBinarizer(source))).text
        }.recoverCatching {
            reader.reset()
            reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(source))).text
        }.getOrNull()
    }
}

@Composable
fun QrCameraScannerDialog(
    onDismiss: () -> Unit,
    onPickFromGallery: () -> Unit,
    onQrDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var torchEnabled by remember { mutableStateOf(false) }
    var activeCamera by remember { mutableStateOf<Camera?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    val detectedOnce = remember { AtomicBoolean(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF05070A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Barra superior
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar escáner",
                            tint = Color.White
                        )
                    }
                    Text(
                        "Escanear QR de proveedor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = {
                            val next = !torchEnabled
                            torchEnabled = next
                            activeCamera?.cameraControl?.enableTorch(next)
                        },
                        enabled = activeCamera?.cameraInfo?.hasFlashUnit() == true
                    ) {
                        Icon(
                            if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Linterna",
                            tint = Color.White
                        )
                    }
                }

                // Vista central de cámara o solicitud de permiso
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF10161F)),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission && cameraError == null) {
                        val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
                        DisposableEffect(Unit) {
                            onDispose {
                                runCatching { analysisExecutor.shutdown() }
                            }
                        }

                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                val previewView = PreviewView(ctx).apply {
                                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    runCatching {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.surfaceProvider = previewView.surfaceProvider
                                        }
                                        val reader = MultiFormatReader().apply {
                                            val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
                                                put(DecodeHintType.POSSIBLE_FORMATS, listOf(BarcodeFormat.QR_CODE))
                                                put(DecodeHintType.TRY_HARDER, true)
                                                put(DecodeHintType.CHARACTER_SET, "UTF-8")
                                            }
                                            setHints(hints)
                                        }
                                        val imageAnalysis = ImageAnalysis.Builder()
                                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                            .build()
                                            .also { analysis ->
                                                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                                    val decoded = decodeFrameWithZxing(imageProxy, reader)
                                                    if (!decoded.isNullOrBlank() && detectedOnce.compareAndSet(false, true)) {
                                                        previewView.post {
                                                            onQrDetected(decoded)
                                                        }
                                                    }
                                                    imageProxy.close()
                                                }
                                            }

                                        cameraProvider.unbindAll()
                                        activeCamera = cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageAnalysis
                                        )
                                    }.onFailure { err ->
                                        cameraError = err.localizedMessage ?: "No se pudo iniciar la cámara."
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            }
                        )

                        // Marco guía de escaneo
                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .border(
                                    BorderStroke(3.dp, Color(0xFF55DDE0)),
                                    RoundedCornerShape(24.dp)
                                )
                        )
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    cameraError ?: "Permiso de cámara requerido",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "Autoriza el acceso a la cámara para escanear el código QR del proveedor en tiempo real, o selecciona una imagen QR desde tu galería.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                if (!hasCameraPermission) {
                                    Button(
                                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Permitir acceso a la cámara")
                                    }
                                }
                            }
                        }
                    }
                }

                // Pie con instrucción y botón de galería
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Apunta al código QR del proveedor o sube una captura compartida por WhatsApp",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB8C4D4),
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onPickFromGallery,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White)
                            Text(
                                "Leer de galería",
                                modifier = Modifier.padding(start = 8.dp),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Cancelar")
                        }
                    }
                }
            }
        }
    }
}

private fun decodeFrameWithZxing(imageProxy: ImageProxy, reader: MultiFormatReader): String? {
    val plane = imageProxy.planes.firstOrNull() ?: return null
    val buffer = plane.buffer
    val rowStride = plane.rowStride
    val pixelStride = plane.pixelStride
    val width = imageProxy.width
    val height = imageProxy.height

    val data = ByteArray(width * height)
    buffer.rewind()
    if (pixelStride == 1 && rowStride == width) {
        buffer.get(data, 0, width * height)
    } else {
        val rowData = ByteArray(rowStride)
        for (row in 0 until height) {
            val bytesToRead = if (row == height - 1) {
                minOf(rowStride, buffer.remaining())
            } else {
                rowStride
            }
            buffer.get(rowData, 0, bytesToRead)
            val offset = row * width
            if (pixelStride == 1) {
                System.arraycopy(rowData, 0, data, offset, width)
            } else {
                for (col in 0 until width) {
                    data[offset + col] = rowData[col * pixelStride]
                }
            }
        }
    }

    val cropSize = (minOf(width, height) * 0.8f).toInt().coerceAtLeast(120)
    val left = (width - cropSize) / 2
    val top = (height - cropSize) / 2
    val source = PlanarYUVLuminanceSource(
        data,
        width,
        height,
        left,
        top,
        cropSize,
        cropSize,
        false
    )
    return runCatching {
        reader.decodeWithState(BinaryBitmap(HybridBinarizer(source))).text
    }.also {
        reader.reset()
    }.getOrNull()
}

package com.kuota.core.ui.components

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kuota.core.ui.theme.KuotaColors
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executors

enum class CameraGuideShape {
    CARD_RECTANGLE,
    SELFIE_OVAL
}

/**
 * Componente CameraX oficial para captura de KYC (DNI y Selfie) según DESIGN.md y PRD §8.1.
 * - Guía visual de encuadre (tarjeta o selfie ovalado).
 * - Control de flash y botón de disparo estilo fintech.
 * - Retorna los bytes JPEG comprimidos listos para subida a Firebase Storage kyc/{uid}/archivos.
 */
@Composable
fun CameraCaptureView(
    modifier: Modifier = Modifier,
    initialIsFrontCamera: Boolean = false,
    guideShape: CameraGuideShape = CameraGuideShape.CARD_RECTANGLE,
    instructionTitle: String,
    instructionSubtitle: String,
    onCaptured: (ByteArray) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isFrontCamera by remember { mutableStateOf(initialIsFrontCamera) }
    var isFlashOn by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Vista previa de CameraX
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val cameraSelector = if (isFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraInstance = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay con máscara de recorte y marco guía
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val guideRect = when (guideShape) {
                CameraGuideShape.CARD_RECTANGLE -> {
                    // Ratio tarjeta ID DNI 85.6mm x 53.98mm = ~1.58
                    val width = canvasWidth * 0.88f
                    val height = width / 1.58f
                    val left = (canvasWidth - width) / 2f
                    val top = (canvasHeight - height) / 2f - 40f
                    Rect(left, top, left + width, top + height)
                }
                CameraGuideShape.SELFIE_OVAL -> {
                    val width = canvasWidth * 0.72f
                    val height = width * 1.35f
                    val left = (canvasWidth - width) / 2f
                    val top = (canvasHeight - height) / 2f - 40f
                    Rect(left, top, left + width, top + height)
                }
            }

            val path = Path().apply {
                when (guideShape) {
                    CameraGuideShape.CARD_RECTANGLE -> {
                        addRoundRect(
                            RoundRect(
                                rect = guideRect,
                                cornerRadius = CornerRadius(24f, 24f)
                            )
                        )
                    }
                    CameraGuideShape.SELFIE_OVAL -> {
                        addOval(guideRect)
                    }
                }
            }

            // Sombra oscura exterior
            clipPath(path, clipOp = ClipOp.Difference) {
                drawRect(color = Color(0x99000000))
            }

            // Borde guía con contraste Lime Voltage
            drawPath(
                path = path,
                color = KuotaColors.LimeVoltage,
                style = Stroke(width = 6f)
            )
        }

        // Cabecera con instrucciones y botones de control
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x66000000), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar cámara",
                        tint = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!isFrontCamera) {
                        IconButton(
                            onClick = {
                                isFlashOn = !isFlashOn
                                cameraInstance?.cameraControl?.enableTorch(isFlashOn)
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0x66000000), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Flash",
                                tint = if (isFlashOn) KuotaColors.LimeVoltage else Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            isFrontCamera = !isFrontCamera
                            isFlashOn = false
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0x66000000), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Girar cámara",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta de instrucciones en la parte superior
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xCC163300)) // Forest Ink translúcido
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = instructionTitle,
                        color = KuotaColors.LimeVoltage,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = instructionSubtitle,
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Barra inferior con botón de captura (Shutter Button)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isCapturing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(72.dp),
                    color = KuotaColors.LimeVoltage,
                    strokeWidth = 4.dp
                )
            } else {
                // Botón disparador estilo fintech con anillo Lime Voltage y núcleo Forest Ink
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(KuotaColors.LimeVoltage)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(KuotaColors.ForestInk)
                        .clickable {
                            isCapturing = true
                            takePhoto(context, imageCapture, cameraExecutor) { bytes ->
                                isCapturing = false
                                if (bytes != null) {
                                    onCaptured(bytes)
                                }
                            }
                        }
                )
            }
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: java.util.concurrent.Executor,
    onResult: (ByteArray?) -> Unit
) {
    val tempFile = File.createTempFile("kuota_kyc_capture_", ".jpg", context.cacheDir)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                try {
                    val bytes = tempFile.readBytes()
                    tempFile.delete()
                    ContextCompat.getMainExecutor(context).execute {
                        onResult(bytes)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    ContextCompat.getMainExecutor(context).execute { onResult(null) }
                }
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
                ContextCompat.getMainExecutor(context).execute { onResult(null) }
            }
        }
    )
}

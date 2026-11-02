package com.kouta.app.feature.onboarding

import android.Manifest
import android.app.Activity
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kouta.app.R
import com.kouta.app.feature.kyc.KycStatusScreen
import com.kuota.core.model.KycStatus
import com.kuota.core.ui.components.CameraCaptureView
import com.kuota.core.ui.components.CameraGuideShape
import com.kuota.core.ui.components.ChipPill
import com.kuota.core.ui.components.KuotaCard
import com.kuota.core.ui.components.KuotaCardVariant
import com.kuota.core.ui.components.PillButton
import com.kuota.core.ui.components.PillButtonVariant
import com.kuota.core.ui.components.ProgressBarHorizontal
import com.kuota.core.ui.theme.KuotaColors
import com.kuota.core.ui.theme.KuotaTypography

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.detectLocation()
        }
    }

    // Si el usuario ya está aprobado o pendiente, mostrar pantalla de estado KYC
    if (state.kycStatus == KycStatus.APPROVED || state.kycStatus == KycStatus.PENDING || state.kycStatus == KycStatus.REJECTED) {
        KycStatusScreen(
            status = state.kycStatus,
            summary = state.userSummary,
            onNavigateToHome = onOnboardingComplete,
            onRetryKyc = {
                viewModel.setStep(6)
            }
        )
        return
    }

    // Modal de pantalla completa para captura CameraX
    if (state.activeCameraCaptureTarget != CameraCaptureTarget.NONE) {
        val isSelfie = state.activeCameraCaptureTarget == CameraCaptureTarget.SELFIE
        val guideShape = if (isSelfie) CameraGuideShape.SELFIE_OVAL else CameraGuideShape.CARD_RECTANGLE
        val instructionTitle = when (state.activeCameraCaptureTarget) {
            CameraCaptureTarget.DNI_FRONT -> "Frente del DNI"
            CameraCaptureTarget.DNI_BACK -> "Reverso del DNI"
            CameraCaptureTarget.SELFIE -> "Selfie con prueba de vida"
            CameraCaptureTarget.NONE -> ""
        }
        val instructionSubtitle = when (state.activeCameraCaptureTarget) {
            CameraCaptureTarget.DNI_FRONT -> "Enfoca el frente de tu DNI dentro del marco con buena iluminación."
            CameraCaptureTarget.DNI_BACK -> "Enfoca el reverso con el código de barras y firma legibles."
            CameraCaptureTarget.SELFIE -> "Ubica tu rostro dentro del óvalo sin gorras ni lentes oscuros."
            CameraCaptureTarget.NONE -> ""
        }

        CameraCaptureView(
            initialIsFrontCamera = isSelfie,
            guideShape = guideShape,
            instructionTitle = instructionTitle,
            instructionSubtitle = instructionSubtitle,
            onCaptured = { bytes ->
                viewModel.onImageCaptured(bytes)
            },
            onClose = {
                viewModel.closeCamera()
            }
        )
        return
    }

    // Flujo normal de Onboarding con barra de progreso continua (Light First)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KuotaColors.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Cabecera con botón atrás y barra de progreso continua
            if (state.step > 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KuotaColors.White)
                        .padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.previousStep() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = KuotaColors.PrussianBlue
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Paso ${state.step} de 6",
                            color = KuotaColors.PrussianBlue,
                            style = KuotaTypography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Image(
                            painter = painterResource(R.drawable.kuota_mark),
                            contentDescription = "KUOTA",
                            modifier = Modifier.height(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ProgressBarHorizontal(
                        progress = state.step / 6f,
                        modifier = Modifier.fillMaxWidth(),
                        fillColor = KuotaColors.PersianBlue,
                        trackColor = KuotaColors.Lavender
                    )
                }
            }

            // Mensaje de error general si ocurre
            AnimatedVisibility(
                visible = state.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                state.errorMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KuotaColors.AlarmRed.copy(alpha = 0.12f))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = msg,
                            color = KuotaColors.AlarmRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Contenido por paso
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (state.step) {
                    0 -> StepWelcome(onStart = { viewModel.setStep(1) })
                    1 -> StepPhoneOtp(
                        phone = state.phone,
                        otpCode = state.otpCode,
                        isOtpSending = state.isOtpSending,
                        isOtpVerifying = state.isOtpVerifying,
                        verificationId = state.verificationId,
                        onPhoneChange = { viewModel.setPhone(it) },
                        onOtpChange = { viewModel.setOtpCode(it) },
                        onSendOtp = { activity?.let { viewModel.sendPhoneOtp(it) } },
                        onVerifyOtp = { viewModel.verifyPhoneOtp() }
                    )
                    2 -> StepTermsAndPrivacy(
                        termsAgreed = state.termsAgreed,
                        privacyAgreed = state.privacyAgreed,
                        onConsentChange = { terms, priv -> viewModel.setTermsAndPrivacy(terms, priv) },
                        onContinue = { viewModel.nextStep() }
                    )
                    3 -> StepRoleSelection(
                        selectedRole = state.role,
                        onRoleSelect = { viewModel.setRole(it) },
                        onContinue = { viewModel.nextStep() }
                    )
                    4 -> StepBusinessProfile(
                        fullName = state.fullName,
                        dni = state.dni,
                        businessName = state.businessName,
                        category = state.category,
                        ruc = state.ruc,
                        address = state.location.address,
                        department = state.location.department,
                        province = state.location.province,
                        district = state.location.district,
                        latitude = state.location.latitude,
                        longitude = state.location.longitude,
                        isDetectingLocation = state.isDetectingLocation,
                        onUpdateProfile = { fn, d, bn, c, r, a, dep, prov, dist ->
                            viewModel.updateBusinessProfile(fn, d, bn, c, r, a, dep, prov, dist)
                        },
                        onDetectLocation = {
                            if (viewModel.hasLocationPermission()) {
                                viewModel.detectLocation()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        onContinue = { viewModel.nextStep() }
                    )
                    5 -> StepPermissions(
                        onPermissionsGranted = { viewModel.nextStep() }
                    )
                    6 -> StepKycCamera(
                        dniFrontBytes = state.dniFrontBytes,
                        dniBackBytes = state.dniBackBytes,
                        selfieBytes = state.selfieBytes,
                        isSubmitting = state.isSubmittingKyc,
                        onCaptureFront = { viewModel.openCamera(CameraCaptureTarget.DNI_FRONT) },
                        onCaptureBack = { viewModel.openCamera(CameraCaptureTarget.DNI_BACK) },
                        onCaptureSelfie = { viewModel.openCamera(CameraCaptureTarget.SELFIE) },
                        onSubmit = { viewModel.submitKyc() }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Paso 0: Bienvenida e Introducción
// -------------------------------------------------------------
@Composable
private fun StepWelcome(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_kuota_logo_horizontal),
            contentDescription = "KUOTA",
            modifier = Modifier
                .height(64.dp)
                .padding(bottom = 12.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = KuotaTypography.headlineMedium,
            color = KuotaColors.ForestInk,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_subtitle),
            style = KuotaTypography.bodyMedium,
            color = KuotaColors.Charcoal,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(36.dp))

        PillButton(
            text = stringResource(R.string.onboarding_btn_start),
            onClick = onStart,
            variant = PillButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.onboarding_legal_disclaimer),
            style = KuotaTypography.bodySmall,
            color = KuotaColors.Charcoal.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

// -------------------------------------------------------------
// Paso 1: Teléfono y Verificación OTP SMS (+51)
// -------------------------------------------------------------
@Composable
private fun StepPhoneOtp(
    phone: String,
    otpCode: String,
    isOtpSending: Boolean,
    isOtpVerifying: Boolean,
    verificationId: String?,
    onPhoneChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.onboarding_step1_title),
            style = KuotaTypography.headlineMedium,
            color = KuotaColors.ForestInk
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_step1_subtitle),
            style = KuotaTypography.bodyMedium,
            color = KuotaColors.Charcoal
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(R.string.onboarding_phone_label),
            fontWeight = FontWeight.SemiBold,
            color = KuotaColors.ForestInk,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(KuotaColors.White)
                    .border(1.dp, KuotaColors.Periwinkle, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 15.dp)
            ) {
                Text(
                    text = "🇵🇪 +51",
                    fontWeight = FontWeight.Bold,
                    color = KuotaColors.PrussianBlue,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                placeholder = { Text(stringResource(R.string.onboarding_phone_placeholder)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = KuotaColors.PrussianBlue,
                    unfocusedTextColor = KuotaColors.PrussianBlue,
                    focusedBorderColor = KuotaColors.PersianBlue,
                    unfocusedBorderColor = KuotaColors.Periwinkle,
                    focusedContainerColor = KuotaColors.White,
                    unfocusedContainerColor = KuotaColors.White,
                    focusedPlaceholderColor = KuotaColors.PrussianBlue.copy(alpha = 0.45f),
                    unfocusedPlaceholderColor = KuotaColors.PrussianBlue.copy(alpha = 0.45f),
                    cursorColor = KuotaColors.PersianBlue
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (verificationId == null) {
            PillButton(
                text = stringResource(R.string.onboarding_send_code),
                onClick = onSendOtp,
                isLoading = isOtpSending,
                enabled = phone.length == 9,
                variant = PillButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = stringResource(R.string.onboarding_otp_label),
                fontWeight = FontWeight.SemiBold,
                color = KuotaColors.PrussianBlue,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = otpCode,
                onValueChange = onOtpChange,
                placeholder = { Text("123456") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = KuotaColors.PrussianBlue,
                    unfocusedTextColor = KuotaColors.PrussianBlue,
                    focusedBorderColor = KuotaColors.PersianBlue,
                    unfocusedBorderColor = KuotaColors.Periwinkle,
                    focusedContainerColor = KuotaColors.White,
                    unfocusedContainerColor = KuotaColors.White,
                    focusedPlaceholderColor = KuotaColors.PrussianBlue.copy(alpha = 0.45f),
                    unfocusedPlaceholderColor = KuotaColors.PrussianBlue.copy(alpha = 0.45f),
                    cursorColor = KuotaColors.PersianBlue
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            PillButton(
                text = stringResource(R.string.onboarding_verify_code),
                onClick = onVerifyOtp,
                isLoading = isOtpVerifying,
                enabled = otpCode.length == 6,
                variant = PillButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            PillButton(
                text = stringResource(R.string.onboarding_resend_code),
                onClick = onSendOtp,
                variant = PillButtonVariant.Tertiary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// -------------------------------------------------------------
// Paso 2: Términos y Tratamiento de Datos (Ley N° 29733)
// -------------------------------------------------------------
@Composable
private fun StepTermsAndPrivacy(
    termsAgreed: Boolean,
    privacyAgreed: Boolean,
    onConsentChange: (Boolean, Boolean) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.onboarding_step2_title),
            style = KuotaTypography.headlineMedium,
            color = KuotaColors.ForestInk
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_step2_subtitle),
            style = KuotaTypography.bodyMedium,
            color = KuotaColors.Charcoal
        )

        Spacer(modifier = Modifier.height(24.dp))

        KuotaCard(
            modifier = Modifier.fillMaxWidth(),
            variant = KuotaCardVariant.Paper
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = KuotaColors.ForestInk,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Marco de Transparencia Legal",
                        fontWeight = FontWeight.Bold,
                        color = KuotaColors.ForestInk,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.onboarding_legal_disclaimer),
                    style = KuotaTypography.bodySmall,
                    color = KuotaColors.Charcoal,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onConsentChange(!termsAgreed, privacyAgreed) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = termsAgreed,
                onCheckedChange = { onConsentChange(it, privacyAgreed) },
                colors = CheckboxDefaults.colors(
                    checkedColor = KuotaColors.ForestInk,
                    checkmarkColor = KuotaColors.LimeVoltage
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.onboarding_terms_checkbox),
                style = KuotaTypography.bodyMedium,
                color = KuotaColors.ForestInk,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onConsentChange(termsAgreed, !privacyAgreed) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = privacyAgreed,
                onCheckedChange = { onConsentChange(termsAgreed, it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = KuotaColors.ForestInk,
                    checkmarkColor = KuotaColors.LimeVoltage
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.onboarding_privacy_checkbox),
                style = KuotaTypography.bodyMedium,
                color = KuotaColors.ForestInk,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        PillButton(
            text = stringResource(R.string.btn_continue),
            onClick = onContinue,
            enabled = termsAgreed && privacyAgreed,
            variant = PillButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// -------------------------------------------------------------
// Paso 3: Selección de Rol
// -------------------------------------------------------------
@Composable
private fun StepRoleSelection(
    selectedRole: String,
    onRoleSelect: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.onboarding_step3_title),
            style = KuotaTypography.headlineMedium,
            color = KuotaColors.ForestInk
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_step3_subtitle),
            style = KuotaTypography.bodyMedium,
            color = KuotaColors.Charcoal
        )

        Spacer(modifier = Modifier.height(24.dp))

        RoleSelectionCard(
            title = stringResource(R.string.onboarding_role_buyer),
            description = stringResource(R.string.onboarding_role_buyer_desc),
            isSelected = selectedRole == "comprador",
            onClick = { onRoleSelect("comprador") }
        )

        Spacer(modifier = Modifier.height(14.dp))

        RoleSelectionCard(
            title = stringResource(R.string.onboarding_role_supplier),
            description = stringResource(R.string.onboarding_role_supplier_desc),
            isSelected = selectedRole == "proveedor",
            onClick = { onRoleSelect("proveedor") }
        )

        Spacer(modifier = Modifier.height(14.dp))

        RoleSelectionCard(
            title = stringResource(R.string.onboarding_role_both),
            description = stringResource(R.string.onboarding_role_both_desc),
            isSelected = selectedRole == "ambos",
            onClick = { onRoleSelect("ambos") }
        )

        Spacer(modifier = Modifier.height(36.dp))

        PillButton(
            text = stringResource(R.string.btn_continue),
            onClick = onContinue,
            variant = PillButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RoleSelectionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) KuotaColors.PersianBlue else KuotaColors.Periwinkle.copy(alpha = 0.5f)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    KuotaCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        variant = if (isSelected) KuotaCardVariant.Paper else KuotaCardVariant.Fog
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = KuotaColors.PrussianBlue,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = KuotaColors.Charcoal,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(KuotaColors.PersianBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = KuotaColors.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Paso 4: Perfil de Negocio MYPE
// -------------------------------------------------------------
@Composable
private fun StepBusinessProfile(
    fullName: String,
    dni: String,
    businessName: String,
    category: String,
    ruc: String,
    address: String,
    department: String,
    province: String,
    district: String,
    latitude: Double,
    longitude: Double,
    isDetectingLocation: Boolean,
    onUpdateProfile: (String, String, String, String, String, String, String, String, String) -> Unit,
    onDetectLocation: () -> Unit,
    onContinue: () -> Unit
) {
    var fn by remember { mutableStateOf(fullName) }
    var d by remember { mutableStateOf(dni) }
    var bn by remember { mutableStateOf(businessName) }
    var c by remember { mutableStateOf(category) }
    var r by remember { mutableStateOf(ruc) }
    var a by remember { mutableStateOf(address) }
    var dep by remember { mutableStateOf(department) }
    var prov by remember { mutableStateOf(province) }
    var dist by remember { mutableStateOf(district) }

    fun triggerUpdate() {
        onUpdateProfile(fn, d, bn, c, r, a, dep, prov, dist)
    }

    androidx.compose.runtime.LaunchedEffect(address, district, province, department) {
        if (address.isNotBlank()) a = address
        if (district.isNotBlank()) dist = district
        if (province.isNotBlank()) prov = province
        if (department.isNotBlank()) dep = department
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.onboarding_step4_title),
            style = KuotaTypography.headlineMedium,
            color = KuotaColors.ForestInk
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.onboarding_step4_subtitle),
            style = KuotaTypography.bodyMedium,
            color = KuotaColors.Charcoal
        )

        Spacer(modifier = Modifier.height(20.dp))

        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = KuotaColors.PrussianBlue,
            unfocusedTextColor = KuotaColors.PrussianBlue,
            focusedBorderColor = KuotaColors.PersianBlue,
            unfocusedBorderColor = KuotaColors.Periwinkle,
            focusedContainerColor = KuotaColors.White,
            unfocusedContainerColor = KuotaColors.White,
            focusedPlaceholderColor = KuotaColors.PrussianBlue.copy(alpha = 0.45f),
            unfocusedPlaceholderColor = KuotaColors.PrussianBlue.copy(alpha = 0.45f),
            cursorColor = KuotaColors.PersianBlue
        )

        // DNI del Titular
        Text(text = stringResource(R.string.onboarding_dni_label), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = KuotaColors.PrussianBlue)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = d,
            onValueChange = { d = it.filter { ch -> ch.isDigit() }.take(8); triggerUpdate() },
            placeholder = { Text("8 dígitos") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = fieldColors
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Nombres y Apellidos
        Text(text = stringResource(R.string.onboarding_full_name_label), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = KuotaColors.PrussianBlue)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = fn,
            onValueChange = { fn = it; triggerUpdate() },
            placeholder = { Text("Ej. María Flores Quispe") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = fieldColors
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Nombre Comercial del Negocio
        Text(text = stringResource(R.string.onboarding_business_name_label), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = KuotaColors.PrussianBlue)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = bn,
            onValueChange = { bn = it; triggerUpdate() },
            placeholder = { Text("Ej. Confecciones Mary") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = fieldColors
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Rubro Comercial
        Text(text = stringResource(R.string.onboarding_business_category_label), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = KuotaColors.PrussianBlue)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = c,
            onValueChange = { c = it; triggerUpdate() },
            placeholder = { Text("Ej. Textil / Confecciones / Galería") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = fieldColors
        )

        Spacer(modifier = Modifier.height(14.dp))

        // RUC (Opcional)
        Text(text = stringResource(R.string.onboarding_ruc_label), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = KuotaColors.PrussianBlue)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = r,
            onValueChange = { r = it.filter { ch -> ch.isDigit() }.take(11); triggerUpdate() },
            placeholder = { Text("10XXXXXXXXX o 20XXXXXXXXX") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = fieldColors
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Dirección
        Text(text = stringResource(R.string.onboarding_address_label), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = KuotaColors.PrussianBlue)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = a,
            onValueChange = { a = it; triggerUpdate() },
            placeholder = { Text("Ej. Jr. Gamarra 1234, Tda 204") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = fieldColors
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Distrito y Provincia
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Distrito", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = KuotaColors.PrussianBlue)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = dist,
                    onValueChange = { dist = it; triggerUpdate() },
                    placeholder = { Text("La Victoria") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = fieldColors
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Provincia", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = KuotaColors.PrussianBlue)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = prov,
                    onValueChange = { prov = it; triggerUpdate() },
                    placeholder = { Text("Lima") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = fieldColors
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PillButton(
            text = if (isDetectingLocation) "Detectando ubicación GPS..." else stringResource(R.string.onboarding_detect_location),
            onClick = onDetectLocation,
            isLoading = isDetectingLocation,
            enabled = !isDetectingLocation,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = KuotaColors.PersianBlue,
                    modifier = Modifier.size(18.dp)
                )
            },
            variant = PillButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth()
        )

        if (latitude != 0.0 && longitude != 0.0) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KuotaColors.Lavender, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = KuotaColors.PersianBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GPS fijado: ${"%.4f".format(java.util.Locale.US, latitude)}, ${"%.4f".format(java.util.Locale.US, longitude)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KuotaColors.PrussianBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val isReady = d.length == 8 && fn.isNotBlank() && bn.isNotBlank() && c.isNotBlank()
        PillButton(
            text = stringResource(R.string.btn_continue),
            onClick = onContinue,
            enabled = isReady,
            variant = PillButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// -------------------------------------------------------------
// Paso 5: Pantalla de Valor de Permisos
// -------------------------------------------------------------
@Composable
private fun StepPermissions(
    onPermissionsGranted: () -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        onPermissionsGranted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.onboarding_step5_title),
            style = KuotaTypography.headlineMedium,
            color = KuotaColors.ForestInk
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_step5_subtitle),
            style = KuotaTypography.bodyMedium,
            color = KuotaColors.Charcoal,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        KuotaCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(KuotaColors.PersianBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = KuotaColors.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = stringResource(R.string.onboarding_permission_camera_title),
                        fontWeight = FontWeight.Bold,
                        color = KuotaColors.ForestInk,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.onboarding_permission_camera_desc),
                        color = KuotaColors.Charcoal,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        KuotaCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(KuotaColors.PersianBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = KuotaColors.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = stringResource(R.string.onboarding_permission_location_title),
                        fontWeight = FontWeight.Bold,
                        color = KuotaColors.ForestInk,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.onboarding_permission_location_desc),
                        color = KuotaColors.Charcoal,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        PillButton(
            text = stringResource(R.string.onboarding_btn_grant_permissions),
            onClick = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            variant = PillButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// -------------------------------------------------------------
// Paso 6: Captura de Documentos KYC (CameraX)
// -------------------------------------------------------------
@Composable
private fun StepKycCamera(
    dniFrontBytes: ByteArray?,
    dniBackBytes: ByteArray?,
    selfieBytes: ByteArray?,
    isSubmitting: Boolean,
    onCaptureFront: () -> Unit,
    onCaptureBack: () -> Unit,
    onCaptureSelfie: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.onboarding_step6_title),
            style = KuotaTypography.headlineMedium,
            color = KuotaColors.ForestInk
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.onboarding_step6_subtitle),
            style = KuotaTypography.bodyMedium,
            color = KuotaColors.Charcoal
        )

        Spacer(modifier = Modifier.height(24.dp))

        KycCaptureItemCard(
            title = stringResource(R.string.onboarding_kyc_dni_front),
            subtitle = "Lado de la fotografía y datos personales",
            imageBytes = dniFrontBytes,
            onCaptureClick = onCaptureFront
        )

        Spacer(modifier = Modifier.height(14.dp))

        KycCaptureItemCard(
            title = stringResource(R.string.onboarding_kyc_dni_back),
            subtitle = "Lado con código de barras y firma",
            imageBytes = dniBackBytes,
            onCaptureClick = onCaptureBack
        )

        Spacer(modifier = Modifier.height(14.dp))

        KycCaptureItemCard(
            title = stringResource(R.string.onboarding_kyc_selfie),
            subtitle = "Rostro de frente, buena luz, sin accesorios",
            imageBytes = selfieBytes,
            onCaptureClick = onCaptureSelfie
        )

        Spacer(modifier = Modifier.height(36.dp))

        val allCaptured = dniFrontBytes != null && dniBackBytes != null && selfieBytes != null

        PillButton(
            text = stringResource(R.string.onboarding_kyc_submit),
            onClick = onSubmit,
            isLoading = isSubmitting,
            enabled = allCaptured && !isSubmitting,
            variant = PillButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun KycCaptureItemCard(
    title: String,
    subtitle: String,
    imageBytes: ByteArray?,
    onCaptureClick: () -> Unit
) {
    val isCaptured = imageBytes != null
    val bitmap = remember(imageBytes) {
        imageBytes?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
        }
    }

    KuotaCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCaptureClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(KuotaColors.Fog),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = KuotaColors.Charcoal,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = KuotaColors.ForestInk,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = KuotaColors.Charcoal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isCaptured) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(KuotaColors.PersianBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Capturado",
                        tint = KuotaColors.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                ChipPill(
                    label = "Capturar",
                    isSelected = false,
                    onClick = onCaptureClick
                )
            }
        }
    }
}

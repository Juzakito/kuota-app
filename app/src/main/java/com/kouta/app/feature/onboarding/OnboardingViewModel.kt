package com.kouta.app.feature.onboarding

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kouta.app.core.util.LocationResult
import com.kouta.app.core.util.LocationTracker
import com.kuota.core.data.repository.AuthRepository
import com.kuota.core.data.repository.KycRepository
import com.kuota.core.data.repository.UserRepository
import com.kuota.core.model.BusinessLocation
import com.kuota.core.model.KycDraft
import com.kuota.core.model.KycStatus
import com.kuota.core.model.KycSubmission
import com.kuota.core.model.TermsConsent
import com.kuota.core.model.UserSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

enum class CameraCaptureTarget {
    NONE,
    DNI_FRONT,
    DNI_BACK,
    SELFIE
}

data class OnboardingUiState(
    val step: Int = 0,
    val phone: String = "",
    val verificationId: String? = null,
    val otpCode: String = "",
    val isOtpSending: Boolean = false,
    val isOtpVerifying: Boolean = false,
    val termsAgreed: Boolean = false,
    val privacyAgreed: Boolean = false,
    val role: String = "comprador",
    val fullName: String = "",
    val dni: String = "",
    val businessName: String = "",
    val category: String = "",
    val ruc: String = "",
    val location: BusinessLocation = BusinessLocation(
        department = "Lima",
        province = "Lima",
        district = "La Victoria"
    ),
    val dniFrontBytes: ByteArray? = null,
    val dniFrontStoragePath: String? = null,
    val dniBackBytes: ByteArray? = null,
    val dniBackStoragePath: String? = null,
    val selfieBytes: ByteArray? = null,
    val selfieStoragePath: String? = null,
    val activeCameraCaptureTarget: CameraCaptureTarget = CameraCaptureTarget.NONE,
    val isSubmittingKyc: Boolean = false,
    val isDetectingLocation: Boolean = false,
    val kycStatus: KycStatus = KycStatus.UNSUBMITTED,
    val userSummary: UserSummary? = null,
    val errorMessage: String? = null,
    val isDraftLoaded: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val kycRepository: KycRepository,
    private val userRepository: UserRepository,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        observeUserSummary()
        checkExistingAuthAndDraft()
    }

    private fun observeUserSummary() {
        viewModelScope.launch {
            userRepository.observeUserSummary()
                .catch { /* Silently catch network drops */ }
                .collectLatest { summary ->
                    if (summary != null) {
                        val parsedStatus = KycStatus.fromRaw(summary.kycStatus)
                        _uiState.update { it.copy(userSummary = summary, kycStatus = parsedStatus) }
                    }
                }
        }
    }

    private fun checkExistingAuthAndDraft() {
        val uid = authRepository.currentUid
        if (uid != null) {
            viewModelScope.launch {
                kycRepository.observeKycDraft(uid)
                    .catch { emit(null) }
                    .collectLatest { draft ->
                        if (draft != null) {
                            _uiState.update { current ->
                                current.copy(
                                    step = if (current.step == 0) draft.currentStep.coerceIn(1, 6) else current.step,
                                    phone = if (current.phone.isBlank()) draft.phone else current.phone,
                                    role = draft.role,
                                    fullName = draft.fullName,
                                    dni = draft.dni,
                                    businessName = draft.businessName,
                                    category = draft.category,
                                    ruc = draft.ruc.orEmpty(),
                                    location = draft.location ?: current.location,
                                    termsAgreed = draft.termsAgreed,
                                    privacyAgreed = draft.privacyAgreed,
                                    dniFrontStoragePath = draft.dniFrontStoragePath,
                                    dniBackStoragePath = draft.dniBackStoragePath,
                                    selfieStoragePath = draft.selfieStoragePath,
                                    isDraftLoaded = true
                                )
                            }
                        } else {
                            _uiState.update { it.copy(isDraftLoaded = true) }
                        }
                    }
            }
        } else {
            _uiState.update { it.copy(isDraftLoaded = true) }
        }
    }

    fun nextStep() {
        _uiState.update { it.copy(step = (it.step + 1).coerceAtMost(6), errorMessage = null) }
        saveCurrentDraft()
    }

    fun previousStep() {
        _uiState.update { it.copy(step = (it.step - 1).coerceAtLeast(1), errorMessage = null) }
        saveCurrentDraft()
    }

    fun setStep(step: Int) {
        _uiState.update { it.copy(step = step.coerceIn(1, 6), errorMessage = null) }
        saveCurrentDraft()
    }

    fun setPhone(phone: String) {
        val filtered = phone.filter { it.isDigit() }.take(9)
        _uiState.update { it.copy(phone = filtered, errorMessage = null) }
    }

    fun setOtpCode(code: String) {
        val filtered = code.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(otpCode = filtered, errorMessage = null) }
    }

    fun sendPhoneOtp(activity: Activity) {
        val phone = _uiState.value.phone.trim()
        if (phone.length != 9 || !phone.startsWith("9")) {
            _uiState.update { it.copy(errorMessage = "Ingresa un número celular peruano válido (9 dígitos comenzando con 9).") }
            return
        }

        _uiState.update { it.copy(isOtpSending = true, errorMessage = null) }

        val e164Phone = "+51$phone"
        authRepository.sendOtp(
            phoneNumber = e164Phone,
            activity = activity,
            onCodeSent = { verificationId ->
                _uiState.update {
                    it.copy(
                        verificationId = verificationId,
                        isOtpSending = false,
                        errorMessage = null
                    )
                }
            },
            onVerificationCompleted = { credential ->
                // Auto-verificación instantánea por Play Services
                viewModelScope.launch {
                    val result = authRepository.signInWithCredential(credential)
                    result.onSuccess {
                        kycRepository.initUserSummary()
                        _uiState.update { it.copy(isOtpSending = false, step = 2) }
                        saveCurrentDraft()
                    }.onFailure { err ->
                        _uiState.update { it.copy(isOtpSending = false, errorMessage = err.message) }
                    }
                }
            },
            onError = { error ->
                val errorMsg = when {
                    error.message?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true ->
                        "Configuración de Firebase pendiente: En la consola de Firebase activa 'Teléfono' en Authentication y agrega la huella SHA-1."
                    error.message?.contains("TOO_MANY_REQUESTS", ignoreCase = true) == true ->
                        "Demasiados intentos. Espera unos minutos antes de solicitar otro SMS."
                    else -> error.message ?: "Error al enviar código SMS."
                }
                _uiState.update { it.copy(isOtpSending = false, errorMessage = errorMsg) }
            }
        )
    }

    fun verifyPhoneOtp() {
        val verificationId = _uiState.value.verificationId
        val code = _uiState.value.otpCode.trim()

        if (verificationId == null) {
            _uiState.update { it.copy(errorMessage = "Primero solicita un código SMS.") }
            return
        }

        if (code.length != 6) {
            _uiState.update { it.copy(errorMessage = "El código SMS debe tener 6 dígitos.") }
            return
        }

        _uiState.update { it.copy(isOtpVerifying = true, errorMessage = null) }

        viewModelScope.launch {
            val result = authRepository.verifyOtp(verificationId, code)
            result.onSuccess {
                kycRepository.initUserSummary()
                _uiState.update { it.copy(isOtpVerifying = false, step = 2, errorMessage = null) }
                saveCurrentDraft()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isOtpVerifying = false,
                        errorMessage = "Código incorrecto o expirado. Por favor, verifica e inténtalo de nuevo."
                    )
                }
            }
        }
    }

    fun setTermsAndPrivacy(terms: Boolean, privacy: Boolean) {
        _uiState.update {
            it.copy(termsAgreed = terms, privacyAgreed = privacy, errorMessage = null)
        }
        saveCurrentDraft()
    }

    fun setRole(role: String) {
        _uiState.update { it.copy(role = role, errorMessage = null) }
        saveCurrentDraft()
    }

    fun updateBusinessProfile(
        fullName: String,
        dni: String,
        businessName: String,
        category: String,
        ruc: String,
        address: String,
        department: String,
        province: String,
        district: String
    ) {
        val cleanedDni = dni.filter { it.isDigit() }.take(8)
        val cleanedRuc = ruc.filter { it.isDigit() }.take(11)

        _uiState.update { current ->
            current.copy(
                fullName = fullName,
                dni = cleanedDni,
                businessName = businessName,
                category = category,
                ruc = cleanedRuc,
                location = current.location.copy(
                    address = address,
                    department = department,
                    province = province,
                    district = district
                ),
                errorMessage = null
            )
        }
        saveCurrentDraft()
    }

    fun setLocationCoordinates(latitude: Double, longitude: Double) {
        _uiState.update { current ->
            current.copy(
                location = current.location.copy(
                    latitude = latitude,
                    longitude = longitude
                )
            )
        }
        saveCurrentDraft()
    }

    fun hasLocationPermission(): Boolean = locationTracker.hasLocationPermission()

    fun detectLocation() {
        _uiState.update { it.copy(isDetectingLocation = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = locationTracker.getCurrentLocation()) {
                is LocationResult.Success -> {
                    val loc = result.location
                    _uiState.update { current ->
                        current.copy(
                            isDetectingLocation = false,
                            location = current.location.copy(
                                latitude = loc.latitude,
                                longitude = loc.longitude,
                                address = loc.address.ifBlank { current.location.address },
                                district = loc.district.ifBlank { current.location.district },
                                province = loc.province.ifBlank { current.location.province },
                                department = loc.department.ifBlank { current.location.department }
                            ),
                            errorMessage = null
                        )
                    }
                    saveCurrentDraft()
                }
                is LocationResult.PermissionDenied -> {
                    _uiState.update {
                        it.copy(
                            isDetectingLocation = false,
                            errorMessage = "Permiso de ubicación denegado. Puedes ingresar los datos de tu negocio manualmente."
                        )
                    }
                }
                is LocationResult.GpsDisabled -> {
                    _uiState.update {
                        it.copy(
                            isDetectingLocation = false,
                            errorMessage = "El GPS está desactivado. Activa la ubicación en los ajustes de tu teléfono."
                        )
                    }
                }
                is LocationResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isDetectingLocation = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun openCamera(target: CameraCaptureTarget) {
        _uiState.update { it.copy(activeCameraCaptureTarget = target) }
    }

    fun closeCamera() {
        _uiState.update { it.copy(activeCameraCaptureTarget = CameraCaptureTarget.NONE) }
    }

    fun onImageCaptured(bytes: ByteArray) {
        val currentTarget = _uiState.value.activeCameraCaptureTarget
        when (currentTarget) {
            CameraCaptureTarget.DNI_FRONT -> {
                _uiState.update { it.copy(dniFrontBytes = bytes, activeCameraCaptureTarget = CameraCaptureTarget.NONE) }
            }
            CameraCaptureTarget.DNI_BACK -> {
                _uiState.update { it.copy(dniBackBytes = bytes, activeCameraCaptureTarget = CameraCaptureTarget.NONE) }
            }
            CameraCaptureTarget.SELFIE -> {
                _uiState.update { it.copy(selfieBytes = bytes, activeCameraCaptureTarget = CameraCaptureTarget.NONE) }
            }
            CameraCaptureTarget.NONE -> Unit
        }
        saveCurrentDraft()
    }

    fun submitKyc() {
        val state = _uiState.value
        val uid = authRepository.currentUid
        if (uid == null) {
            _uiState.update { it.copy(errorMessage = "Debes iniciar sesión con tu número de teléfono primero.") }
            return
        }

        if (state.dni.length != 8) {
            _uiState.update { it.copy(errorMessage = "El DNI debe contener exactamente 8 dígitos.") }
            return
        }

        if (state.fullName.isBlank() || state.businessName.isBlank() || state.category.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Completa todos los campos obligatorios del negocio.") }
            return
        }

        if (state.dniFrontBytes == null && state.dniFrontStoragePath == null) {
            _uiState.update { it.copy(errorMessage = "Debes capturar el frente de tu DNI.") }
            return
        }

        if (state.dniBackBytes == null && state.dniBackStoragePath == null) {
            _uiState.update { it.copy(errorMessage = "Debes capturar el reverso de tu DNI.") }
            return
        }

        if (state.selfieBytes == null && state.selfieStoragePath == null) {
            _uiState.update { it.copy(errorMessage = "Debes capturar una selfie con prueba de vida.") }
            return
        }

        if (!state.termsAgreed || !state.privacyAgreed) {
            _uiState.update { it.copy(errorMessage = "Debes aceptar los Términos y la Política de Privacidad.") }
            return
        }

        _uiState.update { it.copy(isSubmittingKyc = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // 1. Subir DNI frontal a Storage si tenemos bytes nuevos
                val frontPath = if (state.dniFrontBytes != null) {
                    val uploadResult = kycRepository.uploadKycImage(
                        uid = uid,
                        fileName = "dni_front.jpg",
                        imageBytes = state.dniFrontBytes
                    )
                    uploadResult.getOrThrow()
                } else {
                    state.dniFrontStoragePath!!
                }

                // 2. Subir DNI reverso a Storage
                val backPath = if (state.dniBackBytes != null) {
                    val uploadResult = kycRepository.uploadKycImage(
                        uid = uid,
                        fileName = "dni_back.jpg",
                        imageBytes = state.dniBackBytes
                    )
                    uploadResult.getOrThrow()
                } else {
                    state.dniBackStoragePath!!
                }

                // 3. Subir Selfie a Storage
                val selfiePath = if (state.selfieBytes != null) {
                    val uploadResult = kycRepository.uploadKycImage(
                        uid = uid,
                        fileName = "selfie.jpg",
                        imageBytes = state.selfieBytes
                    )
                    uploadResult.getOrThrow()
                } else {
                    state.selfieStoragePath!!
                }

                _uiState.update {
                    it.copy(
                        dniFrontStoragePath = frontPath,
                        dniBackStoragePath = backPath,
                        selfieStoragePath = selfiePath
                    )
                }

                // 4. Formatear marca de tiempo ISO en America/Lima conforme a AGENTS.md
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("America/Lima")
                }
                val consentedAt = sdf.format(Date())

                val submission = KycSubmission(
                    dni = state.dni,
                    fullName = state.fullName,
                    businessName = state.businessName,
                    category = state.category,
                    ruc = state.ruc.ifBlank { null },
                    role = state.role,
                    location = state.location,
                    dniFrontStoragePath = frontPath,
                    dniBackStoragePath = backPath,
                    selfieStoragePath = selfiePath,
                    termsConsent = TermsConsent(
                        agreed = true,
                        version = "2026-v1",
                        consentedAt = consentedAt
                    )
                )

                // 5. Llamada callable a Firebase Function `submitKyc`
                val submitResult = kycRepository.submitKyc(submission)
                submitResult.onSuccess { res ->
                    _uiState.update {
                        it.copy(
                            isSubmittingKyc = false,
                            kycStatus = KycStatus.fromRaw(res.kycStatus)
                        )
                    }
                }.onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isSubmittingKyc = false,
                            errorMessage = err.message ?: "Error al procesar la verificación KYC."
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("Object does not exist", ignoreCase = true) == true ->
                        "Firebase Storage no está activado en tu proyecto de Firebase. Activa Storage en la consola de Firebase (Build > Storage > Comenzar)."
                    e.message?.contains("permission", ignoreCase = true) == true ->
                        "Permiso denegado al subir a Storage. Verifica las reglas de seguridad."
                    else -> e.message ?: "Error al subir imágenes a Storage."
                }
                _uiState.update {
                    it.copy(
                        isSubmittingKyc = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    private fun saveCurrentDraft() {
        val uid = authRepository.currentUid ?: return
        val s = _uiState.value

        viewModelScope.launch {
            val draft = KycDraft(
                currentStep = s.step,
                phone = s.phone,
                role = s.role,
                fullName = s.fullName,
                dni = s.dni,
                businessName = s.businessName,
                category = s.category,
                ruc = s.ruc.ifBlank { null },
                location = s.location,
                dniFrontStoragePath = s.dniFrontStoragePath,
                dniBackStoragePath = s.dniBackStoragePath,
                selfieStoragePath = s.selfieStoragePath,
                termsAgreed = s.termsAgreed,
                privacyAgreed = s.privacyAgreed,
                termsVersion = "2026-v1"
            )
            kycRepository.saveKycDraft(uid, draft)
        }
    }
}

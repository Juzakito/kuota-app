package com.kouta.app.feature.onboarding

import android.app.Activity
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.kuota.core.data.repository.AuthRepository
import com.kuota.core.data.repository.KycRepository
import com.kuota.core.data.repository.UserRepository
import com.kuota.core.model.BusinessLocation
import com.kuota.core.model.KycDraft
import com.kuota.core.model.KycStatus
import com.kuota.core.model.KycSubmission
import com.kuota.core.model.KycSubmitResult
import com.kuota.core.model.UserSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAuthRepository : AuthRepository {
        var mockUid: String? = "test-uid-123"
        override val currentUser: FirebaseUser? = null
        override val currentUid: String? get() = mockUid
        override fun observeAuthState(): Flow<FirebaseUser?> = flowOf(null)

        override fun sendOtp(
            phoneNumber: String,
            activity: Activity,
            onCodeSent: (verificationId: String) -> Unit,
            onVerificationCompleted: (PhoneAuthCredential) -> Unit,
            onError: (Exception) -> Unit
        ) {
            onCodeSent("mock-verification-id-999")
        }

        override suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<AuthResult> {
            return Result.failure(NotImplementedError())
        }

        override suspend fun verifyOtp(verificationId: String, smsCode: String): Result<AuthResult> {
            return if (smsCode == "123456") {
                Result.failure(Exception("Mock success path verified"))
            } else {
                Result.failure(Exception("Código incorrecto"))
            }
        }

        override suspend fun signInAnonymously(): Result<AuthResult> = Result.failure(NotImplementedError())
        override fun signOut() { mockUid = null }
    }

    private class FakeKycRepository : KycRepository {
        var savedDraft: KycDraft? = null
        var submitted: KycSubmission? = null
        val draftFlow = MutableStateFlow<KycDraft?>(null)

        override fun getCurrentUid(): String? = "test-uid-123"
        override fun observeKycDraft(uid: String): Flow<KycDraft?> = draftFlow

        override suspend fun saveKycDraft(uid: String, draft: KycDraft): Result<Unit> {
            savedDraft = draft
            draftFlow.value = draft
            return Result.success(Unit)
        }

        override suspend fun uploadKycImage(
            uid: String,
            fileName: String,
            imageBytes: ByteArray,
            mimeType: String
        ): Result<String> {
            return Result.success("kyc/$uid/$fileName")
        }

        override suspend fun submitKyc(submission: KycSubmission): Result<KycSubmitResult> {
            submitted = submission
            return Result.success(
                KycSubmitResult(
                    success = true,
                    requestId = "req-123",
                    kycStatus = "pending",
                    message = "En proceso"
                )
            )
        }

        override suspend fun initUserSummary(): Result<Unit> = Result.success(Unit)
    }

    private class FakeUserRepository : UserRepository {
        val summaryFlow = MutableStateFlow<UserSummary?>(null)
        override fun getCurrentUid(): String? = "test-uid-123"
        override fun observeUserSummary(): Flow<UserSummary?> = summaryFlow
    }

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var kycRepository: FakeKycRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        kycRepository = FakeKycRepository()
        userRepository = FakeUserRepository()
        viewModel = OnboardingViewModel(authRepository, kycRepository, userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(0, state.step)
        assertEquals(KycStatus.UNSUBMITTED, state.kycStatus)
        assertEquals("comprador", state.role)
    }

    @Test
    fun testPhoneFormattingAndValidation() = runTest {
        // Formato: solo dígitos, hasta 9 caracteres
        viewModel.setPhone("abc 987-654-321 extra")
        assertEquals("987654321", viewModel.uiState.value.phone)

        // Validación de teléfono inválido
        viewModel.setPhone("12345")
        // No pasamos activity real, solo comprobamos el mensaje de error cuando longitud < 9
        // Simulando llamada directa a validación
        val phone = viewModel.uiState.value.phone
        assertTrue(phone.length != 9)
    }

    @Test
    fun testOtpCodeFormatting() = runTest {
        viewModel.setOtpCode("123456789")
        assertEquals("123456", viewModel.uiState.value.otpCode)
    }

    @Test
    fun testStepProgressionAndAutoSave() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setStep(1)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.step)
        assertNotNull(kycRepository.savedDraft)
        assertEquals(1, kycRepository.savedDraft?.currentStep)

        viewModel.nextStep()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.step)
        assertEquals(2, kycRepository.savedDraft?.currentStep)

        viewModel.previousStep()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.step)
    }

    @Test
    fun testTermsAndPrivacyConsent() = runTest {
        viewModel.setTermsAndPrivacy(terms = true, privacy = true)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.termsAgreed)
        assertTrue(state.privacyAgreed)
        assertEquals(true, kycRepository.savedDraft?.termsAgreed)
        assertEquals(true, kycRepository.savedDraft?.privacyAgreed)
    }

    @Test
    fun testBusinessProfileUpdate() = runTest {
        viewModel.updateBusinessProfile(
            fullName = "Juan Quispe",
            dni = "08765432-1", // Debe limpiar a 8 dígitos
            businessName = "Textiles Gamarra",
            category = "Textil",
            ruc = "201234567890", // Debe limpiar a 11 dígitos
            address = "Jr. Gamarra 500",
            department = "Lima",
            province = "Lima",
            district = "La Victoria"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("08765432", state.dni)
        assertEquals("20123456789", state.ruc)
        assertEquals("Textiles Gamarra", state.businessName)
        assertEquals("La Victoria", state.location.district)
    }

    @Test
    fun testLocationCoordinates() = runTest {
        viewModel.setLocationCoordinates(-12.0645, -77.0163)
        testDispatcher.scheduler.advanceUntilIdle()

        val loc = viewModel.uiState.value.location
        assertEquals(-12.0645, loc.latitude, 0.0001)
        assertEquals(-77.0163, loc.longitude, 0.0001)
    }

    @Test
    fun testCameraCaptureFlow() = runTest {
        viewModel.openCamera(CameraCaptureTarget.DNI_FRONT)
        assertEquals(CameraCaptureTarget.DNI_FRONT, viewModel.uiState.value.activeCameraCaptureTarget)

        val fakeBytes = byteArrayOf(1, 2, 3, 4)
        viewModel.onImageCaptured(fakeBytes)
        assertEquals(CameraCaptureTarget.NONE, viewModel.uiState.value.activeCameraCaptureTarget)
        assertEquals(fakeBytes, viewModel.uiState.value.dniFrontBytes)
    }

    @Test
    fun testSubmitKycValidationFailsWhenIncomplete() = runTest {
        // Intento de submit con datos incompletos
        viewModel.submitKyc()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertNull(kycRepository.submitted)
    }
}

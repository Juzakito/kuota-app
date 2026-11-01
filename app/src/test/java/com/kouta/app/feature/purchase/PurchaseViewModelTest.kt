package com.kouta.app.feature.purchase

import com.kuota.core.data.repository.ConfigRepository
import com.kuota.core.data.repository.PurchaseRepository
import com.kuota.core.data.repository.UserRepository
import com.kuota.core.model.CreditPurchase
import com.kuota.core.model.FeesConfig
import com.kuota.core.model.Installment
import com.kuota.core.model.InstallmentScheduleItem
import com.kuota.core.model.LevelsConfig
import com.kuota.core.model.PurchaseQuote
import com.kuota.core.model.ScoringConfig
import com.kuota.core.model.TermOptionConfig
import com.kuota.core.model.TermsConfig
import com.kuota.core.model.UserSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PurchaseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakePurchaseRepository : PurchaseRepository {
        var createPurchaseResult: Result<CreditPurchase>? = null
        var buyerConfirmResult: Result<Boolean> = Result.success(true)
        var providerApproveResult: Result<Boolean> = Result.success(true)
        var cancelPurchaseResult: Result<Boolean> = Result.success(true)
        val purchaseFlow = MutableStateFlow<CreditPurchase?>(null)

        override suspend fun quotePurchase(totalAmountCents: Long, installmentsCount: Int): Result<PurchaseQuote> {
            val downPaymentCents = (totalAmountCents * 30) / 100
            val financedCents = totalAmountCents - downPaymentCents
            val feeCents = (financedCents * 5) / 100
            val totalDebtCents = financedCents + feeCents
            val baseInstallment = totalDebtCents / installmentsCount
            val remainder = totalDebtCents - (baseInstallment * installmentsCount)

            val installments = (1..installmentsCount).map { i ->
                InstallmentScheduleItem(
                    installmentNumber = i,
                    dueDate = "2026-11-${String.format("%02d", i * 7)}",
                    amountCents = if (i == installmentsCount) baseInstallment + remainder else baseInstallment
                )
            }

            return Result.success(
                PurchaseQuote(
                    totalAmountCents = totalAmountCents,
                    downPaymentPercent = 30,
                    downPaymentCents = downPaymentCents,
                    financedAmountCents = financedCents,
                    buyerFeePercent = 5,
                    buyerFeeCents = feeCents,
                    totalDebtCents = totalDebtCents,
                    installmentsCount = installmentsCount,
                    installments = installments,
                    providerFeePercent = 2,
                    providerFeeCents = (totalAmountCents * 2) / 100,
                    providerNetCents = (totalAmountCents * 98) / 100,
                    providerAdvancePercent = 60,
                    providerAdvanceCents = (totalAmountCents * 60) / 100,
                    providerDeferredCents = (totalAmountCents * 38) / 100
                )
            )
        }

        override suspend fun createPurchase(
            supplierId: String,
            supplierName: String,
            totalAmountCents: Long,
            installmentsCount: Int,
            notes: String?
        ): Result<CreditPurchase> {
            return createPurchaseResult ?: Result.success(
                CreditPurchase(
                    purchaseId = "PUR-TEST-123",
                    buyerUid = "buyer-1",
                    providerUid = supplierId,
                    supplierName = supplierName,
                    totalAmountCents = totalAmountCents,
                    installmentsCount = installmentsCount,
                    status = "QUOTED",
                    expiresAtEpochMillis = System.currentTimeMillis() + 900_000L
                )
            )
        }

        override fun observePurchase(purchaseId: String): Flow<CreditPurchase?> = purchaseFlow

        override suspend fun providerApprovePurchase(purchaseId: String): Result<Boolean> = providerApproveResult

        override suspend fun buyerConfirmPurchase(purchaseId: String): Result<Boolean> = buyerConfirmResult

        override suspend fun cancelPurchase(purchaseId: String, reason: String?): Result<Boolean> = cancelPurchaseResult

        override fun observeUserInstallments(buyerUid: String): Flow<List<Installment>> = flowOf(emptyList())

        override fun observeUserPurchases(buyerUid: String): Flow<List<CreditPurchase>> = flowOf(emptyList())
    }

    private class FakeUserRepository : UserRepository {
        val summary = MutableStateFlow<UserSummary?>(
            UserSummary(
                uid = "test-uid",
                level = 1,
                maxCreditCents = 140_000L,
                availableCreditCents = 140_000L,
                kycStatus = "approved"
            )
        )
        override fun getCurrentUid(): String? = "test-uid"
        override fun observeUserSummary(): Flow<UserSummary?> = summary
    }

    private class FakeConfigRepository : ConfigRepository {
        override suspend fun getLevelsConfig(): LevelsConfig = LevelsConfig()
        override suspend fun getFeesConfig(): FeesConfig = FeesConfig()
        override suspend fun getTermsConfig(): TermsConfig = TermsConfig(
            terms = listOf(
                TermOptionConfig(days = 21, installmentsCount = 3),
                TermOptionConfig(days = 28, installmentsCount = 4),
                TermOptionConfig(days = 42, installmentsCount = 6)
            )
        )
        override suspend fun getScoringConfig(): ScoringConfig = ScoringConfig()
        override fun observeLevelsConfig(): Flow<LevelsConfig> = flowOf(LevelsConfig())
    }

    private lateinit var fakePurchaseRepository: FakePurchaseRepository
    private lateinit var fakeUserRepository: FakeUserRepository
    private lateinit var fakeConfigRepository: FakeConfigRepository
    private lateinit var viewModel: PurchaseViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePurchaseRepository = FakePurchaseRepository()
        fakeUserRepository = FakeUserRepository()
        fakeConfigRepository = FakeConfigRepository()

        viewModel = PurchaseViewModel(
            purchaseRepository = fakePurchaseRepository,
            userRepository = fakeUserRepository,
            configRepository = fakeConfigRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun verifyDefaultQuoteMatchesFinancialExample() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Monto inicial por defecto debe ser 1000", "1000", state.amountInput)
        assertEquals("Monto en céntimos debe ser 100,000", 100_000L, state.amountCents)
        assertEquals("4 cuotas por defecto", 4, state.selectedInstallmentsCount)

        val quote = state.quote
        assertNotNull("La cotización no debe ser nula", quote)
        assertEquals("Inicial 30% debe ser S/ 300.00", 30_000L, quote!!.downPaymentCents)
        assertEquals("Financiado debe ser S/ 700.00", 70_000L, quote.financedAmountCents)
        assertEquals("Fee comprador 5% debe ser S/ 35.00", 3_500L, quote.buyerFeeCents)
        assertEquals("Deuda total debe ser S/ 735.00", 73_500L, quote.totalDebtCents)

        assertEquals("Debe tener 4 cuotas", 4, quote.installments.size)
        quote.installments.forEach { inst ->
            assertEquals("Cada cuota debe ser S/ 183.75", 18_375L, inst.amountCents)
        }
    }

    @Test
    fun verifyAmountChangeRecalculatesQuote() = runTest {
        advanceUntilIdle()

        viewModel.onAmountChanged("500")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("500", state.amountInput)
        assertEquals(50_000L, state.amountCents)

        val quote = state.quote
        assertNotNull(quote)
        assertEquals("Inicial 30% de 500 es 150", 15_000L, quote!!.downPaymentCents)
        assertEquals("Financiado de 500 es 350", 35_000L, quote.financedAmountCents)
        assertEquals("Fee 5% de 350 es 17.50", 1_750L, quote.buyerFeeCents)
        assertEquals("Total deuda es 367.50", 36_750L, quote.totalDebtCents)
    }

    @Test
    fun verifyRequestPurchaseMovesToStep2Waiting() = runTest {
        advanceUntilIdle()

        viewModel.requestPurchase()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(PurchaseFlowStep.STEP_2_WAITING, state.currentStep)
        assertNotNull(state.activePurchase)
        assertEquals("PUR-TEST-123", state.activePurchase!!.purchaseId)
    }

    @Test
    fun verifyConfirmPurchaseMovesToSuccess() = runTest {
        advanceUntilIdle()

        viewModel.requestPurchase()
        advanceUntilIdle()

        viewModel.confirmPurchase()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(PurchaseFlowStep.STEP_SUCCESS, state.currentStep)
    }

    @Test
    fun verifyCancelPurchaseResetsToStep1() = runTest {
        advanceUntilIdle()

        viewModel.requestPurchase()
        advanceUntilIdle()

        assertEquals(PurchaseFlowStep.STEP_2_WAITING, viewModel.uiState.value.currentStep)

        viewModel.cancelPurchase()
        advanceUntilIdle()

        assertEquals(PurchaseFlowStep.STEP_1_DETAILS, viewModel.uiState.value.currentStep)
    }
}

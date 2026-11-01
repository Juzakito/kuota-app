package com.kouta.app.feature.supplier

import com.kuota.core.data.repository.SupplierRepository
import com.kuota.core.data.repository.UserRepository
import com.kuota.core.model.CreditPurchase
import com.kuota.core.model.ExpectedPayout
import com.kuota.core.model.PaymentLink
import com.kuota.core.model.ProviderProfile
import com.kuota.core.model.SupplierPayoutSummary
import com.kuota.core.model.UserSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SupplierQrViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeSupplierRepository : SupplierRepository {
        var generateQrResult: (Long?) -> Result<String> = { amount ->
            if (amount == null) {
                Result.success("KUOTA|STATIC|mock_payload|mock_sig")
            } else {
                Result.success("KUOTA|DYNAMIC|mock_dynamic_${amount}|mock_sig")
            }
        }

        override suspend fun generateSupplierQr(amountCents: Long?): Result<String> {
            return generateQrResult(amountCents)
        }

        override suspend fun createPaymentLink(
            amountCents: Long,
            clientPhone: String,
            clientDni: String?,
            concept: String?
        ): Result<PaymentLink> = Result.failure(NotImplementedError())

        override suspend fun getExpectedEarnings(): Result<SupplierPayoutSummary> =
            Result.failure(NotImplementedError())

        override fun observeSupplierPayouts(providerUid: String): Flow<List<ExpectedPayout>> =
            flowOf(emptyList())

        override fun observePublicProviders(
            categoryFilter: String?,
            query: String?
        ): Flow<List<ProviderProfile>> = flowOf(emptyList())

        override fun observePaymentLinks(providerUid: String): Flow<List<PaymentLink>> =
            flowOf(emptyList())

        override fun observePendingPurchasesForApproval(providerUid: String): Flow<List<CreditPurchase>> =
            flowOf(emptyList())
    }

    private class FakeUserRepository : UserRepository {
        override fun getCurrentUid(): String? = "test_user_uid"
        override fun observeUserSummary(): Flow<UserSummary?> = flowOf(null)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads static QR successfully`() = runTest {
        val fakeRepo = FakeSupplierRepository()
        val fakeUserRepo = FakeUserRepository()

        val viewModel = SupplierQrViewModel(fakeRepo, fakeUserRepo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.selectedTab)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals("KUOTA|STATIC|mock_payload|mock_sig", state.staticQrPayload)
    }

    @Test
    fun `selecting dynamic tab generates dynamic QR for default amount`() = runTest {
        val fakeRepo = FakeSupplierRepository()
        val fakeUserRepo = FakeUserRepository()

        val viewModel = SupplierQrViewModel(fakeRepo, fakeUserRepo)
        advanceUntilIdle()

        viewModel.onTabSelected(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.selectedTab)
        assertFalse(state.isLoading)
        assertNotNull(state.dynamicQrPayload)
        assertTrue(state.dynamicQrPayload!!.contains("mock_dynamic_45000"))
    }

    @Test
    fun `changing dynamic amount regenerates QR with new amount`() = runTest {
        val fakeRepo = FakeSupplierRepository()
        val fakeUserRepo = FakeUserRepository()

        val viewModel = SupplierQrViewModel(fakeRepo, fakeUserRepo)
        advanceUntilIdle()

        viewModel.onTabSelected(1)
        advanceUntilIdle()

        viewModel.onAmountChanged(80_000L) // S/ 800.00
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(80_000L, state.dynamicAmountCents)
        assertTrue(state.dynamicQrPayload!!.contains("mock_dynamic_80000"))
    }

    @Test
    fun `error during QR generation exposes readable message`() = runTest {
        val fakeRepo = FakeSupplierRepository()
        fakeRepo.generateQrResult = { Result.failure(RuntimeException("Error de conexión con Firebase")) }
        val fakeUserRepo = FakeUserRepository()

        val viewModel = SupplierQrViewModel(fakeRepo, fakeUserRepo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.staticQrPayload)
        assertEquals("Error de conexión con Firebase", state.errorMessage)
    }
}

package com.kouta.app.feature.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuota.core.data.repository.ConfigRepository
import com.kuota.core.data.repository.PurchaseRepository
import com.kuota.core.data.repository.UserRepository
import com.kuota.core.model.PurchaseStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val userRepository: UserRepository,
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var observePurchaseJob: Job? = null

    init {
        loadInitialConfig()
        calculateQuote()
    }

    private fun loadInitialConfig() {
        viewModelScope.launch {
            val terms = configRepository.getTermsConfig()
            if (terms.terms.isNotEmpty()) {
                val counts = terms.terms.map { it.installmentsCount }
                _uiState.update { it.copy(availableInstallmentOptions = counts) }
            }
        }
    }

    fun onAmountChanged(newInput: String) {
        val clean = newInput.filter { it.isDigit() || it == '.' }
        val soles = clean.toDoubleOrNull() ?: 0.0
        val cents = (soles * 100).toLong()

        _uiState.update {
            it.copy(
                amountInput = clean,
                amountCents = cents,
                errorMessage = null
            )
        }
        calculateQuote()
    }

    fun onInstallmentsSelected(count: Int) {
        _uiState.update {
            it.copy(
                selectedInstallmentsCount = count,
                errorMessage = null
            )
        }
        calculateQuote()
    }

    fun calculateQuote() {
        viewModelScope.launch {
            val cents = _uiState.value.amountCents
            val count = _uiState.value.selectedInstallmentsCount
            if (cents <= 0) return@launch

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            purchaseRepository.quotePurchase(cents, count)
                .onSuccess { quote ->
                    _uiState.update {
                        it.copy(
                            quote = quote,
                            isLoading = false,
                            isEligible = true,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo calcular la cotización"
                        )
                    }
                }
        }
    }

    fun requestPurchase() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            purchaseRepository.createPurchase(
                supplierId = state.supplierId,
                supplierName = state.supplierName,
                totalAmountCents = state.amountCents,
                installmentsCount = state.selectedInstallmentsCount
            ).onSuccess { purchase ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activePurchase = purchase,
                        currentStep = PurchaseFlowStep.STEP_2_WAITING,
                        remainingSeconds = 900 // 15 minutos
                    )
                }
                startCountdownAndObserver(purchase.purchaseId)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al solicitar la compra"
                    )
                }
            }
        }
    }

    private fun startCountdownAndObserver(purchaseId: String) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 &&
                _uiState.value.currentStep == PurchaseFlowStep.STEP_2_WAITING
            ) {
                delay(1000)
                _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }

            if (_uiState.value.remainingSeconds <= 0 &&
                _uiState.value.currentStep == PurchaseFlowStep.STEP_2_WAITING
            ) {
                _uiState.update {
                    it.copy(
                        errorMessage = "La solicitud ha expirado (15 minutos). Por favor cotiza nuevamente."
                    )
                }
            }
        }

        observePurchaseJob?.cancel()
        observePurchaseJob = viewModelScope.launch {
            purchaseRepository.observePurchase(purchaseId).collect { purchase ->
                if (purchase != null) {
                    _uiState.update { it.copy(activePurchase = purchase) }

                    if (purchase.status == "DOWN_PAYMENT_CONFIRMED" &&
                        _uiState.value.currentStep == PurchaseFlowStep.STEP_2_WAITING
                    ) {
                        countdownJob?.cancel()
                        _uiState.update { it.copy(currentStep = PurchaseFlowStep.STEP_3_CONFIRM) }
                    } else if (purchase.status == "CANCELLED" || purchase.status == "EXPIRED") {
                        countdownJob?.cancel()
                        _uiState.update {
                            it.copy(
                                errorMessage = "La compra fue ${purchase.status.lowercase()}.",
                                currentStep = PurchaseFlowStep.STEP_1_DETAILS
                            )
                        }
                    }
                }
            }
        }
    }

    fun confirmPurchase() {
        viewModelScope.launch {
            val purchaseId = _uiState.value.activePurchase?.purchaseId ?: return@launch
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            purchaseRepository.buyerConfirmPurchase(purchaseId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentStep = PurchaseFlowStep.STEP_SUCCESS
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al confirmar la compra"
                        )
                    }
                }
        }
    }

    fun cancelPurchase() {
        viewModelScope.launch {
            val purchaseId = _uiState.value.activePurchase?.purchaseId
            if (purchaseId != null) {
                purchaseRepository.cancelPurchase(purchaseId, "Cancelado por el comprador")
            }
            countdownJob?.cancel()
            observePurchaseJob?.cancel()
            _uiState.update {
                it.copy(
                    currentStep = PurchaseFlowStep.STEP_1_DETAILS,
                    activePurchase = null,
                    errorMessage = null
                )
            }
        }
    }

    fun reset() {
        countdownJob?.cancel()
        observePurchaseJob?.cancel()
        _uiState.update {
            PurchaseUiState(
                amountInput = "1000",
                amountCents = 100_000L,
                selectedInstallmentsCount = 4
            )
        }
        calculateQuote()
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        observePurchaseJob?.cancel()
    }
}

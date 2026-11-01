package com.kouta.app.feature.supplier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuota.core.data.repository.SupplierRepository
import com.kuota.core.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SupplierQrUiState(
    val selectedTab: Int = 0, // 0: Estático, 1: Dinámico
    val staticQrPayload: String? = null,
    val dynamicQrPayload: String? = null,
    val dynamicAmountCents: Long = 45_000L, // S/ 450.00
    val businessName: String = "Comercio MYPE",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SupplierQrViewModel @Inject constructor(
    private val supplierRepository: SupplierRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupplierQrUiState())
    val uiState: StateFlow<SupplierQrUiState> = _uiState.asStateFlow()

    init {
        loadStaticQr()
    }

    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab, errorMessage = null) }
        if (tab == 0 && _uiState.value.staticQrPayload == null) {
            loadStaticQr()
        } else if (tab == 1 && _uiState.value.dynamicQrPayload == null) {
            generateDynamicQr()
        }
    }

    fun onAmountChanged(newCents: Long) {
        _uiState.update { it.copy(dynamicAmountCents = newCents, errorMessage = null) }
        generateDynamicQr()
    }

    fun loadStaticQr() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            supplierRepository.generateSupplierQr(null)
                .onSuccess { qr ->
                    _uiState.update {
                        it.copy(
                            staticQrPayload = qr,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.message ?: "No se pudo generar QR estático"
                        )
                    }
                }
        }
    }

    fun generateDynamicQr() {
        viewModelScope.launch {
            val cents = _uiState.value.dynamicAmountCents
            if (cents <= 0) return@launch

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            supplierRepository.generateSupplierQr(cents)
                .onSuccess { qr ->
                    _uiState.update {
                        it.copy(
                            dynamicQrPayload = qr,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.message ?: "No se pudo generar QR dinámico"
                        )
                    }
                }
        }
    }
}

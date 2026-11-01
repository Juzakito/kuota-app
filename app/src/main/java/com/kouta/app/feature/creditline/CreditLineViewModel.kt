package com.kouta.app.feature.creditline

import androidx.lifecycle.ViewModel
import com.kuota.core.data.repository.UserRepository
import com.kuota.core.model.UserSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class CreditLineViewModel @Inject constructor(
    val userRepository: UserRepository
) : ViewModel() {
    val summary: Flow<UserSummary?> = userRepository.observeUserSummary()
}

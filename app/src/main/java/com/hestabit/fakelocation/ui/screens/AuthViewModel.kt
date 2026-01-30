package com.hestabit.fakelocation.ui.screens

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hestabit.fakelocation.data.repository.AuthRepository
import com.hestabit.fakelocation.data.repository.OtpResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var verificationId: String? = null

    fun sendOtp(phoneNumber: String, activity: Activity) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.sendOtp("+91$phoneNumber", activity).collect { result ->
                when (result) {
                    is OtpResult.CodeSent -> {
                        verificationId = result.verificationId
                        _uiState.value = AuthUiState.OtpSent
                    }
                    is OtpResult.Error -> {
                        _uiState.value = AuthUiState.Error(result.message)
                    }
                    is OtpResult.VerificationCompleted -> {
                        _uiState.value = AuthUiState.Success
                    }
                }
            }
        }
    }

    fun verifyOtp(code: String) {
        val currentVerificationId = verificationId
        if (currentVerificationId == null) {
            _uiState.value = AuthUiState.Error("Verification ID is missing")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.verifyOtp(currentVerificationId, code)
            result.onSuccess {
                _uiState.value = AuthUiState.Success
            }.onFailure {
                _uiState.value = AuthUiState.Error(it.message ?: "Verification failed")
            }
        }
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object OtpSent : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

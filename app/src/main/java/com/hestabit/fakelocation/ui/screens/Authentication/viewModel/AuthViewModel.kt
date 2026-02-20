package com.hestabit.fakelocation.ui.screens.Authentication.viewModel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthProvider
import com.hestabit.fakelocation.data.local.DataStoreManager
import com.hestabit.fakelocation.data.model.ParticleSpec
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
    private val dataStoreManager: DataStoreManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    fun resetResendToken(){
        verificationId = null
        resendToken = null
    }

    val authParticleSpecs = List(6) { i ->
        ParticleSpec(
            xFrac      = ((i * 137 + 23) % 100) / 100f,
            yFrac      = ((i * 97  + 41) % 100) / 100f,
            durationMs = 3000 + (i * 400 % 2000),
            delayMs    = (i * 300 % 2000)
        )
    }

    fun sendOtp(phoneNumber: String, activity: Activity) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.sendOtp(phoneNumber, activity, resendToken).collect { result ->
                when (result) {
                    is OtpResult.CodeSent -> {
                        verificationId = result.verificationId
                        resendToken = result.token
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
                completeAuthentication()
                _uiState.value = AuthUiState.Success
            }.onFailure {
                _uiState.value = AuthUiState.Error(it.message ?: "Verification failed")
            }
        }
    }

    fun completeAuthentication() {
        viewModelScope.launch {
            dataStoreManager.setAuthCompleted(true)
        }
    }

    fun resetToIdle() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object OtpSent : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

package com.hestabit.fakelocation.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<PermissionsUiState>(PermissionsUiState.Idle)
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    fun checkPermissions() {
        if (hasLocationPermissions()) {
            _uiState.value = PermissionsUiState.Granted
        } else {
            _uiState.value = PermissionsUiState.NotGranted
        }
    }

    fun hasLocationPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        // Background location optional initially but good for mock location stability
        // For simplicity, we enforce fine and coarse first.
        
        return fineLocation && coarseLocation
    }
}

sealed class PermissionsUiState {
    object Idle : PermissionsUiState()
    object NotGranted : PermissionsUiState()
    object Granted : PermissionsUiState()
}

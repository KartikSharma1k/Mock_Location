package com.hestabit.fakelocation.ui.screens

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.hestabit.fakelocation.data.repository.MockLocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val locationRepository: MockLocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun onMapClick(latLng: LatLng) {
        val currentState = _uiState.value
        if (currentState.startLocation == null) {
            _uiState.value = currentState.copy(startLocation = latLng)
        } else if (currentState.destinationLocation == null) {
            val start = currentState.startLocation
            val route = listOf(start, latLng) // Linear line for now
            // TODO: In real app, fetch polyline from Directions API
            _uiState.value = currentState.copy(destinationLocation = latLng, routePoints = route)
        } else {
            // Reset if both are set and user clicks again, or handle as new start?
            // For now, let's just clear both and set start to new click for simplicity
            _uiState.value = currentState.copy(startLocation = latLng, destinationLocation = null, routePoints = emptyList())
        }
    }

    fun clearMap() {
        _uiState.value = DashboardUiState()
        stopMocking()
    }

    fun startMocking() {
        val currentState = _uiState.value
        if (currentState.routePoints.isNotEmpty()) {
            locationRepository.startMocking(currentState.routePoints)
            _uiState.value = currentState.copy(isMocking = true)
        }
    }

    fun stopMocking() {
        locationRepository.stopMocking()
        _uiState.value = _uiState.value.copy(isMocking = false)
    }
}

data class DashboardUiState(
    val startLocation: LatLng? = null,
    val destinationLocation: LatLng? = null,
    val routePoints: List<LatLng> = emptyList(),
    val isMocking: Boolean = false,
    val speed: MockSpeed = MockSpeed.MEDIUM
)

enum class MockSpeed {
    SLOW, MEDIUM, FAST
}

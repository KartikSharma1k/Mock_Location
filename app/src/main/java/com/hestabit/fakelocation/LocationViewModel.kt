package com.hestabit.fakelocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    val mockController: MockLocationController
) : ViewModel() {

    private val _locationState = MutableStateFlow("No location")
    val locationState: StateFlow<String> = _locationState.asStateFlow()

    private val _routeProgress = MutableStateFlow("No route active")
    val routeProgress: StateFlow<String> = _routeProgress.asStateFlow()

    fun enableMocking() {
        viewModelScope.launch {
            try {
                val success = mockController.enableMockMode(true)
                _locationState.value = if (success) "Mock mode enabled" else "Failed to enable mock mode"
            } catch (e: Exception) {
                _locationState.value = "Error: ${e.message}"
            }
        }
    }

    // Simulate your custom route
    fun simulateYourCustomRoute(startLocation: CustomLocations, endLocation: CustomLocations) {
        mockController.simulateCustomRoute(
            startLocation = startLocation,
            endLocation = endLocation,
            intervalSeconds = 2L,
            speedKmh = 63.0, // 25 km/h city driving
            onLocationUpdate = { point, index, total ->
                _routeProgress.value = "Route: ${index + 1}/$total - Lat: ${String.format("%.6f", point.latitude)}, Lng: ${String.format("%.6f", point.longitude)}"
            },
            onRouteComplete = {
                _routeProgress.value = "Custom route completed!"
                _locationState.value = "Journey finished"
            }
        )

        _locationState.value = "Custom route simulation started"
    }

    fun stopRoute() {
        mockController.stopRouteSimulation()
        _routeProgress.value = "Route stopped"
        _locationState.value = "Route simulation stopped"
    }

    fun setHomeMockLocation(){
        viewModelScope.launch {
            try {
                val success = mockController.setMockLocation(28.68786, 77.28795)
                _locationState.value = if (success) "Mock location set to Home: 28.68786, 77.28795" else "Failed to set mock location"
            } catch (e: Exception) {
                _locationState.value = "Error: ${e.message}"
            }
        }
    }

    fun setSpotMockLocation(){
        viewModelScope.launch {
            try {
                val success = mockController.setMockLocation(28.627997, 77.381426)
                _locationState.value = if (success) "Mock location set to Spot: 28.627997, 77.381426" else "Failed to set mock location"
            } catch (e: Exception) {
                _locationState.value = "Error: ${e.message}"
            }
        }
    }

    fun setOfficeMockLocation(){
        viewModelScope.launch {
            try {
                val success = mockController.setMockLocation(28.63101, 77.38417)
                _locationState.value = if (success) "Mock location set to Office: 28.63101, 77.38417" else "Failed to set mock location"
            } catch (e: Exception) {
                _locationState.value = "Error: ${e.message}"
            }
        }
    }

    fun setArindamLocation(){
        viewModelScope.launch {
            try {
                val success = mockController.setMockLocation(28.615863, 77.429501)
                _locationState.value = if (success) "Mock location set to Arindam: 28.615863, 77.429501" else "Failed to set mock location"
            } catch (e: Exception) {
                _locationState.value = "Error: ${e.message}"
            }
        }
    }

    fun setNishantLocation(){
        viewModelScope.launch {
            try {
                val success = mockController.setMockLocation(28.6338641832752, 77.43084920262324)
                _locationState.value = if (success) "Mock location set to Nishant: 28.6338641832752, 77.43084920262324" else "Failed to set mock location"
            } catch (e: Exception) {
                _locationState.value = "Error: ${e.message}"
            }
        }
    }

    fun setGymLocation(){
        viewModelScope.launch {
            try {
                val success = mockController.setMockLocation(28.63087229646094, 77.43443316990961)
                _locationState.value = if (success) "Mock location set to Gym: 28.63087229646094, 77.43443316990961" else "Failed to set mock location"
            } catch (e: Exception) {
                _locationState.value = "Error: ${e.message}"
            }
        }
    }

    fun setRimjhimLocation(){
        viewModelScope.launch {
            try {
                val success = mockController.setMockLocation(28.618670618203534, 77.41855650018451)
                _locationState.value = if (success) "Mock location set to Rimjhim: 28.618670618203534, 77.41855650018451" else "Failed to set mock location"
            } catch (e: Exception) {
                _locationState.value = "Error: ${e.message}"
            }
        }
    }

    fun disableMocking() {
        viewModelScope.launch {
            try {
                mockController.stopRouteSimulation()
                val success = mockController.enableMockMode(false)
                _locationState.value = if (success) "Mock mode disabled" else "Failed to disable mock mode"
                _routeProgress.value = "No route active"
            } catch (e: Exception) {
                _locationState.value = "Error: ${e.message}"
            }
        }
        forceLocationRefresh()
    }

    fun forceLocationRefresh() {
        viewModelScope.launch {
            try {
                _locationState.value = "Force refreshing location system..."

                val success = mockController.forceLocationRefresh()

                if (success) {
                    _locationState.value = "Location system refreshed successfully"
                } else {
                    _locationState.value = "Failed to refresh location system"
                }

            } catch (e: Exception) {
                _locationState.value = "Refresh error: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mockController.stopRouteSimulation()
    }


}


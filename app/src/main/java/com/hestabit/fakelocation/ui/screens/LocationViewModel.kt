package com.hestabit.fakelocation.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.hestabit.fakelocation.MockLocationController
import com.hestabit.fakelocation.data.enums.MapAction
import com.hestabit.fakelocation.data.model.SavedLocation
import com.hestabit.fakelocation.data.remote.DirectionsApiService
import com.hestabit.fakelocation.data.repository.SavedLocationRepository
import com.hestabit.fakelocation.ui.MainViewModel.Companion.isLoading
import com.hestabit.fakelocation.utils.PolylineUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val mockController: MockLocationController,
    private val savedLocationRepository: SavedLocationRepository,
    private val directionsApiService: DirectionsApiService
) : ViewModel() {

    private val _locationState = MutableStateFlow("No location")
    val locationState: StateFlow<String> = _locationState.asStateFlow()

    private val _routeProgress = MutableStateFlow("No route active")
    val routeProgress: StateFlow<String> = _routeProgress.asStateFlow()

    private val _savedLocations = MutableStateFlow<List<SavedLocation>>(emptyList())
    val savedLocations: StateFlow<List<SavedLocation>> = _savedLocations.asStateFlow()

    var selectedMapAction = MapAction.FREE_LOOK
        private set

    private val _isMocking = MutableStateFlow(false)
    val isMocking = _isMocking.asStateFlow()

    private val _fullMapUiState = MutableStateFlow<MapUIState>(MapUIState())
    val fullMapUiState = _fullMapUiState.asStateFlow()

    private val _pinnedLocation = MutableStateFlow<LatLng?>(null)
    val pinnedLocation = _pinnedLocation.asStateFlow()

    init {
        observeSavedLocations()
    }

    private fun observeSavedLocations() {
        viewModelScope.launch {
            savedLocationRepository.getSavedLocations().collect { locations ->
                _savedLocations.value = locations
            }
        }
    }

    fun updateUIState(newState: MapUIState){
        viewModelScope.launch {
            _fullMapUiState.emit(newState)
        }
    }

    fun setMockLocation(latitude: Double?, longitude: Double?){
        viewModelScope.launch {
            enableMocking()
            val success = mockController.setMockLocation(latitude ?: 0.0, longitude ?: 0.0)
            _isMocking.emit(success)
            _locationState.value = if (success) "Mock location set" else "Failed to set mock location"
        }
    }

    fun saveLocation(name: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            isLoading.emit(true)
            updateUIState(_fullMapUiState.value.copy(showSaveDialog = !_fullMapUiState.value.showSaveDialog))
            val result = savedLocationRepository.saveLocation(name, latitude, longitude)
            if (result.isSuccess) {
                _locationState.value = "Location '$name' saved!"
                isLoading.emit(false)
            } else {
                Log.d("LocationViewModel", "saveLocation: ${result.exceptionOrNull()?.message}")
                _locationState.value = "Failed to save location: ${result.exceptionOrNull()?.message}"
                updateUIState(_fullMapUiState.value.copy(showSaveDialog = !_fullMapUiState.value.showSaveDialog, locationName = ""))
                isLoading.emit(false)
            }
        }
    }

    fun setPinnedLocation(location: LatLng?){
        _pinnedLocation.value = location
    }
    
    fun deleteLocation(id: String) {
        viewModelScope.launch {
             savedLocationRepository.deleteLocation(id)
        }
    }

    fun toggleFavourite(id: String, favourite: Boolean) {
        viewModelScope.launch {
            savedLocationRepository.toggleFavourite(id, favourite)
        }
    }

    fun setMapAction(action: MapAction){
        selectedMapAction = action
    }

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

    fun disableMocking() {
        viewModelScope.launch {
            try {
                val success = mockController.stopRouteSimulationAndDisable()
                _locationState.value = if (success) "Mock mode disabled" else "Failed to disable mock mode"
                _routeProgress.value = "No route active"
                _isMocking.emit(false)
            } catch (e: Exception) {
                _locationState.value = "Error: ${e.message}"
            }
        }
        forceLocationRefresh()
    }

    fun forceLocationRefresh() {
        viewModelScope.launch {
            try {
                _locationState.value = "Force refreshing location..."
                val success = mockController.forceLocationRefresh()
                _locationState.value = if (success) "Refreshed" else "Refresh failed"
                _isMocking.emit(!success)
            } catch (e: Exception) {
                _locationState.value = "Refresh error: ${e.message}"
            }
        }
    }

    fun startRoute(start: LatLng, end: LatLng, apiKey: String, speedKmh: Double = 60.0) {
        viewModelScope.launch {
            _locationState.value = "Fetching route..."
            try {
                val origin = "${start.latitude},${start.longitude}"
                val dest = "${end.latitude},${end.longitude}"
                val response = directionsApiService.getDirections(origin, dest, apiKey = apiKey)

                Log.d("Route map data", "startRoute: $response")

                if (response.routes.isNotEmpty()) {
                    val points = PolylineUtils.decode(response.routes[0].overview_polyline.points)

                    _locationState.value = "Route fetched. Points: ${points.size}. Starting mock..."

                    val started = mockController.startRouteSimulation(points, speedKmh)
                    if (started) {
                        _isMocking.emit(true)
                        _routeProgress.value = "Route started. Points: ${points.size}"
                    } else {
                        _locationState.value = "Failed to start route simulation"
                    }

                } else {
                    _locationState.value = "No route found"
                }
            } catch (e: Exception) {
                _locationState.value = "Route error: ${e.message}"
            }
        }
    }
    
    fun stopRoute() {
        viewModelScope.launch {
            try {
                val disabled = mockController.stopRouteSimulationAndDisable()
                _routeProgress.value = if (disabled) "Route stopped" else "Failed to stop route"
                _isMocking.emit(false)
            } catch (e: Exception) {
                _locationState.value = "Error stopping route: ${e.message}"
            }
        }
    }

    // New helpers to be called by UI when the service broadcasts events (so UI can reflect state after app restart)
    fun onSimulationStarted(pointsCount: Int) {
        viewModelScope.launch {
            _isMocking.emit(true)
            _routeProgress.value = "Route started. Points: $pointsCount"
            _locationState.value = "Simulation running"
        }
    }

    fun onSimulationStopped() {
        viewModelScope.launch {
            _isMocking.emit(false)
            _routeProgress.value = "No route active"
            _locationState.value = "Simulation stopped"
        }
    }

    override fun onCleared() {
        super.onCleared()
        mockController.stopRouteSimulation()
    }

}

sealed class SaveLocationState(){
    object Idle: SaveLocationState()
    object Loading: SaveLocationState()
    object Success: SaveLocationState()
    object Error: SaveLocationState()
}


data class MapUIState(val locationName: String = "", val selectedLocation: LatLng? = null, val showSaveDialog: Boolean = false)
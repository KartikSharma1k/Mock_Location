package com.hestabit.fakelocation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun FullScreenMapScreen(
    viewModel: LocationViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val startPos = LatLng(28.6139, 77.2090)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPos, 12f)
    }

    val uiState by viewModel.fullMapUiState.collectAsState()
    val savedMarkers by viewModel.savedLocations.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(myLocationButtonEnabled = true, zoomControlsEnabled = true),
            onMapClick = { latLng ->
                viewModel.updateUIState(uiState.copy(selectedLocation = latLng))
            }
        ) {
            uiState.selectedLocation?.let {
                Marker(state = MarkerState(position = it), title = uiState.locationName.ifEmpty { "Selected" })
            }

            savedMarkers.forEach { location ->
                Marker(state = MarkerState(position = LatLng(location.latitude, location.longitude)), title = location.name, snippet = "Saved Location", alpha = 0.5f)
            }

        }

        Row(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(32.dp)) {

            IconButton(onClick = {
                viewModel.updateUIState(uiState.copy(selectedLocation = null))
            }, colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black), enabled = uiState.selectedLocation != null) {
                Icon(Icons.Default.Clear, contentDescription = "",
                    tint = Color.White
                )
            }

            Button(
                onClick = {
                    uiState.selectedLocation?.let { viewModel.updateUIState(uiState.copy(showSaveDialog = !uiState.showSaveDialog, locationName = ""))}
                },
                enabled = uiState.selectedLocation != null,
            ) {
                Text("Confirm Selection")
            }
        }
    }

    if (uiState.showSaveDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.updateUIState(uiState.copy(showSaveDialog = !uiState.showSaveDialog))},
            title = { Text("Save Location") },
            text = {
                Column {
                    Text("Enter a name for this location:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.locationName,
                        onValueChange = { viewModel.updateUIState(uiState.copy(locationName = it))},
                        label = { Text("Name") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        uiState.selectedLocation?.let {
                            viewModel.saveLocation(uiState.locationName, it.latitude, it.longitude)
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.updateUIState(uiState.copy(showSaveDialog = !uiState.showSaveDialog)) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

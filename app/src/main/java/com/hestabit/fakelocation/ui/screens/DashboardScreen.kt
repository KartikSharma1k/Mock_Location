package com.hestabit.fakelocation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.hestabit.fakelocation.data.enums.MapAction
import com.hestabit.fakelocation.data.model.SavedLocation

@Composable
fun DashboardScreen(
    viewModel: LocationViewModel = hiltViewModel(),
    onExpandMap: (MapAction) -> Unit,
    selectedLocationResult: LatLng? = null
) {
    val locationState by viewModel.locationState.collectAsState()
    val savedLocations by viewModel.savedLocations.collectAsState()
    val routeProgress by viewModel.routeProgress.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Handle result from FullScreenMap
    // We can decide where to put it (Start or End). For now, let's auto-fill Start if empty, or End if Start exists.
    // Or just putting it in 'selectedStart' if we were 'selecting start'. 
    // To be simple: If result comes back, we pin it? Or set as Start?
    // Let's set it as "Pinned Location" so user can decide what to do (Save or use as route point)
    val currentSelectedResult by rememberUpdatedState(
        selectedLocationResult
    )
    var isPinMode by remember { mutableStateOf(false) }
    var pinnedLocation by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(currentSelectedResult) {
        currentSelectedResult?.let {
            pinnedLocation = it
        }
    }

    // Map State
    val startPos = LatLng(28.6139, 77.2090) // Default Delhi
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPos, 12f)
    }

    // Interaction State
    // var isPinMode by remember { mutableStateOf(false) } // Removed duplicate
    // var pinnedLocation by remember { mutableStateOf<LatLng?>(null) } // Removed duplicate
    var showSaveDialog by remember { mutableStateOf(false) }
    var selectedStart by remember { mutableStateOf<LatLng?>(null) }
    var selectedEnd by remember { mutableStateOf<LatLng?>(null) }

    // Dialog State
    var locationName by remember { mutableStateOf("") }

    Box(modifier = Modifier.padding(16.dp)) {

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                    zoomControlsEnabled = false,
                    zoomGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    scrollGesturesEnabled = false,
                    scrollGesturesEnabledDuringRotateOrZoom = false,
                ),
                onMapClick = { latLng ->
                    onExpandMap(MapAction.FREE_LOOK)
                }
            ) {
                pinnedLocation?.let {
                    Marker(state = MarkerState(position = it), title = "Pinned Location")
                }
                /*// Markers
            pinnedLocation?.let {
                Marker(state = MarkerState(position = it), title = "Pinned Location", icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_VIOLET))
            }
            selectedStart?.let {
                Marker(state = MarkerState(position = it), title = "Start", icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN))
            }
            selectedEnd?.let {
                Marker(state = MarkerState(position = it), title = "End", icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED))
            }*/
            }

            /*                //Top Status Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(
                    alpha = 0.9f
                )
            )
        ) {
            Text(
                text = if (routeProgress != "No route active") routeProgress else locationState,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                .padding(16.dp)
        ) {
            // Saved Locations List
            if (savedLocations.isNotEmpty()) {
                Text(
                    "Saved Locations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(savedLocations) { location ->
                        SavedLocationItem(location) {
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            location.latitude,
                                            location.longitude
                                        ), 15f
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    selectedStart = null
                    selectedEnd = null
                    pinnedLocation = null
                    viewModel.stopRoute()
                }) {
                    Text("Clear")
                }

                Button(
                    onClick = {
                        if (selectedStart != null && selectedEnd != null) {
                            // TODO: API KEY from usage or config
                            val apiKey = "YOUR_API_KEY"
                            viewModel.startRoute(selectedStart!!, selectedEnd!!, apiKey)
                        }
                    },
                    enabled = selectedStart != null && selectedEnd != null
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mock Route")
                }

                Button(onClick = { viewModel.stopRoute() }) {
                    Text("Stop Mock")
                }

                Button(onClick = onExpandMap) {
                    Text("Expand")
                }
            }
        }

        // Add Location FAB
        FloatingActionButton(
            onClick = { isPinMode = !isPinMode },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(
                    end = 16.dp,
                    bottom = 250.dp
                ), // Adjust position above bottom sheet
            containerColor = if (isPinMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        ) {
            Icon(
                if (isPinMode) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Add Location"
            )
        }

        if (isPinMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text("Tap map to pin location", color = Color.White)
            }
        }*/


            if (savedLocations.isNullOrEmpty()) {
                Text("No saved locations found")
            } else {
                LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(savedLocations) { location ->
                        Button(onClick = {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(location.latitude, location.longitude), 12f)
                            pinnedLocation = LatLng(location.latitude, location.longitude)
                        }) {
                            Text(location.name)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(modifier = Modifier.fillMaxWidth().height(60.dp),onClick = {}) {
                Text("Start Mocking")
            }
        }
        FloatingActionButton(
            onClick = {
                onExpandMap(MapAction.ADD_NEW_LOCATION)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Location")
        }

        /*    // Save Dialog
            if (showSaveDialog) {
                AlertDialog(
                    onDismissRequest = { showSaveDialog = false },
                    title = { Text("Save Location") },
                    text = {
                        Column {
                            Text("Enter a name for this location:")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = locationName,
                                onValueChange = { locationName = it },
                                label = { Text("Name") }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                pinnedLocation?.let {
                                    viewModel.saveLocation(locationName, it.latitude, it.longitude)
                                }
                                showSaveDialog = false
                                locationName = ""
                                pinnedLocation = null // Clear pin after save? Or keep it?
                            }
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSaveDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }*/

    }

    @Composable
    fun SavedLocationItem(location: SavedLocation, onClick: () -> Unit) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .width(120.dp)
                .height(80.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(location.name, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
        }
    }
}

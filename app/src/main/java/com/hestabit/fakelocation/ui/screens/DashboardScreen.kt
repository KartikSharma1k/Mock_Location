package com.hestabit.fakelocation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
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
import android.location.Geocoder
import androidx.compose.material3.TextField
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory

// New Places imports
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.model.Place
import com.hestabit.fakelocation.data.model.FabOption
import com.hestabit.fakelocation.ui.widgets.DashboardBottomSheet
import com.hestabit.fakelocation.ui.widgets.ExpandableFab
import com.hestabit.fakelocation.ui.widgets.MapSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnprotectedBroadcastReceiver")
@Composable
fun DashboardScreen(
    viewModel: LocationViewModel = hiltViewModel(),
    onExpandMap: (MapAction) -> Unit,
    selectedLocationResult: LatLng? = null
) {
    val locationState by viewModel.locationState.collectAsState()
    val savedLocations by viewModel.savedLocations.collectAsState()
    val routeProgress by viewModel.routeProgress.collectAsState()

    val isMocking by viewModel.isMocking.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Initialize Places client (reads API key from manifest meta-data)
    val placesClient: PlacesClient? = remember {
        try {
            val ai: ApplicationInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            val key = ai.metaData?.getString("com.google.android.geo.API_KEY")
            if (!key.isNullOrEmpty() && !Places.isInitialized()) {
                Places.initialize(context.applicationContext, key)
            }
            Places.createClient(context)
        } catch (e: Exception) {
            null
        }
    }

    // Read API key from manifest meta-data safely
    val apiKey = remember {
        try {
            val ai: ApplicationInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            ai.metaData?.getString("com.google.android.geo.API_KEY")
        } catch (e: Exception) {
            null
        }
    }

    // Handle result from FullScreenMap
    val currentSelectedResult by rememberUpdatedState(
        selectedLocationResult
    )
    var isPinMode by remember { mutableStateOf(false) }
    val pinnedLocation by viewModel.pinnedLocation.collectAsState()

    LaunchedEffect(currentSelectedResult) {
        currentSelectedResult?.let {
            viewModel.setPinnedLocation(it)
        }
    }

    // Map State
    val startPos = LatLng(28.6139, 77.2090) // Default Delhi
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPos, 12f)
    }

    // Interaction State
    var showSaveDialog by remember { mutableStateOf(false) }

    // Dialog State
    var locationName by remember { mutableStateOf("") }

    var onSearchEnabled by remember { mutableStateOf(false) }

    var showBottomSheet by remember { mutableStateOf(false) }

    var bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    Scaffold() { _ ->

        Box(modifier = Modifier) {

            GoogleMap(
                modifier = Modifier
                    .fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                ),
                onMapClick = { loc ->
                    viewModel.setPinnedLocation(loc)
                }
            ) {
                pinnedLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Pinned Location",
                        onClick = {
                            true
                        }
                    )
                }

                savedLocations.let { locations ->
                    locations.forEach { location ->
                        Marker(
                            alpha = 0.5f,
                            state = MarkerState(
                                position = LatLng(
                                    location.latitude,
                                    location.longitude
                                )
                            ),
                            title = "Pinned Location",
                            onClick = {
                                viewModel.setPinnedLocation(
                                    LatLng(
                                        location.latitude,
                                        location.longitude
                                    )
                                )
                                true
                            }
                        )
                    }

                }
            }

            Column(modifier = Modifier.align(alignment = Alignment.BottomCenter)) {
                // Bottom-left expandable search button (collapses to a circular FAB and expands to full available width)

                if (!onSearchEnabled) {
                    ExpandableFab(
                        modifier = Modifier.fillMaxWidth(),
                        fabIcon = Icons.Default.Settings,
                        items = listOf(
                            FabOption(Icons.Default.Search, "Search", { onSearchEnabled = true }),
                            FabOption(Icons.Default.Menu, "Menu", {
                                showBottomSheet = true
                            }),
                            FabOption(Icons.Default.AccountCircle, "Account", {}),
                            FabOption(
                                Icons.Default.Close,
                                "Clear Selection",
                                {
                                    viewModel.setPinnedLocation(null)
                                })
                        )
                    )
                }

                if (onSearchEnabled) {
                    MapSearchBar(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .padding(start = 16.dp, bottom = 24.dp),
                        placesClient = placesClient,
                        onPredictionSelected = { prediction ->
                            // When user selects a prediction, fetch place details and move camera
                            val placeId = prediction.placeId
                            val fields = listOf(Place.Field.LAT_LNG)
                            val request = FetchPlaceRequest.newInstance(placeId, fields)
                            try {
                                placesClient?.fetchPlace(request)
                                    ?.addOnSuccessListener { response ->
                                        val latLng = response.place.latLng
                                        if (latLng != null) {
                                            val found = LatLng(latLng.latitude, latLng.longitude)
                                            viewModel.setPinnedLocation(found)
                                            cameraPositionState.position =
                                                CameraPosition.fromLatLngZoom(found, 15f)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Place details not available",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    ?.addOnFailureListener { ex ->
                                        Toast.makeText(
                                            context,
                                            "Failed to get place: ${ex.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Failed to get place: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onSearch = { query ->
                            if (query.isBlank()) return@MapSearchBar
                            // fallback to Geocoder if Places not available
                            scope.launch {
                                try {
                                    val geocoder = Geocoder(context)
                                    val results = withContext(Dispatchers.IO) {
                                        geocoder.getFromLocationName(query, 1)
                                    }
                                    if (!results.isNullOrEmpty()) {
                                        val addr = results[0]
                                        val found = LatLng(addr.latitude, addr.longitude)
                                        viewModel.setPinnedLocation(found)
                                        cameraPositionState.position =
                                            CameraPosition.fromLatLngZoom(found, 15f)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Location not found",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Search failed: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        onClose = {
                            onSearchEnabled = false
                        }
                    )
                }

                if (pinnedLocation != null)
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 50.dp)
                            .padding(horizontal = 20.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        ),
                        onClick = {
                            if (isMocking) viewModel.disableMocking() else viewModel.setMockLocation(
                                pinnedLocation?.latitude,
                                pinnedLocation?.longitude
                            )
                        }) {
                        Text(if (isMocking) "Stop Mocking" else "Start Mocking")
                    }

            }

            if (showBottomSheet)
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = bottomSheetState
                ) {
                    DashboardBottomSheet(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        viewModel = viewModel,
                        onClose = { newLoc ->
                            showBottomSheet = false
                            if (newLoc != null) {
                                scope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLng(newLoc),
                                        durationMs = 200
                                    )
                                }
                            }
                        },
                        onNavigate = {onExpandMap(MapAction.ADD_NEW_LOCATION)})
                }
        }
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
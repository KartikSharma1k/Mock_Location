package com.hestabit.fakelocation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Scaffold
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
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.Alignment

// New Places imports
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.model.Place

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

    // NOTE: removed the runtime BroadcastReceiver registration (was used to listen to MockLocationService events)
    // This was causing a lint error about missing RECEIVER_NOT_EXPORTED on older API levels; re-add with proper
    // modern registration if you need the feature. For search/autocomplete this is not required.

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
    var showSaveDialog by remember { mutableStateOf(false) }

    // Dialog State
    var locationName by remember { mutableStateOf("") }


    Scaffold(topBar = {
        TopAppBar(title = { Text("Mock Location") }, actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Settings, contentDescription = "Setting button")
            }
        })
    }) { paddingValues ->

        Box(modifier = Modifier.padding(paddingValues)) {

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp)),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = true,
                    ),
                    onMapClick = { loc ->
                        pinnedLocation = loc
                    }
                ) {
                    pinnedLocation?.let {
                        Marker(state = MarkerState(position = it), title = "Pinned Location", onClick = {
                            true
                        })
                    }
                }
            }

            // Bottom-left expandable search button (collapses to a circular FAB and expands to full available width)
            MapSearchBar(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .align(Alignment.BottomStart)
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
                                val latLng = response.place?.latLng
                                if (latLng != null) {
                                    val found = LatLng(latLng.latitude, latLng.longitude)
                                    pinnedLocation = found
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(found, 15f)
                                } else {
                                    Toast.makeText(context, "Place details not available", Toast.LENGTH_SHORT).show()
                                }
                            }
                            ?.addOnFailureListener { ex ->
                                Toast.makeText(context, "Failed to get place: ${ex.message}", Toast.LENGTH_SHORT).show()
                            }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to get place: ${e.message}", Toast.LENGTH_SHORT).show()
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
                                pinnedLocation = found
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(found, 15f)
                            } else {
                                Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Search failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

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

// --- New helper types and composables for expandable FAB ---

data class FabOption(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun ExpandableFab(
    fabIcon: ImageVector,
    items: List<FabOption>,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.BottomEnd
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 45f else 0f)

    Box(modifier = modifier.fillMaxSize()) {
        // Fullscreen transparent overlay to collapse when tapping outside - placed before FABs
        if (expanded) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { expanded = false }
            )
        }

        Column(
            modifier = Modifier
                .wrapContentSize()
                .align(alignment)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Action items (appear above main FAB)
            items.forEachIndexed { index, item ->
                AnimatedVisibility(
                    visible = expanded,
                    enter = slideInVertically { (it * (items.size - index)) } + fadeIn(),
                    exit = slideOutVertically { (it * (items.size - index)) } + fadeOut()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            tonalElevation = 2.dp,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = item.label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                        FloatingActionButton(
                            shape = CircleShape,
                            onClick = {
                                item.onClick()
                                expanded = false
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main FAB (rotates when expanded)
            FloatingActionButton(
                shape = CircleShape,
                onClick = { expanded = !expanded }
            ) {
                Icon(
                    fabIcon,
                    contentDescription = "Open actions",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}

// New composable: MapSearchBar with Places suggestions
@Composable
fun MapSearchBar(
    modifier: Modifier = Modifier,
    hint: String = "Search location",
    placesClient: PlacesClient? = null,
    onPredictionSelected: (AutocompletePrediction) -> Unit = {},
    onSearch: (String) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var showDropdown by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val token = remember { AutocompleteSessionToken.newInstance() }

    // Simpler, robust expand/collapse using animateContentSize
    val collapsedSize = 56.dp

    Box(modifier = modifier, contentAlignment = Alignment.BottomStart) {
        // overlay that collapses when tapping outside
        if (expanded) {
            Box(modifier = Modifier
                .matchParentSize()
                .clickable { expanded = false; focusManager.clearFocus() })
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(if (expanded) 24.dp else 28.dp),
            modifier = Modifier
                .animateContentSize()
                .then(if (expanded) Modifier.fillMaxWidth() else Modifier.size(collapsedSize))
                .zIndex(1f)
                .clip(RoundedCornerShape(if (expanded) 24.dp else 28.dp))
        ) {
            if (!expanded) {
                // collapsed circular button
                val _ctx = LocalContext.current

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(collapsedSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { expanded = true; },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Open search",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(collapsedSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Open Menu",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                    TextField(
                        value = query,
                        onValueChange = { new ->
                            query = new
                            if (new.isNotBlank() && placesClient != null) {
                                val request = FindAutocompletePredictionsRequest.builder()
                                    .setSessionToken(token)
                                    .setQuery(new)
                                    .build()
                                try {
                                    placesClient.findAutocompletePredictions(request)
                                        .addOnSuccessListener { resp ->
                                            suggestions = resp.autocompletePredictions
                                            showDropdown = suggestions.isNotEmpty()
                                        }
                                        .addOnFailureListener {
                                            suggestions = emptyList()
                                            showDropdown = false
                                        }
                                } catch (e: Exception) {
                                    suggestions = emptyList()
                                    showDropdown = false
                                }
                            } else {
                                suggestions = emptyList()
                                showDropdown = false
                            }
                        },
                        placeholder = { Text(hint) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = {
                                if (query.isNotBlank()) onSearch(query)
                                focusManager.clearFocus()
                                expanded = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }
            }
        }

        if (expanded && showDropdown) {
            Column(
                modifier = Modifier
                    .offset(y = collapsedSize + 8.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            ) {
                suggestions.forEach { prediction ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                query = prediction.getPrimaryText(null).toString()
                                showDropdown = false
                                suggestions = emptyList()
                                onPredictionSelected(prediction)
                                focusManager.clearFocus()
                                expanded = false
                            }
                            .padding(12.dp)
                    ) {
                        Text(prediction.getPrimaryText(null).toString(), style = MaterialTheme.typography.bodyLarge)
                        val second = prediction.getSecondaryText(null).toString()
                        if (second.isNotBlank()) Text(second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        LaunchedEffect(expanded) { if (expanded) focusRequester.requestFocus() }
    }
}

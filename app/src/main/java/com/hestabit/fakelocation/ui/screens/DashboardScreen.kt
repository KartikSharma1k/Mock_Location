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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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
import com.hestabit.fakelocation.service.MockLocationService

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

    // Register BroadcastReceiver to listen for MockLocationService start/stop events and forward to ViewModel
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.action?.let { action ->
                    when (action) {
                        MockLocationService.ACTION_SIMULATION_STARTED -> {
                            val count = intent.getIntExtra(MockLocationService.EXTRA_POINTS_COUNT, 0)
                            viewModel.onSimulationStarted(count)
                        }
                        MockLocationService.ACTION_SIMULATION_STOPPED -> {
                            viewModel.onSimulationStopped()
                        }
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(MockLocationService.ACTION_SIMULATION_STARTED)
            addAction(MockLocationService.ACTION_SIMULATION_STOPPED)
        }
        // Register as not exported to avoid unprotected broadcast exposure
        try {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } catch (e: Exception) {
            // Fallback to legacy registration if flag not supported on older platforms
            try { context.registerReceiver(receiver, filter) } catch (_: Exception) {}
        }
        onDispose {
            try { context.unregisterReceiver(receiver) } catch (e: Exception) {}
        }
    }

    // Read API key from manifest meta-data safely
    val apiKey = remember {
        try {
            val ai: ApplicationInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
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
                onMapClick = { _ ->
                    onExpandMap(MapAction.FREE_LOOK)
                }
            ) {
                pinnedLocation?.let {
                    Marker(state = MarkerState(position = it), title = "Pinned Location")
                }
            }

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

            Button(modifier = Modifier.fillMaxWidth().height(60.dp),onClick = {
                if (!isMocking) viewModel.setMockLocation(pinnedLocation?.latitude, pinnedLocation?.longitude)
                else viewModel.disableMocking()
            }) {
                Text(if (isMocking) "Stop Mocking" else "Start Mocking")
            }
        }

        // Replace single FAB with an Expandable FAB that rotates and shows labeled actions.
        ExpandableFab(
            fabIcon = Icons.Default.Add,
            items = listOf(
                FabOption(
                    icon = Icons.Default.Add,
                    label = "Add Location",
                    onClick = { onExpandMap(MapAction.ADD_NEW_LOCATION) }
                ),
                FabOption(
                    icon = Icons.Default.LocationOn,
                    label = "Add Route",
                    onClick = {
                        // Guard: we need apiKey and a pinned location
                        if (apiKey == null) {
                            // show some UI or log
                            return@FabOption
                        }
                        val start = pinnedLocation ?: run {
                            // no pinned location; log and return
                            return@FabOption
                        }

                        viewModel.startRoute(start, LatLng(28.6139, 77.2090), apiKey)
                    }
                )
            ),
            modifier = Modifier.fillMaxSize(),
            alignment = Alignment.BottomEnd
        )

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
            items.forEachIndexed { index , item ->
                AnimatedVisibility(
                    visible = expanded,
                    enter = slideInVertically { (it * (items.size - index))} + fadeIn(),
                    exit = slideOutVertically { (it * (items.size - index))} + fadeOut()
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

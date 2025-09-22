package com.hestabit.fakelocation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hestabit.fakelocation.ui.theme.FakeLocationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FakeLocationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MockLocationScreen()
                }
            }
        }
    }
}

//@Composable
//fun MockLocationScreen(viewModel: LocationViewModel = hiltViewModel()) {
//    val locationState by viewModel.locationState.collectAsState()
//    val routeProgress by viewModel.routeProgress.collectAsState()
//
//    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
//        Column(
//            modifier = Modifier.padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            Text(
//                text = locationState,
//                modifier = Modifier.padding(8.dp),
//                style = MaterialTheme.typography.bodyMedium
//            )
//
//            Text(
//                text = routeProgress,
//                modifier = Modifier.padding(8.dp),
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.primary
//            )
//
//            Button(onClick = { viewModel.enableMocking() }) {
//                Text("Enable Mock Mode")
//            }
//
//            Button(onClick = {
//                viewModel.setLocation(28.6282046, 77.3821793)
//            }) {
//                Text("Set Single Location")
//            }
//
//            Button(onClick = {
//                // Example: Simulate route from Delhi to Gurgaon
//                viewModel.simulateRouteToDestination(
//                    startLat = 28.6139, startLng = 77.2090, // Delhi
//                    endLat = 28.4595, endLng = 77.0266,   // Gurgaon
//                    intervalSeconds = 2L
//                )
//            }) {
//                Text("Simulate Realistic Route (Delhi to Gurgaon)")
//            }
//
//            Button(onClick = {
//                // Example: Create custom route with realistic speed
//                val customRoute = listOf(
//                    RoutePoint(28.6282046, 77.3821793), // Start
//                    RoutePoint(28.6292046, 77.3831793), // Point 1
//                    RoutePoint(28.6302046, 77.3841793), // Point 2
//                    RoutePoint(28.6312046, 77.3851793), // Point 3
//                    RoutePoint(28.6322046, 77.3861793)  // End
//                )
//                viewModel.simulateRealisticRoute(customRoute, speedKmh = 40.0)
//            }) {
//                Text("Realistic Route (40 km/h)")
//            }
//
//            Button(onClick = { viewModel.stopRoute() }) {
//                Text("Stop Route")
//            }
//
//            Button(onClick = { viewModel.disableMocking() }) {
//                Text("Disable Mock Mode")
//            }
//        }
//    }
//}

@Composable
fun MockLocationScreen(viewModel: LocationViewModel = hiltViewModel()) {
    val locationState by viewModel.locationState.collectAsState()
    val routeProgress by viewModel.routeProgress.collectAsState()

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = locationState,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = routeProgress,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Button(onClick = { viewModel.enableMocking() }) {
                Text("Enable Mock Mode")
            }

            Button(
                onClick = { viewModel.simulateYourCustomRoute(startLocation = CustomLocations.OFFICE, endLocation = CustomLocations.HOME) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Start route Office -> Home")
            }

            Button(
                onClick = { viewModel.simulateYourCustomRoute(startLocation = CustomLocations.HOME, endLocation = CustomLocations.OFFICE) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Start route Home -> Office")
            }

            Button(
                onClick = { viewModel.simulateYourCustomRoute(startLocation = CustomLocations.SPOT, endLocation = CustomLocations.HOME) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Start route Spot -> Home")
            }

            Button(
                onClick = { viewModel.simulateYourCustomRoute(startLocation = CustomLocations.HOME, endLocation = CustomLocations.SPOT) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Start route Home -> Spot")
            }

            Button(
                onClick = { viewModel.simulateYourCustomRoute(startLocation = CustomLocations.ARINDAM, endLocation = CustomLocations.HOME) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Start route Arindam -> Home")
            }

            Button(
                onClick = { viewModel.simulateYourCustomRoute(startLocation = CustomLocations.HOME, endLocation = CustomLocations.ARINDAM) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Start route Home -> Arindam")
            }

            Button(onClick = { viewModel.setArindamLocation() }) {
                Text("Set Arindam mock Location")
            }

            Button(onClick = { viewModel.setOfficeMockLocation() }) {
                Text("Set Office mock Location")
            }

            Button(onClick = { viewModel.setSpotMockLocation() }) {
                Text("Set Spot mock Location")
            }

            Button(onClick = { viewModel.setHomeMockLocation() }) {
                Text("Set Home mock Location")
            }

            Button(onClick = { viewModel.stopRoute() }) {
                Text("Stop Route")
            }

            Button(onClick = { viewModel.disableMocking() }) {
                Text("Disable Mock Mode")
            }
        }
    }
}

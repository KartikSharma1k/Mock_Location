package com.hestabit.fakelocation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun MockLocationScreen(viewModel: LocationViewModel = hiltViewModel()) {
    val locationState by viewModel.locationState.collectAsState()
    val routeProgress by viewModel.routeProgress.collectAsState()

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {


            item {
                Spacer(modifier = Modifier.padding(top = 100.dp))
            }

           item {
               Text(
                   text = locationState,
                   modifier = Modifier.padding(8.dp),
                   style = MaterialTheme.typography.bodyMedium
               )
           }

            item {
                Text(
                    text = routeProgress,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Button(onClick = { viewModel.enableMocking() }) {
                    Text("Enable Mock Mode")
                }
            }

            item {

                Button(
                    onClick = {
                        viewModel.simulateYourCustomRoute(
                            startLocation = CustomLocations.OFFICE,
                            endLocation = CustomLocations.HOME
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Start route Office -> Home")
                }

            }

            item {
                Button(
                    onClick = {
                        viewModel.simulateYourCustomRoute(
                            startLocation = CustomLocations.HOME,
                            endLocation = CustomLocations.OFFICE
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Start route Home -> Office")
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.simulateYourCustomRoute(
                            startLocation = CustomLocations.SPOT,
                            endLocation = CustomLocations.HOME
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Start route Spot -> Home")
                }

            }

            item {

                Button(
                    onClick = {
                        viewModel.simulateYourCustomRoute(
                            startLocation = CustomLocations.HOME,
                            endLocation = CustomLocations.SPOT
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Start route Home -> Spot")
                }

            }

            item {

                Button(
                    onClick = {
                        viewModel.simulateYourCustomRoute(
                            startLocation = CustomLocations.ARINDAM,
                            endLocation = CustomLocations.HOME
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Start route Arindam -> Home")
                }
            }

            item {

                Button(
                    onClick = {
                        viewModel.simulateYourCustomRoute(
                            startLocation = CustomLocations.HOME,
                            endLocation = CustomLocations.ARINDAM
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Start route Home -> Arindam")
                }

            }

            item {

                Button(onClick = { viewModel.setArindamLocation() }) {
                    Text("Set Arindam mock Location")
                }

            }

            item {

                Button(onClick = { viewModel.setOfficeMockLocation() }) {
                    Text("Set Office mock Location")
                }

            }

            item {
                Button(onClick = { viewModel.setSpotMockLocation() }) {
                    Text("Set Spot mock Location")
                }
            }

            item {

                Button(onClick = { viewModel.setHomeMockLocation() }) {
                    Text("Set Home mock Location")
                }

            }

            item {

                Button(onClick = { viewModel.setNishantLocation() }) {
                    Text("Set Nishant mock Location")
                }

            }

            item {

                Button(onClick = { viewModel.setGymLocation() }) {
                    Text("Set Gym mock Location")
                }
            }

            item {
                Button(onClick = { viewModel.setRimjhimLocation() }) {
                    Text("Set Rimjhim mock Location")
                }
            }

            item {
                Button(onClick = { viewModel.stopRoute() }) {
                    Text("Stop Route")
                }
            }
            item {
                Button(onClick = { viewModel.disableMocking() }) {
                    Text("Disable Mock Mode")
                }
            }

            item {
                Spacer(modifier = Modifier.padding(bottom = 100.dp))
            }
        }
    }
}

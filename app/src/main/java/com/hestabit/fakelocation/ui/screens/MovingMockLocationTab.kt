package com.hestabit.fakelocation.ui.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MovingMockLocationTab(
    modifier: Modifier = Modifier,
    viewModel: LocationViewModel = hiltViewModel(),
    onClose: (LatLng?) -> Unit
) {

    val savedLocations by viewModel.savedLocations.collectAsState()

    var startPoint by remember { mutableStateOf<String?>(null) }
    var startCord by remember { mutableStateOf<LatLng?>(null) }

    var endPoint by remember { mutableStateOf<String?>(null) }
    var endCord by remember { mutableStateOf<LatLng?>(null) }

    val scope = rememberCoroutineScope()

    val context = LocalContext.current

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


    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {

        item {
            Text(
                "Start - End",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 20.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Row() {

                if (startPoint == null) Text("Choose Start Point -> ") else Text("$startPoint -> ")

                if (endPoint == null) Text("Choose End Point") else Text(endPoint.toString())

            }

        }

        item {
            Text(
                "Recent Locations",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 20.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(savedLocations) { location ->

            RecentLocationItem(location, onClose = { latLng, name ->
                if (startPoint != null && endPoint != null) {
                    onClose(startCord)
                } else {
                    if (startPoint == null){
                        startCord = latLng
                        startPoint = name
                    }else {
                        endCord = latLng
                        endPoint = name
                        scope.launch {
                            viewModel.startRoute(startCord!!, endCord!!, apiKey = apiKey!!)
                            delay(500)
                            onClose(startCord)
                        }
                    }
                }
            }, viewModel = viewModel)

        }

    }

}
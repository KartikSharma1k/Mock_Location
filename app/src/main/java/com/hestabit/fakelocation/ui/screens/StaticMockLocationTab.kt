package com.hestabit.fakelocation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.LatLng
import com.hestabit.fakelocation.data.model.SavedLocation

@Composable
fun StaticMockLocationTab(
    modifier: Modifier = Modifier,
    viewModel: LocationViewModel = hiltViewModel(),
    onClose: (LatLng?) -> Unit,
    onNavigate: () -> Unit
) {
    val savedLocations by viewModel.savedLocations.collectAsState()

    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {

        item {
            Text("Recent Locations", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 20.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        items(savedLocations) { location ->

            RecentLocationItem(location, onClose = { latLng, name -> onClose(latLng) }, viewModel = viewModel)

        }

        item {
            Button(onClick = {onNavigate()}) {
                Text("Add more")
            }
        }

    }

}

@Composable
fun RecentLocationItem(location: SavedLocation,
                       viewModel: LocationViewModel = hiltViewModel(),
                       onClose: (LatLng?, String?) -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(){
                viewModel.setPinnedLocation(LatLng(location.latitude, location.longitude))
                onClose(LatLng(location.latitude, location.longitude), location.name)
            }
            .border(1.dp, color = Color.DarkGray, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Icon(
            Icons.Default.LocationOn,
            "",
            modifier = Modifier
                .background(color = Color.White, shape = CircleShape)
                .padding(10.dp)
                .size(25.dp),
            tint = Color.Gray
        )

        Column() {
            Text(
                location.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 22.sp
            )
            Text(
                "lat: ${location.latitude}, long: ${location.longitude}",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Gray
            )

        }

    }

}
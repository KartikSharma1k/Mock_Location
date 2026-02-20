package com.hestabit.fakelocation.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

    LazyColumn(modifier = modifier) {

        item {
            if (savedLocations.any { location -> location.favourite })
                Text(
                    "Favourite Locations",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 20.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
        }

        items(savedLocations, key = { "fav-${it.id}" }) { location ->
            if (location.favourite) {
                RecentLocationItem(
                    modifier = Modifier.padding(bottom = 10.dp),
                    location = location,
                    onClose = { latLng, _ -> onClose(latLng) },
                    viewModel = viewModel
                )
            }
        }

        item {
            if (savedLocations.any { location -> !location.favourite })
                Text(
                    "Saved Locations",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 20.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
        }

        items(savedLocations, key = { it.id }) { location ->
            if (!location.favourite)
                RecentLocationItem(
                    modifier = Modifier.padding(bottom = 10.dp),
                    location = location,
                    onClose = { latLng, _ -> onClose(latLng) },
                    viewModel = viewModel
                )
        }

        item {
            Button(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate() }) {
                Text("Add more")
            }
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentLocationItem(
    modifier: Modifier = Modifier,
    location: SavedLocation,
    viewModel: LocationViewModel = hiltViewModel(),
    onClose: (LatLng?, String?) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe LEFT → Delete
                    viewModel.deleteLocation(location.id)
                    true
                }

                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swipe RIGHT → Toggle favourite
                    viewModel.toggleFavourite(location.id, !location.favourite)
                    true // Reset back to initial position
                }

                else -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.10f }
    )

    // Reset state after a favourite toggle so the item snaps back
    LaunchedEffect(location.favourite) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            SwipeBackground(dismissState.targetValue)
        },
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .animateContentSize()
    ) {
        // Foreground — the actual location card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.setPinnedLocation(LatLng(location.latitude, location.longitude))
                    onClose(LatLng(location.latitude, location.longitude), location.name)
                }
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, color = Color.DarkGray, shape = RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Icon(
                Icons.Default.LocationOn,
                contentDescription = "Location",
                modifier = Modifier
                    .background(color = Color.White, shape = CircleShape)
                    .padding(10.dp)
                    .size(25.dp),
                tint = Color.Gray
            )

            Column(modifier = Modifier.weight(1f)) {
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

            // Favourite indicator icon on the right side of the card
            if (location.favourite)
                Icon(
                    imageVector = if (location.favourite) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = if (location.favourite) "Favourited" else "Not favourited",
                    tint = if (location.favourite) Color(0xFFFFC107) else Color.LightGray,
                    modifier = Modifier.size(22.dp)
                )
        }
    }
}

/**
 * The coloured background that appears behind the card while swiping.
 * - Swipe RIGHT (StartToEnd) → Gold/yellow background with a star icon
 * - Swipe LEFT  (EndToStart) → Red background with a delete icon
 */
@Composable
private fun SwipeBackground(targetValue: SwipeToDismissBoxValue) {
    val isFavouriteSwipe = targetValue == SwipeToDismissBoxValue.StartToEnd
    val isDeleteSwipe = targetValue == SwipeToDismissBoxValue.EndToStart

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFavouriteSwipe -> Color(0xFFFFC107)   // Amber / Gold
            isDeleteSwipe -> Color(0xFFE53935)   // Red
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 300),
        label = "swipe_bg_color"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isFavouriteSwipe || isDeleteSwipe) 1f else 0.6f,
        animationSpec = tween(durationMillis = 200),
        label = "swipe_icon_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 20.dp),
        contentAlignment = if (isFavouriteSwipe) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        when {
            isFavouriteSwipe -> Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Add to favourites",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .scale(iconScale)
            )

            isDeleteSwipe -> Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .scale(iconScale)
            )
        }
    }
}
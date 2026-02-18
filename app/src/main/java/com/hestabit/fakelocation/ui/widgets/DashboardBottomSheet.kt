package com.hestabit.fakelocation.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.hestabit.fakelocation.data.model.SavedLocation
import com.hestabit.fakelocation.ui.screens.LocationViewModel
import com.hestabit.fakelocation.ui.screens.MovingMockLocationTab
import com.hestabit.fakelocation.ui.screens.StaticMockLocationTab
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBottomSheet(
    modifier: Modifier = Modifier,
    viewModel: LocationViewModel,
    onClose: (LatLng?) -> Unit,
    onNavigate: () -> Unit
) {

    var selectedTab by remember { mutableStateOf(0) }

    val pagerState = rememberPagerState(selectedTab) { 2 }

    val scope = rememberCoroutineScope()

    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxSize()) {


            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Settings")
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    onClose(viewModel.pinnedLocation.value)
                }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close button",
                        tint = Color.White
                    )
                }
            }



            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == pagerState.currentPage,
                    onClick = {
                        scope.launch {
                            selectedTab = 0
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    text = { Text("Static location") })
                Tab(
                    selected = selectedTab == pagerState.currentPage,
                    onClick = {
                        scope.launch {
                            selectedTab = 1
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    text = { Text("Moving location") })
            }




            HorizontalPager(state = pagerState) { page ->

                when (page) {
                    0 -> {
                        selectedTab = pagerState.currentPage
                        StaticMockLocationTab(onClose = onClose, viewModel = viewModel, onNavigate = onNavigate)
                    }

                    1 -> {
                        selectedTab = pagerState.currentPage
                        MovingMockLocationTab(viewModel = viewModel, onClose = onClose)
                    }
                }

            }


        }
    }

}
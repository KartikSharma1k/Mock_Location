package com.hestabit.fakelocation.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeveloperInstructionsScreen(
    viewModel: DeveloperInstructionsViewModel = hiltViewModel(),
    onInstructionsCompleted: () -> Unit
) {
    val context = LocalContext.current
    val pages = listOf(
        InstructionPage(
            title = "Enable Developer Options",
            description = "Go to Settings > About Phone > Tap 'Build Number' 7 times.",
            icon = Icons.Default.Settings,
            actionLabel = "Open Settings",
            action = { 
                try {
                    val intent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS).apply {
                        putExtra(":settings:fragment_args_key", "build_number")
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        ),
        InstructionPage(
            title = "Select Mock Location App",
            description = "In Developer Options, find 'Select mock location app' and choose this app.",
            icon = Icons.Default.LocationOn,
            actionLabel = "Open Developer Options",
            action = {
                // Try to open developer settings directly
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                        putExtra(":settings:fragment_args_key", "mock_location_app")
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                     context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        ),
        InstructionPage(
            title = "All Set!",
            description = "You are ready to start mocking your location.",
            icon = Icons.Default.CheckCircle,
            actionLabel = null,
            action = null
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        Spacer(modifier = Modifier.weight(1f))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(4f)
        ) { pageIndex ->
             InstructionPageContent(page = pages[pageIndex])
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
             Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pages.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                         scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                    } else {
                        viewModel.completeInstructions()
                        onInstructionsCompleted()
                    }
                }
            ) {
                Text(if (pagerState.currentPage < pages.size - 1) "Next" else "Finish")
            }
        }
    }
}

@Composable
fun InstructionPageContent(page: InstructionPage) {
     Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        if (page.actionLabel != null && page.action != null) {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = page.action) {
                Text(page.actionLabel)
            }
        }
    }
}

data class InstructionPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val actionLabel: String?,
    val action: (() -> Unit)?
)

package com.hestabit.fakelocation.ui.screens.Onboarding.viewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hestabit.fakelocation.R
import com.hestabit.fakelocation.data.local.DataStoreManager
import com.hestabit.fakelocation.data.model.OnboardingPage
import com.hestabit.fakelocation.data.model.ParticleSpec
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val particleSpecs = List(8) { i ->
        ParticleSpec(
            xFrac = ((i * 137 + 23) % 100) / 100f,
            yFrac = ((i * 97 + 41) % 100) / 100f,
            durationMs = 3000 + (i * 400 % 2000),
            delayMs = (i * 300 % 2000)
        )
    }

    val pages = listOf(
        OnboardingPage(
            title = "Mock Your Location",
            description = "Teleport your phone to anywhere in the world with a single tap.",
            icon = Icons.Default.LocationOn,
            gradientStart = Color(0xFF3B82F6), // blue-500
            gradientEnd = Color(0xFF06B6D4)  // cyan-500
        ),
        OnboardingPage(
            title = "Simulate Movement",
            description = "Create realistic routes and move along them at adjustable speeds.",
            drawableId = R.drawable.moving_location,
            gradientStart = Color(0xFFA855F7), // purple-500
            gradientEnd = Color(0xFFEC4899)  // pink-500
        ),
        OnboardingPage(
            title = "Easy Controls",
            description = "Select start and destination points directly on the map.",
            icon = Icons.Default.Settings,
            gradientStart = Color(0xFFF97316), // orange-500
            gradientEnd = Color(0xFFEF4444)  // red-500
        )
    )

    fun completeOnboarding() {
        viewModelScope.launch {
            dataStoreManager.setOnboardingCompleted(true)
        }
    }
}
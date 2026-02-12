package com.hestabit.fakelocation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.hestabit.fakelocation.ui.MainViewModel
import com.hestabit.fakelocation.ui.MainViewModel.Companion.isLoading
import com.hestabit.fakelocation.ui.navigation.AppNavigation
import com.hestabit.fakelocation.ui.theme.FakeLocationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep splash screen on until start destination is determined
        splashScreen.setKeepOnScreenCondition {
            viewModel.startDestination.value == null
        }

        setContent {
            FakeLocationTheme {

                val isLoading by isLoading.collectAsState()

                val startDestination by viewModel.startDestination.collectAsState()

                if (startDestination != null) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                        val navController = rememberNavController()
                        Box() {
                            AppNavigation(
                                navController = navController,
                                startDestination = startDestination!!,
                                modifier = Modifier
                            )

                            if(isLoading){
                                Box(modifier = Modifier.fillMaxSize().background(color = Color.Black.copy(0.5f)).clickable(false){}.zIndex(10f), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                } else {
                    // Fallback loading screen (should rely on splash screen predominantly)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

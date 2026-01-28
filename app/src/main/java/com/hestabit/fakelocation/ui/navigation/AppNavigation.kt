package com.hestabit.fakelocation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hestabit.fakelocation.ui.screens.AuthScreen
import com.hestabit.fakelocation.ui.screens.DashboardScreen
import com.hestabit.fakelocation.ui.screens.DeveloperInstructionsScreen
import com.hestabit.fakelocation.ui.screens.IntroScreen
import com.hestabit.fakelocation.ui.screens.PermissionsScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Intro.route) {
            IntroScreen(
                onOnboardingFinished = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Intro.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Permissions.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Permissions.route) {
            PermissionsScreen(
                onPermissionsGranted = {
                    navController.navigate(Screen.DeveloperInstructions.route) {
                        popUpTo(Screen.Permissions.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.DeveloperInstructions.route) {
            DeveloperInstructionsScreen(
                onInstructionsCompleted = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.DeveloperInstructions.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }
    }
}

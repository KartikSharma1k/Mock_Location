package com.hestabit.fakelocation.ui.navigation

sealed class Screen(val route: String) {
    object Intro : Screen("intro")
    object Auth : Screen("auth")
    object Permissions : Screen("permissions")
    object DeveloperInstructions : Screen("developer_instructions")
    object Dashboard : Screen("dashboard")
    object FullScreenMap : Screen("full_screen_map")
}

object NavArgs {
    // Define arguments here if needed
}

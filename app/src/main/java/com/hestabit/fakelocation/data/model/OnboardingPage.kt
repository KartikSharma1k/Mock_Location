package com.hestabit.fakelocation.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector? = null,
    val gradientStart: Color,
    val gradientEnd: Color,
    val drawableId: Int? = null,
)

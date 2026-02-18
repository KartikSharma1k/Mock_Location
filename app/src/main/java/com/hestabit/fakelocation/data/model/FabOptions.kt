package com.hestabit.fakelocation.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class FabOption(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

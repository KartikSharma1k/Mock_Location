package com.hestabit.fakelocation.ui.screens.Onboarding.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Floating animated particle.
 *
 * Pass [particleColor] for a solid-colour particle (e.g. white/30 for AuthScreen).
 * Leave it null to use the gradient mode (gradientStart → gradientEnd).
 */
@Composable
fun FloatingParticle(
    xFrac: Float,
    yFrac: Float,
    durationMs: Int,
    delayMs: Int,
    index: Int,
    gradientStart: Color = Color.White,
    gradientEnd: Color = Color.White,
    particleColor: Color? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particle_$index")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing, delayMillis = delayMs),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particleY_$index"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = if (particleColor != null) 0.3f else 0.2f,
        targetValue  = if (particleColor != null) 1.0f else 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing, delayMillis = delayMs),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particleAlpha_$index"
    )
    val particleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing, delayMillis = delayMs),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particleScale_$index"
    )

    val bgModifier = if (particleColor != null) {
        Modifier.background(particleColor.copy(alpha = alpha), CircleShape)
    } else {
        Modifier.background(
            Brush.radialGradient(
                listOf(
                    gradientStart.copy(alpha = alpha),
                    gradientEnd.copy(alpha = alpha * 0.5f)
                )
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(
                    x = (xFrac * 360).dp,
                    y = (yFrac * 740 + offsetY).dp
                )
                .size(8.dp)
                .scale(particleScale)
                .clip(CircleShape)
                .then(bgModifier)
        )
    }
}
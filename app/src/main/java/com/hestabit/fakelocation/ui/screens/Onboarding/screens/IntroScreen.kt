package com.hestabit.fakelocation.ui.screens.Onboarding.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hestabit.fakelocation.ui.screens.Onboarding.components.FloatingParticle
import com.hestabit.fakelocation.ui.screens.Onboarding.components.PageContent
import com.hestabit.fakelocation.ui.screens.Onboarding.viewModel.IntroViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntroScreen(
    viewModel: IntroViewModel = hiltViewModel(),
    onOnboardingFinished: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { viewModel.pages.size })
    val scope = rememberCoroutineScope()

    val currentStep = pagerState.currentPage
    val page = viewModel.pages[currentStep]

    fun finish() {
        viewModel.completeOnboarding()
        onOnboardingFinished()
    }

    fun scrollTo(index: Int) = scope.launch { pagerState.animateScrollToPage(index) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        val bgAlpha by animateFloatAsState(
            targetValue = 0.3f,
            animationSpec = tween(500),
            label = "bgAlpha"
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            page.gradientStart.copy(alpha = bgAlpha),
                            page.gradientEnd.copy(alpha = bgAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        val infiniteTransition = rememberInfiniteTransition(label = "decorBlobs")

        val blobScale1 by infiniteTransition.animateFloat(
            initialValue = 1f, targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                tween(20_000, easing = LinearEasing),
                RepeatMode.Reverse
            ),
            label = "blobScale1"
        )
        val blobRotate1 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(
                tween(20_000, easing = LinearEasing),
                RepeatMode.Restart
            ),
            label = "blobRotate1"
        )
        val blobScale2 by infiniteTransition.animateFloat(
            initialValue = 1f, targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                tween(15_000, easing = LinearEasing),
                RepeatMode.Reverse
            ),
            label = "blobScale2"
        )
        val blobRotate2 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = -360f,
            animationSpec = infiniteRepeatable(
                tween(15_000, easing = LinearEasing),
                RepeatMode.Restart
            ),
            label = "blobRotate2"
        )

        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = (-20).dp, y = 80.dp)
                .align(Alignment.TopEnd)
                .scale(blobScale1)
                .rotate(blobRotate1)
                .blur(60.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            page.gradientStart.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .align(Alignment.BottomStart)
                .scale(blobScale2)
                .rotate(blobRotate2)
                .blur(60.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            page.gradientEnd.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        viewModel.particleSpecs.forEachIndexed { i, spec ->
            FloatingParticle(
                xFrac = spec.xFrac,
                yFrac = spec.yFrac,
                durationMs = spec.durationMs,
                delayMs = spec.delayMs,
                gradientStart = page.gradientStart,
                gradientEnd = page.gradientEnd,
                index = i
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(4f)
            ) { pageIndex ->
                PageContent(page = viewModel.pages[pageIndex])
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                viewModel.pages.forEachIndexed { index, _ ->
                    val isActive = index == currentStep
                    val dotWidth by animateDpAsState(
                        targetValue = if (isActive) 32.dp else 8.dp,
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        label = "dotWidth_$index"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(dotWidth)
                            .clip(RoundedCornerShape(50))
                            .background(if (isActive) Color.White else Color(0xFF4B5563))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { scrollTo(index) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                modifier = Modifier.padding(horizontal = 24.dp),
                targetState = currentStep == viewModel.pages.size - 1,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "buttons"
            ) { isLast ->
                if (isLast) {
                    Button(
                        onClick = { finish() },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Get Started", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { finish() }) {
                            Text("Skip", color = Color(0xFF9CA3AF), fontWeight = FontWeight.Medium)
                        }
                        Button(
                            onClick = { scrollTo(currentStep + 1) },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Next", fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("→", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}



package com.hestabit.fakelocation.ui.screens.Authentication.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hestabit.fakelocation.ui.components.CountryCodePicker
import com.hestabit.fakelocation.ui.components.countryCodes
import com.hestabit.fakelocation.ui.screens.Authentication.viewModel.AuthUiState
import com.hestabit.fakelocation.ui.screens.Authentication.viewModel.AuthViewModel
import com.hestabit.fakelocation.ui.screens.Onboarding.components.FloatingParticle


@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context  = LocalContext.current
    val activity = context.findActivity()

    var phoneNumber by remember { mutableStateOf("") }
    var selectedCountry by remember {
        mutableStateOf(countryCodes.first { it.dialCode == "+91" && it.name == "India" })
    }
    val otpDigits   = remember { mutableStateListOf("", "", "", "", "", "") }

    // Derived UI step from ViewModel state
    val isOtpStep = uiState is AuthUiState.OtpSent
    val isLoading = uiState is AuthUiState.Loading

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) onAuthSuccess()
        if (uiState is AuthUiState.Error) {
            Toast.makeText(context, (uiState as AuthUiState.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    // ── Background ──────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1D4ED8), // blue-700
                        Color(0xFF7C3AED), // purple-700
                        Color(0xFFBE185D)  // pink-700
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // Rotating blobs
        val blobTransition = rememberInfiniteTransition(label = "blobs")
        val blobRotate1 by blobTransition.animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(20_000, easing = LinearEasing), RepeatMode.Restart),
            label = "blobR1"
        )
        val blobScale1 by blobTransition.animateFloat(
            initialValue = 1f, targetValue = 1.2f,
            animationSpec = infiniteRepeatable(tween(20_000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "blobS1"
        )
        val blobRotate2 by blobTransition.animateFloat(
            initialValue = 0f, targetValue = -360f,
            animationSpec = infiniteRepeatable(tween(15_000, easing = LinearEasing), RepeatMode.Restart),
            label = "blobR2"
        )
        val blobScale2 by blobTransition.animateFloat(
            initialValue = 1f, targetValue = 1.3f,
            animationSpec = infiniteRepeatable(tween(15_000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "blobS2"
        )

        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = 60.dp, y = (-160).dp)
                .align(Alignment.TopEnd)
                .scale(blobScale1)
                .rotate(blobRotate1)
                .blur(60.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-60).dp, y = 80.dp)
                .align(Alignment.BottomStart)
                .scale(blobScale2)
                .rotate(blobRotate2)
                .blur(60.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        )

        // Floating particles
        viewModel.authParticleSpecs.forEachIndexed { i, spec ->
            FloatingParticle(
                xFrac         = spec.xFrac,
                yFrac         = spec.yFrac,
                durationMs    = spec.durationMs,
                delayMs       = spec.delayMs,
                index         = i,
                particleColor = Color.White.copy(alpha = 0.3f)
            )
        }

        // ── Card ─────────────────────────────────────────────────────────────
        val cardScale by rememberInfiniteTransition(label = "card").animateFloat(
            // one-shot spring look: just initialise at 0.9 with the infinite staying at 1f
            initialValue = 1f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1), RepeatMode.Restart),
            label = "cardScale"
        )

        // We use AnimatedVisibility to do the pop-in once
        var cardVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { cardVisible = true }

        AnimatedVisibility(
            visible = cardVisible,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
                    slideInHorizontally(spring(stiffness = Spring.StiffnessMedium)) { it / 8 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .padding(horizontal = 28.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Pulsing logo icon ─────────────────────────────────────────
                val pulseTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by pulseTransition.animateFloat(
                    initialValue = 1f, targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome Back",
                    color = Color(0xFF111827),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (!isOtpStep) "Enter your phone number to continue"
                           else "Enter the verification code",
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ── Step content: phone or OTP ────────────────────────────────
                AnimatedContent(
                    targetState = isOtpStep,
                    transitionSpec = {
                        if (targetState) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "stepContent"
                ) { otpStep ->
                    if (!otpStep) {
                        // ── Phone step ─────────────────────────────────────────
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Phone input row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF9FAFB))
                                    .border(1.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Country code picker chip
                                CountryCodePicker(
                                    selected = selectedCountry,
                                    onSelected = { selectedCountry = it }
                                )

                                // Vertical divider
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp)
                                        .width(1.dp)
                                        .height(28.dp)
                                        .background(Color(0xFFE5E7EB))
                                )

                                BasicTextField(
                                    value = phoneNumber,
                                    onValueChange = { new ->
                                        phoneNumber = new.filter { it.isDigit() }.take(10)
                                        viewModel.resetResendToken()
                                    },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color(0xFF111827),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    cursorBrush = SolidColor(Color(0xFF3B82F6)),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    decorationBox = { inner ->
                                        if (phoneNumber.isEmpty()) {
                                            Text("Phone number", color = Color(0xFF9CA3AF), fontSize = 15.sp)
                                        }
                                        inner()
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (activity != null && phoneNumber.length >= 7) {
                                        viewModel.sendOtp("${selectedCountry.dialCode}$phoneNumber", activity)
                                    } else {
                                        Toast.makeText(context, "Enter a valid phone number", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                                        ),
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Continue",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // ── OTP step ───────────────────────────────────────────
                        val focusRequesters = remember { List(6) { FocusRequester() } }
                        LaunchedEffect(Unit) {
                            runCatching { focusRequesters[0].requestFocus() }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            // 6-box OTP row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                otpDigits.forEachIndexed { idx, digit ->
                                    BasicTextField(
                                        value = digit,
                                        onValueChange = { new ->
                                            val cleaned = new.filter { it.isDigit() }.take(1)
                                            otpDigits[idx] = cleaned
                                            if (cleaned.isNotEmpty() && idx < 5) {
                                                runCatching { focusRequesters[idx + 1].requestFocus() }
                                            }
                                            // Auto-submit when last digit entered
                                            if (idx == 5 && cleaned.isNotEmpty()) {
                                                val full = otpDigits.joinToString("")
                                                if (full.length == 6) viewModel.verifyOtp(full)
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF9FAFB))
                                            .border(
                                                width = if (digit.isNotEmpty()) 2.dp else 1.5.dp,
                                                color = if (digit.isNotEmpty()) Color(0xFF3B82F6) else Color(0xFFE5E7EB),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .focusRequester(focusRequesters[idx]),
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = Color(0xFF111827),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        ),
                                        cursorBrush = SolidColor(Color(0xFF3B82F6)),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        decorationBox = { inner ->
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                inner()
                                            }
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Resend
                            Text(
                                text = "Didn't receive the code?",
                                color = Color(0xFF6B7280),
                                fontSize = 13.sp
                            )
                            TextButton(onClick = {
                                otpDigits.fill("")
                                runCatching { focusRequesters[0].requestFocus() }
                                if (activity != null) viewModel.sendOtp("${selectedCountry.dialCode}$phoneNumber", activity)
                            }) {
                                Text(
                                    text = "Resend OTP",
                                    color = Color(0xFF3B82F6),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Verify button
                            Button(
                                onClick = {
                                    val full = otpDigits.joinToString("")
                                    if (full.length == 6) viewModel.verifyOtp(full)
                                },
                                enabled = !isLoading && otpDigits.joinToString("").length == 6,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                                        ),
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Verify & Continue",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Change phone number
                            TextButton(onClick = {
                                otpDigits.fill("")
                                viewModel.resetToIdle()
                            }) {
                                Text(
                                    text = "Change phone number",
                                    color = Color(0xFF6B7280),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Terms
                Text(
                    text = "By continuing, you agree to our Terms of Service and Privacy Policy",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

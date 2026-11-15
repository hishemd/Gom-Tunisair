package com.weathersense.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * A flip clock: HH:MM with each digit on its own split-flap card.
 * The digit card is a dark rounded tile with a centre hinge line, and a
 * rotationX flip animates whenever the digit changes.
 */
@Composable
fun FlipClock(
    time: String,
    modifier: Modifier = Modifier,
    cardColor: Color = Color(0xFF1C1C1E),
    digitColor: Color = Color.White,
) {
    val parts = time.split(":")
    val hours = parts.getOrNull(0) ?: "--"
    val minutes = parts.getOrNull(1) ?: "--"

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        FlipDigit(hours.getOrNull(0)?.toString() ?: "-", cardColor, digitColor)
        FlipDigit(hours.getOrNull(1)?.toString() ?: "-", cardColor, digitColor)
        Colon(digitColor)
        FlipDigit(minutes.getOrNull(0)?.toString() ?: "-", cardColor, digitColor)
        FlipDigit(minutes.getOrNull(1)?.toString() ?: "-", cardColor, digitColor)
    }
}

@Composable
fun FlipDigit(
    digit: String,
    cardColor: Color = Color(0xFF1C1C1E),
    digitColor: Color = Color.White,
) {
    var shown by remember { mutableStateOf(digit) }
    var rotation by remember { mutableFloatStateOf(0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "flipRotation",
    )

    LaunchedEffect(digit) {
        if (digit == shown) return@LaunchedEffect
        rotation = -88f
        shown = digit
        delay(16)
        rotation = 88f
        delay(16)
        rotation = 0f
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(64.dp)
            .height(92.dp)
            .background(
                brush = Brush.verticalGradient(
                    listOf(cardColor.lighten(0.10f), cardColor, cardColor.darken(0.08f)),
                ),
                shape = RoundedCornerShape(10.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Hinge line across the middle of the card.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Black.copy(alpha = 0.55f))
                .align(Alignment.Center),
        )
        Text(
            text = shown,
            modifier = Modifier.graphicsLayer {
                rotationX = animatedRotation
                cameraDistance = 16f * density
            },
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            color = digitColor,
            maxLines = 1,
        )
    }
}

@Composable
fun Colon(color: Color = Color.White) {
    Column(
        modifier = Modifier.width(22.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(7.dp).background(color, RoundedCornerShape(50)))
        Spacer(modifier = Modifier.height(14.dp))
        Box(modifier = Modifier.size(7.dp).background(color, RoundedCornerShape(50)))
    }
}

private fun Color.lighten(factor: Float): Color = Color(
    red = (red + factor).coerceAtMost(1f),
    green = (green + factor).coerceAtMost(1f),
    blue = (blue + factor).coerceAtMost(1f),
    alpha = alpha,
)

private fun Color.darken(factor: Float): Color = Color(
    red = (red - factor).coerceAtLeast(0f),
    green = (green - factor).coerceAtLeast(0f),
    blue = (blue - factor).coerceAtLeast(0f),
    alpha = alpha,
)

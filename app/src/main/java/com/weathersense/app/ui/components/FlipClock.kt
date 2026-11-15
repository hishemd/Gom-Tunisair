package com.weathersense.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CardWidth = 64.dp
private val CardHeight = 92.dp
private val HalfHeight = 46.dp
private const val FlipMillis = 420

/**
 * Split-flap clock. Each digit is a card cut along its middle: the top half of
 * the outgoing digit falls forward around the hinge, then the bottom half of
 * the incoming digit drops into place behind it.
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
    // `shown` is the digit now on the card, `previous` is the one falling away.
    var shown by remember { mutableStateOf(digit) }
    var previous by remember { mutableStateOf(digit) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(digit) {
        if (digit == shown) return@LaunchedEffect
        previous = shown
        shown = digit
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = FlipMillis, easing = LinearEasing),
        )
    }

    val p = progress.value
    val flipping = p > 0f && p < 1f

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(CardWidth)
            .height(CardHeight),
    ) {
        // Back layer: the incoming digit's top half, revealed as the flap falls.
        FlipHalf(
            digit = shown,
            topHalf = true,
            cardColor = cardColor,
            digitColor = digitColor,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // Back layer: the outgoing digit's bottom half until the new flap lands.
        FlipHalf(
            digit = if (p < 0.5f) previous else shown,
            topHalf = false,
            cardColor = cardColor,
            digitColor = digitColor,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (flipping) {
            if (p < 0.5f) {
                // First half of the cycle: old top half rotates down to edge-on.
                FlipHalf(
                    digit = previous,
                    topHalf = true,
                    cardColor = cardColor,
                    digitColor = digitColor,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0.5f, 1f)
                            rotationX = -p * 180f
                            cameraDistance = 14f * density
                        },
                )
            } else {
                // Second half: new bottom half swings from edge-on down to flat.
                FlipHalf(
                    digit = shown,
                    topHalf = false,
                    cardColor = cardColor,
                    digitColor = digitColor,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0.5f, 0f)
                            rotationX = (1f - p) * 180f
                            cameraDistance = 14f * density
                        },
                )
            }
        }

        // Hinge line between the two halves.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Black.copy(alpha = 0.55f))
                .align(Alignment.Center),
        )
    }
}

/** One horizontal half of a flip card, clipped so the digit is cut at the hinge. */
@Composable
private fun FlipHalf(
    digit: String,
    topHalf: Boolean,
    cardColor: Color,
    digitColor: Color,
    modifier: Modifier = Modifier,
) {
    val shape = if (topHalf) {
        RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
    } else {
        RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
    }

    val gradient = if (topHalf) {
        listOf(cardColor.lighten(0.08f), cardColor)
    } else {
        listOf(cardColor.darken(0.04f), cardColor.darken(0.16f))
    }

    Box(
        modifier = modifier
            .width(CardWidth)
            .height(HalfHeight)
            .clip(shape)
            .background(Brush.verticalGradient(gradient)),
    ) {
        // A full-height digit window, shifted so only this half shows through.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CardHeight)
                .offset(y = if (topHalf) 0.dp else -HalfHeight),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = digit,
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = digitColor,
                maxLines = 1,
            )
        }
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

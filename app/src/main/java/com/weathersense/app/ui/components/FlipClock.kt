package com.weathersense.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FlipMillis = 430
private val CardCorner = 16.dp

/**
 * Split-flap clock. Each digit is a card cut along a horizontal hinge: the top
 * half of the outgoing digit falls forward around that hinge, then the bottom
 * half of the incoming digit swings down into place.
 *
 * The card size is derived from the width the clock is given, so four digits
 * plus the colon always fit and never look cramped on a narrow screen.
 */
@Composable
fun FlipClock(
    time: String,
    modifier: Modifier = Modifier,
    cardColor: Color = Color(0xFF17171A),
    digitColor: Color = Color(0xFFF7F7FA),
) {
    val parts = time.split(":")
    val hours = parts.getOrNull(0) ?: "--"
    val minutes = parts.getOrNull(1) ?: "--"

    BoxWithConstraints(modifier = modifier) {
        val gap = 3.dp
        val colonWidth = 18.dp
        val available = maxWidth - colonWidth - gap * 8
        val cardWidth = (available / 4f).coerceIn(44.dp, 72.dp)
        val cardHeight = cardWidth * 1.45f
        // The glyph must be taller than half a card, otherwise each half shows a
        // whole digit instead of one cut at the hinge.
        val digitSize = (cardHeight.value * 1.06f).sp

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            FlipDigit(hours.getOrNull(0)?.toString() ?: "-", cardWidth, cardHeight, digitSize, cardColor, digitColor, gap)
            FlipDigit(hours.getOrNull(1)?.toString() ?: "-", cardWidth, cardHeight, digitSize, cardColor, digitColor, gap)
            Colon(color = digitColor, size = cardHeight)
            FlipDigit(minutes.getOrNull(0)?.toString() ?: "-", cardWidth, cardHeight, digitSize, cardColor, digitColor, gap)
            FlipDigit(minutes.getOrNull(1)?.toString() ?: "-", cardWidth, cardHeight, digitSize, cardColor, digitColor, gap)
        }
    }
}

@Composable
private fun FlipDigit(
    digit: String,
    cardWidth: Dp,
    cardHeight: Dp,
    digitSize: androidx.compose.ui.unit.TextUnit,
    cardColor: Color,
    digitColor: Color,
    gap: Dp,
) {
    // `shown` is the digit currently on the card, `previous` is the one leaving.
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
    val half = cardHeight / 2f
    val shape = RoundedCornerShape(CardCorner)

    Box(
        modifier = Modifier
            .padding(horizontal = gap)
            .width(cardWidth)
            .height(cardHeight)
            .clip(shape),
    ) {
        // Incoming digit's top half, uncovered as the old flap falls away.
        FlipHalf(
            digit = shown,
            topHalf = true,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            digitSize = digitSize,
            cardColor = cardColor,
            digitColor = digitColor,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // Bottom half: the old digit until the new flap lands, then the new one.
        FlipHalf(
            digit = if (p < 0.5f) previous else shown,
            topHalf = false,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            digitSize = digitSize,
            cardColor = cardColor,
            digitColor = digitColor,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (p > 0f && p < 1f) {
            if (p < 0.5f) {
                // Old top half tips forward around the hinge until edge-on.
                FlipHalf(
                    digit = previous,
                    topHalf = true,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    digitSize = digitSize,
                    cardColor = cardColor,
                    digitColor = digitColor,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0.5f, 1f)
                            rotationX = -p * 180f
                            cameraDistance = 16f * density
                        },
                )
            } else {
                // New bottom half drops from edge-on down to flat.
                FlipHalf(
                    digit = shown,
                    topHalf = false,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    digitSize = digitSize,
                    cardColor = cardColor,
                    digitColor = digitColor,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0.5f, 0f)
                            rotationX = (1f - p) * 180f
                            cameraDistance = 16f * density
                        },
                )
            }
        }

        // Hinge: a dark seam plus a faint highlight under it.
        Column(modifier = Modifier.align(Alignment.Center)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(Color.Black.copy(alpha = 0.65f)),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.06f)),
            )
        }
    }
}

/**
 * One horizontal half of a flip card. The digit is laid out at full card height
 * and then clipped, so the two halves join perfectly at the hinge.
 */
@Composable
private fun FlipHalf(
    digit: String,
    topHalf: Boolean,
    cardWidth: Dp,
    cardHeight: Dp,
    digitSize: androidx.compose.ui.unit.TextUnit,
    cardColor: Color,
    digitColor: Color,
    modifier: Modifier = Modifier,
) {
    val half = cardHeight / 2f
    val shape = if (topHalf) {
        RoundedCornerShape(topStart = CardCorner, topEnd = CardCorner)
    } else {
        RoundedCornerShape(bottomStart = CardCorner, bottomEnd = CardCorner)
    }

    val gradient = if (topHalf) {
        listOf(
            cardColor.lighten(0.10f),
            cardColor.lighten(0.04f),
            cardColor,
        )
    } else {
        listOf(
            cardColor.darken(0.02f),
            cardColor.darken(0.08f),
            cardColor.darken(0.18f),
        )
    }

    Box(
        modifier = modifier
            .requiredWidth(cardWidth)
            .requiredHeight(half)
            .clip(shape)
            .background(Brush.verticalGradient(gradient)),
    ) {
        // requiredHeight, not height: the parent only offers half a card of
        // space, and plain height() would be coerced down and clip the digit away.
        Box(
            modifier = Modifier
                .requiredWidth(cardWidth)
                .requiredHeight(cardHeight)
                .align(if (topHalf) Alignment.TopCenter else Alignment.BottomCenter),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = digit,
                fontSize = digitSize,
                fontWeight = FontWeight.Black,
                color = digitColor,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun Colon(color: Color, size: Dp) {
    val dot = (size.value * 0.085f).dp
    Column(
        modifier = Modifier.width(18.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(dot).background(color, RoundedCornerShape(50)))
        Spacer(modifier = Modifier.height(size * 0.16f))
        Box(modifier = Modifier.size(dot).background(color, RoundedCornerShape(50)))
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

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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val FlipMillis = 430
private val CardCorner = 14.dp

private val TopGradient = listOf(Color(0xFF2B2C31), Color(0xFF1D1E23))
private val BottomGradient = listOf(Color(0xFF17181C), Color(0xFF0D0E11))

/**
 * Split-flap clock.
 *
 * A card is two clipped halves. Each half lays out the digit at the FULL card
 * height (requiredHeight, which ignores the parent's max constraint) and then
 * clips to its own half, so the glyph is cut exactly at the hinge.
 *
 * The font size is divided by the system fontScale, so a large accessibility
 * font setting cannot make the digits grow out of their cards.
 */
@Composable
fun FlipClock(
    time: String,
    modifier: Modifier = Modifier,
) {
    val parts = time.split(":")
    val hours = parts.getOrNull(0) ?: "--"
    val minutes = parts.getOrNull(1) ?: "--"

    BoxWithConstraints(modifier = modifier) {
        val gap = 4.dp
        val colonWidth = 16.dp
        val available = maxWidth - colonWidth - gap * 8f
        val cardWidth = (available / 4f).coerceIn(40.dp, 66.dp)
        val cardHeight = cardWidth * 1.34f
        val fontScale = LocalDensity.current.fontScale
        val digitSize = ((cardHeight.value * 1.55f) / fontScale).sp

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            FlipCard(hours.getOrNull(0)?.toString() ?: "-", cardWidth, cardHeight, digitSize, gap)
            FlipCard(hours.getOrNull(1)?.toString() ?: "-", cardWidth, cardHeight, digitSize, gap)
            Colon(cardHeight)
            FlipCard(minutes.getOrNull(0)?.toString() ?: "-", cardWidth, cardHeight, digitSize, gap)
            FlipCard(minutes.getOrNull(1)?.toString() ?: "-", cardWidth, cardHeight, digitSize, gap)
        }
    }
}

@Composable
private fun FlipCard(
    digit: String,
    width: Dp,
    height: Dp,
    digitSize: androidx.compose.ui.unit.TextUnit,
    gap: Dp,
) {
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
    val half = height / 2f
    val shape = RoundedCornerShape(CardCorner)

    Box(
        modifier = Modifier
            .padding(horizontal = gap)
            .width(width)
            .height(height)
            .clipToBounds()
            .background(Brush.verticalGradient(BottomGradient), shape),
    ) {
        // Incoming top half sits behind, revealed as the old flap falls.
        HalfFace(
            digit = shown,
            top = true,
            width = width,
            height = height,
            half = half,
            digitSize = digitSize,
            alpha = 1f,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // Bottom half: old digit until the new flap lands.
        HalfFace(
            digit = if (p < 0.5f) previous else shown,
            top = false,
            width = width,
            height = height,
            half = half,
            digitSize = digitSize,
            alpha = 1f,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (p > 0f && p < 1f) {
            if (p < 0.5f) {
                HalfFace(
                    digit = previous,
                    top = true,
                    width = width,
                    height = height,
                    half = half,
                    digitSize = digitSize,
                    alpha = 1f,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0.5f, 1f)
                            rotationX = -p * 180f
                            cameraDistance = 20f * density
                        },
                )
            } else {
                HalfFace(
                    digit = shown,
                    top = false,
                    width = width,
                    height = height,
                    half = half,
                    digitSize = digitSize,
                    alpha = 1f,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0.5f, 0f)
                            rotationX = (1f - p) * 180f
                            cameraDistance = 20f * density
                        },
                )
            }
        }

        // Hinge seam.
        Column(modifier = Modifier.align(Alignment.Center)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(Color.Black.copy(alpha = 0.7f)),
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

@Composable
private fun HalfFace(
    digit: String,
    top: Boolean,
    width: Dp,
    height: Dp,
    half: Dp,
    digitSize: androidx.compose.ui.unit.TextUnit,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(width)
            .height(half)
            .clipToBounds()
            .background(
                Brush.verticalGradient(if (top) TopGradient else BottomGradient),
            ),
    ) {
        Box(
            modifier = Modifier
                .requiredHeight(height)
                .width(width)
                .align(if (top) Alignment.TopCenter else Alignment.BottomCenter),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = digit,
                fontSize = digitSize,
                fontWeight = FontWeight.Black,
                color = Color(0xFFF8F8FA).copy(alpha = alpha),
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun Colon(cardHeight: Dp) {
    val dot = (cardHeight.value * 0.085f).dp
    Column(
        modifier = Modifier.width(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(dot).background(Color(0xFFF8F8FA), RoundedCornerShape(50)))
        Spacer(modifier = Modifier.height(cardHeight * 0.16f))
        Box(modifier = Modifier.size(dot).background(Color(0xFFF8F8FA), RoundedCornerShape(50)))
    }
}

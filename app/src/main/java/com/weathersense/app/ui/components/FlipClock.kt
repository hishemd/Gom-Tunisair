package com.weathersense.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val FlipMillis = 430
private val CardColor = Color(0xFF17181C)
private val DigitColor = Color(0xFFF8F8FA)

/**
 * Split-flap clock drawn entirely on a Canvas with a TextMeasurer.
 *
 * Drawing the digits directly (rather than laying out a Text that overflows its
 * half) keeps the halves exactly aligned at the hinge whatever the system font
 * scale is: the glyph is measured once and then painted twice through clipRect.
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
        val cardWidth = (available / 4f).coerceIn(42.dp, 74.dp)
        val cardHeight = cardWidth * 1.42f

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            FlipCard(hours.getOrNull(0)?.toString() ?: "-", cardWidth, cardHeight)
            FlipCard(hours.getOrNull(1)?.toString() ?: "-", cardWidth, cardHeight)
            Colon(cardHeight)
            FlipCard(minutes.getOrNull(0)?.toString() ?: "-", cardWidth, cardHeight)
            FlipCard(minutes.getOrNull(1)?.toString() ?: "-", cardWidth, cardHeight)
        }
    }
}

@Composable
private fun FlipCard(digit: String, width: Dp, height: Dp) {
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

    val measurer = rememberTextMeasurer(cacheSize = 12)
    val density = LocalDensity.current
    val fontScale = density.fontScale

    // Font size expressed in card units and divided by fontScale, so a large
    // system font setting cannot blow the digits out of their cards.
    val fontSize = ((height.value * 0.98f) / fontScale).sp
    val style = TextStyle(
        fontSize = fontSize,
        fontWeight = FontWeight.Black,
        color = DigitColor,
    )

    val shownLayout = measurer.measure(AnnotatedString(shown), style, maxLines = 1, softWrap = false)
    val previousLayout = measurer.measure(AnnotatedString(previous), style, maxLines = 1, softWrap = false)

    val p = progress.value

    Canvas(modifier = Modifier.size(width, height)) {
        val w = size.width
        val h = size.height
        val half = h / 2f
        val radius = 14.dp.toPx()

        // Card background: top half catches the light, bottom half falls away.
        val cardPath = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = 0f,
                    top = 0f,
                    right = w,
                    bottom = h,
                    cornerRadius = CornerRadius(radius, radius),
                ),
            )
        }

        clipPath(cardPath) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF26272C), Color(0xFF1B1C21)),
                    startY = 0f,
                    endY = half,
                ),
                size = Size(w, half),
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF141519), Color(0xFF0C0D10)),
                    startY = half,
                    endY = h,
                ),
                topLeft = Offset(0f, half),
                size = Size(w, half),
            )
        }

        // Hinge: dark seam plus a faint highlight underneath.
        drawLine(
            color = Color.Black.copy(alpha = 0.75f),
            start = Offset(0f, half),
            end = Offset(w, half),
            strokeWidth = 2.dp.toPx(),
        )
        drawLine(
            color = Color.White.copy(alpha = 0.05f),
            start = Offset(0f, half + 1.dp.toPx()),
            end = Offset(w, half + 1.dp.toPx()),
            strokeWidth = 1.dp.toPx(),
        )

        val hinge = Offset(w / 2f, half)

        if (p <= 0f) {
            drawHalf(shownLayout, w, h, half, top = true)
            drawHalf(shownLayout, w, h, half, top = false)
        } else if (p < 0.5f) {
            // Incoming top half sits behind, the outgoing one tips forward.
            drawHalf(shownLayout, w, h, half, top = true)
            scale(
                scaleX = 1f,
                scaleY = (1f - p * 2f).coerceAtLeast(0.001f),
                pivot = hinge,
            ) {
                drawHalf(previousLayout, w, h, half, top = true)
            }
            drawHalf(previousLayout, w, h, half, top = false)
        } else {
            drawHalf(shownLayout, w, h, half, top = true)
            drawHalf(previousLayout, w, h, half, top = false)
            scale(
                scaleX = 1f,
                scaleY = ((p - 0.5f) * 2f).coerceAtLeast(0.001f),
                pivot = hinge,
            ) {
                drawHalf(shownLayout, w, h, half, top = false)
            }
        }

        // Soft shading on whichever half is moving, for a sense of depth.
        val movingAlpha = when {
            p <= 0f || p >= 1f -> 0f
            else -> 0.30f * (1f - kotlin.math.abs(p * 2f - 1f))
        }
        if (movingAlpha > 0.01f) {
            val movingTop = p < 0.5f
            drawRect(
                color = Color.Black.copy(alpha = movingAlpha * 0.6f),
                topLeft = Offset(0f, if (movingTop) 0f else half),
                size = Size(w, half),
            )
        }
    }
}

/** Paints one measured glyph through a clip window covering half the card. */
private fun DrawScope.drawHalf(
    layout: TextLayoutResult,
    width: Float,
    height: Float,
    half: Float,
    top: Boolean,
) {
    val left = (width - layout.size.width) / 2f
    val topOffset = (height - layout.size.height) / 2f
    if (top) {
        clipRect(left = 0f, top = 0f, right = width, bottom = half) {
            drawText(layout, topLeft = Offset(left, topOffset))
        }
    } else {
        clipRect(left = 0f, top = half, right = width, bottom = height) {
            drawText(layout, topLeft = Offset(left, topOffset))
        }
    }
}

@Composable
private fun Colon(cardHeight: Dp) {
    val dot = (cardHeight.value * 0.085f).dp
    Canvas(modifier = Modifier.size(width = 16.dp, height = cardHeight)) {
        val cx = size.width / 2f
        val gap = size.height * 0.16f
        drawCircle(color = DigitColor, radius = dot.toPx() / 2f, center = Offset(cx, size.height / 2f - gap))
        drawCircle(color = DigitColor, radius = dot.toPx() / 2f, center = Offset(cx, size.height / 2f + gap))
    }
}

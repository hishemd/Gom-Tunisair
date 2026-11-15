package com.weathersense.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** Weather families that get their own animated backdrop. */
enum class WeatherScene {
    CLEAR_DAY, CLEAR_NIGHT, CLOUDY, RAIN, THUNDER, SNOW, FOG
}

fun sceneFor(iconCode: String): WeatherScene = when {
    iconCode.startsWith("01") && iconCode.endsWith("n") -> WeatherScene.CLEAR_NIGHT
    iconCode.startsWith("01") -> WeatherScene.CLEAR_DAY
    iconCode.startsWith("09") || iconCode.startsWith("10") -> WeatherScene.RAIN
    iconCode.startsWith("11") -> WeatherScene.THUNDER
    iconCode.startsWith("13") -> WeatherScene.SNOW
    iconCode.startsWith("50") -> WeatherScene.FOG
    else -> WeatherScene.CLOUDY
}

private data class ScenePalette(val top: Color, val bottom: Color, val accent: Color)

private fun paletteFor(scene: WeatherScene, dark: Boolean): ScenePalette = when (scene) {
    WeatherScene.CLEAR_DAY -> if (dark) {
        ScenePalette(Color(0xFF08203F), Color(0xFF2C6EA6), Color(0xFFFFD54F))
    } else {
        ScenePalette(Color(0xFF4FA3E3), Color(0xFFCFE8F8), Color(0xFFFFC107))
    }

    WeatherScene.CLEAR_NIGHT -> if (dark) {
        ScenePalette(Color(0xFF030818), Color(0xFF141F45), Color(0xFFBFC7EC))
    } else {
        ScenePalette(Color(0xFF5A6A96), Color(0xFFC9D2E6), Color(0xFF6E7FBF))
    }

    WeatherScene.CLOUDY -> if (dark) {
        ScenePalette(Color(0xFF1E2832), Color(0xFF4E5C68), Color(0xFFB0BEC5))
    } else {
        ScenePalette(Color(0xFF93A7B7), Color(0xFFDFE7EE), Color(0xFF7E93A0))
    }

    WeatherScene.RAIN -> if (dark) {
        ScenePalette(Color(0xFF0A1620), Color(0xFF274152), Color(0xFF4FC3F7))
    } else {
        ScenePalette(Color(0xFF638199), Color(0xFFCFDDE8), Color(0xFF1E88E5))
    }

    WeatherScene.THUNDER -> if (dark) {
        ScenePalette(Color(0xFF0C0A14), Color(0xFF272139), Color(0xFFB39DDB))
    } else {
        ScenePalette(Color(0xFF5F597C), Color(0xFFCFCADE), Color(0xFF7E57C2))
    }

    WeatherScene.SNOW -> if (dark) {
        ScenePalette(Color(0xFF13212B), Color(0xFF3F5C70), Color(0xFFE1F5FE))
    } else {
        ScenePalette(Color(0xFFA6C0D1), Color(0xFFEAF3F9), Color(0xFF4FC3F7))
    }

    WeatherScene.FOG -> if (dark) {
        ScenePalette(Color(0xFF22262A), Color(0xFF474D53), Color(0xFFCFD8DC))
    } else {
        ScenePalette(Color(0xFFAEB5BA), Color(0xFFE9ECEF), Color(0xFF8A959C))
    }
}

private class Drop(val x: Float, val y: Float, val speed: Float, val length: Float, val alpha: Float)
private class Flake(val x: Float, val y: Float, val radius: Float, val sway: Float, val alpha: Float)
private class Cloud(val x: Float, val y: Float, val radius: Float, val alpha: Float, val speed: Float)
private class Star(val x: Float, val y: Float, val radius: Float, val phase: Float)

/**
 * Full-bleed animated backdrop for the weather screen. Every shape is drawn
 * with Canvas primitives, so there are no image assets and nothing to download.
 */
@Composable
fun WeatherBackground(
    iconCode: String,
    modifier: Modifier = Modifier,
) {
    val scene = sceneFor(iconCode)
    val dark = isSystemInDarkTheme()
    val palette = paletteFor(scene, dark)

    val transition = rememberInfiniteTransition(label = "weatherBg")

    // Sun ray rotation and slow haze drift.
    val slow by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "slow",
    )

    // Cloud drift — fast enough to notice without feeling busy.
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "drift",
    )

    // Snowfall and star twinkle.
    val mid by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "mid",
    )

    // Rain.
    val fast by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "fast",
    )

    // Lightning.
    val flash by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7_000
                0f at 0
                0f at 6_200
                0.85f at 6_280
                0.10f at 6_360
                0.60f at 6_440
                0f at 6_560
                0f at 7_000
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "flash",
    )

    // A shooting star every ~11 s on clear nights.
    val meteor by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 11_000
                0f at 0
                0f at 7_000
                1f at 8_600
                1f at 11_000
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "meteor",
    )

    val drops = remember {
        val rnd = Random(7)
        List(64) {
            Drop(
                x = rnd.nextFloat(),
                y = rnd.nextFloat(),
                speed = 0.55f + rnd.nextFloat() * 0.75f,
                length = 14f + rnd.nextFloat() * 22f,
                alpha = 0.18f + rnd.nextFloat() * 0.42f,
            )
        }
    }

    val flakes = remember {
        val rnd = Random(11)
        List(54) {
            Flake(
                x = rnd.nextFloat(),
                y = rnd.nextFloat(),
                radius = 1.6f + rnd.nextFloat() * 3.4f,
                sway = rnd.nextFloat() * 6.28f,
                alpha = 0.30f + rnd.nextFloat() * 0.55f,
            )
        }
    }

    val clouds = remember {
        val rnd = Random(23)
        List(5) {
            Cloud(
                x = rnd.nextFloat(),
                y = 0.10f + rnd.nextFloat() * 0.34f,
                radius = 0.22f + rnd.nextFloat() * 0.24f,
                alpha = 0.10f + rnd.nextFloat() * 0.16f,
                speed = 0.30f + rnd.nextFloat() * 0.50f,
            )
        }
    }

    val wispy = remember {
        val rnd = Random(41)
        List(3) {
            Cloud(
                x = rnd.nextFloat(),
                y = 0.16f + rnd.nextFloat() * 0.40f,
                radius = 0.30f + rnd.nextFloat() * 0.20f,
                alpha = 0.10f + rnd.nextFloat() * 0.08f,
                speed = 0.20f + rnd.nextFloat() * 0.30f,
            )
        }
    }

    val stars = remember {
        val rnd = Random(31)
        List(60) {
            Star(
                x = rnd.nextFloat(),
                y = rnd.nextFloat() * 0.66f,
                radius = 1.3f + rnd.nextFloat() * 3.2f,
                phase = rnd.nextFloat() * 6.28f,
            )
        }
    }

    val scrim = if (dark) 0.16f else 0.20f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(palette.top, palette.bottom))),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val unit = min(w, h)

            when (scene) {
                WeatherScene.CLEAR_DAY -> {
                    drawSun(unit, w, palette.accent, slow)
                    drawClouds(wispy, unit, w, h, drift, 0.9f)
                }

                WeatherScene.CLEAR_NIGHT -> {
                    drawNight(w, h, unit, stars, mid, palette, dark)
                    drawClouds(wispy, unit, w, h, drift, 1f)
                    drawMeteor(meteor, w, h)
                }

                WeatherScene.CLOUDY -> {
                    drawSun(unit, w, palette.accent, slow, softened = true)
                    drawClouds(clouds, unit, w, h, drift, 0.95f)
                }

                WeatherScene.RAIN -> {
                    drawClouds(clouds, unit, w, h, drift, 1.05f)
                    drawRain(drops, w, h, fast)
                }

                WeatherScene.THUNDER -> {
                    drawClouds(clouds, unit, w, h, drift, 1.15f)
                    drawRain(drops, w, h, fast)
                    if (flash > 0.01f) {
                        drawRect(Color.White.copy(alpha = flash * 0.30f))
                    }
                }

                WeatherScene.SNOW -> {
                    drawClouds(clouds, unit, w, h, drift, 0.75f)
                    drawSnow(flakes, w, h, mid)
                }

                WeatherScene.FOG -> drawFog(w, h, slow, palette.accent)
            }
        }

        // Keeps text readable over every palette without muting the motion.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = scrim)),
        )
    }
}

private fun DrawScope.drawSun(unit: Float, width: Float, accent: Color, phase: Float, softened: Boolean = false) {
    val center = Offset(width * 0.82f, unit * 0.13f)
    val baseRadius = unit * if (softened) 0.13f else 0.15f
    val pulse = 1f + 0.05f * sin(phase * 2f * PI.toFloat())

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(accent.copy(alpha = if (softened) 0.20f else 0.32f), Color.Transparent),
            center = center,
            radius = baseRadius * 3.0f * pulse,
        ),
        radius = baseRadius * 3.0f * pulse,
        center = center,
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.70f), accent.copy(alpha = 0.45f)),
            center = center,
            radius = baseRadius * pulse,
        ),
        radius = baseRadius * pulse,
        center = center,
    )

    if (!softened) {
        rotate(degrees = phase * 360f, pivot = center) {
            for (i in 0 until 10) {
                val angle = (i * 36.0) * PI / 180.0
                val c = cos(angle).toFloat()
                val s = sin(angle).toFloat()
                drawLine(
                    color = accent.copy(alpha = 0.40f),
                    start = Offset(center.x + c * baseRadius * 1.35f, center.y + s * baseRadius * 1.35f),
                    end = Offset(center.x + c * baseRadius * 1.75f, center.y + s * baseRadius * 1.75f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

private fun DrawScope.drawNight(
    width: Float,
    height: Float,
    unit: Float,
    stars: List<Star>,
    phase: Float,
    palette: ScenePalette,
    dark: Boolean,
) {
    // Twinkling stars — strong enough to be obvious in both themes.
    stars.forEach { star ->
        val twinkle = 0.25f + 0.75f * ((sin(phase * 2f * PI.toFloat() + star.phase) + 1f) / 2f)
        drawCircle(
            color = Color.White.copy(alpha = (if (dark) 0.90f else 0.80f) * twinkle),
            radius = star.radius,
            center = Offset(star.x * width, star.y * height),
        )
    }

    // Moon: small, high in the corner, with a soft halo instead of a hard disc.
    val moon = Offset(width * 0.86f, unit * 0.10f)
    val moonRadius = unit * 0.042f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
            center = moon,
            radius = moonRadius * 3.4f,
        ),
        radius = moonRadius * 3.4f,
        center = moon,
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFDFCF4).copy(alpha = 0.95f),
                Color(0xFFE8E4D2).copy(alpha = 0.70f),
            ),
            center = moon,
            radius = moonRadius,
        ),
        radius = moonRadius,
        center = moon,
    )
}

private fun DrawScope.drawMeteor(progress: Float, width: Float, height: Float) {
    // Only visible during the short streak window.
    if (progress <= 0f || progress >= 1f) return
    val travel = progress
    if (travel < 0.001f) return

    val headX = width * (0.12f + 0.62f * travel)
    val headY = height * (0.06f + 0.20f * travel)
    val tailLength = width * 0.13f

    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.85f)),
            start = Offset(headX - tailLength, headY - tailLength * 0.32f),
            end = Offset(headX, headY),
        ),
        start = Offset(headX - tailLength, headY - tailLength * 0.32f),
        end = Offset(headX, headY),
        strokeWidth = 2.4f,
        cap = StrokeCap.Round,
    )
    drawCircle(color = Color.White.copy(alpha = 0.9f), radius = 2.6f, center = Offset(headX, headY))
}

private fun DrawScope.drawClouds(
    clouds: List<Cloud>,
    unit: Float,
    width: Float,
    height: Float,
    phase: Float,
    density: Float,
) {
    clouds.forEach { cloud ->
        val travel = (cloud.x + phase * cloud.speed) % 1.4f
        val cx = (travel - 0.2f) * width
        val cy = cloud.y * height
        val r = cloud.radius * unit
        val alpha = cloud.alpha * density

        drawSoftBlob(Offset(cx, cy), r, Color.White.copy(alpha = alpha))
        drawSoftBlob(Offset(cx - r * 0.72f, cy + r * 0.20f), r * 0.66f, Color.White.copy(alpha = alpha * 0.85f))
        drawSoftBlob(Offset(cx + r * 0.78f, cy + r * 0.16f), r * 0.72f, Color.White.copy(alpha = alpha * 0.85f))
    }
}

private fun DrawScope.drawSoftBlob(center: Offset, radius: Float, color: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = color.alpha * 0.35f), Color.Transparent),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

private fun DrawScope.drawRain(drops: List<Drop>, width: Float, height: Float, phase: Float) {
    drops.forEach { drop ->
        val y = ((drop.y + phase * drop.speed) % 1f) * height
        val x = drop.x * width
        drawLine(
            color = Color(0xFFBFE7FF).copy(alpha = drop.alpha),
            start = Offset(x, y),
            end = Offset(x - drop.length * 0.28f, y + drop.length),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawSnow(flakes: List<Flake>, width: Float, height: Float, phase: Float) {
    flakes.forEach { flake ->
        val y = ((flake.y + phase * 0.55f) % 1f) * height
        val swayX = sin(phase * 2f * PI.toFloat() + flake.sway) * width * 0.02f
        drawCircle(
            color = Color.White.copy(alpha = flake.alpha),
            radius = flake.radius,
            center = Offset(flake.x * width + swayX, y),
        )
    }
}

private fun DrawScope.drawFog(width: Float, height: Float, phase: Float, accent: Color) {
    for (i in 0 until 5) {
        val y = height * (0.22f + i * 0.15f)
        val shift = sin(phase * 2f * PI.toFloat() + i * 1.1f) * width * 0.10f
        drawSoftBlob(
            center = Offset(width * 0.5f + shift, y),
            radius = width * 0.62f,
            color = accent.copy(alpha = 0.12f),
        )
    }
}

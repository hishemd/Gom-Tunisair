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
    iconCode.startsWith("02") || iconCode.startsWith("03") || iconCode.startsWith("04") -> WeatherScene.CLOUDY
    else -> WeatherScene.CLOUDY
}

private data class ScenePalette(val top: Color, val bottom: Color, val accent: Color)

private fun paletteFor(scene: WeatherScene, dark: Boolean): ScenePalette = when (scene) {
    WeatherScene.CLEAR_DAY -> if (dark) {
        ScenePalette(Color(0xFF0B2A52), Color(0xFF2C6EA6), Color(0xFFFFD54F))
    } else {
        ScenePalette(Color(0xFF5FB2EC), Color(0xFFD6ECFB), Color(0xFFFFC107))
    }

    WeatherScene.CLEAR_NIGHT -> if (dark) {
        ScenePalette(Color(0xFF040A1F), Color(0xFF16224D), Color(0xFFC5CAE9))
    } else {
        ScenePalette(Color(0xFF6E7FA8), Color(0xFFD3DAEA), Color(0xFF8C9BE0))
    }

    WeatherScene.CLOUDY -> if (dark) {
        ScenePalette(Color(0xFF232E38), Color(0xFF54626E), Color(0xFFB0BEC5))
    } else {
        ScenePalette(Color(0xFF9FB1C0), Color(0xFFE3EAF0), Color(0xFF90A4AE))
    }

    WeatherScene.RAIN -> if (dark) {
        ScenePalette(Color(0xFF0D1B25), Color(0xFF2A4557), Color(0xFF4FC3F7))
    } else {
        ScenePalette(Color(0xFF6E8CA3), Color(0xFFD3E0EA), Color(0xFF29B6F6))
    }

    WeatherScene.THUNDER -> if (dark) {
        ScenePalette(Color(0xFF100E1A), Color(0xFF2A2440), Color(0xFFB39DDB))
    } else {
        ScenePalette(Color(0xFF6A6488), Color(0xFFD6D2E6), Color(0xFF7E57C2))
    }

    WeatherScene.SNOW -> if (dark) {
        ScenePalette(Color(0xFF16242F), Color(0xFF456479), Color(0xFFE1F5FE))
    } else {
        ScenePalette(Color(0xFFAFC7D6), Color(0xFFEDF5FA), Color(0xFF90CAF9))
    }

    WeatherScene.FOG -> if (dark) {
        ScenePalette(Color(0xFF262A2E), Color(0xFF4B5157), Color(0xFFCFD8DC))
    } else {
        ScenePalette(Color(0xFFB6BCC1), Color(0xFFECEFF1), Color(0xFF90A4AE))
    }
}

private class Drop(val x: Float, val y: Float, val speed: Float, val length: Float, val alpha: Float)
private class Flake(val x: Float, val y: Float, val radius: Float, val sway: Float, val alpha: Float)
private class Cloud(val x: Float, val y: Float, val radius: Float, val alpha: Float, val speed: Float)
private class Star(val x: Float, val y: Float, val radius: Float, val phase: Float)

/**
 * Full-bleed animated backdrop for the weather screen. Everything is drawn with
 * Canvas primitives, so there are no image assets and nothing to download.
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

    // Slow drift for clouds, rays and haze (30 s per cycle).
    val slow by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "slow",
    )

    // Medium cycle for snowfall and star twinkle (9 s).
    val mid by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "mid",
    )

    // Fast cycle for rain (1.4 s).
    val fast by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "fast",
    )

    // Occasional lightning flash, only used by the thunderstorm scene.
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
                y = 0.10f + rnd.nextFloat() * 0.42f,
                radius = 0.24f + rnd.nextFloat() * 0.26f,
                alpha = 0.10f + rnd.nextFloat() * 0.16f,
                speed = 0.25f + rnd.nextFloat() * 0.45f,
            )
        }
    }

    val stars = remember {
        val rnd = Random(31)
        List(46) {
            Star(
                x = rnd.nextFloat(),
                y = rnd.nextFloat() * 0.62f,
                radius = 0.9f + rnd.nextFloat() * 2.2f,
                phase = rnd.nextFloat() * 6.28f,
            )
        }
    }

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
                WeatherScene.CLEAR_DAY -> drawSun(unit, w, palette.accent, slow)
                WeatherScene.CLEAR_NIGHT -> drawNight(unit, w, h, stars, mid)
                WeatherScene.CLOUDY -> {
                    drawSun(unit, w, palette.accent, slow, softened = true)
                    drawClouds(clouds, unit, w, h, slow, 0.85f)
                }

                WeatherScene.RAIN -> {
                    drawClouds(clouds, unit, w, h, slow, 1f)
                    drawRain(drops, w, h, fast)
                }

                WeatherScene.THUNDER -> {
                    drawClouds(clouds, unit, w, h, slow, 1.15f)
                    drawRain(drops, w, h, fast)
                    if (flash > 0.01f) {
                        drawRect(Color.White.copy(alpha = flash * 0.30f))
                    }
                }

                WeatherScene.SNOW -> {
                    drawClouds(clouds, unit, w, h, slow, 0.7f)
                    drawSnow(flakes, w, h, mid)
                }

                WeatherScene.FOG -> drawFog(w, h, slow, palette.accent)
            }
        }

        // Keeps text readable over every palette without muting the animation.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.30f)),
        )
    }
}

private fun DrawScope.drawSun(
    unit: Float,
    width: Float,
    accent: Color,
    phase: Float,
    softened: Boolean = false,
) {
    val center = Offset(width * 0.80f, unit * 0.20f)
    val baseRadius = unit * if (softened) 0.16f else 0.22f
    val pulse = 1f + 0.04f * sin(phase * 2f * PI.toFloat())

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                accent.copy(alpha = if (softened) 0.22f else 0.34f),
                Color.Transparent,
            ),
            center = center,
            radius = baseRadius * 2.6f * pulse,
        ),
        radius = baseRadius * 2.6f * pulse,
        center = center,
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.55f), accent.copy(alpha = 0.35f)),
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
                    color = accent.copy(alpha = 0.35f),
                    start = Offset(
                        center.x + c * baseRadius * 1.28f,
                        center.y + s * baseRadius * 1.28f,
                    ),
                    end = Offset(
                        center.x + c * baseRadius * 1.62f,
                        center.y + s * baseRadius * 1.62f,
                    ),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

private fun DrawScope.drawNight(
    unit: Float,
    width: Float,
    height: Float,
    stars: List<Star>,
    phase: Float,
) {
    stars.forEach { star ->
        val twinkle = 0.35f + 0.65f * ((sin(phase * 2f * PI.toFloat() + star.phase) + 1f) / 2f)
        drawCircle(
            color = Color.White.copy(alpha = 0.55f * twinkle),
            radius = star.radius,
            center = Offset(star.x * width, star.y * height),
        )
    }

    val moon = Offset(width * 0.78f, unit * 0.18f)
    val moonRadius = unit * 0.11f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
            center = moon,
            radius = moonRadius * 2.8f,
        ),
        radius = moonRadius * 2.8f,
        center = moon,
    )
    drawCircle(color = Color(0xFFF3F1E7).copy(alpha = 0.92f), radius = moonRadius, center = moon)
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

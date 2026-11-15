package com.weathersense.app.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weathersense.app.data.model.ForecastItem
import com.weathersense.app.data.model.WeatherData
import com.weathersense.app.ui.components.FlipClock
import com.weathersense.app.ui.components.WeatherBackground
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WeatherSenseApp(
    weatherData: WeatherData,
    city: String,
    forecast: List<ForecastItem>,
    onRefreshLocation: () -> Unit,
) {
    var currentTime by remember { mutableStateOf(nowTime()) }
    var currentDate by remember { mutableStateOf(nowDate()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = nowTime()
            currentDate = nowDate()
            delay(1000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WeatherBackground(iconCode = weatherData.icon)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = city,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Text(
                text = currentDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )

            Spacer(Modifier.height(24.dp))

            FlipClock(time = currentTime)

            Spacer(Modifier.height(28.dp))

            WeatherIcon(iconCode = weatherData.icon, modifier = Modifier.size(96.dp))

            Text(
                text = weatherData.temperature,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = weatherData.description,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
            )

            Text(
                text = "H ${weatherData.tempMax ?: "--"}   L ${weatherData.tempMin ?: "--"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            )

            Spacer(Modifier.height(20.dp))

            WeatherDetailCard(weatherData)

            Spacer(Modifier.height(24.dp))

            if (forecast.isNotEmpty()) {
                Text(
                    text = "Forecast",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Start,
                )
                forecast.forEach { item ->
                    ForecastRow(item)
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onRefreshLocation) {
                Text("Update my location")
            }
        }
    }
}

@Composable
fun WeatherIcon(iconCode: String, modifier: Modifier = Modifier) {
    val family = iconCode.take(2)
    val isDay = !iconCode.endsWith("n")

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f * 0.82f

        val cloudColor = if (isDay) Color(0xFFB0BEC5) else Color(0xFF8FA0AC)
        val darkCloudColor = if (isDay) Color(0xFF90A4AE) else Color(0xFF78909C)
        val moonColor = Color(0xFFECEFF1)
        val sunColor = Color(0xFFFFD54F)

        when (family) {
            "01" -> {
                if (isDay) {
                    drawSunIcon(center, radius, sunColor)
                } else {
                    drawMoonIcon(center, radius * 0.88f, moonColor)
                    drawStarDust(center, radius, Color(0xFFCFD8DC))
                }
            }

            "02" -> {
                if (isDay) {
                    drawSunIcon(
                        center = Offset(center.x - radius * 0.30f, center.y - radius * 0.34f),
                        radius = radius * 0.52f,
                        color = Color(0xFFFFB74D),
                    )
                } else {
                    drawMoonIcon(
                        center = Offset(center.x - radius * 0.28f, center.y - radius * 0.34f),
                        radius = radius * 0.44f,
                        color = moonColor,
                    )
                }
                drawCloud(
                    center = Offset(center.x + radius * 0.10f, center.y + radius * 0.22f),
                    radius = radius * 0.92f,
                    color = cloudColor,
                )
            }

            "03", "04" -> drawCloud(center, radius, cloudColor)

            "09", "10" -> {
                drawCloud(
                    center = Offset(center.x, center.y - radius * 0.12f),
                    radius = radius * 0.9f,
                    color = darkCloudColor,
                )
                for (i in -1..2) {
                    val x = center.x + i * radius * 0.28f
                    drawLine(
                        color = Color(0xFF4FC3F7),
                        start = Offset(x, center.y + radius * 0.30f),
                        end = Offset(x - radius * 0.06f, center.y + radius * 0.66f),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round,
                    )
                }
            }

            "11" -> {
                drawCloud(
                    center = Offset(center.x, center.y - radius * 0.14f),
                    radius = radius * 0.9f,
                    color = if (isDay) Color(0xFF7E57C2) else Color(0xFF6A5ACD),
                )
                val bolt = Path().apply {
                    moveTo(center.x + radius * 0.08f, center.y - radius * 0.05f)
                    lineTo(center.x - radius * 0.22f, center.y + radius * 0.34f)
                    lineTo(center.x + radius * 0.02f, center.y + radius * 0.34f)
                    lineTo(center.x - radius * 0.18f, center.y + radius * 0.72f)
                    lineTo(center.x + radius * 0.26f, center.y + radius * 0.22f)
                    lineTo(center.x, center.y + radius * 0.22f)
                    close()
                }
                drawPath(bolt, Color(0xFFFFD54F))
            }

            "13" -> {
                drawCloud(
                    center = Offset(center.x, center.y - radius * 0.14f),
                    radius = radius * 0.9f,
                    color = if (isDay) Color(0xFFCFD8DC) else Color(0xFF9AA8B4),
                )
                for (i in 0 until 5) {
                    val angle = Math.toRadians(i * 72.0)
                    val x = center.x + cos(angle).toFloat() * radius * 0.40f
                    val y = center.y + radius * 0.36f + (i % 3) * radius * 0.10f
                    drawCircle(Color.White, radius = 3.5f, center = Offset(x, y))
                }
            }

            "50" -> {
                val fogColor = if (isDay) Color(0xFFCFD8DC) else Color(0xFF9AA8B4)
                for (i in 0 until 4) {
                    val y = center.y + (i - 1.5f) * radius * 0.30f
                    drawLine(
                        color = fogColor,
                        start = Offset(center.x - radius * 0.6f, y),
                        end = Offset(center.x + radius * 0.6f, y),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round,
                    )
                }
            }

            else -> drawCloud(center, radius, cloudColor)
        }
    }
}

/** Sun disc with eight rays. */
private fun DrawScope.drawSunIcon(center: Offset, radius: Float, color: Color) {
    drawCircle(color = color, radius = radius * 0.55f, center = center)
    for (i in 0 until 8) {
        val angle = Math.toRadians(i * 45.0)
        val c = cos(angle).toFloat()
        val s = sin(angle).toFloat()
        drawLine(
            color = color,
            start = Offset(center.x + c * radius * 0.72f, center.y + s * radius * 0.72f),
            end = Offset(center.x + c * radius, center.y + s * radius),
            strokeWidth = 5f,
            cap = StrokeCap.Round,
        )
    }
}

/** Crescent built by subtracting an offset disc from a full one. */
private fun DrawScope.drawMoonIcon(center: Offset, radius: Float, color: Color) {
    val outer = Path().apply { addOval(Rect(center = center, radius = radius)) }
    val bite = Path().apply {
        addOval(
            Rect(
                center = Offset(center.x + radius * 0.62f, center.y - radius * 0.44f),
                radius = radius * 0.92f,
            ),
        )
    }
    val crescent = Path().apply { op(outer, bite, PathOperation.Difference) }
    drawPath(path = crescent, color = color)
}

/** A few faint stars so a clear night reads as a night, not a dim sun. */
private fun DrawScope.drawStarDust(center: Offset, radius: Float, color: Color) {
    val dots = listOf(
        Triple(-0.64f, -0.50f, 0.045f),
        Triple(0.58f, -0.62f, 0.035f),
        Triple(0.68f, 0.28f, 0.040f),
        Triple(-0.56f, 0.54f, 0.030f),
    )
    dots.forEach { (dx, dy, r) ->
        drawCircle(
            color = color,
            radius = radius * r,
            center = Offset(center.x + dx * radius, center.y + dy * radius),
        )
    }
}

private fun DrawScope.drawCloud(center: Offset, radius: Float, color: Color) {
    val c = color.copy(alpha = 0.9f)
    drawCircle(color = c, radius = radius * 0.42f, center = Offset(center.x, center.y))

    drawCircle(
        color = c,
        radius = radius * 0.30f,
        center = Offset(center.x - radius * 0.32f, center.y - radius * 0.16f),
    )

    drawCircle(
        color = c,
        radius = radius * 0.34f,
        center = Offset(center.x + radius * 0.28f, center.y - radius * 0.20f),
    )

    drawCircle(
        color = c,
        radius = radius * 0.28f,
        center = Offset(center.x, center.y - radius * 0.34f),
    )
}

@Composable
fun WeatherDetailCard(weatherData: WeatherData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                DetailItem(Icons.Default.WaterDrop, "Feels like", weatherData.feelsLike)
                DetailItem(Icons.Default.Air, "Humidity", weatherData.humidity)
                DetailItem(Icons.Default.Air, "Wind", weatherData.windSpeed)
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                DetailItem(Icons.Default.WbSunny, "Sunrise", weatherData.sunrise ?: "--")
                DetailItem(Icons.Default.NightsStay, "Sunset", weatherData.sunset ?: "--")
                DetailItem(Icons.Default.Air, "UV", weatherData.uvIndex ?: "--")
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun ForecastRow(forecast: ForecastItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = forecast.day,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(78.dp),
            )
            WeatherIcon(iconCode = forecast.icon, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = forecast.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = forecast.tempMax,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = " / ${forecast.tempMin}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
        }
    }
}

private fun nowTime(): String =
    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))

private fun nowDate(): String =
    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()))

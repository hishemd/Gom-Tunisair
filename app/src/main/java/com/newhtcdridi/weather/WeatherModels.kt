package com.newhtcdridi.weather

import java.util.Locale

/** A place shown on the main screen or picked from the city list. */
data class City(
    val name: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String?,
) {
    /** Full "Name, Region, Country" label; falls back to the name when nothing else is known. */
    fun label(): String {
        val parts = listOf(name, region, country).filter { it.isNotBlank() }
        return parts.joinToString(", ")
    }

    /** Stable cache key for this city, derived from its coordinates. */
    val key: String
        get() = Units.cityKey(latitude, longitude)
}

/** Weather conditions used to pick an icon. */
enum class Condition {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    THUNDER;

    companion object {
        /** Maps a WMO weather code to a small set of icon conditions. */
        fun fromCode(code: Int): Condition = when (code) {
            0, 1 -> CLEAR
            2 -> PARTLY_CLOUDY
            3 -> CLOUDY
            45, 48 -> FOG
            51, 53, 55, 56, 57 -> DRIZZLE
            61, 63, 65, 66, 67, 80, 81, 82 -> RAIN
            71, 73, 75, 77, 85, 86 -> SNOW
            95, 96, 99 -> THUNDER
            else -> CLOUDY
        }
    }
}

/** Current conditions. */
data class CurrentWeather(
    val temperatureC: Double,
    val apparentC: Double,
    val humidity: Int,
    val windKmh: Double,
    val code: Int,
    val isDay: Boolean,
)

/** One day in the 5-day forecast. */
data class DailyForecast(
    val dateIso: String,
    val code: Int,
    val maxC: Double,
    val minC: Double,
)

/** Everything the UI needs to render the screen. */
data class WeatherReport(
    val city: City,
    val timezoneId: String,
    val current: CurrentWeather,
    val daily: List<DailyForecast>,
    val fetchedAtMillis: Long,
)

/** Unit conversion helpers shared by the UI and the parser. */
object Units {
    /** Celsius to Fahrenheit, rounded to a whole degree. */
    fun cToF(celsius: Double): Int = Math.round(celsius * 9.0 / 5.0 + 32.0).toInt()

    /** Format a Celsius temperature for display, respecting the user's unit choice. */
    fun formatTemp(celsius: Double, fahrenheit: Boolean): String =
        if (fahrenheit) "${cToF(celsius)}°F" else "${Math.round(celsius).toInt()}°C"

    /** A stable cache key for a city (coordinates rounded to ~1 m so tiny GPS jitter doesn't matter). */
    fun cityKey(latitude: Double, longitude: Double): String =
        String.format(Locale.US, "%.4f,%.4f", latitude, longitude)
}

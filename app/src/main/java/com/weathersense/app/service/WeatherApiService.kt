package com.weathersense.app.service

import android.util.Log
import com.weathersense.app.data.model.ForecastItem
import com.weathersense.app.data.model.WeatherData
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Open-Meteo client. No API key required, so the app works out of the box.
 * All calls block — invoke them from a background dispatcher.
 */
object WeatherApiService {

    private const val TAG = "WeatherApiService"
    private const val TIMEOUT_MS = 12_000

    fun fetchWeather(lat: Double, lon: Double, city: String): WeatherData? = try {
        val json = httpGet(forecastUrl(lat, lon))
        parseWeather(json, city)
    } catch (e: Exception) {
        Log.e(TAG, "fetchWeather failed", e)
        null
    }

    fun fetchForecast(lat: Double, lon: Double): List<ForecastItem> = try {
        val json = httpGet(forecastUrl(lat, lon))
        parseForecast(json)
    } catch (e: Exception) {
        Log.e(TAG, "fetchForecast failed", e)
        emptyList()
    }

    /** One request serves both current conditions and the daily forecast. */
    private fun forecastUrl(lat: Double, lon: Double): String =
        "https://api.open-meteo.com/v1/forecast" +
            "?latitude=${coord(lat)}&longitude=${coord(lon)}" +
            "&current=temperature_2m,relative_humidity_2m,apparent_temperature," +
            "is_day,weather_code,wind_speed_10m,surface_pressure" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min," +
            "sunrise,sunset,uv_index_max" +
            "&forecast_days=6&timezone=auto"

    private fun coord(v: Double) = String.format(Locale.US, "%.4f", v)

    private fun httpGet(urlString: String): String {
        val connection = URL(urlString).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "WeatherSense/1.0 (Android)")
            val status = connection.responseCode
            if (status !in 200..299) throw IOException("HTTP $status")
            return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseWeather(json: String, city: String): WeatherData {
        val root = JSONObject(json)
        val current = root.getJSONObject("current")
        val daily = root.optJSONObject("daily")

        return WeatherData(
            temperature = "${round(current.getDouble("temperature_2m"))}°",
            feelsLike = "${round(current.getDouble("apparent_temperature"))}°",
            humidity = "${current.getInt("relative_humidity_2m")}%",
            windSpeed = "${round(current.getDouble("wind_speed_10m"))} km/h",
            description = describe(current.getInt("weather_code")),
            icon = iconCode(current.getInt("weather_code"), current.optInt("is_day", 1) == 1),
            city = city,
            tempMax = daily?.let { "${round(it.getJSONArray("temperature_2m_max").getDouble(0))}°" },
            tempMin = daily?.let { "${round(it.getJSONArray("temperature_2m_min").getDouble(0))}°" },
            sunrise = daily?.let { timeOfDay(it.getString("sunrise"), 0) },
            sunset = daily?.let { timeOfDay(it.getString("sunset"), 0) },
            pressure = "${round(current.optDouble("surface_pressure", 0.0))} hPa",
            visibility = null,
            uvIndex = daily?.let { "${round(it.getJSONArray("uv_index_max").getDouble(0))}" },
        )
    }

    private fun parseForecast(json: String): List<ForecastItem> {
        val daily = JSONObject(json).getJSONObject("daily")
        val codes = daily.getJSONArray("weather_code")
        val maxes = daily.getJSONArray("temperature_2m_max")
        val mins = daily.getJSONArray("temperature_2m_min")
        val dates = daily.getJSONArray("time")

        return (0 until minOf(codes.length(), 6)).map { i ->
            ForecastItem(
                day = dayLabel(dates.getString(i), i),
                tempMax = "${round(maxes.getDouble(i))}°",
                tempMin = "${round(mins.getDouble(i))}°",
                icon = iconCode(codes.getInt(i), isDay = true),
                description = describe(codes.getInt(i)),
            )
        }
    }

    private fun round(value: Double): Int = Math.round(value).toInt()

    private fun dayLabel(isoDate: String, index: Int): String {
        if (index == 0) return "Today"
        return try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate) ?: return isoDate
            SimpleDateFormat("EEE", Locale.getDefault()).format(parsed)
        } catch (e: Exception) {
            isoDate
        }
    }

    private fun timeOfDay(iso: String, index: Int): String =
        iso.substringAfter("T").take(5).ifEmpty { "--" }

    /** WMO weather interpretation codes -> English text. */
    fun describe(code: Int): String = when (code) {
        0 -> "Clear sky"
        1 -> "Mainly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        56, 57 -> "Freezing drizzle"
        61, 63, 65 -> "Rain"
        66, 67 -> "Freezing rain"
        71, 73, 75 -> "Snowfall"
        77 -> "Snow grains"
        80, 81, 82 -> "Rain showers"
        85, 86 -> "Snow showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with hail"
        else -> "Unknown"
    }

    /** Maps WMO codes onto the icon scheme the UI draws (01/02/03/09/10/11/13/50). */
    fun iconCode(code: Int, isDay: Boolean): String {
        val suffix = if (isDay) "d" else "n"
        return when (code) {
            0, 1 -> "01$suffix"
            2 -> "02$suffix"
            3 -> "04$suffix"
            45, 48 -> "50$suffix"
            51, 53, 55, 56, 57 -> "09$suffix"
            61, 63, 65, 66, 67, 80, 81, 82 -> "10$suffix"
            71, 73, 75, 77, 85, 86 -> "13$suffix"
            95, 96, 99 -> "11$suffix"
            else -> "03$suffix"
        }
    }
}

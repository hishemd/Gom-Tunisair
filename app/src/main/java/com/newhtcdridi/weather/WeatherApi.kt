package com.newhtcdridi.weather

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import org.json.JSONException

/** Raw JSON plus the parsed report (the raw text is what we cache on disk). */
data class FetchResult(val raw: String, val report: WeatherReport)

/**
 * Talks to the free Open-Meteo APIs (no API key needed).
 * All methods block: call them from a background thread.
 */
class WeatherApi {

    @Throws(IOException::class, JSONException::class)
    fun fetchForecast(city: City): FetchResult {
        val raw = httpGet(forecastUrl(city))
        val report = WeatherParser.parseForecast(raw, city, System.currentTimeMillis())
        return FetchResult(raw, report)
    }

    @Throws(IOException::class, JSONException::class)
    fun searchCities(query: String, language: String): List<City> =
        WeatherParser.parseGeocoding(httpGet(geocodingUrl(query, language)))

    private fun httpGet(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", USER_AGENT)
            val status = connection.responseCode
            if (status !in 200..299) throw IOException("HTTP $status")
            return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val TIMEOUT_MS = 10_000
        private const val USER_AGENT = "NewhtcDRIDI/1.1 (Android weather app)"

        fun forecastUrl(city: City): String =
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=${coordinate(city.latitude)}" +
                "&longitude=${coordinate(city.longitude)}" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature," +
                "is_day,weather_code,wind_speed_10m" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                "&forecast_days=5&timezone=auto"

        fun geocodingUrl(query: String, language: String): String =
            "https://geocoding-api.open-meteo.com/v1/search" +
                "?name=${URLEncoder.encode(query, "UTF-8")}" +
                "&count=8&format=json" +
                "&language=${URLEncoder.encode(language, "UTF-8")}"

        private fun coordinate(value: Double): String = String.format(Locale.US, "%.4f", value)
    }
}

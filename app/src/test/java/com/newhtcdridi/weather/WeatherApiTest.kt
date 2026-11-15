package com.newhtcdridi.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherApiTest {

    private val tunis = City("Tunis", "", "Tunisia", 36.8065, 10.1815, "Africa/Tunis")

    @Test
    fun forecastUrlContainsCoordinatesAndVariables() {
        val url = WeatherApi.forecastUrl(tunis)
        assertTrue(url.startsWith("https://api.open-meteo.com/v1/forecast?"))
        assertTrue(url.contains("latitude=36.8065"))
        assertTrue(url.contains("longitude=10.1815"))
        assertTrue(url.contains("current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,wind_speed_10m"))
        assertTrue(url.contains("daily=weather_code,temperature_2m_max,temperature_2m_min"))
        assertTrue(url.contains("forecast_days=5"))
        assertTrue(url.contains("timezone=auto"))
    }

    @Test
    fun geocodingUrlEncodesTheQuery() {
        val url = WeatherApi.geocodingUrl("Sidi Bou Said", "ar")
        assertTrue(url.startsWith("https://geocoding-api.open-meteo.com/v1/search?"))
        assertTrue(url.contains("name=Sidi+Bou+Said"))
        assertTrue(url.contains("language=ar"))
    }

    @Test
    fun cityKeyIgnoresTinyDifferences() {
        val a = City("A", "", "", 36.8061, 10.1801, null)
        val b = City("B", "", "", 36.8059, 10.1799, null)
        assertEquals(a.key, b.key)
    }
}

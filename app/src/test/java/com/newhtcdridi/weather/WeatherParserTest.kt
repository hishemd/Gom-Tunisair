package com.newhtcdridi.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherParserTest {

    private val tunis = City("Tunis", "", "Tunisia", 36.8065, 10.1815, "Africa/Tunis")

    private val forecastJson = """
        {
          "latitude": 36.82, "longitude": 10.16, "utc_offset_seconds": 3600,
          "timezone": "Africa/Tunis", "timezone_abbreviation": "GMT+1",
          "current": {
            "time": "2026-11-14T22:30", "interval": 900,
            "temperature_2m": 18.3, "relative_humidity_2m": 71,
            "apparent_temperature": 17.9, "is_day": 0,
            "weather_code": 2, "wind_speed_10m": 9.4
          },
          "daily": {
            "time": ["2026-11-14", "2026-11-15", "2026-11-16", "2026-11-17", "2026-11-18"],
            "weather_code": [2, 61, 63, 3, 0],
            "temperature_2m_max": [22.1, 20.4, 19.8, 21.0, 23.5],
            "temperature_2m_min": [14.2, 13.9, 12.5, 11.8, 12.0]
          }
        }
    """.trimIndent()

    @Test
    fun parsesCurrentConditions() {
        val report = WeatherParser.parseForecast(forecastJson, tunis, 1_000L)
        val current = report.current
        assertEquals(18.3, current.temperatureC, 0.001)
        assertEquals(17.9, current.apparentC, 0.001)
        assertEquals(71, current.humidity)
        assertEquals(9.4, current.windKmh, 0.001)
        assertEquals(2, current.code)
        org.junit.Assert.assertFalse(current.isDay)
        assertEquals("Africa/Tunis", report.timezoneId)
        assertEquals(1_000L, report.fetchedAtMillis)
        assertEquals(tunis, report.city)
    }

    @Test
    fun parsesDailyForecast() {
        val daily = WeatherParser.parseForecast(forecastJson, tunis, 0L).daily
        assertEquals(5, daily.size)
        assertEquals("2026-11-14", daily[0].dateIso)
        assertEquals(2, daily[0].code)
        assertEquals(22.1, daily[0].maxC, 0.001)
        assertEquals(14.2, daily[0].minC, 0.001)
        assertEquals(0, daily[4].code)
    }

    @Test
    fun skipsDaysWithMissingTemperatures() {
        val json = """
            {
              "timezone": "Africa/Tunis",
              "current": { "temperature_2m": 20.0 },
              "daily": {
                "time": ["2026-11-14", "2026-11-15"],
                "weather_code": [1, null],
                "temperature_2m_max": [21.0, null],
                "temperature_2m_min": [15.0, null]
              }
            }
        """.trimIndent()
        val report = WeatherParser.parseForecast(json, tunis, 0L)
        assertEquals(1, report.daily.size)
        assertEquals("2026-11-14", report.daily[0].dateIso)
    }

    @Test
    fun missingOptionalFieldsFallBackToDefaults() {
        val json = """{ "current": { "temperature_2m": 20.0 } }"""
        val report = WeatherParser.parseForecast(json, tunis, 0L)
        assertEquals(20.0, report.current.apparentC, 0.001)
        assertEquals(-1, report.current.humidity)
        assertEquals(-1, report.current.code)
        assertTrue(report.current.isDay)
        assertTrue(report.daily.isEmpty())
        // Falls back to the city's own time zone when the API does not send one.
        assertEquals("Africa/Tunis", report.timezoneId)
    }

    @Test
    fun parsesGeocodingResults() {
        val json = """
            {
              "results": [
                {
                  "id": 2464470, "name": "Tunis", "latitude": 36.81897, "longitude": 10.16579,
                  "country": "Tunisia", "admin1": "Tunis Governorate", "timezone": "Africa/Tunis"
                },
                { "id": 1, "name": "No coordinates" }
              ],
              "generationtime_ms": 0.6
            }
        """.trimIndent()
        val cities = WeatherParser.parseGeocoding(json)
        assertEquals(1, cities.size)
        assertEquals("Tunis", cities[0].name)
        assertEquals(36.81897, cities[0].latitude, 0.00001)
        assertEquals("Africa/Tunis", cities[0].timezoneId)
        assertEquals("Tunis, Tunis Governorate, Tunisia", cities[0].label())
    }

    @Test
    fun geocodingWithoutResultsIsEmpty() {
        assertTrue(WeatherParser.parseGeocoding("""{ "generationtime_ms": 0.3 }""").isEmpty())
    }

    @Test
    fun geocodingMissingTimezoneIsNull() {
        val json = """{ "results": [ { "name": "Somewhere", "latitude": 1.0, "longitude": 2.0 } ] }"""
        assertNull(WeatherParser.parseGeocoding(json)[0].timezoneId)
    }
}

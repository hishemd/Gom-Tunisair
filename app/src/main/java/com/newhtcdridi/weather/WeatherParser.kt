package com.newhtcdridi.weather

import org.json.JSONException

/**
 * Pure JSON -> model conversion using the [Json] adapter (which wraps org.json),
 * so it is easy to unit-test without Android.
 */
object WeatherParser {

    @Throws(JSONException::class)
    fun parseForecast(json: String, city: City, fetchedAtMillis: Long): WeatherReport {
        val root = Json.parse(json)
        val cur = root.get("current")?.asObject() ?: throw JSONException("missing 'current'")
        val temperature = cur.get("temperature_2m")?.asDouble() ?: throw JSONException("missing temperature_2m")

        val current = CurrentWeather(
            temperatureC = temperature,
            apparentC = cur.get("apparent_temperature")?.asDouble() ?: temperature,
            humidity = cur.get("relative_humidity_2m")?.asInt() ?: -1,
            windKmh = cur.get("wind_speed_10m")?.asDouble() ?: 0.0,
            code = cur.get("weather_code")?.asInt() ?: -1,
            isDay = (cur.get("is_day")?.asInt() ?: 1) == 1,
        )

        val days = ArrayList<DailyForecast>()
        val daily = root.get("daily")?.asObject()
        if (daily != null) {
            val times = daily.get("time")?.asArray()
            val codes = daily.get("weather_code")?.asArray()
            val maxes = daily.get("temperature_2m_max")?.asArray()
            val mins = daily.get("temperature_2m_min")?.asArray()
            val count = times?.size() ?: 0
            if (maxes != null && mins != null) {
                val n = minOf(count, maxes.size(), mins.size())
                for (i in 0 until n) {
                    val dateIso = times.get(i)?.asString() ?: continue
                    days.add(
                        DailyForecast(
                            dateIso = dateIso,
                            code = codes?.get(i)?.asInt() ?: -1,
                            maxC = maxes.get(i)?.asDouble() ?: continue,
                            minC = mins.get(i)?.asDouble() ?: continue,
                        ),
                    )
                }
            }
        }

        val timezone = root.get("timezone")?.asString() ?: city.timezoneId ?: "UTC"
        return WeatherReport(city, timezone, current, days, fetchedAtMillis)
    }

    @Throws(JSONException::class)
    fun parseGeocoding(json: String): List<City> {
        // Open-Meteo omits "results" entirely when nothing matches.
        val results = Json.parse(json).get("results")?.asArray() ?: return emptyList()
        val cities = ArrayList<City>()
        for (i in 0 until results.size()) {
            val item = results.get(i)?.asObject() ?: continue
            val name = item.get("name")?.asString() ?: continue
            val lat = item.get("latitude")?.asDouble() ?: continue
            val lon = item.get("longitude")?.asDouble() ?: continue
            cities.add(
                City(
                    name = name,
                    region = item.get("admin1")?.asString().orEmpty(),
                    country = item.get("country")?.asString().orEmpty(),
                    latitude = lat,
                    longitude = lon,
                    timezoneId = item.get("timezone")?.asString(),
                ),
            )
        }
        return cities
    }
}

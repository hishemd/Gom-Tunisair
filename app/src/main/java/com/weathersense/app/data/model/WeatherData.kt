package com.weathersense.app.data.model

data class WeatherData(
    val temperature: String,
    val feelsLike: String,
    val humidity: String,
    val windSpeed: String,
    val description: String,
    val icon: String,
    val city: String,
    val tempMax: String? = null,
    val tempMin: String? = null,
    val sunrise: String? = null,
    val sunset: String? = null,
    val pressure: String? = null,
    val visibility: String? = null,
    val uvIndex: String? = null
)

data class ForecastItem(
    val day: String,
    val tempMax: String,
    val tempMin: String,
    val icon: String,
    val description: String
)

data class HourlyForecast(
    val time: String,
    val temperature: String,
    val icon: String
)

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val cityName: String
)

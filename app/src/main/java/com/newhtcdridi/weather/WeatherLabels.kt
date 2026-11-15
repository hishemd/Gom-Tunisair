package com.newhtcdridi.weather

/** Localised text for a WMO weather code. */
object WeatherLabels {
    fun labelRes(code: Int): Int = when (code) {
        0 -> R.string.wmo_clear_sky
        1 -> R.string.wmo_mainly_clear
        2 -> R.string.wmo_partly_cloudy
        3 -> R.string.wmo_overcast
        45, 48 -> R.string.wmo_fog
        51, 53, 55 -> R.string.wmo_drizzle
        56, 57 -> R.string.wmo_freezing_drizzle
        61 -> R.string.wmo_light_rain
        63 -> R.string.wmo_rain
        65, 82 -> R.string.wmo_heavy_rain
        66, 67 -> R.string.wmo_freezing_rain
        71 -> R.string.wmo_light_snow
        73 -> R.string.wmo_snow
        75 -> R.string.wmo_heavy_snow
        77 -> R.string.wmo_snow_grains
        80, 81 -> R.string.wmo_rain_showers
        85, 86 -> R.string.wmo_snow_showers
        95 -> R.string.wmo_thunderstorm
        96, 99 -> R.string.wmo_thunderstorm_hail
        else -> R.string.wmo_unknown
    }
}

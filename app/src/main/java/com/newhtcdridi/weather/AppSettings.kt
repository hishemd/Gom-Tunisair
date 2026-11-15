package com.newhtcdridi.weather

import android.content.Context
import android.content.SharedPreferences

/** Everything we persist: selected city, temperature unit and the last forecast (offline use). */
class AppSettings(context: Context) {

    /** Last successful download: the raw Open-Meteo JSON, the city it belongs to and when. */
    data class Cached(val cityKey: String, val raw: String, val fetchedAtMillis: Long)

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var useFahrenheit: Boolean
        get() = prefs.getBoolean(KEY_FAHRENHEIT, false)
        set(value) {
            prefs.edit().putBoolean(KEY_FAHRENHEIT, value).apply()
        }

    /** The city picked by the user, or null if they never picked one. */
    var city: City?
        get() {
            val name = prefs.getString(KEY_CITY_NAME, null) ?: return null
            val latitude = prefs.getString(KEY_CITY_LAT, null)?.toDoubleOrNull() ?: return null
            val longitude = prefs.getString(KEY_CITY_LON, null)?.toDoubleOrNull() ?: return null
            return City(
                name = name,
                region = prefs.getString(KEY_CITY_REGION, null).orEmpty(),
                country = prefs.getString(KEY_CITY_COUNTRY, null).orEmpty(),
                latitude = latitude,
                longitude = longitude,
                timezoneId = prefs.getString(KEY_CITY_TZ, null),
            )
        }
        set(value) {
            val editor = prefs.edit()
            if (value == null) {
                editor.remove(KEY_CITY_NAME)
                    .remove(KEY_CITY_REGION)
                    .remove(KEY_CITY_COUNTRY)
                    .remove(KEY_CITY_LAT)
                    .remove(KEY_CITY_LON)
                    .remove(KEY_CITY_TZ)
            } else {
                editor.putString(KEY_CITY_NAME, value.name)
                    .putString(KEY_CITY_REGION, value.region)
                    .putString(KEY_CITY_COUNTRY, value.country)
                    .putString(KEY_CITY_LAT, value.latitude.toString())
                    .putString(KEY_CITY_LON, value.longitude.toString())
                    .putString(KEY_CITY_TZ, value.timezoneId)
            }
            editor.apply()
        }

    fun saveCache(cityKey: String, raw: String, fetchedAtMillis: Long) {
        prefs.edit()
            .putString(KEY_CACHE_CITY, cityKey)
            .putString(KEY_CACHE_RAW, raw)
            .putLong(KEY_CACHE_TIME, fetchedAtMillis)
            .apply()
    }

    fun loadCache(): Cached? {
        val cityKey = prefs.getString(KEY_CACHE_CITY, null) ?: return null
        val raw = prefs.getString(KEY_CACHE_RAW, null) ?: return null
        return Cached(cityKey, raw, prefs.getLong(KEY_CACHE_TIME, 0L))
    }

    /** The city key that the current cache belongs to, or null. */
    val cacheCityKey: String? get() = prefs.getString(KEY_CACHE_CITY, null)

    /** True when the last download failed (so the UI can show an offline hint). */
    var lastRefreshFailed: Boolean
        get() = prefs.getBoolean(KEY_REFRESH_FAILED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_REFRESH_FAILED, value).apply()
        }

    private companion object {
        const val FILE_NAME = "newhtcdridi_prefs"
        const val KEY_FAHRENHEIT = "use_fahrenheit"
        const val KEY_CITY_NAME = "city_name"
        const val KEY_CITY_REGION = "city_region"
        const val KEY_CITY_COUNTRY = "city_country"
        const val KEY_CITY_LAT = "city_lat"
        const val KEY_CITY_LON = "city_lon"
        const val KEY_CITY_TZ = "city_tz"
        const val KEY_CACHE_CITY = "cache_city"
        const val KEY_CACHE_RAW = "cache_raw"
        const val KEY_CACHE_TIME = "cache_time"
        const val KEY_REFRESH_FAILED = "refresh_failed"
    }
}

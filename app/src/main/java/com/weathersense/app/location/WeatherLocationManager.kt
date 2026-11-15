package com.weathersense.app.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Wraps FusedLocationProviderClient so the UI can ask for "where am I" once
 * and receive a sealed result instead of a Google task.
 */
class WeatherLocationManager(private val context: Context) {

    private val client: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult {
        if (!hasPermission()) return LocationResult.Error("Location permission not granted")

        val location: Location? = suspendCancellableCoroutine { cont ->
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc -> cont.resume(loc) }
                .addOnFailureListener { e ->
                    Log.w(TAG, "getCurrentLocation failed, trying lastLocation", e)
                    client.lastLocation
                        .addOnSuccessListener { last -> cont.resume(last) }
                        .addOnFailureListener { cont.resume(null) }
                }
        }

        if (location == null) return LocationResult.Error("No location available")

        return LocationResult.Success(
            latitude = location.latitude,
            longitude = location.longitude,
            cityName = cityName(context, location.latitude, location.longitude) ?: "My location",
        )
    }

    companion object {
        private const val TAG = "WeatherLocationManager"
        const val DEFAULT_LATITUDE = 51.5074
        const val DEFAULT_LONGITUDE = -0.1278

        /** Reverse-geocodes coordinates to a city name; null when unavailable. */
        @Suppress("DEPRECATION")
        fun cityName(context: Context, lat: Double, lon: Double): String? = try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            addresses?.firstOrNull()?.let { addr ->
                addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: addr.countryName
            }
        } catch (e: Exception) {
            Log.w(TAG, "Reverse geocoding failed", e)
            null
        }
    }
}

sealed class LocationResult {
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val cityName: String,
    ) : LocationResult()

    data class Error(val message: String) : LocationResult()
}

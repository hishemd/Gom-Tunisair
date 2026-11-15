package com.newhtcdridi.weather

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Quick check of whether the device currently has a usable internet connection.
 * Used to fail fast (instead of waiting for a connection timeout) when offline.
 */
object NetworkState {

    fun isOnline(context: Context): Boolean {
        val manager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

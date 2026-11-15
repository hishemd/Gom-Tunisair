package com.weathersense.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.weathersense.app.data.model.ForecastItem
import com.weathersense.app.data.model.WeatherData
import com.weathersense.app.location.LocationResult
import com.weathersense.app.location.WeatherLocationManager
import com.weathersense.app.service.WeatherApiService
import com.weathersense.app.ui.screen.WeatherSenseApp
import com.weathersense.app.ui.theme.WeatherSenseTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var locationManager: WeatherLocationManager

    private var state by mutableStateOf<UiState>(UiState.Loading)
    private var cityName by mutableStateOf("")

    private var lastLat: Double? = null
    private var lastLon: Double? = null
    private var lastCity: String = ""

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        if (granted.values.any { it }) {
            refresh()
        } else {
            state = UiState.NeedsPermission
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = WeatherLocationManager(this)

        setContent {
            WeatherSenseTheme {
                when (val s = state) {
                    is UiState.Loading -> LoadingScreen()
                    is UiState.NeedsPermission -> PermissionScreen { requestLocation() }

                    is UiState.Ready -> WeatherSenseApp(
                        weatherData = s.weather,
                        city = s.city,
                        forecast = s.forecast,
                        onRefreshLocation = { refresh() },
                    )

                    is UiState.Error -> ErrorScreen(s.message) { refresh() }
                }
            }
        }

        requestLocation()
        startAutoRefresh()
    }

    /**
     * Re-fetches from the last known coordinates without touching the location
     * stack, so the day/night icon and backdrop follow the clock while the app
     * stays open.
     */
    private fun startAutoRefresh() {
        lifecycleScope.launch {
            while (true) {
                delay(15 * 60 * 1000L)
                refreshWeatherOnly()
            }
        }
    }

    private fun refreshWeatherOnly() {
        val lat = lastLat ?: return
        val lon = lastLon ?: return
        val name = lastCity

        lifecycleScope.launch {
            val weather = withContext(Dispatchers.IO) {
                WeatherApiService.fetchWeather(lat, lon, name)
            }
            val forecast = withContext(Dispatchers.IO) {
                WeatherApiService.fetchForecast(lat, lon)
            }
            if (weather != null) state = UiState.Ready(weather, name, forecast)
        }
    }

    private fun requestLocation() {
        if (locationManager.hasPermission()) {
            refresh()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    private fun refresh() {
        if (!locationManager.hasPermission()) {
            state = UiState.NeedsPermission
            return
        }

        state = UiState.Loading
        lifecycleScope.launch {
            val location = withContext(Dispatchers.IO) { locationManager.getCurrentLocation() }

            val (lat, lon, name) = when (location) {
                is LocationResult.Success -> Triple(
                    location.latitude,
                    location.longitude,
                    location.cityName,
                )

                is LocationResult.Error -> Triple(
                    WeatherLocationManager.DEFAULT_LATITUDE,
                    WeatherLocationManager.DEFAULT_LONGITUDE,
                    "London",
                )
            }
            cityName = name
            lastLat = lat
            lastLon = lon
            lastCity = name

            val weather = withContext(Dispatchers.IO) {
                WeatherApiService.fetchWeather(lat, lon, name)
            }
            val forecast = withContext(Dispatchers.IO) {
                WeatherApiService.fetchForecast(lat, lon)
            }

            state = if (weather != null) {
                UiState.Ready(weather, name, forecast)
            } else {
                UiState.Error("Could not reach the weather service. Check your connection.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (state is UiState.NeedsPermission && locationManager.hasPermission()) {
            refresh()
        }
    }
}

sealed interface UiState {
    data object Loading : UiState
    data object NeedsPermission : UiState
    data class Ready(
        val weather: WeatherData,
        val city: String,
        val forecast: List<ForecastItem>,
    ) : UiState

    data class Error(val message: String) : UiState
}

@androidx.compose.runtime.Composable
private fun LoadingScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@androidx.compose.runtime.Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Try again") }
        }
    }
}

@androidx.compose.runtime.Composable
private fun PermissionScreen(onGrant: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Weather Sense needs your location to show local weather.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onGrant) { Text("Allow location") }
        }
    }
}

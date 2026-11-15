package com.newhtcdridi.weather

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.format.DateFormat
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import java.text.DateFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt

/**
 * Single-screen weather app in the style of the HTC Diamond (TouchFLO 3D) weather clock.
 *
 * - The clock follows the selected city's time zone and ticks every second.
 * - Weather comes from Open-Meteo; the last successful answer is cached for offline use.
 * - Tap the city to change it, the temperature to switch C/F, the status line to refresh.
 */
class MainActivity : Activity() {

    private val main = Handler(Looper.getMainLooper())
    private val io: ExecutorService = Executors.newFixedThreadPool(2)
    private val api = WeatherApi()

    private lateinit var settings: AppSettings
    private lateinit var city: City
    private var report: WeatherReport? = null
    private var loading = false
    private var lastRefreshFailed = false
    private var cityDialog: AlertDialog?
    private var lastDateKey = ""

    private val root by lazy { findViewById<View>(R.id.root) }
    private val content by lazy { findViewById<LinearLayout>(R.id.content) }
    private val clockHour by lazy { findViewById<TextView>(R.id.clockHour) }
    private val clockColon by lazy { findViewById<TextView>(R.id.clockColon) }
    private val clockMinute by lazy { findViewById<TextView>(R.id.clockMinute) }
    private val clockAmPm by lazy { findViewById<TextView>(R.id.clockAmPm) }
    private val dateLabel by lazy { findViewById<TextView>(R.id.dateLabel) }
    private val cityLabel by lazy { findViewById<TextView>(R.id.cityLabel) }
    private val weatherIcon by lazy { findViewById<WeatherIconView>(R.id.weatherIcon) }
    private val tempLabel by lazy { findViewById<TextView>(R.id.tempLabel) }
    private val conditionLabel by lazy { findViewById<TextView>(R.id.conditionLabel) }
    private val highLowLabel by lazy { findViewById<TextView>(R.id.highLowLabel) }
    private val feelsValue by lazy { findViewById<TextView>(R.id.feelsValue) }
    private val humidityValue by lazy { findViewById<TextView>(R.id.humidityValue) }
    private val windValue by lazy { findViewById<TextView>(R.id.windValue) }
    private val forecastRow by lazy { findViewById<LinearLayout>(R.id.forecastRow) }
    private val statusLabel by lazy { findViewById<TextView>(R.id.statusLabel) }
    private val attributionLabel by lazy { findViewById<TextView>(R.id.attributionLabel) }

    private val clockTick = object : Runnable {
        override fun run() {
            updateClock()
            // Wake up again right after the next full second so the clock never drifts.
            main.postDelayed(this, 1000L - System.currentTimeMillis() % 1000L)
        }
    }

    private val autoRefresh = object : Runnable {
        override fun run() {
            refresh()
            main.postDelayed(this, REFRESH_INTERVAL_MS)
        }
    }

    // ---------------------------------------------------------------- lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = AppSettings(this)
        city = settings.city ?: Presets.defaultCity(this)

        setContentView(R.layout.activity_main)
        applyInsets()

        cityLabel.setOnClickListener { showCityDialog() }
        tempLabel.setOnClickListener {
            settings.useFahrenheit = !settings.useFahrenheit
            render()
        }
        statusLabel.setOnClickListener { refresh() }
        weatherIcon.setOnClickListener { refresh() }
        attributionLabel.setOnClickListener { openUrl(ATTRIBUTION_URL) }

        loadCache()
        render()
    }

    override fun onResume() {
        super.onResume()
        weatherIcon.animated = true
        main.removeCallbacks(clockTick)
        main.post(clockTick)

        main.removeCallbacks(autoRefresh)
        main.postDelayed(autoRefresh, REFRESH_INTERVAL_MS)

        val fetchedAt = report?.fetchedAtMillis
        if (fetchedAt == null || System.currentTimeMillis() - fetchedAt > REFRESH_INTERVAL_MS) {
            refresh()
        }
    }

    override fun onPause() {
        super.onPause()
        weatherIcon.animated = false
        main.removeCallbacks(clockTick)
        main.removeCallbacks(autoRefresh)
    }

    override fun onDestroy() {
        cityDialog?.dismiss()
        cityDialog = null
        main.removeCallbacksAndMessages(null)
        io.shutdownNow()
        super.onDestroy()
    }

    // ---------------------------------------------------------------- window insets

    /**
     * Targeting Android 15 (API 35) makes the app draw edge-to-edge, so the content has to
     * stay clear of the status bar, navigation bar and display cutout by itself.
     */
    @Suppress("DEPRECATION")
    private fun applyInsets() {
        val base = (BASE_PADDING_DP * resources.displayMetrics.density).roundToInt()
        content.setPadding(base, base, base, base)
        root.setOnApplyWindowInsetsListener { _, insets ->
            val left: Int
            val top: Int
            val right: Int
            val bottom: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bars = insets.getInsets(
                    WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout(),
                )
                left = bars.left
                top = bars.top
                right = bars.right
                bottom = bars.bottom
            } else {
                left = insets.systemWindowInsetLeft
                top = insets.systemWindowInsetTop
                right = insets.systemWindowInsetRight
                bottom = insets.systemWindowInsetBottom
            }
            content.setPadding(base + left, base + top, base + right, base + bottom)
            insets
        }
        root.requestApplyInsets()
    }

    // ---------------------------------------------------------------- data

    private fun loadCache() {
        val cached = settings.loadCache() ?: return
        if (cached.cityKey != city.key) return
        report = try {
            WeatherParser.parseForecast(cached.raw, city, cached.fetchedAtMillis)
        } catch (e: Exception) {
            Log.w(TAG, "Ignoring unreadable cache", e)
            null
        }
    }

    private fun refresh() {
        if (loading) return
        loading = true
        lastRefreshFailed = false
        renderStatus()

        if (!NetworkState.isOnline(this)) {
            // Fail fast: no point waiting for a connection timeout we already know will happen.
            main.post {
                loading = false
                lastRefreshFailed = true
                settings.lastRefreshFailed = true
                renderStatus()
            }
            return
        }

        val target = city
        io.execute {
            val result = try {
                api.fetchForecast(target)
            } catch (e: Exception) {
                Log.w(TAG, "Forecast download failed", e)
                null
            }
            main.post {
                if (isFinishing || isDestroyed) return@post
                loading = false
                if (target.key != city.key) {
                    // The user picked another city while we were downloading: start over.
                    refresh()
                    return@post
                }
                if (result != null) {
                    report = result.report
                    lastRefreshFailed = false
                    settings.lastRefreshFailed = false
                    settings.saveCache(target.key, result.raw, result.report.fetchedAtMillis)
                } else {
                    lastRefreshFailed = true
                    settings.lastRefreshFailed = true
                }
                render()
            }
        }
    }

    private fun chooseCity(picked: City) {
        city = picked
        settings.city = picked
        report = null
        lastRefreshFailed = false
        loadCache()
        render()
        refresh()
    }

    // ---------------------------------------------------------------- rendering

    private fun render() {
        cityLabel.text = "${city.name.uppercase(Locale.getDefault())} ▾"
        renderCurrent()
        renderForecast()
        renderStatus()
        lastDateKey = ""
        updateClock()
    }

    private fun renderCurrent() {
        val r = report
        if (r == null) {
            val none = getString(R.string.no_value)
            tempLabel.text = "$none°"
            conditionLabel.text = ""
            highLowLabel.text = ""
            feelsValue.text = none
            humidityValue.text = none
            windValue.text = none
            weatherIcon.condition = Condition.CLOUDY
            weatherIcon.alpha = 0.3f
            weatherIcon.contentDescription = null
            return
        }

        val c = r.current
        tempLabel.text = formatTemperatureWithUnit(c.temperatureC)
        conditionLabel.text = getString(WeatherLabels.labelRes(c.code))
        val today = r.daily.firstOrNull()
        highLowLabel.text = if (today == null) {
            ""
        } else {
            getString(R.string.high_low, formatTemperature(today.maxC), formatTemperature(today.minC))
        }
        feelsValue.text = formatTemperature(c.apparentC)
        humidityValue.text = if (c.humidity >= 0) {
            latinFormat(R.string.humidity_value, c.humidity)
        } else {
            getString(R.string.no_value)
        }
        windValue.text = latinFormat(R.string.wind_value, c.windKmh.roundToInt())

        weatherIcon.condition = Condition.fromCode(c.code)
        weatherIcon.daytime = c.isDay
        weatherIcon.alpha = 1f
        weatherIcon.contentDescription = conditionLabel.text
    }

    private fun renderForecast() {
        forecastRow.removeAllViews()
        val days = report?.daily.orEmpty().take(FORECAST_DAYS)
        forecastRow.visibility = if (days.isEmpty()) View.GONE else View.VISIBLE

        val inflater = LayoutInflater.from(this)
        days.forEachIndexed { index, day ->
            val item = inflater.inflate(R.layout.item_forecast, forecastRow, false)
            item.findViewById<TextView>(R.id.fcDay).text =
                if (index == 0) getString(R.string.today) else weekdayLabel(day.dateIso)
            val icon = item.findViewById<WeatherIconView>(R.id.fcIcon)
            icon.condition = Condition.fromCode(day.code)
            icon.daytime = true
            icon.contentDescription = getString(WeatherLabels.labelRes(day.code))
            item.findViewById<TextView>(R.id.fcMax).text = formatTemperature(day.maxC)
            item.findViewById<TextView>(R.id.fcMin).text = formatTemperature(day.minC)
            forecastRow.addView(item)
        }
    }

    private fun renderStatus() {
        val r = report
        statusLabel.text = when {
            loading -> getString(R.string.status_loading)
            r == null -> getString(R.string.status_no_data)
            lastRefreshFailed -> getString(R.string.status_offline, timeText(r.fetchedAtMillis))
            else -> getString(R.string.status_updated, timeText(r.fetchedAtMillis))
        }
    }

    private fun updateClock() {
        val locale = Locale.getDefault()
        val calendar = Calendar.getInstance(currentTimeZone())
        val use24h = DateFormat.is24HourFormat(this)

        val hour = if (use24h) {
            calendar.get(Calendar.HOUR_OF_DAY)
        } else {
            val h = calendar.get(Calendar.HOUR)
            if (h == 0) 12 else h
        }
        clockHour.setTextIfChanged(String.format(Locale.US, "%02d", hour))
        clockMinute.setTextIfChanged(String.format(Locale.US, "%02d", calendar.get(Calendar.MINUTE)))
        // The colon blinks once per second, like the original HTC clock.
        clockColon.alpha = if (calendar.get(Calendar.SECOND) % 2 == 0) 1f else 0.3f

        if (use24h) {
            clockAmPm.visibility = View.GONE
        } else {
            clockAmPm.visibility = View.VISIBLE
            clockAmPm.setTextIfChanged(
                DateFormatSymbols.getInstance(locale).amPmStrings[calendar.get(Calendar.AM_PM)],
            )
        }

        val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}-" +
            "${calendar.timeZone.id}-$locale"
        if (dateKey != lastDateKey) {
            lastDateKey = dateKey
            dateLabel.text = formatDate(calendar.time, calendar.timeZone, locale)
        }
    }

    // ---------------------------------------------------------------- city picker

    private fun showCityDialog() {
        cityDialog?.dismiss()

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_city, null)
        val input = view.findViewById<EditText>(R.id.citySearchInput)
        val status = view.findViewById<TextView>(R.id.cityStatus)
        val list = view.findViewById<ListView>(R.id.cityList)
        val searchButton = view.findViewById<Button>(R.id.citySearchButton)

        val presets = Presets.all(this)
        var shown: List<City> = presets
        val adapter = ArrayAdapter(
            this,
            R.layout.item_city,
            R.id.cityItemText,
            presets.map { it.label() }.toMutableList(),
        )
        list.adapter = adapter

        val dialog = AlertDialog.Builder(this, R.style.NewhtcDialog).setView(view).create()
        cityDialog = dialog

        list.setOnItemClickListener { _, _, position, _ ->
            val picked = shown.getOrNull(position)
            if (picked != null) {
                dialog.dismiss()
                chooseCity(picked)
            }
        }

        // A dedicated button means search always works, even with an IME whose keyboard has no
        // search action or with a non-Latin keyboard that cannot type a query.
        searchButton.setOnClickListener {
            val query = input.text.toString().trim()
            if (query.length >= MIN_SEARCH_LENGTH) {
                performCitySearch(query, dialog, status, adapter) { found ->
                    shown = found
                }
            } else {
                status.visibility = View.VISIBLE
                status.text = getString(R.string.search_too_short)
            }
        }

        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = input.text.toString().trim()
                if (query.length >= MIN_SEARCH_LENGTH) {
                    performCitySearch(query, dialog, status, adapter) { found ->
                        shown = found
                    }
                }
                true
            } else {
                false
            }
        }

        dialog.show()
    }

    private fun performCitySearch(
        query: String,
        dialog: AlertDialog,
        status: TextView,
        adapter: ArrayAdapter<String>,
        onShown: (List<City>) -> Unit,
    ) {
        status.visibility = View.VISIBLE
        status.text = getString(R.string.search_loading)
        searchCities(query, dialog) { found ->
            if (found == null) {
                status.text = getString(R.string.search_error)
            } else if (found.isEmpty()) {
                status.text = getString(R.string.search_no_results)
            } else {
                status.visibility = View.GONE
                onShown(found)
                adapter.clear()
                adapter.addAll(found.map { it.label() })
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun searchCities(query: String, dialog: AlertDialog, onResult: (List<City>?) -> Unit) {
        val language = Locale.getDefault().language
        io.execute {
            val found = try {
                api.searchCities(query, language)
            } catch (e: Exception) {
                Log.w(TAG, "City search failed", e)
                null
            }
            main.post {
                if (!isFinishing && !isDestroyed && dialog.isShowing) onResult(found)
            }
        }
    }

    // ---------------------------------------------------------------- formatting helpers

    private fun currentTimeZone(): TimeZone {
        val id = report?.timezoneId ?: city.timezoneId
        return if (id.isNullOrBlank()) TimeZone.getDefault() else TimeZone.getTimeZone(id)
    }

    private fun toDisplayUnit(celsius: Double): Double =
        if (settings.useFahrenheit) celsius * 9.0 / 5.0 + 32.0 else celsius

    private fun formatTemperature(celsius: Double): String = "${toDisplayUnit(celsius).roundToInt()}°"

    /** "24°C" with the unit letter smaller than the number. */
    private fun formatTemperatureWithUnit(celsius: Double): CharSequence {
        val unit = if (settings.useFahrenheit) "F" else "C"
        val text = formatTemperature(celsius) + unit
        return SpannableString(text).apply {
            setSpan(
                RelativeSizeSpan(UNIT_SIZE_RATIO),
                text.length - 1,
                text.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }

    private fun formatDate(date: Date, zone: TimeZone, locale: Locale): String {
        val weekday = SimpleDateFormat("EEEE", locale).apply { timeZone = zone }.format(date)
        val pattern = DateFormat.getBestDateTimePattern(locale, "MMMMd")
        val monthDay = SimpleDateFormat(pattern, locale).apply { timeZone = zone }.format(date)
        return "$weekday  |  $monthDay".uppercase(locale)
    }

    /** Short weekday name for a yyyy-MM-dd date (parsed and printed in UTC so it never shifts). */
    private fun weekdayLabel(dateIso: String): String {
        val utc = TimeZone.getTimeZone("UTC")
        val locale = Locale.getDefault()
        return try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = utc }.parse(dateIso)
            if (parsed == null) {
                dateIso
            } else {
                SimpleDateFormat("EEE", locale).apply { timeZone = utc }.format(parsed).uppercase(locale)
            }
        } catch (e: ParseException) {
            dateIso
        }
    }

    /** Localised text, but numbers always use Latin digits so they match the clock and temperatures. */
    private fun latinFormat(resId: Int, vararg args: Any): String =
        String.format(Locale.US, getString(resId), *args)

    private fun timeText(millis: Long): String = DateFormat.getTimeFormat(this).format(Date(millis))

    private fun TextView.setTextIfChanged(value: CharSequence) {
        if (text.toString() != value.toString()) text = value
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "No app can open $url", e)
        }
    }

    private companion object {
        const val TAG = "NewhtcDRIDI"
        const val ATTRIBUTION_URL = "https://open-meteo.com/"
        const val REFRESH_INTERVAL_MS = 30 * 60 * 1000L
        const val FORECAST_DAYS = 5
        const val MIN_SEARCH_LENGTH = 2
        const val BASE_PADDING_DP = 24
        const val UNIT_SIZE_RATIO = 0.4f
    }
}

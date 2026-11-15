package com.newhtcdridi.weather

import android.content.Context

/** Built-in cities (Tunisia) so the picker works offline and the first launch needs no search. */
object Presets {

    fun all(context: Context): List<City> {
        val country = context.getString(R.string.country_tunisia)

        fun city(nameRes: Int, latitude: Double, longitude: Double) =
            City(
                name = context.getString(nameRes),
                region = "",
                country = country,
                latitude = latitude,
                longitude = longitude,
                timezoneId = "Africa/Tunis",
            )

        return listOf(
            city(R.string.city_tunis, 36.8065, 10.1815),
            city(R.string.city_sfax, 34.7406, 10.7603),
            city(R.string.city_sousse, 35.8256, 10.6369),
            city(R.string.city_kairouan, 35.6781, 10.0963),
            city(R.string.city_bizerte, 37.2744, 9.8739),
            city(R.string.city_gabes, 33.8815, 10.0982),
            city(R.string.city_nabeul, 36.4513, 10.7357),
            city(R.string.city_monastir, 35.7643, 10.8113),
            city(R.string.city_tozeur, 33.9197, 8.1335),
            city(R.string.city_djerba, 33.8750, 10.8575),
            city(R.string.city_tataouine, 32.9297, 10.4518),
            city(R.string.city_gafsa, 34.4250, 8.7842),
            city(R.string.city_kef, 36.1742, 8.7049),
        )
    }

    /** The city shown on first launch (the original mock-up showed Tunis). */
    fun defaultCity(context: Context): City = all(context).first()
}

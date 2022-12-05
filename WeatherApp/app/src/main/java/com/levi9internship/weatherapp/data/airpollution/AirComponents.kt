package com.levi9internship.weatherapp.data.airpollution

import java.io.Serializable

data class AirComponents(
    val co: Double,
    val no: Double,
    val no2: Double,
    val o3: Double,
    val so2: Double,
    val pm2_5: Double,
    val pm10: Double,
    val nh3: Double
) : Serializable
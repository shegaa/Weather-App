package com.levi9internship.weatherapp.data

import java.util.*

const val CITY_NAME: String = "Tokyo"
const val COUNTRY: String = "JPN"
const val DEF_LAT: Double = 35.68426
const val DEF_LON: Double = 139.78615

data class CurrentWeatherDetails(
    var cityName: String = CITY_NAME,
    var country: String = COUNTRY,
    var date: String = "",
    var currentTemperature: Int = 0,
    var weatherDescription: String = "",
    var humidity: Int = 0,
    var airQuality: Int = 0,
    var visibility: Int = 0,
    var windSpeed: Int = 0,
    var realFeel: Int = 0,
    var grindLevel: Float = 0.0F,
    var seaLevel: Float = 0.0F,
    var clouds: Float = 0.0F,
    var pressure: Float = 0.0F,
    var windGust: Float = 0.0F,
    var minTemp: Int = 0,
    var maxTemp: Int = 0,
    var currentDate: Date = Date(),
    var icon: String = "",
    var lat: Double = DEF_LAT,
    var lon: Double = DEF_LON,
    var id: Int = -1
) : java.io.Serializable

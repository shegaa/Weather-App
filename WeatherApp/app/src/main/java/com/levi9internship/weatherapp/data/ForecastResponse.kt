package com.levi9internship.weatherapp.data

import com.levi9internship.weatherapp.data.weather.WeatherResponse

data class ForecastResponse(
    val list: List<WeatherResponse>
) : java.io.Serializable
package com.levi9internship.weatherapp.data.weather

import java.util.*

data class WeatherResponse(
    val base: String,
    val clouds: Clouds,
    val cod: Int,
    val coord: Coord,
    val dt: Int,
    val id: Int,
    val main: Main,
    val name: String,
    val sys: Sys,
    val timezone: Int,
    val visibility: Int,
    val weather: List<Weather>,
    val wind: Wind,
    var date: Date,
    var dt_txt: String
) : java.io.Serializable
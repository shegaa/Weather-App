package com.levi9internship.weatherapp.data.airpollution

import com.levi9internship.weatherapp.data.weather.Coord
import java.io.Serializable

data class AirPollutionResponse(
    val coord: Coord,
    val list: List<AirList>
) : Serializable

data class AirList(
    val dt: Int,
    val main: AirMain
) : Serializable

data class AirMain(
    val aqi: Int
) : Serializable
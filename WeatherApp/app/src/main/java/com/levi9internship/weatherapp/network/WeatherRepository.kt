package com.levi9internship.weatherapp.network

import com.levi9internship.weatherapp.data.airpollution.AirPollutionResponse
import com.levi9internship.weatherapp.data.weather.WeatherResponse
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService
) {

    suspend fun getWeather(
        lat: Double,
        lon: Double,
        units: String
    ): WeatherResponse {
        return weatherApiService.getWeather(lat = lat, lon = lon, units = units)
    }

    suspend fun getAirPollution(
        lat: Double, lon: Double
    ): AirPollutionResponse {
        return weatherApiService.getAirPollution(lat = lat, lon = lon)
    }

    //suspend fun getForecast() TODO

}
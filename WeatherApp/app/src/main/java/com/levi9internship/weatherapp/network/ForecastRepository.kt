package com.levi9internship.weatherapp.network

import com.levi9internship.weatherapp.data.ForecastResponse
import javax.inject.Inject

class ForecastRepository @Inject constructor(
    private val forecastApiService: ForecastApiService
) {

    suspend fun getForecast(
        lat: Double,
        lon: Double,
        units: String
    ): ForecastResponse {
        return forecastApiService.getForecast(lat = lat, lon = lon, units = units)
    }

}
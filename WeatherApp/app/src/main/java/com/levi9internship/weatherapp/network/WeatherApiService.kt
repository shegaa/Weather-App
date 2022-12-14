package com.levi9internship.weatherapp.network

import com.levi9internship.weatherapp.data.airpollution.AirPollutionResponse
import com.levi9internship.weatherapp.data.weather.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
        const val API_KEY = "f01d0a9dcdb52af30a711bb8b4b4b530"
        const val UNITS_METRIC = "metric"
        const val UNITS_IMPERIAL = "imperial"
    }

    @GET("weather?")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appId: String = API_KEY,
        @Query("units") units: String,
    ): WeatherResponse

    @GET("air_pollution?")
    suspend fun getAirPollution(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appId: String = API_KEY,
    ): AirPollutionResponse

    @GET("forecast?")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appId: String = API_KEY,
        @Query("units") units: String
    )//TODO

}
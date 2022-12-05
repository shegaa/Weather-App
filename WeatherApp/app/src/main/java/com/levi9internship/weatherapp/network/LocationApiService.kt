package com.levi9internship.weatherapp.network

import com.levi9internship.weatherapp.data.City

import retrofit2.http.GET
import retrofit2.http.Query

interface LocationApiService {

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/geo/1.0/"
        const val API_KEY = "f01d0a9dcdb52af30a711bb8b4b4b530"
    }

    @GET("direct?")
    suspend fun getLocations(
        @Query("q") q: String,
        @Query("appid") appId: String = API_KEY,
        @Query("limit") limit: Int,
    ): List<City>

}

package com.levi9internship.weatherapp.network

import com.levi9internship.weatherapp.data.City
import javax.inject.Inject

class LocationRepository @Inject constructor(private val locationApiService: LocationApiService) {

    suspend fun getLocation(
        q: String,
        limit: Int,
    ): List<City> {
        return locationApiService.getLocations(q = q, limit = limit)
    }
}
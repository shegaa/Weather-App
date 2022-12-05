package com.levi9internship.weatherapp.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.levi9internship.weatherapp.data.CurrentWeatherDetails
import com.levi9internship.weatherapp.data.FavouriteRepository
import javax.inject.Inject

/*
Service class for Shared Preferences and Favorite locations
 */
class FavoritesService @Inject constructor(
    var preferencesService: PreferencesService,
    var roomRepository: FavouriteRepository
) {

    val gson = Gson()


    fun getAllFavorites(): MutableList<CurrentWeatherDetails> {

        val itemType = object : TypeToken<MutableList<CurrentWeatherDetails>>() {}.type
        /*val favouriteList = roomRepository.readAllData()
        var finalList = mutableListOf<CurrentWeatherDetails>()
        while(favouriteList.value == null){
            val asd = favouriteList?.value
            val qwer = favouriteList.value
            val qw = 1
        }

        //if (!favouriteList.value.isNullOrEmpty()) {
            for (i in 0..favouriteList.value!!.size) {
                //id
                finalList[i].lat = favouriteList.value!![i].lat
                finalList[i].lon = favouriteList.value!![i].lon
                finalList[i].cityName = favouriteList.value!![i].city
                finalList[i].country = favouriteList.value!![i].country
                //image
                finalList[i].currentTemperature = favouriteList.value!![i].temp.roundToInt()
                finalList[i].humidity = favouriteList.value!![i].humidity
                finalList[i].windSpeed = favouriteList.value!![i].wind.roundToInt()
            }
        //}*/
        return gson.fromJson(
            preferencesService.getFavorites(), itemType
        )
    }

    /*fun addFavorite(favorite: CurrentWeatherDetails) {
        var locations = getAllFavorites()
        if (!isLocationInFavorites(favorite)) {
            locations.add(favorite)
            preferencesService.setFavorites(gson.toJson(locations))
        } else {
            Log.e("LOG", "Already in favorites!")
        }
    }*/

    fun removeFavorite(favorite: CurrentWeatherDetails) {
        var locations = getAllFavorites()
        val newLocations =
            locations.filter { it.cityName != favorite.cityName && it.country != favorite.country }
        preferencesService.setFavorites(gson.toJson(newLocations))

    }

    fun setActiveLocation(favorite: CurrentWeatherDetails) {
        preferencesService.setIsActiveCurrent(false)

        val gson = Gson()
        preferencesService.setActiveLocation(gson.toJson(favorite))
    }

    fun setActiveLocationToCurrent() {
        preferencesService.setIsActiveCurrent(true)
    }

    fun isLocationInFavorites(favorite: CurrentWeatherDetails): Boolean {
        val locations = getAllFavorites()
        val retVal =
            !locations.none { l -> l.cityName == favorite.cityName && l.country == favorite.country }
        return retVal
    }

    fun isActiveLocation(weather: CurrentWeatherDetails): Boolean {
        val activeLocation = getActiveLocation()
        if (activeLocation != null) {
            if ((activeLocation.cityName == weather.cityName) && (activeLocation.country == weather.country)) {
                return true
            }
        }

        return false
    }

    fun getActiveLocation(): CurrentWeatherDetails? {
        return if (preferencesService.getIsActiveCurrent()) {
            null
        } else {
            gson.fromJson(
                preferencesService.getActiveLocation(),
                CurrentWeatherDetails::class.java
            )
        }
    }

}
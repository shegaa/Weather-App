package com.levi9internship.weatherapp.viewmodel

import android.location.Address
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.levi9internship.weatherapp.data.CurrentWeatherDetails
import com.levi9internship.weatherapp.data.Favourite
import com.levi9internship.weatherapp.data.FavouriteRepository
import com.levi9internship.weatherapp.data.FirestoreFavourite
import com.levi9internship.weatherapp.data.weather.WeatherResponse
import com.levi9internship.weatherapp.firebase.FirestoreRepository
import com.levi9internship.weatherapp.network.WeatherApiService
import com.levi9internship.weatherapp.network.WeatherRepository
import com.levi9internship.weatherapp.service.FavoritesService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val favoritesService: FavoritesService,
    private val roomDatabase: FavouriteRepository,
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: FirebaseAuth
) : ViewModel() {
    private val _error = MutableLiveData<Exception>()
    val error: LiveData<Exception> = _error

    private val _unit = MutableLiveData<String>()
    val unit: LiveData<String> = _unit

    private val _userLocation = MutableLiveData<Address>()
    val userLocation: LiveData<Address> = _userLocation

    private val _favoriteLocations = MutableLiveData<MutableList<CurrentWeatherDetails>>()
    val favoriteLocations: LiveData<MutableList<CurrentWeatherDetails>> = _favoriteLocations

    private val _weather = MutableLiveData<CurrentWeatherDetails>()
    val weather: LiveData<CurrentWeatherDetails> = _weather

    private val _favouriteRoom = MutableLiveData<List<Favourite>>()
    val favouriteRoom = _favouriteRoom

    private val _activeLocation = MutableLiveData<CurrentWeatherDetails>()
    val activeLocation: LiveData<CurrentWeatherDetails> = _activeLocation

    init {
        setUnitSystem()
        _favoriteLocations.value = favoritesService.getAllFavorites()
        _weather.value = CurrentWeatherDetails()
        _favouriteRoom.value = roomDatabase.readAllData().value
    }

    private fun setUnitSystem() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val (unit, exception) = firestoreRepository.getUnitSystem()
                _unit.postValue(unit)
                if (exception != null) {
                    _error.postValue(exception)
                }
            }
        }
    }

    fun deleteFavourite(favourite: Favourite) {
        viewModelScope.launch {
            roomDatabase.deleteFavourite(favourite)
            val exceptionFavorite = firestoreRepository.removeFavourite(favourite)
            if (exceptionFavorite != null) {
                _error.postValue(exceptionFavorite!!)
            }
        }
        _activeLocation.value = CurrentWeatherDetails()
    }

    suspend fun getWeather() {
        val lat = _userLocation.value?.latitude
        val lon = _userLocation.value?.longitude

        if (lat != null && lon != null) {
            val response = weatherRepository.getWeather(
                lat, lon, _unit.value!!
            )

            _weather.value = formatWeatherDetails(response, CurrentWeatherDetails())
            if (_userLocation.value != null) {
                _weather.value!!.cityName = _userLocation.value!!.locality
            }
        }
    }

    suspend fun getFavoriteLocationsWeather() {
        var newFavorites = mutableListOf<CurrentWeatherDetails>()
        for (location in _favoriteLocations.value!!) {
            val response = weatherRepository.getWeather(
                location.lat, location.lon, _unit.value!!
            )

            newFavorites.add(
                formatWeatherDetails(
                    response,
                    formatWeatherDetails(response, location)
                )
            )
        }

        _favoriteLocations.value = newFavorites
    }

    fun setUserLocation(address: Address) {
        _userLocation.value = address
    }

    private fun formatWeatherDetails(
        response: WeatherResponse,
        location: CurrentWeatherDetails
    ): CurrentWeatherDetails {
        var retVal = location

        retVal.apply {
            currentTemperature = response.main.temp.roundToInt()
            weatherDescription = (response.weather[0].description).replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
            humidity = response.main.humidity
            windSpeed = (response.wind.speed).roundToInt()
            realFeel = ((response.main.feels_like).roundToInt())
            grindLevel = (response.main.grnd_level).toFloat()
            seaLevel = (response.main.sea_level).toFloat()
            clouds = (response.clouds.all).toFloat()
            pressure = (response.main.pressure).toFloat()
            windGust = (response.wind.gust).toFloat()
            icon = response.weather[0].icon
            lat = (response.coord.lat).toDouble()
            lon = (response.coord.lon).toDouble()
            country = response.sys.country
        }

        return retVal
    }

    fun setActiveLocation(weather: CurrentWeatherDetails) {
        _activeLocation.value = weather
    }

    fun setFavoriteLocations(favoriteLocations: MutableList<CurrentWeatherDetails>) {
        _favoriteLocations.value = favoriteLocations
    }

    fun removeAtIndex(i: Int) {
        _favoriteLocations.value?.removeAt(i)
    }

    fun findByLatLon(lat: Double, lon: Double): CurrentWeatherDetails? {
        if (_weather.value?.lat == lat && _weather.value?.lon == lon) {
            return _weather.value!!
        }

        for (favorite in _favoriteLocations.value!!) {
            if (favorite.lat == lat && favorite.lon == lon) {
                return favorite
            }
        }

        return null
    }

    fun addNewFavorite(newFavorite: CurrentWeatherDetails) {
        viewModelScope.launch {
            val response = weatherRepository.getWeather(
                newFavorite.lat, newFavorite.lon, _unit.value!!
            )

            newFavorite.country = response.sys.country

            val roomFavorite = Favourite(
                response.id,
                newFavorite.lat,
                newFavorite.lon,
                newFavorite.cityName,
                newFavorite.country,
                response.weather[0].icon,
                response.main.temp,
                response.main.humidity,
                response.wind.speed
            )
            val firesstoreFavorite = FirestoreFavourite(
                newFavorite.lat,
                newFavorite.lon,
                newFavorite.cityName,
                newFavorite.country
            )

            roomDatabase.addFavourite(roomFavorite)

            //TODO: Check if user is logged in
            val exception = firestoreRepository.addFavourite(firesstoreFavorite)
            if (exception != null) {
                _error.postValue(exception!!)
            }

            newFavorite.weatherDescription = response.weather[0].description
            newFavorite.currentTemperature = response.main.temp.roundToInt()
            _favoriteLocations.value?.add(newFavorite)

        }
    }


    fun isLocationInFavorites(location: CurrentWeatherDetails): Boolean {
        if (_favoriteLocations.value != null) {
            for (favorite in _favoriteLocations.value!!) {
                if (favorite.cityName == location.cityName && favorite.lon.toInt() == location.lon.toInt() && favorite.lat.toInt() == location.lat.toInt()
                ) {
                    return true
                }
            }
        }
        return false;
    }

}
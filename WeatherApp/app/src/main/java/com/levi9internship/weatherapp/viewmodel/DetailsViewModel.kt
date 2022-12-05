package com.levi9internship.weatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.levi9internship.weatherapp.data.Favourite
import com.levi9internship.weatherapp.data.FavouriteRepository
import com.levi9internship.weatherapp.data.FirestoreFavourite
import com.levi9internship.weatherapp.data.ForecastResponse
import com.levi9internship.weatherapp.data.weather.WeatherResponse
import com.levi9internship.weatherapp.firebase.FirestoreRepository
import com.levi9internship.weatherapp.network.ForecastRepository
import com.levi9internship.weatherapp.network.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val forecastRepository: ForecastRepository,
    private val firestoreRepository: FirestoreRepository,
    private val roomRepository: FavouriteRepository,
    private val authRepository: FirebaseAuth
) : ViewModel() {
    private val _errorFirestore = MutableLiveData<Exception>()
    val errorFirestore: LiveData<Exception> = _errorFirestore

    private val _unit = MutableLiveData<String>()
    val unit: LiveData<String> = _unit

    private val _weather = MutableLiveData<WeatherResponse>()
    val weather: LiveData<WeatherResponse> = _weather

    private val _forecast = MutableLiveData<ForecastResponse>()
    val forecast: LiveData<ForecastResponse> = _forecast

    private val _lat = MutableLiveData<String>()
    val lat: LiveData<String> = _lat

    private val _lon = MutableLiveData<String>()
    val lon: LiveData<String> = _lon


    init {
        setUnitSystem()
    }

    private fun setUnitSystem() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val (unit, exception) = firestoreRepository.getUnitSystem()
                _unit.postValue(unit)
                if (exception != null) {
                    _errorFirestore.postValue(exception)
                }
            }
        }
    }

    fun setLatAndLon(lat: String, lon: String) {
        _lat.value = lat
        _lon.value = lon
    }

    fun getWeather() {
        viewModelScope.launch {
            _weather.value = weatherRepository.getWeather(
                _lat.value!!.toDouble(), _lon.value!!.toDouble(), _unit.value!!
            )
        }
    }

    fun getForecast() {
        viewModelScope.launch {
            _forecast.value = forecastRepository.getForecast(
                _lat.value!!.toDouble(), _lon.value!!.toDouble(), _unit.value!!
            )
        }
    }

    fun addToFirestore(favourite: FirestoreFavourite) {

        viewModelScope.launch {
            val exception = firestoreRepository.addFavourite(favourite)
            if (exception != null) {
                _errorFirestore.value = exception!!
            }
        }
    }

    fun addToRoom(favourite: Favourite) {
        if (inputCheck(
                favourite.id,
                favourite.lat,
                favourite.lon,
                favourite.city,
                favourite.country,
                favourite.image,
                favourite.temp,
                favourite.humidity,
                favourite.wind
            )
        ) {
            viewModelScope.launch {
                roomRepository.addFavourite(favourite)
            }
        } else {
            //TODO return message
        }
    }

    private fun inputCheck(
        id: Int,
        lat: Double,
        lon: Double,
        city: String,
        country: String,
        image: String,
        temp: Double,
        humidity: Int,
        wind: Double
    ): Boolean {
        return !(id == null || lat == null || lon == null || city.isNullOrEmpty() || country.isNullOrEmpty() || image.isNullOrEmpty() || temp.isNaN() || humidity == null || wind.isNaN())
    }
}
package com.levi9internship.weatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levi9internship.weatherapp.data.Favourite
import com.levi9internship.weatherapp.data.FavouriteRepository
import com.levi9internship.weatherapp.data.weather.WeatherResponse
import com.levi9internship.weatherapp.firebase.FirebaseProfileRepository
import com.levi9internship.weatherapp.firebase.FirestoreRepository
import com.levi9internship.weatherapp.network.WeatherApiService
import com.levi9internship.weatherapp.network.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: FirebaseProfileRepository,
    private val roomRepository: FavouriteRepository,
    private val firestoreRepository: FirestoreRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    private val _errorFirestore = MutableLiveData<Exception>()
    val errorFirestore: LiveData<Exception> = _errorFirestore

    private val _unit = MutableLiveData<String>()
    val unit: LiveData<String> = _unit

    private val _isLoggedIn = MutableLiveData<Boolean>(false)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _weather = MutableLiveData<WeatherResponse>()
    val weather: LiveData<WeatherResponse> = _weather

    fun getUnitSystem(){
        viewModelScope.launch {
            val (unit, exception) = firestoreRepository.getUnitSystem()
            _unit.postValue(unit)
            if (exception != null) {
                _errorFirestore.postValue(exception)
            }
        }
    }

    fun isUserLoggedIn() {
        _isLoggedIn.value = repository.isUserLoggedIn()
    }

    fun logout() {
        viewModelScope.launch {
            roomRepository.deleteAllFavourites()
        }
        repository.logout()
        isUserLoggedIn()
    }

    fun setUnitSystem(unit: String) {
        viewModelScope.launch {
            val exception = firestoreRepository.changeUnitSystem(unit)
            if (exception != null) {
                _errorFirestore.postValue(exception!!)
            }
        }
    }

    fun getSpinnerPosition(): Int {
        if (_unit.value == WeatherApiService.UNITS_IMPERIAL) {
            return 1
        } else {
            return 0
        }
    }
    private fun setRoomValue(favourite: Favourite) {
        viewModelScope.launch {
            roomRepository.addFavourite(favourite)
        }
    }

    suspend fun updateRoom(unit: String){
        val (firestoreData, exceptionFavorites ) =  firestoreRepository.getFavourites()
        if (exceptionFavorites != null) {
            _errorFirestore.value = java.lang.Exception(exceptionFavorites.message.toString())
        }
        for (favourite in firestoreData) {
            _weather.value =
                weatherRepository.getWeather(favourite.lat, favourite.lon, unit)
            val roomFavourite = Favourite(
                _weather.value!!.id,
                favourite.lat,
                favourite.lon,
                _weather.value!!.name,
                _weather.value!!.sys.country,
                _weather.value!!.weather[0].icon,
                _weather.value!!.main.temp,
                _weather.value!!.main.humidity,
                _weather.value!!.wind.speed
            )
            setRoomValue(roomFavourite)
        }
    }
}
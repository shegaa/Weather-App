package com.levi9internship.weatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.levi9internship.weatherapp.R
import com.levi9internship.weatherapp.data.Favourite
import com.levi9internship.weatherapp.data.FavouriteRepository
import com.levi9internship.weatherapp.data.weather.WeatherResponse
import com.levi9internship.weatherapp.firebase.FirebaseProfileRepository
import com.levi9internship.weatherapp.firebase.FirestoreRepository
import com.levi9internship.weatherapp.firebase.Resource
import com.levi9internship.weatherapp.network.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "LoginViewModel"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: FirebaseProfileRepository,
    private val weatherRepository: WeatherRepository,
    private val firestoreRepository: FirestoreRepository,
    private val roomRepository: FavouriteRepository
) : ViewModel() {
    private val _loginFlow = MutableLiveData<Resource<FirebaseUser>?>(null)
    val loginFlow: LiveData<Resource<FirebaseUser>?> = _loginFlow

    private val _error = MutableLiveData<Int>()
    val error: LiveData<Int> = _error

    private val _errorFirestore = MutableLiveData<Exception>()
    val errorFirestore: LiveData<Exception> = _errorFirestore

    var email: String? = null
    var password: String? = null

    private val _weather = MutableLiveData<WeatherResponse>()
    val weather: LiveData<WeatherResponse> = _weather

    fun login() = viewModelScope.launch {
        roomRepository.deleteAllFavourites()
        _loginFlow.value = Resource.Loading
        val result = email?.let {
            password?.let { it1 ->
                repository.loginUser(
                    it,
                    it1
                )
            }
        }

        val (firestoreData, exceptionFavorites ) =  firestoreRepository.getFavourites()
        val (unit, exception) = firestoreRepository.getUnitSystem()
        if (exception != null) {
            _errorFirestore.value = java.lang.Exception("${exception.message.toString()} \n\nMetric will be used as default")
        }

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
        _loginFlow.value = result
    }

    private fun setError(value: Int) {
        _error.value = value
    }

    fun isDataForLoginValid(): Boolean {
        if (email.isNullOrEmpty()) {
            setError(R.string.error_empty_email_field)
            return false
        }
        if (password.isNullOrEmpty()) {
            setError(R.string.error_empty_password_field)
            return false
        }
        return true
    }

    private fun setRoomValue(favourite: Favourite) {
        viewModelScope.launch {
            roomRepository.addFavourite(favourite)
        }
    }
}
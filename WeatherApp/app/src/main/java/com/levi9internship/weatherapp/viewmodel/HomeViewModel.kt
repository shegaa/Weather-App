package com.levi9internship.weatherapp.viewmodel

import android.location.Address
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.levi9internship.weatherapp.data.CurrentWeatherDetails
import com.levi9internship.weatherapp.data.DEF_LAT
import com.levi9internship.weatherapp.data.DEF_LON
import com.levi9internship.weatherapp.data.ForecastResponse
import com.levi9internship.weatherapp.data.airpollution.AirPollutionResponse
import com.levi9internship.weatherapp.data.weather.WeatherResponse
import com.levi9internship.weatherapp.firebase.FirestoreRepository
import com.levi9internship.weatherapp.network.ForecastRepository
import com.levi9internship.weatherapp.network.WeatherApiService
import com.levi9internship.weatherapp.network.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val forecastRepository: ForecastRepository,
    private val authRepository: FirebaseAuth,
    private val firestoreRepository: FirestoreRepository,
) : ViewModel() {

    private val _unit = MutableLiveData<String>()
    val unit: LiveData<String> = _unit

    private val _weather = MutableLiveData<WeatherResponse>()
    val weather: LiveData<WeatherResponse> = _weather

    private val _airPollution = MutableLiveData<AirPollutionResponse>()
    val airPollution: LiveData<AirPollutionResponse> = _airPollution

    private val _userLocation = MutableLiveData<Address>()
    val userLocation: LiveData<Address> = _userLocation

    private val _weatherDetails = MutableLiveData<CurrentWeatherDetails>()
    val weatherDetails: LiveData<CurrentWeatherDetails> = _weatherDetails

    private val _forecast = MutableLiveData<ForecastResponse>()
    val forecast: LiveData<ForecastResponse> = _forecast

    init {
        _weatherDetails.value = CurrentWeatherDetails()
        setUnitSystem()
    }

    private fun setUnitSystem() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (authRepository.currentUser != null) {
                    _unit.postValue(firestoreRepository.getUnitSystem().first!!)
                } else {
                    _unit.postValue(WeatherApiService.UNITS_METRIC)
                }
            }
        }
    }

    suspend fun getDefaultWeather() {
        _weather.value = weatherRepository.getWeather(
            DEF_LAT, DEF_LON, _unit.value!!
        )

        _airPollution.value = weatherRepository.getAirPollution(
            DEF_LAT, DEF_LON
        )

        _forecast.value = forecastRepository.getForecast(
            DEF_LAT, DEF_LON, _unit.value!!
        )
    }

    suspend fun getWeather() {
        val lat = _userLocation.value?.latitude
        val lon = _userLocation.value?.longitude

        if (lat != null && lon != null) {
            _weather.value = weatherRepository.getWeather(
                lat, lon, _unit.value!!
            )

            _airPollution.value = weatherRepository.getAirPollution(
                lat, lon
            )

            _forecast.value = forecastRepository.getForecast(
                lat, lon, _unit.value!!
            )
        }
    }

    fun setUserLocation(address: Address) {
        _userLocation.value = address
    }

    fun formatWeatherDetails() {
        _weatherDetails.value = CurrentWeatherDetails()

        _weatherDetails.value?.apply {
            if (_userLocation.value != null) {
                cityName = _userLocation.value!!.locality
            }

            if (_weather.value != null) {
                currentTemperature = _weather.value?.main?.temp?.roundToInt() ?: 0

                weatherDescription = (_weather.value!!.weather[0].description).replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }
                humidity = _weather.value?.main?.humidity ?: 0
                windSpeed = ((_weather.value?.wind?.speed) ?: 0.0).roundToInt()
                realFeel = ((_weather.value?.main?.feels_like ?: 0.0).roundToInt())
                grindLevel = (_weather.value?.main?.grnd_level ?: 0).toFloat()
                seaLevel = (_weather.value?.main?.sea_level ?: 0).toFloat()
                clouds = (_weather.value?.clouds?.all ?: 0).toFloat()
                pressure = (_weather.value?.main?.pressure ?: 0).toFloat()
                windGust = (_weather.value?.wind?.gust ?: 0).toFloat()
                visibility = (_weather.value?.visibility ?: 0)
            }

            if (_airPollution.value != null) {
                airQuality = (_airPollution.value!!.list[0].main.aqi)
            }

            val currentDate = Date()
            val formatter = SimpleDateFormat("EEEE, dd MMMM")
            date = formatter.format(currentDate)
        }
    }
}

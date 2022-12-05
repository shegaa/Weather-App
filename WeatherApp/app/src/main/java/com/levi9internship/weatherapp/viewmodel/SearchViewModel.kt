package com.levi9internship.weatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.levi9internship.weatherapp.data.City
import com.levi9internship.weatherapp.firebase.FirebaseProfileRepository
import com.levi9internship.weatherapp.network.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val repository: FirebaseProfileRepository
) : ViewModel() {
    private val _cityList = MutableLiveData<List<City>>()
    val cityList: LiveData<List<City>> = _cityList

    private val _cityName = MutableLiveData<String>()
    val cityName: LiveData<String> = _cityName

    suspend fun getCityByName(name: String) {
        _cityList.value = locationRepository.getLocation(name, 5)
    }

    fun resetCityList() {
        _cityList.value = emptyList()
    }

    fun setString(string: String) {
        _cityName.value = string
    }

    fun resetCityName() {
        _cityName.value = ""
    }
}
package com.levi9internship.weatherapp.viewmodel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.levi9internship.weatherapp.data.FavouriteRepository
import com.levi9internship.weatherapp.R
import com.levi9internship.weatherapp.firebase.FirebaseProfileRepository
import com.levi9internship.weatherapp.firebase.FirestoreRepository
import com.levi9internship.weatherapp.firebase.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: FirebaseProfileRepository,
    private val firestoreRepository: FirestoreRepository,
    private val roomRepository: FavouriteRepository
) : ViewModel() {
    private val _registerFlow = MutableLiveData<Resource<FirebaseUser>?>(null)
    val registerFlow: LiveData<Resource<FirebaseUser>?> = _registerFlow

    private val _errorFirestore = MutableLiveData<Exception>()
    val errorFirestore: LiveData<Exception> = _errorFirestore

    private val _error = MutableLiveData<Int>()
    val error: LiveData<Int> = _error
    val currentUser: FirebaseUser?
        get() = repository.currentUser

    var email: String? = null
    var password: String? = null
    var passwordConfirmed: String? = null

    fun register() = viewModelScope.launch {
        _registerFlow.value = Resource.Loading
        val result = email?.let {
            password?.let { it1 ->
                repository.registerUser(
                    it,
                    it1
                )
            }
        }
        roomRepository.deleteAllFavourites()//TODO place in onSuccess
        val exception = firestoreRepository.changeUnitSystem("metric")//TODO check if user is registered and add trigger for firestore
        if (exception != null) {
            _errorFirestore.postValue(exception!!)
        }
        _registerFlow.value = result
    }


    fun isDataForRegisterValid(): Boolean {
        if (!isEmailValid()) {
            return false
        }

        if (!isPasswordValid()) {
            return false
        }

        if (!isPasswordConfirmedValid()) {
            return false
        }

        return true
    }


    private fun isEmailValid(): Boolean {
        if (email.isNullOrEmpty()) {
            setError(R.string.error_empty_email_field)
            return false
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(email!!).matches()) {
                setError(R.string.error_invalid_email)
                return false
            } else {
                val part = email!!.split("@")
                if (part[0].length < 3) {
                    setError(R.string.error_invalid_email)
                    return false
                }
                return true
            }
        }
    }

    private fun isPasswordValid(): Boolean {
        return if (password.isNullOrEmpty()) {
            setError(R.string.error_empty_password_field)
            false
        } else {
            if (password!!.length < 6) {
                setError(R.string.error_invalid_password)
                false
            } else {
                true
            }
        }
    }

    private fun setError(value: Int) {
        _error.value = value
    }

    private fun isPasswordConfirmedValid(): Boolean {
        return if (passwordConfirmed.isNullOrEmpty()) {
            setError(R.string.error_empty_confirm_password_field)
            false
        } else {
            if (password != passwordConfirmed) {
                setError(R.string.error_passwords_did_not_match)
                return false
            }
            return true
        }
    }

    /*private fun getWeather(lat: Double, lon:Double, units: String){
        viewModelScope.launch {
            _weather.value = weatherRepository.getWeather(
                lat,lon, units
            )
        }
    }*/

    fun resetRegisterFlow() {
        _registerFlow.value = null
    }
}
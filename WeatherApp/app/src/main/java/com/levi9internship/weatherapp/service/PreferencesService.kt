package com.levi9internship.weatherapp.service

import android.content.SharedPreferences
import com.google.gson.Gson
import com.levi9internship.weatherapp.*
import com.levi9internship.weatherapp.data.CurrentWeatherDetails
import javax.inject.Inject

class PreferencesService @Inject constructor(
    private val preferences: SharedPreferences
) {

    val gson = Gson()

    fun getNumberOfLocationPrompts(): Int {
        return preferences.getInt(NUMBER_OF_LOCATION_PROMPTS, 0)
    }

    fun setNumberOfLocationPrompts(value: Int) {
        preferences.edit().putInt(NUMBER_OF_LOCATION_PROMPTS, value).apply()
    }

    fun getFirstRun(): Boolean {
        return preferences.getBoolean(FIRST_RUN, true)
    }

    fun setFirstRun(value: Boolean) {
        preferences.edit().putBoolean(FIRST_RUN, value).apply()
    }

    fun getFavorites(): String? {
        return preferences.getString(FAVORITES, gson.toJson(listOf<CurrentWeatherDetails>()))
    }

    fun setFavorites(value: String) {
        preferences.edit().putString(FAVORITES, value).apply()
    }

    fun getActiveLocation(): String? {
        return preferences.getString(ACTIVE_LOCATION, "")
    }

    fun setActiveLocation(value: String) {
        preferences.edit().putString(ACTIVE_LOCATION, value).apply()
    }

    fun getIsActiveCurrent(): Boolean {
        return preferences.getBoolean(IS_ACTIVE_CURRENT, false)
    }

    fun setIsActiveCurrent(value: Boolean) {
        preferences.edit().putBoolean(IS_ACTIVE_CURRENT, value).apply()
    }

    fun getDarkMode(): Boolean {
      return  preferences.getBoolean(DARK_MODE, false)
    }

    fun setDarkMode(value:Boolean) {
        preferences.edit().putBoolean(DARK_MODE, value).apply()
    }

}
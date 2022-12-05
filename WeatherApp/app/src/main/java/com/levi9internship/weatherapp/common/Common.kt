package com.levi9internship.weatherapp.common

import com.levi9internship.weatherapp.R

object IconId {
    fun getIconId(iconName: String): Int = when (iconName) {
        "01d" -> R.drawable._1d
        "02d" -> R.drawable._2d
        "03d" -> R.drawable._3d
        "04d" -> R.drawable._4d
        "09d" -> R.drawable._9d
        "10d" -> R.drawable._10d
        "11d" -> R.drawable._11d
        "13d" -> R.drawable._13d
        "50d" -> R.drawable._50d
        "01n" -> R.drawable._1n
        "02n" -> R.drawable._2n
        "03n" -> R.drawable._3n
        "04n" -> R.drawable._4n
        "09n" -> R.drawable._9n
        "10n" -> R.drawable._10n
        "11n" -> R.drawable._11n
        "13n" -> R.drawable._13n
        "50n" -> R.drawable._50n
        else -> {
            R.drawable.logo
        }
    }
}
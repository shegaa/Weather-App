package com.levi9internship.weatherapp.data

data class FirestoreFavourite(
    var lat: Double = 0.0,
    var lon: Double = 0.0,
    var city: String = "",
    var country: String = ""
)

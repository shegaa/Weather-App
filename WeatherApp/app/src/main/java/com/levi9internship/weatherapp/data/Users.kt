package com.levi9internship.weatherapp.data

data class Users(
    var favourites: List<FirestoreFavourite>,
    var unitSystem: String
)
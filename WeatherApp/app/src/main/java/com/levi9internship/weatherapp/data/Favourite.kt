package com.levi9internship.weatherapp.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "favourite_table")
data class Favourite(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val lat: Double,
    val lon: Double,//if i save data received from api then it wont be up to date
    val city: String,
    val country: String,//saving id, city and country for easier sorting in favourites
    val image: String,
    val temp: Double,
    val humidity: Int,
    val wind: Double
) : Parcelable
package com.levi9internship.weatherapp.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.levi9internship.weatherapp.data.Favourite
import com.levi9internship.weatherapp.data.FirestoreFavourite

interface FirestoreRepository {
    val db: FirebaseFirestore
    val auth: FirebaseAuth
    suspend fun changeUnitSystem(unitSystem: String): Exception?
    suspend fun addFavourite(favourite: FirestoreFavourite): Exception?
    suspend fun removeFavourite(favourite: Favourite): Exception?
    suspend fun getFavourites(): Pair<ArrayList<FirestoreFavourite>, Exception?>
    suspend fun getUnitSystem(): Pair<String, Exception?>
}
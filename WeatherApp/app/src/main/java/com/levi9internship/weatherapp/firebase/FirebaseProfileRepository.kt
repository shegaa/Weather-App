package com.levi9internship.weatherapp.firebase

import com.google.firebase.auth.FirebaseUser

interface FirebaseProfileRepository {
    val currentUser: FirebaseUser?
    suspend fun loginUser(email: String, password: String): Resource<FirebaseUser>
    suspend fun registerUser(email: String, password: String): Resource<FirebaseUser>
    fun logout()
    fun isUserLoggedIn(): Boolean?
}

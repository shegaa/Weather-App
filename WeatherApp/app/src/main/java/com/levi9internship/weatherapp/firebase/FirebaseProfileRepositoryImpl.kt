package com.levi9internship.weatherapp.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.levi9internship.weatherapp.firebase.utils.await
import javax.inject.Inject

class FirebaseProfileRepositoryImpl @Inject constructor(
    private val firebaseAuthInstance: FirebaseAuth
) : FirebaseProfileRepository {
    override val currentUser: FirebaseUser?
        get() = firebaseAuthInstance.currentUser

    override fun isUserLoggedIn(): Boolean {
        firebaseAuthInstance.currentUser?.let {
            return true
        }
        return false
    }

    override suspend fun loginUser(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuthInstance.signInWithEmailAndPassword(email, password).await()

            result.user.let {
                Resource.Success(it!!)
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun registerUser(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result =
                firebaseAuthInstance.createUserWithEmailAndPassword(email, password).await()
            result.user.let {
                Resource.Success(result.user!!)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }

    }

    override fun logout() {
        firebaseAuthInstance.signOut()
    }
}
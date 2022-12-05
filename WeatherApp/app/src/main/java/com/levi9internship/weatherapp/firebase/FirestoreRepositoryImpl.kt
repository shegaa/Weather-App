package com.levi9internship.weatherapp.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.levi9internship.weatherapp.data.Favourite
import com.levi9internship.weatherapp.data.FirestoreFavourite
import com.levi9internship.weatherapp.firebase.utils.await
import com.levi9internship.weatherapp.network.WeatherApiService
import javax.inject.Inject

private const val TAG = "FirestoreRepositoryImpl"

class FirestoreRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : FirestoreRepository {
    override val db: FirebaseFirestore
        get() = firestore
    override val auth: FirebaseAuth
        get() = firebaseAuth

    override suspend fun changeUnitSystem(unitSystem: String): Exception? {
        var exception: Exception? = null
        if (auth.currentUser == null) {
            return exception
        }

        db.collection("users").document(auth.currentUser?.email!!).set(
            hashMapOf("unitSystem" to unitSystem)
        ).addOnFailureListener {
            exception = it
        }
        return exception
    }

    override suspend fun addFavourite(favourite: FirestoreFavourite): Exception? {
        var exception: Exception? = null

        if (auth.currentUser == null) {
            return exception
        }

        db.collection("users").document(auth.currentUser?.email!!).collection("favourites")
            .add(favourite)
            .addOnFailureListener {
                exception = it
            }
        return exception
    }


    override suspend fun removeFavourite(favourite: Favourite): Exception? {
        var exception: Exception? = null
        if (auth.currentUser == null) {
            return exception
        }
        db.collection("users")
            .document(auth.currentUser?.email!!)
            .collection("favourites")
            .whereEqualTo("city", favourite.city)
            .whereEqualTo("country", favourite.country)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.documents.isNotEmpty()) {
                    db.collection("users").document(auth.currentUser?.email!!)
                        .collection("favourites").document(documents.first().id).delete()
                }
            }.addOnFailureListener {
                exception = it
            }
        return exception
    }

    override suspend fun getFavourites(): Pair<ArrayList<FirestoreFavourite>, Exception?> {
        var response: ArrayList<FirestoreFavourite> = ArrayList()
        var exception: Exception? = null

        if (auth.currentUser == null) {
            return Pair(response, exception)
        }

        db.collection("users")
            .document(auth.currentUser?.email!!)
            .collection("favourites")
            .get()
            .addOnSuccessListener { document ->
                response =
                    document.toObjects(FirestoreFavourite::class.java) as ArrayList<FirestoreFavourite>
            }.addOnFailureListener {
                exception = it
            }.await()
        return Pair(response, exception)
    }

    override suspend fun getUnitSystem(): Pair<String, Exception?> {
        var unit = WeatherApiService.UNITS_METRIC
        var exception: Exception? = null
        if (auth.currentUser == null) {
            return Pair(unit, exception)
        }

        db.collection("users").document(auth.currentUser?.email!!).get().addOnSuccessListener {
            unit = it.get("unitSystem") as String
        }.addOnFailureListener {
            exception = it
        }.await()

        return Pair(unit, exception)
    }
}
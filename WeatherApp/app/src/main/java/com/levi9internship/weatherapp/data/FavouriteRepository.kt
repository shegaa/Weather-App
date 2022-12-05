package com.levi9internship.weatherapp.data

import androidx.lifecycle.LiveData
import javax.inject.Inject

class FavouriteRepository @Inject constructor(private val favouriteDao: FavouriteDao) {
    //val readAllData: LiveData<List<Favourite>> = favouriteDao.readAllData()

    fun readAllData(): LiveData<List<Favourite>> {
        return favouriteDao.readAllData()
    }

    suspend fun addFavourite(favourite: Favourite) {
        favouriteDao.addFavourite(favourite)
    }

    suspend fun updateFavourite(favourite: Favourite) {
        favouriteDao.updateFavourite(favourite)
    }

    suspend fun deleteFavourite(favourite: Favourite) {
        favouriteDao.deleteFavourite(favourite)
    }

    suspend fun deleteAllFavourites() {
        favouriteDao.deleteAllFavourites()
    }
}
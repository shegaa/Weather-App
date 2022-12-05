package com.levi9internship.weatherapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavourite(favourite: Favourite)

    @Update
    suspend fun updateFavourite(favourite: Favourite)

    @Delete
    suspend fun deleteFavourite(favourite: Favourite)

    @Query("DELETE FROM favourite_table")
    suspend fun deleteAllFavourites()

    @Query("SELECT * FROM favourite_table ORDER BY ID ASC")
    fun readAllData(): LiveData<List<Favourite>>
}
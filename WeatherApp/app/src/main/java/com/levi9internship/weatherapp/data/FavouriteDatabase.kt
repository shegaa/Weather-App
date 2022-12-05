package com.levi9internship.weatherapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Favourite::class], version = 3, exportSchema = false)
abstract class FavouriteDatabase : RoomDatabase() {

    abstract fun favouriteDao(): FavouriteDao

    companion object {
        @Volatile
        private var INSTANCE: FavouriteDatabase? = null

        fun getDatabase(context: Context): FavouriteDatabase {
            val tempInstance = INSTANCE
            if (tempInstance !== null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FavouriteDatabase::class.java,
                    "favourite_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

}
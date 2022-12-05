package com.levi9internship.weatherapp.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.levi9internship.weatherapp.data.FavouriteDatabase
import com.levi9internship.weatherapp.firebase.FirebaseProfileRepository
import com.levi9internship.weatherapp.firebase.FirebaseProfileRepositoryImpl
import com.levi9internship.weatherapp.firebase.FirestoreRepository
import com.levi9internship.weatherapp.firebase.FirestoreRepositoryImpl
import com.levi9internship.weatherapp.network.ForecastApiService
import com.levi9internship.weatherapp.network.LocationApiService
import com.levi9internship.weatherapp.network.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRoomDatabase(@ApplicationContext app: Context) =
        Room.databaseBuilder(
            app,
            FavouriteDatabase::class.java,
            "favourite_database"
        ).build()

    @Provides
    @Singleton
    fun provideFavouriteDao(db: FavouriteDatabase) = db.favouriteDao()

    @Provides
    @Singleton
    fun provideFirestoreRepository(impl: FirestoreRepositoryImpl): FirestoreRepository =
        impl

    @Provides
    @Singleton
    fun provideFirestoreInstance(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseProfileRepository(impl: FirebaseProfileRepositoryImpl): FirebaseProfileRepository =
        impl

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()


    @Provides
    @Singleton
    @Named("Weather")
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(WeatherApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("Location")
    fun provideRetrofitLocation(): Retrofit =
        Retrofit.Builder()
            .baseUrl(LocationApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("Forecast")
    fun provideRetrofitForecast(): Retrofit =
        Retrofit.Builder()
            .baseUrl(ForecastApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideWeatherApiService(@Named("Weather") retrofit: Retrofit): WeatherApiService =
        retrofit.create(WeatherApiService::class.java)

    @Provides
    @Singleton
    fun provideLocationApiService(@Named("Location") retrofit: Retrofit): LocationApiService =
        retrofit.create(LocationApiService::class.java)

    @Provides
    @Singleton
    fun provideForecastApiService(@Named("Forecast") retrofit: Retrofit): ForecastApiService =
        retrofit.create(ForecastApiService::class.java)

}
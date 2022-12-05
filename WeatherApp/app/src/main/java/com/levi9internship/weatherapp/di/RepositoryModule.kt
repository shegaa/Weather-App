package com.levi9internship.weatherapp.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.levi9internship.weatherapp.data.FavouriteRepository
import com.levi9internship.weatherapp.network.*
import com.levi9internship.weatherapp.service.FavoritesService
import com.levi9internship.weatherapp.service.LocationsService
import com.levi9internship.weatherapp.service.PreferencesService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@InstallIn(ActivityRetainedComponent::class)
@Module
object RepositoryModule {

    @Provides
    fun providesDataRepo(apiService: WeatherApiService): WeatherRepository {
        return WeatherRepository(apiService)
    }

    @Provides
    fun providesLocationDataRepo(apiService: LocationApiService): LocationRepository {
        return LocationRepository(apiService)
    }

    @Provides
    fun providesForecastDataRepo(apiService: ForecastApiService): ForecastRepository {
        return ForecastRepository(apiService)
    }

    @Provides
    fun provideFavoritesService(
        preferences: PreferencesService,
        roomRepository: FavouriteRepository
    ): FavoritesService =
        FavoritesService(preferences, roomRepository)

    @Provides
    fun provideLocationsService(@ApplicationContext appContext: Context): LocationsService =
        LocationsService(appContext)

    @Provides
    fun provideSharedPreferences(@ApplicationContext appContext: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(appContext)
}
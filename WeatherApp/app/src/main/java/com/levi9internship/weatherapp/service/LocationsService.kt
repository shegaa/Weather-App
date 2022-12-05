package com.levi9internship.weatherapp.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat

class LocationsService(private var context: Context) {

    fun hasLocationAccess(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasGpsTurnedOn(): Boolean {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return gpsEnabled
    }
}
package com.levi9internship.weatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.levi9internship.weatherapp.R
import com.levi9internship.weatherapp.common.IconId
import com.levi9internship.weatherapp.data.CurrentWeatherDetails
import com.levi9internship.weatherapp.data.FavouriteDatabaseViewModel
import com.levi9internship.weatherapp.databinding.FragmentMapBinding
import com.levi9internship.weatherapp.network.WeatherApiService
import com.levi9internship.weatherapp.service.LocationsService
import com.levi9internship.weatherapp.viewmodel.FavoriteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

private const val SHORT_VIBRATION_DURATION = 200L
private const val STARTER_LAT = 49.0
private const val STARTER_LON = 13.0
private const val ZOOM_LEVEL = 5.0F

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback, OnMarkerClickListener {

    private var binding: FragmentMapBinding? = null
    private lateinit var googleMap: GoogleMap
    private val viewModel: FavoriteViewModel by viewModels()
    private lateinit var mFavouriteDatabaseViewModel: FavouriteDatabaseViewModel
    private var isMapLoaded: Boolean = false
    private var vibration: Vibrator? = null
    private var lastLatLon: LatLng? = null

    @Inject
    lateinit var locationsService: LocationsService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        FragmentMapBinding.inflate(inflater, container, false).apply {
            binding = this
            return root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vibration = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

        mFavouriteDatabaseViewModel =
            ViewModelProvider(this).get(FavouriteDatabaseViewModel::class.java)

        viewModel.unit.observe(this.viewLifecycleOwner) {
            mFavouriteDatabaseViewModel.readAllData.observe(
                viewLifecycleOwner,
                androidx.lifecycle.Observer { favourite ->
                    var finalList = mutableListOf<CurrentWeatherDetails>()
                    for (i in favourite.indices) {
                        var details = CurrentWeatherDetails()
                        details.id = favourite[i].id
                        details.lat = favourite[i].lat
                        details.lon = favourite[i].lon
                        details.cityName = favourite[i].city
                        details.country = favourite[i].country
                        details.icon = favourite[i].image
                        details.currentTemperature = favourite[i].temp.roundToInt()
                        details.humidity = favourite[i].humidity
                        details.windSpeed = favourite[i].wind.roundToInt()
                        finalList.add(i, details)
                    }

                    viewModel.setFavoriteLocations(finalList)
                    lifecycleScope.launch {
                        viewModel.getFavoriteLocationsWeather()
                    }

                    if (locationsService.hasLocationAccess()) {
                        refreshLocation()
                    }
                })
        }

        binding?.apply {
            map.onCreate(savedInstanceState)
            map.onResume()
            map.getMapAsync(this@MapFragment)
        }

        viewModel.favoriteLocations.observe(this.viewLifecycleOwner) {
            if (isMapLoaded) {
                refreshMarkers()
                placeUserLocationMarker(false)
            }
        }

        viewModel.weather.observe(this.viewLifecycleOwner) {
            if (isMapLoaded) {
                refreshMarkers()
                if (lastLatLon == null) {
                    placeUserLocationMarker(true)
                } else {
                    placeUserLocationMarker(false)
                }
                if (locationsService.hasGpsTurnedOn() && locationsService.hasLocationAccess()) {
                    val userLatLng =
                        LatLng(viewModel.weather.value!!.lat, viewModel.weather.value!!.lon)

                    if (lastLatLon == null) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng))
                    }
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(STARTER_LAT, STARTER_LON)))
        if (locationsService.hasGpsTurnedOn() && locationsService.hasLocationAccess()) {
            googleMap.moveCamera(CameraUpdateFactory.zoomBy(ZOOM_LEVEL))
        }
        isMapLoaded = true
        refreshMarkers()
        placeUserLocationMarker(true)

        googleMap.setOnMarkerClickListener(this)
        googleMap.setOnMapClickListener {
            binding?.apply {
                cardView.visibility = View.INVISIBLE
            }
        }

        googleMap.setOnMapLongClickListener {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            var location: Address
            try {
                location = geocoder.getFromLocation(
                    it.latitude,
                    it.longitude,
                    1
                )[0]

                var newFavorite = CurrentWeatherDetails()
                newFavorite.lat = it.latitude
                newFavorite.lon = it.longitude
                newFavorite.cityName = location.locality
                newFavorite.country = location.countryName

                if (!viewModel.isLocationInFavorites(newFavorite)) {
                    vibration?.vibrate(SHORT_VIBRATION_DURATION)
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.add_new_favorite)
                        .setMessage(
                            resources.getString(
                                R.string.add_new_favorite_text,
                                location.locality
                            )
                        )
                        .setPositiveButton(
                            R.string.yes,
                            DialogInterface.OnClickListener { _, _ ->
                                lastLatLon =
                                    LatLng(newFavorite.lat, newFavorite.lon)
                                viewModel.addNewFavorite(newFavorite)
                            })
                        .setNegativeButton(R.string.no, null)
                        .show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun refreshMarkers() {
        googleMap.clear()
        for (favorite in viewModel.favoriteLocations.value!!) {
            val latLng = LatLng(favorite.lat, favorite.lon)
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(favorite.cityName)
                    .snippet(
                        favorite.currentTemperature.toString() + getUnitsString()
                    )
            )
        }
    }

    private fun placeUserLocationMarker(markAsActive: Boolean) {
        if (locationsService.hasGpsTurnedOn() && locationsService.hasLocationAccess() && viewModel.weather.value?.weatherDescription != "") {
            val userLatLng = LatLng(viewModel.weather.value!!.lat, viewModel.weather.value!!.lon)
            val marker =
                MarkerOptions()
                    .position(userLatLng)
                    .title(resources.getString(R.string.current_location))
                    .snippet(
                        viewModel.weather.value!!.currentTemperature.toString() + getUnitsString()
                    )
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

            googleMap.addMarker(marker)
        }
    }

    private fun refreshLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener {
                    if (it != null) {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        try {
                            viewModel.setUserLocation(
                                geocoder.getFromLocation(
                                    it.latitude,
                                    it.longitude,
                                    1
                                )[0]
                            )
                            lifecycleScope.launch {
                                viewModel.getWeather()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onMarkerClick(marker: Marker): Boolean {
        val lat = marker.position.latitude
        val lon = marker.position.longitude

        val weather = viewModel.findByLatLon(lat, lon)

        if (weather != null) {
            binding?.apply {
                textCity.text = weather.cityName
                textWeatherDescription.text =
                    weather.weatherDescription + ", " +
                            weather.currentTemperature.toString() +
                            getUnitsString()

                imageIcon.setImageResource(IconId.getIconId(weather.icon))
                cardView.visibility = View.VISIBLE
            }
        } else {
            binding?.apply {
                cardView.visibility = View.INVISIBLE
            }
        }
        return false
    }

    private fun getUnitsString(): String {
        return if (viewModel.unit.value == WeatherApiService.UNITS_METRIC) {
            getString(R.string.celsius)
        } else {
            getString(R.string.fahrenheit)
        }
    }

}
package com.levi9internship.weatherapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.levi9internship.weatherapp.MainActivity
import com.levi9internship.weatherapp.R
import com.levi9internship.weatherapp.adapter.HomeForecastAdapter
import com.levi9internship.weatherapp.data.CurrentWeatherDetails
import com.levi9internship.weatherapp.data.FavouriteDatabaseViewModel
import com.levi9internship.weatherapp.databinding.FragmentHomeBinding
import com.levi9internship.weatherapp.navigation.Navigation
import com.levi9internship.weatherapp.network.WeatherApiService
import com.levi9internship.weatherapp.service.FavoritesService
import com.levi9internship.weatherapp.service.LocationsService
import com.levi9internship.weatherapp.service.PreferencesService
import com.levi9internship.weatherapp.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


private const val TAG = "HomeFragment"

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var navigation: Navigation
    private lateinit var mFavouriteDatabaseViewModel: FavouriteDatabaseViewModel


    @Inject
    lateinit var favoritesService: FavoritesService

    @Inject
    lateinit var preferencesService: PreferencesService

    @Inject
    lateinit var locationsService: LocationsService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        navigation = (activity as MainActivity).navigation

        FragmentHomeBinding.inflate(inflater, container, false).apply {
            binding = this
            return root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    (activity as MainActivity).customOnBackPressed(R.id.homeFragment)
                }
            })
        mFavouriteDatabaseViewModel =
            ViewModelProvider(this).get(FavouriteDatabaseViewModel::class.java)

        homeViewModel.unit.observe(this.viewLifecycleOwner) {
            mFavouriteDatabaseViewModel.readAllData.observe(
                viewLifecycleOwner,
                androidx.lifecycle.Observer { favourite ->
                    if (favourite.isEmpty()) {
                        refreshLocation()
                    }

                    if (!preferencesService.getFirstRun()) {
                        homeViewModel.unit.observe(this.viewLifecycleOwner) {
                            showLocation()
                        }
                    }

                    homeViewModel.formatWeatherDetails()
                })
        }

        val adapter = HomeForecastAdapter(requireContext())

        binding?.apply {
            refreshForecastLoader()
            refreshTemperatureLoader()

            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(true)
            recyclerView.isNestedScrollingEnabled = false

            viewModel = homeViewModel
            lifecycleOwner = viewLifecycleOwner

            cardTurnOnLocation.setOnClickListener {
                navigation.homeToLocationAlert()
            }

            if (locationsService.hasLocationAccess()) {
                cardTurnOnLocation.visibility = View.GONE
            } else {
                cardTurnOnLocation.visibility = View.VISIBLE
            }
        }

        homeViewModel.weather.observe(this.viewLifecycleOwner) {
            homeViewModel.formatWeatherDetails()
            refreshTemperatureLoader()
            refreshWeatherDetails()
        }

        homeViewModel.airPollution.observe(this.viewLifecycleOwner) {
            homeViewModel.formatWeatherDetails()
        }

        homeViewModel.userLocation.observe(this.viewLifecycleOwner) {
            homeViewModel.formatWeatherDetails()
            (activity as MainActivity).toolbar.title = it.locality
        }

        homeViewModel.forecast.observe(this.viewLifecycleOwner) {
            adapter.setForecastData(it.list, homeViewModel.unit.value!!)
            adapter.notifyDataSetChanged()
            refreshForecastLoader()
        }

    }

    private fun refreshWeatherDetails() {
        binding?.apply {
            if (homeViewModel.unit.value == WeatherApiService.UNITS_IMPERIAL) {
                textDegrees.text = getString(R.string.fahrenheit)
                textWindSpeed.text = getString(
                    R.string.wind_speed_imperial,
                    homeViewModel.weatherDetails.value?.windSpeed
                )
                textRealFeelValue.text = getString(
                    R.string.fahrenheit_formatted,
                    homeViewModel.weatherDetails.value?.realFeel
                )
            } else {
                textDegrees.text = getString(R.string.celsius)
                textWindSpeed.text =
                    getString(R.string.wind_speed, homeViewModel.weatherDetails.value?.windSpeed)
                textRealFeelValue.text = getString(
                    R.string.celsius_formatted,
                    homeViewModel.weatherDetails.value?.realFeel
                )
            }
        }
    }

    private fun showLocation() {
        if (preferencesService.getIsActiveCurrent()) {
            if (hasLocationAndGps()) {
                refreshLocation()
            } else if (favoritesService.getAllFavorites().isEmpty()) {
                navigation.homeToNoFavorites()
            } else {
                val firstLocation = favoritesService.getAllFavorites()[0]
                refreshFavoriteLocation(firstLocation)
            }
        } else {
            val activeLocation = favoritesService.getActiveLocation()
            if (activeLocation != null) {
                refreshFavoriteLocation(activeLocation)
            } else {
                if (hasLocationAndGps()) {
                    refreshLocation()
                } else {
                    navigation.homeToNoFavorites()
                }
            }
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
                            homeViewModel.setUserLocation(
                                geocoder.getFromLocation(
                                    it.latitude,
                                    it.longitude,
                                    1
                                )[0]
                            )
                            lifecycleScope.launch {
                                homeViewModel.getWeather()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
        }
    }

    private fun refreshFavoriteLocation(activeLocation: CurrentWeatherDetails) {
        var address = Address(Locale.getDefault())
        address.locality = activeLocation.cityName
        address.latitude = activeLocation.lat
        address.longitude = activeLocation.lon
        homeViewModel.setUserLocation(address)
        lifecycleScope.launch {
            homeViewModel.getWeather()
        }
    }

    private fun hasLocationAndGps(): Boolean {
        return locationsService.hasLocationAccess() && locationsService.hasGpsTurnedOn()
    }

    private fun refreshTemperatureLoader() {
        if (homeViewModel.weatherDetails.value?.weatherDescription != "") {
            binding?.layoutCurrentTemperature?.visibility = View.VISIBLE
            binding?.progressBarTemperature?.visibility = View.INVISIBLE
        } else {
            binding?.layoutCurrentTemperature?.visibility = View.INVISIBLE
            binding?.progressBarTemperature?.visibility = View.VISIBLE
        }
    }

    private fun refreshForecastLoader() {
        if (homeViewModel.forecast.value?.list?.size == 40) {
            binding?.recyclerView?.visibility = View.VISIBLE
            binding?.progressBarForecast?.visibility = View.INVISIBLE
        } else {
            binding?.recyclerView?.visibility = View.INVISIBLE
            binding?.progressBarForecast?.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}
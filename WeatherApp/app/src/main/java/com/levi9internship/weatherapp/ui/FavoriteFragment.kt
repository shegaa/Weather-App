package com.levi9internship.weatherapp.ui

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.levi9internship.weatherapp.MainActivity
import com.levi9internship.weatherapp.R
import com.levi9internship.weatherapp.adapter.FavoriteLocationsAdapter
import com.levi9internship.weatherapp.data.CurrentWeatherDetails
import com.levi9internship.weatherapp.data.Favourite
import com.levi9internship.weatherapp.data.FavouriteDatabaseViewModel
import com.levi9internship.weatherapp.databinding.FragmentFavoriteBinding
import com.levi9internship.weatherapp.navigation.Navigation
import com.levi9internship.weatherapp.service.FavoritesService
import com.levi9internship.weatherapp.service.LocationsService
import com.levi9internship.weatherapp.service.PreferencesService
import com.levi9internship.weatherapp.viewmodel.FavoriteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt


@AndroidEntryPoint
class FavoriteFragment : Fragment() {

    private var binding: FragmentFavoriteBinding? = null
    private val viewModel: FavoriteViewModel by viewModels()
    private lateinit var navigation: Navigation
    private lateinit var mFavouriteDatabaseViewModel: FavouriteDatabaseViewModel

    @Inject
    lateinit var preferencesService: PreferencesService

    @Inject
    lateinit var favoritesService: FavoritesService

    @Inject
    lateinit var locationsService: LocationsService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        navigation = (activity as MainActivity).navigation

        FragmentFavoriteBinding.inflate(inflater, container, false).apply {
            binding = this
            return root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.error.observe(viewLifecycleOwner) { exception ->
            Snackbar.make(binding!!.root, exception.message.toString(), Snackbar.LENGTH_LONG)
                .setAction("More details") {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Error")
                        .setMessage(exception.toString())
                        .setPositiveButton("Ok") { dialog, _ ->
                            dialog.cancel()
                        }
                        .show()
                }
                .show()
        }

        var adapter = FavoriteLocationsAdapter(
            requireContext(),
            preferencesService,
            favoritesService,
            viewModel,
            (activity as MainActivity),
            object : FavoriteLocationsAdapter.OnRemovedListener {
                override fun onRemoved(favourite: Favourite) {
                    viewModel.deleteFavourite(favourite)
                }
            })

        mFavouriteDatabaseViewModel =
            ViewModelProvider(this).get(FavouriteDatabaseViewModel::class.java)

        viewModel.unit.observe(this.viewLifecycleOwner) {
            mFavouriteDatabaseViewModel.readAllData.observe(
                viewLifecycleOwner,
                androidx.lifecycle.Observer { favourite ->
                    val finalList = mutableListOf<CurrentWeatherDetails>()
                    for (i in favourite.indices) {
                        val details = CurrentWeatherDetails()
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

                    adapter.units = viewModel.unit.value!!

                    viewModel.setFavoriteLocations(finalList)
                    lifecycleScope.launch{
                        viewModel.getFavoriteLocationsWeather()
                        adapter.favoriteLocations = viewModel.favoriteLocations.value
                    }

                    if (locationsService.hasLocationAccess()) {
                        refreshLocation()
                    }
                })
        }

        binding?.apply {
            recyclerView.adapter = adapter
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            recyclerView.isNestedScrollingEnabled = true
            recyclerView.setHasFixedSize(false)

            (activity as MainActivity).editButton.setOnClickListener {
                adapter.toggleEditMode()
            }
        }

        viewModel.weather.observe(this.viewLifecycleOwner) {
            adapter.currentLocation = it
            adapter.notifyItemChanged(0)
        }

        viewModel.favoriteLocations.observe(this.viewLifecycleOwner) {
            if (adapter.favoriteLocations?.size!! <= it.size) {
                adapter.favoriteLocations = it
                adapter.notifyItemRangeChanged(1, adapter.itemCount - 1)
            } else {
                adapter.favoriteLocations = it
            }
            setEditButtonVisibility(adapter)
        }

        viewModel.activeLocation.observe(this.viewLifecycleOwner) {
            setEditButtonVisibility(adapter)
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (adapter.editMode) {
                        adapter.toggleEditMode()
                    } else {
                        (activity as MainActivity).customOnBackPressed(R.id.favoriteFragment)
                    }
                }
            })
    }

    private fun setEditButtonVisibility(adapter: FavoriteLocationsAdapter) {
        if (viewModel.favoriteLocations.value?.size == 0 || (viewModel.favoriteLocations.value?.size == 1 && !preferencesService.getIsActiveCurrent())
        ) {
            (activity as MainActivity).editButton.visibility = View.INVISIBLE
            adapter.editMode = false
        } else {
            (activity as MainActivity).editButton.visibility = View.VISIBLE
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

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}
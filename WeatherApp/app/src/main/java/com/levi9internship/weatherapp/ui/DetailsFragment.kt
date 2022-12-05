package com.levi9internship.weatherapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.levi9internship.weatherapp.MainActivity
import com.levi9internship.weatherapp.R
import com.levi9internship.weatherapp.adapter.DetailsHourlyAdapter
import com.levi9internship.weatherapp.common.IconId
import com.levi9internship.weatherapp.data.CurrentWeatherDetails
import com.levi9internship.weatherapp.data.Favourite
import com.levi9internship.weatherapp.data.FirestoreFavourite
import com.levi9internship.weatherapp.databinding.FragmentDetailsBinding
import com.levi9internship.weatherapp.decoration.HourlyItemDecorator
import com.levi9internship.weatherapp.navigation.Navigation
import com.levi9internship.weatherapp.network.WeatherApiService
import com.levi9internship.weatherapp.service.FavoritesService
import com.levi9internship.weatherapp.viewmodel.DetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

private const val TAG = "DetailsFragment"

@AndroidEntryPoint
class DetailsFragment : Fragment() {

    private var binding: FragmentDetailsBinding? = null
    private val viewModel: DetailsViewModel by viewModels()

    private lateinit var navigation: Navigation

    @Inject
    lateinit var favoritesService: FavoritesService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        arguments?.let {
            viewModel.setLatAndLon(
                it.getString(LAT).toString(),
                it.getString(LON).toString()
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        navigation = (activity as MainActivity).navigation
        viewModel.unit.observe(this.viewLifecycleOwner) {
            viewModel.getWeather()
            viewModel.getForecast()
        }

        FragmentDetailsBinding.inflate(inflater, container, false).apply {
            binding = this
            return root
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    (activity as MainActivity).customOnBackPressed(R.id.detailsFragment)
                }
            })
        binding?.progressBarCurrent?.visibility = View.VISIBLE
        binding?.currentData?.visibility = View.INVISIBLE
        binding?.hourlyRv?.visibility = View.INVISIBLE
        binding?.additionalData?.visibility = View.INVISIBLE
        binding?.addButton?.visibility = View.INVISIBLE

        viewModel.errorFirestore.observe(viewLifecycleOwner) { exception ->
            Snackbar.make(binding!!.root, exception.message.toString(), Snackbar.LENGTH_LONG)
                .setAction("More details") {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Error")
                        .setMessage(exception.toString())
                        .setPositiveButton("Ok") { dialog, _ ->
                            dialog.cancel()
                        }
                        .show()
                }.show()
        }

        binding?.hourlyRv?.addItemDecoration(HourlyItemDecorator(22))//set dimension const instead TODO
        val adapter = DetailsHourlyAdapter(requireContext())
        binding?.hourlyRv?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding?.hourlyRv?.setHasFixedSize(true)

        binding?.hourlyRv?.adapter = adapter

        viewModel.weather.observe(this.viewLifecycleOwner) {
            binding?.apply {
                progressBarCurrent.visibility = View.INVISIBLE
                currentData.visibility = View.VISIBLE
                hourlyRv.visibility = View.VISIBLE
                additionalData.visibility = View.VISIBLE
                addButton.visibility = View.VISIBLE
                currentTemp.text =
                    viewModel.weather.value?.main?.temp?.roundToInt()
                        .toString() + "°"//doable in viewmodel
                currentCity.text = viewModel.weather.value?.name
                currentCountry.text = viewModel.weather.value?.sys?.country
                visibilityValue.text =
                    viewModel.weather.value?.visibility.toString() + " m"//TODO set unit once shared preferences is set up

                cloudinessValue.text = viewModel.weather.value?.clouds?.all.toString() + "%"
                humidityPercentage.text =
                    viewModel.weather.value?.main?.humidity.toString() + "%"
                pressureValue.text =
                    viewModel.weather.value?.main?.pressure.toString() + " mBar"//TODO set unit once shared preferences is set up
                weatherIcon.setImageResource(IconId.getIconId(viewModel.weather.value!!.weather[0].icon))

                if (viewModel.unit.value == WeatherApiService.UNITS_METRIC) {
                    currentTemp.text = resources.getString(R.string.celsius_formatted,viewModel.weather.value?.main?.temp?.roundToInt())
                    windSpeed.text = resources.getString(R.string.wind_speed, viewModel.weather.value?.wind?.speed?.roundToInt())
                    realFeelValue.text = resources.getString(R.string.celsius_formatted, viewModel.weather.value?.main?.feels_like?.roundToInt())
                } else {
                    currentTemp.text = resources.getString(R.string.fahrenheit_formatted,viewModel.weather.value?.main?.temp?.roundToInt())
                    windSpeed.text = resources.getString(R.string.wind_speed_imperial, viewModel.weather.value?.wind?.speed?.roundToInt())
                    realFeelValue.text = resources.getString(R.string.fahrenheit_formatted, viewModel.weather.value?.main?.feels_like?.roundToInt())
                }

                val cityName = viewModel.weather.value?.name
                val country = viewModel.weather.value?.sys!!.country
                if (cityName != null && country != null) {
                    var newFavoriteLocation = CurrentWeatherDetails()
                    newFavoriteLocation.cityName = cityName
                    newFavoriteLocation.country = country
                    if (favoritesService.isLocationInFavorites(newFavoriteLocation)) {
                        binding?.addButton?.visibility = View.INVISIBLE
                    } else {
                        binding?.addButton?.visibility = View.VISIBLE
                    }
                }
            }

            viewModel.forecast.observe(this.viewLifecycleOwner) {
                adapter.units = viewModel.unit.value!!
                adapter.setData(viewModel.forecast.value!!.list)
                adapter.notifyDataSetChanged()
            }

            binding!!.addButton.setOnClickListener {
                val favourite =
                    Favourite(//mogu ove konstruktore pozivati i kao parametar ali ovako ljepse izgleda
                        viewModel.weather.value!!.id,
                        viewModel.lat.value!!.toDouble(),
                        viewModel.lon.value!!.toDouble(),
                        viewModel.weather.value!!.name,
                        viewModel.weather.value!!.sys.country,
                        viewModel.weather.value!!.weather[0].icon,
                        viewModel.weather.value!!.main.temp,
                        viewModel.weather.value!!.main.humidity,
                        viewModel.weather.value!!.wind.speed
                    )
                viewModel.addToRoom(favourite)

                binding?.addButton?.visibility = View.INVISIBLE
                val firestoreFavourite = FirestoreFavourite(
                    viewModel.lat.value!!.toDouble(),
                    viewModel.lon.value!!.toDouble(),
                    viewModel.weather.value!!.name,
                    viewModel.weather.value!!.sys.country
                )
                viewModel.addToFirestore(firestoreFavourite)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object {
        const val LAT = "lat"
        const val LON = "lon"
    }
}
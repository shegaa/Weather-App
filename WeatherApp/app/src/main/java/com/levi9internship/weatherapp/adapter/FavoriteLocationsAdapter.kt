package com.levi9internship.weatherapp.adapter

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.levi9internship.weatherapp.MainActivity
import com.levi9internship.weatherapp.R
import com.levi9internship.weatherapp.data.CurrentWeatherDetails
import com.levi9internship.weatherapp.data.Favourite
import com.levi9internship.weatherapp.databinding.ItemCurrentLocationBinding
import com.levi9internship.weatherapp.databinding.ItemFavoriteLocationBinding
import com.levi9internship.weatherapp.network.WeatherApiService
import com.levi9internship.weatherapp.service.FavoritesService
import com.levi9internship.weatherapp.service.PreferencesService
import com.levi9internship.weatherapp.viewmodel.FavoriteViewModel

private const val SHORT_VIBRATION_DURATION = 200L
private const val STROKE_WIDTH = 8


class FavoriteLocationsAdapter(
    private var context: Context,
    private var preferencesService: PreferencesService,
    private var favoritesService: FavoritesService,
    private var viewModel: FavoriteViewModel,
    private var activity: MainActivity,
    val listener: OnRemovedListener
) :
    RecyclerView.Adapter<FavoriteLocationsAdapter.ViewHolder>() {

    var currentLocation: CurrentWeatherDetails? = viewModel.weather.value
    var favoriteLocations: MutableList<CurrentWeatherDetails>? = viewModel.favoriteLocations.value
    var editMode: Boolean = false
    var units: String = WeatherApiService.UNITS_METRIC
    private val vibration = context.getSystemService(VIBRATOR_SERVICE) as Vibrator?

    abstract inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(weather: CurrentWeatherDetails)
    }

    inner class FavoriteLocationViewHolder(private var binding: ItemFavoriteLocationBinding) :
        FavoriteLocationsAdapter.ViewHolder(binding.root) {
        override fun bind(weather: CurrentWeatherDetails) {
            binding.apply {
                forecast = weather

                if (units == WeatherApiService.UNITS_METRIC) {
                    textUnit.text = context.resources.getString(R.string.celsius)
                    textWindSpeed.text = context.resources.getString(R.string.wind_speed, weather.windSpeed)
                } else {
                    textUnit.text = context.resources.getString(R.string.fahrenheit)
                    textWindSpeed.text = context.resources.getString(R.string.wind_speed_imperial, weather.windSpeed)
                }

                if (favoritesService.isActiveLocation(weather)) {
                    cardView.strokeColor = context.resources.getColor(R.color.white)
                    cardView.strokeWidth = STROKE_WIDTH
                } else {
                    cardView.strokeWidth = 0
                    cardView.setOnLongClickListener {
                        if (!editMode) {
                            setActiveLocation(weather)
                            true
                        } else {
                            false
                        }
                    }
                }

                if (editMode && !favoritesService.isActiveLocation(weather)) {
                    imageCancel.visibility = View.VISIBLE
                    imageCancel.setOnClickListener {
                        removeFavorite(weather, adapterPosition)
                        var favourite: Favourite = Favourite(
                            weather.id,
                            weather.lat,
                            weather.lon,
                            weather.cityName,
                            weather.country,
                            weather.icon,
                            weather.currentTemperature.toDouble(),
                            weather.humidity,
                            weather.windSpeed.toDouble()
                        )
                        listener.onRemoved(favourite)
                    }
                } else {
                    imageCancel.visibility = View.INVISIBLE
                }
            }
        }
    }

    inner class CurrentLocationViewHolder(private var binding: ItemCurrentLocationBinding) :
        FavoriteLocationsAdapter.ViewHolder(binding.root) {
        override fun bind(weather: CurrentWeatherDetails) {
            binding.apply {
                forecast = weather

                if (units == WeatherApiService.UNITS_METRIC) {
                    textUnit.text = context.resources.getString(R.string.celsius)
                    textWindSpeed.text = context.resources.getString(R.string.wind_speed, weather.windSpeed)
                } else {
                    textUnit.text = context.resources.getString(R.string.fahrenheit)
                    textWindSpeed.text = context.resources.getString(R.string.wind_speed_imperial, weather.windSpeed)
                }

                if (currentLocation?.weatherDescription == "") {
                    layoutEnableLocation.visibility = View.VISIBLE
                    layoutLocationDetails.visibility = View.INVISIBLE
                } else {
                    layoutEnableLocation.visibility = View.INVISIBLE
                    layoutLocationDetails.visibility = View.VISIBLE

                    if (preferencesService.getIsActiveCurrent()) {
                        cardView.strokeColor = context.resources.getColor(R.color.white)
                        cardView.strokeWidth = STROKE_WIDTH
                    } else {
                        cardView.strokeWidth = 0
                        cardView.setOnLongClickListener {
                            if (!editMode) {
                                setActiveLocationToCurrent()
                                true
                            } else {
                                false
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavoriteLocationsAdapter.ViewHolder {
        return if (viewType == 1) {
            FavoriteLocationViewHolder(
                ItemFavoriteLocationBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
        } else {
            CurrentLocationViewHolder(
                ItemCurrentLocationBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: FavoriteLocationsAdapter.ViewHolder, position: Int) {
        if (position == 0) {
            holder.bind(currentLocation!!)
        } else {
            holder.bind(favoriteLocations?.get(position - 1)!!)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            0
        } else {
            1
        }
    }

    override fun getItemCount(): Int {
        return favoriteLocations?.size?.plus(1) ?: 1
    }

    private fun setActiveLocation(weather: CurrentWeatherDetails) {
        vibration?.vibrate(SHORT_VIBRATION_DURATION)
        favoritesService.setActiveLocation(weather)
        viewModel.setActiveLocation(weather)
        notifyDataSetChanged()
    }

    private fun setActiveLocationToCurrent() {
        vibration?.vibrate(SHORT_VIBRATION_DURATION)
        favoritesService.setActiveLocationToCurrent()
        viewModel.setActiveLocation(CurrentWeatherDetails())
        notifyDataSetChanged()
    }

    fun toggleEditMode() {
        editMode = !editMode
        notifyDataSetChanged()
    }

    fun removeFavorite(favorite: CurrentWeatherDetails, index: Int) {
        notifyItemRemoved(index)
        favoritesService.removeFavorite(favorite)
        viewModel.removeAtIndex(index - 1)
        if (viewModel.favoriteLocations.value?.isEmpty() == true) {
            activity.navigateToSearch()
        }
    }

    interface OnRemovedListener {
        fun onRemoved(favourite: Favourite)
    }

}
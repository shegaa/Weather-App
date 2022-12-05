package com.levi9internship.weatherapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.levi9internship.weatherapp.R
import com.levi9internship.weatherapp.data.CurrentWeatherDetails
import com.levi9internship.weatherapp.data.weather.WeatherResponse
import com.levi9internship.weatherapp.databinding.WeatherForecastListItemBinding
import com.levi9internship.weatherapp.network.WeatherApiService
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class HomeForecastAdapter(context: Context) :
    RecyclerView.Adapter<HomeForecastAdapter.HomeForecastViewHolder>() {

    val context = context
    var forecast: MutableList<WeatherResponse> = mutableListOf()
    var dailyForecast: MutableList<CurrentWeatherDetails> = mutableListOf()
    var binding: WeatherForecastListItemBinding? = null
    var unit: String = WeatherApiService.UNITS_METRIC

    inner class HomeForecastViewHolder(private var binding: WeatherForecastListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: CurrentWeatherDetails) {
            binding.forecast = forecast

            binding.apply {
                if (adapterPosition == 0) {
                    textDay.setText(R.string.today)
                } else if (adapterPosition == 1) {
                    textDay.setText(R.string.tomorrow)
                } else {
                    val formatter = SimpleDateFormat("EEE")
                    textDay.text = formatter.format(dailyForecast[adapterPosition].currentDate)
                }

                textWeatherType.text = dailyForecast[adapterPosition].weatherDescription

                if (unit == WeatherApiService.UNITS_METRIC) {
                    textMinMaxTemperature.text = context.getString(
                        R.string.max_min_temperature,
                        dailyForecast[adapterPosition].maxTemp,
                        dailyForecast[adapterPosition].minTemp)
                } else {
                    textMinMaxTemperature.text = context.getString(
                        R.string.max_min_temperature_imperial,
                        dailyForecast[adapterPosition].maxTemp,
                        dailyForecast[adapterPosition].minTemp)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeForecastViewHolder {
        return HomeForecastViewHolder(
            WeatherForecastListItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: HomeForecastViewHolder, position: Int) {
        holder.bind(dailyForecast[position])
    }

    @SuppressLint("SimpleDateFormat")
    fun setForecastData(data: List<WeatherResponse>, units: String) {
        dailyForecast = mutableListOf()
        forecast = mutableListOf()
        unit = units

        for (currentForecast in data) {
            val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
            currentForecast.date = formatter.parse(currentForecast.dt_txt) as Date
            forecast.add(currentForecast)
        }

        for (i in 0..4) {
            var singleDailyForecast = CurrentWeatherDetails()

            var calendar = Calendar.getInstance()
            calendar.time = forecast[0].date
            calendar.add(Calendar.DATE, i)
            singleDailyForecast.currentDate = calendar.time

            val formatter = SimpleDateFormat("yyyy-MM-dd")
            val dailyForecastList = forecast.filter {
                formatter.format(it.date).equals(formatter.format(singleDailyForecast.currentDate))
            }

            singleDailyForecast.minTemp = getMinTemperature(dailyForecastList)
            singleDailyForecast.maxTemp = getMaxTemperature(dailyForecastList)
            singleDailyForecast.weatherDescription = mostCommonDailyWeather(dailyForecastList)
            singleDailyForecast.icon = mostCommonIcon(dailyForecastList)

            dailyForecast.add(singleDailyForecast)
        }
    }

    private fun getMinTemperature(list: List<WeatherResponse>): Int {
        var tempMin = 999
        for (item in list) {
            if (item.main.temp_min < tempMin) {
                tempMin = item.main.temp_min.roundToInt()
            }
        }
        return tempMin
    }

    private fun getMaxTemperature(list: List<WeatherResponse>): Int {
        var tempMax = -999
        for (item in list) {
            if (item.main.temp_max > tempMax) {
                tempMax = item.main.temp_max.roundToInt()
            }
        }
        return tempMax
    }

    private fun mostCommonDailyWeather(list: List<WeatherResponse>): String {
        val numbersByElement = list.groupingBy { it.weather[0].main }.eachCount()
        return numbersByElement.maxBy { it.value }.key.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }
    }

    private fun mostCommonIcon(list: List<WeatherResponse>): String {
        val numbersByElement = list.groupingBy { it.weather[0].icon }.eachCount()
        return numbersByElement.maxBy { it.value }.key
    }

    override fun getItemCount(): Int {
        return dailyForecast.size
    }
}
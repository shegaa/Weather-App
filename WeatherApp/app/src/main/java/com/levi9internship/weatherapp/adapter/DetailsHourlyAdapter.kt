package com.levi9internship.weatherapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.levi9internship.weatherapp.R
import com.levi9internship.weatherapp.common.IconId
import com.levi9internship.weatherapp.data.weather.WeatherResponse
import com.levi9internship.weatherapp.databinding.HourlyItemBinding
import com.levi9internship.weatherapp.network.WeatherApiService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class DetailsHourlyAdapter(cont: Context) :
    RecyclerView.Adapter<DetailsHourlyAdapter.DetailsHourlyViewHolder>() {

    var dailyForecast: List<WeatherResponse> = listOf()
    var context = cont
    var units : String = WeatherApiService.UNITS_METRIC

    inner class DetailsHourlyViewHolder(private val binding: HourlyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: WeatherResponse) {
            binding.apply {
                var time = LocalDateTime.parse(
                    forecast.dt_txt,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                if (adapterPosition != 0) {
                    hourlyTime.text = time.format(DateTimeFormatter.ofPattern("HH:mm")).toString()
                } else {
                    hourlyTime.text = "Now"
                }


                hourlyDirection.setImageResource(R.drawable.ic_wind_direction)
                hourlyDirection.rotation = forecast.wind.deg.toFloat()
                hourlyIcon.setImageDrawable(root.context.getDrawable(IconId.getIconId(forecast.weather[0].icon)))

                if (units == WeatherApiService.UNITS_METRIC) {
                    hourlyTemp.text = context.resources.getString(R.string.celsius_formatted,forecast.main.temp.roundToInt())
                    hourlySpeed.text = context.resources.getString(R.string.wind_speed, forecast.wind.speed.roundToInt())
                } else {
                    hourlyTemp.text = context.resources.getString(R.string.fahrenheit_formatted,forecast.main.temp.roundToInt())
                    hourlySpeed.text = context.resources.getString(R.string.wind_speed_imperial, forecast.wind.speed.roundToInt())
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsHourlyViewHolder {
        val binding = HourlyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailsHourlyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailsHourlyViewHolder, position: Int) {
        holder.bind(dailyForecast[position])
    }

    override fun getItemCount(): Int {
        return dailyForecast.size
    }

    fun setData(list: List<WeatherResponse>) {
        dailyForecast = list.take(9)//magic number?
    }
}

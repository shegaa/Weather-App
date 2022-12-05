package com.levi9internship.weatherapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.levi9internship.weatherapp.data.City
import com.levi9internship.weatherapp.databinding.ItemSearchLocationBinding

class ItemSearchAdapter(private val listener: OnItemClickListener) :
    ListAdapter<City, ItemSearchAdapter.ItemSearchViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemSearchViewHolder {
        val binding =
            ItemSearchLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemSearchViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class ItemSearchViewHolder(private val binding: ItemSearchLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                layoutCard.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val location = getItem(position)
                        listener.onMoreDetailsClick(
                            location.lat.toString(),
                            location.lon.toString()
                        )
                    }
                }
            }
        }

        fun bind(city: City) {
            binding.apply {
                textViewCity.text = city.name
                textViewCountry.text = city.country
            }
        }
    }

    interface OnItemClickListener {
        fun onMoreDetailsClick(lat: String, lon: String)
    }

    class DiffCallback : DiffUtil.ItemCallback<City>() {
        override fun areItemsTheSame(oldItem: City, newItem: City): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: City, newItem: City) =
            oldItem == newItem
    }
}

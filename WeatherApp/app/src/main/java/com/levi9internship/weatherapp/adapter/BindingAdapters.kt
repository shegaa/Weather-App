package com.levi9internship.weatherapp.adapter

import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import coil.load
import com.levi9internship.weatherapp.R
import com.levi9internship.weatherapp.common.IconId

@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        imgView.load(imgUri) {
            placeholder(R.drawable.logo)
            error(R.drawable.logo)
        }
    }
}

@BindingAdapter("iconCode")
fun bindImageByIcon(imgView: ImageView, icon: String) {
    val iconId = IconId
    imgView.setImageResource(iconId.getIconId(icon))
    imgView.scaleType = ImageView.ScaleType.CENTER_INSIDE
}
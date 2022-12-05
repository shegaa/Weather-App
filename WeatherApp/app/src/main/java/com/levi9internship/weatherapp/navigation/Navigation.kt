package com.levi9internship.weatherapp.navigation

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.levi9internship.weatherapp.R
import com.levi9internship.weatherapp.ui.DetailsFragment


class Navigation(var navController: NavController) {

    private val optionsAnimation = NavOptions.Builder()
        .setLaunchSingleTop(true)
        .setEnterAnim(R.anim.from_right)
        .setExitAnim(R.anim.to_left)
        .setPopEnterAnim(R.anim.from_right)
        .setPopExitAnim(R.anim.to_right)
        .setPopUpTo(R.id.homeFragment, false)
        .build()

    private val optionsAnimationLeft = NavOptions.Builder()
        .setLaunchSingleTop(true)
        .setEnterAnim(R.anim.from_left)
        .setExitAnim(R.anim.to_right)
        .setPopEnterAnim(R.anim.from_right)
        .setPopExitAnim(R.anim.to_right)
        .build()

    fun goToSettingsFragment() {
        navController.navigate(R.id.settingsFragment, null, optionsAnimation)
    }

    fun goToFavoriteFragment() {
        navController.navigate(R.id.favoriteFragment, null, optionsAnimation)
    }

    fun goToSearchFragment() {
        navController.navigate(R.id.searchFragment, null, optionsAnimation)
    }

    fun goToHomeFragment() {
        navController.navigate(R.id.homeFragment, null, optionsAnimationLeft)
    }

    fun goBack() {
        navController.navigateUp()
    }

    fun registerToSignIn() {
        navController.navigate(
            R.id.action_registerFragment_to_signInFragment,
            null,
            optionsAnimation
        )
    }

    fun settingsToSignIn() {
        navController.navigate(
            R.id.action_settingsFragment_to_signInFragment,
            null,
            optionsAnimation
        )
    }

    fun settingsToRegister() {
        navController.navigate(
            R.id.action_settingsFragment_to_registerFragment,
            null,
            optionsAnimation
        )
    }

    fun homeToWelcome() {
        navController.navigate(R.id.action_homeFragment_to_welcomeFragment)
    }

    fun homeToSearch() {
        navController.navigate(R.id.action_homeFragment_to_searchFragment)
    }

    fun homeToNoFavorites() {
        navController.navigate(R.id.action_homeFragment_to_homeWithNoFavoriteFragment)
    }

    fun welcomeToHome() {
        navController.navigate(R.id.action_welcomeFragment_to_homeFragment)
    }

    fun searchToDetails(lat: String, lon: String) {
        val bundle = Bundle()
        bundle.putString(DetailsFragment.LAT, lat)
        bundle.putString(DetailsFragment.LON, lon)
        navController.navigate(R.id.action_searchFragment_to_detailsFragment, bundle)
    }

    fun noFavLocationToSearch() {
        navController.navigate(R.id.action_homeWithNoFavoriteFragment_to_searchFragment)
    }

    fun locationAlertToHome() {
        navController.navigate(R.id.action_locationAlertFragment_to_homeFragment)
    }

    fun homeToLocationAlert() {
        navController.navigate(R.id.action_homeFragment_to_locationAlertFragment)
    }

    fun welcomeToLocationAlert() {
        navController.navigate(R.id.action_welcomeFragment_to_locationAlertFragment)
    }

    fun searchToMap() {
        navController.navigate(R.id.action_searchFragment_to_map)
    }

}
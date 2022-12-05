package com.levi9internship.weatherapp

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.levi9internship.weatherapp.databinding.ActivityMainBinding
import com.levi9internship.weatherapp.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import java.security.AccessController.getContext
import javax.inject.Inject

const val FIRST_RUN = "firstRun"
const val FAVORITES = "favorites"
const val ACTIVE_LOCATION = "activeLocation"
const val IS_ACTIVE_CURRENT = "isActiveCurrent"
const val DARK_MODE = "darkMode"
const val NUMBER_OF_LOCATION_PROMPTS = "numberOfLocationPrompts"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var navigation: Navigation

    @Inject
    lateinit var preferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding
    lateinit var bottomNavigation: BottomNavigationView
    lateinit var backButton: Button
    lateinit var toolbar: Toolbar
    lateinit var editButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (preferences.getBoolean(DARK_MODE, false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navigation = Navigation(navHostFragment.navController)

        if (preferences.getBoolean(FIRST_RUN, true)) {
            navigation.homeToWelcome()
        }

        bottomNavigation = binding.bottomNavigation
        backButton = binding.buttonBack

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            navigation.apply {
                when (item.itemId) {
                    R.id.homeFragment -> {
                        goToHomeFragment()
                    }
                    R.id.searchFragment -> {
                        goToSearchFragment()
                    }
                    R.id.favoriteFragment -> {
                        goToFavoriteFragment()
                    }
                    R.id.settingsFragment -> {
                        goToSettingsFragment()
                    }
                }
            }

            true
        }
        bottomNavigation.setOnNavigationItemReselectedListener { _ ->
            return@setOnNavigationItemReselectedListener
        }

        toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navigation.navController)

        editButton = binding.buttonEdit

        navigation.navController.addOnDestinationChangedListener { _, destination, _ ->
            // BOTTOM BAR
            when (destination.id) {
                R.id.welcomeFragment -> bottomNavigation.visibility = View.GONE
                R.id.detailsFragment -> bottomNavigation.visibility = View.GONE
                R.id.registerFragment -> bottomNavigation.visibility = View.GONE
                R.id.signInFragment -> bottomNavigation.visibility = View.GONE
                R.id.map -> bottomNavigation.visibility = View.GONE
                else -> bottomNavigation.visibility = View.VISIBLE
            }

            // TOOLBAR
            when (destination.id) {
                R.id.welcomeFragment -> toolbar.visibility = View.GONE
                R.id.locationAlertFragment -> toolbar.visibility = View.GONE
                R.id.homeWithNoFavoriteFragment -> toolbar.visibility = View.GONE
                else -> toolbar.visibility = View.VISIBLE
            }

            // TOOLBAR BACK BUTTON
            toolbar.navigationIcon = null
            when (destination.id) {
                R.id.settingsFragment -> backButton.visibility = View.GONE
                R.id.homeFragment -> backButton.visibility = View.GONE
                R.id.searchFragment -> backButton.visibility = View.GONE
                R.id.favoriteFragment -> backButton.visibility = View.GONE
                else -> backButton.visibility = View.VISIBLE
            }

            // TOOLBAR EDIT BUTTON
            when (destination.id) {
                R.id.favoriteFragment -> editButton.visibility = View.VISIBLE
                else -> editButton.visibility = View.INVISIBLE
            }

            // TOOLBAR COLOR
            when (destination.id) {
                R.id.settingsFragment -> toolbar.setBackgroundColor(resources.getColor(R.color.topBlueDark))
                else -> toolbar.setBackgroundColor(resources.getColor(R.color.topBlue))
            }

            // BACK BUTTON LOGIC
            backButton.setOnClickListener {
                customOnBackPressed(destination.id)
            }
        }
    }


    fun customOnBackPressed(fragmentId: Int) {
        when (fragmentId) {
            R.id.homeFragment -> finish()
            R.id.signInFragment -> {
                navigation.goToSettingsFragment()
                bottomNavigation.selectedItemId = R.id.settingsFragment
            }
            R.id.registerFragment -> {
                navigation.goToSettingsFragment()
                bottomNavigation.selectedItemId = R.id.settingsFragment
            }
            R.id.detailsFragment -> navigation
                .goToSearchFragment()
            R.id.map -> navigation
                .goToSearchFragment()
            else -> {
                bottomNavigation.selectedItemId = R.id.homeFragment
            }
        }
    }

    fun navigateToHome() {
        navigation.goToHomeFragment()
        bottomNavigation.selectedItemId = R.id.homeFragment
    }

    fun focusView(view: View) {
        view.requestFocus()
        val imm =
            this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun navigateToSearch() {
        navigation.goToSearchFragment()
        bottomNavigation.selectedItemId = R.id.searchFragment
    }

    fun navigateToSettings() {
        navigation.goToSearchFragment()
        bottomNavigation.selectedItemId = R.id.settingsFragment
    }
}

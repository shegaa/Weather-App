package com.levi9internship.weatherapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.levi9internship.weatherapp.MainActivity
import com.levi9internship.weatherapp.R
import com.levi9internship.weatherapp.navigation.Navigation
import com.levi9internship.weatherapp.service.PreferencesService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val REQUEST_CODE = 100

@AndroidEntryPoint
class LocationAlertFragment : Fragment() {

    private lateinit var navigation: Navigation

    @Inject
    lateinit var preferencesService: PreferencesService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        navigation = (activity as MainActivity).navigation
        return inflater.inflate(R.layout.fragment_location_alert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshLocation()
        val button: MaterialButton = view.findViewById(R.id.button_home)
        button.setOnClickListener {
            navigation.locationAlertToHome()
        }
    }

    private fun refreshLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            navigation.locationAlertToHome()
        } else {
            requestUserLocation()
        }
    }

    private fun requestUserLocation() {
        val numberOfPrompts = preferencesService.getNumberOfLocationPrompts()
        if (numberOfPrompts == 0) {
            preferencesService.setNumberOfLocationPrompts(1)
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE
            )
        } else {
            openAppSettings()
        }
    }

    private fun openAppSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.data = Uri.parse("package:" + requireContext().packageName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        requireContext().startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                preferencesService.setNumberOfLocationPrompts(1)
                preferencesService.setIsActiveCurrent(true)
                navigation.locationAlertToHome()
            }
        }
    }

}
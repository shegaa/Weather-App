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
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.levi9internship.weatherapp.MainActivity
import com.levi9internship.weatherapp.R
import com.levi9internship.weatherapp.databinding.FragmentSettingsBinding
import com.levi9internship.weatherapp.navigation.Navigation
import com.levi9internship.weatherapp.service.FavoritesService
import com.levi9internship.weatherapp.service.LocationsService
import com.levi9internship.weatherapp.service.PreferencesService
import com.levi9internship.weatherapp.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val REQUEST_CODE = 100

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var binding: FragmentSettingsBinding? = null
    private lateinit var navigation: Navigation
    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var preferencesService: PreferencesService

    @Inject
    lateinit var favoritesService: FavoritesService

    @Inject
    lateinit var locationsService: LocationsService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        navigation = (activity as MainActivity).navigation
        FragmentSettingsBinding.inflate(inflater, container, false).apply {
            binding = this
            return root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    (activity as MainActivity).customOnBackPressed(R.id.settingsFragment)
                }
            })

        binding?.progressBar?.visibility = View.VISIBLE
        binding?.spinner?.visibility = View.INVISIBLE

        settingsViewModel.isUserLoggedIn()

        settingsViewModel.errorFirestore.observe(viewLifecycleOwner) { exception ->
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

        settingsViewModel.isLoggedIn.observe(viewLifecycleOwner) {
            if (it) {
                binding?.layoutSignIn?.visibility = View.GONE
                binding?.layoutCreateAnAccount?.visibility = View.GONE
                binding?.layoutLogOut?.visibility = View.VISIBLE
            } else {
                binding?.layoutSignIn?.visibility = View.VISIBLE
                binding?.layoutCreateAnAccount?.visibility = View.VISIBLE
                binding?.layoutLogOut?.visibility = View.GONE
            }
        }

        settingsViewModel.unit.observe(this.viewLifecycleOwner) {
            binding?.apply {

                progressBar.visibility = View.INVISIBLE
                spinner.visibility = View.VISIBLE

                layoutSignIn.setOnClickListener {
                    navigation.settingsToSignIn()
                }

                layoutCreateAnAccount.setOnClickListener {
                    navigation.settingsToRegister()
                }

                switchDarkMode.setOnClickListener() {
                    if (preferencesService.getDarkMode()) {
                        preferencesService.setDarkMode(false)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        switchDarkMode.isChecked = false
                    } else {
                        preferencesService.setDarkMode(true)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        switchDarkMode.isChecked = true
                    }
                }
                layoutLogOut.setOnClickListener {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(resources.getString(R.string.dialog_log_out_alert))
                        .setMessage(resources.getString(R.string.dialog_log_out_message))
                        .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                            dialog.cancel()
                        }
                        .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                            settingsViewModel.logout()
                            binding?.spinner?.setSelection(0)
                        }
                        .show()


                }

                switchDarkMode.setOnCheckedChangeListener { compoundButton, isChecked -> }

                switchLocation.setOnClickListener {
                    val isChecked = switchLocation.isChecked
                    if (isChecked) {
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
                    } else {
                        openAppSettings()
                    }
                }

                val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.pref_showTemperatureIn,
                    R.layout.menu_selected_item
                )
                adapter.setDropDownViewResource(R.layout.menu_dropdown_item)
                spinner.adapter = adapter
                spinner.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        if (settingsViewModel.isLoggedIn.value!!) {
                            val text = p0?.getItemAtPosition(p3.toInt()).toString()
                            settingsViewModel.setUnitSystem(text.lowercase())
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "You need to be logged in to change unit system",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        loadSettings()
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
                binding?.switchLocation?.isChecked = true
                preferencesService.setNumberOfLocationPrompts(0)
                if (favoritesService.getAllFavorites().size == 0) {
                    preferencesService.setIsActiveCurrent(true)
                }
            } else {
                binding?.switchLocation?.isChecked = false
            }
        }
    }

    private fun loadSettings() {
        settingsViewModel.getUnitSystem()
        binding?.switchDarkMode?.isChecked = preferencesService.getDarkMode()
        var spinnerPosition = settingsViewModel.getSpinnerPosition()
        binding?.switchLocation?.isChecked = locationsService.hasLocationAccess()
        settingsViewModel.unit.observe(this.viewLifecycleOwner) {
            binding?.spinner?.setSelection(settingsViewModel.getSpinnerPosition())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
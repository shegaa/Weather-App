package com.levi9internship.weatherapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.levi9internship.weatherapp.MainActivity
import com.levi9internship.weatherapp.databinding.FragmentWelcomeBinding
import com.levi9internship.weatherapp.navigation.Navigation
import com.levi9internship.weatherapp.service.PreferencesService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WelcomeFragment : Fragment() {

    private var binding: FragmentWelcomeBinding? = null
    private lateinit var navigation: Navigation

    @Inject
    lateinit var preferencesService: PreferencesService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        navigation = (activity as MainActivity).navigation

        FragmentWelcomeBinding.inflate(inflater, container, false).apply {
            binding = this
            return root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            buttonGetStarted.setOnClickListener {
                if (preferencesService.getFirstRun()) {
                    preferencesService.setFirstRun(false)
                }
                navigation.welcomeToLocationAlert()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
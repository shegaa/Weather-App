package com.levi9internship.weatherapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.levi9internship.weatherapp.MainActivity
import com.levi9internship.weatherapp.databinding.FragmentHomeWithNoFavoriteBinding
import com.levi9internship.weatherapp.navigation.Navigation

class HomeWithNoFavoriteFragment : Fragment() {

    private lateinit var navigation: Navigation
    private var binding: FragmentHomeWithNoFavoriteBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        navigation = (activity as MainActivity).navigation
        FragmentHomeWithNoFavoriteBinding.inflate(inflater, container, false).apply {
            binding = this
            return root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            buttonSearchLocations.setOnClickListener {
                (activity as MainActivity).navigateToSearch()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}
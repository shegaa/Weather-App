package com.levi9internship.weatherapp.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.levi9internship.weatherapp.MainActivity
import com.levi9internship.weatherapp.R
import com.levi9internship.weatherapp.adapter.ItemSearchAdapter
import com.levi9internship.weatherapp.data.weather.Main
import com.levi9internship.weatherapp.databinding.FragmentSearchBinding
import com.levi9internship.weatherapp.navigation.Navigation
import com.levi9internship.weatherapp.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG = "SearchFragment"

@AndroidEntryPoint
class SearchFragment : Fragment(), ItemSearchAdapter.OnItemClickListener {

    private var binding: FragmentSearchBinding? = null
    private val viewModel: SearchViewModel by activityViewModels()
    private lateinit var navigation: Navigation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        navigation = (activity as MainActivity).navigation

        FragmentSearchBinding.inflate(inflater, container, false).apply {
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
                    (activity as MainActivity).customOnBackPressed(R.id.searchFragment)
                }
            })

        (activity as MainActivity).focusView(binding!!.editTextSearch)

        if (viewModel.cityName.value?.isNotBlank() == true) {
            binding!!.editTextSearch.setText(viewModel.cityName.value)
            hideClearButton(binding!!.imageViewEmailClear, true)
        }

        val itemAdapter = ItemSearchAdapter(this)
        binding!!.imageViewEmailClear.apply {
            setOnClickListener { onClearClicked(binding!!.editTextSearch) }
        }
        binding!!.recyclerView.apply {
            adapter = itemAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        viewModel.cityList.observe(viewLifecycleOwner) {
            itemAdapter.submitList(it)
        }

        binding!!.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if (p0.isNotEmpty()) {
                        hideClearButton(binding!!.imageViewEmailClear, true)
                        viewModel.setString(p0.toString())
                        if (p0.length > 1) {
                            lifecycleScope.launch {
                                viewModel.getCityByName(p0.toString())
                            }
                        }else{
                            viewModel.resetCityList()
                        }
                    } else {
                        hideClearButton(binding!!.imageViewEmailClear, false)
                        viewModel.resetCityList()
                    }
                }
            }
        })

        binding?.buttonMap?.setOnClickListener {
            navigation.searchToMap()
        }

        viewModel.cityList.observe(this.viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding?.textNoResults?.visibility = View.VISIBLE
            } else {
                binding?.textNoResults?.visibility = View.GONE
            }
        }
    }

    private fun hideClearButton(imageView: ImageView, state: Boolean) {
        if (state) {
            imageView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.INVISIBLE
        }
    }

    private fun onClearClicked(editText: EditText) {
        editText.text.clear()
        viewModel.resetCityName()
    }

    override fun onMoreDetailsClick(lat: String, lon: String) {
        navigation.searchToDetails(lat, lon)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}
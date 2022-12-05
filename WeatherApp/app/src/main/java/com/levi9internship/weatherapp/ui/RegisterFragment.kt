package com.levi9internship.weatherapp.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.levi9internship.weatherapp.MainActivity
import com.levi9internship.weatherapp.R
import com.levi9internship.weatherapp.databinding.FragmentRegisterBinding
import com.levi9internship.weatherapp.firebase.Resource
import com.levi9internship.weatherapp.navigation.Navigation
import com.levi9internship.weatherapp.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "RegisterFragment"

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private var binding: FragmentRegisterBinding? = null
    private val registerViewModel: RegisterViewModel by activityViewModels()
    private lateinit var navigation: Navigation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        navigation = (activity as MainActivity).navigation

        FragmentRegisterBinding.inflate(inflater, container, false).apply {
            binding = this
            return root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).toolbar.setNavigationOnClickListener {
            (activity as MainActivity).customOnBackPressed(R.id.registerFragment)
        }

        registerViewModel.errorFirestore.observe(viewLifecycleOwner){ exception ->
            Snackbar.make(binding!!.root, exception.message.toString(), Snackbar.LENGTH_LONG)
                .setAction("More details") {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Error")
                        .setMessage(exception.toString())
                        .setPositiveButton("Ok") { dialog, _ ->
                            dialog.cancel()
                        }
                        .show()
                }
                .show()
        }

        registerViewModel.error.observe(viewLifecycleOwner){
            binding?.textViewErrorMessage?.text = getString(it)
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    (activity as MainActivity).customOnBackPressed(R.id.registerFragment)
                }
            })

        (activity as MainActivity).focusView(binding!!.editTextEmail)

        registerViewModel.registerFlow.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Failure -> {
                    binding?.apply {
                        progressBarTemperature.visibility = View.INVISIBLE
                        textViewErrorMessage.text = it.exception.message
                    }
                }
                Resource.Loading -> {
                    binding?.apply { progressBarTemperature.visibility = View.VISIBLE }

                }
                is Resource.Success -> {
                    binding?.apply {
                        progressBarTemperature.visibility = View.INVISIBLE
                    }
                    registerViewModel.resetRegisterFlow()
                    (activity as MainActivity).navigateToHome()
                }
                null -> {}
            }
        }

        binding?.buttonRegister?.setOnClickListener {
            if (registerViewModel.isDataForRegisterValid()) {
                registerViewModel.register()
            }
        }

        binding?.editTextEmail?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(email: CharSequence, start: Int, before: Int, count: Int) {
                registerViewModel.email = email.toString()
                if (isTextValid(email.toString())) {
                    binding?.imageViewEmailClear?.let { hideClearButton(it, true) }
                } else {
                    binding?.imageViewEmailClear?.let { hideClearButton(it, false) }
                }
            }
        })

        binding?.imageViewEmailClear?.apply {
            setOnClickListener { binding?.editTextEmail?.let { it1 -> onClearClicked(it1) } }
        }

        binding?.editTextPassword?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(
                password: CharSequence, start: Int, before: Int, count: Int
            ) {
                registerViewModel.password = password.toString()
                if (isTextValid(password.toString())) {
                    binding?.imageViewPasswordClear?.let { hideClearButton(it, true) }
                } else {
                    binding?.imageViewPasswordClear?.let { hideClearButton(it, false) }
                }
            }
        })

        binding?.imageViewPasswordClear?.apply {
            setOnClickListener { binding?.editTextPassword?.let { it1 -> onClearClicked(it1) } }
        }

        binding?.editTextConfirmPassword?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(
                passwordConfirmed: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                registerViewModel.passwordConfirmed = passwordConfirmed.toString()
                if (isTextValid(passwordConfirmed.toString())) {
                    binding?.imageViewConfirmPasswordClear?.let { hideClearButton(it, true) }
                } else {
                    binding?.imageViewConfirmPasswordClear?.let { hideClearButton(it, false) }
                }
            }
        })

        binding?.imageViewConfirmPasswordClear?.apply {
            setOnClickListener { binding?.editTextConfirmPassword?.let { it1 -> onClearClicked(it1) } }
        }
    }

    private fun isTextValid(s: String): Boolean {
        //TODO implement logic for entry validation
        return s.isNotEmpty()
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
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}
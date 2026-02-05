package com.harsh.shopit.seller.auth.ui.signup

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.harsh.shopit.R
import com.harsh.shopit.extensions.isNotNullOrEmpty
import com.harsh.shopit.main.utils.Resource
import com.harsh.shopit.seller.SellerMainHomeActivity
import com.harsh.shopit.seller.auth.viewmodel.SellerAuthViewModel
import com.harsh.shopit.utils.customsnakbar.CustomSnackbar

class SellerSignup : Fragment(R.layout.seller_fragment_signup) {
    private lateinit var viewModel: SellerAuthViewModel
    fun signupBtnHandler(view: View) {
        val nameField = view.findViewById<EditText>(R.id.sellerNameField)
        val emailField = view.findViewById<EditText>(R.id.sellerEmailFieldSignup)
        val passwordField = view.findViewById<EditText>(R.id.sellerPasswordFieldSignup)
        val confPasswordField = view.findViewById<EditText>(R.id.sellerConfPasswordField)
        val signupBtn = view.findViewById<Button>(R.id.sellerSignupBtn)
        signupBtn.setOnClickListener {
            if (!nameField.isNotNullOrEmpty()) {
                nameField.error = "Name is required"
                nameField.requestFocus()
            } else if (!emailField.isNotNullOrEmpty()) {
                emailField.error = "Email is required"
                emailField.requestFocus()
            } else if (!passwordField.isNotNullOrEmpty()) {
                passwordField.error = "Password is required"
                passwordField.requestFocus()
            } else if (!confPasswordField.isNotNullOrEmpty() || confPasswordField.text.toString() != passwordField.text.toString()) {
                confPasswordField.error = "Confirm Password is Invalid"
                confPasswordField.requestFocus()
            } else {
                viewModel.addSeller(
                    nameField.text.toString(),
                    emailField.text.toString(),
                    passwordField.text.toString()
                )
            }
        }

    }

    fun observeSignupState(view: View) {
        val loader = view.findViewById<ProgressBar>(R.id.sellerSignupLoader)
        val signupBtn = view.findViewById<Button>(R.id.sellerSignupBtn)
        viewModel._sellers.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    loader.visibility = View.VISIBLE
                    signupBtn.text = ""
                }

                is Resource.Success -> {
                    loader.visibility = View.GONE
                    signupBtn.text = getString(R.string.signup)
                    startActivity(Intent(requireContext(), SellerMainHomeActivity::class.java))
                    requireActivity().finishAffinity()
                }

                is Resource.Error -> {
                    loader.visibility = View.GONE
                    signupBtn.text = getString(R.string.signup)
                    CustomSnackbar.error(requireView(), "Email already exists")
                }
            }
        }

    }

    fun switchToLogin(view: View) {
        val loginNowBtn = view.findViewById<Button>(R.id.sellerLoginNow)
        loginNowBtn.setOnClickListener {
            val viewPager =
                requireActivity().findViewById<ViewPager2>(R.id.sellerAuthViewPager)
            viewPager.currentItem = 0
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SellerAuthViewModel::class.java]
        signupBtnHandler(view)
        observeSignupState(view)
        switchToLogin(view)
    }
}
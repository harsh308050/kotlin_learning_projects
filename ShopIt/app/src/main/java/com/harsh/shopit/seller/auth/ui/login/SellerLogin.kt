package com.harsh.shopit.seller.auth.ui.login

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

class SellerLogin : Fragment(R.layout.seller_fragment_login) {
    private lateinit var viewModel: SellerAuthViewModel
    fun loginBtnHandler(view: View) {
        val emailField = view.findViewById<EditText>(R.id.sellerEmailField)
        val passwordField = view.findViewById<EditText>(R.id.sellerPasswordField)
        val loginBtn = view.findViewById<Button>(R.id.sellerLoginBtn)
        loginBtn.setOnClickListener {
            if (!emailField.isNotNullOrEmpty()) {
                emailField.error = "Email is required"
                emailField.requestFocus()
            }
            else if (!passwordField.isNotNullOrEmpty()) {
                passwordField.error = "Password is required"
                passwordField.requestFocus()
            } else{
                viewModel.getSeller(emailField.text.toString(),passwordField.text.toString())
            }
        }
    }

    fun observeLoginState(view: View){
        val loader = view.findViewById<ProgressBar>(R.id.sellerLoginLoader)
        val loginBtn = view.findViewById<Button>(R.id.sellerLoginBtn)
        viewModel._sellers.observe(viewLifecycleOwner){result->
            when(result){
                is Resource.Loading->{
                    loginBtn.text = ""
                    loader.visibility = View.VISIBLE
                }
                is Resource.Success->{
                    loader.visibility = View.GONE
                    loginBtn.text = getString(R.string.login)
                    startActivity(Intent(requireContext(), SellerMainHomeActivity::class.java))
                    requireActivity().finishAffinity()
                }
                is Resource.Error->{
                    loader.visibility = View.GONE
                    loginBtn.text = getString(R.string.login)
                    CustomSnackbar.error(requireView(), result.message)
                }
            }
        }
    }
    fun switchToSignup(view: View){
        val signupNowBtn = view.findViewById<Button>(R.id.sellerRegisterNow)
        signupNowBtn.setOnClickListener {
            val viewPager =
                requireActivity().findViewById<ViewPager2>(R.id.sellerAuthViewPager)
            viewPager.currentItem = 1
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SellerAuthViewModel::class.java]
        loginBtnHandler(view)
        observeLoginState(view)
        switchToSignup(view)
    }
}
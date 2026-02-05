package com.harsh.shopit.main.screens.profile.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.harsh.shopit.R
import com.harsh.shopit.initial.GetStartedActivity
import com.harsh.shopit.main.screens.profile.data.model.UserResponseModel
import com.harsh.shopit.main.screens.profile.profileDetails.ProfileDetails
import com.harsh.shopit.main.screens.profile.viewmodel.ProfileViewModel
import com.harsh.shopit.main.utils.Prefs
import com.harsh.shopit.main.utils.Resource
import com.harsh.shopit.main.utils.SharedPrefKeys
import com.harsh.shopit.seller.auth.ui.SellerAuthActivity

class ProfileFragment : Fragment(R.layout.customer_fragment_profile) {

    fun switchToSeller(view: View){
        val switchToSeller = view.findViewById<LinearLayout>(R.id.switchToSeller)
        switchToSeller.setOnClickListener {
            startActivity(Intent(requireContext(), SellerAuthActivity::class.java))
            requireActivity().finishAffinity()
        }
    }
    fun getUser(view:View){
        val token = Prefs.getString(view.context, SharedPrefKeys.authtoken)
        if (!token.isNullOrEmpty()){
            viewModel.getUserDetails("Bearer $token")
        }
        else{
            Toast.makeText(view.context, "Token Not Found Login Again", Toast.LENGTH_SHORT).show()
            Log.e("PROFILE", "Token is null")
        }
    }

    private fun userObeserver(view: View){
        val name = view.findViewById<TextView>(R.id.userName)
        val gender = view.findViewById<TextView>(R.id.gender)
        val address = view.findViewById<TextView>(R.id.address)
        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        viewModel._user.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    name.text = "Loading.."
                    gender.text = "Loading.."
                    address.text = "Loading.."
                }
                is Resource.Success -> {
                    val  user = result.data
                    name.text = "${user.firstName} ${user.lastName}"
                    gender.text = user.gender
                    address.text = "${user.address.city}, ${user.address.state}"
                    Glide.with(view.context).load(user.image).into(profileImage)
                    if(Prefs.getObject<UserResponseModel>(view.context,SharedPrefKeys.userDetails) == null){
                        Prefs.putObject(view.context,SharedPrefKeys.userDetails, user)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun logout(view: View){
        val logoutBtn = view.findViewById<MaterialButton>(R.id.logout)
        logoutBtn.setOnClickListener {
            Prefs.clear(view.context)
            startActivity(Intent(view.context, GetStartedActivity::class.java))
            requireActivity().finishAffinity()
        }
    }

    fun profileInfo(view: View){
        val profileInfo = view.findViewById<LinearLayout>(R.id.profileInfo)
        profileInfo.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileDetails::class.java))
        }
    }

    private lateinit var viewModel: ProfileViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        getUser(view)
        userObeserver(view)
        logout(view)
        profileInfo(view)
        switchToSeller(view)
    }
}

package com.harsh.shopit.seller.auth.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.harsh.shopit.seller.auth.ui.login.SellerLogin
import com.harsh.shopit.seller.auth.ui.signup.SellerSignup

class AuthPagerAdapter (activity: FragmentActivity) : FragmentStateAdapter(activity){
    override fun getItemCount() : Int = 2
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> SellerLogin()
            1 -> SellerSignup()
            else -> SellerLogin()
        }

    }
}
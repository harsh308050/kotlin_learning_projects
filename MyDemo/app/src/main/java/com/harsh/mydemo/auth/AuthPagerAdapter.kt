package com.harsh.mydemo.auth

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.harsh.mydemo.auth.Login
import com.harsh.mydemo.auth.SignUp

class AuthPagerAdapter (activity: FragmentActivity) : FragmentStateAdapter(activity){
    override fun getItemCount() : Int = 2
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> Login()
            1 -> SignUp()
            else -> Login()
        }

    }
}
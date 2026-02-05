package com.harsh.shopit.seller.auth.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.harsh.shopit.R

class SellerAuthActivity : AppCompatActivity() {
    fun authTabs(){
        val tabLayout = findViewById<TabLayout>(R.id.sellerAuthTabLayout)
        val viewPager  = findViewById<ViewPager2>(R.id.sellerAuthViewPager)
        viewPager.adapter = AuthPagerAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Login"
                1 -> "Signup"
                else -> "Login"
            }
        }.attach()

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seller_activity_auth)
        authTabs()
    }
}
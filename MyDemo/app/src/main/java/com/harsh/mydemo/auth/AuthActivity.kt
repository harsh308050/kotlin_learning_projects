package com.harsh.mydemo.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.harsh.mydemo.auth.AuthPagerAdapter
import com.harsh.mydemo.R

class AuthActivity : AppCompatActivity() {

fun tabs(){
    val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
    val viewPager  = findViewById<ViewPager2>(R.id.page_viewer)
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
        setContentView(R.layout.activity_auth)
        tabs()
    }
}
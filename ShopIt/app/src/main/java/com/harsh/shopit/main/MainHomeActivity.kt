package com.harsh.shopit.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.harsh.shopit.R
import com.harsh.shopit.main.screens.cart.ui.CartActivity
import com.harsh.shopit.main.screens.home.HomeFragment
import com.harsh.shopit.main.screens.profile.ui.ProfileFragment
import com.harsh.shopit.main.screens.shop.ui.ShopFragment
import com.harsh.shopit.main.screens.wishlist.ui.WishlistFragment

class MainHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.customer_activity_main_home)
        bottomNavHandler(savedInstanceState)
        floatingButtonHandler()
    }

    fun floatingButtonHandler(){
        val fabCart = findViewById<FloatingActionButton>(R.id.cart)
        fabCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }
    fun bottomNavHandler(savedInstanceState : Bundle?){
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
        findViewById<BottomNavigationView>(R.id.bottom_nav)
            .setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.home -> loadFragment(HomeFragment())
                    R.id.shop -> loadFragment(ShopFragment())
                    R.id.wishlist -> loadFragment(WishlistFragment())
                    R.id.profile -> loadFragment(ProfileFragment())
                }
                true
            }
    }
    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            supportFragmentManager.fragments.forEach { hide(it) }
            if (fragment.isAdded) {
                show(fragment)
            } else {
                add(R.id.fragmentContainer, fragment)
            }
            commit()
        }
    }
}
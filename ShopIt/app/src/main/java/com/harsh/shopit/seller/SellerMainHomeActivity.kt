package com.harsh.shopit.seller

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.harsh.shopit.R
import com.harsh.shopit.seller.products.addProd.ui.SellerAddProductActivity
import com.harsh.shopit.seller.dashboard.SellerDashboardFragment
import com.harsh.shopit.seller.products.showProds.ui.SellerProductsFragment

class SellerMainHomeActivity : AppCompatActivity() {
    fun bottomNavHandler(savedInstanceState : Bundle?){
        if (savedInstanceState == null) {
            loadFragment(SellerDashboardFragment())
        }
        findViewById<BottomNavigationView>(R.id.seller_bottom_nav)
            .setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.sellerDashboard -> loadFragment(SellerDashboardFragment())
                    R.id.sellerProducts -> loadFragment(SellerProductsFragment())
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
                add(R.id.sellerFragmentContainer, fragment)
            }
            commit()
        }
    }

    fun floatingButtonHandler(){
        val fabCart = findViewById<FloatingActionButton>(R.id.addProd)
        fabCart.setOnClickListener {
            startActivity(Intent(this, SellerAddProductActivity::class.java))
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seller_activity_main_home)
        bottomNavHandler(savedInstanceState)
        floatingButtonHandler()
    }
}
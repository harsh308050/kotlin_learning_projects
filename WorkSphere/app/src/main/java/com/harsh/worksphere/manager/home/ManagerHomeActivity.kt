package com.harsh.worksphere.manager.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.harsh.worksphere.R
import com.harsh.worksphere.manager.addusers.ui.ManagerAddUsersActivity
import com.harsh.worksphere.manager.users.ManagerUsersFragment
import com.harsh.worksphere.manager.dashboard.ManagerDashboardFragment
import com.harsh.worksphere.manager.settings.ManagerSettingsFragment
import com.harsh.worksphere.manager.sites.ui.ManagerSitesFragment

class ManagerHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_home_activity)
        floatingButtonHandler()
        bottomNavHandler(savedInstanceState)
    }
    fun floatingButtonHandler(){
        val fabCart = findViewById<ExtendedFloatingActionButton>(R.id.add_user)
        fabCart.setOnClickListener {
            startActivity(Intent(this, ManagerAddUsersActivity::class.java))
        }
    }
    fun bottomNavHandler(savedInstanceState : Bundle?){
        if (savedInstanceState == null) {
            loadFragment(ManagerDashboardFragment())
        }
        findViewById<BottomNavigationView>(R.id.manager_bottom_nav)
            .setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.manager_dashboard -> loadFragment(ManagerDashboardFragment())
                    R.id.manager_sites -> loadFragment(ManagerSitesFragment())
                    R.id.manager_users -> loadFragment(ManagerUsersFragment())
                    R.id.manager_settings -> loadFragment(ManagerSettingsFragment())
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
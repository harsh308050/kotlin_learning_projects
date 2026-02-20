package com.harsh.worksphere.employee.home

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.harsh.worksphere.R
import com.harsh.worksphere.employee.dashboard.EmployeeDashboardFragment
import com.harsh.worksphere.employee.profile.EmployeeProfileFragment
import com.harsh.worksphere.employee.visits.EmployeeVisitsFragment
import com.harsh.worksphere.manager.dashboard.ManagerDashboardFragment
import com.harsh.worksphere.manager.settings.ManagerSettingsFragment
import com.harsh.worksphere.manager.sites.ui.ManagerSitesFragment
import com.harsh.worksphere.manager.users.ManagerUsersFragment

class EmployeeHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.employee_home_activity)
        bottomNavHandler(savedInstanceState)
    }
    fun bottomNavHandler(savedInstanceState : Bundle?){
        if (savedInstanceState == null) {
            loadFragment(EmployeeDashboardFragment())
        }
        findViewById<BottomNavigationView>(R.id.employee_bottom_nav)
            .setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.employee_dashboard -> loadFragment(EmployeeDashboardFragment())
                    R.id.employee_visites -> loadFragment(EmployeeVisitsFragment())
                    R.id.employee_profile -> loadFragment(EmployeeProfileFragment())
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
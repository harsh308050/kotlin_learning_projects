package com.harsh.worksphere.manager.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.harsh.worksphere.R
import com.harsh.worksphere.core.firebase.FirebaseModule
import com.harsh.worksphere.core.utils.Result
import com.harsh.worksphere.initial.auth.data.model.UserRole
import com.harsh.worksphere.initial.auth.data.remote.FirestoreDataSource
import com.harsh.worksphere.manager.notifications.ManagerNotificationActivity
import com.harsh.worksphere.manager.sites.repository.SiteRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ManagerDashboardFragment : Fragment(R.layout.manager_dashboard_fragment) {

    private lateinit var siteCountText: TextView
    private lateinit var supervisorCountText: TextView
    private lateinit var employeeCountText: TextView
    private lateinit var dateText: TextView
    private lateinit var profileBtn: ImageView

    private val firestoreDataSource = FirestoreDataSource()
    private val siteRepository = SiteRepository()
    private val currentUserEmail = FirebaseModule.auth.currentUser?.email ?: ""
    private lateinit var notificationBtn : ImageButton


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        dateFilter(view)
        setTodayDate()
        loadDashboardData()
        loadProfileImage()
        setupProfileClick()
        navToNotification()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
        loadProfileImage()
    }

    private fun initViews(view: View) {
        siteCountText = view.findViewById(R.id.site_count)
        supervisorCountText = view.findViewById(R.id.supervisor_count)
        employeeCountText = view.findViewById(R.id.employee_count)
        dateText = view.findViewById(R.id.manager_dashboard_date)
        profileBtn = view.findViewById(R.id.manager_dashboard_profile_btn)
        notificationBtn = view.findViewById(R.id.manager_dashboard_notification_btn)
    }

    private fun setTodayDate() {
        val dateFormat = SimpleDateFormat("'Today,' MMM d", Locale.getDefault())
        dateText.text = dateFormat.format(Date())
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            // Load supervisor count
            when (val result = firestoreDataSource.getUsersByRole(UserRole.Supervisor)) {
                is Result.Success -> supervisorCountText.text = result.data.size.toString()
                is Result.Error -> supervisorCountText.text = "0"
                is Result.Loading -> {}
            }

            // Load employee count
            when (val result = firestoreDataSource.getUsersByRole(UserRole.Employee)) {
                is Result.Success -> employeeCountText.text = result.data.size.toString()
                is Result.Error -> employeeCountText.text = "0"
                is Result.Loading -> {}
            }

            // Load site count
            when (val result = siteRepository.getAllSites()) {
                is Result.Success -> siteCountText.text = result.data.size.toString()
                is Result.Error -> siteCountText.text = "0"
                is Result.Loading -> {}
            }
        }
    }

    private fun navToNotification(){
        notificationBtn.setOnClickListener {
            startActivity(Intent(requireContext(), ManagerNotificationActivity::class.java))
        }
    }
    private fun loadProfileImage() {
        if (currentUserEmail.isEmpty()) return

        lifecycleScope.launch {
            when (val result = firestoreDataSource.getUser(currentUserEmail)) {
                is Result.Success -> {
                    val user = result.data ?: return@launch
                    if (!user.profilePic.isNullOrEmpty() && isAdded) {
                        Glide.with(this@ManagerDashboardFragment)
                            .load(user.profilePic)
                            .circleCrop()
                            .placeholder(R.drawable.profile)
                            .into(profileBtn)
                    }
                }
                is Result.Error -> {}
                is Result.Loading -> {}
            }
        }
    }

    private fun setupProfileClick() {
        profileBtn.setOnClickListener {
            // Navigate to Settings tab via bottom nav
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.manager_bottom_nav)
            bottomNav.selectedItemId = R.id.manager_settings
        }
    }

    private fun dateFilter(view: View) {
        val dates = listOf("This Week", "This Month", "This Year")
        val dateFilter = view.findViewById<Spinner>(R.id.manager_dashboard_date_filter)
        val dateFilterAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dates
        )
        dateFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dateFilter.adapter = dateFilterAdapter
        dateFilter.setSelection(0)
    }
}
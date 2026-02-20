package com.harsh.worksphere.employee.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import com.harsh.worksphere.R
import com.harsh.worksphere.components.CommonSnackbar.showError
import com.harsh.worksphere.core.firebase.FirebaseModule
import com.harsh.worksphere.core.utils.Result
import com.harsh.worksphere.employee.visitlogs.EmployeeAddVisitLogActivity
import com.harsh.worksphere.initial.auth.data.model.User
import com.harsh.worksphere.initial.auth.data.model.UserStatus
import com.harsh.worksphere.initial.auth.data.remote.FirestoreDataSource
import com.harsh.worksphere.manager.sites.data.model.SiteModel
import com.harsh.worksphere.manager.sites.repository.SiteRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EmployeeDashboardFragment : Fragment(R.layout.employee_dashboard_fragment) {

    private lateinit var addVisitLogBtn: MaterialButton
    private lateinit var employeeOnsiteSwitch: MaterialSwitch
    private lateinit var employeeStatusBtn: LinearLayout
    private lateinit var employeeStatusText: TextView
    private lateinit var employeeDashboardTitle: TextView
    private lateinit var employeeDashboardSubtitle: TextView
    private lateinit var employeeOnsiteTime: TextView
    private lateinit var employeeBreakTime: TextView
    private lateinit var employeeDashboardProfile: ImageView
    private lateinit var employeeDashboardNotification: ImageButton

    // Site card views
    private lateinit var siteCard: MaterialCardView
    private lateinit var siteEmptyState: MaterialCardView
    private lateinit var siteImage: ImageView
    private lateinit var navigateToSiteBtn: ImageButton
    private lateinit var siteStatusBadge: TextView
    private lateinit var siteName: TextView
    private lateinit var siteSupervisor: TextView
    private lateinit var siteShift: TextView

    private val firestoreDataSource = FirestoreDataSource()
    private val siteRepository = SiteRepository()
    private val currentUserEmail = FirebaseModule.auth.currentUser?.email ?: ""

    private var currentUser: User? = null
    private var assignedSite: SiteModel? = null
    private var currentStatus: UserStatus = UserStatus.OFFLINE

    // Switch control flags
    private var isProgrammaticSwitchChange = false
    private var locationVerificationSucceeded = false

    // Visit log result launcher — only update status to ON_SITE when form is submitted
    private val visitLogLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            updateUserStatus(UserStatus.ON_SITE)
        } else {
            showError("You must complete the visit log form to go on-site")
            isProgrammaticSwitchChange = true
            employeeOnsiteSwitch.isChecked = false
            isProgrammaticSwitchChange = false
        }
    }

    // Location permission launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            showLocationVerificationDialog()
        } else {
            showError("Location permission is required for on-site verification")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        setupOnsiteSwitch()
        setupProfileClick()
        setupStatusDropdown()
        navToAddVisitLog()
        navToNotifications()
        loadDashboardData()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun initView(view: View) {
        addVisitLogBtn = view.findViewById(R.id.employee_dashboard_startVisit_btn)
        employeeOnsiteSwitch = view.findViewById(R.id.employee_onsite_switch)
        employeeStatusBtn = view.findViewById(R.id.employee_status_btn)
        employeeStatusText = view.findViewById(R.id.employee_status_text)
        employeeOnsiteTime = view.findViewById(R.id.onsite_time)
        employeeBreakTime = view.findViewById(R.id.break_time)
        employeeDashboardProfile = view.findViewById(R.id.employee_dashboard_profile)
        employeeDashboardSubtitle = view.findViewById(R.id.employee_dashboard_subtitle)
        employeeDashboardTitle = view.findViewById(R.id.employee_dashboard_title)
        employeeDashboardNotification = view.findViewById(R.id.employee_dashboard_notification_btn)

        // Site card views
        siteCard = view.findViewById(R.id.employee_site_card)
        siteEmptyState = view.findViewById(R.id.employee_site_empty_state)
        siteImage = view.findViewById(R.id.siteImage)
        navigateToSiteBtn = view.findViewById(R.id.navigate_to_site_btn)
        siteStatusBadge = view.findViewById(R.id.employee_current_siteStatusBadge)
        siteName = view.findViewById(R.id.employee_current_siteName)
        siteSupervisor = view.findViewById(R.id.employee_current_supervisor)
        siteShift = view.findViewById(R.id.employee_current_siteShift)
    }

    // ── On-Site Switch ───────────────────────────────────────────────────────
    private fun setupOnsiteSwitch() {
        employeeOnsiteSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticSwitchChange) return@setOnCheckedChangeListener

            if (isChecked) {
                // Immediately snap back — will be set ON after successful verification
                isProgrammaticSwitchChange = true
                employeeOnsiteSwitch.isChecked = false
                isProgrammaticSwitchChange = false

                if (assignedSite == null) {
                    showError("No site assigned to you")
                    return@setOnCheckedChangeListener
                }

                if (hasLocationPermission()) {
                    showLocationVerificationDialog()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            } else {
                // User toggled OFF → update status to OFFLINE
                updateUserStatus(UserStatus.OFFLINE)
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showLocationVerificationDialog() {
        val site = assignedSite ?: return
        LocationVerificationDialog(
            fragment = this,
            site = site,
            onVerified = { siteId, siteName ->
                locationVerificationSucceeded = true

                val intent = Intent(requireContext(), EmployeeAddVisitLogActivity::class.java).apply {
                    putExtra("site", site)
                }
                visitLogLauncher.launch(intent)
            },
            onShiftInvalid = { message ->
                if (isAdded) showError(message)
            },
            onDismissed = {
                if (!locationVerificationSucceeded) {
                    isProgrammaticSwitchChange = true
                    employeeOnsiteSwitch.isChecked = false
                    isProgrammaticSwitchChange = false
                }
                locationVerificationSucceeded = false
            }
        ).show()
    }

    // ── Greeting ─────────────────────────────────────────────────────────────
    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            hour < 21 -> "Good Evening"
            else -> "Good Night"
        }
    }

    // ── Load Dashboard Data ──────────────────────────────────────────────────
    private fun loadDashboardData() {
        if (currentUserEmail.isEmpty()) return

        lifecycleScope.launch {
            when (val result = firestoreDataSource.getUser(currentUserEmail)) {
                is Result.Success -> {
                    val user = result.data ?: return@launch
                    currentUser = user

                    // Set greeting with first name
                    val firstName = user.name.split(" ").firstOrNull() ?: user.name
                    employeeDashboardTitle.text = "${getGreeting()}, $firstName"

                    // Set email subtitle
                    employeeDashboardSubtitle.text = user.email

                    // Load profile image
                    if (!user.profilePic.isNullOrEmpty() && isAdded) {
                        Glide.with(this@EmployeeDashboardFragment)
                            .load(user.profilePic)
                            .circleCrop()
                            .placeholder(R.drawable.profile)
                            .into(employeeDashboardProfile)
                    }

                    // Set current status
                    currentStatus = user.status
                    employeeStatusText.text = currentStatus.displayName

                    // Programmatic switch update — avoid triggering listener
                    isProgrammaticSwitchChange = true
                    employeeOnsiteSwitch.isChecked = currentStatus == UserStatus.ON_SITE
                    isProgrammaticSwitchChange = false

                    // Load assigned site
                    loadAssignedSite(user.assignedSite ?: "")
                }
                is Result.Error -> {
                    if (isAdded) showError(result.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    private suspend fun loadAssignedSite(assignedSiteId: String) {
        if (assignedSiteId.isEmpty()) {
            showSiteEmptyState()
            return
        }

        when (val result = siteRepository.getSiteById(assignedSiteId)) {
            is Result.Success -> {
                val site = result.data
                if (site == null) {
                    showSiteEmptyState()
                    return
                }
                assignedSite = site
                showSiteCard(site)
            }
            is Result.Error -> {
                showSiteEmptyState()
                if (isAdded) showError(result.message)
            }
            is Result.Loading -> {}
        }
    }

    private fun showSiteCard(site: SiteModel) {
        siteCard.visibility = View.VISIBLE
        siteEmptyState.visibility = View.GONE

        siteName.text = site.siteName
        siteSupervisor.text = "Supervisor: ${site.supervisorName.ifEmpty { "Not assigned" }}"
        siteStatusBadge.text = site.status.displayName

        // Set shift times
        if (site.visitTimeFrom.isNotEmpty() && site.visitTimeTo.isNotEmpty()) {
            siteShift.text = "Shift: ${site.visitTimeFrom} - ${site.visitTimeTo}"
        } else {
            siteShift.text = "Shift: Not set"
        }

        // Load site image
        if (site.siteImageUrl.isNotEmpty() && isAdded) {
            Glide.with(this)
                .load(site.siteImageUrl)
                .centerCrop()
                .placeholder(R.drawable.siteeee)
                .into(siteImage)
        }

        // Setup location button
        setupLocationButton(site)
    }

    private fun showSiteEmptyState() {
        siteCard.visibility = View.GONE
        siteEmptyState.visibility = View.VISIBLE
    }

    private fun setupLocationButton(site: SiteModel) {
        navigateToSiteBtn.setOnClickListener {
            val lat = site.location.latitude
            val lng = site.location.longitude
            if (lat != 0.0 && lng != 0.0) {
                val uri = Uri.parse("google.navigation:q=$lat,$lng")
                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
                    startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                }
            } else {
                if (isAdded) showError("Site location not available")
            }
        }
    }

    // ── Profile ──────────────────────────────────────────────────────────────
    private fun setupProfileClick() {
        employeeDashboardProfile.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.employee_bottom_nav)
            bottomNav.selectedItemId = R.id.employee_profile
        }
    }

    // ── Status Dropdown ──────────────────────────────────────────────────────
    private fun setupStatusDropdown() {
        employeeStatusBtn.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            UserStatus.entries.forEach { status ->
                popup.menu.add(status.displayName)
            }
            popup.setOnMenuItemClickListener { menuItem ->
                val selectedStatus = UserStatus.entries.find { it.displayName == menuItem.title }
                    ?: return@setOnMenuItemClickListener false

                if (selectedStatus == UserStatus.ON_SITE) {
                    // ON_SITE requires location verification
                    if (assignedSite == null) {
                        showError("No site assigned to you")
                        return@setOnMenuItemClickListener true
                    }
                    if (hasLocationPermission()) {
                        showLocationVerificationDialog()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                } else {
                    updateUserStatus(selectedStatus)
                }
                true
            }
            popup.show()
        }
    }

    private fun updateUserStatus(status: UserStatus) {
        if (currentUserEmail.isEmpty()) return

        lifecycleScope.launch {
            // Update user status in users collection
            when (val result = firestoreDataSource.updateUserStatus(currentUserEmail, status.name)) {
                is Result.Success -> {
                    currentStatus = status
                    employeeStatusText.text = status.displayName
                    isProgrammaticSwitchChange = true
                    employeeOnsiteSwitch.isChecked = status == UserStatus.ON_SITE
                    isProgrammaticSwitchChange = false

                    // Save visit log record only for break/offline (on-site is handled by visit log screen)
                    if (status != UserStatus.ON_SITE) {
                        currentUser?.let { user ->
                            val timestampMillis = System.currentTimeMillis()
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val date = dateFormat.format(Date())
                            val displayFormat = SimpleDateFormat("dd MMM, yyyy - h:mm a", Locale.getDefault())
                            val formattedTimestamp = displayFormat.format(Date(timestampMillis))

                            val visitNotes = when (status) {
                                UserStatus.ON_BREAK -> "On Break"
                                UserStatus.OFFLINE -> "Offline"
                                else -> ""
                            }

                            firestoreDataSource.saveVisitLogRecord(
                                userId = user.email,
                                supervisorId = user.mySupervisor,
                                siteId = assignedSite?.siteId,
                                siteName = assignedSite?.siteName,
                                siteAddress = assignedSite?.location?.address,
                                siteLatitude = assignedSite?.location?.latitude,
                                siteLongitude = assignedSite?.location?.longitude,
                                visitNotes = visitNotes,
                                evidenceImages = emptyList(),
                                status = status.name,
                                timestamp = formattedTimestamp,
                                timestampMillis = timestampMillis,
                                date = date
                            )
                        }
                    }
                }
                is Result.Error -> {
                    if (isAdded) showError(result.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    // ── Navigation ───────────────────────────────────────────────────────────
    private fun navToNotifications() {
        employeeDashboardNotification.setOnClickListener {
            // TODO: Navigate to notifications screen when implemented
        }
    }

    private fun navToAddVisitLog() {
        addVisitLogBtn.setOnClickListener {
            if (assignedSite == null) {
                showError("No site assigned to you")
                return@setOnClickListener
            }
            if (hasLocationPermission()) {
                showLocationVerificationDialog()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
}

package com.harsh.worksphere.manager.sites.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.harsh.worksphere.core.utils.FileUtils
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import com.harsh.worksphere.R
import com.harsh.worksphere.components.CommonBottomSheet
import com.harsh.worksphere.core.firebase.FirebaseModule
import com.harsh.worksphere.initial.auth.data.model.User
import com.harsh.worksphere.manager.sites.MapManager
import com.harsh.worksphere.manager.sites.MapManagerCallback
import com.harsh.worksphere.manager.sites.data.model.SiteLocation
import com.harsh.worksphere.manager.sites.data.model.SiteModel
import com.harsh.worksphere.manager.sites.data.model.SiteStatus
import com.harsh.worksphere.manager.sites.data.model.SupervisorInfo
import com.harsh.worksphere.manager.sites.ui.adapter.SupervisorAdapter
import com.harsh.worksphere.manager.sites.viewmodel.SiteViewModel

class ManagerAddSiteActivity : AppCompatActivity(), MapManagerCallback {

    private lateinit var siteNameField: EditText
    private lateinit var clientNameField: EditText
    private lateinit var supervisorPhoneField: EditText
    private lateinit var siteAddressField: EditText
    private lateinit var workDetailsField: EditText
    private lateinit var latLngDisplay: TextView
    private lateinit var navBackButton: ImageButton
    private lateinit var submitButton: Button
    private lateinit var screenTitle: TextView

    private lateinit var selectSupervisorBtn: LinearLayout
    private lateinit var selectedSupervisorCard: MaterialCardView
    private lateinit var selectedSupervisorName: TextView
    private lateinit var selectedSupervisorEmail: TextView
    private lateinit var selectedSupervisorImage: ImageView
    private lateinit var deleteSupervisorBtn: ImageButton
    private lateinit var loader: ProgressBar
    private lateinit var mapManager: MapManager
    private val viewModel: SiteViewModel by viewModels()

    private var supervisorBottomSheet: CommonBottomSheet? = null
    private lateinit var addSiteImageBtn: FrameLayout
    private lateinit var selectedSiteImage: ImageView
    private var supervisorAdapter: SupervisorAdapter? = null
    private var pendingLocation: SiteLocation? = null

    private var isEditMode = false
    private lateinit var siteActiveToggle: MaterialSwitch
    private lateinit var statusContainer: LinearLayout
    private lateinit var statusDropdown: AutoCompleteTextView
    private val currentUserId = FirebaseModule.auth.currentUser?.email.toString()
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedImage(uri)
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_add_site_activity)

        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)

        initViews()
        setupListeners()
        setupObservers()
        initMapManager()
        setupTextWatchers()
        configureEditMode()

        if (isEditMode) {
            val siteData = intent.getParcelableExtra<SiteModel>("SITE_DATA")
            siteData?.let {
                viewModel.loadSiteForEditing(it)
                populateFields(it)
            }
        }
    }

    private fun initViews() {
        siteNameField = findViewById(R.id.site_name_field)
        clientNameField = findViewById(R.id.client_name_field)
        supervisorPhoneField = findViewById(R.id.site_supervisor_phone_number_field)
        siteAddressField = findViewById(R.id.site_address_field)
        workDetailsField = findViewById(R.id.site_work_details_field)
        latLngDisplay = findViewById(R.id.lat_lng_display)
        navBackButton = findViewById(R.id.navback)
        submitButton = findViewById(R.id.add_site_btn)
        screenTitle = findViewById(R.id.screen_title)
        loader = findViewById(R.id.addsite_progress_bar)
        selectSupervisorBtn = findViewById(R.id.select_site_supervisor_btn)
        selectedSupervisorCard = findViewById(R.id.selected_supervisor_card)
        selectedSupervisorName = findViewById(R.id.selected_supervisor_name)
        selectedSupervisorEmail = findViewById(R.id.selected_supervisor_email)
        selectedSupervisorImage = findViewById(R.id.selected_supervisor_image)
        deleteSupervisorBtn = findViewById(R.id.delete_supervisor_btn)
        siteActiveToggle = findViewById(R.id.site_active_toggle)
        statusContainer = findViewById(R.id.site_status_container)
        statusDropdown = findViewById(R.id.site_status_dropdown)
        addSiteImageBtn = findViewById(R.id.addSiteImageBtn)
        selectedSiteImage = findViewById(R.id.selectedSiteImage)
    }

    private fun configureEditMode() {
        if (isEditMode) {
            screenTitle.text = "Edit Site Configuration"
            submitButton.text = "Update Site"
            siteAddressField.isEnabled = false
            siteAddressField.isFocusable = false
            siteAddressField.isClickable = false
            siteAddressField.alpha = 0.6f
            siteActiveToggle.isVisible = true
            statusContainer.isVisible = true
            setupStatusDropdown()
        } else {
            screenTitle.text = "Site Configuration"
            submitButton.text = "Add Site"
            siteAddressField.isEnabled = true
            siteAddressField.isFocusable = true
            siteAddressField.isFocusableInTouchMode = true
            siteAddressField.isClickable = true
            siteAddressField.alpha = 1.0f
            siteActiveToggle.isVisible = false
            statusContainer.isVisible = false
        }
    }

    private fun setupStatusDropdown() {
        val statusNames = SiteStatus.displayNames()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusNames)
        statusDropdown.setAdapter(adapter)

        // Make dropdown open on click
        statusDropdown.setOnClickListener {
            statusDropdown.showDropDown()
        }

        statusDropdown.setOnItemClickListener { _, _, position, _ ->
            val selected = SiteStatus.entries[position]
            viewModel.setSiteStatus(selected)
        }
    }

    private fun setupListeners() {
        navBackButton.setOnClickListener { finish() }
        selectSupervisorBtn.setOnClickListener { showSupervisorBottomSheet() }
        deleteSupervisorBtn.setOnClickListener { removeSelectedSupervisor() }
        submitButton.setOnClickListener { saveSite() }
        siteActiveToggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setActive(isChecked)
        }
        addSiteImageBtn.setOnClickListener { openGallery() }
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun handleSelectedImage(uri: Uri) {
        // Copy image to app private storage for persistent access
        val persistentPath = FileUtils.copyImageToAppStorage(this, uri)

        if (persistentPath != null) {
            // Store path in ViewModel
            viewModel.setSiteImagePath(persistentPath)

            // Show selected image
            addSiteImageBtn.isVisible = false
            selectedSiteImage.isVisible = true

            // Load image
            Glide.with(this)
                .load(persistentPath)
                .placeholder(R.drawable.siteeee)
                .into(selectedSiteImage)
        } else {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }



    private fun showSupervisorBottomSheet() {
        supervisorAdapter = SupervisorAdapter { user ->
            onSupervisorSelected(user)
            supervisorBottomSheet?.dismiss()
        }

        supervisorBottomSheet = CommonBottomSheet.newInstance(
            config = CommonBottomSheet.Config(
                title = "Select Supervisor",
                emptyMessage = "No supervisors available",
                contentLayoutRes = R.layout.content_supervisor_list,
                contentBinder = { contentView ->
                    val recyclerView = contentView.findViewById<RecyclerView>(R.id.recyclerViewSupervisors)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.adapter = supervisorAdapter
                }
            )
        )

        supervisorBottomSheet?.show(supportFragmentManager, "supervisor_sheet")
        supervisorBottomSheet?.setLoading(true)
        viewModel.fetchSupervisors()
    }

    private fun setupObservers() {
        viewModel.error.observe(this) { error ->
            error?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        viewModel.success.observe(this) { success ->
            if (success) {
                val message = if (isEditMode) "Site updated successfully!" else "Site saved successfully!"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            submitButton.text = if (isLoading) "" else "Submit"
            loader.visibility = if (isLoading) View.VISIBLE else View.GONE
            submitButton.isEnabled = !isLoading
        }

        viewModel.supervisors.observe(this) { supervisors ->
            supervisorBottomSheet?.setLoading(false)
            if (supervisors.isEmpty()) {
                supervisorBottomSheet?.showEmptyState(true)
            } else {
                supervisorBottomSheet?.hideEmptyState()
                supervisorAdapter?.submitList(supervisors)
            }
        }
        viewModel.isActive.observe(this) { isActive ->
            siteActiveToggle.isChecked = isActive
        }
        viewModel.siteImagePath.observe(this) { path ->
            if (path.isNotEmpty()) {
                addSiteImageBtn.isVisible = false
                selectedSiteImage.isVisible = true
                Glide.with(this)
                    .load(path)
                    .placeholder(R.drawable.siteeee)
                    .into(selectedSiteImage)
            }
        }
    }

    private fun setupTextWatchers() {
        siteNameField.addTextChangedListener { viewModel.setSiteName(it.toString()) }
        clientNameField.addTextChangedListener { viewModel.setClientName(it.toString()) }
        supervisorPhoneField.addTextChangedListener { viewModel.setSupervisorPhone(it.toString()) }
        workDetailsField.addTextChangedListener { viewModel.setWorkDetails(it.toString()) }
    }

    private fun initMapManager() {
        mapManager = MapManager(this, this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.site_location_map) as SupportMapFragment

        if (mapManager.hasLocationPermission()) {
            mapManager.initialize(mapFragment)
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.site_location_map) as SupportMapFragment
            mapManager.initialize(mapFragment)
        }
    }

    private fun populateFields(site: SiteModel) {
        siteNameField.setText(site.siteName)
        clientNameField.setText(site.clientName)
        supervisorPhoneField.setText(site.supervisorPhone)
        workDetailsField.setText(site.workDetails)
        siteAddressField.setText(site.location.address)
        latLngDisplay.text = "Lat: ${String.format("%.6f", site.location.latitude)}, Lng: ${String.format("%.6f", site.location.longitude)}"
        if (site.siteImageUrl.isNotEmpty()) {
            addSiteImageBtn.isVisible = false
            selectedSiteImage.isVisible = true
            Glide.with(this)
                .load(site.siteImageUrl)
                .placeholder(R.drawable.siteeee)
                .into(selectedSiteImage)
        }
        if (site.supervisorId.isNotEmpty()) {
            selectedSupervisorCard.isVisible = true
            selectedSupervisorName.text = site.supervisorName
            selectedSupervisorEmail.text = site.supervisorId
            siteActiveToggle.isChecked = site.isActive

            Glide.with(this)
                .load(site.supervisorImageUrl)
                .placeholder(R.drawable.profile)
                .circleCrop()
                .into(selectedSupervisorImage)
        }

        // Pre-select current status in dropdown
        statusDropdown.setText(site.status.displayName, false)

        pendingLocation = site.location
    }

    override fun onMapReady() {
        if (isEditMode) {
            // Disable all map interactions in edit mode
            mapManager.setInteractionEnabled(false)

            // Show saved location
            pendingLocation?.let { location ->
                mapManager.setSelectedLocation(location.latitude, location.longitude, location.address)
                pendingLocation = null
            }
        } else {
            // Enable interactions in add mode
            mapManager.setInteractionEnabled(true)

            if (mapManager.hasLocationPermission()) {
                mapManager.getCurrentLocation(centerMap = true, selectAsLocation = false)
            }
        }
    }

    private fun onSupervisorSelected(user: User) {
        viewModel.setSupervisor(user.toSupervisorInfo())

        selectedSupervisorCard.isVisible = true
        selectedSupervisorName.text = user.name
        selectedSupervisorEmail.text = user.email

        // Auto-fill supervisor phone number
        if (user.phone.isNotEmpty()) {
            supervisorPhoneField.setText(user.phone)
        }

        Glide.with(this)
            .load(user.profilePic)
            .placeholder(R.drawable.profile)
            .circleCrop()
            .into(selectedSupervisorImage)
    }

    private fun removeSelectedSupervisor() {
        viewModel.setSupervisor(null)
        selectedSupervisorCard.isVisible = false
        supervisorPhoneField.setText("")
    }

    private fun User.toSupervisorInfo(): SupervisorInfo {
        return SupervisorInfo(
            id = this.email,
            name = this.name,
            address = "",
            phone = this.phone,
            imageUrl = this.profilePic ?: "",
            description = this.email
        )
    }

    private fun saveSite() {
        viewModel.setSiteName(siteNameField.text.toString())
        viewModel.setClientName(clientNameField.text.toString())
        viewModel.setSupervisorPhone(supervisorPhoneField.text.toString())
        viewModel.setWorkDetails(workDetailsField.text.toString())

        val currentLocation = mapManager.getSelectedLocation()
        if (currentLocation != null && viewModel.location.value == null) {
            viewModel.setLocation(SiteLocation(
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude,
                address = currentLocation.address
            ))
        }

        viewModel.saveSite(currentUserId)
    }

    override fun onLocationSelected(latitude: Double, longitude: Double, address: String) {
        latLngDisplay.text = "Lat: ${String.format("%.6f", latitude)}, Lng: ${String.format("%.6f", longitude)}"
        siteAddressField.setText(address)
        viewModel.setLocation(SiteLocation(latitude, longitude, address))
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapManager.cleanup()
    }
}
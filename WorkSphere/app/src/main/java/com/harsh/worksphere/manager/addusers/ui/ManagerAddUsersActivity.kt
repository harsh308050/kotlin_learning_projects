package com.harsh.worksphere.manager.addusers.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.harsh.worksphere.R
import com.harsh.worksphere.components.CommonBottomSheet
import com.harsh.worksphere.core.utils.Result
import com.harsh.worksphere.initial.auth.data.model.User
import com.harsh.worksphere.initial.auth.data.model.UserRole
import com.harsh.worksphere.manager.addusers.ui.adapter.*
import com.harsh.worksphere.manager.addusers.viewmodel.AddUserViewModel
import com.harsh.worksphere.manager.addusers.viewmodel.EmployeeViewModel
import com.harsh.worksphere.manager.sites.data.model.SiteModel
import com.harsh.worksphere.manager.sites.viewmodel.SiteViewModel
import kotlinx.coroutines.launch
import java.io.File

class ManagerAddUsersActivity : AppCompatActivity() {

    private val siteViewModel: SiteViewModel by viewModels()
    private val userViewModel: EmployeeViewModel by viewModels()
    private val addUserViewModel: AddUserViewModel by viewModels()

    // Bottom sheets
    private var siteBottomSheet: CommonBottomSheet? = null
    private var employeeBottomSheet: CommonBottomSheet? = null
    private var supervisorBottomSheet: CommonBottomSheet? = null

    // Adapters
    private var siteAdapter: SiteSelectionAdapter? = null
    private var employeeAdapter: EmployeeSelectionAdapter? = null
    private var supervisorAdapter: EmployeeSelectionAdapter? = null

    // Selected data
    private var selectedSite: SiteModel? = null
    private var selectedEmployees: List<User> = emptyList()
    private var selectedSupervisor: User? = null

    // Track pending reassignments: employeeEmail -> oldSupervisorEmail
    private val pendingReassignments = mutableMapOf<String, String>()

    // UI References...
    private lateinit var fullNameField: TextInputEditText
    private lateinit var emailField: TextInputEditText
    private lateinit var phoneField: TextInputEditText
    private lateinit var roleSelectionTab: RadioGroup
    private lateinit var submitBtn: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var selectSiteBtn: LinearLayout
    private lateinit var selectedSiteContainer: LinearLayout
    private lateinit var selectedSiteCard: MaterialCardView
    private lateinit var selectedSiteImage: ImageView
    private lateinit var selectedSiteName: TextView
    private lateinit var selectedSiteAddress: TextView
    private lateinit var selectedSiteDescription: TextView
    private lateinit var siteDiscardBtn: ImageButton

    private lateinit var selectEmployeeBtn: LinearLayout
    private lateinit var selectedEmployeesContainer: LinearLayout
    private lateinit var selectedEmployeesListLayout: LinearLayout

    private lateinit var selectSupervisorBtn: LinearLayout
    private lateinit var selectedSupervisorContainer: LinearLayout
    private lateinit var selectedSupervisorCard: MaterialCardView
    private lateinit var selectedSupervisorImage: ImageView
    private lateinit var selectedSupervisorName: TextView
    private lateinit var selectedSupervisorEmail: TextView
    private lateinit var supervisorDiscardBtn: ImageButton
    private lateinit var navback: ImageButton

    private var currentRole: UserRole = UserRole.Supervisor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_adduser_activity)
        initViews()
        roleSelection()
        setupListeners()
        setupObservers()
        navback.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        fullNameField = findViewById(R.id.full_name_field)
        emailField = findViewById(R.id.employee_mail_id_field)
        phoneField = findViewById(R.id.phone_number_field)
        roleSelectionTab = findViewById(R.id.roleSelectionTab)
        submitBtn = findViewById(R.id.add_user_btn)
        progressBar = findViewById(R.id.addUser_progress_bar)

        selectSiteBtn = findViewById(R.id.select_site_btn)
        selectedSiteContainer = findViewById(R.id.selected_site_container)
        selectedSiteCard = findViewById(R.id.selected_site)
        selectedSiteImage = findViewById(R.id.selected_site_image)
        selectedSiteName = findViewById(R.id.selected_site_name)
        selectedSiteAddress = findViewById(R.id.selected_site_address)
        selectedSiteDescription = findViewById(R.id.selected_site_description)
        siteDiscardBtn = findViewById(R.id.site_discard)

        selectEmployeeBtn = findViewById(R.id.select_employee_btn)
        selectedEmployeesContainer = findViewById(R.id.selected_employees_container)
        selectedEmployeesListLayout = findViewById(R.id.selected_employees_list)

        selectSupervisorBtn = findViewById(R.id.select_supervisor_btn)
        selectedSupervisorContainer = findViewById(R.id.selected_supervisor_container)
        selectedSupervisorCard = findViewById(R.id.selected_supervisor)
        selectedSupervisorImage = findViewById(R.id.selected_supervisor_image)
        selectedSupervisorName = findViewById(R.id.selected_supervisor_name)
        selectedSupervisorEmail = findViewById(R.id.selected_supervisor_email)
        supervisorDiscardBtn = findViewById(R.id.supervisor_discard)
        navback = findViewById(R.id.navback)
    }

    private fun setupListeners() {
        selectSiteBtn.setOnClickListener { showSiteBottomSheet() }
        selectEmployeeBtn.setOnClickListener { showEmployeeBottomSheet() }
        siteDiscardBtn.setOnClickListener { removeSelectedSite() }

        selectSupervisorBtn.setOnClickListener { showSupervisorBottomSheet() }
        supervisorDiscardBtn.setOnClickListener { removeSelectedSupervisor() }

        submitBtn.setOnClickListener { validateAndSubmit() }
    }

    private fun setupObservers() {
        siteViewModel.sites.observe(this) { sites ->
            siteBottomSheet?.setLoading(false)
            if (sites.isEmpty()) {
                siteBottomSheet?.showEmptyState(true)
            } else {
                siteBottomSheet?.hideEmptyState()
                siteAdapter?.submitList(sites)
            }
        }

        siteViewModel.error.observe(this) { error ->
            error?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        userViewModel.employees.observe(this) { users ->
            when {
                employeeBottomSheet?.dialog?.isShowing == true -> {
                    employeeBottomSheet?.setLoading(false)
                    if (users.isEmpty()) {
                        employeeBottomSheet?.showEmptyState(true)
                    } else {
                        employeeBottomSheet?.hideEmptyState()
                        employeeAdapter?.submitList(users)
                    }
                }
                supervisorBottomSheet?.dialog?.isShowing == true -> {
                    supervisorBottomSheet?.setLoading(false)
                    if (users.isEmpty()) {
                        supervisorBottomSheet?.showEmptyState(true)
                    } else {
                        supervisorBottomSheet?.hideEmptyState()
                        supervisorAdapter?.submitList(users)
                    }
                }
            }
        }

        userViewModel.error.observe(this) { error ->
            error?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        addUserViewModel.createUserResult.observe(this) { result ->
            progressBar.isVisible = false
            submitBtn.isEnabled = true

            when (result) {
                is Result.Success -> {
                    Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
                is Result.Loading -> {
                    progressBar.isVisible = true
                    submitBtn.isEnabled = false
                }
            }
        }
    }

    private fun showSiteBottomSheet() {
        siteAdapter = SiteSelectionAdapter { site ->
            onSiteSelected(site)
            siteBottomSheet?.dismiss()
        }

        siteBottomSheet = CommonBottomSheet.newInstance(
            config = CommonBottomSheet.Config(
                title = "Select Site",
                emptyMessage = "No sites available",
                contentLayoutRes = R.layout.content_site_list,
                contentBinder = { contentView ->
                    val recyclerView = contentView.findViewById<RecyclerView>(R.id.recyclerViewSites)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.adapter = siteAdapter
                }
            )
        )

        siteBottomSheet?.show(supportFragmentManager, "site_sheet")
        siteBottomSheet?.setLoading(true)
        siteViewModel.fetchSites()
    }

    private fun showEmployeeBottomSheet() {
        employeeAdapter = EmployeeSelectionAdapter(
            onSelectionChanged = { selectedList ->
                // This is called when selection changes
            },
            singleSelection = false,
            showAssignmentStatus = true,
            onReassignRequested = { employee, callback ->
                showReassignmentDialog(employee, callback)
            }
        )

        employeeBottomSheet = CommonBottomSheet.newInstance(
            config = CommonBottomSheet.Config(
                title = "Select Employees",
                emptyMessage = "No employees available",
                contentLayoutRes = R.layout.content_employee_list,
                contentBinder = { contentView ->
                    val recyclerView = contentView.findViewById<RecyclerView>(R.id.recyclerViewEmployees)
                    val btnDone = contentView.findViewById<Button>(R.id.btnDone)

                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.adapter = employeeAdapter

                    // Pre-select already selected employees and restore reassignments
                    employeeAdapter?.setSelectedEmployees(selectedEmployees)
                    // Restore pending reassignments to adapter
                    pendingReassignments.keys.forEach { email ->
                        employeeAdapter?.confirmReassignment(email)
                    }

                    btnDone.setOnClickListener {
                        // Get the NEWLY selected employees from adapter
                        val newlySelected = employeeAdapter?.getSelectedEmployees() ?: emptyList()

                        // Build pending reassignments map based on which employees have mySupervisor set
                        // and are marked for reassignment in the adapter
                        val adapterPendingReassignments = employeeAdapter?.getPendingReassignments() ?: emptySet()

                        pendingReassignments.clear()
                        newlySelected.forEach { employee ->
                            // Only add to pendingReassignments if:
                            // 1. Employee has a current supervisor (mySupervisor is not null/empty)
                            // 2. Employee is marked for reassignment in the adapter
                            if (!employee.mySupervisor.isNullOrEmpty() && adapterPendingReassignments.contains(employee.email)) {
                                pendingReassignments[employee.email] = employee.mySupervisor!!

                            }
                        }

                        selectedEmployees = newlySelected
                        onEmployeesSelected(selectedEmployees)
                        employeeBottomSheet?.dismiss()
                    }
                }
            )
        )

        employeeBottomSheet?.show(supportFragmentManager, "employee_sheet")
        employeeBottomSheet?.setLoading(true)
        userViewModel.fetchEmployees()
    }

    private fun showReassignmentDialog(employee: User, callback: (Boolean) -> Unit) {
        val oldSupervisor = employee.mySupervisor ?: "Unknown"

        val dialog = Dialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reassign_employee, null)
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)

        dialogView.findViewById<TextView>(R.id.dialog_employee_name).text = employee.name
        dialogView.findViewById<TextView>(R.id.dialog_current_supervisor).text =
            "Currently assigned to: $oldSupervisor"

        dialogView.findViewById<MaterialButton>(R.id.dialog_reassign_btn).setOnClickListener {
            callback(true)
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.dialog_cancel_btn).setOnClickListener {
            callback(false)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showSupervisorBottomSheet() {
        supervisorAdapter = EmployeeSelectionAdapter(
            onSelectionChanged = { selectedList ->
                selectedSupervisor = selectedList.firstOrNull()
            },
            singleSelection = true
        )

        supervisorBottomSheet = CommonBottomSheet.newInstance(
            config = CommonBottomSheet.Config(
                title = "Select Supervisor",
                emptyMessage = "No supervisors available",
                contentLayoutRes = R.layout.content_employee_list,
                contentBinder = { contentView ->
                    val recyclerView = contentView.findViewById<RecyclerView>(R.id.recyclerViewEmployees)
                    val btnDone = contentView.findViewById<Button>(R.id.btnDone)

                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.adapter = supervisorAdapter

                    selectedSupervisor?.let { supervisor ->
                        supervisorAdapter?.setSelectedEmployees(listOf(supervisor))
                    }

                    btnDone.setOnClickListener {
                        selectedSupervisor = supervisorAdapter?.getSelectedEmployees()?.firstOrNull()
                        onSupervisorSelected(selectedSupervisor)
                        supervisorBottomSheet?.dismiss()
                    }
                }
            )
        )

        supervisorBottomSheet?.show(supportFragmentManager, "supervisor_sheet")
        supervisorBottomSheet?.setLoading(true)
        userViewModel.fetchSupervisors()
    }

    private fun onSiteSelected(site: SiteModel) {
        selectedSite = site
        selectedSiteContainer.isVisible = true
        selectSiteBtn.isVisible = true

        selectedSiteName.text = site.siteName
        selectedSiteAddress.text = site.location.address
        selectedSiteDescription.text = site.workDetails

        if (site.siteImageUrl.isNotEmpty() && File(site.siteImageUrl).exists()) {
            Glide.with(this)
                .load(File(site.siteImageUrl))
                .placeholder(R.drawable.siteeee)
                .into(selectedSiteImage)
        } else {
            selectedSiteImage.setImageResource(R.drawable.siteeee)
        }
    }

    private fun removeSelectedSite() {
        selectedSite = null
        selectedSiteContainer.isVisible = false
    }

    private fun onEmployeesSelected(employees: List<User>) {
        selectedEmployeesListLayout.removeAllViews()

        if (employees.isEmpty()) {
            selectedEmployeesContainer.isVisible = false
            return
        }

        selectedEmployeesContainer.isVisible = true
        selectEmployeeBtn.isVisible = true

        employees.forEach { employee ->
            val cardView = LayoutInflater.from(this)
                .inflate(R.layout.selected_employee_chip, selectedEmployeesListLayout, false) as MaterialCardView

            val imageView = cardView.findViewById<ImageView>(R.id.chip_employee_image)
            val nameView = cardView.findViewById<TextView>(R.id.chip_employee_name)
            val emailView = cardView.findViewById<TextView>(R.id.chip_employee_email)
            val removeBtn = cardView.findViewById<ImageButton>(R.id.chip_remove_btn)
            val reassignedBadge = cardView.findViewById<TextView>(R.id.reassigned_badge)

            nameView.text = employee.name
            emailView.text = employee.email

            // Show reassigned badge if this employee is being reassigned
            if (pendingReassignments.containsKey(employee.email)) {
                reassignedBadge?.isVisible = true
                reassignedBadge?.text = "Reassigned"
            } else {
                reassignedBadge?.isVisible = false
            }

            Glide.with(this)
                .load(employee.profilePic)
                .placeholder(R.drawable.profile)
                .circleCrop()
                .into(imageView)

            removeBtn.setOnClickListener {
                selectedEmployees = selectedEmployees.filter { it.email != employee.email }
                pendingReassignments.remove(employee.email)
                onEmployeesSelected(selectedEmployees)
            }

            selectedEmployeesListLayout.addView(cardView)
        }
    }

    private fun onSupervisorSelected(supervisor: User?) {
        if (supervisor == null) {
            selectedSupervisorContainer.isVisible = false
            return
        }

        selectedSupervisorContainer.isVisible = true
        selectSupervisorBtn.isVisible = true

        selectedSupervisorName.text = supervisor.name
        selectedSupervisorEmail.text = supervisor.email

        Glide.with(this)
            .load(supervisor.profilePic)
            .placeholder(R.drawable.profile)
            .circleCrop()
            .into(selectedSupervisorImage)
    }

    private fun removeSelectedSupervisor() {
        selectedSupervisor = null
        selectedSupervisorContainer.isVisible = false
    }

    private fun validateAndSubmit() {
        val name = fullNameField.text?.toString()?.trim() ?: ""
        val email = emailField.text?.toString()?.trim() ?: ""
        val phone = phoneField.text?.toString()?.trim() ?: ""

        if (name.isEmpty()) {
            fullNameField.error = "Name is required"
            return
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.error = "Valid email is required"
            return
        }
        if (phone.isEmpty() || phone.length < 10) {
            phoneField.error = "Valid phone number is required"
            return
        }

        when (currentRole) {
            UserRole.Supervisor -> {
                val reassignments = pendingReassignments.toList()

                addUserViewModel.createUser(
                    name = name,
                    email = email,
                    phone = phone,
                    role = UserRole.Supervisor,
                    selectedSite = selectedSite?.siteId, // Can be null
                    assignedEmployees = selectedEmployees.map { it.email }, // Can be empty
                    reassignedEmployees = reassignments
                )
            }
            UserRole.Employee -> {
                lifecycleScope.launch {
                    var supervisorSiteId: String? = null

                    if (selectedSupervisor != null) {
                        supervisorSiteId = addUserViewModel.getSupervisorSiteId(selectedSupervisor!!.email)
                    }



                    addUserViewModel.createUser(
                        name = name,
                        email = email,
                        phone = phone,
                        role = UserRole.Employee,
                        mySupervisor = selectedSupervisor?.email, // Can be null
                        supervisorSiteId = supervisorSiteId // Can be null
                    )
                }
            }
            else -> {
                Toast.makeText(this, "Invalid role selected", Toast.LENGTH_SHORT).show()
                return
            }
        }
    }

    private fun roleSelection() {
        val supervisorFields = findViewById<LinearLayout>(R.id.supervisorFields)
        val employeeFields = findViewById<LinearLayout>(R.id.employeeFields)

        roleSelectionTab.check(R.id.btnSupervisor)

        roleSelectionTab.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.btnSupervisor -> {
                    currentRole = UserRole.Supervisor
                    supervisorFields.visibility = View.VISIBLE
                    employeeFields.visibility = View.GONE
                }
                R.id.btnEmployee -> {
                    currentRole = UserRole.Employee
                    supervisorFields.visibility = View.GONE
                    employeeFields.visibility = View.VISIBLE
                }
            }
        }
    }
}
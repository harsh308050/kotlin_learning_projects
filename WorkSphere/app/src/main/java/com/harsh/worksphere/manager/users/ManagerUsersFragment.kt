package com.harsh.worksphere.manager.users

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.card.MaterialCardView
import com.harsh.worksphere.R
import com.harsh.worksphere.manager.users.adapter.EmployeeListAdapter
import com.harsh.worksphere.manager.users.adapter.SupervisorListAdapter
import com.harsh.worksphere.manager.users.viewmodel.AssignmentFilter
import com.harsh.worksphere.manager.users.viewmodel.RoleTab
import com.harsh.worksphere.manager.users.viewmodel.StatusFilter
import com.harsh.worksphere.manager.users.viewmodel.UsersViewModel

class ManagerUsersFragment : Fragment(R.layout.manager_users_fragment) {

    private val viewModel: UsersViewModel by viewModels()

    private lateinit var searchEditText: EditText
    private lateinit var roleTabGroup: RadioGroup
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var shimmerLayout: ShimmerFrameLayout

    // Status filter chips
    private lateinit var chipAll: TextView
    private lateinit var chipOnSite: TextView
    private lateinit var chipOnBreak: TextView
    private lateinit var chipOffline: TextView
    private lateinit var allStatusChips: List<TextView>

    // Assignment filter dropdown
    private lateinit var assignmentDropdown: MaterialCardView
    private lateinit var assignmentDropdownText: TextView

    private lateinit var supervisorAdapter: SupervisorListAdapter
    private lateinit var employeeAdapter: EmployeeListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupAdapters()
        setupTabs()
        setupFilterChips()
        setupSearch()
        observeViewModel()

        viewModel.fetchUsers()
    }

    private fun initViews(view: View) {
        searchEditText = view.findViewById(R.id.searchEditText)
        roleTabGroup = view.findViewById(R.id.roleTabGroup)
        usersRecyclerView = view.findViewById(R.id.usersRecyclerView)
        emptyState = view.findViewById(R.id.emptyState)
        chipAll = view.findViewById(R.id.chipAll)
        chipOnSite = view.findViewById(R.id.chipOnSite)
        chipOnBreak = view.findViewById(R.id.chipOnBreak)
        chipOffline = view.findViewById(R.id.chipOffline)

        allStatusChips = listOf(chipAll, chipOnSite, chipOnBreak, chipOffline)

        assignmentDropdown = view.findViewById(R.id.assignmentDropdown)
        assignmentDropdownText = view.findViewById(R.id.assignmentDropdownText)
        shimmerLayout = view.findViewById(R.id.users_shimmer_layout)
    }

    private fun setupAdapters() {
        supervisorAdapter = SupervisorListAdapter(
            onItemClick = {  },
        )

        employeeAdapter = EmployeeListAdapter(
            onItemClick = {  },
            supervisorNameResolver = { email -> viewModel.getSupervisorName(email) }
        )

        usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        usersRecyclerView.adapter = supervisorAdapter
    }

    private fun setupTabs() {
        roleTabGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.tabSupervisors -> {
                    viewModel.setRoleTab(RoleTab.SUPERVISORS)
                    usersRecyclerView.adapter = supervisorAdapter
                }
                R.id.tabEmployees -> {
                    viewModel.setRoleTab(RoleTab.EMPLOYEES)
                    usersRecyclerView.adapter = employeeAdapter
                }
            }
        }
    }

    private fun setupFilterChips() {
        // Status chips - mutual exclusive among status group
        chipAll.setOnClickListener { selectStatusChip(chipAll, StatusFilter.ALL) }
        chipOnSite.setOnClickListener { selectStatusChip(chipOnSite, StatusFilter.ON_SITE) }
        chipOnBreak.setOnClickListener { selectStatusChip(chipOnBreak, StatusFilter.ON_BREAK) }
        chipOffline.setOnClickListener { selectStatusChip(chipOffline, StatusFilter.OFFLINE) }

        // Assignment dropdown
        assignmentDropdown.setOnClickListener { showAssignmentDropdown() }
    }

    private fun showAssignmentDropdown() {
        val popup = PopupMenu(requireContext(), assignmentDropdown)
        popup.menu.add(0, 0, 0, "All")
        popup.menu.add(0, 1, 1, "Assigned")
        popup.menu.add(0, 2, 2, "Unassigned")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                0 -> {
                    assignmentDropdownText.text = "All"
                    viewModel.setAssignmentFilter(AssignmentFilter.ALL)
                    updateDropdownStyle(false)
                }
                1 -> {
                    assignmentDropdownText.text = "Assigned"
                    viewModel.setAssignmentFilter(AssignmentFilter.ASSIGNED)
                    updateDropdownStyle(true)
                }
                2 -> {
                    assignmentDropdownText.text = "Unassigned"
                    viewModel.setAssignmentFilter(AssignmentFilter.UNASSIGNED)
                    updateDropdownStyle(true)
                }
            }
            true
        }
        popup.show()
    }

    private fun updateDropdownStyle(isFiltered: Boolean) {
        if (isFiltered) {
            assignmentDropdown.setCardBackgroundColor(
                resources.getColor(R.color.primary_blue, null)
            )
        } else {
            assignmentDropdownText.setTextColor(
                resources.getColor(R.color.dark_grey, null)
            )
        }
    }

    private fun selectStatusChip(selected: TextView, filter: StatusFilter) {
        allStatusChips.forEach { it.isSelected = it == selected }
        viewModel.setStatusFilter(filter)
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewModel.filteredUsers.observe(viewLifecycleOwner) { users ->
            val isSupervisorTab = viewModel.roleTab.value == RoleTab.SUPERVISORS
            if (isSupervisorTab) {
                supervisorAdapter.submitList(users)
            } else {
                employeeAdapter.submitList(users)
            }
            emptyState.isVisible = users.isEmpty() && viewModel.isLoading.value != true
            usersRecyclerView.isVisible = users.isNotEmpty()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                shimmerLayout.isVisible = true
                shimmerLayout.startShimmer()
                usersRecyclerView.isVisible = false
                emptyState.isVisible = false
            } else {
                shimmerLayout.stopShimmer()
                shimmerLayout.isVisible = false
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // Reset chip UI when roleTab changes
        viewModel.roleTab.observe(viewLifecycleOwner) {
            // Reset status chip selections
            allStatusChips.forEach { it.isSelected = false }
            chipAll.isSelected = true
            // Reset assignment dropdown
            assignmentDropdownText.text = "All"
            updateDropdownStyle(false)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchUsers()
    }
}
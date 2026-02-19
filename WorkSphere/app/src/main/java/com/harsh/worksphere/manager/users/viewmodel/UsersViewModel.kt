package com.harsh.worksphere.manager.users.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.worksphere.core.utils.Result
import com.harsh.worksphere.initial.auth.data.model.User
import com.harsh.worksphere.initial.auth.data.model.UserRole
import com.harsh.worksphere.initial.auth.data.model.UserStatus
import com.harsh.worksphere.initial.auth.data.remote.FirestoreDataSource
import kotlinx.coroutines.launch

enum class RoleTab { SUPERVISORS, EMPLOYEES }
enum class StatusFilter { ALL, ON_SITE, ON_BREAK, OFFLINE }
enum class AssignmentFilter { ALL, ASSIGNED, UNASSIGNED }

class UsersViewModel : ViewModel() {

    private val firestoreDataSource = FirestoreDataSource()

    // All fetched users
    private val _allUsers = MutableLiveData<List<User>>(emptyList())

    // Loading & error
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Filters
    private val _roleTab = MutableLiveData(RoleTab.SUPERVISORS)
    val roleTab: LiveData<RoleTab> = _roleTab

    private val _statusFilter = MutableLiveData(StatusFilter.ALL)
    val statusFilter: LiveData<StatusFilter> = _statusFilter

    private val _assignmentFilter = MutableLiveData(AssignmentFilter.ALL)
    val assignmentFilter: LiveData<AssignmentFilter> = _assignmentFilter

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    // Filtered results
    private val _filteredUsers = MediatorLiveData<List<User>>()
    val filteredUsers: LiveData<List<User>> = _filteredUsers

    init {
        // Re-filter whenever any source changes
        _filteredUsers.addSource(_allUsers) { applyFilters() }
        _filteredUsers.addSource(_roleTab) { applyFilters() }
        _filteredUsers.addSource(_statusFilter) { applyFilters() }
        _filteredUsers.addSource(_assignmentFilter) { applyFilters() }
        _filteredUsers.addSource(_searchQuery) { applyFilters() }
    }

    fun setRoleTab(tab: RoleTab) {
        _roleTab.value = tab
        // Reset sub-filters when switching tab
        _statusFilter.value = StatusFilter.ALL
        _assignmentFilter.value = AssignmentFilter.ALL
    }

    fun setStatusFilter(filter: StatusFilter) {
        _statusFilter.value = filter
    }

    fun setAssignmentFilter(filter: AssignmentFilter) {
        _assignmentFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun fetchUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = firestoreDataSource.getAllUsers()) {
                is Result.Success -> {
                    // Exclude managers from the list
                    _allUsers.value = result.data.filter { it.role != UserRole.Manager }
                }
                is Result.Error -> {
                    _error.value = result.message
                }
                is Result.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    private fun applyFilters() {
        val all = _allUsers.value ?: emptyList()
        val tab = _roleTab.value ?: RoleTab.SUPERVISORS
        val status = _statusFilter.value ?: StatusFilter.ALL
        val assignment = _assignmentFilter.value ?: AssignmentFilter.ALL
        val query = _searchQuery.value?.trim()?.lowercase() ?: ""

        var filtered = all

        // 1. Role filter
        filtered = when (tab) {
            RoleTab.SUPERVISORS -> filtered.filter { it.role == UserRole.Supervisor }
            RoleTab.EMPLOYEES -> filtered.filter { it.role == UserRole.Employee }
        }

        // 2. Status filter
        filtered = when (status) {
            StatusFilter.ALL -> filtered
            StatusFilter.ON_SITE -> filtered.filter { it.status == UserStatus.ON_SITE }
            StatusFilter.ON_BREAK -> filtered.filter { it.status == UserStatus.ON_BREAK }
            StatusFilter.OFFLINE -> filtered.filter { it.status == UserStatus.OFFLINE }
        }

        // 3. Assignment filter
        filtered = when (assignment) {
            AssignmentFilter.ALL -> filtered
            AssignmentFilter.ASSIGNED -> when (tab) {
                RoleTab.SUPERVISORS -> filtered.filter { it.assignedSite != null && it.assignedSite.isNotEmpty() }
                RoleTab.EMPLOYEES -> filtered.filter { it.mySupervisor != null && it.mySupervisor.isNotEmpty() }
            }
            AssignmentFilter.UNASSIGNED -> when (tab) {
                RoleTab.SUPERVISORS -> filtered.filter { it.assignedSite.isNullOrEmpty() }
                RoleTab.EMPLOYEES -> filtered.filter { it.mySupervisor.isNullOrEmpty() }
            }
        }

        // 4. Search query
        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.name.lowercase().contains(query) ||
                        it.email.lowercase().contains(query) ||
                        it.phone.lowercase().contains(query)
            }
        }

        _filteredUsers.value = filtered
    }

    /** Helper to get supervisor name by email from the cached user list */
    fun getSupervisorName(email: String): String {
        return _allUsers.value
            ?.find { it.email == email && it.role == UserRole.Supervisor }
            ?.name ?: email
    }
}

// com.harsh.worksphere.manager.sites.viewmodel/SiteViewModel.kt
package com.harsh.worksphere.manager.sites.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.worksphere.initial.auth.data.model.User
import com.harsh.worksphere.initial.auth.data.remote.FirestoreDataSource
import com.harsh.worksphere.manager.sites.data.model.*
import com.harsh.worksphere.manager.sites.repository.SiteRepository
import kotlinx.coroutines.launch

class SiteViewModel : ViewModel() {

    private val repository = SiteRepository()
    private val firestoreDataSource = FirestoreDataSource()

    // Existing site creation fields...
    private val _siteName = MutableLiveData("")
    val siteName: LiveData<String> = _siteName

    private val _clientName = MutableLiveData("")
    val clientName: LiveData<String> = _clientName

    private val _supervisor = MutableLiveData<SupervisorInfo?>()
    val supervisor: LiveData<SupervisorInfo?> = _supervisor

    private val _supervisorPhone = MutableLiveData("")
    val supervisorPhone: LiveData<String> = _supervisorPhone

    private val _location = MutableLiveData<SiteLocation?>()
    val location: LiveData<SiteLocation?> = _location

    private val _workDetails = MutableLiveData("")
    val workDetails: LiveData<String> = _workDetails

    // Loading & Error states
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> = _success

    // Sites list for fragment
    private val _sites = MutableLiveData<List<SiteModel>>()
    val sites: LiveData<List<SiteModel>> = _sites

    private val _isLoadingSites = MutableLiveData(false)
    val isLoadingSites: LiveData<Boolean> = _isLoadingSites

    // Supervisors list
    private val _supervisors = MutableLiveData<List<User>>()
    val supervisors: LiveData<List<User>> = _supervisors

    private val _isLoadingSupervisors = MutableLiveData(false)
    val isLoadingSupervisors: LiveData<Boolean> = _isLoadingSupervisors

    // Editing mode
    private val _isEditMode = MutableLiveData(false)
    val isEditMode: LiveData<Boolean> = _isEditMode

    private var currentSiteId: String = ""
    fun getCurrentSiteId(): String = currentSiteId
    private var originalSupervisorId: String = ""
    private var _pendingReassignmentSiteId: String? = null
    private var _pendingReassignmentSupervisorEmail: String? = null

    fun setPendingReassignment(oldSiteId: String, supervisorEmail: String) {
        _pendingReassignmentSiteId = oldSiteId
        _pendingReassignmentSupervisorEmail = supervisorEmail
    }

    fun clearPendingReassignment() {
        _pendingReassignmentSiteId = null
        _pendingReassignmentSupervisorEmail = null
    }
    private val _isActive = MutableLiveData(true)
    val isActive: LiveData<Boolean> = _isActive
    private val _siteImageUrl = MutableLiveData<String>()
    val siteImagePath: LiveData<String> = _siteImageUrl
    private val _siteStatus = MutableLiveData(SiteStatus.PENDING_ASSIGNMENT)
    val siteStatus: LiveData<SiteStatus> = _siteStatus

    private val _visitTimeFrom = MutableLiveData("")
    val visitTimeFrom: LiveData<String> = _visitTimeFrom

    private val _visitTimeTo = MutableLiveData("")
    val visitTimeTo: LiveData<String> = _visitTimeTo

    fun setSiteImagePath(path: String) {
        _siteImageUrl.value = path
    }

    fun setActive(active: Boolean) {
        _isActive.value = active
    }

    fun setSiteStatus(status: SiteStatus) {
        _siteStatus.value = status
    }

    fun setVisitTimeFrom(time: String) {
        _visitTimeFrom.value = time
    }

    fun setVisitTimeTo(time: String) {
        _visitTimeTo.value = time
    }

    // Setters for UI updates
    fun setSiteName(name: String) { _siteName.value = name }
    fun setClientName(name: String) { _clientName.value = name }
    fun setSupervisor(supervisor: SupervisorInfo?) {
        _supervisor.value = supervisor
        _supervisorPhone.value = supervisor?.phone ?: ""
    }
    fun setSupervisorPhone(phone: String) { _supervisorPhone.value = phone }
    fun setLocation(location: SiteLocation?) { _location.value = location }
    fun setWorkDetails(details: String) { _workDetails.value = details }

    fun loadSiteForEditing(site: SiteModel) {
        _isEditMode.value = true
        currentSiteId = site.siteId
        originalSupervisorId = site.supervisorId
        _siteName.value = site.siteName
        _clientName.value = site.clientName
        _supervisor.value = SupervisorInfo(
            id = site.supervisorId,
            name = site.supervisorName,
            phone = site.supervisorPhone,
            imageUrl = site.supervisorImageUrl,
            address = "",
            description = site.supervisorId
        )
        _supervisorPhone.value = site.supervisorPhone
        _location.value = site.location
        _workDetails.value = site.workDetails
        _isActive.value = site.isActive
        _siteImageUrl.value = site.siteImageUrl
        _siteStatus.value = site.status
        _visitTimeFrom.value = site.visitTimeFrom
        _visitTimeTo.value = site.visitTimeTo
    }

    fun clearEditMode() {
        _isEditMode.value = false
        currentSiteId = ""
        originalSupervisorId = ""
        clear()
    }

    fun fetchSites() {
        viewModelScope.launch {
            _isLoadingSites.value = true
            when (val result = repository.getAllSites()) {
                is com.harsh.worksphere.core.utils.Result.Success -> {
                    _sites.value = result.data
                }
                is com.harsh.worksphere.core.utils.Result.Error -> {
                    _error.value = result.message
                }
                is com.harsh.worksphere.core.utils.Result.Loading -> {
                    _isLoadingSites.value = true
                }
            }
            _isLoadingSites.value = false
        }
    }

    fun fetchSupervisors() {
        viewModelScope.launch {
            _isLoadingSupervisors.value = true
            when (val result = firestoreDataSource.getAllUsers()) {
                is com.harsh.worksphere.core.utils.Result.Success -> {
                    val supervisorList = result.data.filter {
                        it.role == com.harsh.worksphere.initial.auth.data.model.UserRole.Supervisor
                    }
                    _supervisors.value = supervisorList
                }
                is com.harsh.worksphere.core.utils.Result.Error -> {
                    _error.value = result.message
                }
                is com.harsh.worksphere.core.utils.Result.Loading -> {
                    _isLoadingSupervisors.value = true
                }
            }
            _isLoadingSupervisors.value = false
        }
    }

    fun saveSite(createdBy: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val validationError = validateInput()
            if (validationError != null) {
                _error.value = validationError
                _isLoading.value = false
                return@launch
            }

            val site = SiteModel(
                siteId = currentSiteId,
                siteName = _siteName.value!!,
                clientName = _clientName.value!!,
                supervisorId = _supervisor.value?.id ?: "",
                supervisorName = _supervisor.value?.name ?: "",
                supervisorPhone = _supervisorPhone.value ?: "",
                supervisorImageUrl = _supervisor.value?.imageUrl ?: "",
                location = _location.value ?: SiteLocation(),
                workDetails = _workDetails.value ?: "",
                createdBy = createdBy,
                isActive = _isActive.value ?: true,
                siteImageUrl= _siteImageUrl.value ?: "",
                status = resolveStatus(),
                visitTimeFrom = _visitTimeFrom.value ?: "",
                visitTimeTo = _visitTimeTo.value ?: ""
            )

            val isEdit = _isEditMode.value == true
            var savedSiteId = site.siteId

            val success = if (isEdit) {
                val result = repository.updateSite(site)
                when (result) {
                    is com.harsh.worksphere.core.utils.Result.Success -> true
                    is com.harsh.worksphere.core.utils.Result.Error -> {
                        _error.value = result.message; false
                    }
                    is com.harsh.worksphere.core.utils.Result.Loading -> false
                }
            } else {
                val result = repository.createSite(site)
                when (result) {
                    is com.harsh.worksphere.core.utils.Result.Success -> {
                        savedSiteId = result.data; true
                    }
                    is com.harsh.worksphere.core.utils.Result.Error -> {
                        _error.value = result.message; false
                    }
                    is com.harsh.worksphere.core.utils.Result.Loading -> false
                }
            }

            if (success) {
                val isEdit = _isEditMode.value == true
                val oldSupervisorId = originalSupervisorId
                val newSupervisorId = site.supervisorId

                if (isEdit && oldSupervisorId.isNotEmpty() && oldSupervisorId != newSupervisorId) {
                    // Supervisor was removed or changed — clear assignedSite from old supervisor & their employees
                    repository.clearUsersFromSite(oldSupervisorId)
                }

                // If supervisor was reassigned from another site, clear that old site's supervisor fields
                val reassignSiteId = _pendingReassignmentSiteId
                val reassignSupervisorEmail = _pendingReassignmentSupervisorEmail
                if (!reassignSiteId.isNullOrEmpty() && !reassignSupervisorEmail.isNullOrEmpty()) {
                    repository.clearSupervisorFromSite(reassignSiteId, reassignSupervisorEmail)
                    _pendingReassignmentSiteId = null
                    _pendingReassignmentSupervisorEmail = null
                }

                // Sync assignedSite to the new supervisor and all their employees
                if (newSupervisorId.isNotEmpty() && savedSiteId.isNotEmpty()) {
                    repository.syncUsersWithSite(savedSiteId, newSupervisorId)
                }
                _success.value = true
            }

            _isLoading.value = false
        }
    }

    fun deleteSite(siteId: String) {
        viewModelScope.launch {
            when (val result = repository.deleteSite(siteId)) {
                is com.harsh.worksphere.core.utils.Result.Success -> fetchSites()
                is com.harsh.worksphere.core.utils.Result.Error -> _error.value = result.message
                is com.harsh.worksphere.core.utils.Result.Loading -> { _isLoading.value = true
                }
            }
        }
    }

    /**
     * Auto-transition status based on supervisor assignment:
     * - If supervisor is assigned and status is still PENDING_ASSIGNMENT → ASSIGNED
     * - If supervisor is removed and status is ASSIGNED → PENDING_ASSIGNMENT
     * - Otherwise keep the user-selected status
     */
    private fun resolveStatus(): SiteStatus {
        val currentStatus = _siteStatus.value ?: SiteStatus.PENDING_ASSIGNMENT
        val hasSupervisor = !_supervisor.value?.id.isNullOrEmpty()

        return when {
            hasSupervisor && currentStatus == SiteStatus.PENDING_ASSIGNMENT -> SiteStatus.ASSIGNED
            !hasSupervisor && currentStatus == SiteStatus.ASSIGNED -> SiteStatus.PENDING_ASSIGNMENT
            else -> currentStatus
        }
    }

    private fun validateInput(): String? {
        if (_siteName.value.isNullOrBlank()) return "Site name is required"
        if (_clientName.value.isNullOrBlank()) return "Client name is required"
        if (_location.value == null) return "Please select a location on the map"
        return null
    }

    fun clear() {
        _siteName.value = ""
        _clientName.value = ""
        _supervisor.value = null
        _supervisorPhone.value = ""
        _location.value = null
        _workDetails.value = ""
        _isActive.value = true
        _siteImageUrl.value = ""
        _siteStatus.value = SiteStatus.PENDING_ASSIGNMENT
        _visitTimeFrom.value = ""
        _visitTimeTo.value = ""
        _pendingReassignmentSiteId = null
        _pendingReassignmentSupervisorEmail = null
        _error.value = null
        _success.value = false
    }
}
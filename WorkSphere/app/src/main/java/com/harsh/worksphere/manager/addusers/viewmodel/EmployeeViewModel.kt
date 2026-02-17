package com.harsh.worksphere.manager.addusers.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.worksphere.core.utils.Result
import com.harsh.worksphere.initial.auth.data.model.User
import com.harsh.worksphere.initial.auth.data.model.UserRole
import com.harsh.worksphere.initial.auth.data.remote.FirestoreDataSource
import kotlinx.coroutines.launch

class EmployeeViewModel : ViewModel() {

    private val firestoreDataSource = FirestoreDataSource()

    private val _employees = MutableLiveData<List<User>>()
    val employees: LiveData<List<User>> = _employees

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Fetch employees (role = Employee)
    fun fetchEmployees() {
        fetchUsersByRole(UserRole.Employee)
    }

    // Fetch supervisors (role = Supervisor)
    fun fetchSupervisors() {
        fetchUsersByRole(UserRole.Supervisor)
    }

    // Generic method to fetch by role
    private fun fetchUsersByRole(role: UserRole) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = firestoreDataSource.getUsersByRole(role)) {
                is Result.Success -> {
                    _employees.value = result.data
                }
                is Result.Error -> {
                    _error.value = result.message
                }
                is Result.Loading -> {}
            }
            _isLoading.value = false
        }
    }
}
package com.harsh.worksphere.manager.addusers.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.worksphere.core.utils.Result
import com.harsh.worksphere.initial.auth.data.model.User
import com.harsh.worksphere.initial.auth.data.model.UserRole
import com.harsh.worksphere.initial.auth.data.model.UserStatus
import com.harsh.worksphere.initial.auth.data.remote.FirestoreDataSource
import kotlinx.coroutines.launch

class AddUserViewModel : ViewModel() {

    private val firestoreDataSource = FirestoreDataSource()

    private val _createUserResult = MutableLiveData<Result<Unit>>()
    val createUserResult: LiveData<Result<Unit>> = _createUserResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun createUser(
        name: String,
        email: String,
        phone: String,
        role: UserRole,
        selectedSite: String? = null,
        assignedEmployees: List<String> = emptyList(),
        mySupervisor: String? = null,
        supervisorSiteId: String? = null,
        reassignedEmployees: List<Pair<String, String>> = emptyList()
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val user = User(
                userId = "",
                email = email,
                name = name,
                profilePic = null,
                role = role,
                phone = phone,
                assignedEmployees = if (role == UserRole.Supervisor) assignedEmployees else emptyList(),
                mySupervisor = mySupervisor,
                assignedSite = when (role) {
                    UserRole.Supervisor -> selectedSite
                    UserRole.Employee -> supervisorSiteId
                    else -> null
                },
                status = UserStatus.OFFLINE
            )

            // Step 1: Create the new user (supervisor or employee)
            val createResult = firestoreDataSource.createUser(user)

            if (createResult is Result.Success) {
                // Step 2: Handle all related operations
                val operationsResult = handleRelatedOperations(
                    role = role,
                    newUserEmail = email,
                    selectedSite = selectedSite,
                    assignedEmployees = assignedEmployees,
                    mySupervisor = mySupervisor,
                    reassignedEmployees = reassignedEmployees
                )

                _createUserResult.value = operationsResult
            } else {
                _createUserResult.value = createResult
            }

            _isLoading.value = false
        }
    }

    private suspend fun handleRelatedOperations(
        role: UserRole,
        newUserEmail: String,
        selectedSite: String?,
        assignedEmployees: List<String>,
        mySupervisor: String?,
        reassignedEmployees: List<Pair<String, String>>
    ): Result<Unit> {
        return try {
            when (role) {
                UserRole.Supervisor -> {
                    handleSupervisorOperations(
                        supervisorEmail = newUserEmail,
                        selectedSite = selectedSite,
                        assignedEmployees = assignedEmployees,
                        reassignedEmployees = reassignedEmployees
                    )
                }
                UserRole.Employee -> {
                    handleEmployeeOperations(
                        employeeEmail = newUserEmail,
                        supervisorEmail = mySupervisor
                    )
                }
                else -> Result.Success(Unit)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to handle related operations")
        }
    }

    private suspend fun handleSupervisorOperations(
        supervisorEmail: String,
        selectedSite: String?,
        assignedEmployees: List<String>,
        reassignedEmployees: List<Pair<String, String>>
    ): Result<Unit> {
        return try {
            // Separate regular assignments from reassignments
            val reassignedEmployeeEmails = reassignedEmployees.map { it.first }.toSet()
            val regularAssignments = assignedEmployees.filter { !reassignedEmployeeEmails.contains(it) }

            // Step 1: Add regular employees to new supervisor (non-reassigned) - ONLY if there are any
            regularAssignments.forEach { employeeEmail ->
                val result = firestoreDataSource.addEmployeeToSupervisor(supervisorEmail, employeeEmail)
                if (result is Result.Error) {
                    throw Exception("Failed to add employee $employeeEmail: ${result.message}")
                }
            }

            // Step 2: Handle reassignments - ONLY if there are any
            reassignedEmployees.forEach { (employeeEmail, oldSupervisorEmail) ->
                // 2a. Remove employee from old supervisor's assignedEmployees list
                val removeResult = firestoreDataSource.removeEmployeeFromSupervisor(oldSupervisorEmail, employeeEmail)
                if (removeResult is Result.Error) {
                    throw Exception("Failed to remove $employeeEmail from old supervisor: ${removeResult.message}")
                }

                // 2b. Add employee to new supervisor's assignedEmployees list
                val addResult = firestoreDataSource.addEmployeeToSupervisor(supervisorEmail, employeeEmail)
                if (addResult is Result.Error) {
                    throw Exception("Failed to add $employeeEmail to new supervisor: ${addResult.message}")
                }

                // 2c. Update employee's mySupervisor and assignedSite
                val updateResult = firestoreDataSource.updateEmployeeSupervisorAndSite(
                    employeeEmail = employeeEmail,
                    newSupervisorEmail = supervisorEmail,
                    newSiteId = selectedSite ?: ""
                )
                if (updateResult is Result.Error) {
                    throw Exception("Failed to update $employeeEmail supervisor: ${updateResult.message}")
                }
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Supervisor operations failed")
        }
    }

    private suspend fun handleEmployeeOperations(
        employeeEmail: String,
        supervisorEmail: String?
    ): Result<Unit> {
        return try {
            // Only add to supervisor's list if supervisor is provided
            supervisorEmail?.let { supervisor ->
                val result = firestoreDataSource.addEmployeeToSupervisor(supervisor, employeeEmail)
                if (result is Result.Error) {
                    throw Exception("Failed to add employee to supervisor: ${result.message}")
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Employee operations failed")
        }
    }

    suspend fun getSupervisorSiteId(supervisorEmail: String): String? {
        return when (val result = firestoreDataSource.getUser(supervisorEmail)) {
            is Result.Success -> result.data?.assignedSite
            else -> null
        }
    }

    suspend fun getEmployeeDetails(employeeEmail: String): User? {
        return when (val result = firestoreDataSource.getUser(employeeEmail)) {
            is Result.Success -> result.data
            else -> null
        }
    }
}
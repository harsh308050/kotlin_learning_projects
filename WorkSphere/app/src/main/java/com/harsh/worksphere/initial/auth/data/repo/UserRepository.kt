package com.harsh.worksphere.initial.auth.data.repo

import com.harsh.worksphere.core.utils.Result
import com.harsh.worksphere.initial.auth.data.model.User
import com.harsh.worksphere.initial.auth.data.model.UserRole
import com.harsh.worksphere.initial.auth.data.remote.FirestoreDataSource

class UserRepository(private val firestoreDataSource: FirestoreDataSource) {

    suspend fun getUser(email: String): Result<User?> {
        return firestoreDataSource.getUser(email)
    }

    suspend fun createUser(user: User): Result<Unit> {
        return firestoreDataSource.createUser(user)
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return firestoreDataSource.updateUser(user)
    }
    // Determine role for new user
    suspend fun determineUserRole(): Result<UserRole> {
        return when (val result = firestoreDataSource.isFirstUser()) {
            is Result.Success -> {
                val role = if (result.data) UserRole.Manager else UserRole.Supervisor
                Result.Success(role)
            }
            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }

    suspend fun assignSupervisor(employeeEmail: String, supervisorEmail: String): Result<Unit> {
        return firestoreDataSource.assignSupervisor(employeeEmail, supervisorEmail)
    }
    suspend fun addEmployeeToSupervisor(supervisorEmail: String, employeeEmail: String): Result<Unit> {
        return firestoreDataSource.addEmployeeToSupervisor(supervisorEmail, employeeEmail)
    }

    suspend fun getAllUsers(): Result<List<User>> {
        return firestoreDataSource.getAllUsers()
    }
}
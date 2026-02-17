package com.harsh.worksphere.initial.auth.data.model

import com.harsh.worksphere.manager.sites.data.model.SupervisorInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class User(
    val userId: String,
    val email: String,
    val name: String,
    val profilePic: String?,
    val role: UserRole,
    val phone: String = "",
    val assignedEmployees: List<String> = emptyList(), // For Supervisor: list of employee emails
    val mySupervisor: String? = null, // For Employee: supervisor's email
    val assignedSite: String? = null, // Site ID for Supervisor
    val createdAt: String = getCurrentDateTime()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "email" to email,
        "name" to name,
        "profilePic" to profilePic,
        "role" to role.value,
        "phone" to phone,
        "assignedEmployees" to assignedEmployees,
        "mySupervisor" to mySupervisor,
        "assignedSite" to assignedSite,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): User = User(
            userId = map["userId"] as? String ?: "",
            email = map["email"] as? String ?: "",
            name = map["name"] as? String ?: "",
            profilePic = map["profilePic"] as? String,
            role = UserRole.fromString(map["role"] as? String ?: "employee"),
            phone = map["phone"] as? String ?: "",
            assignedEmployees = (map["assignedEmployees"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            mySupervisor = map["mySupervisor"] as? String,
            assignedSite = map["assignedSite"] as? String,
            createdAt = map["createdAt"] as? String ?: getCurrentDateTime()
        )

        private fun getCurrentDateTime(): String {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            return dateFormat.format(Date())
        }
    }
}

data class AuthResult(
    val user: User?,
    val isNewUser: Boolean,
    val role: UserRole
)

sealed class UserRole(val value: String) {
    data object Manager : UserRole("manager")
    data object Supervisor : UserRole("supervisor")
    data object Employee : UserRole("employee")

    companion object {
        fun fromString(role: String): UserRole = when(role.lowercase()) {
            "manager" -> Manager
            "supervisor" -> Supervisor
            else -> Employee
        }
    }
}
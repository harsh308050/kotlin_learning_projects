package com.harsh.worksphere.initial.auth.data.remote

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.harsh.worksphere.core.firebase.FirebaseModule
import com.harsh.worksphere.core.utils.Result
import com.harsh.worksphere.initial.auth.data.model.User
import com.harsh.worksphere.initial.auth.data.model.UserRole
import kotlinx.coroutines.tasks.await

class FirestoreDataSource {

    private val firestore: FirebaseFirestore = FirebaseModule.firestore
    private val usersCollection = firestore.collection("users")

    suspend fun getUser(email: String): Result<User?> {
        return try {
            val document = usersCollection.document(email).get().await()
            if (document.exists()) {
                val user = User.fromMap(document.data ?: emptyMap())
                Result.Success(user)
            } else {
                Result.Success(null)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch user")
        }
    }

    suspend fun createUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.email).set(user.toMap()).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create user")
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.email).set(user.toMap()).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update user")
        }
    }

    suspend fun getUsersByRole(role: UserRole): Result<List<User>> {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("role", role.value)
                .get()
                .await()
            val users = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { User.fromMap(it) }
            }
            Result.Success(users)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch users")
        }
    }

    suspend fun isFirstUser(): Result<Boolean> {
        return try {
            val snapshot = usersCollection.limit(1).get().await()
            Result.Success(snapshot.isEmpty)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to check users")
        }
    }

    suspend fun updateUserRole(email: String, role: String): Result<Unit> {
        return try {
            usersCollection.document(email).update("role", role).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update role")
        }
    }

    suspend fun assignSupervisor(employeeEmail: String, supervisorEmail: String): Result<Unit> {
        return try {
            usersCollection.document(employeeEmail).update("mySupervisor", supervisorEmail).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to assign supervisor")
        }
    }

    suspend fun addEmployeeToSupervisor(supervisorEmail: String, employeeEmail: String): Result<Unit> {
        return try {
            usersCollection.document(supervisorEmail)
                .update("assignedEmployees", FieldValue.arrayUnion(employeeEmail))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to add employee to supervisor")
        }
    }

    suspend fun removeEmployeeFromSupervisor(supervisorEmail: String, employeeEmail: String): Result<Unit> {
        return try {
            usersCollection.document(supervisorEmail)
                .update("assignedEmployees", FieldValue.arrayRemove(employeeEmail))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to remove employee from supervisor")
        }
    }

    suspend fun updateEmployeeSupervisorAndSite(
        employeeEmail: String,
        newSupervisorEmail: String,
        newSiteId: String
    ): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "mySupervisor" to newSupervisorEmail,
                "assignedSite" to newSiteId
            )
            usersCollection.document(employeeEmail).update(updates).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update employee supervisor and site")
        }
    }

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = usersCollection.get().await()
            val users = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { User.fromMap(it) }
            }
            Result.Success(users)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch users")
        }
    }

    suspend fun updateAssignedSite(email: String, siteId: String): Result<Unit> {
        return try {
            usersCollection.document(email).update("assignedSite", siteId).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update assigned site")
        }
    }

    suspend fun getAssignedEmployees(supervisorEmail: String): Result<List<String>> {
        return try {
            val doc = usersCollection.document(supervisorEmail).get().await()
            val employees = (doc.data?.get("assignedEmployees") as? List<*>)
                ?.mapNotNull { it as? String } ?: emptyList()
            Result.Success(employees)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch assigned employees")
        }
    }

    suspend fun clearAssignedSite(email: String): Result<Unit> {
        return try {
            usersCollection.document(email).update("assignedSite", "").await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to clear assigned site")
        }
    }

    suspend fun updateUserProfile(email: String, name: String, phone: String): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "name" to name,
                "phone" to phone
            )
            usersCollection.document(email).update(updates).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update profile")
        }
    }

    suspend fun updateProfilePic(email: String, profilePicPath: String): Result<Unit> {
        return try {
            usersCollection.document(email).update("profilePic", profilePicPath).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update profile picture")
        }
    }

    suspend fun updateUserStatus(email: String, status: String): Result<Unit> {
        return try {
            usersCollection.document(email).update("status", status).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update status")
        }
    }

    /**
     * Save a visit log record to visitLogs collection.
     * Creates a new document with ID: record_{timestampMillis}
     * Used for status changes (break/offline) — auto-saved without dialog.
     */
    suspend fun saveVisitLogRecord(
        userId: String,
        supervisorId: String?,
        siteId: String?,
        siteName: String?,
        siteAddress: String?,
        siteLatitude: Double?,
        siteLongitude: Double?,
        visitNotes: String,
        evidenceImages: List<String>,
        status: String,
        timestamp: String,
        timestampMillis: Long,
        date: String
    ): Result<Unit> {
        return try {
            val recordId = "record_$timestampMillis"
            val recordData = hashMapOf<String, Any?>(
                "siteId" to siteId,
                "siteName" to siteName,
                "siteAddress" to siteAddress,
                "siteLocation" to if (siteLatitude != null && siteLongitude != null) {
                    mapOf(
                        "latitude" to siteLatitude,
                        "longitude" to siteLongitude
                    )
                } else null,
                "visitNotes" to visitNotes,
                "evidenceImages" to evidenceImages,
                "timestamp" to timestamp,
                "timestampMillis" to timestampMillis,
                "status" to status,
                "userId" to userId,
                "supervisorId" to supervisorId
            )

            firestore.collection("visitLogs")
                .document(userId)
                .collection(date)
                .document(recordId)
                .set(recordData)
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save visit log record")
        }
    }
}
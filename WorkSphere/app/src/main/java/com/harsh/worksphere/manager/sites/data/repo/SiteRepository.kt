// com.harsh.worksphere.manager.sites.repository/SiteRepository.kt
package com.harsh.worksphere.manager.sites.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.harsh.worksphere.core.utils.Result
import com.harsh.worksphere.initial.auth.data.remote.FirestoreDataSource
import com.harsh.worksphere.manager.sites.data.model.SiteModel
import kotlinx.coroutines.tasks.await

class SiteRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val sitesCollection = firestore.collection("sites")
    private val firestoreDataSource = FirestoreDataSource()

    suspend fun createSite(site: SiteModel): Result<String> {
        return try {
            val docRef = sitesCollection.document()
            docRef.set(site.toMap()).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create site")
        }
    }

    suspend fun getAllSites(): Result<List<SiteModel>> {
        return try {
            val snapshot = sitesCollection.get().await()
            val sites = snapshot.documents.map { doc ->
                SiteModel.fromMap(doc.id, doc.data ?: emptyMap())
            }
            Result.Success(sites)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch sites")
        }
    }

    suspend fun updateSite(site: SiteModel): Result<Unit> {
        return try {
            if (site.siteId.isEmpty()) {
                return Result.Error("Site ID is empty")
            }
            sitesCollection.document(site.siteId).update(site.toMap()).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update site")
        }
    }

    suspend fun deleteSite(siteId: String): Result<Unit> {
        return try {
            sitesCollection.document(siteId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete site")
        }
    }

    /**
     * After a site is created or updated, sync the assigned site ID to:
     * 1. The supervisor's user document (assignedSite)
     * 2. All employees under that supervisor (assignedSite)
     */
    suspend fun syncUsersWithSite(siteId: String, supervisorEmail: String) {
        if (supervisorEmail.isEmpty()) return

        // Update supervisor's assignedSite
        firestoreDataSource.updateAssignedSite(supervisorEmail, siteId)

        // Get all employees assigned to this supervisor and update their assignedSite
        val employeesResult = firestoreDataSource.getAssignedEmployees(supervisorEmail)
        if (employeesResult is Result.Success) {
            employeesResult.data.forEach { employeeEmail ->
                firestoreDataSource.updateAssignedSite(employeeEmail, siteId)
            }
        }
    }
}
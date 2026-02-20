package com.harsh.worksphere.manager.sites.data.model
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class SiteStatus(val displayName: String) {
    PENDING_ASSIGNMENT("Pending Assignment"),
    ASSIGNED("Assigned"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    ON_HOLD("On Hold"),
    CANCELLED("Cancelled");

    companion object {
        fun fromString(value: String): SiteStatus {
            return entries.find { it.name == value } ?: PENDING_ASSIGNMENT
        }

        fun displayNames(): List<String> = entries.map { it.displayName }
    }
}

@Parcelize
data class SiteModel(
    val siteId: String = "",
    val siteName: String = "",
    val clientName: String = "",
    val supervisorId: String = "",
    val supervisorName: String = "",
    val supervisorPhone: String = "",
    val supervisorImageUrl: String = "",
    val location: SiteLocation = SiteLocation(),
    val workDetails: String = "",
    val createdBy: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val siteImageUrl: String = "",
    val status: SiteStatus = SiteStatus.PENDING_ASSIGNMENT,
    val visitTimeFrom: String = "",
    val visitTimeTo: String = ""
): Parcelable {
    fun toMap(): Map<String, Any?> = mapOf(
        "siteId" to siteId,
        "siteName" to siteName,
        "clientName" to clientName,
        "supervisorId" to supervisorId,
        "supervisorName" to supervisorName,
        "supervisorPhone" to supervisorPhone,
        "supervisorImageUrl" to supervisorImageUrl,
        "location" to location.toMap(),
        "workDetails" to workDetails,
        "createdBy" to createdBy,
        "isActive" to isActive,
        "createdAt" to createdAt,
        "siteImageUrl" to siteImageUrl,
        "status" to status.name,
        "visitTimeFrom" to visitTimeFrom,
        "visitTimeTo" to visitTimeTo
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): SiteModel {
            return SiteModel(
                siteId = id,
                siteName = map["siteName"] as? String ?: "",
                clientName = map["clientName"] as? String ?: "",
                supervisorId = map["supervisorId"] as? String ?: "",
                supervisorName = map["supervisorName"] as? String ?: "",
                supervisorPhone = map["supervisorPhone"] as? String ?: "",
                supervisorImageUrl = map["supervisorImageUrl"] as? String ?: "",
                location = SiteLocation.fromMap(map["location"] as? Map<String, Any?> ?: emptyMap()),
                workDetails = map["workDetails"] as? String ?: "",
                createdBy = map["createdBy"] as? String ?: "",
                isActive = map["isActive"] as? Boolean ?: true,
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
                siteImageUrl = map["siteImageUrl"] as? String ?: "",
                status = SiteStatus.fromString(map["status"] as? String ?: SiteStatus.PENDING_ASSIGNMENT.name),
                visitTimeFrom = map["visitTimeFrom"] as? String ?: "",
                visitTimeTo = map["visitTimeTo"] as? String ?: ""
            )
        }
    }
}
@Parcelize
data class SiteLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
): Parcelable {
    fun toMap(): Map<String, Any?> = mapOf(
        "latitude" to latitude,
        "longitude" to longitude,
        "address" to address
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): SiteLocation {
            return SiteLocation(
                latitude = map["latitude"] as? Double ?: 0.0,
                longitude = map["longitude"] as? Double ?: 0.0,
                address = map["address"] as? String ?: ""
            )
        }
    }
}
data class SupervisorInfo(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val imageUrl: String = "",
    val address: String = "",
    val description: String = ""
)


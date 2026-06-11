package com.example.propmanager.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

enum class UserRole {
    LANDLORD,
    RESIDENT,
    ADMIN
}

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val phone: String = "",
    @SerialName("subscription_tier") val subscriptionTier: String = "Free", // Free, Pro, Enterprise (for landlords)
    @SerialName("landlord_id") val landlordId: String = ""            // for residents: the uid of their landlord
)
// NOTE: password field intentionally removed — authentication is handled
// exclusively by Supabase Auth, never stored in our database.

@Serializable
data class Property(
    val id: String,
    @SerialName("landlord_id") val landlordId: String,
    val name: String,
    val address: String,
    @SerialName("rent_amount") val rentAmount: Double,
    val description: String = "",
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("total_units") val totalUnits: Int = 1,
    @SerialName("occupied_units") val occupiedUnits: Int = 0
)

@Serializable
data class Resident(
    val id: String,
    @SerialName("landlord_id") val landlordId: String,
    @SerialName("property_id") val propertyId: String,
    @SerialName("unit_number") val unitNumber: String = "",
    val name: String,
    val email: String,
    val phone: String = "",
    @SerialName("lease_start") val leaseStart: String = "",
    @SerialName("lease_end") val leaseEnd: String = "",
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class Payment(
    val id: String,
    @SerialName("landlord_id") val landlordId: String,
    @SerialName("resident_id") val residentId: String,
    @SerialName("property_id") val propertyId: String,
    val amount: Double,
    @SerialName("invoice_number") val invoiceNumber: String,
    @SerialName("due_date") val dueDate: String,
    @SerialName("paid_date") val paidDate: String? = null,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val notes: String = ""
)

enum class PaymentStatus {
    PENDING,
    PAID,
    OVERDUE
}

@Serializable
data class MaintenanceRequest(
    val id: String,
    @SerialName("landlord_id") val landlordId: String,
    @SerialName("resident_id") val residentId: String,
    @SerialName("property_id") val propertyId: String,
    val title: String,
    val description: String,
    val priority: RequestPriority = RequestPriority.MEDIUM,
    val status: RequestStatus = RequestStatus.PENDING,
    @SerialName("date_submitted") val dateSubmitted: String,
    @SerialName("image_url") val imageUrl: String = ""
)

enum class RequestPriority {
    LOW,
    MEDIUM,
    HIGH
}

enum class RequestStatus {
    PENDING,
    IN_PROGRESS,
    RESOLVED
}

@Serializable
data class Document(
    val id: String,
    @SerialName("landlord_id") val landlordId: String,
    @SerialName("resident_id") val residentId: String = "", // empty if only landlord sees
    val title: String,
    @SerialName("file_type") val fileType: String, // e.g. "PDF", "Lease", "Agreement", "Receipt"
    @SerialName("file_url") val fileUrl: String = "",
    @SerialName("date_uploaded") val dateUploaded: String
)


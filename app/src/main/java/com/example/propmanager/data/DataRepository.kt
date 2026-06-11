package com.example.propmanager.data

import com.example.propmanager.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Main data contract for PropManager SaaS.
 * All write operations are suspend functions (async Supabase calls).
 * All read-many operations return Flow for real-time Supabase db streams.
 */
interface DataRepository {

    // Current session — reactive; updated by Supabase Auth state listener
    val currentUser: MutableStateFlow<User?>

    // ── Authentication ────────────────────────────────────────────────────────
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(
        email: String,
        name: String,
        role: UserRole,
        phone: String = "",
        password: String
    ): Result<User>
    fun logout()

    // ── Admin Panel ───────────────────────────────────────────────────────────
    fun getLandlords(): Flow<List<User>>
    suspend fun updateLandlordSubscription(landlordId: String, tier: String)

    // ── Properties ────────────────────────────────────────────────────────────
    fun getProperties(landlordId: String): Flow<List<Property>>
    suspend fun addProperty(
        landlordId: String,
        name: String,
        address: String,
        rentAmount: Double,
        description: String
    ): Property
    suspend fun deleteProperty(propertyId: String)

    // ── Residents ─────────────────────────────────────────────────────────────
    fun getResidents(landlordId: String): Flow<List<Resident>>
    fun getResidentProfile(residentId: String): Flow<Resident?>
    suspend fun getResidentsForProperty(propertyId: String): List<Resident>
    suspend fun addResident(
        landlordId: String,
        propertyId: String,
        name: String,
        email: String,
        phone: String,
        unitNumber: String,
        leaseStart: String,
        leaseEnd: String
    ): Resident
    suspend fun deleteResident(residentId: String)
    suspend fun getResidentById(residentId: String): Resident?

    // ── Payments ──────────────────────────────────────────────────────────────
    fun getPaymentsForLandlord(landlordId: String): Flow<List<Payment>>
    fun getPaymentsForResident(residentId: String): Flow<List<Payment>>
    suspend fun generateInvoice(
        landlordId: String,
        residentId: String,
        propertyId: String,
        amount: Double,
        dueDate: String,
        notes: String
    ): Payment
    suspend fun payInvoice(paymentId: String): Boolean

    // ── Maintenance Requests ──────────────────────────────────────────────────
    fun getRequestsForLandlord(landlordId: String): Flow<List<MaintenanceRequest>>
    fun getRequestsForResident(residentId: String): Flow<List<MaintenanceRequest>>
    suspend fun addRequest(
        residentId: String,
        title: String,
        description: String,
        priority: RequestPriority
    ): MaintenanceRequest
    suspend fun updateRequestStatus(requestId: String, status: RequestStatus): Boolean

    // ── Documents ─────────────────────────────────────────────────────────────
    fun getDocumentsForLandlord(landlordId: String): Flow<List<Document>>
    fun getDocumentsForResident(residentId: String): Flow<List<Document>>
    suspend fun addDocument(
        landlordId: String,
        residentId: String,
        title: String,
        fileType: String
    ): Document
}

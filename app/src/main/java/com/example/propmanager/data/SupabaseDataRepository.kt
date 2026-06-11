package com.example.propmanager.data

import com.example.propmanager.data.model.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.jan.supabase.annotations.SupabaseExperimental
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(SupabaseExperimental::class)
@Singleton
class SupabaseDataRepository @Inject constructor(
    private val supabase: SupabaseClient
) : DataRepository {

    override val currentUser = MutableStateFlow<User?>(null)
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Listen to Supabase Auth session changes
        repositoryScope.launch {
            supabase.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val uid = status.session.user?.id
                        if (uid != null) {
                            try {
                                val user = supabase.from("users")
                                    .select {
                                        filter {
                                            eq("id", uid)
                                        }
                                    }
                                    .decodeSingleOrNull<User>()
                                currentUser.value = user
                            } catch (e: Exception) {
                                e.printStackTrace()
                                currentUser.value = null
                            }
                        } else {
                            currentUser.value = null
                        }
                    }
                    else -> {
                        currentUser.value = null
                    }
                }
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val uid = supabase.auth.currentSessionOrNull()?.user?.id ?: throw Exception("Login failed: no UID")
            val user = supabase.from("users")
                .select {
                    filter {
                        eq("id", uid)
                    }
                }
                .decodeSingleOrNull<User>() ?: throw Exception("User profile not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        name: String,
        role: UserRole,
        phone: String,
        password: String
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("name", name)
                    put("role", role.name)
                    put("phone", phone)
                }
            }
            val uid = response?.id
                ?: supabase.auth.currentSessionOrNull()?.user?.id
                ?: throw Exception("Registration failed: no UID")
            
            // For landlords, they are their own landlord.
            val landlordId = if (role == UserRole.LANDLORD) uid else ""
            
            val user = User(
                id = uid,
                name = name,
                email = email,
                role = role,
                phone = phone,
                landlordId = landlordId
            )
            
            // Insert is handled by public.handle_new_user() trigger in PostgreSQL
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        repositoryScope.launch {
            supabase.auth.signOut()
        }
    }

    // ── Admin Panel ───────────────────────────────────────────────────────────

    override fun getLandlords(): Flow<List<User>> {
        return supabase.from("users")
            .selectAsFlow(User::id)
            .map { list -> list.filter { it.role == UserRole.LANDLORD } }
    }

    override suspend fun updateLandlordSubscription(landlordId: String, tier: String) = withContext(Dispatchers.IO) {
        supabase.from("users")
            .update({
                set("subscription_tier", tier)
            }) {
                filter {
                    eq("id", landlordId)
                }
            }
        Unit
    }

    // ── Properties ────────────────────────────────────────────────────────────

    override fun getProperties(landlordId: String): Flow<List<Property>> {
        return supabase.from("properties")
            .selectAsFlow(Property::id)
            .map { list -> list.filter { it.landlordId == landlordId } }
    }

    override suspend fun addProperty(
        landlordId: String,
        name: String,
        address: String,
        rentAmount: Double,
        description: String
    ): Property = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val property = Property(
            id = id,
            landlordId = landlordId,
            name = name,
            address = address,
            rentAmount = rentAmount,
            description = description
        )
        supabase.from("properties").insert(property)
        property
    }

    override suspend fun deleteProperty(propertyId: String) = withContext(Dispatchers.IO) {
        supabase.from("properties")
            .delete {
                filter {
                    eq("id", propertyId)
                }
            }
        Unit
    }

    // ── Residents ─────────────────────────────────────────────────────────────

    override fun getResidents(landlordId: String): Flow<List<Resident>> {
        return supabase.from("residents")
            .selectAsFlow(Resident::id)
            .map { list -> list.filter { it.landlordId == landlordId } }
    }

    override fun getResidentProfile(residentId: String): Flow<Resident?> {
        return supabase.from("residents")
            .selectAsFlow(Resident::id)
            .map { list -> list.find { it.id == residentId } }
    }

    override suspend fun getResidentsForProperty(propertyId: String): List<Resident> = withContext(Dispatchers.IO) {
        try {
            supabase.from("residents")
                .select {
                    filter {
                        eq("property_id", propertyId)
                    }
                }
                .decodeList<Resident>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addResident(
        landlordId: String,
        propertyId: String,
        name: String,
        email: String,
        phone: String,
        unitNumber: String,
        leaseStart: String,
        leaseEnd: String
    ): Resident = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val resident = Resident(
            id = id,
            landlordId = landlordId,
            propertyId = propertyId,
            unitNumber = unitNumber,
            name = name,
            email = email,
            phone = phone,
            leaseStart = leaseStart,
            leaseEnd = leaseEnd
        )
        supabase.from("residents").insert(resident)
        
        // Also update property occupied units count
        try {
            val prop = supabase.from("properties")
                .select {
                    filter {
                        eq("id", propertyId)
                    }
                }
                .decodeSingleOrNull<Property>()
            if (prop != null) {
                supabase.from("properties")
                    .update({
                        set("occupied_units", prop.occupiedUnits + 1)
                    }) {
                        filter {
                            eq("id", propertyId)
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        resident
    }

    override suspend fun deleteResident(residentId: String) = withContext(Dispatchers.IO) {
        try {
            val resident = supabase.from("residents")
                .select {
                    filter {
                        eq("id", residentId)
                    }
                }
                .decodeSingleOrNull<Resident>()
            if (resident != null) {
                val prop = supabase.from("properties")
                    .select {
                        filter {
                            eq("id", resident.propertyId)
                        }
                    }
                    .decodeSingleOrNull<Property>()
                if (prop != null && prop.occupiedUnits > 0) {
                    supabase.from("properties")
                        .update({
                            set("occupied_units", prop.occupiedUnits - 1)
                        }) {
                            filter {
                                eq("id", resident.propertyId)
                            }
                        }
                }
                supabase.from("residents")
                    .delete {
                        filter {
                            eq("id", residentId)
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getResidentById(residentId: String): Resident? = withContext(Dispatchers.IO) {
        try {
            supabase.from("residents")
                .select {
                    filter {
                        eq("id", residentId)
                    }
                }
                .decodeSingleOrNull<Resident>()
        } catch (e: Exception) {
            null
        }
    }

    // ── Payments ──────────────────────────────────────────────────────────────

    override fun getPaymentsForLandlord(landlordId: String): Flow<List<Payment>> {
        return supabase.from("payments")
            .selectAsFlow(Payment::id)
            .map { list -> list.filter { it.landlordId == landlordId }.sortedByDescending { it.dueDate } }
    }

    override fun getPaymentsForResident(residentId: String): Flow<List<Payment>> {
        return supabase.from("payments")
            .selectAsFlow(Payment::id)
            .map { list -> list.filter { it.residentId == residentId }.sortedByDescending { it.dueDate } }
    }

    override suspend fun generateInvoice(
        landlordId: String,
        residentId: String,
        propertyId: String,
        amount: Double,
        dueDate: String,
        notes: String
    ): Payment = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val payment = Payment(
            id = id,
            landlordId = landlordId,
            residentId = residentId,
            propertyId = propertyId,
            amount = amount,
            invoiceNumber = "INV-${UUID.randomUUID().toString().take(6).uppercase()}",
            dueDate = dueDate,
            notes = notes
        )
        supabase.from("payments").insert(payment)
        payment
    }

    override suspend fun payInvoice(paymentId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val date = java.time.LocalDate.now().toString()
            supabase.from("payments")
                .update({
                    set("status", PaymentStatus.PAID.name)
                    set("paid_date", date)
                }) {
                    filter {
                        eq("id", paymentId)
                    }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ── Maintenance Requests ──────────────────────────────────────────────────

    override fun getRequestsForLandlord(landlordId: String): Flow<List<MaintenanceRequest>> {
        return supabase.from("maintenance_requests")
            .selectAsFlow(MaintenanceRequest::id)
            .map { list -> list.filter { it.landlordId == landlordId }.sortedByDescending { it.dateSubmitted } }
    }

    override fun getRequestsForResident(residentId: String): Flow<List<MaintenanceRequest>> {
        return supabase.from("maintenance_requests")
            .selectAsFlow(MaintenanceRequest::id)
            .map { list -> list.filter { it.residentId == residentId }.sortedByDescending { it.dateSubmitted } }
    }

    override suspend fun addRequest(
        residentId: String,
        title: String,
        description: String,
        priority: RequestPriority
    ): MaintenanceRequest = withContext(Dispatchers.IO) {
        val user = currentUser.value ?: throw Exception("Not logged in")
        val landlordId = user.landlordId
        if (landlordId.isEmpty()) throw Exception("No landlord assigned")
        
        val resData = getResidentById(residentId) ?: throw Exception("Resident profile not found")
        
        val id = UUID.randomUUID().toString()
        val req = MaintenanceRequest(
            id = id,
            landlordId = landlordId,
            residentId = residentId,
            propertyId = resData.propertyId,
            title = title,
            description = description,
            priority = priority,
            dateSubmitted = java.time.LocalDate.now().toString()
        )
        supabase.from("maintenance_requests").insert(req)
        req
    }

    override suspend fun updateRequestStatus(requestId: String, status: RequestStatus): Boolean = withContext(Dispatchers.IO) {
        try {
            supabase.from("maintenance_requests")
                .update({
                    set("status", status.name)
                }) {
                    filter {
                        eq("id", requestId)
                    }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ── Documents ─────────────────────────────────────────────────────────────

    override fun getDocumentsForLandlord(landlordId: String): Flow<List<Document>> {
        return supabase.from("documents")
            .selectAsFlow(Document::id)
            .map { list -> list.filter { it.landlordId == landlordId }.sortedByDescending { it.dateUploaded } }
    }

    override fun getDocumentsForResident(residentId: String): Flow<List<Document>> {
        return supabase.from("documents")
            .selectAsFlow(Document::id)
            .map { list -> list.filter { it.residentId == residentId }.sortedByDescending { it.dateUploaded } }
    }

    override suspend fun addDocument(
        landlordId: String,
        residentId: String,
        title: String,
        fileType: String
    ): Document = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val doc = Document(
            id = id,
            landlordId = landlordId,
            residentId = residentId,
            title = title,
            fileType = fileType,
            dateUploaded = java.time.LocalDate.now().toString()
        )
        supabase.from("documents").insert(doc)
        doc
    }
}

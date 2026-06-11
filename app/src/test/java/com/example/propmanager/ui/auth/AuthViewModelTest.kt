package com.example.propmanager.ui.auth

import com.example.propmanager.data.DataRepository
import com.example.propmanager.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeDataRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeDataRepository()
        viewModel = AuthViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_withEmptyCredentials_returnsError() = runTest {
        viewModel.login("", "")
        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Email and password cannot be empty", (state as AuthUiState.Error).message)
    }

    @Test
    fun login_withValidCredentials_returnsSuccess() = runTest(testDispatcher) {
        viewModel.login("test@prop.com", "password123")
        
        // Wait for coroutine inside viewModelScope to execute
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Success)
        assertEquals("Test User", (state as AuthUiState.Success).user.name)
    }

    @Test
    fun register_withEmptyFields_returnsError() = runTest {
        viewModel.register("", "", UserRole.LANDLORD, "", "")
        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Please fill in all required fields", (state as AuthUiState.Error).message)
    }
}

private class FakeDataRepository : DataRepository {
    override val currentUser = MutableStateFlow<User?>(null)

    override suspend fun login(email: String, password: String): Result<User> {
        return if (email == "test@prop.com" && password == "password123") {
            Result.success(User("1", "Test User", email, UserRole.LANDLORD))
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }

    override suspend fun register(
        email: String,
        name: String,
        role: UserRole,
        phone: String,
        password: String
    ): Result<User> {
        return Result.success(User("2", name, email, role, phone))
    }

    override fun logout() {}

    override fun getLandlords(): Flow<List<User>> = emptyFlow()
    override suspend fun updateLandlordSubscription(landlordId: String, tier: String) {}

    override fun getProperties(landlordId: String): Flow<List<Property>> = emptyFlow()
    override suspend fun addProperty(
        landlordId: String,
        name: String,
        address: String,
        rentAmount: Double,
        description: String
    ): Property = Property("", landlordId, name, address, rentAmount, description)
    override suspend fun deleteProperty(propertyId: String) {}

    override fun getResidents(landlordId: String): Flow<List<Resident>> = emptyFlow()
    override fun getResidentProfile(residentId: String): Flow<Resident?> = emptyFlow()
    override suspend fun getResidentsForProperty(propertyId: String): List<Resident> = emptyList()
    override suspend fun addResident(
        landlordId: String,
        propertyId: String,
        name: String,
        email: String,
        phone: String,
        unitNumber: String,
        leaseStart: String,
        leaseEnd: String
    ): Resident = Resident("", landlordId, propertyId, unitNumber, name, email, phone, leaseStart, leaseEnd)
    override suspend fun deleteResident(residentId: String) {}
    override suspend fun getResidentById(residentId: String): Resident? = null

    override fun getPaymentsForLandlord(landlordId: String): Flow<List<Payment>> = emptyFlow()
    override fun getPaymentsForResident(residentId: String): Flow<List<Payment>> = emptyFlow()
    override suspend fun generateInvoice(
        landlordId: String,
        residentId: String,
        propertyId: String,
        amount: Double,
        dueDate: String,
        notes: String
    ): Payment = Payment("", landlordId, residentId, propertyId, amount, "INV-123", dueDate, null, PaymentStatus.PENDING, notes)
    override suspend fun payInvoice(paymentId: String): Boolean = true

    override fun getRequestsForLandlord(landlordId: String): Flow<List<MaintenanceRequest>> = emptyFlow()
    override fun getRequestsForResident(residentId: String): Flow<List<MaintenanceRequest>> = emptyFlow()
    override suspend fun addRequest(
        residentId: String,
        title: String,
        description: String,
        priority: RequestPriority
    ): MaintenanceRequest = MaintenanceRequest("", "", residentId, "", title, description, priority, RequestStatus.PENDING, "")
    override suspend fun updateRequestStatus(requestId: String, status: RequestStatus): Boolean = true

    override fun getDocumentsForLandlord(landlordId: String): Flow<List<Document>> = emptyFlow()
    override fun getDocumentsForResident(residentId: String): Flow<List<Document>> = emptyFlow()
    override suspend fun addDocument(
        landlordId: String,
        residentId: String,
        title: String,
        fileType: String
    ): Document = Document("", landlordId, residentId, title, fileType, "", "")
}

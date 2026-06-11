package com.example.propmanager.ui.landlord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.propmanager.data.DataRepository
import com.example.propmanager.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandlordViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    // For a real application, you'd want to pass landlordId into the ViewModel.
    // Here we are getting it directly from the currently logged in user for convenience.
    private val landlordId: String
        get() = repository.currentUser.value?.id ?: ""

    val currentUser = repository.currentUser

    val properties: StateFlow<List<Property>> = repository.getProperties(landlordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val residents: StateFlow<List<Resident>> = repository.getResidents(landlordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = repository.getPaymentsForLandlord(landlordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val requests: StateFlow<List<MaintenanceRequest>> = repository.getRequestsForLandlord(landlordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addProperty(name: String, address: String, rentAmount: Double, description: String) {
        viewModelScope.launch {
            repository.addProperty(landlordId, name, address, rentAmount, description)
        }
    }

    fun deleteProperty(propertyId: String) {
        viewModelScope.launch {
            repository.deleteProperty(propertyId)
        }
    }

    fun addResident(
        propertyId: String,
        name: String,
        email: String,
        phone: String,
        unitNumber: String,
        leaseStart: String,
        leaseEnd: String
    ) {
        viewModelScope.launch {
            repository.addResident(
                landlordId = landlordId,
                propertyId = propertyId,
                name = name,
                email = email,
                phone = phone,
                unitNumber = unitNumber,
                leaseStart = leaseStart,
                leaseEnd = leaseEnd
            )
        }
    }

    fun deleteResident(residentId: String) {
        viewModelScope.launch {
            repository.deleteResident(residentId)
        }
    }

    fun generateInvoice(
        residentId: String,
        propertyId: String,
        amount: Double,
        dueDate: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.generateInvoice(
                landlordId = landlordId,
                residentId = residentId,
                propertyId = propertyId,
                amount = amount,
                dueDate = dueDate,
                notes = notes
            )
        }
    }

    fun updateRequestStatus(requestId: String, status: RequestStatus) {
        viewModelScope.launch {
            repository.updateRequestStatus(requestId, status)
        }
    }
}

package com.example.propmanager.ui.resident

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
class ResidentViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    private val residentId: String
        get() = repository.currentUser.value?.id ?: ""

    val currentUser = repository.currentUser

    val payments: StateFlow<List<Payment>> = repository.getPaymentsForResident(residentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val requests: StateFlow<List<MaintenanceRequest>> = repository.getRequestsForResident(residentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documents: StateFlow<List<Document>> = repository.getDocumentsForResident(residentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val residentProfile: StateFlow<Resident?> = repository.getResidentProfile(residentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun payInvoice(paymentId: String) {
        viewModelScope.launch {
            repository.payInvoice(paymentId)
        }
    }

    fun submitRequest(title: String, description: String, priority: RequestPriority) {
        viewModelScope.launch {
            repository.addRequest(residentId, title, description, priority)
        }
    }
}

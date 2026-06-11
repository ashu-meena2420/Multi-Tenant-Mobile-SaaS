package com.example.propmanager.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.propmanager.data.DataRepository
import com.example.propmanager.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    val currentUser = repository.currentUser

    val landlords: StateFlow<List<User>> = repository.getLandlords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSubscription(landlordId: String, tier: String) {
        viewModelScope.launch {
            repository.updateLandlordSubscription(landlordId, tier)
        }
    }
}

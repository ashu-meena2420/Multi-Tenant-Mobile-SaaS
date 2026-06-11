package com.example.propmanager

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Login : NavKey

@Serializable
data class Register(val preselectedRole: String? = null) : NavKey

@Serializable
data class LandlordDashboard(val landlordId: String) : NavKey

@Serializable
data class ResidentDashboard(val residentId: String) : NavKey

@Serializable
data class AdminDashboard(val adminId: String) : NavKey

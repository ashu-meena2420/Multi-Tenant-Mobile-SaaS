package com.example.propmanager

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.propmanager.data.model.UserRole
import com.example.propmanager.ui.admin.AdminDashboardScreen
import com.example.propmanager.ui.auth.LoginScreen
import com.example.propmanager.ui.auth.RegisterScreen
import com.example.propmanager.ui.landlord.LandlordDashboardScreen
import com.example.propmanager.ui.resident.ResidentDashboardScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Login)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Login> {
          LoginScreen(
            onNavigateToRegister = { backStack.add(Register()) },
            onLoginSuccess = { user ->
              // Log in and route based on role
              backStack.removeLastOrNull()
              when (user.role) {
                UserRole.LANDLORD -> backStack.add(LandlordDashboard(user.id))
                UserRole.RESIDENT -> backStack.add(ResidentDashboard(user.id))
                UserRole.ADMIN -> backStack.add(AdminDashboard(user.id))
              }
            },
            modifier = Modifier.fillMaxSize()
          )
        }
        entry<Register> { key ->
          RegisterScreen(
            preselectedRole = key.preselectedRole,
            onNavigateToLogin = { backStack.removeLastOrNull() },
            onRegisterSuccess = { user ->
              // Complete registration and redirect
              backStack.removeLastOrNull() // Pop Register
              backStack.removeLastOrNull() // Pop Login
              when (user.role) {
                UserRole.LANDLORD -> backStack.add(LandlordDashboard(user.id))
                UserRole.RESIDENT -> backStack.add(ResidentDashboard(user.id))
                UserRole.ADMIN -> backStack.add(AdminDashboard(user.id))
              }
            },
            modifier = Modifier.fillMaxSize()
          )
        }
        entry<LandlordDashboard> { key ->
          LandlordDashboardScreen(
            landlordId = key.landlordId,
            onLogout = {
              backStack.removeLastOrNull()
              backStack.add(Login)
            },
            modifier = Modifier.fillMaxSize()
          )
        }
        entry<ResidentDashboard> { key ->
          ResidentDashboardScreen(
            residentId = key.residentId,
            onLogout = {
              backStack.removeLastOrNull()
              backStack.add(Login)
            },
            modifier = Modifier.fillMaxSize()
          )
        }
        entry<AdminDashboard> { key ->
          AdminDashboardScreen(
            adminId = key.adminId,
            onLogout = {
              backStack.removeLastOrNull()
              backStack.add(Login)
            },
            modifier = Modifier.fillMaxSize()
          )
        }
      },
  )
}

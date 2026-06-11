package com.example.propmanager.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.propmanager.data.model.User
import com.example.propmanager.data.model.UserRole
import com.example.propmanager.theme.*
import com.example.propmanager.ui.components.GlassCard
import com.example.propmanager.ui.components.PrimaryButton
import com.example.propmanager.ui.components.PropTextField

@Composable
fun RegisterScreen(
    preselectedRole: String?,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (User) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { 
        mutableStateOf(
            if (preselectedRole == "RESIDENT") UserRole.RESIDENT else UserRole.LANDLORD
        ) 
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onRegisterSuccess((uiState as AuthUiState.Success).user)
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(GradientBgStart, GradientBgEnd)
            )
        )
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create Account",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                style = Typography.headlineLarge,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Join our secure multi-tenant property network",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(34.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PropTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            if (uiState is AuthUiState.Error) viewModel.resetState() 
                        },
                        label = "Full Name",
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Name", tint = PrimaryIndigo)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PropTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            if (uiState is AuthUiState.Error) viewModel.resetState() 
                        },
                        label = "Email Address",
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = "Email", tint = PrimaryIndigo)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PropTextField(
                        value = phone,
                        onValueChange = { 
                            phone = it
                            if (uiState is AuthUiState.Error) viewModel.resetState() 
                        },
                        label = "Phone Number",
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone", tint = PrimaryIndigo)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PropTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            if (uiState is AuthUiState.Error) viewModel.resetState() 
                        },
                        label = "Password",
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Password", tint = PrimaryIndigo)
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (passwordVisible) "Hide password" else "Show password"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = description, tint = TextSecondary)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "I am registering as a:",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RoleSelectCard(
                            title = "Landlord",
                            desc = "Manage properties & tenants",
                            isSelected = selectedRole == UserRole.LANDLORD,
                            onClick = { selectedRole = UserRole.LANDLORD },
                            modifier = Modifier.weight(1f)
                        )

                        RoleSelectCard(
                            title = "Resident",
                            desc = "Pay rent & request repairs",
                            isSelected = selectedRole == UserRole.RESIDENT,
                            onClick = { selectedRole = UserRole.RESIDENT },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (uiState is AuthUiState.Error) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (uiState as AuthUiState.Error).message,
                            color = DangerRose,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(color = PrimaryIndigo)
                    } else {
                        PrimaryButton(
                            text = "Register",
                            onClick = { viewModel.register(email, name, selectedRole, phone, password) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Already have an account? ", color = TextSecondary)
                Text(
                    text = "Sign In",
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

@Composable
fun RoleSelectCard(
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) PrimaryIndigo else DarkBorder
    val bgColor = if (isSelected) GlassBg else Color.Transparent

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = if (isSelected) PrimaryIndigo else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = desc,
                color = TextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

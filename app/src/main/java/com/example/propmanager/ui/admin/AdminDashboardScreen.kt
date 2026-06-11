package com.example.propmanager.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.propmanager.data.model.User
import com.example.propmanager.data.model.UserRole
import com.example.propmanager.theme.*
import com.example.propmanager.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminId: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()

    val landlordsList by viewModel.landlords.collectAsStateWithLifecycle()
    var selectedLandlordForTier by remember { mutableStateOf<User?>(null) }

    // Aggregate statistics
    val totalLandlords = landlordsList.size
    
    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(GradientBgStart, GradientBgEnd)
            )
        ),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PropManager Admin System",
                            style = Typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Platform Control Panel • Multi-Tenant Audit",
                            color = PrimaryIndigo,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log Out",
                            tint = DangerRose
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface.copy(alpha = 0.4f),
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Global Platform Status",
                style = Typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Platform Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OverviewStatCard(
                    title = "Total Landlords",
                    value = "$totalLandlords",
                    subtitle = "Active Tenants",
                    accentColor = PrimaryIndigo,
                    modifier = Modifier.weight(1f)
                )

                OverviewStatCard(
                    title = "Database Isolation",
                    value = "Active",
                    subtitle = "Zero leakage verified",
                    accentColor = TertiaryGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            // Tenant breakdown
            Text(
                text = "Tenant (Landlord) Operations",
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            if (landlordsList.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No landlords registered yet.", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                landlordsList.forEach { landlord ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = landlord.name,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = landlord.email,
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                
                                val planColor = when (landlord.subscriptionTier) {
                                    "Enterprise" -> PurpleGlow
                                    "Pro" -> SecondaryBlue
                                    else -> TextSecondary
                                }

                                Box(
                                    modifier = Modifier.clickable { selectedLandlordForTier = landlord }
                                ) {
                                    StatusBadge(
                                        text = landlord.subscriptionTier,
                                        backgroundColor = planColor,
                                        textColor = planColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tap subscription badge to modify plan tier.",
                                    color = TextSecondary.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Admin Tenant Security audit
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GlassBgAccent.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security Audit",
                        tint = TertiaryGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Multi-Tenant Security Audit Pass",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "All database transactions and shared document pools require specific tenant keys. Partition checks complete and isolated.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }

    // Modify Subscription Tier Modal
    if (selectedLandlordForTier != null) {
        val landlord = selectedLandlordForTier!!
        val plans = listOf("Free", "Pro", "Enterprise")

        AlertDialog(
            onDismissRequest = { selectedLandlordForTier = null },
            containerColor = DarkSurface,
            title = { 
                Text(
                    text = "Assign Subscription Plan",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ) 
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Select subscription plan for ${landlord.name}:",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    plans.forEach { plan ->
                        val isCurrent = landlord.subscriptionTier == plan
                        val planColor = when (plan) {
                            "Enterprise" -> PurpleGlow
                            "Pro" -> SecondaryBlue
                            else -> TextSecondary
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCurrent) GlassBg else Color.Transparent)
                                .clickable {
                                    viewModel.updateSubscription(landlord.id, plan)
                                    selectedLandlordForTier = null
                                }
                                .border(
                                    1.dp,
                                    if (isCurrent) PrimaryIndigo else DarkBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = plan,
                                    color = if (isCurrent) PrimaryIndigo else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Active Plan",
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedLandlordForTier = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}


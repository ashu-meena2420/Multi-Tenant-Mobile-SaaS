package com.example.propmanager.ui.landlord

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.propmanager.data.model.*
import com.example.propmanager.theme.*
import com.example.propmanager.ui.components.*
import com.example.propmanager.util.formatRupees
import com.example.propmanager.util.isValidIndianPhone

enum class LandlordTab(val label: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    PROPERTIES("Properties", Icons.Default.Home),
    INVOICES("Invoices", Icons.AutoMirrored.Filled.ReceiptLong),
    MAINTENANCE("Repairs", Icons.Default.Build)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandlordDashboardScreen(
    landlordId: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LandlordViewModel = hiltViewModel()
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(LandlordTab.OVERVIEW) }

    // Data streams
    val propertiesList by viewModel.properties.collectAsStateWithLifecycle()
    val residentsList by viewModel.residents.collectAsStateWithLifecycle()
    val paymentsList by viewModel.payments.collectAsStateWithLifecycle()
    val requestsList by viewModel.requests.collectAsStateWithLifecycle()

    // Dialog state controllers
    var showAddPropDialog by remember { mutableStateOf(false) }
    var showAddResidentDialogByPropertyId by remember { mutableStateOf<String?>(null) }
    var showAddInvoiceDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

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
                            text = "PropManager SaaS",
                            style = Typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${user?.name ?: "Landlord Panel"} • ${user?.subscriptionTier ?: "Free"} Plan",
                            color = PrimaryIndigo,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                actions = {
                    // Profile button on the top right
                    IconButton(
                        onClick = { showProfileDialog = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(GlassBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Details",
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface.copy(alpha = 0.4f),
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface.copy(alpha = 0.6f),
                tonalElevation = 8.dp
            ) {
                LandlordTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (isSelected) PrimaryIndigo else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = GlassBg
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                LandlordTab.PROPERTIES -> {
                    ExtendedFloatingActionButton(
                        text = { Text("New Property", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Add, contentDescription = "Add Property") },
                        onClick = { showAddPropDialog = true },
                        containerColor = PrimaryIndigo,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                LandlordTab.INVOICES -> {
                    ExtendedFloatingActionButton(
                        text = { Text("New Invoice", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Add, contentDescription = "New Invoice") },
                        onClick = { showAddInvoiceDialog = true },
                        containerColor = SecondaryBlue,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                else -> {}
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                LandlordTab.OVERVIEW -> {
                    OverviewTabContent(
                        properties = propertiesList,
                        residents = residentsList,
                        payments = paymentsList,
                        requests = requestsList
                    )
                }
                LandlordTab.PROPERTIES -> {
                    PropertiesTabContent(
                        properties = propertiesList,
                        residents = residentsList,
                        onAddResidentForProperty = { showAddResidentDialogByPropertyId = it },
                        onDeleteProp = { viewModel.deleteProperty(it) },
                        onDeleteResident = { viewModel.deleteResident(it) }
                    )
                }
                LandlordTab.INVOICES -> {
                    InvoicesTabContent(
                        payments = paymentsList,
                        residents = residentsList
                    )
                }
                LandlordTab.MAINTENANCE -> {
                    MaintenanceTabContent(
                        requests = requestsList,
                        onUpdateStatus = { reqId, status ->
                            viewModel.updateRequestStatus(reqId, status)
                        }
                    )
                }
            }
        }
    }

    // Add Property Modal Dialog
    if (showAddPropDialog) {
        var name by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var rentAmt by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddPropDialog = false },
            containerColor = DarkSurface,
            title = { Text("Add New Property", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PropTextField(value = name, onValueChange = { name = it }, label = "Property Name")
                    PropTextField(value = address, onValueChange = { address = it }, label = "Address")
                    PropTextField(
                        value = rentAmt,
                        onValueChange = { rentAmt = it },
                        label = "Monthly Rent (₹)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    PropTextField(value = desc, onValueChange = { desc = it }, label = "Description")
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Save",
                    onClick = {
                        val rent = rentAmt.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank() && address.isNotBlank()) {
                            viewModel.addProperty(name, address, rent, desc)
                            showAddPropDialog = false
                        }
                    },
                    modifier = Modifier.width(100.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddPropDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Add Resident Modal Dialog (Contextual selection)
    if (showAddResidentDialogByPropertyId != null) {
        val targetPropId = showAddResidentDialogByPropertyId!!
        val prop = propertiesList.find { it.id == targetPropId }
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var unit by remember { mutableStateOf("") }
        var leaseStart by remember { mutableStateOf("01-06-2026") }
        var leaseEnd by remember { mutableStateOf("31-05-2027") }
        var formError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddResidentDialogByPropertyId = null },
            containerColor = DarkSurface,
            title = { Text("Link Resident to Unit", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Linking resident to: ${prop?.name ?: "Unknown"}",
                        color = PrimaryIndigo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    PropTextField(value = name, onValueChange = { name = it; formError = "" }, label = "Resident Name")
                    PropTextField(value = email, onValueChange = { email = it; formError = "" }, label = "Email Address")
                    PropTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10) { phone = it; formError = "" } },
                        label = "Phone (10-digit Indian mobile)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    PropTextField(value = unit, onValueChange = { unit = it }, label = "Unit/Flat Number")

                    if (formError.isNotEmpty()) {
                        Text(formError, color = DangerRose, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Link",
                    onClick = {
                        when {
                            name.isBlank() -> formError = "Resident name is required."
                            email.isBlank() -> formError = "Email address is required."
                            phone.isNotBlank() && !isValidIndianPhone(phone) -> formError = "Enter a valid 10-digit Indian mobile number."
                            else -> {
                                viewModel.addResident(
                                    targetPropId, name, email, phone, unit, leaseStart, leaseEnd
                                )
                                showAddResidentDialogByPropertyId = null
                            }
                        }
                    },
                    modifier = Modifier.width(100.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddResidentDialogByPropertyId = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Add Rent Invoice Dialog
    if (showAddInvoiceDialog) {
        var selectedResId by remember { mutableStateOf(residentsList.firstOrNull()?.id ?: "") }
        var amount by remember { mutableStateOf("") }
        var dueDate by remember { mutableStateOf("01-07-2026") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddInvoiceDialog = false },
            containerColor = DarkSurface,
            title = { Text("Generate Rent Invoice", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Select Resident:", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    residentsList.forEach { res ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedResId = res.id }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedResId == res.id,
                                onClick = { selectedResId = res.id },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryIndigo)
                            )
                            Text("${res.name} (${res.unitNumber})", color = TextPrimary, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    PropTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = "Invoice Amount (₹)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    PropTextField(value = dueDate, onValueChange = { dueDate = it }, label = "Due Date (DD-MM-YYYY)")
                    PropTextField(value = notes, onValueChange = { notes = it }, label = "Notes")
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Generate",
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        val resident = residentsList.find { it.id == selectedResId }
                        if (resident != null && amt > 0.0) {
                            viewModel.generateInvoice(
                                selectedResId, resident.propertyId, amt, dueDate, notes
                            )
                            showAddInvoiceDialog = false
                        }
                    },
                    modifier = Modifier.width(120.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddInvoiceDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Profile Details Modal
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Your Landlord Profile", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(GlassBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = user?.name ?: "Landlord Name",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = user?.email ?: "landlord@prop.com",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        if (user?.phone?.isNotBlank() == true) {
                            Text(
                                text = "Phone: ${user?.phone}",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    
                    // Subscription status
                    val tierColor = when (user?.subscriptionTier) {
                        "Enterprise" -> PurpleGlow
                        "Pro" -> SecondaryBlue
                        else -> TextSecondary
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                            .background(DarkSurface.copy(alpha = 0.5f))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SaaS Subscription Plan", color = TextSecondary, fontSize = 12.sp)
                            StatusBadge(
                                text = user?.subscriptionTier ?: "Free",
                                backgroundColor = tierColor,
                                textColor = tierColor
                            )
                        }
                    }
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Log Out",
                    onClick = {
                        showProfileDialog = false
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showProfileDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = TextSecondary, textAlign = TextAlign.Center)
                }
            }
        )
    }
}

// LANDLORD TAB CONTENTS

@Composable
fun OverviewTabContent(
    properties: List<Property>,
    residents: List<Resident>,
    payments: List<Payment>,
    requests: List<MaintenanceRequest>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Overview Dashboard",
            style = Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // Statistics cards grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val totalProps = properties.size
            val occupied = properties.sumOf { it.occupiedUnits }
            val occupancyRate = if (totalProps > 0) (occupied.toFloat() / properties.sumOf { it.totalUnits } * 100).toInt() else 0

            OverviewStatCard(
                title = "Properties",
                value = "$totalProps",
                subtitle = "$occupancyRate% Occupancy",
                accentColor = PrimaryIndigo,
                modifier = Modifier.weight(1f)
            )

            val pendingRepairs = requests.count { it.status == RequestStatus.PENDING }
            OverviewStatCard(
                title = "Active Repairs",
                value = "$pendingRepairs",
                subtitle = "Tickets pending",
                accentColor = DangerRose,
                modifier = Modifier.weight(1f)
            )
        }

        // Cash flow donut Canvas Chart
        val collected = payments.filter { it.status == PaymentStatus.PAID }.sumOf { it.amount }
        val pending = payments.filter { it.status == PaymentStatus.PENDING }.sumOf { it.amount }
        RevenueChart(collected = collected, pending = pending, modifier = Modifier.fillMaxWidth())

        // Tenant isolations debug banner
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = GlassBgAccent.copy(alpha = 0.05f)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Security, contentDescription = "Security", tint = TertiaryGreen, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text("Tenant Data Isolation Active", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("All documents, properties, and payment details are strictly partitioned and restricted to your account profile.", color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
                }
            }
        }
    }
}

@Composable
fun PropertiesTabContent(
    properties: List<Property>,
    residents: List<Resident>,
    onAddResidentForProperty: (String) -> Unit,
    onDeleteProp: (String) -> Unit,
    onDeleteResident: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Properties", style = Typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

        Text("Managed Properties", style = Typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        if (properties.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No properties added yet. Tap '+' to create one.", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            properties.forEach { prop ->
                val propResidents = residents.filter { it.propertyId == prop.id }
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(prop.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            IconButton(onClick = { onDeleteProp(prop.id) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRose, modifier = Modifier.size(20.dp))
                            }
                        }
                        Text(prop.address, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.0.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(prop.description, color = TextPrimary, fontSize = 14.sp, lineHeight = 18.sp)
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Rent: ${formatRupees(prop.rentAmount)}/mo", color = PrimaryIndigo, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            
                            // Contextual link resident action
                            TextButton(
                                onClick = { onAddResidentForProperty(prop.id) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .border(1.dp, SecondaryBlue, RoundedCornerShape(8.dp))
                                    .height(32.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = SecondaryBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ Add Tenant", color = SecondaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (propResidents.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkBorder.copy(alpha = 0.5f))
                            Text("Linked Tenants (${propResidents.size}):", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            propResidents.forEach { res ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${res.name} (Unit ${res.unitNumber})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Text(res.email, color = TextSecondary, fontSize = 11.sp)
                                    }
                                    IconButton(onClick = { onDeleteResident(res.id) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.LinkOff, contentDescription = "Unlink", tint = DangerRose, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(60.dp)) // padding for FAB
    }
}

@Composable
fun InvoicesTabContent(
    payments: List<Payment>,
    residents: List<Resident>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Billing", style = Typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

        Text("Payment Records", style = Typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        if (payments.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No invoices generated yet.", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            payments.forEach { pay ->
                val resident = residents.find { it.id == pay.residentId }
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(pay.invoiceNumber, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Resident: ${resident?.name ?: "Unknown"}", color = TextSecondary, fontSize = 12.sp)
                            Text("Due: ${pay.dueDate}", color = TextSecondary, fontSize = 12.sp)
                            if (pay.status == PaymentStatus.PAID) {
                                Text("Paid: ${pay.paidDate}", color = TertiaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatRupees(pay.amount), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            val badgeColor = when(pay.status) {
                                PaymentStatus.PAID -> TertiaryGreen
                                PaymentStatus.PENDING -> WarningAmber
                                PaymentStatus.OVERDUE -> DangerRose
                            }
                            StatusBadge(text = pay.status.name, backgroundColor = badgeColor, textColor = badgeColor)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(60.dp)) // padding for FAB
    }
}

@Composable
fun MaintenanceTabContent(
    requests: List<MaintenanceRequest>,
    onUpdateStatus: (String, RequestStatus) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Repairs & Support", style = Typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

        if (requests.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No maintenance requests reported.", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            requests.forEach { req ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(req.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            val priorityColor = when(req.priority) {
                                RequestPriority.LOW -> TertiaryGreen
                                RequestPriority.MEDIUM -> WarningAmber
                                RequestPriority.HIGH -> DangerRose
                            }
                            StatusBadge(text = req.priority.name, backgroundColor = priorityColor, textColor = priorityColor)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Submitted: ${req.dateSubmitted}", color = TextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(req.description, color = TextPrimary, fontSize = 14.sp, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val statusColor = when (req.status) {
                                RequestStatus.RESOLVED -> TertiaryGreen
                                RequestStatus.IN_PROGRESS -> SecondaryBlue
                                else -> WarningAmber
                            }
                            Text("Status: ${req.status.name}", color = statusColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (req.status != RequestStatus.IN_PROGRESS && req.status != RequestStatus.RESOLVED) {
                                    Button(
                                        onClick = { onUpdateStatus(req.id, RequestStatus.IN_PROGRESS) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (req.status != RequestStatus.RESOLVED) {
                                    Button(
                                        onClick = { onUpdateStatus(req.id, RequestStatus.RESOLVED) },
                                        colors = ButtonDefaults.buttonColors(containerColor = TertiaryGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Resolve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



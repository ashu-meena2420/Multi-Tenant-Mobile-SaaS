package com.example.propmanager.ui.resident

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.propmanager.data.model.*
import com.example.propmanager.theme.*
import com.example.propmanager.ui.components.*
import com.example.propmanager.util.formatRupees

enum class ResidentTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    RENT("Bills & Ledger", Icons.AutoMirrored.Filled.ReceiptLong),
    REPAIRS("Support", Icons.Default.Build)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentDashboardScreen(
    residentId: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResidentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(ResidentTab.DASHBOARD) }

    // Live streams
    val paymentsList by viewModel.payments.collectAsStateWithLifecycle()
    val requestsList by viewModel.requests.collectAsStateWithLifecycle()
    val documentsList by viewModel.documents.collectAsStateWithLifecycle()

    // Dialog state controllers
    var showAddRequestDialog by remember { mutableStateOf(false) }
    var selectedPaymentForSimulation by remember { mutableStateOf<Payment?>(null) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val residentProfile by viewModel.residentProfile.collectAsStateWithLifecycle()

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
                            text = "Resident Portal",
                            style = Typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${user?.name ?: "Resident"} • Unit: ${residentProfile?.unitNumber ?: "N/A"}",
                            color = PrimaryIndigo,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                actions = {
                    // Profile avatar on the top right
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
                ResidentTab.entries.forEach { tab ->
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
            if (selectedTab == ResidentTab.REPAIRS) {
                ExtendedFloatingActionButton(
                    text = { Text("New Request", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Add, contentDescription = "New Ticket") },
                    onClick = { showAddRequestDialog = true },
                    containerColor = PrimaryIndigo,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                ResidentTab.DASHBOARD -> {
                    ResidentDashboardTabContent(
                        payments = paymentsList,
                        requests = requestsList,
                        profile = residentProfile,
                        documents = documentsList
                    )
                }
                ResidentTab.RENT -> {
                    ResidentRentTabContent(
                        payments = paymentsList,
                        onPayClick = { payItem ->
                            selectedPaymentForSimulation = payItem
                        }
                    )
                }
                ResidentTab.REPAIRS -> {
                    ResidentRepairsTabContent(
                        requests = requestsList
                    )
                }
            }
        }
    }

    // New Maintenance Ticket dialog modal
    if (showAddRequestDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf(RequestPriority.MEDIUM) }

        AlertDialog(
            onDismissRequest = { showAddRequestDialog = false },
            containerColor = DarkSurface,
            title = { Text("Submit Repair Request", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PropTextField(value = title, onValueChange = { title = it }, label = "Problem Summary")
                    PropTextField(value = description, onValueChange = { description = it }, label = "Detailed Description")
                    
                    Text("Severity Priority:", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RequestPriority.entries.forEach { prio ->
                            val isSel = priority == prio
                            val color = when(prio) {
                                RequestPriority.LOW -> TertiaryGreen
                                RequestPriority.MEDIUM -> WarningAmber
                                RequestPriority.HIGH -> DangerRose
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .weight(1f)
                                    .background(if (isSel) color.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(1.5.dp, if (isSel) color else DarkBorder, RoundedCornerShape(12.dp))
                                    .clickable { priority = prio }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(prio.name, color = if (isSel) color else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Submit",
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank()) {
                            viewModel.submitRequest(title, description, priority)
                            showAddRequestDialog = false
                        }
                    },
                    modifier = Modifier.width(100.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddRequestDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Payment credit card simulator sheet
    if (selectedPaymentForSimulation != null) {
        val invoice = selectedPaymentForSimulation!!
        var cardNumber by remember { mutableStateOf("") }
        var cardExpiry by remember { mutableStateOf("") }
        var cardCvv by remember { mutableStateOf("") }
        var isPaying by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isPaying) selectedPaymentForSimulation = null },
            containerColor = DarkSurface,
            title = {
                Text("Secure Checkout", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Credit Card Visual Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryIndigo, PurpleGlow)
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("PropManager billing", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Icon(Icons.Default.CreditCard, contentDescription = "Card icon", tint = Color.White)
                            }
                            Text(
                                text = if (cardNumber.isBlank()) "**** **** **** ****" else cardNumber,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("VAL THRU", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp)
                                    Text(text = if (cardExpiry.isBlank()) "MM/YY" else cardExpiry, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("PAYMENT AMOUNT", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp)
                                    Text(formatRupees(invoice.amount), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    PropTextField(
                        value = cardNumber,
                        onValueChange = { if (it.length <= 16) cardNumber = it },
                        label = "Card Number",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PropTextField(
                            value = cardExpiry,
                            onValueChange = { if (it.length <= 5) cardExpiry = it },
                            label = "Expiry (MM/YY)",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        PropTextField(
                            value = cardCvv,
                            onValueChange = { if (it.length <= 3) cardCvv = it },
                            label = "CVV",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = if (isPaying) "Paying..." else "Pay ${formatRupees(invoice.amount)}",
                    onClick = {
                        isPaying = true
                        viewModel.payInvoice(invoice.id)
                        isPaying = false
                        selectedPaymentForSimulation = null
                        Toast.makeText(context, "Payment Successful!", Toast.LENGTH_LONG).show()
                    },
                    enabled = cardNumber.length >= 12 && cardExpiry.length >= 4 && cardCvv.length >= 3 && !isPaying,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                if (!isPaying) {
                    TextButton(onClick = { selectedPaymentForSimulation = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
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
                Text("Your Resident Profile", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                            text = user?.name ?: "Resident Name",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = user?.email ?: "resident@prop.com",
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
                            Text("Resident Status", color = TextSecondary, fontSize = 12.sp)
                            StatusBadge(
                                text = "Active Lease",
                                backgroundColor = TertiaryGreen,
                                textColor = TertiaryGreen
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

@Composable
fun ResidentDashboardTabContent(
    payments: List<Payment>,
    requests: List<MaintenanceRequest>,
    profile: Resident?,
    documents: List<Document>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Your Resident Space", style = Typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

        // Rent Due Banner
        val activeInvoice = payments.find { it.status == PaymentStatus.PENDING }
        if (activeInvoice != null) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = WarningAmber.copy(alpha = 0.08f),
                borderWidth = 1.5.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = "Due", tint = WarningAmber, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rent Invoice Outstanding", color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Invoice ${activeInvoice.invoiceNumber} of ${formatRupees(activeInvoice.amount)} is due by ${activeInvoice.dueDate}.", color = TextPrimary, fontSize = 14.sp, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Head over to the Bills & Ledger tab to complete payment.", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TertiaryGreen.copy(alpha = 0.08f),
                borderWidth = 1.5.dp
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Paid", tint = TertiaryGreen, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("You're all paid up!", color = TertiaryGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("No outstanding invoices found for this month.", color = TextPrimary, fontSize = 13.sp)
                    }
                }
            }
        }

        // Summary Statistics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val pendingRepairs = requests.count { it.status != RequestStatus.RESOLVED }
            OverviewStatCard(
                title = "Pending Repairs",
                value = "$pendingRepairs",
                subtitle = "Active requests",
                accentColor = PrimaryIndigo,
                modifier = Modifier.weight(1f)
            )

            val paidRentCount = payments.count { it.status == PaymentStatus.PAID }
            OverviewStatCard(
                title = "Paid Invoices",
                value = "$paidRentCount",
                subtitle = "Payment history",
                accentColor = TertiaryGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Lease Timeline Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Lease Timeline", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(14.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("START", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(profile?.leaseStart ?: "N/A", color = PrimaryIndigo, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .padding(horizontal = 10.dp)
                            .background(DarkBorder, CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f) // represent lease completion
                                .fillMaxHeight()
                                .background(PrimaryIndigo, CircleShape)
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text("END", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(profile?.leaseEnd ?: "N/A", color = PurpleGlow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Shared Documents Section
        Text("Shared Lease Documents", style = Typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        if (documents.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No agreement documents shared.", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            documents.forEach { doc ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = "Doc", tint = PrimaryIndigo, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(doc.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${doc.fileType} • Uploaded: ${doc.dateUploaded}", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                        val ctx = LocalContext.current
                        IconButton(onClick = {
                            Toast.makeText(ctx, "Opening lease file: ${doc.title}", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Download, contentDescription = "Download", tint = PrimaryIndigo)
                        }
                    }
                }
            }
        }

        // Data security Isolation Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = GlassBg.copy(alpha = 0.05f)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Lock", tint = PrimaryIndigo, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text("Secure Tenant Space Active", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Your rental information, receipts, and support tickets are isolated securely. Other tenants cannot view your data.", color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ResidentRentTabContent(
    payments: List<Payment>,
    onPayClick: (Payment) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Billing & Receipts", style = Typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

        if (payments.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No billing history found.", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            payments.forEach { pay ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(pay.invoiceNumber, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Due date: ${pay.dueDate}", color = TextSecondary, fontSize = 12.sp)
                                if (pay.paidDate != null) {
                                    Text("Paid on: ${pay.paidDate}", color = TertiaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(formatRupees(pay.amount), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val badgeColor = when(pay.status) {
                                PaymentStatus.PAID -> TertiaryGreen
                                PaymentStatus.PENDING -> WarningAmber
                                PaymentStatus.OVERDUE -> DangerRose
                            }
                            StatusBadge(text = pay.status.name, backgroundColor = badgeColor, textColor = badgeColor)

                            if (pay.status == PaymentStatus.PENDING) {
                                Button(
                                    onClick = { onPayClick(pay) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text("Pay Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResidentRepairsTabContent(
    requests: List<MaintenanceRequest>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Repair Requests", style = Typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

        if (requests.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No support tickets submitted yet. Tap '+' to create one.", color = TextSecondary, fontSize = 14.sp)
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
                            val pColor = when(req.priority) {
                                RequestPriority.LOW -> TertiaryGreen
                                RequestPriority.MEDIUM -> WarningAmber
                                RequestPriority.HIGH -> DangerRose
                            }
                            StatusBadge(text = req.priority.name, backgroundColor = pColor, textColor = pColor)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Submitted: ${req.dateSubmitted}", color = TextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(req.description, color = TextPrimary, fontSize = 14.sp, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        val statusColor = when (req.status) {
                            RequestStatus.RESOLVED -> TertiaryGreen
                            RequestStatus.IN_PROGRESS -> SecondaryBlue
                            else -> WarningAmber
                        }
                        Text("Status: ${req.status.name}", color = statusColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(60.dp)) // padding for FAB
    }
}

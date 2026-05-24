package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ClientVisit
import com.example.data.DrinkItem
import com.example.viewmodel.DrinkViewModel
import com.example.viewmodel.ReportUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkDashboard(
    viewModel: DrinkViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drinks by viewModel.drinksState.collectAsStateWithLifecycle()
    val visits by viewModel.visitsState.collectAsStateWithLifecycle()
    val reportState by viewModel.reportUiState.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    
    // Dialog Triggers
    var showAddDrinkDialog by remember { mutableStateOf(false) }
    var showLogVisitDialog by remember { mutableStateOf(false) }
    var editingDrink by remember { mutableStateOf<DrinkItem?>(null) }
    
    // KPI Math
    val totalRevenue = visits.sumOf { it.totalPrice }
    val totalOrders = visits.size

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalCafe,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "อ๋อคาเฟ่ Pro",
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (drinks.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        if (selectedTab == 1) {
                            showAddDrinkDialog = true
                        } else {
                            if (drinks.isEmpty()) {
                                Toast.makeText(context, "Please configure inventory drinks first!", Toast.LENGTH_LONG).show()
                            } else {
                                showLogVisitDialog = true
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_action")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Default.AddBusiness else Icons.Default.PostAdd,
                            contentDescription = if (selectedTab == 1) "Add New Drink" else "Log Visit"
                        )
                        Text(
                            text = if (selectedTab == 1) "เพิ่มเมนู / New Menu" else "บันทึกออเดอร์ / Log Order",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // KPI Summary Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KPIWidget(
                        label = "รายได้ / Revenue",
                        value = "฿${String.format(java.util.Locale.US, "%.2f", totalRevenue)}",
                        icon = Icons.Default.MonetizationOn,
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                    VerticalDivider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    KPIWidget(
                        label = "ออเดอร์ / Orders",
                        value = "$totalOrders",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        accentColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Tab Headers
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("การสั่งซื้อ / Orders", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.testTag("tab_visits")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("เมนูเครื่องดื่ม / Menu", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.testTag("tab_inventory")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("วิเคราะห์ AI / AI Briefing", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.testTag("tab_briefing")
                )
            }

            // Tab Contents
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(top = 12.dp)
            ) {
                when (selectedTab) {
                    0 -> VisitsWorkspace(
                        visits = visits,
                        onDeleteVisit = { viewModel.deleteClientVisit(it) },
                        onLogOrderClick = {
                            if (drinks.isEmpty()) {
                                Toast.makeText(context, "Please configure inventory drinks first!", Toast.LENGTH_LONG).show()
                            } else {
                                showLogVisitDialog = true
                            }
                        }
                    )
                    1 -> InventoryWorkspace(
                        drinks = drinks,
                        onDeleteDrink = { viewModel.deleteDrink(it) },
                        onEditDrink = { editingDrink = it },
                        onAddDrinkClick = { showAddDrinkDialog = true }
                    )
                    2 -> BriefingWorkspace(
                        reportState = reportState,
                        visits = visits,
                        drinks = drinks,
                        onGenerate = { interval -> viewModel.generateReport(interval) },
                        onReset = { viewModel.resetReport() }
                    )
                }
            }
        }
    }

    // Modal Dialogs
    if (showAddDrinkDialog) {
        AddDrinkModal(
            onDismiss = { showAddDrinkDialog = false },
            onConfirm = { name, price, stock, warning, category, colorHex ->
                viewModel.addNewDrink(name, price, stock, warning, category, colorHex)
                showAddDrinkDialog = false
                Toast.makeText(context, "$name added to Menu", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showLogVisitDialog && drinks.isNotEmpty()) {
        LogVisitModal(
            drinks = drinks,
            onDismiss = { showLogVisitDialog = false },
            onConfirm = { client, drink, qty, notes, discount ->
                viewModel.logClientVisit(client, drink, qty, notes, discount)
                showLogVisitDialog = false
                val promo = if (discount > 0) " (with $discount% discount Promo)" else ""
                Toast.makeText(context, "Logged order of $qty x ${drink.name}$promo", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (editingDrink != null) {
        EditDrinkModal(
            drink = editingDrink!!,
            onDismiss = { editingDrink = null },
            onConfirm = { updatedDrink ->
                viewModel.updateDrink(updatedDrink)
                editingDrink = null
                Toast.makeText(context, "${updatedDrink.name} updated successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun KPIWidget(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ================= WORKSPACE 1: CLIENT VISITS =================
@Composable
fun VisitsWorkspace(
    visits: List<ClientVisit>,
    onDeleteVisit: (ClientVisit) -> Unit,
    onLogOrderClick: () -> Unit
) {
    if (visits.isEmpty()) {
        EmptyStateWidget(
            icon = Icons.Default.SupervisorAccount,
            title = "No Client Visits Logged Today",
            desc = "Start tracking coffee shop sales and visit notes by entering your first customer ledger order.",
            buttonText = "Quick Log Order",
            onAction = onLogOrderClick
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Text(
                    text = "Today's Client Logbook",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(visits, key = { it.id }) { visit ->
                ClientVisitCard(
                    visit = visit,
                    onDelete = { onDeleteVisit(visit) }
                )
            }
        }
    }
}

@Composable
fun ClientVisitCard(
    visit: ClientVisit,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = formatter.format(Date(visit.orderTime))

    val discountPercent = remember(visit.notes) {
        val match = "\\[Discount: (\\d+)%\\]".toRegex().find(visit.notes)
        match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    val cleanNotes = remember(visit.notes) {
        visit.notes.replace("\\[Discount: \\d+%\\]\\s*".toRegex(), "").trim()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("visit_card_${visit.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary)
                        )
                        Text(
                            text = visit.clientName.ifBlank { "Anonymous Guest" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (discountPercent > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                Text(
                                    text = "$discountPercent% OFF",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Time logged: $formattedTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Delete order (Refund)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Void client order",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${visit.qty}x ${visit.drinkName}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    if (discountPercent > 0) {
                        val originalVal = visit.drinkPrice * visit.qty
                        Text(
                            text = "฿${String.format(java.util.Locale.US, "%.2f", originalVal)}",
                            style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough),
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(end = 2.dp)
                        )
                    }
                    Text(
                        text = "฿${String.format(java.util.Locale.US, "%.2f", visit.totalPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (cleanNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "\"$cleanNotes\"",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ================= WORKSPACE 2: INVENTORY =================
@Composable
fun InventoryWorkspace(
    drinks: List<DrinkItem>,
    onDeleteDrink: (DrinkItem) -> Unit,
    onEditDrink: (DrinkItem) -> Unit,
    onAddDrinkClick: () -> Unit
) {
    if (drinks.isEmpty()) {
        EmptyStateWidget(
            icon = Icons.Default.Fastfood,
            title = "Zero Drinks in Menu",
            desc = "Your café menu is blank. Add drinks like Latte, Mocha, or Green Tea along with initial quantities and price.",
            buttonText = "Add First Beverage",
            onAction = onAddDrinkClick
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "รายการเมนูเครื่องดื่ม / Drink Menu Directory",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(drinks, key = { it.id }) { drink ->
                    DrinkInventoryCard(
                        drink = drink,
                        onEdit = { onEditDrink(drink) },
                        onDelete = { onDeleteDrink(drink) }
                    )
                }
            }
        }
    }
}

@Composable
fun DrinkInventoryCard(
    drink: DrinkItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val color = remember(drink.colorHex) {
        parseHexColor(drink.colorHex)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("drink_card_${drink.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Category Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = drink.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Name
            Text(
                text = drink.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            // Price tag
            Text(
                text = "฿${String.format(java.util.Locale.US, "%.2f", drink.price)}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Actions Area
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "ราคาต่อหน่วย / Unit Price",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp).testTag("btn_edit_drink_${drink.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("btn_delete_drink_${drink.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

// ================= WORKSPACE 3: AI SMART BRIEFINGS =================
@Composable
fun BriefingWorkspace(
    reportState: ReportUiState,
    visits: List<ClientVisit>,
    drinks: List<DrinkItem>,
    onGenerate: (com.example.api.ReportInterval) -> Unit,
    onReset: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var selectedInterval by remember { mutableStateOf(com.example.api.ReportInterval.DAILY) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Timeframe Selector Widget (Pre-generation context selection)
        if (reportState is ReportUiState.Initial || reportState is ReportUiState.ApiKeyMissing) {
            Text(
                text = "Select Analytical Range",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                com.example.api.ReportInterval.values().forEach { intervalOption ->
                    val isSelected = selectedInterval == intervalOption
                    val optionLabel = when(intervalOption) {
                        com.example.api.ReportInterval.DAILY -> "Daily"
                        com.example.api.ReportInterval.WEEKLY -> "Weekly (7D)"
                        com.example.api.ReportInterval.MONTHLY -> "Monthly (30D)"
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary 
                                else Color.Transparent
                            )
                            .clickable { selectedInterval = intervalOption }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = optionLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (reportState) {
            is ReportUiState.Initial -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val promptTitle = when (selectedInterval) {
                            com.example.api.ReportInterval.DAILY -> "Daily Drink & Stock Briefings"
                            com.example.api.ReportInterval.WEEKLY -> "Weekly Performance Analytics"
                            com.example.api.ReportInterval.MONTHLY -> "Monthly Café Business Metrics"
                        }
                        
                        val promptDesc = when (selectedInterval) {
                            com.example.api.ReportInterval.DAILY -> "Let Gemini analyze coffee notes, client walk-ins, and drink depletion margins over the past 24 hours to plan your next operations shift."
                            com.example.api.ReportInterval.WEEKLY -> "Synthesize client traffic spikes, modifier popularity trends, and ingredient burn rates over the last 7 days to organize menu logistics."
                            com.example.api.ReportInterval.MONTHLY -> "Compile comprehensive gross revenues, ingredient lifetime longevity indexes, and top business strategy vectors over the last 30 days."
                        }

                        Text(
                            text = promptTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = promptDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { onGenerate(selectedInterval) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_generate_brief")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate AI ${selectedInterval.name} Report", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                // Simulated Preview Card
                Text(
                    text = "SAMPLE BRIEF OUTLINE Preview",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        val previewLabel = when (selectedInterval) {
                            com.example.api.ReportInterval.DAILY -> "☕ DAILY STATUS BREWER BRIEF [PREVIEW]"
                            com.example.api.ReportInterval.WEEKLY -> "☕ WEEKLY LOGISTICS COMPILE [PREVIEW]"
                            com.example.api.ReportInterval.MONTHLY -> "☕ MONTHLY GROWTH STATEMENT [PREVIEW]"
                        }
                        
                        val previewBullet1 = when (selectedInterval) {
                            com.example.api.ReportInterval.DAILY -> "• **Bestseller:** Matcha Latte was today's high driver (42% volume)."
                            com.example.api.ReportInterval.WEEKLY -> "• **Traffic Spike:** Friday is your highest revenue driver (34% weekly gross)."
                            com.example.api.ReportInterval.MONTHLY -> "• **Trend Factor:** Customer order volume represents a +14.5% month-on-month surge."
                        }

                        val previewBullet2 = when (selectedInterval) {
                            com.example.api.ReportInterval.DAILY -> "• **WarningAlert:** Taro Bubble Tea has depleted below threshold (Stock: 4/10)."
                            com.example.api.ReportInterval.WEEKLY -> "• **Burn Rate Warning:** Coffee cups and oat substitutes are burning 18% quicker weekly."
                            com.example.api.ReportInterval.MONTHLY -> "• **Category Lifespan:** Specialty category items maintain underutilized stock levels (65% intact)."
                        }

                        val previewBullet3 = when (selectedInterval) {
                            com.example.api.ReportInterval.DAILY -> "• **Client Requests:** Oatmeal substitute was requested 5 times."
                            com.example.api.ReportInterval.WEEKLY -> "• **Modifier Focus:** Lactose-free and honey-honey sweetener customizations grew in volume."
                            com.example.api.ReportInterval.MONTHLY -> "• **Strategic Opportunity:** Target weekend custom menu combos to boost low margin tea categories."
                        }

                        Text(previewLabel, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(previewBullet1, fontSize = 13.sp)
                        Text(previewBullet2, fontSize = 13.sp)
                        Text(previewBullet3, fontSize = 13.sp)
                    }
                }
            }

            is ReportUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Brewing your ${selectedInterval.name.lowercase(Locale.getDefault())} report...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Gemini is analyzing beverage logs, calendar spikes, and inventory trends.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }

            is ReportUiState.ApiKeyMissing -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Symbols.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text(
                                text = "Gemini API Key Missing",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "To run real-time AI report analyses, please enter your GEMINI_API_KEY inside the Secrets Panel of AI Studio. View the high-fidelity offline report below.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onReset,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Go Back", color = Color.White)
                        }
                    }
                }

                // Generous Interactive fallback mockup generated dynamically so that app has functional beauty anyway!
                MockupDrinkReport(visits, drinks, selectedInterval, onReset)
            }

            is ReportUiState.Error -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Briefing Error",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = reportState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onReset,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Back")
                        }
                    }
                }
            }

            is ReportUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 80.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedInterval.name.lowercase(Locale.getDefault()).replaceFirstChar { it.uppercase() }} Analytics Brief",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(reportState.content))
                                    Toast.makeText(context, "Copied Briefing to Clipboard", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Report")
                            }
                            IconButton(onClick = onReset) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset Board")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            MarkdownText(text = reportState.content)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MockupDrinkReport(
    visits: List<ClientVisit>,
    drinks: List<DrinkItem>,
    interval: com.example.api.ReportInterval,
    onReset: () -> Unit
) {
    val now = System.currentTimeMillis()
    val limitMs = when (interval) {
        com.example.api.ReportInterval.DAILY -> 24 * 60 * 60 * 1000L
        com.example.api.ReportInterval.WEEKLY -> 7 * 24 * 60 * 60 * 1000L
        com.example.api.ReportInterval.MONTHLY -> 30 * 24 * 60 * 60 * 1000L
    }

    val filteredVisits = visits.filter { now - it.orderTime <= limitMs }
    val revenue = filteredVisits.sumOf { it.totalPrice }
    val count = filteredVisits.size
    val warnings = drinks.filter { it.stock <= it.minThreshold }

    // Category insights
    val salesByDrink = filteredVisits.groupBy { it.drinkName }
        .mapValues { entry -> entry.value.sumOf { it.qty } }
    val bestSeller = salesByDrink.maxByOrNull { it.value }?.key ?: "None"
    val bestSellerQty = salesByDrink.maxByOrNull { it.value }?.value ?: 0

    val intervalName = when (interval) {
        com.example.api.ReportInterval.DAILY -> "Daily"
        com.example.api.ReportInterval.WEEKLY -> "Weekly"
        com.example.api.ReportInterval.MONTHLY -> "Monthly"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 80.dp)
    ) {
        Text(
            text = "PREVIEW $intervalName REPORT BRIEF (Offline Generated)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "☕ WORKSPACE BREW BRIEFING ($intervalName)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "### 1. $intervalName Business Summary\n" +
                           "Over the selected timeframe ($intervalName), the café accomplished **$count client visits**, selling a total of **${filteredVisits.sumOf { it.qty }} drinks**, generating **฿${String.format(java.util.Locale.US, "%.2f", revenue)}** in gross sales.\n\n" +
                           "• **Best-selling item:** $bestSeller ($bestSellerQty units sold)\n" +
                           "• **Busiest logged segment:** Active walk-ins remain healthy.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "### 2. Stock Alerts & Forecasting\n" +
                           "- ระบบการนับสต็อกถูกยกเลิก (ไม่มีขีดจำกัดประเภทหรือจำนวนส่วนผสม)\n" +
                           "- Stock counting is deactivated. Inventory levels are assumed infinite.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "### 3. Key Operational Feedback & Trends\n" +
                           let {
                               val feedBackNotes = filteredVisits.mapNotNull { it.notes.takeIf { n -> n.isNotBlank() } }
                               if (feedBackNotes.isEmpty()) {
                                   "- No specialized modifier request trends logged in this period."
                               } else {
                                   feedBackNotes.take(3).joinToString("\n") { note -> "- \"$note\"" }
                               }
                           },
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "### 4. Strategic Recommendations\n" +
                           when (interval) {
                               com.example.api.ReportInterval.DAILY -> {
                                   "- **Barista Priority:** Refill ingredients and ice templates for $bestSeller.\n" +
                                   "- **Active Shift Focus:** Monitor custom requests for substitute items to update barista menus.\n" +
                                   "- **Restock Alert:** Target lowest threshold drinks for immediate ordering."
                               }
                               com.example.api.ReportInterval.WEEKLY -> {
                                   "- **Weekly Inventory Plan:** Coordinate scheduled ingredient arrivals for low stock menu items.\n" +
                                   "- **Promotional Bundle:** Combine $bestSeller with custom modifiers as a signature special.\n" +
                                   "- **Staffing Slots:** Peak periods suggest optimized staffing during peak walk-in frequencies."
                               }
                               com.example.api.ReportInterval.MONTHLY -> {
                                   "- **Menu Optimization:** Review low sales category margins to consider refreshing your beverage catalog.\n" +
                                   "- **Supplier Level Optimization:** Negotiate bulk pricing contracts for your highest volume beverage Base ingredients.\n" +
                                   "- **Loyalty Strategy:** Target repeating customers for structured custom modifiers and seasonal blends."
                               }
                           },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

// Custom Markdown Renderer
@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val lines = text.split("\n")
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### "),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## "),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                trimmed.startsWith("# ") -> {
                    Text(
                        text = trimmed.removePrefix("# "),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        val content = trimmed.removePrefix("- ").removePrefix("* ")
                            .replace("**", "") // simple strip bold marks
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                trimmed.isNotBlank() -> {
                    val styledText = trimmed.replace("**", "")
                    Text(
                        text = styledText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// empty state illustration
@Composable
fun EmptyStateWidget(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    buttonText: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(buttonText, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EditDrinkModal(
    drink: DrinkItem,
    onDismiss: () -> Unit,
    onConfirm: (DrinkItem) -> Unit
) {
    var name by remember { mutableStateOf(drink.name) }
    var priceStr by remember { mutableStateOf(drink.price.toString()) }
    var category by remember { mutableStateOf(drink.category) }
    
    val categories = listOf("Coffee (กาแฟ)", "Tea (ชา)", "Specialty (เมนูพิเศษ)", "Cold Brew (ดีคอฟ/สกัดเย็น)", "Bakery (เบเกอรี่)")
    var selectedColorHex by remember { mutableStateOf(drink.colorHex) }
    val colors = listOf("#8D6E63", "#43A047", "#00ACC1", "#E53935", "#FB8C00", "#5E35B1")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "แก้ไขเมนูเครื่องดื่ม / Edit Menu Drink",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ชื่อเครื่องดื่ม / Drink Name") },
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_drink_name"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("ราคา (฿) / Price") },
                        modifier = Modifier.fillMaxWidth().testTag("input_edit_drink_price"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category selector
                Text("หมวดหมู่ / Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                // Color Selection
                Text("สีธีมเมนู / Menu Color Theme", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(
                                    width = if (selectedColorHex == hex) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("ยกเลิก / Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val price = priceStr.toDoubleOrNull() ?: drink.price
                            val stock = 9999
                            val threshold = 0
                            if (name.isNotBlank()) {
                                onConfirm(drink.copy(
                                    name = name,
                                    price = price,
                                    stock = stock,
                                    minThreshold = threshold,
                                    category = category,
                                    colorHex = selectedColorHex
                                ))
                            }
                        },
                        enabled = name.isNotBlank() && priceStr.isNotBlank(),
                        modifier = Modifier.testTag("btn_update_drink")
                    ) {
                        Text("บันทึก / Save Changes")
                    }
                }
            }
        }
    }
}

// dialog additions
@Composable
fun AddDrinkModal(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int, Int, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Coffee (กาแฟ)") }
    
    val categories = listOf("Coffee (กาแฟ)", "Tea (ชา)", "Specialty (เมนูพิเศษ)", "Cold Brew (ดีคอฟ/สกัดเย็น)", "Bakery (เบเกอรี่)")
    var selectedColorHex by remember { mutableStateOf("#8D6E63") }
    val colors = listOf("#8D6E63", "#43A047", "#00ACC1", "#E53935", "#FB8C00", "#5E35B1")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "เพิ่มเมนูเครื่องดื่ม / Add Menu Drink",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ชื่อเครื่องดื่ม / Drink Name") },
                    modifier = Modifier.fillMaxWidth().testTag("input_drink_name"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("ราคา (฿) / Price") },
                        modifier = Modifier.fillMaxWidth().testTag("input_drink_price"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category selector
                Text("หมวดหมู่ / Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                // Color Selection
                Text("สีธีมเมนู / Menu Color Theme", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(
                                    width = if (selectedColorHex == hex) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("ยกเลิก / Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val price = priceStr.toDoubleOrNull() ?: 3.99
                            val stock = 9999
                            val threshold = 0
                            if (name.isNotBlank()) {
                                onConfirm(name, price, stock, threshold, category, selectedColorHex)
                            }
                        },
                        enabled = name.isNotBlank() && priceStr.isNotBlank(),
                        modifier = Modifier.testTag("btn_save_drink")
                    ) {
                        Text("บันทึก / Save Drink")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogVisitModal(
    drinks: List<DrinkItem>,
    onDismiss: () -> Unit,
    onConfirm: (String, DrinkItem, Int, String, Int) -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var selectedDrink by remember(drinks) { mutableStateOf(drinks.firstOrNull() ?: DrinkItem(id = -1, name = "No Drink Selected", price = 0.0, stock = 0)) }
    var qty by remember { mutableStateOf(1) }
    var notes by remember { mutableStateOf("") }
    var selectedDiscount by remember { mutableStateOf(0) }

    var expandedMenu by remember { mutableStateOf(false) }

    val discountOptions = listOf(0, 5, 10, 15, 20, 25)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "บันทึกคำสั่งซื้อ / Log Order",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("ลูกค้า หรือ โต๊ะ / Customer or Table") },
                    placeholder = { Text("เช่น โต๊ะ 4 / Sarah") },
                    modifier = Modifier.fillMaxWidth().testTag("input_client_name"),
                    singleLine = true
                )

                // Select Menu Drink dropdown
                Text("เลือกเครื่องดื่ม / Select Drink", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        onClick = { expandedMenu = true },
                        modifier = Modifier.fillMaxWidth().testTag("select_drink_dropdown"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "เมนูที่เลือก / Selected Menu",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${selectedDrink.name} (฿${selectedDrink.price})",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = if (expandedMenu) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Toggle menu selection"
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false },
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        drinks.forEach { drink ->
                            DropdownMenuItem(
                                text = { Text("${drink.name} (ราคา: ฿${drink.price})") },
                                onClick = {
                                    selectedDrink = drink
                                    expandedMenu = false
                                }
                            )
                        }
                    }
                }

                // Counter Box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("จำนวนแก้ว / Quantity", fontWeight = FontWeight.Bold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (qty > 1) qty-- },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease qty")
                        }
                        Text(
                            text = "$qty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(
                            onClick = { qty++ },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase qty", tint = Color.White)
                        }
                    }
                }

                // Discount Selector
                Text("เลือกส่วนลดโปรโมชั่น / Select Discount", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    discountOptions.forEach { pct ->
                        val label = if (pct == 0) "ไม่มี / None" else "$pct%"
                        FilterChip(
                            selected = selectedDiscount == pct,
                            onClick = { selectedDiscount = pct },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (pct > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = if (pct > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("discount_chip_$pct")
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("หมายเหตุจากบาริสต้า (ระบุหรือไม่ก็ได้) / Notes") },
                    placeholder = { Text("เช่น หวานน้อย, เพิ่มช็อตกาแฟ, แยกน้ำแข็ง ฯลฯ") },
                    modifier = Modifier.fillMaxWidth().testTag("input_visit_notes"),
                    maxLines = 2
                )

                // Live dynamic checkout bill breakdown panel
                val originalTotal = selectedDrink.price * qty
                val savings = originalTotal * (selectedDiscount / 100.0)
                val finalTotal = originalTotal - savings

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ราคารวม / Subtotal:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("฿${String.format(java.util.Locale.US, "%.2f", originalTotal)}", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (selectedDiscount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ส่วนลด / Discount ($selectedDiscount%):", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                                Text("-฿${String.format(java.util.Locale.US, "%.2f", savings)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("รวมสุทธิ / Total:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("฿${String.format(java.util.Locale.US, "%.2f", finalTotal)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("ยกเลิก / Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onConfirm(clientName.ifBlank { "Guest Client" }, selectedDrink, qty, notes, selectedDiscount)
                        },
                        modifier = Modifier.testTag("btn_save_visit")
                    ) {
                        Text("บันทึกออเดอร์ / Log Order")
                    }
                }
            }
        }
    }
}

// Simple M3 Icons fallback structure helper to avoid icon resolution crashes
object Symbols {
    val Warning = Icons.Default.Warning
}

fun parseHexColor(hex: String, fallback: Color = Color(0xFF8D6E63)): Color {
    return try {
        val cleaned = hex.removePrefix("#")
        val longVal = cleaned.toLong(16)
        when (cleaned.length) {
            6 -> Color(longVal or 0xFF000000)
            8 -> Color(longVal)
            else -> fallback
        }
    } catch (e: Exception) {
        fallback
    }
}


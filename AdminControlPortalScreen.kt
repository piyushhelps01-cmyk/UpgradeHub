package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.AdminAuditLog
import com.example.data.model.AdminPermissions
import com.example.data.model.AdminStudyMaterial
import com.example.data.model.StudentCohortProfile
import com.example.ui.viewmodel.JeePage
import com.example.ui.viewmodel.JeeViewModel
import com.example.ui.viewmodel.TeamAnnouncement

@Composable
fun AdminControlPortalScreen(
    viewModel: JeeViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val adminPermissions by viewModel.adminPermissions.collectAsState()
    val adminAuditLogs by viewModel.adminAuditLogs.collectAsState()
    val uploadedMaterials by viewModel.uploadedMaterials.collectAsState()
    val studentCohortList by viewModel.studentCohortList.collectAsState()
    val teamAnnouncements by viewModel.teamAnnouncements.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var adminAccessGranted by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, adminAccessGranted) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && adminAccessGranted) {
                adminAccessGranted = false
                viewModel.revokeAdminAccess()
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose { lifecycleOwner.lifecycle.removeObserver(lifecycleObserver) }
    }

    // 0: Announcements Broadcast, 1: Upload Study Material, 2: Student Cohort Data, 3: Admin Roles & Controls

    if (!adminAccessGranted || !viewModel.isCurrentUserAdmin()) {
        AdminAccessLockGateView(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onAccessGranted = { adminAccessGranted = true }
        )
        return
    }

    val isAdmin = currentUser?.role.equals("ADMIN", ignoreCase = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP APP BAR
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("admin_portal_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ADMIN CONTROL PORTAL",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isAdmin) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF0070F3).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isAdmin) "VERIFIED ADMIN" else "STUDENT",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isAdmin) Color(0xFFDC2626) else Color(0xFF0070F3),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "MFA-backed session • Admin workspace preview • Server publishing not connected",
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Secure Lock & Exit Admin Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clickable { viewModel.revokeAdminAccess() }
                            .testTag("admin_role_switch_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Lock & Exit Admin",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4 Mini Metric Overview Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniStatChip(
                        label = "Aspirants",
                        value = "${studentCohortList.size}",
                        color = Color(0xFF0070F3),
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatChip(
                        label = "Broadcasts",
                        value = "${teamAnnouncements.size}",
                        color = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatChip(
                        label = "Materials",
                        value = "${uploadedMaterials.size}",
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatChip(
                        label = "Avg Score",
                        value = "${studentCohortList.map { it.mockTestAvgScore }.average().toInt()}/300",
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1.1f)
                    )
                }
            }
        }

        // TABS NAVIGATION
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFF0070F3),
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Broadcasts", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("admin_tab_broadcasts")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Uploads", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("admin_tab_uploads")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Students", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("admin_tab_students")
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Controls", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("admin_tab_controls")
            )
        }

        // TAB CONTENT
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> AdminBroadcastStudioTab(
                    viewModel = viewModel,
                    announcements = teamAnnouncements,
                    canBroadcast = adminPermissions.canBroadcastAnnouncements
                )
                1 -> AdminMaterialUploadCenterTab(
                    viewModel = viewModel,
                    materials = uploadedMaterials,
                    canUpload = adminPermissions.canUploadMaterial
                )
                2 -> AdminStudentCohortRosterTab(
                    viewModel = viewModel,
                    students = studentCohortList,
                    canViewData = adminPermissions.canViewAllStudentData
                )
                3 -> AdminGovernanceControlsTab(
                    viewModel = viewModel,
                    permissions = adminPermissions,
                    auditLogs = adminAuditLogs,
                    canManageRoles = adminPermissions.canManageRoles
                )
            }
        }
    }
}

@Composable
private fun MiniStatChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

// =============================================================================
// TAB 0: ANNOUNCEMENTS & BROADCAST STUDIO
// =============================================================================
@Composable
private fun AdminBroadcastStudioTab(
    viewModel: JeeViewModel,
    announcements: List<TeamAnnouncement>,
    canBroadcast: Boolean
) {
    var title by remember { mutableStateOf("") }
    var messageBody by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("URGENT") }
    var targetBatch by remember { mutableStateOf("All Batches") }
    var isPinned by remember { mutableStateOf(true) }
    var sendNotificationAlert by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Composer Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_announcement_composer_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Broadcast Official Announcement", fontSize = 13.5.sp, fontWeight = FontWeight.Black)
                                Text("Broadcasts to all enrolled student devices instantly", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (!canBroadcast) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF44336).copy(alpha = 0.15f)
                            ) {
                                Text("LOCKED", fontSize = 8.5.sp, fontWeight = FontWeight.Black, color = Color(0xFFD32F2F), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Announcement Headline", fontSize = 11.sp) },
                        placeholder = { Text("e.g. NTA JEE Main 2026 Test Schedule Published", fontSize = 11.sp) },
                        singleLine = true,
                        enabled = canBroadcast,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_announcement_title_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Message Body Input
                    OutlinedTextField(
                        value = messageBody,
                        onValueChange = { messageBody = it },
                        label = { Text("Announcement Content & Instructions", fontSize = 11.sp) },
                        placeholder = { Text("Type detailed broadcast instructions for students...", fontSize = 11.sp) },
                        minLines = 3,
                        enabled = canBroadcast,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_announcement_message_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Chips
                    Text("PRIORITY CATEGORY", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF0070F3), letterSpacing = 0.5.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "URGENT" to Color(0xFFEF4444),
                            "EXAM ALERT" to Color(0xFFF59E0B),
                            "BATCH UPDATE" to Color(0xFF0070F3),
                            "REWARD & CONTEST" to Color(0xFF10B981),
                            "SYSTEM NOTICE" to Color(0xFF6B7280)
                        ).forEach { (cat, col) ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 9.5.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = col.copy(alpha = 0.2f),
                                    selectedLabelColor = col
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Target Batch Selection
                    Text("TARGET AUDIENCE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF0070F3), letterSpacing = 0.5.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "All Batches",
                            "Lakshya JEE 2026 (11th)",
                            "Arjuna JEE 2025 (12th)",
                            "Prayas Droppers"
                        ).forEach { batch ->
                            FilterChip(
                                selected = targetBatch == batch,
                                onClick = { targetBatch = batch },
                                label = { Text(batch, fontSize = 9.5.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = isPinned,
                                onCheckedChange = { isPinned = it },
                                enabled = canBroadcast,
                                modifier = Modifier.testTag("admin_pin_toggle")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pin on Student Feed", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = sendNotificationAlert,
                                onCheckedChange = { sendNotificationAlert = it },
                                enabled = canBroadcast,
                                modifier = Modifier.testTag("admin_push_toggle")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Push Alarm", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.postAdminBroadcastAnnouncement(
                                title = title,
                                message = messageBody,
                                category = selectedCategory,
                                isPinned = isPinned,
                                sendImmediatePush = sendNotificationAlert,
                                targetBatch = targetBatch
                            )
                            title = ""
                            messageBody = ""
                        },
                        enabled = canBroadcast && title.isNotBlank() && messageBody.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_publish_announcement_btn")
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Broadcast to Students Now", fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE BROADCASTS (${announcements.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { viewModel.markAllAnnouncementsRead() }) {
                    Text("Mark All Read", fontSize = 10.sp)
                }
            }
        }

        items(announcements, key = { it.id }) { ann ->
            AnnouncementManageCard(
                announcement = ann,
                onTogglePin = { viewModel.togglePinAnnouncement(ann.id) },
                onDelete = { viewModel.deleteAnnouncement(ann.id) }
            )
        }
    }
}

@Composable
private fun AnnouncementManageCard(
    announcement: TeamAnnouncement,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (announcement.category) {
        "URGENT" -> Color(0xFFEF4444)
        "EXAM ALERT" -> Color(0xFFF59E0B)
        "BATCH UPDATE" -> Color(0xFF0070F3)
        "REWARD & CONTEST" -> Color(0xFF10B981)
        else -> Color(0xFF6B7280)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (announcement.isPinned) categoryColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = announcement.category,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (announcement.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "PINNED",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (announcement.isPinned) Icons.Default.PushPin else Icons.Default.Pin,
                            contentDescription = "Pin/Unpin",
                            tint = if (announcement.isPinned) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = announcement.title, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = announcement.message, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${announcement.authorName} • ${announcement.timestamp}",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "✓ Acknowledged by ${announcement.acknowledgementCount} students",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}

// =============================================================================
// TAB 1: STUDY MATERIAL UPLOAD CENTER ("HOW UPLOAD ANYTHING")
// =============================================================================
@Composable
private fun AdminMaterialUploadCenterTab(
    viewModel: JeeViewModel,
    materials: List<AdminStudyMaterial>,
    canUpload: Boolean
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Physics") }
    var selectedType by remember { mutableStateOf("DPP Sheet") }
    var selectedBatch by remember { mutableStateOf("All Batches") }
    var simulatedSizeMb by remember { mutableStateOf(2.5f) }
    var tagsInput by remember { mutableStateOf("JEE Advanced, High Yield") }
    var searchQuery by remember { mutableStateOf("") }
    var filterSubject by remember { mutableStateOf("All") }

    val filteredMaterials = remember(materials, searchQuery, filterSubject) {
        materials.filter { mat ->
            val matchesQuery = searchQuery.isBlank() ||
                mat.title.contains(searchQuery, ignoreCase = true) ||
                mat.description.contains(searchQuery, ignoreCase = true)
            val matchesSubject = filterSubject == "All" || mat.subject.equals(filterSubject, ignoreCase = true)
            matchesQuery && matchesSubject
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Upload Form Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, Color(0xFF0070F3).copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_upload_material_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF0070F3).copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF0070F3), modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Publish Study Material & Content", fontSize = 13.5.sp, fontWeight = FontWeight.Black)
                                Text("Upload DPPs, Notes, Mock Test Papers & Handouts", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (!canUpload) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF44336).copy(alpha = 0.15f)
                            ) {
                                Text("RESTRICTED", fontSize = 8.5.sp, fontWeight = FontWeight.Black, color = Color(0xFFD32F2F), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Material Title", fontSize = 11.sp) },
                        placeholder = { Text("e.g. Modern Physics Photoelectric Effect DPP #3", fontSize = 11.sp) },
                        singleLine = true,
                        enabled = canUpload,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_material_title_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Instructions", fontSize = 11.sp) },
                        placeholder = { Text("Explain topics covered, derivations, or problem count...", fontSize = 11.sp) },
                        minLines = 2,
                        enabled = canUpload,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_material_desc_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subject Selector
                    Text("SELECT SUBJECT", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF0070F3), letterSpacing = 0.5.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Physics", "Chemistry", "Mathematics", "Multi-Subject").forEach { sub ->
                            FilterChip(
                                selected = selectedSubject == sub,
                                onClick = { selectedSubject = sub },
                                label = { Text(sub, fontSize = 9.5.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Material Type Selector
                    Text("MATERIAL FORMAT", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF0070F3), letterSpacing = 0.5.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("DPP Sheet", "Handwritten Notes", "Mock Question Paper", "Formula Handout", "NCERT Revision Sheet").forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type, fontSize = 9.5.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Target Batch
                    Text("ASSIGN TO BATCH", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF0070F3), letterSpacing = 0.5.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All Batches", "Lakshya JEE 2026 (11th)", "Arjuna JEE 2025 (12th)", "Prayas Droppers").forEach { batch ->
                            FilterChip(
                                selected = selectedBatch == batch,
                                onClick = { selectedBatch = batch },
                                label = { Text(batch, fontSize = 9.5.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated File Attachment Details
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF0070F3), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Simulated PDF/Doc Payload", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Auto-generates high-yield study package for mobile offline sync", fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0070F3).copy(alpha = 0.12f)
                            ) {
                                Text("${simulatedSizeMb} MB", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF0070F3), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val tagsList = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            viewModel.uploadStudyMaterial(
                                title = title,
                                description = description,
                                subject = selectedSubject,
                                materialType = selectedType,
                                targetBatch = selectedBatch,
                                fileSizeMb = simulatedSizeMb,
                                tags = tagsList
                            )
                            title = ""
                            description = ""
                        },
                        enabled = canUpload && title.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0070F3),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_upload_submit_btn")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload & Publish to Students", fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Search & Filter Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search materials by title or keyword...", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("All", "Physics", "Chemistry", "Mathematics", "Multi-Subject").forEach { sub ->
                        FilterChip(
                            selected = filterSubject == sub,
                            onClick = { filterSubject = sub },
                            label = { Text(sub, fontSize = 9.5.sp) }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "PUBLISHED ACADEMIC REPOSITORY (${filteredMaterials.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(filteredMaterials, key = { it.id }) { mat ->
            MaterialItemCard(
                material = mat,
                onDownload = { viewModel.incrementMaterialDownload(mat.id) },
                onDelete = { viewModel.deleteStudyMaterial(mat.id) }
            )
        }
    }
}

@Composable
private fun MaterialItemCard(
    material: AdminStudyMaterial,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val subjectColor = when (material.subject) {
        "Physics" -> Color(0xFF0070F3)
        "Chemistry" -> Color(0xFF10B981)
        "Mathematics" -> Color(0xFF8B5CF6)
        else -> Color(0xFFF59E0B)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = subjectColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "${material.subject.uppercase()} • ${material.materialType.uppercase()}",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = subjectColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = material.targetBatch,
                            fontSize = 8.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = material.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = material.description, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)

            Spacer(modifier = Modifier.height(8.dp))

            // Tags
            if (material.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    material.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Text(text = "#$tag", fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${material.uploaderName} • ${material.uploadedAt} • ${material.fileSizeMb} MB",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(7.dp),
                    color = Color(0xFF0070F3).copy(alpha = 0.12f),
                    modifier = Modifier.clickable { onDownload() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF0070F3), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${material.downloadCount} Downloads",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0070F3)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 2: STUDENT COHORT ROSTER & DATA INSPECTOR ("SEEING DATA OF ALL STUDENTS")
// =============================================================================
@Composable
private fun AdminStudentCohortRosterTab(
    viewModel: JeeViewModel,
    students: List<StudentCohortProfile>,
    canViewData: Boolean
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedBatchFilter by remember { mutableStateOf("All Batches") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var expandedStudentId by remember { mutableStateOf<String?>(null) }

    val filteredStudents = remember(students, searchQuery, selectedBatchFilter, selectedStatusFilter) {
        students.filter { std ->
            val matchesQuery = searchQuery.isBlank() ||
                std.name.contains(searchQuery, ignoreCase = true) ||
                std.email.contains(searchQuery, ignoreCase = true) ||
                std.targetCollege.contains(searchQuery, ignoreCase = true)
            val matchesBatch = selectedBatchFilter == "All Batches" || std.batch.equals(selectedBatchFilter, ignoreCase = true)
            val matchesStatus = selectedStatusFilter == "All" || std.performanceStatus == selectedStatusFilter
            matchesQuery && matchesBatch && matchesStatus
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Cohort Analytics Header
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Analytics, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Student Cohort Intelligence", fontSize = 13.5.sp, fontWeight = FontWeight.Black)
                                Text("Comprehensive roster tracking hours, streaks, scores & syllabus", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Export Report Button
                        Button(
                            onClick = {
                                val report = viewModel.exportCohortReportSummary()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Cohort Report", report)
                                clipboard.setPrimaryClip(clip)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                            modifier = Modifier.testTag("admin_export_cohort_report_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export Summary", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0070F3).copy(alpha = 0.08f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Enrolled", fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${students.size}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF0070F3))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.08f),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Avg Study Hours", fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${"%.1f".format(students.map { it.studyHours }.average())}h", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.08f),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Avg Habit Streak", fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${"%.0f".format(students.map { it.streakDays }.average())} Days", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF8B5CF6).copy(alpha = 0.08f),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Top Performers", fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${students.count { it.performanceStatus == "TOP_PERFORMER" }} Students", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF8B5CF6))
                            }
                        }
                    }
                }
            }
        }

        // Search & Filters
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by student name, email or college...", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_student_search_input")
                )

                // Batch Filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("All Batches", "Lakshya JEE 2026 (11th)", "Arjuna JEE 2025 (12th)", "Prayas Droppers").forEach { b ->
                        FilterChip(
                            selected = selectedBatchFilter == b,
                            onClick = { selectedBatchFilter = b },
                            label = { Text(b, fontSize = 9.5.sp) }
                        )
                    }
                }

                // Status Filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "All" to "All Status",
                        "TOP_PERFORMER" to "Top Performers",
                        "ON_TRACK" to "On Track",
                        "NEEDS_ATTENTION" to "Needs Attention",
                        "NEW_ASPIRANT" to "New Aspirants"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedStatusFilter == key,
                            onClick = { selectedStatusFilter = key },
                            label = { Text(label, fontSize = 9.5.sp) }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "STUDENT ROSTER (${filteredStudents.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(filteredStudents, key = { it.id }) { std ->
            StudentRosterCard(
                student = std,
                isExpanded = expandedStudentId == std.id,
                onToggleExpand = {
                    expandedStudentId = if (expandedStudentId == std.id) null else std.id
                },
                onSendNotice = {
                    viewModel.showSnackbar("📢 Direct nudge dispatched to ${std.name} (${std.email})")
                }
            )
        }
    }
}

@Composable
private fun StudentRosterCard(
    student: StudentCohortProfile,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSendNotice: () -> Unit
) {
    val statusColor = when (student.performanceStatus) {
        "TOP_PERFORMER" -> Color(0xFF10B981)
        "ON_TRACK" -> Color(0xFF0070F3)
        "NEEDS_ATTENTION" -> Color(0xFFEF4444)
        else -> Color(0xFFF59E0B)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isExpanded) Color(0xFF0070F3).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .testTag("admin_student_card_${student.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Student Overview Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = Color(student.avatarColor),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = student.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = student.name, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = statusColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = student.performanceStatus.replace("_", " "),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                        Text(text = "${student.batch} • ${student.targetCollege}", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StudentMetricPill("Hours", "${student.studyHours}h", Color(0xFF0070F3), Modifier.weight(1f))
                StudentMetricPill("Streak", "${student.streakDays}d", Color(0xFFF59E0B), Modifier.weight(1f))
                StudentMetricPill("Syllabus", "${student.syllabusCompletionPct}%", Color(0xFF10B981), Modifier.weight(1f))
                StudentMetricPill("Score", "${student.mockTestAvgScore}/300", Color(0xFF8B5CF6), Modifier.weight(1.1f))
                StudentMetricPill("Accuracy", "${student.accuracyPct}%", Color(0xFF0070F3), Modifier.weight(1f))
            }

            // EXPANDED DETAILS ACCORDION
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("DETAILED ACADEMIC PROGRESS & DRILL ADHERENCE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF0070F3), letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Subject bars
                    SubjectProgressBar("Physics Mastery", student.physicsPct, Color(0xFF0070F3))
                    Spacer(modifier = Modifier.height(4.dp))
                    SubjectProgressBar("Chemistry Mastery", student.chemistryPct, Color(0xFF10B981))
                    Spacer(modifier = Modifier.height(4.dp))
                    SubjectProgressBar("Mathematics Mastery", student.mathsPct, Color(0xFF8B5CF6))

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contact & Meta details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Email: ${student.email}", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Phone: ${student.contactNumber}", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Slot Compliance: ${student.slotCompliancePct}%", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Text("Enrolled: ${student.joinedDate}", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Nudge Button
                    Button(
                        onClick = onSendNotice,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Direct Academic Notice / Nudge", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentMetricPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 10.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
private fun SubjectProgressBar(label: String, pct: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$pct%", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = color)
    }
    Spacer(modifier = Modifier.height(2.dp))
    LinearProgressIndicator(
        progress = { pct / 100f },
        color = color,
        trackColor = color.copy(alpha = 0.15f),
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp))
    )
}

// =============================================================================
// TAB 3: ADMIN GOVERNANCE & CONTROLS ("HOW TO CONTROL ADMINS")
// =============================================================================
@Composable
private fun AdminGovernanceControlsTab(
    viewModel: JeeViewModel,
    permissions: AdminPermissions,
    auditLogs: List<AdminAuditLog>,
    canManageRoles: Boolean
) {
    var allowUploads by remember(permissions) { mutableStateOf(permissions.canUploadMaterial) }
    var allowBroadcasts by remember(permissions) { mutableStateOf(permissions.canBroadcastAnnouncements) }
    var allowStudentData by remember(permissions) { mutableStateOf(permissions.canViewAllStudentData) }
    var allowDeletions by remember(permissions) { mutableStateOf(permissions.canDeleteContent) }
    var allowRoleManagement by remember(permissions) { mutableStateOf(permissions.canManageRoles) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Permissions Control Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_governance_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Admin Role & Feature Permissions", fontSize = 13.5.sp, fontWeight = FontWeight.Black)
                                Text("Configure what admins and faculty can upload, post, or view", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Text("RBAC ACTIVE", fontSize = 8.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    PermissionToggleRow(
                        title = "Allow Study Material & DPP Uploads",
                        description = "Enables publishing PDFs, DPPs, and formula handouts to student feeds",
                        checked = allowUploads,
                        onCheckedChange = { allowUploads = it },
                        enabled = canManageRoles,
                        testTag = "toggle_perm_upload"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                    PermissionToggleRow(
                        title = "Allow Official Broadcast Announcements",
                        description = "Enables publishing announcements and triggering instant push notifications",
                        checked = allowBroadcasts,
                        onCheckedChange = { allowBroadcasts = it },
                        enabled = canManageRoles,
                        testTag = "toggle_perm_broadcast"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                    PermissionToggleRow(
                        title = "Allow Viewing All Student Personal Data",
                        description = "Grants access to the full student cohort roster, scores, and contacts",
                        checked = allowStudentData,
                        onCheckedChange = { allowStudentData = it },
                        enabled = canManageRoles,
                        testTag = "toggle_perm_student_data"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                    PermissionToggleRow(
                        title = "Allow Deleting Content & Demands",
                        description = "Enables purging materials, dismissing announcements, or moderating posts",
                        checked = allowDeletions,
                        onCheckedChange = { allowDeletions = it },
                        enabled = canManageRoles,
                        testTag = "toggle_perm_deletions"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.updateAdminPermissions(
                                AdminPermissions(
                                    canBroadcastAnnouncements = allowBroadcasts,
                                    canUploadMaterial = allowUploads,
                                    canViewAllStudentData = allowStudentData,
                                    canManageRoles = allowRoleManagement,
                                    canDeleteContent = allowDeletions
                                )
                            )
                        },
                        enabled = canManageRoles,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_save_permissions_btn")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Local Preview Preferences", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            // Privileged accounts are provisioned out of band by trusted Firebase Admin SDK tooling.
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "ADMIN ACCESS IS PROVISIONED OUTSIDE THE APP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0070F3),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Firebase custom claims and MFA determine access. This screen does not maintain an admin directory or grant roles. Its content controls are local previews until trusted backend publishing is connected.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            // Audit Log
            Text(
                text = "ADMIN AUDIT LOG & COMPLIANCE TRAIL (${auditLogs.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(auditLogs, key = { it.id }) { log ->
            AuditLogCard(log = log)
        }
    }
}

@Composable
private fun PermissionToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(description, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun AuditLogCard(log: AdminAuditLog) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF0070F3), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = log.action, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF0070F3))
                    Text(text = log.timestamp, fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(text = log.details, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "Triggered by: ${log.adminName}", fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AdminAccessLockGateView(
    viewModel: JeeViewModel,
    onNavigateBack: () -> Unit,
    onAccessGranted: () -> Unit
) {
    BackHandler { onNavigateBack() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? android.app.Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, Color(0xFFEF4444).copy(alpha = 0.45f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_access_lock_gate_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security Lock",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Admin Access",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Sign in with an approved Google account. Access requires server-authorized admin permissions and an MFA-backed Firebase session.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage.orEmpty(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isVerifying = true
                        errorMessage = null
                        if (activity == null) {
                            isVerifying = false
                            errorMessage = "Sign-in could not be started on this screen."
                        } else {
                            viewModel.signInWithGoogleCredential(activity) { signInResult ->
                                if (signInResult.isFailure) {
                                    isVerifying = false
                                    errorMessage = "Access could not be verified. Try again or return to the app."
                                } else {
                                    viewModel.verifyAdminAccess { verificationResult ->
                                        isVerifying = false
                                        if (verificationResult.getOrDefault(false)) {
                                            onAccessGranted()
                                        } else {
                                            errorMessage = "Access could not be verified for this session."
                                        }
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isVerifying,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verify_admin_session_btn")
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isVerifying) "Checking secure session..." else "Sign in & Verify",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Return to Student Dashboard", fontSize = 11.5.sp)
                }
            }
        }
    }
}

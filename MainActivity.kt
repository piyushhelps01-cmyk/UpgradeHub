package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ActiveMockDrillRingingBanner
import com.example.ui.components.AuthDialog
import com.example.ui.components.NotificationCenterDialog
import com.example.ui.components.OnboardingFlowDialog
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.SidebarDrawerContent
import com.example.ui.components.StudySlotAlarmManager
import com.example.ui.components.TermsConditionsDialog
import com.example.ui.screens.AdminControlPortalScreen
import com.example.ui.screens.AiScreen
import com.example.ui.screens.CampusHubScreen
import com.example.ui.screens.CbtTestScreen
import com.example.ui.screens.ChapterTrackerScreen
import com.example.ui.screens.ConnectedFlowScreen
import com.example.ui.screens.DailyPlannerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DataSettingsScreen
import com.example.ui.screens.DemandCentreScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.JeePrepHubPortalScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.MemesHubScreen
import com.example.ui.screens.NcertCommandScreen
import com.example.ui.screens.PracticeStackScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.ReferralRewardsScreen
import com.example.ui.screens.StreakAndFocusScreen
import com.example.ui.screens.StudyHoursScreen
import com.example.ui.screens.WeeklyReviewScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JeePage
import com.example.ui.viewmodel.JeeViewModel
import kotlinx.coroutines.launch

/**
 * 5 Logical Workspace Pillars that organize all 19 JEE Prep Hub features cleanly
 */
private enum class WorkspacePillar(
    val label: String,
    val icon: ImageVector,
    val defaultPage: JeePage,
    val subPages: List<JeePage>
) {
    COMMAND(
        label = "Home",
        icon = Icons.Default.Dashboard,
        defaultPage = JeePage.DASHBOARD,
        subPages = listOf(JeePage.DASHBOARD, JeePage.CONSUMER_UTILITY)
    ),
    STUDY_HUB(
        label = "Study Hub",
        icon = Icons.Default.Layers,
        defaultPage = JeePage.DAILY_PLANNER,
        subPages = listOf(
            JeePage.DAILY_PLANNER,
            JeePage.CHAPTERS,
            JeePage.NCERT,
            JeePage.BOOKMARKS,
            JeePage.FLOW
        )
    ),
    EXAM_ARENA(
        label = "Test Arena",
        icon = Icons.Default.Description,
        defaultPage = JeePage.TESTS,
        subPages = listOf(
            JeePage.TESTS,
            JeePage.PRACTICE,
            JeePage.STREAK
        )
    ),
    ANALYTICS(
        label = "Analytics",
        icon = Icons.Default.ShowChart,
        defaultPage = JeePage.PROGRESS,
        subPages = listOf(
            JeePage.PROGRESS,
            JeePage.HOURS,
            JeePage.WEEKLY,
            JeePage.SETTINGS
        )
    ),
    CAMPUS_AI(
        label = "AI & Campus",
        icon = Icons.Default.SmartToy,
        defaultPage = JeePage.AI,
        subPages = listOf(
            JeePage.AI,
            JeePage.MEMES_HUB,
            JeePage.CAMPUS_HUB,
            JeePage.LEADERBOARD,
            JeePage.EVENTS,
            JeePage.REFERRALS
        )
    )
}

private fun pillarForPage(page: JeePage): WorkspacePillar {
    return WorkspacePillar.entries.firstOrNull { page in it.subPages } ?: WorkspacePillar.COMMAND
}

private fun iconForPage(page: JeePage): ImageVector = when (page) {
    JeePage.DASHBOARD -> Icons.Default.Dashboard
    JeePage.PRACTICE -> Icons.Default.FitnessCenter
    JeePage.CAMPUS_HUB -> Icons.Default.AccountBalance
    JeePage.AI -> Icons.Default.SmartToy
    JeePage.PROGRESS -> Icons.Default.ShowChart
    JeePage.PROFILE -> Icons.Default.Person
    JeePage.LEADERBOARD -> Icons.Default.EmojiEvents
    JeePage.TESTS -> Icons.Default.Description
    JeePage.EVENTS -> Icons.Default.Event
    JeePage.STREAK -> Icons.Default.LocalFireDepartment
    JeePage.DEMAND_CENTRE -> Icons.Default.Lightbulb
    JeePage.DAILY_PLANNER -> Icons.Default.DateRange
    JeePage.CHAPTERS -> Icons.Default.CalendarMonth
    JeePage.NCERT -> Icons.AutoMirrored.Filled.MenuBook
    JeePage.HOURS -> Icons.Default.Schedule
    JeePage.WEEKLY -> Icons.Default.Timeline
    JeePage.FLOW -> Icons.Default.AutoAwesome
    JeePage.SETTINGS -> Icons.Default.Settings
    JeePage.BOOKMARKS -> Icons.Default.Layers
    JeePage.MEMES_HUB -> Icons.Default.EmojiEmotions
    JeePage.REFERRALS -> Icons.Default.Share
    JeePage.ADMIN_PORTAL -> Icons.Default.Security
    JeePage.CONSUMER_UTILITY -> Icons.Default.AutoAwesome
}

class MainActivity : ComponentActivity() {
    private val viewModel: JeeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase App Check with ReCaptcha / Play Store integrity attestation
        try {
            com.example.security.FirebaseAppCheckConfig.configureAppCheck(this, intent)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "App Check initialization failed", e)
        }

        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        // Configure Full-Screen Intent capabilities: turn screen on and display over keyguard/lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // Schedule WorkManager tasks for all active study slots
        try {
            com.example.work.StudySlotWorkScheduler.scheduleAllSlots(this)
        } catch (_: Exception) {
        }

        handleIncomingAlarmIntent(intent)

        setContent {
            var isDarkTheme by rememberSaveable(key = "dark_theme_v4") { mutableStateOf(true) }
            MyApplicationTheme(darkTheme = isDarkTheme) {
                JeeAppContent(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingAlarmIntent(intent)
    }

    private fun handleIncomingAlarmIntent(intent: android.content.Intent?) {
        if (intent?.action == "ACTION_STUDY_SLOT_ALARM") {
            val slotIndex = intent.getIntExtra("EXTRA_SLOT_INDEX", 0)
            val slotTitle = intent.getStringExtra("EXTRA_SLOT_TITLE") ?: "Study Slot ${slotIndex + 1}"
            val slotTime = intent.getStringExtra("EXTRA_SLOT_TIME") ?: ""
            StudySlotAlarmManager.ensureInitialized(this)
            StudySlotAlarmManager.startThirtySecondMockDrillAudio(this, slotIndex, slotTitle, slotTime)
        }
    }
}

@Composable
fun JeeAppContent(
    viewModel: JeeViewModel,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val habitStreakData by viewModel.habitStreakData.collectAsStateWithLifecycle()
    val selectedExamCategory by viewModel.selectedExamCategory.collectAsStateWithLifecycle()
    val batchTargetDays by viewModel.batchTargetDays.collectAsStateWithLifecycle()
    val teamAnnouncements by viewModel.teamAnnouncements.collectAsStateWithLifecycle()
    val isFocusTimerRunning by viewModel.isFocusTimerRunning.collectAsStateWithLifecycle()
    val focusRemainingSeconds by viewModel.focusRemainingSeconds.collectAsStateWithLifecycle()
    val focusPresetMinutes by viewModel.focusPresetMinutes.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var showAuthDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showOnboardingDialog by remember { mutableStateOf(false) }
    var showSpotlightCommandDialog by remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current
    val currentView = LocalView.current

    val triggerHaptic: () -> Unit = remember(currentView, hapticFeedback) {
        {
            try {
                currentView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } catch (_: Throwable) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    val allPages = remember {
        listOf(
            JeePage.DASHBOARD,
            JeePage.BOOKMARKS,
            JeePage.DAILY_PLANNER,
            JeePage.CHAPTERS,
            JeePage.NCERT,
            JeePage.FLOW,
            JeePage.TESTS,
            JeePage.PRACTICE,
            JeePage.STREAK,
            JeePage.PROGRESS,
            JeePage.HOURS,
            JeePage.WEEKLY,
            JeePage.AI,
            JeePage.MEMES_HUB,
            JeePage.CAMPUS_HUB,
            JeePage.LEADERBOARD,
            JeePage.EVENTS,
            JeePage.CONSUMER_UTILITY,
            JeePage.PROFILE,
            JeePage.SETTINGS
        )
    }

    val activePillar = remember(uiState.currentPage) {
        pillarForPage(uiState.currentPage)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    // Continuous 1-second Study Slot Alarm Monitor:
    // Automatically triggers Notification + 30-Second Full-Volume Mock Drill Audio when any customized slot time arrives
    LaunchedEffect(Unit) {
        val appCtx = currentView.context.applicationContext
        StudySlotAlarmManager.ensureInitialized(appCtx)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(appCtx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        while (true) {
            StudySlotAlarmManager.checkAndTriggerDueSlotAlarms(appCtx)
            kotlinx.coroutines.delay(1000L)
        }
    }

    BackHandler(enabled = drawerState.isOpen || uiState.currentPage != JeePage.DASHBOARD) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            triggerHaptic()
            viewModel.setPage(JeePage.DASHBOARD)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(320.dp)
            ) {
                SidebarDrawerContent(
                    viewModel = viewModel,
                    onNavigateToPage = { page ->
                        viewModel.setPage(page)
                    },
                    onOpenAuthDialog = { showAuthDialog = true },
                    onOpenPrivacyPolicy = { showPrivacyDialog = true },
                    onOpenTermsConditions = { showTermsDialog = true },
                    onOpenOnboarding = { showOnboardingDialog = true },
                    onOpenNotificationCenter = { showNotificationDialog = true },
                    onCloseDrawer = {
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    // 1. Clean Executive Studio Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Drawer Menu + Brand & Context Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable {
                                        triggerHaptic()
                                        coroutineScope.launch { drawerState.open() }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Open Navigation Menu",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .size(34.dp)
                                    .clickable { viewModel.setPage(JeePage.DASHBOARD) }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.upgrade_hub_logo),
                                    contentDescription = "Logo",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(9.dp))

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setPage(JeePage.DASHBOARD) }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (uiState.currentPage == JeePage.DASHBOARD) "UpgradeHub" else uiState.currentPage.title,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (uiState.currentPage == JeePage.DASHBOARD) {
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Surface(
                                            shape = RoundedCornerShape(5.dp),
                                            color = Color(0xFF0070F3).copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = if (selectedExamCategory == "All") "MULTI-EXAM" else selectedExamCategory.uppercase(),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF0070F3),
                                                maxLines = 1,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Sprint Day ${uiState.selectedDay}/$batchTargetDays • jeeprephub.co.in",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Right: Quick Streak Pill, Search, Theme Toggle, Notifications & Profile
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Streak & Focus Quick Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFF9500).copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, Color(0xFFFF9500).copy(alpha = 0.35f)),
                                modifier = Modifier.clickable {
                                    triggerHaptic()
                                    viewModel.setPage(JeePage.STREAK)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = Color(0xFFFF9500),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${habitStreakData.currentStreak.coerceAtLeast(12)}d",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFD97706)
                                    )
                                }
                            }

                            // Spotlight Search Icon
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { showSpotlightCommandDialog = true }
                                    .testTag("top_spotlight_search_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search & Navigation",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            // Theme Toggle Icon
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        triggerHaptic()
                                        onToggleDarkTheme()
                                    }
                                    .testTag("top_theme_toggle_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Theme",
                                        tint = if (isDarkTheme) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Unified Notifications & Announcements Hub Bell
                            val unreadAnnCount = teamAnnouncements.count { !it.isRead }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { showNotificationDialog = true }
                                    .testTag("top_team_announcements_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications & Announcements",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(if (unreadAnnCount > 0) 8.dp else 7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444))
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }

                            // User Profile Avatar
                            val initials = currentUser?.fullName
                                ?.split(" ")
                                ?.take(2)
                                ?.mapNotNull { it.firstOrNull()?.toString() }
                                ?.joinToString("") ?: "PK"

                            Surface(
                                shape = CircleShape,
                                color = Color(currentUser?.avatarColor ?: 0xFF0070F3),
                                border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                                modifier = Modifier
                                    .size(33.dp)
                                    .clickable {
                                        val target = if (uiState.currentPage == JeePage.PROFILE) JeePage.DASHBOARD else JeePage.PROFILE
                                        viewModel.setPage(target)
                                    }
                                    .testTag("top_profile_avatar_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = initials.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // 2. Contextual Workspace Sub-Tabs (Shown ONLY when inside a multi-module Pillar!)
                    if (activePillar.subPages.size > 1 || uiState.currentPage == JeePage.PROFILE || uiState.currentPage == JeePage.SETTINGS) {
                        val subTabs = if (uiState.currentPage == JeePage.PROFILE || uiState.currentPage == JeePage.SETTINGS) {
                            listOf(JeePage.PROFILE, JeePage.SETTINGS)
                        } else {
                            activePillar.subPages
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Back to Command Home button
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .clickable {
                                            triggerHaptic()
                                            viewModel.setPage(JeePage.DASHBOARD)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back to Home",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Home",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Contextual Sub-Navigation Pills for this Pillar
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    subTabs.forEach { subPage ->
                                        val isSelected = uiState.currentPage == subPage
                                        val bg by animateColorAsState(
                                            targetValue = if (isSelected) Color(0xFF0070F3)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                            label = "subTabBg"
                                        )
                                        val fg by animateColorAsState(
                                            targetValue = if (isSelected) Color.White
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            label = "subTabFg"
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = bg,
                                            modifier = Modifier
                                                .clickable {
                                                    triggerHaptic()
                                                    viewModel.setPage(subPage)
                                                }
                                                .testTag("module_dock_pill_${subPage.name.lowercase()}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = iconForPage(subPage),
                                                    contentDescription = null,
                                                    tint = fg,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = subPage.navLabel,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                    color = fg,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        )
                    }

                    // 3. Persistent Live Focus Timer Banner (Visible across all screens while timer is running)
                    if (isFocusTimerRunning && uiState.currentPage != JeePage.STREAK) {
                        val fMins = focusRemainingSeconds / 60
                        val fSecs = focusRemainingSeconds % 60
                        val fTime = String.format("%02d:%02d", fMins, fSecs)
                        Surface(
                            color = Color(0xFF0070F3).copy(alpha = 0.14f),
                            border = BorderStroke(0.5.dp, Color(0xFF0070F3).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setPage(JeePage.STREAK) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFF0070F3),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LIVE FOCUS SPRINT (${focusPresetMinutes}m): $fTime",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0070F3)
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF59E0B),
                                        modifier = Modifier.clickable { viewModel.toggleFocusTimer() }
                                    ) {
                                        Text(
                                            text = "Pause",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "Open Timer →",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0070F3)
                                    )
                                }
                            }
                        }
                    }

                    // Live 30-Second Full-Volume Mock Drill Ringing Banner
                    ActiveMockDrillRingingBanner()
                }
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    shadowElevation = 12.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(66.dp)
                    ) {
                        WorkspacePillar.entries.forEach { pillar ->
                            val isSelected = activePillar == pillar &&
                                uiState.currentPage != JeePage.PROFILE &&
                                uiState.currentPage != JeePage.SETTINGS

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    triggerHaptic()
                                    val target = if (pillar == activePillar) {
                                        pillar.defaultPage
                                    } else {
                                        pillar.defaultPage
                                    }
                                    viewModel.setPage(target)
                                },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            when (pillar) {
                                                WorkspacePillar.STUDY_HUB -> {
                                                    Badge(containerColor = Color(0xFF0070F3)) {
                                                        Text("HUB", fontSize = 7.5.sp, fontWeight = FontWeight.Black, color = Color.White)
                                                    }
                                                }
                                                WorkspacePillar.EXAM_ARENA -> {
                                                    Badge(containerColor = Color(0xFF10B981)) {
                                                        Text("CBT", fontSize = 7.5.sp, fontWeight = FontWeight.Black, color = Color.White)
                                                    }
                                                }
                                                WorkspacePillar.CAMPUS_AI -> {
                                                    Badge(containerColor = Color(0xFF8B5CF6)) {
                                                        Text("AI+", fontSize = 7.5.sp, fontWeight = FontWeight.Black, color = Color.White)
                                                    }
                                                }
                                                else -> {}
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = pillar.icon,
                                            contentDescription = pillar.label,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = pillar.label,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0070F3),
                                    selectedTextColor = Color(0xFF0070F3),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                    indicatorColor = Color(0xFF0070F3).copy(alpha = 0.14f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            // Full-bleed edge-to-edge screen container without cramped outer card wrappers
            AnimatedContent(
                targetState = uiState.currentPage,
                transitionSpec = {
                    fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(140))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                label = "pageTransition"
            ) { page ->
                when (page) {
                    JeePage.DASHBOARD -> DashboardScreen(uiState = uiState, viewModel = viewModel)
                    JeePage.PRACTICE -> PracticeStackScreen(uiState = uiState, viewModel = viewModel)
                    JeePage.CAMPUS_HUB -> CampusHubScreen(uiState = uiState, viewModel = viewModel, onNavigate = { viewModel.setPage(it) })
                    JeePage.AI -> AiScreen(uiState = uiState, viewModel = viewModel)
                    JeePage.PROGRESS -> ProgressScreen(uiState = uiState, viewModel = viewModel, onNavigateToPage = { viewModel.setPage(it) })
                    JeePage.PROFILE -> ProfileScreen(uiState = uiState, viewModel = viewModel)
                    JeePage.LEADERBOARD -> LeaderboardScreen(uiState = uiState, viewModel = viewModel)
                    JeePage.TESTS -> CbtTestScreen(uiState = uiState, viewModel = viewModel)
                    JeePage.EVENTS -> EventsScreen(uiState = uiState, viewModel = viewModel)
                    JeePage.REFERRALS -> ReferralRewardsScreen()
                    JeePage.STREAK -> StreakAndFocusScreen(uiState = uiState, viewModel = viewModel)
                    JeePage.DEMAND_CENTRE -> DemandCentreScreen(viewModel = viewModel)
                    JeePage.DAILY_PLANNER -> DailyPlannerScreen(uiState = uiState, viewModel = viewModel)
                    JeePage.CHAPTERS -> ChapterTrackerScreen(uiState = uiState, viewModel = viewModel)
                    JeePage.NCERT -> NcertCommandScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onNavigate = { route ->
                            when (route) {
                                "chapters" -> viewModel.setPage(JeePage.CHAPTERS)
                                "daily_planner" -> viewModel.setPage(JeePage.DAILY_PLANNER)
                                "ncert" -> viewModel.setPage(JeePage.NCERT)
                            }
                        }
                    )
                    JeePage.HOURS -> StudyHoursScreen(uiState = uiState, viewModel = viewModel)
                    JeePage.WEEKLY -> WeeklyReviewScreen(uiState = uiState, viewModel = viewModel)
                    JeePage.FLOW -> ConnectedFlowScreen(viewModel = viewModel)
                    JeePage.SETTINGS -> DataSettingsScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpenAdminGate = { viewModel.setPage(JeePage.ADMIN_PORTAL) }
                    )
                    JeePage.BOOKMARKS -> JeePrepHubPortalScreen(uiState = uiState, viewModel = viewModel)
                    JeePage.MEMES_HUB -> MemesHubScreen(uiState = uiState, viewModel = viewModel)
                    JeePage.CONSUMER_UTILITY -> com.example.ui.screens.ConsumerUtilityScreen(uiState = uiState, viewModel = viewModel)
                    JeePage.ADMIN_PORTAL -> AdminControlPortalScreen(
                        viewModel = viewModel,
                        onNavigateBack = { viewModel.setPage(JeePage.DASHBOARD) }
                    )
                }
            }
        }
    }

    // Spotlight Quick Command Dialog
    if (showSpotlightCommandDialog) {
        SpotlightCommandPaletteDialog(
            allPages = allPages,
            onSelectPage = { page ->
                showSpotlightCommandDialog = false
                viewModel.setPage(page)
            },
            onDismiss = { showSpotlightCommandDialog = false }
        )
    }

    if (showAuthDialog) {
        AuthDialog(
            viewModel = viewModel,
            onDismiss = { showAuthDialog = false }
        )
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyDialog = false }
        )
    }

    if (showTermsDialog) {
        TermsConditionsDialog(
            onDismiss = { showTermsDialog = false }
        )
    }

    if (showNotificationDialog) {
        NotificationCenterDialog(
            isOpen = true,
            viewModel = viewModel,
            onDismiss = { showNotificationDialog = false }
        )
    }

    if (showOnboardingDialog) {
        OnboardingFlowDialog(
            isOpen = showOnboardingDialog,
            onDismiss = { showOnboardingDialog = false },
            onComplete = { _, goal, hours, name ->
                val current = viewModel.currentUser.value
                viewModel.updateUserProfile(
                    fullName = name,
                    email = current?.email ?: "student@upgradehub.app",
                    targetExam = goal,
                    targetScore = current?.targetScore ?: "99 Percentile"
                )
                viewModel.setBatchDailyHours(hours)
            }
        )
    }
}

@Composable
private fun SpotlightCommandPaletteDialog(
    allPages: List<JeePage>,
    onSelectPage: (JeePage) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, allPages) {
        if (query.isBlank()) allPages
        else allPages.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.navLabel.contains(query, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF0070F3),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Quick Search & Navigation",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Type module name, PYQs, Planner, AI...", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0070F3),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered) { page ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectPage(page) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0070F3).copy(alpha = 0.12f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = iconForPage(page),
                                                contentDescription = null,
                                                tint = Color(0xFF0070F3),
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = page.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Jump to ${page.navLabel} workspace",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFF0070F3),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

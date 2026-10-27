package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.JeePage
import com.example.ui.viewmodel.JeeViewModel

@Composable
fun SidebarDrawerContent(
    viewModel: JeeViewModel,
    onNavigateToPage: (JeePage) -> Unit,
    onOpenAuthDialog: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTermsConditions: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenOnboarding: () -> Unit = {},
    onOpenNotificationCenter: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val batchStats by viewModel.batchCompletionStats.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(325.dp)
            .testTag("sidebar_drawer"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // App Branding Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 3.dp,
                        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.4f)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.upgrade_hub_logo),
                            contentDescription = "Upgrade Hub Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "UPGRADE",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "STUDY OS",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color(0xFF0070F3),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "5-PILLAR WORKSPACE • JEE 2026",
                            fontSize = 9.sp,
                            color = Color(0xFF0070F3),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                // Student Identity Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onNavigateToPage(JeePage.PROFILE)
                            onCloseDrawer()
                        }
                        .testTag("sidebar_profile_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val initials = currentUser?.fullName
                                ?.split(" ")
                                ?.take(2)
                                ?.mapNotNull { it.firstOrNull()?.toString() }
                                ?.joinToString("") ?: "PK"

                            Surface(
                                shape = CircleShape,
                                color = Color(currentUser?.avatarColor ?: 0xFF0070F3),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = initials.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.fullName ?: "Prince Kumar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currentUser?.email ?: "prince339569@gmail.com",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0070F3).copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = currentUser?.targetExam ?: "JEE 2026 (Lakshya Batch)",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0070F3),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "${batchStats.overallPercentage}% Syllabus",
                                fontSize = 10.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. PILLAR: HOME & DAILY FLOW
                SidebarSectionHeader("1. HOME & DAILY EXECUTION")
                SidebarMenuItem(
                    icon = Icons.Default.Dashboard,
                    label = "Home Dashboard",
                    tint = Color(0xFF0070F3),
                    isSelected = uiState.currentPage == JeePage.DASHBOARD,
                    testTag = "sidebar_nav_dashboard",
                    onClick = {
                        onNavigateToPage(JeePage.DASHBOARD)
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.AutoAwesome,
                    label = "Student OS (Tasks, Finance & Wellness)",
                    tint = Color(0xFF10B981),
                    isSelected = uiState.currentPage == JeePage.CONSUMER_UTILITY,
                    testTag = "sidebar_nav_consumer_utility",
                    onClick = {
                        onNavigateToPage(JeePage.CONSUMER_UTILITY)
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.CalendarToday,
                    label = "60-Day Daily Planner (4 Slots)",
                    tint = Color(0xFF0070F3),
                    isSelected = uiState.currentPage == JeePage.DAILY_PLANNER,
                    testTag = "sidebar_nav_daily_planner",
                    onClick = {
                        onNavigateToPage(JeePage.DAILY_PLANNER)
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.LocalFireDepartment,
                    label = "Pomodoro Focus & Streak Studio",
                    tint = Color(0xFFFF6D00),
                    isSelected = uiState.currentPage == JeePage.STREAK,
                    testTag = "sidebar_nav_streak",
                    onClick = {
                        onNavigateToPage(JeePage.STREAK)
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.Hub,
                    label = "Connected Study Flow Architecture",
                    tint = Color(0xFF6366F1),
                    isSelected = uiState.currentPage == JeePage.FLOW,
                    testTag = "sidebar_nav_flow",
                    onClick = {
                        onNavigateToPage(JeePage.FLOW)
                        onCloseDrawer()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. PILLAR: STUDY HUB & SYLLABUS
                SidebarSectionHeader("2. STUDY HUB & SYLLABUS VAULT")
                SidebarMenuItem(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    label = "Prep Hub Vault",
                    tint = Color(0xFF0070F3),
                    isSelected = uiState.currentPage == JeePage.BOOKMARKS,
                    testTag = "sidebar_nav_prep_hub_vault",
                    onClick = {
                        onNavigateToPage(JeePage.BOOKMARKS)
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.CheckCircle,
                    label = "9-Step Chapter Mastery Matrix",
                    tint = Color(0xFF10B981),
                    isSelected = uiState.currentPage == JeePage.CHAPTERS,
                    testTag = "sidebar_nav_chapters",
                    onClick = {
                        onNavigateToPage(JeePage.CHAPTERS)
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.MenuBook,
                    label = "NCERT Line-by-Line Tracker",
                    tint = Color(0xFF0EA5E9),
                    isSelected = uiState.currentPage == JeePage.NCERT,
                    testTag = "sidebar_nav_ncert",
                    onClick = {
                        onNavigateToPage(JeePage.NCERT)
                        onCloseDrawer()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. PILLAR: TEST ARENA & CONTESTS
                SidebarSectionHeader("3. CBT TEST ARENA & CONTESTS")
                SidebarMenuItem(
                    icon = Icons.Default.Quiz,
                    label = "Practice Stack (PYQs & Flashcards)",
                    tint = Color(0xFF8B5CF6),
                    isSelected = uiState.currentPage == JeePage.PRACTICE,
                    testTag = "sidebar_nav_practice",
                    onClick = {
                        onNavigateToPage(JeePage.PRACTICE)
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.Description,
                    label = "NTA CBT Mock Test Simulator",
                    tint = Color(0xFF10B981),
                    isSelected = uiState.currentPage == JeePage.TESTS,
                    testTag = "sidebar_nav_cbt_tests",
                    onClick = {
                        onNavigateToPage(JeePage.TESTS)
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.EmojiEvents,
                    label = "All-India Rankers League (AIR)",
                    tint = Color(0xFFF59E0B),
                    isSelected = uiState.currentPage == JeePage.LEADERBOARD,
                    testTag = "sidebar_nav_leaderboard",
                    onClick = {
                        onNavigateToPage(JeePage.LEADERBOARD)
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.Event,
                    label = "Live Events & Contests",
                    tint = Color(0xFFEC4899),
                    isSelected = uiState.currentPage == JeePage.EVENTS,
                    testTag = "sidebar_nav_events",
                    onClick = {
                        onNavigateToPage(JeePage.EVENTS)
                        onCloseDrawer()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4. PILLAR: ANALYTICS & RECHARTS
                SidebarSectionHeader("4. ANALYTICS & RECHARTS LAB")
                SidebarMenuItem(
                    icon = Icons.Default.Timeline,
                    label = "Progress & Recharts Hub",
                    tint = Color(0xFF0070F3),
                    isSelected = uiState.currentPage == JeePage.PROGRESS,
                    testTag = "sidebar_nav_progress_analytics",
                    onClick = {
                        onNavigateToPage(JeePage.PROGRESS)
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.ShowChart,
                    label = "60-Day Study Hours Visualizer",
                    tint = Color(0xFF10B981),
                    isSelected = uiState.currentPage == JeePage.HOURS,
                    testTag = "sidebar_nav_sixty_day_progress",
                    onClick = {
                        onNavigateToPage(JeePage.HOURS)
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.Assessment,
                    label = "Weekly Review & Diagnostics",
                    tint = Color(0xFF8B5CF6),
                    isSelected = uiState.currentPage == JeePage.WEEKLY,
                    testTag = "sidebar_nav_weekly",
                    onClick = {
                        onNavigateToPage(JeePage.WEEKLY)
                        onCloseDrawer()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 5. PILLAR: CAMPUS HUB & AI COACH
                SidebarSectionHeader("5. CAMPUS HUB & AI MENTOR")
                SidebarMenuItem(
                    icon = Icons.Default.AccountBalance,
                    label = "Campus Hub (Student Modules)",
                    tint = Color(0xFF0EA5E9),
                    isSelected = uiState.currentPage == JeePage.CAMPUS_HUB,
                    testTag = "sidebar_nav_campus_hub",
                    onClick = {
                        onNavigateToPage(JeePage.CAMPUS_HUB)
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.SmartToy,
                    label = "AI Study Coach (8 Modes)",
                    tint = Color(0xFF6366F1),
                    isSelected = uiState.currentPage == JeePage.AI,
                    testTag = "sidebar_nav_ai",
                    onClick = {
                        onNavigateToPage(JeePage.AI)
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.EmojiEmotions,
                    label = "Memes Hub (Study Motivation & Memes)",
                    tint = Color(0xFFF59E0B),
                    isSelected = uiState.currentPage == JeePage.MEMES_HUB,
                    testTag = "sidebar_nav_memes_hub",
                    onClick = {
                        onNavigateToPage(JeePage.MEMES_HUB)
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.Lightbulb,
                    label = "Student Demands & Feature Requests",
                    tint = Color(0xFF10B981),
                    isSelected = uiState.currentPage == JeePage.DEMAND_CENTRE,
                    testTag = "sidebar_nav_demand_centre",
                    onClick = {
                        onNavigateToPage(JeePage.DEMAND_CENTRE)
                        onCloseDrawer()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 6. SYSTEM & ACCOUNT
                SidebarSectionHeader("SYSTEM & ACCOUNT")
                SidebarMenuItem(
                    icon = Icons.Default.School,
                    label = "Batch & Goal Onboarding",
                    tint = Color(0xFF10B981),
                    testTag = "sidebar_nav_onboarding",
                    onClick = {
                        onOpenOnboarding()
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.Lock,
                    label = "Register / Login / Account",
                    tint = Color(0xFF5266CF),
                    testTag = "sidebar_nav_auth",
                    onClick = {
                        onOpenAuthDialog()
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.Settings,
                    label = "Settings & Data Backup",
                    tint = Color(0xFFF59E0B),
                    isSelected = uiState.currentPage == JeePage.SETTINGS,
                    testTag = "sidebar_nav_settings",
                    onClick = {
                        onNavigateToPage(JeePage.SETTINGS)
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.Notifications,
                    label = "Slot Alarms & Central Notices",
                    tint = Color(0xFF0070F3),
                    testTag = "sidebar_nav_notifications",
                    onClick = {
                        onOpenNotificationCenter()
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.Security,
                    label = "Privacy Policy",
                    tint = Color(0xFF10B981),
                    testTag = "sidebar_nav_privacy",
                    onClick = {
                        onOpenPrivacyPolicy()
                        onCloseDrawer()
                    }
                )
                SidebarMenuItem(
                    icon = Icons.Default.Gavel,
                    label = "Terms & Conditions",
                    tint = Color(0xFF00B0FF),
                    testTag = "sidebar_nav_terms",
                    onClick = {
                        onOpenTermsConditions()
                        onCloseDrawer()
                    }
                )
            }

            // Bottom Logout / App Info
            Column(modifier = Modifier.padding(top = 16.dp)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.logoutUser()
                            onCloseDrawer()
                        }
                        .padding(vertical = 8.dp)
                        .testTag("sidebar_logout_item"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Log Out",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFD32F2F)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Upgrade Hub v4.0 • 5-Pillar Study Architecture",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SidebarSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.7.sp,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    )
}

@Composable
private fun SidebarMenuItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    testTag: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) tint.copy(alpha = 0.12f) else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, tint.copy(alpha = 0.35f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = tint.copy(alpha = 0.14f),
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isSelected) tint else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

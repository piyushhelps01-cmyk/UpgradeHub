package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JeeEvent
import com.example.ui.components.TeamAnnouncementBannerCard
import com.example.ui.components.TeamAnnouncementBoardDialog
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel

@Composable
fun EventsScreen(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.eventsList.collectAsState()
    val currentMonth by viewModel.currentLeaderboardMonth.collectAsState()
    val teamAnnouncements by viewModel.teamAnnouncements.collectAsState()

    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterTabs = listOf("All Events", "Live Now", "Mega Mocks", "Speed Contests", "Marathons")

    var activeContestEvent by remember { mutableStateOf<JeeEvent?>(null) }
    var showContestSuccessDialog by remember { mutableStateOf(false) }
    var contestPointsAwarded by remember { mutableIntStateOf(0) }
    var isTeamAnnouncementBoardOpen by remember { mutableStateOf(false) }
    var openAnnouncementComposerInitially by remember { mutableStateOf(false) }

    val filteredEvents = remember(events, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> events.filter { it.isLive }
            2 -> events.filter { it.category == "Mega Mock" }
            3 -> events.filter { it.category == "Speed Contest" }
            4 -> events.filter { it.category == "Marathon" }
            else -> events
        }
    }

    val liveHeroEvent = events.find { it.isLive } ?: events.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // Section Title & Active Counter
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "JEE CONTESTS & ARENAS",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0071E3),
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Live Academic Events",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFF3B30).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFFFF3B30).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFF3B30),
                                modifier = Modifier.size(7.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EVENT PREVIEW",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF3B30),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Team Announcements & Important Messages Broadcast Card
        item {
            TeamAnnouncementBannerCard(
                announcements = teamAnnouncements,
                onAcknowledge = { id -> viewModel.acknowledgeAnnouncement(id) },
                onOpenBoard = { openComposer ->
                    openAnnouncementComposerInitially = openComposer
                    isTeamAnnouncementBoardOpen = true
                },
                onNavigateToTarget = { page -> viewModel.setPage(page) }
            )
        }

        // Live Mega Contest Spotlight Banner
        if (liveHeroEvent != null) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("live_event_spotlight_banner")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF0F172A),
                                        Color(0xFF1E1B4B),
                                        Color(0xFF311042)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFF3B30)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White,
                                        modifier = Modifier.size(5.dp)
                                    ) {}
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "DEMO CONTEST",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "EVENT PREVIEW",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = liveHeroEvent.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Text(
                            text = liveHeroEvent.subtitle,
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Event Metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${liveHeroEvent.durationMinutes} Mins",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${liveHeroEvent.participantsCount} Registered",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${liveHeroEvent.totalMarks} Marks",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { activeContestEvent = liveHeroEvent },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0071E3)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Enter Contest Now",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (liveHeroEvent.isRegistered) Color(0xFF00C853).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, if (liveHeroEvent.isRegistered) Color(0xFF00C853) else Color.White.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .height(40.dp)
                                    .clickable { viewModel.toggleEventRegistration(liveHeroEvent.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (liveHeroEvent.isRegistered) Icons.Default.CheckCircle else Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = if (liveHeroEvent.isRegistered) Color(0xFF00C853) else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (liveHeroEvent.isRegistered) "Registered" else "Remind Me",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (liveHeroEvent.isRegistered) Color(0xFF00C853) else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Filter Tabs
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    filterTabs.forEachIndexed { index, tab ->
                        val isSelected = selectedFilterIndex == index
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (isSelected) 1.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFilterIndex = index }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            ) {
                                Text(
                                    text = tab,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Monthly Reward Connection Note
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFD700).copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD700),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Event preview · not live",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Sample events only. Registration, rankings, and XP are not connected to a live contest backend.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Events List
        items(filteredEvents) { event ->
            EventCardItem(
                event = event,
                onRegisterToggle = { viewModel.toggleEventRegistration(event.id) },
                onEnterContest = { activeContestEvent = event }
            )
        }
    }

    // Active Contest Interactive Modal
    activeContestEvent?.let { event ->
        EventQuizContestDialog(
            event = event,
            onDismiss = { activeContestEvent = null },
            onFinish = { scoreEarned ->
                contestPointsAwarded = scoreEarned
                viewModel.boostUserScore(
                    points = scoreEarned,
                    hours = (event.durationMinutes / 60f).coerceAtLeast(0.5f),
                    problems = event.questionCount.coerceAtLeast(15)
                )
                activeContestEvent = null
                showContestSuccessDialog = true
            }
        )
    }

    // Contest Finished / Points Credited Dialog
    if (showContestSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showContestSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00C853),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Contest Completed!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Practice result: +$contestPointsAwarded sample points. This score is local to the preview.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF00C853).copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "These sample points are not added to a live leaderboard. No cash reward campaign is active.",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00C853),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showContestSuccessDialog = false
                        viewModel.setPage(com.example.ui.viewmodel.JeePage.LEADERBOARD)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0071E3))
                ) {
                    Text("View My Rank on Leaderboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showContestSuccessDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    TeamAnnouncementBoardDialog(
        isOpen = isTeamAnnouncementBoardOpen,
        initialShowComposer = openAnnouncementComposerInitially,
        announcements = teamAnnouncements,
        onPostAnnouncement = { title, message, category, authorName, authorRole, isPinned, isImportant, actionLabel, actionTargetPage ->
            viewModel.postTeamAnnouncement(
                title = title,
                message = message,
                category = category,
                authorName = authorName,
                authorRole = authorRole,
                isPinned = isPinned,
                isImportant = isImportant,
                actionLabel = actionLabel,
                actionTargetPage = actionTargetPage
            )
        },
        onTogglePin = { id -> viewModel.togglePinAnnouncement(id) },
        onAcknowledge = { id -> viewModel.acknowledgeAnnouncement(id) },
        onMarkAllRead = { viewModel.markAllAnnouncementsRead() },
        onDeleteAnnouncement = { id -> viewModel.deleteAnnouncement(id) },
        onNavigateToTarget = { page -> viewModel.setPage(page) },
        onDismiss = {
            isTeamAnnouncementBoardOpen = false
            openAnnouncementComposerInitially = false
        }
    )
}

@Composable
fun EventCardItem(
    event: JeeEvent,
    onRegisterToggle: () -> Unit,
    onEnterContest: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category, Schedule & Difficulty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (event.category) {
                            "Mega Mock" -> Color(0xFF5856D6).copy(alpha = 0.15f)
                            "Speed Contest" -> Color(0xFFFF9500).copy(alpha = 0.15f)
                            "Marathon" -> Color(0xFF00C853).copy(alpha = 0.15f)
                            else -> Color(0xFF0071E3).copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = event.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = when (event.category) {
                                "Mega Mock" -> Color(0xFF5856D6)
                                "Speed Contest" -> Color(0xFFFF9500)
                                "Marathon" -> Color(0xFF00C853)
                                else -> Color(0xFF0071E3)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = event.difficulty,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                if (event.isLive) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFF3B30)
                    ) {
                        Text(
                            text = "LIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        text = event.schedule,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = event.subtitle,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Syllabus preview box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Syllabus:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.syllabus,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer info & actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.participantsCount} enrolled",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (event.isRegistered) Color(0xFF00C853).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (event.isRegistered) Color(0xFF00C853).copy(alpha = 0.5f) else Color.Transparent),
                        modifier = Modifier
                            .height(34.dp)
                            .clickable { onRegisterToggle() }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = if (event.isRegistered) "Registered ✓" else "Register",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (event.isRegistered) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onClick = onEnterContest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0071E3)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "Take Contest",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventQuizContestDialog(
    event: JeeEvent,
    onDismiss: () -> Unit,
    onFinish: (scoreEarned: Int) -> Unit
) {
    // 3 Curated High-Yield JEE Contest Questions
    val questions = listOf(
        Triple(
            "A body of mass m is thrown vertically upward with speed v. The work done by gravity during ascent is:",
            listOf("- 1/2 m v²", "+ 1/2 m v²", "- m g h", "+ 2 m v²"),
            0
        ),
        Triple(
            "Which of the following molecules has zero dipole moment?",
            listOf("NH₃", "H₂O", "CCl₄", "SO₂"),
            2
        ),
        Triple(
            "The value of ∫₀^(π/2) sin²(x) dx is equal to:",
            listOf("π/2", "π/4", "1", "π"),
            1
        )
    )

    var currentQIndex by remember { mutableIntStateOf(0) }
    var selectedAnswers by remember { mutableStateOf(mapOf<Int, Int>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = event.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Question ${currentQIndex + 1} of ${questions.size} • Practice Preview",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "DEMO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD97706),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        },
        text = {
            val q = questions[currentQIndex]
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = q.first,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                q.second.forEachIndexed { optIndex, optionText ->
                    val isSelected = selectedAnswers[currentQIndex] == optIndex
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF0071E3).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF0071E3) else Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedAnswers = selectedAnswers + (currentQIndex to optIndex)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) Color(0xFF0071E3) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF0071E3) else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = ('A' + optIndex).toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = optionText,
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (currentQIndex < questions.size - 1) {
                Button(
                    onClick = { currentQIndex++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0071E3))
                ) {
                    Text("Next Question")
                }
            } else {
                Button(
                    onClick = {
                        // Calculate score
                        var correct = 0
                        questions.forEachIndexed { index, triple ->
                            if (selectedAnswers[index] == triple.third) {
                                correct++
                            }
                        }
                        val scoreEarned = (correct * 50).coerceAtLeast(30)
                        onFinish(scoreEarned)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                ) {
                    Text("Submit Contest")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Exit")
            }
        }
    )
}

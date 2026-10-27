package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HallOfFameWinner
import com.example.data.model.LeaderboardStudent
import kotlinx.coroutines.delay

@Composable
fun PodiumSection(
    top1: LeaderboardStudent?,
    top2: LeaderboardStudent?,
    top3: LeaderboardStudent?,
    onStudentClick: (LeaderboardStudent) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Top 3 Podium",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sample podium",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD97706)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3-Podium Columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place (Silver)
                if (top2 != null) {
                    PodiumColumnItem(
                        student = top2,
                        rank = 2,
                        medalColor = Color(0xFF94A3B8),
                        medalIcon = "🥈",
                        crownText = "SILVER",
                        heightDp = 138,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onStudentClick(top2) }
                    )
                }

                // 1st Place (Gold, Tallest)
                if (top1 != null) {
                    PodiumColumnItem(
                        student = top1,
                        rank = 1,
                        medalColor = Color(0xFFFFD700),
                        medalIcon = "🥇",
                        crownText = "CHAMPION",
                        heightDp = 170,
                        modifier = Modifier
                            .weight(1.1f)
                            .clickable { onStudentClick(top1) }
                    )
                }

                // 3rd Place (Bronze)
                if (top3 != null) {
                    PodiumColumnItem(
                        student = top3,
                        rank = 3,
                        medalColor = Color(0xFFCD7F32),
                        medalIcon = "🥉",
                        crownText = "BRONZE",
                        heightDp = 120,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onStudentClick(top3) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumColumnItem(
    student: LeaderboardStudent,
    rank: Int,
    medalColor: Color,
    medalIcon: String,
    crownText: String,
    heightDp: Int,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateIntAsState(
        targetValue = student.monthlyScore,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "podium_score"
    )
    val animatedHeight by animateDpAsState(
        targetValue = heightDp.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "podium_height"
    )

    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = medalColor.copy(alpha = 0.2f)
        ) {
            Text(
                text = crownText,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = if (rank == 1) Color(0xFFD97706) else medalColor,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
            )
        }

        Text(
            text = medalIcon,
            fontSize = 24.sp
        )

        Surface(
            shape = CircleShape,
            color = Color(student.avatarColor),
            border = BorderStroke(2.5.dp, medalColor),
            modifier = Modifier.size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = student.avatarInitials,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = student.studentName.split(" ").firstOrNull() ?: student.studentName,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "$animatedScore pts",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (rank == 1) Color(0xFFD97706) else medalColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pedestal Box
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = medalColor.copy(alpha = 0.16f),
            border = BorderStroke(1.5.dp, medalColor.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedHeight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                AnimatedContent(
                    targetState = rank,
                    transitionSpec = {
                        if (targetState < initialState) {
                            (slideInVertically { -it } + fadeIn()).togetherWith(slideOutVertically { it } + fadeOut())
                        } else {
                            (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                        }
                    },
                    label = "podium_rank"
                ) { targetRank ->
                    Text(
                        text = "#$targetRank",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (targetRank == 1) Color(0xFFD97706) else medalColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF00C853)
                ) {
                    Text(
                        text = "SAMPLE",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${student.studyHours.toInt()}h study",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${student.problemsSolved} Qs",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun UserStandingRaceCard(
    userStudent: LeaderboardStudent?,
    top3CutoffScore: Int,
    onInspectProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (userStudent == null) return

    val isTop3 = userStudent.isTopThree
    val animatedScore by animateIntAsState(
        targetValue = userStudent.monthlyScore,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "user_monthly_score"
    )
    val pointsNeeded = (top3CutoffScore - userStudent.monthlyScore + 10).coerceAtLeast(0)
    val animatedPointsNeeded by animateIntAsState(
        targetValue = pointsNeeded,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "points_needed"
    )
    val progressToCutoff = if (top3CutoffScore > 0) {
        (userStudent.monthlyScore.toFloat() / top3CutoffScore.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progressToCutoff,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "user_cutoff_progress"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTop3) Color(0xFF00C853).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.5.dp,
            if (isTop3) Color(0xFF00C853) else Color(0xFF0071E3).copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onInspectProfile() }
            .testTag("current_user_rank_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Avatar, Name, Rank Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(userStudent.avatarColor),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = userStudent.avatarInitials,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userStudent.studentName,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0071E3).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "YOU",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0071E3),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${userStudent.batchName} • ${userStudent.streakDays}d streak 🔥",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isTop3) Color(0xFF00C853) else Color(0xFF0071E3),
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedContent(
                                targetState = userStudent.rank,
                                transitionSpec = {
                                    if (targetState < initialState) {
                                        (slideInVertically { -it } + fadeIn()).togetherWith(slideOutVertically { it } + fadeOut())
                                    } else {
                                        (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                                    }
                                },
                                label = "user_rank_badge_anim"
                            ) { rankVal ->
                                Text(
                                    text = "RANK #$rankVal",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            if (userStudent.rankDelta != 0) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = if (userStudent.rankDelta > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = "Up",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${Math.abs(userStudent.rankDelta)}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Text(
                        text = "$animatedScore pts",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Local sample-data comparison; not a live ranking.
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTop3) "🏆 Sample Top 3" else "Sample score gap",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTop3) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isTop3) "Maintaining Lead" else "$animatedPointsNeeded pts needed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isTop3) Color(0xFF00C853) else Color(0xFFD97706)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isTop3) Color(0xFF00C853) else Color(0xFF0071E3),
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isTop3)
                            "This sample profile is in the preview's Top 3; it is not a live rank or reward."
                        else
                            "Scores and rank gaps are sample values; study actions here do not change them.",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Server-verified XP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "XP is credited only by the secure Referral & XP backend. This sample leaderboard cannot award points.",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun LeaderboardStudentCard(
    student: LeaderboardStudent,
    isWeeklyTab: Boolean = false,
    onStudentClick: () -> Unit,
    onHighFive: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTop3 = student.isTopThree
    val rankBadgeBg = when (student.rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFF94A3B8)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val rankTextColor = if (student.rank in 1..3) Color(0xFF1E293B) else MaterialTheme.colorScheme.onSurface

    val targetScore = if (isWeeklyTab) student.weeklyScore else student.monthlyScore
    val animatedScore by animateIntAsState(
        targetValue = targetScore,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "student_score"
    )

    var highFiveTrigger by remember { mutableStateOf(false) }
    val highFiveScale by animateFloatAsState(
        targetValue = if (highFiveTrigger) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "high_five_scale"
    )

    LaunchedEffect(student.cheersCount) {
        if (student.cheersCount > 0) {
            highFiveTrigger = true
            delay(250)
            highFiveTrigger = false
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (student.isCurrentUser) Color(0xFF0071E3).copy(alpha = 0.07f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (student.isCurrentUser)
                Color(0xFF0071E3)
            else if (isTop3)
                Color(0xFFFFD700).copy(alpha = 0.45f)
            else
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        shadowElevation = if (isTop3) 2.dp else 0.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onStudentClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Badge with Delta indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(36.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = rankBadgeBg,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AnimatedContent(
                                targetState = student.rank,
                                transitionSpec = {
                                    if (targetState < initialState) {
                                        (slideInVertically { -it } + fadeIn()).togetherWith(slideOutVertically { it } + fadeOut())
                                    } else {
                                        (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                                    }
                                },
                                label = "rank_anim"
                            ) { rankVal ->
                                Text(
                                    text = "#$rankVal",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = rankTextColor
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = student.rankDelta != 0,
                        enter = fadeIn() + slideInVertically { -it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (student.rankDelta > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (student.rankDelta > 0) Color(0xFF00C853) else Color(0xFFFF3B30),
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "${Math.abs(student.rankDelta)}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (student.rankDelta > 0) Color(0xFF00C853) else Color(0xFFFF3B30)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Avatar
                Surface(
                    shape = CircleShape,
                    color = Color(student.avatarColor),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = student.avatarInitials,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Name & Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = student.studentName,
                            fontSize = 13.5.sp,
                            fontWeight = if (student.isCurrentUser) FontWeight.Black else FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isTop3) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF00C853).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "SAMPLE TOP 3",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF00C853),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${student.batchName} • ${student.studyHours.toInt()}h • ${student.problemsSolved} Qs",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // First Badge if present
                    if (student.badges.isNotEmpty()) {
                        Text(
                            text = student.badges.first(),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0071E3),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Score & High-Five Column
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$animatedScore pts",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.5.sp,
                        color = if (isTop3) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${student.streakDays}d",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }

                // High Five ✋ Action Button
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(34.dp)
                        .graphicsLayer {
                            scaleX = highFiveScale
                            scaleY = highFiveScale
                        }
                        .clickable {
                            highFiveTrigger = true
                            onHighFive()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✋",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentDossierDialog(
    student: LeaderboardStudent,
    onDismiss: () -> Unit,
    onHighFive: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(student.avatarColor),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = student.avatarInitials,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = student.studentName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = student.batchName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (student.isTopThree) Color(0xFF00C853) else Color(0xFF0071E3)
                ) {
                    Text(
                        text = "RANK #${student.rank}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Reward Status
                if (student.isTopThree) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF00C853).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF00C853).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF00C853),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sample Top 3 placement · no active reward",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00C853)
                            )
                        }
                    }
                }

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(
                        title = "Study Hours",
                        value = "${student.studyHours.toInt()} hrs",
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Problems Solved",
                        value = "${student.problemsSolved} Qs",
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Accuracy",
                        value = "${student.accuracyPercentage}%",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Points Breakdown Formula
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Points Breakdown",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BreakdownRow("Sample study score", "${(student.studyHours * 10).toInt()} pts")
                        BreakdownRow("Practice Questions (+4/Q)", "${(student.problemsSolved * 2).coerceAtMost(2000)} pts")
                        BreakdownRow("Mock Tests (+50/test)", "${student.mockTestsTaken * 50} pts")
                        BreakdownRow("Streak Multiplier", "${student.streakDays} days active 🔥")
                    }
                }

                // Strongest Subject
                Text(
                    text = "🎯 Strong Subject: ${student.strongSubject}",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // High Five Action inside Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${student.cheersCount} high-fives received 🌟",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onHighFive,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0071E3)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Send ✋ High-Five")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun StatBox(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun HallOfFameTabContent(
    winners: List<HallOfFameWinner>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Authenticity guarantee banner
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Sample learner stories",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700)
                    )
                    Text(
                        text = "Illustrative sample content only. No cash reward campaign or verified payout history is active.",
                        fontSize = 11.5.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        winners.forEach { winner ->
            HallOfFameWinnerCard(winner = winner)
        }
    }
}

@Composable
fun HallOfFameWinnerCard(winner: HallOfFameWinner) {
    val rankMedal = when (winner.rank) {
        1 -> "🥇 Rank 1"
        2 -> "🥈 Rank 2"
        3 -> "🥉 Rank 3"
        else -> "#${winner.rank}"
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                        color = Color(winner.avatarColor),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = winner.avatarInitials,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = winner.studentName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${winner.batchName} • ${winner.month}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF00C853)
                    ) {
                        Text(
                            text = "SAMPLE",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = rankMedal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Testimonial quote
            Text(
                text = winner.tipQuote,
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

        }
    }
}

@Composable
fun LeaderboardRulesDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF0071E3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Leaderboard preview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Not a live competition",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RuleItem(
                    title = "1. Sample rankings",
                    desc = "Names, scores, ranks, streaks, and Hall of Fame entries are illustrative local data. They are not live, official, or verified."
                )
                RuleItem(
                    title = "2. XP and points",
                    desc = "This preview cannot award points. Server-backed XP is shown only in the Referral & XP screen and is not money."
                )
                RuleItem(
                    title = "3. Cash rewards",
                    desc = "No cash or UPI reward campaign is active. UpgradeHub does not ask for payment details here."
                )
                RuleItem(
                    title = "4. Before launch",
                    desc = "A real leaderboard or reward program requires a verified server data source, published eligibility terms, and a separately approved campaign."
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It")
            }
        }
    )
}

@Composable
private fun RuleItem(title: String, desc: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}


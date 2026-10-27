package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.DailySlot
import com.example.ui.viewmodel.JeeViewModel
import com.example.work.StudySlotWorkScheduler

/**
 * Composable UI for the Main Screen (Dashboard) displaying:
 * 1. The list of scheduled study slots with times, targets, and milestone progress.
 * 2. A prominent manual trigger test button console to verify the emergency alarm drill
 *    functionality in the notification bar (high-importance channel, DND bypass, heads-up banner).
 */
@Composable
fun ScheduledStudySlotsEmergencyDrillSection(
    dayIndex: Int,
    slots: List<DailySlot>,
    version: Int,
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    StudySlotAlarmManager.ensureInitialized(context)
    val slotAlarmConfigs by StudySlotAlarmManager.slotConfigs.collectAsState()
    val activeMockDrill by StudySlotAlarmManager.activeMockDrill.collectAsState()

    var customizingSlotIndex by remember { mutableStateOf<Int?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.showSnackbar("🔔 Notification permission granted! You can now test the alarm drill.")
        } else {
            viewModel.showSnackbar("⚠️ Notification permission required to display alarm drill in the notification bar.")
        }
    }

    fun triggerDrillInNotificationBar(slotIdx: Int = 0, customTitle: String? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        val targetSlot = slotAlarmConfigs.getOrNull(slotIdx) ?: StudySlotAlarmManager.getSlotConfig(slotIdx)
        val title = customTitle ?: "🚨 EMERGENCY DRILL: ${targetSlot.title}"
        val body = "Study Slot Drill is ACTIVE! 30-Second unstoppable alarm sounding. Check your notification bar."

        val posted = triggerStudySlotReminderNotification(
            context = context,
            slotTitle = title,
            slotBody = body,
            notificationId = 1042 + slotIdx,
            slotIndex = slotIdx
        )

        StudySlotAlarmManager.startThirtySecondMockDrillAudio(
            context = context,
            slotIndex = slotIdx,
            slotTitle = targetSlot.title,
            scheduledTimeLabel = targetSlot.formattedTime
        )

        if (posted) {
            viewModel.showSnackbar("🚨 Emergency Alarm Drill triggered in Notification Bar! Pull down status bar to verify.")
        } else {
            viewModel.showSnackbar("⚠️ Could not post notification. Please check system notification settings.")
        }
    }

    if (customizingSlotIndex != null) {
        val targetIdx = customizingSlotIndex!!
        val slotTitleForDialog = if (targetIdx >= 0) slots.getOrNull(targetIdx)?.title else null
        StudySlotTimeCustomizerDialog(
            slotIndex = targetIdx,
            slotCustomTitle = slotTitleForDialog,
            onDismiss = {
                if (targetIdx < 0) {
                    viewModel.showSnackbar("⏰ Study slot added! A 30-second focus alert is scheduled for the designated time.")
                } else {
                    val updatedCfg = StudySlotAlarmManager.getSlotConfig(targetIdx)
                    viewModel.showSnackbar("⏰ Slot ${targetIdx + 1} alarm set: ${updatedCfg.formattedTime} • ${updatedCfg.title} (30s focus chime active)")
                }
                customizingSlotIndex = null
            }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header with Add Slot & Set Times Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SCHEDULED STUDY SLOTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = Color(0xFF0070F3)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${slots.size} ACTIVE",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF059669),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Exact time pe 30s unskippable siren auto-rings • Bypasses silent mode",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Test Alarm Drill in Notification Bar
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clickable { triggerDrillInNotificationBar(slotIdx = 0) }
                        .testTag("trigger_emergency_alarm_drill_test_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Test Drill",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }

                // 5s Background Test
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0070F3).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.35f)),
                    modifier = Modifier
                        .clickable {
                            StudySlotWorkScheduler.scheduleTestAlarmWork(
                                context = context,
                                delaySeconds = 5L,
                                slotIndex = 0
                            )
                            viewModel.showSnackbar("⚡ Background alarm drill scheduled! Triggers notification in 5s.")
                        }
                        .testTag("test_background_alarm_workmanager_btn")
                ) {
                    Text(
                        text = "5s Bg",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0070F3),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0070F3),
                    modifier = Modifier
                        .clickable { customizingSlotIndex = -1 }
                        .testTag("dashboard_add_slot_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Slot",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "+ Slot",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable { customizingSlotIndex = 0 }
                        .testTag("dashboard_customize_slot_times_btn")
                ) {
                    Text(
                        text = "⏰ Times",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Active Emergency Mock Drill Live Banner (if ringing right now)
        AnimatedVisibility(
            visible = activeMockDrill != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            activeMockDrill?.let { drill ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444),
                    border = BorderStroke(1.5.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_drill_ringing_main_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "🚨 ALARM DRILL ACTIVE IN NOTIFICATION BAR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "${drill.remainingSeconds}s remaining • ${drill.slotTitle}",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFEF08A)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.35f),
                            modifier = Modifier.clickable {
                                StudySlotAlarmManager.stopMockDrillAudio()
                                viewModel.showSnackbar("Alarm drill stopped")
                            }
                        ) {
                            Text(
                                text = "Mute / Stop",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // LIST OF SCHEDULED STUDY SLOTS
        // =========================================================================
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slots.forEach { slot ->
                val progress = remember(version, dayIndex, slot.slotIndex) {
                    viewModel.getSlotProgress(dayIndex, slot.slotIndex)
                }
                val slotAlarm = slotAlarmConfigs.getOrElse(slot.slotIndex) {
                    StudySlotAlarmManager.getSlotConfig(slot.slotIndex)
                }
                val slotCompletedCount = listOf(
                    progress.lecture,
                    progress.dpp,
                    progress.pyq,
                    progress.revision,
                    progress.ncert
                ).count { it }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (slotCompletedCount >= 3)
                        Color(0xFF10B981).copy(alpha = 0.08f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                    border = BorderStroke(
                        1.dp,
                        if (slotCompletedCount >= 3) Color(0xFF10B981).copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scheduled_study_slot_card_${slot.slotIndex}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(7.dp),
                                    color = Color(0xFF0070F3).copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "SLOT ${slot.slotIndex + 1} • 3H",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0070F3),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))

                                // Customizable Slot Time & Drill Badge
                                Surface(
                                    shape = RoundedCornerShape(7.dp),
                                    color = if (slotAlarm.isEnabled) Color(0xFF10B981).copy(alpha = 0.14f)
                                    else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (slotAlarm.isEnabled) Color(0xFF10B981).copy(alpha = 0.45f)
                                        else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .clickable { customizingSlotIndex = slot.slotIndex }
                                        .testTag("dashboard_slot_time_badge_${slot.slotIndex + 1}")
                                ) {
                                    Text(
                                        text = if (slotAlarm.isEnabled) "⏰ ${slotAlarm.formattedTime} ✎" else "🔕 ${slotAlarm.formattedTime} ✎",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (slotAlarm.isEnabled) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = slot.title,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = slot.suggestedTarget,
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Individual Slot Drill Test Button
                            Surface(
                                shape = RoundedCornerShape(7.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clickable {
                                        triggerDrillInNotificationBar(
                                            slotIdx = slot.slotIndex,
                                            customTitle = "🚨 ${slot.title} (${slotAlarm.formattedTime})"
                                        )
                                    }
                                    .testTag("test_slot_alarm_btn_${slot.slotIndex}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = "Test Notification",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Test",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "$slotCompletedCount/5",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (slotCompletedCount >= 3) Color(0xFF10B981) else Color(0xFF0070F3)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Milestone Check Chips for this Schedule Slot
                        val quickChecks = listOf(
                            Triple("lecture", "Lecture", progress.lecture),
                            Triple("dpp", "DPP", progress.dpp),
                            Triple("pyq", "PYQ", progress.pyq),
                            Triple("ncert", "NCERT", progress.ncert),
                            Triple("revision", "Revise", progress.revision)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            quickChecks.forEach { (key, label, isDone) ->
                                Surface(
                                    shape = RoundedCornerShape(7.dp),
                                    color = if (isDone) Color(0xFF10B981).copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isDone) Color(0xFF10B981).copy(alpha = 0.45f)
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            viewModel.toggleSlotCheck(dayIndex, slot.slotIndex, key, isDone)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (isDone) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = label,
                                            fontSize = 9.5.sp,
                                            fontWeight = if (isDone) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isDone) Color(0xFF059669) else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
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

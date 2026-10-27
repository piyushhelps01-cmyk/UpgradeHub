package com.example.ui.components

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.MainActivity
import com.example.work.StudySlotWorkScheduler
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import com.example.data.repository.CentralNoticeRecord
import com.example.ui.viewmodel.JeeViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R
import java.util.Calendar
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class NotificationPref(
    val id: String,
    val title: String,
    val description: String,
    val timeBadge: String? = null,
    val defaultEnabled: Boolean = true
)

data class StudySlotAlarmConfig(
    val slotIndex: Int, // 0..3
    val title: String,
    val description: String,
    val hour24: Int,    // 0..23
    val minute: Int,    // 0..59
    val isEnabled: Boolean = true
) {
    val formattedTime: String
        get() = formatTime12Hour(hour24, minute)
}

data class ActiveMockDrillState(
    val slotIndex: Int,
    val slotTitle: String,
    val scheduledTimeLabel: String,
    val remainingSeconds: Int = 30,
    val totalSeconds: Int = 30,
    val isDynamicAdjustmentEnabled: Boolean = false,
    val activePlanDays: Int = 60,
    val currentDayInPlan: Int = 18,
    val strugglingTopic: String = "",
    val effectiveFreqHighHz: Double = 960.0,
    val effectiveFreqLowHz: Double = 680.0,
    val intensityLabel: String = "Standard Locked (Do Not Adjust)"
)

enum class DrillIntensitySeverity(
    val label: String,
    val badgeColor: Long,
    val freqMultiplier: Double,
    val chunkMs: Int
) {
    STANDARD("Standard (Static)", 0xFF0070F3, 1.0, 250),
    MODERATE("Moderate Urgency (+15% Pitch)", 0xFFF59E0B, 1.15, 210),
    HIGH("High Weakness Intensity (+35% Pitch)", 0xFFEF4444, 1.35, 175),
    CRITICAL("Critical Emergency Drill (+60% Pitch)", 0xFFB91C1C, 1.60, 140)
}

data class MockDrillFrequencyPolicy(
    val dynamicallyAdjust: Boolean = false, // When false: "Do Not Dynamically Adjust (Static Fixed 30s Mock Drill)"
    val activePlanDays: Int = 60, // 30, 45, 60, 75, 90, 120, 150
    val currentDayInPlan: Int = 18,
    val strugglingTopic: String = "Rotational Dynamics & Torque",
    val strugglingSeverity: DrillIntensitySeverity = DrillIntensitySeverity.HIGH
) {
    val planProgressPercent: Int
        get() = if (activePlanDays > 0) ((currentDayInPlan.coerceAtLeast(1) * 100) / activePlanDays).coerceIn(1, 100) else 0

    val effectiveFreqHigh: Double
        get() = if (dynamicallyAdjust) (960.0 * strugglingSeverity.freqMultiplier).coerceIn(960.0, 1750.0) else 960.0

    val effectiveFreqLow: Double
        get() = if (dynamicallyAdjust) (680.0 * strugglingSeverity.freqMultiplier).coerceIn(680.0, 1250.0) else 680.0

    val effectiveChunkMs: Int
        get() = if (dynamicallyAdjust) strugglingSeverity.chunkMs else 250
}

fun formatTime12Hour(hour24: Int, minute: Int): String {
    val h = ((hour24 % 24) + 24) % 24
    val m = ((minute % 60) + 60) % 60
    val period = if (h < 12) "AM" else "PM"
    val hour12 = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return String.format(Locale.ENGLISH, "%02d:%02d %s", hour12, m, period)
}

const val STUDY_REMINDER_CHANNEL_ID = "study_slot_alarm_fullscreen_channel"
private const val PREFS_NAME = "upgrade_hub_notifications_prefs"

/**
 * Central manager for customizable Study Slot times, real-time slot alarm detection,
 * high-priority notifications, and the 30-second full-volume Mock Drill Siren audio engine.
 */
object StudySlotAlarmManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val defaultSlots = listOf(
        StudySlotAlarmConfig(
            slotIndex = 0,
            title = "Slot 1 • Morning Core Concept",
            description = "High-focus Physics/Maths Lecture + DPP + 30s Mock Drill Siren",
            hour24 = 6,
            minute = 0,
            isEnabled = true
        ),
        StudySlotAlarmConfig(
            slotIndex = 1,
            title = "Slot 2 • Forenoon NCERT & Drills",
            description = "Chemistry NCERT Line-by-Line & Chapter Checklist + 30s Mock Drill",
            hour24 = 10,
            minute = 0,
            isEnabled = true
        ),
        StudySlotAlarmConfig(
            slotIndex = 2,
            title = "Slot 3 • Afternoon PYQ Sprint",
            description = "2019–2025 Shift PYQs & timed problem solving + 30s Mock Drill",
            hour24 = 15,
            minute = 0,
            isEnabled = true
        ),
        StudySlotAlarmConfig(
            slotIndex = 3,
            title = "Slot 4 • Night Revision & Audit",
            description = "Mistake Vault review, flashcards & daily hours log + 30s Mock Drill",
            hour24 = 20,
            minute = 0,
            isEnabled = true
        )
    )

    private val _slotConfigs = MutableStateFlow(defaultSlots)
    val slotConfigs: StateFlow<List<StudySlotAlarmConfig>> = _slotConfigs.asStateFlow()

    private val _activeMockDrill = MutableStateFlow<ActiveMockDrillState?>(null)
    val activeMockDrill: StateFlow<ActiveMockDrillState?> = _activeMockDrill.asStateFlow()

    private val _drillPolicy = MutableStateFlow(MockDrillFrequencyPolicy())
    val drillPolicy: StateFlow<MockDrillFrequencyPolicy> = _drillPolicy.asStateFlow()

    private var isInitialized = false
    private var lastTriggeredStamp: String = ""
    private var drillJob: Job? = null
    private var audioSynthesisJob: Job? = null
    @Volatile
    private var activeMediaPlayer: MediaPlayer? = null
    @Volatile
    private var activeAudioTrack: AudioTrack? = null
    @Volatile
    private var activeToneGen: ToneGenerator? = null
    @Volatile
    private var activeRingtone: Ringtone? = null

    fun ensureInitialized(context: Context) {
        if (isInitialized) return
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCount = prefs.getInt("slots_count", -1)
        val loaded = if (savedCount > 0) {
            (0 until savedCount).map { i ->
                val h = prefs.getInt("slot_${i}_hour24", defaultSlots.getOrNull(i)?.hour24 ?: (6 + i * 4)).coerceIn(0, 23)
                val m = prefs.getInt("slot_${i}_minute", defaultSlots.getOrNull(i)?.minute ?: 0).coerceIn(0, 59)
                val customTitle = prefs.getString("slot_${i}_title", defaultSlots.getOrNull(i)?.title ?: "Study Slot ${i + 1}") ?: "Study Slot ${i + 1}"
                val customDesc = prefs.getString("slot_${i}_desc", defaultSlots.getOrNull(i)?.description ?: "Automated 30s Study Drill Alert") ?: "Automated 30s Study Drill Alert"
                StudySlotAlarmConfig(slotIndex = i, title = customTitle, description = customDesc, hour24 = h, minute = m, isEnabled = true)
            }
        } else {
            defaultSlots.map { def ->
                val idx = def.slotIndex
                val h = prefs.getInt("slot_${idx}_hour24", def.hour24).coerceIn(0, 23)
                val m = prefs.getInt("slot_${idx}_minute", def.minute).coerceIn(0, 59)
                val customTitle = prefs.getString("slot_${idx}_title", def.title) ?: def.title
                def.copy(hour24 = h, minute = m, title = customTitle, isEnabled = true)
            }
        }
        _slotConfigs.value = loaded

        // Load Mock Drill Frequency & Plan policy
        val dynamicAdjust = prefs.getBoolean("mock_drill_dynamically_adjust", false)
        val planDays = prefs.getInt("mock_drill_plan_days", 60)
        val currentDay = prefs.getInt("mock_drill_current_day", 18)
        val topic = prefs.getString("mock_drill_struggling_topic", "Rotational Dynamics & Torque") ?: "Rotational Dynamics & Torque"
        val severityName = prefs.getString("mock_drill_severity", DrillIntensitySeverity.HIGH.name) ?: DrillIntensitySeverity.HIGH.name
        val severity = try { DrillIntensitySeverity.valueOf(severityName) } catch (_: Exception) { DrillIntensitySeverity.HIGH }

        _drillPolicy.value = MockDrillFrequencyPolicy(
            dynamicallyAdjust = dynamicAdjust,
            activePlanDays = planDays,
            currentDayInPlan = currentDay,
            strugglingTopic = topic,
            strugglingSeverity = severity
        )

        isInitialized = true
        try {
            StudySlotWorkScheduler.scheduleAllSlots(context.applicationContext)
        } catch (_: Exception) {
        }
    }

    private fun saveSlotsToPrefs(context: Context, slots: List<StudySlotAlarmConfig>) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt("slots_count", slots.size)
        slots.forEachIndexed { i, s ->
            editor.putInt("slot_${i}_hour24", s.hour24)
            editor.putInt("slot_${i}_minute", s.minute)
            editor.putString("slot_${i}_title", s.title)
            editor.putString("slot_${i}_desc", s.description)
            editor.putBoolean("slot_alarm_${i + 1}", true)
        }
        editor.apply()
        lastTriggeredStamp = ""
    }

    fun addStudySlot(
        context: Context,
        hour24: Int,
        minute: Int,
        title: String,
        description: String = ""
    ) {
        ensureInitialized(context)
        val clampedHour = ((hour24 % 24) + 24) % 24
        val clampedMin = ((minute % 60) + 60) % 60
        val cleanTopic = title.trim().ifBlank { "Study Slot" }
        val current = _slotConfigs.value.toMutableList()
        val nextIdx = current.size
        val newSlot = StudySlotAlarmConfig(
            slotIndex = nextIdx,
            title = cleanTopic,
            description = description.ifBlank { "Automated 30s Study Drill Alert at ${formatTime12Hour(clampedHour, clampedMin)}" },
            hour24 = clampedHour,
            minute = clampedMin,
            isEnabled = true
        )
        current.add(newSlot)
        saveSlotsToPrefs(context, current)
        _slotConfigs.value = current
        try {
            StudySlotWorkScheduler.scheduleAllSlots(context.applicationContext)
        } catch (_: Exception) {
        }
    }

    fun deleteStudySlot(context: Context, slotIndex: Int) {
        ensureInitialized(context)
        val current = _slotConfigs.value.filter { it.slotIndex != slotIndex }
        val reindexed = current.mapIndexed { idx, item ->
            item.copy(slotIndex = idx)
        }
        saveSlotsToPrefs(context, reindexed)
        _slotConfigs.value = reindexed
        try {
            StudySlotWorkScheduler.cancelSlot(context.applicationContext, slotIndex)
            StudySlotWorkScheduler.scheduleAllSlots(context.applicationContext)
        } catch (_: Exception) {
        }
    }

    fun updateDrillFrequencyPolicy(context: Context, policy: MockDrillFrequencyPolicy) {
        ensureInitialized(context)
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("mock_drill_dynamically_adjust", policy.dynamicallyAdjust)
            .putInt("mock_drill_plan_days", policy.activePlanDays)
            .putInt("mock_drill_current_day", policy.currentDayInPlan)
            .putString("mock_drill_struggling_topic", policy.strugglingTopic)
            .putString("mock_drill_severity", policy.strugglingSeverity.name)
            .apply()
        _drillPolicy.value = policy
    }

    fun toggleDynamicAdjustment(context: Context, enabled: Boolean) {
        ensureInitialized(context)
        val current = _drillPolicy.value
        updateDrillFrequencyPolicy(context, current.copy(dynamicallyAdjust = enabled))
    }

    fun setActivePlanDays(context: Context, days: Int) {
        ensureInitialized(context)
        val current = _drillPolicy.value
        val clampedDay = current.currentDayInPlan.coerceIn(1, days)
        updateDrillFrequencyPolicy(context, current.copy(activePlanDays = days, currentDayInPlan = clampedDay))
    }

    fun setCurrentDayInPlan(context: Context, day: Int) {
        ensureInitialized(context)
        val current = _drillPolicy.value
        updateDrillFrequencyPolicy(context, current.copy(currentDayInPlan = day.coerceIn(1, current.activePlanDays)))
    }

    fun setStrugglingTopic(context: Context, topic: String, severity: DrillIntensitySeverity) {
        ensureInitialized(context)
        val current = _drillPolicy.value
        updateDrillFrequencyPolicy(context, current.copy(strugglingTopic = topic, strugglingSeverity = severity))
    }

    fun getSlotConfig(slotIndex: Int): StudySlotAlarmConfig {
        return _slotConfigs.value.getOrElse(slotIndex) {
            defaultSlots.getOrElse(slotIndex) { defaultSlots.first() }
        }
    }

    fun updateSlotTimeAndTarget(
        context: Context,
        slotIndex: Int,
        hour24: Int,
        minute: Int,
        topic: String
    ) {
        ensureInitialized(context)
        val clampedHour = ((hour24 % 24) + 24) % 24
        val clampedMin = ((minute % 60) + 60) % 60
        val cleanTopic = topic.trim().ifBlank {
            _slotConfigs.value.getOrNull(slotIndex)?.title ?: "Slot ${slotIndex + 1} Study Slot"
        }
        val updated = _slotConfigs.value.map { item ->
            if (item.slotIndex == slotIndex) {
                item.copy(hour24 = clampedHour, minute = clampedMin, title = cleanTopic, isEnabled = true)
            } else item
        }
        saveSlotsToPrefs(context, updated)
        _slotConfigs.value = updated

        try {
            StudySlotWorkScheduler.scheduleAllSlots(context.applicationContext)
        } catch (_: Exception) {
        }
    }

    fun updateSlotTime(
        context: Context,
        slotIndex: Int,
        hour24: Int,
        minute: Int,
        isEnabled: Boolean = true
    ) {
        ensureInitialized(context)
        val clampedHour = ((hour24 % 24) + 24) % 24
        val clampedMin = ((minute % 60) + 60) % 60
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("slot_${slotIndex}_hour24", clampedHour)
            .putInt("slot_${slotIndex}_minute", clampedMin)
            .putBoolean("slot_alarm_${slotIndex + 1}", isEnabled)
            .apply()

        // Clear lastTriggeredStamp so if user sets it to the current or next minute it will ring cleanly
        lastTriggeredStamp = ""

        _slotConfigs.update { current ->
            current.map { item ->
                if (item.slotIndex == slotIndex) {
                    item.copy(hour24 = clampedHour, minute = clampedMin, isEnabled = isEnabled)
                } else item
            }
        }

        try {
            StudySlotWorkScheduler.scheduleSlot(context.applicationContext, getSlotConfig(slotIndex))
        } catch (_: Exception) {
        }
    }

    fun toggleSlotEnabled(context: Context, slotIndex: Int, enabled: Boolean) {
        ensureInitialized(context)
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("slot_alarm_${slotIndex + 1}", enabled)
            .apply()

        _slotConfigs.update { current ->
            current.map { item ->
                if (item.slotIndex == slotIndex) item.copy(isEnabled = enabled) else item
            }
        }
    }

    /**
     * Called every second from MainActivity to check if the current clock matches any enabled Study Slot time.
     * When the time arrives, fires the status bar notification and starts the 30-second full-volume Mock Drill Siren.
     */
    fun checkAndTriggerDueSlotAlarms(context: Context) {
        ensureInitialized(context)
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        val matchingSlot = _slotConfigs.value.firstOrNull { slot ->
            slot.isEnabled && slot.hour24 == hour24 && slot.minute == minute
        } ?: return

        val stamp = "${dayOfYear}_slot${matchingSlot.slotIndex}_${hour24}_${minute}"
        if (stamp == lastTriggeredStamp) return
        lastTriggeredStamp = stamp

        triggerSlotAlarmWithThirtySecMockDrill(
            context = context,
            slotIndex = matchingSlot.slotIndex,
            slotTitle = matchingSlot.title,
            scheduledTimeLabel = matchingSlot.formattedTime
        )
    }

    /**
     * Fires the high-priority Android Notification AND starts the 30-second Full-Volume Mock Drill Audio Siren.
     */
    fun triggerSlotAlarmWithThirtySecMockDrill(
        context: Context,
        slotIndex: Int,
        slotTitle: String,
        scheduledTimeLabel: String
    ) {
        val appContext = context.applicationContext
        ensureInitialized(appContext)
        val policy = _drillPolicy.value

        val extraInfo = if (policy.dynamicallyAdjust) {
            " • Dynamic Intensity: ${policy.strugglingSeverity.label} (${policy.strugglingTopic} • ${policy.activePlanDays}-Day Plan)"
        } else {
            " • Fixed Standard Frequency (Do Not Adjust)"
        }

        // 1. Send High-Priority System Notification
        triggerStudySlotReminderNotification(
            context = appContext,
            slotTitle = "🚨 STUDY SLOT ${slotIndex + 1} ($scheduledTimeLabel) • MOCK DRILL!",
            slotBody = "Time for $slotTitle! 30-Sec Full-Volume Mock Drill Siren is ringing$extraInfo — Start your Study Slot now!",
            notificationId = 2040 + slotIndex
        )

        // 2. Start 30-Second Full-Volume Mock Drill Audio Siren
        startThirtySecondMockDrillAudio(
            context = appContext,
            slotIndex = slotIndex,
            slotTitle = slotTitle,
            scheduledTimeLabel = scheduledTimeLabel
        )
    }

    fun startThirtySecondMockDrillAudio(
        context: Context,
        slotIndex: Int,
        slotTitle: String,
        scheduledTimeLabel: String
    ) {
        stopMockDrillAudio()

        val appContext = context.applicationContext
        ensureInitialized(appContext)

        val policy = _drillPolicy.value
        val isDynamic = policy.dynamicallyAdjust
        val effectiveFreqHigh = policy.effectiveFreqHigh
        val effectiveFreqLow = policy.effectiveFreqLow
        val chunkDurationMs = policy.effectiveChunkMs
        val intensityLabel = if (isDynamic) {
            "${policy.strugglingSeverity.label} • ${policy.strugglingTopic}"
        } else {
            "Standard Locked (Do Not Adjust)"
        }

        _activeMockDrill.value = ActiveMockDrillState(
            slotIndex = slotIndex,
            slotTitle = slotTitle,
            scheduledTimeLabel = scheduledTimeLabel,
            remainingSeconds = 30,
            totalSeconds = 30,
            isDynamicAdjustmentEnabled = isDynamic,
            activePlanDays = policy.activePlanDays,
            currentDayInPlan = policy.currentDayInPlan,
            strugglingTopic = policy.strugglingTopic,
            effectiveFreqHighHz = effectiveFreqHigh,
            effectiveFreqLowHz = effectiveFreqLow,
            intensityLabel = intensityLabel
        )

        // Boost device volume to 100% FULL VOLUME for both MUSIC and ALARM streams
        try {
            val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager != null) {
                val maxMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxMusic, 0)
                val maxAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarm, 0)
                val maxNotification = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxNotification, 0)
            }
        } catch (_: Exception) {
        }

        // Trigger Vibrator pulse if available
        try {
            val vibrator = appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                val pattern = longArrayOf(0, 400, 200, 400, 200, 400, 200, 400, 200, 400)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (_: Exception) {
        }

        // Countdown timer job (30 seconds)
        drillJob = scope.launch {
            for (remaining in 30 downTo 1) {
                _activeMockDrill.update { current ->
                    current?.copy(remainingSeconds = remaining)
                }
                delay(1000L)
            }
            stopMockDrillAudio(appContext)
        }

        // Dedicated Full-Volume Emergency Mock Drill Audio Synthesizer Job (30s)
        audioSynthesisJob = scope.launch(Dispatchers.IO) {
            // Engine A: Direct PCM Emergency Siren via AudioTrack on STREAM_MUSIC (100% audible in emulator & devices)
            try {
                val sampleRate = 44100
                val chunkMs = 250
                val samplesPerChunk = (sampleRate * chunkMs) / 1000
                val minBufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(samplesPerChunk * 4)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    activeAudioTrack = track
                    track.play()

                    val highToneBuffer = ShortArray(samplesPerChunk)
                    val lowToneBuffer = ShortArray(samplesPerChunk)
                    for (i in 0 until samplesPerChunk) {
                        val t = i.toDouble() / sampleRate
                        // Sharp alternating dual-tone emergency siren wave (960Hz / 680Hz)
                        val waveHigh = sin(2.0 * PI * 960.0 * t) * 0.75 + sin(4.0 * PI * 960.0 * t) * 0.25
                        val waveLow = sin(2.0 * PI * 680.0 * t) * 0.75 + sin(4.0 * PI * 680.0 * t) * 0.25
                        highToneBuffer[i] = (waveHigh * Short.MAX_VALUE * 0.98).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        lowToneBuffer[i] = (waveLow * Short.MAX_VALUE * 0.98).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }

                    var step = 0
                    val maxSteps = (30 * 1000) / chunkMs
                    while (isActive && step < maxSteps && _activeMockDrill.value != null) {
                        val bufferToPlay = if (step % 2 == 0) highToneBuffer else lowToneBuffer
                        track.write(bufferToPlay, 0, bufferToPlay.size)
                        step++
                    }
                }
            } catch (_: Exception) {
            }

            // Engine B: ToneGenerator emergency chime
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                activeToneGen = toneGen
                var elapsed = 0
                while (isActive && elapsed < 30 && _activeMockDrill.value != null) {
                    toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 900)
                    delay(1000L)
                    elapsed++
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun generateSirenWav(
        outFile: File,
        freqHigh: Double,
        freqLow: Double,
        chunkDurationMs: Int,
        sampleRate: Int = 44100
    ) {
        try {
            val totalDurationMs = 2000 // 2-second looping cycle
            val totalSamples = (sampleRate * totalDurationMs) / 1000
            val samplesPerChunk = (sampleRate * chunkDurationMs) / 1000
            val samples = ShortArray(totalSamples)

            for (i in 0 until totalSamples) {
                val chunkIndex = i / samplesPerChunk.coerceAtLeast(1)
                val isHigh = chunkIndex % 2 == 0
                val freq = if (isHigh) freqHigh else freqLow
                val t = i.toDouble() / sampleRate
                val wave = sin(2.0 * PI * freq * t) * 0.75 + sin(4.0 * PI * freq * t) * 0.25
                samples[i] = (wave * Short.MAX_VALUE * 0.95).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val totalAudioLen = samples.size * 2
            val totalDataLen = totalAudioLen + 36
            val byteRate = sampleRate * 2

            val header = ByteArray(44)
            header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = ((totalDataLen shr 8) and 0xff).toByte()
            header[6] = ((totalDataLen shr 16) and 0xff).toByte()
            header[7] = ((totalDataLen shr 24) and 0xff).toByte()
            header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
            header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
            header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
            header[20] = 1; header[21] = 0
            header[22] = 1; header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()
            header[32] = 2; header[33] = 0
            header[34] = 16; header[35] = 0
            header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
            header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
            header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

            FileOutputStream(outFile).use { fos ->
                fos.write(header)
                val byteBuffer = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
                for (s in samples) {
                    byteBuffer.putShort(s)
                }
                fos.write(byteBuffer.array())
                fos.flush()
            }
        } catch (_: Exception) {
        }
    }

    fun stopMockDrillAudio(context: Context? = null) {
        drillJob?.cancel()
        drillJob = null
        audioSynthesisJob?.cancel()
        audioSynthesisJob = null
        _activeMockDrill.value = null
        try {
            activeRingtone?.stop()
        } catch (_: Exception) {
        }
        activeRingtone = null
        try {
            activeMediaPlayer?.stop()
            activeMediaPlayer?.release()
        } catch (_: Exception) {
        }
        activeMediaPlayer = null
        try {
            activeAudioTrack?.pause()
            activeAudioTrack?.flush()
            activeAudioTrack?.release()
        } catch (_: Exception) {
        }
        activeAudioTrack = null
        try {
            activeToneGen?.release()
        } catch (_: Exception) {
        }
        activeToneGen = null

        // Cancel the non-dismissible notification once the 30-sec drill has finished
        context?.let { ctx ->
            try {
                val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                nm?.cancel(1042)
                for (i in 0..3) {
                    nm?.cancel(2040 + i)
                }
            } catch (_: Exception) {
            }
        }
    }
}

fun sendStudySlotNotification(
    context: Context,
    title: String,
    message: String,
    notificationId: Int = 1042
): Boolean {
    return triggerStudySlotReminderNotification(
        context = context,
        slotTitle = title,
        slotBody = message,
        notificationId = notificationId
    )
}

fun triggerStudySlotReminderNotification(
    context: Context,
    slotTitle: String = "Slot 1 (06:00 AM) • Deep Focus Sprint",
    slotBody: String = "Time to start your study slot! 30-Sec Full-Volume Mock Drill is ringing.",
    notificationId: Int = 1042,
    slotIndex: Int = 0
): Boolean {
    try {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                STUDY_REMINDER_CHANNEL_ID,
                "Study Slot Emergency Alarms (Full-Screen)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-importance alarm notifications with full-screen intent that bypass silent mode"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 200, 800, 200, 800)
                setSound(alarmSoundUri, audioAttributes)
                setBypassDnd(true) // Bypasses Do Not Disturb & Silent Mode
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }

        // Full-Screen Intent directly launching MainActivity on lockscreen/heads-up
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_STUDY_SLOT_ALARM"
            putExtra("EXTRA_SLOT_INDEX", slotIndex)
            putExtra("EXTRA_SLOT_TITLE", slotTitle)
            putExtra("EXTRA_NOTIFICATION_ID", notificationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, STUDY_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(slotTitle)
            .setContentText(slotBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(slotBody))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true) // High-importance Full-Screen Intent capability
            .setOngoing(true) // Non-dismissible while the emergency 30s drill is ringing
            .setAutoCancel(false)
            .setSound(alarmSoundUri, AudioManager.STREAM_ALARM)
            .addAction(
                R.mipmap.ic_launcher,
                "OPEN STUDY DRILL",
                fullScreenPendingIntent
            )
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
        return true
    } catch (_: Exception) {
        return false
    }
}

/**
 * Prominent banner shown at the top of the app whenever a 30-second Study Slot Mock Drill Audio is ringing.
 */
@Composable
fun ActiveMockDrillRingingBanner(
    modifier: Modifier = Modifier
) {
    val activeDrill by StudySlotAlarmManager.activeMockDrill.collectAsState()
    val drill = activeDrill ?: return

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
        border = BorderStroke(1.5.dp, Color(0xFFF87171)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("active_mock_drill_banner")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF991B1B), Color(0xFFDC2626), Color(0xFFB91C1C))
                    )
                )
                .padding(14.dp)
        ) {
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
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Mock Drill Ringing",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = Color(0xFFFEF08A)
                            ) {
                                Text(
                                    text = "🚨 30s MOCK DRILL • 100% FULL VOLUME",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF7F1D1D),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${drill.remainingSeconds}s left",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFEF08A)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${drill.slotTitle} (${drill.scheduledTimeLabel})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Black.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, Color(0xFFFEF08A).copy(alpha = 0.7f)),
                    modifier = Modifier.testTag("unstoppable_mock_drill_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Automated Mock Drill",
                            tint = Color(0xFFFEF08A),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Priority Alert",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFEF08A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (drill.remainingSeconds.toFloat() / drill.totalSeconds.toFloat()).coerceIn(0f, 1f) },
                color = Color(0xFFFEF08A),
                trackColor = Color.White.copy(alpha = 0.25f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.35f)
                ) {
                    Text(
                        text = "🔒 30s TIMED FOCUS DRILL • ACTIVE",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFEF08A),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "Auto-stops in ${drill.remainingSeconds}s",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Interactive Dialog to customize a Study Slot's exact alarm time (Hour, Minute, AM/PM)
 * and test the 30-second full-volume Mock Drill Audio + Notification.
 */
@Composable
fun StudySlotTimeCustomizerDialog(
    slotIndex: Int,
    slotCustomTitle: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    StudySlotAlarmManager.ensureInitialized(context)
    val configs by StudySlotAlarmManager.slotConfigs.collectAsState()
    val isNewSlot = slotIndex < 0
    val config = if (!isNewSlot) {
        configs.getOrElse(slotIndex) { StudySlotAlarmManager.getSlotConfig(slotIndex) }
    } else {
        StudySlotAlarmConfig(
            slotIndex = configs.size,
            title = "New Study Slot",
            description = "Automated 30s Study Drill Alert",
            hour24 = 7,
            minute = 0,
            isEnabled = true
        )
    }

    val initialHour24 = config.hour24
    val initialIsPm = initialHour24 >= 12
    val initialHour12 = when {
        initialHour24 == 0 -> 12
        initialHour24 > 12 -> initialHour24 - 12
        else -> initialHour24
    }

    var selectedHour12 by remember(config.hour24) { mutableIntStateOf(initialHour12) }
    var selectedMinute by remember(config.minute) { mutableIntStateOf(config.minute) }
    var isPm by remember(config.hour24) { mutableStateOf(initialIsPm) }
    val displayTitle = if (isNewSlot) (slotCustomTitle ?: "") else (slotCustomTitle?.takeIf { it.isNotBlank() } ?: config.title)
    var enteredTopic by remember(displayTitle) { mutableStateOf(displayTitle) }

    fun computeHour24(h12: Int, pm: Boolean): Int {
        val clean12 = h12.coerceIn(1, 12)
        return when {
            pm && clean12 < 12 -> clean12 + 12
            !pm && clean12 == 12 -> 0
            else -> clean12
        }
    }

    val previewHour24 = computeHour24(selectedHour12, isPm)
    val previewFormatted = formatTime12Hour(previewHour24, selectedMinute)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        val targetIdx = if (isNewSlot) configs.size else slotIndex
        StudySlotAlarmManager.triggerSlotAlarmWithThirtySecMockDrill(
            context = context,
            slotIndex = targetIdx,
            slotTitle = enteredTopic.ifBlank { if (isNewSlot) "Study Slot" else displayTitle },
            scheduledTimeLabel = previewFormatted
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(16.dp)
                .testTag("study_slot_time_customizer_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isNewSlot) "Add Study Slot" else "Set Study Slot ${slotIndex + 1}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Automated 30-second high-priority reminder",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Input: Study Target / Subject
                OutlinedTextField(
                    value = enteredTopic,
                    onValueChange = { enteredTopic = it },
                    label = { Text("Study Target / Chapter") },
                    placeholder = { Text("e.g. Physics - Rotational Motion & DPP") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("study_slot_topic_input")
                )

                // Live Preview Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0070F3).copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SCHEDULED START TIME",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0070F3),
                                letterSpacing = 0.7.sp
                            )
                            Text(
                                text = enteredTopic.ifBlank { displayTitle },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0070F3)
                        ) {
                            Text(
                                text = previewFormatted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Hour Selection (1..12)
                Text(
                    text = "Select Start Hour (1–12):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..12).forEach { h ->
                        val isSelected = selectedHour12 == h
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.clickable { selectedHour12 = h }
                        ) {
                            Text(
                                text = String.format(Locale.ENGLISH, "%02d", h),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Minute Selection + Fine-Tune Steppers (-1m / +1m) + AM/PM
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Minute (00–59):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Fine-tune +/- 1 min buttons + AM/PM toggle
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                selectedMinute = if (selectedMinute == 0) 59 else selectedMinute - 1
                            }
                        ) {
                            Text(
                                text = "-1m",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                selectedMinute = (selectedMinute + 1) % 60
                            }
                        ) {
                            Text(
                                text = "+1m",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        listOf(false to "AM", true to "PM").forEach { (pmVal, label) ->
                            val active = isPm == pmVal
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (active) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { isPm = pmVal }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55).forEach { m ->
                        val isSelected = selectedMinute == m
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.clickable { selectedMinute = m }
                        ) {
                            Text(
                                text = String.format(Locale.ENGLISH, ":%02d", m),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // Quick Set to Next Minute button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, 1) }
                            val h24 = cal.get(Calendar.HOUR_OF_DAY)
                            val min = cal.get(Calendar.MINUTE)
                            isPm = h24 >= 12
                            selectedHour12 = when {
                                h24 == 0 -> 12
                                h24 > 12 -> h24 - 12
                                else -> h24
                            }
                            selectedMinute = min
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AlarmOn,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⚡ Quick Test (Set Alarm to Next Minute)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD97706)
                        )
                    }
                }

                // Mandatory 30-Sec Focus Chime Notice
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Automated Drill Rule: A 30-second focus alert will sound promptly at $previewFormatted to begin your scheduled session.",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 14.sp
                        )
                    }
                }

                // Action Buttons: Test 30s Mock Drill Now + Save Study Slot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (isNewSlot) {
                                StudySlotAlarmManager.addStudySlot(
                                    context = context,
                                    hour24 = previewHour24,
                                    minute = selectedMinute,
                                    title = enteredTopic
                                )
                            } else {
                                StudySlotAlarmManager.updateSlotTimeAndTarget(
                                    context = context,
                                    slotIndex = slotIndex,
                                    hour24 = previewHour24,
                                    minute = selectedMinute,
                                    topic = enteredTopic
                                )
                            }
                            val targetIdx = if (isNewSlot) configs.size else slotIndex
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                StudySlotAlarmManager.triggerSlotAlarmWithThirtySecMockDrill(
                                    context = context,
                                    slotIndex = targetIdx,
                                    slotTitle = enteredTopic.ifBlank { if (isNewSlot) "Study Slot" else displayTitle },
                                    scheduledTimeLabel = previewFormatted
                                )
                            }
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_30s_mock_drill_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Ring 30s Now",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEF4444)
                        )
                    }

                    Button(
                        onClick = {
                            if (isNewSlot) {
                                StudySlotAlarmManager.addStudySlot(
                                    context = context,
                                    hour24 = previewHour24,
                                    minute = selectedMinute,
                                    title = enteredTopic
                                )
                            } else {
                                StudySlotAlarmManager.updateSlotTimeAndTarget(
                                    context = context,
                                    slotIndex = slotIndex,
                                    hour24 = previewHour24,
                                    minute = selectedMinute,
                                    topic = enteredTopic
                                )
                            }
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_custom_slot_time_btn")
                    ) {
                        Text(
                            text = if (isNewSlot) "+ Add Slot ($previewFormatted)" else "Save ($previewFormatted)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive controller for the 30-Second Mock Drill Audio Frequency.
 * Allows the student to toggle between "Dynamically NOT Adjust (Static Frequency Lock at 960Hz/680Hz)"
 * vs "Dynamically Adjust Frequency & Intensity based on Plan Progress (30, 45, 60, 75, 90, 120, 150-Day Plans)
 * and struggling topics (increasing pitch from 960Hz up to 1536Hz)".
 */
@Composable
fun MockDrillFrequencyPlanControllerCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val policy by StudySlotAlarmManager.drillPolicy.collectAsState()
    val activeDrill by StudySlotAlarmManager.activeMockDrill.collectAsState()
    val isRinging = activeDrill != null

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (policy.dynamicallyAdjust) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.5.dp,
            if (policy.dynamicallyAdjust) Color(0xFF38BDF8) else Color(0xFF0070F3).copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("mock_drill_frequency_plan_controller_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
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
                        shape = RoundedCornerShape(10.dp),
                        color = if (policy.dynamicallyAdjust) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color(0xFF0070F3).copy(alpha = 0.12f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (policy.dynamicallyAdjust) Icons.Default.Bolt else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (policy.dynamicallyAdjust) Color(0xFF38BDF8) else Color(0xFF0070F3),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "30s Mock Drill Audio Frequency Mode",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (policy.dynamicallyAdjust) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (policy.dynamicallyAdjust)
                                "⚡ Dynamic Adaptive Pitch & Weakness Intensity active"
                            else
                                "🔒 Static Mode: Frequency dynamically NOT adjusted",
                            fontSize = 10.5.sp,
                            color = if (policy.dynamicallyAdjust) Color(0xFF7DD3FC) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (policy.dynamicallyAdjust) Color(0xFF0284C7) else Color(0xFF64748B).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (policy.dynamicallyAdjust) "ADAPTIVE" else "LOCKED",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        color = if (policy.dynamicallyAdjust) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Dual Mode Selectors: [Do Not Dynamically Adjust (Static Frequency)] vs [Dynamically Adjust (Adaptive Weakness)]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Option A: "Do Not Dynamically Adjust"
                val isStaticSelected = !policy.dynamicallyAdjust
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isStaticSelected) Color(0xFF0070F3).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(
                        1.5.dp,
                        if (isStaticSelected) Color(0xFF0070F3) else Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { StudySlotAlarmManager.toggleDynamicAdjustment(context, false) }
                        .testTag("mock_drill_do_not_adjust_mode_btn")
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isStaticSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Do Not Adjust",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isStaticSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Lock standard 960Hz / 680Hz frequency without dynamic changes.",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 12.sp
                        )
                    }
                }

                // Option B: "Dynamically Adjust Frequency"
                val isDynamicSelected = policy.dynamicallyAdjust
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDynamicSelected) Color(0xFF38BDF8).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(
                        1.5.dp,
                        if (isDynamicSelected) Color(0xFF38BDF8) else Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { StudySlotAlarmManager.toggleDynamicAdjustment(context, true) }
                        .testTag("mock_drill_dynamic_adjust_mode_btn")
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (isDynamicSelected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Dynamic Intensity",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDynamicSelected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Auto-increase siren frequency & tempo for struggling topics.",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 12.sp
                        )
                    }
                }
            }

            // Plan Progress & Horizon Selection (30, 45, 60, 75, 90, 120, 150-Day Plans)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (policy.dynamicallyAdjust) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE STUDY PLAN HORIZON (30..150 DAYS):",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (policy.dynamicallyAdjust) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${policy.activePlanDays}-Day Plan",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (policy.dynamicallyAdjust) Color(0xFF38BDF8) else Color(0xFF0070F3)
                    )
                }

                // Chips for 30, 45, 60, 75, 90, 120, 150 days
                val availablePlans = listOf(30, 45, 60, 75, 90, 120, 150)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availablePlans.forEach { days ->
                        val isSelected = policy.activePlanDays == days
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) (if (policy.dynamicallyAdjust) Color(0xFF38BDF8) else Color(0xFF0070F3))
                            else (if (policy.dynamicallyAdjust) Color(0xFF334155) else MaterialTheme.colorScheme.surface),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.clickable {
                                StudySlotAlarmManager.setActivePlanDays(context, days)
                            }
                        ) {
                            Text(
                                text = "${days}d",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else (if (policy.dynamicallyAdjust) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Plan Day Progress Steppers (-5d, -1d, +1d, +5d)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Plan Progress: Day ${policy.currentDayInPlan} of ${policy.activePlanDays}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (policy.dynamicallyAdjust) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${policy.planProgressPercent}% completed • Status: ${if (policy.planProgressPercent > 50) "Advanced Sprint" else "Early Foundation"}",
                            fontSize = 9.5.sp,
                            color = if (policy.dynamicallyAdjust) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(-5 to "-5d", -1 to "-1d", 1 to "+1d", 5 to "+5d").forEach { (delta, label) ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (policy.dynamicallyAdjust) Color(0xFF334155) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable {
                                    StudySlotAlarmManager.setCurrentDayInPlan(
                                        context,
                                        (policy.currentDayInPlan + delta).coerceIn(1, policy.activePlanDays)
                                    )
                                }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (policy.dynamicallyAdjust) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { (policy.currentDayInPlan.toFloat() / policy.activePlanDays.toFloat()).coerceIn(0f, 1f) },
                    color = if (policy.dynamicallyAdjust) Color(0xFF38BDF8) else Color(0xFF0070F3),
                    trackColor = if (policy.dynamicallyAdjust) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }

            // Struggling Topics & Weakness Intensity Selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (policy.dynamicallyAdjust) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STRUGGLING TOPIC (INCREASES DRILL INTENSITY):",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (policy.dynamicallyAdjust) Color(0xFFF87171) else Color(0xFFEF4444),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (policy.dynamicallyAdjust) "⚡ Active Weakness Boost" else "🔒 Ignored (Static Mode)",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (policy.dynamicallyAdjust) Color(0xFFFEF08A) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick high-frequency struggling topics
                val commonStrugglingTopics = listOf(
                    "Rotational Dynamics & Torque",
                    "Organic Reaction Mechanisms",
                    "Definite Integrals & Calculus",
                    "Thermodynamics & Heat",
                    "Electrostatics & Capacitors",
                    "Wave Optics & Interference",
                    "Genetics & Molecular Basis",
                    "Chemical Bonding & Hybridization"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commonStrugglingTopics.forEach { topic ->
                        val isSelected = policy.strugglingTopic == topic
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFEF4444).copy(alpha = 0.25f)
                            else (if (policy.dynamicallyAdjust) Color(0xFF334155) else MaterialTheme.colorScheme.surface),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFFEF4444) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.clickable {
                                StudySlotAlarmManager.setStrugglingTopic(context, topic, policy.strugglingSeverity)
                            }
                        ) {
                            Text(
                                text = topic,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                color = if (isSelected) (if (policy.dynamicallyAdjust) Color(0xFFFCA5A5) else Color(0xFFEF4444))
                                else (if (policy.dynamicallyAdjust) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Severity Level Selection (Standard, Moderate +15%, High +35%, Critical +60%)
                Text(
                    text = "Weakness Severity & Pitch Multiplier:",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (policy.dynamicallyAdjust) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    DrillIntensitySeverity.values().forEach { sev ->
                        val isSelected = policy.strugglingSeverity == sev
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(sev.badgeColor).copy(alpha = 0.22f)
                            else (if (policy.dynamicallyAdjust) Color(0xFF334155) else MaterialTheme.colorScheme.surface),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(sev.badgeColor) else Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    StudySlotAlarmManager.setStrugglingTopic(context, policy.strugglingTopic, sev)
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (sev) {
                                        DrillIntensitySeverity.STANDARD -> "Standard"
                                        DrillIntensitySeverity.MODERATE -> "+15%"
                                        DrillIntensitySeverity.HIGH -> "+35%"
                                        DrillIntensitySeverity.CRITICAL -> "+60%"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color(sev.badgeColor) else (if (policy.dynamicallyAdjust) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.onSurface)
                                )
                                Text(
                                    text = "${((960.0 * sev.freqMultiplier).toInt())}Hz",
                                    fontSize = 8.sp,
                                    color = if (policy.dynamicallyAdjust) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Live Audio Frequency Meter & Telemetry
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (policy.dynamicallyAdjust) Color(0xFF0284C7).copy(alpha = 0.15f) else Color(0xFF64748B).copy(alpha = 0.1f),
                border = BorderStroke(
                    1.dp,
                    if (policy.dynamicallyAdjust) Color(0xFF38BDF8).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = if (policy.dynamicallyAdjust) Color(0xFF38BDF8) else Color(0xFF0070F3),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Audio Pitch: ${policy.effectiveFreqHigh.toInt()} Hz / ${policy.effectiveFreqLow.toInt()} Hz",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (policy.dynamicallyAdjust) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Cadence: ${policy.effectiveChunkMs}ms wail alternation • Dual-Harmonic PCM Synthesizer",
                            fontSize = 9.sp,
                            color = if (policy.dynamicallyAdjust) Color(0xFF7DD3FC) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Test 30s Mock Drill Button
                    Button(
                        onClick = {
                            if (isRinging) {
                                StudySlotAlarmManager.stopMockDrillAudio()
                            } else {
                                StudySlotAlarmManager.startThirtySecondMockDrillAudio(
                                    context = context,
                                    slotIndex = 0,
                                    slotTitle = "Dynamic Mock Drill Test (${policy.strugglingTopic})",
                                    scheduledTimeLabel = "Now"
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRinging) Color(0xFFEF4444) else (if (policy.dynamicallyAdjust) Color(0xFF0284C7) else Color(0xFF0070F3))
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isRinging) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRinging) "Stop" else "Test Audio",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCenterDialog(
    isOpen: Boolean,
    viewModel: JeeViewModel? = null,
    onDismiss: () -> Unit
) {
    if (!isOpen) return
    var selectedTopTab by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(14.dp)
                .testTag("notification_center_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0070F3).copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color(0xFF0070F3),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Notification & Announcements Hub",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "30s Slot Alarms • Study Announcements • Important Notices",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Top Tab Switcher: [Study Slot Alarms (30s Drill)] vs [Central Notices & Admin Hub]
                TabRow(
                    selectedTabIndex = selectedTopTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = Color(0xFF0070F3),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTopTab]),
                            color = Color(0xFF0070F3)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTopTab == 0,
                        onClick = { selectedTopTab = 0 },
                        text = {
                            Text(
                                text = "⏰ Slot Alarms (30s Drill)",
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedTopTab == 0) FontWeight.Black else FontWeight.Bold
                            )
                        }
                    )
                    Tab(
                        selected = selectedTopTab == 1,
                        onClick = { selectedTopTab = 1 },
                        text = {
                            Text(
                                text = "📢 Announcements & Notices",
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedTopTab == 1) FontWeight.Black else FontWeight.Bold
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                when (selectedTopTab) {
                    0 -> NotificationAlarmsAndAlertsSection(onDismiss = onDismiss)
                    1 -> CentralCommunicationAndNoticeSection(viewModel = viewModel, onDismiss = onDismiss)
                }
            }
        }
    }
}

/**
 * NOTIFICATION & COMMUNICATION:
 * • Central Notification + Notice system.
 * • Admin announcements.
 * • Platform-wide notices.
 * • Personal/user-specific notifications.
 * • Read/unread notification status.
 * • Admin Communication Center (send to all users or selected users).
 * • Communication history.
 */
@Composable
fun CentralCommunicationAndNoticeSection(
    viewModel: JeeViewModel?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember(viewModel) {
        viewModel?.studyIntelligenceRepository ?: com.example.data.repository.StudyIntelligenceRepository(context.applicationContext)
    }

    val activeEmail by repo.activeUserEmail.collectAsState()
    val activeRole by repo.activeUserRole.collectAsState()
    val allNotices by repo.centralNotices.collectAsState()

    var filterType by remember { mutableStateOf("ALL") } // "ALL", "ADMIN_ANNOUNCEMENT", "PLATFORM_NOTICE", "PERSONAL_NOTIFICATION", "UNREAD"
    var showAdminComposer by remember { mutableStateOf(false) }
    var composerTitle by remember { mutableStateOf("") }
    var composerMessage by remember { mutableStateOf("") }
    var composerType by remember { mutableStateOf("PLATFORM_NOTICE") }
    var composerTargetEmail by remember { mutableStateOf("ALL") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // RLS User-Isolated Notices:
    val userVisibleNotices = remember(allNotices, activeEmail, filterType) {
        allNotices.filter { notice ->
            val isTargetedToUser = notice.targetUserEmail.equals("ALL", ignoreCase = true) ||
                notice.targetUserEmail.equals(activeEmail, ignoreCase = true)
            val isUnread = !notice.readByUserEmails.contains(activeEmail.lowercase())

            isTargetedToUser && when (filterType) {
                "ALL" -> true
                "UNREAD" -> isUnread
                else -> notice.noticeType.equals(filterType, ignoreCase = true)
            }
        }.sortedByDescending { it.createdAtTimestamp }
    }

    val unreadCount = allNotices.count { notice ->
        (notice.targetUserEmail.equals("ALL", true) || notice.targetUserEmail.equals(activeEmail, true)) &&
            !notice.readByUserEmails.contains(activeEmail.lowercase())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Status / Feedback Banner
        if (statusMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage!!,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Filter Bar & Mark All Read
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "ALL" to "All (${allNotices.size})",
                        "UNREAD" to "Unread ($unreadCount)",
                        "PLATFORM_NOTICE" to "Exam & Platform",
                        "ADMIN_ANNOUNCEMENT" to "Announcements",
                        "PERSONAL_NOTIFICATION" to "Personal Alerts"
                    ).forEach { (fVal, fLabel) ->
                        FilterChip(
                            selected = filterType == fVal,
                            onClick = { filterType = fVal },
                            label = { Text(fLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }

        // Notice Items & Communication History
        if (userVisibleNotices.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No notices in this filter.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "You are all caught up with communications!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(userVisibleNotices.size) { idx ->
                val notice = userVisibleNotices[idx]
                val isRead = notice.readByUserEmails.contains(activeEmail.lowercase())
                val isPlatformWide = notice.targetUserEmail.equals("ALL", true)

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (!isRead) Color(0xFF0070F3).copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (notice.noticeType) {
                                        "ADMIN_ANNOUNCEMENT" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                        "PLATFORM_NOTICE" -> Color(0xFF0070F3).copy(alpha = 0.15f)
                                        else -> Color(0xFF10B981).copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = when (notice.noticeType) {
                                            "ADMIN_ANNOUNCEMENT" -> "OFFICIAL ANNOUNCEMENT"
                                            "PLATFORM_NOTICE" -> "IMPORTANT UPDATE"
                                            else -> "STUDY ALERT"
                                        },
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = when (notice.noticeType) {
                                            "ADMIN_ANNOUNCEMENT" -> Color(0xFFEF4444)
                                            "PLATFORM_NOTICE" -> Color(0xFF0070F3)
                                            else -> Color(0xFF059669)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = if (isPlatformWide) "👥 ALL USERS" else "👤 PERSONAL",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Read / Unread Toggle
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isRead) Color(0xFF64748B).copy(alpha = 0.15f) else Color(0xFF0070F3).copy(alpha = 0.15f),
                                    modifier = Modifier.clickable {
                                        repo.markNoticeAsRead(notice.id, activeEmail)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isRead) Icons.Default.MarkEmailRead else Icons.Default.MarkEmailUnread,
                                            contentDescription = null,
                                            tint = if (isRead) Color(0xFF64748B) else Color(0xFF0070F3),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (isRead) "READ" else "NEW / UNREAD",
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isRead) Color(0xFF64748B) else Color(0xFF0070F3)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = notice.title,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = notice.message,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "From: ${notice.senderName} • ${notice.createdAtLabel}",
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (activeRole == "ADMIN" && notice.readByUserEmails.isNotEmpty()) {
                                Text(
                                    text = "Seen by ${notice.readByUserEmails.size} student(s)",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationAlarmsAndAlertsSection(
    onDismiss: () -> Unit,
    isEmbeddedInCombinedFeed: Boolean = false
) {
    val context = LocalContext.current
    StudySlotAlarmManager.ensureInitialized(context)
    val slotConfigs by StudySlotAlarmManager.slotConfigs.collectAsState()
    val activeDrill by StudySlotAlarmManager.activeMockDrill.collectAsState()

    val sharedPrefs = remember {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var statusBanner by remember { mutableStateOf<String?>(null) }
    var customizingSlotIndex by remember { mutableStateOf<Int?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val firstSlot = slotConfigs.firstOrNull()
        StudySlotAlarmManager.triggerSlotAlarmWithThirtySecMockDrill(
            context = context,
            slotIndex = firstSlot?.slotIndex ?: 0,
            slotTitle = firstSlot?.title ?: "Slot 1 • Morning Core Concept",
            scheduledTimeLabel = firstSlot?.formattedTime ?: "06:00 AM"
        )
        statusBanner = if (isGranted) {
            "🚨 Notification sent + 30-Sec Full-Volume Mock Drill Audio ringing!"
        } else {
            "🚨 30-Sec Full-Volume Mock Drill Audio ringing! (Enable notification permission for status bar alerts)"
        }
    }

    val smartAlertsList = remember {
        listOf(
            NotificationPref(
                id = "pref_reschedule",
                title = "Missed Slot Auto-Rescheduler Alert",
                description = "Notify when unfinished daily slots are redistributed across the next 3 days.",
                defaultEnabled = true
            ),
            NotificationPref(
                id = "pref_streak",
                title = "9:30 PM Streak Protection Warning",
                description = "Alert before midnight if today's study hours are not logged yet.",
                defaultEnabled = true
            ),
            NotificationPref(
                id = "pref_lecture",
                title = "Batch Sync & Mock Test Alerts",
                description = "Notify when synced PW / Unacademy / KGS lectures or CBT mocks go live.",
                defaultEnabled = true
            )
        )
    }

    val switchStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            smartAlertsList.forEach { pref ->
                put(pref.id, sharedPrefs.getBoolean(pref.id, pref.defaultEnabled))
            }
        }
    }

    if (customizingSlotIndex != null) {
        val targetIdx = customizingSlotIndex!!
        StudySlotTimeCustomizerDialog(
            slotIndex = targetIdx,
            onDismiss = {
                statusBanner = if (targetIdx < 0) {
                    "✅ Study slot scheduled successfully. A 30-second focus alert will sound at the designated time."
                } else {
                    val updated = StudySlotAlarmManager.getSlotConfig(targetIdx)
                    "✅ Slot ${updated.slotIndex + 1} alarm set: ${updated.formattedTime} • ${updated.title} (30s focus chime active)"
                }
                customizingSlotIndex = null
            }
        )
    }

    @Composable
    fun AlarmsAndAlertsItemsContent() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Automated 30-Second Study Slot Policy Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.2f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🚨 AUTOMATED 30-SECOND STUDY TIME SLOTS",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFEF08A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Scheduled study slots trigger an automated 30-second focus alarm at the exact designated time to maintain strict study discipline.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.92f),
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "⚡ WorkManager Active",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF6EE7B7),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF38BDF8).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "🔔 Full-Screen Intent (Bypasses Silent)",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFBAE6FD),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick WorkManager 5s Test Trigger Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        StudySlotWorkScheduler.scheduleTestAlarmWork(context, 5L, 0)
                        statusBanner = "⚡ WorkManager background task enqueued! Alarm will ring in 5 seconds with full-screen notification."
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Test WorkManager Alarm (5s Delay)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0284C7)
                    ) {
                        Text(
                            text = "RUN TEST",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MANDATORY STUDY SLOTS (AUTO 30s SIREN)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.7.sp,
                        color = Color(0xFF0070F3)
                    )
                    Text(
                        text = "30-second focus chime triggers at scheduled time",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0070F3),
                    modifier = Modifier
                        .clickable { customizingSlotIndex = -1 }
                        .testTag("add_study_slot_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "+ Add Slot",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            slotConfigs.forEach { slot ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        Color(0xFF0070F3).copy(alpha = 0.38f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Interactive Time Chip
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0070F3).copy(alpha = 0.14f),
                                        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.45f)),
                                        modifier = Modifier
                                            .clickable { customizingSlotIndex = slot.slotIndex }
                                            .testTag("customize_slot_time_chip_${slot.slotIndex + 1}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Alarm,
                                                contentDescription = "Set Time",
                                                tint = Color(0xFF0070F3),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = slot.formattedTime,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF0070F3)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Time",
                                                tint = Color(0xFF0070F3),
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = slot.title,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = slot.description,
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Locked ON status badge (No disable toggle)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Auto-Ring ON",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF059669)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Per-Slot Quick Actions: Set Time & Topic | Ring 30s Now
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { customizingSlotIndex = slot.slotIndex }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = Color(0xFF0070F3),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Set Time & Topic (${slot.formattedTime})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                                modifier = Modifier.clickable {
                                    StudySlotAlarmManager.triggerSlotAlarmWithThirtySecMockDrill(
                                        context = context,
                                        slotIndex = slot.slotIndex,
                                        slotTitle = slot.title,
                                        scheduledTimeLabel = slot.formattedTime
                                    )
                                    statusBanner = "🚨 ${slot.title} (${slot.formattedTime}) Notification + 30s Full-Volume Mock Drill ringing!"
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "30s Drill",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }

                            if (slotConfigs.size > 1) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.10f),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.25f)),
                                    modifier = Modifier.clickable {
                                        StudySlotAlarmManager.deleteStudySlot(context, slot.slotIndex)
                                        statusBanner = "🗑️ Slot ${slot.slotIndex + 1} deleted."
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Slot",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "SMART CONSISTENCY & STREAK ALERTS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            smartAlertsList.forEach { pref ->
                val isEnabled = switchStates[pref.id] ?: true
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pref.title,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = pref.description,
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                switchStates[pref.id] = checked
                                sharedPrefs.edit().putBoolean(pref.id, checked).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF0070F3)
                            )
                        )
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // If 30s Mock Drill is currently ringing, show live countdown + Stop button inside the dialog too
        AnimatedVisibility(visible = activeDrill != null) {
            activeDrill?.let { drill ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🔊 30s Mock Drill Ringing (${drill.remainingSeconds}s left • 100% Volume)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFEF4444)
                            )
                            Text(
                                text = "${drill.slotTitle} • ${drill.scheduledTimeLabel}",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Active Drill (${drill.remainingSeconds}s)", fontSize = 9.5.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }

        if (statusBanner != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF10B981).copy(alpha = 0.14f),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = statusBanner!!,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF059669),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (isEmbeddedInCombinedFeed) {
            AlarmsAndAlertsItemsContent()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AlarmsAndAlertsItemsContent()
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        val firstSlot = slotConfigs.firstOrNull()
                        StudySlotAlarmManager.triggerSlotAlarmWithThirtySecMockDrill(
                            context = context,
                            slotIndex = firstSlot?.slotIndex ?: 0,
                            slotTitle = firstSlot?.title ?: "Slot 1 • Morning Core Concept",
                            scheduledTimeLabel = firstSlot?.formattedTime ?: "06:00 AM"
                        )
                        statusBanner = "🚨 Notification sent + 30-Sec Full-Volume Mock Drill Audio ringing!"
                    }
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("send_test_study_notification_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Test 30s Mock Drill",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEF4444)
                )
            }

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Done",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

package com.example.work

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.ui.components.StudySlotAlarmConfig
import com.example.ui.components.StudySlotAlarmManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Manages WorkManager high-priority task scheduling for all defined study slots.
 * Ensures tasks trigger unskippable, full-volume alarm notifications precisely when slot times arrive.
 */
object StudySlotWorkScheduler {
    private const val TAG = "StudySlotWorkScheduler"
    private const val UNIQUE_WORK_PREFIX = "study_slot_alarm_work_"

    /**
     * Schedules a high-priority WorkManager task for the specified study slot.
     * Computes the exact delay until the upcoming slot time (today or tomorrow).
     */
    fun scheduleSlot(context: Context, slot: StudySlotAlarmConfig) {
        val appContext = context.applicationContext
        val workManager = WorkManager.getInstance(appContext)

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, slot.hour24)
            set(Calendar.MINUTE, slot.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If the time has already passed today, schedule for the same time tomorrow
            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelayMs = (target.timeInMillis - now.timeInMillis).coerceAtLeast(1000L)
        val delayMinutes = initialDelayMs / 60_000

        Log.d(TAG, "Scheduling slot ${slot.slotIndex} ('${slot.title}') for ${slot.formattedTime} in $delayMinutes min ($initialDelayMs ms)")

        val inputData = workDataOf(
            StudySlotAlarmWorker.KEY_SLOT_INDEX to slot.slotIndex,
            StudySlotAlarmWorker.KEY_SLOT_TITLE to slot.title,
            StudySlotAlarmWorker.KEY_SLOT_TIME to slot.formattedTime,
            StudySlotAlarmWorker.KEY_IS_TEST to false
        )

        val workRequest = OneTimeWorkRequestBuilder<StudySlotAlarmWorker>()
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("study_slot_alarm")
            .addTag("slot_${slot.slotIndex}")
            .build()

        workManager.enqueueUniqueWork(
            "$UNIQUE_WORK_PREFIX${slot.slotIndex}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Schedules WorkManager tasks for all active study slots.
     */
    fun scheduleAllSlots(context: Context) {
        val appContext = context.applicationContext
        StudySlotAlarmManager.ensureInitialized(appContext)
        val slots = StudySlotAlarmManager.slotConfigs.value
        Log.d(TAG, "Scheduling all ${slots.size} study slots with WorkManager")
        for (slot in slots) {
            scheduleSlot(appContext, slot)
        }
    }

    /**
     * Cancels a scheduled slot task.
     */
    fun cancelSlot(context: Context, slotIndex: Int) {
        val appContext = context.applicationContext
        WorkManager.getInstance(appContext).cancelUniqueWork("$UNIQUE_WORK_PREFIX$slotIndex")
    }

    /**
     * Triggers an immediate or short-delay test work request (e.g. 5 seconds)
     * so user can verify background execution and full-screen notification flow.
     */
    fun scheduleTestAlarmWork(
        context: Context,
        delaySeconds: Long = 5L,
        slotIndex: Int = 0
    ) {
        val appContext = context.applicationContext
        val currentSlot = StudySlotAlarmManager.getSlotConfig(slotIndex)
        val workManager = WorkManager.getInstance(appContext)

        val inputData = workDataOf(
            StudySlotAlarmWorker.KEY_SLOT_INDEX to slotIndex,
            StudySlotAlarmWorker.KEY_SLOT_TITLE to "[WORKMANAGER TEST] ${currentSlot.title}",
            StudySlotAlarmWorker.KEY_SLOT_TIME to "Now (+${delaySeconds}s)",
            StudySlotAlarmWorker.KEY_IS_TEST to true
        )

        val workRequest = OneTimeWorkRequestBuilder<StudySlotAlarmWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .setInputData(inputData)
            .addTag("study_slot_alarm_test")
            .build()

        workManager.enqueueUniqueWork(
            "study_slot_alarm_test_work",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        Log.d(TAG, "Enqueued test WorkManager alarm with ${delaySeconds}s delay")
    }
}

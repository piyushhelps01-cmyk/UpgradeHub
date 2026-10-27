package com.example.work

import android.content.Context
import android.os.PowerManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ui.components.StudySlotAlarmManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * High-priority WorkManager task that fires a non-dismissible, full-volume alarm intent
 * at the user's defined study slot times, keeping the CPU awake to guarantee execution.
 */
class StudySlotAlarmWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "StudySlotAlarmWorker"
        const val KEY_SLOT_INDEX = "slot_index"
        const val KEY_SLOT_TITLE = "slot_title"
        const val KEY_SLOT_TIME = "slot_time"
        const val KEY_IS_TEST = "is_test"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        val appContext = context.applicationContext
        val slotIndex = inputData.getInt(KEY_SLOT_INDEX, 0)
        val slotTitle = inputData.getString(KEY_SLOT_TITLE) ?: "Study Slot ${slotIndex + 1}"
        val scheduledTimeLabel = inputData.getString(KEY_SLOT_TIME) ?: ""
        val isTest = inputData.getBoolean(KEY_IS_TEST, false)

        Log.d(TAG, "StudySlotAlarmWorker executing for slot $slotIndex ($slotTitle at $scheduledTimeLabel, isTest=$isTest)")

        // Keep CPU awake for the duration of the 30-second Mock Drill Alarm
        val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "UpgradeHub:StudySlotAlarmWakeLock"
        )

        try {
            wakeLock?.acquire(35_000L) // 35 seconds safety window

            // Trigger the non-dismissible high-importance full-screen notification + full-volume 30s alarm
            withContext(Dispatchers.Main) {
                StudySlotAlarmManager.ensureInitialized(appContext)
                StudySlotAlarmManager.triggerSlotAlarmWithThirtySecMockDrill(
                    context = appContext,
                    slotIndex = slotIndex,
                    slotTitle = slotTitle,
                    scheduledTimeLabel = scheduledTimeLabel
                )
            }

            // If not a one-off test, automatically re-schedule the next day's occurrence for this slot
            if (!isTest) {
                val currentConfig = StudySlotAlarmManager.getSlotConfig(slotIndex)
                StudySlotWorkScheduler.scheduleSlot(appContext, currentConfig)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing study slot alarm worker", e)
            Result.failure()
        } finally {
            try {
                if (wakeLock?.isHeld == true) {
                    wakeLock.release()
                }
            } catch (_: Exception) {
            }
        }
    }
}

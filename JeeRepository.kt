package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ChapterChain
import com.example.data.model.NcertProgress
import com.example.data.model.SlotProgress
import com.example.data.model.TeacherDailyCheck
import com.example.data.model.WeeklyReviewData
import org.json.JSONObject

class JeeRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("JEE60_PREFS", Context.MODE_PRIVATE)

    // Slot checks
    fun getSlotCheck(dayIndex: Int, slotIndex: Int, key: String): Boolean {
        return prefs.getBoolean("daily_${dayIndex}_${slotIndex}_$key", false)
    }

    fun setSlotCheck(dayIndex: Int, slotIndex: Int, key: String, checked: Boolean) {
        prefs.edit().putBoolean("daily_${dayIndex}_${slotIndex}_$key", checked).apply()
    }

    fun getSlotProgress(dayIndex: Int, slotIndex: Int): SlotProgress {
        return SlotProgress(
            lecture = getSlotCheck(dayIndex, slotIndex, "lecture"),
            dpp = getSlotCheck(dayIndex, slotIndex, "dpp"),
            module = getSlotCheck(dayIndex, slotIndex, "module"),
            prarambh = getSlotCheck(dayIndex, slotIndex, "prarambh"),
            prabal = getSlotCheck(dayIndex, slotIndex, "prabal"),
            prakshit = getSlotCheck(dayIndex, slotIndex, "prakshit"),
            pyq = getSlotCheck(dayIndex, slotIndex, "pyq"),
            ncert = getSlotCheck(dayIndex, slotIndex, "ncert"),
            revision = getSlotCheck(dayIndex, slotIndex, "revision"),
            test = getSlotCheck(dayIndex, slotIndex, "test")
        )
    }

    // Chapter chain
    fun getChapterCheck(chapterId: Int, key: String): Boolean {
        return prefs.getBoolean("chap_${chapterId}_$key", false)
    }

    fun setChapterCheck(chapterId: Int, key: String, checked: Boolean) {
        prefs.edit().putBoolean("chap_${chapterId}_$key", checked).apply()
    }

    fun getChapterChain(chapterId: Int): ChapterChain {
        return ChapterChain(
            lecture = getChapterCheck(chapterId, "lecture"),
            notes = getChapterCheck(chapterId, "notes"),
            dpp = getChapterCheck(chapterId, "dpp"),
            ncert = getChapterCheck(chapterId, "ncert"),
            prarambh = getChapterCheck(chapterId, "prarambh"),
            prabal = getChapterCheck(chapterId, "prabal"),
            prakshit = getChapterCheck(chapterId, "prakshit"),
            pyq = getChapterCheck(chapterId, "pyq"),
            revision = getChapterCheck(chapterId, "revision"),
            test = getChapterCheck(chapterId, "test")
        )
    }

    // NCERT progress
    fun getNcertCheck(chapterId: Int, key: String): Boolean {
        return prefs.getBoolean("ncert_${chapterId}_$key", false)
    }

    fun setNcertCheck(chapterId: Int, key: String, checked: Boolean) {
        prefs.edit().putBoolean("ncert_${chapterId}_$key", checked).apply()
    }

    fun getNcertProgress(chapterId: Int): NcertProgress {
        return NcertProgress(
            read = getNcertCheck(chapterId, "read"),
            examples = getNcertCheck(chapterId, "examples"),
            markedLines = getNcertCheck(chapterId, "marked"),
            revision = getNcertCheck(chapterId, "revision")
        )
    }

    // Daily records: hours, score, notes, weak topics
    fun getDayRecord(dayIndex: Int, key: String, defaultVal: String = ""): String {
        return prefs.getString("rec_${dayIndex}_$key", defaultVal) ?: defaultVal
    }

    fun setDayRecord(dayIndex: Int, key: String, value: String) {
        prefs.edit().putString("rec_${dayIndex}_$key", value).apply()
    }

    // Teacher daily checks
    fun getTeacherCheck(dayIndex: Int, teacherId: String, key: String): Boolean {
        return prefs.getBoolean("t_${dayIndex}_${teacherId}_$key", false)
    }

    fun setTeacherCheck(dayIndex: Int, teacherId: String, key: String, checked: Boolean) {
        prefs.edit().putBoolean("t_${dayIndex}_${teacherId}_$key", checked).apply()
    }

    fun getTeacherLecNo(dayIndex: Int, teacherId: String): String {
        return prefs.getString("t_lec_${dayIndex}_$teacherId", "") ?: ""
    }

    fun setTeacherLecNo(dayIndex: Int, teacherId: String, lecNo: String) {
        prefs.edit().putString("t_lec_${dayIndex}_$teacherId", lecNo).apply()
    }

    // Weekly reviews
    fun getWeeklyReview(weekNumber: Int): WeeklyReviewData {
        val weekInfo = JeeData.WEEKS.find { it.first == weekNumber }
        val dates = weekInfo?.second ?: "Week $weekNumber"
        val startDay = (weekNumber - 1) * 7
        val endDay = minOf(weekNumber * 7 - 1, 59)
        val targetHours = (endDay - startDay + 1) * 12

        // compute sum of hours in this week from day records
        var sumHours = 0f
        for (d in startDay..endDay) {
            val hStr = getDayRecord(d, "hours", "")
            sumHours += hStr.toFloatOrNull() ?: 0f
        }

        return WeeklyReviewData(
            weekNumber = weekNumber,
            datesText = dates,
            targetHours = targetHours,
            actualHours = sumHours,
            daysWorked = prefs.getInt("weekly_${weekNumber}_worked", 0),
            pyqDone = prefs.getBoolean("weekly_${weekNumber}_pyq", false),
            modulesDone = prefs.getBoolean("weekly_${weekNumber}_modules", false),
            weakTopicsDone = prefs.getBoolean("weekly_${weekNumber}_weaks", false),
            testAnalysisDone = prefs.getBoolean("weekly_${weekNumber}_analysis", false),
            weekScore = prefs.getFloat("weekly_${weekNumber}_score", 0f),
            nextPriority = prefs.getString("weekly_${weekNumber}_next", "") ?: ""
        )
    }

    fun setWeeklyReviewFlag(weekNumber: Int, key: String, value: Boolean) {
        prefs.edit().putBoolean("weekly_${weekNumber}_$key", value).apply()
    }

    fun setWeeklyReviewString(weekNumber: Int, key: String, value: String) {
        prefs.edit().putString("weekly_${weekNumber}_$key", value).apply()
    }

    fun setWeeklyReviewScore(weekNumber: Int, score: Float) {
        prefs.edit().putFloat("weekly_${weekNumber}_score", score).apply()
    }

    // Calculator settings
    fun getWorkloadHours(): Float = prefs.getFloat("calc_workload", 1347f)
    fun setWorkloadHours(hours: Float) = prefs.edit().putFloat("calc_workload", hours).apply()

    fun getDailyRate(): Float = prefs.getFloat("calc_daily_rate", 12f)
    fun setDailyRate(rate: Float) = prefs.edit().putFloat("calc_daily_rate", rate).apply()

    // Export JSON
    fun exportAllJson(): String {
        val root = JSONObject()
        val allPrefs = prefs.all
        for ((k, v) in allPrefs) {
            when (v) {
                is Boolean -> root.put(k, v)
                is String -> root.put(k, v)
                is Int -> root.put(k, v)
                is Float -> root.put(k, v.toDouble())
            }
        }
        return root.toString(2)
    }

    // Import JSON
    fun importFromJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val editor = prefs.edit()
            editor.clear()
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = root.get(key)
                when (value) {
                    is Boolean -> editor.putBoolean(key, value)
                    is String -> editor.putString(key, value)
                    is Int -> editor.putInt(key, value)
                    is Double -> editor.putFloat(key, value.toFloat())
                    is Number -> editor.putFloat(key, value.toFloat())
                }
            }
            editor.apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Clear all
    fun resetAll() {
        prefs.edit().clear().apply()
    }
}

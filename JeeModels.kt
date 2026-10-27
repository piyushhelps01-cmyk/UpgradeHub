package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * JEE 60 - Study OS Core Data Models
 * Master targetwise planner, daily execution schedules, practice stack, NCERT tracker,
 * hours calculator, weekly audits and connected chapter flow.
 */

data class ChapterItem(
    val id: Int,
    val subject: String, // "12 Physics", "12 Maths", "12 Chemistry", "11 Physics", "11 Chemistry", "11 Maths"
    val title: String,
    val lectures: String,
    val durationHours: String,
    val questionFocus: String,
    val priority: String // "HIGH", "MED"
)

data class ChapterChain(
    val lecture: Boolean = false,
    val notes: Boolean = false,
    val dpp: Boolean = false,
    val ncert: Boolean = false,
    val prarambh: Boolean = false,
    val prabal: Boolean = false,
    val prakshit: Boolean = false,
    val pyq: Boolean = false,
    val revision: Boolean = false,
    val test: Boolean = false
) {
    fun totalTasksCount(): Int = 10

    fun isComplete(): Boolean = completedCount() == totalTasksCount()

    fun completedCount(): Int {
        var c = 0
        if (lecture) c++
        if (notes) c++
        if (dpp) c++
        if (ncert) c++
        if (prarambh) c++
        if (prabal) c++
        if (prakshit) c++
        if (pyq) c++
        if (revision) c++
        if (test) c++
        return c
    }

    fun remainingCount(): Int = (totalTasksCount() - completedCount()).coerceAtLeast(0)

    fun progressFraction(): Float = (completedCount().toFloat() / totalTasksCount().toFloat()).coerceIn(0f, 1f)

    fun remainingTaskNames(): List<String> = buildList {
        if (!lecture) add("Lecture")
        if (!notes) add("Notes")
        if (!dpp) add("DPP")
        if (!ncert) add("12th NCERT")
        if (!prarambh) add("Prarambh")
        if (!prabal) add("Prabal")
        if (!prakshit) add("Prakshit")
        if (!pyq) add("PYQ")
        if (!revision) add("Revision")
        if (!test) add("Test")
    }
}

data class DailySlot(
    val slotIndex: Int, // 0..4
    val title: String,
    val durationText: String = "3 Hours",
    val suggestedTarget: String
)

data class SlotProgress(
    val lecture: Boolean = false,
    val dpp: Boolean = false,
    val module: Boolean = false,
    val prarambh: Boolean = false,
    val prabal: Boolean = false,
    val prakshit: Boolean = false,
    val pyq: Boolean = false,
    val ncert: Boolean = false,
    val revision: Boolean = false,
    val test: Boolean = false
)

data class TeacherDailyCheck(
    val teacherName: String,
    val subjectLabel: String,
    val lectureNo: String = "",
    val dpp: Boolean = false,
    val ncert: Boolean = false,
    val pyq: Boolean = false,
    val rev: Boolean = false,
    val test: Boolean = false
)

data class DayExecutionData(
    val dayNumber: Int,
    val actualHours: Float = 0f,
    val score: Float = 0f, // 0..10
    val weakTopics: String = "",
    val notesNextDayPlan: String = "",
    val teacherLogs: Map<String, TeacherDailyCheck> = emptyMap(),
    val slotChecks: Map<Int, SlotProgress> = emptyMap()
)

data class NcertProgress(
    val read: Boolean = false,
    val examples: Boolean = false,
    val markedLines: Boolean = false,
    val revision: Boolean = false
)

data class WeeklyReviewData(
    val weekNumber: Int, // 1..9
    val datesText: String,
    val targetHours: Int,
    val actualHours: Float = 0f,
    val daysWorked: Int = 0,
    val pyqDone: Boolean = false,
    val modulesDone: Boolean = false,
    val weakTopicsDone: Boolean = false,
    val testAnalysisDone: Boolean = false,
    val weekScore: Float = 0f,
    val nextPriority: String = ""
)

data class DailyRecordRow(
    val dayNumber: Int,
    val dateText: String,
    val targetText: String,
    var hours: String = "",
    var lectures: String = "",
    var dpp: String = "",
    var module: String = "",
    var pyq: String = "",
    var score: String = "",
    var notes: String = ""
)

data class ConnectedFlowNode(
    val subject: String,
    val icon: ImageVector,
    val color: Color,
    val steps: List<String>
)

data class FacultyStack(
    val subject: String,
    val facultyName: String,
    val course: String,
    val notes: String
)

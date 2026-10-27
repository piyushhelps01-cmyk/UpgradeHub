package com.example.data.repository

import com.example.data.local.DailyStudyScheduleDao
import com.example.data.local.DailyStudyScheduleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DailyScheduleRepository(
    private val dao: DailyStudyScheduleDao
) {
    val allSchedules: Flow<List<DailyStudyScheduleEntity>> = dao.getAllSchedules()

    fun getSchedulesForDuration(durationDays: Int): Flow<List<DailyStudyScheduleEntity>> {
        return dao.getSchedulesForDuration(durationDays)
    }

    fun getScheduleForDay(dayNumber: Int): Flow<DailyStudyScheduleEntity?> {
        return dao.getScheduleForDay(dayNumber)
    }

    fun getScheduleForDayAndDuration(dayNumber: Int, durationDays: Int): Flow<DailyStudyScheduleEntity?> {
        return dao.getScheduleForDayAndDuration(dayNumber, durationDays)
    }

    suspend fun getScheduleForDaySync(dayNumber: Int): DailyStudyScheduleEntity? {
        return dao.getScheduleForDaySync(dayNumber)
    }

    suspend fun getScheduleForDayAndDurationSync(dayNumber: Int, durationDays: Int): DailyStudyScheduleEntity? {
        return dao.getScheduleForDayAndDurationSync(dayNumber, durationDays)
    }

    fun getCompletedDaysCountForDuration(durationDays: Int): Flow<Int> {
        return dao.getCompletedDaysCountForDuration(durationDays)
    }

    val completedDaysCount: Flow<Int> = dao.getCompletedDaysCount()

    val totalActualHours: Flow<Float> = dao.getTotalActualHours().map { it ?: 0f }

    val averageScore: Flow<Float> = dao.getAverageDayScore().map { it ?: 0f }

    suspend fun seedInitialSchedulesIfEmpty(isNeet: Boolean) {
        val existingCount = dao.getScheduleCount()
        if (existingCount >= 60) return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.SEPTEMBER)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.US)

        val schedules = mutableListOf<DailyStudyScheduleEntity>()

        for (day in 1..60) {
            val dateStr = dateFormat.format(calendar.time)
            val dayOfWeekStr = dayOfWeekFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)

            val weekNum = ((day - 1) / 7) + 1
            val weekMilestone = if (isNeet) {
                JeeData.NEET_WEEKS.getOrNull(weekNum - 1)?.third ?: "Milestone: High Yield Medical Revision"
            } else {
                JeeData.WEEKS.getOrNull(weekNum - 1)?.third ?: "Milestone: Advanced JEE Problem Solving"
            }

            val defaultSlots = if (isNeet) {
                when {
                    day % 4 == 1 -> listOf(
                        "Slot 1 (Botany)" to "Plant Kingdom / NCERT Line-by-Line & Diagram Flashcards",
                        "Slot 2 (Zoology)" to "Human Physiology / Active Recall & Organ System Mnemonics",
                        "Slot 3 (NEET Physics)" to "Mechanics & Formula Drills / 45 Timed MCQs",
                        "Slot 4 (Revision)" to "Daily Error Log & Backlog Clearance"
                    )
                    day % 4 == 2 -> listOf(
                        "Slot 1 (Phys Chem)" to "Solutions & Thermodynamics / Numerical Practice",
                        "Slot 2 (Botany)" to "Morphology & Anatomy / NCERT Exemplar MCQs",
                        "Slot 3 (Inorganic)" to "Coordination & Chemical Bonding / NCERT Tables",
                        "Slot 4 (Revision)" to "Formula Revision & DPP Error Review"
                    )
                    day % 4 == 3 -> listOf(
                        "Slot 1 (NEET Physics)" to "Electrostatics & Current / PYQ 2019-2025 Sprints",
                        "Slot 2 (Zoology)" to "Animal Kingdom & Cell Division / 50 MCQs",
                        "Slot 3 (Organic Chem)" to "GOC & Reaction Mechanisms / Conversions Sheet",
                        "Slot 4 (Revision)" to "Full Daily Cumulative Flashcard Review"
                    )
                    else -> listOf(
                        "Slot 1 (Full Bio Mock)" to "Botany + Zoology 90 Questions (45 Mins Sprint)",
                        "Slot 2 (Physics Sprints)" to "High Weightage Formulas & Derivations",
                        "Slot 3 (Chemistry Sprints)" to "Named Reactions & Reagents Table",
                        "Slot 4 (Analysis)" to "Mock Test Mistake Notebook Analysis"
                    )
                }
            } else {
                when {
                    day % 4 == 1 -> listOf(
                        "Slot 1 (Physics)" to "Electrostatics & Gauss Law / 35 PYQs + Formula Derivations",
                        "Slot 2 (Mathematics)" to "Matrices & Determinants / System of Equations & Cramer's Rule",
                        "Slot 3 (Physical Chem)" to "Solutions & Colligative Properties / 25 Numerical Problems",
                        "Slot 4 (Self-Study & DPP)" to "PW DPP Analysis, Mistake Log & Daily Backlog Clearance"
                    )
                    day % 4 == 2 -> listOf(
                        "Slot 1 (Inorganic Chem)" to "Coordination Compounds & CFT / Isomerism & Magnetic Moments",
                        "Slot 2 (Physics)" to "Current Electricity & Kirchhoff's Laws / Bridge & Potentiometer Drills",
                        "Slot 3 (Mathematics)" to "Relations, Functions & Inverse Trig / Domain, Range & Graphs",
                        "Slot 4 (Self-Study & DPP)" to "Error Notebook Review & Timed Mini-Mock Sprint"
                    )
                    day % 4 == 3 -> listOf(
                        "Slot 1 (Organic Chem)" to "GOC, Acidity/Basicity & Reaction Mechanisms / Electrophiles",
                        "Slot 2 (Mathematics)" to "Limits, Continuity & Differentiability / L'Hopital & Standard Forms",
                        "Slot 3 (Physics)" to "Capacitance & Dielectric Circuits / Charge Redistribution",
                        "Slot 4 (Self-Study & DPP)" to "PYQ Speed Sprint & Formula Flashcards"
                    )
                    else -> listOf(
                        "Slot 1 (Full JEE Sprint)" to "Mixed 75-Question Simulation (Physics + Chemistry + Math)",
                        "Slot 2 (Physics Deep Dive)" to "Moving Charges & Magnetism / Biot-Savart & Lorentz Force",
                        "Slot 3 (Chemistry Deep Dive)" to "Chemical Kinetics & Electrochemistry / Nernst Equation",
                        "Slot 4 (Weekly Consolidation)" to "Weak Topic Targeted Practice & Next Week Blueprint"
                    )
                }
            }

            schedules.add(
                DailyStudyScheduleEntity(
                    dayNumber = day,
                    dateText = dateStr,
                    dayOfWeek = dayOfWeekStr,
                    slot1Title = defaultSlots[0].first,
                    slot1Target = defaultSlots[0].second,
                    slot1Done = false,
                    slot1Hours = 3.0f,
                    slot2Title = defaultSlots[1].first,
                    slot2Target = defaultSlots[1].second,
                    slot2Done = false,
                    slot2Hours = 3.0f,
                    slot3Title = defaultSlots[2].first,
                    slot3Target = defaultSlots[2].second,
                    slot3Done = false,
                    slot3Hours = 3.0f,
                    slot4Title = defaultSlots[3].first,
                    slot4Target = defaultSlots[3].second,
                    slot4Done = false,
                    slot4Hours = 3.0f,
                    actualHours = 0f,
                    targetHours = 12f,
                    dayScore = 0,
                    weakTopics = "",
                    nextDayPlan = "Execute next 3-hour focus blocks strictly",
                    isCompleted = false,
                    curriculumMilestone = "Week $weekNum: $weekMilestone"
                )
            )
        }

        dao.insertSchedules(schedules)
    }

    suspend fun saveOrUpdateSchedule(schedule: DailyStudyScheduleEntity) {
        dao.insertSchedule(schedule)
    }

    suspend fun toggleSlotDone(dayNumber: Int, slotIndex: Int) {
        val existing = dao.getScheduleForDaySync(dayNumber) ?: return
        val updated = when (slotIndex) {
            0 -> existing.copy(slot1Done = !existing.slot1Done, lastUpdated = System.currentTimeMillis())
            1 -> existing.copy(slot2Done = !existing.slot2Done, lastUpdated = System.currentTimeMillis())
            2 -> existing.copy(slot3Done = !existing.slot3Done, lastUpdated = System.currentTimeMillis())
            3 -> existing.copy(slot4Done = !existing.slot4Done, lastUpdated = System.currentTimeMillis())
            else -> existing
        }

        // Auto-compute completed status if all 4 slots are done
        val allDone = updated.slot1Done && updated.slot2Done && updated.slot3Done && updated.slot4Done
        val finalSchedule = updated.copy(isCompleted = allDone)
        dao.updateSchedule(finalSchedule)
    }

    suspend fun setSlotDoneExplicit(dayNumber: Int, slotIndex: Int, isDone: Boolean) {
        val existing = dao.getScheduleForDaySync(dayNumber) ?: return
        val updated = when (slotIndex) {
            0 -> existing.copy(slot1Done = isDone, lastUpdated = System.currentTimeMillis())
            1 -> existing.copy(slot2Done = isDone, lastUpdated = System.currentTimeMillis())
            2 -> existing.copy(slot3Done = isDone, lastUpdated = System.currentTimeMillis())
            3 -> existing.copy(slot4Done = isDone, lastUpdated = System.currentTimeMillis())
            else -> existing
        }
        val allDone = updated.slot1Done && updated.slot2Done && updated.slot3Done && updated.slot4Done
        val finalSchedule = updated.copy(isCompleted = allDone)
        dao.updateSchedule(finalSchedule)
    }

    suspend fun setDayCompleted(dayNumber: Int, isCompleted: Boolean) {
        val existing = dao.getScheduleForDaySync(dayNumber) ?: return
        val updated = existing.copy(
            isCompleted = isCompleted,
            slot1Done = if (isCompleted) true else existing.slot1Done,
            slot2Done = if (isCompleted) true else existing.slot2Done,
            slot3Done = if (isCompleted) true else existing.slot3Done,
            slot4Done = if (isCompleted) true else existing.slot4Done,
            lastUpdated = System.currentTimeMillis()
        )
        dao.updateSchedule(updated)
    }

    suspend fun updateDayAudit(
        dayNumber: Int,
        actualHours: Float,
        score: Int,
        weakTopics: String,
        nextDayPlan: String
    ) {
        val existing = dao.getScheduleForDaySync(dayNumber)
        if (existing != null) {
            val updated = existing.copy(
                actualHours = actualHours,
                dayScore = score,
                weakTopics = weakTopics,
                nextDayPlan = nextDayPlan,
                lastUpdated = System.currentTimeMillis()
            )
            dao.updateSchedule(updated)
        } else {
            dao.updateDayMetrics(
                dayNumber = dayNumber,
                actualHours = actualHours,
                dayScore = score,
                weakTopics = weakTopics,
                nextDayPlan = nextDayPlan
            )
        }
    }

    suspend fun clearAll() {
        dao.clearAllSchedules()
    }
}

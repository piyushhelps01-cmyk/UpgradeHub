package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.AtomicHabitDao
import com.example.data.local.AtomicHabitEntity
import com.example.data.local.ProductivityTaskDao
import com.example.data.local.ProductivityTaskEntity
import com.example.data.local.StudentExpenseDao
import com.example.data.local.StudentExpenseEntity
import com.example.data.local.WellnessDailyLogDao
import com.example.data.local.WellnessDailyLogEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ConsumerUtilityRepository(
    private val database: AppDatabase,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val taskDao: ProductivityTaskDao = database.productivityTaskDao()
    private val expenseDao: StudentExpenseDao = database.studentExpenseDao()
    private val habitDao: AtomicHabitDao = database.atomicHabitDao()
    private val wellnessDao: WellnessDailyLogDao = database.wellnessDailyLogDao()

    val allTasks: Flow<List<ProductivityTaskEntity>> = taskDao.getAllTasks()
    val allExpenses: Flow<List<StudentExpenseEntity>> = expenseDao.getAllExpenses()
    val subscriptions: Flow<List<StudentExpenseEntity>> = expenseDao.getSubscriptions()
    val totalSpent: Flow<Double?> = expenseDao.getTotalSpent()
    val allHabits: Flow<List<AtomicHabitEntity>> = habitDao.getAllHabits()
    val latestWellnessLog: Flow<WellnessDailyLogEntity?> = wellnessDao.getLatestLog()
    val allWellnessLogs: Flow<List<WellnessDailyLogEntity>> = wellnessDao.getAllLogs()

    init {
        externalScope.launch {
            seedInitialDataIfEmpty()
        }
    }

    private suspend fun seedInitialDataIfEmpty() {
        if (taskDao.getTaskCount() == 0) {
            val initialTasks = listOf(
                ProductivityTaskEntity(
                    title = "Solve 25 JEE Advanced Rotational Dynamics PYQs",
                    quadrant = "DO_FIRST",
                    priority = "P1",
                    estimatedPomodoros = 3,
                    completedPomodoros = 1,
                    isCompleted = false,
                    category = "Physics",
                    dueDate = "Today 04:00 PM"
                ),
                ProductivityTaskEntity(
                    title = "NCERT Line-by-Line Highlight: Coordination Compounds",
                    quadrant = "SCHEDULE",
                    priority = "P2",
                    estimatedPomodoros = 2,
                    completedPomodoros = 0,
                    isCompleted = false,
                    category = "Chemistry",
                    dueDate = "Tomorrow"
                ),
                ProductivityTaskEntity(
                    title = "Print 2024 Shift-1 CBT Mock Test OMR Sheet",
                    quadrant = "DELEGATE",
                    priority = "P3",
                    estimatedPomodoros = 1,
                    completedPomodoros = 1,
                    isCompleted = true,
                    category = "Stationery",
                    dueDate = "Done"
                ),
                ProductivityTaskEntity(
                    title = "Audit Mistake Notebook: Definite Integrals Pitfalls",
                    quadrant = "DO_FIRST",
                    priority = "P1",
                    estimatedPomodoros = 2,
                    completedPomodoros = 0,
                    isCompleted = false,
                    category = "Maths",
                    dueDate = "Today 08:00 PM"
                ),
                ProductivityTaskEntity(
                    title = "Limit Telegram Discussion Group mindless scrolling",
                    quadrant = "ELIMINATE",
                    priority = "P3",
                    estimatedPomodoros = 0,
                    completedPomodoros = 0,
                    isCompleted = false,
                    category = "Wellness",
                    dueDate = "Daily"
                )
            )
            taskDao.insertTasks(initialTasks)
        }

        if (expenseDao.getExpenseCount() == 0) {
            val now = System.currentTimeMillis()
            val initialExpenses = listOf(
                StudentExpenseEntity(
                    title = "All-India Test Series (AITS 2026)",
                    amount = 4500.0,
                    category = "TEST_SERIES",
                    isSubscription = true,
                    billingCycle = "ANNUAL",
                    dateEpoch = now - 86400000L * 5,
                    notes = "15 Full Syllabus + 10 Part Syllabus Computer-Based Tests"
                ),
                StudentExpenseEntity(
                    title = "H.C. Verma Concepts of Physics Vol 1 & 2",
                    amount = 890.0,
                    category = "BOOKS",
                    isSubscription = false,
                    billingCycle = "ONE_TIME",
                    dateEpoch = now - 86400000L * 12,
                    notes = "Core Mechanics and Electromagnetism theory + problems"
                ),
                StudentExpenseEntity(
                    title = "Hostel Mess & Tiffin Monthly Pass",
                    amount = 3200.0,
                    category = "FOOD_MESS",
                    isSubscription = true,
                    billingCycle = "MONTHLY",
                    dateEpoch = now - 86400000L * 2,
                    notes = "Includes evening study snack & breakfast"
                ),
                StudentExpenseEntity(
                    title = "A4 Rough Notebooks (Bundle of 6) + Refills",
                    amount = 350.0,
                    category = "TECH_STATIONERY",
                    isSubscription = false,
                    billingCycle = "ONE_TIME",
                    dateEpoch = now - 86400000L * 18,
                    notes = "High-speed scratch work for timed problem solving"
                ),
                StudentExpenseEntity(
                    title = "Coaching Institute Revision Marathon Module",
                    amount = 2800.0,
                    category = "COACHING",
                    isSubscription = false,
                    billingCycle = "ONE_TIME",
                    dateEpoch = now - 86400000L * 25,
                    notes = "Intensive formula compendium + doubt clearing batches"
                )
            )
            expenseDao.insertExpenses(initialExpenses)
        }

        if (habitDao.getHabitCount() == 0) {
            val initialHabits = listOf(
                AtomicHabitEntity(
                    title = "Morning Formula Sheet Active Recall Drill",
                    iconName = "Flash",
                    category = "MORNING_ROUTINE",
                    targetDaysPerWeek = 7,
                    currentStreak = 16,
                    bestStreak = 24,
                    isCompletedToday = true,
                    reminderTime = "06:30 AM"
                ),
                AtomicHabitEntity(
                    title = "90-Minute Unbroken Deep Work CBT Sprint",
                    iconName = "Timer",
                    category = "STUDY_FOCUS",
                    targetDaysPerWeek = 6,
                    currentStreak = 11,
                    bestStreak = 15,
                    isCompletedToday = true,
                    reminderTime = "09:00 AM"
                ),
                AtomicHabitEntity(
                    title = "3 Liters Hydration & Eye Strain Blink Pauses",
                    iconName = "Water",
                    category = "PHYSICAL_WELLNESS",
                    targetDaysPerWeek = 7,
                    currentStreak = 8,
                    bestStreak = 12,
                    isCompletedToday = false,
                    reminderTime = "02:00 PM"
                ),
                AtomicHabitEntity(
                    title = "Nightly Mistake Log & Weak Area Tagging",
                    iconName = "Book",
                    category = "EVENING_WINDDOWN",
                    targetDaysPerWeek = 7,
                    currentStreak = 19,
                    bestStreak = 21,
                    isCompletedToday = false,
                    reminderTime = "10:15 PM"
                ),
                AtomicHabitEntity(
                    title = "Screen-Free 30 Mins Before Sleep (Mind Calm)",
                    iconName = "Moon",
                    category = "EVENING_WINDDOWN",
                    targetDaysPerWeek = 6,
                    currentStreak = 6,
                    bestStreak = 9,
                    isCompletedToday = false,
                    reminderTime = "11:00 PM"
                )
            )
            habitDao.insertHabits(initialHabits)
        }

        val todayDate = getTodayDateString()
        if (wellnessDao.getLogForDate(todayDate) == null) {
            wellnessDao.insertOrUpdateLog(
                WellnessDailyLogEntity(
                    dateString = todayDate,
                    sleepHours = 7.5f,
                    sleepQualityRating = 4,
                    waterIntakeMl = 2250,
                    targetWaterMl = 3000,
                    stressRating = 3,
                    mindfulMinutes = 12,
                    notes = "Strong mental clarity after morning Physics sprint. Stayed hydrated!"
                )
            )
        }
    }

    // --- Productivity Task Operations ---
    suspend fun addTask(
        title: String,
        quadrant: String,
        priority: String,
        estimatedPomodoros: Int,
        category: String,
        dueDate: String
    ): Long {
        return taskDao.insertTask(
            ProductivityTaskEntity(
                title = title.trim(),
                quadrant = quadrant,
                priority = priority,
                estimatedPomodoros = estimatedPomodoros,
                category = category.trim().ifEmpty { "General" },
                dueDate = dueDate.trim().ifEmpty { "Today" }
            )
        )
    }

    suspend fun toggleTaskCompleted(taskId: Long, completed: Boolean) {
        taskDao.toggleTaskCompleted(taskId, completed)
    }

    suspend fun deleteTask(task: ProductivityTaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun clearCompletedTasks() {
        taskDao.clearCompletedTasks()
    }

    // --- Student Expense Operations ---
    suspend fun addExpense(
        title: String,
        amount: Double,
        category: String,
        isSubscription: Boolean,
        billingCycle: String,
        notes: String
    ): Long {
        return expenseDao.insertExpense(
            StudentExpenseEntity(
                title = title.trim(),
                amount = amount,
                category = category,
                isSubscription = isSubscription,
                billingCycle = billingCycle,
                notes = notes.trim()
            )
        )
    }

    suspend fun deleteExpense(expense: StudentExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    // --- Atomic Habit Operations ---
    suspend fun toggleHabitCompletedToday(habit: AtomicHabitEntity) {
        val newCompleted = !habit.isCompletedToday
        val now = System.currentTimeMillis()
        val newStreak = if (newCompleted) habit.currentStreak + 1 else maxOf(0, habit.currentStreak - 1)
        val newBest = maxOf(habit.bestStreak, newStreak)
        habitDao.updateHabitProgress(habit.id, newCompleted, newStreak, newBest, if (newCompleted) now else habit.lastCompletedEpoch)
    }

    suspend fun addHabit(
        title: String,
        category: String,
        targetDaysPerWeek: Int,
        reminderTime: String
    ): Long {
        return habitDao.insertHabit(
            AtomicHabitEntity(
                title = title.trim(),
                category = category,
                targetDaysPerWeek = targetDaysPerWeek,
                currentStreak = 1,
                bestStreak = 1,
                isCompletedToday = false,
                reminderTime = reminderTime.trim().ifEmpty { "08:00 AM" }
            )
        )
    }

    suspend fun deleteHabit(habit: AtomicHabitEntity) {
        habitDao.deleteHabit(habit)
    }

    // --- Wellness Operations ---
    suspend fun logWellness(
        sleepHours: Float,
        sleepQualityRating: Int,
        waterIntakeMl: Int,
        stressRating: Int,
        mindfulMinutes: Int,
        notes: String
    ) {
        val today = getTodayDateString()
        val existing = wellnessDao.getLogForDate(today)
        val log = (existing ?: WellnessDailyLogEntity(dateString = today)).copy(
            sleepHours = sleepHours,
            sleepQualityRating = sleepQualityRating,
            waterIntakeMl = waterIntakeMl,
            stressRating = stressRating,
            mindfulMinutes = mindfulMinutes,
            notes = notes,
            loggedAtEpoch = System.currentTimeMillis()
        )
        wellnessDao.insertOrUpdateLog(log)
    }

    suspend fun addWaterIntake(amountMl: Int) {
        val today = getTodayDateString()
        val existing = wellnessDao.getLogForDate(today)
        val currentMl = existing?.waterIntakeMl ?: 0
        val newMl = (currentMl + amountMl).coerceAtLeast(0)
        if (existing == null) {
            wellnessDao.insertOrUpdateLog(
                WellnessDailyLogEntity(
                    dateString = today,
                    waterIntakeMl = newMl
                )
            )
        } else {
            wellnessDao.updateWaterForDate(today, newMl)
        }
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}

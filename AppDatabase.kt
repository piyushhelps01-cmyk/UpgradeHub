package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TestResultEntity::class,
        BookmarkEntity::class,
        StudyProgressEntity::class,
        NcertChapterEntity::class,
        DailyGoalEntity::class,
        FlashcardDeckEntity::class,
        FlashcardItemEntity::class,
        UserAccountEntity::class,
        DailyStudyScheduleEntity::class,
        PracticeQueueEntity::class,
        ChapterProgressEntity::class,
        PracticeSessionHistoryEntity::class,
        ProductivityTaskEntity::class,
        StudentExpenseEntity::class,
        AtomicHabitEntity::class,
        WellnessDailyLogEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun ncertDao(): NcertDao
    abstract fun dailyGoalDao(): DailyGoalDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun dailyStudyScheduleDao(): DailyStudyScheduleDao
    abstract fun practiceQueueDao(): PracticeQueueDao
    abstract fun chapterProgressDao(): ChapterProgressDao
    abstract fun practiceSessionHistoryDao(): PracticeSessionHistoryDao
    abstract fun productivityTaskDao(): ProductivityTaskDao
    abstract fun studentExpenseDao(): StudentExpenseDao
    abstract fun atomicHabitDao(): AtomicHabitDao
    abstract fun wellnessDailyLogDao(): WellnessDailyLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jee60_app_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

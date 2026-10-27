package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ChapterChain

/**
 * Room Entity storing individual chapter completion progress across the 10-step
 * JEE Study OS preparation chain (Lectures, Notes, DPP, NCERT, Prarambh, Prabal, Prakshit, PYQs, Revision, Test).
 */
@Entity(tableName = "chapter_progress")
data class ChapterProgressEntity(
    @PrimaryKey
    val chapterId: Int,
    val chapterTitle: String,
    val subject: String,
    val classGrade: String = "Class 12th",
    val priority: String = "HIGH",
    val lecture: Boolean = false,
    val notes: Boolean = false,
    val dpp: Boolean = false,
    val ncert: Boolean = false,
    val prarambh: Boolean = false,
    val prabal: Boolean = false,
    val prakshit: Boolean = false,
    val pyq: Boolean = false,
    val revision: Boolean = false,
    val test: Boolean = false,
    val completedTasksCount: Int = 0,
    val isCompleted: Boolean = false,
    val notesContent: String = "",
    val weakConcepts: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toChapterChain(): ChapterChain {
        return ChapterChain(
            lecture = lecture,
            notes = notes,
            dpp = dpp,
            ncert = ncert,
            prarambh = prarambh,
            prabal = prabal,
            prakshit = prakshit,
            pyq = pyq,
            revision = revision,
            test = test
        )
    }

    companion object {
        fun fromChain(
            chapterId: Int,
            chapterTitle: String,
            subject: String,
            chain: ChapterChain,
            notesContent: String = "",
            weakConcepts: String = ""
        ): ChapterProgressEntity {
            val count = chain.completedCount()
            return ChapterProgressEntity(
                chapterId = chapterId,
                chapterTitle = chapterTitle,
                subject = subject,
                lecture = chain.lecture,
                notes = chain.notes,
                dpp = chain.dpp,
                ncert = chain.ncert,
                prarambh = chain.prarambh,
                prabal = chain.prabal,
                prakshit = chain.prakshit,
                pyq = chain.pyq,
                revision = chain.revision,
                test = chain.test,
                completedTasksCount = count,
                isCompleted = chain.isComplete(),
                notesContent = notesContent,
                weakConcepts = weakConcepts,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}

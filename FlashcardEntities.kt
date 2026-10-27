package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "flashcard_decks")
data class FlashcardDeckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subject: String = "Physics", // "Physics", "Chemistry", "Mathematics", "Formulas"
    val description: String = "",
    val colorHex: Long = 0xFF6574FF,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "flashcard_items",
    foreignKeys = [
        ForeignKey(
            entity = FlashcardDeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["deckId"])]
)
data class FlashcardItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckId: Long,
    val front: String, // Formula / Concept question / Name
    val back: String,  // Mathematical equation / Law / Explanation
    val tag: String = "Formula", // "Formula", "Concept", "Shortcut", "Rule"
    val isMastered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

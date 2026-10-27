package com.example.data.model

import com.example.data.local.FlashcardDeckEntity
import com.example.data.local.FlashcardItemEntity

data class FlashcardDeckUiModel(
    val deck: FlashcardDeckEntity,
    val cardCount: Int,
    val masteredCount: Int,
    val masteryPercentage: Int
)

data class FlashcardRevisionState(
    val deck: FlashcardDeckEntity,
    val cards: List<FlashcardItemEntity>,
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val masteredInSessionCount: Int = 0,
    val isCompleted: Boolean = false
)

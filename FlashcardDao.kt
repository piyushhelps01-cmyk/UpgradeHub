package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcard_decks ORDER BY createdAt DESC")
    fun getAllDecks(): Flow<List<FlashcardDeckEntity>>

    @Query("SELECT * FROM flashcard_decks WHERE id = :deckId")
    suspend fun getDeckById(deckId: Long): FlashcardDeckEntity?

    @Query("SELECT * FROM flashcard_decks ORDER BY createdAt DESC")
    suspend fun getAllDecksSync(): List<FlashcardDeckEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: FlashcardDeckEntity): Long

    @Update
    suspend fun updateDeck(deck: FlashcardDeckEntity)

    @Query("DELETE FROM flashcard_decks WHERE id = :deckId")
    suspend fun deleteDeckById(deckId: Long)

    @Query("SELECT * FROM flashcard_items WHERE deckId = :deckId ORDER BY id ASC")
    fun getCardsForDeck(deckId: Long): Flow<List<FlashcardItemEntity>>

    @Query("SELECT * FROM flashcard_items WHERE deckId = :deckId ORDER BY id ASC")
    suspend fun getCardsForDeckSync(deckId: Long): List<FlashcardItemEntity>

    @Query("SELECT * FROM flashcard_items ORDER BY id ASC")
    fun getAllCards(): Flow<List<FlashcardItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: FlashcardItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<FlashcardItemEntity>)

    @Update
    suspend fun updateCard(card: FlashcardItemEntity)

    @Query("UPDATE flashcard_items SET isMastered = :isMastered WHERE id = :cardId")
    suspend fun setCardMastered(cardId: Long, isMastered: Boolean)

    @Query("DELETE FROM flashcard_items WHERE id = :cardId")
    suspend fun deleteCardById(cardId: Long)

    @Query("DELETE FROM flashcard_items WHERE deckId = :deckId")
    suspend fun deleteCardsForDeck(deckId: Long)

    @Query("SELECT COUNT(*) FROM flashcard_items WHERE deckId = :deckId")
    suspend fun getCardCountForDeck(deckId: Long): Int

    @Query("SELECT COUNT(*) FROM flashcard_items WHERE deckId = :deckId AND isMastered = 1")
    suspend fun getMasteredCountForDeck(deckId: Long): Int
}

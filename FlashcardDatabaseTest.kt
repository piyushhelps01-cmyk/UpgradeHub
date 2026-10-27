package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.FlashcardDao
import com.example.data.repository.FlashcardRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FlashcardDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var flashcardDao: FlashcardDao
    private lateinit var repository: FlashcardRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        flashcardDao = database.flashcardDao()
        repository = FlashcardRepository(flashcardDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testDefaultDecksSeeded() = runBlocking {
        repository.ensureDefaultDecksSeeded()
        val decks = repository.getAllDecks().first()
        assertTrue("Default decks should be seeded", decks.isNotEmpty())

        val kinematicsDeck = decks.find { it.title.contains("Kinematics") }
        assertTrue("Kinematics deck should exist", kinematicsDeck != null)

        val cards = repository.getCardsForDeck(kinematicsDeck!!.id).first()
        assertTrue("Kinematics deck should have formula cards", cards.isNotEmpty())
    }

    @Test
    fun testCreateCustomDeckAndAddCards() = runBlocking {
        val deckId = repository.createDeck(
            title = "Organic Named Reactions",
            subject = "Chemistry",
            description = "Aldol, Cannizzaro, Reimer-Tiemann",
            colorHex = 0xFFFFAB00
        )

        val decks = repository.getAllDecks().first()
        val deck = decks.find { it.id == deckId }
        assertTrue(deck != null)
        assertEquals("Organic Named Reactions", deck!!.title)
        assertEquals("Chemistry", deck.subject)

        val cardId = repository.addCard(
            deckId = deckId,
            front = "Cannizzaro Reaction Condition",
            back = "Non-enolizable aldehydes treated with conc. KOH undergo disproportionation.",
            tag = "Reaction"
        )

        val cards = repository.getCardsForDeck(deckId).first()
        assertEquals(1, cards.size)
        assertEquals("Cannizzaro Reaction Condition", cards[0].front)
        assertFalse(cards[0].isMastered)

        // Toggle mastery
        repository.setCardMastered(cardId, true)
        val updatedCards = repository.getCardsForDeck(deckId).first()
        assertTrue(updatedCards[0].isMastered)

        // Reset mastery
        repository.resetDeckMastery(deckId)
        val resetCards = repository.getCardsForDeck(deckId).first()
        assertFalse(resetCards[0].isMastered)
    }

    @Test
    fun testCascadeDeleteDeckDeletesCards() = runBlocking {
        val deckId = repository.createDeck(
            title = "Calculus Limits",
            subject = "Mathematics"
        )
        repository.addCard(deckId, "lim (sin x)/x as x->0", "1", "Formula")
        repository.addCard(deckId, "lim (1+1/n)^n as n->inf", "e", "Formula")

        var cards = repository.getCardsForDeck(deckId).first()
        assertEquals(2, cards.size)

        // Delete deck
        repository.deleteDeck(deckId)
        val decks = repository.getAllDecks().first()
        assertTrue(decks.none { it.id == deckId })

        cards = repository.getCardsForDeck(deckId).first()
        assertEquals(0, cards.size)
    }
}

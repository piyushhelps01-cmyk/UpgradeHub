package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.MotivationalQuoteRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MotivationalQuoteTest {

    private lateinit var repository: MotivationalQuoteRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = MotivationalQuoteRepository(context)
    }

    @Test
    fun testCuratedQuotesNotEmpty() {
        assertTrue(MotivationalQuoteRepository.CURATED_QUOTES.isNotEmpty())
        assertTrue(MotivationalQuoteRepository.CURATED_QUOTES.size >= 10)
    }

    @Test
    fun testDailyQuoteRotation() {
        val day1Quote = repository.getDailyQuote(1)
        val day2Quote = repository.getDailyQuote(2)

        assertNotNull(day1Quote)
        assertNotNull(day2Quote)
        assertTrue(day1Quote.text.isNotBlank())
        assertTrue(day1Quote.author.isNotBlank())
        assertTrue(day1Quote.aspirantTakeaway.isNotBlank())

        // Different days should have distinct quotes
        assertTrue(day1Quote.id != day2Quote.id)
    }

    @Test
    fun testQuoteBookmarkState() {
        val quoteId = "test_quote_1"
        assertFalse(repository.isQuoteBookmarked(quoteId))

        repository.setQuoteBookmarked(quoteId, true)
        assertTrue(repository.isQuoteBookmarked(quoteId))

        repository.setQuoteBookmarked(quoteId, false)
        assertFalse(repository.isQuoteBookmarked(quoteId))
    }

    @Test
    fun testRandomQuoteExcludesCurrent() {
        val currentQuote = repository.getDailyQuote(1)
        val nextQuote = repository.getRandomQuote(excludeId = currentQuote.id)
        assertTrue(nextQuote.id != currentQuote.id)
    }
}

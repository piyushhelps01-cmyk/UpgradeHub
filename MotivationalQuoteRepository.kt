package com.example.data.repository

import android.content.Context
import com.example.data.model.MotivationalQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MotivationalQuoteRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("jee_quotes_prefs", Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()

    companion object {
        val CURATED_QUOTES = listOf(
            MotivationalQuote(
                id = "feynman_1",
                text = "Study hard what interests you the most in the most undisciplined, irreverent and original manner possible. You don't have to understand everything at once; master the fundamental principles.",
                author = "Richard P. Feynman",
                role = "Theoretical Physicist, Nobel Laureate",
                category = "Academic Discipline",
                aspirantTakeaway = "Derive concepts from first principles instead of merely memorizing final formulas."
            ),
            MotivationalQuote(
                id = "kalam_1",
                text = "Dream is not that which you see while sleeping; it is something that does not let you sleep. If you want to shine like a sun, first burn like a sun.",
                author = "Dr. A.P.J. Abdul Kalam",
                role = "Aerospace Scientist & 11th President of India",
                category = "Engineering Rigor",
                aspirantTakeaway = "Commit fully to each 3-hour study block—relentless effort compounds into top ranks."
            ),
            MotivationalQuote(
                id = "tesla_1",
                text = "The mind is sharper and keener in seclusion and uninterrupted solitude. Originality thrives in freedom from outside influences.",
                author = "Nikola Tesla",
                role = "Electrical & Mechanical Engineer, Inventor",
                category = "Deep Focus",
                aspirantTakeaway = "Eliminate phone distractions during question-solving to develop razor-sharp focus."
            ),
            MotivationalQuote(
                id = "ramanujan_1",
                text = "An equation for me has no meaning unless it expresses a thought of profound truth. Work through the steps patiently until the solution reveals itself.",
                author = "Srinivasa Ramanujan",
                role = "Master Mathematician",
                category = "Academic Rigor",
                aspirantTakeaway = "Don't fear complex algebra or calculus. Write out each transformation line by line."
            ),
            MotivationalQuote(
                id = "einstein_1",
                text = "It's not that I'm so smart, it's just that I stay with problems longer. Persistence is the true catalyst of scientific breakthroughs.",
                author = "Albert Einstein",
                role = "Theoretical Physicist",
                category = "Problem Solving",
                aspirantTakeaway = "When stuck on a tough Level 3 Prakshit problem, pause and dissect the boundary conditions."
            ),
            MotivationalQuote(
                id = "curie_1",
                text = "Nothing in life is to be feared, it is only to be understood. Now is the time to understand more, so that we may fear less.",
                author = "Marie Curie",
                role = "Physicist & Chemist, Two-time Nobel Laureate",
                category = "Scientific Mindset",
                aspirantTakeaway = "Confront your weakest syllabus topics today rather than avoiding them."
            ),
            MotivationalQuote(
                id = "bhabha_1",
                text = "My success is not due to any special virtue, but to my habit of concentration and systematic, structured work.",
                author = "Homi J. Bhabha",
                role = "Father of the Indian Nuclear Program",
                category = "Systematic Discipline",
                aspirantTakeaway = "Consistency in daily DPPs and module problem sets outperforms sporadic cramming."
            ),
            MotivationalQuote(
                id = "shannon_1",
                text = "Information is the resolution of uncertainty. A disciplined engineer looks at chaos and systematically extracts order and clarity.",
                author = "Claude Shannon",
                role = "Father of Information Theory & Electrical Engineer",
                category = "Engineering Thinking",
                aspirantTakeaway = "Structure your rough calculation sheets cleanly to prevent silly arithmetic errors."
            ),
            MotivationalQuote(
                id = "von_neumann_1",
                text = "In mathematics, you don't understand things. You just get used to them by repeated, rigorous application.",
                author = "John von Neumann",
                role = "Polymath & Computer Pioneer",
                category = "Practice Discipline",
                aspirantTakeaway = "Familiarity and intuition in advanced math come from solving dozens of PYQs."
            ),
            MotivationalQuote(
                id = "sarabhai_1",
                text = "He who can listen to music in the midst of noise can achieve the internal harmony required for great discoveries.",
                author = "Vikram Sarabhai",
                role = "Physicist, Founder of ISRO",
                category = "Mental Composure",
                aspirantTakeaway = "Exam temperament is developed during mock test practice. Stay calm under time pressure."
            ),
            MotivationalQuote(
                id = "raman_1",
                text = "I am the master of my failure; if I never fail, how will I ever learn? Put your whole heart and soul into the experiment.",
                author = "Sir C.V. Raman",
                role = "Physicist, Nobel Laureate in Physics",
                category = "Resilience",
                aspirantTakeaway = "Review every error in your test analysis notebook—mistakes are where rank gains happen."
            ),
            MotivationalQuote(
                id = "hamilton_1",
                text = "There was no choice but to be ambitious; the flight software had to work flawlessly on the first attempt. Rigorous engineering leaves nothing to chance.",
                author = "Margaret Hamilton",
                role = "Director of Software Engineering, Apollo Project",
                category = "Engineering Precision",
                aspirantTakeaway = "Double-check units, signs, and dimensions before bubbling your final answers."
            ),
            MotivationalQuote(
                id = "lewin_1",
                text = "Physics is not about memorizing equations; it is about seeing how the universe dances according to immutable laws of nature.",
                author = "Walter Lewin",
                role = "Astrophysicist & Renowned Physics Educator",
                category = "Academic Inspiration",
                aspirantTakeaway = "Visualize mechanics and electromagnetism setups in your mind before calculating."
            ),
            MotivationalQuote(
                id = "johnson_1",
                text = "Like what you do, and then you will do your best. Check and re-check every calculation with unwavering discipline.",
                author = "Katherine Johnson",
                role = "NASA Orbital Mechanics Mathematician",
                category = "Mathematical Precision",
                aspirantTakeaway = "Accuracy is as valuable as speed in JEE. Guard against careless calculation traps."
            ),
            MotivationalQuote(
                id = "oppenheimer_1",
                text = "No one can predict where curiosity and intense problem-solving will lead. Cultivate discipline, and the breakthroughs will follow.",
                author = "J. Robert Oppenheimer",
                role = "Theoretical Physicist",
                category = "Deep Inquiry",
                aspirantTakeaway = "Treat each difficult practice problem as an intellectual adventure rather than a chore."
            )
        )
    }

    /**
     * Get the deterministic quote for a sprint day (1..60).
     */
    fun getDailyQuote(dayNumber: Int): MotivationalQuote {
        val index = (dayNumber - 1).coerceAtLeast(0) % CURATED_QUOTES.size
        return CURATED_QUOTES[index]
    }

    /**
     * Get quote by index or randomized for fresh inspiration.
     */
    fun getRandomQuote(excludeId: String = ""): MotivationalQuote {
        val candidates = CURATED_QUOTES.filter { it.id != excludeId }
        return candidates.randomOrNull() ?: CURATED_QUOTES.first()
    }

    /**
     * Attempts to fetch an online quote from a public endpoint,
     * gracefully falling back to a fresh curated engineering discipline quote if offline or timed out.
     */
    suspend fun fetchOnlineOrCuratedQuote(): MotivationalQuote = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://zenquotes.io/api/random")
                .header("User-Agent", "JeeSprintPlanner/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val jsonArray = JSONArray(body)
                    if (jsonArray.length() > 0) {
                        val obj = jsonArray.getJSONObject(0)
                        val quoteText = obj.optString("q", "")
                        val author = obj.optString("a", "Unknown")
                        if (quoteText.isNotBlank()) {
                            return@withContext MotivationalQuote(
                                id = "online_${System.currentTimeMillis()}",
                                text = quoteText,
                                author = author,
                                role = "Inspiring Scholar",
                                category = "Academic Discipline",
                                aspirantTakeaway = "Channel this wisdom into your next 3-hour problem-solving session."
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Offline or network timeout - fallback smoothly to curated list
        }

        // Return a fresh curated quote
        getRandomQuote()
    }

    fun isQuoteBookmarked(quoteId: String): Boolean {
        return prefs.getBoolean("bookmark_$quoteId", false)
    }

    fun setQuoteBookmarked(quoteId: String, bookmarked: Boolean) {
        prefs.edit().putBoolean("bookmark_$quoteId", bookmarked).apply()
    }
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.export.PdfExportImportManager
import com.example.data.export.PdfImportOutcome
import com.example.data.local.DailyGoalEntity
import com.example.data.repository.BatchCurriculumRepository
import com.example.ui.viewmodel.JeeUiState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PdfExportImportTest {

    private lateinit var context: Context
    private lateinit var pdfManager: PdfExportImportManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        pdfManager = PdfExportImportManager(context)
    }

    @Test
    fun testPdfExportGeneratesFile() = runBlocking {
        val uiState = JeeUiState(selectedDay = 10, totalFocusedHours = 24.5f, overallCompletionPct = 35)
        val batch = BatchCurriculumRepository.PRESET_BATCHES.first()
        val dummyGoals = listOf(
            DailyGoalEntity(dayNumber = 10, title = "Electrostatics Lecture 1", targetHours = 3f, slotIndex = 0),
            DailyGoalEntity(dayNumber = 10, title = "DPP Problem Set 1", targetHours = 3f, isCompleted = true, slotIndex = 1)
        )
        val dummySnapshot = """{"version":1,"records":{"daily_0_0_lecture":true}}"""

        val generatedFile = pdfManager.exportPdfReport(
            uiState = uiState,
            activeBatch = batch,
            targetDays = 60,
            dailyHours = 12f,
            goals = dummyGoals,
            fullSnapshotJson = dummySnapshot
        )

        assertNotNull("Generated PDF file should not be null", generatedFile)
        assertTrue("PDF file should exist on disk", generatedFile.exists())
        assertTrue("PDF file should have non-zero size", generatedFile.length() > 0)
        assertTrue("PDF file should have .pdf extension", generatedFile.name.endsWith(".pdf"))
    }

    @Test
    fun testPdfImportRecoversEmbeddedSnapshot() = runBlocking {
        val testJson = """{"testKey":"UpgradeHubValue123","totalHours":45.0}"""
        val rawContent = "Some text\n" + PdfExportImportManager.PAYLOAD_START + testJson + PdfExportImportManager.PAYLOAD_END

        val result = pdfManager.parsePdfStream(rawContent.byteInputStream())
        assertTrue("Outcome should be SnapshotRestored", result is PdfImportOutcome.SnapshotRestored)

        val restored = result as PdfImportOutcome.SnapshotRestored
        assertEquals(testJson, restored.rawJson)
    }

    @Test
    fun testPdfImportExtractsSyllabusText() = runBlocking {
        val sampleSyllabus = """
            JEE Advanced Lakshya 2026 Batch Curriculum Plan
            Physics: Mechanics, Waves, Thermodynamics (35 Lectures)
            Chemistry: Physical Chemistry, Inorganic Chemistry (40 Lectures)
            Mathematics: Calculus, Coordinate Geometry, Algebra (45 Lectures)
        """.trimIndent()

        val result = pdfManager.parsePdfStream(sampleSyllabus.byteInputStream())
        assertTrue("Outcome should be SyllabusImported", result is PdfImportOutcome.SyllabusImported)

        val imported = result as PdfImportOutcome.SyllabusImported
        assertTrue("Title should be extracted", imported.title.isNotEmpty())
        assertTrue("Lectures should be parsed", imported.totalLectures > 0)
    }
}

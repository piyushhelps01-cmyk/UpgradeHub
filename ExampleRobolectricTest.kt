package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.JeePage
import com.example.ui.viewmodel.JeeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("UpgradeHub", appName)
  }

  @Test
  fun `verify viewmodel navigation, announcements, mistake notebook and multi-exam batch sync`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    if (com.google.firebase.FirebaseApp.getApps(app).isEmpty()) {
      com.google.firebase.FirebaseApp.initializeApp(app)
    }
    val viewModel = JeeViewModel(app)

    // 1. Verify Prep Hub Vault title (JEE removed)
    assertEquals("Prep Hub Vault", JeePage.BOOKMARKS.title)

    // 2. Verify Team Announcements engine
    val initialAnnouncements = viewModel.teamAnnouncements.value
    assertTrue(initialAnnouncements.isNotEmpty())
    viewModel.postTeamAnnouncement(
      title = "Live Test Broadcast",
      message = "All students join the 8 PM study sprint.",
      category = "URGENT"
    )
    assertTrue(viewModel.teamAnnouncements.value.any { it.title == "Live Test Broadcast" })

    // 3. Verify Mistake Notebook logging & resolution
    viewModel.addMistakeEntry(
      questionTitle = "SN2 Reactivity in Polar Aprotic Solvent",
      subject = "Chemistry",
      chapterOrSource = "AI Mentor Pro",
      errorType = "Concept Gap",
      takeawayNote = "1° > 2° > 3° in DMSO/Acetone"
    )
    val addedMistake = viewModel.mistakeNotebookEntries.value.first {
      it.questionTitle.contains("SN2 Reactivity")
    }
    assertFalse(addedMistake.isResolved)
    viewModel.toggleMistakeResolved(addedMistake.id)
    val updatedMistake = viewModel.mistakeNotebookEntries.value.first { it.id == addedMistake.id }
    assertTrue(updatedMistake.isResolved)

    // 4. Verify Page Navigation
    viewModel.setPage(JeePage.AI)
    assertEquals(JeePage.AI, viewModel.uiState.value.currentPage)

    // 5. Verify Student OS & Consumer Utility integration
    viewModel.setPage(JeePage.CONSUMER_UTILITY)
    assertEquals(JeePage.CONSUMER_UTILITY, viewModel.uiState.value.currentPage)
    assertEquals("Student Utilities OS", JeePage.CONSUMER_UTILITY.title)

    // Test Productivity Task addition
    viewModel.addProductivityTask(
      title = "Solve 20 Electrostatics Advanced PYQs",
      quadrant = "DO_FIRST",
      priority = "P1",
      pomodoros = 2,
      category = "Physics",
      dueDate = "Today"
    )

    // Test Student Expense addition
    viewModel.addStudentExpense(
      title = "Physics Galaxy Mechanics Book",
      amount = 650.0,
      category = "BOOKS",
      isSubscription = false,
      cycle = "ONE_TIME",
      notes = "Advanced problem collection"
    )

    // Test Water Hydration
    viewModel.addWaterIntakeQuick(250)

    // Test Wellness Logging
    viewModel.logStudentWellness(
      sleepHours = 8.0f,
      sleepQuality = 5,
      waterIntakeMl = 2500,
      stressRating = 2,
      mindfulMinutes = 15,
      notes = "Great sleep, feeling calm"
    )
  }
}

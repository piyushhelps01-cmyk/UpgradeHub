package com.example

import com.example.security.HiddenAdminTapTracker
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HiddenAdminTapTrackerTest {
    @Test
    fun ninthDeliberateTapWithinFiveSecondsRevealsGateOnce() {
        val tracker = HiddenAdminTapTracker()

        for (tap in 0 until 8) {
            assertFalse(tracker.recordTap(tap * 150L))
        }
        assertTrue(tracker.recordTap(1_200L))
        assertFalse(tracker.recordTap(1_350L))
    }

    @Test
    fun sequenceRestartsAfterFiveSecondWindow() {
        val tracker = HiddenAdminTapTracker()

        for (tap in 0 until 8) {
            assertFalse(tracker.recordTap(tap * 500L))
        }
        // This tap is more than five seconds after the sequence began, so it starts a new one.
        assertFalse(tracker.recordTap(5_500L))
        for (tap in 1 until 8) {
            assertFalse(tracker.recordTap(5_500L + tap * 150L))
        }
        assertTrue(tracker.recordTap(6_700L))
    }

    @Test
    fun rapidDuplicateTapsDoNotAdvanceSequence() {
        val tracker = HiddenAdminTapTracker()

        assertFalse(tracker.recordTap(0L))
        assertFalse(tracker.recordTap(50L))
        for (tap in 1 until 9) {
            val revealed = tracker.recordTap(tap * 150L)
            assertTrue(revealed == (tap == 8))
        }
    }

    @Test
    fun resetClearsPartialSequence() {
        val tracker = HiddenAdminTapTracker()

        repeat(8) { assertFalse(tracker.recordTap(it * 150L)) }
        tracker.reset()
        repeat(8) { assertFalse(tracker.recordTap(2_000L + it * 150L)) }
        assertTrue(tracker.recordTap(3_200L))
    }
}

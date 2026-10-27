package com.example.security

/**
 * Tracks only the local gesture that reveals the admin sign-in gate.
 * This class never authenticates a user or grants a role.
 */
class HiddenAdminTapTracker(
    private val requiredTapCount: Int = 9,
    private val windowMillis: Long = 5_000L,
    private val duplicateTapGuardMillis: Long = 120L
) {
    private var firstTapAt: Long? = null
    private var lastAcceptedTapAt: Long? = null
    private var acceptedTapCount: Int = 0

    fun recordTap(nowMillis: Long): Boolean {
        val first = firstTapAt
        if (first == null || nowMillis < first || nowMillis - first > windowMillis) {
            reset()
            firstTapAt = nowMillis
            lastAcceptedTapAt = nowMillis
            acceptedTapCount = 1
            return false
        }

        val last = lastAcceptedTapAt
        if (last != null && nowMillis < last) {
            reset()
            firstTapAt = nowMillis
            lastAcceptedTapAt = nowMillis
            acceptedTapCount = 1
            return false
        }
        if (last != null && nowMillis - last < duplicateTapGuardMillis) {
            return false
        }

        lastAcceptedTapAt = nowMillis
        acceptedTapCount += 1
        if (acceptedTapCount >= requiredTapCount) {
            reset()
            return true
        }
        return false
    }

    fun reset() {
        firstTapAt = null
        lastAcceptedTapAt = null
        acceptedTapCount = 0
    }
}

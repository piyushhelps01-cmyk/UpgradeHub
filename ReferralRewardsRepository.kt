package com.example.data.repository

import com.example.data.model.ReferralRewardsDashboard
import com.example.data.model.ReferralXpEntry
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class ReferralRewardsRepository(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("asia-south1")
) {
    suspend fun dashboard(): ReferralRewardsDashboard {
        val response = call("getReferralDashboard")
        val referrals = (response["referrals"] as? List<*>)
            .orEmpty()
            .mapNotNull { value ->
                val item = value as? Map<*, *> ?: return@mapNotNull null
                val state = item["state"] as? String ?: "registered"
                val days = (item["qualifiedDays"] as? Number)?.toInt() ?: 0
                state to days
            }
        val ledger = (response["recentLedger"] as? List<*>)
            .orEmpty()
            .mapNotNull { value ->
                val item = value as? Map<*, *> ?: return@mapNotNull null
                ReferralXpEntry(
                    amount = (item["amount"] as? Number)?.toInt() ?: 0,
                    category = item["category"] as? String ?: "unknown",
                    createdAtMs = (item["createdAtMs"] as? Number)?.toLong()
                )
            }
        return ReferralRewardsDashboard(
            code = response["code"] as? String,
            referralState = response["referralState"] as? String,
            qualifiedDays = (response["qualifiedDays"] as? Number)?.toInt() ?: 0,
            todayEligibleMinutes = (response["todayEligibleMinutes"] as? Number)?.toInt() ?: 0,
            lifetimeXp = (response["lifetimeXp"] as? Number)?.toInt() ?: 0,
            weeklyXp = (response["weeklyXp"] as? Number)?.toInt() ?: 0,
            studyXp = (response["studyXp"] as? Number)?.toInt() ?: 0,
            referralXp = (response["referralXp"] as? Number)?.toInt() ?: 0,
            activeStudySessionId = response["activeStudySessionId"] as? String,
            activeStudySessionStartedAtMs = (response["activeStudySessionStartedAtMs"] as? Number)?.toLong(),
            requiredDays = (response["requiredDays"] as? Number)?.toInt() ?: 15,
            minutesPerDay = (response["minutesPerDay"] as? Number)?.toInt() ?: 60,
            referralRewardXp = (response["referralRewardXp"] as? Number)?.toInt() ?: 500,
            referrals = referrals,
            recentLedger = ledger
        )
    }

    suspend fun createReferralCode(): String {
        val result = call("createReferralCode")
        return result["code"] as? String
            ?: throw IllegalStateException("The referral code response was incomplete.")
    }

    suspend fun claimReferralCode(code: String) {
        call("claimReferralCode", mapOf("code" to code.trim().uppercase()))
    }

    suspend fun startStudySession(): String {
        val result = call("startStudySession")
        return result["sessionId"] as? String
            ?: throw IllegalStateException("The study session response was incomplete.")
    }

    suspend fun completeStudySession(sessionId: String) {
        call(
            "completeStudySession",
            mapOf("sessionId" to sessionId, "confirmFocusedStudy" to true)
        )
    }

    private suspend fun call(
        name: String,
        data: Map<String, Any> = emptyMap()
    ): Map<String, Any?> {
        val result = functions.getHttpsCallable(name).call(data).await()
        return result.data as? Map<String, Any?>
            ?: throw IllegalStateException("The server returned an invalid response.")
    }
}

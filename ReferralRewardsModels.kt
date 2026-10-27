package com.example.data.model

data class ReferralXpEntry(
    val amount: Int,
    val category: String,
    val createdAtMs: Long?
)

data class ReferralRewardsDashboard(
    val code: String? = null,
    val referralState: String? = null,
    val qualifiedDays: Int = 0,
    val todayEligibleMinutes: Int = 0,
    val lifetimeXp: Int = 0,
    val weeklyXp: Int = 0,
    val studyXp: Int = 0,
    val referralXp: Int = 0,
    val activeStudySessionId: String? = null,
    val activeStudySessionStartedAtMs: Long? = null,
    val requiredDays: Int = 15,
    val minutesPerDay: Int = 60,
    val referralRewardXp: Int = 500,
    val referrals: List<Pair<String, Int>> = emptyList(),
    val recentLedger: List<ReferralXpEntry> = emptyList()
)

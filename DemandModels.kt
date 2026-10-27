package com.example.data.model

enum class DemandStatus(val label: String, val colorHex: Long) {
    NEW("New", 0xFF06B6D4),
    REVIEWING("Reviewing", 0xFFF59E0B),
    PLANNED("Planned", 0xFF6366F1),
    IN_PROGRESS("In Progress", 0xFF3B82F6),
    COMPLETED("Completed", 0xFF10B981),
    REJECTED("Declined", 0xFF6B7280)
}

enum class DemandCategory(val label: String) {
    FEATURE("App Feature"),
    BATCH("Batch / Course"),
    EXAM("Exam Support"),
    CONTENT("Study Material"),
    IMPROVEMENT("UI / UX Improvement")
}

data class DemandItem(
    val id: String,
    val title: String,
    val category: DemandCategory,
    val description: String,
    val status: DemandStatus,
    val upvotes: Int = 0,
    val isUpvotedByMe: Boolean = false,
    val submittedBy: String = "You",
    val createdAt: String = "18 Sep 2026",
    val adminResponse: String? = null
)

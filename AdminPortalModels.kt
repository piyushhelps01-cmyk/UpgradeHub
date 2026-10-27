package com.example.data.model

data class AdminPermissions(
    val canBroadcastAnnouncements: Boolean = true,
    val canUploadMaterial: Boolean = true,
    val canViewAllStudentData: Boolean = true,
    val canManageRoles: Boolean = true,
    val canDeleteContent: Boolean = true
)

data class AdminStudyMaterial(
    val id: String,
    val title: String,
    val description: String,
    val subject: String, // "Physics", "Chemistry", "Mathematics", "Multi-Subject"
    val materialType: String, // "DPP Sheet", "Handwritten Notes", "Mock Question Paper", "Formula Handout", "NCERT Revision Sheet"
    val targetBatch: String, // "All Batches", "Lakshya JEE 2026 (11th)", "Arjuna JEE 2025 (12th)", "Prayas Droppers"
    val uploaderName: String,
    val uploaderRole: String,
    val fileSizeMb: Float,
    val fileUrl: String = "",
    val tags: List<String> = emptyList(),
    val uploadedAt: String,
    val downloadCount: Int = 0
)

data class AdminAuditLog(
    val id: String,
    val adminName: String,
    val action: String, // "UPLOAD_MATERIAL", "POST_ANNOUNCEMENT", "UPDATE_PERMISSIONS", "ROLE_SWITCH", "DELETE_MATERIAL"
    val details: String,
    val timestamp: String
)

data class StudentCohortProfile(
    val id: String,
    val name: String,
    val email: String,
    val batch: String,
    val targetExam: String,
    val targetCollege: String,
    val studyHours: Float,
    val streakDays: Int,
    val syllabusCompletionPct: Int,
    val problemsSolved: Int,
    val mockTestAvgScore: Int,
    val accuracyPct: Int,
    val slotCompliancePct: Int,
    val performanceStatus: String, // "TOP_PERFORMER", "ON_TRACK", "NEEDS_ATTENTION", "NEW_ASPIRANT"
    val avatarColor: Long,
    val joinedDate: String,
    val contactNumber: String,
    val physicsPct: Int = 78,
    val chemistryPct: Int = 85,
    val mathsPct: Int = 72
)

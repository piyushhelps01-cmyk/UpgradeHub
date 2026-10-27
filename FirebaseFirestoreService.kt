package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.AdminStudyMaterial
import com.example.ui.viewmodel.JeePage
import com.example.ui.viewmodel.TeamAnnouncement
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Production Firebase Firestore Service.
 * Binds directly to the provisioned database ID from R.string.firestore_database_id.
 * Provides real-time synchronization for study materials, announcements, and cloud study backups.
 */
class FirebaseFirestoreService(context: Context) {

    private val databaseId: String = context.getString(R.string.firestore_database_id)
    private val firestore: FirebaseFirestore by lazy {
        if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
            com.google.firebase.FirebaseApp.initializeApp(context)
        }
        FirebaseFirestore.getInstance(databaseId)
    }

    init {
        Log.i(TAG, "Configured Firestore service for database: $databaseId")
    }

    /**
     * Backs up full student progress snapshot JSON to Cloud Firestore.
     */
    suspend fun backupUserData(
        userId: String,
        snapshotJson: String,
        metadata: Map<String, Any>
    ): Result<Unit> {
        return try {
            val payload = hashMapOf<String, Any>(
                "snapshotJson" to snapshotJson,
                "metadata" to metadata,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users")
                .document(userId)
                .collection("backups")
                .document("latest")
                .set(payload, SetOptions.merge())
                .await()
            Log.i(TAG, "Successfully backed up data to Cloud Firestore for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to backup user data to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Restores student progress snapshot JSON from Cloud Firestore.
     */
    suspend fun restoreUserData(userId: String): Result<String?> {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("backups")
                .document("latest")
                .get()
                .await()

            val json = doc.getString("snapshotJson")
            Result.success(json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore user data from Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Synchronizes structured JEE Study OS progress (Schedules, Chapters, Practice history)
     * to Cloud Firestore under /users/{userId}/sync/study_os.
     */
    suspend fun syncStudyProgressData(
        userId: String,
        payload: Map<String, Any>
    ): Result<Unit> {
        return try {
            val fullPayload = HashMap(payload).apply {
                put("syncedAt", FieldValue.serverTimestamp())
                put("syncVersion", 2)
            }
            firestore.collection("users")
                .document(userId)
                .collection("sync")
                .document("study_os")
                .set(fullPayload, SetOptions.merge())
                .await()
            Log.i(TAG, "Successfully synced study progress to Firestore for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync study progress to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches synchronized study progress data from Cloud Firestore.
     */
    suspend fun fetchStudyProgressData(userId: String): Result<Map<String, Any>?> {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("sync")
                .document("study_os")
                .get()
                .await()
            Result.success(doc.data)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch study progress from Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time stream of official faculty study materials from Cloud Firestore.
     */
    fun observeMaterials(): Flow<List<AdminStudyMaterial>> = callbackFlow {
        val listener: ListenerRegistration = firestore.collection("materials")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing materials collection", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val materials = snapshot.documents.mapNotNull { doc ->
                        try {
                            AdminStudyMaterial(
                                id = doc.getString("id") ?: doc.id,
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description") ?: "",
                                subject = doc.getString("subject") ?: "Physics",
                                materialType = doc.getString("materialType") ?: "DPP Sheet",
                                targetBatch = doc.getString("targetBatch") ?: "All Batches",
                                uploaderName = doc.getString("uploaderName") ?: "Faculty Admin",
                                uploaderRole = doc.getString("uploaderRole") ?: "Subject Expert",
                                fileSizeMb = (doc.getDouble("fileSizeMb") ?: 4.2).toFloat(),
                                fileUrl = doc.getString("fileUrl") ?: "",
                                tags = (doc.get("tags") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                uploadedAt = doc.getString("uploadedAt") ?: "Recently",
                                downloadCount = (doc.getLong("downloadCount") ?: 0L).toInt()
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Error deserializing material: ${doc.id}", e)
                            null
                        }
                    }
                    trySend(materials)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Uploads official study material to Cloud Firestore.
     */
    suspend fun uploadMaterial(material: AdminStudyMaterial): Result<Unit> {
        return try {
            val data = hashMapOf(
                "id" to material.id,
                "title" to material.title,
                "description" to material.description,
                "subject" to material.subject,
                "materialType" to material.materialType,
                "targetBatch" to material.targetBatch,
                "uploaderName" to material.uploaderName,
                "uploaderRole" to material.uploaderRole,
                "fileSizeMb" to material.fileSizeMb,
                "fileUrl" to material.fileUrl,
                "tags" to material.tags,
                "uploadedAt" to material.uploadedAt,
                "downloadCount" to material.downloadCount,
                "serverTimestamp" to FieldValue.serverTimestamp()
            )
            firestore.collection("materials")
                .document(material.id)
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload study material to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes official study material from Cloud Firestore.
     */
    suspend fun deleteMaterial(materialId: String): Result<Unit> {
        return try {
            firestore.collection("materials").document(materialId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete material from Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time stream of team announcements from Cloud Firestore.
     */
    fun observeAnnouncements(): Flow<List<TeamAnnouncement>> = callbackFlow {
        val listener = firestore.collection("announcements")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing announcements", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val actionTargetStr = doc.getString("actionTargetPage")
                            val page = actionTargetStr?.let {
                                try { JeePage.valueOf(it) } catch (_: Exception) { null }
                            }
                            TeamAnnouncement(
                                id = doc.getString("id") ?: doc.id,
                                title = doc.getString("title") ?: "",
                                message = doc.getString("message") ?: "",
                                category = doc.getString("category") ?: "SYSTEM NOTICE",
                                authorName = doc.getString("authorName") ?: "Upgrade Hub Team",
                                authorRole = doc.getString("authorRole") ?: "Admin",
                                timestamp = doc.getString("timestamp") ?: "Recently",
                                isPinned = doc.getBoolean("isPinned") ?: false,
                                isImportant = doc.getBoolean("isImportant") ?: false,
                                isRead = doc.getBoolean("isRead") ?: false,
                                actionLabel = doc.getString("actionLabel"),
                                actionTargetPage = page,
                                acknowledgementCount = (doc.getLong("acknowledgementCount") ?: 1L).toInt(),
                                isAcknowledgedByMe = doc.getBoolean("isAcknowledgedByMe") ?: false
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Error deserializing announcement: ${doc.id}", e)
                            null
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Broadcasts an announcement to Cloud Firestore.
     */
    suspend fun publishAnnouncement(announcement: TeamAnnouncement): Result<Unit> {
        return try {
            val data = hashMapOf(
                "id" to announcement.id,
                "title" to announcement.title,
                "message" to announcement.message,
                "category" to announcement.category,
                "authorName" to announcement.authorName,
                "authorRole" to announcement.authorRole,
                "timestamp" to announcement.timestamp,
                "isPinned" to announcement.isPinned,
                "isImportant" to announcement.isImportant,
                "isRead" to announcement.isRead,
                "actionLabel" to (announcement.actionLabel ?: ""),
                "actionTargetPage" to (announcement.actionTargetPage?.name ?: ""),
                "acknowledgementCount" to announcement.acknowledgementCount,
                "isAcknowledgedByMe" to announcement.isAcknowledgedByMe,
                "serverTimestamp" to FieldValue.serverTimestamp()
            )
            firestore.collection("announcements")
                .document(announcement.id)
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to publish announcement to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Toggles pin status on Firestore.
     */
    suspend fun togglePin(announcementId: String, pinned: Boolean): Result<Unit> {
        return try {
            firestore.collection("announcements")
                .document(announcementId)
                .update("isPinned", pinned)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes announcement from Firestore.
     */
    suspend fun deleteAnnouncement(announcementId: String): Result<Unit> {
        return try {
            firestore.collection("announcements").document(announcementId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDatabaseId(): String = databaseId

    companion object {
        private const val TAG = "FirestoreService"
    }
}

package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.UserAccountDao
import com.example.data.local.UserAccountEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import kotlinx.coroutines.tasks.await
import com.example.security.FirebaseAdminClaimPolicy

class AuthRepository(
    private val context: Context,
    private val userAccountDao: UserAccountDao
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("upgrade_hub_auth_prefs", Context.MODE_PRIVATE)

    val googleAuthManager = com.example.security.GoogleAuthManager(context)

    private val _currentUser = MutableStateFlow<UserAccountEntity?>(null)
    val currentUser: StateFlow<UserAccountEntity?> = _currentUser.asStateFlow()

    private val _adminAuthorized = MutableStateFlow(false)
    val adminAuthorized: StateFlow<Boolean> = _adminAuthorized.asStateFlow()

    private val _allUsers = userAccountDao.getAllUsers()
    val allUsers = _allUsers

    init {
        CoroutineScope(Dispatchers.IO).launch {
            restoreActiveSession()
        }
    }

    private suspend fun restoreActiveSession() {
        val savedEmail = prefs.getString(KEY_ACTIVE_EMAIL, null)
        val user = savedEmail
            ?.takeIf { it.isNotBlank() }
            ?.let { userAccountDao.getUserByEmail(it) }
        if (user != null) {
            // A cached Room role is presentation data only. Privileged access is re-verified
            // against a freshly refreshed Firebase ID token at the admin gate.
            val normalizedUser = if (user.role != "USER") user.copy(role = "USER") else user
            if (normalizedUser != user) userAccountDao.updateUser(normalizedUser)
            _currentUser.value = normalizedUser
            prefs.edit().putString(KEY_ACTIVE_EMAIL, normalizedUser.email).apply()
        }
    }

    /**
     * Primary Login System: Google Sign-In
     * Authenticates or provisions a user account isolated by their Google email.
     */
    suspend fun signInWithGoogle(
        email: String,
        fullName: String,
        targetExam: String = "JEE 2026 (Lakshya Batch)",
        clearFirebaseSession: Boolean = true
    ): Result<UserAccountEntity> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return@withContext Result.failure(IllegalArgumentException("Valid Google account email is required."))
        }
        _adminAuthorized.value = false
        // This non-interactive path is local profile setup only, never Firebase authentication.
        if (clearFirebaseSession) {
            googleAuthManager.signOut()
        }
        val existing = userAccountDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            val updated = existing.copy(
                lastLoginAt = System.currentTimeMillis(),
                authProvider = "GOOGLE",
                role = "USER"
            )
            userAccountDao.updateUser(updated)
            _currentUser.value = updated
            prefs.edit().putString(KEY_ACTIVE_EMAIL, updated.email).apply()
            return@withContext Result.success(updated)
        } else {
            val newUser = UserAccountEntity(
                email = trimmedEmail,
                passwordHash = hashPassword("google_oauth_${trimmedEmail}"),
                fullName = fullName.trim().ifBlank { trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() } },
                targetExam = targetExam.ifBlank { "JEE 2026" },
                targetScore = "280+ / 300",
                avatarColor = pickRandomAvatarColor(),
                role = "USER",
                authProvider = "GOOGLE"
            )
            val id = userAccountDao.insertUser(newUser)
            val inserted = newUser.copy(id = id)
            _currentUser.value = inserted
            prefs.edit().putString(KEY_ACTIVE_EMAIL, inserted.email).apply()
            return@withContext Result.success(inserted)
        }
    }

    /**
     * Primary Google Sign-In via Jetpack Credential Manager & Firebase Authentication.
     * Authenticates with Google, signs in to Firebase Auth, and stores profile.
     */
    suspend fun signInWithGoogleCredential(
        activity: android.app.Activity,
        targetExam: String = "JEE 2026 (Lakshya Batch)"
    ): Result<UserAccountEntity> = withContext(Dispatchers.IO) {
        _adminAuthorized.value = false
        val authResult = googleAuthManager.signInWithGoogle(activity)
        if (authResult.isFailure) {
            val ex = authResult.exceptionOrNull() ?: Exception("Google Sign-In failed")
            return@withContext Result.failure(ex)
        }

        val firebaseUser = authResult.getOrNull()
            ?: return@withContext Result.failure(IllegalStateException("Firebase user was null after sign-in"))

        val email = firebaseUser.email ?: ""
        val name = firebaseUser.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
        val localResult = signInWithGoogle(
            email = email,
            fullName = name,
            targetExam = targetExam,
            clearFirebaseSession = false
        )

        // Sync authenticated profile to Firestore under /users/{userId}
        try {
            val dbId = context.getString(com.example.R.string.firestore_database_id)
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance(dbId)
            val userDoc = mapOf(
                "uid" to firebaseUser.uid,
                "email" to email,
                "displayName" to name,
                "targetExam" to targetExam,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(firebaseUser.uid)
                .set(userDoc, com.google.firebase.firestore.SetOptions.merge())
        } catch (e: Exception) {
            android.util.Log.w("AuthRepository", "Firestore profile sync note: ${e.message}")
        }

        return@withContext localResult
    }

    fun requireAuthenticatedEmail(): String {
        return _currentUser.value?.email?.lowercase()
            ?: prefs.getString(KEY_ACTIVE_EMAIL, "guest@upgradehub.local")?.lowercase()
            ?: "guest@upgradehub.local"
    }

    fun isCurrentUserAdmin(): Boolean {
        return _adminAuthorized.value
    }

    /**
     * Privileged access requires a server-provisioned Firebase custom claim and an ID token
     * issued after an MFA sign-in. Neither a local profile role nor an email address grants access.
     */
    suspend fun refreshAdminAuthorization(): Result<Boolean> = withContext(Dispatchers.IO) {
        val firebaseUser = googleAuthManager.currentUser
        if (firebaseUser == null) {
            _adminAuthorized.value = false
            return@withContext Result.success(false)
        }

        try {
            val token = firebaseUser.getIdToken(true).await()
            val authorized = FirebaseAdminClaimPolicy.allowsAdmin(token.claims)
            _adminAuthorized.value = authorized
            _currentUser.value = _currentUser.value?.let {
                val role = if (authorized) "ADMIN" else "USER"
                if (it.role == role) it else it.copy(role = role)
            }
            Result.success(authorized)
        } catch (error: Exception) {
            _adminAuthorized.value = false
            _currentUser.value = _currentUser.value?.let {
                if (it.role == "USER") it else it.copy(role = "USER")
            }
            Result.failure(error)
        }
    }

    fun revokeAdminAuthorization() {
        _adminAuthorized.value = false
        _currentUser.value = _currentUser.value?.let {
            if (it.role == "USER") it else it.copy(role = "USER")
        }
    }

    fun validateRowLevelOwnership(recordOwnerEmail: String): Boolean {
        val activeEmail = requireAuthenticatedEmail()
        return recordOwnerEmail.equals(activeEmail, ignoreCase = true)
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        targetExam: String,
        targetScore: String
    ): Result<UserAccountEntity> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }
        if (fullName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter your full name."))
        }

        val existing = userAccountDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return@withContext Result.failure(IllegalArgumentException("An account with this email already exists."))
        }

        _adminAuthorized.value = false
        googleAuthManager.signOut()
        val newUser = UserAccountEntity(
            email = trimmedEmail,
            passwordHash = hashPassword(password),
            fullName = fullName.trim(),
            targetExam = targetExam.ifBlank { "JEE 2026" },
            targetScore = targetScore.ifBlank { "280+ / 300" },
            avatarColor = pickRandomAvatarColor(),
            role = "USER",
            authProvider = "EMAIL"
        )

        val id = userAccountDao.insertUser(newUser)
        val insertedUser = newUser.copy(id = id)
        _currentUser.value = insertedUser
        prefs.edit().putString(KEY_ACTIVE_EMAIL, insertedUser.email).apply()
        Result.success(insertedUser)
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<UserAccountEntity> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        val user = userAccountDao.getUserByEmail(trimmedEmail)
            ?: return@withContext Result.failure(IllegalArgumentException("Account not found. Please register or sign in with Google."))

        val hash = hashPassword(password)
        if (user.passwordHash != hash && user.authProvider != "GOOGLE") {
            return@withContext Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }

        _adminAuthorized.value = false
        googleAuthManager.signOut()
        val updatedUser = user.copy(lastLoginAt = System.currentTimeMillis(), role = "USER")
        userAccountDao.updateUser(updatedUser)
        _currentUser.value = updatedUser
        prefs.edit().putString(KEY_ACTIVE_EMAIL, updatedUser.email).apply()
        Result.success(updatedUser)
    }

    suspend fun updateProfile(
        fullName: String,
        email: String = "",
        targetExam: String,
        targetScore: String,
        targetCollege: String = "",
        bio: String = ""
    ): Result<UserAccountEntity> = withContext(Dispatchers.IO) {
        val current = _currentUser.value
            ?: return@withContext Result.failure(IllegalStateException("No user currently logged in."))

        val trimmedEmail = email.trim().lowercase().ifBlank { current.email }
        if (!trimmedEmail.contains("@")) {
            return@withContext Result.failure(IllegalArgumentException("Please provide a valid email address."))
        }

        if (!trimmedEmail.equals(current.email, ignoreCase = true)) {
            val existing = userAccountDao.getUserByEmail(trimmedEmail)
            if (existing != null && existing.id != current.id) {
                return@withContext Result.failure(IllegalArgumentException("Another account is already using this email."))
            }
            revokeAdminAuthorization()
            googleAuthManager.signOut()
        }

        val updated = current.copy(
            fullName = fullName.trim().ifBlank { current.fullName },
            email = trimmedEmail,
            targetExam = targetExam.trim().ifBlank { current.targetExam },
            targetScore = targetScore.trim().ifBlank { current.targetScore },
            targetCollege = targetCollege.trim().ifBlank { current.targetCollege },
            bio = bio.trim().ifBlank { current.bio }
        )
        userAccountDao.updateUser(updated)
        _currentUser.value = updated
        prefs.edit().putString(KEY_ACTIVE_EMAIL, updated.email).apply()
        Result.success(updated)
    }

    fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            googleAuthManager.signOut()
        }
        _adminAuthorized.value = false
        _currentUser.value = null
        prefs.edit().remove(KEY_ACTIVE_EMAIL).apply()
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun pickRandomAvatarColor(): Long {
        val palette = listOf(
            0xFF6574FF, 0xFF00C853, 0xFFFF9100, 0xFF00B0FF, 0xFF9C27B0, 0xFFFF4081
        )
        return palette.random()
    }

    companion object {
        private const val KEY_ACTIVE_EMAIL = "active_user_email"
    }
}

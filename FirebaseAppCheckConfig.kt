package com.example.security

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

/**
 * Firebase App Check Configuration with ReCaptcha Enterprise.
 *
 * Ensures only legitimate app instances distributed through Google Play Store (and authorized
 * developer/test devices with debug tokens) can interact with Firebase and backend services.
 */
object FirebaseAppCheckConfig {
    private const val TAG = "FirebaseAppCheckConfig"
    private var isInitialized = false
    private var activeProviderName = "UNINITIALIZED"

    fun configureAppCheck(context: Context, intent: Intent? = null) {
        if (isInitialized) {
            Log.d(TAG, "Firebase App Check is already configured with provider: $activeProviderName")
            return
        }

        try {
            // Ensure FirebaseApp is initialized before configuring App Check
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
                Log.d(TAG, "Initialized default FirebaseApp")
            }

            val appCheck = FirebaseAppCheck.getInstance()

            // In Debug builds (local development, test rigs, emulators), use the DebugAppCheckProviderFactory
            if (BuildConfig.DEBUG) {
                // If an automated test or deep-link supplies a debug secret, log readiness
                val debugTokenFromIntent = intent?.getStringExtra("FIREBASE_APPCHECK_DEBUG_TOKEN")
                if (!debugTokenFromIntent.isNullOrBlank()) {
                    Log.i(TAG, "Configuring App Check with debug token from Intent")
                }

                appCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                activeProviderName = "DEBUG_PROVIDER"
                Log.i(TAG, "Firebase App Check initialized with DebugAppCheckProviderFactory")
            } else {
                // In Production / Release builds, use ReCaptcha Enterprise provider
                val siteKey = resolveRecaptchaSiteKey(context)

                if (siteKey.isNotBlank() && !siteKey.contains("PLACEHOLDER")) {
                    appCheck.installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                    activeProviderName = "RECAPTCHA_ENTERPRISE"
                    Log.i(TAG, "Firebase App Check initialized with RecaptchaAppCheckProviderFactory")
                } else {
                    // Never weaken release attestation to the debug provider. Callable endpoints
                    // enforce App Check, so the unconfigured release build fails closed.
                    activeProviderName = "UNCONFIGURED"
                    Log.e(TAG, "ReCaptcha site key is not configured. App Check remains unavailable.")
                    return
                }
            }

            // Enable automatic token refresh to ensure continuous valid attestation
            appCheck.setTokenAutoRefreshEnabled(true)
            isInitialized = true
            Log.i(TAG, "Firebase App Check successfully configured. Active provider: $activeProviderName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase App Check", e)
        }
    }

    private fun resolveRecaptchaSiteKey(context: Context): String {
        return try {
            context.getString(R.string.recaptcha_site_key)
        } catch (_: Exception) {
            ""
        }
    }

    fun isConfigured(): Boolean = isInitialized

    fun getActiveProvider(): String = activeProviderName
}

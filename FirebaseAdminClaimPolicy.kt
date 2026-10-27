package com.example.security

/**
 * Client-side gate for the admin UI. Firestore rules remain the authorization boundary.
 */
object FirebaseAdminClaimPolicy {
    fun allowsAdmin(claims: Map<*, *>): Boolean {
        val firebaseClaims = claims["firebase"] as? Map<*, *>
        val secondFactor = firebaseClaims?.get("sign_in_second_factor") as? String
        return claims["admin"] == true && !secondFactor.isNullOrBlank()
    }
}

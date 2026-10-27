package com.example

import com.example.security.FirebaseAdminClaimPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseAdminClaimPolicyTest {
    @Test
    fun acceptsOnlyAdminClaimWithMfaSecondFactor() {
        val claims = mapOf(
            "admin" to true,
            "firebase" to mapOf("sign_in_second_factor" to "phone")
        )

        assertTrue(FirebaseAdminClaimPolicy.allowsAdmin(claims))
    }

    @Test
    fun rejectsRoleFieldWithoutTrustedAdminClaim() {
        val claims = mapOf(
            "role" to "ADMIN",
            "firebase" to mapOf("sign_in_second_factor" to "phone")
        )

        assertFalse(FirebaseAdminClaimPolicy.allowsAdmin(claims))
    }

    @Test
    fun rejectsAdminClaimWithoutMfa() {
        assertFalse(FirebaseAdminClaimPolicy.allowsAdmin(mapOf("admin" to true)))
        assertFalse(
            FirebaseAdminClaimPolicy.allowsAdmin(
                mapOf("admin" to true, "firebase" to mapOf("sign_in_second_factor" to ""))
            )
        )
    }
}

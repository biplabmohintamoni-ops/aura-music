package com.example.auramusic.data.auth

enum class AuthType {
    GUEST,
    GOOGLE_ACCOUNT
}

data class AccountState(
    val authType: AuthType = AuthType.GUEST,
    val userId: String = "guest_user",
    val displayName: String = "Guest User",
    val email: String? = null,
    val photoUrl: String? = null,
    val isLoggedIn: Boolean = false
)

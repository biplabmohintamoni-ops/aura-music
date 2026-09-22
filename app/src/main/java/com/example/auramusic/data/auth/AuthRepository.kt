package com.example.auramusic.data.auth

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("aura_auth_prefs", Context.MODE_PRIVATE)

    private val _accountState = MutableStateFlow(loadInitialAccountState())
    val accountState: StateFlow<AccountState> = _accountState.asStateFlow()

    private fun loadInitialAccountState(): AccountState {
        val typeStr = prefs.getString("auth_type", AuthType.GUEST.name) ?: AuthType.GUEST.name
        val authType = try { AuthType.valueOf(typeStr) } catch (e: Exception) { AuthType.GUEST }
        val userId = prefs.getString("user_id", "guest_user") ?: "guest_user"
        val name = prefs.getString("display_name", if (authType == AuthType.GOOGLE_ACCOUNT) "Google User" else "Guest User") ?: "Guest User"
        val email = prefs.getString("email", null)
        val photoUrl = prefs.getString("photo_url", null)

        return AccountState(
            authType = authType,
            userId = userId,
            displayName = name,
            email = email,
            photoUrl = photoUrl,
            isLoggedIn = authType == AuthType.GOOGLE_ACCOUNT
        )
    }

    fun signInWithGoogle(idToken: String, displayName: String, email: String, photoUrl: String? = null) {
        val newState = AccountState(
            authType = AuthType.GOOGLE_ACCOUNT,
            userId = "google_${email.hashCode()}",
            displayName = displayName,
            email = email,
            photoUrl = photoUrl,
            isLoggedIn = true
        )
        saveState(newState)
        _accountState.value = newState
        Log.i("AuthRepository", "Google Sign-In successful for $email")
    }

    fun switchToGuestMode() {
        val newState = AccountState(
            authType = AuthType.GUEST,
            userId = "guest_user",
            displayName = "Guest User",
            email = null,
            photoUrl = null,
            isLoggedIn = false
        )
        saveState(newState)
        _accountState.value = newState
        Log.i("AuthRepository", "Switched to Guest Mode")
    }

    fun signOut() {
        switchToGuestMode()
    }

    private fun saveState(state: AccountState) {
        prefs.edit()
            .putString("auth_type", state.authType.name)
            .putString("user_id", state.userId)
            .putString("display_name", state.displayName)
            .putString("email", state.email)
            .putString("photo_url", state.photoUrl)
            .apply()
    }

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(context: Context): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}

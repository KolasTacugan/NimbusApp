package com.example.nimbus.Login

import android.util.Patterns
import android.util.Log

class LoginPresenter {

    private val model = LoginModel()

    /**
     * Attempts to log in a user with the provided email and password.
     *
     * @param email The user's email
     * @param password The user's password
     * @param onSuccess Callback invoked when login succeeds, returns User object
     * @param onError Callback invoked when login fails, returns error message
     */
    fun login(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        // 1. Validate input
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            onError("Invalid email address")
            return
        }

        if (trimmedPassword.length < 6) {
            onError("Password must be at least 6 charaacters")
            return
        }

        // 2. Normalize email for consistency
        val normalizedEmail = trimmedEmail.lowercase()

        // 3. Validate against Firebase
        model.validateUser(
            normalizedEmail,
            trimmedPassword,
            onSuccess = { user ->
                Log.d("LoginPresenter", "Login successful for UID: ${user.uid}")
                onSuccess(user)
            },
            onError = { errorMessage ->
                Log.e("LoginPresenter", "Login failed: $errorMessage")
                onError(errorMessage)
            }
        )
    }
}

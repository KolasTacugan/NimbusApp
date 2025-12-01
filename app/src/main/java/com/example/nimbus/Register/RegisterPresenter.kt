package com.example.nimbus.Register

import android.util.Patterns
import com.google.firebase.database.FirebaseDatabase

class RegisterPresenter(private val view: RegisterActivity) {

    private val model = RegisterModel()
    private val database = FirebaseDatabase.getInstance().reference.child("users")

    fun register(
        firstName: String,
        middleName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {

        // 1. Validate inputs
        if (firstName.isEmpty()) {
            view.showNameError("First name cannot be empty", "first")
            return
        }

        if (lastName.isEmpty()) {
            view.showNameError("Last name cannot be empty", "last")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showEmailError("Invalid email address")
            return
        }

        if (password.length < 6) {
            view.showPasswordError("Password must be at least 6 characters")
            return
        }

        if (password != confirmPassword) {
            view.showConfirmPasswordError("Passwords do not match")
            return
        }

        // Normalize email
        val normalizedEmail = email.trim().lowercase()

        // 2. Check if email already exists in Firebase
        checkEmailUnique(normalizedEmail) { isUnique ->
            if (isUnique) {
                // 3. Save user using your RegisterModel
                model.saveUser(
                    firstName,
                    middleName,
                    lastName,
                    normalizedEmail,
                    password,
                    onSuccess = { view.showRegisterSuccess() },
                    onError = { errorMessage -> view.showRegisterError(errorMessage) }
                )
            } else {
                view.showEmailError("Email is already in use")
            }
        }
    }

    private fun checkEmailUnique(email: String, callback: (Boolean) -> Unit) {
        val normalizedEmail = email.trim().lowercase()

        database.get().addOnSuccessListener { snapshot ->
            var exists = false

            for (userSnap in snapshot.children) {
                val savedEmail = userSnap.child("email")
                    .getValue(String::class.java)
                    ?.trim()?.lowercase()

                if (savedEmail == normalizedEmail) {
                    exists = true
                    break
                }
            }

            callback(!exists)

        }.addOnFailureListener {
            view.showRegisterError("Failed to check email uniqueness")
            callback(false)
        }
    }
}

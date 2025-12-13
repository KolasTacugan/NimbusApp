package com.example.nimbus.ForgotPassword

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ForgotPasswordModel {

    interface ForgotPasswordCallback {
        fun onEmailVerified(userKey: String)
        fun onEmailNotFound()
        fun onEmailValidationError(error: String)
        fun onPasswordResetSuccess()
        fun onPasswordResetError(error: String)
    }

    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    /**
     * Validates if the email exists in the database
     */
    fun validateEmail(email: String, callback: ForgotPasswordCallback) {
        if (email.isEmpty()) {
            callback.onEmailValidationError("Email cannot be empty")
            return
        }

        if (!isValidEmail(email)) {
            callback.onEmailValidationError("Please enter a valid email address")
            return
        }

        usersRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Email found, get the user key
                        var userKey = ""
                        for (userSnapshot in dataSnapshot.children) {
                            userKey = userSnapshot.key ?: ""
                            break
                        }

                        if (userKey.isNotEmpty()) {
                            callback.onEmailVerified(userKey)
                        } else {
                            callback.onEmailNotFound()
                        }
                    } else {
                        callback.onEmailNotFound()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("ForgotPasswordModel", "Email validation error: ${databaseError.message}")
                    callback.onEmailValidationError("Database error: ${databaseError.message}")
                }
            })
    }

    /**
     * Resets the password for a specific user
     */
    fun resetPassword(userKey: String, newPassword: String, callback: ForgotPasswordCallback) {
        if (userKey.isEmpty()) {
            callback.onPasswordResetError("User not found")
            return
        }

        if (newPassword.isEmpty()) {
            callback.onPasswordResetError("Password cannot be empty")
            return
        }

        if (newPassword.length < 6) {
            callback.onPasswordResetError("Password must be at least 6 characters")
            return
        }

        // Update password in Firebase
        usersRef.child(userKey).child("password")
            .setValue(newPassword)
            .addOnSuccessListener {
                callback.onPasswordResetSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("ForgotPasswordModel", "Password reset error: ${exception.message}")
                callback.onPasswordResetError("Failed to reset password: ${exception.message}")
            }
    }

    /**
     * Helper function to validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
        return emailPattern.matches(email)
    }
}
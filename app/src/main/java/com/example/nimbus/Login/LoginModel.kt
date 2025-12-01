package com.example.nimbus.Login

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

// Data class representing a user in Firebase
data class User(
    val uid: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = ""
)

class LoginModel {

    // Reference to Firebase Realtime Database 'users' node
    private val database = FirebaseDatabase.getInstance().reference.child("users")

    /**
     * Validates user login credentials.
     *
     * @param email User email (trimmed and converted to lowercase)
     * @param password User password
     * @param onSuccess Callback invoked with User object if login succeeds
     * @param onError Callback invoked with error message if login fails
     */
    fun validateUser(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        val normalizedEmail = email.trim().lowercase()

        // Query Firebase for the user by email
        database.orderByChild("email").equalTo(normalizedEmail).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onError("Invalid email or password")
                    return@addOnSuccessListener
                }

                var foundUser: User? = null

                for (userSnap in snapshot.children) {
                    val user = userSnap.getValue(User::class.java)
                    val uid = userSnap.key

                    if (user == null) continue
                    if (user.password != password) continue
                    if (uid.isNullOrEmpty()) {
                        Log.e("LoginModel", "Firebase node UID missing for email: $email")
                        onError("Internal error: UID missing")
                        return@addOnSuccessListener
                    }

                    // Copy UID into user object
                    foundUser = user.copy(uid = uid)
                    break
                }

                if (foundUser != null) {
                    Log.d("LoginModel", "Login successful for UID: ${foundUser.uid}")
                    onSuccess(foundUser)
                } else {
                    onError("Invalid email or password")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LoginModel", "Firebase read failed", exception)
                onError("Failed to access database: ${exception.message}")
            }
    }
}

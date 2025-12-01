package com.example.nimbus.EditProfile

import android.content.Context
import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class EditProfileModel(private val context: Context) {

    private val sharedPref = context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
    private val database = FirebaseDatabase.getInstance().getReference("users")

    // Helper function to capitalize first letters
    private fun String.capitalizeFirstLetter(): String {
        return this.trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    // Load user data from SharedPreferences (excluding email)
    fun getUserData(): Map<String, String> {
        return mapOf(
            "firstName" to (sharedPref.getString("USER_FIRST_NAME", "") ?: ""),
            "middleName" to (sharedPref.getString("USER_MIDDLE_NAME", "") ?: ""),
            "lastName" to (sharedPref.getString("USER_LAST_NAME", "") ?: ""),
            "uid" to (sharedPref.getString("USER_UID", "") ?: "")
        )
    }

    // Update user data safely (excluding email)
    fun updateUserData(
        newFirstName: String,
        newMiddleName: String? = null,
        newLastName: String? = null,
        callback: (Boolean) -> Unit
    ) {
        val userId = sharedPref.getString("USER_UID", null)
        if (userId.isNullOrEmpty()) {
            Log.e("EditProfileModel", "Cannot update profile: UID is null or empty")
            return callback(false)
        }

        val capitalFirstName = newFirstName.capitalizeFirstLetter()
        val capitalMiddleName = newMiddleName?.capitalizeFirstLetter()
        val capitalLastName = newLastName?.capitalizeFirstLetter()

        // Update SharedPreferences
        sharedPref.edit().apply {
            putString("USER_FIRST_NAME", capitalFirstName)
            capitalMiddleName?.let { putString("USER_MIDDLE_NAME", it) }
            capitalLastName?.let { putString("USER_LAST_NAME", it) }
            apply()
        }

        // Prepare Firebase updates (excluding email)
        val updates = mutableMapOf<String, Any>("firstName" to capitalFirstName)
        capitalMiddleName?.let { updates["middleName"] = it }
        capitalLastName?.let { updates["lastName"] = it }

        Log.d("EditProfileModel", "Updating Firebase UID: $userId with $updates")

        database.child(userId).updateChildren(updates)
            .addOnSuccessListener {
                Log.d("EditProfileModel", "Firebase update successful")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("EditProfileModel", "Firebase update failed", e)
                callback(false)
            }
    }
}

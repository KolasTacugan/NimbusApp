package com.example.nimbus.Profile

import android.content.Context
import com.example.nimbus.R

data class Profile(
    val name: String,
    val email: String,
    val imageRes: Int
)

class ProfileModel(private val context: Context) {

    fun getProfile(): Profile {
        val sharedPref = context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
        val firstName = sharedPref.getString("USER_FIRST_NAME", "Unknown")
        val middleName = sharedPref.getString("USER_MIDDLE_NAME", "")
        val lastName = sharedPref.getString("USER_LAST_NAME", "")
        val email = sharedPref.getString("USER_EMAIL", "unknown@example.com")

        val fullName = if (!middleName.isNullOrBlank()) {
            "$firstName $middleName $lastName"
        } else {
            "$firstName $lastName"
        }

        return Profile(
            name = fullName,
            email = email ?: "unknown@example.com",
            imageRes = R.drawable.ic_profile  // <- use your actual drawable
        )

    }
}

package com.example.nimbus.Register

import com.google.firebase.database.FirebaseDatabase


data class User(
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = ""   // Consider hashing later
)



class RegisterModel {

    private val database = FirebaseDatabase.getInstance().reference

    // Capitalize first letter of a name
    private fun capitalizeName(name: String): String {
        return name.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
    }

    fun saveUser(
        firstName: String,
        middleName: String,
        lastName: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = database.child("users").push().key
        if (userId == null) {
            onError("Failed to generate user ID")
            return
        }

        val user = User(
            firstName = capitalizeName(firstName),
            middleName = capitalizeName(middleName),
            lastName = capitalizeName(lastName),
            email = email.trim().lowercase(),
            password = password
        )

        database.child("users").child(userId).setValue(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { ex ->
                onError(ex.message ?: "Unknown error")
            }
    }
}

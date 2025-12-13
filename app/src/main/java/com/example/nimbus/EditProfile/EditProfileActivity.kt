package com.example.nimbus.EditProfile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nimbus.Profile.ProfileActivity  // Import ProfileActivity
import com.example.nimbus.R

class EditProfileActivity : AppCompatActivity(), EditProfileView {

    private lateinit var presenter: EditProfilePresenter
    private lateinit var model: EditProfileModel

    // UI Elements
    private lateinit var firstNameEdit: EditText
    private lateinit var middleNameEdit: EditText
    private lateinit var lastNameEdit: EditText
    private lateinit var updateButton: Button
    private lateinit var backButton: Button  // Keep as Button since XML uses Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Model and Presenter
        model = EditProfileModel(this)
        presenter = EditProfilePresenter(this, model)

        // Initialize UI
        firstNameEdit = findViewById(R.id.editFirstName)
        middleNameEdit = findViewById(R.id.editMiddleName)
        lastNameEdit = findViewById(R.id.editLastName)
        updateButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)  // This is now correct

        // Load existing user data
        presenter.loadUserData()

        // Set button listeners
        updateButton.setOnClickListener {
            val firstName = firstNameEdit.text.toString()
            val middleName = middleNameEdit.text.toString().takeIf { it.isNotBlank() }
            val lastName = lastNameEdit.text.toString().takeIf { it.isNotBlank() }

            if (firstName.isBlank()) {
                Toast.makeText(this, "First name cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                presenter.updateUserData(firstName, middleName, lastName)
                // Don't navigate here - wait for onUpdateSuccess() callback
            }
        }

        backButton.setOnClickListener {
            // Navigate back to ProfileActivity without saving
            navigateToProfileActivity()
        }
    }

    // Navigate to ProfileActivity
    private fun navigateToProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        // You can use these flags if you want to clear the back stack
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()  // Close this activity
    }

    // ===== EditProfileView Implementation =====
    override fun onUserDataLoaded(userData: Map<String, String>) {
        firstNameEdit.setText(userData["firstName"] ?: "")
        middleNameEdit.setText(userData["middleName"] ?: "")
        lastNameEdit.setText(userData["lastName"] ?: "")
    }

    override fun onUpdateSuccess() {
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        // Navigate after successful update
        navigateToProfileActivity()
    }

    override fun onUpdateFailure() {
        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
        // Stay on the same screen on failure so user can try again
    }
}
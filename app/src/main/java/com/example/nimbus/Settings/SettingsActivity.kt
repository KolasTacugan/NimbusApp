package com.example.nimbus.Settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nimbus.Login.LoginActivity
import com.example.nimbus.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button functionality
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Go back to previous activity
        }

        // Edit Profile card functionality
        val editProfileCard = findViewById<LinearLayout>(R.id.btnEditProfile)
        editProfileCard.setOnClickListener {
            // TODO: Navigate to Edit Profile Activity
            // Uncomment when EditProfileActivity is created:
            // val intent = Intent(this, EditProfileActivity::class.java)
            // startActivity(intent)

            // Temporary toast message
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show()
        }

        // Logout button functionality
        val logoutButton = findViewById<Button>(R.id.btnLogout)
        logoutButton.setOnClickListener {
            // Optional: Clear user session or shared preferences here if needed
            // Example:
            // val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            // sharedPreferences.edit().clear().apply()

            // Go back to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}

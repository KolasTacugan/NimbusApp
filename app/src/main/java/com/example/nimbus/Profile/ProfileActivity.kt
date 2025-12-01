package com.example.nimbus.Profile

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AppCompatActivity
import com.example.nimbus.EditProfile.EditProfileActivity
import com.example.nimbus.R

class ProfileActivity : AppCompatActivity(), ProfileView {

    private lateinit var presenter: ProfilePresenter
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var backButton: ImageView
    private lateinit var editButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Bind views to XML IDs
        nameTextView = findViewById(R.id.profileName)
        emailTextView = findViewById(R.id.profileEmail)
        profileImageView = findViewById(R.id.profileImage)
        backButton = findViewById(R.id.back_button)
        editButton = findViewById(R.id.editButton)

        // Initialize Presenter
        val model = ProfileModel(this)
        presenter = ProfilePresenter(this, model)

        // Load profile
        presenter.loadProfile()

        // Back button functionality
        backButton.setOnClickListener {
            finish()
        }

        // Edit button functionality (optional, just a placeholder)
        editButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

    }

    override fun displayProfile(profile: Profile) {
        nameTextView.text = profile.name
        emailTextView.text = profile.email
        profileImageView.setImageResource(profile.imageRes)
    }
}

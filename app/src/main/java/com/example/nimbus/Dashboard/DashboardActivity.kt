package com.example.nimbus.Dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nimbus.Profile.ProfileActivity
import com.example.nimbus.Settings.SettingsActivity
import com.example.nimbus.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.LinearLayout
import com.example.nimbus.ClotheslineStatus.ClotheslineStatusActivity
import com.example.nimbus.History.HistoryActivity


class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        // Handle system insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Handle History Card click
        val cardHistory = findViewById<LinearLayout>(R.id.cardHistory)
        cardHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Clothesline Status card
        val cardClothesline = findViewById<LinearLayout>(R.id.cardClothesline)
        cardClothesline.setOnClickListener {
            startActivity(Intent(this, ClotheslineStatusActivity::class.java))
        }
    }
}

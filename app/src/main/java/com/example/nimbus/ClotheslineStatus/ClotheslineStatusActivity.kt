package com.example.nimbus.ClotheslineStatus

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nimbus.R

class ClotheslineStatusActivity : AppCompatActivity(),
    ClotheslineStatusPresenter.ClotheslineStatusView {

    private lateinit var presenter: ClotheslineStatusPresenter

    // UI Components
    private lateinit var currentStatusText: TextView
    private lateinit var rainStatusText: TextView
    private lateinit var autoModeToggle: Switch
    private lateinit var minutePicker: NumberPicker
    private lateinit var btnExtendShade: Button
    private lateinit var btnRetractShade: Button
    private lateinit var manualOverrideTimer: TextView

    // Loading overlay
    private lateinit var loadingOverlay: View

    // Current state
    private var currentShadeStatus = false
    private var currentAutomaticMode = true
    private var currentRainStatus = false
    private var currentManualShade = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_clothesline_status)

        // Initialize presenter
        presenter = ClotheslineStatusPresenter(this)

        // Initialize UI components
        initViews()

        // Set up UI listeners
        setupListeners()

        // Initialize presenter to start listening for real-time updates
        presenter.initialize()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        // Initialize TextViews
        currentStatusText = findViewById(R.id.currentStatusText)
        rainStatusText = findViewById(R.id.rainStatusText)
        manualOverrideTimer = findViewById(R.id.manualOverrideTimer)

        // Initialize Switch
        autoModeToggle = findViewById(R.id.autoModeToggle)

        // Initialize NumberPicker
        minutePicker = findViewById(R.id.minutePicker)
        minutePicker.minValue = 1
        minutePicker.maxValue = 120
        minutePicker.value = 30

        // Initialize Buttons
        btnExtendShade = findViewById(R.id.btnExtendShade)
        btnRetractShade = findViewById(R.id.btnRetractShade)

        // Initialize loading overlay (you'll need to add this to your XML)
        loadingOverlay = findViewById(R.id.loadingOverlay)
    }

    private fun setupListeners() {
        // Automatic mode toggle listener
        autoModeToggle.setOnCheckedChangeListener { _, isChecked ->
            presenter.onAutomaticModeSwitchToggled(isChecked)
        }

        // Manual shade extend button
        btnExtendShade.setOnClickListener {
            val minutes = minutePicker.value
            presenter.onManualShadeTimeSelected(minutes)
            // Note: The actual extension should be handled via shade status update
        }

        // Manual shade retract button
        btnRetractShade.setOnClickListener {
            presenter.toggleShadeStatus(currentShadeStatus)
        }

        // Minute picker value change listener
        minutePicker.setOnValueChangedListener { _, _, newVal ->
            // You could implement real-time preview or debounced update here
        }
    }

    // ===== View Interface Implementation =====

    override fun showLoading() {
        loadingOverlay.visibility = View.VISIBLE
        // Disable interactive elements while loading
        autoModeToggle.isEnabled = false
        btnExtendShade.isEnabled = false
        btnRetractShade.isEnabled = false
        minutePicker.isEnabled = false
    }

    override fun hideLoading() {
        loadingOverlay.visibility = View.GONE
        // Re-enable interactive elements
        autoModeToggle.isEnabled = true
        btnExtendShade.isEnabled = true
        btnRetractShade.isEnabled = true
        minutePicker.isEnabled = true
    }

    override fun showAutomaticMode(isAutomatic: Boolean) {
        runOnUiThread {
            currentAutomaticMode = isAutomatic
            autoModeToggle.isChecked = isAutomatic

            // Update UI state based on automatic mode
            if (isAutomatic) {
                minutePicker.isEnabled = false
                btnExtendShade.isEnabled = false
                btnRetractShade.isEnabled = false
                manualOverrideTimer.text = "Automatic Mode Active"
            } else {
                minutePicker.isEnabled = true
                btnExtendShade.isEnabled = true
                btnRetractShade.isEnabled = true
                manualOverrideTimer.text = "Manual Override Active"
            }
        }
    }

    override fun showManualShade(minutes: Int) {
        runOnUiThread {
            currentManualShade = minutes
            minutePicker.value = minutes

            // Update timer display
            manualOverrideTimer.text = "Manual Override: $minutes minutes"
        }
    }

    override fun showRainStatus(isRaining: Boolean, statusText: String) {
        runOnUiThread {
            currentRainStatus = isRaining
            rainStatusText.text = "Rain: $statusText"

            // Visual feedback based on rain status
            if (isRaining) {
                rainStatusText.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
            } else {
                rainStatusText.setTextColor(resources.getColor(R.color.dark_blue, null))
            }
        }
    }

    override fun showShadeStatus(isRetracted: Boolean, statusText: String) {
        runOnUiThread {
            currentShadeStatus = isRetracted
            currentStatusText.text = "Shade Status: $statusText"

            // Update button states based on shade status
            if (isRetracted) {
                currentStatusText.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                btnExtendShade.text = "Extend Shade"
                btnRetractShade.text = "Retracted"
                btnRetractShade.isEnabled = false
                btnExtendShade.isEnabled = true
            } else {
                currentStatusText.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
                btnExtendShade.text = "Extended"
                btnRetractShade.text = "Retract Shade"
                btnExtendShade.isEnabled = false
                btnRetractShade.isEnabled = true
            }
        }
    }

    override fun showFullStatus(status: ClotheslineStatusModel.ClotheslineStatus) {
        runOnUiThread {
            // Update all UI elements with the complete status
            showAutomaticMode(status.automaticMode)
            showManualShade(status.manualShade)
            showRainStatus(status.rainStatus, ClotheslineStatusModel.getRainStatusText(status.rainStatus))
            showShadeStatus(status.shadeStatus, ClotheslineStatusModel.getShadeStatusText(status.shadeStatus))

            // Log or show toast with full status (optional)
            // Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showError(message: String) {
        runOnUiThread {
            hideLoading()
            // Show error message - you can use Toast, Snackbar, or an error TextView
            android.widget.Toast.makeText(
                this@ClotheslineStatusActivity,
                "Error: $message",
                android.widget.Toast.LENGTH_LONG
            ).show()

            // You could also show the error in a TextView in your layout
            // errorTextView.text = message
            // errorTextView.visibility = View.VISIBLE
        }
    }

    override fun onAutomaticModeUpdated(success: Boolean) {
        runOnUiThread {
            if (!success) {
                // Revert toggle if update failed
                autoModeToggle.isChecked = currentAutomaticMode
                showError("Failed to update automatic mode")
            } else {
                // Show success feedback
                android.widget.Toast.makeText(
                    this@ClotheslineStatusActivity,
                    if (currentAutomaticMode) "Automatic mode enabled" else "Manual mode enabled",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onManualShadeUpdated(success: Boolean) {
        runOnUiThread {
            if (!success) {
                // Revert to previous value if update failed
                minutePicker.value = currentManualShade
                showError("Failed to update manual shade time")
            } else {
                // Show success feedback
                android.widget.Toast.makeText(
                    this@ClotheslineStatusActivity,
                    "Manual shade time set to $currentManualShade minutes",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                // Update timer display
                manualOverrideTimer.text = "Manual Override: $currentManualShade minutes remaining"
            }
        }
    }

    override fun onRainStatusUpdated(success: Boolean) {
        runOnUiThread {
            if (!success) {
                showError("Failed to update rain status")
            }
            // No need to revert UI since rain status is typically sensor-driven
        }
    }

    override fun onShadeStatusUpdated(success: Boolean) {
        runOnUiThread {
            if (!success) {
                // Revert shade status if update failed
                showShadeStatus(currentShadeStatus,
                    ClotheslineStatusModel.getShadeStatusText(currentShadeStatus))
                showError("Failed to update shade status")
            } else {
                // Show success feedback
                val statusText = if (currentShadeStatus) "retracted" else "extended"
                android.widget.Toast.makeText(
                    this@ClotheslineStatusActivity,
                    "Shade $statusText successfully",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ===== Lifecycle Methods =====

    override fun onResume() {
        super.onResume()
        // Re-initialize if needed
        if (!::presenter.isInitialized) {
            presenter = ClotheslineStatusPresenter(this)
            presenter.initialize()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up presenter resources
        presenter.onDestroy()
    }

    // ===== Helper Methods =====

    private fun startTimerDisplay(minutes: Int) {
        // This is a placeholder for timer functionality
        // You would typically use a CountDownTimer or Handler here
        manualOverrideTimer.text = "Manual Override: $minutes:00 remaining"
    }

    private fun updateButtonStates() {
        // Update button enabled states based on current mode
        val isManualMode = !currentAutomaticMode
        btnExtendShade.isEnabled = isManualMode && currentShadeStatus
        btnRetractShade.isEnabled = isManualMode && !currentShadeStatus
        minutePicker.isEnabled = isManualMode
    }
}
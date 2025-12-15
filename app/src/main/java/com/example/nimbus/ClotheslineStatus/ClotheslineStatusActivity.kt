package com.example.nimbus.ClotheslineStatus

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nimbus.R

class ClotheslineStatusActivity : AppCompatActivity(),
    ClotheslineStatusPresenter.ClotheslineStatusView {

    private lateinit var presenter: ClotheslineStatusPresenter
    private lateinit var currentStatusText: TextView
    private lateinit var rainStatusText: TextView
    private lateinit var autoModeToggle: Switch
    private lateinit var minutePicker: NumberPicker
    private lateinit var btnExtendShade: Button
    private lateinit var btnRetractShade: Button
    private lateinit var remainingTimeText: TextView
    private lateinit var loadingOverlay: View

    private var countdownTimer: CountDownTimer? = null
    private var currentShadeStatus = false
    private var currentAutomaticMode = true
    private var currentRainStatus = false
    private var currentManualShade = 30
    private var currentCountdownSeconds: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_clothesline_status)

        presenter = ClotheslineStatusPresenter(this)
        initViews()
        setupListeners()
        presenter.initialize()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        currentStatusText = findViewById(R.id.currentStatusText)
        rainStatusText = findViewById(R.id.rainStatusText)
        remainingTimeText = findViewById(R.id.remainingTimeText)
        autoModeToggle = findViewById(R.id.autoModeToggle)
        minutePicker = findViewById(R.id.minutePicker)
        minutePicker.minValue = 1
        minutePicker.maxValue = 120
        minutePicker.value = 30
        btnExtendShade = findViewById(R.id.btnExtendShade)
        btnRetractShade = findViewById(R.id.btnRetractShade)
        loadingOverlay = findViewById(R.id.loadingOverlay)

        remainingTimeText.visibility = View.GONE
    }

    private fun setupListeners() {
        autoModeToggle.setOnCheckedChangeListener { _, isChecked ->
            presenter.onAutomaticModeSwitchToggled(isChecked)
            updateShadeButtonsEnabledState(!isChecked)
        }

        btnExtendShade.setOnClickListener {
            if (autoModeToggle.isChecked) return@setOnClickListener
            val minutes = minutePicker.value
            addToTimer(minutes)
        }

        btnRetractShade.setOnClickListener {
            if (autoModeToggle.isChecked) return@setOnClickListener
            if (currentShadeStatus) { // If shade is retracted, extend it
                presenter.toggleShadeStatus(true)
                setDefaultExtendTimer(10)
            } else { // If shade is extended, retract it
                presenter.toggleShadeStatus(false)
                stopCountdownTimer()
                remainingTimeText.visibility = View.GONE
            }
        }

        minutePicker.setOnValueChangedListener { _, _, newVal ->
            // Optional: Implement real-time preview here
        }
    }


    private fun setDefaultExtendTimer(minutes: Int) {
        val totalSeconds = minutes * 60L
        currentCountdownSeconds = totalSeconds
        updateTimerDisplay(totalSeconds)
        startCountdownTimer(totalSeconds)
        presenter.startCountdown(minutes)

        Toast.makeText(
            this@ClotheslineStatusActivity,
            "Shade extended for $minutes minutes",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun addToTimer(minutes: Int) {
        val additionalSeconds = minutes * 60L
        val newTotalSeconds = currentCountdownSeconds + additionalSeconds
        currentCountdownSeconds = newTotalSeconds
        updateTimerDisplay(newTotalSeconds)

        // Restart countdown with new total time
        startCountdownTimer(newTotalSeconds)
        presenter.startCountdown(minutes)

        Toast.makeText(
            this@ClotheslineStatusActivity,
            "Added $minutes minutes to timer",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun startCountdownTimer(totalSeconds: Long) {
        stopCountdownTimer()

        if (totalSeconds <= 0) return

        countdownTimer = object : CountDownTimer(totalSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                currentCountdownSeconds = secondsLeft
                updateTimerDisplay(secondsLeft)

                if (secondsLeft % 30 == 0L) {
                    presenter.updateCountdown(secondsLeft)
                }
            }

            override fun onFinish() {
                currentCountdownSeconds = 0
                updateTimerDisplay(0)
                presenter.cancelCountdown()

                if (!currentShadeStatus) {
                    presenter.toggleShadeStatus(false)
                    Toast.makeText(
                        this@ClotheslineStatusActivity,
                        "Timer finished. Shade retracted.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateRetractShadeButtonText(true)
                }
            }
        }.start()
    }

    private fun stopCountdownTimer() {
        countdownTimer?.cancel()
        countdownTimer = null
    }

    private fun updateShadeButtonsEnabledState(enabled: Boolean) {
        btnExtendShade.isEnabled = enabled
        btnRetractShade.isEnabled = enabled
    }

    private fun updateRetractShadeButtonText(isRetracted: Boolean) {
        runOnUiThread {
            btnRetractShade.text = if (isRetracted) "Extend Shade" else "Retract Shade"
            // Disable Extend Time button when shade is retracted
            btnExtendShade.isEnabled = !isRetracted
            // Disable Extend Shade button when shade is retracted
            btnRetractShade.isEnabled = !isRetracted
        }
    }

    private fun updateTimerDisplay(secondsLeft: Long) {
        runOnUiThread {
            if (secondsLeft > 0) {
                val formattedTime = ClotheslineStatusModel.formatTimeFromSeconds(secondsLeft)
                remainingTimeText.text = "Remaining Time: $formattedTime"
                remainingTimeText.visibility = View.VISIBLE
            } else {
                remainingTimeText.visibility = View.GONE
            }
        }
    }

    override fun showLoading() {
        loadingOverlay.visibility = View.VISIBLE
        autoModeToggle.isEnabled = false
        updateShadeButtonsEnabledState(false)
        minutePicker.isEnabled = false
    }

    override fun hideLoading() {
        loadingOverlay.visibility = View.GONE
        autoModeToggle.isEnabled = true
        updateShadeButtonsEnabledState(!currentAutomaticMode)
        minutePicker.isEnabled = !currentAutomaticMode
    }

    override fun showAutomaticMode(isAutomatic: Boolean) {
        runOnUiThread {
            currentAutomaticMode = isAutomatic
            autoModeToggle.isChecked = isAutomatic
            updateShadeButtonsEnabledState(!isAutomatic)
            minutePicker.isEnabled = !isAutomatic

            if (isAutomatic) {
                remainingTimeText.visibility = View.GONE
                stopCountdownTimer()
            }
        }
    }

    override fun showManualShade(minutes: Int) {
        runOnUiThread {
            currentManualShade = minutes
            minutePicker.value = minutes
        }
    }

    override fun showRainStatus(isRaining: Boolean, statusText: String) {
        runOnUiThread {
            currentRainStatus = isRaining
            rainStatusText.text = "Rain: $statusText"
            rainStatusText.setTextColor(
                if (isRaining) {
                    resources.getColor(android.R.color.holo_red_dark, null)
                } else {
                    resources.getColor(R.color.dark_blue, null)
                }
            )
        }
    }

    override fun showShadeStatus(isRetracted: Boolean, statusText: String) {
        runOnUiThread {
            currentShadeStatus = isRetracted
            currentStatusText.text = "Shade Status: $statusText"
            currentStatusText.setTextColor(
                if (isRetracted) {
                    resources.getColor(android.R.color.holo_green_dark, null)
                } else {
                    resources.getColor(android.R.color.holo_orange_dark, null)
                }
            )

            // Set button text
            btnRetractShade.text = if (isRetracted) "Extend Shade" else "Retract Shade"
            btnExtendShade.text = "Extend Time"

            if (isRetracted) {
                // Disable Extend Time button and hide timer
                btnExtendShade.isEnabled = false
                btnExtendShade.isClickable = false
                remainingTimeText.visibility = View.GONE

                // Retract button should also be disabled
                btnRetractShade.isEnabled = false
            } else {
                // Shade is extended → enable Extend button (if manual mode) and Retract button
                btnExtendShade.isEnabled = !currentAutomaticMode
                btnExtendShade.isClickable = true
                btnRetractShade.isEnabled = true
            }
        }
    }


    override fun showFullStatus(status: ClotheslineStatusModel.ClotheslineStatus) {
        runOnUiThread {
            showAutomaticMode(status.automaticMode)
            showManualShade(status.manualShade)
            showRainStatus(status.rainStatus, ClotheslineStatusModel.getRainStatusText(status.rainStatus))
            showShadeStatus(status.shadeStatus, ClotheslineStatusModel.getShadeStatusText(status.shadeStatus))
            updateShadeButtonsEnabledState(!status.automaticMode)

            status.countdownModel?.let { countdownModel ->
                currentCountdownSeconds = countdownModel.secondsLeft
                updateTimerDisplay(currentCountdownSeconds)

                if (currentCountdownSeconds > 0 && !status.automaticMode) {
                    startCountdownTimer(currentCountdownSeconds)
                } else {
                    stopCountdownTimer()
                }
            } ?: run {
                stopCountdownTimer()
                remainingTimeText.visibility = View.GONE
            }
        }
    }

    override fun showError(message: String) {
        runOnUiThread {
            hideLoading()
            Toast.makeText(
                this@ClotheslineStatusActivity,
                "Error: $message",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onAutomaticModeUpdated(success: Boolean) {
        runOnUiThread {
            if (!success) {
                autoModeToggle.isChecked = currentAutomaticMode
                showError("Failed to update automatic mode")
            } else {
                Toast.makeText(
                    this@ClotheslineStatusActivity,
                    if (currentAutomaticMode) "Automatic mode enabled" else "Manual mode enabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onManualShadeUpdated(success: Boolean) {
        runOnUiThread {
            if (!success) {
                minutePicker.value = currentManualShade
                showError("Failed to update manual shade time")
            } else {
                Toast.makeText(
                    this@ClotheslineStatusActivity,
                    "Manual shade time set to $currentManualShade minutes",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onRainStatusUpdated(success: Boolean) {
        runOnUiThread {
            if (!success) {
                showError("Failed to update rain status")
            }
        }
    }

    override fun onShadeStatusUpdated(success: Boolean) {
        runOnUiThread {
            if (!success) {
                showShadeStatus(currentShadeStatus,
                    ClotheslineStatusModel.getShadeStatusText(currentShadeStatus))
                showError("Failed to update shade status")
            } else {
                val statusText = if (currentShadeStatus) "retracted" else "extended"
                Toast.makeText(
                    this@ClotheslineStatusActivity,
                    "Shade $statusText successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!::presenter.isInitialized) {
            presenter = ClotheslineStatusPresenter(this)
            presenter.initialize()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCountdownTimer()
        presenter.onDestroy()
    }
}
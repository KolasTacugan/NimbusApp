package com.example.nimbus.ClotheslineStatus

class ClotheslineStatusPresenter(
    private val view: ClotheslineStatusView,
    private val model: ClotheslineStatusModel = ClotheslineStatusModel()
) {

    // View interface that the Activity must implement
    interface ClotheslineStatusView {
        fun showLoading()
        fun hideLoading()
        fun showAutomaticMode(isAutomatic: Boolean)
        fun showManualShade(minutes: Int)
        fun showRainStatus(isRaining: Boolean, statusText: String)
        fun showShadeStatus(isRetracted: Boolean, statusText: String)
        fun showFullStatus(status: ClotheslineStatusModel.ClotheslineStatus)
        fun showError(message: String)
        fun onAutomaticModeUpdated(success: Boolean)
        fun onManualShadeUpdated(success: Boolean)
        fun onRainStatusUpdated(success: Boolean)
        fun onShadeStatusUpdated(success: Boolean)
    }

    // Initialize presenter and start listening for real-time updates
    fun initialize() {
        view.showLoading()
        startListeningToStatus()
    }

    // Start real-time listening to status changes
    private fun startListeningToStatus() {
        model.getCurrentStatus(object : ClotheslineStatusModel.StatusListener {
            override fun onStatusChanged(status: ClotheslineStatusModel.ClotheslineStatus) {
                view.hideLoading()

                // Update all views with current status
                view.showAutomaticMode(status.automaticMode)
                view.showManualShade(status.manualShade)
                view.showRainStatus(status.rainStatus, ClotheslineStatusModel.getRainStatusText(status.rainStatus))
                view.showShadeStatus(status.shadeStatus, ClotheslineStatusModel.getShadeStatusText(status.shadeStatus))
                view.showFullStatus(status)
            }

            override fun onError(error: String) {
                view.hideLoading()
                view.showError(error)
            }
        })
    }

    // Toggle automatic mode
    fun toggleAutomaticMode(currentMode: Boolean) {
        val newMode = !currentMode
        view.showLoading()

        model.updateAutomaticMode(newMode, object : ClotheslineStatusModel.OperationListener {
            override fun onSuccess() {
                view.hideLoading()
                view.showAutomaticMode(newMode)
                view.onAutomaticModeUpdated(true)
            }

            override fun onError(error: String) {
                view.hideLoading()
                view.showError("Failed to toggle automatic mode: $error")
                view.onAutomaticModeUpdated(false)
                // Revert to previous state in UI
                view.showAutomaticMode(currentMode)
            }
        })
    }

    // Update automatic mode with specific value
    fun updateAutomaticMode(isAutomatic: Boolean) {
        view.showLoading()

        model.updateAutomaticMode(isAutomatic, object : ClotheslineStatusModel.OperationListener {
            override fun onSuccess() {
                view.hideLoading()
                view.showAutomaticMode(isAutomatic)
                view.onAutomaticModeUpdated(true)
            }

            override fun onError(error: String) {
                view.hideLoading()
                view.showError("Failed to update automatic mode: $error")
                view.onAutomaticModeUpdated(false)
            }
        })
    }

    // Update manual shade time
    fun updateManualShade(minutes: Int) {
        view.showLoading()

        // Validate input
        if (minutes < 0) {
            view.hideLoading()
            view.showError("Invalid minutes value. Must be positive.")
            view.onManualShadeUpdated(false)
            return
        }

        model.updateManualShade(minutes, object : ClotheslineStatusModel.OperationListener {
            override fun onSuccess() {
                view.hideLoading()
                view.showManualShade(minutes)
                view.onManualShadeUpdated(true)
            }

            override fun onError(error: String) {
                view.hideLoading()
                view.showError("Failed to update manual shade: $error")
                view.onManualShadeUpdated(false)
            }
        })
    }

    // Update rain status (typically called from sensor updates)
    fun updateRainStatus(isRaining: Boolean) {
        view.showLoading()

        model.updateRainStatus(isRaining, object : ClotheslineStatusModel.OperationListener {
            override fun onSuccess() {
                view.hideLoading()
                view.showRainStatus(isRaining, ClotheslineStatusModel.getRainStatusText(isRaining))
                view.onRainStatusUpdated(true)
            }

            override fun onError(error: String) {
                view.hideLoading()
                view.showError("Failed to update rain status: $error")
                view.onRainStatusUpdated(false)
            }
        })
    }

    // Update shade status
    fun updateShadeStatus(isRetracted: Boolean) {
        view.showLoading()

        model.updateShadeStatus(isRetracted, object : ClotheslineStatusModel.OperationListener {
            override fun onSuccess() {
                view.hideLoading()
                view.showShadeStatus(isRetracted, ClotheslineStatusModel.getShadeStatusText(isRetracted))
                view.onShadeStatusUpdated(true)
            }

            override fun onError(error: String) {
                view.hideLoading()
                view.showError("Failed to update shade status: $error")
                view.onShadeStatusUpdated(false)
            }
        })
    }

    // Get current status once (without real-time updates)
    fun getCurrentStatusOnce() {
        view.showLoading()

        // This method would require adding a similar method to the Model
        // For now, we rely on real-time updates from initialize()
        view.hideLoading()
    }

    // Update all status at once (for batch operations)
    fun updateAllStatus(
        automaticMode: Boolean,
        manualShade: Int,
        rainStatus: Boolean,
        shadeStatus: Boolean
    ) {
        view.showLoading()

        val status = ClotheslineStatusModel.ClotheslineStatus(
            automaticMode = automaticMode,
            manualShade = manualShade,
            rainStatus = rainStatus,
            shadeStatus = shadeStatus
        )

        model.updateClotheslineStatus(status, object : ClotheslineStatusModel.OperationListener {
            override fun onSuccess() {
                view.hideLoading()
                view.showFullStatus(status)
                view.onAutomaticModeUpdated(true)
                view.onManualShadeUpdated(true)
                view.onRainStatusUpdated(true)
                view.onShadeStatusUpdated(true)
            }

            override fun onError(error: String) {
                view.hideLoading()
                view.showError("Failed to update all status: $error")
                // Update UI with previous values would need to be handled
            }
        })
    }

    // Handle manual shade time picker selection
    fun onManualShadeTimeSelected(minutes: Int) {
        updateManualShade(minutes)
    }

    // Handle automatic mode switch toggle
    fun onAutomaticModeSwitchToggled(isChecked: Boolean) {
        updateAutomaticMode(isChecked)
    }

    // Handle rain sensor update
    fun onRainSensorUpdate(isWet: Boolean) {
        updateRainStatus(isWet)
    }

    // Handle shade control
    fun toggleShadeStatus(currentStatus: Boolean) {
        updateShadeStatus(!currentStatus)
    }

    // Clean up resources when activity is destroyed
    fun onDestroy() {
        // The model should have a method to remove listeners
        // model.removeStatusListener()
    }
}
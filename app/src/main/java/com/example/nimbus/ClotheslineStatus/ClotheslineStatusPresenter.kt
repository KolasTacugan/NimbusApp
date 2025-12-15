package com.example.nimbus.ClotheslineStatus

class ClotheslineStatusPresenter(
    private val view: ClotheslineStatusView,
    private val model: ClotheslineStatusModel = ClotheslineStatusModel()
) {

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

    fun initialize() {
        view.showLoading()
        startListeningToStatus()
    }

    private fun startListeningToStatus() {
        model.getCurrentStatus(object : ClotheslineStatusModel.StatusListener {
            override fun onStatusChanged(status: ClotheslineStatusModel.ClotheslineStatus) {
                view.hideLoading()
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

    fun startCountdown(minutes: Int) {
        view.showLoading()
        model.startCountdown(minutes, object : ClotheslineStatusModel.OperationListener {
            override fun onSuccess() {
                view.hideLoading()
                view.onManualShadeUpdated(true)
            }

            override fun onError(error: String) {
                view.hideLoading()
                view.showError("Failed to start countdown: $error")
                view.onManualShadeUpdated(false)
            }
        })
    }

    fun updateCountdown(secondsLeft: Long) {
        model.updateCountdown(secondsLeft, object : ClotheslineStatusModel.OperationListener {
            override fun onSuccess() {
                // Countdown updated successfully
            }

            override fun onError(error: String) {
                view.showError("Failed to update countdown: $error")
            }
        })
    }

    fun cancelCountdown() {
        view.showLoading()
        model.cancelCountdown(object : ClotheslineStatusModel.OperationListener {
            override fun onSuccess() {
                view.hideLoading()
            }

            override fun onError(error: String) {
                view.hideLoading()
                view.showError("Failed to cancel countdown: $error")
            }
        })
    }

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
                view.showAutomaticMode(currentMode)
            }
        })
    }

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

    fun updateManualShade(minutes: Int) {
        view.showLoading()

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

    fun getCurrentStatusOnce() {
        view.showLoading()
        view.hideLoading()
    }

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
            }
        })
    }

    fun onManualShadeTimeSelected(minutes: Int) {
        updateManualShade(minutes)
        startCountdown(minutes)
    }

    fun onAutomaticModeSwitchToggled(isChecked: Boolean) {
        updateAutomaticMode(isChecked)
        if (isChecked) {
            cancelCountdown()
        }
    }

    fun onRainSensorUpdate(isWet: Boolean) {
        updateRainStatus(isWet)
    }

    fun toggleShadeStatus(currentStatus: Boolean) {
        updateShadeStatus(!currentStatus)
        cancelCountdown()
    }

    fun onDestroy() {
        // Cleanup if needed
    }
}
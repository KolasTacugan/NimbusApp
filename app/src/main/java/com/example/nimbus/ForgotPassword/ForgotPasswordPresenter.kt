package com.example.nimbus.ForgotPassword

interface ForgotPasswordView {
    fun showEmailError(error: String)
    fun showResetPasswordError(error: String)
    fun showLoading(isLoading: Boolean)
    fun showResetPasswordDialog()
    fun onPasswordResetSuccess()
}

class ForgotPasswordPresenter(
    private val view: ForgotPasswordView,
    private val model: ForgotPasswordModel
) {

    private var verifiedUserKey: String = ""

    fun validateEmail(email: String) {
        // Clear any previous errors
        view.showEmailError("")

        if (email.isBlank()) {
            view.showEmailError("Email cannot be empty")
            return
        }

        val trimmedEmail = email.trim()

        if (!isValidEmailFormat(trimmedEmail)) {
            view.showEmailError("Please enter a valid email address")
            return
        }

        view.showLoading(true)

        model.validateEmail(trimmedEmail, object : ForgotPasswordModel.ForgotPasswordCallback {
            override fun onEmailVerified(userKey: String) {
                view.showLoading(false)
                verifiedUserKey = userKey
                view.showResetPasswordDialog()
            }

            override fun onEmailNotFound() {
                view.showLoading(false)
                view.showEmailError("Email not found in our system")
            }

            override fun onEmailValidationError(error: String) {
                view.showLoading(false)
                view.showEmailError(error)
            }

            override fun onPasswordResetSuccess() {
                // Not used in email validation
            }

            override fun onPasswordResetError(error: String) {
                // Not used in email validation
            }
        })
    }

    fun resetPassword(newPassword: String, confirmPassword: String) {
        // Clear any previous errors
        view.showResetPasswordError("")

        if (newPassword.isBlank()) {
            view.showResetPasswordError("New password cannot be empty")
            return
        }

        if (newPassword.length < 6) {
            view.showResetPasswordError("Password must be at least 6 characters")
            return
        }

        if (confirmPassword.isBlank()) {
            view.showResetPasswordError("Please confirm your password")
            return
        }

        if (newPassword != confirmPassword) {
            view.showResetPasswordError("Passwords do not match")
            return
        }

        if (verifiedUserKey.isEmpty()) {
            view.showResetPasswordError("User session expired. Please try again.")
            return
        }

        view.showLoading(true)

        model.resetPassword(verifiedUserKey, newPassword, object : ForgotPasswordModel.ForgotPasswordCallback {
            override fun onEmailVerified(userKey: String) {
                // Not used in password reset
            }

            override fun onEmailNotFound() {
                view.showLoading(false)
                view.showResetPasswordError("User not found. Please try again.")
            }

            override fun onEmailValidationError(error: String) {
                view.showLoading(false)
                view.showResetPasswordError("Validation error: $error")
            }

            override fun onPasswordResetSuccess() {
                view.showLoading(false)
                clearUserSession()
                view.onPasswordResetSuccess()
            }

            override fun onPasswordResetError(error: String) {
                view.showLoading(false)
                view.showResetPasswordError(error)
            }
        })
    }

    fun clearUserSession() {
        verifiedUserKey = ""
    }

    private fun isValidEmailFormat(email: String): Boolean {
        val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailPattern.matches(email)
    }
}
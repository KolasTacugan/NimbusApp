package com.example.nimbus.ForgotPassword

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nimbus.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ForgotPasswordActivity : AppCompatActivity(), ForgotPasswordView {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var submitButton: Button
    private lateinit var backToLoginText: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var presenter: ForgotPasswordPresenter
    private lateinit var model: ForgotPasswordModel

    private var resetPasswordDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI components
        initViews()

        // Initialize MVP components
        model = ForgotPasswordModel()
        presenter = ForgotPasswordPresenter(this, model)

        // Setup listeners
        setupListeners()
    }

    private fun initViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout)
        emailEditText = findViewById(R.id.emailEditText)
        submitButton = findViewById(R.id.submitButton)
        backToLoginText = findViewById(R.id.backToLoginText)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        submitButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            presenter.validateEmail(email)
        }

        backToLoginText.setOnClickListener {
            finish() // Go back to login activity
        }

        // Clear error when user starts typing
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                emailInputLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun showEmailError(error: String) {
        runOnUiThread {
            emailInputLayout.error = error
            if (error.isNotEmpty()) {
                emailEditText.requestFocus()
            }
        }
    }

    override fun showResetPasswordError(error: String) {
        runOnUiThread {
            Toast.makeText(this@ForgotPasswordActivity, error, Toast.LENGTH_LONG).show()
        }
    }

    override fun showLoading(isLoading: Boolean) {
        runOnUiThread {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            submitButton.isEnabled = !isLoading

            // Disable back to login during loading
            backToLoginText.isEnabled = !isLoading
        }
    }

    override fun showResetPasswordDialog() {
        runOnUiThread {
            createAndShowResetPasswordDialog()
        }
    }

    override fun onPasswordResetSuccess() {
        runOnUiThread {
            resetPasswordDialog?.dismiss()
            Toast.makeText(
                this@ForgotPasswordActivity,
                "Password reset successfully!",
                Toast.LENGTH_LONG
            ).show()

            // Navigate back to login after successful reset
            finish()
        }
    }

    private fun createAndShowResetPasswordDialog() {
        resetPasswordDialog = Dialog(this).apply {
            setContentView(R.layout.dialog_reset_password)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            setCancelable(false)

            val newPasswordEditText = findViewById<TextInputEditText>(R.id.newPasswordEditText)
            val confirmPasswordEditText = findViewById<TextInputEditText>(R.id.confirmPasswordEditText)
            val resetPasswordButton = findViewById<Button>(R.id.resetPasswordButton)
            val cancelButton = findViewById<Button>(R.id.cancelButton)
            val newPasswordInputLayout = findViewById<TextInputLayout>(R.id.newPasswordInputLayout)
            val confirmPasswordInputLayout = findViewById<TextInputLayout>(R.id.confirmPasswordInputLayout)

            // Clear errors when typing
            newPasswordEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    newPasswordInputLayout.error = null
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    confirmPasswordInputLayout.error = null
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            resetPasswordButton.setOnClickListener {
                val newPassword = newPasswordEditText.text.toString().trim()
                val confirmPassword = confirmPasswordEditText.text.toString().trim()

                // Basic validation
                if (newPassword.isEmpty()) {
                    newPasswordInputLayout.error = "New password cannot be empty"
                    newPasswordEditText.requestFocus()
                    return@setOnClickListener
                }

                if (confirmPassword.isEmpty()) {
                    confirmPasswordInputLayout.error = "Please confirm your password"
                    confirmPasswordEditText.requestFocus()
                    return@setOnClickListener
                }

                if (newPassword.length < 6) {
                    newPasswordInputLayout.error = "Password must be at least 6 characters"
                    newPasswordEditText.requestFocus()
                    return@setOnClickListener
                }

                if (newPassword != confirmPassword) {
                    confirmPasswordInputLayout.error = "Passwords do not match"
                    confirmPasswordEditText.requestFocus()
                    return@setOnClickListener
                }

                // Call presenter to reset password
                presenter.resetPassword(newPassword, confirmPassword)
            }

            cancelButton.setOnClickListener {
                presenter.clearUserSession()
                dismiss()
            }
        }

        resetPasswordDialog?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        resetPasswordDialog?.dismiss()
        presenter.clearUserSession()
    }

    companion object {
        fun startActivity(activity: AppCompatActivity) {
            val intent = Intent(activity, ForgotPasswordActivity::class.java)
            activity.startActivity(intent)
        }
    }
}
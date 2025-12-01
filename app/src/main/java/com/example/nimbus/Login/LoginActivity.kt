package com.example.nimbus.Login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nimbus.Dashboard.DashboardActivity
import com.example.nimbus.R
import com.example.nimbus.Register.RegisterActivity
import com.example.nimbus.ForgotPassword.ForgotPasswordActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var signUpText: TextView
    private lateinit var forgotPasswordText: TextView

    private val presenter = LoginPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signUpText = findViewById(R.id.signUpText)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)

        // Login button click
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            handleLogin(email, password)
        }

        // Navigate to RegisterActivity
        signUpText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Navigate to ForgotPasswordActivity
        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun handleLogin(email: String, password: String) {
        presenter.login(
            email,
            password,
            onSuccess = { user ->
                // Save user info in SharedPreferences
                val sharedPref = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
                sharedPref.edit()
                    .putString("USER_UID", user.uid)
                    .putString("USER_EMAIL", user.email)
                    .putString("USER_FIRST_NAME", user.firstName)
                    .putString("USER_MIDDLE_NAME", user.middleName)
                    .putString("USER_LAST_NAME", user.lastName)
                    .apply()

                Toast.makeText(
                    this,
                    "Login successful! Welcome, ${user.firstName}",
                    Toast.LENGTH_SHORT
                ).show()

                // Navigate to DashboardActivity
                val intent = Intent(this, DashboardActivity::class.java).apply {
                    putExtra("USER_FIRST_NAME", user.firstName)
                    putExtra("USER_LAST_NAME", user.lastName)
                    putExtra("USER_EMAIL", user.email)
                }
                startActivity(intent)
                finish()
            },
            onError = { errorMessage ->
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }
}

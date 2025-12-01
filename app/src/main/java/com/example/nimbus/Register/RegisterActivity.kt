package com.example.nimbus.Register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nimbus.Login.LoginActivity
import com.example.nimbus.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var presenter: RegisterPresenter

    // UI components
    private lateinit var etFirstName: EditText
    private lateinit var etMiddleName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        presenter = RegisterPresenter(this)

        initViews()
        initListeners()
    }

    private fun initViews() {
        etFirstName = findViewById(R.id.etFirstName)
        etMiddleName = findViewById(R.id.etMiddleName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initListeners() {
        btnRegister.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            presenter.register(
                firstName = etFirstName.text.toString().trim(),
                middleName = etMiddleName.text.toString().trim(),
                lastName = etLastName.text.toString().trim(),
                email = etEmail.text.toString().trim(),
                password = etPassword.text.toString().trim(),
                confirmPassword = etConfirmPassword.text.toString().trim()
            )
        }
    }

    // Presenter Callbacks

    fun showNameError(message: String, field: String) {
        progressBar.visibility = View.GONE
        when (field) {
            "first" -> etFirstName.error = message
            "last" -> etLastName.error = message
        }
    }

    fun showEmailError(message: String) {
        progressBar.visibility = View.GONE
        etEmail.error = message
    }

    fun showPasswordError(message: String) {
        progressBar.visibility = View.GONE
        etPassword.error = message
    }

    fun showConfirmPasswordError(message: String) {
        progressBar.visibility = View.GONE
        etConfirmPassword.error = message
    }

    fun showRegisterSuccess() {
        progressBar.visibility = View.GONE
        Toast.makeText(this, "Registration Successful! Please login.", Toast.LENGTH_LONG).show()

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    fun showRegisterError(message: String) {
        progressBar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

package com.laila.sustainwise.ui.password

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.laila.sustainwise.R
import com.laila.sustainwise.ui.customview.EmailEditText
import com.laila.sustainwise.ui.login.LoginActivity

class PasswordActivity : AppCompatActivity() {

    // Firebase Authentication instance
    private lateinit var auth: FirebaseAuth

    // UI components
    private lateinit var emailInput: EmailEditText
    private lateinit var resetButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Bind UI components
        emailInput = findViewById(R.id.ed_forgotp_email)
        resetButton = findViewById(R.id.change_password_button)

        // Set up click listener for the reset button
        resetButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (TextUtils.isEmpty(email)) {
                emailInput.error = "Email field can't be empty"
                return@setOnClickListener
            }

            sendPasswordResetEmail(email)

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            Log.d("PasswordActivity", "FirebaseAuth initialized")
            Log.d("PasswordActivity", "Email entered: ${emailInput.text.toString().trim()}")

        }

        setupActionBar()
    }

    private fun setupActionBar() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                // Show success message
                Toast.makeText(
                    this,
                    "Password reset email sent to $email. Please check your inbox.",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { exception ->
                // Show failure message
                Toast.makeText(
                    this,
                    "Failed to send reset email: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
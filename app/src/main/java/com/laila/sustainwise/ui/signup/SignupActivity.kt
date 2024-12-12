package com.laila.sustainwise.ui.signup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.laila.sustainwise.R
import com.laila.sustainwise.ui.main.MainActivity

class SignupActivity : AppCompatActivity() {

    // Firebase Authentication instance
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    // Request code for Google Sign-In
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize FirebaseAuth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Google Sign-In Button (ImageView)
        val googleLoginButton = findViewById<ImageView>(R.id.google_login_button)
        googleLoginButton.setOnClickListener {
            signInWithGoogle()  // Trigger Google Sign-In
        }

        // Signup button (MaterialButton)
        val signupButton = findViewById<MaterialButton>(R.id.signup_button)
        signupButton.setOnClickListener {
            createUserWithEmailAndPassword()
        }

        // Google Sign-In Configuration
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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

    // Google Sign-In logic
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Handle the result from Google Sign-In
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign-In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign-In failed
                Log.w("SignUpActivity", "Google sign-in failed", e)
                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Firebase Authentication with Google
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, navigate to main activity or show success
                        val user = auth.currentUser
                        if (user != null) {
                            saveGoogleUserToFirestore(user.uid, account.email ?: "")
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("SignUpActivity", "signInWithCredential:failure", task.exception)
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // Save Google user to Firestore
    private fun saveGoogleUserToFirestore(userId: String, email: String) {
        val user = hashMapOf(
            "username" to "",
            "email" to email,
            "saldo" to 0,
            "photo" to null
        )

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    // Create a new user in Firestore
                    db.collection("users").document(userId).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Google User added successfully",
                                Toast.LENGTH_LONG
                            ).show()
                            navigateToMainActivity()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Failed to save Google user: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    // User exists, navigate to main activity
                    navigateToMainActivity()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to check Firestore: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
    }

    // Create user with email and password
    private fun createUserWithEmailAndPassword() {
        val username = findViewById<EditText>(R.id.ed_signup_name).text.toString()
        val email = findViewById<EditText>(R.id.ed_signup_email).text.toString()
        val password = findViewById<EditText>(R.id.ed_signup_password).text.toString()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User created successfully, store additional user info
                    val user = auth.currentUser
                    val userData = hashMapOf(
                        "username" to username,
                        "email" to email,
                        "saldo" to 0,  // Default saldo is 0
                        "photo" to null  // Photo is set to null initially
                    )

                    // Save user data to Firestore
                    if (user != null) {
                        db.collection("users").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "User created and data saved",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateToMainActivity()
                            }
                            .addOnFailureListener { e ->
                                Log.w("SignUpActivity", "Error adding document", e)
                                Toast.makeText(this, "Error saving user data", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                } else {
                    // If user creation fails, display a message to the user.
                    Log.w(
                        "SignUpActivity",
                        "createUserWithEmailAndPassword:failure",
                        task.exception
                    )
                    Toast.makeText(this, "Signup Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Navigate to MainActivity after successful signup or login
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Close the signup activity
    }
}
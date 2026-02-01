package com.example.hydrasense

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import android.widget.ImageButton

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnThemeToggle = findViewById<ImageButton>(R.id.btnThemeToggle)
        val btnGoogle = findViewById<com.google.android.gms.common.SignInButton>(R.id.btnGoogleSignIn)
        val btnOffline = findViewById<Button>(R.id.btnOffline)
        
        updateThemeIcon(btnThemeToggle)
        btnThemeToggle.setOnClickListener {
            ThemeManager.toggleTheme(this)
            updateThemeIcon(btnThemeToggle)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                // For now, handle legacy login as offline entry or sync attempt
                navigateToDashboard("User", email)
            }
        }

        btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        btnOffline.setOnClickListener {
            navigateToDashboard("Guest", "offline@hydrasense.local")
        }

        findViewById<TextView>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, RegisterUser::class.java))
        }

        findViewById<android.view.View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val name = account?.displayName ?: "Google User"
            val email = account?.email ?: "google@user.com"
            Toast.makeText(this, "Signed in as $name", Toast.LENGTH_SHORT).show()
            navigateToDashboard(name, email)
        } catch (e: ApiException) {
            Toast.makeText(this, "Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToDashboard(userName: String, userEmail: String, age: Int = 25, weight: String = "72 Kg", height: String = "178 cm") {
        // Save session for persistent login
        val prefs = getSharedPreferences("hydrasense_auth_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("current_user_email", userEmail).apply()
        prefs.edit().putString("current_user_name", userName).apply()

        SessionManager.addSavedUser(this, SavedUser(userName, userEmail, age, weight, height))
        
        val intent = Intent(this, CardActivity::class.java)
        intent.putExtra("USER_NAME", userName)
        intent.putExtra("USER_EMAIL", userEmail)
        startActivity(intent)
        finish()
    }

    private fun updateThemeIcon(button: ImageButton) {
        if (ThemeManager.isDarkMode(this)) {
            button.setImageResource(R.drawable.ic_sun)
        } else {
            button.setImageResource(R.drawable.ic_moon)
        }
    }
}

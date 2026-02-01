package com.example.hydrasense

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterUser : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_user)
        supportActionBar?.hide()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etOtp = findViewById<EditText>(R.id.etOtp)
        val btnSendOtp = findViewById<Button>(R.id.btnSendOtp)
        val btnVerifyOtp = findViewById<Button>(R.id.btnVerifyOtp)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tlOtp = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tlOtp)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        var isEmailVerified = false
        btnRegister.isEnabled = false
        btnRegister.alpha = 0.5f

        btnSendOtp.setOnClickListener {
            val email = etEmail.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.instance.sendOtp(OtpRequest(email)).enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterUser, "OTP Sent! Check your Gmail.", Toast.LENGTH_SHORT).show()
                        tlOtp.visibility = android.view.View.VISIBLE
                        btnVerifyOtp.visibility = android.view.View.VISIBLE
                        btnSendOtp.text = "Resend OTP"
                    }
                }
                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterUser, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        btnVerifyOtp.setOnClickListener {
            val email = etEmail.text.toString()
            val code = etOtp.text.toString()
            if (code.length < 6) {
                Toast.makeText(this, "Enter 6-digit code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.instance.verifyOtp(OtpVerifyRequest(email, code)).enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    if (response.isSuccessful) {
                        isEmailVerified = true
                        Toast.makeText(this@RegisterUser, "Email Verified!", Toast.LENGTH_SHORT).show()
                        btnRegister.isEnabled = true
                        btnRegister.alpha = 1.0f
                        btnVerifyOtp.visibility = android.view.View.GONE
                        tlOtp.isEnabled = false
                        etEmail.isEnabled = false
                    } else {
                        Toast.makeText(this@RegisterUser, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterUser, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (!isEmailVerified) {
                Toast.makeText(this, "Please verify your email first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val registerRequest = LoginRequest(email, password)

            RetrofitClient.instance.register(registerRequest).enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterUser, "Account Created!", Toast.LENGTH_LONG).show()
                        
                        val newUser = SavedUser(
                            userName = email.substringBefore("@"),
                            userEmail = email,
                            age = 25,
                            weight = "0",
                            height = "0"
                        )
                        SessionManager.addSavedUser(this@RegisterUser, newUser)

                        val intent = Intent(this@RegisterUser, User_Details::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@RegisterUser, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterUser, "Network Error", Toast.LENGTH_SHORT).show()
                }
            })
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<android.view.View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }
}

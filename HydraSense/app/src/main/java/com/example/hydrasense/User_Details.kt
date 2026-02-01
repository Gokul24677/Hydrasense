package com.example.hydrasense

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class User_Details : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_user_details)
        supportActionBar?.hide()

        val tvGender = findViewById<TextView>(R.id.tvGenderValue)
        val tvDob = findViewById<TextView>(R.id.tvDobValue)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val btnNext = findViewById<Button>(R.id.btnNext)

        findViewById<android.view.View>(R.id.llGender).setOnClickListener {
            val genders = arrayOf("Male", "Female", "Other")
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Gender")
                .setItems(genders) { _, which ->
                    tvGender.text = genders[which]
                    tvGender.setTextColor(android.graphics.Color.parseColor("#333333"))
                }
                .show()
        }

        findViewById<android.view.View>(R.id.llDob).setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                tvDob.text = "$d/${m+1}/$y"
                tvDob.setTextColor(android.graphics.Color.parseColor("#333333"))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnNext.setOnClickListener {
            val weightStr = etWeight.text.toString()
            val heightStr = etHeight.text.toString()
            val gender = tvGender.text.toString()
            val dob = tvDob.text.toString()

            if (weightStr.isEmpty() || heightStr.isEmpty() || gender == "Choose Gender" || dob == "Date of Birth") {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weight = weightStr.toDoubleOrNull() ?: 0.0
            val goalLiters = weight * 0.035
            val goalRounded = String.format("%.1f", goalLiters)

            val currentUserEmail = getSharedPreferences("hydrasense_auth_prefs", Context.MODE_PRIVATE)
                .getString("current_user_email", "guest@example.com") ?: "guest@example.com"
            val currentUserName = getSharedPreferences("hydrasense_auth_prefs", Context.MODE_PRIVATE)
                .getString("current_user_name", "User") ?: "User"

            // Save to Local Room DB
            val db = com.example.hydrasense.db.AppDatabase.getDatabase(this)
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                val userEntity = com.example.hydrasense.db.UserEntity(
                    email = currentUserEmail,
                    name = currentUserName,
                    age = 25, // Default or fetched
                    weight = weight,
                    height = heightStr.toDoubleOrNull() ?: 0.0,
                    gender = gender,
                    dob = dob,
                    dailyGoal = goalLiters,
                    avatarRes = R.drawable.prof
                )
                db.userDao().insertUser(userEntity)
            }

            // Still update session manager for legacy support and selector
            val updatedUser = SavedUser(
                userName = currentUserName,
                userEmail = currentUserEmail,
                age = 25,
                weight = "$weightStr Kg",
                height = "$heightStr cm",
                gender = gender,
                dob = dob,
                dailyGoal = goalRounded
            )
            SessionManager.addSavedUser(this, updatedUser)

            Toast.makeText(this, "Profile Saved Locally", Toast.LENGTH_SHORT).show()
            Toast.makeText(this, "Daily Goal: $goalRounded Liters", Toast.LENGTH_LONG).show()
            
            val intent = Intent(this, CardActivity::class.java)
            intent.putExtra("USER_NAME", currentUserName)
            intent.putExtra("DAILY_GOAL", goalRounded)
            startActivity(intent)
            finish()
        }
    }
}

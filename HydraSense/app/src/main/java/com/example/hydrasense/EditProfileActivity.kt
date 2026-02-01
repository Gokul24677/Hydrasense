package com.example.hydrasense

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_edit_profile)
        supportActionBar?.hide()

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etWeight = findViewById<TextInputEditText>(R.id.etWeight)
        val etHeight = findViewById<TextInputEditText>(R.id.etHeight)
        val etDOB = findViewById<TextInputEditText>(R.id.etDOB)

        // Initialize with current data
        etName.setText(intent.getStringExtra("USER_NAME") ?: "Gokul Sridhar")
        etWeight.setText(intent.getStringExtra("USER_WEIGHT") ?: "72")
        etHeight.setText(intent.getStringExtra("USER_HEIGHT") ?: "178")
        etDOB.setText(intent.getStringExtra("USER_DOB") ?: "15 Oct 1998")

        findViewById<android.view.View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.btnSave).setOnClickListener {
            // In a real app, this would update the database and shared prefs
            Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }

        etDOB.setOnClickListener {
            Toast.makeText(this, "Opening Date Picker...", Toast.LENGTH_SHORT).show()
        }
    }
}

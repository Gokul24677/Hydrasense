package com.example.hydrasense

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

import android.widget.ImageButton

class CardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ThemeManager.applyTheme(this)
        
        setContentView(R.layout.activity_card)
        supportActionBar?.hide()

        val userName = intent.getStringExtra("USER_NAME") ?: "User"
        findViewById<TextView>(R.id.tvGreeting).text = "Hi, $userName 👋"
        
        val btnThemeToggle = findViewById<ImageButton>(R.id.btnThemeToggle)
        updateThemeIcon(btnThemeToggle)
        
        btnThemeToggle.setOnClickListener {
            ThemeManager.toggleTheme(this)
            updateThemeIcon(btnThemeToggle)
        }

        // Card Clicks
        findViewById<View>(R.id.cardUrine).setOnClickListener {
            startActivity(Intent(this, PH_Readings::class.java))
        }

        findViewById<View>(R.id.cardDevice).setOnClickListener {
            startActivity(Intent(this, Device::class.java))
        }

        findViewById<View>(R.id.cardDiagnosis).setOnClickListener {
            startActivity(Intent(this, Diagnostics::class.java))
        }

        findViewById<View>(R.id.cardFamily).setOnClickListener {
            startActivity(Intent(this, FamilyActivity::class.java))
        }

        findViewById<View>(R.id.cardMeter).setOnClickListener {
            startActivity(Intent(this, HydrationMeterActivity::class.java))
        }

        findViewById<HydrationMeterView>(R.id.dashboardMeterView).setPercentage(48)

        findViewById<BottomNavigationView>(R.id.bottomNav)
        NavigationHelper.setupBottomNavigation(this, R.id.nav_home)
    }

    private fun updateThemeIcon(button: ImageButton) {
        if (ThemeManager.isDarkMode(this)) {
            button.setImageResource(R.drawable.ic_sun)
        } else {
            button.setImageResource(R.drawable.ic_moon)
        }
    }
}

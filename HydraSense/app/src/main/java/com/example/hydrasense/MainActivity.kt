package com.example.hydrasense

import com.example.hydrasense.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView

import android.widget.ImageButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply theme before setting content view
        ThemeManager.applyTheme(this)
        
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val ivWaterDrop = findViewById<ImageView>(R.id.ivWaterDrop)
        val tvAppName = findViewById<TextView>(R.id.tvAppName)
        val tvTagline = findViewById<TextView>(R.id.tvTagline)
        val btnThemeToggle = findViewById<ImageButton>(R.id.btnThemeToggle)

        // Setup Theme Toggle
        updateThemeIcon(btnThemeToggle)
        btnThemeToggle.setOnClickListener {
            ThemeManager.toggleTheme(this)
            updateThemeIcon(btnThemeToggle)
            // Recreate to apply theme immediately
            // Note: Since this is MainActivity with animations, recreating might be jarring, 
            // but it's required for proper theme switch. 
            // Ideally, we'd rely on automatic activity recreation by setDefaultNightMode.
        }

        // 1. Load the water drop falling animation
        val dropDownAnim = AnimationUtils.loadAnimation(this, R.anim.anim_drop_down)
        ivWaterDrop.startAnimation(dropDownAnim)

        // 2. Load the fade-in animation for text
        val fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        // Briefly wait for the drop to "land" before showing text and starting pulse
        Handler(Looper.getMainLooper()).postDelayed({
            tvAppName.visibility = View.VISIBLE
            tvAppName.alpha = 1.0f
            tvAppName.startAnimation(fadeInAnim)

            tvTagline.visibility = View.VISIBLE
            tvTagline.alpha = 1.0f
            tvTagline.startAnimation(fadeInAnim)

            // Start a subtle wavy floating animation for the logo
            val waveAnim = AnimationUtils.loadAnimation(this, R.anim.wave_float)
            ivWaterDrop.startAnimation(waveAnim)
        }, 1200) // Adjusted to match dropDownAnim duration

        // 3. Transition Logic: Skip login if user exists, otherwise go to User selector
        Handler(Looper.getMainLooper()).postDelayed({
            val savedUsers = SessionManager.getSavedUsers(this)
            if (savedUsers.isNotEmpty()) {
                val user = savedUsers[0]
                val intent = Intent(this, CardActivity::class.java)
                intent.putExtra("USER_NAME", user.userName)
                intent.putExtra("USER_EMAIL", user.userEmail)
                startActivity(intent)
            } else {
                val intent = Intent(this, User::class.java)
                startActivity(intent)
            }
            finish()
        }, 4000)
    }

    private fun updateThemeIcon(button: ImageButton) {
        if (ThemeManager.isDarkMode(this)) {
            button.setImageResource(R.drawable.ic_sun)
        } else {
            button.setImageResource(R.drawable.ic_moon)
        }
    }
}

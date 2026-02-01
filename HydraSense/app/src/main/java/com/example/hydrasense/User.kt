package com.example.hydrasense

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout

class User : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_user2)
        supportActionBar?.hide()

        // Setup Pulse Animations
        startPulseAnimation(findViewById(R.id.pulseRing1), 0)
        startPulseAnimation(findViewById(R.id.pulseRing2), 400)
        startPulseAnimation(findViewById(R.id.pulseRing3), 800)

        findViewById<android.view.View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            startActivity(Intent(this, RegisterUser::class.java))
        }

        refreshUserList()
    }

    private fun refreshUserList() {
        val container = findViewById<LinearLayout>(R.id.userListContainer)
        container.removeAllViews()

        val savedUsers = SessionManager.getSavedUsers(this)
        val inflater = LayoutInflater.from(this)

        savedUsers.forEach { user ->
            val itemView = inflater.inflate(R.layout.item_saved_user, container, false)
            
            itemView.findViewById<android.widget.TextView>(R.id.tvName).text = user.userName
            itemView.findViewById<android.widget.TextView>(R.id.tvEmail).text = user.userEmail
            itemView.findViewById<android.widget.ImageView>(R.id.ivAvatar).setImageResource(user.avatarRes)

            itemView.setOnClickListener {
                navigateToDashboard(user)
            }

            itemView.findViewById<android.view.View>(R.id.btnRemove).setOnClickListener {
                SessionManager.removeUser(this, user.userEmail)
                refreshUserList()
            }

            container.addView(itemView)
        }
    }

    private fun navigateToDashboard(user: SavedUser) {
        val intent = Intent(this, CardActivity::class.java)
        intent.putExtra("USER_NAME", user.userName)
        intent.putExtra("USER_EMAIL", user.userEmail)
        intent.putExtra("USER_AGE", user.age)
        intent.putExtra("USER_WEIGHT", user.weight)
        intent.putExtra("USER_HEIGHT", user.height)
        startActivity(intent)
        finish()
    }

    private fun startPulseAnimation(view: android.view.View, delay: Long) {
        val scaleX = android.animation.ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f)
        val scaleY = android.animation.ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f)
        val alpha = android.animation.ObjectAnimator.ofFloat(view, "alpha", 0.6f, 0.2f, 0.6f)

        android.animation.AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 2000
            startDelay = delay
            // Make it repeating
            scaleX.repeatCount = android.animation.ValueAnimator.INFINITE
            scaleY.repeatCount = android.animation.ValueAnimator.INFINITE
            alpha.repeatCount = android.animation.ValueAnimator.INFINITE
            scaleX.repeatMode = android.animation.ValueAnimator.REVERSE
            scaleY.repeatMode = android.animation.ValueAnimator.REVERSE
            alpha.repeatMode = android.animation.ValueAnimator.REVERSE
            start()
        }
    }
}

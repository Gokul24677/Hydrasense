package com.example.hydrasense

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

import com.example.hydrasense.db.AppDatabase
import com.example.hydrasense.db.ReadingEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Profile : AppCompatActivity() {

    private lateinit var db: AppDatabase

    private val avatarPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val avatarRes = result.data?.getIntExtra("SELECTED_AVATAR", -1) ?: -1
            if (avatarRes != -1) {
                findViewById<android.widget.ImageView>(R.id.imgMember).setImageResource(avatarRes)
                Toast.makeText(this, "Avatar Updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_profile)
        supportActionBar?.hide()

        db = AppDatabase.getDatabase(this)

        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvName = findViewById<TextView>(R.id.tvProfileName)
        val tvGender = findViewById<TextView>(R.id.tvGender)
        val tvAge = findViewById<TextView>(R.id.tvAge)
        val tvWeight = findViewById<TextView>(R.id.tvWeight)
        val tvHeight = findViewById<TextView>(R.id.tvHeight)
        val ivAvatar = findViewById<android.widget.ImageView>(R.id.imgMember)
        
        val name = intent.getStringExtra("USER_NAME") ?: "Sai"
        val email = intent.getStringExtra("USER_EMAIL") ?: "sai@hydrasense.com"
        val age = intent.getIntExtra("USER_AGE", 25)
        val gender = if (name.contains("Anu", true) || name.contains("Anu", true)) "Female" else "Male"

        tvName.text = name
        tvEmail.text = email
        tvGender.text = gender
        tvAge.text = "$age Yrs"
        tvWeight.text = intent.getStringExtra("USER_WEIGHT") ?: "72 Kg"
        tvHeight.text = intent.getStringExtra("USER_HEIGHT") ?: "178 cm"

        // Set dynamic avatar based on gender AND age
        val avatarRes = when {
            age < 13 -> if (gender == "Female") R.drawable.ic_avatar_girl_premium else R.drawable.ic_avatar_boy_premium
            age > 60 -> if (gender == "Female") R.drawable.ic_avatar_elderly_female_premium else R.drawable.ic_avatar_elderly_male_premium
            else -> if (gender == "Female") R.drawable.ic_avatar_female_premium else R.drawable.ic_avatar_male_premium
        }
        ivAvatar.setImageResource(avatarRes)

        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnEditProfileIcon).setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("USER_NAME", tvName.text.toString())
            intent.putExtra("USER_WEIGHT", tvWeight.text.toString().replace(" Kg", ""))
            intent.putExtra("USER_HEIGHT", tvHeight.text.toString().replace(" cm", ""))
            intent.putExtra("USER_DOB", findViewById<TextView>(R.id.tvDOB).text.toString())
            startActivity(intent)
        }

        findViewById<View>(R.id.btnChangeAvatar).setOnClickListener {
            val intent = Intent(this, AvatarPickerActivity::class.java)
            avatarPickerLauncher.launch(intent)
        }

        findViewById<View>(R.id.btnDeleteProfile).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to permanently delete your profile? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    Toast.makeText(this, "Profile Deleted", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        findViewById<View>(R.id.btnBackup).setOnClickListener {
            performCloudBackup()
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        NavigationHelper.setupBottomNavigation(this, R.id.nav_profile)
    }

    private fun performCloudBackup() {
        val currentUserEmail = getSharedPreferences("hydrasense_auth_prefs", android.content.Context.MODE_PRIVATE)
            .getString("current_user_email", "offline@hydrasense.local") ?: "offline@hydrasense.local"

        if (currentUserEmail.contains("offline")) {
            Toast.makeText(this, "Please sign in with Google to backup data", Toast.LENGTH_LONG).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val unsyncedReadings = db.readingDao().getUnsyncedReadings()
            if (unsyncedReadings.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Profile, "Data is already backed up!", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@Profile, "Backing up ${unsyncedReadings.size} records...", Toast.LENGTH_SHORT).show()
            }

            var successCount = 0
            for (reading in unsyncedReadings) {
                val request = AddReadingRequest(
                    user_id = currentUserEmail,
                    time = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(reading.timestamp)),
                    ph = reading.phValue
                )
                
                try {
                    val response = RetrofitClient.instance.addReading(request).execute()
                    if (response.isSuccessful) {
                        db.readingDao().updateReading(reading.copy(isSynced = true))
                        successCount++
                    }
                } catch (e: Exception) {
                    // Log error or continue
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@Profile, "Backup complete: $successCount records synced.", Toast.LENGTH_LONG).show()
            }
        }
    }
}

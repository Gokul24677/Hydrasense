package com.example.hydrasense

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.hydrasense.db.AppDatabase
import com.example.hydrasense.db.ReadingEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PH_Readings : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private val records = mutableListOf<HydrationRecord>()
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_ph_readings)
        supportActionBar?.hide()

        db = AppDatabase.getDatabase(this)

        val tvPh = findViewById<TextView>(R.id.tvPh)
        val tvColor = findViewById<TextView>(R.id.tvColor)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvRecommended = findViewById<TextView>(R.id.tvRecommended)
        val progressBar = findViewById<android.widget.ProgressBar>(R.id.analysisProgress)
        rvHistory = findViewById(R.id.rvHistory)

        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        // Setup History from Local DB
        rvHistory.layoutManager = LinearLayoutManager(this)
        loadLocalHistory()

        // Check for live data from BLE
        val isLive = intent.getBooleanExtra("IS_LIVE", false)
        val livePh = intent.getDoubleExtra("LIVE_PH", 5.8)
        val liveColorIndex = intent.getIntExtra("LIVE_COLOR", 0)

        // Set values
        if (isLive) {
            tvPh?.text = String.format("%.1f", livePh)
            tvColor?.text = "Index: $liveColorIndex"
            
            val status = when {
                livePh < 6.0 -> "Acidic ⚠️"
                livePh > 7.5 -> "Alkaline ⚠️"
                else -> "Normal ✅"
            }
            tvStatus?.text = status
            
            val colorDesc = when(liveColorIndex) {
                0 -> "Clear"
                1 -> "Pale Yellow"
                2 -> "Yellow"
                3 -> "Amber"
                4 -> "Dark"
                else -> "Unknown"
            }
            tvRecommended?.text = "Color: $colorDesc"
            
            progressBar?.progress = (livePh * 10).toInt().coerceIn(0, 100)
            
            // Save to Local DB
            saveReadingToLocal(livePh, liveColorIndex)
            Toast.makeText(this, "Live Data Saved Locally", Toast.LENGTH_SHORT).show()
        } else {
            // Initial mock values for demo
            tvPh?.text = "5.8"
            tvColor?.text = "3800"
            tvStatus?.text = "Needs Hydration ⚠️"
            tvRecommended?.text = "2.0 L left"
            progressBar?.progress = 38
        }
// ... rest of the file ...

        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            if (isLive) {
                // If live, maybe return to Device to scan again
                val intentScan = Intent(this, Device::class.java)
                startActivity(intentScan)
                finish()
            } else {
                tvPh?.text = "6.0"
                tvColor?.text = "2200"
                tvStatus?.text = "Hydrated ✅"
                tvStatus?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                tvRecommended?.text = "0.5 L left"
                progressBar?.progress = 85
                Toast.makeText(this, "Analysis Updated", Toast.LENGTH_SHORT).show()
            }
        }

        NavigationHelper.setupBottomNavigation(this, R.id.nav_analysis)
    }

    private fun loadLocalHistory() {
        val currentUserEmail = getSharedPreferences("hydrasense_auth_prefs", Context.MODE_PRIVATE)
            .getString("current_user_email", "guest@example.com") ?: "guest@example.com"

        CoroutineScope(Dispatchers.IO).launch {
            db.readingDao().getReadingsForUser(currentUserEmail).collect { readingEntities ->
                val uiRecords = readingEntities.map { entity ->
                    val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                    val timeStr = sdf.format(java.util.Date(entity.timestamp))
                    
                    val status = when {
                        entity.phValue < 6.0 -> "Acidic"
                        entity.phValue > 7.5 -> "Alkaline"
                        else -> "Healthy"
                    }
                    HydrationRecord(timeStr, "Analysis Result", entity.phValue.toString(), status)
                }
                
                withContext(Dispatchers.Main) {
                    records.clear()
                    records.addAll(uiRecords)
                    rvHistory.adapter = HydrationHistoryAdapter(records)
                }
            }
        }
    }

    private fun saveReadingToLocal(ph: Double, color: Int) {
        val currentUserEmail = getSharedPreferences("hydrasense_auth_prefs", Context.MODE_PRIVATE)
            .getString("current_user_email", "guest@example.com") ?: "guest@example.com"

        CoroutineScope(Dispatchers.IO).launch {
            val newReading = ReadingEntity(
                userEmail = currentUserEmail,
                phValue = ph,
                colorIndex = color,
                timestamp = System.currentTimeMillis()
            )
            db.readingDao().insertReading(newReading)
        }
    }
}

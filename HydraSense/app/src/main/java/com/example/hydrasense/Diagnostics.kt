package com.example.hydrasense

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Diagnostics : AppCompatActivity() {
    private lateinit var rvHistory: RecyclerView
    private lateinit var trendGraph: TrendGraphView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_diagnostics)
        supportActionBar?.hide()
        
        rvHistory = findViewById(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)
        
        trendGraph = findViewById(R.id.trendGraph)
        setupGraph()

        findViewById<android.view.View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        // Using mock data as requested
        showDemoHistory()

        NavigationHelper.setupBottomNavigation(this, R.id.nav_home)
    }

    private fun setupGraph() {
        val phMock = listOf(6.5, 5.8, 7.2, 6.0, 5.5, 6.8, 7.0)
        val colorMock = listOf(2200.0, 3800.0, 1500.0, 4200.0, 4800.0, 1800.0, 1200.0)
        trendGraph.setData(phMock, colorMock)
    }

    private fun fetchHistory(userId: String) {
        RetrofitClient.instance.getReadings(userId).enqueue(object : Callback<List<NetworkReading>> {
            override fun onResponse(call: Call<List<NetworkReading>>, response: Response<List<NetworkReading>>) {
                if (response.isSuccessful) {
                    val readings = response.body() ?: emptyList()
                    if (readings.isEmpty()) {
                        Toast.makeText(this@Diagnostics, "No history found", Toast.LENGTH_SHORT).show()
                    } else {
                        rvHistory.adapter = HistoryAdapter(readings.reversed()) // Show latest first
                    }
                } else {
                    Toast.makeText(this@Diagnostics, "Failed to load history", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<NetworkReading>>, t: Throwable) {
                Toast.makeText(this@Diagnostics, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                
                // Fallback for demo purposes if backend is off
                showDemoHistory()
            }
        })
    }

    private fun showDemoHistory() {
        val demoReadings = listOf(
            NetworkReading("user", "1234", 5.8, "10:30 AM", "2024-01-29"),
            NetworkReading("user", "1234", 6.2, "09:15 AM", "2024-01-29"),
            NetworkReading("user", "1234", 5.5, "08:00 PM", "2024-01-28"),
            NetworkReading("user", "1234", 7.1, "02:20 PM", "2024-01-28")
        )
        rvHistory.adapter = HistoryAdapter(demoReadings)
    }
}

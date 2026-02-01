package com.example.hydrasense

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HydrationMeterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_meter)
        supportActionBar?.hide()

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        val meterView = findViewById<HydrationMeterView>(R.id.meterView)
        meterView.setPercentage(48) // Demo value

        NavigationHelper.setupBottomNavigation(this, R.id.nav_meter)
    }
}

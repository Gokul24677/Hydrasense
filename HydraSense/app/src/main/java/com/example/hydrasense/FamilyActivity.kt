package com.example.hydrasense

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FamilyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_family)
        supportActionBar?.hide()

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<android.view.View>(R.id.btnAddMember).setOnClickListener {
            Toast.makeText(this, "Add Family Member Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        val rvFamily = findViewById<RecyclerView>(R.id.rvFamily)
        rvFamily.layoutManager = LinearLayoutManager(this)

        // Demo Data
        val members = listOf(
            FamilyMember("Dad", 85, "Hydrated", R.drawable.prof),
            FamilyMember("Mom", 42, "Needs Water", R.drawable.prof),
            FamilyMember("Brother", 70, "Hydrated", R.drawable.prof),
            FamilyMember("Sister", 30, "Dehydrated ⚠️", R.drawable.prof)
        )

        rvFamily.adapter = FamilyAdapter(members)

        NavigationHelper.setupBottomNavigation(this, R.id.nav_home)
    }
}

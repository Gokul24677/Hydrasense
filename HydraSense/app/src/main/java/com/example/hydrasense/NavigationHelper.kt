package com.example.hydrasense

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavigationHelper {

    fun setupBottomNavigation(activity: Activity, selectedItemId: Int) {
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottomNav) ?: return
        bottomNav.selectedItemId = selectedItemId

        // Animate content fade-in to make the transition smooth
        val content = activity.findViewById<android.view.ViewGroup>(android.R.id.content)
        if (content != null && content.childCount > 0) {
            val root = content.getChildAt(0)
            if (root is android.view.ViewGroup) {
                // Find the child that is NOT the bottom nav and animate it
                for (i in 0 until root.childCount) {
                    val child = root.getChildAt(i)
                    if (child.id != R.id.bottomNav) {
                        child.alpha = 0f
                        child.animate().alpha(1f).setDuration(400).start()
                    }
                }
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (activity !is CardActivity) {
                        val intent = Intent(activity, CardActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        activity.intent.extras?.let { intent.putExtras(it) }
                        activity.startActivity(intent)
                        activity.overridePendingTransition(0, 0)
                        activity.finish()
                    }
                    true
                }
                R.id.nav_device -> {
                    if (activity !is Device) {
                        val intent = Intent(activity, Device::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        activity.intent.extras?.let { intent.putExtras(it) }
                        activity.startActivity(intent)
                        activity.overridePendingTransition(0, 0)
                        activity.finish()
                    }
                    true
                }
                R.id.nav_analysis -> {
                    if (activity !is PH_Readings) {
                        val intent = Intent(activity, PH_Readings::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        activity.intent.extras?.let { intent.putExtras(it) }
                        activity.startActivity(intent)
                        activity.overridePendingTransition(0, 0)
                        activity.finish()
                    }
                    true
                }
                R.id.nav_meter -> {
                    if (activity !is HydrationMeterActivity) {
                        val intent = Intent(activity, HydrationMeterActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        activity.intent.extras?.let { intent.putExtras(it) }
                        activity.startActivity(intent)
                        activity.overridePendingTransition(0, 0)
                        activity.finish()
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (activity !is Profile) {
                        val intent = Intent(activity, Profile::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        activity.intent.extras?.let { intent.putExtras(it) }
                        activity.startActivity(intent)
                        activity.overridePendingTransition(0, 0)
                        activity.finish()
                    }
                    true
                }
                else -> false
            }
        }
    }
}

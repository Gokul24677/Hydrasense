package com.example.hydrasense

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class SavedUser(
    val userName: String,
    val userEmail: String,
    val age: Int,
    val weight: String,
    val height: String,
    val gender: String = "Not Set",
    val dob: String = "Not Set",
    val dailyGoal: String = "2.5",
    val avatarRes: Int = R.drawable.prof
)

object SessionManager {
    private const val PREFS_NAME = "hydrasense_auth_prefs"
    private const val KEY_SAVED_USERS = "saved_user_list"
    private val gson = Gson()

    fun addSavedUser(context: Context, user: SavedUser) {
        val users = getSavedUsers(context).toMutableList()
        // Remove existing entry with same email to avoid duplicates
        users.removeAll { it.userEmail == user.userEmail }
        users.add(0, user) // Add to top

        // Keep only last 5 users for the selector
        if (users.size > 5) {
            users.removeAt(5)
        }

        saveList(context, users)
    }

    fun getSavedUsers(context: Context): List<SavedUser> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SAVED_USERS, null) ?: return emptyList()
        val type = object : TypeToken<List<SavedUser>>() {}.type
        return gson.fromJson(json, type)
    }

    fun removeUser(context: Context, email: String) {
        val users = getSavedUsers(context).toMutableList()
        users.removeAll { it.userEmail == email }
        saveList(context, users)
    }

    private fun saveList(context: Context, users: List<SavedUser>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(users)
        prefs.edit().putString(KEY_SAVED_USERS, json).apply()
    }
}

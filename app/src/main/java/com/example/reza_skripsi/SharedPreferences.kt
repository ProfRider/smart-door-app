package com.example.reza_skripsi

import android.content.Context
import android.content.SharedPreferences

class SharedPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

    // Keys for SharedPreferences
    private val KEY_EMAIL = "email"
    private val KEY_USERNAME = "username"

    // Method to save email and username
    fun saveUserData(email: String, username: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_USERNAME, username)
        editor.apply()
    }

    // Method to get the saved email
    fun getEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }

    // Method to get the saved username
    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    // Method to clear saved user data
    fun clearUserData() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_EMAIL)
        editor.remove(KEY_USERNAME)
        editor.apply()
    }
}

package com.example.reza_skripsi

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.reza_skripsi.auth.Login

class Splash : AppCompatActivity() {
    private lateinit var sharedPreferencesManager: SharedPreferences
    private val splashDelay: Long = 3000 // 3 detik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        sharedPreferencesManager = SharedPreferences(this)

        // Menggunakan Handler untuk delay
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, splashDelay)
    }

    private fun checkLoginStatus() {
        val email = sharedPreferencesManager.getEmail()
        if (email != null) {
            // Jika sudah login, pergi ke MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // Jika belum login, pergi ke LoginActivity
            startActivity(Intent(this, Login::class.java))
        }
        finish() // Tutup splash screen agar tidak bisa kembali
    }
}
package com.example.reza_skripsi.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.reza_skripsi.MainActivity
import com.example.reza_skripsi.R
import com.example.reza_skripsi.SharedPreferences
import com.example.reza_skripsi.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferencesManager: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferencesManager = SharedPreferences(this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.apply {
            btnLoginEmail.setOnClickListener{
                val emailLogin = email.text.toString()
                val passwordLogin = password.text.toString()
                if(emailLogin.isEmpty()){
                    email.error = "Email harus diisi"
                    email.requestFocus()
                    return@setOnClickListener
                }
                if(passwordLogin.isEmpty()){
                    password.error = "Password harus diisi"
                    password.requestFocus()
                    return@setOnClickListener
                }
                loginEmail(emailLogin, passwordLogin)
            }
            btnLoginGoogle.setOnClickListener {

            }
            toRegister.setOnClickListener {
                val intent = Intent(this@Login, Register::class.java)
                startActivity(intent)
            }

        }
    }
    private fun loginEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        firestore.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val username = document.getString("username") ?: "Tidak diketahui"
                                    Toast.makeText(this, "Selamat datang, $username!", Toast.LENGTH_SHORT).show()
                                    sharedPreferencesManager.saveUserData(email, username)

                                    // Contoh: Pindah ke halaman utama (MainActivity)
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.putExtra("username", username)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Data user tidak ditemukan", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Gagal mengambil data user", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
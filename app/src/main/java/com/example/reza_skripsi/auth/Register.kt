package com.example.reza_skripsi.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.reza_skripsi.R
import com.example.reza_skripsi.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {
    private lateinit var binding:ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.apply {

            toLogin.setOnClickListener{
                val intent = Intent(this@Register, Login::class.java)
                startActivity(intent)
                finish()
            }
            btnRegisterEmail.setOnClickListener{
                val email = emailReg.text.toString()
                val username = usernameReg.text.toString()
                val password = passwordReg.text.toString()
                val paswordConfirm = passwordConfirmReg.text.toString()

                if(email.isEmpty()){
                    emailReg.error = "Email harus diisi"
                    emailReg.requestFocus()
                    return@setOnClickListener
                }
                if(username.isEmpty()){
                    usernameReg.error = "Username harus diisi"
                    usernameReg.requestFocus()
                    return@setOnClickListener
                }
                if(password.isEmpty()){
                    passwordReg.error = "Password harus diisi"
                    passwordReg.requestFocus()
                    return@setOnClickListener
                }
                if(paswordConfirm.isEmpty()){
                    passwordConfirmReg.error = "Konfirmasi password harus diisi"
                    passwordConfirmReg.requestFocus()
                    return@setOnClickListener
                }
                if(password != paswordConfirm){
                    passwordConfirmReg.error = "Password tidak sama"
                    passwordConfirmReg.requestFocus()
                    return@setOnClickListener
                }
                registerUser(email, password, username)
            }
        }

    }
    private fun registerUser(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userMap = hashMapOf(
                        "username" to username,
                        "email" to email
                    )
                    firestore.collection("users").document(uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Register berhasil", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Login::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal menyimpan ke Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Register gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
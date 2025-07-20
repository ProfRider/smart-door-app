package com.example.reza_skripsi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.reza_skripsi.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferencesManager: SharedPreferences
    private lateinit var firebaseAuth: FirebaseAuth
    var status : String? = ""
    private lateinit var speechRecognizer: SpeechRecognizer


    private companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO
        )
        private const val INPUT_SIZE = 224
        private val REQUEST_CODE_MICROPHONE = 1001
        private const val TAG = "MoneyClassifier"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferencesManager = SharedPreferences(this)
        firebaseAuth = FirebaseAuth.getInstance()

        // Inisialisasi Firebase Database
        database = FirebaseDatabase.getInstance().reference

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        } else {
            startSpeechRecognition()
        }

        // Ambil referensi kondisi pintu
        val kondisiRef = database.child("pintu").child("kondisi").child("status")
        val lastOpen = database.child("pintu").child("kondisi").child("timestamp")
        val kunciRef = database.child("pintu").child("kondisi").child("kunci")
        val voiceRef = database.child("pintu").child("kondisi").child("voice")



        binding.etKunciVoice.isEnabled = false
        binding.etKunciVoice.isFocusable = false
        binding.etKunciVoice.isFocusableInTouchMode = false

        voiceRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val voice = snapshot.getValue(String::class.java)
                binding.etKunciVoice.setText(voice)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Gagal membaca data", Toast.LENGTH_SHORT).show()
            }
        })
        binding.btnEditVoice.setOnClickListener {
            binding.btnKirimEditVoice.visibility = View.VISIBLE
            binding.btnEditVoice.visibility = View.GONE
            // Buat EditText bisa diedit
            binding.etKunciVoice.isEnabled = true
            binding.etKunciVoice.isFocusableInTouchMode = true
            binding.etKunciVoice.requestFocus()
        }
        binding.btnKirimEditVoice.setOnClickListener {
            val newVoice = binding.etKunciVoice.text.toString()
            voiceRef.setValue(newVoice)
            binding.btnKirimEditVoice.visibility = View.GONE
            binding.btnEditVoice.visibility = View.VISIBLE
            binding.etKunciVoice.isEnabled = false
            binding.etKunciVoice.isFocusable = false
            binding.etKunciVoice.isFocusableInTouchMode = false
        }
        kunciRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                binding.customSwitch.isChecked = status == "YA"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Gagal membaca data", Toast.LENGTH_SHORT).show()
            }
        })

        binding.customSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                kunciRef.setValue("YA") // Kirim "iya" saat switch aktif
                Toast.makeText(this, "Pintu Aktif", Toast.LENGTH_SHORT).show()
            } else {
                kunciRef.setValue("TIDAK") // Kirim "tidak" saat switch tidak aktif
                Toast.makeText(this, "Pintu Tidak Aktif", Toast.LENGTH_SHORT).show()
            }
        }

        binding.logout.setOnClickListener {
            // Clear SharedPreferences data
            sharedPreferencesManager.clearUserData()

            // Log out from Firebase
            firebaseAuth.signOut()

            // Show Toast to inform user
            Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show()

            // Finish current activity and go to LoginActivity
            finish()
        }

        voiceRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                status = snapshot.getValue(String::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                status = ""
            }

        })
        lastOpen.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val last = snapshot.getValue(Long::class.java) // Pastikan tipe data Long
                if (last != null) {
                    val sdf = java.text.SimpleDateFormat(
                        "dd MMM yyyy HH:mm:ss",
                        java.util.Locale("id", "ID")
                    )
                    sdf.timeZone = java.util.TimeZone.getTimeZone("GMT+7")
                    val date = java.util.Date(last)
                    val formattedDate = sdf.format(date)

                    binding.lastOpenDoor.text = "Waktu Terakhir Dibuka: $formattedDate"
                } else {
                    binding.lastOpenDoor.text = "Waktu Terakhir Dibuka: Tidak Ditemukan"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "Gagal memuat waktu terakhir dibuka",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Dengarkan perubahan data
        kondisiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val kondisi = snapshot.getValue(String::class.java)

                // Animasi fade out terlebih dahulu
                val fadeOut = AlphaAnimation(1f, 0f)
                fadeOut.duration = 300 // 0.3 detik
                fadeOut.fillAfter = true

                // Set animasi dan handler untuk perubahan setelah animasi selesai
                binding.imageDoor.startAnimation(fadeOut)

                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        when (kondisi) {
                            "TERBUKA" -> {
                                binding.statusDoor.text = "Pintu Terbuka"
                                binding.imageDoor.setImageResource(R.drawable.open)
                            }

                            "TERTUTUP" -> {
                                binding.statusDoor.text = "Pintu Tertutup"
                                binding.imageDoor.setImageResource(R.drawable.close)
                            }

                            else -> {
                                binding.statusDoor.text = "Status Tidak Dikenal"
                                binding.imageDoor.setImageResource(R.drawable.close)
                            }
                        }

                        // Animasi fade in setelah gambar diganti
                        val fadeIn = AlphaAnimation(0f, 1f)
                        fadeIn.duration = 300 // 0.3 detik
                        binding.imageDoor.startAnimation(fadeIn)
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {
                binding.statusDoor.text = "Gagal memuat data: ${error.message}"
            }
        })
    }

    private fun startSpeechRecognition() {
        Toast.makeText(this, "Mulai Bicara", Toast.LENGTH_SHORT).show()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                speechRecognizer.startListening(intent)
            }
            override fun onResults(results: Bundle?) {
                Toast.makeText(this@MainActivity, "Berhasil Bicara", Toast.LENGTH_SHORT).show()
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.get(0)?.lowercase(Locale.getDefault()) ?: ""
                Toast.makeText(this@MainActivity, "Spoken Text: $spokenText", Toast.LENGTH_SHORT).show()
                val voiceSecond = database.child("pintu").child("kondisi").child("voice2")

                if (status.toString().lowercase() in spokenText.lowercase()) {
                    voiceSecond.setValue("TERBUKA")
                    Toast.makeText(this@MainActivity, "Pintu Dibuka", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "Spoken Text: $spokenText")
                }
                speechRecognizer.startListening(intent)
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}
package com.example.myflower

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myflower.AccountSettingsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminAccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_account_settings)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        userId = firebaseAuth.currentUser?.uid ?: return

        // Formdaki alanlar
        val mailField = findViewById<EditText>(R.id.mail)
        val sifreField = findViewById<EditText>(R.id.sifre)
        val profilGuncelleButton = findViewById<Button>(R.id.profil_guncelle)

        // Firebase'den kullanıcı bilgilerini al ve ekrana yansıt
        val userRef = database.child("Admin").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val email = snapshot.child("email").getValue(String::class.java) ?: ""
                    mailField.setText(email) // E-posta bilgisi ekrana yansıtılıyor
                } else {
                    Log.w("AdminSettings", "Admin verisi bulunamadı.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AdminSettings", "Hata: ${error.message}")
            }
        })

        // Profil güncelleme işlemi
        profilGuncelleButton.setOnClickListener {
            val mail = mailField.text.toString()
            val sifre = sifreField.text.toString()

            if (mail.isNotEmpty() && sifre.isNotEmpty()) {
                // Firebase Authentication ile şifreyi güncelle
                firebaseAuth.currentUser?.updatePassword(sifre)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val updatedUserMap = mapOf(
                            "email" to mail
                        )
                        userRef.updateChildren(updatedUserMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Profil güncellendi!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Güncelleme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Şifre güncellenemedi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }

        // Toolbar'ı tanımı ve geri butonunu aktif
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Geri butonunu aktif
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Geri butonuna tıklanma
        toolbar.setNavigationOnClickListener {
            finish()  // Aktiviteyi sonlandırarak bir önceki aktiviteye dönme
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_account_settings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

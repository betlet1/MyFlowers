package com.example.myflowers

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Firebase Auth ve Database referanslar
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Formdaki alanlar
        val adField = findViewById<TextInputEditText>(R.id.ad)
        val soyadField = findViewById<TextInputEditText>(R.id.soyad)
        val kullaniciAdiField = findViewById<TextInputEditText>(R.id.kullanici_adi)
        val emailField = findViewById<TextInputEditText>(R.id.email)
        val sifreField = findViewById<TextInputEditText>(R.id.sifre)
        val kayitButton = findViewById<MaterialButton>(R.id.yeni_kayit_button)

        kayitButton.setOnClickListener {
            val ad = adField.text.toString()
            val soyad = soyadField.text.toString()
            val kullaniciAdi = kullaniciAdiField.text.toString()
            val email = emailField.text.toString()
            val sifre = sifreField.text.toString()

            if (ad.isNotEmpty() && soyad.isNotEmpty() && kullaniciAdi.isNotEmpty() && email.isNotEmpty() && sifre.isNotEmpty()) {
                // Firebase Authentication ile kullanıcı kaydı
                firebaseAuth.createUserWithEmailAndPassword(email, sifre)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = firebaseAuth.currentUser?.uid
                            if (userId != null) {
                                // Kullanıcı bilgilerini Database'e yazması için
                                val userMap = mapOf(
                                    "ad" to ad,
                                    "soyad" to soyad,
                                    "kullaniciAdi" to kullaniciAdi,
                                    "email" to email,
                                    "role" to "user",
                                    "savedFlowers" to mutableMapOf<String, Boolean>() // Başlangıçta kaydedilen çiçekler boş
                                )

                                // Kullanıcı bilgilerini Firebase Realtime Database'e yazma
                                database.child("Kullanıcılar").child(userId).setValue(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Kayıt başarılı!", Toast.LENGTH_SHORT).show()
                                        finish() // Aktiviteyi kapat
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Veritabanı hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Kayıt başarısız: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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

        // Geri butonuna tıklanma
        toolbar.setNavigationOnClickListener {
            finish()  // Aktiviteyi sonlandırarak bir önceki aktiviteye dönme
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

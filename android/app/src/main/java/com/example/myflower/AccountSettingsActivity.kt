package com.example.myflower

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account_settings)

        // Firebase Auth ve Database referanslar
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Kullanıcı ID'yi al
        userId = firebaseAuth.currentUser?.uid ?: return

        // Formdaki alanlar
        val adField = findViewById<EditText>(R.id.ad)
        val soyadField = findViewById<EditText>(R.id.soyad)
        val kullaniciAdiField = findViewById<EditText>(R.id.kullanici_adi)
        val mailField = findViewById<EditText>(R.id.mail)
        val sifreField = findViewById<EditText>(R.id.sifre)
        val profilGuncelleButton = findViewById<Button>(R.id.profil_guncelle)

        // Firebase'den kullanıcı bilgilerini al ve ekrana yansıt
        val userRef = database.child("Kullanıcılar").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userMap = snapshot.value as? Map<String, Any>
                userMap?.let {
                    adField.setText(it["ad"] as? String)
                    soyadField.setText(it["soyad"] as? String)
                    kullaniciAdiField.setText(it["kullaniciAdi"] as? String)
                    mailField.setText(it["email"] as? String)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AccountSettingsActivity, "Veri alınamadı: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Profil güncelleme işlemi
        profilGuncelleButton.setOnClickListener {
            val ad = adField.text.toString()
            val soyad = soyadField.text.toString()
            val kullaniciAdi = kullaniciAdiField.text.toString()
            val mail = mailField.text.toString()
            val sifre = sifreField.text.toString()

            if (ad.isNotEmpty() && soyad.isNotEmpty() && kullaniciAdi.isNotEmpty() && mail.isNotEmpty() && sifre.isNotEmpty()) {
                // Firebase Authentication ile şifreyi güncelle
                firebaseAuth.currentUser?.updatePassword(sifre)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Firebase veritabanına yeni bilgileri kaydet
                        val updatedUserMap = mapOf(
                            "ad" to ad,
                            "soyad" to soyad,
                            "kullaniciAdi" to kullaniciAdi,
                            "email" to mail,
                            "role" to "user"
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

        // Toolbar'ı tanımla ve geri butonunu aktif et
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Geri butonunu aktif et
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Geri butonuna tıklanma
        toolbar.setNavigationOnClickListener {
            finish()  // Aktiviteyi sonlandırarak bir önceki aktiviteye dön
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.account_settings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

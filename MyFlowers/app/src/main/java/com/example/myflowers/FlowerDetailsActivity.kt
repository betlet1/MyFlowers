package com.example.myflowers

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myflowers.model.Flower
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso


class FlowerDetailsActivity : AppCompatActivity() {

    private lateinit var flowerNameTextView: TextView
    private lateinit var flowerDescriptionTextView: TextView
    private lateinit var flowerImageView: ImageView
    private lateinit var addToMyFlowersButton: Button
    private lateinit var setAlarmButton: Button // Alarm Kur Butonu

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_flower_details)

        // Toolbar'ı tanımlayıp geri butonunu aktif etme
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Geri butonunu aktif
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Geri butonuna tıklanma işlemi
        toolbar.setNavigationOnClickListener {
            finish() // Aktiviteyi sonlandırarak bir önceki aktiviteye dönme
        }

        // Sistem çubuğuyla uyumlu pencere ayarları
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.flower_details)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // UI bileşenlerini tanımlama
        flowerNameTextView = findViewById(R.id.flowerNameEditText)
        flowerDescriptionTextView = findViewById(R.id.flowerDescriptionEditText)
        flowerImageView = findViewById(R.id.flowerImageView)
        addToMyFlowersButton = findViewById(R.id.addToMyFlowersButton)

        // Intent ile gelen çiçek verilerini alma
        val flowerId = intent.getStringExtra("flower_id")
        val flowerName = intent.getStringExtra("flower_name")
        val flowerDescription = intent.getStringExtra("flower_description")
        val flowerImageUrl = intent.getStringExtra("flower_image")
        Log.d("Picasso", "Yüklenen URL: $flowerImageUrl")
        // Çiçek bilgilerini UI'da görüntüleme
        flowerName?.let { flowerNameTextView.text = it }
        flowerDescription?.let { flowerDescriptionTextView.text = it }

        // Cloudinary URL ile çiçek resmini gösterme
        flowerImageUrl?.let { url ->
            Log.d("Picasso", "Yüklenen URL: $url")

            Picasso.get().load(url).into(flowerImageView, object : com.squareup.picasso.Callback {

                override fun onSuccess() {
                    Log.d("Picasso", "Resim başarıyla yüklendi!")
                }

                override fun onError(e: Exception?) {
                    Log.e("Picasso", "Resim yüklenemedi: ${e?.message}")
                    flowerImageView.setImageResource(R.drawable.bos_resim)
                }
            })
        }

        // Çiçek detaylarını Firebase'den almak ve UI'yı güncellemek
        if (flowerId != null) {
            val database = FirebaseDatabase.getInstance().reference
            database.child("Flowers").child(flowerId).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val flower = dataSnapshot.getValue(Flower::class.java)
                    flower?.let {
                        flowerNameTextView.text = it.name
                        flowerDescriptionTextView.text = it.description
                        Picasso.get().load(it.imageUrl).into(flowerImageView)
                    }
                }
            }
            // Butonun başlangıç durumunu güncelle
            updateButtonState(flowerId)
        }
    }

    // Çiçeğin kullanıcının kaydedilen çiçekler listesinde olup olmadığını kontrol etme ve UI'yı güncelleme
    private fun updateButtonState(flowerId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference

        database.child("Kullanıcılar").child(userId).child("savedFlowers").child(flowerId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Çiçek listede mevcutsa "Çıkar" olarak güncelle
                    addToMyFlowersButton.text = "Çiçeklerimden Çıkar"
                    addToMyFlowersButton.setBackgroundColor(ContextCompat.getColor(this, R.color.kirmizi))
                    addToMyFlowersButton.setOnClickListener { removeFromMyFlowers(flowerId) }


                } else {
                    // Çiçek listede değilse "Ekle" olarak güncelle
                    addToMyFlowersButton.text = "Çiçeklerime Ekle"
                    addToMyFlowersButton.setBackgroundColor(ContextCompat.getColor(this, R.color.yesilmavi))
                    addToMyFlowersButton.setOnClickListener { addToMyFlowers(flowerId) }

                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Durum kontrol edilemedi", Toast.LENGTH_SHORT).show()
            }
    }

    // Çiçeği kullanıcının kaydedilen çiçekler listesinden çıkarma işlemi
    private fun removeFromMyFlowers(flowerId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference

        database.child("Kullanıcılar").child(userId).child("savedFlowers").child(flowerId)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Çiçeklerim listesinden çıkarıldı", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Çiçeklerim listesinden çıkarılamadı", Toast.LENGTH_SHORT).show()
            }
    }

    // Çiçeği kullanıcının kaydedilen çiçekler listesine ekleme işlemi
    private fun addToMyFlowers(flowerId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference

        database.child("Kullanıcılar").child(userId).child("savedFlowers").child(flowerId)
            .setValue(flowerId)
            .addOnSuccessListener {
                Toast.makeText(this, "Çiçeklerim listesine eklendi", Toast.LENGTH_SHORT).show()
                finish() // Butonu güncelle
            }
            .addOnFailureListener {
                Toast.makeText(this, "Çiçeklerim listesine eklenemedi", Toast.LENGTH_SHORT).show()
            }
    }


}

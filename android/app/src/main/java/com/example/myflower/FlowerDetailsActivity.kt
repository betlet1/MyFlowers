package com.example.myflower

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myflower.model.Flower
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class FlowerDetailsActivity : AppCompatActivity() {

    private lateinit var flowerNameTextView: TextView
    private lateinit var flowerDescriptionTextView: TextView
    private lateinit var flowerImageView: ImageView
    private lateinit var addToMyFlowersButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_flower_details)

        // Toolbar'ı tanımlayıp geri butonunu aktif etme
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Geri butonunu aktif
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Geri butonuna tıklanma işlemi
        toolbar.setNavigationOnClickListener {
            finish()  // Aktiviteyi sonlandırarak bir önceki aktiviteye dönme
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
        val flowerId = intent.getStringExtra("flower_id") // Çiçek ID'sini alıyoruz
        val flowerName = intent.getStringExtra("flower_name")
        val flowerDescription = intent.getStringExtra("flower_description")
        val flowerImageUrl = intent.getStringExtra("flower_image_url")

        // Çiçek bilgilerini UI'da görüntüleme
        flowerName?.let { flowerNameTextView.text = it }
        flowerDescription?.let { flowerDescriptionTextView.text = it }

        // Cloudinary URL ile çiçek resmini gösterme
        flowerImageUrl?.let { url ->
            Picasso.get().load(url).into(flowerImageView, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    // Resim başarılı şekilde yüklendi
                    // Burada ekstra bir işlem yapmanıza gerek yok, sadece görsellik için onSuccess kullanabilirsiniz
                }

                override fun onError(e: Exception?) {
                    // Hata durumunda yapılacak işlemler
                    flowerImageView.setImageResource(R.drawable.bos_resim) // Hata durumunda bir yedek resim
                    Toast.makeText(this@FlowerDetailsActivity, "Resim yüklenemedi", Toast.LENGTH_SHORT).show()
                }
            })
        }


        // Firebase Realtime Database üzerinden çiçek detaylarını almak
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
        }

        // "Çiçeklerime Ekle" butonuna tıklanma işlemi (Firebase'e kaydetme)
        addToMyFlowersButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid  // Giriş yapan kullanıcının ID'si
            val flowerId = intent.getStringExtra("flower_id")  // Çiçek ID'sini alın

            if (userId != null && flowerId != null) {
                val database = FirebaseDatabase.getInstance().reference

                // Çiçek ID'sini kullanıcının kaydedilen çiçekler listesine kaydediyoruz
                database.child("Kullanıcılar").child(userId).child("savedFlowers").child(flowerId).setValue(flowerId)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Çiçek Kaydedildi", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MyFlowersActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Çiçek kaydedilemedi", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Kullanıcı giriş yapmamış veya çiçek ID'si eksik
                if (userId == null) {
                    Toast.makeText(this, "Lütfen giriş yapın", Toast.LENGTH_SHORT).show()
                    // Giriş yapmayan kullanıcıyı giriş sayfasına yönlendirebilirsiniz
                    val loginIntent = Intent(this, LoginActivity::class.java)  // LoginActivity'yi uygun şekilde düzenleyin
                    startActivity(loginIntent)
                } else {
                    Toast.makeText(this, "Çiçek bilgisi eksik", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}

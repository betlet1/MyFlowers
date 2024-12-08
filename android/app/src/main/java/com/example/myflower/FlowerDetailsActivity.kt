package com.example.myflower

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

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
        val flowerName = intent.getStringExtra("flower_name")
        val flowerDescription = intent.getStringExtra("flower_description")
        val flowerImageBase64 = intent.getStringExtra("flower_image")

        // Çiçek bilgilerini UI'da görüntüleme
        flowerName?.let { flowerNameTextView.text = it }
        flowerDescription?.let { flowerDescriptionTextView.text = it }

        // Base64 resmini çözümleyip ImageView'da gösterme
        if (!flowerImageBase64.isNullOrEmpty()) {
            val decodedString: ByteArray = Base64.decode(flowerImageBase64, Base64.DEFAULT)
            val decodedBitmap: Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            flowerImageView.setImageBitmap(decodedBitmap)
        }

        // "Kayıt Ol" butonuna tıklanma işlemi
        addToMyFlowersButton.setOnClickListener {
            val intent = Intent(this, MyFlowersActivity::class.java)
            startActivity(intent)
        }
    }
}

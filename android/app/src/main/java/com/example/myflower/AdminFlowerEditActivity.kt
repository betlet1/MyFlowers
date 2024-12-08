package com.example.myflower

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.myflower.AdminFlowersActivity
import com.squareup.picasso.Picasso

class AdminFlowerEditActivity : AppCompatActivity() {

    private lateinit var flowerName: String
    private lateinit var flowerImage: String
    private lateinit var flowerDescription: String

    private lateinit var editTextName: EditText
    private lateinit var imageViewFlower: ImageView
    private lateinit var editTextDescription: EditText
    private lateinit var saveButton: Button

    private lateinit var progressDialog: ProgressDialog

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_flower_edit)

        // Toolbar tanımı ve geri butonunu aktif etme
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Geri butonunu aktif etme
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Geri butonuna tıklanma
        toolbar.setNavigationOnClickListener {
            finish()  // Aktiviteyi sonlandırarak bir önceki aktiviteye dönme
        }

        // Sistem çubuğuyla uyumlu pencere ayarları
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_flower_edit)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Layout bileşenlerini tanımlama
        editTextName = findViewById(R.id.flowerNameEditText)
        imageViewFlower = findViewById(R.id.flowerImageView)
        editTextDescription = findViewById(R.id.flowerDescriptionEditText)
        saveButton = findViewById(R.id.guncelle_buton)

        // Intent ile gelen çiçek verilerini alma
        flowerName = intent.getStringExtra("flower_name") ?: ""
        flowerImage = intent.getStringExtra("flower_image_url") ?: ""
        flowerDescription = intent.getStringExtra("flower_description") ?: ""

        // Verileri UI bileşenlerine aktarma
        editTextName.setText(flowerName)
        editTextDescription.setText(flowerDescription)

        // Cloudinary resmini Picasso ile yükleyerek gösterme
        Picasso.get().load(flowerImage).into(imageViewFlower)

        // Kaydetme işlemi
        saveButton.setOnClickListener {
            saveFlowerDetails()
        }

        // ProgressDialog tanımlama
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Yükleniyor...")
    }

    private fun saveFlowerDetails() {
        val newFlowerName = editTextName.text.toString()
        val newFlowerDescription = editTextDescription.text.toString()

        if (newFlowerName.isEmpty() || newFlowerDescription.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            return
        }

        // Resmi yükleme işlemi (varsa yeni bir resim seçilmişse Cloudinary'ye yükleme)
        val imageUrl = flowerImage // Burada imageUrl, Cloudinary'den gelen URL olmalı

        // Çiçek bilgilerini Cloudinary'ye kaydetme işlemi
        updateFlowerDetails(newFlowerName, newFlowerDescription, imageUrl)
    }

    private fun updateFlowerDetails(name: String, description: String, imageUrl: String) {
        progressDialog.show()

        // Cloudinary'ye yükleme işlemi
        val request = MediaManager.get().upload(imageUrl)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("Cloudinary", "Yükleme başladı")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    Log.d("Cloudinary", "Yükleme ilerleme: $bytes/$totalBytes")
                }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    progressDialog.dismiss()
                    Toast.makeText(this@AdminFlowerEditActivity, "Çiçek bilgileri güncellendi.", Toast.LENGTH_SHORT).show()
                    // Yedekleme ve yönlendirme işlemi
                    val intent = Intent(this@AdminFlowerEditActivity, AdminFlowersActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                // onError metodunu doğru şekilde implement et
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    progressDialog.dismiss()
                    Toast.makeText(this@AdminFlowerEditActivity, "Yükleme hatası: ${error?.description}", Toast.LENGTH_SHORT).show()
                }

                // onReschedule metodunu doğru şekilde implement et
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    Log.d("Cloudinary", "Yükleme yeniden planlandı, hata: ${error?.description}")
                    progressDialog.dismiss()
                    Toast.makeText(this@AdminFlowerEditActivity, "Yükleme yeniden planlandı, hata: ${error?.description}", Toast.LENGTH_SHORT).show()
                }
            }).dispatch()  // Yükleme işlemini başlat
    }
}

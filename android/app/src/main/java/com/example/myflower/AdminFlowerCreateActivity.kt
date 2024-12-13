package com.example.myflower

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.myflower.model.Flower
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.util.*

class AdminFlowerCreateActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var uploadImageButton: Button
    private lateinit var flowerImageView: ImageView
    private lateinit var addButton: Button
    private var imageUri: Uri? = null

    private val REQUEST_PERMISSION_CODE = 200  // İzin isteği için

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        flowerImageView.setImageURI(uri)  // Görüntüyü önizlemede göster
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_flower_create)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Formdaki alanlar
        val adField = findViewById<EditText>(R.id.flowerNameEditText)
        val aciklamaField = findViewById<EditText>(R.id.flowerDescriptionEditText)
        uploadImageButton = findViewById(R.id.uploadImageButton)
        addButton = findViewById(R.id.addButton)
        flowerImageView = findViewById(R.id.flowerImageView)

        // Toolbar'ı tanımı ve geri butonunu aktif
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        uploadImageButton.setOnClickListener {
            checkPermission()  // İzin kontrolü
        }

        addButton.setOnClickListener {
            val ad = adField.text.toString()
            val aciklama = aciklamaField.text.toString()

            if (ad.isNotEmpty() && aciklama.isNotEmpty()) {
                if (imageUri != null) {
                    val flower = Flower(
                        id = UUID.randomUUID().toString(),
                        imageUrl = "", // Başlangıçta boş URL, Cloudinary yükleme sonrası güncellenir
                        name = ad,
                        description = aciklama
                    )
                    uploadImageToCloudinary(imageUri!!, flower)
                } else {
                    Toast.makeText(this, "Lütfen bir resim seçin.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Tüm alanları doldurduğunuzdan emin olun.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Depolama izni kontrol fonksiyonu
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_CODE
            )
        } else {
            pickImageLauncher.launch("image/*")
        }
    }

    // Cloudinary'ye resmi yükleme fonksiyonu
    private fun uploadImageToCloudinary(uri: Uri, flower: Flower) {
        val cloudinary = Cloudinary("cloudinary://674952127384548:Ft-dPH-ZbxNuC478A8i9VWk9vkM@dr2frdnxk")
        val file = File(getRealPathFromURI(uri))

        // Resmi Cloudinary'ye yükleme işlemi
        Thread {
            try {
                val uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
                val imageUrl = uploadResult["url"].toString()  // Cloudinary URL'si

                // Cloudinary URL'sinin HTTPS formatında olup olmadığını kontrol etme (isteğe bağlı)
                val httpsImageUrl = if (imageUrl.startsWith("http://")) {
                    imageUrl.replace("http://", "https://")  // HTTP'yi HTTPS'ye çevirme
                } else {
                    imageUrl
                }

                flower.imageUrl = httpsImageUrl  // HTTPS URL'si ile güncelle

                runOnUiThread {
                    saveFlowerToDatabase(flower)  // Resim yüklendikten sonra Firebase'e kaydet
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Resim yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }


    // URI'den gerçek dosya yolunu al
    private fun getRealPathFromURI(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
        val realPath = columnIndex?.let { cursor.getString(it) }
        cursor?.close()
        return realPath ?: ""
    }

    // Firebase Realtime Database'e çiçek bilgilerini kaydetme
    private fun saveFlowerToDatabase(flower: Flower) {
        val flowerRef = database.child("Flowers").push()  //Yeni bir node oluşturuyor

        flowerRef.setValue(flower)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Çiçek başarıyla eklendi.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Çiçek eklenemedi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
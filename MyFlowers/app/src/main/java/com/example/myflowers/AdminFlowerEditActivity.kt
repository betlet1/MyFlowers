package com.example.myflowers

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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*

class AdminFlowerEditActivity : AppCompatActivity() {

    private lateinit var flowerNameEditText: EditText
    private lateinit var flowerDescriptionEditText: EditText
    private lateinit var flowerImageView: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var guncelleButton: Button
    private var imageUri: Uri? = null
    private var flowerId: String = "" // Güncellenecek çiçeğin ID'si
    private var imageUrl: String = ""  // Resim URL'si

    private val REQUEST_PERMISSION_CODE = 200  // İzin isteği için

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        flowerImageView.setImageURI(uri)  // Görüntüyü önizlemede göster
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_flower_edit)

        // Görünüm öğelerini bağla
        flowerNameEditText = findViewById(R.id.flowerNameEditText)
        flowerDescriptionEditText = findViewById(R.id.flowerDescriptionEditText)
        flowerImageView = findViewById(R.id.flowerImageView)
        uploadImageButton = findViewById(R.id.uploadImageButton)
        guncelleButton = findViewById(R.id.guncelle_buton)

        // Toolbar'ı tanımla
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Intent verilerini al
        flowerId = intent.getStringExtra("flowerId") ?: ""
        val flowerName = intent.getStringExtra("flowerName")
        val flowerDescription = intent.getStringExtra("flowerDescription")
        val flowerImage = intent.getStringExtra("flowerImage")

        if (flowerId.isEmpty()) {
            Toast.makeText(this, "Çiçek bilgisi eksik. Lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Çiçek bilgilerini set et
        flowerNameEditText.setText(flowerName)
        flowerDescriptionEditText.setText(flowerDescription)
        Picasso.get().load(flowerImage).into(flowerImageView)

        // Yeni resim URL'sini tutmak için değişkeni güncelleme
        imageUrl = flowerImage ?: ""

        // Resim yükleme butonu işlemi
        uploadImageButton.setOnClickListener {
            checkPermission()  // İzin kontrolü
        }

        // Güncelleme işlemi
        guncelleButton.setOnClickListener {
            if (imageUri != null) {
                uploadImageToCloudinary(imageUri!!, flowerId)
            } else {
                updateFlowerInfo(flowerId)  // Resim yoksa güncelleme yap
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
    private fun uploadImageToCloudinary(uri: Uri, flowerId: String) {
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

                this.imageUrl = httpsImageUrl  // HTTPS URL'si ile güncelle

                runOnUiThread {
                    updateFlowerInfo(flowerId)  // Resim yüklendikten sonra Firebase'e kaydet
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

    // Çiçek bilgilerini güncelleme
    private fun updateFlowerInfo(flowerId: String) {
        val name = flowerNameEditText.text.toString().trim()
        val description = flowerDescriptionEditText.text.toString().trim()

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase veritabanında flowerId'ye sahip veriyi buluyoruz
        val database = FirebaseDatabase.getInstance().reference.child("Flowers")

        database.orderByChild("id").equalTo(flowerId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // İlgili flowerId'ye sahip veriyi bulduk
                    for (childSnapshot in snapshot.children) {
                        val firebaseKey = childSnapshot.key // Firebase'in otomatik oluşturduğu ID (key)

                        // Firebase key üzerinden veriyi güncelliyoruz
                        val updates = mapOf(
                            "name" to name,
                            "description" to description,
                            "imageUrl" to imageUrl // Yeni resim URL'sini ekleyin
                        )

                        firebaseKey?.let {
                            database.child(it).updateChildren(updates).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this@AdminFlowerEditActivity, "Çiçek başarıyla güncellendi", Toast.LENGTH_SHORT).show()
                                    finish()  // Başarıyla güncellenirse, aktiviteyi kapat
                                } else {
                                    Toast.makeText(this@AdminFlowerEditActivity, "Güncelleme hatası: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@AdminFlowerEditActivity, "Çiçek bulunamadı.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumu
                Toast.makeText(this@AdminFlowerEditActivity, "Veritabanı hatası: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}

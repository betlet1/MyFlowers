package com.example.myflower

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myflower.model.Flower
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class AdminFlowerEditActivity : AppCompatActivity() {

    private lateinit var flowerNameEditText: EditText
    private lateinit var flowerDescriptionEditText: EditText
    private lateinit var flowerImageView: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var guncelleButton: Button
    private var flowerId: String = ""  // Güncellenecek çiçeğin kendi atadığınız ID'si
    private var imageUrl: String = ""  // Çiçek için resim URL'si

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_flower_edit)

        // Görünüm öğelerini bağla
        flowerNameEditText = findViewById(R.id.flowerNameEditText)
        flowerDescriptionEditText = findViewById(R.id.flowerDescriptionEditText)
        flowerImageView = findViewById(R.id.flowerImageView)
        uploadImageButton = findViewById(R.id.uploadImageButton)
        guncelleButton = findViewById(R.id.guncelle_buton)

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

        // Yeni resim URL'sini tutmak için değişkeni güncelle
        imageUrl = flowerImage ?: ""

        // Resim yükleme butonu işlemi
        uploadImageButton.setOnClickListener {
            // Cloudinary üzerinden resim URL'sini al (örnek URL)
            val selectedImageUrl = "https://res.cloudinary.com/your-cloud-name/image/upload/v1677777777/flower_image.jpg"
            imageUrl = selectedImageUrl
            Picasso.get().load(imageUrl).into(flowerImageView)
        }

        // Güncelleme işlemi
        guncelleButton.setOnClickListener {
            fetchFirebaseIdAndUpdate()
        }
    }

    // Firebase'den çiçeğin otomatik ID'sini bul ve güncelle
    private fun fetchFirebaseIdAndUpdate() {
        val database = FirebaseDatabase.getInstance().reference.child("Flowers")
        database.orderByChild("id").equalTo(flowerId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (childSnapshot in snapshot.children) {
                        val firebaseId = childSnapshot.key // Firebase'in atadığı ID
                        if (firebaseId != null) {
                            updateFlowerInfo(firebaseId)
                        }
                    }
                } else {
                    Toast.makeText(this@AdminFlowerEditActivity, "Çiçek bulunamadı.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminFlowerEditActivity, "Hata: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Çiçek bilgilerini güncelleme
    private fun updateFlowerInfo(firebaseId: String) {
        val name = flowerNameEditText.text.toString().trim()
        val description = flowerDescriptionEditText.text.toString().trim()

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf(
            "name" to name,
            "description" to description,
            "imageUrl" to imageUrl
        )

        val database = FirebaseDatabase.getInstance().reference.child("Flowers").child(firebaseId)
        database.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Çiçek başarıyla güncellendi", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Güncelleme hatası: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

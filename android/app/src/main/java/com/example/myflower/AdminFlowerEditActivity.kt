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
    private var flowerId: String = ""  // Güncellenecek çiçeğin ID'si
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

        // Çiçek ID'sini al
        flowerId = intent.getStringExtra("flowerId") ?: ""

        // Çiçek bilgilerini Firebase'den çek
        fetchFlowerDetails()

        // Resim yükleme butonuna tıklandığında
        uploadImageButton.setOnClickListener {
            // Cloudinary üzerinden resim URL'sini al (örnek olarak)
            val selectedImageUrl = "https://res.cloudinary.com/your-cloud-name/image/upload/v1677777777/flower_image.jpg" // Cloudinary URL'si
            imageUrl = selectedImageUrl
            Picasso.get().load(imageUrl).into(flowerImageView)
        }

        // Güncelle butonuna tıklanırsa
        guncelleButton.setOnClickListener {
            updateFlowerInfo()
        }
    }

    // Firebase'den çiçek bilgilerini çekme
    private fun fetchFlowerDetails() {
        val database = FirebaseDatabase.getInstance().reference.child("Flowers").child(flowerId)
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val flower = snapshot.getValue(Flower::class.java)
                if (flower != null) {
                    flowerNameEditText.setText(flower.name)
                    flowerDescriptionEditText.setText(flower.description)
                    Picasso.get().load(flower.imageUrl).into(flowerImageView)
                    imageUrl = flower.imageUrl // Eski resim URL'sini kaydediyoruz
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminFlowerEditActivity, "Veri çekme hatası: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Çiçek bilgilerini güncelleme
    private fun updateFlowerInfo() {
        val name = flowerNameEditText.text.toString().trim()
        val description = flowerDescriptionEditText.text.toString().trim()

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            return
        }

        // Yeni Flower nesnesi oluşturuyoruz
        val updatedFlower = Flower(
            id = flowerId, // Çiçeğin mevcut ID'si
            imageUrl = imageUrl, // Yeni resim URL'si (eski ya da yeni)
            name = name,
            description = description
        )

        // Firebase'e kaydetme
        val database = FirebaseDatabase.getInstance().reference.child("Flowers").child(flowerId)
        database.setValue(updatedFlower).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Çiçek başarıyla güncellendi", Toast.LENGTH_SHORT).show()
                finish()  // Activity'yi kapat
            } else {
                Toast.makeText(this, "Güncelleme hatası: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}



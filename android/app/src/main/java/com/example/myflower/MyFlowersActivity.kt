package com.example.myflower

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myflower.model.Flower
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MyFlowersActivity : AppCompatActivity() {

    private lateinit var flowerRecyclerView: RecyclerView
    private lateinit var flowerAdapter: FlowerAdapter
    private val flowersList = ArrayList<Flower>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_flowers)

        flowerRecyclerView = findViewById(R.id.flowerRecyclerView)
        flowerRecyclerView.layoutManager = LinearLayoutManager(this)

        // Adapter'ı doğru şekilde oluşturuyoruz
        flowerAdapter = FlowerAdapter(flowersList, this)
        flowerRecyclerView.adapter = flowerAdapter

        // Kullanıcının kaydettiği çiçekleri Firebase Realtime Database'ten alıyoruz
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance().reference
            database.child("Kullanıcılar").child(userId).child("savedFlowers").get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (flowerSnapshot in snapshot.children) {
                        val flowerId = flowerSnapshot.key
                        flowerId?.let {
                            getFlowerDetails(it)
                        }
                    }
                } else {
                    Toast.makeText(this, "Kaydedilmiş çiçek bulunmuyor", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Veri alınırken hata oluştu", Toast.LENGTH_SHORT).show()
            }
        }
        // Toolbar'ı tanımla ve geri butonunu aktif et
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Geri butonunu aktif et
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Geri butonuna tıklanma
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, FlowersActivity::class.java)  // FlowersActivity'ye git
            startActivity(intent)  // Yeni aktiviteyi başlat
            finish()  // Mevcut aktiviteyi sonlandır
        }

        // Sistem çubuğuyla uyumlu pencere ayarları
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.my_flowers)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getFlowerDetails(flowerId: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("Flowers").child(flowerId).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val flower = dataSnapshot.getValue(Flower::class.java)
                flower?.let {
                    flowersList.add(it)  // Çiçeği listeye ekliyoruz
                    flowerAdapter.notifyDataSetChanged()  // Adapter'ı güncelliyoruz
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Çiçek verileri alınamadı", Toast.LENGTH_SHORT).show()
        }
    }
}


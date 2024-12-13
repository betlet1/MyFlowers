package com.example.myflower

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myflower.model.Flower
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyFlowersActivity : AppCompatActivity() {

    private lateinit var flowerRecyclerView: RecyclerView
    private lateinit var flowerAdapter: FlowerAdapter
    private lateinit var flowersList: ArrayList<Flower>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_flowers)

        // Toolbar'ı tanımla ve geri butonunu aktif et
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Geri butonunu aktif et
        supportActionBar?.setDisplayShowHomeEnabled(true)
        // TextView üzerindeki başlığı kullanması için Toolbar başlığını kaldırma
        supportActionBar?.setDisplayShowTitleEnabled(false)

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

        init()
    }

    private fun init() {
        // RecyclerView ve adaptörü tanımlama
        flowerRecyclerView = findViewById(R.id.flowerRecyclerView)
        flowerRecyclerView.setHasFixedSize(true)
        flowerRecyclerView.layoutManager = GridLayoutManager(this, 2)

        flowersList = ArrayList()
        flowerAdapter = FlowerAdapter(flowersList, this)
        flowerRecyclerView.adapter = flowerAdapter

        fetchMyFlowersFromFirebase()
    }

    // Firebase'den çiçeklerim verilerini çekme
    private fun fetchMyFlowersFromFirebase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Lütfen giriş yapınız.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val database = FirebaseDatabase.getInstance().reference
        val savedFlowersRef = database.child("Kullanıcılar").child(currentUser.uid).child("savedFlowers")

        // "Kullanıcılar" altındaki "savedFlowers" verisini çekiyoruz
        savedFlowersRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                Toast.makeText(this@MyFlowersActivity, "Kaydedilmiş çiçek bulunamadı.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            flowersList.clear()

            // Çiçek ID'lerini alıp "Flowers" tablosundan veri çekiyoruz
            for (flowerSnapshot in snapshot.children) {
                val flowerId = flowerSnapshot.key
                flowerId?.let { getFlowerDetails(it) }
            }
        }.addOnFailureListener {
            Toast.makeText(this@MyFlowersActivity, "Veri çekme hatası: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Flower ID'sine göre "Flowers" tablosundan çiçek detaylarını çekme
    private fun getFlowerDetails(flowerId: String) {
        val database = FirebaseDatabase.getInstance().reference
        val flowerRef = database.child("Flowers").child(flowerId)

        // "Flowers" tablosundan çiçek detaylarını alıyoruz
        flowerRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val flower = dataSnapshot.getValue(Flower::class.java)
                flower?.let {
                    flowersList.add(it)  // Çiçeği listeye ekliyoruz
                    flowerAdapter.notifyDataSetChanged()  // Adapter'ı güncelliyoruz
                }
            } else {
                Toast.makeText(this@MyFlowersActivity, "Çiçek verileri bulunamadı", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this@MyFlowersActivity, "Çiçek verileri alınamadı", Toast.LENGTH_SHORT).show()
        }
    }
}


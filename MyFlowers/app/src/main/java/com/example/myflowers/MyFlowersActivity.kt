package com.example.myflowers

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myflowers.model.Flower
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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

        val userId = currentUser.uid
        val database = FirebaseDatabase.getInstance().reference
        val savedFlowersRef = database.child("Kullanıcılar").child(userId).child("savedFlowers")

        savedFlowersRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                Toast.makeText(this@MyFlowersActivity, "Kaydedilmiş çiçek bulunamadı.", Toast.LENGTH_SHORT).show()
                Log.e("FetchMyFlowers", "Kullanıcı: $userId - Kaydedilmiş çiçek yok.")
                return@addOnSuccessListener
            }
            flowersList.clear()
            for (flowerSnapshot in snapshot.children) {
                val flowerId = flowerSnapshot.key
                if (flowerId == null) {
                    Log.e("FetchMyFlowers", "Kullanıcı: $userId - Geçersiz çiçek ID.")
                    continue
                }
                fetchFlowerDetails(flowerId)
            }
        }.addOnFailureListener {
            Toast.makeText(this@MyFlowersActivity, "Veri çekme hatası: ${it.message}", Toast.LENGTH_SHORT).show()
            Log.e("FetchMyFlowers", "Kullanıcı: $userId - Veri çekme hatası: ${it.message}")
        }
    }

    // Flower ID'sine göre "Flowers" tablosundan çiçek detaylarını çekme
    private fun fetchFlowerDetails(flowerId: String) {
        val database = FirebaseDatabase.getInstance().reference.child("Flowers")

        // `id` alanına göre sorgulama yap
        database.orderByChild("id").equalTo(flowerId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (childSnapshot in snapshot.children) {
                    val flower = childSnapshot.getValue(Flower::class.java)
                    if (flower != null) {
                        flowersList.add(flower) // Eşleşen çiçeği listeye ekle
                        flowerAdapter.notifyDataSetChanged() // Listeyi güncelle
                    }
                }
            } else {
                // Eğer eşleşen veri yoksa
                Toast.makeText(this@MyFlowersActivity, "Çiçek bulunamadı: $flowerId", Toast.LENGTH_SHORT).show()
                Log.e("FetchFlowerDetails", "Çiçek ID bulunamadı: $flowerId")
            }
        }.addOnFailureListener { error ->
            Toast.makeText(this@MyFlowersActivity, "Çiçek verisi alınamadı: ${error.message}", Toast.LENGTH_SHORT).show()
            Log.e("FetchFlowerDetails", "Çiçek verisi alınamadı: ${error.message}")
        }
    }

    // Sayfa her görünür olduğunda çağrılır, verileri yeniden çekip günceller
    override fun onResume() {
        super.onResume()
        fetchMyFlowersFromFirebase() // Listeyi yenile
    }
}


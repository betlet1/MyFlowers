package com.example.myflower

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myflower.model.Flower
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminFlowersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adminFlowerList: ArrayList<Flower>
    private lateinit var adminFlowerAdapter: AdminFlowerAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_flowers)

        // Toolbar'ı tanımlayıp ActionBar olarak ayarlama
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // TextView üzerindeki başlığı kullanması için Toolbar başlığını kaldırma
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Edge to edge padding düzenlemesi
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_flowers)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // RecyclerView'i ve adapter'ı başlatma
        initRecyclerView()
    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.adminFlowerRecyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        adminFlowerList = ArrayList()
        adminFlowerAdapter = AdminFlowerAdapter(adminFlowerList, this)
        recyclerView.adapter = adminFlowerAdapter

        // Firebase'den verileri çekme
        fetchFlowersFromFirebase()
    }

    // Firebase'den veri çekme
    private fun fetchFlowersFromFirebase() {
        val database = FirebaseDatabase.getInstance().reference.child("Flowers")

        // Firebase veri dinleyicisi
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                adminFlowerList.clear()  // Yeni veriler alındığında listeyi temizle
                for (flowerSnapshot in snapshot.children) {
                    val flower = flowerSnapshot.getValue(Flower::class.java)
                    if (flower != null) {
                        adminFlowerList.add(flower)
                        Log.d("Firebase", "Çiçek Yükleniyor: ${flower.name}")
                    }
                }
                adminFlowerAdapter.notifyDataSetChanged()  // Adapter'ı güncelle
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunda işlem
                Toast.makeText(this@AdminFlowersActivity, "Veri çekme hatası: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Menü oluşturma
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_alt_menu, menu) // Menü dosyasını inflate et
        return true
    }

    // Menü elemanlarına tıklama işlemleri
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.my_account -> {
                startActivity(Intent(this, AdminAccountSettingsActivity::class.java))
                return true
            }

            R.id.new_flower -> {
                startActivity(Intent(this, AdminFlowerCreateActivity::class.java)) // Çiçek ekleme sayfasına yönlendirme
                return true
            }

            R.id.cikis -> {
                startActivity(Intent(this, MainActivity::class.java)) // Ana sayfaya yönlendirme
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

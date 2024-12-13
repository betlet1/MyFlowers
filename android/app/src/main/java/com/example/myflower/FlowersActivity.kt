package com.example.myflower

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
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


class FlowersActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var flowerList: ArrayList<Flower>
    private lateinit var flowerAdapter: FlowerAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flowers)

        // Toolbar'ı tanımlayıp ActionBar olarak ayarlama
        val toolbar: Toolbar = findViewById(R.id.toolbar) // activity_flowers.xml dosyasındaki toolbar
        setSupportActionBar(toolbar)

        // TextView üzerindeki başlığı kullanması için Toolbar başlığını kaldırma
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Arka planın sistem çubuğu ile uyumlu olması için pencere ayarları
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.flowers)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
    }

    private fun init() {
        // RecyclerView ve adaptörü tanımlama
        recyclerView = findViewById(R.id.flowerRecyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        flowerList = ArrayList()
        flowerAdapter = FlowerAdapter(flowerList, this)
        recyclerView.adapter = flowerAdapter

        fetchFlowersFromFirebase()
    }

    // Firebase'den çiçek verilerini çekme
    private fun fetchFlowersFromFirebase() {
        val database = FirebaseDatabase.getInstance().reference.child("Flowers")

        // Veritabanından veri çekme
        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                flowerList.clear()  // Yeni veriler alındığında listeyi temizle
                for (flowerSnapshot in snapshot.children) {
                    try {
                        val flower = flowerSnapshot.getValue(Flower::class.java)
                        if (flower != null) {
                            flowerList.add(flower)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                flowerAdapter.notifyDataSetChanged()  // Adapter'ı güncelle
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunda işlemler
                Toast.makeText(this@FlowersActivity, "Veri çekme hatası: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Menü oluşturma
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.alt_menu, menu) // Menü dosyasını inflate et
        return true
    }

    // Menü elemanlarına tıklama işlemleri
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.my_account -> {
                startActivity(Intent(this, AccountSettingsActivity::class.java))
                return true
            }
            R.id.my_flowers -> {
                startActivity(Intent(this, MyFlowersActivity::class.java))
                return true
            }
            R.id.cikis -> {
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

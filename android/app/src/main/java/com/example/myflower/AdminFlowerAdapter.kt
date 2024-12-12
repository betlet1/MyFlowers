package com.example.myflower

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myflower.model.Flower
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class AdminFlowerAdapter(private val flowerList: ArrayList<Flower>, private val context: Context) :
    RecyclerView.Adapter<AdminFlowerAdapter.FlowerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlowerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.iteem_admin_flower_card, parent, false)
        return FlowerViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlowerViewHolder, position: Int) {
        val flower = flowerList[position]
        holder.flowerName.text = flower.name

        // Picasso kütüphanesi ile URL'den resmi ImageView'a yükleyelim
        Picasso.get().load(flower.imageUrl).into(holder.flowerImage)

        holder.editFlowerButton.tag = flower.id
        holder.deleteFlowerButton.tag = flower.id

        holder.editFlowerButton.setOnClickListener {
            val flowerId = it.tag as String  // Edit butonunun tag'ini alıyoruz
            onEditClick(flowerId)
        }

        holder.deleteFlowerButton.setOnClickListener {
            val flowerId = it.tag as String
            onDeleteClick(flowerId)
        }
    }

    override fun getItemCount(): Int {
        return flowerList.size
    }

    private fun onEditClick(flowerId: String) {
        val flower = flowerList.find { it.id == flowerId }
        flower?.let {
            val intent = Intent(context, AdminFlowerEditActivity::class.java)
            intent.putExtra("flower_id", it.id)
            intent.putExtra("flower_name", it.name)
            intent.putExtra("flower_image", it.imageUrl)
            intent.putExtra("flower_description", it.description)
            context.startActivity(intent)
        } ?: run {
            Toast.makeText(context, "Çiçek bilgisi bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onDeleteClick(flowerId: String) {
        Log.d("DeleteFlower", "Silinecek çiçeğin ID'si: $flowerId")  // ID'yi logla
        val alertDialog = android.app.AlertDialog.Builder(context)
        alertDialog.setTitle("Silme Onayı")
        alertDialog.setMessage("Bu çiçeği silmek istediğinize emin misiniz?")
        alertDialog.setPositiveButton("Evet") { _, _ ->
            // Firebase veritabanından silme işlemi
            deleteFlowerFromDatabase(flowerId)
        }
        alertDialog.setNegativeButton("Hayır") { dialog, _ -> dialog.dismiss() }
        alertDialog.create().show()
    }

    private fun deleteFlowerFromDatabase(customId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Flowers")

        // Kendi ID'nizle veri sorgulama
        databaseReference.orderByChild("id").equalTo(customId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Snapshot içinde veriyi bulduk, şimdi Firebase'in otomatik ID'sine (key) erişelim
                    for (childSnapshot in snapshot.children) {
                        val firebaseId = childSnapshot.key // Bu Firebase'in otomatik oluşturduğu ID (key)
                        Log.d("DeleteFlower", "Firebase ID: $firebaseId")

                        // Şimdi bu Firebase ID'yi kullanarak veriyi silebiliriz
                        firebaseId?.let {
                            databaseReference.child(it).removeValue()
                                .addOnSuccessListener {
                                    // Silme başarılı
                                    Toast.makeText(context, "Çiçek başarıyla silindi.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    // Silme işlemi başarısız
                                    Toast.makeText(context, "Hata: ${exception.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                } else {
                    // Veri bulunamadı
                    Toast.makeText(context, "Çiçek bulunamadı.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumu
                Toast.makeText(context, "Veritabanı hatası: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    class FlowerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flowerImage: ImageView = itemView.findViewById(R.id.flowerImage)
        val flowerName: TextView = itemView.findViewById(R.id.flowerName)
        val editFlowerButton: Button = itemView.findViewById(R.id.editFlowerButton)
        val deleteFlowerButton: Button = itemView.findViewById(R.id.deleteFlowerButton)
    }
}


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
import com.google.firebase.database.FirebaseDatabase
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
            val flowerId = it.tag as String  // Butona atanan tag'i alıyoruz
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
            Log.d("EditClick", "Flower ID: ${it.id}, Name: ${it.name}, Image: ${it.imageUrl}")
            context.startActivity(intent)
        } ?: run {
            Toast.makeText(context, "Çiçek bilgisi bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun onDeleteClick(flowerId: String) {
        // Adminden silme işlemi onayı
        val alertDialog = android.app.AlertDialog.Builder(context)
        alertDialog.setTitle("Silme Onayı")
        alertDialog.setMessage("Bu çiçeği silmek istediğinize emin misiniz?")
        alertDialog.setPositiveButton("Evet") { _, _ ->
            // Firebase veritabanından silme işlemi
            deleteFlowerFromDatabase(flowerId)
        }
        alertDialog.setNegativeButton("Hayır") { dialog, _ ->
            dialog.dismiss() // Dialog'u kapat
        }
        alertDialog.create().show()
    }

    private fun deleteFlowerFromDatabase(flowerId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("flowers")
        databaseReference.child(flowerId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Çiçek başarıyla silindi.", Toast.LENGTH_SHORT).show()
                Log.d("DeleteFlower", "Çiçek silindi: $flowerId")
                flowerList.removeAll { it.id == flowerId }
                notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Hata: ${exception.message}", Toast.LENGTH_LONG).show()
                Log.e("DeleteFlower", "Silme hatası: ${exception.message}", exception)
            }
    }


    class FlowerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flowerImage: ImageView = itemView.findViewById(R.id.flowerImage)
        val flowerName: TextView = itemView.findViewById(R.id.flowerName)
        val editFlowerButton: Button = itemView.findViewById(R.id.editFlowerButton)
        val deleteFlowerButton: Button = itemView.findViewById(R.id.deleteFlowerButton)
    }
}

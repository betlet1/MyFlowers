package com.example.myflowers

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import com.example.myflowers.model.Flower
import com.squareup.picasso.Picasso // Resim yüklemek için Picasso kütüphanesini kullanıyoruz

class FlowerAdapter(private val flowerList: ArrayList<Flower>, private val context: Context) :
    RecyclerView.Adapter<FlowerAdapter.FlowerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlowerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_flower_card, parent, false)
        return FlowerViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlowerViewHolder, position: Int) {
        val flower = flowerList[position]
        holder.flowerName.text = flower.name

        // Picasso kütüphanesi ile URL'den resmi ImageView'a yükleme
        Picasso.get().load(flower.imageUrl).into(holder.flowerImage)

        holder.detailsButton.setOnClickListener {
            onDetailsClick(flower)
        }
    }

    override fun getItemCount(): Int {
        return flowerList.size
    }

    private fun onDetailsClick(flower: Flower) {
        val intent = Intent(context, FlowerDetailsActivity::class.java)
        intent.putExtra("flower_id", flower.id)
        intent.putExtra("flower_name", flower.name)
        intent.putExtra("flower_image", flower.imageUrl)
        intent.putExtra("flower_description", flower.description)
        context.startActivity(intent)
    }

    class FlowerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flowerImage: ImageView = itemView.findViewById(R.id.flowerImage)
        val flowerName: TextView = itemView.findViewById(R.id.flowerName)
        val detailsButton: Button = itemView.findViewById(R.id.detailsButton)
    }
}

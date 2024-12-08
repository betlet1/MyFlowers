package com.example.myflower


data class Flower(
    val id: String,
    var imageUrl: String,  // Cloudinary'den alınan URL
    val name: String,
    val description: String
)

package com.example.myflowers.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Flower(
    val id: String = "",
    var imageUrl: String = "",
    val name: String = "",
    val description: String = ""
) : Parcelable

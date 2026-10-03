package com.company.jifiit.Hotel.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServiceItemModel(
    val id: String,
    val title: String,
    val image: String,          // ← was: imageRes: Int
    val saveBadge: String,
    val optionsCount: Int,
    val price: String,
    val originalPrice: String,
    val discountPercent: String,
    val duration: String,
    val type: String = ""
) : Parcelable
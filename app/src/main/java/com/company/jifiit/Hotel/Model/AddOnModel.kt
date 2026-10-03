package com.company.jifiit.Hotel.Model

data class AddOnModel(
    val id: String,
    val title: String,
    val meta: String,
    val price: String,
    val originalPrice: String,
    val discountPercent: String,
    val saveBadge: String,
    val optionsCount: Int,
    val duration: String,
    val type: String,
    val image: String
)
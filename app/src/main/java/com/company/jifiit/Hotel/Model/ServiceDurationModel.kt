package com.company.jifiit.Hotel.Model

data class ServiceDurationModel(
    val id: String,
    val name: String,
    val duration: String,
    val originalPrice: Double,
    val discountPrice: Double,
    val discountPercent: Int,
    val type:String,
    val image: String
)
package com.company.jifiit.Hotel.Model

data class ServiceDetailsModel(
    val serviceId: String,
    val serviceType: String,
    val name: String,
    val description: String,
    val image: String,

    val cover: String,
    val technique: String,
    val pressure: String,
    val oil: String,
    val benefits: String,
    val recommendedFor: String,

    val options: List<ServiceDurationModel>
)
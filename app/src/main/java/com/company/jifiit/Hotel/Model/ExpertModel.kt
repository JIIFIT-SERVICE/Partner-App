package com.company.jifiit.Hotel.Model

data class ExpertModel(
    val id: String = "",
    val name: String = "",
    val employeeId: String = "",
    val experience: String = "",
    val image: String = "",

    // =====================================================
    // NEW FIELDS
    // =====================================================

    val role: String = "Massage Expert",
    val hometown: String = "",
    val lifetimeExp: String = "",
    val rating: String = "0.0",
    val ratingCount: String = "0",
    val availability: String = "Available for male client only"
)
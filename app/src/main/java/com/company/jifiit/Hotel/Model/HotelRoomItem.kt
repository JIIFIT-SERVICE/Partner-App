package com.company.jifiit.Hotel.Model

data class HotelRoomItem(
    val roomNumber: String,
    val roomType: String,
    val floor: Int,
    val isBooked: Boolean = false,
    val activeSessionText: String? = null
)

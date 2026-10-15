package com.company.jifiit.Hotel

data class BookingModel(
    val bookingId: String,
    val isInRoom: Boolean,
    val status: String,
    val customerName: String,
    val roomInfo: String,
    val date: String,
    val time: String,
    val room_number: String,
    val massage_types: String,
    val client_number: String,
    val expertName: String,
    val duration: String,
    val image: String = "",
    val EndTime: String,
    val ExpertId: String
)
package com.company.jifiit.Hotel

data class BookingModel(
    val bookingId: String,
    val isInRoom: Boolean,
    val status: String,
    val customerName: String,
    val roomInfo: String,
    val date: String,
    val time: String
)
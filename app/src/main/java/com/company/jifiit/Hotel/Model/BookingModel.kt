package com.company.jifiit.Hotel

data class BookingModel(
    val bookingId: String,
    val isInRoom: Boolean,
    val status: String,
    val customerName: String,
    val roomInfo: String,
    val date: String,
    val time: String,
    val serviceName: String = "In-Room Massage",
    val phone: String = "+91 98765 43210"
)
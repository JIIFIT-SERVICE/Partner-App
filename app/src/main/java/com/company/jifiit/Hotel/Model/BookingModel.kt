package com.company.jifiit.Hotel

data class BookingModel(
    val bookingId: String,
    val isInRoom: Boolean = true,
    val status: String,
    val customerName: String,
    val roomInfo: String,
    val date: String,
    val time: String,
    val room_number: String = "",
    val massage_types: String = "",
    val serviceName: String = "",
    val phone: String = "",
    val expertName: String = "",
    val expertPhoto: String = "",
    val commissionAmount: String = "₹100",
    val client_number: String = "",
    val duration: String = "",
    val image: String = "",
    val EndTime: String = ""
)
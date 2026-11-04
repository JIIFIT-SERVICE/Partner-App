package com.company.jifiit.Influencer

data class InfluencerBooking(
    val id: String,
    val customerName: String,
    val discountTag: String = "Saved ₹100 with coupon",
    val phoneNumber: String,
    val serviceName: String,
    val bookingDate: String,
    val location: String,
    val locationArea: String = "Bangalore",
    var rating: Float = 5.0f,
    var feedback: String = "",
    var paymentStatus: String = "Paid", // "Paid", "Pending", "Settled", "Cancelled"
    val commission: String = "+₹50",
    val dateCategory: String = "today" // "yesterday", "today", "tomorrow"
)

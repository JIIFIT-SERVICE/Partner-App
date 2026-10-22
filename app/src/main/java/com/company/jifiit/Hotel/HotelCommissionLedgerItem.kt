package com.company.jifiit.Hotel

data class HotelCommissionLedgerItem(
    val bookingId: String,
    val roomNumber: String,
    val customerName: String,
    val serviceName: String,
    val dateText: String,
    val bookingChannel: String,
    val amountText: String = "+₹500.00",
    val isSettled: Boolean
)

package com.example.driverappfrontend.network

data class CashPaymentBody(val amountPaise: Long, val note: String?)

data class Payment(
    val id: String,
    val bookingId: String,
    val payerId: String,
    val collectedBy: String,
    val amountPaise: Long,
    val method: String,
    val status: String, // COLLECTED | DISPUTED
    val note: String?,
    val collectedAt: String,
    val createdAt: String
)

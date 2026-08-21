package com.example.driverappfrontend.network

data class RateBody(val stars: Int, val comment: String?)

data class Rating(
    val id: String,
    val bookingId: String,
    val raterId: String,
    val rateeId: String,
    val stars: Int,
    val comment: String?,
    val createdAt: String
)

package com.example.driverappfrontend.network

data class OtpRequestBody(val phone: String)

data class OtpRequestResponse(val status: String)

data class OtpVerifyBody(
    val phone: String,
    val otp: String,
    val signupRole: String,
    val deviceId: String,
    val platform: String
)

data class RefreshBody(val refreshToken: String)

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
    val newUser: Boolean
)

/** Mirrors backend's ApiError: { code, message, fields, timestamp }. */
data class ApiError(
    val code: String?,
    val message: String?,
    val fields: Map<String, String>?,
    val timestamp: String?
)

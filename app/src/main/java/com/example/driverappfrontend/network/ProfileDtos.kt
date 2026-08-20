package com.example.driverappfrontend.network

/** Mirrors backend's User entity JSON (BaseEntity fields + user fields). */
data class UserProfile(
    val id: String,
    val createdAt: String,
    val updatedAt: String,
    val phoneE164: String,
    val fullName: String?,
    val email: String?,
    val role: String, // CUSTOMER | DRIVER | ADMIN
    val status: String, // ACTIVE | BLOCKED | DELETED
    val cityId: Int?,
    val photoUrl: String?,
    val ratingAvg: Double,
    val ratingCount: Int,
    val fcmToken: String?,
    val blockedReason: String?,
    val deletedAt: String?
)

data class UpdateProfileBody(
    val fullName: String? = null,
    val email: String? = null,
    val cityId: Int? = null,
    val photoUrl: String? = null,
    val fcmToken: String? = null
)

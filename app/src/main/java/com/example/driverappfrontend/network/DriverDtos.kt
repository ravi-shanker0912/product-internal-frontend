package com.example.driverappfrontend.network

data class CreateDriverProfileBody(
    val licenseNumber: String,
    val licenseExpiry: String?, // ISO-8601 date, e.g. "2030-01-01"
    val ownsVehicle: Boolean,
    val canDriveAutomatic: Boolean
)

data class AvailabilityBody(val status: String) // "OFFLINE" | "ONLINE" | "ON_TRIP"

data class LocationBody(
    val lat: Double,
    val lon: Double,
    val bearing: Float?
)

data class UploadDocumentBody(
    val docType: String, // DL_FRONT | DL_BACK | SELFIE | RC | INSURANCE | AADHAAR
    val fileUrl: String,
    val expiresAt: String?
)

/** Mirrors backend's DriverProfile entity JSON. */
data class DriverProfile(
    val userId: String,
    val licenseNumber: String,
    val licenseExpiry: String?,
    val ownsVehicle: Boolean,
    val canDriveAutomatic: Boolean,
    val verifyStatus: String, // PENDING | APPROVED | REJECTED
    val verifiedBy: String?,
    val verifiedAt: String?,
    val rejectReason: String?,
    val availability: String, // OFFLINE | ONLINE | ON_TRIP
    val totalTrips: Int
)

/** Mirrors backend's DriverDocument entity JSON. */
data class DriverDocument(
    val id: String,
    val driverId: String,
    val docType: String,
    val fileUrl: String,
    val expiresAt: String?,
    val uploadedAt: String
)

data class AddVehicleBody(
    val ownerType: String, // DRIVER | CUSTOMER
    val registrationNo: String?,
    val make: String,
    val model: String,
    val gearbox: String, // MANUAL | AUTOMATIC
    val seats: Short?,
    val insuranceExpiry: String?
)

/** Mirrors backend's Vehicle entity JSON. */
data class Vehicle(
    val id: String,
    val createdAt: String,
    val updatedAt: String,
    val ownerUserId: String,
    val ownerType: String,
    val registrationNo: String?,
    val make: String,
    val model: String,
    val gearbox: String,
    val seats: Short,
    val insuranceExpiry: String?,
    val active: Boolean
)

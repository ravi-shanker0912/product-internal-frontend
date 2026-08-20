package com.example.driverappfrontend.data

import com.example.driverappfrontend.network.AddVehicleBody
import com.example.driverappfrontend.network.AvailabilityBody
import com.example.driverappfrontend.network.CreateDriverProfileBody
import com.example.driverappfrontend.network.DriverApi
import com.example.driverappfrontend.network.DriverDocument
import com.example.driverappfrontend.network.DriverProfile
import com.example.driverappfrontend.network.LocationBody
import com.example.driverappfrontend.network.UploadDocumentBody
import com.example.driverappfrontend.network.Vehicle
import retrofit2.HttpException

class DriverRepository(private val api: DriverApi) {

    suspend fun getProfile(): Result<DriverProfile> = runCatching { api.getProfile() }

    suspend fun createProfile(
        licenseNumber: String,
        licenseExpiry: String?,
        ownsVehicle: Boolean,
        canDriveAutomatic: Boolean
    ): Result<DriverProfile> = runCatching {
        api.createProfile(CreateDriverProfileBody(licenseNumber, licenseExpiry, ownsVehicle, canDriveAutomatic))
    }

    suspend fun setAvailability(online: Boolean): Result<DriverProfile> = runCatching {
        api.setAvailability(AvailabilityBody(if (online) "ONLINE" else "OFFLINE"))
    }

    suspend fun pingLocation(lat: Double, lon: Double, bearing: Float?): Result<Unit> = runCatching {
        val response = api.pingLocation(LocationBody(lat, lon, bearing))
        if (!response.isSuccessful) throw HttpException(response)
    }

    suspend fun uploadDocument(
        docType: String,
        fileUrl: String,
        expiresAt: String?
    ): Result<DriverDocument> = runCatching {
        api.uploadDocument(UploadDocumentBody(docType, fileUrl, expiresAt))
    }

    suspend fun listDocuments(): Result<List<DriverDocument>> = runCatching { api.listDocuments() }

    suspend fun addVehicle(
        registrationNo: String?,
        make: String,
        model: String,
        gearbox: String,
        seats: Short?,
        insuranceExpiry: String?
    ): Result<Vehicle> = runCatching {
        api.addVehicle(
            AddVehicleBody(
                ownerType = "DRIVER",
                registrationNo = registrationNo,
                make = make,
                model = model,
                gearbox = gearbox,
                seats = seats,
                insuranceExpiry = insuranceExpiry
            )
        )
    }

    suspend fun listVehicles(): Result<List<Vehicle>> = runCatching { api.listVehicles() }
}

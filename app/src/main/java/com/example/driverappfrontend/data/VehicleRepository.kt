package com.example.driverappfrontend.data

import com.example.driverappfrontend.network.AddVehicleBody
import com.example.driverappfrontend.network.DriverApi
import com.example.driverappfrontend.network.Vehicle

/**
 * Vehicles registered by the currently logged-in user, regardless of role.
 * Backend's /api/me/vehicles is shared by both: a driver adds their own car
 * (ownerType DRIVER), a customer adds the car they want a driver for
 * (ownerType CUSTOMER). [ownerType] fixes which this instance manages.
 */
class VehicleRepository(private val api: DriverApi, private val ownerType: String) {

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
                ownerType = ownerType,
                registrationNo = registrationNo,
                make = make,
                model = model,
                gearbox = gearbox,
                seats = seats,
                insuranceExpiry = insuranceExpiry
            )
        )
    }

    suspend fun listVehicles(): Result<List<Vehicle>> = runCatching {
        api.listVehicles().filter { it.ownerType == ownerType }
    }
}

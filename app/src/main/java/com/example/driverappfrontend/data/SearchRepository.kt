package com.example.driverappfrontend.data

import com.example.driverappfrontend.network.NearbyDriver
import com.example.driverappfrontend.network.SearchApi

class SearchRepository(private val api: SearchApi) {

    suspend fun searchDrivers(
        lat: Double,
        lon: Double,
        serviceType: String,
        automaticOnly: Boolean
    ): Result<List<NearbyDriver>> = runCatching {
        api.searchDrivers(lat = lat, lon = lon, radiusKm = null, serviceType = serviceType, automaticOnly = automaticOnly)
    }
}

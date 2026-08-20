package com.example.driverappfrontend.network

import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi {

    @GET("api/search/drivers")
    suspend fun searchDrivers(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radiusKm") radiusKm: Double?,
        @Query("serviceType") serviceType: String, // WITH_CAR | WITHOUT_CAR
        @Query("automaticOnly") automaticOnly: Boolean
    ): List<NearbyDriver>
}

package com.example.driverappfrontend.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface DriverApi {

    @GET("api/me/driver")
    suspend fun getProfile(): DriverProfile

    @POST("api/me/driver")
    suspend fun createProfile(@Body body: CreateDriverProfileBody): DriverProfile

    @POST("api/me/driver/availability")
    suspend fun setAvailability(@Body body: AvailabilityBody): DriverProfile

    @POST("api/me/driver/location")
    suspend fun pingLocation(@Body body: LocationBody): Response<Unit>

    @POST("api/me/driver/documents")
    suspend fun uploadDocument(@Body body: UploadDocumentBody): DriverDocument

    @GET("api/me/driver/documents")
    suspend fun listDocuments(): List<DriverDocument>

    @POST("api/me/vehicles")
    suspend fun addVehicle(@Body body: AddVehicleBody): Vehicle

    @GET("api/me/vehicles")
    suspend fun listVehicles(): List<Vehicle>
}

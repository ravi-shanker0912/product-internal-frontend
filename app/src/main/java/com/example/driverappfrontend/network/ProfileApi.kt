package com.example.driverappfrontend.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface ProfileApi {

    @GET("api/me")
    suspend fun getProfile(): UserProfile

    @PATCH("api/me")
    suspend fun updateProfile(@Body body: UpdateProfileBody): UserProfile
}

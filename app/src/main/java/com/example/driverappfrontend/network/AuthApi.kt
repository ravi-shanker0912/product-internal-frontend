package com.example.driverappfrontend.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/otp/request")
    suspend fun requestOtp(@Body body: OtpRequestBody): OtpRequestResponse

    @POST("api/auth/otp/verify")
    suspend fun verifyOtp(@Body body: OtpVerifyBody): TokenPair

    @POST("api/auth/refresh")
    suspend fun refresh(@Body body: RefreshBody): TokenPair

    @POST("api/auth/logout")
    suspend fun logout(@Body body: RefreshBody): Response<Unit>
}

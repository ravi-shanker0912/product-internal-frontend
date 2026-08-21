package com.example.driverappfrontend.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BookingApi {

    @POST("api/bookings")
    suspend fun create(@Body body: CreateBookingBody): Booking

    @GET("api/bookings")
    suspend fun mine(@Query("as") asRole: String): List<Booking>

    @GET("api/bookings/{id}")
    suspend fun one(@Path("id") id: String): Booking

    @GET("api/bookings/{id}/timeline")
    suspend fun timeline(@Path("id") id: String): List<BookingStatusHistory>

    @POST("api/bookings/{id}/accept")
    suspend fun accept(@Path("id") id: String, @Body at: LocationStamp): AcceptResponse

    @POST("api/bookings/{id}/arrived")
    suspend fun arrived(@Path("id") id: String, @Body at: LocationStamp): Booking

    @POST("api/bookings/{id}/start")
    suspend fun start(@Path("id") id: String, @Body body: StartBody): Booking

    @POST("api/bookings/{id}/complete")
    suspend fun complete(@Path("id") id: String, @Body body: CompleteBody): Booking

    @POST("api/bookings/{id}/cancel")
    suspend fun cancel(@Path("id") id: String, @Body body: CancelBody): Booking

    @POST("api/bookings/{id}/rate")
    suspend fun rate(@Path("id") id: String, @Body body: RateBody): Rating

    @POST("api/bookings/{id}/payment/cash")
    suspend fun recordCashPayment(@Path("id") id: String, @Body body: CashPaymentBody): Payment
}

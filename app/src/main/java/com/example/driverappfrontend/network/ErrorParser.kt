package com.example.driverappfrontend.network

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

/** Turns a failed API call into the message the user should see. */
object ErrorParser {

    private val gson = Gson()

    fun extractMessage(t: Throwable): String {
        return when (t) {
            is HttpException -> {
                val body = t.response()?.errorBody()?.string()
                val apiError = body?.let {
                    runCatching { gson.fromJson(it, ApiError::class.java) }.getOrNull()
                }
                apiError?.message ?: "Something went wrong (HTTP ${t.code()})"
            }
            is IOException -> "Can't reach the server. Check your connection and try again."
            else -> t.message ?: "Something went wrong"
        }
    }
}

package com.example.driverappfrontend.data

import android.content.Context
import android.provider.Settings
import com.example.driverappfrontend.network.AuthApi
import com.example.driverappfrontend.network.OtpRequestBody
import com.example.driverappfrontend.network.OtpVerifyBody
import com.example.driverappfrontend.network.RefreshBody
import com.example.driverappfrontend.network.TokenPair

class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
    private val context: Context
) {

    private val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown-device"
    }

    suspend fun requestOtp(phone: String): Result<Unit> = runCatching {
        api.requestOtp(OtpRequestBody(phone))
        Unit
    }

    suspend fun verifyOtp(phone: String, otp: String): Result<TokenPair> = runCatching {
        val tokens = api.verifyOtp(
            OtpVerifyBody(
                phone = phone,
                otp = otp,
                signupRole = "CUSTOMER",
                deviceId = deviceId,
                platform = "ANDROID"
            )
        )
        tokenStore.saveTokens(tokens.accessToken, tokens.refreshToken)
        tokens
    }

    /** Best-effort server-side logout; local tokens are always cleared regardless. */
    suspend fun logout() {
        val refreshToken = tokenStore.getRefreshToken()
        if (refreshToken != null) {
            runCatching { api.logout(RefreshBody(refreshToken)) }
        }
        tokenStore.clear()
    }
}

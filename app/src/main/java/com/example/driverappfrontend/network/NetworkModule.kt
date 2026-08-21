package com.example.driverappfrontend.network

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 10.0.2.2 is the Android emulator's alias for the host machine's localhost.
 * Only valid when testing from the emulator against a locally running backend.
 */
object NetworkModule {

    private const val BASE_URL = "http://192.168.1.12:8080/"

    /** Wired up by the app's composition root. */
    var accessTokenProvider: (() -> String?)? = null
    var refreshTokenProvider: (() -> String?)? = null
    var onTokensRefreshed: ((accessToken: String, refreshToken: String) -> Unit)? = null
    var onRefreshFailed: (() -> Unit)? = null

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = accessTokenProvider?.invoke()
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            original
        }
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /** Bare client with no auth header/authenticator — used only to call /api/auth/refresh, to avoid recursing. */
    private val plainOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val plainRetrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(plainOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val plainAuthApi: AuthApi = plainRetrofit.create(AuthApi::class.java)

    /**
     * The backend rotates the refresh token on every use and treats replay of an
     * already-consumed one as a compromise signal — it revokes every session for
     * that user (see AuthService.refresh). Two requests 401-ing around the same
     * moment must not both call /api/auth/refresh with the same stale token, or
     * the loser's call reads as a replay and silently logs the user out
     * everywhere. This lock makes refresh single-flight: whoever gets the lock
     * first refreshes; whoever else was waiting notices the token already
     * changed and just reuses it instead of refreshing again.
     */
    private val refreshLock = Any()

    private val tokenAuthenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            if (responseCount(response) >= 2) return null // already retried once — give up

            synchronized(refreshLock) {
                val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                val currentToken = accessTokenProvider?.invoke()
                if (!currentToken.isNullOrBlank() && currentToken != failedToken) {
                    // Another thread already refreshed while we were waiting for the lock.
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                }

                val refreshToken = refreshTokenProvider?.invoke() ?: return null

                val newTokens = try {
                    runBlocking { plainAuthApi.refresh(RefreshBody(refreshToken)) }
                } catch (e: Exception) {
                    null
                }

                if (newTokens == null) {
                    onRefreshFailed?.invoke()
                    return null
                }

                onTokensRefreshed?.invoke(newTokens.accessToken, newTokens.refreshToken)
                return response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken}")
                    .build()
            }
        }

        private fun responseCount(response: Response): Int {
            var count = 1
            var prior = response.priorResponse
            while (prior != null) {
                count++
                prior = prior.priorResponse
            }
            return count
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenAuthenticator)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val driverApi: DriverApi = retrofit.create(DriverApi::class.java)
    val profileApi: ProfileApi = retrofit.create(ProfileApi::class.java)
    val searchApi: SearchApi = retrofit.create(SearchApi::class.java)
    val bookingApi: BookingApi = retrofit.create(BookingApi::class.java)
}

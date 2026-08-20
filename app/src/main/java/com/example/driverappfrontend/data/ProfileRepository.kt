package com.example.driverappfrontend.data

import com.example.driverappfrontend.network.ProfileApi
import com.example.driverappfrontend.network.UpdateProfileBody
import com.example.driverappfrontend.network.UserProfile

class ProfileRepository(private val api: ProfileApi) {

    suspend fun getProfile(): Result<UserProfile> = runCatching { api.getProfile() }

    suspend fun updateProfile(fullName: String?, email: String?): Result<UserProfile> = runCatching {
        api.updateProfile(UpdateProfileBody(fullName = fullName, email = email))
    }
}

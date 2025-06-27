package org.easydictionary.app.data.models.auth

import com.google.gson.annotations.SerializedName
import org.easydictionary.app.domain.models.auth.Auth

data class AuthResponse(
    @SerializedName("AccessToken") val accessToken: String,
    @SerializedName("RefreshToken") val refreshToken: String,
    @SerializedName("RefreshTokenExp") val refreshTokenExp: String
) {
    fun toDomain(): Auth {
        return Auth(
            accessToken = accessToken,
            refreshToken = refreshToken,
            refreshTokenExp = refreshTokenExp
        )
    }
}
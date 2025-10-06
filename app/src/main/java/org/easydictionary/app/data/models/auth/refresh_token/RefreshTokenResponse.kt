package org.easydictionary.app.data.models.auth.refresh_token

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("AccessToken") val accessToken: String,
    @SerializedName("RefreshToken") val refreshToken: String,
    @SerializedName("RefreshTokenExp") val refreshTokenExp: String
)

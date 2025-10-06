package org.easydictionary.app.data.models.auth.refresh_token

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequest(
    @SerializedName("RefreshToken") val refreshToken: String
)
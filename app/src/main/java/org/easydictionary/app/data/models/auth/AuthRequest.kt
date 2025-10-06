package org.easydictionary.app.data.models.auth

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    @SerializedName("email") val email: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("provider") val provider: String,
    @SerializedName("provider_token") val providerToken: String?
)
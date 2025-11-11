package org.easydictionary.app.data.models.signup

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("provider") val provider: String,
    @SerializedName("provider_token") val providerToken: String?
)
package org.easydictionary.app.data.models

import com.google.gson.annotations.SerializedName

data class CreatedResponse(
    @SerializedName("code")
    val code: Int?,
    @SerializedName("message")
    val message: String?
)
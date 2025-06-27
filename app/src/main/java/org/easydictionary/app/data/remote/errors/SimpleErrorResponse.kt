package org.easydictionary.app.data.remote.errors

import com.google.gson.annotations.SerializedName

data class SimpleErrorResponse(
    @SerializedName("code")
    val code: Int?,
    @SerializedName("message")
    val message: String?
)
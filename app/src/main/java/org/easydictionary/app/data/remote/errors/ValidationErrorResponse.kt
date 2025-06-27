package org.easydictionary.app.data.remote.errors

import com.google.gson.annotations.SerializedName

data class ValidationErrorResponse(
    @SerializedName("validation_errors")
    val validationErrors: Map<String, String>?
)
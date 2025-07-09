package org.easydictionary.app.data.remote.errors

import com.google.gson.Gson
import okhttp3.ResponseBody

object ErrorParser {
    private val gson = Gson()

    fun parse(errorBody: ResponseBody?): String {
        return parse(errorBody?.string() ?: return "Unknown error")
    }

    fun parse(errorBody: String?): String {
        return try {
            val json = errorBody ?: return "Unknown error"

            val validation = gson.fromJson(json, ValidationErrorResponse::class.java)
            validation.validationErrors?.entries?.joinToString("\n") {
                "${it.key}: ${it.value}"
            } ?: run {
                val simple = gson.fromJson(json, SimpleErrorResponse::class.java)
                simple.message ?: "Unknown error"
            }
        } catch (e: Exception) {
            "Failed to parse error: ${e.message}"
        }
    }
}


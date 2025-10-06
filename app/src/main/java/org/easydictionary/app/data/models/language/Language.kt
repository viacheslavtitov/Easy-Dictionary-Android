package org.easydictionary.app.data.models.language

import com.google.gson.annotations.SerializedName
import org.easydictionary.app.domain.models.language.Flag
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.models.language.LanguageListItem

data class LanguageRequest(
    val name: String,
    val code: String?
)
data class LanguageResponse(
    val id: Int,
    val name: String,
    val code: String? = null
) {
    fun toDomain(): Language {
        return Language(
            id = id,
            name = name,
            code = code
        )
    }
}

data class LanguageListResponse(
    @SerializedName("flags") val flags: FlagResponse,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("id") val id: Int?
) {
    fun toDomain(): LanguageListItem {
        return LanguageListItem(
            flags = Flag(
                png = flags.png,
                svg = flags.svg
            ),
            code = code,
            name = name,
            id = id
        )
    }
}

data class FlagResponse(
    @SerializedName("png") val png: String,
    @SerializedName("svg") val svg: String
)
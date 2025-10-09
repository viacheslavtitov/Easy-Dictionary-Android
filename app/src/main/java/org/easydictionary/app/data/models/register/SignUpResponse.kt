package org.easydictionary.app.data.models.register

import com.google.gson.annotations.SerializedName
import org.easydictionary.app.domain.models.users.Provider
import org.easydictionary.app.domain.models.users.User

data class SignUpResponse(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("uuid") val uuid: String,
    @SerializedName("providers") val providers: List<ProviderResponse>,
) {
    fun toDomain(): User {
        return User(
            firstName = firstName,
            lastName = lastName,
            uuid = uuid,
            providers = providers.map { it.toDomain() }
        )
    }
}

data class ProviderResponse(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("providerToken") val providerToken: String?,
    @SerializedName("id") val id: Int
) {
    fun toDomain(): Provider {
        return Provider(
            email = email,
            name = name,
            providerToken = providerToken,
            id = id
        )
    }
}
package org.easydictionary.app.domain.models.users

data class Provider(
    val email: String,
    val id: Int,
    val name: String,
    val providerToken: String?,
)

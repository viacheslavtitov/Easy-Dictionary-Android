package org.easydictionary.app.domain.models.users

data class User(
    val uuid: String,
    val firstName: String,
    val lastName: String,
    val providers: List<Provider>
)
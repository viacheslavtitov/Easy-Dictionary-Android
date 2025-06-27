package org.easydictionary.app.domain.models.auth

data class Auth(
    val accessToken: String,
    val refreshToken: String,
    val refreshTokenExp: String
)
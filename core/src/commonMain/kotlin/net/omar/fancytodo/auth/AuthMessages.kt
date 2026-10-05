package net.omar.fancytodo.auth

import kotlinx.serialization.Serializable

// Bodies of the /auth requests and responses.

@Serializable
data class RegisterRequest(val name: String, val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

/** Returned after logging in or creating an account; the app sends [token] with every request. */
@Serializable
data class AuthResponse(val token: String, val user: UserProfile)

const val MIN_PASSWORD_LENGTH = 8

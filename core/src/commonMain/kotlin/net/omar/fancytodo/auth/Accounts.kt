package net.omar.fancytodo.auth

import kotlinx.serialization.Serializable

/** The public part of an account: what the app is allowed to see. */
@Serializable
data class UserProfile(val id: String, val email: String, val name: String)

/** An account as the server stores it. Never sent to the app (it holds the password hash). */
@Serializable
data class UserAccount(
    val id: String,
    /** Lowercased; unique across accounts. */
    val email: String,
    val name: String,
    /** bcrypt hash of the password; the password itself is never stored. */
    val passwordHash: String
) {
    val profile: UserProfile get() = UserProfile(id, email, name)
}

/** A logged-in device. Only a hash of the token is stored, so a leaked file can't be used to log in. */
@Serializable
data class Session(val tokenHash: String, val userId: String, val expiresAtMillis: Long)

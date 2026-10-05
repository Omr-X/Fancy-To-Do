package net.omar.fancytodo.routes

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal
import net.omar.fancytodo.storage.GUEST_ID
import net.omar.fancytodo.storage.UserData
import net.omar.fancytodo.storage.dataOf

/** Name of the bearer-token authentication set up in Application.kt. */
const val SESSION_AUTH = "session"

/** Id of the logged-in user, or null for a guest. */
fun ApplicationCall.loggedInUserId(): String? = principal<UserIdPrincipal>()?.name

/** The logged-in user's data, or the shared guest data when nobody is logged in. */
fun ApplicationCall.userData(): UserData = dataOf(loggedInUserId() ?: GUEST_ID)

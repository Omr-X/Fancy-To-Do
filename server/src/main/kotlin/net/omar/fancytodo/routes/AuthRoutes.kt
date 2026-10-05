package net.omar.fancytodo.routes

import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import net.omar.fancytodo.auth.AuthResponse
import net.omar.fancytodo.auth.LoginRequest
import net.omar.fancytodo.auth.MIN_PASSWORD_LENGTH
import net.omar.fancytodo.auth.RegisterRequest
import net.omar.fancytodo.storage.accountById
import net.omar.fancytodo.storage.accountCount
import net.omar.fancytodo.storage.checkCredentials
import net.omar.fancytodo.storage.createAccount
import net.omar.fancytodo.storage.createSession
import net.omar.fancytodo.storage.deleteSession
import net.omar.fancytodo.storage.moveGuestDataTo

private val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

/** Creating an account and logging in; open to everyone (but rate limited in Application.kt). */
fun Route.authRoutes() {
    post("/auth/register") {
        val request = call.receive<RegisterRequest>()
        val problem = when {
            request.name.isBlank() -> "Enter your name."
            !EMAIL_PATTERN.matches(request.email.trim()) -> "Enter a valid email address."
            request.password.length < MIN_PASSWORD_LENGTH -> "Use at least $MIN_PASSWORD_LENGTH characters for your password."
            else -> null
        }
        if (problem != null) return@post call.respondText(problem, status = HttpStatusCode.BadRequest)

        val isFirstAccount = accountCount() == 0
        val account = createAccount(request.name, request.email, request.password)
            ?: return@post call.respondText("An account with this email already exists.", status = HttpStatusCode.Conflict)
        // What was made before any account existed belongs to whoever sets the app up first.
        if (isFirstAccount) moveGuestDataTo(account.id)
        call.respond(HttpStatusCode.Created, AuthResponse(createSession(account.id), account.profile))
    }

    post("/auth/login") {
        val request = call.receive<LoginRequest>()
        // Same message whether the email or the password is wrong, so it doesn't reveal which emails have accounts.
        val account = checkCredentials(request.email, request.password)
            ?: return@post call.respondText("Email or password is incorrect.", status = HttpStatusCode.Unauthorized)
        call.respond(AuthResponse(createSession(account.id), account.profile))
    }
}

/** Who is logged in, and logging out; only for authenticated requests. */
fun Route.sessionRoutes() {
    get("/auth/me") {
        val account = call.loggedInUserId()?.let { accountById(it) } ?: return@get call.respond(HttpStatusCode.Unauthorized)
        call.respond(account.profile)
    }

    post("/auth/logout") {
        val header = call.request.parseAuthorizationHeader() as? HttpAuthHeader.Single
        header?.blob?.let { deleteSession(it) }
        call.respond(HttpStatusCode.NoContent)
    }
}

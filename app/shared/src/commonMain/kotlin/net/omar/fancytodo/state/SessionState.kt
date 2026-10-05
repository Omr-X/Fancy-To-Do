package net.omar.fancytodo.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.omar.fancytodo.auth.AuthResponse
import net.omar.fancytodo.auth.LoginRequest
import net.omar.fancytodo.auth.MIN_PASSWORD_LENGTH
import net.omar.fancytodo.auth.RegisterRequest
import net.omar.fancytodo.auth.UserProfile
import net.omar.fancytodo.platform.clearToken
import net.omar.fancytodo.platform.loadToken
import net.omar.fancytodo.platform.saveToken

private const val UNREACHABLE = "Can't reach the server. Check your connection and try again."

/** Who is logged in. The token is kept in the browser so a reload keeps you logged in. */
internal class SessionState(private val scope: CoroutineScope) {
    private val client = createApiClient()

    var user by mutableStateOf<UserProfile?>(null)
        private set
    var token by mutableStateOf<String?>(null)
        private set
    /** True until the saved token (if any) has been checked with the server. */
    var checking by mutableStateOf(true)
        private set
    /** A log in / sign up request is in progress. */
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    /** Logs back in with the token saved by an earlier visit. */
    fun restore() {
        scope.launch {
            val saved = loadToken()
            if (saved != null) {
                try {
                    val response = client.get("auth/me") { bearerAuth(saved) }
                    when {
                        response.status.isSuccess() -> {
                            user = response.body<UserProfile>()
                            token = saved
                        }
                        response.status == HttpStatusCode.Unauthorized -> clearToken()
                        else -> error = UNREACHABLE
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    // Keep the token: the server may just be down for a moment.
                    error = UNREACHABLE
                }
            }
            checking = false
        }
    }

    fun logIn(email: String, password: String) {
        if (email.isBlank() || password.isEmpty()) {
            error = "Enter your email and password."
            return
        }
        submit("auth/login", LoginRequest(email.trim(), password))
    }

    fun createAccount(name: String, email: String, password: String) {
        error = when {
            name.isBlank() -> "Enter your name."
            !email.contains('@') -> "Enter a valid email address."
            password.length < MIN_PASSWORD_LENGTH -> "Use at least $MIN_PASSWORD_LENGTH characters for your password."
            else -> null
        }
        if (error == null) submit("auth/register", RegisterRequest(name.trim(), email.trim(), password))
    }

    fun logOut() {
        val current = token
        endSession()
        // Tell the server so the token stops working everywhere; nothing to do if that fails.
        if (current != null) scope.launch { runCatching { client.post("auth/logout") { bearerAuth(current) } } }
    }

    /** The server stopped accepting the token (expired, or logged out on another device). */
    fun onSessionExpired() {
        endSession()
        error = "Your session ended. Please log in again."
    }

    fun clearError() {
        error = null
    }

    private inline fun <reified T> submit(path: String, body: T) {
        if (busy) return
        busy = true
        error = null
        scope.launch {
            try {
                val response = client.post(path) { jsonBody(body) }
                when {
                    response.status.isSuccess() -> {
                        val auth = response.body<AuthResponse>()
                        saveToken(auth.token)
                        token = auth.token
                        user = auth.user
                    }
                    response.status == HttpStatusCode.TooManyRequests -> error = "Too many attempts. Wait a minute and try again."
                    else -> error = response.bodyAsText().ifBlank { "Something went wrong (${response.status.value})." }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                error = UNREACHABLE
            }
            busy = false
        }
    }

    private fun endSession() {
        clearToken()
        token = null
        user = null
    }
}
